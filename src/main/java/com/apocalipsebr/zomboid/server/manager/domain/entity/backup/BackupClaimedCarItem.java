package com.apocalipsebr.zomboid.server.manager.domain.entity.backup;

import jakarta.persistence.*;

@Entity
@Table(name = "claimed_car_items")
public class BackupClaimedCarItem {

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
    private BackupClaimedCar claimedCar;

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullType() {
        return fullType;
    }

    public Integer getCount() {
        return count;
    }

    public String getContainer() {
        return container;
    }

    public BackupClaimedCar getClaimedCar() {
        return claimedCar;
    }
}
