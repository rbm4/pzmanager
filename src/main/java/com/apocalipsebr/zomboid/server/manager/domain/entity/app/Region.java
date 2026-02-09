package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String categories; // Comma-separated categories

    @Column(nullable = false)
    private Integer x1;

    @Column(nullable = false)
    private Integer x2;

    @Column(nullable = false)
    private Integer y1;

    @Column(nullable = false)
    private Integer y2;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer z = 0;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean enabled = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean permanent = false;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RegionCustomProperty> customProperties = new ArrayList<>();

    public Region() {
    }

    public Region(String code, String name, String categories, Integer x1, Integer x2, Integer y1, Integer y2, Integer z, Boolean enabled, Boolean permanent) {
        this.code = code;
        this.name = name;
        this.categories = categories;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z = z;
        this.enabled = enabled;
        this.permanent = permanent;
    }

    // Utility: get categories as array
    public String[] getCategoryArray() {
        if (categories == null || categories.isEmpty()) {
            return new String[0];
        }
        return categories.split(",");
    }

    // Utility: set categories from array
    public void setCategoriesFromArray(String[] cats) {
        if (cats == null || cats.length == 0) {
            this.categories = "";
        } else {
            this.categories = String.join(",", cats);
        }
    }

    // Helper to manage bidirectional relationship
    public void addCustomProperty(RegionCustomProperty property) {
        customProperties.add(property);
        property.setRegion(this);
    }

    public void removeCustomProperty(RegionCustomProperty property) {
        customProperties.remove(property);
        property.setRegion(null);
    }

    public void clearCustomProperties() {
        for (RegionCustomProperty prop : new ArrayList<>(customProperties)) {
            removeCustomProperty(prop);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }
    public Integer getX1() { return x1; }
    public void setX1(Integer x1) { this.x1 = x1; }
    public Integer getX2() { return x2; }
    public void setX2(Integer x2) { this.x2 = x2; }
    public Integer getY1() { return y1; }
    public void setY1(Integer y1) { this.y1 = y1; }
    public Integer getY2() { return y2; }
    public void setY2(Integer y2) { this.y2 = y2; }
    public Integer getZ() { return z; }
    public void setZ(Integer z) { this.z = z; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getPermanent() { return permanent; }
    public void setPermanent(Boolean permanent) { this.permanent = permanent; }
    public List<RegionCustomProperty> getCustomProperties() { return customProperties; }
    public void setCustomProperties(List<RegionCustomProperty> customProperties) { this.customProperties = customProperties; }
}
