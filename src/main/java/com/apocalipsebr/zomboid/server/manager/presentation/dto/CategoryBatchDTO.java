package com.apocalipsebr.zomboid.server.manager.presentation.dto;

import java.util.List;

public class CategoryBatchDTO {
    private String category;
    private List<ItemDTO> items;

    public CategoryBatchDTO() {
    }

    public CategoryBatchDTO(String category, List<ItemDTO> items) {
        this.category = category;
        this.items = items;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }
}
