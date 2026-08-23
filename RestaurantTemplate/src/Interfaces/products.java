package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller for the Products and Menu Items Management Window.
 * Features:
 * - Live Arabic clock & date in the top bar.
 * - 5 Form Fields for Product Info (Name, Reference Number, Category, Unit, Price).
 * - Full CRUD action buttons (Add, Edit, Delete, Clear).
 * - Real-time filtered Search by Name, Reference, or Category.
 * - TableView showing all products and menu items with selection binding.
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
    @FXML private JFXComboBox<String> cbProdUnit;
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
    // Data Structures & Timers
    // =========================================================================
    private final ObservableList<ProductItemModel> productsList = FXCollections.observableArrayList();
    private FilteredList<ProductItemModel> filteredProductsList;

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
        setupTableSelection();
        setupSearchFilter();
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
     * Initializes dropdown options for Category and Unit.
     */
    private void initDropdownOptions() {
        if (cbProdCategory != null) {
            cbProdCategory.setItems(FXCollections.observableArrayList(
                "سندوتشات اللحم والبرجر",
                "وجبات الدجاج المقرمش",
                "البيتزا الإيطالية",
                "المشويات والباربيكيو",
                "المكرونة والباستا",
                "الكريب والسناكس",
                "السلطات والمقبلات",
                "المشروبات والعصائر الفريش",
                "الحلويات والوافل"
            ));
        }

        if (cbProdUnit != null) {
            cbProdUnit.setItems(FXCollections.observableArrayList(
                "قطعة (Pcs)",
                "سندوتش (Sandwich)",
                "وجبة كاملة (Meal)",
                "طبق (Plate)",
                "كوب كبير (Cup)",
                "زجاجة (Bottle)",
                "كجم (Kg)",
                "كرتونة (Carton)"
            ));
        }
    }

    /**
     * Maps table columns to ProductItemModel properties.
     */
    private void initTableColumns() {
        if (colSeq != null) colSeq.setCellValueFactory(cellData -> cellData.getValue().seqProperty());
        if (colProdRef != null) colProdRef.setCellValueFactory(cellData -> cellData.getValue().referenceNoProperty());
        if (colProdName != null) colProdName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        if (colProdCategory != null) colProdCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        if (colProdUnit != null) colProdUnit.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        if (colProdPrice != null) colProdPrice.setCellValueFactory(cellData -> cellData.getValue().priceProperty());

        filteredProductsList = new FilteredList<>(productsList, p -> true);
        if (tblProducts != null) {
            tblProducts.setItems(filteredProductsList);
        }
        updateTotalCount();
    }

    /**
     * Loads realistic initial mock data for restaurant products.
     */
    private void loadInitialMockData() {
        productsList.add(new ProductItemModel(1, "PRD-101", "برجر ميكس لحم دوبل", "سندوتشات اللحم والبرجر", "سندوتش (Sandwich)", "150.00 ج.م"));
        productsList.add(new ProductItemModel(2, "PRD-102", "برجر تشيز مشروم فاخر", "سندوتشات اللحم والبرجر", "سندوتش (Sandwich)", "135.00 ج.م"));
        productsList.add(new ProductItemModel(3, "PRD-103", "بيتزا شاورما فراخ سوبريم", "البيتزا الإيطالية", "وجبة كاملة (Meal)", "175.00 ج.م"));
        productsList.add(new ProductItemModel(4, "PRD-104", "ميكس جريل مشويات مشكل", "المشويات والباربيكيو", "طبق (Plate)", "320.00 ج.م"));
        productsList.add(new ProductItemModel(5, "PRD-105", "وجبة زنجر كرسبي عائلي", "وجبات الدجاج المقرمش", "وجبة كاملة (Meal)", "210.00 ج.م"));
        productsList.add(new ProductItemModel(6, "PRD-106", "باستا ألفريدو دجاج ومشروم", "المكرونة والباستا", "طبق (Plate)", "130.00 ج.م"));
        productsList.add(new ProductItemModel(7, "PRD-107", "كريب كرانشي دجاج حار", "الكريب والسناكس", "قطعة (Pcs)", "95.00 ج.م"));
        productsList.add(new ProductItemModel(8, "PRD-108", "عصير برتقال فريش طبيعي", "المشروبات والعصائر الفريش", "كوب كبير (Cup)", "50.00 ج.م"));
        productsList.add(new ProductItemModel(9, "PRD-109", "مولتن كيك شوكولاتة ساخنة", "الحلويات والوافل", "قطعة (Pcs)", "85.00 ج.م"));

        refreshSequenceNumbers();
        updateTotalCount();
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

    /**
     * Sets up live real-time search filtering.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                applySearchQuery(newValue);
            });
        }
    }

    private void applySearchQuery(String query) {
        if (filteredProductsList == null) return;

        filteredProductsList.setPredicate(prod -> {
            if (query == null || query.trim().isEmpty()) {
                return true;
            }
            String lowerCaseFilter = query.trim().toLowerCase();

            boolean matchName = prod.getName() != null && prod.getName().toLowerCase().contains(lowerCaseFilter);
            boolean matchRef = prod.getReferenceNo() != null && prod.getReferenceNo().toLowerCase().contains(lowerCaseFilter);
            boolean matchCat = prod.getCategory() != null && prod.getCategory().toLowerCase().contains(lowerCaseFilter);
            boolean matchUnit = prod.getUnit() != null && prod.getUnit().toLowerCase().contains(lowerCaseFilter);

            return matchName || matchRef || matchCat || matchUnit;
        });

        updateTotalCount();
    }

    // =========================================================================
    // CRUD Action Handlers
    // =========================================================================

    /**
     * 1. Add New Product
     */
    @FXML
    private void handleAddProduct(ActionEvent event) {
        String name = getSafeText(txtProdName);
        String ref = getSafeText(txtProdRef);
        String cat = cbProdCategory != null ? cbProdCategory.getValue() : "عام";
        String unit = cbProdUnit != null ? cbProdUnit.getValue() : "قطعة";
        String priceStr = getSafeText(txtProdPrice);

        if (name.isEmpty() || ref.isEmpty() || priceStr.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (اسم المنتج، الرقم المرجعي، والسعر)!", true);
            return;
        }

        // Check for duplicate reference
        for (ProductItemModel p : productsList) {
            if (p.getReferenceNo().equalsIgnoreCase(ref)) {
                showNotification("الرقم المرجعي (" + ref + ") مسجل مسبقًا لمنتج آخر (" + p.getName() + ")!", true);
                return;
            }
        }

        double priceVal = 0.0;
        try {
            priceVal = Double.parseDouble(priceStr.replace("ج.م", "").trim());
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال سعر صحيح بالأرقام!", true);
            return;
        }

        int newSeq = productsList.size() + 1;
        String formattedPrice = String.format(Locale.US, "%,.2f ج.م", priceVal);
        ProductItemModel newProduct = new ProductItemModel(newSeq, ref, name, cat != null ? cat : "عام", unit != null ? unit : "قطعة", formattedPrice);
        productsList.add(newProduct);
        refreshSequenceNumbers();
        updateTotalCount();

        if (tblProducts != null) {
            tblProducts.getSelectionModel().select(newProduct);
            tblProducts.scrollTo(newProduct);
        }

        showNotification("تمت إضافة المنتج (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 2. Edit Selected Product
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
        String cat = cbProdCategory != null ? cbProdCategory.getValue() : selected.getCategory();
        String unit = cbProdUnit != null ? cbProdUnit.getValue() : selected.getUnit();
        String priceStr = getSafeText(txtProdPrice);

        if (name.isEmpty() || ref.isEmpty() || priceStr.isEmpty()) {
            showNotification("لا يمكن ترك اسم المنتج، الرقم المرجعي، أو السعر فارغًا!", true);
            return;
        }

        double priceVal = 0.0;
        try {
            priceVal = Double.parseDouble(priceStr.replace("ج.م", "").trim());
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال سعر صحيح بالأرقام!", true);
            return;
        }

        selected.setName(name);
        selected.setReferenceNo(ref);
        selected.setCategory(cat != null ? cat : selected.getCategory());
        selected.setUnit(unit != null ? unit : selected.getUnit());
        selected.setPrice(String.format(Locale.US, "%,.2f ج.م", priceVal));

        if (tblProducts != null) {
            tblProducts.refresh();
        }

        showNotification("تم حفظ وتحديث بيانات المنتج (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Product
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
        confirm.setHeaderText("حذف المنتج: " + selected.getName());
        confirm.setContentText("هل أنت متأكد من حذف هذا المنتج من المنيو وقائمة الأصناف؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedName = selected.getName();
            productsList.remove(selected);
            refreshSequenceNumbers();
            updateTotalCount();
            handleClearFields(null);
            showNotification("تم حذف المنتج (" + deletedName + ") بنجاح! 🗑️", false);
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
        applySearchQuery(query);
    }

    /**
     * 6. Clear Search Handler
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        applySearchQuery("");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void populateFieldsFromModel(ProductItemModel model) {
        if (txtProdName != null) txtProdName.setText(model.getName());
        if (txtProdRef != null) txtProdRef.setText(model.getReferenceNo());
        if (cbProdCategory != null) cbProdCategory.setValue(model.getCategory());
        if (cbProdUnit != null) cbProdUnit.setValue(model.getUnit());
        if (txtProdPrice != null) txtProdPrice.setText(model.getPrice().replace("ج.م", "").trim());

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض بيانات: " + model.getName());
        }
    }

    private void refreshSequenceNumbers() {
        for (int i = 0; i < productsList.size(); i++) {
            productsList.get(i).setSeq(i + 1);
        }
    }

    private void updateTotalCount() {
        if (lblTotalCount != null) {
            int count = filteredProductsList != null ? filteredProductsList.size() : productsList.size();
            lblTotalCount.setText(String.valueOf(count));
        }
    }

    private String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
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
    // Product Item Data Model Class
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
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty referenceNoProperty() { return referenceNo; }
        public String getReferenceNo() { return referenceNo.get(); }
        public void setReferenceNo(String val) { this.referenceNo.set(val); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String val) { this.name.set(val); }

        public SimpleStringProperty categoryProperty() { return category; }
        public String getCategory() { return category.get(); }
        public void setCategory(String val) { this.category.set(val); }

        public SimpleStringProperty unitProperty() { return unit; }
        public String getUnit() { return unit.get(); }
        public void setUnit(String val) { this.unit.set(val); }

        public SimpleStringProperty priceProperty() { return price; }
        public String getPrice() { return price.get(); }
        public void setPrice(String val) { this.price.set(val); }
    }
}
