package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nmims.canteen.R;
import com.nmims.canteen.adapters.ReviewAdapter;
import com.nmims.canteen.models.Review;
import com.nmims.canteen.services.FirestoreService;
import com.nmims.canteen.utils.FirebaseUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Customer review submission and display activity
 * Handles review submission with rating input and existing reviews display
 */
public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";

    // UI Components
    private Toolbar toolbar;
    private CardView submitReviewCard;
    private TextView foodItemNameTextView;
    private RatingBar ratingBar;
    private TextInputLayout commentLayout;
    private TextInputEditText commentEditText;
    private TextView characterCountTextView;
    private Button submitReviewButton;
    private RecyclerView reviewsRecyclerView;
    private TextView averageRatingTextView;
    private TextView reviewCountTextView;
    private TextView noReviewsTextView;
    private View loadingView;

    // Services
    private FirestoreService firestoreService;

    // Data
    private String foodItemId;
    private String foodItemName;
    private ArrayList<Review> reviews;
    private ReviewAdapter reviewAdapter;
    private boolean isEditing = false;
    private Review existingReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize services
        firestoreService = FirestoreService.getInstance();
        reviews = new ArrayList<>();

        // Get food item ID from intent
        getFoodItemDetails();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupRatingBar();
        setupCommentField();
        setupButtons();
        setupRecyclerView();

        // Load data
        loadFoodItemDetails();
        loadReviews();
    }

    private void getFoodItemDetails() {
        Intent intent = getIntent();
        if (intent != null) {
            foodItemId = intent.getStringExtra("food_item_id");
            foodItemName = intent.getStringExtra("food_item_name");
            isEditing = intent.getBooleanExtra("is_editing", false);
            if (foodItemName == null) {
                foodItemName = "Food Item";
            }
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        submitReviewCard = findViewById(R.id.submitReviewCard);
        foodItemNameTextView = findViewById(R.id.foodItemNameTextView);
        ratingBar = findViewById(R.id.ratingBar);
        commentLayout = findViewById(R.id.commentLayout);
        commentEditText = findViewById(R.id.commentEditText);
        characterCountTextView = findViewById(R.id.characterCountTextView);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        averageRatingTextView = findViewById(R.id.averageRatingTextView);
        reviewCountTextView = findViewById(R.id.reviewCountTextView);
        noReviewsTextView = findViewById(R.id.noReviewsTextView);
        loadingView = findViewById(R.id.loadingView);

        // Set food item name
        foodItemNameTextView.setText(foodItemName);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reviews");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                updateRatingDisplay(rating);
            }
        });
    }

    private void setupCommentField() {
        commentEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateCharacterCount(s.length());
            }
        });
    }

    private void setupButtons() {
        submitReviewButton.setOnClickListener(v -> attemptSubmitReview());
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(this, new ReviewAdapter.OnReviewInteractionListener() {
            @Override
            public void onReviewClick(Review review) {
                showReviewDetails(review);
            }

            @Override
            public void onHelpfulClick(Review review) {
                markReviewHelpful(review, true);
            }

            @Override
            public void onNotHelpfulClick(Review review) {
                markReviewHelpful(review, false);
            }

            @Override
            public void onReportClick(Review review) {
                reportReview(review);
            }

            @Override
            public void onDeleteClick(Review review) {
                deleteReviewConfirmation(review);
            }

            @Override
            public void onEditClick(Review review) {
                editReview(review);
            }
        });

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadFoodItemDetails() {
        // In a real implementation, you would load food item details from Firestore
        // For now, we'll use the data from the intent
    }

    private void loadReviews() {
        if (foodItemId == null) {
            showError("No food item specified");
            return;
        }

        showLoading(true);

        firestoreService.getFoodItemReviews(foodItemId, new FirestoreService.DatabaseCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> result) {
                reviews = new ArrayList<>(result);
                reviewAdapter.setReviews(reviews);
                updateRatingSummary();
                showLoading(false);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                showError("Failed to load reviews: " + error);
            }
        });
    }

    private void attemptSubmitReview() {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            commentLayout.setError("Please write a review");
            return;
        }

        if (comment.length() > 1000) {
            commentLayout.setError("Review must be less than 1000 characters");
            return;
        }

        // Disable button to prevent multiple submissions
        submitReviewButton.setEnabled(false);
        submitReviewButton.setText("Submitting...");

        Review review;
        if (isEditing && existingReview != null) {
            // Update existing review
            review = existingReview;
            review.setRating((int) rating);
            review.setComment(comment);
            review.setEdited(true);
            review.setEditReason("User edit");
        } else {
            // Create new review
            String reviewId = FirebaseUtils.generateDocumentId();
            review = new Review(reviewId, FirebaseUtils.getCurrentUserId(), foodItemId,
                    FirebaseUtils.getCurrentUser().getDisplayName(), (int) rating, comment);
        }

        firestoreService.updateReview(review, new FirestoreService.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                submitReviewButton.setEnabled(true);
                submitReviewButton.setText(isEditing ? "Update Review" : "Submit Review");

                if (isEditing) {
                    Toast.makeText(ReviewActivity.this, "Review updated successfully", Toast.LENGTH_SHORT).show();
                    isEditing = false;
                    existingReview = null;
                } else {
                    Toast.makeText(ReviewActivity.this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                }

                // Clear form
                ratingBar.setRating(0);
                commentEditText.setText("");
                updateCharacterCount(0);
                updateRatingDisplay(0);

                // Reload reviews
                loadReviews();
            }

            @Override
            public void onFailure(String error) {
                submitReviewButton.setEnabled(true);
                submitReviewButton.setText(isEditing ? "Update Review" : "Submit Review");
                showError("Failed to submit review: " + error);
            }
        });
    }

    private void updateRatingDisplay(float rating) {
        if (rating == 0) {
            submitReviewButton.setEnabled(false);
        } else {
            submitReviewButton.setEnabled(true);
        }
    }

    private void updateCharacterCount(int count) {
        characterCountTextView.setText(count + "/1000");

        // Change color based on character count
        if (count > 900) {
            characterCountTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else if (count > 800) {
            characterCountTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else {
            characterCountTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void updateRatingSummary() {
        if (reviews.isEmpty()) {
            averageRatingTextView.setText("No ratings");
            reviewCountTextView.setText("(0 reviews)");
            noReviewsTextView.setVisibility(View.VISIBLE);
        } else {
            double averageRating = reviewAdapter.getAverageRating();
            int reviewCount = reviews.size();

            averageRatingTextView.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
            reviewCountTextView.setText(String.format("(%d %s)", reviewCount, reviewCount == 1 ? "review" : "reviews"));
            noReviewsTextView.setVisibility(View.GONE);
        }
    }

    private void showReviewDetails(Review review) {
        StringBuilder details = new StringBuilder();
        details.append("Reviewer: ").append(review.getUserName()).append("\n");
        details.append("Rating: ").append(review.getRatingDisplay()).append("\n");
        details.append("Date: ").append(review.getTimeAgo()).append("\n");

        if (review.getComment() != null) {
            details.append("Review: ").append(review.getComment()).append("\n");
        }

        if (review.hasAdminResponse()) {
            details.append("\nAdmin Response: ").append(review.getAdminResponse());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Review Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void markReviewHelpful(Review review, boolean helpful) {
        // In a real implementation, you would update the helpful/not helpful counts
        // and save to Firestore
        if (helpful) {
            review.addHelpfulVote();
        } else {
            review.addNotHelpfulVote();
        }

        firestoreService.updateReview(review, new FirestoreService.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(ReviewActivity.this, "Thank you for your feedback", Toast.LENGTH_SHORT).show();
                loadReviews(); // Reload to update display
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ReviewActivity.this, "Failed to update vote", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reportReview(Review review) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Report Review")
                .setMessage("Why are you reporting this review?")
                .setPositiveButton("Report", (dialog, which) -> {
                    // In a real implementation, you would save the report
                    Toast.makeText(ReviewActivity.this, "Review reported", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReviewConfirmation(Review review) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete your review?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestoreService.deleteReview(review.getReviewId(), new FirestoreService.DatabaseCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(ReviewActivity.this, "Review deleted", Toast.LENGTH_SHORT).show();
                            loadReviews(); // Reload to update display
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ReviewActivity.this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editReview(Review review) {
        // Populate form with existing review data
        isEditing = true;
        existingReview = review;

        ratingBar.setRating(review.getRating());
        commentEditText.setText(review.getComment());
        updateCharacterCount(review.getComment().length());
        updateRatingDisplay(review.getRating());

        submitReviewButton.setText("Update Review");

        // Scroll to review form
        submitReviewCard.getParent().requestChildFocus(submitReviewCard);
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        reviewsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_sort_by_newest) {
            reviewAdapter.sortByNewest();
            return true;
        } else if (itemId == R.id.action_sort_by_oldest) {
            reviewAdapter.sortByOldest();
            return true;
        } else if (itemId == R.id.action_sort_by_highest) {
            reviewAdapter.sortByHighestRating();
            return true;
        } else if (itemId == R.id.action_sort_by_lowest) {
            reviewAdapter.sortByLowestRating();
            return true;
        } else if (itemId == R.id.action_sort_by_most_helpful) {
            reviewAdapter.sortByMostHelpful();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}