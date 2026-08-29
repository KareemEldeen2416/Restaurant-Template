package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Controller for the Cashier and Point of Sale (POS) Interface.
 * Features:
 * - 3 Tabs: Menu & Cart, Orders Log, and Tables Floor Status.
 * - Clicking any menu item adds it to the cart list on the right.
 * - 3 Main Actions on Cart: Assign to Table, Cancel, Pay.
 * - Orders Log categorized by Open, Running, Closed, and All.
 * - Tables tab displaying Empty, Assigned to Order, or Reserved status.
 * 
 * @author KareemEldeen
 */
public class cashier implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private TabPane tabPaneCashier;

    // =========================================================================
    // FXML Tab 1: Menu & POS Cart
    // =========================================================================
    @FXML private HBox pnlCategoryButtons;
    @FXML private JFXTextField txtMenuSearch;
    @FXML private FlowPane pnlMenuItems;

    @FXML private Label lblCurrentOrderNum;
    @FXML private JFXComboBox<String> cbOrderTable;
    @FXML private JFXTextField txtOrderCustName;
    @FXML private JFXButton btnClearCart;

    @FXML private TableView<CartItemModel> tblCart;
    @FXML private TableColumn<CartItemModel, String> colCartItem;
    @FXML private TableColumn<CartItemModel, Number> colCartQty;
    @FXML private TableColumn<CartItemModel, String> colCartPrice;
    @FXML private TableColumn<CartItemModel, String> colCartTotal;

    @FXML private Label lblSubTotal;
    @FXML private Label lblTax;
    @FXML private Label lblGrandTotal;

    @FXML private JFXButton btnAssignTable;
    @FXML private JFXButton btnCancelOrder;
    @FXML private JFXButton btnPayOrder;

    // =========================================================================
    // FXML Tab 2: Orders
    // =========================================================================
    @FXML private JFXButton btnFilterAll;
    @FXML private JFXButton btnFilterOpen;
    @FXML private JFXButton btnFilterRunning;
    @FXML private JFXButton btnFilterClosed;
    @FXML private JFXTextField txtOrdersSearch;
    @FXML private Label lblOrdersCount;

    @FXML private TableView<OrderModel> tblOrders;
    @FXML private TableColumn<OrderModel, String> colOrderId;
    @FXML private TableColumn<OrderModel, String> colOrderCust;
    @FXML private TableColumn<OrderModel, String> colOrderItems;
    @FXML private TableColumn<OrderModel, String> colOrderTable;
    @FXML private TableColumn<OrderModel, String> colOrderTotal;
    @FXML private TableColumn<OrderModel, String> colOrderTime;
    @FXML private TableColumn<OrderModel, String> colOrderStatus;

    // =========================================================================
    // FXML Tab 3: Tables
    // =========================================================================
    @FXML private Label lblTablesEmpty;
    @FXML private Label lblTablesAssigned;
    @FXML private Label lblTablesReserved;
    @FXML private FlowPane pnlCashierTables;

    // =========================================================================
    // Data Collections
    // =========================================================================
    private final List<MenuItemModel> allMenuItems = new ArrayList<>();
    private final ObservableList<CartItemModel> cartList = FXCollections.observableArrayList();
    private final ObservableList<OrderModel> ordersList = FXCollections.observableArrayList();
    private FilteredList<OrderModel> filteredOrdersList;

    private final List<CashierTableModel> cashierTables = new ArrayList<>();

    private int orderCounter = 501;
    private String selectedCategory = "الكل";
    private String currentOrderFilter = "ALL";

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        initTableControls();
        loadInitialData();
        renderCategoryButtons();
        renderMenuItems("");
        renderCashierTableCards();
        setupSearchListeners();
    }

    private void initUserSessionDisplay() {
        if (lblUserName != null) {
            lblUserName.setText(UserSession.getDisplayName());
        }
        if (lblUserRole != null) {
            lblUserRole.setText(UserSession.getUserRole());
        }
    }

    /**
     * Live Arabic clock & date.
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
        if (lblDate != null) lblDate.setText(now.format(dateFormatter));
        if (lblTime != null) lblTime.setText(now.format(timeFormatter));
    }

    /**
     * Initializes Cart and Orders TableViews.
     */
    private void initTableControls() {
        // Cart TableView
        if (colCartItem != null) colCartItem.setCellValueFactory(c -> c.getValue().nameProperty());
        if (colCartQty != null) colCartQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        if (colCartPrice != null) colCartPrice.setCellValueFactory(c -> c.getValue().priceProperty());
        if (colCartTotal != null) colCartTotal.setCellValueFactory(c -> c.getValue().totalProperty());
        if (tblCart != null) tblCart.setItems(cartList);

        // Orders TableView
        if (colOrderId != null) colOrderId.setCellValueFactory(c -> c.getValue().orderIdProperty());
        if (colOrderCust != null) colOrderCust.setCellValueFactory(c -> c.getValue().customerNameProperty());
        if (colOrderItems != null) colOrderItems.setCellValueFactory(c -> c.getValue().itemsSummaryProperty());
        if (colOrderTable != null) colOrderTable.setCellValueFactory(c -> c.getValue().tableNoProperty());
        if (colOrderTotal != null) colOrderTotal.setCellValueFactory(c -> c.getValue().totalAmountProperty());
        if (colOrderTime != null) colOrderTime.setCellValueFactory(c -> c.getValue().timeStrProperty());
        if (colOrderStatus != null) colOrderStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        filteredOrdersList = new FilteredList<>(ordersList, p -> true);
        if (tblOrders != null) tblOrders.setItems(filteredOrdersList);

        // Table options for cart
        if (cbOrderTable != null) {
            ObservableList<String> tables = FXCollections.observableArrayList("تيك أواي (Takeaway)", "دليفري (Delivery)");
            for (int i = 1; i <= 12; i++) {
                tables.add("طاولة " + i);
            }
            cbOrderTable.setItems(tables);
            cbOrderTable.setValue("طاولة 1");
        }
    }

    /**
     * Loads initial menu items, tables, and existing sample orders.
     */
    private void loadInitialData() {
        // 1. Menu Items
        allMenuItems.add(new MenuItemModel("برجر ميكس لحم دوبل", "سندوتشات", 150.0, "🍔"));
        allMenuItems.add(new MenuItemModel("برجر تشيز مشروم فاخر", "سندوتشات", 135.0, "🍔"));
        allMenuItems.add(new MenuItemModel("سندوتش زنجر سوبريم", "سندوتشات", 110.0, "🥪"));
        allMenuItems.add(new MenuItemModel("بيتزا شاورما فراخ", "بيتزا", 175.0, "🍕"));
        allMenuItems.add(new MenuItemModel("بيتزا كواترو مارجريتا", "بيتزا", 140.0, "🍕"));
        allMenuItems.add(new MenuItemModel("وجبة ميكس جريل مشويات", "وجبات", 320.0, "🥩"));
        allMenuItems.add(new MenuItemModel("وجبة زنجر كرسبي عائلي", "وجبات", 210.0, "🍗"));
        allMenuItems.add(new MenuItemModel("باستا ألفريدو دجاج", "مكرونة", 130.0, "🍝"));
        allMenuItems.add(new MenuItemModel("باستا ريد صوص بولونيز", "مكرونة", 125.0, "🍝"));
        allMenuItems.add(new MenuItemModel("بطاطس كرسبي مع صوص جبن", "سناكس", 45.0, "🍟"));
        allMenuItems.add(new MenuItemModel("كريب كرانشي حار", "سناكس", 95.0, "🌯"));
        allMenuItems.add(new MenuItemModel("عصير برتقال فريش طبيعي", "مشروبات", 50.0, "🍹"));
        allMenuItems.add(new MenuItemModel("موهيتو رمان ونعناع", "مشروبات", 55.0, "🍸"));
        allMenuItems.add(new MenuItemModel("مولتن كيك شوكولاتة ساخنة", "حلويات", 85.0, "🍰"));
        allMenuItems.add(new MenuItemModel("تشيز كيك لوتس فاخر", "حلويات", 90.0, "🍰"));

        // 2. Tables Floor Map
        int[] tableChairs = {2, 4, 4, 6, 2, 4, 8, 6, 4, 2, 8, 10};
        for (int i = 1; i <= 12; i++) {
            cashierTables.add(new CashierTableModel(i, tableChairs[i - 1]));
        }

        // Setup mock table statuses
        cashierTables.get(1).setState(TableState.ASSIGNED, "#ORD-498", "محمد طارق", "435.00 ج.م");
        cashierTables.get(3).setState(TableState.ASSIGNED, "#ORD-499", "أحمد الشناوي", "320.00 ج.م");
        cashierTables.get(6).setState(TableState.RESERVED, "", "مروان خالد (حجز 09:00 م)", "");
        cashierTables.get(10).setState(TableState.RESERVED, "", "ياسمين سامي (حجز 08:30 م)", "");

        // 3. Existing Sample Orders
        ordersList.add(new OrderModel("#ORD-498", "محمد طارق", "2x برجر دوبل، 1x باستا ألفريدو", "طاولة 2", "435.00 ج.م", "07:30 م", "جاري التنفيذ (Running)"));
        ordersList.add(new OrderModel("#ORD-499", "أحمد الشناوي", "1x وجبة ميكس جريل", "طاولة 4", "320.00 ج.م", "07:45 م", "مفتوح (Open)"));
        ordersList.add(new OrderModel("#ORD-500", "نورهان علاء", "1x بيتزا شاورما، 2x موهيتو", "طاولة 7", "285.00 ج.م", "08:10 م", "مكتمل ومدفوع (Closed)"));

        updateTableBadges();
        updateOrderCount();
        updateCurrentOrderLabel();
    }

    /**
     * Renders category filtering pills in Tab 1.
     */
    private void renderCategoryButtons() {
        if (pnlCategoryButtons == null) return;
        pnlCategoryButtons.getChildren().clear();

        String[] categories = {"الكل", "سندوتشات", "بيتزا", "وجبات", "مكرونة", "سناكس", "مشروبات", "حلويات"};
        for (String cat : categories) {
            JFXButton btn = new JFXButton(cat);
            btn.getStyleClass().add(cat.equals(selectedCategory) ? "btn-action-add" : "btn-action-clear");
            btn.setOnAction(e -> {
                selectedCategory = cat;
                renderCategoryButtons();
                renderMenuItems(txtMenuSearch != null ? txtMenuSearch.getText() : "");
            });
            pnlCategoryButtons.getChildren().add(btn);
        }
    }

    /**
     * Renders food and beverage item cards in Tab 1.
     */
    private void renderMenuItems(String search) {
        if (pnlMenuItems == null) return;
        pnlMenuItems.getChildren().clear();

        String query = search != null ? search.trim().toLowerCase() : "";

        for (MenuItemModel item : allMenuItems) {
            if (!selectedCategory.equals("الكل") && !item.getCategory().equals(selectedCategory)) {
                continue;
            }
            if (!query.isEmpty() && !item.getName().toLowerCase().contains(query)) {
                continue;
            }

            VBox card = new VBox(6.0);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("pos-menu-card");

            Label lblIcon = new Label(item.getIcon());
            lblIcon.setStyle("-fx-font-size: 28px;");

            Label lblTitle = new Label(item.getName());
            lblTitle.getStyleClass().add("pos-menu-title");

            Label lblCat = new Label(item.getCategory());
            lblCat.getStyleClass().add("pos-menu-category");

            Label lblPrice = new Label(String.format(Locale.US, "%,.2f ج.م", item.getPrice()));
            lblPrice.getStyleClass().add("pos-menu-price");

            card.getChildren().addAll(lblIcon, lblTitle, lblCat, lblPrice);

            // Click Handler: Adds once to Cart
            card.setOnMouseClicked(e -> {
                addItemToCart(item);
            });

            pnlMenuItems.getChildren().add(card);
        }
    }

    /**
     * Adds an item to the ordered items cart.
     */
    private void addItemToCart(MenuItemModel item) {
        for (CartItemModel cartItem : cartList) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartItem.setTotal(String.format(Locale.US, "%,.2f ج.م", (cartItem.getQuantity() * item.getPrice())));
                if (tblCart != null) tblCart.refresh();
                calculateCartTotals();
                return;
            }
        }

        // New Item
        CartItemModel newItem = new CartItemModel(item.getName(), 1, String.format(Locale.US, "%,.2f ج.م", item.getPrice()), String.format(Locale.US, "%,.2f ج.م", item.getPrice()), item.getPrice());
        cartList.add(newItem);
        calculateCartTotals();
    }

    /**
     * Computes Subtotal, VAT (14%), and Grand Total for active cart.
     */
    private void calculateCartTotals() {
        double subtotal = 0;
        for (CartItemModel item : cartList) {
            subtotal += item.getRawPrice() * item.getQuantity();
        }
        double tax = subtotal * 0.14;
        double grandTotal = subtotal + tax;

        if (lblSubTotal != null) lblSubTotal.setText(String.format(Locale.US, "%,.2f ج.م", subtotal));
        if (lblTax != null) lblTax.setText(String.format(Locale.US, "%,.2f ج.م", tax));
        if (lblGrandTotal != null) lblGrandTotal.setText(String.format(Locale.US, "%,.2f ج.م", grandTotal));
    }

    /**
     * Renders visual table cards in Tab 3 (Tables Floor Plan).
     */
    private void renderCashierTableCards() {
        if (pnlCashierTables == null) return;
        pnlCashierTables.getChildren().clear();

        for (CashierTableModel table : cashierTables) {
            VBox card = new VBox(8.0);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("table-card-btn");
            card.setPrefWidth(240.0);
            card.setMinWidth(220.0);
            card.setMinHeight(140.0);

            HBox header = new HBox(8.0);
            header.setAlignment(Pos.CENTER_LEFT);

            SVGPath tableIcon = new SVGPath();
            tableIcon.setContent("M4 6h16v2H4zm14 3h-2v9h2zm-12 0H4v9h2zm4 0h4v9h-4z");
            tableIcon.setScaleX(0.95);
            tableIcon.setScaleY(0.95);

            Label lblTable = new Label("طاولة " + table.getTableNo());
            lblTable.getStyleClass().add("table-title-text");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblStatus = new Label();
            if (table.getState() == TableState.EMPTY) {
                card.getStyleClass().add("table-card-available");
                tableIcon.setFill(Color.web("#2E7D32"));
                lblStatus.setText("فارغة ومتاحة");
                lblStatus.getStyleClass().add("badge-available");
            } else if (table.getState() == TableState.ASSIGNED) {
                card.getStyleClass().add("table-card-assigned");
                tableIcon.setFill(Color.web("#E65100"));
                lblStatus.setText("مشغولة بطلب");
                lblStatus.getStyleClass().add("badge-assigned");
            } else {
                card.getStyleClass().add("table-card-reserved");
                tableIcon.setFill(Color.web("#D84315"));
                lblStatus.setText("محجوزة");
                lblStatus.getStyleClass().add("badge-reserved");
            }

            header.getChildren().addAll(tableIcon, lblTable, spacer, lblStatus);

            HBox sub = new HBox(6.0);
            sub.setAlignment(Pos.CENTER_RIGHT);
            Label lblChairs = new Label("🪑 سعة " + table.getChairs() + " كراسي");
            lblChairs.getStyleClass().add("table-chairs-text");
            sub.getChildren().add(lblChairs);

            VBox infoBox = new VBox(3.0);
            infoBox.setAlignment(Pos.CENTER_RIGHT);

            if (table.getState() == TableState.ASSIGNED) {
                Label lblOrder = new Label("🧾 طلب: " + table.getActiveOrderId() + " (" + table.getActiveCustomer() + ")");
                lblOrder.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #E65100;");
                Label lblBill = new Label("💰 الحساب: " + table.getActiveTotal());
                lblBill.setStyle("-fx-font-size: 11px; -fx-text-fill: #6D4C41;");
                infoBox.getChildren().addAll(lblOrder, lblBill);
            } else if (table.getState() == TableState.RESERVED) {
                Label lblRes = new Label("👤 " + table.getActiveCustomer());
                lblRes.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #D84315;");
                infoBox.getChildren().add(lblRes);
            } else {
                Label lblFree = new Label("✔ جاهزة لاستقبال طلبات الزبائن");
                lblFree.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32;");
                infoBox.getChildren().add(lblFree);
            }

            card.getChildren().addAll(header, sub, infoBox);

            // Table card selection
            card.setOnMouseClicked(e -> {
                if (cbOrderTable != null) cbOrderTable.setValue("طاولة " + table.getTableNo());
                if (tabPaneCashier != null) tabPaneCashier.getSelectionModel().select(0); // Switch to Menu & Cart
            });

            pnlCashierTables.getChildren().add(card);
        }
    }

    // =========================================================================
    // Cart 3 Action Button Handlers
    // =========================================================================

    /**
     * 1. ASSIGN TO TABLE
     */
    @FXML
    private void handleAssignToTable(ActionEvent event) {
        if (cartList.isEmpty()) {
            showAlert("تنبيه", "سلة الطلب فارغة! يرجى اختيار أصناف من القائمة أولاً.", AlertType.WARNING);
            return;
        }

        String tableStr = cbOrderTable != null ? cbOrderTable.getValue() : "طاولة 1";
        String custName = txtOrderCustName != null && !txtOrderCustName.getText().trim().isEmpty()
                ? txtOrderCustName.getText().trim() : "عميل صالة";
        String orderId = "#ORD-" + orderCounter;
        String grandTotal = lblGrandTotal != null ? lblGrandTotal.getText() : "0.00 ج.م";

        StringBuilder itemsSummary = new StringBuilder();
        for (CartItemModel item : cartList) {
            if (itemsSummary.length() > 0) itemsSummary.append("، ");
            itemsSummary.append(item.getQuantity()).append("x ").append(item.getName());
        }

        // Add to Orders list with status 'Running'
        OrderModel newOrder = new OrderModel(orderId, custName, itemsSummary.toString(), tableStr, grandTotal, LocalDateTime.now().format(timeFormatter), "جاري التنفيذ (Running)");
        ordersList.add(0, newOrder);

        // Update Table status in Floor plan if assigned to a specific table
        if (tableStr.startsWith("طاولة ")) {
            int tIndex = extractTableNumber(tableStr) - 1;
            if (tIndex >= 0 && tIndex < cashierTables.size()) {
                cashierTables.get(tIndex).setState(TableState.ASSIGNED, orderId, custName, grandTotal);
                renderCashierTableCards();
                updateTableBadges();
            }
        }

        orderCounter++;
        updateCurrentOrderLabel();
        updateOrderCount();
        handleClearCart(null);

        showAlert("تم تعيين الطلب", "تم إرسال الطلب (" + orderId + ") بنجاح إلى المطبخ وتعيينه لـ " + tableStr + "!", AlertType.INFORMATION);
    }

    /**
     * 2. CANCEL ORDER
     */
    @FXML
    private void handleCancelOrder(ActionEvent event) {
        if (cartList.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد إلغاء الطلب");
        confirm.setHeaderText("إلغاء الطلب الحالي وتفريغ السلة");
        confirm.setContentText("هل أنت متأكد من رغبتك في إلغاء هذا الطلب بالكامل؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleClearCart(null);
        }
    }

    /**
     * 3. PAY ORDER
     */
    @FXML
    private void handlePayOrder(ActionEvent event) {
        if (cartList.isEmpty()) {
            showAlert("تنبيه", "سلة الطلب فارغة! يرجى اختيار أصناف من القائمة للدفع.", AlertType.WARNING);
            return;
        }

        String tableStr = cbOrderTable != null ? cbOrderTable.getValue() : "تيك أواي";
        String custName = txtOrderCustName != null && !txtOrderCustName.getText().trim().isEmpty()
                ? txtOrderCustName.getText().trim() : "عميل كاشير";
        String orderId = "#ORD-" + orderCounter;
        String grandTotal = lblGrandTotal != null ? lblGrandTotal.getText() : "0.00 ج.م";

        StringBuilder itemsSummary = new StringBuilder();
        for (CartItemModel item : cartList) {
            if (itemsSummary.length() > 0) itemsSummary.append("، ");
            itemsSummary.append(item.getQuantity()).append("x ").append(item.getName());
        }

        // Add to Orders list with status 'Closed'
        OrderModel newOrder = new OrderModel(orderId, custName, itemsSummary.toString(), tableStr, grandTotal, LocalDateTime.now().format(timeFormatter), "مكتمل ومدفوع (Closed)");
        ordersList.add(0, newOrder);

        // Free Table if was occupied
        if (tableStr.startsWith("طاولة ")) {
            int tIndex = extractTableNumber(tableStr) - 1;
            if (tIndex >= 0 && tIndex < cashierTables.size()) {
                cashierTables.get(tIndex).setState(TableState.EMPTY, "", "", "");
                renderCashierTableCards();
                updateTableBadges();
            }
        }

        orderCounter++;
        updateCurrentOrderLabel();
        updateOrderCount();
        handleClearCart(null);

        showAlert("تمت عملية الدفع بنجاح", "تم استلام مبلغ " + grandTotal + " نقدًا/فيزا.\nتم إصدار الفاتورة رقم " + orderId + " وإغلاق الحساب بنجاح! 🧾✔", AlertType.INFORMATION);
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        cartList.clear();
        calculateCartTotals();
        if (txtOrderCustName != null) txtOrderCustName.clear();
    }

    // =========================================================================
    // Orders Tab Filters & Search
    // =========================================================================

    @FXML
    private void handleFilterAllOrders(ActionEvent event) {
        currentOrderFilter = "ALL";
        highlightOrderFilterButton(btnFilterAll);
        applyOrdersFilter();
    }

    @FXML
    private void handleFilterOpenOrders(ActionEvent event) {
        currentOrderFilter = "OPEN";
        highlightOrderFilterButton(btnFilterOpen);
        applyOrdersFilter();
    }

    @FXML
    private void handleFilterRunningOrders(ActionEvent event) {
        currentOrderFilter = "RUNNING";
        highlightOrderFilterButton(btnFilterRunning);
        applyOrdersFilter();
    }

    @FXML
    private void handleFilterClosedOrders(ActionEvent event) {
        currentOrderFilter = "CLOSED";
        highlightOrderFilterButton(btnFilterClosed);
        applyOrdersFilter();
    }

    private void highlightOrderFilterButton(JFXButton activeBtn) {
        JFXButton[] buttons = {btnFilterAll, btnFilterOpen, btnFilterRunning, btnFilterClosed};
        for (JFXButton b : buttons) {
            if (b != null) {
                b.getStyleClass().removeAll("btn-action-add", "btn-action-clear");
                b.getStyleClass().add(b == activeBtn ? "btn-action-add" : "btn-action-clear");
            }
        }
    }

    private void applyOrdersFilter() {
        if (filteredOrdersList == null) return;
        String search = txtOrdersSearch != null ? txtOrdersSearch.getText().trim().toLowerCase() : "";

        filteredOrdersList.setPredicate(order -> {
            boolean matchesCategory = true;
            if (currentOrderFilter.equals("OPEN")) {
                matchesCategory = order.getStatus().contains("Open") || order.getStatus().contains("مفتوح");
            } else if (currentOrderFilter.equals("RUNNING")) {
                matchesCategory = order.getStatus().contains("Running") || order.getStatus().contains("جاري");
            } else if (currentOrderFilter.equals("CLOSED")) {
                matchesCategory = order.getStatus().contains("Closed") || order.getStatus().contains("مكتمل");
            }

            boolean matchesSearch = true;
            if (!search.isEmpty()) {
                matchesSearch = (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(search))
                        || (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(search))
                        || (order.getTableNo() != null && order.getTableNo().toLowerCase().contains(search));
            }

            return matchesCategory && matchesSearch;
        });

        updateOrderCount();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void setupSearchListeners() {
        if (txtMenuSearch != null) {
            txtMenuSearch.textProperty().addListener((obs, o, n) -> renderMenuItems(n));
        }
        if (txtOrdersSearch != null) {
            txtOrdersSearch.textProperty().addListener((obs, o, n) -> applyOrdersFilter());
        }
    }

    private void updateCurrentOrderLabel() {
        if (lblCurrentOrderNum != null) {
            lblCurrentOrderNum.setText("طلب رقم: #ORD-" + orderCounter);
        }
    }

    private void updateOrderCount() {
        if (lblOrdersCount != null) {
            int c = filteredOrdersList != null ? filteredOrdersList.size() : ordersList.size();
            lblOrdersCount.setText(String.valueOf(c));
        }
    }

    private void updateTableBadges() {
        int empty = 0, assigned = 0, reserved = 0;
        for (CashierTableModel t : cashierTables) {
            if (t.getState() == TableState.EMPTY) empty++;
            else if (t.getState() == TableState.ASSIGNED) assigned++;
            else if (t.getState() == TableState.RESERVED) reserved++;
        }
        if (lblTablesEmpty != null) lblTablesEmpty.setText(String.valueOf(empty));
        if (lblTablesAssigned != null) lblTablesAssigned.setText(String.valueOf(assigned));
        if (lblTablesReserved != null) lblTablesReserved.setText(String.valueOf(reserved));
    }

    private int extractTableNumber(String tableStr) {
        String digits = tableStr.replaceAll("\\D+", "");
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    // =========================================================================
    // Inner Models & Enums
    // =========================================================================

    public enum TableState {
        EMPTY, ASSIGNED, RESERVED
    }

    public static class MenuItemModel {
        private final String name;
        private final String category;
        private final double price;
        private final String icon;

        public MenuItemModel(String name, String category, double price, String icon) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.icon = icon;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public String getIcon() { return icon; }
    }

    public static class CartItemModel {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleStringProperty price;
        private final SimpleStringProperty total;
        private final double rawPrice;

        public CartItemModel(String name, int quantity, String price, String total, double rawPrice) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.price = new SimpleStringProperty(price);
            this.total = new SimpleStringProperty(total);
            this.rawPrice = rawPrice;
        }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }

        public SimpleIntegerProperty quantityProperty() { return quantity; }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int q) { this.quantity.set(q); }

        public SimpleStringProperty priceProperty() { return price; }
        public SimpleStringProperty totalProperty() { return total; }
        public void setTotal(String t) { this.total.set(t); }

        public double getRawPrice() { return rawPrice; }
    }

    public static class OrderModel {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty customerName;
        private final SimpleStringProperty itemsSummary;
        private final SimpleStringProperty tableNo;
        private final SimpleStringProperty totalAmount;
        private final SimpleStringProperty timeStr;
        private final SimpleStringProperty status;

        public OrderModel(String orderId, String customerName, String itemsSummary, String tableNo, String totalAmount, String timeStr, String status) {
            this.orderId = new SimpleStringProperty(orderId);
            this.customerName = new SimpleStringProperty(customerName);
            this.itemsSummary = new SimpleStringProperty(itemsSummary);
            this.tableNo = new SimpleStringProperty(tableNo);
            this.totalAmount = new SimpleStringProperty(totalAmount);
            this.timeStr = new SimpleStringProperty(timeStr);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty orderIdProperty() { return orderId; }
        public String getOrderId() { return orderId.get(); }

        public SimpleStringProperty customerNameProperty() { return customerName; }
        public String getCustomerName() { return customerName.get(); }

        public SimpleStringProperty itemsSummaryProperty() { return itemsSummary; }
        public String getItemsSummary() { return itemsSummary.get(); }

        public SimpleStringProperty tableNoProperty() { return tableNo; }
        public String getTableNo() { return tableNo.get(); }

        public SimpleStringProperty totalAmountProperty() { return totalAmount; }
        public String getTotalAmount() { return totalAmount.get(); }

        public SimpleStringProperty timeStrProperty() { return timeStr; }
        public String getTimeStr() { return timeStr.get(); }

        public SimpleStringProperty statusProperty() { return status; }
        public String getStatus() { return status.get(); }
        public void setStatus(String s) { this.status.set(s); }
    }

    public static class CashierTableModel {
        private final int tableNo;
        private final int chairs;
        private TableState state;
        private String activeOrderId = "";
        private String activeCustomer = "";
        private String activeTotal = "";

        public CashierTableModel(int tableNo, int chairs) {
            this.tableNo = tableNo;
            this.chairs = chairs;
            this.state = TableState.EMPTY;
        }

        public int getTableNo() { return tableNo; }
        public int getChairs() { return chairs; }
        public TableState getState() { return state; }
        public void setState(TableState state, String orderId, String customer, String total) {
            this.state = state;
            this.activeOrderId = orderId;
            this.activeCustomer = customer;
            this.activeTotal = total;
        }

        public String getActiveOrderId() { return activeOrderId; }
        public String getActiveCustomer() { return activeCustomer; }
        public String getActiveTotal() { return activeTotal; }
    }
}
