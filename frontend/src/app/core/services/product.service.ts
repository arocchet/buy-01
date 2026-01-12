import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Product, ProductRequest } from '../../shared/models/product.model';

@Injectable({
    providedIn: 'root'
})
export class ProductService {
    private productsSignal = signal<Product[]>([]);
    private loadingSignal = signal<boolean>(false);

    products = this.productsSignal.asReadonly();
    loading = this.loadingSignal.asReadonly();

    constructor(private http: HttpClient) { }

    loadProducts(): Observable<Product[]> {
        this.loadingSignal.set(true);
        return this.http.get<Product[]>(`${environment.apiUrl}/products`)
            .pipe(
                tap(products => {
                    this.productsSignal.set(products);
                    this.loadingSignal.set(false);
                })
            );
    }

    getProduct(id: string): Observable<Product> {
        return this.http.get<Product>(`${environment.apiUrl}/products/${id}`);
    }

    getProductsByUser(userId: string): Observable<Product[]> {
        return this.http.get<Product[]>(`${environment.apiUrl}/products/user/${userId}`);
    }

    searchProducts(name: string): Observable<Product[]> {
        return this.http.get<Product[]>(`${environment.apiUrl}/products/search?name=${name}`);
    }

    createProduct(product: ProductRequest): Observable<Product> {
        return this.http.post<Product>(`${environment.apiUrl}/products`, product)
            .pipe(
                tap(newProduct => {
                    this.productsSignal.update(products => [...products, newProduct]);
                })
            );
    }

    updateProduct(id: string, product: ProductRequest): Observable<Product> {
        return this.http.put<Product>(`${environment.apiUrl}/products/${id}`, product)
            .pipe(
                tap(updatedProduct => {
                    this.productsSignal.update(products =>
                        products.map(p => p.id === id ? updatedProduct : p)
                    );
                })
            );
    }

    deleteProduct(id: string): Observable<void> {
        return this.http.delete<void>(`${environment.apiUrl}/products/${id}`)
            .pipe(
                tap(() => {
                    this.productsSignal.update(products =>
                        products.filter(p => p.id !== id)
                    );
                })
            );
    }
}
