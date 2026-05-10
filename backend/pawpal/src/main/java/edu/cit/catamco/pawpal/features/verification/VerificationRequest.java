package edu.cit.catamco.pawpal.features.verification;

import edu.cit.catamco.pawpal.features.auth.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private String idImageUrl;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String adminComment;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public VerificationRequest() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Status getStatus() { return status; }
    public String getIdImageUrl() { return idImageUrl; }
    public String getReason() { return reason; }
    public String getAdminComment() { return adminComment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setStatus(Status status) { this.status = status; }
    public void setIdImageUrl(String idImageUrl) { this.idImageUrl = idImageUrl; }
    public void setReason(String reason) { this.reason = reason; }
    public void setAdminComment(String adminComment) { this.adminComment = adminComment; }
}