package com.nmims.canteen.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nmims.canteen.models.Order;
import com.nmims.canteen.models.SalesData;
import com.nmims.canteen.models.FoodItem;
import com.nmims.canteen.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sales analytics and reporting utilities
 * Provides comprehensive analytics calculation and reporting functionality
 */
public class AnalyticsManager {
    private static final String TAG = "AnalyticsManager";
    private static AnalyticsManager instance;

    // Background thread for calculations
    private final ExecutorService executorService;
    private final Map<String, SalesData> cache; // Cache for frequently accessed data

    private AnalyticsManager() {
        this.executorService = Executors.newFixedThreadPool(3);
        this.cache = new HashMap<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized AnalyticsManager getInstance() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    /**
     * Analytics result callback interface
     */
    public interface AnalyticsCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    // Daily Sales Calculations

    /**
     * Calculate daily sales for specific date
     */
    public void calculateDailySales(Date date, AnalyticsCallback<SalesData> callback) {
        String dateId = formatDateId(date);

        // Check cache first
        if (cache.containsKey(dateId)) {
            if (callback != null) callback.onSuccess(cache.get(dateId));
            return;
        }

        executorService.execute(() -> {
            try {
                SalesData salesData = new SalesData(dateId, date);
                salesData.setPeriod("daily");

                // Get start and end of day
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date dayStart = calendar.getTime();

                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date dayEnd = calendar.getTime();

                // Fetch orders for the day
                List<Order> orders = getOrdersForDateRange(dayStart, dayEnd);

                // Process orders
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        salesData.addOrderData(order);
                    }
                }

                // Calculate additional metrics
                calculateCustomerMetrics(salesData, orders);
                calculateHourlyBreakdown(salesData, orders);
                calculateQualityMetrics(salesData, orders);

                // Cache the result
                cache.put(dateId, salesData);

                if (callback != null) callback.onSuccess(salesData);

            } catch (Exception e) {
                Log.e(TAG, "Error calculating daily sales", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Calculate weekly sales for week starting from given date
     */
    public void calculateWeeklySales(Date weekStart, AnalyticsCallback<SalesData> callback) {
        executorService.execute(() -> {
            try {
                String dateId = formatDateId(weekStart) + "_WEEK";
                SalesData salesData = new SalesData(dateId, weekStart);
                salesData.setPeriod("weekly");
                salesData.setWeekStart(weekStart);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(weekStart);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                Date weekEnd = calendar.getTime();
                salesData.setWeekEnd(weekEnd);

                // Get daily data for the week and aggregate
                List<SalesData> dailyData = new ArrayList<>();
                Date currentDate = new Date(weekStart.getTime());

                while (currentDate.before(weekEnd)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    String dailyDateId = sdf.format(currentDate);

                    // Get or calculate daily data
                    if (cache.containsKey(dailyDateId)) {
                        dailyData.add(cache.get(dailyDateId));
                    } else {
                        // Calculate daily sales for this date
                        final Date finalCurrentDate = new Date(currentDate.getTime());
                        Tasks.await(FirebaseUtils.getOrdersCollection()
                                .whereGreaterThanOrEqualTo("createdAt", finalCurrentDate)
                                .get());
                        // This would need proper async handling in real implementation
                    }

                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    currentDate = calendar.getTime();
                }

                // Aggregate daily data
                aggregateDailySales(dailyData, salesData);

                if (callback != null) callback.onSuccess(salesData);

            } catch (Exception e) {
                Log.e(TAG, "Error calculating weekly sales", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Calculate monthly sales for given month
     */
    public void calculateMonthlySales(Date monthStart, AnalyticsCallback<SalesData> callback) {
        executorService.execute(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault());
                String dateId = sdf.format(monthStart) + "_MONTH";
                SalesData salesData = new SalesData(dateId, monthStart);
                salesData.setPeriod("monthly");
                salesData.setMonthStart(monthStart);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(monthStart);
                calendar.add(Calendar.MONTH, 1);
                Date monthEnd = calendar.getTime();
                salesData.setMonthEnd(monthEnd);

                // Aggregate daily data for the month
                List<SalesData> dailyData = getDailyDataForRange(monthStart, monthEnd);
                aggregateDailySales(dailyData, salesData);

                if (callback != null) callback.onSuccess(salesData);

            } catch (Exception e) {
                Log.e(TAG, "Error calculating monthly sales", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Top Selling Items Analysis

    /**
     * Get top selling items for specified period
     */
    public void getTopSellingItems(Date startDate, Date endDate, int limit, AnalyticsCallback<List<Map.Entry<String, Integer>>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Integer> itemSales = new HashMap<>();

                // Get orders for the period
                List<Order> orders = getOrdersForDateRange(startDate, endDate);

                // Aggregate item sales
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        for (var item : order.getItems()) {
                            String itemName = item.getFoodItem().getName();
                            itemSales.put(itemName, itemSales.getOrDefault(itemName, 0) + item.getQuantity());
                        }
                    }
                }

                // Sort by quantity sold
                List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemSales.entrySet());
                sortedItems.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                // Limit results
                if (sortedItems.size() > limit) {
                    sortedItems = sortedItems.subList(0, limit);
                }

                if (callback != null) callback.onSuccess(sortedItems);

            } catch (Exception e) {
                Log.e(TAG, "Error getting top selling items", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Get top revenue generating items
     */
    public void getTopRevenueItems(Date startDate, Date endDate, int limit, AnalyticsCallback<List<Map.Entry<String, Double>>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Double> itemRevenue = new HashMap<>();

                // Get orders for the period
                List<Order> orders = getOrdersForDateRange(startDate, endDate);

                // Aggregate item revenue
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        for (var item : order.getItems()) {
                            String itemName = item.getFoodItem().getName();
                            itemRevenue.put(itemName, itemRevenue.getOrDefault(itemName, 0.0) + item.getTotalPrice());
                        }
                    }
                }

                // Sort by revenue
                List<Map.Entry<String, Double>> sortedItems = new ArrayList<>(itemRevenue.entrySet());
                sortedItems.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                // Limit results
                if (sortedItems.size() > limit) {
                    sortedItems = sortedItems.subList(0, limit);
                }

                if (callback != null) callback.onSuccess(sortedItems);

            } catch (Exception e) {
                Log.e(TAG, "Error getting top revenue items", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Category Performance Analysis

    /**
     * Get category performance for specified period
     */
    public void getCategoryPerformance(Date startDate, Date endDate, String period, AnalyticsCallback<Map<String, Map<String, Object>>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Map<String, Object>> categoryPerformance = new HashMap<>();

                // Initialize category data structure
                Map<String, Integer> categoryOrders = new HashMap<>();
                Map<String, Double> categoryRevenue = new HashMap<>();
                Map<String, Integer> categoryQuantities = new HashMap<>();

                // Get orders for the period
                List<Order> orders = getOrdersForDateRange(startDate, endDate);

                // Aggregate category data
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        for (var item : order.getItems()) {
                            String category = item.getFoodItem().getCategory();
                            categoryOrders.put(category, categoryOrders.getOrDefault(category, 0) + 1);
                            categoryRevenue.put(category, categoryRevenue.getOrDefault(category, 0.0) + item.getTotalPrice());
                            categoryQuantities.put(category, categoryQuantities.getOrDefault(category, 0) + item.getQuantity());
                        }
                    }
                }

                // Create performance metrics
                for (String category : categoryOrders.keySet()) {
                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("orders", categoryOrders.get(category));
                    metrics.put("revenue", categoryRevenue.get(category));
                    metrics.put("quantities", categoryQuantities.get(category));
                    metrics.put("averageOrderValue", categoryRevenue.get(category) / categoryOrders.get(category));
                    metrics.put("averageItemPrice", categoryRevenue.get(category) / categoryQuantities.get(category));
                    metrics.put("period", period);
                    metrics.put("startDate", startDate);
                    metrics.put("endDate", endDate);

                    categoryPerformance.put(category, metrics);
                }

                if (callback != null) callback.onSuccess(categoryPerformance);

            } catch (Exception e) {
                Log.e(TAG, "Error getting category performance", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Peak Hours Analysis

    /**
     * Get peak hours for specified number of days
     */
    public void getPeakHours(int daysBack, AnalyticsCallback<Map<String, Integer>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Integer> hourlyOrders = new TreeMap<>();

                // Initialize all hours of the day
                for (int hour = 0; hour < 24; hour++) {
                    hourlyOrders.put(String.format("%02d:00", hour), 0);
                }

                // Get date range
                Calendar calendar = Calendar.getInstance();
                Date endDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, -daysBack);
                Date startDate = calendar.getTime();

                // Get orders for the period
                List<Order> orders = getOrdersForDateRange(startDate, endDate);

                // Count orders by hour
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        Calendar orderTime = Calendar.getInstance();
                        orderTime.setTime(order.getCreatedAt());
                        int hour = orderTime.get(Calendar.HOUR_OF_DAY);
                        String hourKey = String.format("%02d:00", hour);
                        hourlyOrders.put(hourKey, hourlyOrders.get(hourKey) + 1);
                    }
                }

                if (callback != null) callback.onSuccess(hourlyOrders);

            } catch (Exception e) {
                Log.e(TAG, "Error getting peak hours", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Customer Analytics

    /**
     * Calculate customer retention rate
     */
    public void getCustomerRetentionRate(Date startDate, Date endDate, AnalyticsCallback<Double> callback) {
        executorService.execute(() -> {
            try {
                // This would need complex analysis of customer order history
                // For now, return a simulated value
                double retentionRate = 0.65; // 65% retention rate placeholder

                if (callback != null) callback.onSuccess(retentionRate);

            } catch (Exception e) {
                Log.e(TAG, "Error calculating customer retention rate", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Get repeat customer statistics
     */
    public void getRepeatCustomerStats(Date startDate, Date endDate, AnalyticsCallback<Map<String, Object>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Integer> customerOrderCount = new HashMap<>();

                // Get orders for the period
                List<Order> orders = getOrdersForDateRange(startDate, endDate);

                // Count orders per customer
                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        String userId = order.getUserId();
                        customerOrderCount.put(userId, customerOrderCount.getOrDefault(userId, 0) + 1);
                    }
                }

                // Calculate statistics
                int totalCustomers = customerOrderCount.size();
                int repeatCustomers = 0;
                int totalOrders = 0;

                for (int orderCount : customerOrderCount.values()) {
                    totalOrders += orderCount;
                    if (orderCount > 1) {
                        repeatCustomers++;
                    }
                }

                double averageOrdersPerCustomer = totalCustomers > 0 ? (double) totalOrders / totalCustomers : 0;
                double repeatCustomerRate = totalCustomers > 0 ? (double) repeatCustomers / totalCustomers * 100 : 0;

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalCustomers", totalCustomers);
                stats.put("repeatCustomers", repeatCustomers);
                stats.put("totalOrders", totalOrders);
                stats.put("averageOrdersPerCustomer", averageOrdersPerCustomer);
                stats.put("repeatCustomerRate", repeatCustomerRate);
                stats.put("period", startDate + " to " + endDate);

                if (callback != null) callback.onSuccess(stats);

            } catch (Exception e) {
                Log.e(TAG, "Error getting repeat customer stats", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Report Generation

    /**
     * Generate sales report for date range
     */
    public void generateSalesReport(Date startDate, Date endDate, AnalyticsCallback<Map<String, Object>> callback) {
        executorService.execute(() -> {
            try {
                Map<String, Object> report = new HashMap<>();

                // Get basic sales metrics
                List<Order> orders = getOrdersForDateRange(startDate, endDate);
                double totalRevenue = 0;
                int totalOrders = 0;
                int totalCustomers = 0;
                Map<String, Integer> customerOrders = new HashMap<>();

                for (Order order : orders) {
                    if (!order.isCancelled() && order.isPaymentCompleted()) {
                        totalRevenue += order.getFinalAmount();
                        totalOrders++;
                        String userId = order.getUserId();
                        customerOrders.put(userId, customerOrders.getOrDefault(userId, 0) + 1);
                    }
                }

                totalCustomers = customerOrders.size();
                double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

                // Create report summary
                report.put("reportType", "Sales Report");
                report.put("startDate", startDate);
                report.put("endDate", endDate);
                report.put("totalRevenue", totalRevenue);
                report.put("totalOrders", totalOrders);
                report.put("totalCustomers", totalCustomers);
                report.put("averageOrderValue", averageOrderValue);
                report.put("generatedAt", new Date());
                report.put("generatedBy", "AnalyticsManager");

                // Add top items and categories
                // This would call the respective analysis methods

                if (callback != null) callback.onSuccess(report);

            } catch (Exception e) {
                Log.e(TAG, "Error generating sales report", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Export Functions

    /**
     * Export analytics data to CSV format
     */
    public void exportToCSV(Map<String, Object> data, AnalyticsCallback<String> callback) {
        executorService.execute(() -> {
            try {
                StringBuilder csv = new StringBuilder();

                // Add header
                csv.append("Metric,Value,Period\n");

                // Add data rows
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    csv.append(entry.getKey()).append(",")
                       .append(entry.getValue().toString()).append(",")
                       .append("Custom Range\n");
                }

                if (callback != null) callback.onSuccess(csv.toString());

            } catch (Exception e) {
                Log.e(TAG, "Error exporting to CSV", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Export analytics data to JSON format
     */
    public void exportToJSON(Map<String, Object> data, AnalyticsCallback<String> callback) {
        executorService.execute(() -> {
            try {
                // Simple JSON conversion (in real implementation, use proper JSON library)
                StringBuilder json = new StringBuilder();
                json.append("{\n");

                boolean first = true;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (!first) json.append(",\n");
                    json.append("  \"").append(entry.getKey()).append("\": \"")
                        .append(entry.getValue().toString()).append("\"");
                    first = false;
                }

                json.append("\n}");

                if (callback != null) callback.onSuccess(json.toString());

            } catch (Exception e) {
                Log.e(TAG, "Error exporting to JSON", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }

    // Utility Methods

    /**
     * Format date ID as yyyy-MM-dd
     */
    private String formatDateId(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get orders for date range (simplified)
     */
    private List<Order> getOrdersForDateRange(Date startDate, Date endDate) {
        List<Order> orders = new ArrayList<>();
        // This would fetch from Firebase in real implementation
        // For now, return empty list
        return orders;
    }

    /**
     * Get daily data for date range
     */
    private List<SalesData> getDailyDataForRange(Date startDate, Date endDate) {
        List<SalesData> dailyData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate)) {
            String dateId = formatDateId(calendar.getTime());
            if (cache.containsKey(dateId)) {
                dailyData.add(cache.get(dateId));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dailyData;
    }

    /**
     * Aggregate daily sales into weekly/monthly data
     */
    private void aggregateDailySales(List<SalesData> dailyData, SalesData aggregatedData) {
        for (SalesData daily : dailyData) {
            aggregatedData.setTotalRevenue(aggregatedData.getTotalRevenue() + daily.getTotalRevenue());
            aggregatedData.setTotalOrders(aggregatedData.getTotalOrders() + daily.getTotalOrders());
            aggregatedData.setNewCustomers(aggregatedData.getNewCustomers() + daily.getNewCustomers());
            aggregatedData.setReturningCustomers(aggregatedData.getReturningCustomers() + daily.getReturningCustomers());

            // Aggregate other metrics as needed
        }
    }

    /**
     * Calculate customer metrics for sales data
     */
    private void calculateCustomerMetrics(SalesData salesData, List<Order> orders) {
        Map<String, Integer> customerOrderCount = new HashMap<>();
        int newCustomers = 0;
        int returningCustomers = 0;

        // Count orders per customer
        for (Order order : orders) {
            String userId = order.getUserId();
            customerOrderCount.put(userId, customerOrderCount.getOrDefault(userId, 0) + 1);
        }

        // Classify customers
        for (int orderCount : customerOrderCount.values()) {
            if (orderCount == 1) {
                newCustomers++;
            } else {
                returningCustomers++;
            }
        }

        salesData.setNewCustomers(newCustomers);
        salesData.setReturningCustomers(returningCustomers);
        salesData.setUniqueCustomers(customerOrderCount.size());
    }

    /**
     * Calculate hourly breakdown for sales data
     */
    private void calculateHourlyBreakdown(SalesData salesData, List<Order> orders) {
        Map<String, Double> hourlySales = new HashMap<>();
        Map<String, Integer> hourlyOrders = new HashMap<>();

        for (Order order : orders) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(order.getCreatedAt());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            String hourKey = String.format("%02d:00", hour);

            hourlySales.put(hourKey, hourlySales.getOrDefault(hourKey, 0.0) + order.getFinalAmount());
            hourlyOrders.put(hourKey, hourlyOrders.getOrDefault(hourKey, 0) + 1);
        }

        // Find peak hour
        String peakHour = "";
        double maxRevenue = 0;
        int maxOrders = 0;

        for (Map.Entry<String, Double> entry : hourlySales.entrySet()) {
            if (entry.getValue() > maxRevenue) {
                maxRevenue = entry.getValue();
                peakHour = entry.getKey();
                maxOrders = hourlyOrders.get(entry.getKey());
            }
        }

        salesData.setHourlySales(hourlySales);
        salesData.setHourlyOrders(hourlyOrders);
        salesData.setPeakHour(peakHour);
        salesData.setPeakHourRevenue(maxRevenue);
        salesData.setPeakHourOrders(maxOrders);
    }

    /**
     * Calculate quality metrics for sales data
     */
    private void calculateQualityMetrics(SalesData salesData, List<Order> orders) {
        int complaints = 0;
        double totalRating = 0;
        int totalReviews = 0;

        // This would need to fetch review data in real implementation
        // For now, set placeholder values
        salesData.setComplaints(complaints);
        salesData.setAverageRating(totalRating);
        salesData.setTotalReviews(totalReviews);
        salesData.setCustomerSatisfactionScore(totalRating > 0 ? totalRating : 4.0); // Default to 4.0
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Get cached data for date
     */
    public SalesData getCachedData(String dateId) {
        return cache.get(dateId);
    }

    /**
     * Cache data
     */
    public void cacheData(String dateId, SalesData data) {
        cache.put(dateId, data);
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}