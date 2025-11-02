package com.nmims.canteen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.nmims.canteen.R;
import com.nmims.canteen.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for customer reviews
 * Handles review display with rating visualization and time formatting
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private List<Review> reviews;
    private final OnReviewInteractionListener listener;
    private final SimpleDateFormat dateFormat;

    /**
     * Interface for handling review interactions
     */
    public interface OnReviewInteractionListener {
        void onReviewClick(Review review);
        void onHelpfulClick(Review review);
        void onNotHelpfulClick(Review review);
        void onReportClick(Review review);
        void onDeleteClick(Review review);
        void onEditClick(Review review);
    }

    public ReviewAdapter(Context context, OnReviewInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.reviews = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * Update reviews list with DiffUtil for efficient updates
     */
    public void updateReviews(List<Review> newReviews) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ReviewDiffCallback(this.reviews, newReviews));
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Set reviews list
     */
    public void setReviews(List<Review> reviews) {
        this.reviews = new ArrayList<>(reviews);
        notifyDataSetChanged();
    }

    /**
     * Get review at position
     */
    public Review getReviewAt(int position) {
        return reviews.get(position);
    }

    /**
     * Sort reviews by date (newest first)
     */
    public void sortByNewest() {
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        updateReviews(sorted);
    }

    /**
     * Sort reviews by date (oldest first)
     */
    public void sortByOldest() {
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort(Review::compareTo);
        updateReviews(sorted);
    }

    /**
     * Sort reviews by rating (highest first)
     */
    public void sortByHighestRating() {
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort((r1, r2) -> Integer.compare(r2.getRating(), r1.getRating()));
        updateReviews(sorted);
    }

    /**
     * Sort reviews by rating (lowest first)
     */
    public void sortByLowestRating() {
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort((r1, r2) -> Integer.compare(r1.getRating(), r2.getRating()));
        updateReviews(sorted);
    }

    /**
     * Sort reviews by helpfulness
     */
    public void sortByMostHelpful() {
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort((r1, r2) -> Integer.compare(r2.getHelpfulVotes(), r1.getHelpfulVotes()));
        updateReviews(sorted);
    }

    /**
     * Get average rating
     */
    public double getAverageRating() {
        if (reviews.isEmpty()) return 0;
        double total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        return total / reviews.size();
    }

    /**
     * Get rating distribution
     */
    public int[] getRatingDistribution() {
        int[] distribution = new int[5]; // 1-5 stars
        for (Review review : reviews) {
            if (review.getRating() >= 1 && review.getRating() <= 5) {
                distribution[review.getRating() - 1]++;
            }
        }
        return distribution;
    }

    /**
     * ViewHolder class for reviews
     */
    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView userImageView;
        private final TextView userNameTextView;
        private final TextView reviewDateTextView;
        private final RatingBar ratingBar;
        private final TextView ratingTextTextView;
        private final TextView commentTextView;
        private final TextView helpfulCountTextView;
        private final TextView notHelpfulCountTextView;
        private final ImageButton helpfulButton;
        private final ImageButton notHelpfulButton;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final ImageButton reportButton;
        private final Chip verifiedPurchaseChip;
        private final Chip editedChip;
        private final TextView adminResponseTextView;
        private final View adminResponseContainer;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            userImageView = itemView.findViewById(R.id.userImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            reviewDateTextView = itemView.findViewById(R.id.reviewDateTextView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingTextTextView = itemView.findViewById(R.id.ratingTextTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            helpfulCountTextView = itemView.findViewById(R.id.helpfulCountTextView);
            notHelpfulCountTextView = itemView.findViewById(R.id.notHelpfulCountTextView);
            helpfulButton = itemView.findViewById(R.id.helpfulButton);
            notHelpfulButton = itemView.findViewById(R.id.notHelpfulButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            reportButton = itemView.findViewById(R.id.reportButton);
            verifiedPurchaseChip = itemView.findViewById(R.id.verifiedPurchaseChip);
            editedChip = itemView.findViewById(R.id.editedChip);
            adminResponseTextView = itemView.findViewById(R.id.adminResponseTextView);
            adminResponseContainer = itemView.findViewById(R.id.adminResponseContainer);
        }

        void bind(Review review) {
            // Set user information
            userNameTextView.setText(review.getUserName());

            // Load user image
            if (review.getUserImageUrl() != null && !review.getUserImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(review.getUserImageUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(userImageView);
            } else {
                userImageView.setImageResource(R.drawable.ic_person);
            }

            // Set date
            reviewDateTextView.setText(review.getTimeAgo());

            // Set rating
            ratingBar.setRating(review.getRating());
            ratingTextTextView.setText(review.getRatingDisplay() + " " + review.getRatingText());

            // Set comment
            if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
                commentTextView.setText(review.getComment());
                commentTextView.setVisibility(View.VISIBLE);
            } else {
                commentTextView.setVisibility(View.GONE);
            }

            // Set helpful votes
            helpfulCountTextView.setText(String.valueOf(review.getHelpfulVotes()));
            notHelpfulCountTextView.setText(String.valueOf(review.getNotHelpfulVotes()));

            // Show verified purchase badge
            if (review.isVerifiedPurchase()) {
                verifiedPurchaseChip.setVisibility(View.VISIBLE);
            } else {
                verifiedPurchaseChip.setVisibility(View.GONE);
            }

            // Show edited badge
            if (review.isEdited()) {
                editedChip.setVisibility(View.VISIBLE);
                editedChip.setText("Edited " + review.getTimeAgo());
            } else {
                editedChip.setVisibility(View.GONE);
            }

            // Show admin response
            if (review.hasAdminResponse()) {
                adminResponseTextView.setText(review.getAdminResponse());
                adminResponseContainer.setVisibility(View.VISIBLE);
            } else {
                adminResponseContainer.setVisibility(View.GONE);
            }

            // Set card elevation based on rating
            if (review.getRating() >= 4) {
                cardView.setCardElevation(8f); // Higher elevation for good reviews
            } else if (review.getRating() <= 2) {
                cardView.setCardElevation(2f); // Lower elevation for poor reviews
            } else {
                cardView.setCardElevation(4f); // Normal elevation
            }

            // Set click listeners
            helpfulButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHelpfulClick(review);
                }
            });

            notHelpfulButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotHelpfulClick(review);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(review);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(review);
                }
            });

            reportButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReportClick(review);
                }
            });

            // Card click for review details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReviewClick(review);
                }
            });

            // Long press for more options
            itemView.setOnLongClickListener(v -> {
                // Show context menu or bottom sheet with options
                return true;
            });

            // Set star rating bar color based on rating
            updateRatingBarColor(review.getRating());
        }

        private void updateRatingBarColor(int rating) {
            if (rating >= 4) {
                ratingBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_green_dark));
            } else if (rating <= 2) {
                ratingBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_red_dark));
            } else {
                ratingBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_orange_dark));
            }
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private static class ReviewDiffCallback extends DiffUtil.Callback {
        private final List<Review> oldList;
        private final List<Review> newList;

        public ReviewDiffCallback(List<Review> oldList, List<Review> newList) {
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
            Review oldReview = oldList.get(oldItemPosition);
            Review newReview = newList.get(newItemPosition);
            return oldReview.getReviewId().equals(newReview.getReviewId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Review oldReview = oldList.get(oldItemPosition);
            Review newReview = newList.get(newItemPosition);

            return oldReview.getRating() == newReview.getRating() &&
                   (oldReview.getComment() != null ?
                    oldReview.getComment().equals(newReview.getComment()) :
                    newReview.getComment() == null) &&
                   oldReview.getHelpfulVotes() == newReview.getHelpfulVotes() &&
                   oldReview.getNotHelpfulVotes() == newReview.getNotHelpfulVotes() &&
                   (oldReview.getAdminResponse() != null ?
                    oldReview.getAdminResponse().equals(newReview.getAdminResponse()) :
                    newReview.getAdminResponse() == null);
        }
    }
}