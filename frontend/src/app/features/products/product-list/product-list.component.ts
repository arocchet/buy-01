import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { MediaService } from '../../../core/services/media.service';
import { Product } from '../../../shared/models/product.model';
import { Media } from '../../../shared/models/media.model';

@Component({
    selector: 'app-product-list',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
    <div class="products-page">
      <div class="container">
        <header class="page-header">
          <div class="header-content">
            <h1 class="page-title">Discover Products</h1>
            <p class="page-subtitle">Browse our marketplace and find what you need</p>
          </div>
        </header>

        @if (loading()) {
          <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading products...</p>
          </div>
        } @else if (products().length === 0) {
          <div class="empty-state">
            <span class="empty-icon">📦</span>
            <h2>No products available</h2>
            <p>Check back later for new listings</p>
          </div>
        } @else {
          <div class="products-grid grid grid-4">
            @for (product of products(); track product.id) {
              <div class="product-card card">
                <div class="product-image">
                  @if (getProductImage(product.id)) {
                    <img [src]="getProductImage(product.id)" [alt]="product.name">
                  } @else {
                    <div class="placeholder-image">
                      <span>🖼️</span>
                    </div>
                  }
                </div>
                <div class="product-content">
                  <h3 class="product-name">{{ product.name }}</h3>
                  <p class="product-description">{{ product.description | slice:0:80 }}{{ product.description.length > 80 ? '...' : '' }}</p>
                  <div class="product-footer">
                    <span class="product-price">\${{ product.price.toFixed(2) }}</span>
                    <span class="product-stock" [class.low-stock]="product.quantity < 5">
                      {{ product.quantity }} in stock
                    </span>
                  </div>
                  <a [routerLink]="['/products', product.id]" class="btn btn-secondary btn-block mt-md">
                    View Details
                  </a>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
    styles: [`
    .products-page {
      padding: 2rem 0;
    }

    .page-header {
      text-align: center;
      margin-bottom: 3rem;
      padding: 3rem 0;
      background: 
        radial-gradient(ellipse at center, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
    }

    .page-title {
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .page-subtitle {
      color: var(--text-secondary);
      font-size: 1.125rem;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      color: var(--text-secondary);
    }

    .loading-container p {
      margin-top: 1rem;
    }

    .empty-state {
      text-align: center;
      padding: 4rem;
    }

    .empty-icon {
      font-size: 4rem;
      display: block;
      margin-bottom: 1rem;
    }

    .empty-state h2 {
      margin-bottom: 0.5rem;
    }

    .empty-state p {
      color: var(--text-secondary);
    }

    .products-grid {
      gap: 1.5rem;
    }

    .product-card {
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .product-image {
      height: 200px;
      overflow: hidden;
      background: var(--bg-tertiary);
    }

    .product-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-normal);
    }

    .product-card:hover .product-image img {
      transform: scale(1.05);
    }

    .placeholder-image {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 3rem;
      background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
    }

    .product-content {
      padding: 1.25rem;
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .product-name {
      font-size: 1.125rem;
      margin-bottom: 0.5rem;
      color: var(--text-primary);
    }

    .product-description {
      color: var(--text-secondary);
      font-size: 0.875rem;
      flex: 1;
      margin-bottom: 1rem;
    }

    .product-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .product-price {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--color-primary-light);
    }

    .product-stock {
      font-size: 0.75rem;
      color: var(--color-success);
    }

    .product-stock.low-stock {
      color: var(--color-warning);
    }

    .btn-block {
      width: 100%;
    }
  `]
})
export class ProductListComponent implements OnInit {
    private productService = inject(ProductService);
    private mediaService = inject(MediaService);

    products = this.productService.products;
    loading = signal(true);
    productImages = signal<Map<string, string>>(new Map());

    ngOnInit(): void {
        this.loadProducts();
    }

    loadProducts(): void {
        this.loading.set(true);
        this.productService.loadProducts().subscribe({
            next: (products) => {
                this.loading.set(false);
                // Load images for each product
                products.forEach(product => this.loadProductImage(product.id));
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    loadProductImage(productId: string): void {
        this.mediaService.loadMediaByProduct(productId).subscribe({
            next: (media) => {
                if (media.length > 0) {
                    this.productImages.update(map => {
                        const newMap = new Map(map);
                        newMap.set(productId, media[0].url);
                        return newMap;
                    });
                }
            }
        });
    }

    getProductImage(productId: string): string | undefined {
        return this.productImages().get(productId);
    }
}
