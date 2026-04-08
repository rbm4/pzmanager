package com.apocalipsebr.zomboid.server.manager.presentation.dto;

public class SellableBatchItemDTO {
    private String itemId;
    private String itemName;
    private Integer value;
    private Boolean sellable;
    private String storeDescription;
    private String category;
    private String icon;

    public SellableBatchItemDTO() {
    }

    public SellableBatchItemDTO(String itemId, Integer value, Boolean sellable, String storeDescription, String category, String icon) {
        this.itemId = itemId;
        this.value = value;
        this.sellable = sellable;
        this.storeDescription = storeDescription;
        this.category = category;
        this.icon = icon;
    }

    public String getItemName(){
        return itemName;
    }

    public void setItemName(String itemName){
        this.itemName = itemName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public void setSellable(Boolean sellable) {
        this.sellable = sellable;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon(){
        return this.icon;
    }

    public void setIcon(String icon){
        this.icon = icon;
    }
}
