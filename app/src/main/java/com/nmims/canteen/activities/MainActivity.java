package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nmims.canteen.R;
import com.nmims.canteen.adapters.FoodItemAdapter;
import com.nmims.canteen.models.FoodItem;
import com.nmims.canteen.models.User;
import com.nmims.canteen.services.FirebaseAuthService;
import com.nmims.canteen.services.FirestoreService;
import com.nmims.canteen.utils.CartManager;
import com.nmims.canteen.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Main app interface with food menu
 * Handles food menu display with search, filtering, and navigation
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // UI Components
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private RecyclerView foodItemsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private FloatingActionButton cartFab;
    private TextView cartBadgeTextView;
    private ImageView cartIconImageView;
    private TextView headerTitleTextView;
    private TextView headerEmailTextView;

    // Adapters and Services
    private FoodItemAdapter foodItemAdapter;
    private CartManager cartManager;
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    // Data
    private List<FoodItem> foodItems;
    private User currentUser;
    private boolean isAdmin = false;

    // Navigation
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize services
        initializeServices();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupNavigation();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Load data
        loadUserData();
        loadFoodItems();

        // Setup cart manager listener
        setupCartListener();

        // Check for deep links or notifications
        handleIntent();
    }

    private void initializeServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
        cartManager = CartManager.getInstance(this);
        foodItems = new ArrayList<>();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        foodItemsRecyclerView = findViewById(R.id.foodItemsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navigationView = findViewById(R.id.navigationView);
        cartFab = findViewById(R.id.cartFab);

        // Navigation header views
        View headerView = navigationView.getHeaderView(0);
        headerTitleTextView = headerView.findViewById(R.id.headerTitleTextView);
        headerEmailTextView = headerView.findViewById(R.id.headerEmailTextView);

        // Cart badge views in FAB
        View fabLayout = cartFab.findViewById(R.id.cartFabLayout);
        cartIconImageView = fabLayout.findViewById(R.id.cartIconImageView);
        cartBadgeTextView = fabLayout.findViewById(R.id.cartBadgeTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("NMIMS Canteen");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void setupNavigation() {
        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.navigation_cart) {
                navigateToCart();
                return true;
            } else if (itemId == R.id.navigation_orders) {
                navigateToOrders();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navigateToProfile();
                return true;
            }
            return false;
        });

        // Setup navigation drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_cart) {
                navigateToCart();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_orders) {
                navigateToOrders();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navigateToProfile();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_reviews) {
                navigateToReviews();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_settings) {
                navigateToSettings();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_admin && isAdmin) {
                navigateToAdmin();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_logout) {
                showLogoutConfirmation();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });

        // Configure Navigation Component
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_cart, R.id.navigation_orders, R.id.navigation_profile)
                .setOpenableLayouts(drawerLayout)
                .build();
    }

    private void setupRecyclerView() {
        foodItemAdapter = new FoodItemAdapter(this, new FoodItemAdapter.OnFoodItemClickListener() {
            @Override
            public void onFoodItemClick(FoodItem foodItem) {
                showFoodItemDetails(foodItem);
            }

            @Override
            public void onAddToCartClick(FoodItem foodItem) {
                addToCart(foodItem);
            }

            @Override
            public void onFavoriteClick(FoodItem foodItem) {
                toggleFavorite(foodItem);
            }
        });

        foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodItemsRecyclerView.setAdapter(foodItemAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFoodItems();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
    }

    private void setupFab() {
        cartFab.setOnClickListener(v -> navigateToCart());
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = authService.getCurrentUser();
        if (firebaseUser != null) {
            // Load user profile from Firestore
            authService.getCurrentUserProfile(new FirebaseAuthService.UserProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    isAdmin = user.isAdmin();
                    updateNavigationHeader();
                    updateNavigationMenu();
                }

                @Override
                public void onFailure(String error) {
                    // Use basic user info from Firebase Auth
                    currentUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
                    headerTitleTextView.setText(currentUser.getName());
                    headerEmailTextView.setText(currentUser.getEmail());
                }
            });
        }
    }

    private void loadFoodItems() {
        firestoreService.getAllFoodItems(new FirestoreService.DatabaseCallback<List<FoodItem>>() {
            @Override
            public void onSuccess(List<FoodItem> result) {
                foodItems = result;
                foodItemAdapter.setFoodItems(foodItems);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(String error) {
                swipeRefreshLayout.setRefreshing(false);
                showError("Failed to load food items: " + error);
            }
        });
    }

    private void setupCartListener() {
        cartManager.addCartChangeListener(new CartManager.CartChangeListener() {
            @Override
            public void onCartChanged(java.util.ArrayList<com.nmims.canteen.models.CartItem> cartItems) {
                updateCartBadge();
            }

            @Override
            public void onItemAdded(com.nmims.canteen.models.CartItem item) {
                updateCartBadge();
                showCartAddedMessage(item.getFoodItem().getName());
            }

            @Override
            public void onItemRemoved(com.nmims.canteen.models.CartItem item) {
                updateCartBadge();
            }

            @Override
            public void onItemUpdated(com.nmims.canteen.models.CartItem item) {
                updateCartBadge();
            }

            @Override
            public void onCartCleared() {
                updateCartBadge();
            }
        });
    }

    private void updateCartBadge() {
        int itemCount = cartManager.getItemCount();
        if (itemCount > 0) {
            cartBadgeTextView.setVisibility(View.VISIBLE);
            cartBadgeTextView.setText(String.valueOf(itemCount));
            if (itemCount > 99) {
                cartBadgeTextView.setText("99+");
            }
        } else {
            cartBadgeTextView.setVisibility(View.GONE);
        }
    }

    private void updateNavigationHeader() {
        if (currentUser != null) {
            headerTitleTextView.setText(currentUser.getName());
            headerEmailTextView.setText(currentUser.getEmail());
        }
    }

    private void updateNavigationMenu() {
        Menu menu = navigationView.getMenu();
        MenuItem adminItem = menu.findItem(R.id.nav_admin);
        if (adminItem != null) {
            adminItem.setVisible(isAdmin);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("navigate_to")) {
            String destination = intent.getStringExtra("navigate_to");
            switch (destination) {
                case "cart":
                    navigateToCart();
                    break;
                case "orders":
                    navigateToOrders();
                    break;
                case "admin":
                    if (isAdmin) navigateToAdmin();
                    break;
            }
        }
    }

    // Navigation methods
    private void navigateToCart() {
        startActivity(new Intent(this, CartActivity.class));
    }

    private void navigateToOrders() {
        startActivity(new Intent(this, OrderHistoryActivity.class));
    }

    private void navigateToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void navigateToReviews() {
        startActivity(new Intent(this, ReviewActivity.class));
    }

    private void navigateToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void navigateToAdmin() {
        if (isAdmin) {
            startActivity(new Intent(this, AdminActivity.class));
        }
    }

    // Food item interactions
    private void showFoodItemDetails(FoodItem foodItem) {
        // Show food item details dialog or navigate to details activity
        Intent intent = new Intent(this, FoodItemDetailsActivity.class);
        intent.putExtra("food_item_id", foodItem.getItemId());
        startActivity(intent);
    }

    private void addToCart(FoodItem foodItem) {
        if (foodItem.isAvailable() && !foodItem.isOutOfStock()) {
            cartManager.addItem(foodItem, 1);
        } else {
            showError("This item is currently out of stock");
        }
    }

    private void toggleFavorite(FoodItem foodItem) {
        // Implement favorite functionality
        Toast.makeText(this, "Favorite feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showCartAddedMessage(String itemName) {
        Toast.makeText(this, itemName + " added to cart", Toast.LENGTH_SHORT).show();
    }

    // Utility methods
    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        authService.signOut();
        cartManager.clearCart();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        updateCartBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners
        cartManager.removeCartChangeListener(null);
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.action_search) {
            // Implement search functionality
            return true;
        } else if (itemId == R.id.action_filter) {
            // Implement filter functionality
            return true;
        } else if (itemId == R.id.action_cart) {
            navigateToCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}