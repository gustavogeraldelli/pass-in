package dev.gustavo.passin.security;

import dev.gustavo.passin.repository.OrganizerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final OrganizerRepository organizerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length());
            String organizerId = jwtService.getSubject(token);
            organizerRepository.findById(organizerId)
                    .map(OrganizerPrincipal::new)
                    .ifPresent(principal -> {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        catch (BadCredentialsException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
