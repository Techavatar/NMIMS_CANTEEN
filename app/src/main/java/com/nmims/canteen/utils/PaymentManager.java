package com.nmims.canteen.utils;

import android.util.Log;

import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.CartItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Payment processing simulation utility
 * Handles payment operations, transaction management, and payment gateway simulation
 */
public class PaymentManager {
    private static final String TAG = "PaymentManager";

    // Payment gateway configuration
    private static final String GATEWAY_NAME = "PaySafe Gateway";
    private static final String MERCHANT_ID = "NMIMS_CANTEEN_MERCHANT_001";
    private static final double PROCESSING_FEE_PERCENTAGE = 0.02; // 2%
    private static final double PROCESSING_FEE_MIN = 1.0;
    private static final double PROCESSING_FEE_MAX = 10.0;

    // Transaction limits
    private static final double MIN_TRANSACTION_AMOUNT = 10.0;
    private static final double MAX_TRANSACTION_AMOUNT = 10000.0;

    // Payment processing simulation delays (in milliseconds)
    private static final int MIN_PROCESSING_TIME = 2000; // 2 seconds
    private static final int MAX_PROCESSING_TIME = 5000; // 5 seconds

    private static PaymentManager instance;

    // Private constructor for singleton pattern
    private PaymentManager() {
    }

    /**
     * Get singleton instance
     */
    public static synchronized PaymentManager getInstance() {
        if (instance == null) {
            instance = new PaymentManager();
        }
        return instance;
    }

    /**
     * Payment result data class
     */
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private String message;
        private String errorCode;
        private double amount;
        private double processingFee;
        private String paymentMethod;
        private Date processedAt;
        private String gatewayReference;
        private String authCode;
        private PaymentError error;

        public PaymentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.processedAt = new Date();
            this.transactionId = generateTransactionId();
        }

        public PaymentResult(boolean success, String message, String transactionId) {
            this(success, message);
            this.transactionId = transactionId;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public double getProcessingFee() { return processingFee; }
        public void setProcessingFee(double processingFee) { this.processingFee = processingFee; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public Date getProcessedAt() { return processedAt; }
        public void setProcessedAt(Date processedAt) { this.processedAt = processedAt; }

        public String getGatewayReference() { return gatewayReference; }
        public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }

        public String getAuthCode() { return authCode; }
        public void setAuthCode(String authCode) { this.authCode = authCode; }

        public PaymentError getError() { return error; }
        public void setError(PaymentError error) { this.error = error; }

        @Override
        public String toString() {
            return "PaymentResult{" +
                    "success=" + success +
                    ", transactionId='" + transactionId + '\'' +
                    ", message='" + message + '\'' +
                    ", amount=" + amount +
                    ", processedAt=" + processedAt +
                    '}';
        }
    }

    /**
     * Payment error enumeration
     */
    public enum PaymentError {
        INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Insufficient funds in account"),
        CARD_DECLINED("CARD_DECLINED", "Card was declined by bank"),
        INVALID_CARD("INVALID_CARD", "Invalid card details"),
        EXPIRED_CARD("EXPIRED_CARD", "Card has expired"),
        NETWORK_ERROR("NETWORK_ERROR", "Network connection error"),
        TIMEOUT("TIMEOUT", "Transaction timeout"),
        INVALID_AMOUNT("INVALID_AMOUNT", "Invalid transaction amount"),
        PROCESSOR_ERROR("PROCESSOR_ERROR", "Payment processor error"),
        FRAUD_DETECTED("FRAUD_DETECTED", "Transaction flagged as suspicious"),
        DAILY_LIMIT_EXCEEDED("DAILY_LIMIT_EXCEEDED", "Daily transaction limit exceeded"),
        TECHNICAL_ERROR("TECHNICAL_ERROR", "Technical processing error");

        private final String code;
        private final String message;

        PaymentError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    /**
     * Payment details class
     */
    public static class PaymentDetails {
        private String paymentMethod;
        private String cardNumber;
        private String cardHolderName;
        private String expiryDate;
        private String cvv;
        private String upiId;
        private String walletId;
        private double amount;
        private String currency;
        private String description;
        private String orderId;
        private String userId;
        private String customerEmail;
        private String customerPhone;
        private String billingAddress;
        private String ipAddress;
        private String deviceId;
        private boolean saveCardForFuture;
        private boolean isInternationalTransaction;

        public PaymentDetails(double amount, String paymentMethod, String orderId) {
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.orderId = orderId;
            this.currency = "INR";
            this.saveCardForFuture = false;
            this.isInternationalTransaction = false;
        }

        // Getters and Setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getCardHolderName() { return cardHolderName; }
        public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }

        public String getUpiId() { return upiId; }
        public void setUpiId(String upiId) { this.upiId = upiId; }

        public String getWalletId() { return walletId; }
        public void setWalletId(String walletId) { this.walletId = walletId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

        public String getBillingAddress() { return billingAddress; }
        public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public boolean isSaveCardForFuture() { return saveCardForFuture; }
        public void setSaveCardForFuture(boolean saveCardForFuture) { this.saveCardForFuture = saveCardForFuture; }

        public boolean isInternationalTransaction() { return isInternationalTransaction; }
        public void setInternationalTransaction(boolean internationalTransaction) { this.isInternationalTransaction = internationalTransaction; }
    }

    /**
     * Payment callback interface
     */
    public interface PaymentCallback {
        void onPaymentStarted();
        void onPaymentProgress(int progress);
        void onPaymentSuccess(PaymentResult result);
        void onPaymentFailure(PaymentResult result);
        void onPaymentCancelled();
    }

    // Main Payment Methods

    /**
     * Process payment for order
     */
    public void processPayment(Order order, PaymentCallback callback) {
        if (order == null) {
            PaymentResult result = new PaymentResult(false, "Invalid order details");
            result.setError(PaymentError.INVALID_AMOUNT);
            if (callback != null) callback.onPaymentFailure(result);
            return;
        }

        double amount = order.getFinalAmount();
        String paymentMethod = order.getPaymentMethod().getDisplayName();

        PaymentDetails details = new PaymentDetails(amount, paymentMethod, order.getOrderId());
        details.setUserId(order.getUserId());
        details.setDescription("Payment for order " + order.getOrderId());
        details.setOrderId(order.getOrderId());

        processPayment(details, callback);
    }

    /**
     * Process payment with details
     */
    public void processPayment(PaymentDetails details, PaymentCallback callback) {
        if (callback != null) {
            callback.onPaymentStarted();
        }

        // Validate payment details
        PaymentError validationError = validatePaymentDetails(details);
        if (validationError != null) {
            PaymentResult result = new PaymentResult(false, validationError.getMessage());
            result.setError(validationError);
            if (callback != null) callback.onPaymentFailure(result);
            return;
        }

        // Calculate processing fee
        double processingFee = calculateProcessingFee(details.getAmount());
        double totalAmount = details.getAmount() + processingFee;

        // Simulate payment processing in background thread
        new Thread(() -> {
            try {
                // Simulate processing time
                int processingTime = MIN_PROCESSING_TIME + new Random().nextInt(MAX_PROCESSING_TIME - MIN_PROCESSING_TIME);
                int progressSteps = 5;
                int stepDelay = processingTime / progressSteps;

                for (int i = 1; i <= progressSteps; i++) {
                    Thread.sleep(stepDelay);
                    final int progress = (i * 100) / progressSteps;
                    if (callback != null) {
                        // Run on main thread
                        callback.onPaymentProgress(progress);
                    }
                }

                // Simulate payment result (90% success rate for demo)
                boolean success = Math.random() < 0.9;
                PaymentResult result;

                if (success) {
                    result = createSuccessfulPaymentResult(details, totalAmount, processingFee);
                    Log.d(TAG, "Payment processed successfully: " + result.getTransactionId());
                    if (callback != null) callback.onPaymentSuccess(result);
                } else {
                    result = createFailedPaymentResult(details);
                    Log.w(TAG, "Payment processing failed: " + result.getMessage());
                    if (callback != null) callback.onPaymentFailure(result);
                }

            } catch (InterruptedException e) {
                PaymentResult result = new PaymentResult(false, "Payment processing was interrupted");
                result.setError(PaymentError.TIMEOUT);
                if (callback != null) callback.onPaymentFailure(result);
            } catch (Exception e) {
                PaymentResult result = new PaymentResult(false, "Unexpected error during payment processing");
                result.setError(PaymentError.TECHNICAL_ERROR);
                if (callback != null) callback.onPaymentFailure(result);
            }
        }).start();
    }

    /**
     * Process payment synchronously (for testing)
     */
    public PaymentResult processPaymentSync(double amount, String userId) {
        PaymentDetails details = new PaymentDetails(amount, "CASH", "TEST_ORDER_" + System.currentTimeMillis());
        details.setUserId(userId);
        details.setDescription("Test payment");

        PaymentError validationError = validatePaymentDetails(details);
        if (validationError != null) {
            PaymentResult result = new PaymentResult(false, validationError.getMessage());
            result.setError(validationError);
            return result;
        }

        // Simulate immediate processing
        double processingFee = calculateProcessingFee(details.getAmount());
        double totalAmount = details.getAmount() + processingFee;

        // Simulate 90% success rate
        if (Math.random() < 0.9) {
            return createSuccessfulPaymentResult(details, totalAmount, processingFee);
        } else {
            return createFailedPaymentResult(details);
        }
    }

    /**
     * Refund payment
     */
    public PaymentResult refundPayment(String transactionId, double amount, String reason) {
        try {
            // Simulate refund processing
            Thread.sleep(1000);

            String refundId = "REF_" + generateTransactionId();
            PaymentResult result = new PaymentResult(true, "Refund processed successfully");
            result.setTransactionId(refundId);
            result.setAmount(amount);
            result.setMessage("Refund of ₹" + String.format("%.2f", amount) + " processed successfully. Reason: " + reason);
            result.setGatewayReference("REF_GW_" + refundId);

            Log.d(TAG, "Refund processed: " + refundId);
            return result;

        } catch (Exception e) {
            PaymentResult result = new PaymentResult(false, "Refund processing failed: " + e.getMessage());
            result.setError(PaymentError.TECHNICAL_ERROR);
            return result;
        }
    }

    /**
     * Validate payment details
     */
    private PaymentError validatePaymentDetails(PaymentDetails details) {
        // Validate amount
        if (details.getAmount() < MIN_TRANSACTION_AMOUNT || details.getAmount() > MAX_TRANSACTION_AMOUNT) {
            return PaymentError.INVALID_AMOUNT;
        }

        // Validate payment method
        if (details.getPaymentMethod() == null || details.getPaymentMethod().trim().isEmpty()) {
            return PaymentError.TECHNICAL_ERROR;
        }

        // Validate card details for card payments
        if ("Credit Card".equals(details.getPaymentMethod()) || "Debit Card".equals(details.getPaymentMethod())) {
            if (details.getCardNumber() == null || details.getCardNumber().length() < 13) {
                return PaymentError.INVALID_CARD;
            }
            if (details.getExpiryDate() == null || !isValidExpiryDate(details.getExpiryDate())) {
                return PaymentError.EXPIRED_CARD;
            }
            if (details.getCvv() == null || details.getCvv().length() < 3) {
                return PaymentError.INVALID_CARD;
            }
        }

        // Validate UPI ID for UPI payments
        if ("UPI".equals(details.getPaymentMethod())) {
            if (details.getUpiId() == null || !isValidUpiId(details.getUpiId())) {
                return PaymentError.TECHNICAL_ERROR;
            }
        }

        return null; // No validation errors
    }

    /**
     * Calculate processing fee
     */
    private double calculateProcessingFee(double amount) {
        double fee = amount * PROCESSING_FEE_PERCENTAGE;
        // Ensure fee is within min/max limits
        fee = Math.max(PROCESSING_FEE_MIN, Math.min(PROCESSING_FEE_MAX, fee));
        return Math.round(fee * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Create successful payment result
     */
    private PaymentResult createSuccessfulPaymentResult(PaymentDetails details, double totalAmount, double processingFee) {
        PaymentResult result = new PaymentResult(true, "Payment processed successfully");
        result.setAmount(totalAmount);
        result.setProcessingFee(processingFee);
        result.setPaymentMethod(details.getPaymentMethod());
        result.setGatewayReference("GW_" + result.getTransactionId());
        result.setAuthCode(generateAuthCode());

        return result;
    }

    /**
     * Create failed payment result
     */
    private PaymentResult createFailedPaymentResult(PaymentDetails details) {
        // Randomly select a failure reason for simulation
        PaymentError[] errors = {
                PaymentError.INSUFFICIENT_FUNDS,
                PaymentError.CARD_DECLINED,
                PaymentError.NETWORK_ERROR,
                PaymentError.TIMEOUT,
                PaymentError.PROCESSOR_ERROR
        };

        PaymentError error = errors[new Random().nextInt(errors.length)];
        PaymentResult result = new PaymentResult(false, error.getMessage());
        result.setError(error);
        result.setAmount(details.getAmount());
        result.setPaymentMethod(details.getPaymentMethod());

        return result;
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "TXN" + timestamp + random;
    }

    /**
     * Generate authorization code
     */
    private String generateAuthCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Validate expiry date format (MM/YY)
     */
    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.length() != 5) {
            return false;
        }

        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) {
                return false;
            }

            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convert YY to YYYY

            if (month < 1 || month > 12) {
                return false;
            }

            // Check if date is in the future
            Date now = new Date();
            Date expiry = new Date(year - 1900, month - 1, 1); // Month is 0-based in Date constructor

            return expiry.after(now);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate UPI ID format
     */
    private boolean isValidUpiId(String upiId) {
        if (upiId == null) {
            return false;
        }

        // Basic UPI ID validation (username@bankname)
        return upiId.contains("@") && upiId.length() > 5;
    }

    /**
     * Get supported payment methods
     */
    public List<String> getSupportedPaymentMethods() {
        List<String> methods = new ArrayList<>();
        methods.add("Cash");
        methods.add("Credit Card");
        methods.add("Debit Card");
        methods.add("UPI");
        methods.add("Wallet");
        return methods;
    }

    /**
     * Get payment method display name
     */
    public String getPaymentMethodDisplayName(String method) {
        switch (method) {
            case "CASH": return "Cash on Delivery";
            case "CREDIT_CARD": return "Credit Card";
            case "DEBIT_CARD": return "Debit Card";
            case "UPI": return "UPI Payment";
            case "WALLET": return "Mobile Wallet";
            default: return method;
        }
    }

    /**
     * Format amount for display
     */
    public String formatAmount(double amount) {
        return String.format("₹%.2f", amount);
    }

    /**
     * Get payment gateway name
     */
    public String getGatewayName() {
        return GATEWAY_NAME;
    }

    /**
     * Get merchant ID
     */
    public String getMerchantId() {
        return MERCHANT_ID;
    }

    /**
     * Calculate total amount including processing fee
     */
    public double calculateTotalAmount(double amount) {
        return amount + calculateProcessingFee(amount);
    }

    /**
     * Get processing fee for amount
     */
    public double getProcessingFee(double amount) {
        return calculateProcessingFee(amount);
    }

    /**
     * Check if payment method supports processing fee
     */
    public boolean paymentMethodSupportsFee(String paymentMethod) {
        // Cash payments typically don't have processing fees
        return !"Cash".equals(paymentMethod);
    }

    /**
     * Get daily transaction limit
     */
    public double getDailyTransactionLimit() {
        return MAX_TRANSACTION_AMOUNT;
    }

    /**
     * Check if amount exceeds daily limit
     */
    public boolean exceedsDailyLimit(double amount) {
        return amount > MAX_TRANSACTION_AMOUNT;
    }
}