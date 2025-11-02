package com.nmims.canteen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.button.MaterialButton;
import com.nmims.canteen.R;
import com.nmims.canteen.models.FoodItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for food items display
 * Handles food item listing with search/filter functionality
 */
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> implements Filterable {
    private final Context context;
    private List<FoodItem> foodItems;
    private List<FoodItem> foodItemsFull; // For filtering
    private final OnFoodItemClickListener listener;
    private final DecimalFormat currencyFormatter;

    /**
     * Interface for handling food item clicks
     */
    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
        void onAddToCartClick(FoodItem foodItem);
        void onFavoriteClick(FoodItem foodItem);
    }

    public FoodItemAdapter(Context context, OnFoodItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.foodItems = new ArrayList<>();
        this.foodItemsFull = new ArrayList<>();
        this.currencyFormatter = new DecimalFormat("₹##,##0.00");
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_card, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    /**
     * Update food items list with DiffUtil for efficient updates
     */
    public void updateFoodItems(List<FoodItem> newFoodItems) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FoodItemDiffCallback(this.foodItems, newFoodItems));
        this.foodItems.clear();
        this.foodItems.addAll(newFoodItems);
        this.foodItemsFull.clear();
        this.foodItemsFull.addAll(newFoodItems);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set food items list
     */
    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = new ArrayList<>(foodItems);
        this.foodItemsFull = new ArrayList<>(foodItems);
        notifyDataSetChanged();
    }

    /**
     * Get food item at position
     */
    public FoodItem getFoodItemAt(int position) {
        return foodItems.get(position);
    }

    @Override
    public Filter getFilter() {
        return new FoodFilter();
    }

    /**
     * Filter by category
     */
    public void filterByCategory(String category) {
        if (category == null || category.equals("All")) {
            updateFoodItems(foodItemsFull);
        } else {
            List<FoodItem> filtered = new ArrayList<>();
            for (FoodItem item : foodItemsFull) {
                if (item.getCategory().equals(category)) {
                    filtered.add(item);
                }
            }
            updateFoodItems(filtered);
        }
    }

    /**
     * Filter vegetarian items
     */
    public void filterVegetarian(boolean showOnlyVegetarian) {
        if (!showOnlyVegetarian) {
            updateFoodItems(foodItemsFull);
        } else {
            List<FoodItem> filtered = new ArrayList<>();
            for (FoodItem item : foodItemsFull) {
                if (item.isVegetarian()) {
                    filtered.add(item);
                }
            }
            updateFoodItems(filtered);
        }
    }

    /**
     * Sort items by price
     */
    public void sortByPrice(boolean ascending) {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> ascending ?
                Double.compare(item1.getPrice(), item2.getPrice()) :
                Double.compare(item2.getPrice(), item1.getPrice()));
        updateFoodItems(sorted);
    }

    /**
     * Sort items by rating
     */
    public void sortByRating() {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> Double.compare(item2.getRating(), item1.getRating()));
        updateFoodItems(sorted);
    }

    /**
     * Sort items by popularity (items sold)
     */
    public void sortByPopularity() {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> Integer.compare(item2.getTotalSold(), item1.getTotalSold()));
        updateFoodItems(sorted);
    }

    /**
     * ViewHolder class for food items
     */
    class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView foodImageView;
        private final TextView nameTextView;
        private final TextView descriptionTextView;
        private final TextView priceTextView;
        private final TextView discountedPriceTextView;
        private final TextView ratingTextView;
        private final TextView prepTimeTextView;
        private final Chip vegetarianChip;
        private final Chip discountChip;
        private final Chip categoryChip;
        private final MaterialButton addToCartButton;
        private final ImageView favoriteImageView;
        private final View outOfStockOverlay;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            discountedPriceTextView = itemView.findViewById(R.id.discountedPriceTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            prepTimeTextView = itemView.findViewById(R.id.prepTimeTextView);
            vegetarianChip = itemView.findViewById(R.id.vegetarianChip);
            discountChip = itemView.findViewById(R.id.discountChip);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
            outOfStockOverlay = itemView.findViewById(R.id.outOfStockOverlay);
        }

        void bind(FoodItem foodItem) {
            // Set basic information
            nameTextView.setText(foodItem.getName());
            descriptionTextView.setText(foodItem.getDescription());
            categoryChip.setText(foodItem.getCategory());

            // Load image
            if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(foodItem.getImageUrl())
                        .placeholder(R.drawable.placeholder_food)
                        .error(R.drawable.placeholder_food)
                        .centerCrop()
                        .into(foodImageView);
            } else {
                foodImageView.setImageResource(R.drawable.placeholder_food);
            }

            // Set price
            if (foodItem.isDiscounted() && foodItem.getDiscountedPrice() < foodItem.getPrice()) {
                priceTextView.setText(currencyFormatter.format(foodItem.getPrice()));
                priceTextView.setPaintFlags(priceTextView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                discountedPriceTextView.setText(currencyFormatter.format(foodItem.getDiscountedPrice()));
                discountedPriceTextView.setVisibility(View.VISIBLE);
                discountChip.setVisibility(View.VISIBLE);
                discountChip.setText("-" + String.format("%.0f%%", foodItem.getDiscountPercentage()) + " OFF");
            } else {
                priceTextView.setText(currencyFormatter.format(foodItem.getPrice()));
                priceTextView.setPaintFlags(priceTextView.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                discountedPriceTextView.setVisibility(View.GONE);
                discountChip.setVisibility(View.GONE);
            }

            // Set rating
            if (foodItem.getRating() > 0) {
                ratingTextView.setText(String.format("★ %.1f (%d)", foodItem.getRating(), foodItem.getReviewCount()));
            } else {
                ratingTextView.setText("No ratings");
            }

            // Set preparation time
            prepTimeTextView.setText("⏱ " + foodItem.getPreparationTime() + " min");

            // Set vegetarian indicator
            if (foodItem.isVegetarian()) {
                vegetarianChip.setText("🟢 Veg");
                vegetarianChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
            } else {
                vegetarianChip.setText("🔴 Non-Veg");
                vegetarianChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
            }

            // Handle availability
            if (!foodItem.isAvailable() || foodItem.isOutOfStock()) {
                addToCartButton.setEnabled(false);
                addToCartButton.setText("Out of Stock");
                outOfStockOverlay.setVisibility(View.VISIBLE);
            } else {
                addToCartButton.setEnabled(true);
                addToCartButton.setText("Add to Cart");
                outOfStockOverlay.setVisibility(View.GONE);
            }

            // Featured indicator
            if (foodItem.isFeatured()) {
                cardView.setStrokeColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                cardView.setStrokeWidth(2);
            } else {
                cardView.setStrokeColor(context.getResources().getColor(android.R.color.transparent));
                cardView.setStrokeWidth(0);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodItemClick(foodItem);
                }
            });

            addToCartButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(foodItem);
                }
            });

            favoriteImageView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(foodItem);
                }
            });
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class FoodItemDiffCallback extends DiffUtil.Callback {
        private final List<FoodItem> oldList;
        private final List<FoodItem> newList;

        public FoodItemDiffCallback(List<FoodItem> oldList, List<FoodItem> newList) {
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
            FoodItem oldItem = oldList.get(oldItemPosition);
            FoodItem newItem = newList.get(newItemPosition);
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            FoodItem oldItem = oldList.get(oldItemPosition);
            FoodItem newItem = newList.get(newItemPosition);

            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   oldItem.getRating() == newItem.getRating() &&
                   oldItem.isAvailable() == newItem.isAvailable() &&
                   oldItem.isDiscounted() == newItem.isDiscounted() &&
                   oldItem.getDiscountedPrice() == newItem.getDiscountedPrice();
        }
    }

    /**
     * Custom filter for food items
     */
    private class FoodFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<FoodItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(foodItemsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (FoodItem item : foodItemsFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) ||
                        item.getDescription().toLowerCase().contains(filterPattern) ||
                        item.getCategory().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<FoodItem> filtered = (List<FoodItem>) results.values;
            updateFoodItems(filtered);
        }
    }
}