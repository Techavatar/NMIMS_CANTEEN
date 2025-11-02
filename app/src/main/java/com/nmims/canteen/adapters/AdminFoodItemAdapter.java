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
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nmims.canteen.R;
import com.nmims.canteen.models.FoodItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin RecyclerView adapter for food item management
 * Handles food item display with admin controls for inventory and sales management
 */
public class AdminFoodItemAdapter extends RecyclerView.Adapter<AdminFoodItemAdapter.AdminFoodItemViewHolder> {
    private final Context context;
    private List<FoodItem> foodItems;
    private final OnAdminFoodItemInteractionListener listener;
    private final DecimalFormat currencyFormatter;
    private boolean isSelectionMode = false;
    private final List<String> selectedItems;

    /**
     * Interface for handling admin food item interactions
     */
    public interface OnAdminFoodItemInteractionListener {
        void onEditClick(FoodItem foodItem);
        void onDeleteClick(FoodItem foodItem);
        void onRestockClick(FoodItem foodItem);
        void onAvailabilityToggle(FoodItem foodItem, boolean isAvailable);
        void onStockUpdate(FoodItem foodItem, int newStock);
        void onItemSelected(FoodItem foodItem, boolean isSelected);
        void onBatchOperation(List<String> selectedItems, String operation);
        void onViewSalesClick(FoodItem foodItem);
        void onViewInventoryClick(FoodItem foodItem);
        void onDuplicateClick(FoodItem foodItem);
    }

    public AdminFoodItemAdapter(Context context, OnAdminFoodItemInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.foodItems = new ArrayList<>();
        this.currencyFormatter = new DecimalFormat("₹##,##0.00");
        this.selectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public AdminFoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_food_card, parent, false);
        return new AdminFoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodItemViewHolder holder, int position) {
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
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AdminFoodItemDiffCallback(this.foodItems, newFoodItems));
        this.foodItems.clear();
        this.foodItems.addAll(newFoodItems);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set food items list
     */
    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = new ArrayList<>(foodItems);
        notifyDataSetChanged();
    }

    /**
     * Get food item at position
     */
    public FoodItem getFoodItemAt(int position) {
        return foodItems.get(position);
    }

    /**
     * Enable/disable selection mode
     */
    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * Get selected items
     */
    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    /**
     * Select all items
     */
    public void selectAll() {
        selectedItems.clear();
        for (FoodItem item : foodItems) {
            selectedItems.add(item.getItemId());
        }
        notifyDataSetChanged();
    }

    /**
     * Clear selection
     */
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Get items with low stock
     */
    public List<FoodItem> getLowStockItems() {
        List<FoodItem> lowStockItems = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.isLowStock()) {
                lowStockItems.add(item);
            }
        }
        return lowStockItems;
    }

    /**
     * Get out of stock items
     */
    public List<FoodItem> getOutOfStockItems() {
        List<FoodItem> outOfStockItems = new ArrayList<>();
        for (FoodItem item : foodItems) {
            if (item.isOutOfStock()) {
                outOfStockItems.add(item);
            }
        }
        return outOfStockItems;
    }

    /**
     * Sort by stock level
     */
    public void sortByStockLevel() {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> Integer.compare(item1.getStockQuantity(), item2.getStockQuantity()));
        updateFoodItems(sorted);
    }

    /**
     * Sort by revenue generated
     */
    public void sortByRevenue() {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> Double.compare(item2.getRevenueGenerated(), item1.getRevenueGenerated()));
        updateFoodItems(sorted);
    }

    /**
     * Sort by items sold
     */
    public void sortByItemsSold() {
        List<FoodItem> sorted = new ArrayList<>(foodItems);
        sorted.sort((item1, item2) -> Integer.compare(item2.getTotalSold(), item1.getTotalSold()));
        updateFoodItems(sorted);
    }

    /**
     * ViewHolder class for admin food items
     */
    class AdminFoodItemViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView foodImageView;
        private final TextView nameTextView;
        private final TextView categoryTextView;
        private final TextView priceTextView;
        private final TextView stockTextView;
        private final TextView salesTextView;
        private final TextView revenueTextView;
        private final SwitchMaterial availabilitySwitch;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final ImageButton restockButton;
        private final ImageButton salesButton;
        private final ImageButton inventoryButton;
        private final ImageButton duplicateButton;
        private final Chip lowStockChip;
        private final Chip outOfStockChip;
        private final Chip featuredChip;
        private final Chip vegetarianChip;
        private final View selectionOverlay;
        private final View stockIndicator;

        public AdminFoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            stockTextView = itemView.findViewById(R.id.stockTextView);
            salesTextView = itemView.findViewById(R.id.salesTextView);
            revenueTextView = itemView.findViewById(R.id.revenueTextView);
            availabilitySwitch = itemView.findViewById(R.id.availabilitySwitch);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            restockButton = itemView.findViewById(R.id.restockButton);
            salesButton = itemView.findViewById(R.id.salesButton);
            inventoryButton = itemView.findViewById(R.id.inventoryButton);
            duplicateButton = itemView.findViewById(R.id.duplicateButton);
            lowStockChip = itemView.findViewById(R.id.lowStockChip);
            outOfStockChip = itemView.findViewById(R.id.outOfStockChip);
            featuredChip = itemView.findViewById(R.id.featuredChip);
            vegetarianChip = itemView.findViewById(R.id.vegetarianChip);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
            stockIndicator = itemView.findViewById(R.id.stockIndicator);
        }

        void bind(FoodItem foodItem) {
            // Set basic information
            nameTextView.setText(foodItem.getName());
            categoryTextView.setText(foodItem.getCategory());
            priceTextView.setText(currencyFormatter.format(foodItem.getPrice()));

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

            // Set stock information
            stockTextView.setText("Stock: " + foodItem.getStockQuantity());
            updateStockIndicator(foodItem.getStockQuantity(), foodItem.getLowStockThreshold());

            // Set sales information
            salesTextView.setText("Sold: " + foodItem.getTotalSold());
            revenueTextView.setText("Revenue: " + currencyFormatter.format(foodItem.getRevenueGenerated()));

            // Set availability switch
            availabilitySwitch.setChecked(foodItem.isAvailable());

            // Show/hide chips based on status
            if (foodItem.isLowStock() && !foodItem.isOutOfStock()) {
                lowStockChip.setVisibility(View.VISIBLE);
            } else {
                lowStockChip.setVisibility(View.GONE);
            }

            if (foodItem.isOutOfStock()) {
                outOfStockChip.setVisibility(View.VISIBLE);
            } else {
                outOfStockChip.setVisibility(View.GONE);
            }

            if (foodItem.isFeatured()) {
                featuredChip.setVisibility(View.VISIBLE);
            } else {
                featuredChip.setVisibility(View.GONE);
            }

            if (foodItem.isVegetarian()) {
                vegetarianChip.setText("🟢 Veg");
                vegetarianChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
            } else {
                vegetarianChip.setText("🔴 Non-Veg");
                vegetarianChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
            }

            // Set selection mode
            if (isSelectionMode) {
                selectionOverlay.setVisibility(View.VISIBLE);
                boolean isSelected = selectedItems.contains(foodItem.getItemId());
                selectionOverlay.setAlpha(isSelected ? 0.3f : 0.0f);
                selectionOverlay.setBackgroundColor(isSelected ?
                        context.getResources().getColor(android.R.color.holo_blue_light) :
                        context.getResources().getColor(android.R.color.transparent));
            } else {
                selectionOverlay.setVisibility(View.GONE);
            }

            // Set click listeners
            availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAvailabilityToggle(foodItem, isChecked);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(foodItem);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(foodItem);
                }
            });

            restockButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestockClick(foodItem);
                }
            });

            salesButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewSalesClick(foodItem);
                }
            });

            inventoryButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewInventoryClick(foodItem);
                }
            });

            duplicateButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDuplicateClick(foodItem);
                }
            });

            // Card click for selection mode
            itemView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    boolean isSelected = selectedItems.contains(foodItem.getItemId());
                    if (isSelected) {
                        selectedItems.remove(foodItem.getItemId());
                    } else {
                        selectedItems.add(foodItem.getItemId());
                    }
                    if (listener != null) {
                        listener.onItemSelected(foodItem, !isSelected);
                    }
                    notifyItemChanged(getAdapterPosition());
                } else {
                    // Show item details or edit
                    if (listener != null) {
                        listener.onEditClick(foodItem);
                    }
                }
            });

            // Long press for selection mode
            itemView.setOnLongClickListener(v -> {
                if (!isSelectionMode && listener != null) {
                    setSelectionMode(true);
                    selectedItems.add(foodItem.getItemId());
                    listener.onItemSelected(foodItem, true);
                    notifyItemChanged(getAdapterPosition());
                    return true;
                }
                return false;
            });

            // Quick stock update buttons
            stockTextView.setOnClickListener(v -> {
                if (listener != null) {
                    showStockUpdateDialog(foodItem);
                }
            });
        }

        private void updateStockIndicator(int currentStock, int lowStockThreshold) {
            if (currentStock <= 0) {
                stockIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (currentStock <= lowStockThreshold) {
                stockIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                stockIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
            }
        }

        private void showStockUpdateDialog(FoodItem foodItem) {
            String[] options = {"Add 10", "Add 25", "Add 50", "Add 100", "Custom amount"};

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Update Stock for " + foodItem.getName())
                    .setItems(options, (dialog, which) -> {
                        int quantity = 0;
                        switch (which) {
                            case 0: quantity = 10; break;
                            case 1: quantity = 25; break;
                            case 2: quantity = 50; break;
                            case 3: quantity = 100; break;
                            case 4:
                                // Custom amount dialog would go here
                                quantity = 50; // Default for demo
                                break;
                        }
                        if (listener != null) {
                            listener.onStockUpdate(foodItem, quantity);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class AdminFoodItemDiffCallback extends DiffUtil.Callback {
        private final List<FoodItem> oldList;
        private final List<FoodItem> newList;

        public AdminFoodItemDiffCallback(List<FoodItem> oldList, List<FoodItem> newList) {
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
                   oldItem.getStockQuantity() == newItem.getStockQuantity() &&
                   oldItem.isAvailable() == newItem.isAvailable() &&
                   oldItem.getTotalSold() == newItem.getTotalSold() &&
                   oldItem.getRevenueGenerated() == newItem.getRevenueGenerated() &&
                   oldItem.isFeatured() == newItem.isFeatured();
        }
    }
}