package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Inventory tracking model
 * Contains comprehensive inventory management data for food items
 */
public class InventoryItem implements Serializable {
    // Basic information
    private String inventoryId;
    private String itemId;
    private String itemName;
    private String category;
    private String supplierName;
    private String supplierContact;
    private String supplierEmail;

    // Stock quantities
    private int currentStock;
    private int minimumStock;
    private int maximumStock;
    private int reorderPoint;
    private int reorderQuantity;

    // Supply chain information
    private String lastRestockDate;
    private int leadTimeDays;
    private String batchNumber;
    private String expiryDate;
    private String manufacturingDate;

    // Quality control
    private double storageTemperature;
    private String storageLocation;
    private String storageConditions;
    private String qualityStatus; // "Good", "Fair", "Poor", "Expired"

    // Movement tracking
    private int stockIn;
    private int stockOut;
    private int wasted;
    private int adjusted;
    private int sold;
    private String lastMovementDate;
    private String lastMovementType;
    private String lastMovementReason;

    // Cost analysis
    private double unitCost;
    private double totalValue;
    private double potentialLossValue;
    private double sellingPrice;
    private double profitMargin;

    // Alert settings
    private boolean lowStockAlert;
    private boolean expiryAlert;
    private boolean qualityAlert;
    private int lowStockThreshold;
    private int expiryWarningDays;
    private String alertLevel; // "Low", "Medium", "High", "Critical"

    // Additional tracking
    private List<InventoryMovement> movementHistory;
    private String lastVerifiedDate;
    private String verifiedBy;
    private String notes;
    private boolean isActive;
    private String barcode;
    private String qrCode;

    // Default constructor for Firebase
    public InventoryItem() {
        this.currentStock = 0;
        this.minimumStock = 10;
        this.maximumStock = 100;
        this.reorderPoint = 15;
        this.reorderQuantity = 50;
        this.leadTimeDays = 7;
        this.lowStockAlert = false;
        this.expiryAlert = false;
        this.qualityAlert = false;
        this.lowStockThreshold = 10;
        this.expiryWarningDays = 7;
        this.qualityStatus = "Good";
        this.alertLevel = "Low";
        this.isActive = true;
        this.movementHistory = new ArrayList<>();
        this.lastMovementDate = new Date().toString();
    }

    // Parameterized constructor
    public InventoryItem(String inventoryId, String itemId, String itemName) {
        this();
        this.inventoryId = inventoryId;
        this.itemId = itemId;
        this.itemName = itemName;
    }

    // Inner class for tracking inventory movements
    public static class InventoryMovement implements Serializable {
        private String movementId;
        private Date timestamp;
        private String movementType; // "STOCK_IN", "STOCK_OUT", "SALE", "WASTE", "ADJUSTMENT"
        private int quantity;
        private int previousStock;
        private int newStock;
        private String reason;
        private String performedBy;
        private String referenceId; // Order ID, batch number, etc.
        private double costImpact;

        public InventoryMovement() {
            this.timestamp = new Date();
        }

        public InventoryMovement(String movementType, int quantity, int previousStock, String reason, String performedBy) {
            this();
            this.movementType = movementType;
            this.quantity = quantity;
            this.previousStock = previousStock;
            this.newStock = previousStock + quantity;
            this.reason = reason;
            this.performedBy = performedBy;
        }

        // Getters and Setters
        public String getMovementId() { return movementId; }
        public void setMovementId(String movementId) { this.movementId = movementId; }

        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public int getPreviousStock() { return previousStock; }
        public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }

        public int getNewStock() { return newStock; }
        public void setNewStock(int newStock) { this.newStock = newStock; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public String getReferenceId() { return referenceId; }
        public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

        public double getCostImpact() { return costImpact; }
        public void setCostImpact(double costImpact) { this.costImpact = costImpact; }
    }

    // Getters and Setters
    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
        calculateTotalValue();
        checkAlertLevels();
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
        checkAlertLevels();
    }

    public int getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(int maximumStock) {
        this.maximumStock = maximumStock;
    }

    public int getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(int reorderPoint) {
        this.reorderPoint = reorderPoint;
        checkAlertLevels();
    }

    public int getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(int reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getLastRestockDate() {
        return lastRestockDate;
    }

    public void setLastRestockDate(String lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(int leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        checkExpiryAlert();
    }

    public String getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(String manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public double getStorageTemperature() {
        return storageTemperature;
    }

    public void setStorageTemperature(double storageTemperature) {
        this.storageTemperature = storageTemperature;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
        checkQualityAlert();
    }

    public int getStockIn() {
        return stockIn;
    }

    public void setStockIn(int stockIn) {
        this.stockIn = stockIn;
    }

    public int getStockOut() {
        return stockOut;
    }

    public void setStockOut(int stockOut) {
        this.stockOut = stockOut;
    }

    public int getWasted() {
        return wasted;
    }

    public void setWasted(int wasted) {
        this.wasted = wasted;
    }

    public int getAdjusted() {
        return adjusted;
    }

    public void setAdjusted(int adjusted) {
        this.adjusted = adjusted;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public String getLastMovementDate() {
        return lastMovementDate;
    }

    public void setLastMovementDate(String lastMovementDate) {
        this.lastMovementDate = lastMovementDate;
    }

    public String getLastMovementType() {
        return lastMovementType;
    }

    public void setLastMovementType(String lastMovementType) {
        this.lastMovementType = lastMovementType;
    }

    public String getLastMovementReason() {
        return lastMovementReason;
    }

    public void setLastMovementReason(String lastMovementReason) {
        this.lastMovementReason = lastMovementReason;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
        calculateTotalValue();
        calculateProfitMargin();
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getPotentialLossValue() {
        return potentialLossValue;
    }

    public void setPotentialLossValue(double potentialLossValue) {
        this.potentialLossValue = potentialLossValue;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
        calculateProfitMargin();
    }

    public double getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(double profitMargin) {
        this.profitMargin = profitMargin;
    }

    public boolean isLowStockAlert() {
        return lowStockAlert;
    }

    public void setLowStockAlert(boolean lowStockAlert) {
        this.lowStockAlert = lowStockAlert;
    }

    public boolean isExpiryAlert() {
        return expiryAlert;
    }

    public void setExpiryAlert(boolean expiryAlert) {
        this.expiryAlert = expiryAlert;
    }

    public boolean isQualityAlert() {
        return qualityAlert;
    }

    public void setQualityAlert(boolean qualityAlert) {
        this.qualityAlert = qualityAlert;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
        checkAlertLevels();
    }

    public int getExpiryWarningDays() {
        return expiryWarningDays;
    }

    public void setExpiryWarningDays(int expiryWarningDays) {
        this.expiryWarningDays = expiryWarningDays;
        checkExpiryAlert();
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public List<InventoryMovement> getMovementHistory() {
        return movementHistory;
    }

    public void setMovementHistory(List<InventoryMovement> movementHistory) {
        this.movementHistory = movementHistory != null ? movementHistory : new ArrayList<>();
    }

    public String getLastVerifiedDate() {
        return lastVerifiedDate;
    }

    public void setLastVerifiedDate(String lastVerifiedDate) {
        this.lastVerifiedDate = lastVerifiedDate;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    /**
     * Calculates total value of current stock
     */
    public void calculateTotalValue() {
        this.totalValue = currentStock * unitCost;
        calculatePotentialLoss();
    }

    /**
     * Calculates potential loss if all stock expires
     */
    public void calculatePotentialLoss() {
        this.potentialLossValue = currentStock * unitCost;
    }

    /**
     * Calculates profit margin
     */
    public void calculateProfitMargin() {
        if (unitCost > 0 && sellingPrice > 0) {
            this.profitMargin = ((sellingPrice - unitCost) / sellingPrice) * 100;
        }
    }

    /**
     * Adds stock movement to history
     */
    public void addMovement(String movementType, int quantity, String reason, String performedBy, String referenceId) {
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementId("MOV_" + System.currentTimeMillis());
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(this.currentStock);
        movement.setNewStock(this.currentStock + quantity);
        movement.setReason(reason);
        movement.setPerformedBy(performedBy);
        movement.setReferenceId(referenceId);
        movement.setCostImpact(quantity * unitCost);

        this.movementHistory.add(0, movement); // Add to beginning of list

        // Update current stock
        this.currentStock = movement.getNewStock();
        this.lastMovementDate = movement.getTimestamp().toString();
        this.lastMovementType = movementType;
        this.lastMovementReason = reason;

        // Update movement counters
        switch (movementType) {
            case "STOCK_IN":
                this.stockIn += quantity;
                break;
            case "STOCK_OUT":
                this.stockOut += Math.abs(quantity);
                break;
            case "SALE":
                this.sold += Math.abs(quantity);
                break;
            case "WASTE":
                this.wasted += Math.abs(quantity);
                break;
            case "ADJUSTMENT":
                this.adjusted += quantity;
                break;
        }

        // Recalculate totals and check alerts
        calculateTotalValue();
        checkAlertLevels();
    }

    /**
     * Checks stock levels and updates alerts
     */
    public void checkAlertLevels() {
        this.lowStockAlert = false;

        if (currentStock <= 0) {
            this.alertLevel = "Critical";
            this.lowStockAlert = true;
        } else if (currentStock <= lowStockThreshold) {
            this.alertLevel = "High";
            this.lowStockAlert = true;
        } else if (currentStock <= reorderPoint) {
            this.alertLevel = "Medium";
        } else {
            this.alertLevel = "Low";
        }
    }

    /**
     * Checks expiry date and updates alerts
     */
    public void checkExpiryAlert() {
        if (expiryDate != null && !expiryDate.isEmpty()) {
            try {
                // Simple expiry check (would need proper date parsing in real implementation)
                this.expiryAlert = false; // Would calculate based on actual date comparison
            } catch (Exception e) {
                // Handle date parsing error
            }
        }
    }

    /**
     * Checks quality status and updates alerts
     */
    public void checkQualityAlert() {
        this.qualityAlert = !"Good".equals(qualityStatus);
    }

    /**
     * Gets days until expiry
     */
    public int getDaysToExpiry() {
        // Simplified implementation - would need proper date parsing
        return 30; // Placeholder
    }

    /**
     * Checks if item is expiring soon
     */
    public boolean isExpiringSoon() {
        return getDaysToExpiry() <= expiryWarningDays;
    }

    /**
     * Checks if item needs reordering
     */
    public boolean needsReorder() {
        return currentStock <= reorderPoint;
    }

    /**
     * Gets stock status as display text
     */
    public String getStockStatus() {
        if (currentStock <= 0) {
            return "Out of Stock";
        } else if (currentStock <= lowStockThreshold) {
            return "Low Stock";
        } else if (currentStock <= reorderPoint) {
            return "Reorder Soon";
        } else {
            return "In Stock";
        }
    }

    /**
     * Gets stock level percentage for display
     */
    public int getStockLevelPercentage() {
        if (maximumStock > 0) {
            return (int) ((double) currentStock / maximumStock * 100);
        }
        return 0;
    }

    /**
     * Calculates turnover rate
     */
    public double getTurnoverRate() {
        if (currentStock > 0 && stockIn > 0) {
            return (double) sold / currentStock;
        }
        return 0;
    }

    /**
     * Gets waste percentage
     */
    public double getWastePercentage() {
        int totalMovement = stockIn + stockOut + wasted + adjusted;
        if (totalMovement > 0) {
            return (double) wasted / totalMovement * 100;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemName='" + itemName + '\'' +
                ", currentStock=" + currentStock +
                ", alertLevel='" + alertLevel + '\'' +
                ", totalValue=" + totalValue +
                '}';
    }
}