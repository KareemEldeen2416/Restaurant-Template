package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

/**
 * Controller for the Inventory and Suppliers Management Window.
 * Features:
 * - 3 Dedicated Tabs: Products, Suppliers, and Information (Supplement Process Log).
 * - Tab 1 (Products): Connected directly with MySQL 'inventory' database table.
 * - Field mappings: product_name, reference_no, product_unit, product_category,
 *   available_quantity, low_limit, purchase_price, sales_price, supplier,
 *   date_of_last_supplement, show_in_menu.
 * - Dynamic Category & Unit Management via modal window with full CRUD.
 * - Full CRUD & search against MySQL database for Inventory.
 * - Tab 2 (Suppliers): Suppliers CRUD, Search, and TableView.
 * - Tab 3 (Information / Supplements): Supplement logging with auto total-price computation,
 *   CRUD, and real-time inventory quantity auto-synchronization in the database.
 * 
 * @author KareemEldeen
 */
public class inventory implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    @FXML private TabPane tabPaneInventory;

    // =========================================================================
    // FXML Tab 1: Products (Inventory)
    // =========================================================================
    @FXML private JFXComboBox<String> cbProdName;
    @FXML private JFXButton btnSearchSuppliedProd;
    @FXML private JFXTextField txtProdRef;
    @FXML private JFXTextField txtProdMinQty;
    @FXML private JFXTextField txtProdSellPrice;

    @FXML private JFXButton btnProdAdd;
    @FXML private JFXButton btnProdEdit;
    @FXML private JFXButton btnProdDelete;
    @FXML private JFXButton btnProdClear;
    @FXML private JFXCheckBox chkShowInMenu;
    @FXML private Label lblProdFormStatus;
    @FXML private Label lblProdActionMessage;

    @FXML private JFXTextField txtProdSearch;
    @FXML private JFXButton btnProdSearch;
    @FXML private JFXButton btnProdClearSearch;
    @FXML private Label lblProdTotalCount;

    @FXML private TableView<ProductModel> tblProducts;
    @FXML private TableColumn<ProductModel, Number> colProdSeq;
    @FXML private TableColumn<ProductModel, String> colProdRef;
    @FXML private TableColumn<ProductModel, String> colProdName;
    @FXML private TableColumn<ProductModel, String> colProdCategory;
    @FXML private TableColumn<ProductModel, String> colProdUnit;
    @FXML private TableColumn<ProductModel, Number> colProdQty;
    @FXML private TableColumn<ProductModel, Number> colProdMinQty;
    @FXML private TableColumn<ProductModel, String> colProdCostPrice;
    @FXML private TableColumn<ProductModel, String> colProdSellPrice;
    @FXML private TableColumn<ProductModel, String> colProdShowInMenu;
    @FXML private TableColumn<ProductModel, String> colProdSupplier;

    // =========================================================================
    // FXML Tab 2: Suppliers
    // =========================================================================
    @FXML private JFXTextField txtSuppName;
    @FXML private JFXTextField txtSuppNationalId;
    @FXML private JFXTextField txtSuppPhone;
    @FXML private JFXTextField txtSuppAddress;
    @FXML private JFXTextField txtSuppAccount;

    @FXML private JFXButton btnSuppAdd;
    @FXML private JFXButton btnSuppEdit;
    @FXML private JFXButton btnSuppDelete;
    @FXML private JFXButton btnSuppClear;
    @FXML private Label lblSuppFormStatus;
    @FXML private Label lblSuppActionMessage;

    @FXML private JFXTextField txtSuppSearch;
    @FXML private JFXButton btnSuppSearch;
    @FXML private JFXButton btnSuppClearSearch;
    @FXML private Label lblSuppTotalCount;

    @FXML private TableView<SupplierModel> tblSuppliers;
    @FXML private TableColumn<SupplierModel, Number> colSuppSeq;
    @FXML private TableColumn<SupplierModel, String> colSuppName;
    @FXML private TableColumn<SupplierModel, String> colSuppNationalId;
    @FXML private TableColumn<SupplierModel, String> colSuppPhone;
    @FXML private TableColumn<SupplierModel, String> colSuppAddress;
    @FXML private TableColumn<SupplierModel, String> colSuppAccount;

    // =========================================================================
    // FXML Tab 3: Information & Supplement Processes
    // =========================================================================
    @FXML private JFXTextField txtProcProduct;
    @FXML private JFXComboBox<String> cbProcCategory;
    @FXML private JFXComboBox<String> cbProcSupplier;
    @FXML private DatePicker dpProcDate;
    @FXML private JFXComboBox<String> cbProcUnit;
    @FXML private JFXTextField txtProcQty;
    @FXML private JFXTextField txtProcUnitPrice;

    @FXML private JFXButton btnProcAdd;
    @FXML private JFXButton btnProcEdit;
    @FXML private JFXButton btnProcDelete;
    @FXML private JFXButton btnProcClear;
    @FXML private Label lblProcFormStatus;
    @FXML private Label lblProcActionMessage;

    @FXML private JFXTextField txtProcSearch;
    @FXML private JFXButton btnProcSearch;
    @FXML private JFXButton btnProcClearSearch;
    @FXML private Label lblProcTotalCount;

    @FXML private TableView<SupplementProcessModel> tblSupplements;
    @FXML private TableColumn<SupplementProcessModel, Number> colProcSeq;
    @FXML private TableColumn<SupplementProcessModel, String> colProcDate;
    @FXML private TableColumn<SupplementProcessModel, String> colProcProduct;
    @FXML private TableColumn<SupplementProcessModel, String> colProcSupplier;
    @FXML private TableColumn<SupplementProcessModel, String> colProcUnit;
    @FXML private TableColumn<SupplementProcessModel, Number> colProcQty;
    @FXML private TableColumn<SupplementProcessModel, String> colProcUnitPrice;
    @FXML private TableColumn<SupplementProcessModel, String> colProcTotalPrice;

    // =========================================================================
    // FXML Tab 4: Internal Supplements
    // =========================================================================
    @FXML private JFXComboBox<String> cbInternalProduct;
    @FXML private Label lblInternalAvailableQty;
    @FXML private JFXTextField txtInternalQty;
    @FXML private JFXComboBox<String> cbInternalDept;
    @FXML private DatePicker dpInternalDate;
    @FXML private JFXTextField txtInternalNotes;
    @FXML private Label lblInternalFormStatus;
    @FXML private Label lblInternalActionMessage;
    @FXML private JFXButton btnAddInternal;
    @FXML private JFXButton btnDeleteInternal;
    @FXML private JFXButton btnClearInternal;

    @FXML private JFXTextField txtInternalSearch;
    @FXML private JFXButton btnInternalSearch;
    @FXML private JFXButton btnInternalClearSearch;
    @FXML private Label lblInternalTotalCount;

    @FXML private TableView<InternalSupplementModel> tblInternalSupplements;
    @FXML private TableColumn<InternalSupplementModel, Number> colInternalSeq;
    @FXML private TableColumn<InternalSupplementModel, String> colInternalDate;
    @FXML private TableColumn<InternalSupplementModel, String> colInternalProduct;
    @FXML private TableColumn<InternalSupplementModel, String> colInternalDept;
    @FXML private TableColumn<InternalSupplementModel, Number> colInternalQty;
    @FXML private TableColumn<InternalSupplementModel, String> colInternalUnit;
    @FXML private TableColumn<InternalSupplementModel, String> colInternalNotes;

    // =========================================================================
    // Data Collections & State
    // =========================================================================
    private final ObservableList<ProductModel> productList = FXCollections.observableArrayList();
    private final ObservableList<SupplierModel> supplierList = FXCollections.observableArrayList();

    private final ObservableList<SupplementProcessModel> supplementList = FXCollections.observableArrayList();
    private final ObservableList<InternalSupplementModel> internalSupplementsList = FXCollections.observableArrayList();

    private final ObservableList<String> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<String> unitsList = FXCollections.observableArrayList();
    private final ObservableList<String> departmentsList = FXCollections.observableArrayList();
    private final ObservableList<String> internalProductsList = FXCollections.observableArrayList();

    private static final String OPTION_MANAGE_CATEGORIES = "➕ إضافة / إدارة الأقسام...";
    private static final String OPTION_MANAGE_UNITS = "➕ إضافة / إدارة الوحدات...";
    private static final String OPTION_MANAGE_DEPTS = "➕ إضافة / إدارة الأقسام...";

    // Database column mappings for inventory table
    private final List<String> existingInventoryColumns = new ArrayList<>();
    private String invNameColumn = "product_name";
    private String invRefColumn = "reference_no";
    private String invUnitColumn = "product_unit";
    private String invCategoryColumn = "product_category";
    private String invQtyColumn = "available_quantity";
    private String invMinQtyColumn = "low_limit";
    private String invCostPriceColumn = "purchase_price";
    private String invSellPriceColumn = "sales_price";
    private String invSupplierColumn = "supplier";
    private String invDateColumn = null;
    private String invShowMenuColumn = "show_in_menu";

    // Database column mappings for suppliers table
    private final List<String> existingSupplierColumns = new ArrayList<>();
    private String suppNidColumn = "n_id";
    private String suppNameColumn = "supplier_name";
    private String suppPhoneColumn = "phone";
    private String suppAddressColumn = "address";
    private String suppAccountColumn = "bank_account";

    // Database column mappings for supplements table
    private final List<String> existingSupplementColumns = new ArrayList<>();
    private String suppProcIdColumn = "id";
    private String suppProcProdIdColumn = "product_id";
    private boolean suppProcProdIdIsInteger = false;
    private String suppProcProdNameColumn = "product_name";
    private String suppProcSuppNameColumn = "supplier_name";
    private String suppProcDateColumn = "date_of_supplement";
    private String suppProcUnitColumn = "unit";
    private String suppProcQtyColumn = "quantity";
    private String suppProcUnitPriceColumn = "unit_price";
    private String suppProcTotalPriceColumn = null;

    // Database column mappings for products table
    private final List<String> existingProductColumns = new ArrayList<>();
    private String productsIdColumn = "product_id";
    private String productsNameColumn = "product_name";

    private String categoryNameColumn = "category_name";
    private String unitNameColumn = "unit";
    private String unitAbbreviationColumn = null;

    private boolean hasColumn(List<String> columns, String target) {
        if (columns == null || target == null) return false;
        for (String c : columns) {
            if (target.equalsIgnoreCase(c.trim())) return true;
        }
        return false;
    }

    private String getActualColumnName(List<String> columns, String target, String fallback) {
        if (columns != null && target != null) {
            for (String c : columns) {
                if (target.equalsIgnoreCase(c.trim())) return c;
            }
        }
        return fallback;
    }

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        setupNumericInputFilters();
        initTableColumns();
        ensureDatabaseTablesExist();
        detectDatabaseColumnNames();
        loadCategoriesFromDatabase();
        loadUnitsFromDatabase();
        syncDatabaseInventoryStockWithSupplements();
        loadProductsFromDatabase(null);
        loadSuppliersFromDatabase(null);
        loadSupplementsFromDatabase(null);
        setupTableSelectionListeners();
        setupSearchFilters();
        setupComboBoxActionListeners();
        setupSuppliedProductPicker();
        setupShowInMenuHandler();
        loadDepartmentsFromDatabase();
        loadInternalProductChoices();
        loadInternalSupplementsFromDatabase(null);
        setupInternalSupplementsListeners();
    }

    /**
     * Helper to keep only digits in text input.
     */
    private void applyIntegerFilter(JFXTextField field, int maxLen) {
        if (field == null) return;
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (maxLen > 0 && filtered.length() > maxLen) {
                filtered = filtered.substring(0, maxLen);
            }
            if (!filtered.equals(newVal)) {
                field.setText(filtered);
            }
        });
    }

    /**
     * Helper to keep only valid numeric values (digits and at most one decimal point).
     */
    private void applyDecimalFilter(JFXTextField field) {
        if (field == null) return;
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            StringBuilder sb = new StringBuilder();
            boolean hasDot = false;
            for (char c : newVal.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else if (c == '.' && !hasDot) {
                    sb.append(c);
                    hasDot = true;
                }
            }
            String filtered = sb.toString();
            if (!filtered.equals(newVal)) {
                field.setText(filtered);
            }
        });
    }

    /**
     * Sets up numeric-only constraints on all numeric fields across inventory tabs:
     * - Tab 1 (Products): Quantity, Min Quantity, Purchase/Cost Price, Sell Price
     * - Tab 2 (Suppliers): National ID (14 digits), Phone (11 digits), Bank Account (numbers only)
     * - Tab 3 (Supplements / Information): Quantity, Unit Price
     */
    private void setupNumericInputFilters() {
        // Tab 1: Products
        applyDecimalFilter(txtProdMinQty);
        applyDecimalFilter(txtProdSellPrice);

        // Tab 2: Suppliers
        applyIntegerFilter(txtSuppNationalId, 14);
        applyIntegerFilter(txtSuppPhone, 11);
        applyIntegerFilter(txtSuppAccount, -1);

        // Tab 3: Supplement Processes
        applyDecimalFilter(txtProcQty);
        applyDecimalFilter(txtProcUnitPrice);

        // Tab 4: Internal Supplements
        applyDecimalFilter(txtInternalQty);
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
     * Maps table columns to data models.
     */
    private void initTableColumns() {
        // Tab 1: Products Columns
        if (colProdSeq != null) colProdSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colProdRef != null) colProdRef.setCellValueFactory(c -> c.getValue().referenceNoProperty());
        if (colProdName != null) colProdName.setCellValueFactory(c -> c.getValue().nameProperty());
        if (colProdCategory != null) colProdCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        if (colProdUnit != null) colProdUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        if (colProdQty != null) colProdQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        if (colProdMinQty != null) colProdMinQty.setCellValueFactory(c -> c.getValue().minQuantityProperty());
        if (colProdCostPrice != null) colProdCostPrice.setCellValueFactory(c -> c.getValue().costPriceProperty());
        if (colProdSellPrice != null) colProdSellPrice.setCellValueFactory(c -> c.getValue().sellPriceProperty());
        if (colProdShowInMenu != null) {
            colProdShowInMenu.setCellValueFactory(c -> 
                Bindings.createStringBinding(
                    () -> c.getValue().isShowInMenu() ? "✔ نعم" : "❌ لا",
                    c.getValue().showInMenuProperty()
                )
            );
        }
        if (colProdSupplier != null) colProdSupplier.setCellValueFactory(c -> c.getValue().supplierProperty());

        if (tblProducts != null) tblProducts.setItems(productList);

        // Tab 2: Suppliers Columns
        if (colSuppSeq != null) colSuppSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colSuppName != null) colSuppName.setCellValueFactory(c -> c.getValue().nameProperty());
        if (colSuppNationalId != null) colSuppNationalId.setCellValueFactory(c -> c.getValue().nationalIdProperty());
        if (colSuppPhone != null) colSuppPhone.setCellValueFactory(c -> c.getValue().phoneProperty());
        if (colSuppAddress != null) colSuppAddress.setCellValueFactory(c -> c.getValue().addressProperty());
        if (colSuppAccount != null) colSuppAccount.setCellValueFactory(c -> c.getValue().accountNumberProperty());

        if (tblSuppliers != null) tblSuppliers.setItems(supplierList);

        // Tab 3: Supplement Processes Columns
        if (colProcSeq != null) colProcSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colProcDate != null) colProcDate.setCellValueFactory(c -> c.getValue().dateProperty());
        if (colProcProduct != null) colProcProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        if (colProcSupplier != null) colProcSupplier.setCellValueFactory(c -> c.getValue().supplierNameProperty());
        if (colProcUnit != null) colProcUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        if (colProcQty != null) colProcQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        if (colProcUnitPrice != null) colProcUnitPrice.setCellValueFactory(c -> c.getValue().unitPriceProperty());
        if (colProcTotalPrice != null) colProcTotalPrice.setCellValueFactory(c -> c.getValue().totalPriceProperty());

        if (tblSupplements != null) tblSupplements.setItems(supplementList);

        // Tab 4: Internal Supplements Columns
        if (colInternalSeq != null) colInternalSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colInternalDate != null) colInternalDate.setCellValueFactory(c -> c.getValue().dateProperty());
        if (colInternalProduct != null) colInternalProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        if (colInternalDept != null) colInternalDept.setCellValueFactory(c -> c.getValue().deptNameProperty());
        if (colInternalQty != null) colInternalQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        if (colInternalUnit != null) colInternalUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        if (colInternalNotes != null) colInternalNotes.setCellValueFactory(c -> c.getValue().notesProperty());

        if (tblInternalSupplements != null) tblInternalSupplements.setItems(internalSupplementsList);
    }

    /**
     * Ensures MySQL tables 'inventory', 'categories', 'units', 'suppliers', 'products', and 'supplements' exist in the database.
     */
    private void ensureDatabaseTablesExist() {
        String sqlCategories = "CREATE TABLE IF NOT EXISTS categories ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "category_name VARCHAR(100) NOT NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlCategories);

        String sqlUnits = "CREATE TABLE IF NOT EXISTS units ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "unit VARCHAR(100) NOT NULL,"
                + "abbreviation VARCHAR(50) NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlUnits);

        String sqlInventory = "CREATE TABLE IF NOT EXISTS inventory ("
                + "reference_no VARCHAR(50) PRIMARY KEY,"
                + "product_name VARCHAR(150) NOT NULL,"
                + "product_unit VARCHAR(100) NULL,"
                + "product_category VARCHAR(100) NULL,"
                + "available_quantity DOUBLE DEFAULT 0,"
                + "low_limit DOUBLE DEFAULT 0,"
                + "purchase_price DECIMAL(10,2) DEFAULT 0,"
                + "sales_price DECIMAL(10,2) DEFAULT 0,"
                + "supplier VARCHAR(150) NULL,"
                + "show_in_menu BOOLEAN DEFAULT TRUE"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlInventory);

        String sqlSuppliers = "CREATE TABLE IF NOT EXISTS suppliers ("
                + "n_id VARCHAR(100) PRIMARY KEY,"
                + "supplier_name VARCHAR(200) NOT NULL,"
                + "phone VARCHAR(100) NULL,"
                + "address VARCHAR(255) NULL,"
                + "bank_account VARCHAR(150) NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlSuppliers);

        String sqlProducts = "CREATE TABLE IF NOT EXISTS products ("
                + "product_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "product_name VARCHAR(150) NOT NULL,"
                + "product_category VARCHAR(100) NULL,"
                + "product_unit VARCHAR(100) NULL,"
                + "product_price DECIMAL(10,2) NOT NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlProducts);

        String sqlSupplements = "CREATE TABLE IF NOT EXISTS supplements ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "product_id VARCHAR(100) NULL,"
                + "product_name VARCHAR(200) NOT NULL,"
                + "supplier_name VARCHAR(200) NOT NULL,"
                + "date_of_supplement DATE NOT NULL,"
                + "unit VARCHAR(100) NOT NULL,"
                + "quantity DOUBLE NOT NULL DEFAULT 0,"
                + "unit_price DECIMAL(10,2) NOT NULL DEFAULT 0"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlSupplements);

        // Ensure columns in suppliers have sufficient capacity to prevent Data truncation errors
        try {
            DBConnection.executeUpdate("ALTER TABLE suppliers MODIFY COLUMN phone VARCHAR(100) NULL;");
            DBConnection.executeUpdate("ALTER TABLE suppliers MODIFY COLUMN n_id VARCHAR(100) NOT NULL;");
            DBConnection.executeUpdate("ALTER TABLE suppliers MODIFY COLUMN supplier_name VARCHAR(200) NOT NULL;");
            DBConnection.executeUpdate("ALTER TABLE suppliers MODIFY COLUMN address VARCHAR(255) NULL;");
            DBConnection.executeUpdate("ALTER TABLE suppliers MODIFY COLUMN bank_account VARCHAR(150) NULL;");
        } catch (Exception ignored) {}

        // Check if show_in_menu column exists in inventory, if not add it
        ResultSet rsMenu = DBConnection.executeQuery("SHOW COLUMNS FROM inventory LIKE 'show_in_menu';");
        if (rsMenu != null) {
            try {
                if (!rsMenu.next()) {
                    DBConnection.executeUpdate("ALTER TABLE inventory ADD COLUMN show_in_menu BOOLEAN DEFAULT TRUE;");
                }
                rsMenu.close();
            } catch (SQLException ignored) {}
        }

        // Check if supplier column exists in inventory, if not add it
        try {
            ResultSet rsSuppCol = DBConnection.executeQuery("SHOW COLUMNS FROM inventory LIKE 'supplier';");
            if (rsSuppCol != null) {
                if (!rsSuppCol.next()) {
                    DBConnection.executeUpdate("ALTER TABLE inventory ADD COLUMN supplier VARCHAR(150) NULL;");
                }
                rsSuppCol.close();
            }
        } catch (Exception ignored) {}

        // Ensure depts table exists
        String sqlDepts = "CREATE TABLE IF NOT EXISTS depts ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "dept_name VARCHAR(150) NOT NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlDepts);

        // Ensure default depts exist if table is newly created
        ResultSet rsCheckDepts = DBConnection.executeQuery("SELECT COUNT(*) FROM depts;");
        if (rsCheckDepts != null) {
            try {
                if (rsCheckDepts.next() && rsCheckDepts.getInt(1) == 0) {
                    DBConnection.executeUpdate("INSERT INTO depts (dept_name) VALUES ('المطبخ الرئيسي'), ('البار والمشروبات'), ('الصالة والخدمة'), ('المخبوزات والحلويات'), ('النظافة والصيانة');");
                }
                rsCheckDepts.close();
            } catch (SQLException ignored) {}
        }

        // Ensure internal_supplements table exists
        String sqlInternalSupplements = "CREATE TABLE IF NOT EXISTS internal_supplements ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "product_name VARCHAR(150) NOT NULL,"
                + "dept_name VARCHAR(150) NOT NULL,"
                + "quantity DOUBLE NOT NULL DEFAULT 0,"
                + "unit VARCHAR(100) NULL,"
                + "date_of_supplement DATE NOT NULL,"
                + "notes VARCHAR(255) NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlInternalSupplements);
    }

    /**
     * Dynamically inspects all column names from MySQL database schema.
     */
    private void detectDatabaseColumnNames() {
        // 1. Detect columns in 'categories'
        ResultSet rsCat = DBConnection.executeQuery("SHOW COLUMNS FROM categories;");
        if (rsCat != null) {
            try {
                List<String> catCols = new ArrayList<>();
                while (rsCat.next()) {
                    catCols.add(rsCat.getString("Field"));
                }
                rsCat.close();

                categoryNameColumn = null;
                for (String col : catCols) {
                    if ("category_name".equalsIgnoreCase(col) || "category".equalsIgnoreCase(col)
                            || "cat_name".equalsIgnoreCase(col) || "name".equalsIgnoreCase(col)
                            || "title".equalsIgnoreCase(col)) {
                        categoryNameColumn = col;
                        break;
                    }
                }
                if (categoryNameColumn == null && !catCols.isEmpty()) {
                    for (String col : catCols) {
                        if (!"id".equalsIgnoreCase(col)) {
                            categoryNameColumn = col;
                            break;
                        }
                    }
                    if (categoryNameColumn == null) categoryNameColumn = catCols.get(0);
                }
            } catch (SQLException ignored) {}
        }

        // 2. Detect columns in 'units'
        ResultSet rsUnit = DBConnection.executeQuery("SHOW COLUMNS FROM units;");
        if (rsUnit != null) {
            try {
                List<String> unitCols = new ArrayList<>();
                while (rsUnit.next()) {
                    String col = rsUnit.getString("Field");
                    unitCols.add(col);
                    if ("abbreviation".equalsIgnoreCase(col) || "abbr".equalsIgnoreCase(col)
                            || "symbol".equalsIgnoreCase(col) || "code".equalsIgnoreCase(col)
                            || "short_name".equalsIgnoreCase(col)) {
                        unitAbbreviationColumn = col;
                    }
                }
                rsUnit.close();

                unitNameColumn = null;
                for (String col : unitCols) {
                    if ("unit".equalsIgnoreCase(col)) {
                        unitNameColumn = col;
                        break;
                    }
                }
                if (unitNameColumn == null) {
                    for (String col : unitCols) {
                        if ("unit_name".equalsIgnoreCase(col) || "units".equalsIgnoreCase(col)
                                || "name".equalsIgnoreCase(col) || "title".equalsIgnoreCase(col)) {
                            unitNameColumn = col;
                            break;
                        }
                    }
                }
                if (unitNameColumn == null && !unitCols.isEmpty()) {
                    for (String col : unitCols) {
                        if (!"id".equalsIgnoreCase(col) && !col.equalsIgnoreCase(unitAbbreviationColumn)) {
                            unitNameColumn = col;
                            break;
                        }
                    }
                    if (unitNameColumn == null) unitNameColumn = unitCols.get(0);
                }
            } catch (SQLException ignored) {}
        }

        // 3. Detect columns in 'inventory'
        existingInventoryColumns.clear();
        invDateColumn = null;
        invSupplierColumn = null;
        ResultSet rsInv = DBConnection.executeQuery("SHOW COLUMNS FROM inventory;");
        if (rsInv != null) {
            try {
                while (rsInv.next()) {
                    existingInventoryColumns.add(rsInv.getString("Field"));
                }
                rsInv.close();

                for (String col : existingInventoryColumns) {
                    if ("product_id".equalsIgnoreCase(col) || "prod_id".equalsIgnoreCase(col)
                            || "reference_no".equalsIgnoreCase(col) || "ref_no".equalsIgnoreCase(col)
                            || "ref".equalsIgnoreCase(col) || "id".equalsIgnoreCase(col)
                            || "barcode".equalsIgnoreCase(col) || "code".equalsIgnoreCase(col)) {
                        invRefColumn = col;
                    } else if ("product_name".equalsIgnoreCase(col) || "name".equalsIgnoreCase(col)
                            || "item_name".equalsIgnoreCase(col) || "prod_name".equalsIgnoreCase(col)) {
                        invNameColumn = col;
                    } else if ("product_unit".equalsIgnoreCase(col) || "unit".equalsIgnoreCase(col) || "unit_name".equalsIgnoreCase(col)) {
                        invUnitColumn = col;
                    } else if ("product_category".equalsIgnoreCase(col) || "category".equalsIgnoreCase(col)
                            || "category_name".equalsIgnoreCase(col) || "dept".equalsIgnoreCase(col)) {
                        invCategoryColumn = col;
                    } else if ("available_quantity".equalsIgnoreCase(col) || "quantity".equalsIgnoreCase(col)
                            || "qty".equalsIgnoreCase(col) || "stock".equalsIgnoreCase(col) || "current_qty".equalsIgnoreCase(col)) {
                        invQtyColumn = col;
                    } else if ("low_limit".equalsIgnoreCase(col) || "min_quantity".equalsIgnoreCase(col)
                            || "min_qty".equalsIgnoreCase(col) || "minimum_quantity".equalsIgnoreCase(col) || "limit".equalsIgnoreCase(col)) {
                        invMinQtyColumn = col;
                    } else if ("purchase_price".equalsIgnoreCase(col) || "cost_price".equalsIgnoreCase(col)
                            || "buy_price".equalsIgnoreCase(col) || "cost".equalsIgnoreCase(col)) {
                        invCostPriceColumn = col;
                    } else if ("sales_price".equalsIgnoreCase(col) || "sell_price".equalsIgnoreCase(col)
                            || "price".equalsIgnoreCase(col) || "selling_price".equalsIgnoreCase(col)) {
                        invSellPriceColumn = col;
                    } else if ("supplier".equalsIgnoreCase(col) || "supplier_name".equalsIgnoreCase(col) || "supp".equalsIgnoreCase(col)) {
                        invSupplierColumn = col;
                    } else if ("date_of_last_supplement".equalsIgnoreCase(col) || "supplement_date".equalsIgnoreCase(col)
                            || "last_supplement_date".equalsIgnoreCase(col) || "date".equalsIgnoreCase(col)) {
                        invDateColumn = col;
                    } else if ("show_in_menu".equalsIgnoreCase(col) || "show_menu".equalsIgnoreCase(col)
                            || "is_menu".equalsIgnoreCase(col) || "menu_display".equalsIgnoreCase(col)) {
                        invShowMenuColumn = col;
                    }
                }
            } catch (SQLException ignored) {}
        }

        // 4. Detect columns in 'suppliers'
        existingSupplierColumns.clear();
        ResultSet rsSupp = DBConnection.executeQuery("SHOW COLUMNS FROM suppliers;");
        if (rsSupp != null) {
            try {
                while (rsSupp.next()) {
                    existingSupplierColumns.add(rsSupp.getString("Field"));
                }
                rsSupp.close();

                for (String col : existingSupplierColumns) {
                    if ("n_id".equalsIgnoreCase(col) || "national_id".equalsIgnoreCase(col)
                            || "id_card".equalsIgnoreCase(col) || "nid".equalsIgnoreCase(col)) {
                        suppNidColumn = col;
                    } else if ("supplier_name".equalsIgnoreCase(col) || "name".equalsIgnoreCase(col)
                            || "supp_name".equalsIgnoreCase(col) || "company_name".equalsIgnoreCase(col)) {
                        suppNameColumn = col;
                    } else if ("phone".equalsIgnoreCase(col) || "mobile".equalsIgnoreCase(col)
                            || "telephone".equalsIgnoreCase(col) || "phone_number".equalsIgnoreCase(col)) {
                        suppPhoneColumn = col;
                    } else if ("address".equalsIgnoreCase(col) || "location".equalsIgnoreCase(col)
                            || "addr".equalsIgnoreCase(col) || "city".equalsIgnoreCase(col)) {
                        suppAddressColumn = col;
                    } else if ("bank_account".equalsIgnoreCase(col) || "account".equalsIgnoreCase(col)
                            || "account_number".equalsIgnoreCase(col) || "account_no".equalsIgnoreCase(col) || "bank_acc".equalsIgnoreCase(col)) {
                        suppAccountColumn = col;
                    }
                }
            } catch (SQLException ignored) {}
        }

        // 5. Detect columns in 'products'
        existingProductColumns.clear();
        ResultSet rsProd = DBConnection.executeQuery("SHOW COLUMNS FROM products;");
        if (rsProd != null) {
            try {
                while (rsProd.next()) {
                    existingProductColumns.add(rsProd.getString("Field"));
                }
                rsProd.close();

                productsIdColumn = null;
                for (String col : existingProductColumns) {
                    if ("product_id".equalsIgnoreCase(col) || "prod_id".equalsIgnoreCase(col)
                            || "id".equalsIgnoreCase(col) || "reference_no".equalsIgnoreCase(col)) {
                        productsIdColumn = col;
                        break;
                    }
                }
                if (productsIdColumn == null && !existingProductColumns.isEmpty()) {
                    productsIdColumn = existingProductColumns.get(0);
                }

                productsNameColumn = null;
                for (String col : existingProductColumns) {
                    if ("product_name".equalsIgnoreCase(col) || "name".equalsIgnoreCase(col)
                            || "item_name".equalsIgnoreCase(col) || "prod_name".equalsIgnoreCase(col)) {
                        productsNameColumn = col;
                        break;
                    }
                }
                if (productsNameColumn == null) productsNameColumn = "product_name";
            } catch (SQLException ignored) {}
        }

        // 6. Detect columns in 'supplements'
        existingSupplementColumns.clear();
        suppProcProdIdIsInteger = false;
        ResultSet rsSuppProc = DBConnection.executeQuery("SHOW COLUMNS FROM supplements;");
        if (rsSuppProc != null) {
            try {
                while (rsSuppProc.next()) {
                    String col = rsSuppProc.getString("Field");
                    String colType = rsSuppProc.getString("Type");
                    existingSupplementColumns.add(col);

                    if ("id".equalsIgnoreCase(col) || "supplement_id".equalsIgnoreCase(col)
                            || "proc_id".equalsIgnoreCase(col) || "process_id".equalsIgnoreCase(col)) {
                        suppProcIdColumn = col;
                    } else if ("product_id".equalsIgnoreCase(col) || "prod_id".equalsIgnoreCase(col)
                            || "product_code".equalsIgnoreCase(col) || "ref_no".equalsIgnoreCase(col)) {
                        suppProcProdIdColumn = col;
                        if (colType != null && (colType.toLowerCase().startsWith("int") || colType.toLowerCase().contains("int"))) {
                            suppProcProdIdIsInteger = true;
                        }
                    } else if ("product_name".equalsIgnoreCase(col) || "prod_name".equalsIgnoreCase(col)
                            || "item_name".equalsIgnoreCase(col) || "product".equalsIgnoreCase(col)
                            || "name".equalsIgnoreCase(col)) {
                        suppProcProdNameColumn = col;
                    } else if ("supplier_name".equalsIgnoreCase(col) || "supplier".equalsIgnoreCase(col)
                            || "supp_name".equalsIgnoreCase(col) || "company_name".equalsIgnoreCase(col)) {
                        suppProcSuppNameColumn = col;
                    } else if ("date_of_supplement".equalsIgnoreCase(col) || "supplement_date".equalsIgnoreCase(col)
                            || "date".equalsIgnoreCase(col) || "process_date".equalsIgnoreCase(col)) {
                        suppProcDateColumn = col;
                    } else if ("unit".equalsIgnoreCase(col) || "product_unit".equalsIgnoreCase(col)
                            || "proc_unit".equalsIgnoreCase(col)) {
                        suppProcUnitColumn = col;
                    } else if ("quantity".equalsIgnoreCase(col) || "qty".equalsIgnoreCase(col)
                            || "amount".equalsIgnoreCase(col)) {
                        suppProcQtyColumn = col;
                    } else if ("unit_price".equalsIgnoreCase(col) || "price".equalsIgnoreCase(col)
                            || "cost_price".equalsIgnoreCase(col) || "purchase_price".equalsIgnoreCase(col)) {
                        suppProcUnitPriceColumn = col;
                    } else if ("total_price".equalsIgnoreCase(col) || "total".equalsIgnoreCase(col)
                            || "total_cost".equalsIgnoreCase(col)) {
                        suppProcTotalPriceColumn = col;
                    }
                }
                rsSuppProc.close();
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Loads categories from MySQL table 'categories'.
     */
    private void loadCategoriesFromDatabase() {
        categoriesList.clear();
        categoriesList.add(OPTION_MANAGE_CATEGORIES);

        String query = "SELECT * FROM categories;";
        ResultSet rs = DBConnection.executeQuery(query);
        if (rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                while (rs.next()) {
                    String cat = null;
                    try {
                        cat = rs.getString(categoryNameColumn);
                    } catch (Exception ignored) {}
                    if (cat == null) {
                        for (int i = 1; i <= count; i++) {
                            String cn = meta.getColumnName(i);
                            if (!"id".equalsIgnoreCase(cn)) {
                                cat = rs.getString(i);
                                if (cat != null) break;
                            }
                        }
                    }
                    if (cat != null && !cat.trim().isEmpty()) {
                        String clean = cat.trim();
                        if (!categoriesList.contains(clean)) {
                            categoriesList.add(clean);
                        }
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        String[] defaults = {
            "اللحوم والبرجر", "الدواجن والطيور", "الجبن والألبان", "الخضروات والفواكه",
            "المخبوزات والخبز", "المشروبات والعصائر", "الصلصات والبهارات", "الزيوت والمقليات", "مستلزمات التعبئة"
        };
        for (String d : defaults) {
            if (!categoriesList.contains(d)) {
                categoriesList.add(d);
                DBConnection.executeUpdate("INSERT INTO categories (" + categoryNameColumn + ") VALUES ('" + escapeSql(d) + "');");
            }
        }

        if (cbProcCategory != null) {
            cbProcCategory.setItems(categoriesList);
        }
    }

    /**
     * Loads units from MySQL table 'units'.
     */
    private void loadUnitsFromDatabase() {
        unitsList.clear();
        unitsList.add(OPTION_MANAGE_UNITS);

        String query = "SELECT * FROM units;";
        ResultSet rs = DBConnection.executeQuery(query);
        if (rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                while (rs.next()) {
                    String u = null;
                    try {
                        u = rs.getString(unitNameColumn);
                    } catch (Exception ignored) {}
                    if (u == null) {
                        for (int i = 1; i <= count; i++) {
                            String cn = meta.getColumnName(i);
                            if (!"id".equalsIgnoreCase(cn)) {
                                u = rs.getString(i);
                                if (u != null) break;
                            }
                        }
                    }
                    if (u != null && !u.trim().isEmpty()) {
                        String clean = u.trim();
                        if (!unitsList.contains(clean)) {
                            unitsList.add(clean);
                        }
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        String[][] defaultUnits = {
            {"كجم", "Kg"},
            {"قطعة", "Pcs"},
            {"لتر", "Litre"},
            {"كرتونة", "Carton"},
            {"باكت", "Pack"},
            {"شيكارة", "Bag"}
        };
        for (String[] du : defaultUnits) {
            if (!unitsList.contains(du[0])) {
                unitsList.add(du[0]);
                if (unitAbbreviationColumn != null) {
                    DBConnection.executeUpdate("INSERT INTO units (" + unitNameColumn + ", " + unitAbbreviationColumn + ") VALUES ('" + escapeSql(du[0]) + "', '" + escapeSql(du[1]) + "');");
                } else {
                    DBConnection.executeUpdate("INSERT INTO units (" + unitNameColumn + ") VALUES ('" + escapeSql(du[0]) + "');");
                }
            }
        }

        if (cbProcUnit != null) {
            cbProcUnit.setItems(unitsList);
        }
    }

    /**
     * Ensures that a unit exists in the 'units' table before referencing it in 'inventory'
     * to satisfy the MySQL foreign key constraint (inventory_ibfk_1).
     */
    private void ensureUnitExistsInDatabase(String unit) {
        if (unit == null || unit.trim().isEmpty() || OPTION_MANAGE_UNITS.equals(unit)) return;
        String clean = unit.trim();
        String chkSql = "SELECT COUNT(*) FROM units WHERE " + unitNameColumn + " = '" + escapeSql(clean) + "';";
        ResultSet rs = DBConnection.executeQuery(chkSql);
        boolean exists = false;
        if (rs != null) {
            try {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (!exists) {
            String abbr = clean;
            if (unitAbbreviationColumn != null) {
                String ins = "INSERT INTO units (" + unitNameColumn + ", " + unitAbbreviationColumn + ") VALUES ('" + escapeSql(clean) + "', '" + escapeSql(abbr) + "');";
                DBConnection.executeUpdate(ins);
            } else {
                String ins = "INSERT INTO units (" + unitNameColumn + ") VALUES ('" + escapeSql(clean) + "');";
                DBConnection.executeUpdate(ins);
            }
            if (!unitsList.contains(clean)) {
                unitsList.add(clean);
            }
        }
    }

    /**
     * Ensures that a category exists in the 'categories' table before inserting.
     */
    private void ensureCategoryExistsInDatabase(String cat) {
        if (cat == null || cat.trim().isEmpty() || OPTION_MANAGE_CATEGORIES.equals(cat)) return;
        String clean = cat.trim();
        String chkSql = "SELECT COUNT(*) FROM categories WHERE " + categoryNameColumn + " = '" + escapeSql(clean) + "';";
        ResultSet rs = DBConnection.executeQuery(chkSql);
        boolean exists = false;
        if (rs != null) {
            try {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (!exists) {
            String ins = "INSERT INTO categories (" + categoryNameColumn + ") VALUES ('" + escapeSql(clean) + "');";
            DBConnection.executeUpdate(ins);
            if (!categoriesList.contains(clean)) {
                categoriesList.add(clean);
            }
        }
    }

    private String getFirstAvailableUnit() {
        for (String u : unitsList) {
            if (u != null && !OPTION_MANAGE_UNITS.equals(u) && !u.trim().isEmpty()) {
                return u.trim();
            }
        }
        ensureUnitExistsInDatabase("قطعة");
        return "قطعة";
    }

    /**
     * Consolidates duplicate rows in MySQL table 'inventory' where (product_name, supplier) are identical.
     * Keeps the primary reference_no, sets its available_quantity to the sum of all duplicates,
     * updates supplements references, and removes the redundant duplicate rows.
     */
    private void consolidateInventoryDuplicates() {
        if (invSupplierColumn == null || !hasColumn(existingInventoryColumns, invSupplierColumn)) {
            return;
        }
        try {
            String groupSql = "SELECT " + invNameColumn + ", " + invSupplierColumn + ", COUNT(*) as cnt, SUM(" + invQtyColumn + ") as total_qty, MIN(" + invRefColumn + ") as keep_ref "
                    + "FROM inventory "
                    + "WHERE " + invNameColumn + " IS NOT NULL AND " + invSupplierColumn + " IS NOT NULL "
                    + "GROUP BY " + invNameColumn + ", " + invSupplierColumn + " "
                    + "HAVING COUNT(*) > 1;";
            ResultSet rs = DBConnection.executeQuery(groupSql);
            List<Object[]> duplicates = new ArrayList<>();
            if (rs != null) {
                while (rs.next()) {
                    String pName = rs.getString(1);
                    String sName = rs.getString(2);
                    double totalQ = rs.getDouble(4);
                    String keepRef = rs.getString(5);
                    duplicates.add(new Object[]{pName, sName, totalQ, keepRef});
                }
                rs.close();
            }

            for (Object[] row : duplicates) {
                String pName = (String) row[0];
                String sName = (String) row[1];
                double totalQ = (Double) row[2];
                String keepRef = (String) row[3];

                // 1. Update the kept row with total summed available_quantity
                String upd = "UPDATE inventory SET " + invQtyColumn + " = " + totalQ + " WHERE " + invRefColumn + " = '" + escapeSql(keepRef) + "';";
                DBConnection.executeUpdate(upd);

                // 2. Update any supplements referencing other duplicate reference numbers
                if (hasColumn(existingSupplementColumns, suppProcProdIdColumn)) {
                    String updSupp = "UPDATE supplements SET " + suppProcProdIdColumn + " = '" + escapeSql(keepRef) + "' "
                            + "WHERE " + suppProcProdNameColumn + " = '" + escapeSql(pName) + "' AND " + suppProcSuppNameColumn + " = '" + escapeSql(sName) + "';";
                    DBConnection.executeUpdate(updSupp);
                }

                // 3. Remove the redundant rows
                String del = "DELETE FROM inventory WHERE " + invNameColumn + " = '" + escapeSql(pName) + "' "
                        + "AND " + invSupplierColumn + " = '" + escapeSql(sName) + "' "
                        + "AND " + invRefColumn + " != '" + escapeSql(keepRef) + "';";
                DBConnection.executeUpdate(del);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Loads products strictly from MySQL table 'inventory'.
     * Groups products by product name and supplier into one row, summing their available quantities.
     */
    private void loadProductsFromDatabase(String searchQuery) {
        consolidateInventoryDuplicates();
        productList.clear();

        StringBuilder sql = new StringBuilder("SELECT * FROM inventory WHERE " + invNameColumn + " IN ("
                + "SELECT DISTINCT " + suppProcProdNameColumn + " FROM supplements WHERE " + suppProcProdNameColumn + " IS NOT NULL AND " + suppProcProdNameColumn + " != '')");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = escapeSql(searchQuery.trim());
            sql.append(" AND (")
               .append(invNameColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invRefColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invCategoryColumn).append(" LIKE '%").append(q).append("%'");
            if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
                sql.append(" OR ").append(invSupplierColumn).append(" LIKE '%").append(q).append("%'");
            }
            sql.append(" OR ").append(invUnitColumn).append(" LIKE '%").append(q).append("%')");
        }
        sql.append(";");

        ResultSet rs = DBConnection.executeQuery(sql.toString());
        Map<String, ProductModel> groupedProducts = new LinkedHashMap<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    String name = getColumnStringSafe(rs, invNameColumn, "product_name", "name");
                    String ref = getColumnStringSafe(rs, invRefColumn, "reference_no", "ref_no");
                    String unit = getColumnStringSafe(rs, invUnitColumn, "product_unit", "unit");
                    String cat = getColumnStringSafe(rs, invCategoryColumn, "product_category", "category");
                    double qty = getColumnDoubleSafe(rs, invQtyColumn, "available_quantity", "quantity", "qty");
                    double minQty = getColumnDoubleSafe(rs, invMinQtyColumn, "low_limit", "min_quantity", "min_qty");
                    double cost = getColumnDoubleSafe(rs, invCostPriceColumn, "purchase_price", "cost_price");
                    double sell = getColumnDoubleSafe(rs, invSellPriceColumn, "sales_price", "sell_price");
                    String supp = "-";
                    if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
                        supp = getColumnStringSafe(rs, invSupplierColumn, "supplier", "supplier_name");
                    }
                    if (supp == null || supp.trim().isEmpty() || "-".equals(supp.trim())) {
                        String orderClause = "";
                        if (suppProcIdColumn != null && hasColumn(existingSupplementColumns, suppProcIdColumn)) {
                            orderClause = " ORDER BY " + suppProcIdColumn + " DESC";
                        } else if (suppProcDateColumn != null && hasColumn(existingSupplementColumns, suppProcDateColumn)) {
                            orderClause = " ORDER BY " + suppProcDateColumn + " DESC";
                        }
                        String sqlSupp = "SELECT " + suppProcSuppNameColumn + " FROM supplements WHERE " + suppProcProdNameColumn + " = '" + escapeSql(name) + "'" + orderClause + " LIMIT 1;";
                        ResultSet rsS = DBConnection.executeQuery(sqlSupp);
                        if (rsS != null) {
                            try {
                                if (rsS.next()) {
                                    String sName = rsS.getString(1);
                                    if (sName != null && !sName.trim().isEmpty()) {
                                        supp = sName.trim();
                                    }
                                }
                                rsS.close();
                            } catch (SQLException ignored) {}
                        }
                    }
                    if (supp == null || supp.trim().isEmpty()) {
                        supp = "-";
                    }
                    String date = getColumnStringSafe(rs, invDateColumn, "date_of_last_supplement", "supplement_date", "date");
                    boolean showMenu = getColumnBooleanSafe(rs, invShowMenuColumn, "show_in_menu", "show_menu");

                    String costFormatted = String.format(Locale.US, "%,.2f ج.م", cost);
                    String sellFormatted = String.format(Locale.US, "%,.2f ج.م", sell);

                    String key = (name != null ? name.trim().toLowerCase() : "") + "|||" + (supp != null ? supp.trim().toLowerCase() : "");

                    if (groupedProducts.containsKey(key)) {
                        ProductModel existing = groupedProducts.get(key);
                        existing.setQuantity(existing.getQuantity() + qty);
                        if (existing.getMinQuantity() <= 0 && minQty > 0) {
                            existing.setMinQuantity(minQty);
                        }
                        if ((existing.getSellPrice() == null || existing.getSellPrice().startsWith("0")) && sell > 0) {
                            existing.setSellPrice(sellFormatted);
                        }
                        if ((existing.getCostPrice() == null || existing.getCostPrice().startsWith("0")) && cost > 0) {
                            existing.setCostPrice(costFormatted);
                        }
                        if (!showMenu) {
                            existing.setShowInMenu(false);
                        }
                    } else {
                        ProductModel model = new ProductModel(0, name, ref, unit, qty, minQty, costFormatted, sellFormatted, cat, supp, date, showMenu);
                        groupedProducts.put(key, model);
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        int seq = 1;
        for (ProductModel model : groupedProducts.values()) {
            model.setSeq(seq++);
            productList.add(model);
        }

        if (lblProdTotalCount != null) {
            lblProdTotalCount.setText(String.valueOf(productList.size()));
        }
        if (tblProducts != null) {
            tblProducts.refresh();
        }
        syncDropdownChoices();
        loadInternalProductChoices();
        if (cbInternalProduct != null && cbInternalProduct.getValue() != null) {
            updateInternalAvailableStockDisplay(cbInternalProduct.getValue());
        }
    }

    /**
     * Loads suppliers from MySQL table 'suppliers'.
     */
    private void loadSuppliersFromDatabase(String searchQuery) {
        supplierList.clear();

        StringBuilder sql = new StringBuilder("SELECT * FROM suppliers");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = escapeSql(searchQuery.trim());
            sql.append(" WHERE ")
               .append(suppNameColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppNidColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppPhoneColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppAddressColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppAccountColumn).append(" LIKE '%").append(q).append("%'");
        }
        sql.append(";");

        ResultSet rs = DBConnection.executeQuery(sql.toString());
        int seq = 1;
        if (rs != null) {
            try {
                while (rs.next()) {
                    String name = getColumnStringSafe(rs, suppNameColumn, "supplier_name", "name");
                    String nid = getColumnStringSafe(rs, suppNidColumn, "n_id", "national_id");
                    String phone = getColumnStringSafe(rs, suppPhoneColumn, "phone", "mobile");
                    String addr = getColumnStringSafe(rs, suppAddressColumn, "address", "location");
                    String acc = getColumnStringSafe(rs, suppAccountColumn, "bank_account", "account_number", "account");

                    SupplierModel model = new SupplierModel(seq++, name, nid.isEmpty() ? "غير مسجل" : nid, phone, addr, acc.isEmpty() ? "غير مسجل" : acc);
                    supplierList.add(model);
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (lblSuppTotalCount != null) {
            lblSuppTotalCount.setText(String.valueOf(supplierList.size()));
        }
        if (tblSuppliers != null) {
            tblSuppliers.refresh();
        }
        syncDropdownChoices();
    }

    /**
     * Loads supplement processes strictly from MySQL table 'supplements'.
     */
    private void loadSupplementsFromDatabase(String searchQuery) {
        supplementList.clear();

        StringBuilder sql = new StringBuilder("SELECT * FROM supplements");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = escapeSql(searchQuery.trim());
            sql.append(" WHERE ")
               .append(suppProcProdNameColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppProcSuppNameColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppProcDateColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(suppProcUnitColumn).append(" LIKE '%").append(q).append("%'");
            if (existingSupplementColumns.contains(suppProcProdIdColumn)) {
                sql.append(" OR ").append(suppProcProdIdColumn).append(" LIKE '%").append(q).append("%'");
            }
        }
        sql.append(" ORDER BY ");
        if (existingSupplementColumns.contains(suppProcIdColumn)) {
            sql.append(suppProcIdColumn).append(" DESC;");
        } else {
            sql.append(suppProcDateColumn).append(" DESC;");
        }

        ResultSet rs = DBConnection.executeQuery(sql.toString());
        int seq = 1;
        if (rs != null) {
            try {
                while (rs.next()) {
                    int dbId = 0;
                    if (existingSupplementColumns.contains(suppProcIdColumn)) {
                        try { dbId = rs.getInt(suppProcIdColumn); } catch (Exception ignored) {}
                    }
                    String prodId = getColumnStringSafe(rs, suppProcProdIdColumn, "product_id", "prod_id");
                    String prodName = getColumnStringSafe(rs, suppProcProdNameColumn, "product_name", "prod_name", "name");
                    String suppName = getColumnStringSafe(rs, suppProcSuppNameColumn, "supplier_name", "supplier");
                    String date = getColumnStringSafe(rs, suppProcDateColumn, "date_of_supplement", "supplement_date", "date");
                    String unit = getColumnStringSafe(rs, suppProcUnitColumn, "unit", "product_unit");
                    double qty = getColumnDoubleSafe(rs, suppProcQtyColumn, "quantity", "qty");
                    double unitPrice = getColumnDoubleSafe(rs, suppProcUnitPriceColumn, "unit_price", "price", "cost_price");

                    double total = qty * unitPrice;
                    String unitPriceFormatted = String.format(Locale.US, "%,.2f ج.م", unitPrice);
                    String totalPriceFormatted = String.format(Locale.US, "%,.2f ج.م", total);

                    SupplementProcessModel model = new SupplementProcessModel(seq++, dbId, prodId, date, prodName, suppName, unit, qty, unitPriceFormatted, totalPriceFormatted);
                    supplementList.add(model);
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (dpProcDate != null) dpProcDate.setValue(LocalDate.now());

        if (lblProcTotalCount != null) {
            lblProcTotalCount.setText(String.valueOf(supplementList.size()));
        }
        if (tblSupplements != null) {
            tblSupplements.refresh();
        }
        syncDropdownChoices();
        updateAllCounts();
    }

    /**
     * Looks up product_id / reference_no directly from 'inventory' table for a given product_name.
     * Returns null if product does not exist in 'inventory' table.
     */
    private String getProductIdFromInventoryTable(String productName) {
        if (productName == null || productName.trim().isEmpty()) return null;
        String clean = escapeSql(productName.trim());

        String foundId = null;

        // 1. Query the 'inventory' table for invRefColumn (reference_no / product_id)
        String sql = "SELECT " + invRefColumn + " FROM inventory WHERE " + invNameColumn + " = '" + clean + "' LIMIT 1;";
        ResultSet rs = DBConnection.executeQuery(sql);
        if (rs != null) {
            try {
                if (rs.next()) {
                    foundId = rs.getString(1);
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        // 2. Fallback: check other ID columns in 'inventory' if any
        if (foundId == null || foundId.trim().isEmpty()) {
            for (String col : existingInventoryColumns) {
                if ("product_id".equalsIgnoreCase(col) || "prod_id".equalsIgnoreCase(col) || "id".equalsIgnoreCase(col)) {
                    String sqlId = "SELECT " + col + " FROM inventory WHERE " + invNameColumn + " = '" + clean + "' LIMIT 1;";
                    ResultSet rsId = DBConnection.executeQuery(sqlId);
                    if (rsId != null) {
                        try {
                            if (rsId.next()) {
                                foundId = rsId.getString(1);
                            }
                            rsId.close();
                        } catch (SQLException ignored) {}
                    }
                    if (foundId != null && !foundId.trim().isEmpty()) break;
                }
            }
        }

        // 3. Fallback: in-memory productList
        if (foundId == null || foundId.trim().isEmpty()) {
            for (ProductModel p : productList) {
                if (p.getName() != null && p.getName().trim().equalsIgnoreCase(productName.trim())) {
                    foundId = p.getReferenceNo();
                    break;
                }
            }
        }

        // 4. Verify product exists in 'inventory'
        boolean productExistsInInventory = false;
        for (ProductModel p : productList) {
            if (p.getName() != null && p.getName().trim().equalsIgnoreCase(productName.trim())) {
                productExistsInInventory = true;
                break;
            }
        }
        if (!productExistsInInventory) {
            String chkSql = "SELECT COUNT(*) FROM inventory WHERE " + invNameColumn + " = '" + clean + "';";
            ResultSet rsChk = DBConnection.executeQuery(chkSql);
            if (rsChk != null) {
                try {
                    if (rsChk.next() && rsChk.getInt(1) > 0) {
                        productExistsInInventory = true;
                    }
                    rsChk.close();
                } catch (SQLException ignored) {}
            }
        }

        if (!productExistsInInventory && (foundId == null || foundId.trim().isEmpty())) {
            return null;
        }

        return foundId != null && !foundId.trim().isEmpty() ? foundId.trim() : "1";
    }

    /**
     * Ensures that the product exists in the 'products' table to satisfy 
     * MySQL foreign key constraints (supplements_ibfk_1 on product_name, supplements_ibfk_2 on product_id).
     * Returns the matching product_id from the products table.
     */
    private String ensureProductExistsInProductsTable(String productName, String unit, double unitPrice) {
        if (productName == null || productName.trim().isEmpty()) return "1";
        String clean = escapeSql(productName.trim());

        // 1. Ensure unit exists in 'units' to satisfy products foreign key
        if (unit != null && !unit.trim().isEmpty() && !OPTION_MANAGE_UNITS.equals(unit)) {
            ensureUnitExistsInDatabase(unit);
        }

        // 2. Check if product already exists in 'products' table
        String sql = "SELECT " + productsIdColumn + ", " + productsNameColumn + " FROM products WHERE " + productsNameColumn + " = '" + clean + "' LIMIT 1;";
        ResultSet rs = DBConnection.executeQuery(sql);
        String foundId = null;
        if (rs != null) {
            try {
                if (rs.next()) {
                    foundId = rs.getString(1);
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (foundId == null) {
            // Case-insensitive check
            String sqlLike = "SELECT " + productsIdColumn + ", " + productsNameColumn + " FROM products WHERE LOWER(" + productsNameColumn + ") = LOWER('" + clean + "') LIMIT 1;";
            ResultSet rs2 = DBConnection.executeQuery(sqlLike);
            if (rs2 != null) {
                try {
                    if (rs2.next()) {
                        foundId = rs2.getString(1);
                    }
                    rs2.close();
                } catch (SQLException ignored) {}
            }
        }

        // 3. If not found in 'products', insert it into 'products' table
        if (foundId == null || foundId.trim().isEmpty()) {
            String cat = "عام";
            double price = unitPrice;
            for (ProductModel p : productList) {
                if (p.getName() != null && p.getName().trim().equalsIgnoreCase(productName.trim())) {
                    if (p.getCategory() != null && !p.getCategory().trim().isEmpty() && !OPTION_MANAGE_CATEGORIES.equals(p.getCategory())) {
                        cat = p.getCategory();
                    }
                    try {
                        price = Double.parseDouble(p.getSellPrice().replace("ج.م", "").replace(",", "").trim());
                    } catch (Exception ignored) {}
                    break;
                }
            }
            ensureCategoryExistsInDatabase(cat);

            List<String> prodCols = new ArrayList<>();
            List<String> prodVals = new ArrayList<>();

            if (existingProductColumns.contains(productsNameColumn)) {
                prodCols.add(productsNameColumn);
                prodVals.add("'" + clean + "'");
            }
            if (existingProductColumns.contains("product_category")) {
                prodCols.add("product_category");
                prodVals.add("'" + escapeSql(cat) + "'");
            }
            if (existingProductColumns.contains("product_unit")) {
                prodCols.add("product_unit");
                prodVals.add("'" + escapeSql(unit != null && !OPTION_MANAGE_UNITS.equals(unit) ? unit : "قطعة") + "'");
            }
            if (existingProductColumns.contains("product_price")) {
                prodCols.add("product_price");
                prodVals.add(String.valueOf(price));
            }

            if (!prodCols.isEmpty()) {
                String insSql = "INSERT INTO products (" + String.join(", ", prodCols) + ") VALUES (" + String.join(", ", prodVals) + ");";
                DBConnection.executeUpdate(insSql);
            }

            // Retrieve newly generated product_id
            ResultSet rsNew = DBConnection.executeQuery(sql);
            if (rsNew != null) {
                try {
                    if (rsNew.next()) {
                        foundId = rsNew.getString(1);
                    }
                    rsNew.close();
                } catch (SQLException ignored) {}
            }
        }

        return foundId != null && !foundId.trim().isEmpty() ? foundId.trim() : "1";
    }

    /**
     * Ensures supplier exists in 'suppliers' table.
     */
    private void ensureSupplierExistsInDatabase(String supplierName) {
        if (supplierName == null || supplierName.trim().isEmpty()) return;
        String clean = escapeSql(supplierName.trim());
        String chk = "SELECT COUNT(*) FROM suppliers WHERE " + suppNameColumn + " = '" + clean + "';";
        ResultSet rs = DBConnection.executeQuery(chk);
        boolean exists = false;
        if (rs != null) {
            try {
                if (rs.next() && rs.getInt(1) > 0) exists = true;
                rs.close();
            } catch (SQLException ignored) {}
        }
        if (!exists) {
            String ins = "INSERT INTO suppliers (" + suppNidColumn + ", " + suppNameColumn + ") VALUES ('" + ("SUP-" + (System.currentTimeMillis() % 100000)) + "', '" + clean + "');";
            DBConnection.executeUpdate(ins);
        }
    }

    /**
     * Synchronizes dropdown lists of suppliers and products.
     */
    private void syncDropdownChoices() {
        ObservableList<String> suppNames = FXCollections.observableArrayList();
        for (SupplierModel s : supplierList) {
            if (!suppNames.contains(s.getName())) {
                suppNames.add(s.getName());
            }
        }
        if (cbProcSupplier != null) cbProcSupplier.setItems(suppNames);

        ObservableList<String> prodNames = FXCollections.observableArrayList();
        
        // Load product names from 'inventory' table first
        ResultSet rsInv = DBConnection.executeQuery("SELECT DISTINCT " + invNameColumn + " FROM inventory WHERE " + invNameColumn + " IS NOT NULL AND " + invNameColumn + " != '';");
        if (rsInv != null) {
            try {
                while (rsInv.next()) {
                    String pName = rsInv.getString(1);
                    if (pName != null && !pName.trim().isEmpty() && !prodNames.contains(pName.trim())) {
                        prodNames.add(pName.trim());
                    }
                }
                rsInv.close();
            } catch (SQLException ignored) {}
        }

        // Also include from supplements table
        ResultSet rsSupp = DBConnection.executeQuery("SELECT DISTINCT " + suppProcProdNameColumn + " FROM supplements WHERE " + suppProcProdNameColumn + " IS NOT NULL AND " + suppProcProdNameColumn + " != '';");
        if (rsSupp != null) {
            try {
                while (rsSupp.next()) {
                    String pName = rsSupp.getString(1);
                    if (pName != null && !pName.trim().isEmpty() && !prodNames.contains(pName.trim())) {
                        prodNames.add(pName.trim());
                    }
                }
                rsSupp.close();
            } catch (SQLException ignored) {}
        }

        // Also include any in productList
        for (ProductModel p : productList) {
            if (p.getName() != null && !p.getName().trim().isEmpty() && !prodNames.contains(p.getName().trim())) {
                prodNames.add(p.getName().trim());
            }
        }

        ObservableList<String> suppliedNames = FXCollections.observableArrayList();
        for (ProductModel p : productList) {
            String itemText = p.getName();
            if (p.getSupplier() != null && !p.getSupplier().trim().isEmpty() && !"-".equals(p.getSupplier().trim())) {
                itemText = p.getName() + " (" + p.getSupplier() + ")";
            }
            if (!suppliedNames.contains(itemText)) {
                suppliedNames.add(itemText);
            }
        }
        if (cbProdName != null) cbProdName.setItems(suppliedNames);
    }

    /**
     * Table selection listeners for clicking rows.
     */
    private void setupTableSelectionListeners() {
        // Tab 1: Products selection
        if (tblProducts != null) {
            tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateProductFields(newVal);
                }
            });
        }

        // Tab 2: Suppliers selection
        if (tblSuppliers != null) {
            tblSuppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateSupplierFields(newVal);
                }
            });
        }

        // Tab 3: Supplement Processes selection
        if (tblSupplements != null) {
            tblSupplements.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateProcessFields(newVal);
                }
            });
        }
    }

    /**
     * Live search filters for each tab.
     */
    private void setupSearchFilters() {
        if (txtProdSearch != null) {
            txtProdSearch.textProperty().addListener((obs, oldVal, newVal) -> loadProductsFromDatabase(newVal));
        }
        if (txtSuppSearch != null) {
            txtSuppSearch.textProperty().addListener((obs, oldVal, newVal) -> loadSuppliersFromDatabase(newVal));
        }
        if (txtProcSearch != null) {
            txtProcSearch.textProperty().addListener((obs, oldVal, newVal) -> loadSupplementsFromDatabase(newVal));
        }
    }

    /**
     * Setup ComboBox action listeners for "Add / Manage Units" and "Add / Manage Categories".
     */
    private void setupComboBoxActionListeners() {
        if (cbProcCategory != null) {
            cbProcCategory.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_CATEGORIES.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbProcCategory.setValue(oldVal);
                        openLookupManagerDialog(true);
                    });
                }
            });
        }
        if (cbProcUnit != null) {
            cbProcUnit.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_UNITS.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbProcUnit.setValue(oldVal);
                        openLookupManagerDialog(false);
                    });
                }
            });
        }
    }

    @FXML
    private void handleOpenCategoryManager(ActionEvent event) {
        openLookupManagerDialog(true);
    }

    @FXML
    private void handleOpenUnitManager(ActionEvent event) {
        openLookupManagerDialog(false);
    }

    // =========================================================================
    // Tab 1: Products (Inventory) CRUD Actions Against Database
    // =========================================================================

    @FXML
    private void handleAddProduct(ActionEvent event) {
        handleEditProduct(event);
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        String rawValue = cbProdName != null ? cbProdName.getValue() : null;
        ProductModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if ((rawValue == null || rawValue.trim().isEmpty()) && selected != null) {
            rawValue = selected.getName();
        }

        if (rawValue == null || rawValue.trim().isEmpty()) {
            showProductNotification("يرجى اختيار صنف مورّد من القائمة أولاً لتحديد وحفظ بياناته!", true);
            return;
        }

        String name = rawValue;
        if (name.contains(" (") && name.endsWith(")")) {
            name = name.substring(0, name.lastIndexOf(" (")).trim();
        }

        String ref = getSafeText(txtProdRef);
        String minQtyStr = getSafeText(txtProdMinQty);
        String sellStr = getSafeText(txtProdSellPrice);
        boolean showInMenu = chkShowInMenu != null && chkShowInMenu.isSelected();

        String targetRef = (selected != null && selected.getReferenceNo() != null && !selected.getReferenceNo().isEmpty()) ? selected.getReferenceNo() : ref;

        if (ref.isEmpty()) {
            ref = targetRef.isEmpty() ? generateAutoReferenceNo() : targetRef;
        } else {
            String chkSql = "SELECT COUNT(*) FROM inventory WHERE " + invRefColumn + " = '" + escapeSql(ref) + "' AND " + invRefColumn + " != '" + escapeSql(targetRef) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showProductNotification("الرقم المرجعي (" + ref + ") مسجل مسبقًا لصنف آخر في قاعدة البيانات!", true);
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        }

        double minQty = 0, sellPrice = 0;
        try {
            minQty = !minQtyStr.isEmpty() ? Double.parseDouble(minQtyStr.replace("ج.م", "").replace(",", "").trim()) : (selected != null ? selected.getMinQuantity() : 0);
            sellPrice = !sellStr.isEmpty() ? Double.parseDouble(sellStr.replace("ج.م", "").replace(",", "").trim()) : 0;
        } catch (NumberFormatException e) {
            showProductNotification("يرجى إدخال أرقام صحيحة للحد الأدنى وسعر البيع!", true);
            return;
        }

        List<String> setClauses = new ArrayList<>();
        if (hasColumn(existingInventoryColumns, invRefColumn) || existingInventoryColumns.isEmpty()) {
            setClauses.add(invRefColumn + " = '" + escapeSql(ref) + "'");
        }
        if (hasColumn(existingInventoryColumns, invMinQtyColumn) || existingInventoryColumns.isEmpty()) {
            setClauses.add(invMinQtyColumn + " = " + minQty);
        }
        if (hasColumn(existingInventoryColumns, invSellPriceColumn) || existingInventoryColumns.isEmpty()) {
            setClauses.add(invSellPriceColumn + " = " + sellPrice);
        }
        if (hasColumn(existingInventoryColumns, invShowMenuColumn) || existingInventoryColumns.isEmpty()) {
            setClauses.add(invShowMenuColumn + " = " + (showInMenu ? "1" : "0"));
        }

        String sql;
        if (targetRef != null && !targetRef.trim().isEmpty()) {
            sql = "UPDATE inventory SET " + String.join(", ", setClauses)
                    + " WHERE " + invRefColumn + " = '" + escapeSql(targetRef) + "';";
        } else {
            sql = "UPDATE inventory SET " + String.join(", ", setClauses)
                    + " WHERE " + invNameColumn + " = '" + escapeSql(name) + "';";
        }

        DBConnection.executeUpdate(sql);
        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        showProductNotification("تم حفظ وتحديث بيانات الصنف (" + name + ") بنجاح في قاعدة البيانات! ✔", false);
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        ProductModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProductNotification("يرجى اختيار صنف من الجدول لحذفه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد حذف الصنف");
        confirm.setHeaderText("حذف الصنف: " + selected.getName());
        confirm.setContentText("هل أنت متأكد من حذف هذا الصنف نهائيًا من قاعدة بيانات المخزن؟");
        confirm.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = selected.getName();
            DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            String sql = "DELETE FROM inventory WHERE " + invRefColumn + " = '" + escapeSql(selected.getReferenceNo()) + "';";
            DBConnection.executeUpdate(sql);
            DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");

            loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
            handleClearProductFields(null);
            showProductNotification("تم حذف الصنف (" + name + ") نهائيًا من المخزن! 🗑️", false);
        }
    }

    @FXML
    private void handleClearProductFields(ActionEvent event) {
        isPopulatingProductFields = true;
        try {
            if (cbProdName != null) cbProdName.setValue(null);
            if (txtProdRef != null) txtProdRef.clear();
            if (txtProdMinQty != null) txtProdMinQty.clear();
            if (txtProdSellPrice != null) txtProdSellPrice.clear();
            if (chkShowInMenu != null) chkShowInMenu.setSelected(true);

            if (tblProducts != null) tblProducts.getSelectionModel().clearSelection();
            if (lblProdFormStatus != null) lblProdFormStatus.setText("تم تفريغ الحقول. يرجى اختيار صنف مورّد لتحديد بياناته.");
        } finally {
            isPopulatingProductFields = false;
        }
    }

    @FXML
    private void handleProductSearch(ActionEvent event) {
        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : "");
    }

    @FXML
    private void handleClearProductSearch(ActionEvent event) {
        if (txtProdSearch != null) txtProdSearch.clear();
        loadProductsFromDatabase(null);
    }

    /**
     * Opens the popup manager dialog for Categories or Units with Add, Edit, Delete, and Search.
     */
    private void openLookupManagerDialog(boolean isCategory) {
        String entityTitle = isCategory ? "الأقسام والتصنيفات" : "الوحدات والرموز";
        String singleTitle = isCategory ? "القسم" : "الوحدة";
        String tableName = isCategory ? "categories" : "units";
        String columnName = isCategory ? categoryNameColumn : unitNameColumn;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("إدارة " + entityTitle);
        dialog.setHeaderText("إدارة قائمة " + entityTitle + " (إضافة، تعديل، حذف، وبحث)");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(14.0);
        content.setPadding(new Insets(16.0));
        content.setPrefWidth(520.0);
        content.setPrefHeight(480.0);

        // Form inputs
        VBox formBox = new VBox(10.0);
        formBox.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #EEEEEE; -fx-border-radius: 8px;");

        HBox inputsRow = new HBox(10.0);
        inputsRow.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(4.0);
        Label lblNamePrompt = new Label("اسم " + singleTitle + " *:");
        lblNamePrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");
        JFXTextField txtNameInput = new JFXTextField();
        txtNameInput.setPromptText("أدخل اسم " + singleTitle + "...");
        txtNameInput.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        txtNameInput.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));
        nameBox.getChildren().addAll(lblNamePrompt, txtNameInput);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        VBox abbrBox = new VBox(4.0);
        JFXTextField txtAbbrInput = new JFXTextField();
        if (!isCategory && unitAbbreviationColumn != null) {
            Label lblAbbrPrompt = new Label("الاختصار / الرمز:");
            lblAbbrPrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");
            txtAbbrInput.setPromptText("مثال: Pcs أو كجم...");
            txtAbbrInput.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
            txtAbbrInput.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));
            abbrBox.getChildren().addAll(lblAbbrPrompt, txtAbbrInput);
            abbrBox.setPrefWidth(160.0);
            inputsRow.getChildren().addAll(nameBox, abbrBox);
        } else {
            inputsRow.getChildren().add(nameBox);
        }

        HBox btnBox = new HBox(8.0);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        JFXButton btnAddLookup = new JFXButton("➕ إضافة");
        btnAddLookup.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnEditLookup = new JFXButton("✏️ تعديل");
        btnEditLookup.setStyle("-fx-background-color: #E65100; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnDeleteLookup = new JFXButton("🗑️ حذف");
        btnDeleteLookup.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnClearLookup = new JFXButton("🔄 تفريغ");
        btnClearLookup.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        btnBox.getChildren().addAll(btnAddLookup, btnEditLookup, btnDeleteLookup, btnClearLookup);
        formBox.getChildren().addAll(inputsRow, btnBox);

        // Search Field
        JFXTextField txtSearchLookup = new JFXTextField();
        txtSearchLookup.setPromptText("🔍 بحث في قائمة " + entityTitle + "...");
        txtSearchLookup.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        txtSearchLookup.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));

        // TableView for Lookup Items
        TableView<LookupItem> tblLookup = new TableView<>();
        tblLookup.setStyle("-fx-background-radius: 8px;");
        VBox.setVgrow(tblLookup, Priority.ALWAYS);

        TableColumn<LookupItem, Number> colSeqL = new TableColumn<>("م");
        colSeqL.setCellValueFactory(c -> c.getValue().seqProperty());
        colSeqL.setPrefWidth(50.0);

        TableColumn<LookupItem, String> colNameL = new TableColumn<>("اسم " + singleTitle);
        colNameL.setCellValueFactory(c -> c.getValue().nameProperty());
        colNameL.setPrefWidth(260.0);

        TableColumn<LookupItem, String> colAbbrL = new TableColumn<>("الاختصار");
        colAbbrL.setCellValueFactory(c -> c.getValue().abbrProperty());
        colAbbrL.setPrefWidth(120.0);

        if (!isCategory && unitAbbreviationColumn != null) {
            tblLookup.getColumns().addAll(colSeqL, colNameL, colAbbrL);
        } else {
            colNameL.setPrefWidth(420.0);
            tblLookup.getColumns().addAll(colSeqL, colNameL);
        }

        ObservableList<LookupItem> lookupData = FXCollections.observableArrayList();
        tblLookup.setItems(lookupData);

        final String activeCol = columnName;
        final String abbrCol = unitAbbreviationColumn;

        // Loader function
        Runnable loadData = () -> {
            lookupData.clear();
            String q = txtSearchLookup.getText() != null ? txtSearchLookup.getText().trim() : "";
            String sql = "SELECT * FROM " + tableName + ";";
            ResultSet rs = DBConnection.executeQuery(sql);
            if (rs != null) {
                try {
                    ResultSetMetaData meta = rs.getMetaData();
                    int count = meta.getColumnCount();
                    int seq = 1;
                    while (rs.next()) {
                        String n = null;
                        try {
                            n = rs.getString(activeCol);
                        } catch (Exception ignored) {}
                        if (n == null) {
                            for (int i = 1; i <= count; i++) {
                                String cn = meta.getColumnName(i);
                                if (!cn.equalsIgnoreCase("id") && !cn.equalsIgnoreCase(abbrCol)) {
                                    n = rs.getString(i);
                                    if (n != null) break;
                                }
                            }
                        }

                        String a = "";
                        if (abbrCol != null) {
                            try {
                                a = rs.getString(abbrCol);
                            } catch (Exception ignored) {}
                        }

                        if (n != null && !n.trim().isEmpty()) {
                            if (q.isEmpty() || n.toLowerCase().contains(q.toLowerCase()) || (a != null && a.toLowerCase().contains(q.toLowerCase()))) {
                                lookupData.add(new LookupItem(seq++, n.trim(), a != null ? a.trim() : ""));
                            }
                        }
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        };

        txtSearchLookup.textProperty().addListener((obs, oldVal, newVal) -> loadData.run());
        loadData.run();

        // Selection listener in dialog table
        tblLookup.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNameInput.setText(newVal.getName());
                txtAbbrInput.setText(newVal.getAbbr());
            }
        });

        // Add Handler
        btnAddLookup.setOnAction(e -> {
            String val = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (val.isEmpty()) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى إدخال اسم " + singleTitle + " أولاً!");
                return;
            }

            // Check duplicate
            String chkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + activeCol + " = '" + escapeSql(val) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showSimpleAlert(AlertType.WARNING, "تنبيه", "اسم " + singleTitle + " (" + val + ") مسجل مسبقًا في قاعدة البيانات!");
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }

            String insSql;
            if (!isCategory && abbrCol != null) {
                String abbr = txtAbbrInput.getText() != null && !txtAbbrInput.getText().trim().isEmpty() ? txtAbbrInput.getText().trim() : val;
                insSql = "INSERT INTO " + tableName + " (" + activeCol + ", " + abbrCol + ") VALUES ('" + escapeSql(val) + "', '" + escapeSql(abbr) + "');";
            } else {
                insSql = "INSERT INTO " + tableName + " (" + activeCol + ") VALUES ('" + escapeSql(val) + "');";
            }

            DBConnection.executeUpdate(insSql);
            txtNameInput.clear();
            txtAbbrInput.clear();
            loadData.run();
            if (isCategory) {
                loadCategoriesFromDatabase();
                if (cbProcCategory != null) cbProcCategory.setValue(val);
            } else {
                loadUnitsFromDatabase();
                if (cbProcUnit != null) cbProcUnit.setValue(val);
            }
        });

        // Edit Handler
        btnEditLookup.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى اختيار عنصر من الجدول لتعديله!");
                return;
            }
            String newVal = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (newVal.isEmpty()) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "لا يمكن ترك الاسم فارغًا!");
                return;
            }
            String oldVal = sel.getName();

            DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");

            String updSql;
            if (!isCategory && abbrCol != null) {
                String abbr = txtAbbrInput.getText() != null && !txtAbbrInput.getText().trim().isEmpty() ? txtAbbrInput.getText().trim() : newVal;
                updSql = "UPDATE " + tableName + " SET " + activeCol + " = '" + escapeSql(newVal) + "', " + abbrCol + " = '" + escapeSql(abbr) + "' WHERE " + activeCol + " = '" + escapeSql(oldVal) + "';";
            } else {
                updSql = "UPDATE " + tableName + " SET " + activeCol + " = '" + escapeSql(newVal) + "' WHERE " + activeCol + " = '" + escapeSql(oldVal) + "';";
            }
            DBConnection.executeUpdate(updSql);

            // Update referencing records in inventory table
            String invCol = isCategory ? invCategoryColumn : invUnitColumn;
            if (existingInventoryColumns.contains(invCol)) {
                String updInvSql = "UPDATE inventory SET " + invCol + " = '" + escapeSql(newVal) + "' WHERE " + invCol + " = '" + escapeSql(oldVal) + "';";
                DBConnection.executeUpdate(updInvSql);
            }
            try {
                String prodCol = isCategory ? "product_category" : "product_unit";
                DBConnection.executeUpdate("UPDATE products SET " + prodCol + " = '" + escapeSql(newVal) + "' WHERE " + prodCol + " = '" + escapeSql(oldVal) + "';");
            } catch (Exception ignored) {}

            DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");

            txtNameInput.clear();
            txtAbbrInput.clear();
            loadData.run();
            if (isCategory) {
                loadCategoriesFromDatabase();
                if (cbProcCategory != null && oldVal.equals(cbProcCategory.getValue())) {
                    cbProcCategory.setValue(newVal);
                }
            } else {
                loadUnitsFromDatabase();
                if (cbProcUnit != null && oldVal.equals(cbProcUnit.getValue())) {
                    cbProcUnit.setValue(newVal);
                }
            }
            loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        });

        // Delete Handler
        btnDeleteLookup.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى اختيار عنصر من الجدول لحذفه!");
                return;
            }
            String delVal = sel.getName();

            Alert conf = new Alert(AlertType.CONFIRMATION);
            conf.setTitle("تأكيد الحذف");
            conf.setHeaderText("حذف " + singleTitle + ": " + delVal);
            conf.setContentText("هل أنت متأكد من حذف هذا السجل نهائيًا من قاعدة البيانات؟");
            conf.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Optional<ButtonType> res = conf.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
                String delSql = "DELETE FROM " + tableName + " WHERE " + activeCol + " = '" + escapeSql(delVal) + "';";
                DBConnection.executeUpdate(delSql);
                DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");

                txtNameInput.clear();
                txtAbbrInput.clear();
                loadData.run();
                if (isCategory) {
                    loadCategoriesFromDatabase();
                    if (cbProcCategory != null && delVal.equals(cbProcCategory.getValue())) {
                        cbProcCategory.setValue(null);
                    }
                } else {
                    loadUnitsFromDatabase();
                    if (cbProcUnit != null && delVal.equals(cbProcUnit.getValue())) {
                        cbProcUnit.setValue(null);
                    }
                }
            }
        });

        // Clear Handler
        btnClearLookup.setOnAction(e -> {
            txtNameInput.clear();
            txtAbbrInput.clear();
            tblLookup.getSelectionModel().clearSelection();
        });

        // Double click to pick and close
        tblLookup.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    if (isCategory && cbProcCategory != null) {
                        ensureCategoryExistsInDatabase(sel.getName());
                        cbProcCategory.setValue(sel.getName());
                    } else if (!isCategory) {
                        ensureUnitExistsInDatabase(sel.getName());
                        if (cbProcUnit != null) cbProcUnit.setValue(sel.getName());
                    }
                    dialog.close();
                }
            }
        });

        content.getChildren().addAll(formBox, txtSearchLookup, tblLookup);
        dialog.getDialogPane().setContent(content);

        ButtonType btnApply = new ButtonType("✔ تطبيق واختيار", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType btnClose = new ButtonType("إلغاء", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(btnApply, btnClose);

        dialog.setResultConverter(b -> {
            if (b == btnApply) {
                LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    if (isCategory && cbProcCategory != null) {
                        ensureCategoryExistsInDatabase(sel.getName());
                        cbProcCategory.setValue(sel.getName());
                    } else if (!isCategory) {
                        ensureUnitExistsInDatabase(sel.getName());
                        if (cbProcUnit != null) cbProcUnit.setValue(sel.getName());
                    }
                } else {
                    String input = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
                    if (!input.isEmpty()) {
                        if (isCategory && cbProcCategory != null) {
                            ensureCategoryExistsInDatabase(input);
                            cbProcCategory.setValue(input);
                        } else if (!isCategory) {
                            ensureUnitExistsInDatabase(input);
                            if (cbProcUnit != null) cbProcUnit.setValue(input);
                        }
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
        if (isCategory) loadCategoriesFromDatabase(); else loadUnitsFromDatabase();
    }

    private void showSimpleAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    // =========================================================================
    // Tab 2: Suppliers CRUD Actions (Direct Database Connection)
    // =========================================================================

    private String generateAutoSupplierNationalId() {
        int max = 1000;
        for (SupplierModel s : supplierList) {
            String n = s.getNationalId();
            if (n != null && n.startsWith("SUPP-")) {
                try {
                    int num = Integer.parseInt(n.replaceAll("[^0-9]", ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return "SUPP-" + (max + 1);
    }

    @FXML
    private void handleAddSupplier(ActionEvent event) {
        String name = getSafeText(txtSuppName);
        String nationalId = getSafeText(txtSuppNationalId);
        String phone = getSafeText(txtSuppPhone);
        String address = getSafeText(txtSuppAddress);
        String account = getSafeText(txtSuppAccount);

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showSupplierNotification("يرجى ملء الحقول الإجبارية (اسم المورد، رقم الهاتف، والعنوان)!", true);
            return;
        }

        if (nationalId.isEmpty()) {
            nationalId = generateAutoSupplierNationalId();
        } else {
            String chkSql = "SELECT COUNT(*) FROM suppliers WHERE " + suppNidColumn + " = '" + escapeSql(nationalId) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showSupplierNotification("الرقم القومي (" + nationalId + ") مسجل مسبقًا لمورد آخر في قاعدة البيانات!", true);
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        }

        String sql = "INSERT INTO suppliers ("
                + suppNidColumn + ", "
                + suppNameColumn + ", "
                + suppPhoneColumn + ", "
                + suppAddressColumn + ", "
                + suppAccountColumn + ") VALUES ("
                + "'" + escapeSql(nationalId) + "', "
                + "'" + escapeSql(name) + "', "
                + getSqlNullable(phone) + ", "
                + getSqlNullable(address) + ", "
                + getSqlNullable(account) + ");";

        int res = DBConnection.executeUpdate(sql);
        loadSuppliersFromDatabase(txtSuppSearch != null ? txtSuppSearch.getText() : null);
        handleClearSupplierFields(null);

        showSupplierNotification("تمت إضافة المورد (" + name + ") بنجاح إلى قاعدة البيانات! ✔", false);
    }

    @FXML
    private void handleEditSupplier(ActionEvent event) {
        SupplierModel selected = tblSuppliers != null ? tblSuppliers.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showSupplierNotification("يرجى اختيار مورد من الجدول لتعديله!", true);
            return;
        }

        String name = getSafeText(txtSuppName);
        String nationalId = getSafeText(txtSuppNationalId);
        String phone = getSafeText(txtSuppPhone);
        String address = getSafeText(txtSuppAddress);
        String account = getSafeText(txtSuppAccount);

        if (name.isEmpty() || phone.isEmpty()) {
            showSupplierNotification("لا يمكن ترك اسم المورد أو رقم الهاتف فارغًا!", true);
            return;
        }

        if (nationalId.isEmpty()) {
            nationalId = selected.getNationalId();
        }

        String oldNid = selected.getNationalId();
        String oldName = selected.getName();

        String sql = "UPDATE suppliers SET "
                + suppNidColumn + " = '" + escapeSql(nationalId) + "', "
                + suppNameColumn + " = '" + escapeSql(name) + "', "
                + suppPhoneColumn + " = " + getSqlNullable(phone) + ", "
                + suppAddressColumn + " = " + getSqlNullable(address) + ", "
                + suppAccountColumn + " = " + getSqlNullable(account)
                + " WHERE " + suppNidColumn + " = '" + escapeSql(oldNid) + "' OR " + suppNameColumn + " = '" + escapeSql(oldName) + "';";

        DBConnection.executeUpdate(sql);

        // If supplier name changed, update referenced records in inventory
        if (!oldName.equals(name) && invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
            String updInv = "UPDATE inventory SET " + invSupplierColumn + " = '" + escapeSql(name) + "' WHERE " + invSupplierColumn + " = '" + escapeSql(oldName) + "';";
            DBConnection.executeUpdate(updInv);
        }

        loadSuppliersFromDatabase(txtSuppSearch != null ? txtSuppSearch.getText() : null);
        showSupplierNotification("تم حفظ وتحديث بيانات المورد بنجاح في قاعدة البيانات! ✔", false);
    }

    @FXML
    private void handleDeleteSupplier(ActionEvent event) {
        SupplierModel selected = tblSuppliers != null ? tblSuppliers.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showSupplierNotification("يرجى اختيار مورد من الجدول لحذفه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد حذف المورد");
        confirm.setHeaderText("حذف المورد: " + selected.getName());
        confirm.setContentText("هل أنت متأكد من حذف هذا المورد نهائيًا من قاعدة البيانات؟");
        confirm.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = selected.getName();
            String nid = selected.getNationalId();
            String sql = "DELETE FROM suppliers WHERE " + suppNidColumn + " = '" + escapeSql(nid) + "' OR " + suppNameColumn + " = '" + escapeSql(name) + "';";
            DBConnection.executeUpdate(sql);

            loadSuppliersFromDatabase(txtSuppSearch != null ? txtSuppSearch.getText() : null);
            handleClearSupplierFields(null);
            showSupplierNotification("تم حذف المورد (" + name + ") نهائيًا من قاعدة البيانات! 🗑️", false);
        }
    }

    @FXML
    private void handleClearSupplierFields(ActionEvent event) {
        if (txtSuppName != null) txtSuppName.clear();
        if (txtSuppNationalId != null) txtSuppNationalId.clear();
        if (txtSuppPhone != null) txtSuppPhone.clear();
        if (txtSuppAddress != null) txtSuppAddress.clear();
        if (txtSuppAccount != null) txtSuppAccount.clear();

        if (tblSuppliers != null) tblSuppliers.getSelectionModel().clearSelection();
        if (lblSuppFormStatus != null) lblSuppFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال مورد جديد.");
    }

    @FXML
    private void handleSupplierSearch(ActionEvent event) {
        loadSuppliersFromDatabase(txtSuppSearch != null ? txtSuppSearch.getText() : "");
    }

    @FXML
    private void handleClearSupplierSearch(ActionEvent event) {
        if (txtSuppSearch != null) txtSuppSearch.clear();
        loadSuppliersFromDatabase(null);
    }

    // =========================================================================
    // Tab 3: Supplement Process Log CRUD Actions Against Database
    // =========================================================================

    @FXML
    private void handleAddProc(ActionEvent event) {
        String prodName = getSafeText(txtProcProduct);
        String category = cbProcCategory != null ? cbProcCategory.getValue() : null;
        if (OPTION_MANAGE_CATEGORIES.equals(category)) {
            category = "";
        }
        String suppName = cbProcSupplier != null ? cbProcSupplier.getValue() : null;
        LocalDate date = dpProcDate != null ? dpProcDate.getValue() : LocalDate.now();
        String unit = cbProcUnit != null ? cbProcUnit.getValue() : "قطعة";
        String qtyStr = getSafeText(txtProcQty);
        String unitPriceStr = getSafeText(txtProcUnitPrice);

        if (prodName.isEmpty() || suppName == null || suppName.trim().isEmpty() 
                || qtyStr.isEmpty() || unitPriceStr.isEmpty()) {
            showProcNotification("يرجى ملء جميع الحقول الإجبارية لعملية التوريد!", true);
            return;
        }

        if (OPTION_MANAGE_UNITS.equals(unit)) {
            showProcNotification("يرجى اختيار وحدة صالحة!", true);
            return;
        }

        double qty = 0, unitPrice = 0;
        try {
            qty = Double.parseDouble(qtyStr);
            unitPrice = Double.parseDouble(unitPriceStr.replace("ج.م", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            showProcNotification("يرجى إدخال أرقام صحيحة للكمية وسعر الوحدة!", true);
            return;
        }

        // 1. Ensure foreign key dependencies exist (units, suppliers, categories, products)
        ensureUnitExistsInDatabase(unit);
        ensureSupplierExistsInDatabase(suppName);
        if (category != null && !category.trim().isEmpty()) {
            ensureCategoryExistsInDatabase(category.trim());
        }
        String prodId = ensureProductExistsInProductsTable(prodName, unit, unitPrice);

        // 2. Insert into 'supplements' table in MySQL database
        String dateStr = date != null ? date.toString() : LocalDate.now().toString();

        List<String> cols = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        String actualProdIdCol = getActualColumnName(existingSupplementColumns, suppProcProdIdColumn, "product_id");
        String actualProdNameCol = getActualColumnName(existingSupplementColumns, suppProcProdNameColumn, "product_name");
        String actualSuppNameCol = getActualColumnName(existingSupplementColumns, suppProcSuppNameColumn, "supplier_name");
        String actualDateCol = getActualColumnName(existingSupplementColumns, suppProcDateColumn, "date_of_supplement");
        String actualUnitCol = getActualColumnName(existingSupplementColumns, suppProcUnitColumn, "unit");
        String actualQtyCol = getActualColumnName(existingSupplementColumns, suppProcQtyColumn, "quantity");
        String actualUnitPriceCol = getActualColumnName(existingSupplementColumns, suppProcUnitPriceColumn, "unit_price");

        if (hasColumn(existingSupplementColumns, actualProdIdCol) || existingSupplementColumns.isEmpty()) {
            cols.add(actualProdIdCol);
            if (suppProcProdIdIsInteger) {
                String numOnly = prodId.replaceAll("[^0-9]", "");
                vals.add(!numOnly.isEmpty() ? numOnly : "0");
            } else {
                vals.add("'" + escapeSql(prodId) + "'");
            }
        }
        cols.add(actualProdNameCol);
        vals.add("'" + escapeSql(prodName) + "'");

        cols.add(actualSuppNameCol);
        vals.add("'" + escapeSql(suppName) + "'");

        cols.add(actualDateCol);
        vals.add("'" + escapeSql(dateStr) + "'");

        cols.add(actualUnitCol);
        vals.add("'" + escapeSql(unit) + "'");

        cols.add(actualQtyCol);
        vals.add(String.valueOf(qty));

        cols.add(actualUnitPriceCol);
        vals.add(String.valueOf(unitPrice));

        if (suppProcTotalPriceColumn != null && hasColumn(existingSupplementColumns, suppProcTotalPriceColumn)) {
            cols.add(suppProcTotalPriceColumn);
            vals.add(String.valueOf(qty * unitPrice));
        }

        String insSql = "INSERT INTO supplements (" + String.join(", ", cols) + ") VALUES (" + String.join(", ", vals) + ");";
        DBConnection.executeUpdate(insSql);

        // 3. Update or Insert into 'inventory' table:
        // If a new supplement added, and it is the same supplier add the new supplied quantity to the available quantity to the existing product, but if the supplier is different handle it as a different product
        String existingRef = null;
        if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
            String checkSql = "SELECT " + invRefColumn + " FROM inventory WHERE " 
                    + invNameColumn + " = '" + escapeSql(prodName.trim()) + "' AND " 
                    + invSupplierColumn + " = '" + escapeSql(suppName.trim()) + "' LIMIT 1;";
            ResultSet rsCheck = DBConnection.executeQuery(checkSql);
            if (rsCheck != null) {
                try {
                    if (rsCheck.next()) {
                        existingRef = rsCheck.getString(1);
                    }
                    rsCheck.close();
                } catch (SQLException ignored) {}
            }
        } else {
            String checkSql = "SELECT " + suppProcProdIdColumn + " FROM supplements WHERE " 
                    + suppProcProdNameColumn + " = '" + escapeSql(prodName.trim()) + "' AND " 
                    + suppProcSuppNameColumn + " = '" + escapeSql(suppName.trim()) + "' LIMIT 1;";
            ResultSet rsCheck = DBConnection.executeQuery(checkSql);
            if (rsCheck != null) {
                try {
                    if (rsCheck.next()) {
                        String prevRef = rsCheck.getString(1);
                        if (prevRef != null && !prevRef.trim().isEmpty()) {
                            String chkInv = "SELECT " + invRefColumn + " FROM inventory WHERE " + invRefColumn + " = '" + escapeSql(prevRef.trim()) + "' LIMIT 1;";
                            ResultSet rsInv = DBConnection.executeQuery(chkInv);
                            if (rsInv != null) {
                                if (rsInv.next()) {
                                    existingRef = rsInv.getString(1);
                                }
                                rsInv.close();
                            }
                        }
                    }
                    rsCheck.close();
                } catch (SQLException ignored) {}
            }
        }

        if (existingRef != null) {
            // Same supplier: add the new supplied quantity to the available quantity of the existing product
            List<String> invSets = new ArrayList<>();
            if (hasColumn(existingInventoryColumns, invQtyColumn) || existingInventoryColumns.isEmpty()) {
                invSets.add(invQtyColumn + " = " + invQtyColumn + " + " + qty);
            }
            if (category != null && !category.trim().isEmpty() && (hasColumn(existingInventoryColumns, invCategoryColumn) || existingInventoryColumns.isEmpty())) {
                invSets.add(invCategoryColumn + " = '" + escapeSql(category.trim()) + "'");
            }
            if (hasColumn(existingInventoryColumns, invCostPriceColumn) || existingInventoryColumns.isEmpty()) {
                invSets.add(invCostPriceColumn + " = " + unitPrice);
            }
            if (unit != null && !unit.trim().isEmpty() && (hasColumn(existingInventoryColumns, invUnitColumn) || existingInventoryColumns.isEmpty())) {
                invSets.add(invUnitColumn + " = '" + escapeSql(unit) + "'");
            }
            if (invDateColumn != null && hasColumn(existingInventoryColumns, invDateColumn)) {
                invSets.add(invDateColumn + " = '" + escapeSql(dateStr) + "'");
            }
            if (!invSets.isEmpty()) {
                String updSql = "UPDATE inventory SET " + String.join(", ", invSets)
                        + " WHERE " + invRefColumn + " = '" + escapeSql(existingRef) + "';";
                DBConnection.executeUpdate(updSql);
            }
        } else {
            // Different supplier (or brand new product): handle it as a different product with its own reference_no!
            String autoRef = generateAutoReferenceNo();
            List<String> invCols = new ArrayList<>();
            List<String> invVals = new ArrayList<>();

            if (hasColumn(existingInventoryColumns, invNameColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invNameColumn);
                invVals.add("'" + escapeSql(prodName.trim()) + "'");
            }
            if (hasColumn(existingInventoryColumns, invRefColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invRefColumn);
                invVals.add("'" + escapeSql(autoRef) + "'");
            }
            if (invSupplierColumn != null && (hasColumn(existingInventoryColumns, invSupplierColumn) || existingInventoryColumns.isEmpty())) {
                invCols.add(invSupplierColumn);
                invVals.add("'" + escapeSql(suppName.trim()) + "'");
            }
            if (category != null && !category.trim().isEmpty() && (hasColumn(existingInventoryColumns, invCategoryColumn) || existingInventoryColumns.isEmpty())) {
                invCols.add(invCategoryColumn);
                invVals.add("'" + escapeSql(category.trim()) + "'");
            }
            if (hasColumn(existingInventoryColumns, invQtyColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invQtyColumn);
                invVals.add(String.valueOf(qty)); // starts with 0 and increases by supplied qty => qty
            }
            if (hasColumn(existingInventoryColumns, invCostPriceColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invCostPriceColumn);
                invVals.add(String.valueOf(unitPrice));
            }
            if (hasColumn(existingInventoryColumns, invUnitColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invUnitColumn);
                invVals.add("'" + escapeSql(unit) + "'");
            }
            if (hasColumn(existingInventoryColumns, invMinQtyColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invMinQtyColumn);
                invVals.add("0");
            }
            if (hasColumn(existingInventoryColumns, invSellPriceColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invSellPriceColumn);
                invVals.add("0");
            }
            if (hasColumn(existingInventoryColumns, invShowMenuColumn) || existingInventoryColumns.isEmpty()) {
                invCols.add(invShowMenuColumn);
                invVals.add("1");
            }
            if (invDateColumn != null && hasColumn(existingInventoryColumns, invDateColumn)) {
                invCols.add(invDateColumn);
                invVals.add("'" + escapeSql(dateStr) + "'");
            }

            String insInv = "INSERT INTO inventory (" + String.join(", ", invCols) + ") VALUES (" + String.join(", ", invVals) + ");";
            DBConnection.executeUpdate(insInv);
        }

        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        loadSupplementsFromDatabase(null);
        loadInternalProductChoices();
        handleClearProcFields(null);
        showProcNotification("تم تسجيل وحفظ عملية التوريد وتحديث المخزن بنجاح! ✔", false);
    }

    @FXML
    private void handleEditProc(ActionEvent event) {
        SupplementProcessModel selected = tblSupplements != null ? tblSupplements.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProcNotification("يرجى اختيار عملية توريد من الجدول لتعديلها!", true);
            return;
        }

        String prodName = getSafeText(txtProcProduct);
        if (prodName.isEmpty() && selected != null) {
            prodName = selected.getProductName();
        }
        String category = cbProcCategory != null ? cbProcCategory.getValue() : null;
        if (OPTION_MANAGE_CATEGORIES.equals(category)) category = "";
        String suppName = cbProcSupplier != null ? cbProcSupplier.getValue() : selected.getSupplierName();
        LocalDate date = dpProcDate != null ? dpProcDate.getValue() : null;
        String unit = cbProcUnit != null ? cbProcUnit.getValue() : selected.getUnit();
        String qtyStr = getSafeText(txtProcQty);
        String unitPriceStr = getSafeText(txtProcUnitPrice);

        if (prodName.isEmpty() || suppName == null || suppName.trim().isEmpty() 
                || qtyStr.isEmpty() || unitPriceStr.isEmpty()) {
            showProcNotification("يرجى ملء جميع الحقول الإجبارية لعملية التوريد!", true);
            return;
        }

        if (OPTION_MANAGE_UNITS.equals(unit)) {
            showProcNotification("يرجى اختيار وحدة صالحة!", true);
            return;
        }

        double qty = 0, unitPrice = 0;
        try {
            qty = Double.parseDouble(qtyStr);
            unitPrice = Double.parseDouble(unitPriceStr.replace("ج.م", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            showProcNotification("يرجى إدخال أرقام صحيحة للكمية وسعر الوحدة!", true);
            return;
        }

        // Ensure foreign key dependencies exist (units, suppliers, products)
        ensureUnitExistsInDatabase(unit);
        ensureSupplierExistsInDatabase(suppName);
        if (category != null && !category.trim().isEmpty()) {
            ensureCategoryExistsInDatabase(category.trim());
            if (hasColumn(existingInventoryColumns, invCategoryColumn)) {
                if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
                    DBConnection.executeUpdate("UPDATE inventory SET " + invCategoryColumn + " = '" + escapeSql(category.trim()) + "' WHERE " + invNameColumn + " = '" + escapeSql(prodName.trim()) + "' AND " + invSupplierColumn + " = '" + escapeSql(suppName.trim()) + "';");
                } else {
                    DBConnection.executeUpdate("UPDATE inventory SET " + invCategoryColumn + " = '" + escapeSql(category.trim()) + "' WHERE " + invNameColumn + " = '" + escapeSql(prodName.trim()) + "';");
                }
            }
        }
        String prodId = ensureProductExistsInProductsTable(prodName, unit, unitPrice);

        String dateStr = date != null ? date.toString() : selected.getDate();
        String actualProdIdCol = getActualColumnName(existingSupplementColumns, suppProcProdIdColumn, "product_id");
        String actualProdNameCol = getActualColumnName(existingSupplementColumns, suppProcProdNameColumn, "product_name");
        String actualSuppNameCol = getActualColumnName(existingSupplementColumns, suppProcSuppNameColumn, "supplier_name");
        String actualDateCol = getActualColumnName(existingSupplementColumns, suppProcDateColumn, "date_of_supplement");
        String actualUnitCol = getActualColumnName(existingSupplementColumns, suppProcUnitColumn, "unit");
        String actualQtyCol = getActualColumnName(existingSupplementColumns, suppProcQtyColumn, "quantity");
        String actualUnitPriceCol = getActualColumnName(existingSupplementColumns, suppProcUnitPriceColumn, "unit_price");

        List<String> sets = new ArrayList<>();

        if (hasColumn(existingSupplementColumns, actualProdIdCol) || existingSupplementColumns.isEmpty()) {
            if (suppProcProdIdIsInteger) {
                String numOnly = prodId.replaceAll("[^0-9]", "");
                sets.add(actualProdIdCol + " = " + (!numOnly.isEmpty() ? numOnly : "0"));
            } else {
                sets.add(actualProdIdCol + " = '" + escapeSql(prodId) + "'");
            }
        }
        sets.add(actualProdNameCol + " = '" + escapeSql(prodName) + "'");
        sets.add(actualSuppNameCol + " = '" + escapeSql(suppName) + "'");
        sets.add(actualDateCol + " = '" + escapeSql(dateStr) + "'");
        sets.add(actualUnitCol + " = '" + escapeSql(unit) + "'");
        sets.add(actualQtyCol + " = " + qty);
        sets.add(actualUnitPriceCol + " = " + unitPrice);

        if (suppProcTotalPriceColumn != null && hasColumn(existingSupplementColumns, suppProcTotalPriceColumn)) {
            sets.add(suppProcTotalPriceColumn + " = " + (qty * unitPrice));
        }

        StringBuilder updSql = new StringBuilder("UPDATE supplements SET ");
        updSql.append(String.join(", ", sets));

        if (selected.getDbId() > 0 && hasColumn(existingSupplementColumns, suppProcIdColumn)) {
            updSql.append(" WHERE ").append(suppProcIdColumn).append(" = ").append(selected.getDbId()).append(";");
        } else {
            updSql.append(" WHERE ").append(actualProdNameCol).append(" = '").append(escapeSql(selected.getProductName())).append("'")
                  .append(" AND ").append(actualDateCol).append(" = '").append(escapeSql(selected.getDate())).append("' LIMIT 1;");
        }

        DBConnection.executeUpdate(updSql.toString());
        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        loadSupplementsFromDatabase(null);
        showProcNotification("تم حفظ وتحديث عملية التوريد في قاعدة البيانات بنجاح! ✔", false);
    }

    @FXML
    private void handleDeleteProc(ActionEvent event) {
        SupplementProcessModel selected = tblSupplements != null ? tblSupplements.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProcNotification("يرجى اختيار عملية من الجدول لحذفها!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد حذف عملية التوريد");
        confirm.setHeaderText("حذف عملية توريد: " + selected.getProductName());
        confirm.setContentText("هل أنت متأكد من حذف هذا السجل نهائيًا من قاعدة البيانات؟");
        confirm.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String actualProdNameCol = getActualColumnName(existingSupplementColumns, suppProcProdNameColumn, "product_name");
            String actualDateCol = getActualColumnName(existingSupplementColumns, suppProcDateColumn, "date_of_supplement");

            String delSql;
            if (selected.getDbId() > 0 && hasColumn(existingSupplementColumns, suppProcIdColumn)) {
                delSql = "DELETE FROM supplements WHERE " + suppProcIdColumn + " = " + selected.getDbId() + ";";
            } else {
                delSql = "DELETE FROM supplements WHERE " + actualProdNameCol + " = '" + escapeSql(selected.getProductName()) + "' AND " + actualDateCol + " = '" + escapeSql(selected.getDate()) + "' LIMIT 1;";
            }
            DBConnection.executeUpdate(delSql);

            // Decrement available_quantity in inventory for this exact product & supplier
            if (hasColumn(existingInventoryColumns, invQtyColumn)) {
                if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
                    DBConnection.executeUpdate("UPDATE inventory SET " + invQtyColumn + " = GREATEST(0, " + invQtyColumn + " - " + selected.getQuantity() + ") WHERE " + invNameColumn + " = '" + escapeSql(selected.getProductName()) + "' AND " + invSupplierColumn + " = '" + escapeSql(selected.getSupplierName()) + "';");
                } else {
                    DBConnection.executeUpdate("UPDATE inventory SET " + invQtyColumn + " = GREATEST(0, " + invQtyColumn + " - " + selected.getQuantity() + ") WHERE " + invNameColumn + " = '" + escapeSql(selected.getProductName()) + "';");
                }
            }

            loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
            loadSupplementsFromDatabase(null);
            handleClearProcFields(null);
            showProcNotification("تم حذف سجل عملية التوريد من قاعدة البيانات بنجاح! 🗑️", false);
        }
    }

    @FXML
    private void handleClearProcFields(ActionEvent event) {
        if (txtProcProduct != null) txtProcProduct.clear();
        if (cbProcCategory != null) cbProcCategory.setValue(null);
        if (cbProcSupplier != null) cbProcSupplier.setValue(null);
        if (dpProcDate != null) dpProcDate.setValue(LocalDate.now());
        if (cbProcUnit != null) cbProcUnit.setValue(null);
        if (txtProcQty != null) txtProcQty.clear();
        if (txtProcUnitPrice != null) txtProcUnitPrice.clear();

        if (tblSupplements != null) tblSupplements.getSelectionModel().clearSelection();
        if (lblProcFormStatus != null) lblProcFormStatus.setText("تم تفريغ الحقول. جاهز لتسجيل عملية توريد جديدة.");
    }

    @FXML
    private void handleProcSearch(ActionEvent event) {
        loadSupplementsFromDatabase(txtProcSearch != null ? txtProcSearch.getText() : null);
    }

    @FXML
    private void handleClearProcSearch(ActionEvent event) {
        if (txtProcSearch != null) txtProcSearch.clear();
        loadSupplementsFromDatabase(null);
    }

    // =========================================================================
    // Tab 4: Internal Supplements Operations
    // =========================================================================

    /**
     * Loads departments from MySQL database table 'depts' into departmentsList.
     */
    private void loadDepartmentsFromDatabase() {
        departmentsList.clear();
        departmentsList.add(OPTION_MANAGE_DEPTS);

        String query = "SELECT dept_name FROM depts ORDER BY dept_name ASC;";
        ResultSet rs = DBConnection.executeQuery(query);
        if (rs != null) {
            try {
                while (rs.next()) {
                    String d = rs.getString(1);
                    if (d != null && !d.trim().isEmpty() && !departmentsList.contains(d.trim())) {
                        departmentsList.add(d.trim());
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (cbInternalDept != null) {
            cbInternalDept.setItems(departmentsList);
        }
    }

    /**
     * Populates cbInternalProduct with products that have already been supplied through supplements.
     */
    private void loadInternalProductChoices() {
        syncDatabaseInventoryStockWithSupplements();
        String currentSelection = cbInternalProduct != null ? cbInternalProduct.getValue() : null;
        internalProductsList.clear();

        // Get distinct product names from supplements table
        String suppNameCol = (suppProcProdNameColumn != null && !suppProcProdNameColumn.isEmpty()) ? suppProcProdNameColumn : "product_name";
        String sql = "SELECT DISTINCT " + suppNameCol + " FROM supplements WHERE "
                + suppNameCol + " IS NOT NULL AND " + suppNameCol + " != '' ORDER BY " + suppNameCol + " ASC;";
        ResultSet rs = DBConnection.executeQuery(sql);
        if (rs != null) {
            try {
                while (rs.next()) {
                    String p = rs.getString(1);
                    if (p != null && !p.trim().isEmpty()) {
                        String clean = p.trim();
                        if (clean.contains(" (") && clean.endsWith(")")) {
                            clean = clean.substring(0, clean.lastIndexOf(" (")).trim();
                        }
                        if (!internalProductsList.contains(clean)) {
                            internalProductsList.add(clean);
                        }
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        // Also check if any exist in productList / inventory that were supplied
        for (ProductModel pm : productList) {
            if (pm.getName() != null && !pm.getName().trim().isEmpty()) {
                String clean = pm.getName().trim();
                if (clean.contains(" (") && clean.endsWith(")")) {
                    clean = clean.substring(0, clean.lastIndexOf(" (")).trim();
                }
                if (!internalProductsList.contains(clean)) {
                    internalProductsList.add(clean);
                }
            }
        }

        if (cbInternalProduct != null) {
            cbInternalProduct.setItems(internalProductsList);
            if (currentSelection != null && internalProductsList.contains(currentSelection)) {
                cbInternalProduct.setValue(currentSelection);
                updateInternalAvailableStockDisplay(currentSelection);
            }
        }
    }

    /**
     * Sets up listeners for Tab 4 (Internal Supplements).
     */
    private void setupInternalSupplementsListeners() {
        // 1. Department ComboBox listener for 'Manage Depts'
        if (cbInternalDept != null) {
            cbInternalDept.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_DEPTS.equals(newVal)) {
                    Platform.runLater(() -> {
                        cbInternalDept.setValue(oldVal != null && !OPTION_MANAGE_DEPTS.equals(oldVal) ? oldVal : null);
                        openDepartmentManagerDialog();
                    });
                }
            });
        }

        // 2. Product ComboBox listener to update available stock display
        if (cbInternalProduct != null) {
            cbInternalProduct.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateInternalAvailableStockDisplay(newVal);
            });
        }

        // 3. TableView selection listener
        if (tblInternalSupplements != null) {
            tblInternalSupplements.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (cbInternalProduct != null) cbInternalProduct.setValue(newVal.getProductName());
                    if (cbInternalDept != null) cbInternalDept.setValue(newVal.getDeptName());
                    if (txtInternalQty != null) txtInternalQty.setText(String.valueOf(newVal.getQuantity()));
                    if (dpInternalDate != null && newVal.getDate() != null) {
                        try { dpInternalDate.setValue(LocalDate.parse(newVal.getDate())); } catch (Exception ignored) {}
                    }
                    if (txtInternalNotes != null) txtInternalNotes.setText(newVal.getNotes());
                    updateInternalAvailableStockDisplay(newVal.getProductName());
                    if (lblInternalFormStatus != null) {
                        lblInternalFormStatus.setText("تم تحديد عملية توريد داخلي: " + newVal.getProductName() + " للقسم: " + newVal.getDeptName());
                    }
                }
            });
        }

        // Set default date to today
        if (dpInternalDate != null && dpInternalDate.getValue() == null) {
            dpInternalDate.setValue(LocalDate.now());
        }
    }

    /**
     * Helper class holding resolved stock information for a product.
     */
    public static class StockInfo {
        private final double quantity;
        private final String unit;
        private final String cleanName;

        public StockInfo(double quantity, String unit, String cleanName) {
            this.quantity = quantity;
            this.unit = unit != null && !unit.trim().isEmpty() ? unit.trim() : "قطعة";
            this.cleanName = cleanName != null ? cleanName.trim() : "";
        }

        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public String getCleanName() { return cleanName; }
    }

    /**
     * Accurately determines the total available quantity for any product from the MySQL database.
     * Checks:
     * 1. Total available_quantity in table 'inventory'.
     * 2. Sum of all supplied quantities in 'supplements' minus total dispatched in 'internal_supplements'.
     * Synchronizes table 'inventory' automatically if it was zero or uninitialized.
     */
    private StockInfo getDatabaseTotalAvailableQuantity(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return new StockInfo(0.0, "قطعة", "");
        }

        String cleanName = productName.trim();
        if (cleanName.contains(" (") && cleanName.endsWith(")")) {
            cleanName = cleanName.substring(0, cleanName.lastIndexOf(" (")).trim();
        }

        String unit = "قطعة";
        double invStock = 0.0;
        boolean foundInInventory = false;

        // 1. Query 'inventory' table safely using SELECT * to avoid unknown column errors
        String invNameCol = (invNameColumn != null && !invNameColumn.isEmpty()) ? invNameColumn : "product_name";
        String sqlInv = "SELECT * FROM inventory WHERE LOWER(TRIM(" + invNameCol + ")) = LOWER(TRIM('" + escapeSql(cleanName) + "'));";
        ResultSet rsInv = DBConnection.executeQuery(sqlInv);
        if (rsInv != null) {
            try {
                while (rsInv.next()) {
                    foundInInventory = true;
                    double q = getColumnDoubleSafe(rsInv, invQtyColumn, "available_quantity", "quantity", "qty", "stock");
                    invStock += q;
                    String u = getColumnStringSafe(rsInv, invUnitColumn, "product_unit", "unit", "unit_name");
                    if (u != null && !u.trim().isEmpty()) {
                        unit = u.trim();
                    }
                }
                rsInv.close();
            } catch (SQLException ignored) {}
        }

        // 2. Query 'supplements' table for total quantity supplied
        double totalSupplied = 0.0;
        String suppNameCol = (suppProcProdNameColumn != null && !suppProcProdNameColumn.isEmpty()) ? suppProcProdNameColumn : "product_name";
        String suppQtyCol = (suppProcQtyColumn != null && !suppProcQtyColumn.isEmpty()) ? suppProcQtyColumn : "quantity";
        String suppUnitCol = (suppProcUnitColumn != null && !suppProcUnitColumn.isEmpty()) ? suppProcUnitColumn : "unit";

        String sqlSupp = "SELECT * FROM supplements WHERE LOWER(TRIM(" + suppNameCol + ")) = LOWER(TRIM('" + escapeSql(cleanName) + "'));";
        ResultSet rsSupp = DBConnection.executeQuery(sqlSupp);
        if (rsSupp != null) {
            try {
                while (rsSupp.next()) {
                    double q = getColumnDoubleSafe(rsSupp, suppQtyCol, "quantity", "qty", "amount");
                    totalSupplied += q;
                    String u = getColumnStringSafe(rsSupp, suppUnitCol, "unit", "product_unit", "proc_unit");
                    if (u != null && !u.trim().isEmpty() && "قطعة".equals(unit)) {
                        unit = u.trim();
                    }
                }
                rsSupp.close();
            } catch (SQLException ignored) {}
        }

        // 3. Query 'internal_supplements' table for total quantity dispatched to departments
        double totalDispatched = 0.0;
        String sqlInt = "SELECT * FROM internal_supplements WHERE LOWER(TRIM(product_name)) = LOWER(TRIM('" + escapeSql(cleanName) + "'));";
        ResultSet rsInt = DBConnection.executeQuery(sqlInt);
        if (rsInt != null) {
            try {
                while (rsInt.next()) {
                    double q = getColumnDoubleSafe(rsInt, "quantity", "qty", "amount");
                    totalDispatched += q;
                    String u = getColumnStringSafe(rsInt, "unit", "product_unit");
                    if (u != null && !u.trim().isEmpty() && "قطعة".equals(unit)) {
                        unit = u.trim();
                    }
                }
                rsInt.close();
            } catch (SQLException ignored) {}
        }

        double operationalNetStock = Math.max(0, totalSupplied - totalDispatched);

        // 4. Resolve definitive total available quantity:
        double finalStock;
        if (invStock > 0) {
            finalStock = invStock;
        } else if (operationalNetStock > 0) {
            // Inventory was 0 or uninitialized despite having supplement records in DB -> sync it!
            finalStock = operationalNetStock;
            if (foundInInventory) {
                String upd = "UPDATE inventory SET " + invQtyColumn + " = " + operationalNetStock
                        + " WHERE LOWER(TRIM(" + invNameCol + ")) = LOWER(TRIM('" + escapeSql(cleanName) + "')) LIMIT 1;";
                DBConnection.executeUpdate(upd);
            } else {
                String autoRef = generateAutoReferenceNo();
                String ins = "INSERT INTO inventory (" + invRefColumn + ", " + invNameCol + ", " + invQtyColumn + ", " + invUnitColumn + ") VALUES ("
                        + "'" + escapeSql(autoRef) + "', '" + escapeSql(cleanName) + "', " + operationalNetStock + ", '" + escapeSql(unit) + "');";
                DBConnection.executeUpdate(ins);
            }
        } else {
            // Fallback to in-memory productList
            double listQty = 0;
            for (ProductModel pm : productList) {
                if (pm.getName() != null && pm.getName().trim().equalsIgnoreCase(cleanName)) {
                    listQty += pm.getQuantity();
                    if (pm.getUnit() != null && !pm.getUnit().trim().isEmpty()) {
                        unit = pm.getUnit().trim();
                    }
                }
            }
            finalStock = Math.max(invStock, listQty);
        }

        return new StockInfo(finalStock, unit, cleanName);
    }

    /**
     * Synchronizes table 'inventory' available_quantity with all supplied supplements minus internal supplements.
     * If any product has zero available_quantity in inventory despite having records in supplements,
     * this automatically calculates and restores the true total available quantity in database.
     */
    private void syncDatabaseInventoryStockWithSupplements() {
        try {
            String suppNameCol = (suppProcProdNameColumn != null && !suppProcProdNameColumn.isEmpty()) ? suppProcProdNameColumn : "product_name";
            String suppQtyCol = (suppProcQtyColumn != null && !suppProcQtyColumn.isEmpty()) ? suppProcQtyColumn : "quantity";
            String suppUnitCol = (suppProcUnitColumn != null && !suppProcUnitColumn.isEmpty()) ? suppProcUnitColumn : "unit";
            String invNameCol = (invNameColumn != null && !invNameColumn.isEmpty()) ? invNameColumn : "product_name";

            // Get total supplied per product
            String sqlGroupSupp = "SELECT " + suppNameCol + ", SUM(" + suppQtyCol + ") as tot_qty, MAX(" + suppUnitCol + ") as u "
                    + "FROM supplements WHERE " + suppNameCol + " IS NOT NULL AND " + suppNameCol + " != '' "
                    + "GROUP BY " + suppNameCol + ";";
            ResultSet rsS = DBConnection.executeQuery(sqlGroupSupp);
            Map<String, Double> suppliedMap = new LinkedHashMap<>();
            Map<String, String> unitMap = new LinkedHashMap<>();
            if (rsS != null) {
                while (rsS.next()) {
                    String p = rsS.getString(1);
                    double q = rsS.getDouble(2);
                    String u = rsS.getString(3);
                    if (p != null && !p.trim().isEmpty()) {
                        suppliedMap.put(p.trim().toLowerCase(), q);
                        if (u != null && !u.trim().isEmpty()) {
                            unitMap.put(p.trim().toLowerCase(), u.trim());
                        }
                    }
                }
                rsS.close();
            }

            // Get total internal dispatched per product
            Map<String, Double> dispatchedMap = new LinkedHashMap<>();
            ResultSet rsD = DBConnection.executeQuery("SELECT product_name, SUM(quantity) FROM internal_supplements GROUP BY product_name;");
            if (rsD != null) {
                while (rsD.next()) {
                    String p = rsD.getString(1);
                    double q = rsD.getDouble(2);
                    if (p != null && !p.trim().isEmpty()) {
                        dispatchedMap.put(p.trim().toLowerCase(), q);
                    }
                }
                rsD.close();
            }

            // For each supplied product, check inventory available_quantity
            for (Map.Entry<String, Double> entry : suppliedMap.entrySet()) {
                String key = entry.getKey();
                double supplied = entry.getValue();
                double dispatched = dispatchedMap.getOrDefault(key, 0.0);
                double net = Math.max(0, supplied - dispatched);
                String u = unitMap.getOrDefault(key, "قطعة");

                String chkSql = "SELECT SUM(" + invQtyColumn + ") FROM inventory WHERE LOWER(TRIM(" + invNameCol + ")) = '" + escapeSql(key) + "';";
                ResultSet rsInv = DBConnection.executeQuery(chkSql);
                double currentInvQty = 0;
                boolean hasRow = false;
                if (rsInv != null) {
                    if (rsInv.next()) {
                        currentInvQty = rsInv.getDouble(1);
                        hasRow = true;
                    }
                    rsInv.close();
                }

                // If inventory has 0 (or row missing) but net > 0, sync it!
                if (currentInvQty <= 0 && net > 0) {
                    if (hasRow) {
                        String upd = "UPDATE inventory SET " + invQtyColumn + " = " + net
                                + " WHERE LOWER(TRIM(" + invNameCol + ")) = '" + escapeSql(key) + "' LIMIT 1;";
                        DBConnection.executeUpdate(upd);
                    } else {
                        String autoRef = generateAutoReferenceNo();
                        String ins = "INSERT INTO inventory (" + invRefColumn + ", " + invNameCol + ", " + invQtyColumn + ", " + invUnitColumn + ") VALUES ("
                                + "'" + escapeSql(autoRef) + "', '" + escapeSql(key) + "', " + net + ", '" + escapeSql(u) + "');";
                        DBConnection.executeUpdate(ins);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Updates the available stock indicator for a given product in Tab 4.
     */
    private void updateInternalAvailableStockDisplay(String productName) {
        if (lblInternalAvailableQty == null) return;

        StockInfo info = getDatabaseTotalAvailableQuantity(productName);

        if (info.getCleanName().isEmpty()) {
            lblInternalAvailableQty.setText("الرصيد المتاح: -");
            lblInternalAvailableQty.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E65100; -fx-padding: 8px 0 0 0;");
            return;
        }

        double totalAvail = info.getQuantity();
        String unit = info.getUnit();

        lblInternalAvailableQty.setText("الرصيد المتاح بالمخزن: " + String.format(Locale.US, "%,.2f", totalAvail) + " " + unit);
        if (totalAvail <= 0) {
            lblInternalAvailableQty.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #D32F2F; -fx-padding: 8px 0 0 0;");
        } else {
            lblInternalAvailableQty.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2E7D32; -fx-padding: 8px 0 0 0;");
        }
    }

    /**
     * Loads internal supplement records from database table 'internal_supplements'.
     */
    private void loadInternalSupplementsFromDatabase(String search) {
        internalSupplementsList.clear();
        StringBuilder sql = new StringBuilder("SELECT id, product_name, dept_name, quantity, unit, date_of_supplement, notes FROM internal_supplements");
        if (search != null && !search.trim().isEmpty()) {
            String q = escapeSql(search.trim());
            sql.append(" WHERE product_name LIKE '%").append(q).append("%'")
               .append(" OR dept_name LIKE '%").append(q).append("%'")
               .append(" OR date_of_supplement LIKE '%").append(q).append("%'")
               .append(" OR notes LIKE '%").append(q).append("%'");
        }
        sql.append(" ORDER BY id DESC;");

        ResultSet rs = DBConnection.executeQuery(sql.toString());
        int seq = 1;
        if (rs != null) {
            try {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String prod = rs.getString(2);
                    String dept = rs.getString(3);
                    double qty = rs.getDouble(4);
                    String unit = rs.getString(5);
                    String date = rs.getString(6);
                    String notes = rs.getString(7);

                    internalSupplementsList.add(new InternalSupplementModel(
                            seq++, id, date != null ? date : "",
                            prod != null ? prod : "",
                            dept != null ? dept : "",
                            qty,
                            unit != null ? unit : "قطعة",
                            notes != null ? notes : ""
                    ));
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (lblInternalTotalCount != null) {
            lblInternalTotalCount.setText(String.valueOf(internalSupplementsList.size()));
        }
        if (tblInternalSupplements != null) {
            tblInternalSupplements.refresh();
        }
    }

    /**
     * Records a new internal supplement process:
     * - Validates inputs.
     * - Validates that quantity <= available quantity in inventory.
     * - Deducts quantity from inventory available quantity.
     * - Inserts into internal_supplements table.
     */
    @FXML
    private void handleAddInternalSupplement(ActionEvent event) {
        String prodName = cbInternalProduct != null ? cbInternalProduct.getValue() : null;
        if (prodName == null || prodName.trim().isEmpty()) {
            showInternalNotification("يرجى اختيار الصنف المراد توريده وصرفه أولاً!", true);
            return;
        }

        String deptName = cbInternalDept != null ? cbInternalDept.getValue() : null;
        if (deptName == null || deptName.trim().isEmpty() || OPTION_MANAGE_DEPTS.equals(deptName)) {
            showInternalNotification("يرجى تحديد القسم المستلم للتوريد الداخلي!", true);
            return;
        }

        String qtyStr = getSafeText(txtInternalQty);
        if (qtyStr.isEmpty()) {
            showInternalNotification("يرجى إدخال الكمية المراد صرفها!", true);
            return;
        }

        double qty = 0;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            showInternalNotification("يرجى إدخال قيمة رقمية صحيحة للكمية!", true);
            return;
        }

        if (qty <= 0) {
            showInternalNotification("يجب أن تكون الكمية المصروفة أكبر من الصفر!", true);
            return;
        }

        LocalDate d = dpInternalDate != null && dpInternalDate.getValue() != null ? dpInternalDate.getValue() : LocalDate.now();
        String dateStr = d.toString();
        String notes = getSafeText(txtInternalNotes);

        // Fetch current available stock & unit for this product from database safely
        StockInfo stockInfo = getDatabaseTotalAvailableQuantity(prodName);
        double availableStock = stockInfo.getQuantity();
        String unit = stockInfo.getUnit();
        String cleanProdName = stockInfo.getCleanName();

        // VALIDATION: Must be <= available quantity in inventory
        if (qty > availableStock) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("رصيد المخزن غير كافٍ");
            alert.setHeaderText("الكمية المطلوبة تتجاوز الكمية المتاحة في المخزن!");
            alert.setContentText("الكمية المراد صرفها: " + qty + " " + unit + "\nالرصيد المتاح حاليًا بالمخزن: " + String.format(Locale.US, "%,.2f", availableStock) + " " + unit + "\n\nلا يمكن صرف كمية أكبر من الرصيد المتوفر.");
            alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            alert.showAndWait();
            return;
        }

        // 1. Deduct from inventory table
        String invNameCol = (invNameColumn != null && !invNameColumn.isEmpty()) ? invNameColumn : "product_name";
        String updSql = "UPDATE inventory SET " + invQtyColumn + " = GREATEST(0, " + invQtyColumn + " - " + qty + ") WHERE LOWER(TRIM(" + invNameCol + ")) = LOWER(TRIM('" + escapeSql(cleanProdName) + "')) LIMIT 1;";
        DBConnection.executeUpdate(updSql);

        // 2. Insert into internal_supplements table
        String insSql = "INSERT INTO internal_supplements (product_name, dept_name, quantity, unit, date_of_supplement, notes) VALUES ("
                + "'" + escapeSql(cleanProdName) + "', "
                + "'" + escapeSql(deptName.trim()) + "', "
                + qty + ", "
                + "'" + escapeSql(unit) + "', "
                + "'" + escapeSql(dateStr) + "', "
                + "'" + escapeSql(notes) + "');";
        DBConnection.executeUpdate(insSql);

        // 3. Refresh data across tabs
        loadInternalSupplementsFromDatabase(null);
        loadProductsFromDatabase(null);
        updateInternalAvailableStockDisplay(cleanProdName);
        handleClearInternalFields(null);

        showInternalNotification("تم تسجيل عملية التوريد الداخلي للقسم (" + deptName + ") وخصم الكمية من المخزن بنجاح! ✔", false);
    }

    /**
     * Deletes an internal supplement record and restores the deducted quantity back to inventory.
     */
    @FXML
    private void handleDeleteInternalSupplement(ActionEvent event) {
        InternalSupplementModel selected = tblInternalSupplements != null ? tblInternalSupplements.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showInternalNotification("يرجى تحديد عملية توريد داخلي من الجدول أولاً لحذفها!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد حذف عملية توريد داخلي");
        confirm.setHeaderText("هل أنت متأكد من حذف هذه العملية واسترجاع الكمية إلى المخزن؟");
        confirm.setContentText("الصنف: " + selected.getProductName() + "\nالقسم: " + selected.getDeptName() + "\nالكمية: " + selected.getQuantity() + " " + selected.getUnit());
        confirm.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            String cleanProdName = selected.getProductName().trim();
            if (cleanProdName.contains(" (") && cleanProdName.endsWith(")")) {
                cleanProdName = cleanProdName.substring(0, cleanProdName.lastIndexOf(" (")).trim();
            }

            // Restore quantity in inventory
            String invNameCol = (invNameColumn != null && !invNameColumn.isEmpty()) ? invNameColumn : "product_name";
            String restoreSql = "UPDATE inventory SET " + invQtyColumn + " = " + invQtyColumn + " + " + selected.getQuantity()
                    + " WHERE LOWER(TRIM(" + invNameCol + ")) = LOWER(TRIM('" + escapeSql(cleanProdName) + "')) LIMIT 1;";
            DBConnection.executeUpdate(restoreSql);

            // Delete from internal_supplements
            String delSql = "DELETE FROM internal_supplements WHERE id = " + selected.getDbId() + ";";
            DBConnection.executeUpdate(delSql);

            loadInternalSupplementsFromDatabase(null);
            loadProductsFromDatabase(null);
            updateInternalAvailableStockDisplay(cleanProdName);
            handleClearInternalFields(null);

            showInternalNotification("تم حذف عملية التوريد الداخلي واسترجاع الكمية إلى رصيد المخزن بنجاح! 🗑️", false);
        }
    }

    @FXML
    private void handleClearInternalFields(ActionEvent event) {
        if (cbInternalProduct != null) cbInternalProduct.setValue(null);
        if (cbInternalDept != null) cbInternalDept.setValue(null);
        if (txtInternalQty != null) txtInternalQty.clear();
        if (txtInternalNotes != null) txtInternalNotes.clear();
        if (dpInternalDate != null) dpInternalDate.setValue(LocalDate.now());
        if (lblInternalAvailableQty != null) {
            lblInternalAvailableQty.setText("الرصيد المتاح: -");
            lblInternalAvailableQty.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E65100; -fx-padding: 8px 0 0 0;");
        }
        if (tblInternalSupplements != null) tblInternalSupplements.getSelectionModel().clearSelection();
        if (lblInternalFormStatus != null) lblInternalFormStatus.setText("اختر الصنف والقسم والكمية المطلوبة للصرف الداخلي");
    }

    @FXML
    private void handleInternalSearch(ActionEvent event) {
        String q = txtInternalSearch != null ? txtInternalSearch.getText() : null;
        loadInternalSupplementsFromDatabase(q);
    }

    @FXML
    private void handleClearInternalSearch(ActionEvent event) {
        if (txtInternalSearch != null) txtInternalSearch.clear();
        loadInternalSupplementsFromDatabase(null);
    }

    private void showInternalNotification(String message, boolean isError) {
        if (lblInternalActionMessage != null) {
            lblInternalActionMessage.setText(message);
            lblInternalActionMessage.setStyle(isError ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 13px;" : "-fx-text-fill: #388E3C; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblInternalActionMessage.setVisible(true);

            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> lblInternalActionMessage.setVisible(false)));
            t.play();
        }
    }

    /**
     * Opens a modal popup window for managing Departments (table: depts, col: dept_name).
     * Allows user to add, edit, delete, and search departments.
     */
    private void openDepartmentManagerDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("إدارة الأقسام والإدارات");
        dialog.setHeaderText("إضافة، تعديل، حذف، وبحث في قائمة الأقسام والإدارات");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("inventory.css").toExternalForm());
        } catch (Exception ignored) {}
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(12.0);
        content.setPrefWidth(520.0);
        content.setPrefHeight(480.0);
        content.setPadding(new Insets(12.0));

        // Form Box
        VBox formBox = new VBox(10.0);
        formBox.setStyle("-fx-background-color: #FFF8F4; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #FFDEC9; -fx-border-radius: 8px;");

        VBox nameBox = new VBox(4.0);
        Label lblPrompt = new Label("اسم القسم *:");
        lblPrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");
        JFXTextField txtNameInput = new JFXTextField();
        txtNameInput.setPromptText("اكتب اسم القسم هنا...");
        txtNameInput.setFocusColor(Color.web("#FF6B00"));
        txtNameInput.setUnFocusColor(Color.web("#FFDEC9"));
        nameBox.getChildren().addAll(lblPrompt, txtNameInput);

        HBox btnBox = new HBox(8.0);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        JFXButton btnAdd = new JFXButton("➕ إضافة");
        btnAdd.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnEdit = new JFXButton("✏️ تعديل");
        btnEdit.setStyle("-fx-background-color: #E65100; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnDelete = new JFXButton("🗑️ حذف");
        btnDelete.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnClear = new JFXButton("🔄 تفريغ");
        btnClear.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        btnBox.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnClear);
        formBox.getChildren().addAll(nameBox, btnBox);

        // Search Field
        JFXTextField txtSearch = new JFXTextField();
        txtSearch.setPromptText("🔍 بحث في قائمة الأقسام...");
        txtSearch.setFocusColor(Color.web("#FF6B00"));
        txtSearch.setUnFocusColor(Color.web("#FFDEC9"));

        // TableView for Departments
        TableView<LookupItem> tblLookup = new TableView<>();
        tblLookup.setStyle("-fx-background-radius: 8px;");
        VBox.setVgrow(tblLookup, Priority.ALWAYS);

        TableColumn<LookupItem, Number> colSeq = new TableColumn<>("م");
        colSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        colSeq.setPrefWidth(50.0);

        TableColumn<LookupItem, String> colName = new TableColumn<>("اسم القسم");
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colName.setPrefWidth(430.0);

        tblLookup.getColumns().addAll(colSeq, colName);

        ObservableList<LookupItem> lookupData = FXCollections.observableArrayList();
        tblLookup.setItems(lookupData);

        Runnable loadData = () -> {
            lookupData.clear();
            String q = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
            String sql = "SELECT id, dept_name FROM depts;";
            ResultSet rs = DBConnection.executeQuery(sql);
            if (rs != null) {
                try {
                    int seq = 1;
                    while (rs.next()) {
                        String n = rs.getString("dept_name");
                        if (n != null && !n.trim().isEmpty()) {
                            if (q.isEmpty() || n.toLowerCase().contains(q.toLowerCase())) {
                                lookupData.add(new LookupItem(seq++, n.trim(), ""));
                            }
                        }
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        };

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData.run());
        loadData.run();

        // Selection listener
        tblLookup.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNameInput.setText(newVal.getName());
            }
        });

        // Double click to pick and close
        tblLookup.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblLookup.getSelectionModel().getSelectedItem() != null) {
                String selectedVal = tblLookup.getSelectionModel().getSelectedItem().getName();
                if (cbInternalDept != null) cbInternalDept.setValue(selectedVal);
                dialog.close();
            }
        });

        // Add
        btnAdd.setOnAction(e -> {
            String val = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (val.isEmpty()) {
                Alert a = new Alert(AlertType.WARNING, "يرجى إدخال اسم القسم أولاً!");
                a.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                a.showAndWait();
                return;
            }
            String chkSql = "SELECT COUNT(*) FROM depts WHERE dept_name = '" + escapeSql(val) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        Alert a = new Alert(AlertType.WARNING, "اسم القسم (" + val + ") مسجل مسبقًا في قاعدة البيانات!");
                        a.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        a.showAndWait();
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
            DBConnection.executeUpdate("INSERT INTO depts (dept_name) VALUES ('" + escapeSql(val) + "');");
            loadData.run();
            loadDepartmentsFromDatabase();
            if (cbInternalDept != null) cbInternalDept.setValue(val);
            txtNameInput.clear();
        });

        // Edit
        btnEdit.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                Alert a = new Alert(AlertType.WARNING, "يرجى اختيار قسم من الجدول لتعديله!");
                a.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                a.showAndWait();
                return;
            }
            String val = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (val.isEmpty()) return;
            String oldVal = sel.getName();
            DBConnection.executeUpdate("UPDATE depts SET dept_name = '" + escapeSql(val) + "' WHERE dept_name = '" + escapeSql(oldVal) + "';");
            DBConnection.executeUpdate("UPDATE internal_supplements SET dept_name = '" + escapeSql(val) + "' WHERE dept_name = '" + escapeSql(oldVal) + "';");
            loadData.run();
            loadDepartmentsFromDatabase();
            if (cbInternalDept != null) cbInternalDept.setValue(val);
        });

        // Delete
        btnDelete.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                Alert a = new Alert(AlertType.WARNING, "يرجى اختيار قسم من الجدول لحذفه!");
                a.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                a.showAndWait();
                return;
            }
            String targetVal = sel.getName();
            Alert conf = new Alert(AlertType.CONFIRMATION, "هل أنت متأكد من حذف القسم: " + targetVal + "؟", ButtonType.YES, ButtonType.NO);
            conf.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Optional<ButtonType> res = conf.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                DBConnection.executeUpdate("DELETE FROM depts WHERE dept_name = '" + escapeSql(targetVal) + "';");
                loadData.run();
                loadDepartmentsFromDatabase();
                txtNameInput.clear();
            }
        });

        // Clear
        btnClear.setOnAction(e -> {
            txtNameInput.clear();
            tblLookup.getSelectionModel().clearSelection();
        });

        content.getChildren().addAll(formBox, txtSearch, tblLookup);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // =========================================================================
    // Helpers & Search Filters
    // =========================================================================

    @FXML
    private void handleOpenSuppliedProductSearch(ActionEvent event) {
        openSuppliedProductSearchDialog();
    }

    private void setupSuppliedProductPicker() {
        if (cbProdName != null) {
            cbProdName.setOnMouseClicked(event -> {
                event.consume();
                openSuppliedProductSearchDialog();
            });
            cbProdName.setOnShowing(event -> {
                Platform.runLater(() -> {
                    cbProdName.hide();
                    openSuppliedProductSearchDialog();
                });
            });
        }
    }

    private boolean isPopulatingProductFields = false;

    private void setupShowInMenuHandler() {
        if (chkShowInMenu != null) {
            chkShowInMenu.setOnAction(e -> handleShowInMenuToggle());
        }
    }

    private void handleShowInMenuToggle() {
        if (isPopulatingProductFields) return;

        boolean isSelected = chkShowInMenu != null && chkShowInMenu.isSelected();

        // 1. Identify which product is currently selected in table or combobox
        ProductModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;

        String rawValue = cbProdName != null ? cbProdName.getValue() : null;
        if (selected == null && rawValue != null && !rawValue.trim().isEmpty()) {
            String cleanName = rawValue;
            String suppName = null;
            if (cleanName.contains(" (") && cleanName.endsWith(")")) {
                suppName = cleanName.substring(cleanName.lastIndexOf(" (") + 2, cleanName.length() - 1).trim();
                cleanName = cleanName.substring(0, cleanName.lastIndexOf(" (")).trim();
            }
            if (tblProducts != null) {
                for (ProductModel p : tblProducts.getItems()) {
                    if (p.getName().equalsIgnoreCase(cleanName)) {
                        if (suppName == null || p.getSupplier().equalsIgnoreCase(suppName)) {
                            selected = p;
                            break;
                        }
                    }
                }
            }
        }

        if (selected == null) {
            String ref = getSafeText(txtProdRef);
            if (ref != null && !ref.isEmpty() && tblProducts != null) {
                for (ProductModel p : tblProducts.getItems()) {
                    if (p.getReferenceNo().equalsIgnoreCase(ref)) {
                        selected = p;
                        break;
                    }
                }
            }
        }

        if (selected == null) {
            showProductNotification("يرجى اختيار صنف من الجدول أو القائمة أولاً لتعديل حالة العرض في المنيو!", true);
            return;
        }

        // 2. Update model in table immediately so UI changes without delay
        selected.setShowInMenu(isSelected);
        if (tblProducts != null) {
            tblProducts.refresh();
        }

        // 3. Update database table 'inventory' immediately
        if (hasColumn(existingInventoryColumns, invShowMenuColumn) || existingInventoryColumns.isEmpty()) {
            String colName = (invShowMenuColumn != null && hasColumn(existingInventoryColumns, invShowMenuColumn)) ? invShowMenuColumn : "show_in_menu";
            String boolVal = isSelected ? "1" : "0";

            if (selected.getReferenceNo() != null && !selected.getReferenceNo().trim().isEmpty()) {
                String sql = "UPDATE inventory SET " + colName + " = " + boolVal
                        + " WHERE " + invRefColumn + " = '" + escapeSql(selected.getReferenceNo().trim()) + "';";
                DBConnection.executeUpdate(sql);
            }

            if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)
                    && selected.getSupplier() != null && !selected.getSupplier().trim().isEmpty() && !"-".equals(selected.getSupplier().trim())) {
                String sql2 = "UPDATE inventory SET " + colName + " = " + boolVal
                        + " WHERE " + invNameColumn + " = '" + escapeSql(selected.getName().trim()) + "' AND " + invSupplierColumn + " = '" + escapeSql(selected.getSupplier().trim()) + "';";
                DBConnection.executeUpdate(sql2);
            } else {
                String sql2 = "UPDATE inventory SET " + colName + " = " + boolVal
                        + " WHERE " + invNameColumn + " = '" + escapeSql(selected.getName().trim()) + "';";
                DBConnection.executeUpdate(sql2);
            }
        }

        showProductNotification("تم " + (isSelected ? "تفعيل" : "إلغاء") + " عرض الصنف (" + selected.getName() + ") في المنيو بنجاح! ✔", false);
    }

    private void openSuppliedProductSearchDialog() {
        Dialog<ProductModel> dialog = new Dialog<>();
        dialog.setTitle("اختيار وبحث صنف مورّد");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("inventory.css").toExternalForm());
        } catch (Exception ignored) {}
        dialog.getDialogPane().setPrefSize(720, 500);

        VBox content = new VBox(12);
        content.setPadding(new Insets(14));

        Label lblTitle = new Label("الأصناف المسجلة من عمليات التوريد");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #E65100;");

        Label lblSub = new Label("اختر صنفًا تم توريده مسبقًا لتحديد الرقم المرجعي، الحد الأدنى للطلب، وسعر البيع للعملاء:");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        JFXTextField txtSearch = new JFXTextField();
        txtSearch.setPromptText("ابحث باسم الصنف، التصنيف، أو المورد...");
        txtSearch.setFocusColor(Color.web("#FF6B00"));
        txtSearch.setUnFocusColor(Color.web("#FFDEC9"));
        txtSearch.setMaxWidth(Double.MAX_VALUE);

        TableView<ProductModel> tblSupplied = new TableView<>();
        tblSupplied.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblSupplied, Priority.ALWAYS);

        TableColumn<ProductModel, Number> colSeq = new TableColumn<>("م");
        colSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        colSeq.setPrefWidth(45);

        TableColumn<ProductModel, String> colName = new TableColumn<>("اسم الصنف المورّد");
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colName.setPrefWidth(170);

        TableColumn<ProductModel, String> colCat = new TableColumn<>("القسم / التصنيف");
        colCat.setCellValueFactory(c -> c.getValue().categoryProperty());
        colCat.setPrefWidth(120);

        TableColumn<ProductModel, Number> colQty = new TableColumn<>("الكمية المتوفرة");
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colQty.setPrefWidth(95);

        TableColumn<ProductModel, String> colUnit = new TableColumn<>("الوحدة");
        colUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        colUnit.setPrefWidth(75);

        TableColumn<ProductModel, String> colCost = new TableColumn<>("سعر الشراء");
        colCost.setCellValueFactory(c -> c.getValue().costPriceProperty());
        colCost.setPrefWidth(100);

        TableColumn<ProductModel, String> colSupp = new TableColumn<>("المورد");
        colSupp.setCellValueFactory(c -> c.getValue().supplierProperty());
        colSupp.setPrefWidth(130);

        tblSupplied.getColumns().addAll(colSeq, colName, colCat, colQty, colUnit, colCost, colSupp);

        ObservableList<ProductModel> masterList = FXCollections.observableArrayList();
        FilteredList<ProductModel> filteredList = new FilteredList<>(masterList, p -> true);
        loadSuppliedProductsIntoList(masterList);
        tblSupplied.setItems(filteredList);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(p -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                String lower = newVal.trim().toLowerCase();
                return (p.getName() != null && p.getName().toLowerCase().contains(lower))
                        || (p.getCategory() != null && p.getCategory().toLowerCase().contains(lower))
                        || (p.getSupplier() != null && p.getSupplier().toLowerCase().contains(lower));
            });
        });

        tblSupplied.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                ProductModel sel = tblSupplied.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    selectProductInProductsTab(sel);
                    dialog.close();
                }
            }
        });

        content.getChildren().addAll(lblTitle, lblSub, txtSearch, tblSupplied);
        dialog.getDialogPane().setContent(content);

        ButtonType btnSelect = new ButtonType("✔ اختيار الصنف", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(btnSelect, btnCancel);

        dialog.setResultConverter(b -> {
            if (b == btnSelect) {
                return tblSupplied.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<ProductModel> result = dialog.showAndWait();
        result.ifPresent(this::selectProductInProductsTab);
    }

    private void loadSuppliedProductsIntoList(ObservableList<ProductModel> list) {
        list.clear();
        String sql = "SELECT * FROM inventory WHERE " + invNameColumn + " IN ("
                + "SELECT DISTINCT " + suppProcProdNameColumn + " FROM supplements WHERE " + suppProcProdNameColumn + " IS NOT NULL AND " + suppProcProdNameColumn + " != '') "
                + "ORDER BY " + invNameColumn + " ASC;";
        ResultSet rs = DBConnection.executeQuery(sql);
        Map<String, ProductModel> groupedSupplied = new LinkedHashMap<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    String name = getColumnStringSafe(rs, invNameColumn, "product_name", "name");
                    String ref = getColumnStringSafe(rs, invRefColumn, "reference_no", "ref_no");
                    String unit = getColumnStringSafe(rs, invUnitColumn, "product_unit", "unit");
                    String cat = getColumnStringSafe(rs, invCategoryColumn, "product_category", "category");
                    double qty = getColumnDoubleSafe(rs, invQtyColumn, "available_quantity", "quantity", "qty");
                    double minQty = getColumnDoubleSafe(rs, invMinQtyColumn, "low_limit", "min_quantity", "min_qty");
                    double cost = getColumnDoubleSafe(rs, invCostPriceColumn, "purchase_price", "cost_price");
                    double sell = getColumnDoubleSafe(rs, invSellPriceColumn, "sales_price", "sell_price");
                    String supp = "-";
                    if (invSupplierColumn != null && hasColumn(existingInventoryColumns, invSupplierColumn)) {
                        supp = getColumnStringSafe(rs, invSupplierColumn, "supplier", "supplier_name");
                    }
                    if (supp == null || supp.trim().isEmpty() || "-".equals(supp.trim())) {
                        String orderClause = "";
                        if (suppProcIdColumn != null && hasColumn(existingSupplementColumns, suppProcIdColumn)) {
                            orderClause = " ORDER BY " + suppProcIdColumn + " DESC";
                        } else if (suppProcDateColumn != null && hasColumn(existingSupplementColumns, suppProcDateColumn)) {
                            orderClause = " ORDER BY " + suppProcDateColumn + " DESC";
                        }
                        String sqlSupp = "SELECT " + suppProcSuppNameColumn + " FROM supplements WHERE " + suppProcProdNameColumn + " = '" + escapeSql(name) + "'" + orderClause + " LIMIT 1;";
                        ResultSet rsS = DBConnection.executeQuery(sqlSupp);
                        if (rsS != null) {
                            try {
                                if (rsS.next()) {
                                    String sName = rsS.getString(1);
                                    if (sName != null && !sName.trim().isEmpty()) {
                                        supp = sName.trim();
                                    }
                                }
                                rsS.close();
                            } catch (SQLException ignored) {}
                        }
                    }
                    if (supp == null || supp.trim().isEmpty()) {
                        supp = "-";
                    }
                    String date = getColumnStringSafe(rs, invDateColumn, "date_of_last_supplement", "supplement_date", "date");
                    boolean showMenu = getColumnBooleanSafe(rs, invShowMenuColumn, "show_in_menu", "show_menu");

                    String costFormatted = String.format(Locale.US, "%,.2f ج.م", cost);
                    String sellFormatted = String.format(Locale.US, "%,.2f ج.م", sell);

                    String key = (name != null ? name.trim().toLowerCase() : "") + "|||" + (supp != null ? supp.trim().toLowerCase() : "");

                    if (groupedSupplied.containsKey(key)) {
                        ProductModel existing = groupedSupplied.get(key);
                        existing.setQuantity(existing.getQuantity() + qty);
                        if (existing.getMinQuantity() <= 0 && minQty > 0) {
                            existing.setMinQuantity(minQty);
                        }
                        if ((existing.getSellPrice() == null || existing.getSellPrice().startsWith("0")) && sell > 0) {
                            existing.setSellPrice(sellFormatted);
                        }
                        if ((existing.getCostPrice() == null || existing.getCostPrice().startsWith("0")) && cost > 0) {
                            existing.setCostPrice(costFormatted);
                        }
                        if (!showMenu) {
                            existing.setShowInMenu(false);
                        }
                    } else {
                        ProductModel model = new ProductModel(0, name, ref, unit, qty, minQty, costFormatted, sellFormatted, cat, supp, date, showMenu);
                        groupedSupplied.put(key, model);
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        int seq = 1;
        for (ProductModel model : groupedSupplied.values()) {
            model.setSeq(seq++);
            list.add(model);
        }
    }

    private void selectProductInProductsTab(ProductModel model) {
        if (model == null) return;
        if (cbProdName != null) {
            String itemText = model.getName();
            if (model.getSupplier() != null && !model.getSupplier().trim().isEmpty() && !"-".equals(model.getSupplier().trim())) {
                itemText = model.getName() + " (" + model.getSupplier() + ")";
            }
            if (!cbProdName.getItems().contains(itemText)) {
                cbProdName.getItems().add(itemText);
            }
            cbProdName.setValue(itemText);
        }
        populateProductFields(model);
        if (tblProducts != null) {
            for (ProductModel p : tblProducts.getItems()) {
                if (p.getReferenceNo().equalsIgnoreCase(model.getReferenceNo())) {
                    tblProducts.getSelectionModel().select(p);
                    break;
                }
            }
        }
    }

    private void populateProductFields(ProductModel model) {
        isPopulatingProductFields = true;
        try {
            if (cbProdName != null) {
                String itemText = model.getName();
                if (model.getSupplier() != null && !model.getSupplier().trim().isEmpty() && !"-".equals(model.getSupplier().trim())) {
                    itemText = model.getName() + " (" + model.getSupplier() + ")";
                }
                if (!cbProdName.getItems().contains(itemText)) {
                    cbProdName.getItems().add(itemText);
                }
                cbProdName.setValue(itemText);
            }
            if (txtProdRef != null) txtProdRef.setText(model.getReferenceNo());
            if (txtProdMinQty != null) txtProdMinQty.setText(String.valueOf(model.getMinQuantity()));
            if (txtProdSellPrice != null) txtProdSellPrice.setText(model.getSellPrice().replace("ج.م", "").replace(",", "").trim());
            if (chkShowInMenu != null) chkShowInMenu.setSelected(model.isShowInMenu());
            if (lblProdFormStatus != null) lblProdFormStatus.setText("تم عرض بيانات الصنف: " + model.getName());
        } finally {
            isPopulatingProductFields = false;
        }
    }

    private void populateSupplierFields(SupplierModel model) {
        if (txtSuppName != null) txtSuppName.setText(model.getName());
        if (txtSuppNationalId != null) txtSuppNationalId.setText(model.getNationalId());
        if (txtSuppPhone != null) txtSuppPhone.setText(model.getPhone());
        if (txtSuppAddress != null) txtSuppAddress.setText(model.getAddress());
        if (txtSuppAccount != null) txtSuppAccount.setText(model.getAccountNumber());
        if (lblSuppFormStatus != null) lblSuppFormStatus.setText("تم عرض بيانات المورد: " + model.getName());
    }

    private void populateProcessFields(SupplementProcessModel model) {
        if (txtProcProduct != null) txtProcProduct.setText(model.getProductName());
        if (cbProcSupplier != null) cbProcSupplier.setValue(model.getSupplierName());
        if (dpProcDate != null && model.getDate() != null) {
            try { dpProcDate.setValue(LocalDate.parse(model.getDate())); } catch (Exception ignored) {}
        }
        if (cbProcUnit != null) cbProcUnit.setValue(model.getUnit());
        if (txtProcQty != null) txtProcQty.setText(String.valueOf(model.getQuantity()));
        if (txtProcUnitPrice != null) txtProcUnitPrice.setText(model.getUnitPrice().replace("ج.م", "").trim());
        if (cbProcCategory != null) {
            String cat = null;
            for (ProductModel p : productList) {
                if (p.getName().equalsIgnoreCase(model.getProductName()) &&
                    (model.getSupplierName() == null || model.getSupplierName().equalsIgnoreCase(p.getSupplier()))) {
                    cat = p.getCategory();
                    break;
                }
            }
            if (cat != null) cbProcCategory.setValue(cat);
        }
        if (lblProcFormStatus != null) lblProcFormStatus.setText("تم عرض عملية توريد: " + model.getProductName());
    }

    private void refreshProductSeq() {
        for (int i = 0; i < productList.size(); i++) productList.get(i).setSeq(i + 1);
    }

    private void refreshSupplierSeq() {
        for (int i = 0; i < supplierList.size(); i++) supplierList.get(i).setSeq(i + 1);
    }

    private void refreshProcSeq() {
        for (int i = 0; i < supplementList.size(); i++) supplementList.get(i).setSeq(i + 1);
    }

    private void updateAllCounts() {
        if (lblProdTotalCount != null) {
            lblProdTotalCount.setText(String.valueOf(productList.size()));
        }
        if (lblSuppTotalCount != null) {
            lblSuppTotalCount.setText(String.valueOf(supplierList.size()));
        }
        if (lblProcTotalCount != null) {
            lblProcTotalCount.setText(String.valueOf(supplementList.size()));
        }
    }

    private String generateAutoReferenceNo() {
        int max = 1000;
        for (ProductModel p : productList) {
            String r = p.getReferenceNo();
            if (r != null && (r.startsWith("REF-") || r.startsWith("INV-") || r.startsWith("PRD-"))) {
                try {
                    int num = Integer.parseInt(r.replaceAll("[^0-9]", ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return "REF-" + (max + 1);
    }

    private String escapeSql(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }

    private String getSqlNullable(String val) {
        if (val == null || val.trim().isEmpty()) return "NULL";
        return "'" + escapeSql(val.trim()) + "'";
    }

    private String getColumnStringSafe(ResultSet rs, String primaryCol, String... fallbacks) {
        if (primaryCol != null) {
            try {
                String val = rs.getString(primaryCol);
                if (val != null) return val.trim();
            } catch (Exception ignored) {}
        }
        for (String fb : fallbacks) {
            try {
                String val = rs.getString(fb);
                if (val != null) return val.trim();
            } catch (Exception ignored) {}
        }
        return "";
    }

    private double getColumnDoubleSafe(ResultSet rs, String primaryCol, String... fallbacks) {
        if (primaryCol != null) {
            try {
                return rs.getDouble(primaryCol);
            } catch (Exception ignored) {}
        }
        for (String fb : fallbacks) {
            try {
                return rs.getDouble(fb);
            } catch (Exception ignored) {}
        }
        return 0.0;
    }

    private boolean getColumnBooleanSafe(ResultSet rs, String primaryCol, String... fallbacks) {
        if (primaryCol != null) {
            try {
                Object val = rs.getObject(primaryCol);
                if (val != null) {
                    if (val instanceof Boolean) return (Boolean) val;
                    if (val instanceof Number) return ((Number) val).intValue() != 0;
                    String s = val.toString().trim();
                    return "1".equals(s) || "true".equalsIgnoreCase(s);
                }
            } catch (Exception ignored) {}
        }
        for (String fb : fallbacks) {
            try {
                Object val = rs.getObject(fb);
                if (val != null) {
                    if (val instanceof Boolean) return (Boolean) val;
                    if (val instanceof Number) return ((Number) val).intValue() != 0;
                    String s = val.toString().trim();
                    return "1".equals(s) || "true".equalsIgnoreCase(s);
                }
            } catch (Exception ignored) {}
        }
        return true;
    }

    private String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private void showProductNotification(String message, boolean isError) {
        if (lblProdActionMessage != null) {
            lblProdActionMessage.setText(message);
            lblProdActionMessage.setStyle(isError ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 13px;" : "-fx-text-fill: #388E3C; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblProdActionMessage.setVisible(true);

            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> lblProdActionMessage.setVisible(false)));
            t.play();
        }
    }

    private void showSupplierNotification(String message, boolean isError) {
        if (lblSuppActionMessage != null) {
            lblSuppActionMessage.setText(message);
            lblSuppActionMessage.setStyle(isError ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 13px;" : "-fx-text-fill: #388E3C; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblSuppActionMessage.setVisible(true);

            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> lblSuppActionMessage.setVisible(false)));
            t.play();
        }
    }

    private void showProcNotification(String message, boolean isError) {
        if (lblProcActionMessage != null) {
            lblProcActionMessage.setText(message);
            lblProcActionMessage.setStyle(isError ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 13px;" : "-fx-text-fill: #388E3C; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblProcActionMessage.setVisible(true);

            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> lblProcActionMessage.setVisible(false)));
            t.play();
        }
    }

    // =========================================================================
    // Data Model 1: Product (Inventory) Model
    // =========================================================================
    public static class ProductModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty referenceNo;
        private final SimpleStringProperty unit;
        private final SimpleDoubleProperty quantity;
        private final SimpleDoubleProperty minQuantity;
        private final SimpleStringProperty costPrice;
        private final SimpleStringProperty sellPrice;
        private final SimpleStringProperty category;
        private final SimpleStringProperty supplier;
        private final SimpleStringProperty supplementDate;
        private final SimpleBooleanProperty showInMenu;

        public ProductModel(int seq, String name, String referenceNo, String unit, double quantity, double minQuantity, String costPrice, String sellPrice, String category, String supplier, String supplementDate, boolean showInMenu) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.referenceNo = new SimpleStringProperty(referenceNo);
            this.unit = new SimpleStringProperty(unit);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.minQuantity = new SimpleDoubleProperty(minQuantity);
            this.costPrice = new SimpleStringProperty(costPrice);
            this.sellPrice = new SimpleStringProperty(sellPrice);
            this.category = new SimpleStringProperty(category);
            this.supplier = new SimpleStringProperty(supplier);
            this.supplementDate = new SimpleStringProperty(supplementDate);
            this.showInMenu = new SimpleBooleanProperty(showInMenu);
        }

        public ProductModel(int seq, String name, String referenceNo, String unit, double quantity, double minQuantity, String costPrice, String sellPrice, String category, String supplier, String supplementDate) {
            this(seq, name, referenceNo, unit, quantity, minQuantity, costPrice, sellPrice, category, supplier, supplementDate, true);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String v) { this.name.set(v); }

        public SimpleStringProperty referenceNoProperty() { return referenceNo; }
        public String getReferenceNo() { return referenceNo.get(); }
        public void setReferenceNo(String v) { this.referenceNo.set(v); }

        public SimpleStringProperty unitProperty() { return unit; }
        public String getUnit() { return unit.get(); }
        public void setUnit(String v) { this.unit.set(v); }

        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public double getQuantity() { return quantity.get(); }
        public void setQuantity(double v) { this.quantity.set(v); }

        public SimpleDoubleProperty minQuantityProperty() { return minQuantity; }
        public double getMinQuantity() { return minQuantity.get(); }
        public void setMinQuantity(double v) { this.minQuantity.set(v); }

        public SimpleStringProperty costPriceProperty() { return costPrice; }
        public String getCostPrice() { return costPrice.get(); }
        public void setCostPrice(String v) { this.costPrice.set(v); }

        public SimpleStringProperty sellPriceProperty() { return sellPrice; }
        public String getSellPrice() { return sellPrice.get(); }
        public void setSellPrice(String v) { this.sellPrice.set(v); }

        public SimpleStringProperty categoryProperty() { return category; }
        public String getCategory() { return category.get(); }
        public void setCategory(String v) { this.category.set(v); }

        public SimpleStringProperty supplierProperty() { return supplier; }
        public String getSupplier() { return supplier.get(); }
        public void setSupplier(String v) { this.supplier.set(v); }

        public SimpleStringProperty supplementDateProperty() { return supplementDate; }
        public String getSupplementDate() { return supplementDate.get(); }
        public void setSupplementDate(String v) { this.supplementDate.set(v); }

        public SimpleBooleanProperty showInMenuProperty() { return showInMenu; }
        public boolean isShowInMenu() { return showInMenu.get(); }
        public void setShowInMenu(boolean v) { this.showInMenu.set(v); }
    }

    // =========================================================================
    // Data Model 2: Supplier Model
    // =========================================================================
    public static class SupplierModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty nationalId;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty address;
        private final SimpleStringProperty accountNumber;

        public SupplierModel(int seq, String name, String nationalId, String phone, String address, String accountNumber) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.nationalId = new SimpleStringProperty(nationalId);
            this.phone = new SimpleStringProperty(phone);
            this.address = new SimpleStringProperty(address);
            this.accountNumber = new SimpleStringProperty(accountNumber);
        }

        public SupplierModel(int seq, String name, String phone, String address, String accountNumber) {
            this(seq, name, "غير مسجل", phone, address, accountNumber);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String v) { this.name.set(v); }

        public SimpleStringProperty nationalIdProperty() { return nationalId; }
        public String getNationalId() { return nationalId.get(); }
        public void setNationalId(String v) { this.nationalId.set(v); }

        public SimpleStringProperty phoneProperty() { return phone; }
        public String getPhone() { return phone.get(); }
        public void setPhone(String v) { this.phone.set(v); }

        public SimpleStringProperty addressProperty() { return address; }
        public String getAddress() { return address.get(); }
        public void setAddress(String v) { this.address.set(v); }

        public SimpleStringProperty accountNumberProperty() { return accountNumber; }
        public String getAccountNumber() { return accountNumber.get(); }
        public void setAccountNumber(String v) { this.accountNumber.set(v); }
    }

    // =========================================================================
    // Data Model 3: Supplement Process Model
    // =========================================================================
    public static class SupplementProcessModel {
        private final SimpleIntegerProperty seq;
        private int dbId;
        private final SimpleStringProperty productId;
        private final SimpleStringProperty date;
        private final SimpleStringProperty productName;
        private final SimpleStringProperty supplierName;
        private final SimpleStringProperty unit;
        private final SimpleDoubleProperty quantity;
        private final SimpleStringProperty unitPrice;
        private final SimpleStringProperty totalPrice;

        public SupplementProcessModel(int seq, int dbId, String productId, String date, String productName, String supplierName, String unit, double quantity, String unitPrice, String totalPrice) {
            this.seq = new SimpleIntegerProperty(seq);
            this.dbId = dbId;
            this.productId = new SimpleStringProperty(productId != null ? productId : "");
            this.date = new SimpleStringProperty(date);
            this.productName = new SimpleStringProperty(productName);
            this.supplierName = new SimpleStringProperty(supplierName);
            this.unit = new SimpleStringProperty(unit);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.unitPrice = new SimpleStringProperty(unitPrice);
            this.totalPrice = new SimpleStringProperty(totalPrice);
        }

        public SupplementProcessModel(int seq, String date, String productName, String supplierName, String unit, double quantity, String unitPrice, String totalPrice) {
            this(seq, 0, "", date, productName, supplierName, unit, quantity, unitPrice, totalPrice);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public int getDbId() { return dbId; }
        public void setDbId(int v) { this.dbId = v; }

        public SimpleStringProperty productIdProperty() { return productId; }
        public String getProductId() { return productId.get(); }
        public void setProductId(String v) { this.productId.set(v); }

        public SimpleStringProperty dateProperty() { return date; }
        public String getDate() { return date.get(); }
        public void setDate(String v) { this.date.set(v); }

        public SimpleStringProperty productNameProperty() { return productName; }
        public String getProductName() { return productName.get(); }
        public void setProductName(String v) { this.productName.set(v); }

        public SimpleStringProperty supplierNameProperty() { return supplierName; }
        public String getSupplierName() { return supplierName.get(); }
        public void setSupplierName(String v) { this.supplierName.set(v); }

        public SimpleStringProperty unitProperty() { return unit; }
        public String getUnit() { return unit.get(); }
        public void setUnit(String v) { this.unit.set(v); }

        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public double getQuantity() { return quantity.get(); }
        public void setQuantity(double v) { this.quantity.set(v); }

        public SimpleStringProperty unitPriceProperty() { return unitPrice; }
        public String getUnitPrice() { return unitPrice.get(); }
        public void setUnitPrice(String v) { this.unitPrice.set(v); }

        public SimpleStringProperty totalPriceProperty() { return totalPrice; }
        public String getTotalPrice() { return totalPrice.get(); }
        public void setTotalPrice(String v) { this.totalPrice.set(v); }
    }

    // =========================================================================
    // Data Model 4: Lookup Item (For Units / Categories Modal Manager)
    // =========================================================================
    public static class LookupItem {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty abbr;

        public LookupItem(int seq, String name, String abbr) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.abbr = new SimpleStringProperty(abbr);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String v) { this.name.set(v); }

        public SimpleStringProperty abbrProperty() { return abbr; }
        public String getAbbr() { return abbr.get(); }
        public void setAbbr(String v) { this.abbr.set(v); }
    }

    // =========================================================================
    // Data Model 5: Internal Supplement Process Model
    // =========================================================================
    public static class InternalSupplementModel {
        private final SimpleIntegerProperty seq;
        private final SimpleIntegerProperty dbId;
        private final SimpleStringProperty date;
        private final SimpleStringProperty productName;
        private final SimpleStringProperty deptName;
        private final SimpleDoubleProperty quantity;
        private final SimpleStringProperty unit;
        private final SimpleStringProperty notes;

        public InternalSupplementModel(int seq, int dbId, String date, String productName, String deptName, double quantity, String unit, String notes) {
            this.seq = new SimpleIntegerProperty(seq);
            this.dbId = new SimpleIntegerProperty(dbId);
            this.date = new SimpleStringProperty(date);
            this.productName = new SimpleStringProperty(productName);
            this.deptName = new SimpleStringProperty(deptName);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.unit = new SimpleStringProperty(unit);
            this.notes = new SimpleStringProperty(notes);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public int getDbId() { return dbId.get(); }

        public SimpleStringProperty dateProperty() { return date; }
        public String getDate() { return date.get(); }
        public void setDate(String v) { this.date.set(v); }

        public SimpleStringProperty productNameProperty() { return productName; }
        public String getProductName() { return productName.get(); }
        public void setProductName(String v) { this.productName.set(v); }

        public SimpleStringProperty deptNameProperty() { return deptName; }
        public String getDeptName() { return deptName.get(); }
        public void setDeptName(String v) { this.deptName.set(v); }

        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public double getQuantity() { return quantity.get(); }
        public void setQuantity(double v) { this.quantity.set(v); }

        public SimpleStringProperty unitProperty() { return unit; }
        public String getUnit() { return unit.get(); }
        public void setUnit(String v) { this.unit.set(v); }

        public SimpleStringProperty notesProperty() { return notes; }
        public String getNotes() { return notes.get(); }
        public void setNotes(String v) { this.notes.set(v); }
    }
}
