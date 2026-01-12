package com.letsplay.gateway.filter;

import com.letsplay.gateway.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    // Routes that don't require authentication
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/products",
            "/api/media");

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            // Allow all OPTIONS requests (CORS preflight) and return OK immediately
            if ("OPTIONS".equals(method)) {
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            // Check if route is public
            if (isPublicRoute(path, method)) {
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user info and add to headers for downstream services
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicRoute(String path, String method) {
        // GET requests to products and media are public
        if ("GET".equals(method)) {
            if (path.startsWith("/api/products") || path.startsWith("/api/media")) {
                return true;
            }
        }

        // Auth endpoints are public
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
