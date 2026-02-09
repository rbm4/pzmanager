package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

@Entity
@Table(name = "region_custom_properties")
public class RegionCustomProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    public RegionCustomProperty() {
    }

    public RegionCustomProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public RegionCustomProperty(String name, String value, Region region) {
        this.name = name;
        this.value = value;
        this.region = region;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
}
