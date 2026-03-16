import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <nav class="navbar">
      <div class="container navbar-content">
        <a routerLink="/" class="navbar-brand">
          <span class="brand-icon">🎮</span>
          <span class="brand-text">Buy01</span>
        </a>

        <div class="navbar-links">
          <a routerLink="/products" class="nav-link">Products</a>

          @if (authService.isAuthenticated()) {
            @if (authService.isSeller()) {
              <a routerLink="/seller/dashboard" class="nav-link">Dashboard</a>
              <a routerLink="/orders" class="nav-link">Orders</a>
            } @else {
              <!-- Client links -->
              <a routerLink="/orders" class="nav-link">My Orders</a>
              <a routerLink="/cart" class="nav-link cart-link">
                🛒 Cart
                @if (cartItemCount() > 0) {
                  <span class="cart-badge">{{ cartItemCount() }}</span>
                }
              </a>
            }
            <div class="user-menu">
              <a routerLink="/profile" class="nav-link user-name">{{ authService.currentUser()?.name }}</a>
              <span class="user-role badge" [class.badge-primary]="authService.isSeller()">
                {{ authService.currentUser()?.role }}
              </span>
              <button class="btn btn-secondary btn-sm" (click)="authService.logout()">
                Logout
              </button>
            </div>
          } @else {
            <a routerLink="/login" class="nav-link">Login</a>
            <a routerLink="/register" class="btn btn-primary">Register</a>
          }
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      height: 80px;
      background: rgba(15, 15, 35, 0.95);
      backdrop-filter: blur(20px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      z-index: 1000;
    }

    .navbar-content {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--text-primary);
      text-decoration: none;
    }

    .brand-icon {
      font-size: 2rem;
    }

    .brand-text {
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .navbar-links {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }

    .nav-link {
      color: var(--text-secondary);
      font-weight: 500;
      transition: color var(--transition-fast);
      text-decoration: none;
    }

    .nav-link:hover {
      color: var(--text-primary);
    }

    .cart-link {
      position: relative;
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .cart-badge {
      position: absolute;
      top: -8px;
      right: -10px;
      background: var(--primary, #6366f1);
      color: white;
      border-radius: 9999px;
      font-size: 0.7rem;
      font-weight: 700;
      min-width: 18px;
      height: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
    }

    .user-menu {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .user-name {
      font-weight: 500;
      color: var(--text-primary);
    }

    .user-role {
      text-transform: capitalize;
    }

    .btn-sm {
      padding: 0.5rem 1rem;
      font-size: 0.875rem;
    }

    @media (max-width: 768px) {
      .navbar-links {
        gap: 1rem;
      }

      .user-name {
        display: none;
      }
    }
  `]
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  private cartService = inject(CartService);

  cartItemCount = this.cartService.itemCount;

  ngOnInit() {
    // Load cart when user is authenticated
    if (this.authService.isAuthenticated() && this.authService.isClient()) {
      this.cartService.loadCart().subscribe();
    }
  }
}
