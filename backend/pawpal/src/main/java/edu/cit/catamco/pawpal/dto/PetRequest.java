package edu.cit.catamco.pawpal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PetRequest {

    @NotBlank(message = "Pet name is required.")
    private String name;

    @NotBlank(message = "Pet type is required.")
    private String type;

    private String breed;

    @NotBlank(message = "Age is required.")
    private String age;

    private String gender;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Location is required.")
    private String location;

    private Double latitude;
    private Double longitude;
    private List<String> characteristics;

    private Boolean vaccinated;
    private Boolean neutered;
    private Boolean microchipped;
    private Boolean healthChecked;

    public String getName()                        { return name; }
    public String getType()                        { return type; }
    public String getBreed()                       { return breed; }
    public String getAge()                         { return age; }
    public String getGender()                      { return gender; }
    public String getDescription()                 { return description; }
    public String getLocation()                    { return location; }
    public Double getLatitude()                    { return latitude; }
    public Double getLongitude()                   { return longitude; }
    public List<String> getCharacteristics()       { return characteristics; }
    public Boolean getVaccinated()                 { return vaccinated; }
    public Boolean getNeutered()                   { return neutered; }
    public Boolean getMicrochipped()               { return microchipped; }
    public Boolean getHealthChecked()              { return healthChecked; }

    public void setName(String name)                        { this.name = name; }
    public void setType(String type)                        { this.type = type; }
    public void setBreed(String breed)                      { this.breed = breed; }
    public void setAge(String age)                          { this.age = age; }
    public void setGender(String gender)                    { this.gender = gender; }
    public void setDescription(String description)          { this.description = description; }
    public void setLocation(String location)                { this.location = location; }
    public void setLatitude(Double latitude)                { this.latitude = latitude; }
    public void setLongitude(Double longitude)              { this.longitude = longitude; }
    public void setCharacteristics(List<String> c)          { this.characteristics = c; }
    public void setVaccinated(Boolean vaccinated)           { this.vaccinated = vaccinated; }
    public void setNeutered(Boolean neutered)               { this.neutered = neutered; }
    public void setMicrochipped(Boolean microchipped)       { this.microchipped = microchipped; }
    public void setHealthChecked(Boolean healthChecked)     { this.healthChecked = healthChecked; }
}
