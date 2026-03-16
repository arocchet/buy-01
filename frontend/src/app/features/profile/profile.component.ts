import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { ProductService } from '../../core/services/product.service';
import { User } from '../../shared/models/user.model';
import { ClientStats, SellerStats } from '../../shared/models/order.model';
import { Product } from '../../shared/models/product.model';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="profile-page">
      <div class="container">
        <header class="page-header">
          <h1 class="page-title">My Profile</h1>
        </header>

        @if (loading()) {
          <div class="loading-container"><div class="spinner"></div><p>Loading profile...</p></div>
        } @else {
          <div class="profile-layout">

            <!-- Profile card -->
            <div class="profile-card card">
              <div class="avatar-section">
                @if (user()?.avatar) {
                  <img [src]="user()!.avatar" alt="avatar" class="avatar-img" />
                } @else {
                  <div class="avatar-placeholder">{{ user()?.name?.charAt(0)?.toUpperCase() }}</div>
                }
              </div>

              @if (!editing()) {
                <div class="profile-info">
                  <h2>{{ user()?.name }}</h2>
                  <p class="user-email">{{ user()?.email }}</p>
                  <span class="role-badge" [class]="'role-' + user()?.role">{{ user()?.role }}</span>
                  <button class="btn btn-secondary mt-md" (click)="startEdit()">Edit Profile</button>
                </div>
              } @else {
                <div class="edit-form">
                  <div class="form-group">
                    <label>Name</label>
                    <input type="text" class="form-control" [(ngModel)]="editName" />
                  </div>
                  <div class="form-group">
                    <label>Email</label>
                    <input type="email" class="form-control" [(ngModel)]="editEmail" />
                  </div>
                  @if (saveError()) {
                    <p class="error-msg">{{ saveError() }}</p>
                  }
                  <div class="edit-actions">
                    <button class="btn btn-primary" (click)="saveProfile()" [disabled]="saving()">
                      {{ saving() ? 'Saving...' : 'Save' }}
                    </button>
                    <button class="btn btn-secondary" (click)="cancelEdit()">Cancel</button>
                  </div>
                </div>
              }
            </div>

            <!-- Stats -->
            <div class="stats-section">
              @if (!isSeller()) {
                <!-- Client stats -->
                @if (clientStats()) {
                  <div class="stats-grid">
                    <div class="stat-card card">
                      <div class="stat-icon">📦</div>
                      <div class="stat-value">{{ clientStats()!.totalOrders }}</div>
                      <div class="stat-label">Total Orders</div>
                    </div>
                    <div class="stat-card card">
                      <div class="stat-icon">💰</div>
                      <div class="stat-value">\${{ clientStats()!.totalSpent.toFixed(2) }}</div>
                      <div class="stat-label">Total Spent</div>
                    </div>
                  </div>
                  @if (clientStats()!.mostBoughtProducts.length > 0) {
                    <div class="card top-products">
                      <h3>Most Bought Products</h3>
                      <ul class="product-list">
                        @for (item of clientStats()!.mostBoughtProducts; track item.productId; let i = $index) {
                          <li>
                            <span class="rank">#{{ i + 1 }}</span>
                            <span class="product-name-text">{{ getProductName(item.productId) }}</span>
                            <span class="qty-badge">× {{ item.totalQuantity }}</span>
                          </li>
                        }
                      </ul>
                    </div>
                  }
                }
              } @else {
                <!-- Seller stats -->
                @if (sellerStats()) {
                  <div class="stats-grid">
                    <div class="stat-card card">
                      <div class="stat-icon">🛒</div>
                      <div class="stat-value">{{ sellerStats()!.totalOrdersReceived }}</div>
                      <div class="stat-label">Orders Received</div>
                    </div>
                    <div class="stat-card card">
                      <div class="stat-icon">💵</div>
                      <div class="stat-value">\${{ sellerStats()!.totalRevenue.toFixed(2) }}</div>
                      <div class="stat-label">Total Revenue</div>
                    </div>
                  </div>
                  @if (sellerStats()!.bestSellingProducts.length > 0) {
                    <div class="card top-products">
                      <h3>Best-Selling Products</h3>
                      <ul class="product-list">
                        @for (item of sellerStats()!.bestSellingProducts; track item.productId; let i = $index) {
                          <li>
                            <span class="rank">#{{ i + 1 }}</span>
                            <span class="product-name-text">{{ getProductName(item.productId) }}</span>
                            <span class="qty-badge">{{ item.totalSold }} sold</span>
                          </li>
                        }
                      </ul>
                    </div>
                  }
                }
              }

              <div class="quick-links card">
                <h3>Quick Links</h3>
                @if (!isSeller()) {
                  <a routerLink="/orders" class="quick-link">📋 My Orders</a>
                  <a routerLink="/cart" class="quick-link">🛒 My Cart</a>
                } @else {
                  <a routerLink="/orders" class="quick-link">📋 Customer Orders</a>
                  <a routerLink="/seller/dashboard" class="quick-link">📊 Seller Dashboard</a>
                }
              </div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
    styles: [`
    .profile-page { padding: 2rem 0; }
    .page-header { margin-bottom: 2rem; }
    .page-title {
      font-size: 2rem;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .profile-layout {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 2rem;
      align-items: start;
    }
    @media (max-width: 768px) {
      .profile-layout { grid-template-columns: 1fr; }
    }
    .profile-card { padding: 2rem; text-align: center; }
    .avatar-img {
      width: 100px; height: 100px;
      border-radius: 50%; object-fit: cover;
      border: 3px solid var(--primary);
      margin-bottom: 1rem;
    }
    .avatar-placeholder {
      width: 100px; height: 100px;
      border-radius: 50%;
      background: var(--gradient-primary);
      display: flex; align-items: center; justify-content: center;
      font-size: 2.5rem; font-weight: 700; color: white;
      margin: 0 auto 1rem;
    }
    .profile-info h2 { margin-bottom: 0.25rem; }
    .user-email { color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 0.75rem; }
    .role-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: capitalize;
    }
    .role-client { background: rgba(59,130,246,0.15); color: #3b82f6; }
    .role-seller { background: rgba(34,197,94,0.15); color: #22c55e; }
    .mt-md { margin-top: 1rem; }
    .edit-form { text-align: left; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; font-weight: 600; font-size: 0.9rem; margin-bottom: 0.4rem; }
    .form-control {
      width: 100%;
      padding: 0.6rem 0.75rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      box-sizing: border-box;
    }
    .edit-actions { display: flex; gap: 0.75rem; margin-top: 1rem; }
    .error-msg { color: #ef4444; font-size: 0.9rem; }
    .stats-section { display: flex; flex-direction: column; gap: 1.5rem; }
    .stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; }
    .stat-card { padding: 1.5rem; text-align: center; }
    .stat-icon { font-size: 2rem; margin-bottom: 0.5rem; }
    .stat-value { font-size: 1.75rem; font-weight: 700; color: var(--primary); }
    .stat-label { color: var(--text-secondary); font-size: 0.9rem; margin-top: 0.25rem; }
    .top-products { padding: 1.5rem; }
    .top-products h3 { margin-bottom: 1rem; font-size: 1rem; }
    .product-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.6rem; }
    .product-list li {
      display: flex; align-items: center; gap: 0.75rem;
      background: var(--surface); padding: 0.6rem 0.75rem; border-radius: 0.4rem;
    }
    .rank { font-weight: 700; color: var(--primary); min-width: 24px; }
    .product-name-text { flex: 1; }
    .qty-badge {
      background: rgba(99,102,241,0.1); color: var(--primary);
      padding: 0.2rem 0.5rem; border-radius: 0.3rem; font-size: 0.8rem; font-weight: 600;
    }
    .quick-links { padding: 1.5rem; }
    .quick-links h3 { margin-bottom: 1rem; font-size: 1rem; }
    .quick-link {
      display: block;
      padding: 0.6rem 0.75rem;
      margin-bottom: 0.5rem;
      background: var(--surface);
      border-radius: 0.4rem;
      text-decoration: none;
      color: var(--text-primary);
      transition: background 0.2s;
    }
    .quick-link:hover { background: rgba(99,102,241,0.1); color: var(--primary); }
    .loading-container { display:flex; flex-direction:column; align-items:center; padding:4rem; }
  `]
})
export class ProfileComponent implements OnInit {
    private userService = inject(UserService);
    private orderService = inject(OrderService);
    private productService = inject(ProductService);
    private authService = inject(AuthService);

    isSeller = this.authService.isSeller;

    user = signal<User | null>(null);
    clientStats = signal<ClientStats | null>(null);
    sellerStats = signal<SellerStats | null>(null);
    loading = signal(true);
    editing = signal(false);
    saving = signal(false);
    saveError = signal('');

    editName = '';
    editEmail = '';

    private productNames = new Map<string, string>();

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        const statsObs$ = this.isSeller()
            ? this.orderService.getSellerStats()
            : this.orderService.getClientStats();

        forkJoin({
            user: this.userService.getMyProfile(),
            stats: statsObs$
        }).subscribe({
            next: ({ user, stats }) => {
                this.user.set(user);
                if (this.isSeller()) {
                    this.sellerStats.set(stats as SellerStats);
                    this.loadProductNames((stats as SellerStats).bestSellingProducts.map(p => p.productId));
                } else {
                    this.clientStats.set(stats as ClientStats);
                    this.loadProductNames((stats as ClientStats).mostBoughtProducts.map(p => p.productId));
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    loadProductNames(ids: string[]) {
        ids.forEach(id => {
            if (!this.productNames.has(id)) {
                this.productService.getProduct(id).subscribe({
                    next: p => this.productNames.set(id, p.name),
                    error: () => this.productNames.set(id, id.slice(0, 8) + '...')
                });
            }
        });
    }

    getProductName(id: string): string {
        return this.productNames.get(id) ?? id.slice(0, 8) + '...';
    }

    startEdit() {
        this.editName = this.user()?.name ?? '';
        this.editEmail = this.user()?.email ?? '';
        this.saveError.set('');
        this.editing.set(true);
    }

    cancelEdit() {
        this.editing.set(false);
    }

    saveProfile() {
        this.saving.set(true);
        this.saveError.set('');
        this.userService.updateMyProfile({ name: this.editName, email: this.editEmail }).subscribe({
            next: updated => {
                this.user.set(updated);
                this.editing.set(false);
                this.saving.set(false);
            },
            error: err => {
                this.saving.set(false);
                this.saveError.set(err?.error?.message || 'Failed to save profile');
            }
        });
    }
}
