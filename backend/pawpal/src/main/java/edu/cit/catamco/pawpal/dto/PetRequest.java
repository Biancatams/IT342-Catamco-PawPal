package edu.cit.catamco.pawpal.dto;

import java.util.List;

public class PetRequest {

    private String name;
    private String type;
    private String breed;
    private String age;
    private String gender;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> characteristics;

    public String getName() { return name; }
    public String getType() { return type; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public List<String> getCharacteristics() { return characteristics; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setCharacteristics(List<String> characteristics) { this.characteristics = characteristics; }
}