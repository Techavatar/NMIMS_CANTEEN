package com.nmims.canteen.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Firebase initialization and common operations utility class
 * Provides centralized access to Firebase services and common database operations
 */
public class FirebaseUtils {
    // Firebase instances
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mFirestore;
    private static FirebaseStorage mStorage;

    // Collection names
    public static final String USERS_COLLECTION = "users";
    public static final String FOOD_ITEMS_COLLECTION = "food_items";
    public static final String ORDERS_COLLECTION = "orders";
    public static final String REVIEWS_COLLECTION = "reviews";
    public static final String CARTS_COLLECTION = "carts";
    public static final String CATEGORIES_COLLECTION = "categories";
    public static final String INVENTORY_COLLECTION = "inventory";
    public static final String SALES_ANALYTICS_COLLECTION = "sales_analytics";
    public static final String ADMIN_NOTIFICATIONS_COLLECTION = "admin_notifications";
    public static final String CANTEEN_SETTINGS_COLLECTION = "canteen_settings";
    public static final String INVENTORY_LOGS_COLLECTION = "inventory_logs";
    public static final String USER_SESSIONS_COLLECTION = "user_sessions";

    // Storage paths
    public static final String FOOD_IMAGES_PATH = "food_images/";
    public static final String USER_PROFILES_PATH = "user_profiles/";
    public static final String REVIEW_IMAGES_PATH = "review_images/";
    public static final String REPORTS_PATH = "reports/";

    // Initialize Firebase services
    static {
        try {
            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mStorage = FirebaseStorage.getInstance();

            // Configure Firestore settings for offline support
            FirebaseFirestore.Settings settings = new FirebaseFirestore.Settings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            mFirestore.setFirestoreSettings(settings);

        } catch (Exception e) {
            // Handle initialization error
            e.printStackTrace();
        }
    }

    /**
     * Initialize Firebase services
     */
    public static void initializeFirebase() {
        // Firebase is already initialized in static block
        // This method can be used for additional setup if needed
    }

    /**
     * Get current authenticated user
     */
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Get current user ID
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Get FirebaseAuth instance
     */
    public static FirebaseAuth getAuth() {
        return mAuth;
    }

    /**
     * Get FirebaseFirestore instance
     */
    public static FirebaseFirestore getFirestore() {
        return mFirestore;
    }

    /**
     * Get FirebaseStorage instance
     */
    public static FirebaseStorage getStorage() {
        return mStorage;
    }

    // Collection References

    /**
     * Get users collection reference
     */
    public static CollectionReference getUsersCollection() {
        return mFirestore.collection(USERS_COLLECTION);
    }

    /**
     * Get specific user document reference
     */
    public static DocumentReference getUserDocument(String userId) {
        return getUsersCollection().document(userId);
    }

    /**
     * Get current user document reference
     */
    public static DocumentReference getCurrentUserDocument() {
        String userId = getCurrentUserId();
        return userId != null ? getUserDocument(userId) : null;
    }

    /**
     * Get food items collection reference
     */
    public static CollectionReference getFoodItemsCollection() {
        return mFirestore.collection(FOOD_ITEMS_COLLECTION);
    }

    /**
     * Get specific food item document reference
     */
    public static DocumentReference getFoodItemDocument(String itemId) {
        return getFoodItemsCollection().document(itemId);
    }

    /**
     * Get available food items query
     */
    public static Query getAvailableFoodItemsQuery() {
        return getFoodItemsCollection()
                .whereEqualTo("isAvailable", true)
                .orderBy("name");
    }

    /**
     * Get food items by category query
     */
    public static Query getFoodItemsByCategoryQuery(String category) {
        return getFoodItemsCollection()
                .whereEqualTo("category", category)
                .whereEqualTo("isAvailable", true)
                .orderBy("name");
    }

    /**
     * Get featured food items query
     */
    public static Query getFeaturedFoodItemsQuery() {
        return getFoodItemsCollection()
                .whereEqualTo("isFeatured", true)
                .whereEqualTo("isAvailable", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10);
    }

    /**
     * Get orders collection reference
     */
    public static CollectionReference getOrdersCollection() {
        return mFirestore.collection(ORDERS_COLLECTION);
    }

    /**
     * Get specific order document reference
     */
    public static DocumentReference getOrderDocument(String orderId) {
        return getOrdersCollection().document(orderId);
    }

    /**
     * Get user orders query
     */
    public static Query getUserOrdersQuery(String userId) {
        return getOrdersCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get current user orders query
     */
    public static Query getCurrentUserOrdersQuery() {
        String userId = getCurrentUserId();
        return userId != null ? getUserOrdersQuery(userId) : null;
    }

    /**
     * Get orders by status query
     */
    public static Query getOrdersByStatusQuery(String status) {
        return getOrdersCollection()
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get reviews collection reference
     */
    public static CollectionReference getReviewsCollection() {
        return mFirestore.collection(REVIEWS_COLLECTION);
    }

    /**
     * Get specific review document reference
     */
    public static DocumentReference getReviewDocument(String reviewId) {
        return getReviewsCollection().document(reviewId);
    }

    /**
     * Get food item reviews query
     */
    public static Query getFoodItemReviewsQuery(String foodItemId) {
        return getReviewsCollection()
                .whereEqualTo("foodItemId", foodItemId)
                .whereEqualTo("isApproved", true)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get user reviews query
     */
    public static Query getUserReviewsQuery(String userId) {
        return getReviewsCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get pending reviews query (for admin moderation)
     */
    public static Query getPendingReviewsQuery() {
        return getReviewsCollection()
                .whereEqualTo("isApproved", false)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get carts collection reference
     */
    public static CollectionReference getCartsCollection() {
        return mFirestore.collection(CARTS_COLLECTION);
    }

    /**
     * Get user cart document reference
     */
    public static DocumentReference getUserCartDocument(String userId) {
        return getCartsCollection().document(userId);
    }

    /**
     * Get current user cart document reference
     */
    public static DocumentReference getCurrentUserCartDocument() {
        String userId = getCurrentUserId();
        return userId != null ? getUserCartDocument(userId) : null;
    }

    /**
     * Get categories collection reference
     */
    public static CollectionReference getCategoriesCollection() {
        return mFirestore.collection(CATEGORIES_COLLECTION);
    }

    /**
     * Get inventory collection reference
     */
    public static CollectionReference getInventoryCollection() {
        return mFirestore.collection(INVENTORY_COLLECTION);
    }

    /**
     * Get inventory item document reference
     */
    public static DocumentReference getInventoryItemDocument(String inventoryId) {
        return getInventoryCollection().document(inventoryId);
    }

    /**
     * Get low stock items query
     */
    public static Query getLowStockItemsQuery() {
        return getInventoryCollection()
                .whereLessThanOrEqualTo("currentStock", "lowStockThreshold")
                .whereEqualTo("isActive", true)
                .orderBy("currentStock", Query.Direction.ASCENDING);
    }

    /**
     * Get expiring items query
     */
    public static Query getExpiringItemsQuery() {
        // This would need proper date handling in a real implementation
        return getInventoryCollection()
                .whereEqualTo("expiryAlert", true)
                .whereEqualTo("isActive", true)
                .orderBy("expiryDate");
    }

    /**
     * Get sales analytics collection reference
     */
    public static CollectionReference getSalesAnalyticsCollection() {
        return mFirestore.collection(SALES_ANALYTICS_COLLECTION);
    }

    /**
     * Get sales data for specific date
     */
    public static DocumentReference getSalesDataDocument(String dateId) {
        return getSalesAnalyticsCollection().document(dateId);
    }

    /**
     * Get admin notifications collection reference
     */
    public static CollectionReference getAdminNotificationsCollection() {
        return mFirestore.collection(ADMIN_NOTIFICATIONS_COLLECTION);
    }

    /**
     * Get specific notification document reference
     */
    public static DocumentReference getNotificationDocument(String notificationId) {
        return getAdminNotificationsCollection().document(notificationId);
    }

    /**
     * Get unread notifications query
     */
    public static Query getUnreadNotificationsQuery() {
        return getAdminNotificationsCollection()
                .whereEqualTo("isRead", false)
                .orderBy("priority", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get notifications by type query
     */
    public static Query getNotificationsByTypeQuery(String type) {
        return getAdminNotificationsCollection()
                .whereEqualTo("type", type)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Get canteen settings collection reference
     */
    public static CollectionReference getCanteenSettingsCollection() {
        return mFirestore.collection(CANTEEN_SETTINGS_COLLECTION);
    }

    /**
     * Get specific setting document reference
     */
    public static DocumentReference getSettingDocument(String settingKey) {
        return getCanteenSettingsCollection().document(settingKey);
    }

    /**
     * Get inventory logs collection reference
     */
    public static CollectionReference getInventoryLogsCollection() {
        return mFirestore.collection(INVENTORY_LOGS_COLLECTION);
    }

    /**
     * Get recent inventory logs query
     */
    public static Query getRecentInventoryLogsQuery() {
        return getInventoryLogsCollection()
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100);
    }

    /**
     * Get user sessions collection reference
     */
    public static CollectionReference getUserSessionsCollection() {
        return mFirestore.collection(USER_SESSIONS_COLLECTION);
    }

    // Storage References

    /**
     * Get food images storage reference
     */
    public static StorageReference getFoodImagesStorageReference() {
        return mStorage.getReference().child(FOOD_IMAGES_PATH);
    }

    /**
     * Get specific food image storage reference
     */
    public static StorageReference getFoodImageStorageReference(String imageName) {
        return getFoodImagesStorageReference().child(imageName);
    }

    /**
     * Get user profiles storage reference
     */
    public static StorageReference getUserProfilesStorageReference() {
        return mStorage.getReference().child(USER_PROFILES_PATH);
    }

    /**
     * Get specific user profile image storage reference
     */
    public static StorageReference getUserProfileStorageReference(String userId) {
        return getUserProfilesStorageReference().child(userId + ".jpg");
    }

    /**
     * Get review images storage reference
     */
    public static StorageReference getReviewImagesStorageReference() {
        return mStorage.getReference().child(REVIEW_IMAGES_PATH);
    }

    /**
     * Get specific review image storage reference
     */
    public static StorageReference getReviewImageStorageReference(String imageName) {
        return getReviewImagesStorageReference().child(imageName);
    }

    /**
     * Get reports storage reference
     */
    public static StorageReference getReportsStorageReference() {
        return mStorage.getReference().child(REPORTS_PATH);
    }

    // Utility Methods

    /**
     * Generate unique document ID
     */
    public static String generateDocumentId() {
        return mFirestore.collection("_").document().getId();
    }

    /**
     * Create a batched write operation
     */
    public static com.google.firebase.firestore.WriteBatch createBatch() {
        return mFirestore.batch();
    }

    /**
     * Run a transaction
     */
    public static com.google.android.gms.tasks.Task<TransactionResult> runTransaction(
            com.google.firebase.firestore.Transaction.Function<TransactionResult> transactionFunc) {
        return mFirestore.runTransaction(transactionFunc);
    }

    // Transaction result class
    public static class TransactionResult {
        private boolean success;
        private String message;
        private Object data;

        public TransactionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public TransactionResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    /**
     * Enable offline persistence
     */
    public static void enableOfflinePersistence() {
        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            mFirestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    /**
     * Clear Firestore cache
     */
    public static void clearCache() {
        mFirestore.clearPersistence();
    }

    /**
     * Check network connectivity
     */
    public static boolean isNetworkAvailable() {
        // This would need actual network checking implementation
        return true; // Placeholder
    }

    /**
     * Sign out current user
     */
    public static void signOut() {
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

    /**
     * Get timestamp for server
     */
    public static com.google.firebase.firestore.FieldValue getServerTimestamp() {
        return com.google.firebase.firestore.FieldValue.serverTimestamp();
    }

    /**
     * Delete user account and all associated data
     */
    public static void deleteUserAccount() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            // Delete user document
            getCurrentUserDocument().delete();

            // Delete user's cart
            getCurrentUserCartDocument().delete();

            // Delete user's reviews (this would need to be done carefully)
            // getCurrentUserReviewsQuery().get().addOnSuccessListener...

            // Delete Firebase Auth account
            user.delete();
        }
    }
}