package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller for the Inventory and Suppliers Management Window.
 * Features:
 * - 3 Dedicated Tabs: Products, Suppliers, and Information (Supplement Process Log).
 * - Tab 1 (Products): 10 Form Fields, CRUD, Real-time Search, and TableView.
 * - Tab 2 (Suppliers): 4 Form Fields, CRUD, Real-time Search, and TableView.
 * - Tab 3 (Information / Supplements): Supplement logging with auto total-price computation, CRUD, and TableView.
 * - Real-time stock quantity auto-synchronization when recording supplement operations.
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
    // FXML Tab 1: Products
    // =========================================================================
    @FXML private JFXTextField txtProdName;
    @FXML private JFXTextField txtProdRef;
    @FXML private JFXComboBox<String> cbProdUnit;
    @FXML private JFXTextField txtProdQty;
    @FXML private JFXTextField txtProdMinQty;
    @FXML private JFXTextField txtProdCostPrice;
    @FXML private JFXTextField txtProdSellPrice;
    @FXML private JFXComboBox<String> cbProdCategory;
    @FXML private JFXComboBox<String> cbProdSupplier;
    @FXML private DatePicker dpProdSupplementDate;

    @FXML private JFXButton btnProdAdd;
    @FXML private JFXButton btnProdEdit;
    @FXML private JFXButton btnProdDelete;
    @FXML private JFXButton btnProdClear;
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
    @FXML private TableColumn<ProductModel, String> colProdSupplier;
    @FXML private TableColumn<ProductModel, String> colProdDate;

    // =========================================================================
    // FXML Tab 2: Suppliers
    // =========================================================================
    @FXML private JFXTextField txtSuppName;
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
    @FXML private JFXTextField txtProcTotalPrice;
    @FXML private JFXTextField txtProcNotes;

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
    @FXML private TableColumn<SupplementProcessModel, String> colProcNotes;

    // =========================================================================
    // Data Collections
    // =========================================================================
    private final ObservableList<ProductModel> productList = FXCollections.observableArrayList();
    private FilteredList<ProductModel> filteredProductList;

    private final ObservableList<SupplierModel> supplierList = FXCollections.observableArrayList();
    private FilteredList<SupplierModel> filteredSupplierList;

    private final ObservableList<SupplementProcessModel> supplementList = FXCollections.observableArrayList();
    private FilteredList<SupplementProcessModel> filteredSupplementList;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initDropdownOptions();
        initTableColumns();
        loadInitialMockData();
        setupTableSelectionListeners();
        setupSearchFilters();
        setupAutoTotalCalculation();
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
     * Initializes dropdown options.
     */
    private void initDropdownOptions() {
        ObservableList<String> units = FXCollections.observableArrayList("كجم (Kg)", "قطعة (Pcs)", "لتر (Litre)", "كرتونة (Carton)", "باكت (Pack)", "شيكارة (Bag)");
        if (cbProdUnit != null) cbProdUnit.setItems(units);
        if (cbProcUnit != null) cbProcUnit.setItems(units);

        ObservableList<String> categories = FXCollections.observableArrayList(
            "اللحوم والبرجر", "الدواجن والطيور", "الجبن والألبان", "الخضروات والفواكه",
            "المخبوزات والخبز", "الصلصات والبهارات", "المشروبات والعصائر", "الزيوت والمقليات", "مستلزمات التعبئة"
        );
        if (cbProdCategory != null) cbProdCategory.setItems(categories);

        if (dpProdSupplementDate != null) dpProdSupplementDate.setValue(LocalDate.now());
        if (dpProcDate != null) dpProcDate.setValue(LocalDate.now());
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
        if (colProdSupplier != null) colProdSupplier.setCellValueFactory(c -> c.getValue().supplierProperty());
        if (colProdDate != null) colProdDate.setCellValueFactory(c -> c.getValue().supplementDateProperty());

        filteredProductList = new FilteredList<>(productList, p -> true);
        if (tblProducts != null) tblProducts.setItems(filteredProductList);

        // Tab 2: Suppliers Columns
        if (colSuppSeq != null) colSuppSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colSuppName != null) colSuppName.setCellValueFactory(c -> c.getValue().nameProperty());
        if (colSuppPhone != null) colSuppPhone.setCellValueFactory(c -> c.getValue().phoneProperty());
        if (colSuppAddress != null) colSuppAddress.setCellValueFactory(c -> c.getValue().addressProperty());
        if (colSuppAccount != null) colSuppAccount.setCellValueFactory(c -> c.getValue().accountNumberProperty());

        filteredSupplierList = new FilteredList<>(supplierList, p -> true);
        if (tblSuppliers != null) tblSuppliers.setItems(filteredSupplierList);

        // Tab 3: Supplement Processes Columns
        if (colProcSeq != null) colProcSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colProcDate != null) colProcDate.setCellValueFactory(c -> c.getValue().dateProperty());
        if (colProcProduct != null) colProcProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        if (colProcSupplier != null) colProcSupplier.setCellValueFactory(c -> c.getValue().supplierNameProperty());
        if (colProcUnit != null) colProcUnit.setCellValueFactory(c -> c.getValue().unitProperty());
        if (colProcQty != null) colProcQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        if (colProcUnitPrice != null) colProcUnitPrice.setCellValueFactory(c -> c.getValue().unitPriceProperty());
        if (colProcTotalPrice != null) colProcTotalPrice.setCellValueFactory(c -> c.getValue().totalPriceProperty());
        if (colProcNotes != null) colProcNotes.setCellValueFactory(c -> c.getValue().notesProperty());

        filteredSupplementList = new FilteredList<>(supplementList, p -> true);
        if (tblSupplements != null) tblSupplements.setItems(filteredSupplementList);
    }

    /**
     * Loads realistic initial mock data across all 3 sections.
     */
    private void loadInitialMockData() {
        // 1. Suppliers
        supplierList.add(new SupplierModel(1, "شركة الأهرام لتوريد اللحوم الطازجة", "01011223344", "القاهرة - منطقة المدابغ، عين الصيرة", "EG12345678901234"));
        supplierList.add(new SupplierModel(2, "مزارع الوادي للدواجن والبيض", "01122334455", "الجيزة - طريق مصر إسكندرية الصحراوي", "EG98765432109876"));
        supplierList.add(new SupplierModel(3, "مؤسسة النيل لتجارة الأجبان والألبان", "01233445566", "الإسكندرية - سموحة، شارع فوزي معاذ", "EG45678901234567"));
        supplierList.add(new SupplierModel(4, "شركة البركة لتوريد الخضروات المجمدة", "01544332211", "القاهرة - سوق العبور المركزي", "EG67890123456789"));
        supplierList.add(new SupplierModel(5, "الشركة العالمية لمنتجات التعبئة والتغليف", "01099887766", "مدينة 6 أكتوبر - المنطقة الصناعية الثالثة", "EG89012345678901"));

        // 2. Products
        productList.add(new ProductModel(1, "لحم بقري مفروم ممتاز", "REF-1001", "كجم (Kg)", 85.0, 20.0, "280 ج.م", "380 ج.م", "اللحوم والبرجر", "شركة الأهرام لتوريد اللحوم الطازجة", "2026-08-22"));
        productList.add(new ProductModel(2, "صدور دجاج فيليه طازجة", "REF-1002", "كجم (Kg)", 60.0, 15.0, "150 ج.م", "220 ج.م", "الدواجن والطيور", "مزارع الوادي للدواجن والبيض", "2026-08-21"));
        productList.add(new ProductModel(3, "جبنة موتزاريلا إيطالي مبشورة", "REF-1003", "كجم (Kg)", 45.0, 10.0, "190 ج.م", "260 ج.م", "الجبن والألبان", "مؤسسة النيل لتجارة الأجبان والألبان", "2026-08-20"));
        productList.add(new ProductModel(4, "خبز برجر سمسم فاخر (جامبو)", "REF-1004", "باكت (Pack)", 120.0, 30.0, "25 ج.م", "40 ج.م", "المخبوزات والخبز", "الشركة العالمية لمنتجات التعبئة والتغليف", "2026-08-23"));
        productList.add(new ProductModel(5, "بطاطس نصف مقلية للتجهيز", "REF-1005", "كرتونة (Carton)", 40.0, 10.0, "320 ج.م", "450 ج.م", "الخضروات والفواكه", "شركة البركة لتوريد الخضروات المجمدة", "2026-08-19"));
        productList.add(new ProductModel(6, "زيت قلي نقي مخصص للمطاعم", "REF-1006", "لتر (Litre)", 110.0, 25.0, "65 ج.م", "90 ج.م", "الزيوت والمقليات", "شركة الأهرام لتوريد اللحوم الطازجة", "2026-08-22"));

        // 3. Supplement Processes
        supplementList.add(new SupplementProcessModel(1, "2026-08-22", "لحم بقري مفروم ممتاز", "شركة الأهرام لتوريد اللحوم الطازجة", "كجم (Kg)", 50.0, "280 ج.م", "14,000 ج.م", "فاتورة توريد رقم #8841"));
        supplementList.add(new SupplementProcessModel(2, "2026-08-21", "صدور دجاج فيليه طازجة", "مزارع الوادي للدواجن والبيض", "كجم (Kg)", 40.0, "150 ج.م", "6,000 ج.م", "فاتورة توريد رقم #5120"));
        supplementList.add(new SupplementProcessModel(3, "2026-08-20", "جبنة موتزاريلا إيطالي مبشورة", "مؤسسة النيل لتجارة الأجبان والألبان", "كجم (Kg)", 30.0, "190 ج.م", "5,700 ج.م", "استلام شحنة أسبوعية"));
        supplementList.add(new SupplementProcessModel(4, "2026-08-19", "بطاطس نصف مقلية للتجهيز", "شركة البركة لتوريد الخضروات المجمدة", "كرتونة (Carton)", 20.0, "320 ج.م", "6,400 ج.م", "توريد مواد مجمدة"));

        syncDropdownChoices();
        updateAllCounts();
    }

    /**
     * Synchronizes dropdown lists of suppliers and products.
     */
    private void syncDropdownChoices() {
        ObservableList<String> suppNames = FXCollections.observableArrayList();
        for (SupplierModel s : supplierList) {
            suppNames.add(s.getName());
        }
        if (cbProdSupplier != null) cbProdSupplier.setItems(suppNames);
        if (cbProcSupplier != null) cbProcSupplier.setItems(suppNames);

        ObservableList<String> prodNames = FXCollections.observableArrayList();
        for (ProductModel p : productList) {
            prodNames.add(p.getName());
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
            txtProdSearch.textProperty().addListener((obs, oldVal, newVal) -> applyProductSearch(newVal));
        }
        if (txtSuppSearch != null) {
            txtSuppSearch.textProperty().addListener((obs, oldVal, newVal) -> applySupplierSearch(newVal));
        }
        if (txtProcSearch != null) {
            txtProcSearch.textProperty().addListener((obs, oldVal, newVal) -> applyProcessSearch(newVal));
        }
    }

    /**
     * Automatically computes Total Price in Tab 3 when Qty or Unit Price changes.
     */
    private void setupAutoTotalCalculation() {
        if (txtProcQty != null && txtProcUnitPrice != null && txtProcTotalPrice != null) {
            javafx.beans.value.ChangeListener<String> totalCalc = (obs, oldVal, newVal) -> {
                try {
                    String qStr = txtProcQty.getText().trim().replace("ج.م", "").trim();
                    String pStr = txtProcUnitPrice.getText().trim().replace("ج.م", "").trim();
                    if (!qStr.isEmpty() && !pStr.isEmpty()) {
                        double q = Double.parseDouble(qStr);
                        double p = Double.parseDouble(pStr);
                        txtProcTotalPrice.setText(String.format(Locale.US, "%,.2f ج.م", (q * p)));
                    } else {
                        txtProcTotalPrice.clear();
                    }
                } catch (Exception ignored) {
                    txtProcTotalPrice.clear();
                }
            };

            txtProcQty.textProperty().addListener(totalCalc);
            txtProcUnitPrice.textProperty().addListener(totalCalc);
        }
    }

    // =========================================================================
    // Tab 1: Products CRUD Actions
    // =========================================================================

    @FXML
    private void handleAddProduct(ActionEvent event) {
        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String unit = cbProdUnit != null ? cbProdUnit.getValue() : "قطعة";
        String qtyStr = getSafeText(txtProdQty);
        String minQtyStr = getSafeText(txtProdMinQty);
        String costStr = getSafeText(txtProdCostPrice);
        String sellStr = getSafeText(txtProdSellPrice);
        String cat = cbProdCategory != null ? cbProdCategory.getValue() : "عام";
        String supp = cbProdSupplier != null ? cbProdSupplier.getValue() : "";
        LocalDate date = dpProdSupplementDate != null ? dpProdSupplementDate.getValue() : LocalDate.now();

        if (name.isEmpty() || ref.isEmpty() || qtyStr.isEmpty() || costStr.isEmpty() || sellStr.isEmpty()) {
            showProductNotification("يرجى ملء الحقول الإجبارية للصنف!", true);
            return;
        }

        double qty = 0, minQty = 0;
        try {
            qty = Double.parseDouble(qtyStr);
            minQty = minQtyStr.isEmpty() ? 0 : Double.parseDouble(minQtyStr);
        } catch (NumberFormatException e) {
            showProductNotification("يرجى إدخال كميات صحيحة بالأرقام!", true);
            return;
        }

        int newSeq = productList.size() + 1;
        String costFormatted = costStr.endsWith("ج.م") ? costStr : costStr + " ج.م";
        String sellFormatted = sellStr.endsWith("ج.م") ? sellStr : sellStr + " ج.م";
        String dateStr = date != null ? date.toString() : LocalDate.now().toString();

        ProductModel newProd = new ProductModel(newSeq, name, ref, unit, qty, minQty, costFormatted, sellFormatted, cat, supp, dateStr);
        productList.add(newProd);
        refreshProductSeq();
        syncDropdownChoices();
        updateAllCounts();

        if (tblProducts != null) {
            tblProducts.getSelectionModel().select(newProd);
            tblProducts.scrollTo(newProd);
        }

        showProductNotification("تمت إضافة الصنف (" + name + ") بنجاح! ✔", false);
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        ProductModel selected = tblProducts != null ? tblProducts.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showProductNotification("يرجى اختيار صنف من الجدول لتعديله!", true);
            return;
        }

        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String unit = cbProdUnit != null ? cbProdUnit.getValue() : selected.getUnit();
        String qtyStr = getSafeText(txtProdQty);
        String minQtyStr = getSafeText(txtProdMinQty);
        String costStr = getSafeText(txtProdCostPrice);
        String sellStr = getSafeText(txtProdSellPrice);
        String cat = cbProdCategory != null ? cbProdCategory.getValue() : selected.getCategory();
        String supp = cbProdSupplier != null ? cbProdSupplier.getValue() : selected.getSupplier();
        LocalDate date = dpProdSupplementDate != null ? dpProdSupplementDate.getValue() : null;

        if (name.isEmpty() || ref.isEmpty()) {
            showProductNotification("لا يمكن ترك اسم الصنف أو الرقم المرجعي فارغًا!", true);
            return;
        }

        try {
            if (!qtyStr.isEmpty()) selected.setQuantity(Double.parseDouble(qtyStr));
            if (!minQtyStr.isEmpty()) selected.setMinQuantity(Double.parseDouble(minQtyStr));
        } catch (NumberFormatException ignored) {}

        selected.setName(name);
        selected.setReferenceNo(ref);
        selected.setUnit(unit);
        selected.setCostPrice(costStr.endsWith("ج.م") ? costStr : costStr + " ج.م");
        selected.setSellPrice(sellStr.endsWith("ج.م") ? sellStr : sellStr + " ج.م");
        selected.setCategory(cat);
        selected.setSupplier(supp);
        if (date != null) selected.setSupplementDate(date.toString());

        if (tblProducts != null) tblProducts.refresh();
        syncDropdownChoices();
        showProductNotification("تم حفظ وتحديث بيانات الصنف بنجاح! ✔", false);
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
        confirm.setContentText("هل أنت متأكد من حذف هذا الصنف نهائيًا من المخزن؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = selected.getName();
            productList.remove(selected);
            refreshProductSeq();
            syncDropdownChoices();
            updateAllCounts();
            handleClearProductFields(null);
            showProductNotification("تم حذف الصنف (" + name + ") بنجاح! 🗑️", false);
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
        if (cbProdCategory != null) cbProdCategory.setValue(null);
        if (cbProdSupplier != null) cbProdSupplier.setValue(null);
        if (dpProdSupplementDate != null) dpProdSupplementDate.setValue(LocalDate.now());

        if (tblProducts != null) tblProducts.getSelectionModel().clearSelection();
        if (lblProdFormStatus != null) lblProdFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال صنف جديد.");
    }

    @FXML
    private void handleProductSearch(ActionEvent event) {
        applyProductSearch(txtProdSearch != null ? txtProdSearch.getText() : "");
    }

    @FXML
    private void handleClearProductSearch(ActionEvent event) {
        if (txtProdSearch != null) txtProdSearch.clear();
        applyProductSearch("");
    }

    // =========================================================================
    // Tab 2: Suppliers CRUD Actions
    // =========================================================================

    @FXML
    private void handleAddSupplier(ActionEvent event) {
        String name = getSafeText(txtSuppName);
        String phone = getSafeText(txtSuppPhone);
        String address = getSafeText(txtSuppAddress);
        String account = getSafeText(txtSuppAccount);

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showSupplierNotification("يرجى ملء الحقول الإجبارية (اسم المورد، رقم الهاتف، والعنوان)!", true);
            return;
        }

        int newSeq = supplierList.size() + 1;
        SupplierModel newSupp = new SupplierModel(newSeq, name, phone, address, account.isEmpty() ? "غير مسجل" : account);
        supplierList.add(newSupp);
        refreshSupplierSeq();
        syncDropdownChoices();
        updateAllCounts();

        if (tblSuppliers != null) {
            tblSuppliers.getSelectionModel().select(newSupp);
            tblSuppliers.scrollTo(newSupp);
        }

        showSupplierNotification("تمت إضافة المورد (" + name + ") بنجاح! ✔", false);
    }

    @FXML
    private void handleEditSupplier(ActionEvent event) {
        SupplierModel selected = tblSuppliers != null ? tblSuppliers.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showSupplierNotification("يرجى اختيار مورد من الجدول لتعديله!", true);
            return;
        }

        String name = getSafeText(txtSuppName);
        String phone = getSafeText(txtSuppPhone);
        String address = getSafeText(txtSuppAddress);
        String account = getSafeText(txtSuppAccount);

        if (name.isEmpty() || phone.isEmpty()) {
            showSupplierNotification("لا يمكن ترك اسم المورد أو رقم الهاتف فارغًا!", true);
            return;
        }

        selected.setName(name);
        selected.setPhone(phone);
        selected.setAddress(address);
        selected.setAccountNumber(account.isEmpty() ? "غير مسجل" : account);

        if (tblSuppliers != null) tblSuppliers.refresh();
        syncDropdownChoices();
        showSupplierNotification("تم حفظ وتحديث بيانات المورد بنجاح! ✔", false);
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
        confirm.setContentText("هل أنت متأكد من حذف هذا المورد نهائيًا من النظام؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = selected.getName();
            supplierList.remove(selected);
            refreshSupplierSeq();
            syncDropdownChoices();
            updateAllCounts();
            handleClearSupplierFields(null);
            showSupplierNotification("تم حذف المورد (" + name + ") بنجاح! 🗑️", false);
        }
    }

    @FXML
    private void handleClearSupplierFields(ActionEvent event) {
        if (txtSuppName != null) txtSuppName.clear();
        if (txtSuppPhone != null) txtSuppPhone.clear();
        if (txtSuppAddress != null) txtSuppAddress.clear();
        if (txtSuppAccount != null) txtSuppAccount.clear();

        if (tblSuppliers != null) tblSuppliers.getSelectionModel().clearSelection();
        if (lblSuppFormStatus != null) lblSuppFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال مورد جديد.");
    }

    @FXML
    private void handleSupplierSearch(ActionEvent event) {
        applySupplierSearch(txtSuppSearch != null ? txtSuppSearch.getText() : "");
    }

    @FXML
    private void handleClearSupplierSearch(ActionEvent event) {
        if (txtSuppSearch != null) txtSuppSearch.clear();
        applySupplierSearch("");
    }

    // =========================================================================
    // Tab 3: Supplement Process Log CRUD Actions
    // =========================================================================

    @FXML
    private void handleAddProc(ActionEvent event) {
        String prodName = cbProcProduct != null ? cbProcProduct.getValue() : null;
        String suppName = cbProcSupplier != null ? cbProcSupplier.getValue() : null;
        LocalDate date = dpProcDate != null ? dpProcDate.getValue() : LocalDate.now();
        String unit = cbProcUnit != null ? cbProcUnit.getValue() : "قطعة";
        String qtyStr = getSafeText(txtProcQty);
        String unitPriceStr = getSafeText(txtProcUnitPrice);
        String notes = getSafeText(txtProcNotes);

        if (prodName == null || suppName == null || qtyStr.isEmpty() || unitPriceStr.isEmpty()) {
            showProcNotification("يرجى ملء الحقول الإجبارية لعملية التوريد!", true);
            return;
        }

        double qty = 0, unitPrice = 0;
        try {
            qty = Double.parseDouble(qtyStr);
            unitPrice = Double.parseDouble(unitPriceStr.replace("ج.م", "").trim());
        } catch (NumberFormatException e) {
            showProcNotification("يرجى إدخال أرقام صحيحة للكمية وسعر الوحدة!", true);
            return;
        }

        double total = qty * unitPrice;
        String totalFormatted = String.format(Locale.US, "%,.2f ج.م", total);
        String unitPriceFormatted = String.format(Locale.US, "%,.2f ج.م", unitPrice);
        String dateStr = date != null ? date.toString() : LocalDate.now().toString();

        int newSeq = supplementList.size() + 1;
        SupplementProcessModel newProc = new SupplementProcessModel(newSeq, dateStr, prodName, suppName, unit, qty, unitPriceFormatted, totalFormatted, notes.isEmpty() ? "عملية توريد مباشرة" : notes);
        supplementList.add(newProc);

        // Auto-increment product stock in Tab 1
        for (ProductModel p : productList) {
            if (p.getName().equals(prodName)) {
                p.setQuantity(p.getQuantity() + qty);
                p.setSupplementDate(dateStr);
                p.setCostPrice(unitPriceFormatted);
                break;
            }
        }
        if (tblProducts != null) tblProducts.refresh();

        refreshProcSeq();
        updateAllCounts();

        if (tblSupplements != null) {
            tblSupplements.getSelectionModel().select(newProc);
            tblSupplements.scrollTo(newProc);
        }

        showProcNotification("تم تسجيل عملية توريد (" + prodName + ") وتحديث رصيد المخزن بنجاح! ✔", false);
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
        String notes = getSafeText(txtProcNotes);

        try {
            if (!qtyStr.isEmpty()) selected.setQuantity(Double.parseDouble(qtyStr));
            if (!unitPriceStr.isEmpty()) {
                double up = Double.parseDouble(unitPriceStr.replace("ج.م", "").trim());
                selected.setUnitPrice(String.format(Locale.US, "%,.2f ج.م", up));
                selected.setTotalPrice(String.format(Locale.US, "%,.2f ج.م", (selected.getQuantity() * up)));
            }
        } catch (NumberFormatException ignored) {}

        selected.setProductName(prodName);
        selected.setSupplierName(suppName);
        selected.setUnit(unit);
        if (date != null) selected.setDate(date.toString());
        selected.setNotes(notes);

        if (tblSupplements != null) tblSupplements.refresh();
        showProcNotification("تم حفظ وتحديث عملية التوريد بنجاح! ✔", false);
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
        confirm.setContentText("هل أنت متأكد من حذف هذا السجل من حركات التوريد؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            supplementList.remove(selected);
            refreshProcSeq();
            updateAllCounts();
            handleClearProcFields(null);
            showProcNotification("تم حذف سجل عملية التوريد بنجاح! 🗑️", false);
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
        if (txtProcTotalPrice != null) txtProcTotalPrice.clear();
        if (txtProcNotes != null) txtProcNotes.clear();

        if (tblSupplements != null) tblSupplements.getSelectionModel().clearSelection();
        if (lblProcFormStatus != null) lblProcFormStatus.setText("تم تفريغ الحقول. جاهز لتسجيل عملية توريد جديدة.");
    }

    @FXML
    private void handleProcSearch(ActionEvent event) {
        applyProcessSearch(txtProcSearch != null ? txtProcSearch.getText() : "");
    }

    @FXML
    private void handleClearProcSearch(ActionEvent event) {
        if (txtProcSearch != null) txtProcSearch.clear();
        applyProcessSearch("");
    }

    // =========================================================================
    // Helpers & Search Filters
    // =========================================================================

    private void applyProductSearch(String query) {
        if (filteredProductList == null) return;
        filteredProductList.setPredicate(p -> {
            if (query == null || query.trim().isEmpty()) return true;
            String l = query.trim().toLowerCase();
            return (p.getName() != null && p.getName().toLowerCase().contains(l))
                || (p.getReferenceNo() != null && p.getReferenceNo().toLowerCase().contains(l))
                || (p.getCategory() != null && p.getCategory().toLowerCase().contains(l))
                || (p.getSupplier() != null && p.getSupplier().toLowerCase().contains(l));
        });
        updateAllCounts();
    }

    private void applySupplierSearch(String query) {
        if (filteredSupplierList == null) return;
        filteredSupplierList.setPredicate(s -> {
            if (query == null || query.trim().isEmpty()) return true;
            String l = query.trim().toLowerCase();
            return (s.getName() != null && s.getName().toLowerCase().contains(l))
                || (s.getPhone() != null && s.getPhone().toLowerCase().contains(l))
                || (s.getAddress() != null && s.getAddress().toLowerCase().contains(l))
                || (s.getAccountNumber() != null && s.getAccountNumber().toLowerCase().contains(l));
        });
        updateAllCounts();
    }

    private void applyProcessSearch(String query) {
        if (filteredSupplementList == null) return;
        filteredSupplementList.setPredicate(sp -> {
            if (query == null || query.trim().isEmpty()) return true;
            String l = query.trim().toLowerCase();
            return (sp.getProductName() != null && sp.getProductName().toLowerCase().contains(l))
                || (sp.getSupplierName() != null && sp.getSupplierName().toLowerCase().contains(l))
                || (sp.getDate() != null && sp.getDate().toLowerCase().contains(l))
                || (sp.getNotes() != null && sp.getNotes().toLowerCase().contains(l));
        });
        updateAllCounts();
    }

    private void populateProductFields(ProductModel model) {
        if (txtProdName != null) txtProdName.setText(model.getName());
        if (txtProdRef != null) txtProdRef.setText(model.getReferenceNo());
        if (cbProdUnit != null) cbProdUnit.setValue(model.getUnit());
        if (txtProdQty != null) txtProdQty.setText(String.valueOf(model.getQuantity()));
        if (txtProdMinQty != null) txtProdMinQty.setText(String.valueOf(model.getMinQuantity()));
        if (txtProdCostPrice != null) txtProdCostPrice.setText(model.getCostPrice().replace("ج.م", "").trim());
        if (txtProdSellPrice != null) txtProdSellPrice.setText(model.getSellPrice().replace("ج.م", "").trim());
        if (cbProdCategory != null) cbProdCategory.setValue(model.getCategory());
        if (cbProdSupplier != null) cbProdSupplier.setValue(model.getSupplier());
        if (dpProdSupplementDate != null && model.getSupplementDate() != null) {
            try { dpProdSupplementDate.setValue(LocalDate.parse(model.getSupplementDate())); } catch (Exception ignored) {}
        }
        if (lblProdFormStatus != null) lblProdFormStatus.setText("تم عرض بيانات الصنف: " + model.getName());
    }

    private void populateSupplierFields(SupplierModel model) {
        if (txtSuppName != null) txtSuppName.setText(model.getName());
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
        if (txtProcTotalPrice != null) txtProcTotalPrice.setText(model.getTotalPrice());
        if (txtProcNotes != null) txtProcNotes.setText(model.getNotes());
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
            int c = filteredProductList != null ? filteredProductList.size() : productList.size();
            lblProdTotalCount.setText(String.valueOf(c));
        }
        if (lblSuppTotalCount != null) {
            int c = filteredSupplierList != null ? filteredSupplierList.size() : supplierList.size();
            lblSuppTotalCount.setText(String.valueOf(c));
        }
        if (lblProcTotalCount != null) {
            int c = filteredSupplementList != null ? filteredSupplementList.size() : supplementList.size();
            lblProcTotalCount.setText(String.valueOf(c));
        }
    }

    private String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private void showProductNotification(String msg, boolean error) {
        if (lblProdActionMessage != null) {
            lblProdActionMessage.setText(msg);
            lblProdActionMessage.getStyleClass().removeAll("login-msg-error", "login-msg-success");
            lblProdActionMessage.getStyleClass().add(error ? "login-msg-error" : "login-msg-success");
            lblProdActionMessage.setVisible(true);
        }
    }

    private void showSupplierNotification(String msg, boolean error) {
        if (lblSuppActionMessage != null) {
            lblSuppActionMessage.setText(msg);
            lblSuppActionMessage.getStyleClass().removeAll("login-msg-error", "login-msg-success");
            lblSuppActionMessage.getStyleClass().add(error ? "login-msg-error" : "login-msg-success");
            lblSuppActionMessage.setVisible(true);
        }
    }

    private void showProcNotification(String msg, boolean error) {
        if (lblProcActionMessage != null) {
            lblProcActionMessage.setText(msg);
            lblProcActionMessage.getStyleClass().removeAll("login-msg-error", "login-msg-success");
            lblProcActionMessage.getStyleClass().add(error ? "login-msg-error" : "login-msg-success");
            lblProcActionMessage.setVisible(true);
        }
    }

    // =========================================================================
    // Data Model 1: Product Model
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

        public ProductModel(int seq, String name, String referenceNo, String unit, double quantity, double minQuantity, String costPrice, String sellPrice, String category, String supplier, String supplementDate) {
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
    }

    // =========================================================================
    // Data Model 2: Supplier Model
    // =========================================================================
    public static class SupplierModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty address;
        private final SimpleStringProperty accountNumber;

        public SupplierModel(int seq, String name, String phone, String address, String accountNumber) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.phone = new SimpleStringProperty(phone);
            this.address = new SimpleStringProperty(address);
            this.accountNumber = new SimpleStringProperty(accountNumber);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String v) { this.name.set(v); }

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
        private final SimpleStringProperty date;
        private final SimpleStringProperty productName;
        private final SimpleStringProperty supplierName;
        private final SimpleStringProperty unit;
        private final SimpleDoubleProperty quantity;
        private final SimpleStringProperty unitPrice;
        private final SimpleStringProperty totalPrice;
        private final SimpleStringProperty notes;

        public SupplementProcessModel(int seq, String date, String productName, String supplierName, String unit, double quantity, String unitPrice, String totalPrice, String notes) {
            this.seq = new SimpleIntegerProperty(seq);
            this.date = new SimpleStringProperty(date);
            this.productName = new SimpleStringProperty(productName);
            this.supplierName = new SimpleStringProperty(supplierName);
            this.unit = new SimpleStringProperty(unit);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.unitPrice = new SimpleStringProperty(unitPrice);
            this.totalPrice = new SimpleStringProperty(totalPrice);
            this.notes = new SimpleStringProperty(notes);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int v) { this.seq.set(v); }

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

        public SimpleStringProperty notesProperty() { return notes; }
        public String getNotes() { return notes.get(); }
        public void setNotes(String v) { this.notes.set(v); }
    }
}
