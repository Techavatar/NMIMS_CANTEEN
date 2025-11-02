package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Food item data model with inventory tracking
 * Contains detailed information about menu items including inventory and sales data
 */
public class FoodItem implements Serializable {
    // Basic information
    private String itemId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private boolean isAvailable;
    private boolean isVegetarian;
    private boolean isFeatured;
    private double rating;
    private int reviewCount;
    private Date createdAt;
    private Date updatedAt;

    // Inventory fields
    private int stockQuantity;
    private int lowStockThreshold;
    private int reorderLevel;
    private double unitCost;
    private String supplierInfo;
    private String batchNumber;
    private Date expiryDate;
    private String storageConditions;

    // Sales tracking
    private int totalSold;
    private double revenueGenerated;
    private Date lastSoldDate;
    private int dailySold;
    private int weeklySold;
    private int monthlySold;

    // Additional fields
    private String ingredients;
    private String allergens;
    private int preparationTime; // in minutes
    private String nutritionalInfo;
    private double discountPercentage;
    private boolean isDiscounted;
    private Date discountExpiryDate;

    // Default constructor for Firebase
    public FoodItem() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isAvailable = true;
        this.isVegetarian = false;
        this.isFeatured = false;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.stockQuantity = 0;
        this.lowStockThreshold = 10;
        this.reorderLevel = 15;
        this.totalSold = 0;
        this.revenueGenerated = 0.0;
        this.dailySold = 0;
        this.weeklySold = 0;
        this.monthlySold = 0;
        this.preparationTime = 10;
        this.isDiscounted = false;
        this.discountPercentage = 0.0;
    }

    // Parameterized constructor
    public FoodItem(String itemId, String name, String description, double price, String category) {
        this();
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.updatedAt = new Date();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
        this.updatedAt = new Date();
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.updatedAt = new Date();
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(String supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public double getRevenueGenerated() {
        return revenueGenerated;
    }

    public void setRevenueGenerated(double revenueGenerated) {
        this.revenueGenerated = revenueGenerated;
    }

    public Date getLastSoldDate() {
        return lastSoldDate;
    }

    public void setLastSoldDate(Date lastSoldDate) {
        this.lastSoldDate = lastSoldDate;
    }

    public int getDailySold() {
        return dailySold;
    }

    public void setDailySold(int dailySold) {
        this.dailySold = dailySold;
    }

    public int getWeeklySold() {
        return weeklySold;
    }

    public void setWeeklySold(int weeklySold) {
        this.weeklySold = weeklySold;
    }

    public int getMonthlySold() {
        return monthlySold;
    }

    public void setMonthlySold(int monthlySold) {
        this.monthlySold = monthlySold;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getNutritionalInfo() {
        return nutritionalInfo;
    }

    public void setNutritionalInfo(String nutritionalInfo) {
        this.nutritionalInfo = nutritionalInfo;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isDiscounted() {
        return isDiscounted;
    }

    public void setDiscounted(boolean discounted) {
        isDiscounted = discounted;
    }

    public Date getDiscountExpiryDate() {
        return discountExpiryDate;
    }

    public void setDiscountExpiryDate(Date discountExpiryDate) {
        this.discountExpiryDate = discountExpiryDate;
    }

    /**
     * Gets the discounted price if applicable
     */
    public double getDiscountedPrice() {
        if (isDiscounted && discountPercentage > 0) {
            return price - (price * discountPercentage / 100);
        }
        return price;
    }

    /**
     * Gets the vegetarian icon text
     */
    public String getVegetarianIcon() {
        return isVegetarian ? "🟢" : "🔴";
    }

    /**
     * Decreases stock by specified quantity
     */
    public void decreaseStock(int quantity) {
        if (quantity > 0 && this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            this.totalSold += quantity;
            this.revenueGenerated += (price * quantity);
            this.lastSoldDate = new Date();
            this.updatedAt = new Date();
        }
    }

    /**
     * Increases stock by specified quantity (restocking)
     */
    public void increaseStock(int quantity) {
        if (quantity > 0) {
            this.stockQuantity += quantity;
            this.updatedAt = new Date();
        }
    }

    /**
     * Checks if item is low on stock
     */
    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold;
    }

    /**
     * Checks if item needs reordering
     */
    public boolean needsReorder() {
        return stockQuantity <= reorderLevel;
    }

    /**
     * Checks if item is out of stock
     */
    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    /**
     * Calculates profit margin
     */
    public double getProfitMargin() {
        if (unitCost > 0) {
            return ((price - unitCost) / price) * 100;
        }
        return 0;
    }

    /**
     * Adds a sale to the sales tracking
     */
    public void addSale(int quantity, double salePrice) {
        if (quantity > 0) {
            this.totalSold += quantity;
            this.revenueGenerated += salePrice;
            this.dailySold += quantity;
            this.weeklySold += quantity;
            this.monthlySold += quantity;
            this.lastSoldDate = new Date();
            this.decreaseStock(quantity);
        }
    }

    /**
     * Updates rating based on new review
     */
    public void updateRating(int newRating) {
        if (newRating >= 1 && newRating <= 5) {
            double totalRating = this.rating * this.reviewCount;
            this.reviewCount++;
            this.rating = (totalRating + newRating) / this.reviewCount;
            this.updatedAt = new Date();
        }
    }

    /**
     * Gets stock status as string
     */
    public String getStockStatus() {
        if (stockQuantity <= 0) {
            return "Out of Stock";
        } else if (stockQuantity <= lowStockThreshold) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }

    /**
     * Checks if discount is still valid
     */
    public boolean isDiscountValid() {
        if (!isDiscounted || discountExpiryDate == null) {
            return false;
        }
        return new Date().before(discountExpiryDate);
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", isAvailable=" + isAvailable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return itemId != null && itemId.equals(foodItem.itemId);
    }

    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }
}