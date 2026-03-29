package edu.cit.catamco.pawpal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_requests")
public class AdoptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adopter_id", nullable = false)
    private User adopter;

    @Column(nullable = false)
    private String adopterName;

    @Column(nullable = false)
    private String contactInfo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String noteToOwner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum RequestStatus { PENDING, APPROVED, DECLINED }

    public AdoptionRequest() {}

    public Long getId() { return id; }
    public Pet getPet() { return pet; }
    public User getAdopter() { return adopter; }
    public String getAdopterName() { return adopterName; }
    public String getContactInfo() { return contactInfo; }
    public String getReason() { return reason; }
    public String getNoteToOwner() { return noteToOwner; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPet(Pet pet) { this.pet = pet; }
    public void setAdopter(User adopter) { this.adopter = adopter; }
    public void setAdopterName(String adopterName) { this.adopterName = adopterName; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public void setReason(String reason) { this.reason = reason; }
    public void setNoteToOwner(String noteToOwner) { this.noteToOwner = noteToOwner; }
    public void setStatus(RequestStatus status) { this.status = status; }
}