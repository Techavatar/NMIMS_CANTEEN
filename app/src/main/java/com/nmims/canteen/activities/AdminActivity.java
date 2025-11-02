package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.nmims.canteen.R;
import com.nmims.canteen.adapters.AdminFoodItemAdapter;
import com.nmims.canteen.adapters.InventoryAdapter;
import com.nmims.canteen.adapters.OrderManagementAdapter;
import com.nmims.canteen.fragments.DashboardFragment;
import com.nmims.canteen.fragments.SalesAnalyticsFragment;
import com.nims.canteen.fragments.InventoryManagementFragment;
import com.nmims.canteen.fragments.OrderManagementFragment;
import com.nmims.canteen.fragments.ReviewManagementFragment;
import com.nmims.canteen.fragments.UserManagementFragment;
import com.nmims.canteen.models.FoodItem;
import com.nmims.canteen.models.InventoryItem;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.AdminNotification;
import com.nmims.canteen.models.SalesData;
import com.nmims.canteen.services.FirebaseAuthService;
import com.nmims.canteen.services.FirestoreService;
import com.nmims.canteen.utils.AnalyticsManager;
import com.nmims.canteen.utils.InventoryManager;
import com.nmims.canteen.utils.NotificationManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Comprehensive admin panel with sales analytics and inventory management
 * Handles all admin functionality with real-time monitoring and data visualization
 */
public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";

    // UI Components
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private NavigationRailView navigationRail;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Dashboard components
    private CardView salesSummaryCard;
    private MaterialTextView todaySalesTextView;
    private MaterialTextView todayOrdersTextView;
    private MaterialTextView activeUsersTextView;
    private MaterialTextView lowStockItemsTextView;
    private MaterialTextView pendingReviewsTextView;

    // Real-time indicators
    private TextView newOrdersBadge;
    private TextView lowStockBadge;
    private TextView notificationsBadge;

    // Services
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;
    private AnalyticsManager analyticsManager;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;

    // Adapters and Data
    private AdminPagerAdapter pagerAdapter;
    private ArrayList<AdminNotification> notifications;

    // Real-time data
    private double todaySales;
    private int todayOrders;
    private int activeUsers;
    private int lowStockItems;
    private int pendingReviews;

    // Auto-refresh
    private Timer autoRefreshTimer;
    private final DecimalFormat currencyFormatter = new DecimalFormat("₹##,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize services
        initializeServices();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupViewPager();
        setupNavigationRail();
        setupTabLayout();
        setupFab();
        setupSwipeRefresh();

        // Load initial data
        loadDashboardData();

        // Setup real-time monitoring
        startRealTimeMonitoring();

        // Start auto-refresh
        startAutoRefresh();
    }

    private void initializeServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
        analyticsManager = AnalyticsManager.getInstance();
        inventoryManager = InventoryManager.getInstance();
        notificationManager = NotificationManager.getInstance();
        notifications = new ArrayList<>();
    }

    private void initializeViews() {
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        navigationRail = findViewById(R.id.navigationRail);
        fab = findViewById(R.id.fab);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Dashboard summary cards
        salesSummaryCard = findViewById(R.id.salesSummaryCard);
        todaySalesTextView = findViewById(R.id.todaySalesTextView);
        todayOrdersTextView = findViewById(R.id.todayOrdersTextView);
        activeUsersTextView = findViewById(R.id.activeUsersTextView);
        lowStockItemsTextView = findViewById(R.id.lowStockItemsTextView);
        pendingReviewsTextView = findViewById(R.id.pendingReviewsTextView);

        // Badge indicators
        newOrdersBadge = findViewById(R.id.newOrdersBadge);
        lowStockBadge = findViewById(R.id.lowStockBadge);
        notificationsBadge = findViewById(R.id.notificationsBadge);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
            getSupportActionBar().setHomeAsUpIndicator(false); // Admin has no back navigation
        }
    }

    private void setupViewPager() {
        pagerAdapter = new AdminPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, false, (tab, position) -> {
            viewPager.setCurrentItem(position, true);
        }).attach();
    }

    private void setupNavigationRail() {
        navigationRail.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                viewPager.setCurrentItem(0, true);
            } else if (itemId == R.id.nav_analytics) {
                viewPager.setCurrentItem(1, true);
            } else if (itemId == R.id.nav_inventory) {
                viewPager.setCurrentItem(2, true);
            } else if (itemId == R.id.nav_orders) {
                viewPager.setCurrentItem(3, true);
            } else if (itemId == R.id.nav_reviews) {
                viewPager.setCurrentItem(4, true);
            } else if (itemId == R.id.nav_users) {
                viewPager.setCurrentItem(5, true);
            } else if (itemId == R.id.nav_reports) {
                    viewPager.setCurrentItem(6, true);
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(7, true);
            }
            return true;
        });
    }

    private void setupTabLayout() {
        // TabLayout is already connected with ViewPager2 in setupViewPager
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            showQuickActions();
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDashboardData();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 2000);
        });
    }

    private void loadDashboardData() {
        // Load today's analytics
        analyticsManager.calculateDailySales(new Date(), new AnalyticsManager.AnalyticsCallback<SalesData>() {
            @Override
            public void onSuccess(SalesData salesData) {
                todaySales = salesData.getTotalRevenue();
                todayOrders = salesData.getTotalOrders();
                activeUsers = salesData.getUniqueCustomers();
                updateDashboardSummary();
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to load analytics data: " + error);
            }
        });

        // Load inventory data
        inventoryManager.checkLowStock(new InventoryManager.InventoryAnalysisCallback() {
            @Override
            public void onSuccess(java.util.Map<String, Object> analysis) {
                @SuppressWarnings("unchecked")
                List<InventoryItem> lowStockItems = (List<InventoryItem>) analysis.get("lowStockItems");
                AdminActivity.this.lowStockItems = lowStockItems != null ? lowStockItems.size() : 0;
                updateDashboardSummary();
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to load inventory data: " + error);
            }
        });

        // Load pending reviews
        loadPendingReviewsCount();

        // Load new orders count
        loadNewOrdersCount();
    }

    private void loadPendingReviewsCount() {
        // In a real implementation, you would query Firestore for unapproved reviews
        pendingReviews = 0;
        updateDashboardSummary();
    }

    private void loadNewOrdersCount() {
        // In a real implementation, you would query Firestore for pending orders
        firestoreService.getOrdersByStatus(Order.OrderStatus.PENDING, new FirestoreService.DatabaseCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                // This would be handled by real-time listeners
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });
    }

    private void updateDashboardSummary() {
        runOnUiThread(() -> {
            todaySalesTextView.setText(currencyFormatter.format(todaySales));
            todayOrdersTextView.setText(String.valueOf(todayOrders));
            activeUsersTextView.setText(String.valueOf(activeUsers));
            lowStockItemsTextView.setText(String.valueOf(lowStockItems));
            pendingReviewsTextView.setText(String.valueOf(pendingReviews));

            // Update badges
            updateBadges();
        });
    }

    private void updateBadges() {
        // Update notification badges
        int totalNotifications = lowStockItems + pendingReviews;
        notificationsBadge.setText(String.valueOf(totalNotifications));
        notificationsBadge.setVisibility(totalNotifications > 0 ? View.VISIBLE : View.GONE);

        // Update low stock badge
        lowStockBadge.setText(String.valueOf(lowStockItems));
        lowStockBadge.setVisibility(lowStockItems > 0 ? View.VISIBLE : View.GONE);

        // Update new orders badge
        newOrdersBadge.setText(String.valueOf(0)); // Would be updated by real-time listener
        newOrdersBadge.setVisibility(View.GONE);
    }

    private void startRealTimeMonitoring() {
        // Start inventory manager real-time monitoring
        inventoryManager.startRealTimeMonitoring();

        // Start notification manager real-time monitoring
        notificationManager.startRealTimeMonitoring();

        // Setup Firestore listeners for real-time data
        setupFirestoreListeners();
    }

    private void setupFirestoreListeners() {
        // Listen to new orders
        firestoreService.listenToOrdersUpdates(FirebaseUtils.getCurrentUserId(), new FirestoreService.RealtimeDataCallback<List<Order>>() {
            @Override
            public void onDataChanged(List<Order> orders) {
                int pendingCount = 0;
                for (Order order : orders) {
                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        pendingCount++;
                    }
                }
                // Update UI
                if (pagerAdapter != null) {
                    // Notify the appropriate fragment
                    // This would need proper fragment communication
                }
            }

            @Override
            public void onError(String error) {
                showError("Error listening to orders: " + error);
            }
        });

        // Listen to low stock items
        inventoryManager.checkLowStock(new InventoryManager.InventoryAnalysisCallback() {
            @Override
            public void onSuccess(java.util.Map<String, Object> analysis) {
                @SuppressWarnings("unchecked")
                List<InventoryItem> lowStockItems = (List<InventoryItem>) analysis.get("lowStockItems");
                lowStockItems = lowStockItems != null ? lowStockItems.size() : 0;
                updateDashboardSummary();

                // Create and send notification if needed
                if (lowStockItems > 0 && !lowStockItems.isEmpty()) {
                    List<String> itemIds = new ArrayList<>();
                    for (InventoryItem item : lowStockItems) {
                        itemIds.add(item.getItemId());
                    }
                    notificationManager.sendLowStockAlert(itemIds);
                }
            }

            @Override
            public void onFailure(String error) {
                showError("Error checking inventory: " + error);
            }
        });
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer();
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    loadDashboardData();
                });
            }
        }, 60000, 60000); // Refresh every 60 seconds
    }

    private void showQuickActions() {
        String[] actions = {
                "Add Food Item",
                "Process Orders",
                "Generate Report",
                "View Analytics",
                "Manage Inventory",
                "Review Moderation",
                "User Management"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Quick Actions")
                .setItems(actions, (dialog, which) -> {
                    String action = actions[which];
                    handleQuickAction(action);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleQuickAction(String action) {
        switch (action) {
            case "Add Food Item":
                navigateToFoodItemManagement();
                break;
            case "Process Orders":
                navigateToOrderManagement();
                break;
            case "Generate Report":
                generateSalesReport();
                break;
            case "View Analytics":
                navigateToAnalytics();
                break;
            case "Manage Inventory":
                navigateToInventoryManagement();
                break;
            case "Review Moderation":
                navigateToReviewManagement();
                break;
            case "User Management":
                navigateToUserManagement();
                break;
        }
    }

    // Navigation methods
    private void navigateToFoodItemManagement() {
        Intent intent = new Intent(this, FoodItemManagementActivity.class);
        startActivity(intent);
    }

    private void navigateToOrderManagement() {
        Intent intent = new Intent(this, OrderManagementActivity.class);
        startActivity(intent);
    }

    private void navigateToAnalytics() {
        Intent intent = new Intent(this, SalesAnalyticsActivity.class);
        startActivity(intent);
    }

    private void navigateToInventoryManagement() {
        Intent intent = new Intent(this, InventoryManagementActivity.class);
        startActivity(intent);
    }

    private void navigateToReviewManagement() {
        Intent intent = new Intent(this, ReviewManagementActivity.class);
        startActivity(intent);
    }

    private void navigateToUserManagement() {
        Intent intent = new Intent(this, UserManagementActivity.class);
        startActivity(intent);
    }

    private void generateSalesReport() {
        // Generate sales report for today
        analyticsManager.generateSalesReport(new Date(), new Date(), new AnalyticsManager.AnalyticsCallback<java.util.Map<String, Object>>() {
            @Override
            public void onSuccess(java.util.Map<String, Object> report) {
                showReport(report);
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to generate report: " + error);
            }
        });
    }

    private void showReport(java.util.Map<String, Object> report) {
        StringBuilder reportText = new StringBuilder();
        reportText.append("SALES REPORT - ").append(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date())).append("\n\n");
        reportText.append("Total Revenue: ").append(currencyFormatter.format((Double) report.get("totalRevenue"))).append("\n");
        reportText.append("Total Orders: ").append(report.get("totalOrders")).append("\n");
        reportText.append("Active Customers: ").append(report.get("activeCustomers")).append("\n");
        reportText.append("Average Order Value: ").append(currencyFormatter.format((Double) report.get("averageOrderValue"))).append("\n\n");
        reportText.append("Top Categories:\n");

        // Add category breakdown
        if (report.containsKey("categoryRevenue")) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Double> categoryRevenue = (java.util.Map<String, Double>) report.get("categoryRevenue");
            for (java.util.Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                reportText.append("  ").append(entry.getKey()).append(": ").append(currencyFormatter.format(entry.getValue())).append("\n");
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Sales Report")
                .setMessage(reportText.toString())
                .setPositiveButton("Export", (dialog, which) -> {
                    exportReport(report);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void exportReport(java.util.Map<String, Object> report) {
        analyticsManager.exportToCSV(report, new AnalyticsManager.AnalyticsCallback<String>() {
            @Override
            public void onSuccess(String csvData) {
                // Save to file or share
                Toast.makeText(AdminActivity.this, "Report exported successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to export report: " + error);
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Pager adapter for admin tabs
    private static class AdminPagerAdapter extends FragmentPagerAdapter {
        private final String[] TAB_TITLES = {
                "Dashboard",
                "Analytics",
                "Inventory",
                "Orders",
                "Reviews",
                "Users",
                "Reports",
                "Settings"
        };

        public AdminPagerAdapter(@NonNull androidx.fragment.app.FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new DashboardFragment();
                case 1:
                    return new SalesAnalyticsFragment();
                case 2:
                    return new InventoryManagementFragment();
                case 3:
                    return new OrderManagementFragment();
                case 4:
                    return new ReviewManagementFragment();
                case 5:
                    return new UserManagementFragment();
                case 6:
                    return new ReportsFragment();
                case 7:
                    return new SettingsFragment();
                default:
                    return new DashboardFragment();
            }
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITITLES[position];
        }
    }

    // Lifecycle methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
        }

        // Stop real-time monitoring
        inventoryManager.stopRealTimeMonitoring();
        notificationManager.stopRealTimeMonitoring();
        firestoreService.removeAllListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sync) {
            loadDashboardData();
            return true;
        } else if (itemId == R.id.action_export) {
            generateSalesReport();
            return true;
        } else if (itemId == R.id.action_notifications) {
            showNotifications();
            return true;
        } else if (itemId == R.id.action_settings) {
            navigateToSettings();
            return true;
        } else if (itemId == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNotifications() {
        if (notifications.isEmpty()) {
            showError("No notifications");
            return;
        }

        StringBuilder notificationList = new StringBuilder();
        for (AdminNotification notification : notifications) {
            notificationList.append("• ").append(notification.getTitle()).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Notifications")
                .setMessage(notificationList.toString())
                .setPositiveButton("Clear All", (dialog, which) -> {
                    // Clear all notifications
                    notifications.clear();
                    updateBadges();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void navigateToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from the admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        authService.signOut();
        notificationManager.removeAllListeners();
        inventoryManager.shutdown();
        analyticsManager.shutdown();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}