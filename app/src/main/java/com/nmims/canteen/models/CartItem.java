package com.nmims.canteen.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

/**
 * Shopping cart item model
 * Represents a food item added to cart with quantity and price calculations
 */
public class CartItem implements Parcelable {
    private FoodItem foodItem;
    private int quantity;
    private double totalPrice;
    private double unitPrice;
    private Date addedAt;
    private String specialInstructions;
    private double discountApplied;
    private String cartItemId;

    // Default constructor
    public CartItem() {
        this.addedAt = new Date();
        this.quantity = 1;
        this.discountApplied = 0.0;
    }

    // Parameterized constructor
    public CartItem(FoodItem foodItem, int quantity) {
        this();
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.unitPrice = foodItem.getDiscountedPrice();
        this.calculateTotalPrice();
        this.cartItemId = generateCartItemId();
    }

    // Constructor for Parcelable
    protected CartItem(Parcel in) {
        foodItem = in.readParcelable(FoodItem.class.getClassLoader());
        quantity = in.readInt();
        totalPrice = in.readDouble();
        unitPrice = in.readDouble();
        addedAt = new Date(in.readLong());
        specialInstructions = in.readString();
        discountApplied = in.readDouble();
        cartItemId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(foodItem, flags);
        dest.writeInt(quantity);
        dest.writeDouble(totalPrice);
        dest.writeDouble(unitPrice);
        dest.writeLong(addedAt.getTime());
        dest.writeString(specialInstructions);
        dest.writeDouble(discountApplied);
        dest.writeString(cartItemId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // Getters and Setters
    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        if (foodItem != null) {
            this.unitPrice = foodItem.getDiscountedPrice();
            calculateTotalPrice();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
            calculateTotalPrice();
        }
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
        calculateTotalPrice();
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    /**
     * Calculates total price based on unit price, quantity, and discount
     */
    private void calculateTotalPrice() {
        double subtotal = unitPrice * quantity;
        this.totalPrice = subtotal - discountApplied;
        // Ensure total price doesn't go below zero
        if (this.totalPrice < 0) {
            this.totalPrice = 0;
        }
    }

    /**
     * Updates the quantity and recalculates total price
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity > 0) {
            this.quantity = newQuantity;
            calculateTotalPrice();
        }
    }

    /**
     * Increments quantity by 1
     */
    public void incrementQuantity() {
        this.quantity++;
        calculateTotalPrice();
    }

    /**
     * Decrements quantity by 1, but doesn't go below 1
     */
    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            calculateTotalPrice();
        }
    }

    /**
     * Adds a discount amount to this cart item
     */
    public void applyDiscount(double discountAmount) {
        if (discountAmount > 0 && discountAmount <= (unitPrice * quantity)) {
            this.discountApplied = discountAmount;
            calculateTotalPrice();
        }
    }

    /**
     * Applies a percentage discount to this cart item
     */
    public void applyPercentageDiscount(double discountPercentage) {
        if (discountPercentage > 0 && discountPercentage <= 100) {
            double discountAmount = (unitPrice * quantity) * (discountPercentage / 100);
            this.discountApplied = discountAmount;
            calculateTotalPrice();
        }
    }

    /**
     * Removes any applied discount
     */
    public void removeDiscount() {
        this.discountApplied = 0.0;
        calculateTotalPrice();
    }

    /**
     * Gets the total discount applied
     */
    public double getTotalDiscount() {
        return discountApplied;
    }

    /**
     * Gets the price before discount
     */
    public double getOriginalTotalPrice() {
        return unitPrice * quantity;
    }

    /**
     * Gets the savings amount due to discount
     */
    public double getSavings() {
        return getOriginalTotalPrice() - totalPrice;
    }

    /**
     * Checks if this cart item has any discount applied
     */
    public boolean hasDiscount() {
        return discountApplied > 0;
    }

    /**
     * Gets the food item name
     */
    public String getFoodItemName() {
        return foodItem != null ? foodItem.getName() : "";
    }

    /**
     * Gets the food item category
     */
    public String getFoodItemCategory() {
        return foodItem != null ? foodItem.getCategory() : "";
    }

    /**
     * Checks if the food item is vegetarian
     */
    public boolean isVegetarian() {
        return foodItem != null && foodItem.isVegetarian();
    }

    /**
     * Gets the food item image URL
     */
    public String getFoodItemImageUrl() {
        return foodItem != null ? foodItem.getImageUrl() : "";
    }

    /**
     * Gets the preparation time for the food item
     */
    public int getPreparationTime() {
        return foodItem != null ? foodItem.getPreparationTime() : 0;
    }

    /**
     * Gets the total preparation time for this cart item quantity
     */
    public int getTotalPreparationTime() {
        int prepTime = getPreparationTime();
        // Preparation time typically doesn't scale linearly with quantity
        // Add a small increment for each additional item
        if (quantity > 1) {
            prepTime += (quantity - 1) * 2; // Add 2 minutes per additional item
        }
        return prepTime;
    }

    /**
     * Validates the cart item data
     */
    public boolean isValid() {
        return foodItem != null &&
               foodItem.isValid() &&
               quantity > 0 &&
               totalPrice >= 0 &&
               cartItemId != null && !cartItemId.isEmpty();
    }

    /**
     * Generates a unique cart item ID
     */
    private String generateCartItemId() {
        if (foodItem != null) {
            return "cart_" + foodItem.getItemId() + "_" + System.currentTimeMillis();
        }
        return "cart_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * Checks if this cart item is the same as another (based on food item ID)
     */
    public boolean isSameFoodItem(CartItem other) {
        if (other == null || other.getFoodItem() == null || this.getFoodItem() == null) {
            return false;
        }
        return this.getFoodItem().getItemId().equals(other.getFoodItem().getItemId());
    }

    /**
     * Merges this cart item with another (adds quantities)
     */
    public void mergeWith(CartItem other) {
        if (isSameFoodItem(other)) {
            this.quantity += other.getQuantity();
            this.discountApplied += other.getDiscountApplied();
            calculateTotalPrice();
        }
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "foodItem=" + (foodItem != null ? foodItem.getName() : "null") +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", addedAt=" + addedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return cartItemId != null && cartItemId.equals(cartItem.cartItemId);
    }

    @Override
    public int hashCode() {
        return cartItemId != null ? cartItemId.hashCode() : 0;
    }
}