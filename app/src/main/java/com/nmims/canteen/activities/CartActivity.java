package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nmims.canteen.R;
import com.nmims.canteen.adapters.CartAdapter;
import com.nmims.canteen.models.CartItem;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.utils.CartManager;
import com.nmims.canteen.utils.PaymentManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Shopping cart management activity
 * Handles cart item display, quantity adjustments, and checkout process
 */
public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";

    // UI Components
    private Toolbar toolbar;
    private RecyclerView cartRecyclerView;
    private TextView emptyCartTextView;
    private TextView continueShoppingTextView;
    private MaterialCardView summaryCard;
    private TextView itemCountTextView;
    private TextView subtotalTextView;
    private TextView discountTextView;
    private TextView deliveryChargesTextView;
    private TextView taxTextView;
    private TextView totalAmountTextView;
    private MaterialButton checkoutButton;
    private MaterialButton continueShoppingButton;
    private View emptyStateView;

    // Adapters and Services
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private PaymentManager paymentManager;

    // Data
    private ArrayList<CartItem> cartItems;
    private double subtotal;
    private double discountAmount;
    private double deliveryCharges;
    private double taxAmount;
    private double totalAmount;
    private final DecimalFormat currencyFormatter = new DecimalFormat("₹##,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize services
        cartManager = CartManager.getInstance(this);
        paymentManager = PaymentManager.getInstance();
        cartItems = new ArrayList<>();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        setupCartListener();

        // Load cart data
        loadCartData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        continueShoppingTextView = findViewById(R.id.continueShoppingTextView);
        summaryCard = findViewById(R.id.summaryCard);
        itemCountTextView = findViewById(R.id.itemCountTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        discountTextView = findViewById(R.id.discountTextView);
        deliveryChargesTextView = findViewById(R.id.deliveryChargesTextView);
        taxTextView = findViewById(R.id.taxTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);
        emptyStateView = findViewById(R.id.emptyStateView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Shopping Cart");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onQuantityChanged(CartItem cartItem, int newQuantity) {
                cartManager.updateItemQuantity(cartItem.getCartItemId(), newQuantity);
                updateSummary();
            }

            @Override
            public void onItemRemoved(CartItem cartItem) {
                showRemoveItemConfirmation(cartItem);
            }

            @Override
            public void onItemEdit(CartItem cartItem) {
                // Show item details or edit dialog
                showItemDetails(cartItem);
            }

            @Override
            public void onSpecialInstructionsChanged(CartItem cartItem, String instructions) {
                cartManager.updateItemInstructions(cartItem.getCartItemId(), instructions);
            }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupButtons() {
        checkoutButton.setOnClickListener(v -> proceedToCheckout());
        continueShoppingButton.setOnClickListener(v -> continueShopping());
        continueShoppingTextView.setOnClickListener(v -> continueShopping());
    }

    private void setupCartListener() {
        cartManager.addCartChangeListener(new CartManager.CartChangeListener() {
            @Override
            public void onCartChanged(ArrayList<CartItem> cartItems) {
                loadCartData();
            }

            @Override
            public void onItemAdded(CartItem item) {
                // Cart already updated in loadCartData
            }

            @Override
            public void onItemRemoved(CartItem item) {
                // Cart already updated in loadCartData
            }

            @Override
            public void onItemUpdated(CartItem item) {
                // Cart already updated in loadCartData
            }

            @Override
            public void onCartCleared() {
                loadCartData();
            }
        });
    }

    private void loadCartData() {
        cartItems = new ArrayList<>(cartManager.getCartItems());
        cartAdapter.setCartItems(cartItems);
        updateSummary();
        updateEmptyState();
    }

    private void updateSummary() {
        int itemCount = cartManager.getUniqueItemCount();
        subtotal = cartManager.getOriginalTotalPrice();
        discountAmount = cartManager.getTotalDiscount();
        deliveryCharges = calculateDeliveryCharges();
        taxAmount = calculateTax();
        totalAmount = subtotal - discountAmount + deliveryCharges + taxAmount;

        // Update UI
        itemCountTextView.setText(String.format("%d %s", itemCount, itemCount == 1 ? "Item" : "Items"));
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

        // Update checkout button state
        checkoutButton.setEnabled(!cartItems.isEmpty());
    }

    private void updateEmptyState() {
        boolean isEmpty = cartItems.isEmpty();

        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        summaryCard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        cartRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        continueShoppingButton.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private double calculateDeliveryCharges() {
        // Free delivery for orders above ₹299
        if (subtotal >= 299.0) {
            return 0.0;
        }
        return 40.0; // Standard delivery charge
    }

    private double calculateTax() {
        // Calculate 8% GST on subtotal (after discount)
        double taxableAmount = subtotal - discountAmount;
        return taxableAmount * 0.08;
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate cart items (check availability, prices, etc.)
        cartManager.validateCart(new CartManager.InventoryAnalysisCallback() {
            @Override
            public void onSuccess(java.util.Map<String, Object> analysis) {
                // Cart is valid, proceed to checkout
                navigateToPayment();
            }

            @Override
            public void onFailure(String error) {
                // Cart validation failed
                showCartValidationError(error);
            }
        });
    }

    private void navigateToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("subtotal", subtotal);
        intent.putExtra("discount", discountAmount);
        intent.putExtra("delivery_charges", deliveryCharges);
        intent.putExtra("tax", taxAmount);
        intent.putExtra("total_amount", totalAmount);
        startActivity(intent);
    }

    private void continueShopping() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showRemoveItemConfirmation(CartItem cartItem) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove " + cartItem.getFoodItemName() + " from your cart?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    cartManager.removeItem(cartItem.getCartItemId());
                    Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showItemDetails(CartItem cartItem) {
        // Show item details in a dialog
        StringBuilder details = new StringBuilder();
        details.append("Item: ").append(cartItem.getFoodItemName()).append("\n");
        details.append("Category: ").append(cartItem.getFoodItemCategory()).append("\n");
        details.append("Price: ").append(currencyFormatter.format(cartItem.getUnitPrice())).append("\n");
        details.append("Quantity: ").append(cartItem.getQuantity()).append("\n");
        details.append("Total: ").append(currencyFormatter.format(cartItem.getTotalPrice())).append("\n");

        if (cartItem.hasDiscount()) {
            details.append("Discount: ").append(currencyFormatter.format(cartItem.getSavings())).append("\n");
        }

        if (cartItem.getSpecialInstructions() != null && !cartItem.getSpecialInstructions().trim().isEmpty()) {
            details.append("Special Instructions: ").append(cartItem.getSpecialInstructions()).append("\n");
        }

        details.append("Preparation Time: ").append(cartItem.getTotalPreparationTime()).append(" minutes");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Item Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showCartValidationError(String error) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cart Validation Error")
                .setMessage(error)
                .setPositiveButton("OK", null)
                .show();
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_clear_cart) {
            clearCart();
            return true;
        } else if (itemId == R.id.action_select_all) {
            selectAllItems();
            return true;
        } else if (itemId == R.id.action_save_cart) {
            saveCartForLater();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCart() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to clear all items from your cart?")
                .setPositiveButton("Clear Cart", (dialog, which) -> {
                    cartManager.clearCart();
                    Toast.makeText(CartActivity.this, "Cart cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectAllItems() {
        // Implement select all items functionality
        cartAdapter.setEditable(true);
        Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show();
    }

    private void saveCartForLater() {
        // This would save the current cart state for later retrieval
        cartManager.forceSync();
        Toast.makeText(this, "Cart saved for later", Toast.LENGTH_SHORT).show();
    }

    // Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart data when activity resumes
        loadCartData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners
        cartManager.removeCartChangeListener(null);
    }

    @Override
    public void onBackPressed() {
        // Instead of going back to previous screen, go to main menu
        continueShopping();
    }
}