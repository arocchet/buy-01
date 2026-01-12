import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    template: `
    <div class="auth-page">
      <div class="auth-container">
        <div class="auth-card card">
          <div class="card-header text-center">
            <h1 class="auth-title">Create Account</h1>
            <p class="auth-subtitle">Join our marketplace</p>
          </div>

          <div class="card-body">
            @if (error()) {
              <div class="alert alert-error">{{ error() }}</div>
            }

            <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
              <div class="form-group">
                <label class="form-label" for="name">Full Name</label>
                <input
                  type="text"
                  id="name"
                  class="form-input"
                  formControlName="name"
                  placeholder="Enter your full name"
                >
                @if (registerForm.get('name')?.invalid && registerForm.get('name')?.touched) {
                  <span class="form-error">Name must be between 2 and 100 characters</span>
                }
              </div>

              <div class="form-group">
                <label class="form-label" for="email">Email</label>
                <input
                  type="email"
                  id="email"
                  class="form-input"
                  formControlName="email"
                  placeholder="Enter your email"
                >
                @if (registerForm.get('email')?.invalid && registerForm.get('email')?.touched) {
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
                  placeholder="Enter your password (min 8 characters)"
                >
                @if (registerForm.get('password')?.invalid && registerForm.get('password')?.touched) {
                  <span class="form-error">Password must be at least 8 characters</span>
                }
              </div>

              <div class="form-group">
                <label class="form-label">Account Type</label>
                <div class="role-selector">
                  <button
                    type="button"
                    class="role-option"
                    [class.active]="registerForm.get('role')?.value === 'client'"
                    (click)="registerForm.patchValue({ role: 'client' })"
                  >
                    <span class="role-icon">🛒</span>
                    <span class="role-name">Client</span>
                    <span class="role-desc">Browse and purchase products</span>
                  </button>
                  <button
                    type="button"
                    class="role-option"
                    [class.active]="registerForm.get('role')?.value === 'seller'"
                    (click)="registerForm.patchValue({ role: 'seller' })"
                  >
                    <span class="role-icon">🏪</span>
                    <span class="role-name">Seller</span>
                    <span class="role-desc">Sell your products</span>
                  </button>
                </div>
              </div>

              <button
                type="submit"
                class="btn btn-primary btn-block"
                [disabled]="loading() || registerForm.invalid"
              >
                @if (loading()) {
                  <span class="spinner-sm"></span>
                  Creating account...
                } @else {
                  Create Account
                }
              </button>
            </form>

            <div class="auth-footer">
              <p>Already have an account? <a routerLink="/login">Sign in</a></p>
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
      max-width: 480px;
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

    .role-selector {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .role-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 1.5rem;
      background: var(--bg-glass);
      border: 2px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-lg);
      cursor: pointer;
      transition: all var(--transition-normal);
      text-align: center;
    }

    .role-option:hover {
      border-color: rgba(99, 102, 241, 0.5);
    }

    .role-option.active {
      border-color: var(--color-primary);
      background: rgba(99, 102, 241, 0.1);
    }

    .role-icon {
      font-size: 2rem;
      margin-bottom: 0.5rem;
    }

    .role-name {
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 0.25rem;
    }

    .role-desc {
      font-size: 0.75rem;
      color: var(--text-muted);
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
export class RegisterComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);

    loading = signal(false);
    error = signal<string | null>(null);

    registerForm: FormGroup = this.fb.group({
        name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        role: ['client', [Validators.required]]
    });

    onSubmit(): void {
        if (this.registerForm.invalid) return;

        this.loading.set(true);
        this.error.set(null);

        this.authService.register(this.registerForm.value).subscribe({
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
                this.error.set(err.error?.message || 'Registration failed. Please try again.');
            }
        });
    }
}
