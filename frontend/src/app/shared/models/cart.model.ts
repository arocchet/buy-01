export interface CartItem {
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    sellerId: string;
    subtotal?: number;
}

export interface Cart {
    id: string;
    userId: string;
    items: CartItem[];
    totalAmount: number;
}

export interface CartItemRequest {
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    sellerId: string;
}
