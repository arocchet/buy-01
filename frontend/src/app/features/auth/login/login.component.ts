import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    template: `
    <div class="auth-page">
      <div class="auth-container">
        <div class="auth-card card">
          <div class="card-header text-center">
            <h1 class="auth-title">Welcome Back</h1>
            <p class="auth-subtitle">Sign in to your account</p>
          </div>

          <div class="card-body">
            @if (error()) {
              <div class="alert alert-error">{{ error() }}</div>
            }

            <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
              <div class="form-group">
                <label class="form-label" for="email">Email</label>
                <input
                  type="email"
                  id="email"
                  class="form-input"
                  formControlName="email"
                  placeholder="Enter your email"
                >
                @if (loginForm.get('email')?.invalid && loginForm.get('email')?.touched) {
                  <span class="form-error">Please enter a valid email</span>
                }
              </div>

              <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input
                  type="password"
                  id="password"
                  class="form-input"
                  formControlName="password"
                  placeholder="Enter your password"
                >
                @if (loginForm.get('password')?.invalid && loginForm.get('password')?.touched) {
                  <span class="form-error">Password is required</span>
                }
              </div>

              <button
                type="submit"
                class="btn btn-primary btn-block"
                [disabled]="loading() || loginForm.invalid"
              >
                @if (loading()) {
                  <span class="spinner-sm"></span>
                  Signing in...
                } @else {
                  Sign In
                }
              </button>
            </form>

            <div class="auth-footer">
              <p>Don't have an account? <a routerLink="/register">Register</a></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .auth-page {
      min-height: calc(100vh - 80px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      background: 
        radial-gradient(ellipse at top left, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
        radial-gradient(ellipse at bottom right, rgba(168, 85, 247, 0.15) 0%, transparent 50%),
        var(--bg-primary);
    }

    .auth-container {
      width: 100%;
      max-width: 420px;
    }

    .auth-card {
      background: var(--bg-card);
    }

    .auth-title {
      font-size: 1.75rem;
      margin-bottom: 0.5rem;
    }

    .auth-subtitle {
      color: var(--text-secondary);
    }

    .btn-block {
      width: 100%;
      padding: 1rem;
      font-size: 1rem;
    }

    .auth-footer {
      margin-top: 1.5rem;
      text-align: center;
      color: var(--text-secondary);
    }

    .alert {
      padding: 1rem;
      border-radius: var(--radius-md);
      margin-bottom: 1rem;
    }

    .alert-error {
      background: rgba(239, 68, 68, 0.1);
      color: var(--color-error);
      border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .spinner-sm {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-right: 0.5rem;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);

    loading = signal(false);
    error = signal<string | null>(null);

    loginForm: FormGroup = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]]
    });

    onSubmit(): void {
        if (this.loginForm.invalid) return;

        this.loading.set(true);
        this.error.set(null);

        this.authService.login(this.loginForm.value).subscribe({
            next: () => {
                this.loading.set(false);
                const user = this.authService.currentUser();
                if (user?.role === 'seller') {
                    this.router.navigate(['/seller/dashboard']);
                } else {
                    this.router.navigate(['/products']);
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set(err.error?.message || 'Login failed. Please check your credentials.');
            }
        });
    }
}
