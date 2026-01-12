import { Component, inject, OnInit, signal, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MediaService } from '../../../core/services/media.service';
import { ProductService } from '../../../core/services/product.service';
import { Media } from '../../../shared/models/media.model';
import { Product } from '../../../shared/models/product.model';

@Component({
    selector: 'app-media-manager',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
    <div class="media-page">
      <div class="container">
        <header class="page-header">
          <div class="header-left">
            <a routerLink="/seller/dashboard" class="back-link">← Back to Dashboard</a>
            <h1>Manage Images</h1>
            @if (product()) {
              <p>{{ product()!.name }}</p>
            }
          </div>
        </header>

        <div class="upload-section card">
          <div 
            class="drop-zone" 
            [class.drag-over]="isDragOver()"
            (dragover)="onDragOver($event)"
            (dragleave)="onDragLeave($event)"
            (drop)="onDrop($event)"
            (click)="fileInput.click()"
          >
            <input 
              #fileInput 
              type="file" 
              accept="image/jpeg,image/png,image/gif,image/webp" 
              multiple 
              (change)="onFileSelected($event)"
              style="display: none"
            >
            @if (uploading()) {
              <div class="spinner"></div>
              <p>Uploading...</p>
            } @else {
              <span class="upload-icon">📸</span>
              <p>Drag & drop images here or click to browse</p>
              <span class="upload-hint">Max 2MB per file. JPEG, PNG, GIF, WebP</span>
            }
          </div>

          @if (uploadError()) {
            <div class="alert alert-error">{{ uploadError() }}</div>
          }
        </div>

        <section class="media-section">
          <h2>Product Images ({{ media().length }})</h2>

          @if (loading()) {
            <div class="loading-container">
              <div class="spinner"></div>
            </div>
          } @else if (media().length === 0) {
            <div class="empty-state">
              <span class="empty-icon">🖼️</span>
              <p>No images uploaded yet</p>
            </div>
          } @else {
            <div class="media-grid">
              @for (m of media(); track m.id) {
                <div class="media-item">
                  <img [src]="m.url" [alt]="m.originalFilename">
                  <div class="media-overlay">
                    <button class="btn-delete" (click)="deleteMedia(m)" title="Delete">
                      🗑️
                    </button>
                  </div>
                  <div class="media-info">
                    <span class="filename">{{ m.originalFilename }}</span>
                    <span class="filesize">{{ formatFileSize(m.fileSize) }}</span>
                  </div>
                </div>
              }
            </div>
          }
        </section>
      </div>
    </div>
  `,
    styles: [`
    .media-page {
      padding: 2rem 0;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .back-link {
      display: inline-block;
      color: var(--text-secondary);
      margin-bottom: 0.5rem;
      font-size: 0.875rem;
    }

    .back-link:hover {
      color: var(--color-primary-light);
    }

    .page-header h1 {
      margin-bottom: 0.25rem;
    }

    .page-header p {
      color: var(--text-secondary);
    }

    .upload-section {
      margin-bottom: 2rem;
    }

    .drop-zone {
      padding: 3rem;
      text-align: center;
      border: 2px dashed rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-lg);
      cursor: pointer;
      transition: all var(--transition-normal);
    }

    .drop-zone:hover {
      border-color: var(--color-primary);
      background: rgba(99, 102, 241, 0.05);
    }

    .drop-zone.drag-over {
      border-color: var(--color-primary);
      background: rgba(99, 102, 241, 0.1);
    }

    .upload-icon {
      font-size: 3rem;
      display: block;
      margin-bottom: 1rem;
    }

    .drop-zone p {
      color: var(--text-secondary);
      margin-bottom: 0.5rem;
    }

    .upload-hint {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .alert {
      padding: 1rem;
      border-radius: var(--radius-md);
      margin-top: 1rem;
    }

    .alert-error {
      background: rgba(239, 68, 68, 0.1);
      color: var(--color-error);
      border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .media-section h2 {
      margin-bottom: 1rem;
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 3rem;
    }

    .empty-state {
      text-align: center;
      padding: 3rem;
      background: var(--bg-card);
      border-radius: var(--radius-xl);
    }

    .empty-icon {
      font-size: 3rem;
      display: block;
      margin-bottom: 0.5rem;
    }

    .empty-state p {
      color: var(--text-secondary);
    }

    .media-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1rem;
    }

    .media-item {
      position: relative;
      aspect-ratio: 1;
      border-radius: var(--radius-lg);
      overflow: hidden;
      background: var(--bg-secondary);
    }

    .media-item img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .media-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity var(--transition-fast);
    }

    .media-item:hover .media-overlay {
      opacity: 1;
    }

    .btn-delete {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(239, 68, 68, 0.9);
      border: none;
      border-radius: var(--radius-full);
      cursor: pointer;
      font-size: 1.25rem;
      transition: transform var(--transition-fast);
    }

    .btn-delete:hover {
      transform: scale(1.1);
    }

    .media-info {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 0.75rem;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    }

    .filename {
      display: block;
      font-size: 0.75rem;
      color: white;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .filesize {
      font-size: 0.625rem;
      color: var(--text-muted);
    }
  `]
})
export class MediaManagerComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private mediaService = inject(MediaService);
    private productService = inject(ProductService);

    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    product = signal<Product | null>(null);
    media = signal<Media[]>([]);
    loading = signal(true);
    uploading = signal(false);
    isDragOver = signal(false);
    uploadError = signal<string | null>(null);

    productId: string | null = null;

    ngOnInit(): void {
        this.productId = this.route.snapshot.paramMap.get('id');
        if (this.productId) {
            this.loadProduct();
            this.loadMedia();
        }
    }

    loadProduct(): void {
        if (!this.productId) return;
        this.productService.getProduct(this.productId).subscribe({
            next: (product) => this.product.set(product)
        });
    }

    loadMedia(): void {
        if (!this.productId) return;
        this.loading.set(true);
        this.mediaService.loadMediaByProduct(this.productId).subscribe({
            next: (media) => {
                this.media.set(media);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver.set(true);
    }

    onDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver.set(false);
    }

    onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver.set(false);

        const files = event.dataTransfer?.files;
        if (files) {
            this.uploadFiles(Array.from(files));
        }
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            this.uploadFiles(Array.from(input.files));
        }
    }

    uploadFiles(files: File[]): void {
        if (!this.productId) return;

        this.uploadError.set(null);

        for (const file of files) {
            try {
                this.mediaService.validateFile(file);
                this.uploading.set(true);

                this.mediaService.uploadMedia(file, this.productId).subscribe({
                    next: (media) => {
                        this.media.update(m => [...m, media]);
                        this.uploading.set(false);
                    },
                    error: (err) => {
                        this.uploading.set(false);
                        this.uploadError.set(err.error?.message || 'Upload failed');
                    }
                });
            } catch (error: any) {
                this.uploadError.set(error.message);
            }
        }
    }

    deleteMedia(media: Media): void {
        if (confirm('Are you sure you want to delete this image?')) {
            this.mediaService.deleteMedia(media.id).subscribe({
                next: () => {
                    this.media.update(m => m.filter(item => item.id !== media.id));
                }
            });
        }
    }

    formatFileSize(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
}
