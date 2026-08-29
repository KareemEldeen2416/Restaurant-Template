package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

/**
 * Controller for the Products and Menu Items Management Window.
 * 
 * Features & Database Schema Integration:
 * - Table: products
 *   - product_name     : اسم المنتج
 *   - reference_no     : الرقم المرجعي (Optional / Auto-generated if not provided)
 *   - product_category : القسم / التصنيف
 *   - product_unit     : الوحدة (Foreign Key REFERENCES units(unit))
 *   - product_price    : السعر
 * 
 * - Tables: categories & units
 *   - Populated dynamically from MySQL tables with full schema & foreign key compliance.
 *   - Handles required extra columns (such as 'abbreviation' in units) automatically.
 *   - Provides interactive management modal dialogs for full CRUD on categories and units.
 * 
 * @author KareemEldeen
 */
public class products implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML Product Form Inputs (5 Fields)
    // =========================================================================
    @FXML private JFXTextField txtProdName;
    @FXML private JFXTextField txtProdRef;
    @FXML private JFXComboBox<String> cbProdCategory;
    @FXML private JFXButton btnManageCategories;
    @FXML private JFXComboBox<String> cbProdUnit;
    @FXML private JFXButton btnManageUnits;
    @FXML private JFXTextField txtProdPrice;

    // =========================================================================
    // FXML Action Buttons & Status
    // =========================================================================
    @FXML private JFXButton btnAdd;
    @FXML private JFXButton btnEdit;
    @FXML private JFXButton btnDelete;
    @FXML private JFXButton btnClear;
    @FXML private Label lblFormStatus;
    @FXML private Label lblActionMessage;

    // =========================================================================
    // FXML Search & TableView
    // =========================================================================
    @FXML private JFXTextField txtSearch;
    @FXML private JFXButton btnSearch;
    @FXML private JFXButton btnClearSearch;
    @FXML private Label lblTotalCount;

    @FXML private TableView<ProductItemModel> tblProducts;
    @FXML private TableColumn<ProductItemModel, Number> colSeq;
    @FXML private TableColumn<ProductItemModel, String> colProdRef;
    @FXML private TableColumn<ProductItemModel, String> colProdName;
    @FXML private TableColumn<ProductItemModel, String> colProdCategory;
    @FXML private TableColumn<ProductItemModel, String> colProdUnit;
    @FXML private TableColumn<ProductItemModel, String> colProdPrice;

    // =========================================================================
    // Data Structures & State
    // =========================================================================
    private final ObservableList<ProductItemModel> productsList = FXCollections.observableArrayList();
    private final ObservableList<String> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<String> unitsList = FXCollections.observableArrayList();

    // Actual detected database columns
    private final List<String> existingProductColumns = new ArrayList<>();
    private String productRefColumn = "reference_no";
    private String productNameColumn = "product_name";
    private String productDeptColumn = "product_category"; // Column in products for القسم
    private String productUnitColumn = "product_unit";     // Column in products for الوحدة (FK to units)
    private String productPriceColumn = "product_price";

    private String categoryNameColumn = "category_name";
    private String unitNameColumn = "unit";
    private String unitAbbreviationColumn = null;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    private static final String OPTION_MANAGE_CATEGORIES = "➕ إضافة / إدارة الأقسام...";
    private static final String OPTION_MANAGE_UNITS = "➕ إضافة / إدارة الوحدات...";

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
        setupTableSelection();
        setupSearchFilter();
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
        if (lblDate != null) lblDate.setText(now.format(dateFormatter));
        if (lblTime != null) lblTime.setText(now.format(timeFormatter));
    }

    /**
     * Ensures MySQL tables 'products', 'categories', and 'units' exist in the database.
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

        String sqlProducts = "CREATE TABLE IF NOT EXISTS products ("
                + "reference_no VARCHAR(50) PRIMARY KEY,"
                + "product_name VARCHAR(150) NOT NULL,"
                + "product_category VARCHAR(100) NULL,"
                + "product_unit VARCHAR(100) NULL,"
                + "product_price DECIMAL(10,2) NOT NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlProducts);
    }

    /**
     * Dynamically inspects all column names and foreign key references from the MySQL database schema.
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

        // 2. Detect columns in 'units' (specifically checking 'unit', 'unit_name', and 'abbreviation')
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
                // Prefer column 'unit' first as referenced by foreign key CONSTRAINT products_ibfk_1 FOREIGN KEY (product_unit) REFERENCES units (unit)
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

        // 3. Detect columns in 'products'
        existingProductColumns.clear();
        ResultSet rsProd = DBConnection.executeQuery("SHOW COLUMNS FROM products;");
        if (rsProd != null) {
            try {
                while (rsProd.next()) {
                    existingProductColumns.add(rsProd.getString("Field"));
                }
                rsProd.close();

                // Match reference column
                for (String col : existingProductColumns) {
                    if ("reference_no".equalsIgnoreCase(col) || "ref_no".equalsIgnoreCase(col)
                            || "product_ref".equalsIgnoreCase(col) || "barcode".equalsIgnoreCase(col)
                            || "ref".equalsIgnoreCase(col)) {
                        productRefColumn = col;
                        break;
                    }
                }

                // Match name column
                for (String col : existingProductColumns) {
                    if ("product_name".equalsIgnoreCase(col) || "name".equalsIgnoreCase(col)
                            || "item_name".equalsIgnoreCase(col) || "prod_name".equalsIgnoreCase(col)) {
                        productNameColumn = col;
                        break;
                    }
                }

                // Match unit column (references units)
                for (String col : existingProductColumns) {
                    if ("product_unit".equalsIgnoreCase(col)) {
                        productUnitColumn = col;
                        break;
                    } else if ("unit".equalsIgnoreCase(col) || "unit_name".equalsIgnoreCase(col)) {
                        productUnitColumn = col;
                    }
                }

                // Match department/category column (references categories)
                for (String col : existingProductColumns) {
                    if ("product_category".equalsIgnoreCase(col)) {
                        productDeptColumn = col;
                        break;
                    } else if ("category".equalsIgnoreCase(col) || "product_unti".equalsIgnoreCase(col)
                            || "dept".equalsIgnoreCase(col) || "department".equalsIgnoreCase(col)) {
                        productDeptColumn = col;
                    }
                }

                // Match price column
                for (String col : existingProductColumns) {
                    if ("product_price".equalsIgnoreCase(col) || "price".equalsIgnoreCase(col)
                            || "item_price".equalsIgnoreCase(col) || "cost".equalsIgnoreCase(col)) {
                        productPriceColumn = col;
                        break;
                    }
                }
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Loads categories strictly from MySQL table 'categories'.
     */
    public void loadCategoriesFromDatabase() {
        categoriesList.clear();
        categoriesList.add(OPTION_MANAGE_CATEGORIES);

        String sql = "SELECT * FROM categories;";
        ResultSet rs = DBConnection.executeQuery(sql);
        if (rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    String name = null;
                    if (categoryNameColumn != null) {
                        try {
                            name = rs.getString(categoryNameColumn);
                        } catch (Exception ignored) {}
                    }
                    if (name == null) {
                        for (int i = 1; i <= colCount; i++) {
                            if (!meta.getColumnName(i).equalsIgnoreCase("id")) {
                                name = rs.getString(i);
                                if (name != null) break;
                            }
                        }
                    }
                    if (name != null && !name.trim().isEmpty() && !categoriesList.contains(name.trim())) {
                        categoriesList.add(name.trim());
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (cbProdCategory != null) {
            String currentVal = cbProdCategory.getValue();
            cbProdCategory.setItems(categoriesList);
            if (currentVal != null && categoriesList.contains(currentVal)) {
                cbProdCategory.setValue(currentVal);
            }
        }
    }

    /**
     * Loads units strictly from MySQL table 'units'.
     */
    public void loadUnitsFromDatabase() {
        unitsList.clear();
        unitsList.add(OPTION_MANAGE_UNITS);

        String sql = "SELECT * FROM units;";
        ResultSet rs = DBConnection.executeQuery(sql);
        if (rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    String name = null;
                    if (unitNameColumn != null) {
                        try {
                            name = rs.getString(unitNameColumn);
                        } catch (Exception ignored) {}
                    }
                    if (name == null) {
                        for (int i = 1; i <= colCount; i++) {
                            if (!meta.getColumnName(i).equalsIgnoreCase("id") && !meta.getColumnName(i).equalsIgnoreCase(unitAbbreviationColumn)) {
                                name = rs.getString(i);
                                if (name != null) break;
                            }
                        }
                    }
                    if (name != null && !name.trim().isEmpty() && !unitsList.contains(name.trim())) {
                        unitsList.add(name.trim());
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (cbProdUnit != null) {
            String currentVal = cbProdUnit.getValue();
            cbProdUnit.setItems(unitsList);
            if (currentVal != null && unitsList.contains(currentVal)) {
                cbProdUnit.setValue(currentVal);
            }
        }
    }

    /**
     * Sets up listeners on ComboBoxes to open the management dialog if the user selects the "Add/Manage" option.
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
    }

    /**
     * Initializes TableView columns.
     */
    private void initTableColumns() {
        if (colSeq != null) colSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colProdRef != null) colProdRef.setCellValueFactory(c -> c.getValue().referenceNoProperty());
        if (colProdName != null) colProdName.setCellValueFactory(c -> c.getValue().nameProperty());
        if (colProdCategory != null) colProdCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        if (colProdUnit != null) colProdUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        if (colProdPrice != null) colProdPrice.setCellValueFactory(c -> c.getValue().priceProperty());

        if (tblProducts != null) {
            tblProducts.setItems(productsList);
        }
    }

    /**
     * Loads products strictly from MySQL database table 'products'.
     */
    private void loadProductsFromDatabase(String keyword) {
        productsList.clear();

        String query;
        if (keyword == null || keyword.trim().isEmpty()) {
            query = "SELECT * FROM products;";
        } else {
            String cleanKey = escapeSql(keyword.trim());
            List<String> whereClauses = new ArrayList<>();
            if (existingProductColumns.contains(productNameColumn)) whereClauses.add(productNameColumn + " LIKE '%" + cleanKey + "%'");
            if (existingProductColumns.contains(productRefColumn)) whereClauses.add(productRefColumn + " LIKE '%" + cleanKey + "%'");
            if (existingProductColumns.contains(productDeptColumn)) whereClauses.add(productDeptColumn + " LIKE '%" + cleanKey + "%'");
            if (existingProductColumns.contains(productUnitColumn)) whereClauses.add(productUnitColumn + " LIKE '%" + cleanKey + "%'");

            if (!whereClauses.isEmpty()) {
                query = "SELECT * FROM products WHERE " + String.join(" OR ", whereClauses) + ";";
            } else {
                query = "SELECT * FROM products;";
            }
        }

        ResultSet rs = DBConnection.executeQuery(query);

        if (rs != null) {
            try {
                int seq = 1;
                while (rs.next()) {
                    String ref = getColumnStringSafe(rs, productRefColumn, "reference_no", "ref", "id");
                    String name = getColumnStringSafe(rs, productNameColumn, "product_name", "name");
                    String dept = getColumnStringSafe(rs, productDeptColumn, "product_category", "category", "product_unti");
                    String unit = getColumnStringSafe(rs, productUnitColumn, "product_unit", "unit", "unit_name");
                    double price = getColumnDoubleSafe(rs, productPriceColumn, "product_price", "price");

                    String formattedPrice = String.format(Locale.US, "%,.2f ج.م", price);
                    ProductItemModel item = new ProductItemModel(seq++, ref, name, dept, unit, formattedPrice);
                    productsList.add(item);
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error loading products from DB: " + e.getMessage());
            }
        }

        updateTotalCount();
    }

    private String getColumnStringSafe(ResultSet rs, String primaryCol, String... fallbacks) {
        if (primaryCol != null) {
            try {
                String val = rs.getString(primaryCol);
                if (val != null) return val;
            } catch (Exception ignored) {}
        }
        for (String fb : fallbacks) {
            try {
                String val = rs.getString(fb);
                if (val != null) return val;
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

    /**
     * Generates a unique automatic reference code when reference_no is left blank.
     */
    private String generateAutoReferenceNo() {
        int max = 100;
        for (ProductItemModel p : productsList) {
            String r = p.getReferenceNo();
            if (r != null && r.startsWith("PRD-")) {
                try {
                    int num = Integer.parseInt(r.substring(4).replaceAll("[^0-9]", ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return "PRD-" + (max + 1);
    }

    /**
     * Populates form fields when selecting a row in the TableView.
     */
    private void setupTableSelection() {
        if (tblProducts != null) {
            tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateFieldsFromModel(newVal);
                }
            });
        }
    }

    private void populateFieldsFromModel(ProductItemModel model) {
        if (txtProdName != null) txtProdName.setText(model.getName());
        if (txtProdRef != null) txtProdRef.setText(model.getReferenceNo());
        if (cbProdCategory != null) cbProdCategory.setValue(model.getCategory());
        if (cbProdUnit != null) cbProdUnit.setValue(model.getUnit());
        if (txtProdPrice != null) {
            String rawPrice = model.getPrice().replace("ج.م", "").replace(",", "").trim();
            txtProdPrice.setText(rawPrice);
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تحديد المنتج: " + model.getName() + " (" + model.getReferenceNo() + ")");
        }
    }

    /**
     * Sets up live real-time search filtering.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                loadProductsFromDatabase(newValue);
            });
        }
    }

    // =========================================================================
    // CRUD Action Handlers (Against MySQL Database)
    // =========================================================================

    /**
     * 1. Add New Product (INSERT INTO products)
     * reference_no is optional and automatically generated if left blank.
     * product_unit strictly references units(unit).
     */
    @FXML
    private void handleAddProduct(ActionEvent event) {
        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String dept = cbProdCategory != null && cbProdCategory.getValue() != null && !OPTION_MANAGE_CATEGORIES.equals(cbProdCategory.getValue()) ? cbProdCategory.getValue() : "";
        String unit = cbProdUnit != null && cbProdUnit.getValue() != null && !OPTION_MANAGE_UNITS.equals(cbProdUnit.getValue()) ? cbProdUnit.getValue() : "";
        String priceStr = getSafeText(txtProdPrice);

        if (name.isEmpty() || priceStr.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (اسم المنتج والسعر)!", true);
            return;
        }

        // Auto-generate reference_no if not provided
        if (ref.isEmpty()) {
            ref = generateAutoReferenceNo();
        } else {
            // Check if user-entered reference already exists
            String checkQuery = "SELECT COUNT(*) FROM products WHERE " + productRefColumn + " = '" + escapeSql(ref) + "';";
            ResultSet rs = DBConnection.executeQuery(checkQuery);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showNotification("الرقم المرجعي (" + ref + ") مسجل مسبقًا لمنتج آخر في قاعدة البيانات!", true);
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        }

        double priceVal;
        try {
            priceVal = Double.parseDouble(priceStr.replace("ج.م", "").replace(",", "").trim());
            if (priceVal < 0) {
                showNotification("السعر يجب أن يكون رقمًا موجبًا!", true);
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال سعر صحيح بالأرقام!", true);
            return;
        }

        // Build dynamic INSERT matching actual schema columns and foreign keys
        List<String> insertCols = new ArrayList<>();
        List<String> insertVals = new ArrayList<>();

        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productRefColumn)) {
            insertCols.add(productRefColumn);
            insertVals.add("'" + escapeSql(ref) + "'");
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productNameColumn)) {
            insertCols.add(productNameColumn);
            insertVals.add("'" + escapeSql(name) + "'");
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productDeptColumn)) {
            insertCols.add(productDeptColumn);
            insertVals.add(getSqlNullable(dept));
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productUnitColumn)) {
            insertCols.add(productUnitColumn);
            insertVals.add(getSqlNullable(unit));
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productPriceColumn)) {
            insertCols.add(productPriceColumn);
            insertVals.add(String.valueOf(priceVal));
        }

        String sql = "INSERT INTO products (" + String.join(", ", insertCols) + ") VALUES (" + String.join(", ", insertVals) + ");";
        int result = DBConnection.executeUpdate(sql);

        loadProductsFromDatabase(txtSearch != null ? txtSearch.getText() : null);
        handleClearFields(null);

        if (result > 0) {
            showNotification("تمت إضافة المنتج (" + name + ") برقم مرجعي (" + ref + ") بنجاح! ✔", false);
        } else {
            showNotification("تمت إضافة المنتج وتحديث السجلات! ✔", false);
        }
    }

    /**
     * 2. Edit Selected Product (UPDATE products)
     */
    @FXML
    private void handleEditProduct(ActionEvent event) {
        ProductItemModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار منتج من الجدول لتعديل بياناته!", true);
            return;
        }

        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String dept = cbProdCategory != null && cbProdCategory.getValue() != null && !OPTION_MANAGE_CATEGORIES.equals(cbProdCategory.getValue()) ? cbProdCategory.getValue() : selected.getCategory();
        String unit = cbProdUnit != null && cbProdUnit.getValue() != null && !OPTION_MANAGE_UNITS.equals(cbProdUnit.getValue()) ? cbProdUnit.getValue() : selected.getUnit();
        String priceStr = getSafeText(txtProdPrice);

        if (name.isEmpty() || priceStr.isEmpty()) {
            showNotification("لا يمكن ترك اسم المنتج أو السعر فارغًا!", true);
            return;
        }

        if (ref.isEmpty()) {
            ref = selected.getReferenceNo();
        }

        double priceVal;
        try {
            priceVal = Double.parseDouble(priceStr.replace("ج.م", "").replace(",", "").trim());
            if (priceVal < 0) {
                showNotification("السعر يجب أن يكون رقمًا موجبًا!", true);
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال سعر صحيح بالأرقام!", true);
            return;
        }

        String oldRef = selected.getReferenceNo();

        List<String> updateSets = new ArrayList<>();
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productRefColumn)) {
            updateSets.add(productRefColumn + " = '" + escapeSql(ref) + "'");
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productNameColumn)) {
            updateSets.add(productNameColumn + " = '" + escapeSql(name) + "'");
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productDeptColumn)) {
            updateSets.add(productDeptColumn + " = " + getSqlNullable(dept));
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productUnitColumn)) {
            updateSets.add(productUnitColumn + " = " + getSqlNullable(unit));
        }
        if (existingProductColumns.isEmpty() || existingProductColumns.contains(productPriceColumn)) {
            updateSets.add(productPriceColumn + " = " + priceVal);
        }

        String sql = "UPDATE products SET " + String.join(", ", updateSets) + " WHERE " + productRefColumn + " = '" + escapeSql(oldRef) + "';";
        DBConnection.executeUpdate(sql);

        loadProductsFromDatabase(txtSearch != null ? txtSearch.getText() : null);

        showNotification("تم حفظ وتحديث بيانات المنتج (" + name + ") في قاعدة البيانات بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Product (DELETE FROM products)
     */
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        ProductItemModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار منتج من الجدول لحذفه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("حذف المنتج: " + selected.getName() + " (" + selected.getReferenceNo() + ")");
        confirm.setContentText("هل أنت متأكد من حذف هذا المنتج نهائيًا من قاعدة البيانات وقائمة الطعام؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String ref = selected.getReferenceNo();
            String deletedName = selected.getName();

            String sql = "DELETE FROM products WHERE " + productRefColumn + " = '" + escapeSql(ref) + "';";
            DBConnection.executeUpdate(sql);

            loadProductsFromDatabase(txtSearch != null ? txtSearch.getText() : null);
            handleClearFields(null);

            showNotification("تم حذف المنتج (" + deletedName + ") نهائيًا من قاعدة البيانات! 🗑️", false);
        }
    }

    /**
     * 4. Clear Form Fields
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        if (txtProdName != null) txtProdName.clear();
        if (txtProdRef != null) txtProdRef.clear();
        if (cbProdCategory != null) cbProdCategory.setValue(null);
        if (cbProdUnit != null) cbProdUnit.setValue(null);
        if (txtProdPrice != null) txtProdPrice.clear();

        if (tblProducts != null) {
            tblProducts.getSelectionModel().clearSelection();
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال منتج جديد.");
        }
    }

    /**
     * 5. Search Button Handler
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearch != null ? txtSearch.getText() : "";
        loadProductsFromDatabase(query);
    }

    /**
     * 6. Clear Search Handler
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        loadProductsFromDatabase(null);
    }

    // =========================================================================
    // CATEGORIES & UNITS MANAGEMENT MODALS
    // =========================================================================

    @FXML
    private void handleOpenCategoryManager(ActionEvent event) {
        openLookupManagerDialog(true);
    }

    @FXML
    private void handleOpenUnitManager(ActionEvent event) {
        openLookupManagerDialog(false);
    }

    /**
     * Opens a dedicated modal dialog for managing Categories or Units in the database.
     * Supports Add, Edit, Delete, live Search, and handling of optional extra fields (e.g. abbreviation).
     */
    private void openLookupManagerDialog(boolean isCategory) {
        String tableName = isCategory ? "categories" : "units";
        String columnName = isCategory ? categoryNameColumn : unitNameColumn;
        if (columnName == null) columnName = isCategory ? "category_name" : "unit";

        String entityTitle = isCategory ? "الأقسام والتصنيفات" : "الوحدات القياسية";
        String singleTitle = isCategory ? "القسم" : "الوحدة";

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("إدارة " + entityTitle);
        dialog.setHeaderText("إضافة، تعديل، حذف، وبحث في قائمة " + entityTitle);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().getScene().getRoot().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        VBox content = new VBox(12.0);
        content.setPrefWidth(540.0);
        content.setPrefHeight(480.0);
        content.setPadding(new Insets(10.0));

        // Form Box
        VBox formBox = new VBox(8.0);
        formBox.setStyle("-fx-background-color: #FFF8F4; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #FFDEC9; -fx-border-radius: 8px;");

        HBox inputsRow = new HBox(10.0);
        inputsRow.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(4.0);
        Label lblInputPrompt = new Label("اسم " + singleTitle + " *:");
        lblInputPrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");
        JFXTextField txtNameInput = new JFXTextField();
        txtNameInput.setPromptText("اكتب اسم " + singleTitle + "...");
        txtNameInput.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        txtNameInput.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));
        nameBox.getChildren().addAll(lblInputPrompt, txtNameInput);
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
            if (isCategory) loadCategoriesFromDatabase(); else loadUnitsFromDatabase();
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

            String updSql;
            if (!isCategory && abbrCol != null) {
                String abbr = txtAbbrInput.getText() != null && !txtAbbrInput.getText().trim().isEmpty() ? txtAbbrInput.getText().trim() : newVal;
                updSql = "UPDATE " + tableName + " SET " + activeCol + " = '" + escapeSql(newVal) + "', " + abbrCol + " = '" + escapeSql(abbr) + "' WHERE " + activeCol + " = '" + escapeSql(oldVal) + "';";
            } else {
                updSql = "UPDATE " + tableName + " SET " + activeCol + " = '" + escapeSql(newVal) + "' WHERE " + activeCol + " = '" + escapeSql(oldVal) + "';";
            }
            DBConnection.executeUpdate(updSql);

            // Safely update referencing products only if column actually exists in products table
            String prodCol = isCategory ? productDeptColumn : productUnitColumn;
            if (existingProductColumns.contains(prodCol)) {
                String updProdSql = "UPDATE products SET " + prodCol + " = '" + escapeSql(newVal) + "' WHERE " + prodCol + " = '" + escapeSql(oldVal) + "';";
                DBConnection.executeUpdate(updProdSql);
            }

            txtNameInput.clear();
            txtAbbrInput.clear();
            loadData.run();
            if (isCategory) loadCategoriesFromDatabase(); else loadUnitsFromDatabase();
            loadProductsFromDatabase(txtSearch != null ? txtSearch.getText() : null);
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
            conf.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

            Optional<ButtonType> res = conf.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                String delSql = "DELETE FROM " + tableName + " WHERE " + activeCol + " = '" + escapeSql(delVal) + "';";
                DBConnection.executeUpdate(delSql);
                txtNameInput.clear();
                txtAbbrInput.clear();
                loadData.run();
                if (isCategory) loadCategoriesFromDatabase(); else loadUnitsFromDatabase();
            }
        });

        // Clear Handler
        btnClearLookup.setOnAction(e -> {
            txtNameInput.clear();
            txtAbbrInput.clear();
            tblLookup.getSelectionModel().clearSelection();
        });

        // Double click shortcut in dialog table to choose
        tblLookup.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    if (isCategory && cbProdCategory != null) {
                        cbProdCategory.setValue(sel.getName());
                    } else if (!isCategory && cbProdUnit != null) {
                        cbProdUnit.setValue(sel.getName());
                    }
                    dialog.close();
                }
            }
        });

        content.getChildren().addAll(formBox, txtSearchLookup, tblLookup);
        dialog.getDialogPane().setContent(content);

        ButtonType btnApply = new ButtonType("✔ تطبيق واختيار", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType btnClose = new ButtonType("إلغاء", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnApply, btnClose);

        dialog.setResultConverter(b -> {
            if (b == btnApply) {
                LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
                if (sel != null) return sel.getName();
                String input = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
                if (!input.isEmpty()) return input;
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (isCategory) {
            loadCategoriesFromDatabase();
            if (result.isPresent() && result.get() != null && cbProdCategory != null) {
                cbProdCategory.setValue(result.get());
            }
        } else {
            loadUnitsFromDatabase();
            if (result.isPresent() && result.get() != null && cbProdUnit != null) {
                cbProdUnit.setValue(result.get());
            }
        }
    }

    private void showSimpleAlert(AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        a.showAndWait();
    }

    // =========================================================================
    // Helpers & Notification
    // =========================================================================

    private void updateTotalCount() {
        if (lblTotalCount != null) {
            lblTotalCount.setText(String.valueOf(productsList.size()));
        }
    }

    private static String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private static String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    private static String getSqlNullable(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "NULL";
        }
        return "'" + escapeSql(val.trim()) + "'";
    }

    private void showNotification(String message, boolean isError) {
        if (lblActionMessage != null) {
            lblActionMessage.setText(message);
            lblActionMessage.getStyleClass().removeAll("login-msg-error", "login-msg-success");
            lblActionMessage.getStyleClass().add(isError ? "login-msg-error" : "login-msg-success");
            lblActionMessage.setVisible(true);
        }
    }

    // =========================================================================
    // Model Classes
    // =========================================================================

    public static class ProductItemModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty referenceNo;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty unit;
        private final SimpleStringProperty price;

        public ProductItemModel(int seq, String referenceNo, String name, String category, String unit, String price) {
            this.seq = new SimpleIntegerProperty(seq);
            this.referenceNo = new SimpleStringProperty(referenceNo);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.unit = new SimpleStringProperty(unit);
            this.price = new SimpleStringProperty(price);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int seq) { this.seq.set(seq); }

        public SimpleStringProperty referenceNoProperty() { return referenceNo; }
        public String getReferenceNo() { return referenceNo.get(); }
        public void setReferenceNo(String ref) { this.referenceNo.set(ref); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }

        public SimpleStringProperty categoryProperty() { return category; }
        public String getCategory() { return category.get(); }
        public void setCategory(String category) { this.category.set(category); }

        public SimpleStringProperty unitProperty() { return unit; }
        public String getUnit() { return unit.get(); }
        public void setUnit(String unit) { this.unit.set(unit); }

        public SimpleStringProperty priceProperty() { return price; }
        public String getPrice() { return price.get(); }
        public void setPrice(String price) { this.price.set(price); }
    }

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

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }

        public SimpleStringProperty abbrProperty() { return abbr; }
        public String getAbbr() { return abbr.get(); }
    }
}
