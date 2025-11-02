package com.nmims.canteen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nmims.canteen.R;
import com.nmims.canteen.models.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Admin RecyclerView adapter for order management
 * Handles order display with status updates and fulfillment tracking
 */
public class OrderManagementAdapter extends RecyclerView.Adapter<OrderManagementAdapter.OrderViewHolder> {
    private final Context context;
    private List<Order> orders;
    private final OnOrderInteractionListener listener;
    private final DecimalFormat currencyFormatter;
    private final SimpleDateFormat dateTimeFormat;

    /**
     * Interface for handling order interactions
     */
    public interface OnOrderInteractionListener {
        void onStatusUpdate(Order order, Order.OrderStatus newStatus);
        void onViewDetails(Order order);
        void onCustomerCall(Order order);
        void onPrintOrder(Order order);
        void onRefundOrder(Order order);
        void onBatchStatusUpdate(List<Order> orders, Order.OrderStatus status);
        void onOrderFilter(String filterType);
        void onOrderSearch(String query);
    }

    public OrderManagementAdapter(Context context, OnOrderInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.orders = new ArrayList<>();
        this.currencyFormatter = new DecimalFormat("₹##,##0.00");
        this.dateTimeFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_management_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    /**
     * Update orders list with DiffUtil for efficient updates
     */
    public void updateOrders(List<Order> newOrders) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new OrderDiffCallback(this.orders, newOrders));
        this.orders.clear();
        this.orders.addAll(newOrders);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set orders list
     */
    public void setOrders(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
        notifyDataSetChanged();
    }

    /**
     * Get order at position
     */
    public Order getOrderAt(int position) {
        return orders.get(position);
    }

    /**
     * Filter orders by status
     */
    public void filterByStatus(Order.OrderStatus status) {
        List<Order> filtered = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == status) {
                filtered.add(order);
            }
        }
        updateOrders(filtered);
    }

    /**
     * Filter orders by date range
     */
    public void filterByDateRange(Date startDate, Date endDate) {
        List<Order> filtered = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCreatedAt().after(startDate) && order.getCreatedAt().before(endDate)) {
                filtered.add(order);
            }
        }
        updateOrders(filtered);
    }

    /**
     * Sort orders by date
     */
    public void sortByDate(boolean ascending) {
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort((order1, order2) -> ascending ?
                order1.getCreatedAt().compareTo(order2.getCreatedAt()) :
                order2.getCreatedAt().compareTo(order1.getCreatedAt()));
        updateOrders(sorted);
    }

    /**
     * Sort orders by amount
     */
    public void sortByAmount(boolean descending) {
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort((order1, order2) -> descending ?
                Double.compare(order2.getFinalAmount(), order1.getFinalAmount()) :
                Double.compare(order1.getFinalAmount(), order2.getFinalAmount()));
        updateOrders(sorted);
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> filtered = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == status) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    /**
     * Get high priority orders
     */
    public List<Order> getHighPriorityOrders() {
        List<Order> highPriority = new ArrayList<>();
        for (Order order : orders) {
            if (order.isPriorityOrder() || order.getStatus() == Order.OrderStatus.PENDING) {
                highPriority.add(order);
            }
        }
        return highPriority;
    }

    /**
     * Get orders that need attention
     */
    public List<Order> getOrdersNeedingAttention() {
        List<Order> needsAttention = new ArrayList<>();
        for (Order order : orders) {
            // Orders pending for more than 30 minutes
            long pendingTime = System.currentTimeMillis() - order.getCreatedAt().getTime();
            if (order.getStatus() == Order.OrderStatus.PENDING && pendingTime > 30 * 60 * 1000) {
                needsAttention.add(order);
            }
        }
        return needsAttention;
    }

    /**
     * ViewHolder class for orders
     */
    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView orderIdTextView;
        private final TextView customerNameTextView;
        private final TextView customerPhoneTextView;
        private final TextView orderTimeTextView;
        private final TextView totalAmountTextView;
        private final TextView itemCountTextView;
        private final TextView preparationTimeTextView;
        private final TextView deliveryTimeTextView;
        private final Spinner statusSpinner;
        private final Chip statusChip;
        private final Chip priorityChip;
        private final Chip paymentMethodChip;
        private final ImageButton viewDetailsButton;
        private final ImageButton customerCallButton;
        private final ImageButton printButton;
        private final ImageButton refundButton;
        private final View statusIndicator;
        private final TextView elapsedTimeTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            customerPhoneTextView = itemView.findViewById(R.id.customerPhoneTextView);
            orderTimeTextView = itemView.findViewById(R.id.orderTimeTextView);
            totalAmountTextView = itemView.findViewById(R.id.totalAmountTextView);
            itemCountTextView = itemView.findViewById(R.id.itemCountTextView);
            preparationTimeTextView = itemView.findViewById(R.id.preparationTimeTextView);
            deliveryTimeTextView = itemView.findViewById(R.id.deliveryTimeTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            statusChip = itemView.findViewById(R.id.statusChip);
            priorityChip = itemView.findViewById(R.id.priorityChip);
            paymentMethodChip = itemView.findViewById(R.id.paymentMethodChip);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            customerCallButton = itemView.findViewById(R.id.customerCallButton);
            printButton = itemView.findViewById(R.id.printButton);
            refundButton = itemView.findViewById(R.id.refundButton);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            elapsedTimeTextView = itemView.findViewById(R.id.elapsedTimeTextView);
        }

        void bind(Order order) {
            // Set order information
            orderIdTextView.setText("Order #" + order.getOrderId());
            customerNameTextView.setText(order.getUserName());

            if (order.getUserPhone() != null && !order.getUserPhone().isEmpty()) {
                customerPhoneTextView.setText("📞 " + order.getUserPhone());
                customerPhoneTextView.setVisibility(View.VISIBLE);
                customerCallButton.setVisibility(View.VISIBLE);
            } else {
                customerPhoneTextView.setVisibility(View.GONE);
                customerCallButton.setVisibility(View.GONE);
            }

            orderTimeTextView.setText(dateTimeFormat.format(order.getCreatedAt()));
            totalAmountTextView.setText(currencyFormatter.format(order.getFinalAmount()));
            itemCountTextView.setText(order.getTotalItemCount() + " items");

            // Set preparation time
            preparationTimeTextView.setText("⏱ " + order.getTotalPreparationTime() + " min");

            // Set elapsed time
            long elapsedMillis = System.currentTimeMillis() - order.getCreatedAt().getTime();
            int elapsedMinutes = (int) (elapsedMillis / (60 * 1000));
            if (elapsedMinutes < 60) {
                elapsedTimeTextView.setText(elapsedMinutes + " min ago");
            } else {
                int elapsedHours = elapsedMinutes / 60;
                elapsedTimeTextView.setText(elapsedHours + "h " + (elapsedMinutes % 60) + "min ago");
            }

            // Set status chip and indicator
            updateStatusDisplay(order.getStatus());

            // Set payment method
            paymentMethodChip.setText(order.getPaymentMethod().getDisplayName());

            // Set priority chip
            if (order.isPriorityOrder()) {
                priorityChip.setVisibility(View.VISIBLE);
                priorityChip.setText("⭐ Priority");
            } else {
                priorityChip.setVisibility(View.GONE);
            }

            // Setup status spinner
            setupStatusSpinner(order);

            // Set click listeners
            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(order);
                }
            });

            customerCallButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCustomerCall(order);
                }
            });

            printButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPrintOrder(order);
                }
            });

            refundButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRefundOrder(order);
                }
            });

            // Card click for details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(order);
                }
            });

            // Set card elevation based on priority
            if (order.isPriorityOrder()) {
                cardView.setCardElevation(8f);
            } else {
                cardView.setCardElevation(4f);
            }
        }

        private void updateStatusDisplay(Order.OrderStatus status) {
            statusChip.setText(status.getDisplayName());

            switch (status) {
                case PENDING:
                    statusChip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case CONFIRMED:
                    statusChip.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                    break;
                case PREPARING:
                    statusChip.setChipBackgroundColorResource(android.R.color.holo_purple_light);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_purple_dark));
                    break;
                case READY:
                    statusChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case DELIVERED:
                    statusChip.setChipBackgroundColorResource(android.R.color.darker_gray);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    break;
                case CANCELLED:
                    statusChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    statusChip.setChipBackgroundColorResource(android.R.color.darker_gray);
                    statusIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    break;
            }
        }

        private void setupStatusSpinner(Order order) {
            Order.OrderStatus[] statuses = Order.OrderStatus.values();
            String[] statusNames = new String[statuses.length];
            for (int i = 0; i < statuses.length; i++) {
                statusNames[i] = statuses[i].getDisplayName();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, statusNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);

            // Set current selection
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i] == order.getStatus()) {
                    statusSpinner.setSelection(i);
                    break;
                }
            }

            // Handle status change
            statusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    Order.OrderStatus newStatus = statuses[position];
                    if (newStatus != order.getStatus()) {
                        if (listener != null) {
                            listener.onStatusUpdate(order, newStatus);
                        }
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class OrderDiffCallback extends DiffUtil.Callback {
        private final List<Order> oldList;
        private final List<Order> newList;

        public OrderDiffCallback(List<Order> oldList, List<Order> newList) {
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
            Order oldOrder = oldList.get(oldItemPosition);
            Order newOrder = newList.get(newItemPosition);
            return oldOrder.getOrderId().equals(newOrder.getOrderId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Order oldOrder = oldList.get(oldItemPosition);
            Order newOrder = newList.get(newItemPosition);

            return oldOrder.getStatus() == newOrder.getStatus() &&
                   oldOrder.getFinalAmount() == newOrder.getFinalAmount() &&
                   oldOrder.getTotalItemCount() == newOrder.getTotalItemCount() &&
                   oldOrder.isPriorityOrder() == newOrder.isPriorityOrder() &&
                   oldOrder.isPaymentCompleted() == newOrder.isPaymentCompleted();
        }
    }
}