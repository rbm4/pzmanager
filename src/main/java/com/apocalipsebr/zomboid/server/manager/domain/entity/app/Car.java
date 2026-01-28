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

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String images; // Comma-separated image URLs

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean available = true;

    public Car() {
    }

    public Car(String name, String model, Integer value, String description, String images) {
        this.name = name;
        this.model = model;
        this.value = value;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
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
