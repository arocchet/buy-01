import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { MediaService } from '../../../core/services/media.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../shared/models/product.model';
import { Media } from '../../../shared/models/media.model';

@Component({
    selector: 'app-product-list',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule],
    template: `
    <div class="products-page">
      <div class="container">
        <header class="page-header">
          <div class="header-content">
            <h1 class="page-title">Discover Products</h1>
            <p class="page-subtitle">Browse our marketplace and find what you need</p>
          </div>
        </header>

        <!-- Search & Filter bar -->
        <div class="filter-bar">
          <div class="search-input-wrap">
            <span class="search-icon">&#128269;</span>
            <input
              type="text"
              class="search-input"
              placeholder="Search products..."
              [(ngModel)]="keyword"
              (input)="onSearch()" />
          </div>
          <div class="price-filters">
            <input
              type="number"
              class="price-input"
              placeholder="Min \$"
              [(ngModel)]="minPrice"
              (input)="onSearch()"
              min="0" />
            <span class="price-sep">&ndash;</span>
            <input
              type="number"
              class="price-input"
              placeholder="Max \$"
              [(ngModel)]="maxPrice"
              (input)="onSearch()"
              min="0" />
          </div>
          <button class="btn btn-secondary btn-sm" (click)="clearFilters()">Clear</button>
        </div>

        @if (loading()) {
          <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading products...</p>
          </div>
        } @else if (products().length === 0) {
          <div class="empty-state">
            <span class="empty-icon">&#128274;</span>
            <h2>No products found</h2>
            <p>Try adjusting your search or filters</p>
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
                      <span class="img-placeholder-icon">IMG</span>
                    </div>
                  }
                </div>
                <div class="product-content">
                  <h3 class="product-name">{{ product.name }}</h3>
                  <p class="product-description">{{ product.description | slice:0:80 }}{{ product.description && product.description.length > 80 ? '...' : '' }}</p>
                  <div class="product-footer">
                    <span class="product-price">\${{ product.price.toFixed(2) }}</span>
                    <span class="product-stock" [class.low-stock]="product.quantity < 5">
                      {{ product.quantity }} in stock
                    </span>
                  </div>                    @if (cartMessage()?.productId === product.id) {
                      <div class="cart-msg" [class.cart-msg-ok]="cartMessage()!.ok" [class.cart-msg-err]="!cartMessage()!.ok">
                        {{ cartMessage()!.text }}
                      </div>
                    }                  <div class="product-actions">
                    <a [routerLink]="['/products', product.id]" class="btn btn-secondary btn-sm flex-1">
                      Details
                    </a>
                    @if (isClient()) {
                      <button
                        class="btn btn-primary btn-sm flex-1"
                        (click)="addToCart(product)"
                        [disabled]="addingToCart().has(product.id) || product.quantity === 0">
                        @if (addingToCart().has(product.id)) { &#10003; Added } @else { Add to Cart }
                      </button>
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
    .products-page {
      padding: 2rem 0;
    }

    .page-header {
      text-align: center;
      margin-bottom: 2rem;
      padding: 3rem 0 1rem;
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

    /* Filter bar */
    .filter-bar {
      display: flex;
      gap: 1rem;
      align-items: center;
      margin-bottom: 2rem;
      flex-wrap: wrap;
    }

    .search-input-wrap {
      position: relative;
      flex: 1;
      min-width: 200px;
    }

    .search-icon {
      position: absolute;
      left: 0.75rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 1rem;
    }

    .search-input {
      width: 100%;
      padding: 0.6rem 0.75rem 0.6rem 2.25rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      font-size: 0.95rem;
      box-sizing: border-box;
    }

    .search-input:focus {
      outline: 2px solid var(--primary);
      outline-offset: 2px;
    }

    .price-filters {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .price-input {
      width: 90px;
      padding: 0.6rem 0.5rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      font-size: 0.9rem;
    }

    .price-sep { color: var(--text-secondary); }

    .btn-sm { padding: 0.45rem 0.9rem; font-size: 0.875rem; border-radius: 0.4rem; cursor: pointer; }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      color: var(--text-secondary);
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

    .empty-state h2 { margin-bottom: 0.5rem; }
    .empty-state p { color: var(--text-secondary); }

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
      margin-bottom: 0.75rem;
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

    .product-actions {
      display: flex;
      gap: 0.5rem;
    }

    .flex-1 { flex: 1; text-align: center; }

    .cart-msg {
      font-size: 0.75rem;
      text-align: center;
      padding: 0.25rem 0.5rem;
      border-radius: 0.3rem;
      margin-bottom: 0.35rem;
    }
    .cart-msg-ok  { background: rgba(34,197,94,.15); color: #22c55e; }
    .cart-msg-err { background: rgba(239,68,68,.15);  color: #ef4444; }

    .img-placeholder-icon { font-size: 2rem; color: var(--text-secondary); }
  `]
})
export class ProductListComponent implements OnInit {
    private productService = inject(ProductService);
    private mediaService = inject(MediaService);
    private cartService = inject(CartService);
    private authService = inject(AuthService);

    products = signal<Product[]>([]);
    loading = signal(true);
    productImages = signal<Map<string, string>>(new Map());
    addingToCart = signal<Set<string>>(new Set());

    isClient = this.authService.isClient;
    isAuthenticated = this.authService.isAuthenticated;

    keyword = '';
    minPrice: number | null = null;
    maxPrice: number | null = null;
    cartMessage = signal<{ text: string; ok: boolean; productId: string } | null>(null);

    private searchTimeout: ReturnType<typeof setTimeout> | null = null;

    ngOnInit(): void {
        this.loadProducts();
    }

    loadProducts(keyword?: string, minPrice?: number, maxPrice?: number): void {
        this.loading.set(true);
        const obs$ = (keyword || minPrice !== undefined || maxPrice !== undefined)
            ? this.productService.searchProducts(keyword, minPrice ?? undefined, maxPrice ?? undefined)
            : this.productService.loadProducts();

        obs$.subscribe({
            next: (products) => {
                this.products.set(products);
                this.loading.set(false);
                products.forEach(product => this.loadProductImage(product.id));
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onSearch(): void {
        if (this.searchTimeout) clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(() => {
            this.loadProducts(
                this.keyword || undefined,
                this.minPrice ?? undefined,
                this.maxPrice ?? undefined
            );
        }, 400);
    }

    clearFilters(): void {
        this.keyword = '';
        this.minPrice = null;
        this.maxPrice = null;
        this.loadProducts();
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

    addToCart(product: Product): void {
        const currentUser = this.authService.currentUser();
        if (!currentUser) return;

        this.addingToCart.update(s => new Set([...s, product.id]));
        this.cartMessage.set(null);

        this.cartService.addItem({
            productId: product.id,
            productName: product.name,
            quantity: 1,
            unitPrice: product.price,
            sellerId: product.userId
        }).subscribe({
            next: () => {
                this.cartMessage.set({ text: 'Added to cart!', ok: true, productId: product.id });
                this.addingToCart.update(s => {
                    const next = new Set(s); next.delete(product.id); return next;
                });
                setTimeout(() => this.cartMessage.set(null), 2500);
            },
            error: (err) => {
                const msg = err?.error?.message ?? err?.message ?? 'Could not add to cart';
                this.cartMessage.set({ text: msg, ok: false, productId: product.id });
                this.addingToCart.update(s => {
                    const next = new Set(s); next.delete(product.id); return next;
                });
                setTimeout(() => this.cartMessage.set(null), 4000);
            }
        });
    }
}
