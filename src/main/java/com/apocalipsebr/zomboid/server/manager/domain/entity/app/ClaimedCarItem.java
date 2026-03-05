package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

@Entity
@Table(name = "claimed_car_items")
public class ClaimedCarItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_type", nullable = false)
    private String fullType;

    @Column(name = "count", nullable = false)
    private Integer count = 1;

    @Column(name = "container")
    private String container;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_car_id", nullable = false)
    private ClaimedCar claimedCar;

    public ClaimedCarItem() {
    }

    public ClaimedCarItem(String fullType, Integer count, String container) {
        this.fullType = fullType;
        this.count = count;
        this.container = container;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public ClaimedCar getClaimedCar() {
        return claimedCar;
    }

    public void setClaimedCar(ClaimedCar claimedCar) {
        this.claimedCar = claimedCar;
    }
}
