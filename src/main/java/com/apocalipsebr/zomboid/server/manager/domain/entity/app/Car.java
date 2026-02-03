package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "vehicle_script", nullable = false)
    private String vehicleScript;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "trunk_size")
    private Integer trunkSize;

    @Column(name = "seats")
    private Integer seats;

    @Column(name = "doors")
    private Integer doors;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String images; // Comma-separated image URLs

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean available = true;

    public Car() {
    }

    public Car(String name, String model, String vehicleScript, Integer value, Integer trunkSize, Integer seats, Integer doors, String description, String images) {
        this.name = name;
        this.model = model;
        this.vehicleScript = vehicleScript;
        this.value = value;
        this.trunkSize = trunkSize;
        this.seats = seats;
        this.doors = doors;
        this.description = description;
        this.images = images;
        this.available = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleScript() {
        return vehicleScript;
    }

    public void setVehicleScript(String vehicleScript) {
        this.vehicleScript = vehicleScript;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getTrunkSize() {
        return trunkSize;
    }

    public void setTrunkSize(Integer trunkSize) {
        this.trunkSize = trunkSize;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Integer getDoors() {
        return doors;
    }

    public void setDoors(Integer doors) {
        this.doors = doors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    // Utility method to get images as array
    public String[] getImageArray() {
        if (images == null || images.isEmpty()) {
            return new String[0];
        }
        return images.split(",");
    }
}
