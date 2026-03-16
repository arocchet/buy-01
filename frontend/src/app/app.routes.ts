import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { sellerGuard } from './core/guards/seller.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./features/products/product-list/product-list.component')
            .then(m => m.ProductListComponent)
    },
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component')
            .then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component')
            .then(m => m.RegisterComponent)
    },
    {
        path: 'products',
        loadComponent: () => import('./features/products/product-list/product-list.component')
            .then(m => m.ProductListComponent)
    },
    {
        path: 'products/:id',
        loadComponent: () => import('./features/products/product-detail/product-detail.component')
            .then(m => m.ProductDetailComponent)
    },
    {
        path: 'cart',
        canActivate: [authGuard],
        loadComponent: () => import('./features/cart/cart.component')
            .then(m => m.CartComponent)
    },
    {
        path: 'orders',
        canActivate: [authGuard],
        loadComponent: () => import('./features/orders/orders.component')
            .then(m => m.OrdersComponent)
    },
    {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () => import('./features/profile/profile.component')
            .then(m => m.ProfileComponent)
    },
    {
        path: 'seller',
        canActivate: [authGuard, sellerGuard],
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./features/seller/dashboard/dashboard.component')
                    .then(m => m.DashboardComponent)
            },
            {
                path: 'products/new',
                loadComponent: () => import('./features/seller/product-form/product-form.component')
                    .then(m => m.ProductFormComponent)
            },
            {
                path: 'products/:id/edit',
                loadComponent: () => import('./features/seller/product-form/product-form.component')
                    .then(m => m.ProductFormComponent)
            },
            {
                path: 'products/:id/media',
                loadComponent: () => import('./features/seller/media-manager/media-manager.component')
                    .then(m => m.MediaManagerComponent)
            },
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];

