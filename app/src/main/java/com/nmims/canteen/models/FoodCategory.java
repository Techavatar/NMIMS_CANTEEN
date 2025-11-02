package com.nmims.canteen.models;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing food categories for NMIMS Canteen
 * Organizes the food items provided by the user into logical categories
 */
public enum FoodCategory {
    INDIAN_BREAKFAST("Indian Breakfast", "Traditional Indian breakfast items",
        Arrays.asList("aloo_paratha", "paneer_paratha")),

    SNACKS("Snacks", "Quick bites and evening snacks",
        Arrays.asList("bread_pakora", "cheese_sandwich", "samosa", "veg_sandwich")),

    NORTH_INDIAN("North Indian", "Authentic North Indian cuisine",
        Arrays.asList("chole_bhature", "chole_kulche")),

    SOUTH_INDIAN("South Indian", "Delicious South Indian dishes",
        Arrays.asList("idli_sambar", "masala_dosa", "rava_dosa")),

    GUJARATI("Gujarati", "Traditional Gujarati food items",
        Arrays.asList("khamand")),

    MAHARASHTRIAN("Maharashtrian", "Authentic Maharashtrian cuisine",
        Arrays.asList("pav_bhaji", "vada_pav")),

    ITALIAN("Italian", "Italian delicacies and pasta dishes",
        Arrays.asList("pizza", "pizza2", "red_pasta", "white_pasta")),

    FAST_FOOD("Fast Food", "Quick and delicious fast food items",
        Arrays.asList("burger")),

    ALL("All", "All food items",
        Arrays.asList(
            "aloo_paratha", "bread_pakora", "burger", "cheese_sandwich", "chole_bhature",
            "chole_kulche", "idli_sambar", "khamand", "masala_dosa", "paneer_paratha",
            "pav_bhaji", "pizza", "pizza2", "rava_dosa", "red_pasta", "samosa",
            "vada_pav", "veg_sandwich", "white_pasta"
        ));

    private final String displayName;
    private final String description;
    private final List<String> foodItemIds;

    FoodCategory(String displayName, String description, List<String> foodItemIds) {
        this.displayName = displayName;
        this.description = description;
        this.foodItemIds = foodItemIds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getFoodItemIds() {
        return foodItemIds;
    }

    /**
     * Get category by food item ID
     */
    public static FoodCategory getCategoryByFoodItemId(String foodItemId) {
        for (FoodCategory category : values()) {
            if (category != ALL && category.getFoodItemIds().contains(foodItemId)) {
                return category;
            }
        }
        return SNACKS; // Default category
    }

    /**
     * Get all categories except ALL
     */
    public static FoodCategory[] getFilterableCategories() {
        FoodCategory[] allCategories = values();
        FoodCategory[] filterableCategories = new FoodCategory[allCategories.length - 1];
        System.arraycopy(allCategories, 0, filterableCategories, 0, allCategories.length - 1);
        return filterableCategories;
    }

    /**
     * Get category by display name
     */
    public static FoodCategory getByDisplayName(String displayName) {
        for (FoodCategory category : values()) {
            if (category.getDisplayName().equals(displayName)) {
                return category;
            }
        }
        return ALL;
    }
}