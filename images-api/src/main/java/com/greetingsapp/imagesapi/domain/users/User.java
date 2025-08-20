package com.greetingsapp.imagesapi.domain.users;

import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends AuditableBaseEntity implements UserDetails {


    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;

    // Con @Enumerated(EnumType.STRING), JPA guardará los roles como texto
    // ("ADMIN", "CLIENT") en la base de datos, lo cual es muy legible.
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;


    // --- Métodos de UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Devuelve el rol del usuario en el formato que Spring Security espera.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Los siguientes métodos son para controlar el estado de la cuenta.
    // Para este caso, los dejamos siempre en 'true'.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
