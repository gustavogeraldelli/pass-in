package dev.gustavo.passin.security;

import dev.gustavo.passin.repository.OrganizerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizerUserDetailsService implements UserDetailsService {

    private final OrganizerRepository organizerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return organizerRepository.findByEmail(username)
                .map(OrganizerPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
}
