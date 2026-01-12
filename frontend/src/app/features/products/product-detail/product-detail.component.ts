import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { MediaService } from '../../../core/services/media.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../shared/models/product.model';
import { Media } from '../../../shared/models/media.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="product-detail-page">
      <div class="container">
        @if (loading()) {
          <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading product...</p>
          </div>
        } @else if (product()) {
          <div class="product-detail">
            <div class="product-gallery">
              @if (media().length > 0) {
                <div class="main-image">
                  <img [src]="selectedImage() || media()[0].url" [alt]="product()!.name">
                </div>
                @if (media().length > 1) {
                  <div class="thumbnail-list">
                    @for (m of media(); track m.id) {
                      <button 
                        class="thumbnail" 
                        [class.active]="selectedImage() === m.url"
                        (click)="selectImage(m.url)"
                      >
                        <img [src]="m.url" [alt]="product()!.name">
                      </button>
                    }
                  </div>
                }
              } @else {
                <div class="no-image">
                  <span>🖼️</span>
                  <p>No images available</p>
                </div>
              }
            </div>

            <div class="product-info">
              <nav class="breadcrumb">
                <a routerLink="/products">Products</a>
                <span>/</span>
                <span>{{ product()!.name }}</span>
              </nav>

              <h1 class="product-title">{{ product()!.name }}</h1>
              
              <div class="product-price-container">
                <span class="product-price">\${{ product()!.price.toFixed(2) }}</span>
                <span class="product-stock" [class.low-stock]="product()!.quantity < 5">
                  {{ product()!.quantity }} in stock
                </span>
              </div>

              <div class="product-description">
                <h3>Description</h3>
                <p>{{ product()!.description }}</p>
              </div>

              <div class="product-actions">
                @if (authService.isClient()) {
                  <button class="btn btn-primary btn-lg">
                    Add to Cart
                  </button>
                }
                @if (isOwner()) {
                  <a [routerLink]="['/seller/products', product()!.id, 'edit']" class="btn btn-secondary">
                    Edit Product
                  </a>
                  <a [routerLink]="['/seller/products', product()!.id, 'media']" class="btn btn-outline">
                    Manage Images
                  </a>
                }
              </div>
            </div>
          </div>
        } @else {
          <div class="not-found">
            <h2>Product not found</h2>
            <a routerLink="/products" class="btn btn-primary">Back to Products</a>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .product-detail-page {
      padding: 2rem 0;
    }

    .loading-container, .not-found {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      text-align: center;
    }

    .loading-container p {
      margin-top: 1rem;
      color: var(--text-secondary);
    }

    .not-found h2 {
      margin-bottom: 1rem;
    }

    .product-detail {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 3rem;
    }

    @media (max-width: 968px) {
      .product-detail {
        grid-template-columns: 1fr;
      }
    }

    .product-gallery {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .main-image {
      aspect-ratio: 1;
      border-radius: var(--radius-xl);
      overflow: hidden;
      background: var(--bg-secondary);
    }

    .main-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .thumbnail-list {
      display: flex;
      gap: 0.75rem;
      overflow-x: auto;
    }

    .thumbnail {
      width: 80px;
      height: 80px;
      border-radius: var(--radius-md);
      overflow: hidden;
      border: 2px solid transparent;
      background: var(--bg-secondary);
      cursor: pointer;
      transition: border-color var(--transition-fast);
    }

    .thumbnail:hover, .thumbnail.active {
      border-color: var(--color-primary);
    }

    .thumbnail img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .no-image {
      aspect-ratio: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: var(--bg-secondary);
      border-radius: var(--radius-xl);
      color: var(--text-muted);
    }

    .no-image span {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-muted);
      margin-bottom: 1rem;
    }

    .breadcrumb a {
      color: var(--text-secondary);
    }

    .breadcrumb a:hover {
      color: var(--color-primary-light);
    }

    .product-title {
      font-size: 2rem;
      margin-bottom: 1rem;
    }

    .product-price-container {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .product-price {
      font-size: 2rem;
      font-weight: 700;
      color: var(--color-primary-light);
    }

    .product-stock {
      padding: 0.5rem 1rem;
      background: rgba(16, 185, 129, 0.1);
      color: var(--color-success);
      border-radius: var(--radius-full);
      font-size: 0.875rem;
    }

    .product-stock.low-stock {
      background: rgba(245, 158, 11, 0.1);
      color: var(--color-warning);
    }

    .product-description {
      margin-bottom: 2rem;
    }

    .product-description h3 {
      font-size: 1rem;
      color: var(--text-secondary);
      margin-bottom: 0.5rem;
    }

    .product-description p {
      color: var(--text-primary);
      line-height: 1.8;
    }

    .product-actions {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .btn-lg {
      padding: 1rem 2rem;
      font-size: 1.125rem;
    }
  `]
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private mediaService = inject(MediaService);
  authService = inject(AuthService);

  product = signal<Product | null>(null);
  media = signal<Media[]>([]);
  loading = signal(true);
  selectedImage = signal<string | null>(null);

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(productId);
    }
  }

  loadProduct(id: string): void {
    this.loading.set(true);
    this.productService.getProduct(id).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loadMedia(id);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  loadMedia(productId: string): void {
    console.log('Loading media for product:', productId);
    this.mediaService.loadMediaByProduct(productId).subscribe({
      next: (media) => {
        console.log('Media loaded:', media);
        this.media.set(media);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading media:', err);
        this.loading.set(false);
      }
    });
  }

  selectImage(url: string): void {
    this.selectedImage.set(url);
  }

  isOwner(): boolean {
    const user = this.authService.currentUser();
    const product = this.product();
    return !!user && !!product && user.id === product.userId;
  }
}
