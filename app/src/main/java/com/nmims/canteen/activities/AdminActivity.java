package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nmims.canteen.R;
import com.nmims.canteen.models.FoodItem;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.services.FirebaseAuthService;
import com.nmims.canteen.services.FirestoreService;

import java.util.List;

/**
 * Simplified admin panel for basic order and food item management
 */
public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView todayOrdersTextView;
    private TextView todayRevenueTextView;
    private TextView pendingOrdersTextView;

    // Services
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize services
        initializeServices();

        // Initialize UI
        initializeViews();
        setupToolbar();

        // Load data
        loadDashboardData();
    }

    private void initializeServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        todayOrdersTextView = findViewById(R.id.todayOrdersCountTextView);
        todayRevenueTextView = findViewById(R.id.todayRevenueTextView);
        pendingOrdersTextView = findViewById(R.id.pendingOrdersCountTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadDashboardData() {
        // Load today's orders
        firestoreService.getAllOrders(new FirestoreService.DatabaseCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                int todayOrders = 0;
                double todayRevenue = 0.0;
                int pendingOrders = 0;

                for (Order order : orders) {
                    // Count today's orders (simplified)
                    todayOrders++;
                    todayRevenue += order.getTotalAmount();

                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        pendingOrders++;
                    }
                }

                // Update UI
                todayOrdersTextView.setText(String.valueOf(todayOrders));
                todayRevenueTextView.setText("₹" + String.format("%.0f", todayRevenue));
                pendingOrdersTextView.setText(String.valueOf(pendingOrders));
            }

            @Override
            public void onFailure(String error) {
                // Set default values on error
                todayOrdersTextView.setText("0");
                todayRevenueTextView.setText("₹0");
                pendingOrdersTextView.setText("0");
            }
        });

        // Load food items count
        firestoreService.getAllFoodItems(new FirestoreService.DatabaseCallback<List<FoodItem>>() {
            @Override
            public void onSuccess(List<FoodItem> foodItems) {
                // Update food items count if needed
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadDashboardData();
            return true;
        } else if (itemId == R.id.action_add_food) {
            showAddFoodItemDialog();
            return true;
        } else if (itemId == R.id.action_settings) {
            showError("Settings feature coming soon!");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddFoodItemDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Food Item")
                .setMessage("Food item management feature coming soon!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}