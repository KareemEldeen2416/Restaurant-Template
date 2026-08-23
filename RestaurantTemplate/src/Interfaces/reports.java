package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

/**
 * Controller for the Reports and Analytics Dashboard.
 * Features:
 * - Live Arabic clock and date in the top bar.
 * - Key Performance Indicators (KPI) tiles.
 * - Daily Revenue Bar Chart (Revenue per day).
 * - Monthly Revenue Line Chart (Revenue per month).
 * - Table 1: Most sold products ordered descendingly.
 * - Table 2: Products of the highest revenue.
 * - Table 3: Most purchasing customers (Customer Name, Money Spent, Orders Count).
 * - Period selector filter and Print/Export report.
 * 
 * @author KareemEldeen
 */
public class reports implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML KPI Summary Cards
    // =========================================================================
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblTopProduct;
    @FXML private Label lblTopProductSales;
    @FXML private Label lblTopCustomer;
    @FXML private Label lblTopCustomerSpent;

    // =========================================================================
    // FXML Charts (Daily & Monthly Revenue)
    // =========================================================================
    @FXML private BarChart<String, Number> chartDailyRevenue;
    @FXML private CategoryAxis axisDailyDays;
    @FXML private NumberAxis axisDailyRevenue;

    @FXML private LineChart<String, Number> chartMonthlyRevenue;
    @FXML private CategoryAxis axisMonthlyMonths;
    @FXML private NumberAxis axisMonthlyRevenue;

    // =========================================================================
    // FXML Filter & Action Controls
    // =========================================================================
    @FXML private JFXComboBox<String> cbPeriod;
    @FXML private JFXButton btnRefresh;
    @FXML private JFXButton btnExport;

    // =========================================================================
    // FXML Table 1: Most Sold Products (Ordered Descendingly)
    // =========================================================================
    @FXML private TableView<MostSoldProductModel> tblMostSold;
    @FXML private TableColumn<MostSoldProductModel, Number> colSoldRank;
    @FXML private TableColumn<MostSoldProductModel, String> colSoldName;
    @FXML private TableColumn<MostSoldProductModel, String> colSoldCategory;
    @FXML private TableColumn<MostSoldProductModel, String> colSoldQty;
    @FXML private TableColumn<MostSoldProductModel, String> colSoldShare;
    @FXML private TableColumn<MostSoldProductModel, String> colSoldTotalRevenue;

    // =========================================================================
    // FXML Table 2: Products of Highest Revenue
    // =========================================================================
    @FXML private TableView<ProductRevenueModel> tblHighestRevenue;
    @FXML private TableColumn<ProductRevenueModel, Number> colRevRank;
    @FXML private TableColumn<ProductRevenueModel, String> colRevName;
    @FXML private TableColumn<ProductRevenueModel, String> colRevCategory;
    @FXML private TableColumn<ProductRevenueModel, String> colRevPrice;
    @FXML private TableColumn<ProductRevenueModel, String> colRevQty;
    @FXML private TableColumn<ProductRevenueModel, String> colRevTotal;

    // =========================================================================
    // FXML Table 3: Most Purchasing Customers
    // =========================================================================
    @FXML private TableView<CustomerSpentModel> tblTopCustomers;
    @FXML private TableColumn<CustomerSpentModel, Number> colCustRank;
    @FXML private TableColumn<CustomerSpentModel, String> colCustName;
    @FXML private TableColumn<CustomerSpentModel, String> colCustSpent;
    @FXML private TableColumn<CustomerSpentModel, String> colCustOrders;
    @FXML private TableColumn<CustomerSpentModel, String> colCustPhone;
    @FXML private TableColumn<CustomerSpentModel, String> colCustLastOrder;

    // =========================================================================
    // Data Collections & Timers
    // =========================================================================
    private final ObservableList<MostSoldProductModel> mostSoldList = FXCollections.observableArrayList();
    private final ObservableList<ProductRevenueModel> highestRevenueList = FXCollections.observableArrayList();
    private final ObservableList<CustomerSpentModel> topCustomersList = FXCollections.observableArrayList();

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initPeriodFilter();
        initTableColumns();
        loadReportsData();
        populateDailyChart();
        populateMonthlyChart();
    }

    /**
     * Initializes the live counting clock and date in Arabic.
     */
    private void initLiveDateTime() {
        updateDateTimeDisplay();

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateDateTimeDisplay();
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void updateDateTimeDisplay() {
        LocalDateTime now = LocalDateTime.now();
        if (lblDate != null) {
            lblDate.setText(now.format(dateFormatter));
        }
        if (lblTime != null) {
            lblTime.setText(now.format(timeFormatter));
        }
    }

    /**
     * Initializes the period combo box.
     */
    private void initPeriodFilter() {
        if (cbPeriod != null) {
            cbPeriod.setItems(FXCollections.observableArrayList(
                "اليوم الحالي",
                "الأسبوع الحالي",
                "الشهر الحالي",
                "الربع سنوي (٣ أشهر)",
                "العام الحالي (2026)"
            ));
            cbPeriod.setValue("الشهر الحالي");
        }
    }

    /**
     * Maps table columns to model properties.
     */
    private void initTableColumns() {
        // Table 1: Most Sold Products
        if (colSoldRank != null) colSoldRank.setCellValueFactory(cell -> cell.getValue().rankProperty());
        if (colSoldName != null) colSoldName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        if (colSoldCategory != null) colSoldCategory.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        if (colSoldQty != null) colSoldQty.setCellValueFactory(cell -> cell.getValue().quantitySoldProperty());
        if (colSoldShare != null) colSoldShare.setCellValueFactory(cell -> cell.getValue().salesShareProperty());
        if (colSoldTotalRevenue != null) colSoldTotalRevenue.setCellValueFactory(cell -> cell.getValue().totalRevenueProperty());
        if (tblMostSold != null) tblMostSold.setItems(mostSoldList);

        // Table 2: Highest Revenue Products
        if (colRevRank != null) colRevRank.setCellValueFactory(cell -> cell.getValue().rankProperty());
        if (colRevName != null) colRevName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        if (colRevCategory != null) colRevCategory.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        if (colRevPrice != null) colRevPrice.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());
        if (colRevQty != null) colRevQty.setCellValueFactory(cell -> cell.getValue().quantitySoldProperty());
        if (colRevTotal != null) colRevTotal.setCellValueFactory(cell -> cell.getValue().totalRevenueProperty());
        if (tblHighestRevenue != null) tblHighestRevenue.setItems(highestRevenueList);

        // Table 3: Most Purchasing Customers
        if (colCustRank != null) colCustRank.setCellValueFactory(cell -> cell.getValue().rankProperty());
        if (colCustName != null) colCustName.setCellValueFactory(cell -> cell.getValue().customerNameProperty());
        if (colCustSpent != null) colCustSpent.setCellValueFactory(cell -> cell.getValue().moneySpentProperty());
        if (colCustOrders != null) colCustOrders.setCellValueFactory(cell -> cell.getValue().ordersCountProperty());
        if (colCustPhone != null) colCustPhone.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        if (colCustLastOrder != null) colCustLastOrder.setCellValueFactory(cell -> cell.getValue().lastOrderDateProperty());
        if (tblTopCustomers != null) tblTopCustomers.setItems(topCustomersList);
    }

    /**
     * Loads structured mock report data into tables and summary KPIs.
     */
    private void loadReportsData() {
        mostSoldList.clear();
        highestRevenueList.clear();
        topCustomersList.clear();

        // 1. Most Sold Products (Ordered Descendingly by Quantity)
        mostSoldList.add(new MostSoldProductModel(1, "برجر ميكس لحم دوبل", "سندوتشات اللحم", "380 قطعة", "24.5%", "57,000 ج.م"));
        mostSoldList.add(new MostSoldProductModel(2, "بيتزا شاورما فراخ سوبريم", "البيتزا الإيطالية", "315 قطعة", "20.3%", "55,125 ج.م"));
        mostSoldList.add(new MostSoldProductModel(3, "وجبة زنجر كرسبي عائلي", "وجبات الدجاج", "290 قطعة", "18.7%", "60,900 ج.م"));
        mostSoldList.add(new MostSoldProductModel(4, "ميكس جريل مشويات مشكل", "المشويات والباربيكيو", "245 قطعة", "15.8%", "78,400 ج.م"));
        mostSoldList.add(new MostSoldProductModel(5, "كريب كرانشي دجاج حار", "الكريب والسناكس", "210 قطعة", "13.5%", "23,100 ج.م"));
        mostSoldList.add(new MostSoldProductModel(6, "باستا ألفريدو دجاج ومشروم", "المكرونة والباستا", "185 قطعة", "11.9%", "24,050 ج.م"));
        mostSoldList.add(new MostSoldProductModel(7, "عصير برتقال فريش طبيعي", "المشروبات والعصائر", "160 قطعة", "10.3%", "8,000 ج.م"));

        // 2. Products of Highest Revenue (Ordered Descendingly by Total Revenue)
        highestRevenueList.add(new ProductRevenueModel(1, "ميكس جريل مشويات مشكل", "المشويات والباربيكيو", "320 ج.م", "245", "78,400 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(2, "وجبة زنجر كرسبي عائلي", "وجبات الدجاج", "210 ج.م", "290", "60,900 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(3, "برجر ميكس لحم دوبل", "سندوتشات اللحم", "150 ج.م", "380", "57,000 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(4, "بيتزا شاورما فراخ سوبريم", "البيتزا الإيطالية", "175 ج.م", "315", "55,125 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(5, "تورتة شوكولاتة نوتيلا فاخرة", "الحلويات الشرقية والغربية", "400 ج.م", "95", "38,000 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(6, "باستا ألفريدو دجاج ومشروم", "المكرونة والباستا", "130 ج.م", "185", "24,050 ج.م"));
        highestRevenueList.add(new ProductRevenueModel(7, "كريب كرانشي دجاج حار", "الكريب والسناكس", "110 ج.م", "210", "23,100 ج.م"));

        // 3. Most Purchasing Customers (Contains Customer Name, Money Spent, etc.)
        topCustomersList.add(new CustomerSpentModel(1, "محمد طارق الأحمدي", "8,650 ج.م", "18 طلب", "01099887766", "2026-08-22"));
        topCustomersList.add(new CustomerSpentModel(2, "نورهان علاء الدين", "6,800 ج.م", "14 طلب", "01233445566", "2026-08-21"));
        topCustomersList.add(new CustomerSpentModel(3, "ياسمين شريف سامي", "5,920 ج.م", "11 طلب", "01544332211", "2026-08-23"));
        topCustomersList.add(new CustomerSpentModel(4, "مروان خالد عبد العزيز", "4,450 ج.م", "9 طلبات", "01155667788", "2026-08-20"));
        topCustomersList.add(new CustomerSpentModel(5, "إيهاب فاروق النجار", "3,700 ج.م", "7 طلبات", "01011223344", "2026-08-19"));
        topCustomersList.add(new CustomerSpentModel(6, "سلوى محمود رشدي", "3,150 ج.م", "6 طلبات", "01288776655", "2026-08-18"));
    }

    /**
     * Populates Daily Revenue Bar Chart (Revenue per day).
     */
    private void populateDailyChart() {
        if (chartDailyRevenue == null) return;
        chartDailyRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("الإيراد اليومي (ج.م)");

        series.getData().add(new XYChart.Data<>("السبت", 18500));
        series.getData().add(new XYChart.Data<>("الأحد", 14200));
        series.getData().add(new XYChart.Data<>("الاثنين", 16800));
        series.getData().add(new XYChart.Data<>("الثلاثاء", 19400));
        series.getData().add(new XYChart.Data<>("الأربعاء", 22100));
        series.getData().add(new XYChart.Data<>("الخميس", 29600));
        series.getData().add(new XYChart.Data<>("الجمعة", 27900));

        chartDailyRevenue.getData().add(series);
    }

    /**
     * Populates Monthly Revenue Line Chart (Revenue per month).
     */
    private void populateMonthlyChart() {
        if (chartMonthlyRevenue == null) return;
        chartMonthlyRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("الإيراد الشهري (ج.م)");

        series.getData().add(new XYChart.Data<>("يناير", 95000));
        series.getData().add(new XYChart.Data<>("فبراير", 108000));
        series.getData().add(new XYChart.Data<>("مارس", 125000));
        series.getData().add(new XYChart.Data<>("أبريل", 140000));
        series.getData().add(new XYChart.Data<>("مايو", 132000));
        series.getData().add(new XYChart.Data<>("يونيو", 155000));
        series.getData().add(new XYChart.Data<>("يوليو", 168000));
        series.getData().add(new XYChart.Data<>("أغسطس", 148500));

        chartMonthlyRevenue.getData().add(series);
    }

    // =========================================================================
    // Filter & Action Handlers
    // =========================================================================

    @FXML
    private void handlePeriodChange(ActionEvent event) {
        String period = cbPeriod != null ? cbPeriod.getValue() : "الشهر الحالي";
        // Refresh charts & tables based on period
        populateDailyChart();
        populateMonthlyChart();
        loadReportsData();
    }

    @FXML
    private void handleRefreshReports(ActionEvent event) {
        populateDailyChart();
        populateMonthlyChart();
        loadReportsData();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("تحديث الإحصائيات");
        alert.setHeaderText("تم تحديث التقارير بنجاح");
        alert.setContentText("تمت إعادة احتساب إجمالي الإيرادات والمبيعات والعملاء الأكثر شراءً حتى تاريخ اليوم.");
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    @FXML
    private void handleExportReport(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("طباعة التقرير");
        alert.setHeaderText("جاهز للطباعة والتصدير");
        alert.setContentText("تم تجهيز التقرير الإحصائي الشامل للمطعم.\nيمكنك الآن إرسال الملف إلى طابعة الفواتير والتقارير أو الحفظ كملف PDF.");
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    private void styleDialog(DialogPane pane) {
        try {
            URL css = getClass().getResource("/Interfaces/style.css");
            if (css != null) {
                pane.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception ignored) {
        }
    }

    // =========================================================================
    // Model 1: Most Sold Product Model
    // =========================================================================
    public static class MostSoldProductModel {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty quantitySold;
        private final SimpleStringProperty salesShare;
        private final SimpleStringProperty totalRevenue;

        public MostSoldProductModel(int rank, String name, String category, String quantitySold, String salesShare, String totalRevenue) {
            this.rank = new SimpleIntegerProperty(rank);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.quantitySold = new SimpleStringProperty(quantitySold);
            this.salesShare = new SimpleStringProperty(salesShare);
            this.totalRevenue = new SimpleStringProperty(totalRevenue);
        }

        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty quantitySoldProperty() { return quantitySold; }
        public SimpleStringProperty salesShareProperty() { return salesShare; }
        public SimpleStringProperty totalRevenueProperty() { return totalRevenue; }
    }

    // =========================================================================
    // Model 2: Product Revenue Model
    // =========================================================================
    public static class ProductRevenueModel {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty unitPrice;
        private final SimpleStringProperty quantitySold;
        private final SimpleStringProperty totalRevenue;

        public ProductRevenueModel(int rank, String name, String category, String unitPrice, String quantitySold, String totalRevenue) {
            this.rank = new SimpleIntegerProperty(rank);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.unitPrice = new SimpleStringProperty(unitPrice);
            this.quantitySold = new SimpleStringProperty(quantitySold);
            this.totalRevenue = new SimpleStringProperty(totalRevenue);
        }

        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty unitPriceProperty() { return unitPrice; }
        public SimpleStringProperty quantitySoldProperty() { return quantitySold; }
        public SimpleStringProperty totalRevenueProperty() { return totalRevenue; }
    }

    // =========================================================================
    // Model 3: Customer Spent Model
    // =========================================================================
    public static class CustomerSpentModel {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty customerName;
        private final SimpleStringProperty moneySpent;
        private final SimpleStringProperty ordersCount;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty lastOrderDate;

        public CustomerSpentModel(int rank, String customerName, String moneySpent, String ordersCount, String phone, String lastOrderDate) {
            this.rank = new SimpleIntegerProperty(rank);
            this.customerName = new SimpleStringProperty(customerName);
            this.moneySpent = new SimpleStringProperty(moneySpent);
            this.ordersCount = new SimpleStringProperty(ordersCount);
            this.phone = new SimpleStringProperty(phone);
            this.lastOrderDate = new SimpleStringProperty(lastOrderDate);
        }

        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty customerNameProperty() { return customerName; }
        public SimpleStringProperty moneySpentProperty() { return moneySpent; }
        public SimpleStringProperty ordersCountProperty() { return ordersCount; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty lastOrderDateProperty() { return lastOrderDate; }
    }
}
