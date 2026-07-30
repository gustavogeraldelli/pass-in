package dev.gustavo.passin.security;

import dev.gustavo.passin.entity.Organizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class OrganizerPrincipal implements UserDetails {

    private final Organizer organizer;

    public OrganizerPrincipal(Organizer organizer) {
        this.organizer = organizer;
    }

    public String getId() {
        return organizer.getId();
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
    }

    @Override
    public String getPassword() {
        return organizer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return organizer.getEmail();
    }
}
