package edu.cit.catamco.pawpal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType type;

    private String breed;

    @Column(nullable = false)
    private String age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    private Double latitude;
    private Double longitude;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus status = PetStatus.UNDER_REVIEW;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    // Personality traits (e.g. "Friendly", "Playful", "House-trained")
    @ElementCollection
    @CollectionTable(name = "pet_characteristics", joinColumns = @JoinColumn(name = "pet_id"))
    @Column(name = "characteristic")
    private List<String> characteristics;

    // Health & Care flags
    private Boolean vaccinated = false;
    private Boolean neutered = false;
    private Boolean microchipped = false;
    private Boolean healthChecked = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PetType { DOG, CAT, BIRD, RABBIT, OTHER }
    public enum Gender { MALE, FEMALE }
    public enum PetStatus { UNDER_REVIEW, AVAILABLE, PENDING, ADOPTED, REJECTED }

    public Pet() {}

    // Getters
    public String getAdminNote() { return adminNote; }
    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getName() { return name; }
    public PetType getType() { return type; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }
    public Gender getGender() { return gender; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getImageUrl() { return imageUrl; }
    public PetStatus getStatus() { return status; }
    public List<String> getCharacteristics() { return characteristics; }
    public Boolean getVaccinated() { return vaccinated; }
    public Boolean getNeutered() { return neutered; }
    public Boolean getMicrochipped() { return microchipped; }
    public Boolean getHealthChecked() { return healthChecked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setName(String name) { this.name = name; }
    public void setType(PetType type) { this.type = type; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(String age) { this.age = age; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(PetStatus status) { this.status = status; }
    public void setCharacteristics(List<String> characteristics) { this.characteristics = characteristics; }
    public void setVaccinated(Boolean vaccinated) { this.vaccinated = vaccinated; }
    public void setNeutered(Boolean neutered) { this.neutered = neutered; }
    public void setMicrochipped(Boolean microchipped) { this.microchipped = microchipped; }
    public void setHealthChecked(Boolean healthChecked) { this.healthChecked = healthChecked; }
}