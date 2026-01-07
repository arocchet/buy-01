import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../../shared/models/user.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly TOKEN_KEY = 'auth_token';
    private readonly USER_KEY = 'auth_user';

    private currentUserSignal = signal<User | null>(this.getStoredUser());

    currentUser = this.currentUserSignal.asReadonly();
    isAuthenticated = computed(() => !!this.currentUserSignal());
    isSeller = computed(() => this.currentUserSignal()?.role === 'seller');
    isClient = computed(() => this.currentUserSignal()?.role === 'client');

    constructor(
        private http: HttpClient,
        private router: Router
    ) { }

    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
            .pipe(
                tap(response => this.handleAuthSuccess(response)),
                catchError(error => {
                    console.error('Login error:', error);
                    return throwError(() => error);
                })
            );
    }

    register(data: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, data)
            .pipe(
                tap(response => this.handleAuthSuccess(response)),
                catchError(error => {
                    console.error('Registration error:', error);
                    return throwError(() => error);
                })
            );
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSignal.set(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    private handleAuthSuccess(response: AuthResponse): void {
        localStorage.setItem(this.TOKEN_KEY, response.token);

        // Convert flat response to User object
        const user: User = {
            id: response.id,
            name: response.name,
            email: response.email,
            role: response.role
        };

        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUserSignal.set(user);
    }

    private getStoredUser(): User | null {
        const userJson = localStorage.getItem(this.USER_KEY);
        if (userJson) {
            try {
                return JSON.parse(userJson);
            } catch {
                return null;
            }
        }
        return null;
    }

    uploadAvatar(userId: string, file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${environment.apiUrl}/users/${userId}/avatar`, formData);
    }
}
