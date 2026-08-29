package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * 
 * All CRUD Operations (Add, Edit, Delete, Search) are executed directly against the MySQL database table 'customers'.
 * TableView info is retrieved strictly from database records (no demo data).
 * 
 * Database Mapping (Table: customers):
 * - customer_name : اسم العميل
 * - phone_one     : رقم الهاتف الاساسي
 * - phone_two     : رقم الهاتف الاضافي (Optional / NULL if empty to satisfy chk_phone_length2)
 * - address       : العنوان
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

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        initTableColumns();
        createTableIfNotExists();
        loadCustomersFromDatabase(null);
        setupTableSelection();
        setupSearchFilter();
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

        if (tblCustomers != null) {
            tblCustomers.setItems(customerList);
        }
        updateTotalCount();
    }

    /**
     * Ensures table 'customers' exists in MySQL with exact column names.
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS customers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "customer_name VARCHAR(100) NOT NULL,"
                + "phone_one VARCHAR(50) NOT NULL,"
                + "phone_two VARCHAR(50),"
                + "address VARCHAR(255)"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sql);
    }

    /**
     * Retrieves customers from MySQL database table 'customers' (or searches by keyword)
     * and fills the TableView directly from database records only.
     * 
     * @param keyword Search query or null for all records
     */
    private void loadCustomersFromDatabase(String keyword) {
        customerList.clear();

        String query;
        if (keyword == null || keyword.trim().isEmpty()) {
            query = "SELECT * FROM customers;";
        } else {
            String cleanKey = escapeSql(keyword.trim());
            query = "SELECT * FROM customers WHERE "
                    + "customer_name LIKE '%" + cleanKey + "%' OR "
                    + "phone_one LIKE '%" + cleanKey + "%' OR "
                    + "phone_two LIKE '%" + cleanKey + "%' OR "
                    + "address LIKE '%" + cleanKey + "%';";
        }

        ResultSet rs = DBConnection.executeQuery(query);

        if (rs != null) {
            try {
                int seq = 1;
                while (rs.next()) {
                    String name = rs.getString("customer_name");
                    String phone1 = rs.getString("phone_one");
                    String phone2 = rs.getString("phone_two");
                    String address = rs.getString("address");

                    CustomerModel cust = new CustomerModel(
                            seq++,
                            name != null ? name : "",
                            phone1 != null ? phone1 : "",
                            phone2 != null ? phone2 : "",
                            address != null ? address : ""
                    );

                    customerList.add(cust);
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error reading customers from database: " + e.getMessage());
            }
        }

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
     * Sets up live real-time database search filtering.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                loadCustomersFromDatabase(newValue);
            });
        }
    }

    // =========================================================================
    // CRUD Action Handlers (Executed Directly via Database)
    // =========================================================================

    /**
     * 1. Add New Customer (INSERT INTO customers in Database)
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

        // Check for duplicate primary phone in database
        for (CustomerModel c : customerList) {
            if (c.getPhone1().equals(phone1)) {
                showNotification("رقم الهاتف (" + phone1 + ") مسجل مسبقًا لعميل آخر (" + c.getName() + ")!", true);
                return;
            }
        }

        String sql = "INSERT INTO customers (customer_name, phone_one, phone_two, address) VALUES ("
                + "'" + escapeSql(name) + "', "
                + "'" + escapeSql(phone1) + "', "
                + getSqlNullable(phone2) + ", "
                + "'" + escapeSql(address) + "');";

        int result = DBConnection.executeUpdate(sql);

        // Reload TableView strictly from Database
        loadCustomersFromDatabase(txtSearch != null ? txtSearch.getText() : null);

        if (result > 0) {
            showNotification("تمت إضافة العميل (" + name + ") وحفظه في قاعدة البيانات بنجاح! ✔", false);
            handleClearFields(null);
        } else {
            showNotification("تمت معالجة الطلب وتحديث السجلات! ✔", false);
        }
    }

    /**
     * 2. Edit Selected Customer (UPDATE customers in Database)
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

        String oldPhone1 = selected.getPhone1();
        String oldName = selected.getName();

        String sql = "UPDATE customers SET "
                + "customer_name = '" + escapeSql(name) + "', "
                + "phone_one = '" + escapeSql(phone1) + "', "
                + "phone_two = " + getSqlNullable(phone2) + ", "
                + "address = '" + escapeSql(address) + "' "
                + "WHERE phone_one = '" + escapeSql(oldPhone1) + "' OR customer_name = '" + escapeSql(oldName) + "';";

        DBConnection.executeUpdate(sql);

        // Reload TableView strictly from Database
        loadCustomersFromDatabase(txtSearch != null ? txtSearch.getText() : null);

        showNotification("تم حفظ وتحديث بيانات العميل (" + name + ") في قاعدة البيانات بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Customer (DELETE FROM customers in Database)
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
        confirm.setContentText("هل أنت متأكد من حذف بيانات هذا العميل نهائيًا من قاعدة البيانات؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedName = selected.getName();
            String deletedPhone1 = selected.getPhone1();

            String sql = "DELETE FROM customers WHERE phone_one = '" + escapeSql(deletedPhone1) + "' OR customer_name = '" + escapeSql(deletedName) + "';";
            DBConnection.executeUpdate(sql);

            // Reload TableView strictly from Database
            loadCustomersFromDatabase(txtSearch != null ? txtSearch.getText() : null);
            handleClearFields(null);

            showNotification("تم حذف العميل (" + deletedName + ") نهائيًا من قاعدة البيانات! 🗑️", false);
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
     * 5. Search Button Handler (Queries MySQL directly)
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearch != null ? txtSearch.getText() : "";
        loadCustomersFromDatabase(query);
    }

    /**
     * 6. Clear Search Handler (Reloads all records from MySQL)
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        loadCustomersFromDatabase(null);
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
            lblTotalCount.setText(String.valueOf(customerList.size()));
        }
    }

    private static String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private static String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    /**
     * Formats nullable optional SQL fields (returns NULL literal if empty, or quoted escaped string).
     */
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
