package com.nmims.canteen.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Sales analytics data model
 * Contains comprehensive sales performance metrics and analytics data
 */
public class SalesData implements Serializable {
    // Date and period information
    private String dateId; // Format: yyyy-MM-dd
    private Date date;
    private String period; // "daily", "weekly", "monthly", "yearly"
    private Date weekStart;
    private Date weekEnd;
    private Date monthStart;
    private Date monthEnd;
    private Date yearStart;
    private Date yearEnd;

    // Core metrics
    private double totalRevenue;
    private int totalOrders;
    private double averageOrderValue;
    private String peakHour;
    private int peakHourOrders;
    private double peakHourRevenue;

    // Customer metrics
    private int newCustomers;
    private int returningCustomers;
    private double repeatRate;
    private int uniqueCustomers;
    private double customerRetentionRate;

    // Payment breakdown
    private Map<String, Integer> paymentMethodCounts;
    private Map<String, Double> paymentMethodRevenue;

    // Item performance
    private Map<String, Integer> itemQuantities;
    private Map<String, Double> itemRevenue;
    private Map<String, Integer> itemOrderCounts;
    private String topSellingItemId;
    private String topRevenueItemId;

    // Category performance
    private Map<String, Integer> categoryOrders;
    private Map<String, Double> categoryRevenue;
    private Map<String, Integer> categoryQuantities;
    private String topCategory;

    // Time-based sales data
    private Map<String, Double> hourlySales;
    private Map<String, Integer> hourlyOrders;
    private Map<String, Double> dailySales;
    private Map<String, Integer> dailyOrders;
    private Map<String, Double> weeklySales;
    private Map<String, Integer> weeklyOrders;
    private Map<String, Double> monthlySales;
    private Map<String, Integer> monthlyOrders;

    // Performance indicators
    private double growthRate;
    private double profitMargin;
    private double costOfGoodsSold;
    private double grossProfit;
    private int itemsPerOrderAverage;
    private double discountGiven;
    private int cancelledOrders;
    private double refundedAmount;

    // Delivery and fulfillment
    private int pickupOrders;
    private int deliveryOrders;
    private double deliveryRevenue;
    private double averageDeliveryTime;
    private int onTimeDeliveries;

    // Quality metrics
    private double averageRating;
    private int totalReviews;
    private int complaints;
    private double customerSatisfactionScore;

    // Inventory metrics
    private int lowStockAlerts;
    private int outOfStockItems;
    private double wasteValue;
    private int stockTurnoverRate;

    // Default constructor for Firebase
    public SalesData() {
        this.date = new Date();
        this.paymentMethodCounts = new HashMap<>();
        this.paymentMethodRevenue = new HashMap<>();
        this.itemQuantities = new HashMap<>();
        this.itemRevenue = new HashMap<>();
        this.itemOrderCounts = new HashMap<>();
        this.categoryOrders = new HashMap<>();
        this.categoryRevenue = new HashMap<>();
        this.categoryQuantities = new HashMap<>();
        this.hourlySales = new HashMap<>();
        this.hourlyOrders = new HashMap<>();
        this.dailySales = new HashMap<>();
        this.dailyOrders = new HashMap<>();
        this.weeklySales = new HashMap<>();
        this.weeklyOrders = new HashMap<>();
        this.monthlySales = new HashMap<>();
        this.monthlyOrders = new HashMap<>();

        // Initialize payment methods
        paymentMethodCounts.put("Cash", 0);
        paymentMethodCounts.put("Credit Card", 0);
        paymentMethodCounts.put("Debit Card", 0);
        paymentMethodCounts.put("UPI", 0);
        paymentMethodCounts.put("Wallet", 0);

        paymentMethodRevenue.put("Cash", 0.0);
        paymentMethodRevenue.put("Credit Card", 0.0);
        paymentMethodRevenue.put("Debit Card", 0.0);
        paymentMethodRevenue.put("UPI", 0.0);
        paymentMethodRevenue.put("Wallet", 0.0);
    }

    // Parameterized constructor
    public SalesData(String dateId, Date date) {
        this();
        this.dateId = dateId;
        this.date = date;
    }

    // Getters and Setters
    public String getDateId() {
        return dateId;
    }

    public void setDateId(String dateId) {
        this.dateId = dateId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(Date weekStart) {
        this.weekStart = weekStart;
    }

    public Date getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(Date weekEnd) {
        this.weekEnd = weekEnd;
    }

    public Date getMonthStart() {
        return monthStart;
    }

    public void setMonthStart(Date monthStart) {
        this.monthStart = monthStart;
    }

    public Date getMonthEnd() {
        return monthEnd;
    }

    public void setMonthEnd(Date monthEnd) {
        this.monthEnd = monthEnd;
    }

    public Date getYearStart() {
        return yearStart;
    }

    public void setYearStart(Date yearStart) {
        this.yearStart = yearStart;
    }

    public Date getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Date yearEnd) {
        this.yearEnd = yearEnd;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
        calculateAverageOrderValue();
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
        calculateAverageOrderValue();
    }

    public double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public String getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(String peakHour) {
        this.peakHour = peakHour;
    }

    public int getPeakHourOrders() {
        return peakHourOrders;
    }

    public void setPeakHourOrders(int peakHourOrders) {
        this.peakHourOrders = peakHourOrders;
    }

    public double getPeakHourRevenue() {
        return peakHourRevenue;
    }

    public void setPeakHourRevenue(double peakHourRevenue) {
        this.peakHourRevenue = peakHourRevenue;
    }

    public int getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(int newCustomers) {
        this.newCustomers = newCustomers;
        calculateRepeatRate();
    }

    public int getReturningCustomers() {
        return returningCustomers;
    }

    public void setReturningCustomers(int returningCustomers) {
        this.returningCustomers = returningCustomers;
        calculateRepeatRate();
    }

    public double getRepeatRate() {
        return repeatRate;
    }

    public void setRepeatRate(double repeatRate) {
        this.repeatRate = repeatRate;
    }

    public int getUniqueCustomers() {
        return uniqueCustomers;
    }

    public void setUniqueCustomers(int uniqueCustomers) {
        this.uniqueCustomers = uniqueCustomers;
    }

    public double getCustomerRetentionRate() {
        return customerRetentionRate;
    }

    public void setCustomerRetentionRate(double customerRetentionRate) {
        this.customerRetentionRate = customerRetentionRate;
    }

    public Map<String, Integer> getPaymentMethodCounts() {
        return paymentMethodCounts;
    }

    public void setPaymentMethodCounts(Map<String, Integer> paymentMethodCounts) {
        this.paymentMethodCounts = paymentMethodCounts;
    }

    public Map<String, Double> getPaymentMethodRevenue() {
        return paymentMethodRevenue;
    }

    public void setPaymentMethodRevenue(Map<String, Double> paymentMethodRevenue) {
        this.paymentMethodRevenue = paymentMethodRevenue;
    }

    public Map<String, Integer> getItemQuantities() {
        return itemQuantities;
    }

    public void setItemQuantities(Map<String, Integer> itemQuantities) {
        this.itemQuantities = itemQuantities;
    }

    public Map<String, Double> getItemRevenue() {
        return itemRevenue;
    }

    public void setItemRevenue(Map<String, Double> itemRevenue) {
        this.itemRevenue = itemRevenue;
    }

    public Map<String, Integer> getItemOrderCounts() {
        return itemOrderCounts;
    }

    public void setItemOrderCounts(Map<String, Integer> itemOrderCounts) {
        this.itemOrderCounts = itemOrderCounts;
    }

    public String getTopSellingItemId() {
        return topSellingItemId;
    }

    public void setTopSellingItemId(String topSellingItemId) {
        this.topSellingItemId = topSellingItemId;
    }

    public String getTopRevenueItemId() {
        return topRevenueItemId;
    }

    public void setTopRevenueItemId(String topRevenueItemId) {
        this.topRevenueItemId = topRevenueItemId;
    }

    public Map<String, Integer> getCategoryOrders() {
        return categoryOrders;
    }

    public void setCategoryOrders(Map<String, Integer> categoryOrders) {
        this.categoryOrders = categoryOrders;
    }

    public Map<String, Double> getCategoryRevenue() {
        return categoryRevenue;
    }

    public void setCategoryRevenue(Map<String, Double> categoryRevenue) {
        this.categoryRevenue = categoryRevenue;
    }

    public Map<String, Integer> getCategoryQuantities() {
        return categoryQuantities;
    }

    public void setCategoryQuantities(Map<String, Integer> categoryQuantities) {
        this.categoryQuantities = categoryQuantities;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public Map<String, Double> getHourlySales() {
        return hourlySales;
    }

    public void setHourlySales(Map<String, Double> hourlySales) {
        this.hourlySales = hourlySales;
    }

    public Map<String, Integer> getHourlyOrders() {
        return hourlyOrders;
    }

    public void setHourlyOrders(Map<String, Integer> hourlyOrders) {
        this.hourlyOrders = hourlyOrders;
    }

    public Map<String, Double> getDailySales() {
        return dailySales;
    }

    public void setDailySales(Map<String, Double> dailySales) {
        this.dailySales = dailySales;
    }

    public Map<String, Integer> getDailyOrders() {
        return dailyOrders;
    }

    public void setDailyOrders(Map<String, Integer> dailyOrders) {
        this.dailyOrders = dailyOrders;
    }

    public Map<String, Double> getWeeklySales() {
        return weeklySales;
    }

    public void setWeeklySales(Map<String, Double> weeklySales) {
        this.weeklySales = weeklySales;
    }

    public Map<String, Integer> getWeeklyOrders() {
        return weeklyOrders;
    }

    public void setWeeklyOrders(Map<String, Integer> weeklyOrders) {
        this.weeklyOrders = weeklyOrders;
    }

    public Map<String, Double> getMonthlySales() {
        return monthlySales;
    }

    public void setMonthlySales(Map<String, Double> monthlySales) {
        this.monthlySales = monthlySales;
    }

    public Map<String, Integer> getMonthlyOrders() {
        return monthlyOrders;
    }

    public void setMonthlyOrders(Map<String, Integer> monthlyOrders) {
        this.monthlyOrders = monthlyOrders;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public double getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(double profitMargin) {
        this.profitMargin = profitMargin;
    }

    public double getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(double costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public int getItemsPerOrderAverage() {
        return itemsPerOrderAverage;
    }

    public void setItemsPerOrderAverage(int itemsPerOrderAverage) {
        this.itemsPerOrderAverage = itemsPerOrderAverage;
    }

    public double getDiscountGiven() {
        return discountGiven;
    }

    public void setDiscountGiven(double discountGiven) {
        this.discountGiven = discountGiven;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public double getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(double refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public int getPickupOrders() {
        return pickupOrders;
    }

    public void setPickupOrders(int pickupOrders) {
        this.pickupOrders = pickupOrders;
    }

    public int getDeliveryOrders() {
        return deliveryOrders;
    }

    public void setDeliveryOrders(int deliveryOrders) {
        this.deliveryOrders = deliveryOrders;
    }

    public double getDeliveryRevenue() {
        return deliveryRevenue;
    }

    public void setDeliveryRevenue(double deliveryRevenue) {
        this.deliveryRevenue = deliveryRevenue;
    }

    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }

    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }

    public int getOnTimeDeliveries() {
        return onTimeDeliveries;
    }

    public void setOnTimeDeliveries(int onTimeDeliveries) {
        this.onTimeDeliveries = onTimeDeliveries;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getComplaints() {
        return complaints;
    }

    public void setComplaints(int complaints) {
        this.complaints = complaints;
    }

    public double getCustomerSatisfactionScore() {
        return customerSatisfactionScore;
    }

    public void setCustomerSatisfactionScore(double customerSatisfactionScore) {
        this.customerSatisfactionScore = customerSatisfactionScore;
    }

    public int getLowStockAlerts() {
        return lowStockAlerts;
    }

    public void setLowStockAlerts(int lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }

    public int getOutOfStockItems() {
        return outOfStockItems;
    }

    public void setOutOfStockItems(int outOfStockItems) {
        this.outOfStockItems = outOfStockItems;
    }

    public double getWasteValue() {
        return wasteValue;
    }

    public void setWasteValue(double wasteValue) {
        this.wasteValue = wasteValue;
    }

    public int getStockTurnoverRate() {
        return stockTurnoverRate;
    }

    public void setStockTurnoverRate(int stockTurnoverRate) {
        this.stockTurnoverRate = stockTurnoverRate;
    }

    /**
     * Calculates average order value
     */
    private void calculateAverageOrderValue() {
        if (totalOrders > 0) {
            this.averageOrderValue = totalRevenue / totalOrders;
        } else {
            this.averageOrderValue = 0;
        }
    }

    /**
     * Calculates repeat rate
     */
    private void calculateRepeatRate() {
        int totalCustomers = newCustomers + returningCustomers;
        if (totalCustomers > 0) {
            this.repeatRate = (double) returningCustomers / totalCustomers * 100;
        } else {
            this.repeatRate = 0;
        }
    }

    /**
     * Adds order data to this sales record
     */
    public void addOrderData(Order order) {
        if (order == null || order.isCancelled()) return;

        this.totalOrders++;
        this.totalRevenue += order.getFinalAmount();

        // Update payment method data
        String paymentMethod = order.getPaymentMethod().getDisplayName();
        paymentMethodCounts.put(paymentMethod, paymentMethodCounts.getOrDefault(paymentMethod, 0) + 1);
        paymentMethodRevenue.put(paymentMethod, paymentMethodRevenue.getOrDefault(paymentMethod, 0.0) + order.getFinalAmount());

        // Update delivery type data
        if (order.getDeliveryType() == Order.DeliveryType.PICKUP) {
            pickupOrders++;
        } else {
            deliveryOrders++;
            deliveryRevenue += order.getDeliveryCharges();
        }

        // Update order items data
        for (CartItem item : order.getItems()) {
            String itemId = item.getFoodItem().getItemId();
            String itemName = item.getFoodItem().getName();
            String category = item.getFoodItem().getCategory();

            // Update item quantities
            itemQuantities.put(itemId, itemQuantities.getOrDefault(itemId, 0) + item.getQuantity());
            itemRevenue.put(itemId, itemRevenue.getOrDefault(itemId, 0.0) + item.getTotalPrice());
            itemOrderCounts.put(itemId, itemOrderCounts.getOrDefault(itemId, 0) + 1);

            // Update category data
            categoryOrders.put(category, categoryOrders.getOrDefault(category, 0) + 1);
            categoryRevenue.put(category, categoryRevenue.getOrDefault(category, 0.0) + item.getTotalPrice());
            categoryQuantities.put(category, categoryQuantities.getOrDefault(category, 0) + item.getQuantity());
        }

        // Calculate average order value
        calculateAverageOrderValue();

        // Update top performers
        updateTopPerformers();
    }

    /**
     * Updates top selling items and categories
     */
    private void updateTopPerformers() {
        // Find top selling item by quantity
        int maxQuantity = 0;
        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            if (entry.getValue() > maxQuantity) {
                maxQuantity = entry.getValue();
                topSellingItemId = entry.getKey();
            }
        }

        // Find top revenue item
        double maxRevenue = 0;
        for (Map.Entry<String, Double> entry : itemRevenue.entrySet()) {
            if (entry.getValue() > maxRevenue) {
                maxRevenue = entry.getValue();
                topRevenueItemId = entry.getKey();
            }
        }

        // Find top category
        int maxCategoryOrders = 0;
        for (Map.Entry<String, Integer> entry : categoryOrders.entrySet()) {
            if (entry.getValue() > maxCategoryOrders) {
                maxCategoryOrders = entry.getValue();
                topCategory = entry.getKey();
            }
        }
    }

    /**
     * Calculates growth rate compared to previous period
     */
    public void calculateGrowthRate(SalesData previousPeriod) {
        if (previousPeriod != null && previousPeriod.getTotalRevenue() > 0) {
            this.growthRate = ((this.totalRevenue - previousPeriod.getTotalRevenue()) / previousPeriod.getTotalRevenue()) * 100;
        } else {
            this.growthRate = 0;
        }
    }

    /**
     * Gets top selling items with specified limit
     */
    public Map<String, Integer> getTopSellingItems(int limit) {
        Map<String, Integer> topItems = new HashMap<>();

        // Sort items by quantity and take top N
        itemQuantities.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> topItems.put(entry.getKey(), entry.getValue()));

        return topItems;
    }

    /**
     * Gets peak hours with sales data
     */
    public Map<String, Double> getPeakHours() {
        Map<String, Double> peakHours = new HashMap<>();

        hourlySales.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> peakHours.put(entry.getKey(), entry.getValue()));

        return peakHours;
    }

    /**
     * Gets category performance data
     */
    public Map<String, Double> getCategoryPerformance() {
        Map<String, Double> performance = new HashMap<>();

        for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
            String category = entry.getKey();
            double revenue = entry.getValue();
            int orders = categoryOrders.getOrDefault(category, 0);

            // Calculate average order value per category
            double avgOrderValue = orders > 0 ? revenue / orders : 0;
            performance.put(category, avgOrderValue);
        }

        return performance;
    }

    /**
     * Gets revenue breakdown by payment method
     */
    public Map<String, Double> getPaymentBreakdown() {
        Map<String, Double> breakdown = new HashMap<>();

        for (Map.Entry<String, Double> entry : paymentMethodRevenue.entrySet()) {
            String method = entry.getKey();
            double revenue = entry.getValue();

            // Calculate percentage of total revenue
            double percentage = totalRevenue > 0 ? (revenue / totalRevenue) * 100 : 0;
            breakdown.put(method, percentage);
        }

        return breakdown;
    }

    @Override
    public String toString() {
        return "SalesData{" +
                "dateId='" + dateId + '\'' +
                ", totalRevenue=" + totalRevenue +
                ", totalOrders=" + totalOrders +
                ", averageOrderValue=" + averageOrderValue +
                ", peakHour='" + peakHour + '\'' +
                '}';
    }
}