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

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.nmims.canteen.R;
import com.nmims.canteen.models.InventoryItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for inventory management
 * Handles inventory item display with stock tracking and management controls
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private final Context context;
    private List<InventoryItem> inventoryItems;
    private final OnInventoryInteractionListener listener;
    private final DecimalFormat currencyFormatter;
    private final SimpleDateFormat dateFormat;

    /**
     * Interface for handling inventory interactions
     */
    public interface OnInventoryInteractionListener {
        void onStockUpdate(InventoryItem item, int quantity, String reason);
        void onViewHistory(InventoryItem item);
        void onReorderItem(InventoryItem item);
        void onEditItem(InventoryItem item);
        void onDeleteItem(InventoryItem item);
        void onQualityCheck(InventoryItem item);
        void onExportReport(InventoryItem item);
        void onItemSelect(InventoryItem item, boolean selected);
        void onBatchOperation(List<InventoryItem> selectedItems, String operation);
    }

    public InventoryAdapter(Context context, OnInventoryInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.inventoryItems = new ArrayList<>();
        this.currencyFormatter = new DecimalFormat("₹##,##0.00");
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_card, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    /**
     * Update inventory items list with DiffUtil for efficient updates
     */
    public void updateInventoryItems(List<InventoryItem> newInventoryItems) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new InventoryItemDiffCallback(this.inventoryItems, newInventoryItems));
        this.inventoryItems.clear();
        this.inventoryItems.addAll(newInventoryItems);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set inventory items list
     */
    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = new ArrayList<>(inventoryItems);
        notifyDataSetChanged();
    }

    /**
     * Get inventory item at position
     */
    public InventoryItem getInventoryItemAt(int position) {
        return inventoryItems.get(position);
    }

    /**
     * Get low stock items
     */
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            if (item.isLowStock()) {
                lowStockItems.add(item);
            }
        }
        return lowStockItems;
    }

    /**
     * Get expiring items
     */
    public List<InventoryItem> getExpiringItems() {
        List<InventoryItem> expiringItems = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            if (item.isExpiringSoon()) {
                expiringItems.add(item);
            }
        }
        return expiringItems;
    }

    /**
     * Get out of stock items
     */
    public List<InventoryItem> getOutOfStockItems() {
        List<InventoryItem> outOfStockItems = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            if (item.getCurrentStock() <= 0) {
                outOfStockItems.add(item);
            }
        }
        return outOfStockItems;
    }

    /**
     * Sort by stock level
     */
    public void sortByStockLevel() {
        List<InventoryItem> sorted = new ArrayList<>(inventoryItems);
        sorted.sort((item1, item2) -> Integer.compare(item1.getCurrentStock(), item2.getCurrentStock()));
        updateInventoryItems(sorted);
    }

    /**
     * Sort by total value
     */
    public void sortByTotalValue() {
        List<InventoryItem> sorted = new ArrayList<>(inventoryItems);
        sorted.sort((item1, item2) -> Double.compare(item2.getTotalValue(), item1.getTotalValue()));
        updateInventoryItems(sorted);
    }

    /**
     * Sort by waste percentage
     */
    public void sortByWastePercentage() {
        List<InventoryItem> sorted = new ArrayList<>(inventoryItems);
        sorted.sort((item1, item2) -> Double.compare(item2.getWastePercentage(), item1.getWastePercentage()));
        updateInventoryItems(sorted);
    }

    /**
     * Calculate total inventory value
     */
    public double getTotalInventoryValue() {
        double totalValue = 0;
        for (InventoryItem item : inventoryItems) {
            totalValue += item.getTotalValue();
        }
        return totalValue;
    }

    /**
     * Calculate total potential loss
     */
    public double getTotalPotentialLoss() {
        double totalLoss = 0;
        for (InventoryItem item : inventoryItems) {
            totalLoss += item.getPotentialLossValue();
        }
        return totalLoss;
    }

    /**
     * ViewHolder class for inventory items
     */
    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView itemNameTextView;
        private final TextView categoryTextView;
        private final TextView currentStockTextView;
        private final TextView reorderPointTextView;
        private final TextView unitCostTextView;
        private final TextView totalValueTextView;
        private final TextView lastUpdatedTextView;
        private final TextView supplierTextView;
        private final TextView expiryDateTextView;
        private final LinearProgressIndicator stockProgressIndicator;
        private final TextView stockPercentageTextView;
        private final ImageButton increaseStockButton;
        private final ImageButton decreaseStockButton;
        private final ImageButton historyButton;
        private final ImageButton reorderButton;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final ImageButton qualityButton;
        private final Chip lowStockChip;
        private final Chip outOfStockChip;
        private final Chip expiryChip;
        private final Chip qualityChip;
        private final View stockStatusIndicator;
        private final View selectionOverlay;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            currentStockTextView = itemView.findViewById(R.id.currentStockTextView);
            reorderPointTextView = itemView.findViewById(R.id.reorderPointTextView);
            unitCostTextView = itemView.findViewById(R.id.unitCostTextView);
            totalValueTextView = itemView.findViewById(R.id.totalValueTextView);
            lastUpdatedTextView = itemView.findViewById(R.id.lastUpdatedTextView);
            supplierTextView = itemView.findViewById(R.id.supplierTextView);
            expiryDateTextView = itemView.findViewById(R.id.expiryDateTextView);
            stockProgressIndicator = itemView.findViewById(R.id.stockProgressIndicator);
            stockPercentageTextView = itemView.findViewById(R.id.stockPercentageTextView);
            increaseStockButton = itemView.findViewById(R.id.increaseStockButton);
            decreaseStockButton = itemView.findViewById(R.id.decreaseStockButton);
            historyButton = itemView.findViewById(R.id.historyButton);
            reorderButton = itemView.findViewById(R.id.reorderButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            qualityButton = itemView.findViewById(R.id.qualityButton);
            lowStockChip = itemView.findViewById(R.id.lowStockChip);
            outOfStockChip = itemView.findViewById(R.id.outOfStockChip);
            expiryChip = itemView.findViewById(R.id.expiryChip);
            qualityChip = itemView.findViewById(R.id.qualityChip);
            stockStatusIndicator = itemView.findViewById(R.id.stockStatusIndicator);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }

        void bind(InventoryItem item) {
            // Set basic information
            itemNameTextView.setText(item.getItemName());
            categoryTextView.setText(item.getCategory());

            // Set stock information
            currentStockTextView.setText(String.valueOf(item.getCurrentStock()));
            reorderPointTextView.setText("Reorder at: " + item.getReorderPoint());

            // Set cost information
            unitCostTextView.setText(currencyFormatter.format(item.getUnitCost()));
            totalValueTextView.setText("Total: " + currencyFormatter.format(item.getTotalValue()));

            // Set supplier information
            if (item.getSupplierName() != null && !item.getSupplierName().isEmpty()) {
                supplierTextView.setText("📦 " + item.getSupplierName());
                supplierTextView.setVisibility(View.VISIBLE);
            } else {
                supplierTextView.setVisibility(View.GONE);
            }

            // Set expiry date
            if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
                expiryDateTextView.setText("📅 " + item.getExpiryDate());
                expiryDateTextView.setVisibility(View.VISIBLE);
            } else {
                expiryDateTextView.setVisibility(View.GONE);
            }

            // Set last updated time
            if (item.getLastMovementDate() != null) {
                lastUpdatedTextView.setText("Last updated: " + formatTimeAgo(item.getLastMovementDate()));
            }

            // Update stock progress indicator
            updateStockProgress(item);

            // Show/hide status chips
            updateStatusChips(item);

            // Set click listeners
            increaseStockButton.setOnClickListener(v -> {
                showStockUpdateDialog(item, true);
            });

            decreaseStockButton.setOnClickListener(v -> {
                showStockUpdateDialog(item, false);
            });

            historyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewHistory(item);
                }
            });

            reorderButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReorderItem(item);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItem(item);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(item);
                }
            });

            qualityButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQualityCheck(item);
                }
            });

            // Card click for details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItem(item);
                }
            });

            // Set card elevation based on stock status
            if (item.isOutOfStock()) {
                cardView.setCardElevation(2f);
            } else if (item.isLowStock()) {
                cardView.setCardElevation(6f);
            } else {
                cardView.setCardElevation(4f);
            }
        }

        private void updateStockProgress(InventoryItem item) {
            int maxStock = Math.max(item.getMaximumStock(), item.getCurrentStock() + item.getReorderQuantity());
            int progress = (int) ((double) item.getCurrentStock() / maxStock * 100);

            stockProgressIndicator.setProgress(progress);
            stockPercentageTextView.setText(progress + "%");

            // Set color based on stock level
            if (item.getCurrentStock() <= 0) {
                stockProgressIndicator.setIndicatorColor(context.getResources().getColor(android.R.color.holo_red_dark));
                stockStatusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (item.isLowStock()) {
                stockProgressIndicator.setIndicatorColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                stockStatusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                stockProgressIndicator.setIndicatorColor(context.getResources().getColor(android.R.color.holo_green_dark));
                stockStatusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
            }
        }

        private void updateStatusChips(InventoryItem item) {
            // Low stock chip
            if (item.isLowStock() && !item.isOutOfStock()) {
                lowStockChip.setVisibility(View.VISIBLE);
                lowStockChip.setText("⚠️ Low Stock");
            } else {
                lowStockChip.setVisibility(View.GONE);
            }

            // Out of stock chip
            if (item.isOutOfStock()) {
                outOfStockChip.setVisibility(View.VISIBLE);
                outOfStockChip.setText("❌ Out of Stock");
            } else {
                outOfStockChip.setVisibility(View.GONE);
            }

            // Expiry chip
            if (item.isExpiringSoon()) {
                expiryChip.setVisibility(View.VISIBLE);
                expiryChip.setText("⏰ Expiring Soon");
            } else {
                expiryChip.setVisibility(View.GONE);
            }

            // Quality chip
            if (item.getQualityStatus() != null && !"Good".equals(item.getQualityStatus())) {
                qualityChip.setVisibility(View.VISIBLE);
                qualityChip.setText("🔍 " + item.getQualityStatus());
            } else {
                qualityChip.setVisibility(View.GONE);
            }
        }

        private void showStockUpdateDialog(InventoryItem item, boolean isIncrease) {
            String[] options;
            String title;

            if (isIncrease) {
                title = "Add Stock - " + item.getItemName();
                options = new String[]{"+10", "+25", "+50", "+100", "+Reorder Qty", "Custom"};
            } else {
                title = "Remove Stock - " + item.getItemName();
                options = new String[]{"-1", "-5", "-10", "-25", "Custom"};
            }

            new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setItems(options, (dialog, which) -> {
                        int quantity = 0;
                        String reason = isIncrease ? "Stock addition" : "Stock removal";

                        switch (which) {
                            case 0: quantity = isIncrease ? 10 : -1; break;
                            case 1: quantity = isIncrease ? 25 : -5; break;
                            case 2: quantity = isIncrease ? 50 : -10; break;
                            case 3: quantity = isIncrease ? 100 : -25; break;
                            case 4:
                                if (isIncrease) {
                                    quantity = item.getReorderQuantity();
                                    reason = "Automatic reorder";
                                }
                                break;
                            case 5:
                                // Custom amount dialog would go here
                                quantity = isIncrease ? 50 : -5; // Default for demo
                                reason = "Manual adjustment";
                                break;
                        }

                        if (listener != null) {
                            listener.onStockUpdate(item, quantity, reason);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private String formatTimeAgo(String dateString) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(dateString);
                if (date != null) {
                    long diffInMillis = System.currentTimeMillis() - date.getTime();
                    long diffInSeconds = diffInMillis / 1000;
                    long diffInMinutes = diffInSeconds / 60;
                    long diffInHours = diffInMinutes / 60;
                    long diffInDays = diffInHours / 24;

                    if (diffInDays > 0) {
                        return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
                    } else if (diffInHours > 0) {
                        return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
                    } else if (diffInMinutes > 0) {
                        return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
                    } else {
                        return "Just now";
                    }
                }
            } catch (Exception e) {
                // Handle parsing error
            }
            return dateString;
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class InventoryItemDiffCallback extends DiffUtil.Callback {
        private final List<InventoryItem> oldList;
        private final List<InventoryItem> newList;

        public InventoryItemDiffCallback(List<InventoryItem> oldList, List<InventoryItem> newList) {
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
            InventoryItem oldItem = oldList.get(oldItemPosition);
            InventoryItem newItem = newList.get(newItemPosition);
            return oldItem.getInventoryId().equals(newItem.getInventoryId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            InventoryItem oldItem = oldList.get(oldItemPosition);
            InventoryItem newItem = newList.get(newItemPosition);

            return oldItem.getItemName().equals(newItem.getItemName()) &&
                   oldItem.getCurrentStock() == newItem.getCurrentStock() &&
                   oldItem.getUnitCost() == newItem.getUnitCost() &&
                   oldItem.getTotalValue() == newItem.getTotalValue() &&
                   (oldItem.getExpiryDate() != null ?
                    oldItem.getExpiryDate().equals(newItem.getExpiryDate()) :
                    newItem.getExpiryDate() == null);
        }
    }
}