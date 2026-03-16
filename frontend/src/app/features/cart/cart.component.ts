import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { Cart } from '../../shared/models/cart.model';

@Component({
    selector: 'app-cart',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="cart-page">
      <div class="container">
        <header class="page-header">
          <h1 class="page-title">Shopping Cart</h1>
        </header>

        @if (loading()) {
          <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading cart...</p>
          </div>
        } @else if (!cart() || cart()!.items.length === 0) {
          <div class="empty-state">
            <span class="empty-icon">🛒</span>
            <h2>Your cart is empty</h2>
            <p>Browse products and add some items!</p>
            <a routerLink="/products" class="btn btn-primary">Browse Products</a>
          </div>
        } @else {
          <div class="cart-layout">
            <div class="cart-items">
              @for (item of cart()!.items; track item.productId) {
                <div class="cart-item card">
                  <div class="item-info">
                    <h3>{{ item.productName }}</h3>
                    <p class="item-price">\${{ item.unitPrice.toFixed(2) }} each</p>
                  </div>
                  <div class="item-controls">
                    <div class="quantity-control">
                      <button class="qty-btn" (click)="decrementQty(item.productId, item.quantity)">−</button>
                      <span class="qty-value">{{ item.quantity }}</span>
                      <button class="qty-btn" (click)="incrementQty(item.productId, item.quantity)">+</button>
                    </div>
                    <span class="item-subtotal">\${{ (item.unitPrice * item.quantity).toFixed(2) }}</span>
                    <button class="btn-icon remove-btn" (click)="removeItem(item.productId)" title="Remove">🗑️</button>
                  </div>
                </div>
              }
            </div>

            <div class="cart-summary card">
              <h2>Order Summary</h2>
              <div class="summary-row">
                <span>Items ({{ cart()!.items.length }})</span>
                <span>\${{ cart()!.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="summary-row total">
                <span>Total</span>
                <span><strong>\${{ cart()!.totalAmount.toFixed(2) }}</strong></span>
              </div>

              <div class="checkout-form">
                <label class="form-label">Shipping Address</label>
                <textarea
                  class="form-control"
                  [(ngModel)]="shippingAddress"
                  placeholder="Enter your shipping address..."
                  rows="3">
                </textarea>
                <p class="payment-note">💳 Payment: <strong>Pay on Delivery</strong></p>
              </div>

              @if (checkoutError()) {
                <p class="error-msg">{{ checkoutError() }}</p>
              }
              @if (checkoutSuccess()) {
                <p class="success-msg">✅ {{ checkoutSuccess() }}</p>
              }

              <button
                class="btn btn-primary btn-block mt-md"
                (click)="checkout()"
                [disabled]="checkingOut() || !shippingAddress.trim()">
                {{ checkingOut() ? 'Placing order...' : 'Place Order' }}
              </button>
              <button class="btn btn-secondary btn-block mt-sm" (click)="clearCart()">
                Clear Cart
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
    styles: [`
    .cart-page { padding: 2rem 0; }
    .page-header { margin-bottom: 2rem; }
    .page-title {
      font-size: 2rem;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .cart-layout {
      display: grid;
      grid-template-columns: 1fr 350px;
      gap: 2rem;
      align-items: start;
    }
    @media (max-width: 768px) {
      .cart-layout { grid-template-columns: 1fr; }
    }
    .cart-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 1.5rem;
      margin-bottom: 1rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .item-info h3 { margin: 0 0 0.25rem; }
    .item-price { color: var(--text-secondary); font-size: 0.9rem; }
    .item-controls {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .quantity-control {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: var(--surface);
      border-radius: 0.5rem;
      padding: 0.25rem;
    }
    .qty-btn {
      background: none;
      border: none;
      color: var(--primary);
      font-size: 1.2rem;
      cursor: pointer;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 0.25rem;
      transition: background 0.2s;
    }
    .qty-btn:hover { background: rgba(99,102,241,0.1); }
    .qty-value { font-weight: 600; min-width: 24px; text-align: center; }
    .item-subtotal { font-weight: 700; color: var(--primary); }
    .remove-btn { background: none; border: none; cursor: pointer; font-size: 1.1rem; padding: 0.25rem; }
    .cart-summary { padding: 1.5rem; }
    .cart-summary h2 { margin-bottom: 1.5rem; font-size: 1.25rem; }
    .summary-row {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      border-bottom: 1px solid var(--border);
    }
    .summary-row.total { font-size: 1.1rem; border-bottom: none; padding-top: 1rem; }
    .checkout-form { margin-top: 1.5rem; }
    .form-label { display: block; font-weight: 600; margin-bottom: 0.5rem; font-size: 0.9rem; }
    .form-control {
      width: 100%;
      padding: 0.6rem 0.75rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      resize: vertical;
      font-family: inherit;
      box-sizing: border-box;
    }
    .payment-note { margin-top: 0.75rem; font-size: 0.9rem; color: var(--text-secondary); }
    .error-msg { color: #ef4444; font-size: 0.9rem; margin-top: 0.5rem; }
    .success-msg { color: #22c55e; font-size: 0.9rem; margin-top: 0.5rem; }
    .btn-block { width: 100%; }
    .mt-md { margin-top: 1rem; }
    .mt-sm { margin-top: 0.5rem; }
    .loading-container { display:flex; flex-direction:column; align-items:center; padding:4rem; }
    .empty-state { text-align:center; padding:4rem; }
    .empty-icon { font-size:3rem; }
  `]
})
export class CartComponent implements OnInit {
    private cartService = inject(CartService);
    private orderService = inject(OrderService);
    private router = inject(Router);

    cart = this.cartService.cart;
    loading = signal(false);
    checkingOut = signal(false);
    checkoutError = signal('');
    checkoutSuccess = signal('');
    shippingAddress = '';

    ngOnInit() {
        this.loading.set(true);
        this.cartService.loadCart().subscribe({
            next: () => this.loading.set(false),
            error: () => this.loading.set(false)
        });
    }

    incrementQty(productId: string, current: number) {
        this.cartService.updateItem(productId, current + 1).subscribe();
    }

    decrementQty(productId: string, current: number) {
        if (current <= 1) {
            this.removeItem(productId);
            return;
        }
        this.cartService.updateItem(productId, current - 1).subscribe();
    }

    removeItem(productId: string) {
        this.cartService.removeItem(productId).subscribe();
    }

    clearCart() {
        this.cartService.clearCart().subscribe();
    }

    checkout() {
        if (!this.shippingAddress.trim()) return;
        this.checkingOut.set(true);
        this.checkoutError.set('');
        this.checkoutSuccess.set('');

        this.orderService.checkout({
            shippingAddress: this.shippingAddress,
            paymentMethod: 'PAY_ON_DELIVERY'
        }).subscribe({
            next: order => {
                this.checkingOut.set(false);
                this.checkoutSuccess.set('Order placed successfully! Redirecting...');
                setTimeout(() => this.router.navigate(['/orders']), 1500);
            },
            error: err => {
                this.checkingOut.set(false);
                this.checkoutError.set(err?.error?.message || 'Failed to place order. Please try again.');
            }
        });
    }
}
