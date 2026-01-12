import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Media } from '../../shared/models/media.model';

@Injectable({
    providedIn: 'root'
})
export class MediaService {
    private mediaSignal = signal<Media[]>([]);
    private uploadingSignal = signal<boolean>(false);

    media = this.mediaSignal.asReadonly();
    uploading = this.uploadingSignal.asReadonly();

    private readonly MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private readonly ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

    constructor(private http: HttpClient) { }

    loadMediaByProduct(productId: string): Observable<Media[]> {
        return this.http.get<Media[]>(`${environment.apiUrl}/media/product/${productId}`)
            .pipe(
                tap(media => this.mediaSignal.set(media))
            );
    }

    uploadMedia(file: File, productId: string): Observable<Media> {
        this.validateFile(file);

        const formData = new FormData();
        formData.append('file', file);
        formData.append('productId', productId);

        this.uploadingSignal.set(true);

        return this.http.post<Media>(`${environment.apiUrl}/media/upload`, formData)
            .pipe(
                tap(newMedia => {
                    this.mediaSignal.update(media => [...media, newMedia]);
                    this.uploadingSignal.set(false);
                })
            );
    }

    deleteMedia(id: string): Observable<void> {
        return this.http.delete<void>(`${environment.apiUrl}/media/${id}`)
            .pipe(
                tap(() => {
                    this.mediaSignal.update(media => media.filter(m => m.id !== id));
                })
            );
    }

    validateFile(file: File): void {
        if (file.size > this.MAX_FILE_SIZE) {
            throw new Error(`File size exceeds maximum limit of 2MB. Current size: ${(file.size / 1024 / 1024).toFixed(2)}MB`);
        }

        if (!this.ALLOWED_TYPES.includes(file.type)) {
            throw new Error(`Invalid file type: ${file.type}. Allowed types: JPEG, PNG, GIF, WebP`);
        }
    }

    isValidFile(file: File): boolean {
        return file.size <= this.MAX_FILE_SIZE && this.ALLOWED_TYPES.includes(file.type);
    }

    getMaxFileSize(): number {
        return this.MAX_FILE_SIZE;
    }

    getAllowedTypes(): string[] {
        return this.ALLOWED_TYPES;
    }
}
