import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order, CheckoutRequest, ClientStats, SellerStats } from '../../shared/models/order.model';

@Injectable({
    providedIn: 'root'
})
export class OrderService {
    constructor(private http: HttpClient) {}

    // Checkout from cart
    checkout(request: CheckoutRequest): Observable<Order> {
        return this.http.post<Order>(`${environment.apiUrl}/orders/checkout`, request);
    }

    // Client: get own orders with optional search
    getMyOrders(search?: string): Observable<Order[]> {
        let params = new HttpParams();
        if (search) params = params.set('search', search);
        return this.http.get<Order[]>(`${environment.apiUrl}/orders/my`, { params });
    }

    cancelOrder(orderId: string): Observable<Order> {
        return this.http.patch<Order>(`${environment.apiUrl}/orders/${orderId}/cancel`, {});
    }

    getClientStats(): Observable<ClientStats> {
        return this.http.get<ClientStats>(`${environment.apiUrl}/orders/my/stats`);
    }

    // Seller: get orders for seller's products
    getSellerOrders(search?: string): Observable<Order[]> {
        let params = new HttpParams();
        if (search) params = params.set('search', search);
        return this.http.get<Order[]>(`${environment.apiUrl}/orders/seller`, { params });
    }

    updateOrderStatus(orderId: string, status: string): Observable<Order> {
        return this.http.patch<Order>(`${environment.apiUrl}/orders/${orderId}/status`, { status });
    }

    getSellerStats(): Observable<SellerStats> {
        return this.http.get<SellerStats>(`${environment.apiUrl}/orders/seller/stats`);
    }

    getOrder(id: string): Observable<Order> {
        return this.http.get<Order>(`${environment.apiUrl}/orders/${id}`);
    }
}
