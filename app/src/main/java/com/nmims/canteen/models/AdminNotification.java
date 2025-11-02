package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin notification system model
 * Contains comprehensive notification data for admin alerts and system updates
 */
public class AdminNotification implements Serializable {
    // Notification types
    public enum NotificationType {
        LOW_STOCK("Low Stock Alert", "inventory", "⚠️"),
        NEW_ORDER("New Order", "orders", "📝"),
        HIGH_SALES("High Sales Activity", "sales", "📈"),
        EXPIRY_ALERT("Expiry Alert", "inventory", "⏰"),
        SYSTEM_UPDATE("System Update", "system", "🔄"),
        PAYMENT_ISSUE("Payment Issue", "payments", "💳"),
        CUSTOMER_COMPLAINT("Customer Complaint", "customer", "😞"),
        DELIVERY_DELAY("Delivery Delay", "delivery", "🚚"),
        QUALITY_ISSUE("Quality Issue", "quality", "🔍"),
        STAFF_ALERT("Staff Alert", "staff", "👥"),
        MAINTENANCE("Maintenance Required", "maintenance", "🔧"),
        SECURITY("Security Alert", "security", "🔒"),
        BACKUP_COMPLETE("Backup Complete", "system", "💾"),
        REPORT_AVAILABLE("Report Available", "reports", "📊"),
        PROMO_SUCCESS("Promotion Success", "marketing", "🎉");

        private final String defaultTitle;
        private final String category;
        private final String icon;

        NotificationType(String defaultTitle, String category, String icon) {
            this.defaultTitle = defaultTitle;
            this.category = category;
            this.icon = icon;
        }

        public String getDefaultTitle() { return defaultTitle; }
        public String getCategory() { return category; }
        public String getIcon() { return icon; }
    }

    // Priority levels
    public enum Priority {
        LOW("Low", "#4CAF50"),      // Green
        MEDIUM("Medium", "#FF9800"), // Orange
        HIGH("High", "#FF5722"),     // Deep Orange
        URGENT("Urgent", "#F44336"); // Red

        private final String displayName;
        private final String color;

        Priority(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }

    // Basic notification information
    private String notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private String detailedMessage;
    private Priority priority;
    private String category;

    // Timestamps
    private Date createdAt;
    private Date readAt;
    private Date acknowledgedAt;
    private Date resolvedAt;
    private Date autoExpireAt;

    // Status flags
    private boolean isRead;
    private boolean isAcknowledged;
    private boolean isResolved;
    private boolean isDismissable;
    private boolean isArchived;
    private boolean requiresAction;

    // Action fields
    private String actionRequired;
    private String actionUrl;
    private Map<String, Object> actionData;
    private String[] actionButtons;
    private String actionType; // "redirect", "modal", "api_call", "external"

    // Target and filtering
    private String targetUserId; // For user-specific notifications
    private String[] targetRoles; // For role-based notifications
    private String[] targetDepartments;
    private boolean isGlobal;
    private String scope; // "all", "department", "role", "user"

    // Escalation settings
    private int escalationLevel;
    private String escalatedTo;
    private Date escalatedAt;
    private String originalNotificationId;
    private boolean isEscalated;

    // Reference and context
    private String referenceId; // Order ID, Item ID, etc.
    private String referenceType; // "order", "item", "user", etc.
    private Map<String, Object> contextData;
    private String source; // "system", "user", "automation"

    // Interaction tracking
    private int viewCount;
    private Date lastViewedAt;
    private String lastViewedBy;
    private String[] interactions;
    private Map<String, Date> interactionHistory;

    // System fields
    private String createdBy;
    private String updatedBy;
    private Date lastUpdatedAt;
    private String version;
    private boolean isDeleted;
    private Date deletedAt;

    // Default constructor for Firebase
    public AdminNotification() {
        this.createdAt = new Date();
        this.lastUpdatedAt = new Date();
        this.isRead = false;
        this.isAcknowledged = false;
        this.isResolved = false;
        this.isDismissable = true;
        this.isArchived = false;
        this.requiresAction = false;
        this.isGlobal = false;
        this.scope = "all";
        this.escalationLevel = 0;
        this.isEscalated = false;
        this.viewCount = 0;
        this.priority = Priority.MEDIUM;
        this.actionData = new HashMap<>();
        this.contextData = new HashMap<>();
        this.interactionHistory = new HashMap<>();
        this.version = "1.0";
        this.isDeleted = false;
        this.source = "system";
    }

    // Parameterized constructor
    public AdminNotification(NotificationType type, String title, String message, Priority priority) {
        this();
        this.type = type;
        this.title = title != null ? title : type.getDefaultTitle();
        this.message = message;
        this.priority = priority;
        this.category = type.getCategory();
        this.notificationId = generateNotificationId();
    }

    // Convenience constructors for common notification types
    public static AdminNotification createLowStockAlert(String itemId, String itemName, int currentStock) {
        AdminNotification notification = new AdminNotification(
            NotificationType.LOW_STOCK,
            "Low Stock Alert",
            itemName + " is running low on stock (" + currentStock + " remaining)",
            Priority.HIGH
        );
        notification.setReferenceId(itemId);
        notification.setReferenceType("inventory_item");
        notification.setRequiresAction(true);
        notification.setActionRequired("Restock item");
        notification.setActionUrl("/inventory/" + itemId);
        notification.setAutoExpireAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours
        return notification;
    }

    public static AdminNotification createNewOrderNotification(String orderId, String customerName, double amount) {
        AdminNotification notification = new AdminNotification(
            NotificationType.NEW_ORDER,
            "New Order Received",
            "Order " + orderId + " from " + customerName + " for ₹" + String.format("%.2f", amount),
            Priority.MEDIUM
        );
        notification.setReferenceId(orderId);
        notification.setReferenceType("order");
        notification.setRequiresAction(true);
        notification.setActionRequired("Process Order");
        notification.setActionUrl("/orders/" + orderId);
        notification.setPriority(customerName.contains("VIP") ? Priority.HIGH : Priority.MEDIUM);
        return notification;
    }

    public static AdminNotification createExpiryAlert(String itemId, String itemName, String expiryDate) {
        AdminNotification notification = new AdminNotification(
            NotificationType.EXPIRY_ALERT,
            "Item Expiring Soon",
            itemName + " expires on " + expiryDate,
            Priority.HIGH
        );
        notification.setReferenceId(itemId);
        notification.setReferenceType("inventory_item");
        notification.setRequiresAction(true);
        notification.setActionRequired("Manage Expiring Stock");
        notification.setActionUrl("/inventory/expiring");
        return notification;
    }

    public static AdminNotification createHighSalesNotification(double revenue, int orderCount) {
        AdminNotification notification = new AdminNotification(
            NotificationType.HIGH_SALES,
            "High Sales Activity",
            "Great performance! ₹" + String.format("%.2f", revenue) + " revenue from " + orderCount + " orders",
            Priority.LOW
        );
        notification.setReferenceType("sales_data");
        notification.setRequiresAction(false);
        notification.setActionUrl("/analytics");
        return notification;
    }

    public static AdminNotification createCustomerComplaint(String orderId, String complaintType) {
        AdminNotification notification = new AdminNotification(
            NotificationType.CUSTOMER_COMPLAINT,
            "Customer Complaint",
            "New complaint received for order " + orderId + ": " + complaintType,
            Priority.HIGH
        );
        notification.setReferenceId(orderId);
        notification.setReferenceType("order");
        notification.setRequiresAction(true);
        notification.setActionRequired("Review Complaint");
        notification.setActionUrl("/orders/" + orderId + "/complaint");
        return notification;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
        if (type != null) {
            this.category = type.getCategory();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getReadAt() {
        return readAt;
    }

    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }

    public Date getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Date acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Date getAutoExpireAt() {
        return autoExpireAt;
    }

    public void setAutoExpireAt(Date autoExpireAt) {
        this.autoExpireAt = autoExpireAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        if (read && readAt == null) {
            readAt = new Date();
        }
    }

    public boolean isAcknowledged() {
        return isAcknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        isAcknowledged = acknowledged;
        if (acknowledged && acknowledgedAt == null) {
            acknowledgedAt = new Date();
        }
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
        if (resolved && resolvedAt == null) {
            resolvedAt = new Date();
        }
    }

    public boolean isDismissable() {
        return isDismissable;
    }

    public void setDismissable(boolean dismissable) {
        isDismissable = dismissable;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public boolean requiresAction() {
        return requiresAction;
    }

    public void setRequiresAction(boolean requiresAction) {
        this.requiresAction = requiresAction;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Map<String, Object> getActionData() {
        return actionData;
    }

    public void setActionData(Map<String, Object> actionData) {
        this.actionData = actionData != null ? actionData : new HashMap<>();
    }

    public String[] getActionButtons() {
        return actionButtons;
    }

    public void setActionButtons(String[] actionButtons) {
        this.actionButtons = actionButtons;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String[] getTargetRoles() {
        return targetRoles;
    }

    public void setTargetRoles(String[] targetRoles) {
        this.targetRoles = targetRoles;
    }

    public String[] getTargetDepartments() {
        return targetDepartments;
    }

    public void setTargetDepartments(String[] targetDepartments) {
        this.targetDepartments = targetDepartments;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(int escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public String getEscalatedTo() {
        return escalatedTo;
    }

    public void setEscalatedTo(String escalatedTo) {
        this.escalatedTo = escalatedTo;
    }

    public Date getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(Date escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public String getOriginalNotificationId() {
        return originalNotificationId;
    }

    public void setOriginalNotificationId(String originalNotificationId) {
        this.originalNotificationId = originalNotificationId;
    }

    public boolean isEscalated() {
        return isEscalated;
    }

    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData != null ? contextData : new HashMap<>();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Date getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(Date lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }

    public String getLastViewedBy() {
        return lastViewedBy;
    }

    public void setLastViewedBy(String lastViewedBy) {
        this.lastViewedBy = lastViewedBy;
    }

    public String[] getInteractions() {
        return interactions;
    }

    public void setInteractions(String[] interactions) {
        this.interactions = interactions;
    }

    public Map<String, Date> getInteractionHistory() {
        return interactionHistory;
    }

    public void setInteractionHistory(Map<String, Date> interactionHistory) {
        this.interactionHistory = interactionHistory != null ? interactionHistory : new HashMap<>();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        if (deleted) {
            deletedAt = new Date();
        }
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Generates a unique notification ID
     */
    private String generateNotificationId() {
        return "NOTIF_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * Marks notification as read
     */
    public void markAsRead(String userId) {
        if (!isRead) {
            isRead = true;
            readAt = new Date();
            addInteraction("read", userId);
        }
        lastUpdatedAt = new Date();
    }

    /**
     * Marks notification as acknowledged
     */
    public void acknowledge(String userId) {
        if (!isAcknowledged) {
            isAcknowledged = true;
            acknowledgedAt = new Date();
            addInteraction("acknowledged", userId);
        }
        lastUpdatedAt = new Date();
    }

    /**
     * Marks notification as resolved
     */
    public void resolve(String userId, String resolutionNote) {
        if (!isResolved) {
            isResolved = true;
            resolvedAt = new Date();
            setDetailedMessage(resolutionNote);
            addInteraction("resolved", userId);
        }
        lastUpdatedAt = new Date();
    }

    /**
     * Adds an interaction to the history
     */
    public void addInteraction(String action, String userId) {
        if (interactionHistory == null) {
            interactionHistory = new HashMap<>();
        }
        interactionHistory.put(action + "_" + System.currentTimeMillis(), new Date());
        lastViewedAt = new Date();
        lastViewedBy = userId;
        viewCount++;
    }

    /**
     * Escalates notification to higher priority or different user
     */
    public void escalate(String escalatedTo, String reason, String escalatedBy) {
        this.escalationLevel++;
        this.escalatedTo = escalatedTo;
        this.escalatedAt = new Date();
        this.isEscalated = true;
        this.priority = getNextPriority(this.priority);

        if (this.originalNotificationId == null) {
            this.originalNotificationId = this.notificationId;
        }

        addInteraction("escalated_to_" + escalatedTo, escalatedBy);
        lastUpdatedAt = new Date();
    }

    /**
     * Gets the next higher priority level
     */
    private Priority getNextPriority(Priority current) {
        switch (current) {
            case LOW: return Priority.MEDIUM;
            case MEDIUM: return Priority.HIGH;
            case HIGH: return Priority.URGENT;
            case URGENT: return Priority.URGENT; // Already highest
            default: return Priority.MEDIUM;
        }
    }

    /**
     * Gets priority level as integer for sorting
     */
    public int getPriorityLevel() {
        switch (priority) {
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            case URGENT: return 4;
            default: return 0;
        }
    }

    /**
     * Gets notification type icon
     */
    public String getIcon() {
        return type != null ? type.getIcon() : "📢";
    }

    /**
     * Gets time ago string for display
     */
    public String getTimeAgo() {
        long diffInMillis = System.currentTimeMillis() - createdAt.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;

        if (diffInDays > 0) {
            return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
        } else if (diffInHours > 0) {
            return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
        } else if (diffInMinutes > 0) {
            return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }

    /**
     * Checks if notification is expired
     */
    public boolean isExpired() {
        return autoExpireAt != null && new Date().after(autoExpireAt);
    }

    /**
     * Checks if notification requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return priority == Priority.URGENT ||
               (priority == Priority.HIGH && requiresAction && !isAcknowledged);
    }

    /**
     * Validates notification data
     */
    public boolean isValid() {
        return notificationId != null && !notificationId.isEmpty() &&
               title != null && !title.isEmpty() &&
               message != null && !message.isEmpty() &&
               type != null &&
               priority != null;
    }

    @Override
    public String toString() {
        return "AdminNotification{" +
                "notificationId='" + notificationId + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminNotification that = (AdminNotification) o;
        return notificationId != null && notificationId.equals(that.notificationId);
    }

    @Override
    public int hashCode() {
        return notificationId != null ? notificationId.hashCode() : 0;
    }
}