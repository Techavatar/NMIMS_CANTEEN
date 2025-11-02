package com.nmims.canteen.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.nmims.canteen.models.FoodItem;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.Review;
import com.nmims.canteen.models.User;
import com.nmims.canteen.models.CartItem;
import com.nmims.canteen.models.InventoryItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Firestore database operations service
 * Handles all database CRUD operations and real-time data synchronization
 */
public class FirestoreService {
    private static final String TAG = "FirestoreService";
    private static FirestoreService instance;

    private final Executor executor;

    // Real-time listeners
    private final Map<String, com.google.firebase.firestore.ListenerRegistration> activeListeners;

    // Database operation callbacks
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public interface RealtimeDataCallback<T> {
        void onDataChanged(T data);
        void onError(String error);
    }

    private FirestoreService() {
        this.executor = Executors.newFixedThreadPool(4);
        this.activeListeners = new ConcurrentHashMap<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    // Food Items Operations

    /**
     * Get all food items
     */
    public void getAllFoodItems(DatabaseCallback<List<FoodItem>> callback) {
        FirebaseUtils.getAvailableFoodItemsQuery()
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<FoodItem> foodItems = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setItemId(doc.getId());
                            foodItems.add(item);
                        }
                    }
                    if (callback != null) callback.onSuccess(foodItems);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting food items", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get food item by ID
     */
    public void getFoodItemById(String itemId, DatabaseCallback<FoodItem> callback) {
        FirebaseUtils.getFoodItemDocument(itemId)
                .get()
                .addOnSuccessListener(executor, documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FoodItem item = documentSnapshot.toObject(FoodItem.class);
                        if (item != null) {
                            item.setItemId(documentSnapshot.getId());
                        }
                        if (callback != null) callback.onSuccess(item);
                    } else {
                        if (callback != null) callback.onFailure("Food item not found");
                    }
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting food item", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get food items by category
     */
    public void getFoodItemsByCategory(String category, DatabaseCallback<List<FoodItem>> callback) {
        FirebaseUtils.getFoodItemsByCategoryQuery(category)
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<FoodItem> foodItems = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setItemId(doc.getId());
                            foodItems.add(item);
                        }
                    }
                    if (callback != null) callback.onSuccess(foodItems);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting food items by category", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get featured food items
     */
    public void getFeaturedFoodItems(DatabaseCallback<List<FoodItem>> callback) {
        FirebaseUtils.getFeaturedFoodItemsQuery()
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<FoodItem> foodItems = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setItemId(doc.getId());
                            foodItems.add(item);
                        }
                    }
                    if (callback != null) callback.onSuccess(foodItems);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting featured food items", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Add new food item (admin only)
     */
    public void addFoodItem(FoodItem foodItem, DatabaseCallback<String> callback) {
        if (foodItem == null) {
            if (callback != null) callback.onFailure("Food item cannot be null");
            return;
        }

        // Generate ID if not provided
        if (foodItem.getItemId() == null || foodItem.getItemId().isEmpty()) {
            foodItem.setItemId(FirebaseUtils.generateDocumentId());
        }

        // Set timestamps
        foodItem.setCreatedAt(new Date());
        foodItem.setUpdatedAt(new Date());

        FirebaseUtils.getFoodItemDocument(foodItem.getItemId())
                .set(foodItem)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Food item added successfully: " + foodItem.getItemId());
                    if (callback != null) callback.onSuccess(foodItem.getItemId());
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error adding food item", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Update food item (admin only)
     */
    public void updateFoodItem(FoodItem foodItem, DatabaseCallback<Boolean> callback) {
        if (foodItem == null || foodItem.getItemId() == null) {
            if (callback != null) callback.onFailure("Invalid food item");
            return;
        }

        // Update timestamp
        foodItem.setUpdatedAt(new Date());

        FirebaseUtils.getFoodItemDocument(foodItem.getItemId())
                .set(foodItem)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Food item updated successfully: " + foodItem.getItemId());
                    if (callback != null) callback.onSuccess(true);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error updating food item", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Delete food item (admin only)
     */
    public void deleteFoodItem(String itemId, DatabaseCallback<Boolean> callback) {
        if (itemId == null || itemId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid food item ID");
            return;
        }

        FirebaseUtils.getFoodItemDocument(itemId)
                .delete()
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Food item deleted successfully: " + itemId);
                    if (callback != null) callback.onSuccess(true);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error deleting food item", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    // Orders Operations

    /**
     * Create new order
     */
    public void createOrder(Order order, DatabaseCallback<String> callback) {
        if (order == null) {
            if (callback != null) callback.onFailure("Order cannot be null");
            return;
        }

        // Generate order ID if not provided
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(FirebaseUtils.generateDocumentId());
        }

        // Set timestamps
        order.setCreatedAt(new Date());
        order.setLastUpdatedAt(new Date());

        FirebaseUtils.getOrderDocument(order.getOrderId())
                .set(order)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Order created successfully: " + order.getOrderId());

                    // Update inventory if order is confirmed
                    if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
                        updateInventoryForOrder(order);
                    }

                    if (callback != null) callback.onSuccess(order.getOrderId());
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error creating order", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get user orders
     */
    public void getUserOrders(String userId, DatabaseCallback<List<Order>> callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid user ID");
            return;
        }

        FirebaseUtils.getUserOrdersQuery(userId)
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId());
                            orders.add(order);
                        }
                    }
                    if (callback != null) callback.onSuccess(orders);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting user orders", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get order by ID
     */
    public void getOrderById(String orderId, DatabaseCallback<Order> callback) {
        if (orderId == null || orderId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid order ID");
            return;
        }

        FirebaseUtils.getOrderDocument(orderId)
                .get()
                .addOnSuccessListener(executor, documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(documentSnapshot.getId());
                        }
                        if (callback != null) callback.onSuccess(order);
                    } else {
                        if (callback != null) callback.onFailure("Order not found");
                    }
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting order", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Update order status (admin only)
     */
    public void updateOrderStatus(String orderId, Order.OrderStatus newStatus, DatabaseCallback<Boolean> callback) {
        if (orderId == null || orderId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid order ID");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus.toString());
        updates.put("lastUpdatedAt", new Date());

        // Add timestamp based on status
        Date now = new Date();
        switch (newStatus) {
            case CONFIRMED:
                updates.put("confirmedAt", now);
                break;
            case PREPARING:
                updates.put("preparingAt", now);
                break;
            case READY:
                updates.put("readyAt", now);
                break;
            case DELIVERED:
                updates.put("deliveredAt", now);
                break;
            case CANCELLED:
                updates.put("cancelledAt", now);
                break;
        }

        FirebaseUtils.getOrderDocument(orderId)
                .update(updates)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Order status updated successfully: " + orderId);
                    if (callback != null) callback.onSuccess(true);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error updating order status", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get orders by status (admin only)
     */
    public void getOrdersByStatus(Order.OrderStatus status, DatabaseCallback<List<Order>> callback) {
        FirebaseUtils.getOrdersByStatusQuery(status.toString())
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId());
                            orders.add(order);
                        }
                    }
                    if (callback != null) callback.onSuccess(orders);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting orders by status", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    // Reviews Operations

    /**
     * Add new review
     */
    public void addReview(Review review, DatabaseCallback<String> callback) {
        if (review == null) {
            if (callback != null) callback.onFailure("Review cannot be null");
            return;
        }

        // Generate review ID if not provided
        if (review.getReviewId() == null || review.getReviewId().isEmpty()) {
            review.setReviewId(FirebaseUtils.generateDocumentId());
        }

        // Set timestamps
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        FirebaseUtils.getReviewDocument(review.getReviewId())
                .set(review)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Review added successfully: " + review.getReviewId());

                    // Update food item rating
                    updateFoodItemRating(review.getFoodItemId());

                    if (callback != null) callback.onSuccess(review.getReviewId());
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error adding review", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get food item reviews
     */
    public void getFoodItemReviews(String foodItemId, DatabaseCallback<List<Review>> callback) {
        if (foodItemId == null || foodItemId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid food item ID");
            return;
        }

        FirebaseUtils.getFoodItemReviewsQuery(foodItemId)
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Review review = doc.toObject(Review.class);
                        if (review != null) {
                            review.setReviewId(doc.getId());
                            reviews.add(review);
                        }
                    }
                    if (callback != null) callback.onSuccess(reviews);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting food item reviews", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Get user reviews
     */
    public void getUserReviews(String userId, DatabaseCallback<List<Review>> callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid user ID");
            return;
        }

        FirebaseUtils.getUserReviewsQuery(userId)
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Review review = doc.toObject(Review.class);
                        if (review != null) {
                            review.setReviewId(doc.getId());
                            reviews.add(review);
                        }
                    }
                    if (callback != null) callback.onSuccess(reviews);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error getting user reviews", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Update review (owner or admin only)
     */
    public void updateReview(Review review, DatabaseCallback<Boolean> callback) {
        if (review == null || review.getReviewId() == null) {
            if (callback != null) callback.onFailure("Invalid review");
            return;
        }

        // Update timestamp
        review.setUpdatedAt(new Date());

        FirebaseUtils.getReviewDocument(review.getReviewId())
                .set(review)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Review updated successfully: " + review.getReviewId());

                    // Update food item rating
                    updateFoodItemRating(review.getFoodItemId());

                    if (callback != null) callback.onSuccess(true);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error updating review", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    /**
     * Delete review (owner or admin only)
     */
    public void deleteReview(String reviewId, DatabaseCallback<Boolean> callback) {
        if (reviewId == null || reviewId.isEmpty()) {
            if (callback != null) callback.onFailure("Invalid review ID");
            return;
        }

        FirebaseUtils.getReviewDocument(reviewId)
                .delete()
                .addOnSuccessListener(executor, aVoid -> {
                    Log.d(TAG, "Review deleted successfully: " + reviewId);
                    if (callback != null) callback.onSuccess(true);
                })
                .addOnFailureListener(executor, e -> {
                    Log.e(TAG, "Error deleting review", e);
                    if (callback != null) callback.onFailure(getErrorMessage(e));
                });
    }

    // Real-time Listeners

    /**
     * Listen to food items updates
     */
    public void listenToFoodItemsUpdates(RealtimeDataCallback<List<FoodItem>> callback) {
        String listenerId = "food_items_updates";

        // Remove existing listener if any
        removeListener(listenerId);

        com.google.firebase.firestore.ListenerRegistration registration = FirebaseUtils.getAvailableFoodItemsQuery()
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Food items listener error", e);
                        if (callback != null) callback.onError(getErrorMessage(e));
                        return;
                    }

                    List<FoodItem> foodItems = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            if (item != null) {
                                item.setItemId(doc.getId());
                                foodItems.add(item);
                            }
                        }
                    }

                    if (callback != null) callback.onDataChanged(foodItems);
                });

        activeListeners.put(listenerId, registration);
    }

    /**
     * Listen to order updates for a user
     */
    public void listenToOrderUpdates(String userId, RealtimeDataCallback<List<Order>> callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        String listenerId = "order_updates_" + userId;

        // Remove existing listener if any
        removeListener(listenerId);

        com.google.firebase.firestore.ListenerRegistration registration = FirebaseUtils.getUserOrdersQuery(userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Orders listener error", e);
                        if (callback != null) callback.onError(getErrorMessage(e));
                        return;
                    }

                    List<Order> orders = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                orders.add(order);
                            }
                        }
                    }

                    if (callback != null) callback.onDataChanged(orders);
                });

        activeListeners.put(listenerId, registration);
    }

    /**
     * Remove listener
     */
    public void removeListener(String listenerId) {
        com.google.firebase.firestore.ListenerRegistration registration = activeListeners.remove(listenerId);
        if (registration != null) {
            registration.remove();
            Log.d(TAG, "Listener removed: " + listenerId);
        }
    }

    /**
     * Remove all listeners
     */
    public void removeAllListeners() {
        for (Map.Entry<String, com.google.firebase.firestore.ListenerRegistration> entry : activeListeners.entrySet()) {
            entry.getValue().remove();
        }
        activeListeners.clear();
        Log.d(TAG, "All listeners removed");
    }

    // Transaction Operations

    /**
     * Process order with inventory updates in transaction
     */
    public void processOrderWithInventory(Order order, DatabaseCallback<String> callback) {
        if (order == null) {
            if (callback != null) callback.onFailure("Order cannot be null");
            return;
        }

        FirebaseUtils.getFirestore().runTransaction((Transaction.Function<TransactionResult>) transaction -> {
            try {
                // Check inventory for all items
                for (CartItem item : order.getItems()) {
                    DocumentReference inventoryRef = FirebaseUtils.getInventoryItemDocument(item.getFoodItem().getItemId());
                    DocumentSnapshot inventoryDoc = transaction.get(inventoryRef);

                    if (!inventoryDoc.exists()) {
                        throw new Exception("Inventory item not found: " + item.getFoodItem().getItemId());
                    }

                    InventoryItem inventoryItem = inventoryDoc.toObject(InventoryItem.class);
                    if (inventoryItem == null || inventoryItem.getCurrentStock() < item.getQuantity()) {
                        throw new Exception("Insufficient stock for: " + item.getFoodItem().getName());
                    }
                }

                // Create order
                DocumentReference orderRef = FirebaseUtils.getOrderDocument(order.getOrderId());
                transaction.set(orderRef, order);

                // Update inventory
                for (CartItem item : order.getItems()) {
                    DocumentReference inventoryRef = FirebaseUtils.getInventoryItemDocument(item.getFoodItem().getItemId());
                    DocumentSnapshot inventoryDoc = transaction.get(inventoryRef);
                    InventoryItem inventoryItem = inventoryDoc.toObject(InventoryItem.class);

                    if (inventoryItem != null) {
                        int newStock = inventoryItem.getCurrentStock() - item.getQuantity();
                        transaction.update(inventoryRef, "currentStock", newStock);
                        transaction.update(inventoryRef, "updatedAt", new Date());

                        // Add movement to history
                        Map<String, Object> movement = new HashMap<>();
                        movement.put("movementType", "SALE");
                        movement.put("quantity", -item.getQuantity());
                        movement.put("reason", "Order: " + order.getOrderId());
                        movement.put("performedBy", "system");
                        movement.put("timestamp", new Date());

                        // This would need to be handled properly with array operations
                    }
                }

                return TransactionResult.success(true);

            } catch (Exception e) {
                return TransactionResult.rollback(e);
            }
        }).addOnSuccessListener(executor, result -> {
            Log.d(TAG, "Order processed with inventory updates: " + order.getOrderId());
            if (callback != null) callback.onSuccess(order.getOrderId());
        }).addOnFailureListener(executor, e -> {
            Log.e(TAG, "Failed to process order with inventory", e);
            if (callback != null) callback.onFailure(getErrorMessage(e));
        });
    }

    // Helper Methods

    /**
     * Update food item rating after review changes
     */
    private void updateFoodItemRating(String foodItemId) {
        FirebaseUtils.getFoodItemReviewsQuery(foodItemId)
                .get()
                .addOnSuccessListener(executor, queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        double totalRating = 0;
                        int reviewCount = 0;

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            if (review != null && review.isApproved()) {
                                totalRating += review.getRating();
                                reviewCount++;
                            }
                        }

                        if (reviewCount > 0) {
                            double averageRating = totalRating / reviewCount;

                            FirebaseUtils.getFoodItemDocument(foodItemId)
                                    .update("rating", averageRating, "reviewCount", reviewCount)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Food item rating updated: " + foodItemId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update food item rating", e);
                                    });
                        }
                    }
                });
    }

    /**
     * Update inventory for confirmed order
     */
    private void updateInventoryForOrder(Order order) {
        for (CartItem item : order.getItems()) {
            String itemId = item.getFoodItem().getItemId();
            int quantity = item.getQuantity();

            FirebaseUtils.getInventoryItemDocument(itemId)
                    .get()
                    .addOnSuccessListener(executor, documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            InventoryItem inventoryItem = documentSnapshot.toObject(InventoryItem.class);
                            if (inventoryItem != null) {
                                int newStock = inventoryItem.getCurrentStock() - quantity;
                                if (newStock >= 0) {
                                    FirebaseUtils.getInventoryItemDocument(itemId)
                                            .update("currentStock", newStock, "updatedAt", new Date())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Inventory updated for order: " + itemId);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to update inventory", e);
                                            });
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Get user-friendly error message
     */
    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }

        String message = exception.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return "An error occurred while processing your request";
    }

    /**
     * Transaction result class
     */
    private static class TransactionResult {
        private final boolean success;
        private final Exception error;

        private TransactionResult(boolean success) {
            this.success = success;
            this.error = null;
        }

        private TransactionResult(Exception error) {
            this.success = false;
            this.error = error;
        }

        public static TransactionResult success(boolean success) {
            return new TransactionResult(success);
        }

        public static TransactionResult rollback(Exception error) {
            return new TransactionResult(error);
        }

        public boolean isSuccess() {
            return success;
        }

        public Exception getError() {
            return error;
        }
    }
}