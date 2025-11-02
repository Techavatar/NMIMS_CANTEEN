package com.nmims.canteen.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.Query;
import com.nmims.canteen.models.InventoryItem;
import com.nmims.canteen.models.AdminNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inventory management and stock tracking utility
 * Handles inventory operations, monitoring, and management
 */
public class InventoryManager {
    private static final String TAG = "InventoryManager";
    private static InventoryManager instance;

    // Real-time monitoring
    private final Map<String, InventoryItem> inventoryCache;
    private final Map<String, InventoryChangeListener> listeners;
    private final List<InventoryAlert> activeAlerts;
    private boolean isMonitoringActive;

    // Default thresholds
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int DEFAULT_REORDER_LEVEL = 15;
    private static final int DEFAULT_EXPIRY_WARNING_DAYS = 7;

    private InventoryManager() {
        this.inventoryCache = new ConcurrentHashMap<>();
        this.listeners = new HashMap<>();
        this.activeAlerts = Collections.synchronizedList(new ArrayList<>());
        this.isMonitoringActive = false;
    }

    /**
     * Get singleton instance
     */
    public static synchronized InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    /**
     * Inventory change listener interface
     */
    public interface InventoryChangeListener {
        void onInventoryChanged(InventoryItem item);
        void onLowStockAlert(InventoryItem item);
        void onExpiryAlert(InventoryItem item);
        void onStockUpdated(InventoryItem item, String movementType, int quantity);
        void onNewItemAdded(InventoryItem item);
        void onItemRemoved(InventoryItem item);
    }

    /**
     * Inventory alert class
     */
    public static class InventoryAlert {
        public enum AlertType {
            LOW_STOCK("Low Stock Alert", AdminNotification.NotificationType.LOW_STOCK),
            EXPIRY("Expiry Alert", AdminNotification.NotificationType.EXPIRY_ALERT),
            OUT_OF_STOCK("Out of Stock Alert", AdminNotification.NotificationType.LOW_STOCK),
            QUALITY("Quality Issue Alert", AdminNotification.NotificationType.QUALITY_ISSUE);

            private final String title;
            private final AdminNotification.NotificationType notificationType;

            AlertType(String title, AdminNotification.NotificationType notificationType) {
                this.title = title;
                this.notificationType = notificationType;
            }

            public String getTitle() { return title; }
            public AdminNotification.NotificationType getNotificationType() { return notificationType; }
        }

        private String alertId;
        private String itemId;
        private String itemName;
        private AlertType alertType;
        private String message;
        private Date createdAt;
        private boolean isResolved;
        private Date resolvedAt;
        private AdminNotification.Priority priority;
        private String actionRequired;
        private String actionUrl;

        public InventoryAlert(String itemId, String itemName, AlertType alertType, String message) {
            this.alertId = generateAlertId();
            this.itemId = itemId;
            this.itemName = itemName;
            this.alertType = alertType;
            this.message = message;
            this.createdAt = new Date();
            this.isResolved = false;
            this.priority = determinePriority(alertType);
        }

        // Getters and Setters
        public String getAlertId() { return alertId; }
        public String getItemId() { return itemId; }
        public String getItemName() { return itemName; }
        public AlertType getAlertType() { return alertType; }
        public String getMessage() { return message; }
        public Date getCreatedAt() { return createdAt; }
        public boolean isResolved() { return isResolved; }
        public void setResolved(boolean resolved) {
            isResolved = resolved;
            if (resolved) resolvedAt = new Date();
        }
        public Date getResolvedAt() { return resolvedAt; }
        public AdminNotification.Priority getPriority() { return priority; }
        public String getActionRequired() { return actionRequired; }
        public void setActionRequired(String actionRequired) { this.actionRequired = actionRequired; }
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

        private AdminNotification.Priority determinePriority(AlertType type) {
            switch (type) {
                case OUT_OF_STOCK: return AdminNotification.Priority.URGENT;
                case LOW_STOCK: return AdminNotification.Priority.HIGH;
                case EXPIRY: return AdminNotification.Priority.HIGH;
                case QUALITY: return AdminNotification.Priority.MEDIUM;
                default: return AdminNotification.Priority.LOW;
            }
        }

        private String generateAlertId() {
            return "INV_ALERT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
    }

    /**
     * Stock movement callback interface
     */
    public interface StockMovementCallback {
        void onSuccess(InventoryItem item);
        void onFailure(String error);
    }

    /**
     * Inventory analysis callback interface
     */
    public interface InventoryAnalysisCallback {
        void onSuccess(Map<String, Object> analysis);
        void onFailure(String error);
    }

    // Core Inventory Operations

    /**
     * Update stock for an item
     */
    public void updateStock(String itemId, int quantity, String reason, String performedBy, StockMovementCallback callback) {
        if (itemId == null || quantity == 0) {
            if (callback != null) callback.onFailure("Invalid item ID or quantity");
            return;
        }

        // Determine movement type
        String movementType = quantity > 0 ? "STOCK_IN" : "STOCK_OUT";

        // Get current item from cache or database
        getInventoryItem(itemId, new InventoryItemCallback() {
            @Override
            public void onSuccess(InventoryItem item) {
                try {
                    // Update stock quantity
                    int previousStock = item.getCurrentStock();
                    int newStock = previousStock + quantity;

                    // Validate new stock level
                    if (newStock < 0) {
                        newStock = 0;
                    }

                    // Add movement to history
                    item.addMovement(movementType, quantity, reason, performedBy, "");

                    // Update item in cache
                    inventoryCache.put(itemId, item);

                    // Update in Firebase
                    FirebaseUtils.getInventoryItemDocument(itemId)
                            .set(item)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Stock updated successfully: " + itemName + " " + movementType + " " + Math.abs(quantity));

                                // Notify listeners
                                for (InventoryChangeListener listener : listeners.values()) {
                                    listener.onStockUpdated(item, movementType, quantity);
                                }

                                // Check for alerts
                                checkAndCreateAlerts(item);

                                if (callback != null) callback.onSuccess(item);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update stock in Firebase", e);
                                if (callback != null) callback.onFailure(e.getMessage());
                            });

                } catch (Exception e) {
                    Log.e(TAG, "Error updating stock", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get inventory item for stock update", error);
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    /**
     * Check for low stock items
     */
    public void checkLowStock(InventoryAnalysisCallback callback) {
        List<InventoryItem> lowStockItems = new ArrayList<>();

        for (InventoryItem item : inventoryCache.values()) {
            if (item.needsReorder()) {
                lowStockItems.add(item);
            }
        }

        // Sort by urgency (lowest stock first)
        lowStockItems.sort(Comparator.comparingInt(InventoryItem::getCurrentStock));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("lowStockItems", lowStockItems);
        analysis.put("totalCount", lowStockItems.size());
        analysis.put("checkedAt", new Date());
        analysis.put("threshold", DEFAULT_REORDER_LEVEL);

        if (callback != null) callback.onSuccess(analysis);
    }

    /**
     * Check for expiring items
     */
    public void checkExpiringItems(InventoryAnalysisCallback callback) {
        List<InventoryItem> expiringItems = new ArrayList<>();
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, DEFAULT_EXPIRY_WARNING_DAYS);
        Date warningDate = calendar.getTime();

        for (InventoryItem item : inventoryCache.values()) {
            if (item.getExpiryDate() != null && isItemExpiring(item, warningDate)) {
                expiringItems.add(item);
            }
        }

        // Sort by expiry date (earliest first)
        expiringItems.sort(Comparator.comparing(InventoryItem::getExpiryDate));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("expiringItems", expiringItems);
        analysis.put("totalCount", expiringItems.size());
        analysis.put("checkedAt", today);
        analysis.put("warningDays", DEFAULT_EXPIRY_WARNING_DAYS);

        if (callback != null) callback.onSuccess(analysis);
    }

    /**
     * Generate restock suggestions
     */
    public void generateRestockSuggestions(InventoryAnalysisCallback callback) {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (InventoryItem item : inventoryCache.values()) {
            if (item.needsReorder()) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("itemId", item.getItemId());
                suggestion.put("itemName", item.getItemName());
                suggestion.put("currentStock", item.getCurrentStock());
                suggestion.put("reorderPoint", item.getReorderPoint());
                suggestion.put("reorderQuantity", item.getReorderQuantity());
                suggestion.put("supplierName", item.getSupplierName());
                suggestion.put("supplierContact", item.getSupplierContact());
                suggestion.put("leadTimeDays", item.getLeadTimeDays());
                suggestion.put("urgency", calculateUrgency(item));
                suggestion.put("suggestedOrderDate", calculateSuggestedOrderDate(item));

                suggestions.add(suggestion);
            }
        }

        // Sort by urgency
        suggestions.sort((a, b) -> {
            String urgencyA = (String) a.get("urgency");
            String urgencyB = (String) b.get("urgency");
            return getUrgencyLevel(urgencyB) - getUrgencyLevel(urgencyA);
        });

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("suggestions", suggestions);
        analysis.put("totalCount", suggestions.size());
        analysis.put("generatedAt", new Date());
        analysis.put("totalEstimatedCost", calculateTotalRestockCost(suggestions));

        if (callback != null) callback.onSuccess(analysis);
    }

    /**
     * Calculate total inventory value
     */
    public void calculateInventoryValue(InventoryAnalysisCallback callback) {
        double totalValue = 0.0;
        double potentialLossValue = 0.0;
        int totalItems = 0;
        int outOfStockItems = 0;
        int lowStockItems = 0;

        for (InventoryItem item : inventoryCache.values()) {
            totalValue += item.getTotalValue();
            potentialLossValue += item.getPotentialLossValue();
            totalItems += item.getCurrentStock();

            if (item.getCurrentStock() <= 0) {
                outOfStockItems++;
            } else if (item.needsReorder()) {
                lowStockItems++;
            }
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalValue", totalValue);
        analysis.put("potentialLossValue", potentialLossValue);
        analysis.put("totalItems", totalItems);
        analysis.put("outOfStockItems", outOfStockItems);
        analysis.put("lowStockItems", lowStockItems);
        analysis.put("uniqueProducts", inventoryCache.size());
        analysis.put("calculatedAt", new Date());
        analysis.put("averageItemValue", inventoryCache.size() > 0 ? totalValue / inventoryCache.size() : 0);

        if (callback != null) callback.onSuccess(analysis);
    }

    /**
     * Get waste percentage
     */
    public void getWastePercentage(InventoryAnalysisCallback callback) {
        double totalWasted = 0.0;
        double totalStocked = 0.0;

        for (InventoryItem item : inventoryCache.values()) {
            totalWasted += item.getWasted();
            totalStocked += item.getStockIn() + item.getStockOut() + item.getWasted() + item.getAdjusted();
        }

        double wastePercentage = totalStocked > 0 ? (totalWasted / totalStocked) * 100 : 0;

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalWasted", totalWasted);
        analysis.put("totalStocked", totalStocked);
        analysis.put("wastePercentage", wastePercentage);
        analysis.put("calculatedAt", new Date());

        if (callback != null) callback.onSuccess(analysis);
    }

    // Real-time Monitoring

    /**
     * Start real-time inventory monitoring
     */
    public void startMonitoring() {
        if (isMonitoringActive) {
            return;
        }

        isMonitoringActive = true;
        Log.d(TAG, "Starting real-time inventory monitoring");

        // Set up Firestore listener for inventory changes
        FirebaseUtils.getInventoryCollection()
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            InventoryItem item = dc.getDocument().toObject(InventoryItem.class);
                            item.setInventoryId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    handleNewItem(item);
                                    break;
                                case MODIFIED:
                                    handleModifiedItem(item);
                                    break;
                                case REMOVED:
                                    handleRemovedItem(dc.getDocument().getId());
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     * Stop real-time inventory monitoring
     */
    public void stopMonitoring() {
        isMonitoringActive = false;
        Log.d(TAG, "Stopped real-time inventory monitoring");
    }

    /**
     * Add inventory change listener
     */
    public void addInventoryChangeListener(String id, InventoryChangeListener listener) {
        if (listener != null) {
            listeners.put(id, listener);
        }
    }

    /**
     * Remove inventory change listener
     */
    public void removeInventoryChangeListener(String id) {
        listeners.remove(id);
    }

    // Alert Management

    /**
     * Get active alerts
     */
    public List<InventoryAlert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }

    /**
     * Get alerts by type
     */
    public List<InventoryAlert> getAlertsByType(InventoryAlert.AlertType type) {
        List<InventoryAlert> filteredAlerts = new ArrayList<>();
        for (InventoryAlert alert : activeAlerts) {
            if (alert.getAlertType() == type && !alert.isResolved()) {
                filteredAlerts.add(alert);
            }
        }
        return filteredAlerts;
    }

    /**
     * Resolve alert
     */
    public void resolveAlert(String alertId, String resolvedBy) {
        for (InventoryAlert alert : activeAlerts) {
            if (alert.getAlertId().equals(alertId)) {
                alert.setResolved(true);
                Log.d(TAG, "Alert resolved: " + alert.getMessage());
                break;
            }
        }
    }

    /**
     * Create admin notification from inventory alert
     */
    public AdminNotification createNotificationFromAlert(InventoryAlert alert) {
        AdminNotification notification = new AdminNotification(
                alert.getAlertType().getNotificationType(),
                alert.getAlertType().getTitle(),
                alert.getMessage(),
                alert.getPriority()
        );

        notification.setReferenceId(alert.getItemId());
        notification.setReferenceType("inventory_item");
        notification.setRequiresAction(true);
        notification.setActionRequired(alert.getActionRequired());
        notification.setActionUrl(alert.getActionUrl());

        return notification;
    }

    // Batch Operations

    /**
     * Batch update stock for multiple items
     */
    public void batchUpdateStock(Map<String, Integer> stockUpdates, String reason, String performedBy,
                                InventoryAnalysisCallback callback) {
        List<String> failedItems = new ArrayList<>();
        List<InventoryItem> updatedItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : stockUpdates.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            updateStock(itemId, quantity, reason, performedBy, new StockMovementCallback() {
                @Override
                public void onSuccess(InventoryItem item) {
                    updatedItems.add(item);
                }

                @Override
                public void onFailure(String error) {
                    failedItems.add(itemId + ": " + error);
                }
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put("updatedItems", updatedItems);
        result.put("failedItems", failedItems);
        result.put("totalUpdates", stockUpdates.size());
        result.put("successCount", updatedItems.size());
        result.put("failureCount", failedItems.size());
        result.put("performedAt", new Date());

        if (callback != null) callback.onSuccess(result);
    }

    // Helper Methods

    /**
     * Get inventory item
     */
    private void getInventoryItem(String itemId, InventoryItemCallback callback) {
        // Check cache first
        if (inventoryCache.containsKey(itemId)) {
            if (callback != null) callback.onSuccess(inventoryCache.get(itemId));
            return;
        }

        // Fetch from Firebase
        FirebaseUtils.getInventoryItemDocument(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        InventoryItem item = documentSnapshot.toObject(InventoryItem.class);
                        item.setInventoryId(documentSnapshot.getId());
                        inventoryCache.put(itemId, item);
                        if (callback != null) callback.onSuccess(item);
                    } else {
                        if (callback != null) callback.onFailure("Inventory item not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get inventory item", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Check if item is expiring
     */
    private boolean isItemExpiring(InventoryItem item, Date warningDate) {
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            Date expiryDate = sdf.parse(item.getExpiryDate());
            return expiryDate.before(warningDate);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing expiry date", e);
            return false;
        }
    }

    /**
     * Check and create alerts for item
     */
    private void checkAndCreateAlerts(InventoryItem item) {
        // Check for low stock
        if (item.needsReorder()) {
            createAlert(item, InventoryAlert.AlertType.LOW_STOCK,
                    item.getCurrentStock() <= 0 ? "Item is out of stock" :
                    "Item is running low on stock (" + item.getCurrentStock() + " remaining)",
                    "Restock Item",
                    "/inventory/" + item.getItemId());
        }

        // Check for expiry
        if (item.isExpiringSoon()) {
            createAlert(item, InventoryAlert.AlertType.EXPIRY,
                    "Item expires on " + item.getExpiryDate(),
                    "Manage Expiring Stock",
                    "/inventory/expiring");
        }

        // Check for quality issues
        if (item.getQualityStatus() != null && !"Good".equals(item.getQualityStatus())) {
            createAlert(item, InventoryAlert.AlertType.QUALITY,
                    "Quality issue: " + item.getQualityStatus(),
                    "Quality Check Required",
                    "/inventory/" + item.getItemId());
        }
    }

    /**
     * Create inventory alert
     */
    private void createAlert(InventoryItem item, InventoryAlert.AlertType alertType,
                            String message, String actionRequired, String actionUrl) {
        // Check if similar alert already exists
        for (InventoryAlert existingAlert : activeAlerts) {
            if (existingAlert.getItemId().equals(item.getItemId()) &&
                existingAlert.getAlertType() == alertType &&
                !existingAlert.isResolved()) {
                return; // Alert already exists
            }
        }

        // Create new alert
        InventoryAlert alert = new InventoryAlert(item.getItemId(), item.getItemName(), alertType, message);
        alert.setActionRequired(actionRequired);
        alert.setActionUrl(actionUrl);

        activeAlerts.add(alert);
        Log.d(TAG, "Created inventory alert: " + alert.getMessage());

        // Notify listeners
        for (InventoryChangeListener listener : listeners.values()) {
            if (alertType == InventoryAlert.AlertType.LOW_STOCK) {
                listener.onLowStockAlert(item);
            } else if (alertType == InventoryAlert.AlertType.EXPIRY) {
                listener.onExpiryAlert(item);
            }
        }
    }

    /**
     * Calculate urgency level
     */
    private String calculateUrgency(InventoryItem item) {
        if (item.getCurrentStock() <= 0) {
            return "Critical";
        } else if (item.getCurrentStock() <= item.getLowStockThreshold()) {
            return "High";
        } else if (item.needsReorder()) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    /**
     * Get numeric urgency level for sorting
     */
    private int getUrgencyLevel(String urgency) {
        switch (urgency) {
            case "Critical": return 4;
            case "High": return 3;
            case "Medium": return 2;
            case "Low": return 1;
            default: return 0;
        }
    }

    /**
     * Calculate suggested order date
     */
    private String calculateSuggestedOrderDate(InventoryItem item) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, item.getLeadTimeDays());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * Calculate total restock cost
     */
    private double calculateTotalRestockCost(List<Map<String, Object>> suggestions) {
        double totalCost = 0.0;
        for (Map<String, Object> suggestion : suggestions) {
            int reorderQuantity = (Integer) suggestion.get("reorderQuantity");
            totalCost += reorderQuantity * 50.0; // Assuming average unit cost of 50
        }
        return totalCost;
    }

    // Event Handlers

    private void handleNewItem(InventoryItem item) {
        inventoryCache.put(item.getInventoryId(), item);
        checkAndCreateAlerts(item);

        for (InventoryChangeListener listener : listeners.values()) {
            listener.onNewItemAdded(item);
        }
    }

    private void handleModifiedItem(InventoryItem item) {
        InventoryItem previousItem = inventoryCache.get(item.getInventoryId());
        inventoryCache.put(item.getInventoryId(), item);
        checkAndCreateAlerts(item);

        for (InventoryChangeListener listener : listeners.values()) {
            listener.onInventoryChanged(item);
        }
    }

    private void handleRemovedItem(String itemId) {
        InventoryItem item = inventoryCache.remove(itemId);

        // Remove related alerts
        activeAlerts.removeIf(alert -> alert.getItemId().equals(itemId));

        if (item != null) {
            for (InventoryChangeListener listener : listeners.values()) {
                listener.onItemRemoved(item);
            }
        }
    }

    /**
     * Inventory item callback interface
     */
    private interface InventoryItemCallback {
        void onSuccess(InventoryItem item);
        void onFailure(String error);
    }
}