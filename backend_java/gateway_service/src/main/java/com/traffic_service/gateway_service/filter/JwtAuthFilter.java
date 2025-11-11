package com.traffic_service.gateway_service.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component("JwtAuthFilter") // must match filter name in application.properties
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            System.out.println("🔹 Incoming path: " + path);

            // ✅ Skip auth for open endpoints (login and register only)
            if (path.equals("/api/auth/login") || path.equals("/api/auth/register") || 
                path.equals("/auth/login") || path.equals("/auth/register")) {
                return chain.filter(exchange);
            }

            // ✅ Validate JWT header
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

                // ✅ Forward username header downstream
                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.add("X-Username", claims.getSubject());
                            h.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                        }))
                        .build();

                return chain.filter(mutated);
            } catch (Exception e) {
                e.printStackTrace();
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {}
}
