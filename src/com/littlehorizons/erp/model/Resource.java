package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a loanable school resource (book, microscope, tablet, etc.).
 */
public class Resource {
    private int resourceId;
    private String resourceCode;
    private String resourceName;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private String resourceStatus;
    private String categoryName;

    public Resource() {
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s - %s | Cat: %s | Ubicación: %s | Estado: %s",
                resourceId, resourceCode, resourceName,
                categoryName != null ? categoryName : String.valueOf(categoryId),
                physicalLocation != null ? physicalLocation : "-", resourceStatus);
    }
}
