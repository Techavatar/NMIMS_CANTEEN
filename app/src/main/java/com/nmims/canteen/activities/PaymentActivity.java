package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nmims.canteen.R;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.CartItem;
import com.nmims.canteen.models.PaymentManager;
import com.nmims.canteen.services.FirestoreService;
import com.nmims.canteen.utils.CartManager;
import com.nmims.canteen.utils.FirebaseUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Payment processing interface activity
 * Handles payment method selection and order processing
 */
public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";

    // UI Components
    private Toolbar toolbar;
    private CardView orderSummaryCard;
    private RecyclerView orderItemsRecyclerView;
    private TextView subtotalTextView;
    private TextView discountTextView;
    private TextView deliveryChargesTextView;
    private TextView taxTextView;
    private TextView totalAmountTextView;
    private RadioGroup paymentMethodGroup;
    private RadioButton cashRadioButton;
    private RadioButton creditCardRadioButton;
    private RadioButton debitCardRadioButton;
    private RadioButton upiRadioButton;
    private RadioButton walletRadioButton;
    private CardView paymentDetailsCard;
    private TextInputLayout cardNumberLayout;
    private TextInputLayout cardHolderLayout;
    private TextInputLayout expiryLayout;
    private TextInputLayout cvvLayout;
    private TextInputEditText cardNumberEditText;
    private TextInputEditText cardHolderEditText;
    private TextInputEditText expiryEditText;
    private TextInputEditText cvvEditText;
    private TextInputLayout upiIdLayout;
    private TextInputEditText upiIdEditText;
    private Button payButton;
    private LinearProgressIndicator progressIndicator;
    private TextView processingMessageTextView;
    private View paymentSuccessView;
    private View paymentErrorView;
    private TextView successMessageTextView;
    private TextView successOrderIdTextView;
    private TextView errorMessageTextView;
    private TextView errorRetryTextView;

    // Services
    private CartManager cartManager;
    private PaymentManager paymentManager;
    private FirestoreService firestoreService;

    // Data
    private ArrayList<CartItem> orderItems;
    private double subtotal;
    private double discountAmount;
    private double deliveryCharges;
    private double taxAmount;
    private double totalAmount;
    private Order.OrderStatus orderStatus;
    private final DecimalFormat currencyFormatter = new DecimalFormat("₹##,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize services
        cartManager = CartManager.getInstance(this);
        paymentManager = PaymentManager.getInstance();
        firestoreService = FirestoreService.getInstance();
        orderItems = new ArrayList<>();

        // Get payment details from intent
        getPaymentDetails();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupOrderSummary();
        setupPaymentMethods();
        setupButtons();

        // Load order items
        loadOrderItems();
    }

    private void getPaymentDetails() {
        Intent intent = getIntent();
        if (intent != null) {
            subtotal = intent.getDoubleExtra("subtotal", 0.0);
            discountAmount = intent.getDoubleExtra("discount", 0.0);
            deliveryCharges = intent.getDoubleExtra("delivery_charges", 0.0);
            taxAmount = intent.getDoubleExtra("tax", 0.0);
            totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        orderSummaryCard = findViewById(R.id.orderSummaryCard);
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        discountTextView = findViewById(R.id.discountTextView);
        deliveryChargesTextView = findViewById(R.id.deliveryChargesTextView);
        taxTextView = findViewById(R.id.taxTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        cashRadioButton = findViewById(R.id.cashRadioButton);
        creditCardRadioButton = findViewById(R.id.creditCardRadioButton);
        debitCardRadioButton = findViewById(R.id.debitCardRadioButton);
        upiRadioButton = findViewById(R.id.upiRadioButton);
        walletRadioButton = findViewById(R.id.walletRadioButton);
        paymentDetailsCard = findViewById(R.id.paymentDetailsCard);
        cardNumberLayout = findViewById(R.id.cardNumberLayout);
        cardHolderLayout = findViewById(R.id.cardHolderLayout);
        expiryLayout = findViewById(R.id.expiryLayout);
        cvvLayout = findViewById(R.id.cvvLayout);
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cardHolderEditText = findViewById(R.id.cardHolderEditText);
        expiryEditText = findViewById(R.id.expiryEditText);
        cvvEditText = findViewById(R.id.cvvEditText);
        upiIdLayout = findViewById(R.id.upiIdLayout);
        upiIdEditText = findViewById(R.id.upiIdEditText);
        payButton = findViewById(R.id.payButton);
        progressIndicator = findViewById(R.id.progressIndicator);
        processingMessageTextView = findViewById(R.id.processingMessageTextView);
        paymentSuccessView = findViewById(R.id.paymentSuccessView);
        paymentErrorView = findViewById(R.id.paymentErrorView);
        successMessageTextView = findViewById(R.id.successMessageTextView);
        successOrderIdTextView = findViewById(R.id.successOrderIdTextView);
        errorMessageTextView = findViewById(R.id.errorMessageTextView);
        errorRetryTextView = findViewById(R.id.errorRetryTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupOrderSummary() {
        // Set order summary values
        subtotalTextView.setText(currencyFormatter.format(subtotal));

        if (discountAmount > 0) {
            discountTextView.setText("-" + currencyFormatter.format(discountAmount));
            discountTextView.setVisibility(View.VISIBLE);
        } else {
            discountTextView.setVisibility(View.GONE);
        }

        deliveryChargesTextView.setText(currencyFormatter.format(deliveryCharges));
        taxTextView.setText(currencyFormatter.format(taxAmount));
        totalAmountTextView.setText(currencyFormatter.format(totalAmount));
    }

    private void setupPaymentMethods() {
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updatePaymentDetailsVisibility(checkedId);
        });

        // Set default selection to cash
        paymentMethodGroup.check(R.id.cashRadioButton);
    }

    private void updatePaymentDetailsVisibility(int checkedId) {
        // Hide all payment details first
        cardNumberLayout.setVisibility(View.GONE);
        cardHolderLayout.setVisibility(View.GONE);
        expiryLayout.setVisibility(View.GONE);
        cvvLayout.setVisibility(View.GONE);
        upiIdLayout.setVisibility(View.GONE);

        // Show relevant payment details
        if (checkedId == R.id.creditCardRadioButton || checkedId == R.id.debitCardRadioButton) {
            cardNumberLayout.setVisibility(View.VISIBLE);
            cardHolderLayout.setVisibility(View.VISIBLE);
            expiryLayout.setVisibility(View.VISIBLE);
            cvvLayout.setVisibility(View.VISIBLE);
            paymentDetailsCard.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.upiRadioButton) {
            upiIdLayout.setVisibility(View.VISIBLE);
            paymentDetailsCard.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.cashRadioButton || checkedId == R.id.walletRadioButton) {
            paymentDetailsCard.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        payButton.setOnClickListener(v -> attemptPayment());
        errorRetryTextView.setOnClickListener(v -> attemptPayment());
    }

    private void loadOrderItems() {
        orderItems = new ArrayList<>(cartManager.getCartItems());

        // Setup RecyclerView to display order items
        // Note: In a real implementation, you would create a simple adapter for this
        // For now, we'll just show the count
        TextView itemCountTextView = findViewById(R.id.itemCountTextView);
        itemCountTextView.setText(String.format("%d %s", orderItems.size(), orderItems.size() == 1 ? "Item" : "Items"));
    }

    private void attemptPayment() {
        if (!validatePaymentDetails()) {
            return;
        }

        // Create order
        Order order = createOrder();
        if (order == null) {
            showError("Failed to create order");
            return;
        }

        // Show processing state
        showProcessing(true);

        // Get payment method
        String paymentMethod = getSelectedPaymentMethod();
        order.setPaymentMethod(Order.PaymentMethod.valueOf(paymentMethod.toUpperCase().replace(" ", "_")));

        // Process payment
        paymentManager.processPayment(order, new PaymentManager.PaymentCallback() {
            @Override
            public void onPaymentStarted() {
                // Processing already shown
            }

            @Override
            public void onPaymentProgress(int progress) {
                runOnUiThread(() -> {
                    progressIndicator.setProgress(progress);
                    processingMessageTextView.setText("Processing payment... " + progress + "%");
                });
            }

            @Override
            public void onPaymentSuccess(PaymentManager.PaymentResult result) {
                runOnUiThread(() -> {
                    showPaymentSuccess(result, order);
                });
            }

            @Override
            public void onPaymentFailure(PaymentManager.PaymentResult result) {
                runOnUiThread(() -> {
                    showPaymentError(result);
                });
            }

            @Override
            public void onPaymentCancelled() {
                runOnUiThread(() -> {
                    showProcessing(false);
                    Toast.makeText(PaymentActivity.this, "Payment cancelled", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validatePaymentDetails() {
        int selectedPaymentId = paymentMethodGroup.getCheckedRadioButtonId();

        if (selectedPaymentId == R.id.creditCardRadioButton || selectedPaymentId == R.id.debitCardRadioButton) {
            return validateCardDetails();
        } else if (selectedPaymentId == R.id.upiRadioButton) {
            return validateUpiDetails();
        }

        return true; // Cash and wallet don't need validation
    }

    private boolean validateCardDetails() {
        String cardNumber = cardNumberEditText.getText().toString().trim();
        String cardHolder = cardHolderEditText.getText().toString().trim();
        String expiry = expiryEditText.getText().toString().trim();
        String cvv = cvvEditText.getText().toString().trim();

        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberLayout.setError("Card number is required");
            return false;
        } else if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            cardNumberLayout.setError("Invalid card number");
            return false;
        } else {
            cardNumberLayout.setError(null);
        }

        if (TextUtils.isEmpty(cardHolder)) {
            cardHolderLayout.setError("Card holder name is required");
            return false;
        } else {
            cardHolderLayout.setError(null);
        }

        if (TextUtils.isEmpty(expiry)) {
            expiryLayout.setError("Expiry date is required");
            return false;
        } else if (!expiry.matches("^(0[1-9]|1[0-2])\\/?([0-9]{2})$")) {
            expiryLayout.setError("Invalid expiry date (MM/YY)");
            return false;
        } else {
            expiryLayout.setError(null);
        }

        if (TextUtils.isEmpty(cvv)) {
            cvvLayout.setError("CVV is required");
            return false;
        } else if (cvv.length() < 3 || cvv.length() > 4) {
            cvvLayout.setError("Invalid CVV");
            return false;
        } else {
            cvvLayout.setError(null);
        }

        return true;
    }

    private boolean validateUpiDetails() {
        String upiId = upiIdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(upiId)) {
            upiIdLayout.setError("UPI ID is required");
            return false;
        } else if (!upiId.contains("@")) {
            upiIdLayout.setError("Invalid UPI ID");
            return false;
        } else {
            upiIdLayout.setError(null);
        }

        return true;
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.cashRadioButton) {
            return "Cash";
        } else if (selectedId == R.id.creditCardRadioButton) {
            return "Credit Card";
        } else if (selectedId == R.id.debitCardRadioButton) {
            return "Debit Card";
        } else if (selectedId == R.id.upiRadioButton) {
            return "UPI";
        } else if (selectedId == R.id.walletRadioButton) {
            return "Wallet";
        }
        return "Cash";
    }

    private Order createOrder() {
        if (orderItems.isEmpty()) {
            return null;
        }

        // Generate order ID
        String orderId = "ORD-" + System.currentTimeMillis();

        // Create order
        Order order = new Order(orderId, FirebaseUtils.getCurrentUserId(), orderItems);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryCharges(deliveryCharges);
        order.setTaxAmount(taxAmount);
        order.setFinalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(Order.PaymentMethod.CASH); // Default, will be updated
        order.setOrderSource("mobile_app");

        // Set delivery type (default to pickup)
        order.setDeliveryType(Order.DeliveryType.PICKUP);

        return order;
    }

    private void showProcessing(boolean show) {
        if (show) {
            progressIndicator.setVisibility(View.VISIBLE);
            processingMessageTextView.setVisibility(View.VISIBLE);
            processingMessageTextView.setText("Initializing payment...");
            payButton.setVisibility(View.GONE);
            paymentMethodGroup.setEnabled(false);
            paymentDetailsCard.setEnabled(false);
            orderSummaryCard.setEnabled(false);
        } else {
            progressIndicator.setVisibility(View.GONE);
            processingMessageTextView.setVisibility(View.GONE);
            payButton.setVisibility(View.VISIBLE);
            payButton.setEnabled(true);
            paymentMethodGroup.setEnabled(true);
            paymentDetailsCard.setEnabled(true);
            orderSummaryCard.setEnabled(true);
        }
    }

    private void showPaymentSuccess(PaymentManager.PaymentResult result, Order order) {
        showProcessing(false);

        // Update order with payment details
        order.setPaymentId(result.getTransactionId());
        order.setPaymentCompleted(true);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        // Save order to Firestore
        saveOrder(order);

        // Show success view
        paymentSuccessView.setVisibility(View.VISIBLE);
        successMessageTextView.setText("Payment successful!");
        successOrderIdTextView.setText("Order ID: " + order.getOrderId());

        // Clear cart
        cartManager.clearCart();

        // Set timer to navigate to order status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToOrderStatus(order.getOrderId());
        }, 3000);
    }

    private void showPaymentError(PaymentManager.PaymentResult result) {
        showProcessing(false);

        // Show error view
        paymentErrorView.setVisibility(View.VISIBLE);
        errorMessageTextView.setText(result.getMessage());
        errorRetryTextView.setVisibility(View.VISIBLE);
        payButton.setText("Retry Payment");
    }

    private void saveOrder(Order order) {
        firestoreService.createOrder(order, new FirestoreService.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String orderId) {
                // Order saved successfully
            }

            @Override
            public void onFailure(String error) {
                // Handle error - maybe show retry option
                Toast.makeText(PaymentActivity.this, "Failed to save order: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToOrderStatus(String orderId) {
        Intent intent = new Intent(this, OrderStatusActivity.class);
        intent.putExtra("order_id", orderId);
        startActivity(intent);
        finish();
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.payment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (paymentSuccessView.getVisibility() == View.VISIBLE) {
            // Payment successful, navigate to order status
            navigateToOrderStatus(successOrderIdTextView.getText().toString().replace("Order ID: ", ""));
        } else {
            super.onBackPressed();
        }
    }
}