package com.soufyan.userservice.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.soufyan.userservice.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column (name = "first_name", nullable = false)
    private String firstName;
    @Column (name = "last_name", nullable = false)
    private String lastName;
    @Column (unique = true, nullable = false)
    private String email;
    @Column ( nullable = false)
    private String phone;
    @Column (nullable = false)  
    private String password;
    @Column (nullable = false)
    private String country;
    @Column (nullable = false)
    private String city;
    @Column (nullable = false)
    private String street;
    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;  
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }
    @Override
    public String getUsername() {
        return this.getEmail();
    }

    public Role getRole() {
        return this.role;
    }

}
