export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface OrderItem {
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    sellerId: string;
    subtotal?: number;
}

export interface Order {
    id: string;
    clientId: string;
    items: OrderItem[];
    status: OrderStatus;
    paymentMethod: string;
    totalAmount: number;
    shippingAddress: string;
    createdAt: string;
    updatedAt: string;
}

export interface CheckoutRequest {
    shippingAddress: string;
    paymentMethod?: string;
}

export interface ClientStats {
    totalOrders: number;
    totalSpent: number;
    mostBoughtProducts: { productId: string; totalQuantity: number }[];
}

export interface SellerStats {
    totalOrdersReceived: number;
    totalRevenue: number;
    bestSellingProducts: { productId: string; totalSold: number }[];
}
