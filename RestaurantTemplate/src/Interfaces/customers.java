package Interfaces;

import com.jfoenix.controls.JFXButton;
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
 * Controller for the Customers Management Window.
 * Features:
 * - Live Arabic clock & date in the top bar.
 * - Form fields for Customer Info (Name, Phone 1, Phone 2, Address).
 * - Action buttons for Add, Edit, Delete, and Clear.
 * - Real-time Search by Name, Phone, or Address.
 * - Interactive TableView displaying customer records.
 * 
 * @author KareemEldeen
 */
public class customers implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML Customer Form Inputs (4 Fields)
    // =========================================================================
    @FXML private JFXTextField txtName;
    @FXML private JFXTextField txtPhone1;
    @FXML private JFXTextField txtPhone2;
    @FXML private JFXTextField txtAddress;

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

    @FXML private TableView<CustomerModel> tblCustomers;
    @FXML private TableColumn<CustomerModel, Number> colSeq;
    @FXML private TableColumn<CustomerModel, String> colName;
    @FXML private TableColumn<CustomerModel, String> colPhone1;
    @FXML private TableColumn<CustomerModel, String> colPhone2;
    @FXML private TableColumn<CustomerModel, String> colAddress;

    // =========================================================================
    // Data Structures & Timers
    // =========================================================================
    private final ObservableList<CustomerModel> customerList = FXCollections.observableArrayList();
    private FilteredList<CustomerModel> filteredCustomerList;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
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
        if (lblDate != null) {
            lblDate.setText(now.format(dateFormatter));
        }
        if (lblTime != null) {
            lblTime.setText(now.format(timeFormatter));
        }
    }

    /**
     * Maps table columns to CustomerModel properties.
     */
    private void initTableColumns() {
        if (colSeq != null) colSeq.setCellValueFactory(cellData -> cellData.getValue().seqProperty());
        if (colName != null) colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        if (colPhone1 != null) colPhone1.setCellValueFactory(cellData -> cellData.getValue().phone1Property());
        if (colPhone2 != null) colPhone2.setCellValueFactory(cellData -> cellData.getValue().phone2Property());
        if (colAddress != null) colAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());

        filteredCustomerList = new FilteredList<>(customerList, p -> true);
        if (tblCustomers != null) {
            tblCustomers.setItems(filteredCustomerList);
        }
        updateTotalCount();
    }

    /**
     * Pre-populates sample customer data for realistic presentation.
     */
    private void loadInitialMockData() {
        customerList.add(new CustomerModel(1, "محمد طارق الأحمدي", "01099887766", "01122334455", "القاهرة - التجمع الخامس، شارع التسعين"));
        customerList.add(new CustomerModel(2, "نورهان علاء الدين", "01233445566", "01088776655", "الجيزة - الشيخ زايد، حي النرجس"));
        customerList.add(new CustomerModel(3, "مروان خالد عبد العزيز", "01155667788", "", "القاهرة - مصر الجديدة، ميدان الكوربة"));
        customerList.add(new CustomerModel(4, "ياسمين شريف سامي", "01544332211", "01288990011", "الجيزة - الدقي، شارع مصدق"));
        customerList.add(new CustomerModel(5, "إيهاب فاروق النجار", "01011223344", "", "القاهرة - المعادي، دجلة"));

        refreshSequenceNumbers();
        updateTotalCount();
    }

    /**
     * Populates form fields when selecting a row in the TableView.
     */
    private void setupTableSelection() {
        if (tblCustomers != null) {
            tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
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
        if (filteredCustomerList == null) return;

        filteredCustomerList.setPredicate(cust -> {
            if (query == null || query.trim().isEmpty()) {
                return true;
            }
            String lowerCaseFilter = query.trim().toLowerCase();

            boolean matchName = cust.getName() != null && cust.getName().toLowerCase().contains(lowerCaseFilter);
            boolean matchPhone1 = cust.getPhone1() != null && cust.getPhone1().toLowerCase().contains(lowerCaseFilter);
            boolean matchPhone2 = cust.getPhone2() != null && cust.getPhone2().toLowerCase().contains(lowerCaseFilter);
            boolean matchAddress = cust.getAddress() != null && cust.getAddress().toLowerCase().contains(lowerCaseFilter);

            return matchName || matchPhone1 || matchPhone2 || matchAddress;
        });

        updateTotalCount();
    }

    // =========================================================================
    // CRUD Action Handlers
    // =========================================================================

    /**
     * 1. Add New Customer
     */
    @FXML
    private void handleAddCustomer(ActionEvent event) {
        String name = getSafeText(txtName);
        String phone1 = getSafeText(txtPhone1);
        String phone2 = getSafeText(txtPhone2);
        String address = getSafeText(txtAddress);

        if (name.isEmpty() || phone1.isEmpty() || address.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (اسم العميل، رقم الهاتف الأساسي، والعنوان)!", true);
            return;
        }

        // Check for duplicate phone number
        for (CustomerModel c : customerList) {
            if (c.getPhone1().equals(phone1)) {
                showNotification("رقم الهاتف (" + phone1 + ") مسجل مسبقًا لعميل آخر (" + c.getName() + ")!", true);
                return;
            }
        }

        int newSeq = customerList.size() + 1;
        CustomerModel newCustomer = new CustomerModel(newSeq, name, phone1, phone2, address);
        customerList.add(newCustomer);
        refreshSequenceNumbers();
        updateTotalCount();

        if (tblCustomers != null) {
            tblCustomers.getSelectionModel().select(newCustomer);
            tblCustomers.scrollTo(newCustomer);
        }

        showNotification("تمت إضافة العميل (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 2. Edit Selected Customer
     */
    @FXML
    private void handleEditCustomer(ActionEvent event) {
        CustomerModel selected = tblCustomers != null ? tblCustomers.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار عميل من الجدول لتعديل بياناته!", true);
            return;
        }

        String name = getSafeText(txtName);
        String phone1 = getSafeText(txtPhone1);
        String phone2 = getSafeText(txtPhone2);
        String address = getSafeText(txtAddress);

        if (name.isEmpty() || phone1.isEmpty()) {
            showNotification("لا يمكن ترك اسم العميل أو رقم الهاتف فارغًا!", true);
            return;
        }

        selected.setName(name);
        selected.setPhone1(phone1);
        selected.setPhone2(phone2);
        selected.setAddress(address);

        if (tblCustomers != null) {
            tblCustomers.refresh();
        }

        showNotification("تم حفظ وتحديث بيانات العميل (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Customer
     */
    @FXML
    private void handleDeleteCustomer(ActionEvent event) {
        CustomerModel selected = tblCustomers != null ? tblCustomers.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار عميل من الجدول لحذفه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("حذف العميل: " + selected.getName());
        confirm.setContentText("هل أنت متأكد من حذف بيانات هذا العميل نهائيًا؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedName = selected.getName();
            customerList.remove(selected);
            refreshSequenceNumbers();
            updateTotalCount();
            handleClearFields(null);
            showNotification("تم حذف العميل (" + deletedName + ") بنجاح! 🗑️", false);
        }
    }

    /**
     * 4. Clear Form Fields
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        if (txtName != null) txtName.clear();
        if (txtPhone1 != null) txtPhone1.clear();
        if (txtPhone2 != null) txtPhone2.clear();
        if (txtAddress != null) txtAddress.clear();

        if (tblCustomers != null) {
            tblCustomers.getSelectionModel().clearSelection();
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال عميل جديد.");
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
    // Helpers & Model Creation
    // =========================================================================

    private void populateFieldsFromModel(CustomerModel model) {
        if (txtName != null) txtName.setText(model.getName());
        if (txtPhone1 != null) txtPhone1.setText(model.getPhone1());
        if (txtPhone2 != null) txtPhone2.setText(model.getPhone2());
        if (txtAddress != null) txtAddress.setText(model.getAddress());

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض بيانات: " + model.getName());
        }
    }

    private void refreshSequenceNumbers() {
        for (int i = 0; i < customerList.size(); i++) {
            customerList.get(i).setSeq(i + 1);
        }
    }

    private void updateTotalCount() {
        if (lblTotalCount != null) {
            int count = filteredCustomerList != null ? filteredCustomerList.size() : customerList.size();
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
    // Customer Data Model Class
    // =========================================================================
    public static class CustomerModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone1;
        private final SimpleStringProperty phone2;
        private final SimpleStringProperty address;

        public CustomerModel(int seq, String name, String phone1, String phone2, String address) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.phone1 = new SimpleStringProperty(phone1);
            this.phone2 = new SimpleStringProperty(phone2);
            this.address = new SimpleStringProperty(address);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String val) { this.name.set(val); }

        public SimpleStringProperty phone1Property() { return phone1; }
        public String getPhone1() { return phone1.get(); }
        public void setPhone1(String val) { this.phone1.set(val); }

        public SimpleStringProperty phone2Property() { return phone2; }
        public String getPhone2() { return phone2.get(); }
        public void setPhone2(String val) { this.phone2.set(val); }

        public SimpleStringProperty addressProperty() { return address; }
        public String getAddress() { return address.get(); }
        public void setAddress(String val) { this.address.set(val); }
    }
}
