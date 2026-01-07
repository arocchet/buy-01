export interface User {
    id: string;
    name: string;
    email: string;
    role: 'client' | 'seller';
    avatar?: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    role: 'client' | 'seller';
}

// Backend returns flat structure, not nested user object
export interface AuthResponse {
    token: string;
    type: string;
    id: string;
    email: string;
    name: string;
    role: 'client' | 'seller';
}
