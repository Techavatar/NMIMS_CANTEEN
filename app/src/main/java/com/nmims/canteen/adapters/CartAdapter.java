package com.nmims.canteen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nmims.canteen.R;
import com.nmims.canteen.models.CartItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for shopping cart items
 * Handles cart item display with quantity controls and price calculations
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final Context context;
    private List<CartItem> cartItems;
    private final OnCartItemClickListener listener;
    private final DecimalFormat currencyFormatter;
    private boolean isEditable = true;

    /**
     * Interface for handling cart item interactions
     */
    public interface OnCartItemClickListener {
        void onQuantityChanged(CartItem cartItem, int newQuantity);
        void onItemRemoved(CartItem cartItem);
        void onItemEdit(CartItem cartItem);
        void onSpecialInstructionsChanged(CartItem cartItem, String instructions);
    }

    public CartAdapter(Context context, OnCartItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.cartItems = new ArrayList<>();
        this.currencyFormatter = new DecimalFormat("₹##,##0.00");
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_card, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Update cart items list with DiffUtil for efficient updates
     */
    public void updateCartItems(List<CartItem> newCartItems) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CartItemDiffCallback(this.cartItems, newCartItems));
        this.cartItems.clear();
        this.cartItems.addAll(newCartItems);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set cart items list
     */
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = new ArrayList<>(cartItems);
        notifyDataSetChanged();
    }

    /**
     * Get cart item at position
     */
    public CartItem getCartItemAt(int position) {
        return cartItems.get(position);
    }

    /**
     * Set edit mode
     */
    public void setEditable(boolean editable) {
        this.isEditable = editable;
        notifyDataSetChanged();
    }

    /**
     * Get total cart value
     */
    public double getTotalCartValue() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    /**
     * Get total savings from discounts
     */
    public double getTotalSavings() {
        double totalSavings = 0;
        for (CartItem item : cartItems) {
            totalSavings += item.getSavings();
        }
        return totalSavings;
    }

    /**
     * Get total preparation time
     */
    public int getTotalPreparationTime() {
        int maxTime = 0;
        for (CartItem item : cartItems) {
            maxTime = Math.max(maxTime, item.getTotalPreparationTime());
        }
        return maxTime;
    }

    /**
     * ViewHolder class for cart items
     */
    class CartViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView foodImageView;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView originalPriceTextView;
        private final TextView totalPriceTextView;
        private final TextView savingsTextView;
        private final ImageButton decreaseButton;
        private final ImageButton increaseButton;
        private final TextView quantityTextView;
        private final MaterialButton removeButton;
        private final ImageButton editButton;
        private final TextInputEditText instructionsEditText;
        private final View instructionsLayout;
        private final TextView prepTimeTextView;
        private final TextView vegetarianIndicator;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            originalPriceTextView = itemView.findViewById(R.id.originalPriceTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            savingsTextView = itemView.findViewById(R.id.savingsTextView);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
            editButton = itemView.findViewById(R.id.editButton);
            instructionsEditText = itemView.findViewById(R.id.instructionsEditText);
            instructionsLayout = itemView.findViewById(R.id.instructionsLayout);
            prepTimeTextView = itemView.findViewById(R.id.prepTimeTextView);
            vegetarianIndicator = itemView.findViewById(R.id.vegetarianIndicator);
        }

        void bind(CartItem cartItem) {
            // Set food item information
            nameTextView.setText(cartItem.getFoodItemName());

            // Load image
            if (cartItem.getFoodItemImageUrl() != null && !cartItem.getFoodItemImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(cartItem.getFoodItemImageUrl())
                        .placeholder(R.drawable.placeholder_food)
                        .error(R.drawable.placeholder_food)
                        .centerCrop()
                        .into(foodImageView);
            } else {
                foodImageView.setImageResource(R.drawable.placeholder_food);
            }

            // Set unit price
            priceTextView.setText(currencyFormatter.format(cartItem.getUnitPrice()));

            // Handle discount display
            if (cartItem.hasDiscount()) {
                originalPriceTextView.setText(currencyFormatter.format(cartItem.getOriginalTotalPrice()));
                originalPriceTextView.setPaintFlags(originalPriceTextView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                originalPriceTextView.setVisibility(View.VISIBLE);
                savingsTextView.setText("Save: " + currencyFormatter.format(cartItem.getSavings()));
                savingsTextView.setVisibility(View.VISIBLE);
            } else {
                originalPriceTextView.setVisibility(View.GONE);
                savingsTextView.setVisibility(View.GONE);
            }

            // Set total price
            totalPriceTextView.setText(currencyFormatter.format(cartItem.getTotalPrice()));

            // Set quantity
            quantityTextView.setText(String.valueOf(cartItem.getQuantity()));

            // Set preparation time
            prepTimeTextView.setText("⏱ " + cartItem.getTotalPreparationTime() + " min");

            // Set vegetarian indicator
            if (cartItem.isVegetarian()) {
                vegetarianIndicator.setText("🟢");
                vegetarianIndicator.setContentDescription("Vegetarian");
            } else {
                vegetarianIndicator.setText("🔴");
                vegetarianIndicator.setContentDescription("Non-Vegetarian");
            }

            // Set special instructions
            if (cartItem.getSpecialInstructions() != null && !cartItem.getSpecialInstructions().trim().isEmpty()) {
                instructionsEditText.setText(cartItem.getSpecialInstructions());
                instructionsLayout.setVisibility(View.VISIBLE);
            } else {
                instructionsEditText.setText("");
                instructionsLayout.setVisibility(View.GONE);
            }

            // Enable/disable controls based on edit mode
            decreaseButton.setEnabled(isEditable);
            increaseButton.setEnabled(isEditable);
            removeButton.setEnabled(isEditable);
            editButton.setEnabled(isEditable);
            instructionsEditText.setEnabled(isEditable);

            // Set click listeners
            decreaseButton.setOnClickListener(v -> {
                if (isEditable && cartItem.getQuantity() > 1) {
                    int newQuantity = cartItem.getQuantity() - 1;
                    quantityTextView.setText(String.valueOf(newQuantity));
                    if (listener != null) {
                        listener.onQuantityChanged(cartItem, newQuantity);
                    }
                }
            });

            increaseButton.setOnClickListener(v -> {
                if (isEditable) {
                    int newQuantity = cartItem.getQuantity() + 1;
                    quantityTextView.setText(String.valueOf(newQuantity));
                    if (listener != null) {
                        listener.onQuantityChanged(cartItem, newQuantity);
                    }
                }
            });

            removeButton.setOnClickListener(v -> {
                if (isEditable && listener != null) {
                    listener.onItemRemoved(cartItem);
                }
            });

            editButton.setOnClickListener(v -> {
                if (isEditable) {
                    boolean isExpanded = instructionsLayout.getVisibility() == View.VISIBLE;
                    if (isExpanded) {
                        instructionsLayout.setVisibility(View.GONE);
                        editButton.setImageResource(R.drawable.ic_edit);

                        // Save instructions
                        String instructions = instructionsEditText.getText().toString().trim();
                        if (listener != null) {
                            listener.onSpecialInstructionsChanged(cartItem, instructions);
                        }
                    } else {
                        instructionsLayout.setVisibility(View.VISIBLE);
                        editButton.setImageResource(R.drawable.ic_save);
                        instructionsEditText.requestFocus();
                    }
                }
            });

            // Handle instructions text change
            instructionsEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && isEditable) {
                    String instructions = instructionsEditText.getText().toString().trim();
                    if (listener != null) {
                        listener.onSpecialInstructionsChanged(cartItem, instructions);
                    }
                }
            });

            // Card click for item details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemEdit(cartItem);
                }
            });
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class CartItemDiffCallback extends DiffUtil.Callback {
        private final List<CartItem> oldList;
        private final List<CartItem> newList;

        public CartItemDiffCallback(List<CartItem> oldList, List<CartItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CartItem oldItem = oldList.get(oldItemPosition);
            CartItem newItem = newList.get(newItemPosition);
            return oldItem.getCartItemId().equals(newItem.getCartItemId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CartItem oldItem = oldList.get(oldItemPosition);
            CartItem newItem = newList.get(newItemPosition);

            return oldItem.getQuantity() == newItem.getQuantity() &&
                   oldItem.getTotalPrice() == newItem.getTotalPrice() &&
                   oldItem.getUnitPrice() == newItem.getUnitPrice() &&
                   (oldItem.getSpecialInstructions() != null ?
                    oldItem.getSpecialInstructions().equals(newItem.getSpecialInstructions()) :
                    newItem.getSpecialInstructions() == null);
        }
    }
}