package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Customer review model
 * Contains user ratings and feedback for food items and orders
 */
public class Review implements Serializable {
    // Review details
    private String reviewId;
    private String userId;
    private String foodItemId;
    private String orderId;
    private String userName;
    private String userEmail;
    private int rating; // 1-5 stars
    private String comment;
    private String title;

    // Timestamps
    private Date createdAt;
    private Date updatedAt;

    // Moderation fields
    private boolean isApproved;
    private boolean isFlagged;
    private String flaggedReason;
    private String moderatedBy;
    private Date moderatedAt;
    private String moderationNotes;

    // User interaction fields
    private int helpfulVotes;
    private int notHelpfulVotes;
    private boolean isVerifiedPurchase;
    private String userImageUrl;
    private String[] reviewImages; // URLs of images attached to review

    // Admin response
    private String adminResponse;
    private String adminRespondedBy;
    private Date adminRespondedAt;

    // Quality control
    private boolean isEdited;
    private Date editedAt;
    private String editReason;
    private boolean isFeatured;
    private int reportCount;

    // Default constructor for Firebase
    public Review() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isApproved = true; // Auto-approve reviews
        this.isFlagged = false;
        this.helpfulVotes = 0;
        this.notHelpfulVotes = 0;
        this.isVerifiedPurchase = false;
        this.isEdited = false;
        this.isFeatured = false;
        this.reportCount = 0;
    }

    // Parameterized constructor
    public Review(String reviewId, String userId, String foodItemId, String userName, int rating, String comment) {
        this();
        this.reviewId = reviewId;
        this.userId = userId;
        this.foodItemId = foodItemId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        validateReview();
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(String foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
            this.updatedAt = new Date();
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.updatedAt = new Date();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
        this.moderatedAt = new Date();
        this.updatedAt = new Date();
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
        this.updatedAt = new Date();
    }

    public String getFlaggedReason() {
        return flaggedReason;
    }

    public void setFlaggedReason(String flaggedReason) {
        this.flaggedReason = flaggedReason;
        this.isFlagged = true;
        this.updatedAt = new Date();
    }

    public String getModeratedBy() {
        return moderatedBy;
    }

    public void setModeratedBy(String moderatedBy) {
        this.moderatedBy = moderatedBy;
        this.moderatedAt = new Date();
        this.updatedAt = new Date();
    }

    public Date getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(Date moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public String getModerationNotes() {
        return moderationNotes;
    }

    public void setModerationNotes(String moderationNotes) {
        this.moderationNotes = moderationNotes;
        this.updatedAt = new Date();
    }

    public int getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(int helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
        this.updatedAt = new Date();
    }

    public int getNotHelpfulVotes() {
        return notHelpfulVotes;
    }

    public void setNotHelpfulVotes(int notHelpfulVotes) {
        this.notHelpfulVotes = notHelpfulVotes;
        this.updatedAt = new Date();
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
        this.updatedAt = new Date();
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String[] getReviewImages() {
        return reviewImages;
    }

    public void setReviewImages(String[] reviewImages) {
        this.reviewImages = reviewImages;
        this.updatedAt = new Date();
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
        this.adminRespondedAt = new Date();
        this.updatedAt = new Date();
    }

    public String getAdminRespondedBy() {
        return adminRespondedBy;
    }

    public void setAdminRespondedBy(String adminRespondedBy) {
        this.adminRespondedBy = adminRespondedBy;
    }

    public Date getAdminRespondedAt() {
        return adminRespondedAt;
    }

    public void setAdminRespondedAt(Date adminRespondedAt) {
        this.adminRespondedAt = adminRespondedAt;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
        if (edited) {
            this.editedAt = new Date();
        }
        this.updatedAt = new Date();
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public String getEditReason() {
        return editReason;
    }

    public void setEditReason(String editReason) {
        this.editReason = editReason;
        this.isEdited = true;
        this.editedAt = new Date();
        this.updatedAt = new Date();
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
        this.updatedAt = new Date();
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
        if (reportCount > 0) {
            this.isFlagged = true;
        }
        this.updatedAt = new Date();
    }

    /**
     * Validates review data
     */
    private void validateReview() {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        if (comment != null && comment.length() > 1000) {
            comment = comment.substring(0, 1000);
        }
        if (title != null && title.length() > 100) {
            title = title.substring(0, 100);
        }
    }

    /**
     * Adds a helpful vote to this review
     */
    public void addHelpfulVote() {
        this.helpfulVotes++;
        this.updatedAt = new Date();
    }

    /**
     * Adds a not helpful vote to this review
     */
    public void addNotHelpfulVote() {
        this.notHelpfulVotes++;
        this.updatedAt = new Date();
    }

    /**
     * Reports this review
     */
    public void reportReview(String reason) {
        this.reportCount++;
        if (this.reportCount >= 3) {
            this.isFlagged = true;
            this.flaggedReason = "Multiple reports: " + reason;
        }
        this.updatedAt = new Date();
    }

    /**
     * Gets the total number of votes
     */
    public int getTotalVotes() {
        return helpfulVotes + notHelpfulVotes;
    }

    /**
     * Gets the helpfulness percentage
     */
    public double getHelpfulnessPercentage() {
        int totalVotes = getTotalVotes();
        if (totalVotes == 0) return 0;
        return (double) helpfulVotes / totalVotes * 100;
    }

    /**
     * Gets rating display text
     */
    public String getRatingDisplay() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    /**
     * Gets rating text description
     */
    public String getRatingText() {
        switch (rating) {
            case 5: return "Excellent";
            case 4: return "Good";
            case 3: return "Average";
            case 2: return "Poor";
            case 1: return "Terrible";
            default: return "No Rating";
        }
    }

    /**
     * Checks if the review has admin response
     */
    public boolean hasAdminResponse() {
        return adminResponse != null && !adminResponse.trim().isEmpty();
    }

    /**
     * Checks if the review has images
     */
    public boolean hasImages() {
        return reviewImages != null && reviewImages.length > 0;
    }

    /**
     * Gets the time ago string for display
     */
    public String getTimeAgo() {
        long diffInMillis = System.currentTimeMillis() - createdAt.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;

        if (diffInDays > 0) {
            return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
        } else if (diffInHours > 0) {
            return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
        } else if (diffInMinutes > 0) {
            return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }

    /**
     * Validates the review data for completeness
     */
    public boolean isValid() {
        return reviewId != null && !reviewId.isEmpty() &&
               userId != null && !userId.isEmpty() &&
               foodItemId != null && !foodItemId.isEmpty() &&
               userName != null && !userName.isEmpty() &&
               rating >= 1 && rating <= 5;
    }

    /**
     * Checks if the review can be edited by the user
     */
    public boolean canBeEdited() {
        // Allow editing within 24 hours of creation
        long diffInMillis = System.currentTimeMillis() - createdAt.getTime();
        long diffInHours = diffInMillis / (1000 * 60 * 60);
        return diffInHours < 24 && !isEdited;
    }

    /**
     * Creates a copy of this review for editing
     */
    public Review createEditCopy() {
        Review copy = new Review();
        copy.reviewId = this.reviewId;
        copy.userId = this.userId;
        copy.foodItemId = this.foodItemId;
        copy.orderId = this.orderId;
        copy.userName = this.userName;
        copy.userEmail = this.userEmail;
        copy.rating = this.rating;
        copy.comment = this.comment;
        copy.title = this.title;
        copy.userImageUrl = this.userImageUrl;
        copy.reviewImages = this.reviewImages;
        copy.isVerifiedPurchase = this.isVerifiedPurchase;
        return copy;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId='" + reviewId + '\'' +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", isApproved=" + isApproved +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId != null && reviewId.equals(review.reviewId);
    }

    @Override
    public int hashCode() {
        return reviewId != null ? reviewId.hashCode() : 0;
    }
}