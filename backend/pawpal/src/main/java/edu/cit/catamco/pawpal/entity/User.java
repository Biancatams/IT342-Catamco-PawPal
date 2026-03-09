package edu.cit.catamco.pawpal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Role {
        ADOPTER, PET_OWNER
    }

    public User() {}

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fullName;
        private String email;
        private String password;
        private Role role;

        public Builder fullName(String fullName) {
            this.fullName = fullName; return this;
        }
        public Builder email(String email) {
            this.email = email; return this;
        }
        public Builder password(String password) {
            this.password = password; return this;
        }
        public Builder role(Role role) {
            this.role = role; return this;
        }
        public User build() {
            User user = new User();
            user.fullName = this.fullName;
            user.email = this.email;
            user.password = this.password;
            user.role = this.role;
            return user;
        }
    }
}