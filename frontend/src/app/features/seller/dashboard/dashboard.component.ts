import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../shared/models/product.model';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
    <div class="dashboard-page">
      <div class="container">
        <header class="dashboard-header">
          <div class="header-left">
            <h1>Seller Dashboard</h1>
            <p>Welcome back, {{ authService.currentUser()?.name }}</p>
          </div>
          <div class="header-right">
            <a routerLink="/seller/products/new" class="btn btn-primary">
              <span>+</span> Add New Product
            </a>
          </div>
        </header>

        <div class="dashboard-stats">
          <div class="stat-card card">
            <div class="stat-icon">📦</div>
            <div class="stat-content">
              <span class="stat-value">{{ products().length }}</span>
              <span class="stat-label">Total Products</span>
            </div>
          </div>
          <div class="stat-card card">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <span class="stat-value">\${{ getTotalValue().toFixed(2) }}</span>
              <span class="stat-label">Inventory Value</span>
            </div>
          </div>
          <div class="stat-card card">
            <div class="stat-icon">📊</div>
            <div class="stat-content">
              <span class="stat-value">{{ getTotalStock() }}</span>
              <span class="stat-label">Total Stock</span>
            </div>
          </div>
        </div>

        <section class="products-section">
          <h2>Your Products</h2>

          @if (loading()) {
            <div class="loading-container">
              <div class="spinner"></div>
            </div>
          } @else if (products().length === 0) {
            <div class="empty-state card">
              <span class="empty-icon">📦</span>
              <h3>No products yet</h3>
              <p>Start by adding your first product</p>
              <a routerLink="/seller/products/new" class="btn btn-primary">Add Product</a>
            </div>
          } @else {
            <div class="products-table">
              <table>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (product of products(); track product.id) {
                    <tr>
                      <td>
                        <div class="product-cell">
                          <span class="product-name">{{ product.name }}</span>
                          <span class="product-desc">{{ product.description | slice:0:50 }}...</span>
                        </div>
                      </td>
                      <td class="price-cell">\${{ product.price.toFixed(2) }}</td>
                      <td>
                        <span class="stock-badge" [class.low-stock]="product.quantity < 5">
                          {{ product.quantity }}
                        </span>
                      </td>
                      <td>
                        <div class="actions-cell">
                          <a [routerLink]="['/seller/products', product.id, 'edit']" class="btn-icon" title="Edit">
                            ✏️
                          </a>
                          <a [routerLink]="['/seller/products', product.id, 'media']" class="btn-icon" title="Images">
                            🖼️
                          </a>
                          <button class="btn-icon danger" title="Delete" (click)="deleteProduct(product)">
                            🗑️
                          </button>
                        </div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </section>
      </div>
    </div>
  `,
    styles: [`
    .dashboard-page {
      padding: 2rem 0;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .dashboard-header h1 {
      margin-bottom: 0.25rem;
    }

    .dashboard-header p {
      color: var(--text-secondary);
    }

    .dashboard-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    @media (max-width: 768px) {
      .dashboard-stats {
        grid-template-columns: 1fr;
      }
    }

    .stat-card {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1.5rem;
    }

    .stat-icon {
      font-size: 2.5rem;
    }

    .stat-content {
      display: flex;
      flex-direction: column;
    }

    .stat-value {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--text-primary);
    }

    .stat-label {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .products-section h2 {
      margin-bottom: 1rem;
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 3rem;
    }

    .empty-state {
      text-align: center;
      padding: 3rem;
    }

    .empty-icon {
      font-size: 3rem;
      display: block;
      margin-bottom: 1rem;
    }

    .empty-state h3 {
      margin-bottom: 0.5rem;
    }

    .empty-state p {
      color: var(--text-secondary);
      margin-bottom: 1rem;
    }

    .products-table {
      background: var(--bg-card);
      border-radius: var(--radius-xl);
      border: 1px solid rgba(255, 255, 255, 0.05);
      overflow: hidden;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th, td {
      padding: 1rem 1.5rem;
      text-align: left;
    }

    th {
      background: var(--bg-tertiary);
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    tr {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    tr:last-child {
      border-bottom: none;
    }

    tr:hover {
      background: rgba(255, 255, 255, 0.02);
    }

    .product-cell {
      display: flex;
      flex-direction: column;
    }

    .product-name {
      font-weight: 500;
      color: var(--text-primary);
    }

    .product-desc {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .price-cell {
      font-weight: 600;
      color: var(--color-primary-light);
    }

    .stock-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      background: rgba(16, 185, 129, 0.1);
      color: var(--color-success);
      border-radius: var(--radius-full);
      font-weight: 500;
    }

    .stock-badge.low-stock {
      background: rgba(245, 158, 11, 0.1);
      color: var(--color-warning);
    }

    .actions-cell {
      display: flex;
      gap: 0.5rem;
    }

    .btn-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-glass);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: all var(--transition-fast);
      text-decoration: none;
    }

    .btn-icon:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .btn-icon.danger:hover {
      background: rgba(239, 68, 68, 0.2);
      border-color: rgba(239, 68, 68, 0.5);
    }
  `]
})
export class DashboardComponent implements OnInit {
    authService = inject(AuthService);
    private productService = inject(ProductService);

    products = signal<Product[]>([]);
    loading = signal(true);

    ngOnInit(): void {
        this.loadProducts();
    }

    loadProducts(): void {
        const userId = this.authService.currentUser()?.id;
        if (!userId) return;

        this.loading.set(true);
        this.productService.getProductsByUser(userId).subscribe({
            next: (products) => {
                this.products.set(products);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    getTotalValue(): number {
        return this.products().reduce((sum, p) => sum + (p.price * p.quantity), 0);
    }

    getTotalStock(): number {
        return this.products().reduce((sum, p) => sum + p.quantity, 0);
    }

    deleteProduct(product: Product): void {
        if (confirm(`Are you sure you want to delete "${product.name}"?`)) {
            this.productService.deleteProduct(product.id).subscribe({
                next: () => {
                    this.products.update(products => products.filter(p => p.id !== product.id));
                }
            });
        }
    }
}
