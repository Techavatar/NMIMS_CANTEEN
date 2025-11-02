package com.nmims.canteen.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mphil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nmims.canteen.R;
import com.nmims.canteen.models.SalesData;
import com.nmims.canteen.utils.AnalyticsManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Detailed sales analytics and reporting activity
 * Handles sales analytics visualization and comprehensive reporting
 */
public class SalesAnalyticsActivity extends AppCompatActivity {
    private static final String TAG = "SalesAnalyticsActivity";

    // UI Components
    private Toolbar toolbar;
    private CardView dateRangeCard;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private Button selectDateRangeButton;
    private Button todayButton;
    private Button weekButton;
    private Button monthButton;
    private Button yearButton;
    private Button customRangeButton;
    private Button exportButton;

    // Chart components
    private LineChart revenueChart;
    private BarChart categoryChart;
    private BarChart topItemsChart;
    private PieChart paymentChart;

    // Summary components
    private CardView summaryCard;
    private TextView totalRevenueTextView;
    private TextView totalOrdersTextView;
    private TextView averageOrderValueTextView;
    private TextView peakHourTextView;
    private TextView customerRetentionTextView;

    // Data
    private AnalyticsManager analyticsManager;
    private SalesData currentSalesData;
    private Date startDate;
    private Date endDate;
    private final DecimalFormat currencyFormatter = new DecimalFormat("₹##,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_analytics);

        // Initialize services
        analyticsManager = AnalyticsManager.getInstance();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupCharts();
        setupSummaryCard();
        setupDateControls();
        setupExportButton();

        // Set default date range to today
        setDefaultDateRange();

        // Load analytics data
        loadSalesData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        dateRangeCard = findViewById(R.id.dateRangeCard);
        startDateTextView = findViewById(R.id.startDateTextView);
        endDateTextView = findViewById(R.id.endDateTextView);
        selectDateRangeButton = findViewById(R.id.selectDateRangeButton);
        todayButton = findViewById(R.id.todayButton);
        weekButton = findViewById(R.id.weekButton);
        monthButton = findViewById(R.id.monthButton);
        yearButton = findViewById(R.id.yearButton);
        customRangeButton = findViewById(R.id.customRangeButton);
        exportButton = findViewById(R.id.exportButton);
        revenueChart = findViewById(R.id.revenueChart);
        categoryChart = findViewById(R.id.categoryChart);
        topItemsChart = findViewById(R.id.topItemsChart);
        paymentChart = findViewById(R.id.paymentChart);
        summaryCard = findViewById(R.id.summaryCard);
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        totalOrdersTextView = findViewById(R.id.totalOrdersTextView);
        averageOrderValueTextView = findViewById(R.id.averageOrderTextView);
        peakHourTextView = findViewById(R.id.peakHourTextView);
        customerRetentionTextView = findViewById(R.id.customerRetentionTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sales Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupCharts() {
        setupRevenueChart();
        setupCategoryChart();
        setupTopItemsChart();
        setupPaymentChart();
    }

    private void setupRevenueChart() {
        revenueChart.getDescription().setEnabled(true);
        revenueChart.setTouchEnabled(true);
        revenueChart.setDrawGridBackground(false);
        revenueChart.setDrawBorders(false);
        revenueChart.setPinchZoom(false);
        revenueChart.setDoubleTapToZoomEnabled(false);

        X xAxis = revenueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date((long) value));
            }
        });

        YAxis leftAxis = revenueChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisPosition.START);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return currencyFormatter.format(value);
            }
        });

        YAxis rightAxis = revenueChart.getAxisRight();
        rightAxis.setPosition(YAxis.YAxisPosition.END);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setTextColor(Color.GRAY);
    }

    private void setupCategoryChart() {
        categoryChart.getDescription().setEnabled(true);
        categoryChart.setTouchEnabled(true);
        categoryChart.setDrawGridBackground(false);
        categoryChart.setDrawBorders(false);
        categoryChart.setPinchZoom(false);
        categoryChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = categoryChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setLabelRotationAngle(45f);

        YAxis leftAxis = categoryChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxis.Position.START);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return currencyFormatter.format(value);
            }
        });

        YAxis rightAxis = categoryChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupTopItemsChart() {
        topItemsChart.getDescription().setEnabled(true);
        topItemsChart.setTouchEnabled(true);
        topItemsChart.setDrawGridBackground(false);
        topItemsChart.setDrawBorders(false);
        topItemsChart.setPinchZoom(false);
        topItemsChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = topItemsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxis.Position.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setLabelRotationAngle(45f);

        YAxis leftAxis = topItemsChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxis.Position.START);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return currencyFormatter.format(value);
            }
        });

        YAxis rightAxis = topItemsChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupPaymentChart() {
        paymentChart.getDescription().setEnabled(true);
        paymentChart.setTouchEnabled(true);
        paymentChart.setDrawGridBackground(false);
        paymentChart.setDrawBorders(false);
        paymentChart.setPinchZoom(false);
        paymentChart.setDoubleTapToZoomEnabled(false);

        // Legend setup would go here
    }

    private void setupSummaryCard() {
        // Summary card is already initialized
    }

    private void setupDateControls() {
        todayButton.setOnClickListener(v -> selectToday());
        weekButton.setOnClickListener(v -> selectWeek());
        monthButton.setOnClickListener(v -> selectMonth());
        yearButton.setOnClickListener(v -> selectYear());
        customRangeButton.setOnClickListener(v -> showCustomDatePicker());
        selectDateRangeButton.setOnClickListener(v -> showCustomDatePicker());
        exportButton.setOnClickListener(v -> exportData());
    }

    private void setupExportButton() {
        exportButton.setOnClickListener(v -> {
            showExportOptions();
        });
    }

    private void setDefaultDateRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        endDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -7);
        startDate = calendar.getTime();

        updateDateDisplay();
        loadSalesData();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        startDateTextView.setText(dateFormat.format(startDate));
        endDateTextView.setText(dateFormat.format(endDate));
    }

    private void selectToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        startDate = calendar.getTime();
        endDate = calendar.getTime();

        updateDateDisplay();
        loadSalesData();
    }

    private void selectWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK) - 1);
        startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 6);
        endDate = calendar.getTime();

        updateDateDisplay();
        loadSalesData();
    }

    private void selectMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        endDate = calendar.getTime();

        updateDateDisplay();
        loadSalesData();
    }

    private void selectYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        startDate = calendar.getTime();

        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        endDate = calendar.getTime();

        updateDateDisplay();
        loadSalesData();
    }

    private void showCustomDatePicker() {
        // Start date picker
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.Builder()
                .setSelection(Pair.create(startDate, endDate));

        builder.setTitle("Select Date Range");
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialDatePicker);
        builder.setPositiveButton("OK", (dialog, which) -> {
            startDate = builder.getSelection().first;
            endDate = builder.getSelection().second;
            updateDateDisplay();
            loadSalesData();
        });
        builder.setNegativeButton("Cancel", null);

        MaterialDatePicker datePicker = builder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void loadSalesData() {
        showLoading(true);

        analyticsManager.calculateDailySales(startDate, new AnalyticsManager.AnalyticsCallback<SalesData>() {
            @Override
            public void onSuccess(SalesData salesData) {
                currentSalesData = salesData;
                updateCharts();
                updateSummary();
                showLoading(false);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                showError("Failed to load sales data: " + error);
            }
        });
    }

    private void updateCharts() {
        if (currentSalesData == null) {
            return;
        }

        // Update revenue chart
        updateRevenueChart();

        // Update category chart
        updateCategoryChart();

        // Update top items chart
        updateTopItemsChart();

        // Update payment chart
        updatePaymentChart();
    }

    private void updateRevenueChart() {
        if (currentSalesData == null) return;

        ArrayList<Entry> revenueEntries = new ArrayList<>();
        ArrayList<String> dateLabels = new ArrayList<>();

        // Get hourly sales data
        Map<String, Double> hourlySales = currentSalesData.getHourlySales();
        for (Map.Entry<String, Double> entry : hourlySales.entrySet()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
                Date date = sdf.parse(entry.getKey() + " 00:00");
                revenueEntries.add(new Entry(date.getTime(), entry.getValue()));
                dateLabels.add(entry.getKey());
            } catch (Exception e) {
                // Handle parsing error
            }
        }

        LineDataSet revenueDataSet = new LineDataSet(revenueEntries, "Revenue");
        revenueDataSet.setColor(Color.parseColor("#4CAF50"));
        revenueDataSet.setLineWidth(2f);
        revenueDataSet.setDrawCircles(true);
        revenueDataSet.setDrawValues(true);
        revenueDataSet.setValueTextSize(10f);
        revenueDataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(revenueDataSet);
        revenueChart.setData(lineData);
        revenueChart.animateX(1000);
    }

    private void updateCategoryChart() {
        if (currentSalesData == null) return;

        ArrayList<BarEntry> categoryEntries = new ArrayList<>();
        ArrayList<String> categoryLabels = new ArrayList<>();

        Map<String, Double> categoryRevenue = currentSalesData.getCategoryRevenue();
        for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
            categoryEntries.add(new BarEntry(categoryLabels.size(), entry.getValue()));
            categoryLabels.add(entry.getKey());
        }

        BarDataSet categoryDataSet = new BarDataSet(categoryEntries, "Revenue by Category");
        categoryDataSet.setColor(Color.parseColor("#2196F3"));
        categoryDataSet.setValueTextSize(10f);
        categoryDataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(categoryDataSet);
        categoryChart.setData(barData);
        categoryChart.animateY(1000);
    }

    private void updateTopItemsChart() {
        if (currentSalesData == null) return;

        analyticsManager.getTopRevenueItems(startDate, endDate, 10, new AnalyticsManager.AnalyticsCallback<List<Map.Entry<String, Double>>>() {
            @Override
            public void onSuccess(List<Map.Entry<String, Double>> topItems) {
                if (topItems != null && !topItems.isEmpty()) {
                    ArrayList<BarEntry> itemEntries = new ArrayList<>();
                    ArrayList<String> itemLabels = new ArrayList<>();

                    for (Map.Entry<String, Double> entry : topItems) {
                        itemEntries.add(new BarEntry(itemLabels.size(), entry.getValue()));
                        itemLabels.add(entry.getKey());
                    }

                    BarDataSet topItemsDataSet = new BarDataSet(itemEntries, "Top Selling Items");
                    topItemsDataSet.setColor(Color.parseColor("#FF9800"));
                    topItemsDataSet.setValueTextSize(10f);
                    topItemsDataSet.setValueTextColor(Color.BLACK);

                    BarData topItemsData = new BarData(topItemsDataSet);
                    topItemsChart.setData(topItemsData);
                    topItemsChart.animateY(1000);
                }
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to load top items: " + error);
            }
        });
    }

    private void updatePaymentChart() {
        if (currentSalesData == null) return;

        // Payment breakdown data
        Map<String, Double> paymentBreakdown = currentSalesData.getPaymentBreakdown();

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        colors.add(Color.parseColor("#4CAF50"));  // Cash
        colors.add(Color.parseColor("#2196F3"));  // Credit Card
        colors.add(Color.parseColor("#FF9800"));  // Debit Card
        colors.add(Color.parseColor("#9C27B0"));  // UPI
        colors.add(Color.parseColor("#607D8B"));  // Wallet

        for (Map.Entry<String, Double> entry : paymentBreakdown.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            labels.add(entry.getKey());
        }

        // This would need a PieChart setup
        // For now, we'll update the BarChart to show payment breakdown
        updatePaymentBarChart(pieEntries, labels, colors);
    }

    private void updatePaymentBarChart(List<PieEntry> pieEntries, ArrayList<String> labels, ArrayList<Integer> colors) {
        // Convert pie chart data to bar chart for now
        ArrayList<BarEntry> paymentEntries = new ArrayList<>();
        for (int i = 0; i < pieEntries.size(); i++) {
            paymentEntries.add(new BarEntry(i, pieEntries.get(i).getValue()));
        }

        BarDataSet paymentDataSet = new BarDataSet(paymentEntries, "Payment Methods");
        paymentDataSet.setColors(colors);
        paymentDataSet.setValueTextSize(10f);
        paymentDataSet.setValueTextColor(Color.BLACK);

        BarData paymentData = new BarData(paymentDataSet);
        paymentChart.setData(paymentData);
        paymentChart.animateY(1000);
    }

    private void updateSummary() {
        if (currentSalesData == null) return;

        runOnUiThread(() -> {
            totalRevenueTextView.setText(currencyFormatter.format(currentSalesData.getTotalRevenue()));
            totalOrdersTextView.setText(String.valueOf(currentSalesData.getTotalOrders()));
            averageOrderValueTextView.setText(currencyFormatter.format(currentSalesData.getAverageOrderValue()));
            peakHourTextView.setText(currentSalesData.getPeakHour());
            customerRetentionTextView.setText(String.format("%.1f%%", currentSalesData.getRepeatRate()));
        });
    }

    private void showLoading(boolean show) {
        // Show loading indicator (would need to add to layout)
    }

    private void showExportOptions() {
        String[] exportOptions = {
                "Export as CSV",
                "Export as JSON",
                "Export to PDF",
                "Share Report",
                "Print Report"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Options")
                .setItems(exportOptions, (dialog, which) -> {
                    String option = exportOptions[which];
                    handleExport(option);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleExport(String option) {
        if (currentSalesData == null) {
            showError("No data to export");
            return;
        }

        switch (option) {
            case "Export as CSV":
                exportToCSV();
                break;
            case "Export as JSON":
                exportToJSON();
                break;
            case "Export to PDF":
                exportToPDF();
                break;
            case "Share Report":
                shareReport();
                break;
            case "Print Report":
                printReport();
                break;
        }
    }

    private void exportToCSV() {
        Map<String, Object> reportData = createReportData();
        analyticsManager.exportToCSV(reportData, new AnalyticsManager.AnalyticsCallback<String>() {
            @Override
            public void onSuccess(String csvData) {
                // Save to file or share
                showExportSuccess("CSV", csvData);
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to export CSV: " + error);
            }
        });
    }

    private void exportToJSON() {
        Map<String, Object> reportData = createReportData();
        analyticsManager.exportToJSON(reportData, new AnalyticsManager.AnalyticsCallback<String>() {
            @Override
            public void onSuccess(String jsonData) {
                showExportSuccess("JSON", jsonData);
            }

            @Override
            public void onFailure(String error) {
                showError("Failed to export JSON: " + error);
            }
        });
    }

    private void exportToPDF() {
        // PDF export would require additional libraries
        showError("PDF export not available");
    }

    private void shareReport() {
        Map<String, Object> reportData = createReportData();
        StringBuilder reportText = createReportText(reportData);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sales Report - " +
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(startDate) + " to " +
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(endDate));
        startActivity(Intent.createChooser(shareIntent, "Share Sales Report"));
    }

    private void printReport() {
        // Print functionality would require additional libraries
        showError("Print not available");
    }

    private Map<String, Object> createReportData() {
        Map<String, Object> reportData = new HashMap<>();

        if (currentSalesData != null) {
            reportData.put("period", startDate + " to " + endDate);
            reportData.put("totalRevenue", currentSalesData.getTotalRevenue());
            reportData.put("totalOrders", currentSalesData.getTotalOrders());
            reportData.put("averageOrderValue", currentSalesData.getAverageOrderValue());
            reportData.put("peakHour", currentSalesData.getPeakHour());
            reportData.put("customerRetentionRate", currentSalesData.getRepeatRate());
            reportData.put("activeCustomers", currentSalesData.getUniqueCustomers());
            reportData.put("newCustomers", currentSalesData.getNewCustomers());
            reportData.put("returningCustomers", currentSalesData.getReturningCustomers());
            reportData.put("lowStockAlerts", currentSalesData.getLowStockAlerts());
            reportData.put("outOfStockItems", currentSalesData.getOutOfStockItems());
            reportData.put("complaints", currentSalesData.getComplaints());
            reportData.put("totalReviews", currentSalesData.getTotalReviews());
            reportData.put("averageRating", currentSalesData.getAverageRating());
            reportData.put("customerSatisfactionScore", currentSalesData.getCustomerSatisfactionScore()));
            reportData.put("itemPerformance", currentSalesData.getItemPerformance());
            reportData.put("categoryPerformance", currentSalesData.getCategoryPerformance());
            reportData.put("hourlySales", currentSalesData.getHourlySales());
            reportData.put("dailySales", currentSalesData.getDailySales());
            reportData.put("weeklySales", currentSalesData.getWeeklySales());
            reportData.put("monthlySales", currentSalesData.getMonthlySales());
        }

        return reportData;
    }

    private String createReportText(Map<String, Object> reportData) {
        StringBuilder reportText = new StringBuilder();
        reportText.append("SALES REPORT\n");
        reportText.append("Period: ").append(reportData.get("period")).append("\n");
        reportText.append("Generated: ").append(new Date()).append("\n\n");
        reportText.append("SUMMARY:\n");
        reportText.append("Total Revenue: ").append(reportData.get("totalRevenue")).append("\n");
        reportText.append("Total Orders: ").append(reportData.get("totalOrders")).append("\n");
        reportText.append("Average Order Value: ").append(reportData.get("averageOrderValue")).append("\n");
        reportText.append("Peak Hour: ").append(reportData.get("peakHour")).append("\n");
        reportText.append("Customer Retention: ").append(reportData.get("customerRetentionRate")).append("%\n\n");

        reportText.append("CUSTOMER METRICS:\n");
        reportText.append("Active Customers: ").append(reportData.get("activeCustomers")).append("\n");
        reportText.append("New Customers: ").append(reportData.get("newCustomers")).append("\n");
        reportText.append("Returning Customers: ").append(reportData.get("returningCustomers")).append("\n\n");

        return reportText.toString();
    }

    private void showExportSuccess(String format, String data) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Successful")
                .setMessage("Report exported successfully as " + format.toUpperCase() + " format")
                .setPositiveButton("OK", null)
                .setNeutralButton("Share", (dialog, which) -> {
                    // Share functionality
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, data);
                    startActivity(Intent.createChooser(shareIntent, "Share Report"));
                })
                .show();
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.analytics_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadSalesData();
            return true;
        } else if (itemId == R.id.action_export) {
            showExportOptions();
            return true;
        } else if (itemId == R.id.action_share) {
            shareReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}