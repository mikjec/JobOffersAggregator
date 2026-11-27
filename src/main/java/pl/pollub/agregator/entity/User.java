package pl.pollub.agregator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.pollub.agregator.dto.UserDTO;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "`user`")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
        this.role = Role.USER;
    }

    public UserDTO toSnapshot() {
        return UserDTO.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .role(this.role)
                .build();
    }

    public UserDTO toDTO() {
        return toSnapshot();
    }

    public static User fromDTO(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(dto.getRole() != null ? dto.getRole() : Role.USER)
                .build();
    }

    public void restoreFromSnapshot(UserDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot for User entity (id=" + this.id + ") must not be null.");
        }
        this.id = snapshot.getId();
        this.username = snapshot.getUsername();
        this.email = snapshot.getEmail();
        if (snapshot.getRole() != null) {
            this.role = snapshot.getRole();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role effectiveRole = this.role != null ? this.role : Role.USER;
        return List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "', role=" + role + "}";
    }
}
