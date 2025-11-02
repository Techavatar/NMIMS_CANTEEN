package com.nmims.canteen.utils;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to initialize Firebase database with sample data
 * Including the food items provided by the user
 */
public class FirebaseDataInitializer {

    private static final String TAG = "FirebaseDataInitializer";
    private final FirebaseFirestore db;
    private final Context context;

    public FirebaseDataInitializer(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Initialize the database with food items from user's list
     */
    public void initializeFoodItems() {
        List<Map<String, Object>> foodItems = createFoodItemsFromUserList();

        WriteBatch batch = db.batch();

        for (Map<String, Object> foodItem : foodItems) {
            String itemId = (String) foodItem.get("itemId");
            batch.set(db.collection("foodItems").document(itemId), foodItem);
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Food items initialized successfully");
                // Initialize categories
                initializeCategories();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error initializing food items", e);
            });
    }

    /**
     * Create food items from the list provided by user
     */
    private List<Map<String, Object>> createFoodItemsFromUserList() {
        return Arrays.asList(
            createFoodItem("aloo_paratha", "Aloo Paratha",
                "Crispy and delicious potato-stuffed flatbread served with butter and curd",
                "Indian Breakfast", 60.00, "aloo_paratha.jpg", true, 15),

            createFoodItem("bread_pakora", "Bread Pakora",
                "Crispy bread fritters stuffed with spicy potato filling",
                "Snacks", 40.00, "bread_pakora.jpg", true, 12),

            createFoodItem("burger", "Veg Burger",
                "Juicy vegetable patty with fresh lettuce, tomato, and special sauce",
                "Fast Food", 80.00, "burger.jpg", true, 10),

            createFoodItem("cheese_sandwich", "Cheese Sandwich",
                "Grilled cheese sandwich with melted cheese and vegetables",
                "Snacks", 50.00, "cheese_sandwich.jpg", true, 8),

            createFoodItem("chole_bhature", "Chole Bhature",
                "Spicy chickpea curry served with fluffy fried bread",
                "North Indian", 90.00, "chole_bhature.jpg", true, 20),

            createFoodItem("chole_kulche", "Chole Kulche",
                "Spicy chickpea curry served with soft leavened bread",
                "North Indian", 70.00, "chole_kulche.jpg", true, 15),

            createFoodItem("idli_sambar", "Idli Sambar",
                "Soft steamed rice cakes served with lentil soup",
                "South Indian", 60.00, "idli_sambar.jpg", true, 12),

            createFoodItem("khamand", "Khamand",
                "Soft and spongy steamed savory cake with sweet and spicy chutney",
                "Gujarati", 50.00, "khamand.jpg", true, 15),

            createFoodItem("masala_dosa", "Masala Dosa",
                "Crispy rice crepe filled with spiced potato mixture",
                "South Indian", 80.00, "masala_dosa.jpg", true, 18),

            createFoodItem("paneer_paratha", "Paneer Paratha",
                "Flatbread stuffed with cottage cheese and spices",
                "Indian Breakfast", 90.00, "paneer_paratha.png", true, 18),

            createFoodItem("pav_bhaji", "Pav Bhaji",
                "Spicy mashed vegetable curry served with buttered bread rolls",
                "Maharashtrian", 100.00, "pav_bhaji.jpg", true, 20),

            createFoodItem("pizza", "Veg Pizza",
                "Classic margherita pizza with fresh vegetables and cheese",
                "Italian", 150.00, "pizza.jpg", true, 25),

            createFoodItem("pizza2", "Special Pizza",
                "Gourmet pizza with premium vegetables and extra cheese",
                "Italian", 200.00, "pizza2.jpg", true, 30),

            createFoodItem("rava_dosa", "Rava Dosa",
                "Crispy semolina crepe with onion and spices",
                "South Indian", 70.00, "rava_dosa.jpg", true, 15),

            createFoodItem("red_pasta", "Red Pasta",
                "Pasta in tangy tomato sauce with vegetables",
                "Italian", 120.00, "red_pasta.jpg", true, 20),

            createFoodItem("samosa", "Samosa",
                "Crispy triangular pastry filled with spiced potatoes and peas",
                "Snacks", 20.00, "samosa.jpg", true, 10),

            createFoodItem("vada_pav", "Vada Pav",
                "Spicy potato fritter sandwich in a bread bun",
                "Maharashtrian", 30.00, "vada_pav.jpg", true, 8),

            createFoodItem("veg_sandwich", "Veg Sandwich",
                "Fresh vegetables sandwich with chutney and cheese",
                "Snacks", 45.00, "veg_sandwich.jpg", true, 8),

            createFoodItem("white_pasta", "White Pasta",
                "Creamy pasta with white sauce and vegetables",
                "Italian", 130.00, "white_pasta.jpg", true, 20)
        );
    }

    /**
     * Create a food item map with common properties
     */
    private Map<String, Object> createFoodItem(String itemId, String name, String description,
                                             String category, double price, String imageUrl,
                                             boolean vegetarian, int preparationTime) {
        Map<String, Object> foodItem = new HashMap<>();

        // Basic info
        foodItem.put("itemId", itemId);
        foodItem.put("name", name);
        foodItem.put("description", description);
        foodItem.put("category", category);
        foodItem.put("price", price);
        foodItem.put("imageUrl", "https://picsum.photos/food_items/" + imageUrl);
        foodItem.put("vegetarian", vegetarian);
        foodItem.put("available", true);

        // Rating
        foodItem.put("rating", 4.0 + Math.random()); // Random rating between 4.0 and 5.0
        foodItem.put("ratingCount", 50 + (int)(Math.random() * 200)); // Random rating count

        // Time and preparation
        foodItem.put("preparationTime", preparationTime);
        foodItem.put("ingredients", Arrays.asList("Main Ingredient", "Spices", "Fresh Produce"));

        // Inventory
        Map<String, Object> inventory = new HashMap<>();
        inventory.put("stockQuantity", 30 + (int)(Math.random() * 70)); // Random stock between 30-100
        inventory.put("lowStockThreshold", 10);
        inventory.put("batchNumber", "B" + System.currentTimeMillis());
        inventory.put("expiryDate", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)); // 7 days from now
        foodItem.put("inventory", inventory);

        // Sales data
        Map<String, Object> salesData = new HashMap<>();
        salesData.put("totalSold", (int)(Math.random() * 500));
        salesData.put("revenue", price * (int)(Math.random() * 500));
        foodItem.put("salesData", salesData);

        // Timestamps
        foodItem.put("createdAt", new Date());
        foodItem.put("updatedAt", new Date());
        foodItem.put("isActive", true);

        return foodItem;
    }

    /**
     * Initialize food categories
     */
    private void initializeCategories() {
        List<Map<String, Object>> categories = Arrays.asList(
            createCategory("indian_breakfast", "Indian Breakfast",
                "Traditional Indian breakfast items", 1),
            createCategory("snacks", "Snacks",
                "Quick bites and evening snacks", 2),
            createCategory("north_indian", "North Indian",
                "Authentic North Indian cuisine", 3),
            createCategory("south_indian", "South Indian",
                "Delicious South Indian dishes", 4),
            createCategory("gujarati", "Gujarati",
                "Traditional Gujarati food items", 5),
            createCategory("maharashtrian", "Maharashtrian",
                "Authentic Maharashtrian cuisine", 6),
            createCategory("italian", "Italian",
                "Italian delicacies and pasta dishes", 7),
            createCategory("fast_food", "Fast Food",
                "Quick and delicious fast food items", 8)
        );

        WriteBatch batch = db.batch();

        for (Map<String, Object> category : categories) {
            String categoryId = (String) category.get("categoryId");
            batch.set(db.collection("categories").document(categoryId), category);
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Categories initialized successfully");
                // Initialize sample analytics data
                initializeAnalytics();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error initializing categories", e);
            });
    }

    /**
     * Create a category map
     */
    private Map<String, Object> createCategory(String categoryId, String name,
                                              String description, int displayOrder) {
        Map<String, Object> category = new HashMap<>();

        category.put("categoryId", categoryId);
        category.put("name", name);
        category.put("description", description);
        category.put("icon", "https://picsum.photos/icons/" + categoryId + ".png");
        category.put("isActive", true);
        category.put("displayOrder", displayOrder);
        category.put("itemCount", 2 + (int)(Math.random() * 5)); // Random item count

        return category;
    }

    /**
     * Initialize sample analytics data
     */
    private void initializeAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Today's metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", 50 + (int)(Math.random() * 100));
        metrics.put("totalRevenue", 5000.00 + (Math.random() * 10000));
        metrics.put("averageOrderValue", 100.00 + (Math.random() * 50));
        metrics.put("uniqueCustomers", 30 + (int)(Math.random() * 70));
        metrics.put("repeatCustomers", 10 + (int)(Math.random() * 40));
        analytics.put("metrics", metrics);

        analytics.put("date", new Date());
        analytics.put("createdAt", new Date());

        db.collection("analytics")
            .document(FirebaseUtils.formatDate(new Date()))
            .set(analytics)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Analytics initialized successfully");
                // Initialize inventory data
                initializeInventory();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error initializing analytics", e);
            });
    }

    /**
     * Initialize inventory data for food items
     */
    private void initializeInventory() {
        WriteBatch batch = db.batch();

        List<String> itemIds = Arrays.asList(
            "aloo_paratha", "bread_pakora", "burger", "cheese_sandwich", "chole_bhature",
            "chole_kulche", "idli_sambar", "khamand", "masala_dosa", "paneer_paratha",
            "pav_bhaji", "pizza", "pizza2", "rava_dosa", "red_pasta", "samosa",
            "vada_pav", "veg_sandwich", "white_pasta"
        );

        for (String itemId : itemIds) {
            Map<String, Object> inventory = new HashMap<>();
            inventory.put("foodItemId", itemId);
            inventory.put("currentStock", 30 + (int)(Math.random() * 70));
            inventory.put("lowStockThreshold", 10);
            inventory.put("maxStock", 100);
            inventory.put("reorderLevel", 15);
            inventory.put("batchNumber", "B" + System.currentTimeMillis());
            inventory.put("expiryDate", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
            inventory.put("lastRestocked", new Date());
            inventory.put("totalConsumed", (int)(Math.random() * 50));
            inventory.put("wastage", (int)(Math.random() * 5));
            inventory.put("updatedAt", new Date());

            batch.set(db.collection("inventory").document(itemId), inventory);
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Inventory initialized successfully");
                Log.d(TAG, "Database initialization completed successfully!");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error initializing inventory", e);
            });
    }

    /**
     * Check if data is already initialized
     */
    public void checkAndInitializeIfNeeded() {
        db.collection("foodItems")
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "Food items collection is empty, initializing...");
                    initializeFoodItems();
                } else {
                    Log.d(TAG, "Food items already exist, skipping initialization");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking food items collection", e);
            });
    }
}