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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
    @FXML private JFXTextField txtProdName;
    @FXML private JFXTextField txtProdRef;
    @FXML private JFXComboBox<String> cbProdUnit;
    @FXML private JFXButton btnManageUnits;
    @FXML private JFXTextField txtProdQty;
    @FXML private JFXTextField txtProdMinQty;
    @FXML private JFXTextField txtProdCostPrice;
    @FXML private JFXTextField txtProdSellPrice;
    @FXML private JFXComboBox<String> cbProdCategory;
    @FXML private JFXButton btnManageCategories;
    @FXML private JFXComboBox<String> cbProdSupplier;
    @FXML private DatePicker dpProdSupplementDate;

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
    @FXML private TableColumn<ProductModel, String> colProdDate;

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
    @FXML private JFXComboBox<String> cbProcProduct;
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
    // Data Collections & State
    // =========================================================================
    private final ObservableList<ProductModel> productList = FXCollections.observableArrayList();
    private final ObservableList<SupplierModel> supplierList = FXCollections.observableArrayList();

    private final ObservableList<SupplementProcessModel> supplementList = FXCollections.observableArrayList();

    private final ObservableList<String> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<String> unitsList = FXCollections.observableArrayList();

    private static final String OPTION_MANAGE_CATEGORIES = "➕ إضافة / إدارة الأقسام...";
    private static final String OPTION_MANAGE_UNITS = "➕ إضافة / إدارة الوحدات...";

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
    private String invDateColumn = "date_of_last_supplement";
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

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        initTableColumns();
        ensureDatabaseTablesExist();
        detectDatabaseColumnNames();
        loadCategoriesFromDatabase();
        loadUnitsFromDatabase();
        loadProductsFromDatabase(null);
        loadSuppliersFromDatabase(null);
        loadSupplementsFromDatabase(null);
        setupTableSelectionListeners();
        setupSearchFilters();
        setupComboBoxActionListeners();
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
        if (colProdShowInMenu != null) colProdShowInMenu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isShowInMenu() ? "✔ نعم" : "❌ لا"));
        if (colProdSupplier != null) colProdSupplier.setCellValueFactory(c -> c.getValue().supplierProperty());
        if (colProdDate != null) colProdDate.setCellValueFactory(c -> c.getValue().supplementDateProperty());

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
                + "date_of_last_supplement DATE NULL,"
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

        if (cbProdCategory != null) {
            cbProdCategory.setItems(categoriesList);
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

        if (cbProdUnit != null) {
            cbProdUnit.setItems(unitsList);
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
     * Loads products strictly from MySQL table 'inventory'.
     */
    private void loadProductsFromDatabase(String searchQuery) {
        productList.clear();

        StringBuilder sql = new StringBuilder("SELECT * FROM inventory");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = escapeSql(searchQuery.trim());
            sql.append(" WHERE ")
               .append(invNameColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invRefColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invCategoryColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invSupplierColumn).append(" LIKE '%").append(q).append("%'")
               .append(" OR ").append(invUnitColumn).append(" LIKE '%").append(q).append("%'");
        }
        sql.append(";");

        ResultSet rs = DBConnection.executeQuery(sql.toString());
        int seq = 1;
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
                    String supp = getColumnStringSafe(rs, invSupplierColumn, "supplier", "supplier_name");
                    String date = getColumnStringSafe(rs, invDateColumn, "date_of_last_supplement", "supplement_date", "date");
                    boolean showMenu = getColumnBooleanSafe(rs, invShowMenuColumn, "show_in_menu", "show_menu");

                    String costFormatted = String.format(Locale.US, "%,.2f ج.م", cost);
                    String sellFormatted = String.format(Locale.US, "%,.2f ج.م", sell);

                    ProductModel model = new ProductModel(seq++, name, ref, unit, qty, minQty, costFormatted, sellFormatted, cat, supp, date, showMenu);
                    productList.add(model);
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (lblProdTotalCount != null) {
            lblProdTotalCount.setText(String.valueOf(productList.size()));
        }
        if (tblProducts != null) {
            tblProducts.refresh();
        }
        syncDropdownChoices();
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

        if (dpProdSupplementDate != null) dpProdSupplementDate.setValue(LocalDate.now());
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
        if (cbProdSupplier != null) cbProdSupplier.setItems(suppNames);
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

        // Also include any in productList
        for (ProductModel p : productList) {
            if (p.getName() != null && !p.getName().trim().isEmpty() && !prodNames.contains(p.getName().trim())) {
                prodNames.add(p.getName().trim());
            }
        }

        if (cbProcProduct != null) cbProcProduct.setItems(prodNames);
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
        if (cbProdCategory != null) {
            cbProdCategory.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_CATEGORIES.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbProdCategory.setValue(oldVal);
                        openLookupManagerDialog(true);
                    });
                }
            });
        }
        if (cbProdUnit != null) {
            cbProdUnit.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_UNITS.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbProdUnit.setValue(oldVal);
                        openLookupManagerDialog(false);
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
        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String unit = cbProdUnit != null && cbProdUnit.getValue() != null && !OPTION_MANAGE_UNITS.equals(cbProdUnit.getValue()) ? cbProdUnit.getValue() : "";
        String qtyStr = getSafeText(txtProdQty);
        String minQtyStr = getSafeText(txtProdMinQty);
        String costStr = getSafeText(txtProdCostPrice);
        String sellStr = getSafeText(txtProdSellPrice);
        String cat = cbProdCategory != null && cbProdCategory.getValue() != null && !OPTION_MANAGE_CATEGORIES.equals(cbProdCategory.getValue()) ? cbProdCategory.getValue() : "";
        String supp = cbProdSupplier != null && cbProdSupplier.getValue() != null ? cbProdSupplier.getValue() : "";
        LocalDate date = dpProdSupplementDate != null ? dpProdSupplementDate.getValue() : LocalDate.now();
        boolean showInMenu = chkShowInMenu != null && chkShowInMenu.isSelected();

        if (name.isEmpty() || qtyStr.isEmpty() || costStr.isEmpty() || sellStr.isEmpty()) {
            showProductNotification("يرجى ملء الحقول الإجبارية (الاسم، الكمية، سعر الشراء، سعر البيع)!", true);
            return;
        }

        if (ref.isEmpty()) {
            ref = generateAutoReferenceNo();
        } else {
            String chkSql = "SELECT COUNT(*) FROM inventory WHERE " + invRefColumn + " = '" + escapeSql(ref) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showProductNotification("الرقم المرجعي (" + ref + ") مسجل مسبقًا في قاعدة البيانات!", true);
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        }

        double qty = 0, minQty = 0, costPrice = 0, sellPrice = 0;
        try {
            qty = Double.parseDouble(qtyStr.replace("ج.م", "").replace(",", "").trim());
            minQty = minQtyStr.isEmpty() ? 0 : Double.parseDouble(minQtyStr.replace("ج.م", "").replace(",", "").trim());
            costPrice = Double.parseDouble(costStr.replace("ج.م", "").replace(",", "").trim());
            sellPrice = Double.parseDouble(sellStr.replace("ج.م", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            showProductNotification("يرجى إدخال قيم رقمية صحيحة للكميات والأسعار!", true);
            return;
        }

        if (unit.isEmpty()) {
            unit = getFirstAvailableUnit();
        }
        ensureUnitExistsInDatabase(unit);

        if (!cat.isEmpty()) {
            ensureCategoryExistsInDatabase(cat);
        }

        String dateStr = date != null ? date.toString() : LocalDate.now().toString();

        String sql = "INSERT INTO inventory ("
                + invNameColumn + ", "
                + invRefColumn + ", "
                + invUnitColumn + ", "
                + invCategoryColumn + ", "
                + invQtyColumn + ", "
                + invMinQtyColumn + ", "
                + invCostPriceColumn + ", "
                + invSellPriceColumn + ", "
                + invSupplierColumn + ", "
                + invDateColumn + ", "
                + invShowMenuColumn + ") VALUES ("
                + "'" + escapeSql(name) + "', "
                + "'" + escapeSql(ref) + "', "
                + getSqlNullable(unit) + ", "
                + getSqlNullable(cat) + ", "
                + qty + ", "
                + minQty + ", "
                + costPrice + ", "
                + sellPrice + ", "
                + getSqlNullable(supp) + ", "
                + getSqlNullable(dateStr) + ", "
                + (showInMenu ? "1" : "0") + ");";

        int res = DBConnection.executeUpdate(sql);
        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        handleClearProductFields(null);

        if (res > 0) {
            showProductNotification("تمت إضافة المنتج (" + name + ") برقم مرجعي (" + ref + ") بنجاح! ✔", false);
        } else {
            showProductNotification("تم حفظ المنتج وتحديث المخزن! ✔", false);
        }
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        ProductModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProductNotification("يرجى اختيار صنف من الجدول لتعديل بياناته!", true);
            return;
        }

        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String unit = cbProdUnit != null && cbProdUnit.getValue() != null && !OPTION_MANAGE_UNITS.equals(cbProdUnit.getValue()) ? cbProdUnit.getValue() : selected.getUnit();
        String qtyStr = getSafeText(txtProdQty);
        String minQtyStr = getSafeText(txtProdMinQty);
        String costStr = getSafeText(txtProdCostPrice);
        String sellStr = getSafeText(txtProdSellPrice);
        String cat = cbProdCategory != null && cbProdCategory.getValue() != null && !OPTION_MANAGE_CATEGORIES.equals(cbProdCategory.getValue()) ? cbProdCategory.getValue() : selected.getCategory();
        String supp = cbProdSupplier != null && cbProdSupplier.getValue() != null ? cbProdSupplier.getValue() : selected.getSupplier();
        LocalDate date = dpProdSupplementDate != null ? dpProdSupplementDate.getValue() : null;
        boolean showInMenu = chkShowInMenu != null && chkShowInMenu.isSelected();

        if (name.isEmpty()) {
            showProductNotification("لا يمكن ترك اسم الصنف فارغًا!", true);
            return;
        }

        if (ref.isEmpty()) {
            ref = selected.getReferenceNo();
        }

        double qty = 0, minQty = 0, costPrice = 0, sellPrice = 0;
        try {
            qty = !qtyStr.isEmpty() ? Double.parseDouble(qtyStr.replace("ج.م", "").replace(",", "").trim()) : selected.getQuantity();
            minQty = !minQtyStr.isEmpty() ? Double.parseDouble(minQtyStr.replace("ج.م", "").replace(",", "").trim()) : selected.getMinQuantity();
            costPrice = !costStr.isEmpty() ? Double.parseDouble(costStr.replace("ج.م", "").replace(",", "").trim()) : 0;
            sellPrice = !sellStr.isEmpty() ? Double.parseDouble(sellStr.replace("ج.م", "").replace(",", "").trim()) : 0;
        } catch (NumberFormatException e) {
            showProductNotification("يرجى إدخال أرقام صحيحة للكمية والسعر!", true);
            return;
        }

        if (unit.isEmpty()) {
            unit = getFirstAvailableUnit();
        }
        ensureUnitExistsInDatabase(unit);

        if (!cat.isEmpty()) {
            ensureCategoryExistsInDatabase(cat);
        }

        String oldRef = selected.getReferenceNo();
        String dateStr = date != null ? date.toString() : selected.getSupplementDate();

        String sql = "UPDATE inventory SET "
                + invNameColumn + " = '" + escapeSql(name) + "', "
                + invRefColumn + " = '" + escapeSql(ref) + "', "
                + invUnitColumn + " = " + getSqlNullable(unit) + ", "
                + invCategoryColumn + " = " + getSqlNullable(cat) + ", "
                + invQtyColumn + " = " + qty + ", "
                + invMinQtyColumn + " = " + minQty + ", "
                + invCostPriceColumn + " = " + costPrice + ", "
                + invSellPriceColumn + " = " + sellPrice + ", "
                + invSupplierColumn + " = " + getSqlNullable(supp) + ", "
                + invDateColumn + " = " + getSqlNullable(dateStr) + ", "
                + invShowMenuColumn + " = " + (showInMenu ? "1" : "0")
                + " WHERE " + invRefColumn + " = '" + escapeSql(oldRef) + "';";

        DBConnection.executeUpdate(sql);
        loadProductsFromDatabase(txtProdSearch != null ? txtProdSearch.getText() : null);
        showProductNotification("تم حفظ وتحديث بيانات الصنف بنجاح في قاعدة البيانات! ✔", false);
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
        if (txtProdName != null) txtProdName.clear();
        if (txtProdRef != null) txtProdRef.clear();
        if (cbProdUnit != null) cbProdUnit.setValue(null);
        if (txtProdQty != null) txtProdQty.clear();
        if (txtProdMinQty != null) txtProdMinQty.clear();
        if (txtProdCostPrice != null) txtProdCostPrice.clear();
        if (txtProdSellPrice != null) txtProdSellPrice.clear();
        if (chkShowInMenu != null) chkShowInMenu.setSelected(true);
        if (cbProdCategory != null) cbProdCategory.setValue(null);
        if (cbProdSupplier != null) cbProdSupplier.setValue(null);
        if (dpProdSupplementDate != null) dpProdSupplementDate.setValue(LocalDate.now());

        if (tblProducts != null) tblProducts.getSelectionModel().clearSelection();
        if (lblProdFormStatus != null) lblProdFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال صنف جديد.");
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
                if (cbProdCategory != null) cbProdCategory.setValue(val);
            } else {
                loadUnitsFromDatabase();
                if (cbProdUnit != null) cbProdUnit.setValue(val);
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
                if (cbProdCategory != null && oldVal.equals(cbProdCategory.getValue())) {
                    cbProdCategory.setValue(newVal);
                }
            } else {
                loadUnitsFromDatabase();
                if (cbProdUnit != null && oldVal.equals(cbProdUnit.getValue())) {
                    cbProdUnit.setValue(newVal);
                }
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
                    if (cbProdCategory != null && delVal.equals(cbProdCategory.getValue())) {
                        cbProdCategory.setValue(null);
                    }
                } else {
                    loadUnitsFromDatabase();
                    if (cbProdUnit != null && delVal.equals(cbProdUnit.getValue())) {
                        cbProdUnit.setValue(null);
                    }
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
                    if (isCategory && cbProdCategory != null) {
                        ensureCategoryExistsInDatabase(sel.getName());
                        cbProdCategory.setValue(sel.getName());
                    } else if (!isCategory) {
                        ensureUnitExistsInDatabase(sel.getName());
                        if (cbProdUnit != null) cbProdUnit.setValue(sel.getName());
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
                    if (isCategory && cbProdCategory != null) {
                        ensureCategoryExistsInDatabase(sel.getName());
                        cbProdCategory.setValue(sel.getName());
                    } else if (!isCategory) {
                        ensureUnitExistsInDatabase(sel.getName());
                        if (cbProdUnit != null) cbProdUnit.setValue(sel.getName());
                        if (cbProcUnit != null) cbProcUnit.setValue(sel.getName());
                    }
                } else {
                    String input = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
                    if (!input.isEmpty()) {
                        if (isCategory && cbProdCategory != null) {
                            ensureCategoryExistsInDatabase(input);
                            cbProdCategory.setValue(input);
                        } else if (!isCategory) {
                            ensureUnitExistsInDatabase(input);
                            if (cbProdUnit != null) cbProdUnit.setValue(input);
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
        if (!oldName.equals(name)) {
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
        String prodName = cbProcProduct != null ? cbProcProduct.getValue() : null;
        String suppName = cbProcSupplier != null ? cbProcSupplier.getValue() : null;
        LocalDate date = dpProcDate != null ? dpProcDate.getValue() : LocalDate.now();
        String unit = cbProcUnit != null ? cbProcUnit.getValue() : "قطعة";
        String qtyStr = getSafeText(txtProcQty);
        String unitPriceStr = getSafeText(txtProcUnitPrice);

        if (prodName == null || prodName.trim().isEmpty() || suppName == null || suppName.trim().isEmpty() 
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

        // 1. Ensure foreign key dependencies exist (units, suppliers, products)
        ensureUnitExistsInDatabase(unit);
        ensureSupplierExistsInDatabase(suppName);
        String prodId = ensureProductExistsInProductsTable(prodName, unit, unitPrice);

        // 3. Insert into 'supplements' table in MySQL database
        String dateStr = date != null ? date.toString() : LocalDate.now().toString();
        StringBuilder insSql = new StringBuilder("INSERT INTO supplements (");
        StringBuilder valSql = new StringBuilder(" VALUES (");

        List<String> cols = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        if (existingSupplementColumns.contains(suppProcProdIdColumn)) {
            cols.add(suppProcProdIdColumn);
            if (suppProcProdIdIsInteger) {
                String numOnly = prodId.replaceAll("[^0-9]", "");
                vals.add(!numOnly.isEmpty() ? numOnly : "0");
            } else {
                vals.add("'" + escapeSql(prodId) + "'");
            }
        }
        if (existingSupplementColumns.contains(suppProcProdNameColumn)) {
            cols.add(suppProcProdNameColumn);
            vals.add("'" + escapeSql(prodName) + "'");
        }
        if (existingSupplementColumns.contains(suppProcSuppNameColumn)) {
            cols.add(suppProcSuppNameColumn);
            vals.add("'" + escapeSql(suppName) + "'");
        }
        if (existingSupplementColumns.contains(suppProcDateColumn)) {
            cols.add(suppProcDateColumn);
            vals.add("'" + escapeSql(dateStr) + "'");
        }
        if (existingSupplementColumns.contains(suppProcUnitColumn)) {
            cols.add(suppProcUnitColumn);
            vals.add("'" + escapeSql(unit) + "'");
        }
        if (existingSupplementColumns.contains(suppProcQtyColumn)) {
            cols.add(suppProcQtyColumn);
            vals.add(String.valueOf(qty));
        }
        if (existingSupplementColumns.contains(suppProcUnitPriceColumn)) {
            cols.add(suppProcUnitPriceColumn);
            vals.add(String.valueOf(unitPrice));
        }
        if (suppProcTotalPriceColumn != null && existingSupplementColumns.contains(suppProcTotalPriceColumn)) {
            cols.add(suppProcTotalPriceColumn);
            vals.add(String.valueOf(qty * unitPrice));
        }

        insSql.append(String.join(", ", cols)).append(")");
        valSql.append(String.join(", ", vals)).append(");");

        DBConnection.executeUpdate(insSql.toString() + valSql.toString());

        // 4. Auto-increment product stock in 'inventory' table if product is present
        for (ProductModel p : productList) {
            if (p.getName().equalsIgnoreCase(prodName)) {
                double newQty = p.getQuantity() + qty;
                p.setQuantity(newQty);
                p.setSupplementDate(dateStr);
                p.setCostPrice(String.format(Locale.US, "%,.2f ج.م", unitPrice));

                String updSql = "UPDATE inventory SET " 
                        + invQtyColumn + " = " + newQty + ", "
                        + invDateColumn + " = '" + escapeSql(dateStr) + "', "
                        + invCostPriceColumn + " = " + unitPrice 
                        + " WHERE " + invNameColumn + " = '" + escapeSql(prodName) + "';";
                DBConnection.executeUpdate(updSql);
                break;
            }
        }
        if (tblProducts != null) tblProducts.refresh();

        loadSupplementsFromDatabase(null);
        handleClearProcFields(null);
        showProcNotification("تم تسجيل وحفظ عملية التوريد في قاعدة البيانات بنجاح! ✔", false);
    }

    @FXML
    private void handleEditProc(ActionEvent event) {
        SupplementProcessModel selected = tblSupplements != null ? tblSupplements.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProcNotification("يرجى اختيار عملية توريد من الجدول لتعديلها!", true);
            return;
        }

        String prodName = cbProcProduct != null ? cbProcProduct.getValue() : selected.getProductName();
        String suppName = cbProcSupplier != null ? cbProcSupplier.getValue() : selected.getSupplierName();
        LocalDate date = dpProcDate != null ? dpProcDate.getValue() : null;
        String unit = cbProcUnit != null ? cbProcUnit.getValue() : selected.getUnit();
        String qtyStr = getSafeText(txtProcQty);
        String unitPriceStr = getSafeText(txtProcUnitPrice);

        if (prodName == null || prodName.trim().isEmpty() || suppName == null || suppName.trim().isEmpty() 
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
        String prodId = ensureProductExistsInProductsTable(prodName, unit, unitPrice);

        String dateStr = date != null ? date.toString() : selected.getDate();
        StringBuilder updSql = new StringBuilder("UPDATE supplements SET ");
        List<String> sets = new ArrayList<>();

        if (existingSupplementColumns.contains(suppProcProdIdColumn)) {
            if (suppProcProdIdIsInteger) {
                String numOnly = prodId.replaceAll("[^0-9]", "");
                sets.add(suppProcProdIdColumn + " = " + (!numOnly.isEmpty() ? numOnly : "0"));
            } else {
                sets.add(suppProcProdIdColumn + " = '" + escapeSql(prodId) + "'");
            }
        }
        if (existingSupplementColumns.contains(suppProcProdNameColumn)) {
            sets.add(suppProcProdNameColumn + " = '" + escapeSql(prodName) + "'");
        }
        if (existingSupplementColumns.contains(suppProcSuppNameColumn)) {
            sets.add(suppProcSuppNameColumn + " = '" + escapeSql(suppName) + "'");
        }
        if (existingSupplementColumns.contains(suppProcDateColumn)) {
            sets.add(suppProcDateColumn + " = '" + escapeSql(dateStr) + "'");
        }
        if (existingSupplementColumns.contains(suppProcUnitColumn)) {
            sets.add(suppProcUnitColumn + " = '" + escapeSql(unit) + "'");
        }
        if (existingSupplementColumns.contains(suppProcQtyColumn)) {
            sets.add(suppProcQtyColumn + " = " + qty);
        }
        if (existingSupplementColumns.contains(suppProcUnitPriceColumn)) {
            sets.add(suppProcUnitPriceColumn + " = " + unitPrice);
        }
        if (suppProcTotalPriceColumn != null && existingSupplementColumns.contains(suppProcTotalPriceColumn)) {
            sets.add(suppProcTotalPriceColumn + " = " + (qty * unitPrice));
        }

        updSql.append(String.join(", ", sets));

        if (selected.getDbId() > 0 && existingSupplementColumns.contains(suppProcIdColumn)) {
            updSql.append(" WHERE ").append(suppProcIdColumn).append(" = ").append(selected.getDbId()).append(";");
        } else {
            updSql.append(" WHERE ").append(suppProcProdNameColumn).append(" = '").append(escapeSql(selected.getProductName())).append("'")
                  .append(" AND ").append(suppProcDateColumn).append(" = '").append(escapeSql(selected.getDate())).append("' LIMIT 1;");
        }

        DBConnection.executeUpdate(updSql.toString());
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
            String delSql;
            if (selected.getDbId() > 0 && existingSupplementColumns.contains(suppProcIdColumn)) {
                delSql = "DELETE FROM supplements WHERE " + suppProcIdColumn + " = " + selected.getDbId() + ";";
            } else {
                delSql = "DELETE FROM supplements WHERE " + suppProcProdNameColumn + " = '" + escapeSql(selected.getProductName()) + "' AND " + suppProcDateColumn + " = '" + escapeSql(selected.getDate()) + "' LIMIT 1;";
            }
            DBConnection.executeUpdate(delSql);

            loadSupplementsFromDatabase(null);
            handleClearProcFields(null);
            showProcNotification("تم حذف سجل عملية التوريد من قاعدة البيانات بنجاح! 🗑️", false);
        }
    }

    @FXML
    private void handleClearProcFields(ActionEvent event) {
        if (cbProcProduct != null) cbProcProduct.setValue(null);
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
    // Helpers & Search Filters
    // =========================================================================

    private void populateProductFields(ProductModel model) {
        if (txtProdName != null) txtProdName.setText(model.getName());
        if (txtProdRef != null) txtProdRef.setText(model.getReferenceNo());
        if (cbProdUnit != null) cbProdUnit.setValue(model.getUnit());
        if (txtProdQty != null) txtProdQty.setText(String.valueOf(model.getQuantity()));
        if (txtProdMinQty != null) txtProdMinQty.setText(String.valueOf(model.getMinQuantity()));
        if (txtProdCostPrice != null) txtProdCostPrice.setText(model.getCostPrice().replace("ج.م", "").replace(",", "").trim());
        if (txtProdSellPrice != null) txtProdSellPrice.setText(model.getSellPrice().replace("ج.م", "").replace(",", "").trim());
        if (chkShowInMenu != null) chkShowInMenu.setSelected(model.isShowInMenu());
        if (cbProdCategory != null) cbProdCategory.setValue(model.getCategory());
        if (cbProdSupplier != null) cbProdSupplier.setValue(model.getSupplier());
        if (dpProdSupplementDate != null && model.getSupplementDate() != null) {
            try { dpProdSupplementDate.setValue(LocalDate.parse(model.getSupplementDate())); } catch (Exception ignored) {}
        }
        if (lblProdFormStatus != null) lblProdFormStatus.setText("تم عرض بيانات الصنف: " + model.getName());
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
        if (cbProcProduct != null) cbProcProduct.setValue(model.getProductName());
        if (cbProcSupplier != null) cbProcSupplier.setValue(model.getSupplierName());
        if (dpProcDate != null && model.getDate() != null) {
            try { dpProcDate.setValue(LocalDate.parse(model.getDate())); } catch (Exception ignored) {}
        }
        if (cbProcUnit != null) cbProcUnit.setValue(model.getUnit());
        if (txtProcQty != null) txtProcQty.setText(String.valueOf(model.getQuantity()));
        if (txtProcUnitPrice != null) txtProcUnitPrice.setText(model.getUnitPrice().replace("ج.م", "").trim());
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
                return rs.getBoolean(primaryCol);
            } catch (Exception ignored) {}
        }
        for (String fb : fallbacks) {
            try {
                return rs.getBoolean(fb);
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
}
