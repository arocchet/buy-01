import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { Order, OrderStatus } from '../../shared/models/order.model';

@Component({
    selector: 'app-orders',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="orders-page">
      <div class="container">
        <header class="page-header">
          <h1 class="page-title">{{ isSeller() ? 'Customer Orders' : 'My Orders' }}</h1>
          <p class="page-subtitle">{{ isSeller() ? 'Manage orders for your products' : 'Track your order history' }}</p>
        </header>

        <!-- Search bar -->
        <div class="search-bar">
          <input
            type="text"
            class="form-control"
            placeholder="Search by status or product name..."
            [(ngModel)]="searchTerm"
            (input)="onSearch()" />
        </div>

        @if (loading()) {
          <div class="loading-container"><div class="spinner"></div><p>Loading orders...</p></div>
        } @else if (orders().length === 0) {
          <div class="empty-state">
            <span class="empty-icon">📋</span>
            <h2>No orders found</h2>
            @if (!isSeller()) {
              <a routerLink="/products" class="btn btn-primary">Shop Now</a>
            }
          </div>
        } @else {
          <div class="orders-list">
            @for (order of orders(); track order.id) {
              <div class="order-card card">
                <div class="order-header">
                  <div>
                    <span class="order-id">Order #{{ order.id | slice:0:8 }}...</span>
                    <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                  </div>
                  <span class="status-badge" [class]="'status-' + order.status.toLowerCase()">
                    {{ order.status }}
                  </span>
                </div>

                <div class="order-items">
                  @for (item of order.items; track item.productId) {
                    <div class="order-item">
                      <span class="item-name">{{ item.productName }}</span>
                      <span class="item-detail">× {{ item.quantity }} {{ '@' }} \${{ item.unitPrice.toFixed(2) }}</span>
                      <span class="item-subtotal">\${{ (item.unitPrice * item.quantity).toFixed(2) }}</span>
                    </div>
                  }
                </div>

                <div class="order-footer">
                  <div class="order-total">
                    Total: <strong>\${{ order.totalAmount.toFixed(2) }}</strong>
                    <span class="payment-badge">{{ order.paymentMethod }}</span>
                  </div>
                  <div class="order-actions">
                    @if (!isSeller() && (order.status === 'PENDING' || order.status === 'CONFIRMED')) {
                      <button class="btn btn-danger btn-sm" (click)="cancelOrder(order.id)" [disabled]="processing()">
                        Cancel
                      </button>
                    }
                    @if (isSeller() && order.status !== 'CANCELLED' && order.status !== 'DELIVERED') {
                      <select class="status-select" [value]="order.status" (change)="updateStatus(order.id, $any($event.target).value)">
                        <option value="PENDING">PENDING</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                        <option value="SHIPPED">SHIPPED</option>
                        <option value="DELIVERED">DELIVERED</option>
                      </select>
                    }
                  </div>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
    styles: [`
    .orders-page { padding: 2rem 0; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title {
      font-size: 2rem;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .page-subtitle { color: var(--text-secondary); }
    .search-bar { margin-bottom: 1.5rem; }
    .form-control {
      width: 100%;
      max-width: 500px;
      padding: 0.6rem 1rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      box-sizing: border-box;
    }
    .orders-list { display: flex; flex-direction: column; gap: 1rem; }
    .order-card { padding: 1.5rem; }
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      flex-wrap: wrap;
      gap: 0.5rem;
    }
    .order-id { font-weight: 600; font-family: monospace; font-size: 0.9rem; }
    .order-date { display: block; color: var(--text-secondary); font-size: 0.8rem; margin-top: 0.25rem; }
    .status-badge {
      padding: 0.3rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    .status-pending { background: rgba(234,179,8,0.15); color: #eab308; }
    .status-confirmed { background: rgba(59,130,246,0.15); color: #3b82f6; }
    .status-shipped { background: rgba(168,85,247,0.15); color: #a855f7; }
    .status-delivered { background: rgba(34,197,94,0.15); color: #22c55e; }
    .status-cancelled { background: rgba(239,68,68,0.15); color: #ef4444; }
    .order-items { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1rem; }
    .order-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      font-size: 0.9rem;
      padding: 0.4rem;
      background: var(--surface);
      border-radius: 0.4rem;
    }
    .item-name { flex: 1; font-weight: 500; }
    .item-detail { color: var(--text-secondary); }
    .item-subtotal { font-weight: 600; }
    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
    }
    .order-total { font-size: 1rem; }
    .payment-badge {
      margin-left: 0.75rem;
      font-size: 0.75rem;
      background: rgba(99,102,241,0.1);
      color: var(--primary);
      padding: 0.2rem 0.5rem;
      border-radius: 0.3rem;
    }
    .order-actions { display: flex; gap: 0.5rem; align-items: center; }
    .btn-danger { background: #ef4444; color: white; border: none; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.85rem; border-radius: 0.35rem; cursor: pointer; }
    .status-select {
      padding: 0.3rem 0.5rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.4rem;
      color: var(--text-primary);
      cursor: pointer;
    }
    .loading-container { display:flex; flex-direction:column; align-items:center; padding:4rem; }
    .empty-state { text-align:center; padding:4rem; }
    .empty-icon { font-size:3rem; }
  `]
})
export class OrdersComponent implements OnInit {
    private orderService = inject(OrderService);
    private authService = inject(AuthService);

    isSeller = this.authService.isSeller;
    orders = signal<Order[]>([]);
    loading = signal(false);
    processing = signal(false);
    searchTerm = '';
    private searchTimeout: ReturnType<typeof setTimeout> | null = null;

    ngOnInit() {
        this.loadOrders();
    }

    loadOrders(search?: string) {
        this.loading.set(true);
        const obs$ = this.isSeller()
            ? this.orderService.getSellerOrders(search)
            : this.orderService.getMyOrders(search);

        obs$.subscribe({
            next: orders => {
                this.orders.set(orders);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onSearch() {
        if (this.searchTimeout) clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(() => this.loadOrders(this.searchTerm || undefined), 400);
    }

    cancelOrder(orderId: string) {
        this.processing.set(true);
        this.orderService.cancelOrder(orderId).subscribe({
            next: updated => {
                this.orders.update(list => list.map(o => o.id === orderId ? updated : o));
                this.processing.set(false);
            },
            error: () => this.processing.set(false)
        });
    }

    updateStatus(orderId: string, status: string) {
        this.orderService.updateOrderStatus(orderId, status).subscribe({
            next: updated => {
                this.orders.update(list => list.map(o => o.id === orderId ? updated : o));
            }
        });
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    }
}
