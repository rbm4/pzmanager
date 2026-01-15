package com.apocalipsebr.zomboid.server.manager.presentation.dto;

public class ItemDTO {
    private String icon;
    private String name;
    private String page;
    private String itemId;

    public ItemDTO() {
    }

    public ItemDTO(String icon, String name, String page, String itemId) {
        this.icon = icon;
        this.name = name;
        this.page = page;
        this.itemId = itemId;
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
}
