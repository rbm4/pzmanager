package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

@Entity
@Table(name = "zomboid_items")
public class ZomboidItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 500)
    private String icon;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String page;

    @Column(nullable = false, unique = true)
    private String itemId;

    @Column(nullable = false)
    private Boolean sellable = false;

    public ZomboidItem() {
    }

    public ZomboidItem(String category, String icon, String name, String page, String itemId) {
        this.category = category;
        this.icon = icon;
        this.name = name;
        this.page = page;
        this.itemId = itemId;
        this.sellable = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public void setSellable(Boolean sellable) {
        this.sellable = sellable;
    }
}
