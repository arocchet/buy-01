import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../shared/models/product.model';

@Component({
    selector: 'app-product-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    template: `
    <div class="form-page">
      <div class="container">
        <div class="form-container">
          <div class="form-card card">
            <div class="card-header">
              <h1>{{ isEditMode() ? 'Edit Product' : 'Add New Product' }}</h1>
              <p>{{ isEditMode() ? 'Update your product details' : 'Fill in the details for your new product' }}</p>
            </div>

            <div class="card-body">
              @if (error()) {
                <div class="alert alert-error">{{ error() }}</div>
              }

              <form [formGroup]="productForm" (ngSubmit)="onSubmit()">
                <div class="form-group">
                  <label class="form-label" for="name">Product Name</label>
                  <input
                    type="text"
                    id="name"
                    class="form-input"
                    formControlName="name"
                    placeholder="Enter product name"
                  >
                  @if (productForm.get('name')?.invalid && productForm.get('name')?.touched) {
                    <span class="form-error">Name must be between 2 and 200 characters</span>
                  }
                </div>

                <div class="form-group">
                  <label class="form-label" for="description">Description</label>
                  <textarea
                    id="description"
                    class="form-input form-textarea"
                    formControlName="description"
                    placeholder="Describe your product"
                    rows="4"
                  ></textarea>
                  @if (productForm.get('description')?.invalid && productForm.get('description')?.touched) {
                    <span class="form-error">Description cannot exceed 1000 characters</span>
                  }
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label" for="price">Price ($)</label>
                    <input
                      type="number"
                      id="price"
                      class="form-input"
                      formControlName="price"
                      placeholder="0.00"
                      step="0.01"
                      min="0.01"
                    >
                    @if (productForm.get('price')?.invalid && productForm.get('price')?.touched) {
                      <span class="form-error">Price must be greater than 0</span>
                    }
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="quantity">Quantity</label>
                    <input
                      type="number"
                      id="quantity"
                      class="form-input"
                      formControlName="quantity"
                      placeholder="0"
                      min="0"
                    >
                    @if (productForm.get('quantity')?.invalid && productForm.get('quantity')?.touched) {
                      <span class="form-error">Quantity must be 0 or greater</span>
                    }
                  </div>
                </div>

                <div class="form-actions">
                  <a routerLink="/seller/dashboard" class="btn btn-secondary">Cancel</a>
                  <button
                    type="submit"
                    class="btn btn-primary"
                    [disabled]="loading() || productForm.invalid"
                  >
                    @if (loading()) {
                      <span class="spinner-sm"></span>
                      {{ isEditMode() ? 'Updating...' : 'Creating...' }}
                    } @else {
                      {{ isEditMode() ? 'Update Product' : 'Create Product' }}
                    }
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .form-page {
      padding: 2rem 0;
    }

    .form-container {
      max-width: 640px;
      margin: 0 auto;
    }

    .form-card .card-header h1 {
      margin-bottom: 0.25rem;
    }

    .form-card .card-header p {
      color: var(--text-secondary);
    }

    .form-textarea {
      resize: vertical;
      min-height: 100px;
    }

    .form-row {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    @media (max-width: 640px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1.5rem;
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
export class ProductFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private productService = inject(ProductService);

    loading = signal(false);
    error = signal<string | null>(null);
    isEditMode = signal(false);
    productId = signal<string | null>(null);

    productForm: FormGroup = this.fb.group({
        name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
        description: ['', [Validators.maxLength(1000)]],
        price: [null, [Validators.required, Validators.min(0.01)]],
        quantity: [0, [Validators.required, Validators.min(0)]]
    });

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.isEditMode.set(true);
            this.productId.set(id);
            this.loadProduct(id);
        }
    }

    loadProduct(id: string): void {
        this.productService.getProduct(id).subscribe({
            next: (product) => {
                this.productForm.patchValue({
                    name: product.name,
                    description: product.description,
                    price: product.price,
                    quantity: product.quantity
                });
            },
            error: () => {
                this.error.set('Failed to load product');
            }
        });
    }

    onSubmit(): void {
        if (this.productForm.invalid) return;

        this.loading.set(true);
        this.error.set(null);

        const productData = this.productForm.value;

        if (this.isEditMode() && this.productId()) {
            this.productService.updateProduct(this.productId()!, productData).subscribe({
                next: () => {
                    this.loading.set(false);
                    this.router.navigate(['/seller/dashboard']);
                },
                error: (err) => {
                    this.loading.set(false);
                    this.error.set(err.error?.message || 'Failed to update product');
                }
            });
        } else {
            this.productService.createProduct(productData).subscribe({
                next: (product) => {
                    this.loading.set(false);
                    this.router.navigate(['/seller/products', product.id, 'media']);
                },
                error: (err) => {
                    this.loading.set(false);
                    this.error.set(err.error?.message || 'Failed to create product');
                }
            });
        }
    }
}
