package com.nmims.canteen.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nmims.canteen.models.AdminNotification;
import com.nmims.canteen.models.InventoryItem;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.SalesData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Admin notification system utility
 * Handles creation, management, and delivery of admin notifications
 */
public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;

    // Notification settings
    private final Map<String, NotificationPreferences> userPreferences;
    private final List<NotificationDeliveryListener> deliveryListeners;
    private final ScheduledExecutorService scheduler;

    // Real-time monitoring
    private boolean isRealTimeMonitoringActive;
    private final List<AdminNotification> notificationCache;

    // Default notification settings
    private static final int DEFAULT_RETENTION_DAYS = 30;
    private static final int MAX_NOTIFICATIONS_PER_USER = 100;
    private static final long NOTIFICATION_CHECK_INTERVAL = 60; // seconds

    private NotificationManager() {
        this.userPreferences = new ConcurrentHashMap<>();
        this.deliveryListeners = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.isRealTimeMonitoringActive = false;
        this.notificationCache = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Get singleton instance
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Notification preferences class
     */
    public static class NotificationPreferences {
        private String userId;
        private boolean emailNotifications;
        private boolean pushNotifications;
        private boolean inAppNotifications;
        private Map<AdminNotification.NotificationType, Boolean> typePreferences;
        private Map<AdminNotification.Priority, Boolean> priorityPreferences;
        private boolean quietHoursEnabled;
        private int quietHoursStart;
        private int quietHoursEnd;
        private String timezone;
        private int maxNotificationsPerDay;
        private String notificationLanguage;

        public NotificationPreferences(String userId) {
            this.userId = userId;
            this.emailNotifications = true;
            this.pushNotifications = true;
            this.inAppNotifications = true;
            this.typePreferences = new HashMap<>();
            this.priorityPreferences = new HashMap<>();
            this.quietHoursEnabled = false;
            this.quietHoursStart = 22; // 10 PM
            this.quietHoursEnd = 8;   // 8 AM
            this.timezone = "Asia/Kolkata";
            this.maxNotificationsPerDay = 50;
            this.notificationLanguage = "en";

            // Enable all notification types by default
            for (AdminNotification.NotificationType type : AdminNotification.NotificationType.values()) {
                typePreferences.put(type, true);
            }

            // Enable all priority levels by default
            for (AdminNotification.Priority priority : AdminNotification.Priority.values()) {
                priorityPreferences.put(priority, true);
            }
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        public boolean isPushNotifications() { return pushNotifications; }
        public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
        public boolean isInAppNotifications() { return inAppNotifications; }
        public void setInAppNotifications(boolean inAppNotifications) { this.inAppNotifications = inAppNotifications; }
        public Map<AdminNotification.NotificationType, Boolean> getTypePreferences() { return typePreferences; }
        public Map<AdminNotification.Priority, Boolean> getPriorityPreferences() { return priorityPreferences; }
        public boolean isQuietHoursEnabled() { return quietHoursEnabled; }
        public void setQuietHoursEnabled(boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }
        public int getQuietHoursStart() { return quietHoursStart; }
        public void setQuietHoursStart(int quietHoursStart) { this.quietHoursStart = quietHoursStart; }
        public int getQuietHoursEnd() { return quietHoursEnd; }
        public void setQuietHoursEnd(int quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public int getMaxNotificationsPerDay() { return maxNotificationsPerDay; }
        public void setMaxNotificationsPerDay(int maxNotificationsPerDay) { this.maxNotificationsPerDay = maxNotificationsPerDay; }
        public String getNotificationLanguage() { return notificationLanguage; }
        public void setNotificationLanguage(String notificationLanguage) { this.notificationLanguage = notificationLanguage; }

        public boolean isNotificationTypeEnabled(AdminNotification.NotificationType type) {
            return typePreferences.getOrDefault(type, true);
        }

        public boolean isPriorityEnabled(AdminNotification.Priority priority) {
            return priorityPreferences.getOrDefault(priority, true);
        }

        public boolean isInQuietHours() {
            if (!quietHoursEnabled) return false;

            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

            if (quietHoursStart > quietHoursEnd) {
                // Quiet hours spans midnight (e.g., 22:00 to 08:00)
                return currentHour >= quietHoursStart || currentHour < quietHoursEnd;
            } else {
                // Normal case (e.g., 01:00 to 06:00)
                return currentHour >= quietHoursStart && currentHour < quietHoursEnd;
            }
        }
    }

    /**
     * Notification delivery listener interface
     */
    public interface NotificationDeliveryListener {
        void onNotificationDelivered(AdminNotification notification);
        void onNotificationFailed(AdminNotification notification, String error);
        void onNotificationRead(AdminNotification notification);
        void onNotificationAcknowledged(AdminNotification notification);
    }

    /**
     * Notification callback interface
     */
    public interface NotificationCallback {
        void onSuccess(AdminNotification notification);
        void onFailure(String error);
    }

    // Core Notification Operations

    /**
     * Create notification
     */
    public void createNotification(AdminNotification.NotificationType type, String title, String message,
                                  AdminNotification.Priority priority, NotificationCallback callback) {
        AdminNotification notification = new AdminNotification(type, title, message, priority);

        // Save to Firebase
        FirebaseUtils.getAdminNotificationsCollection()
                .document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification created successfully: " + notification.getNotificationId());

                    // Add to cache
                    notificationCache.add(0, notification);

                    // Deliver notification
                    deliverNotification(notification);

                    if (callback != null) callback.onSuccess(notification);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create notification", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Send low stock alert
     */
    public void sendLowStockAlert(List<String> itemIds) {
        for (String itemId : itemIds) {
            // Get inventory item details
            FirebaseUtils.getInventoryItemDocument(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            InventoryItem item = documentSnapshot.toObject(InventoryItem.class);
                            if (item != null) {
                                AdminNotification notification = AdminNotification.createLowStockAlert(
                                        item.getItemId(), item.getItemName(), item.getCurrentStock()
                                );

                                // Add to cache and deliver
                                notificationCache.add(0, notification);
                                deliverNotification(notification);

                                // Save to Firebase
                                FirebaseUtils.getAdminNotificationsCollection()
                                        .document(notification.getNotificationId())
                                        .set(notification);
                            }
                        }
                    });
        }
    }

    /**
     * Send new order notification
     */
    public void sendNewOrderNotification(String orderId) {
        FirebaseUtils.getOrderDocument(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            AdminNotification notification = AdminNotification.createNewOrderNotification(
                                    order.getOrderId(), order.getUserName(), order.getFinalAmount()
                            );

                            // Add to cache and deliver
                            notificationCache.add(0, notification);
                            deliverNotification(notification);

                            // Save to Firebase
                            FirebaseUtils.getAdminNotificationsCollection()
                                    .document(notification.getNotificationId())
                                    .set(notification);
                        }
                    }
                });
    }

    /**
     * Send expiry alert
     */
    public void sendExpiryAlert(List<String> expiringItems) {
        for (String itemId : expiringItems) {
            FirebaseUtils.getInventoryItemDocument(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            InventoryItem item = documentSnapshot.toObject(InventoryItem.class);
                            if (item != null) {
                                AdminNotification notification = AdminNotification.createExpiryAlert(
                                        item.getItemId(), item.getItemName(), item.getExpiryDate()
                                );

                                // Add to cache and deliver
                                notificationCache.add(0, notification);
                                deliverNotification(notification);

                                // Save to Firebase
                                FirebaseUtils.getAdminNotificationsCollection()
                                        .document(notification.getNotificationId())
                                        .set(notification);
                            }
                        }
                    });
        }
    }

    /**
     * Send sales milestone notification
     */
    public void sendSalesMilestoneNotification(double revenue) {
        String title = "Sales Milestone Reached!";
        String message = "Congratulations! Daily sales have reached ₹" + String.format("%.0f", revenue);

        createNotification(
                AdminNotification.NotificationType.HIGH_SALES,
                title,
                message,
                AdminNotification.Priority.LOW,
                null
        );
    }

    /**
     * Send customer complaint notification
     */
    public void sendCustomerComplaintNotification(String orderId, String complaintType, String description) {
        String title = "Customer Complaint Received";
        String message = "New complaint for order " + orderId + ": " + complaintType;

        AdminNotification notification = AdminNotification.createCustomerComplaint(orderId, complaintType);
        notification.setDetailedMessage(description);

        // Add to cache and deliver
        notificationCache.add(0, notification);
        deliverNotification(notification);

        // Save to Firebase
        FirebaseUtils.getAdminNotificationsCollection()
                .document(notification.getNotificationId())
                .set(notification);
    }

    // Notification Management

    /**
     * Get notifications for user
     */
    public void getNotificationsForUser(String userId, int limit, NotificationCallback callback) {
        FirebaseUtils.getUnreadNotificationsQuery()
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AdminNotification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        AdminNotification notification = doc.toObject(AdminNotification.class);
                        notifications.add(notification);
                    }

                    if (callback != null) {
                        // Return the first notification for simplicity
                        if (!notifications.isEmpty()) {
                            callback.onSuccess(notifications.get(0));
                        } else {
                            callback.onFailure("No notifications found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get notifications", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId, String userId) {
        for (AdminNotification notification : notificationCache) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.markAsRead(userId);

                // Update in Firebase
                FirebaseUtils.getNotificationDocument(notificationId)
                        .update("isRead", true, "readAt", new Date())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Notification marked as read: " + notificationId);

                            // Notify listeners
                            for (NotificationDeliveryListener listener : deliveryListeners) {
                                listener.onNotificationRead(notification);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to mark notification as read", e);
                        });

                break;
            }
        }
    }

    /**
     * Acknowledge notification
     */
    public void acknowledgeNotification(String notificationId, String userId) {
        for (AdminNotification notification : notificationCache) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.acknowledge(userId);

                // Update in Firebase
                FirebaseUtils.getNotificationDocument(notificationId)
                        .update("isAcknowledged", true, "acknowledgedAt", new Date())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Notification acknowledged: " + notificationId);

                            // Notify listeners
                            for (NotificationDeliveryListener listener : deliveryListeners) {
                                listener.onNotificationAcknowledged(notification);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to acknowledge notification", e);
                        });

                break;
            }
        }
    }

    /**
     * Resolve notification
     */
    public void resolveNotification(String notificationId, String userId, String resolutionNote) {
        for (AdminNotification notification : notificationCache) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.resolve(userId, resolutionNote);

                // Update in Firebase
                FirebaseUtils.getNotificationDocument(notificationId)
                        .update("isResolved", true, "resolvedAt", new Date(),
                                "detailedMessage", resolutionNote)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Notification resolved: " + notificationId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to resolve notification", e);
                        });

                break;
            }
        }
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId) {
        FirebaseUtils.getNotificationDocument(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted: " + notificationId);

                    // Remove from cache
                    notificationCache.removeIf(notification -> notification.getNotificationId().equals(notificationId));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete notification", e);
                });
    }

    /**
     * Escalate notification
     */
    public void escalateNotification(String notificationId, String escalatedTo, String reason) {
        for (AdminNotification notification : notificationCache) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.escalate(escalatedTo, reason, "system");

                // Update in Firebase
                FirebaseUtils.getNotificationDocument(notificationId)
                        .update("escalationLevel", notification.getEscalationLevel(),
                                "escalatedTo", escalatedTo, "escalatedAt", notification.getEscalatedAt(),
                                "isEscalated", true)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Notification escalated: " + notificationId);

                            // Redeliver with higher priority
                            deliverNotification(notification);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to escalate notification", e);
                        });

                break;
            }
        }
    }

    // Real-time Monitoring

    /**
     * Start real-time notification monitoring
     */
    public void startRealTimeMonitoring() {
        if (isRealTimeMonitoringActive) {
            return;
        }

        isRealTimeMonitoringActive = true;
        Log.d(TAG, "Starting real-time notification monitoring");

        // Set up Firestore listener for new notifications
        FirebaseUtils.getUnreadNotificationsQuery()
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Notification listener failed", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (var change : snapshots.getDocumentChanges()) {
                            if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                AdminNotification notification = change.getDocument().toObject(AdminNotification.class);
                                notificationCache.add(0, notification);
                                deliverNotification(notification);
                            }
                        }
                    }
                });

        // Schedule periodic notification cleanup
        scheduler.scheduleAtFixedRate(this::cleanupOldNotifications, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Stop real-time notification monitoring
     */
    public void stopRealTimeMonitoring() {
        isRealTimeMonitoringActive = false;
        Log.d(TAG, "Stopped real-time notification monitoring");
    }

    // Preferences Management

    /**
     * Get user preferences
     */
    public NotificationPreferences getUserPreferences(String userId) {
        return userPreferences.computeIfAbsent(userId, NotificationPreferences::new);
    }

    /**
     * Update user preferences
     */
    public void updateUserPreferences(String userId, NotificationPreferences preferences) {
        userPreferences.put(userId, preferences);

        // Save to Firebase
        FirebaseUtils.getUserDocument(userId)
                .update("notificationPreferences", preferences)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User preferences updated: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user preferences", e);
                });
    }

    // Delivery Methods

    /**
     * Deliver notification through appropriate channels
     */
    private void deliverNotification(AdminNotification notification) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            return;
        }

        NotificationPreferences preferences = getUserPreferences(userId);

        // Check quiet hours
        if (preferences.isInQuietHours() && notification.getPriority() != AdminNotification.Priority.URGENT) {
            Log.d(TAG, "Notification delayed due to quiet hours: " + notification.getNotificationId());
            scheduleForQuietHours(notification);
            return;
        }

        // Check notification type preferences
        if (!preferences.isNotificationTypeEnabled(notification.getType())) {
            Log.d(TAG, "Notification type disabled: " + notification.getType());
            return;
        }

        // Check priority preferences
        if (!preferences.isPriorityEnabled(notification.getPriority())) {
            Log.d(TAG, "Notification priority disabled: " + notification.getPriority());
            return;
        }

        boolean delivered = false;

        // In-app notification (always available)
        if (preferences.isInAppNotifications()) {
            deliverInAppNotification(notification);
            delivered = true;
        }

        // Push notification
        if (preferences.isPushNotifications()) {
            deliverPushNotification(notification);
            delivered = true;
        }

        // Email notification
        if (preferences.isEmailNotifications() && notification.getPriority().ordinal() >= AdminNotification.Priority.HIGH.ordinal()) {
            deliverEmailNotification(notification);
            delivered = true;
        }

        if (delivered) {
            // Notify delivery listeners
            for (NotificationDeliveryListener listener : deliveryListeners) {
                listener.onNotificationDelivered(notification);
            }
        } else {
            Log.w(TAG, "Notification not delivered: no enabled delivery methods");
        }
    }

    /**
     * Deliver in-app notification
     */
    private void deliverInAppNotification(AdminNotification notification) {
        // In a real implementation, this would show a notification in the app UI
        Log.d(TAG, "In-app notification delivered: " + notification.getTitle());
    }

    /**
     * Deliver push notification
     */
    private void deliverPushNotification(AdminNotification notification) {
        // Get FCM token for current user
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null) {
                        Log.d(TAG, "Push notification delivered: " + notification.getTitle());
                        // In a real implementation, this would use FCM API to send push notification
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get FCM token", e);
                });
    }

    /**
     * Deliver email notification
     */
    private void deliverEmailNotification(AdminNotification notification) {
        // In a real implementation, this would use an email service API
        Log.d(TAG, "Email notification delivered: " + notification.getTitle());
    }

    /**
     * Schedule notification for after quiet hours
     */
    private void scheduleForQuietHours(AdminNotification notification) {
        NotificationPreferences preferences = getUserPreferences(FirebaseUtils.getCurrentUserId());

        // Calculate next available time after quiet hours
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int targetHour = preferences.getQuietHoursEnd();

        if (currentHour >= targetHour) {
            // Next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, targetHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        scheduler.schedule(() -> {
            deliverNotification(notification);
        }, delay, TimeUnit.MILLISECONDS);

        Log.d(TAG, "Notification scheduled for after quiet hours: " + notification.getNotificationId());
    }

    // Utility Methods

    /**
     * Add delivery listener
     */
    public void addDeliveryListener(NotificationDeliveryListener listener) {
        if (listener != null) {
            deliveryListeners.add(listener);
        }
    }

    /**
     * Remove delivery listener
     */
    public void removeDeliveryListener(NotificationDeliveryListener listener) {
        deliveryListeners.remove(listener);
    }

    /**
     * Get unread notification count
     */
    public int getUnreadNotificationCount() {
        int count = 0;
        for (AdminNotification notification : notificationCache) {
            if (!notification.isRead() && !notification.isResolved()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get urgent notifications
     */
    public List<AdminNotification> getUrgentNotifications() {
        List<AdminNotification> urgent = new ArrayList<>();
        for (AdminNotification notification : notificationCache) {
            if (notification.getPriority() == AdminNotification.Priority.URGENT && !notification.isResolved()) {
                urgent.add(notification);
            }
        }
        return urgent;
    }

    /**
     * Clean up old notifications
     */
    private void cleanupOldNotifications() {
        long cutoffTime = System.currentTimeMillis() - (DEFAULT_RETENTION_DAYS * 24 * 60 * 60 * 1000L);

        notificationCache.removeIf(notification -> {
            return notification.getCreatedAt().getTime() < cutoffTime && notification.isResolved();
        });

        // Also clean up from Firebase
        FirebaseUtils.getAdminNotificationsCollection()
                .whereLessThan("createdAt", new Date(cutoffTime))
                .whereEqualTo("isResolved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                });

        Log.d(TAG, "Cleaned up old notifications");
    }

    /**
     * Get notification statistics
     */
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalNotifications = notificationCache.size();
        int unreadNotifications = 0;
        int urgentNotifications = 0;
        int resolvedNotifications = 0;

        Map<AdminNotification.NotificationType, Integer> typeCounts = new HashMap<>();
        Map<AdminNotification.Priority, Integer> priorityCounts = new HashMap<>();

        for (AdminNotification notification : notificationCache) {
            if (!notification.isRead()) {
                unreadNotifications++;
            }
            if (notification.getPriority() == AdminNotification.Priority.URGENT) {
                urgentNotifications++;
            }
            if (notification.isResolved()) {
                resolvedNotifications++;
            }

            // Count by type
            typeCounts.put(notification.getType(), typeCounts.getOrDefault(notification.getType(), 0) + 1);

            // Count by priority
            priorityCounts.put(notification.getPriority(), priorityCounts.getOrDefault(notification.getPriority(), 0) + 1);
        }

        stats.put("totalNotifications", totalNotifications);
        stats.put("unreadNotifications", unreadNotifications);
        stats.put("urgentNotifications", urgentNotifications);
        stats.put("resolvedNotifications", resolvedNotifications);
        stats.put("activeNotifications", totalNotifications - resolvedNotifications);
        stats.put("typeCounts", typeCounts);
        stats.put("priorityCounts", priorityCounts);
        stats.put("generatedAt", new Date());

        return stats;
    }

    /**
     * Shutdown scheduler
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        stopRealTimeMonitoring();
    }
}