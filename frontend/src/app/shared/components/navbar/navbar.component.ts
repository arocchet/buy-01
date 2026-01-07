import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
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
            }
            <div class="user-menu">
              <span class="user-name">{{ authService.currentUser()?.name }}</span>
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
    }

    .nav-link:hover {
      color: var(--text-primary);
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
export class NavbarComponent {
  authService = inject(AuthService);
}
