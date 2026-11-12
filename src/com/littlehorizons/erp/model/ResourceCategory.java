package com.littlehorizons.erp.model;

/**
 * Domain entity representing a resource category (book, lab equipment, etc.).
 */
public class ResourceCategory {
    private int categoryId;
    private String categoryName;
    private String description;

    public ResourceCategory() {
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s", categoryId, categoryName);
    }
}
