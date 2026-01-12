export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    quantity: number;
    userId: string;
}

export interface ProductRequest {
    name: string;
    description: string;
    price: number;
    quantity: number;
}
