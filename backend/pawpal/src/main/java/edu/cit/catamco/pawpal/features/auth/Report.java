package edu.cit.catamco.pawpal.features.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    private String reason;
    private String status = "PENDING";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getReporter() { return reporter; }
    public User getReportedUser() { return reportedUser; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setReporter(User reporter) { this.reporter = reporter; }
    public void setReportedUser(User reportedUser) { this.reportedUser = reportedUser; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }

    @ManyToOne
    @JoinColumn(name = "adoption_request_id")
    private edu.cit.catamco.pawpal.features.adoption.AdoptionRequest adoptionRequest;

    public edu.cit.catamco.pawpal.features.adoption.AdoptionRequest getAdoptionRequest() { return adoptionRequest; }
    public void setAdoptionRequest(edu.cit.catamco.pawpal.features.adoption.AdoptionRequest adoptionRequest) { this.adoptionRequest = adoptionRequest; }
}