import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cart, CartItemRequest } from '../../shared/models/cart.model';

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private cartSignal = signal<Cart | null>(null);

    cart = this.cartSignal.asReadonly();
    itemCount = computed(() => this.cartSignal()?.items?.length ?? 0);
    totalAmount = computed(() => this.cartSignal()?.totalAmount ?? 0);

    constructor(private http: HttpClient) {}

    loadCart(): Observable<Cart> {
        return this.http.get<Cart>(`${environment.apiUrl}/cart`).pipe(
            tap(cart => this.cartSignal.set(cart))
        );
    }

    addItem(item: CartItemRequest): Observable<Cart> {
        return this.http.post<Cart>(`${environment.apiUrl}/cart/items`, item).pipe(
            tap(cart => this.cartSignal.set(cart))
        );
    }

    updateItem(productId: string, quantity: number): Observable<Cart> {
        return this.http.put<Cart>(`${environment.apiUrl}/cart/items/${productId}`, { quantity }).pipe(
            tap(cart => this.cartSignal.set(cart))
        );
    }

    removeItem(productId: string): Observable<Cart> {
        return this.http.delete<Cart>(`${environment.apiUrl}/cart/items/${productId}`).pipe(
            tap(cart => this.cartSignal.set(cart))
        );
    }

    clearCart(): Observable<Cart> {
        return this.http.delete<Cart>(`${environment.apiUrl}/cart`).pipe(
            tap(cart => this.cartSignal.set(cart))
        );
    }
}
