package com.quizapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filter that protects Swagger UI and OpenAPI documentation with HTTP Basic Authentication.
 * Only intercepts /swagger-ui, /swagger-ui.html, and /v3/api-docs, leaving all /api endpoints untouched.
 */
@Component
@Order(1)
public class SwaggerBasicAuthFilter extends OncePerRequestFilter {

    @Value("${swagger.auth.enabled:true}")
    private boolean authEnabled;

    @Value("${swagger.auth.username:premkumar}")
    private String swaggerUsername;

    @Value("${swagger.auth.password:prem$kumar}")
    private String swaggerPassword;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (authEnabled && isSwaggerPath(path)) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
                try {
                    String base64Credentials = authHeader.substring(6).trim();
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    final String[] values = credentials.split(":", 2);

                    if (values.length == 2 &&
                            swaggerUsername.equals(values[0]) &&
                            swaggerPassword.equals(values[1])) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception ignored) {
                    // Invalid base64 or format, fall through to 401 challenge
                }
            }

            // Challenge the browser with Basic Auth popup modal
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Swagger Documentation\"");
            response.setContentType("text/plain");
            response.getWriter().write("401 Unauthorized: Valid credentials required to access Swagger UI.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerPath(String path) {
        return path != null && (
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html")
        );
    }
}
