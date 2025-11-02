package com.nmims.canteen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nmims.canteen.models.CartItem;
import com.nmims.canteen.models.FoodItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping cart management and persistence utility
 * Handles cart operations with local storage and Firebase synchronization
 */
public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREFS_NAME = "cart_prefs";
    private static final String CART_KEY = "cart_items";
    private static final String LAST_SYNC_KEY = "last_sync";

    private static CartManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private ArrayList<CartItem> cartItems;
    private final ArrayList<CartChangeListener> listeners;
    private Date lastSync;

    // Private constructor for singleton pattern
    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.cartItems = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.lastSync = new Date(preferences.getLong(LAST_SYNC_KEY, 0));

        // Load cart from local storage
        loadCartFromLocalStorage();

        // Sync with Firebase if user is authenticated
        if (FirebaseUtils.isUserAuthenticated()) {
            syncWithFirebase();
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    /**
     * Cart change listener interface
     */
    public interface CartChangeListener {
        void onCartChanged(ArrayList<CartItem> cartItems);
        void onItemAdded(CartItem item);
        void onItemRemoved(CartItem item);
        void onItemUpdated(CartItem item);
        void onCartCleared();
    }

    /**
     * Add cart change listener
     */
    public void addCartChangeListener(CartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove cart change listener
     */
    public void removeCartChangeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify listeners of cart changes
     */
    private void notifyCartChanged() {
        for (CartChangeListener listener : listeners) {
            listener.onCartChanged(new ArrayList<>(cartItems));
        }
    }

    private void notifyItemAdded(CartItem item) {
        for (CartChangeListener listener : listeners) {
            listener.onItemAdded(item);
        }
    }

    private void notifyItemRemoved(CartItem item) {
        for (CartChangeListener listener : listeners) {
            listener.onItemRemoved(item);
        }
    }

    private void notifyItemUpdated(CartItem item) {
        for (CartChangeListener listener : listeners) {
            listener.onItemUpdated(item);
        }
    }

    private void notifyCartCleared() {
        for (CartChangeListener listener : listeners) {
            listener.onCartCleared();
        }
    }

    // Cart Operations

    /**
     * Add item to cart
     */
    public boolean addItem(FoodItem foodItem, int quantity) {
        if (foodItem == null || quantity <= 0) {
            return false;
        }

        // Check if item already exists in cart
        CartItem existingItem = findItemByFoodItemId(foodItem.getItemId());
        if (existingItem != null) {
            // Update quantity of existing item
            int newQuantity = existingItem.getQuantity() + quantity;
            return updateItemQuantity(existingItem.getCartItemId(), newQuantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem(foodItem, quantity);
            cartItems.add(newItem);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemAdded(newItem);
            notifyCartChanged();

            Log.d(TAG, "Added item to cart: " + foodItem.getName() + " (Quantity: " + quantity + ")");
            return true;
        }
    }

    /**
     * Add item to cart with special instructions
     */
    public boolean addItem(FoodItem foodItem, int quantity, String specialInstructions) {
        if (addItem(foodItem, quantity)) {
            CartItem item = findItemByFoodItemId(foodItem.getItemId());
            if (item != null) {
                item.setSpecialInstructions(specialInstructions);
                saveCartToLocalStorage();
                syncWithFirebase();
                notifyItemUpdated(item);
            }
            return true;
        }
        return false;
    }

    /**
     * Remove item from cart
     */
    public boolean removeItem(String cartItemId) {
        CartItem itemToRemove = findItemByCartItemId(cartItemId);
        if (itemToRemove != null) {
            cartItems.remove(itemToRemove);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemRemoved(itemToRemove);
            notifyCartChanged();

            Log.d(TAG, "Removed item from cart: " + itemToRemove.getFoodItemName());
            return true;
        }
        return false;
    }

    /**
     * Update item quantity
     */
    public boolean updateItemQuantity(String cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(cartItemId);
        }

        CartItem item = findItemByCartItemId(cartItemId);
        if (item != null) {
            item.setQuantity(newQuantity);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemUpdated(item);
            notifyCartChanged();

            Log.d(TAG, "Updated item quantity: " + item.getFoodItemName() + " -> " + newQuantity);
            return true;
        }
        return false;
    }

    /**
     * Update item special instructions
     */
    public boolean updateItemInstructions(String cartItemId, String instructions) {
        CartItem item = findItemByCartItemId(cartItemId);
        if (item != null) {
            item.setSpecialInstructions(instructions);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemUpdated(item);

            return true;
        }
        return false;
    }

    /**
     * Apply discount to cart item
     */
    public boolean applyItemDiscount(String cartItemId, double discountAmount) {
        CartItem item = findItemByCartItemId(cartItemId);
        if (item != null) {
            item.applyDiscount(discountAmount);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemUpdated(item);
            notifyCartChanged();

            return true;
        }
        return false;
    }

    /**
     * Apply percentage discount to cart item
     */
    public boolean applyItemPercentageDiscount(String cartItemId, double discountPercentage) {
        CartItem item = findItemByCartItemId(cartItemId);
        if (item != null) {
            item.applyPercentageDiscount(discountPercentage);

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemUpdated(item);
            notifyCartChanged();

            return true;
        }
        return false;
    }

    /**
     * Remove discount from cart item
     */
    public boolean removeItemDiscount(String cartItemId) {
        CartItem item = findItemByCartItemId(cartItemId);
        if (item != null) {
            item.removeDiscount();

            // Save to local storage
            saveCartToLocalStorage();

            // Sync with Firebase
            syncWithFirebase();

            // Notify listeners
            notifyItemUpdated(item);
            notifyCartChanged();

            return true;
        }
        return false;
    }

    /**
     * Clear entire cart
     */
    public void clearCart() {
        cartItems.clear();

        // Save to local storage
        saveCartToLocalStorage();

        // Sync with Firebase
        syncWithFirebase();

        // Notify listeners
        notifyCartCleared();
        notifyCartChanged();

        Log.d(TAG, "Cart cleared");
    }

    /**
     * Merge cart with Firebase cart (for login scenarios)
     */
    public void mergeWithFirebaseCart(ArrayList<CartItem> firebaseCart) {
        if (firebaseCart == null || firebaseCart.isEmpty()) {
            return;
        }

        for (CartItem firebaseItem : firebaseCart) {
            CartItem localItem = findItemByFoodItemId(firebaseItem.getFoodItem().getItemId());
            if (localItem != null) {
                // Merge quantities - keep the higher one
                int mergedQuantity = Math.max(localItem.getQuantity(), firebaseItem.getQuantity());
                localItem.setQuantity(mergedQuantity);
            } else {
                // Add new item from Firebase
                cartItems.add(firebaseItem);
            }
        }

        // Save and sync
        saveCartToLocalStorage();
        syncWithFirebase();
        notifyCartChanged();

        Log.d(TAG, "Cart merged with Firebase cart");
    }

    // Getters

    /**
     * Get all cart items
     */
    public ArrayList<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    /**
     * Get cart item by cart item ID
     */
    public CartItem findItemByCartItemId(String cartItemId) {
        for (CartItem item : cartItems) {
            if (item.getCartItemId().equals(cartItemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get cart item by food item ID
     */
    public CartItem findItemByFoodItemId(String foodItemId) {
        for (CartItem item : cartItems) {
            if (item.getFoodItem().getItemId().equals(foodItemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get total number of items in cart
     */
    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * Get total number of unique items in cart
     */
    public int getUniqueItemCount() {
        return cartItems.size();
    }

    /**
     * Get total price of all items in cart
     */
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    /**
     * Get total original price (before discounts)
     */
    public double getOriginalTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getOriginalTotalPrice();
        }
        return total;
    }

    /**
     * Get total discount amount
     */
    public double getTotalDiscount() {
        return getOriginalTotalPrice() - getTotalPrice();
    }

    /**
     * Get total preparation time for all items
     */
    public int getTotalPreparationTime() {
        int maxTime = 0;
        for (CartItem item : cartItems) {
            maxTime = Math.max(maxTime, item.getTotalPreparationTime());
        }
        return maxTime;
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Check if cart contains items with discounts
     */
    public boolean hasDiscountedItems() {
        for (CartItem item : cartItems) {
            if (item.hasDiscount()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get cart items grouped by category
     */
    public Map<String, ArrayList<CartItem>> getItemsByCategory() {
        Map<String, ArrayList<CartItem>> categorizedItems = new HashMap<>();
        for (CartItem item : cartItems) {
            String category = item.getFoodItemCategory();
            if (!categorizedItems.containsKey(category)) {
                categorizedItems.put(category, new ArrayList<>());
            }
            categorizedItems.get(category).add(item);
        }
        return categorizedItems;
    }

    // Storage Operations

    /**
     * Save cart to local SharedPreferences
     */
    private void saveCartToLocalStorage() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (CartItem item : cartItems) {
                JSONObject itemJson = cartItemToJsonObject(item);
                jsonArray.put(itemJson);
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CART_KEY, jsonArray.toString());
            editor.putLong(LAST_SYNC_KEY, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cart saved to local storage");
        } catch (JSONException e) {
            Log.e(TAG, "Error saving cart to local storage", e);
        }
    }

    /**
     * Load cart from local SharedPreferences
     */
    private void loadCartFromLocalStorage() {
        try {
            String cartJson = preferences.getString(CART_KEY, "[]");
            JSONArray jsonArray = new JSONArray(cartJson);

            cartItems.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemJson = jsonArray.getJSONObject(i);
                CartItem item = jsonObjectToCartItem(itemJson);
                if (item != null) {
                    cartItems.add(item);
                }
            }

            Log.d(TAG, "Cart loaded from local storage: " + cartItems.size() + " items");
        } catch (JSONException e) {
            Log.e(TAG, "Error loading cart from local storage", e);
            cartItems.clear();
        }
    }

    /**
     * Sync cart with Firebase
     */
    public void syncWithFirebase() {
        if (!FirebaseUtils.isUserAuthenticated()) {
            return;
        }

        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            return;
        }

        try {
            // Convert cart items to map format for Firebase
            Map<String, Object> cartData = new HashMap<>();
            cartData.put("items", cartItemsToMapList());
            cartData.put("totalItems", getItemCount());
            cartData.put("totalPrice", getTotalPrice());
            cartData.put("lastUpdated", FirebaseUtils.getServerTimestamp());

            // Save to Firebase
            FirebaseUtils.getCurrentUserCartDocument()
                    .set(cartData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Cart synced with Firebase");
                            lastSync = new Date();
                        } else {
                            Log.e(TAG, "Error syncing cart with Firebase", task.getException());
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error syncing cart with Firebase", e);
        }
    }

    /**
     * Load cart from Firebase
     */
    public void loadFromFirebase(OnCompleteListener<ArrayList<CartItem>> onCompleteListener) {
        if (!FirebaseUtils.isUserAuthenticated()) {
            if (onCompleteListener != null) {
                onCompleteListener.onComplete(Task.forResult(new ArrayList<>()));
            }
            return;
        }

        FirebaseUtils.getCurrentUserCartDocument()
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList<CartItem> firebaseCart = new ArrayList<>();
                            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) document.get("items");
                            if (itemsMap != null) {
                                for (Map<String, Object> itemMap : itemsMap) {
                                    CartItem item = mapToCartItem(itemMap);
                                    if (item != null) {
                                        firebaseCart.add(item);
                                    }
                                }
                            }
                            if (onCompleteListener != null) {
                                onCompleteListener.onComplete(Task.forResult(firebaseCart));
                            }
                        } else {
                            if (onCompleteListener != null) {
                                onCompleteListener.onComplete(Task.forResult(new ArrayList<>()));
                            }
                        }
                    } else {
                        Log.e(TAG, "Error loading cart from Firebase", task.getException());
                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete(Task.forResult(new ArrayList<>()));
                        }
                    }
                });
    }

    // JSON Serialization Helper Methods

    private JSONObject cartItemToJsonObject(CartItem item) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("cartItemId", item.getCartItemId());
        json.put("quantity", item.getQuantity());
        json.put("totalPrice", item.getTotalPrice());
        json.put("unitPrice", item.getUnitPrice());
        json.put("addedAt", item.getAddedAt().getTime());
        json.put("specialInstructions", item.getSpecialInstructions());
        json.put("discountApplied", item.getDiscountApplied());

        // Convert FoodItem to JSON
        if (item.getFoodItem() != null) {
            JSONObject foodItemJson = foodItemToJsonObject(item.getFoodItem());
            json.put("foodItem", foodItemJson);
        }

        return json;
    }

    private JSONObject foodItemToJsonObject(FoodItem foodItem) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("itemId", foodItem.getItemId());
        json.put("name", foodItem.getName());
        json.put("description", foodItem.getDescription());
        json.put("price", foodItem.getPrice());
        json.put("imageUrl", foodItem.getImageUrl());
        json.put("category", foodItem.getCategory());
        json.put("isVegetarian", foodItem.isVegetarian());
        json.put("preparationTime", foodItem.getPreparationTime());
        return json;
    }

    private CartItem jsonObjectToCartItem(JSONObject json) {
        try {
            // Reconstruct FoodItem
            JSONObject foodItemJson = json.getJSONObject("foodItem");
            FoodItem foodItem = jsonObjectToFoodItem(foodItemJson);

            if (foodItem == null) {
                return null;
            }

            // Reconstruct CartItem
            CartItem item = new CartItem(foodItem, json.getInt("quantity"));
            item.setCartItemId(json.getString("cartItemId"));
            item.setTotalPrice(json.getDouble("totalPrice"));
            item.setUnitPrice(json.getDouble("unitPrice"));
            item.setAddedAt(new Date(json.getLong("addedAt")));
            item.setSpecialInstructions(json.optString("specialInstructions", ""));
            item.setDiscountApplied(json.optDouble("discountApplied", 0.0));

            return item;

        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to CartItem", e);
            return null;
        }
    }

    private FoodItem jsonObjectToFoodItem(JSONObject json) {
        try {
            FoodItem foodItem = new FoodItem();
            foodItem.setItemId(json.getString("itemId"));
            foodItem.setName(json.getString("name"));
            foodItem.setDescription(json.optString("description", ""));
            foodItem.setPrice(json.getDouble("price"));
            foodItem.setImageUrl(json.optString("imageUrl", ""));
            foodItem.setCategory(json.optString("category", ""));
            foodItem.setVegetarian(json.optBoolean("isVegetarian", false));
            foodItem.setPreparationTime(json.optInt("preparationTime", 10));

            return foodItem;

        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to FoodItem", e);
            return null;
        }
    }

    private List<Map<String, Object>> cartItemsToMapList() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = cartItemToMap(item);
            mapList.add(itemMap);
        }
        return mapList;
    }

    private Map<String, Object> cartItemToMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("cartItemId", item.getCartItemId());
        map.put("quantity", item.getQuantity());
        map.put("totalPrice", item.getTotalPrice());
        map.put("unitPrice", item.getUnitPrice());
        map.put("addedAt", item.getAddedAt());
        map.put("specialInstructions", item.getSpecialInstructions());
        map.put("discountApplied", item.getDiscountApplied());

        if (item.getFoodItem() != null) {
            map.put("foodItem", foodItemToMap(item.getFoodItem()));
        }

        return map;
    }

    private Map<String, Object> foodItemToMap(FoodItem foodItem) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", foodItem.getItemId());
        map.put("name", foodItem.getName());
        map.put("description", foodItem.getDescription());
        map.put("price", foodItem.getPrice());
        map.put("imageUrl", foodItem.getImageUrl());
        map.put("category", foodItem.getCategory());
        map.put("isVegetarian", foodItem.isVegetarian());
        map.put("preparationTime", foodItem.getPreparationTime());
        return map;
    }

    private CartItem mapToCartItem(Map<String, Object> map) {
        try {
            // Reconstruct FoodItem
            @SuppressWarnings("unchecked")
            Map<String, Object> foodItemMap = (Map<String, Object>) map.get("foodItem");
            FoodItem foodItem = mapToFoodItem(foodItemMap);

            if (foodItem == null) {
                return null;
            }

            // Reconstruct CartItem
            CartItem item = new CartItem(foodItem, ((Number) map.get("quantity")).intValue());
            item.setCartItemId((String) map.get("cartItemId"));
            item.setTotalPrice(((Number) map.get("totalPrice")).doubleValue());
            item.setUnitPrice(((Number) map.get("unitPrice")).doubleValue());
            item.setAddedAt((Date) map.get("addedAt"));
            item.setSpecialInstructions((String) map.get("specialInstructions"));
            item.setDiscountApplied(((Number) map.get("discountApplied")).doubleValue());

            return item;

        } catch (Exception e) {
            Log.e(TAG, "Error converting Map to CartItem", e);
            return null;
        }
    }

    private FoodItem mapToFoodItem(Map<String, Object> map) {
        try {
            FoodItem foodItem = new FoodItem();
            foodItem.setItemId((String) map.get("itemId"));
            foodItem.setName((String) map.get("name"));
            foodItem.setDescription((String) map.get("description"));
            foodItem.setPrice(((Number) map.get("price")).doubleValue());
            foodItem.setImageUrl((String) map.get("imageUrl"));
            foodItem.setCategory((String) map.get("category"));
            foodItem.setVegetarian((Boolean) map.getOrDefault("isVegetarian", false));
            foodItem.setPreparationTime(((Number) map.getOrDefault("preparationTime", 10)).intValue());

            return foodItem;

        } catch (Exception e) {
            Log.e(TAG, "Error converting Map to FoodItem", e);
            return null;
        }
    }

    // Utility Methods

    /**
     * Validate cart items (check if items are still available, prices haven't changed, etc.)
     */
    public void validateCart(OnCompleteListener<Boolean> onCompleteListener) {
        // This would check each cart item against current food item data
        // to ensure items are still available and prices are up to date
        // For now, just return true
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(Task.forResult(true));
        }
    }

    /**
     * Get last sync timestamp
     */
    public Date getLastSync() {
        return lastSync;
    }

    /**
     * Force sync with Firebase
     */
    public void forceSync() {
        syncWithFirebase();
    }
}