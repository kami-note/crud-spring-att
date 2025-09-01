package com.kryptforge.crud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptforge.crud.exception.ErrorDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.lang.IllegalArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                handleJwtException(response, HttpStatus.UNAUTHORIZED, "Token Expirado", e.getMessage());
                return;
            } catch (MalformedJwtException e) {
                handleJwtException(response, HttpStatus.BAD_REQUEST, "Token Inválido: Formato Malformado", e.getMessage());
                return;
            } catch (SignatureException e) {
                handleJwtException(response, HttpStatus.UNAUTHORIZED, "Token Inválido: Assinatura Inválida", e.getMessage());
                return;
            } catch (UnsupportedJwtException e) {
                handleJwtException(response, HttpStatus.BAD_REQUEST, "Token Inválido: Token Não Suportado", e.getMessage());
                return;
            } catch (IllegalArgumentException e) {
                handleJwtException(response, HttpStatus.BAD_REQUEST, "Token Inválido: Argumento Ilegal", e.getMessage());
                return;
            } catch (Exception e) {
                handleJwtException(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno do Servidor", e.getMessage());
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, HttpStatus status, String message, String details) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), message, details);
        new ObjectMapper().writeValue(response.getWriter(), errorDetails);
    }
}
