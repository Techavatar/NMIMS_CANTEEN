package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Order data model
 * Represents a customer order with items, status, and tracking information
 */
public class Order implements Serializable {
    public enum OrderStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        PREPARING("Preparing"),
        READY("Ready"),
        OUT_FOR_DELIVERY("Out for Delivery"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static OrderStatus fromString(String status) {
            try {
                return OrderStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                return PENDING;
            }
        }
    }

    public enum PaymentMethod {
        CASH("Cash"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        UPI("UPI"),
        WALLET("Wallet");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryType {
        PICKUP("Pickup"),
        DELIVERY("Delivery");

        private final String displayName;

        DeliveryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Order details
    private String orderId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private List<CartItem> items;
    private double totalAmount;
    private double discountAmount;
    private double deliveryCharges;
    private double taxAmount;
    private double finalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private DeliveryType deliveryType;
    private String paymentId;
    private boolean paymentCompleted;
    private Date paymentCompletedAt;

    // Timestamps
    private Date createdAt;
    private Date confirmedAt;
    private Date preparingAt;
    private Date readyAt;
    private Date deliveredAt;
    private Date cancelledAt;
    private Date estimatedDeliveryTime;

    // Delivery information
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPostalCode;
    private String deliveryInstructions;
    private String deliveryPersonName;
    private String deliveryPersonPhone;

    // Order processing
    private String specialInstructions;
    private String cancellationReason;
    private String orderNotes;
    private int preparationTimeMinutes;
    private String trackingNumber;
    private boolean isPriorityOrder;
    private String promoCodeApplied;
    private double promoDiscountAmount;

    // Admin and system fields
    private String processedBy;
    private String lastUpdatedBy;
    private Date lastUpdatedAt;
    private String orderSource; // "mobile_app", "web", "admin"

    // Default constructor for Firebase
    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.paymentMethod = PaymentMethod.CASH;
        this.deliveryType = DeliveryType.PICKUP;
        this.deliveryCharges = 0.0;
        this.taxAmount = 0.0;
        this.discountAmount = 0.0;
        this.paymentCompleted = false;
        this.isPriorityOrder = false;
        this.createdAt = new Date();
        this.lastUpdatedAt = new Date();
        this.orderSource = "mobile_app";
        this.preparationTimeMinutes = 0;
    }

    // Parameterized constructor
    public Order(String orderId, String userId, List<CartItem> items) {
        this();
        this.orderId = orderId;
        this.userId = userId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        calculateTotals();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        calculateTotals();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        calculateFinalAmount();
    }

    public double getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(double deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
        calculateFinalAmount();
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
        calculateFinalAmount();
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        updateStatusTimestamp();
        this.lastUpdatedAt = new Date();
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
        // Update delivery charges based on delivery type
        if (deliveryType == DeliveryType.DELIVERY) {
            this.deliveryCharges = 40.0; // Default delivery charge
        } else {
            this.deliveryCharges = 0.0;
        }
        calculateFinalAmount();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public void setPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
        if (paymentCompleted && paymentCompletedAt == null) {
            this.paymentCompletedAt = new Date();
        }
        this.lastUpdatedAt = new Date();
    }

    public Date getPaymentCompletedAt() {
        return paymentCompletedAt;
    }

    public void setPaymentCompletedAt(Date paymentCompletedAt) {
        this.paymentCompletedAt = paymentCompletedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Date getPreparingAt() {
        return preparingAt;
    }

    public void setPreparingAt(Date preparingAt) {
        this.preparingAt = preparingAt;
    }

    public Date getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Date readyAt) {
        this.readyAt = readyAt;
    }

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Date getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Date estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public String getDeliveryPersonName() {
        return deliveryPersonName;
    }

    public void setDeliveryPersonName(String deliveryPersonName) {
        this.deliveryPersonName = deliveryPersonName;
    }

    public String getDeliveryPersonPhone() {
        return deliveryPersonPhone;
    }

    public void setDeliveryPersonPhone(String deliveryPersonPhone) {
        this.deliveryPersonPhone = deliveryPersonPhone;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public int getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public void setPreparationTimeMinutes(int preparationTimeMinutes) {
        this.preparationTimeMinutes = preparationTimeMinutes;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public boolean isPriorityOrder() {
        return isPriorityOrder;
    }

    public void setPriorityOrder(boolean priorityOrder) {
        isPriorityOrder = priorityOrder;
    }

    public String getPromoCodeApplied() {
        return promoCodeApplied;
    }

    public void setPromoCodeApplied(String promoCodeApplied) {
        this.promoCodeApplied = promoCodeApplied;
    }

    public double getPromoDiscountAmount() {
        return promoDiscountAmount;
    }

    public void setPromoDiscountAmount(double promoDiscountAmount) {
        this.promoDiscountAmount = promoDiscountAmount;
        this.discountAmount += promoDiscountAmount;
        calculateFinalAmount();
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    /**
     * Calculates total amount from cart items
     */
    public void calculateTotals() {
        this.totalAmount = 0;
        this.preparationTimeMinutes = 0;

        for (CartItem item : items) {
            this.totalAmount += item.getTotalPrice();
            this.preparationTimeMinutes = Math.max(this.preparationTimeMinutes, item.getTotalPreparationTime());
        }

        calculateFinalAmount();
    }

    /**
     * Calculates final amount including tax and delivery charges
     */
    private void calculateFinalAmount() {
        // Apply tax (8% GST)
        this.taxAmount = this.totalAmount * 0.08;

        // Calculate final amount
        this.finalAmount = this.totalAmount - this.discountAmount + this.taxAmount + this.deliveryCharges;

        // Ensure final amount is not negative
        if (this.finalAmount < 0) {
            this.finalAmount = 0;
        }
    }

    /**
     * Updates timestamp based on status change
     */
    private void updateStatusTimestamp() {
        Date now = new Date();
        switch (status) {
            case CONFIRMED:
                if (confirmedAt == null) confirmedAt = now;
                break;
            case PREPARING:
                if (preparingAt == null) preparingAt = now;
                break;
            case READY:
                if (readyAt == null) readyAt = now;
                break;
            case DELIVERED:
                if (deliveredAt == null) deliveredAt = now;
                break;
            case CANCELLED:
                if (cancelledAt == null) cancelledAt = now;
                break;
        }
    }

    /**
     * Gets the total number of items in the order
     */
    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * Checks if the order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Checks if the order is completed
     */
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }

    /**
     * Checks if the order is cancelled
     */
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED;
    }

    /**
     * Gets order preparation progress percentage
     */
    public int getProgressPercentage() {
        switch (status) {
            case PENDING: return 0;
            case CONFIRMED: return 25;
            case PREPARING: return 50;
            case READY: return 75;
            case DELIVERED: return 100;
            case CANCELLED:
            case REFUNDED: return 0;
            default: return 0;
        }
    }

    /**
     * Gets the full delivery address
     */
    public String getFullDeliveryAddress() {
        StringBuilder address = new StringBuilder();
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            address.append(deliveryAddress);
        }
        if (deliveryCity != null && !deliveryCity.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(deliveryCity);
        }
        if (deliveryPostalCode != null && !deliveryPostalCode.isEmpty()) {
            if (address.length() > 0) address.append(" - ");
            address.append(deliveryPostalCode);
        }
        return address.toString();
    }

    /**
     * Gets order summary string
     */
    public String getOrderSummary() {
        return getTotalItemCount() + " items • ₹" + String.format("%.2f", finalAmount);
    }

    /**
     * Generates a tracking number if not already set
     */
    public void generateTrackingNumber() {
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            this.trackingNumber = "ORD" + System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", finalAmount=" + finalAmount +
                ", itemCount=" + getTotalItemCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId != null && orderId.equals(order.orderId);
    }

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }
}