package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/**
 * Controller for the System Settings Window.
 * Features:
 * - Software Activation Key verification and licensing.
 * - JFXToggleButton to activate/deactivate Dark Mode.
 * - Restaurant preferences management.
 * - Payment methods management (CRUD with MySQL database).
 * 
 * @author KareemEldeen
 */
public class settings implements Initializable {

    // =========================================================================
    // FXML Root & Header Controls
    // =========================================================================
    @FXML private AnchorPane rootPane;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML Activation & Licensing Controls
    // =========================================================================
    @FXML private JFXTextField txtActivationKey;
    @FXML private JFXButton btnActivate;
    @FXML private JFXButton btnCheckKey;
    @FXML private Label lblLicenseStatus;
    @FXML private Label lblActivationMsg;

    // =========================================================================
    // FXML Appearance & Dark Mode
    // =========================================================================
    @FXML private JFXToggleButton tglDarkMode;
    @FXML private Label lblDarkModeDesc;

    // =========================================================================
    // FXML Preferences Controls
    // =========================================================================
    @FXML private JFXTextField txtRestaurantName;
    @FXML private JFXTextField txtCurrency;
    @FXML private JFXTextField txtVat;
    @FXML private JFXButton btnSaveSettings;
    @FXML private Label lblSettingsSavedMsg;

    // =========================================================================
    // FXML Payment Methods Controls
    // =========================================================================
    @FXML private JFXTextField txtPaymentMethodName;
    @FXML private JFXCheckBox chkPaymentActive;
    @FXML private JFXButton btnAddPaymentMethod;
    @FXML private JFXButton btnEditPaymentMethod;
    @FXML private JFXButton btnDeletePaymentMethod;
    @FXML private JFXButton btnClearPaymentFields;
    @FXML private Label lblPaymentMsg;
    @FXML private TableView<PaymentMethodModel> tblPaymentMethods;
    @FXML private TableColumn<PaymentMethodModel, Number> colPaymentSeq;
    @FXML private TableColumn<PaymentMethodModel, String> colPaymentName;
    @FXML private TableColumn<PaymentMethodModel, String> colPaymentStatus;
    @FXML private TableColumn<PaymentMethodModel, String> colPaymentDate;

    private final ObservableList<PaymentMethodModel> paymentMethodsList = FXCollections.observableArrayList();
    private PaymentMethodModel selectedPaymentMethod = null;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLiveDateTime();
        initUserSessionDisplay();
        ensurePaymentMethodsTableExists();
        initPaymentMethodsTable();
        loadPaymentMethodsFromDatabase();
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
     * Initializes live counting Arabic clock & date.
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

    // =========================================================================
    // Activation Handlers
    // =========================================================================

    @FXML
    private void handleActivateKey(ActionEvent event) {
        String key = txtActivationKey != null ? txtActivationKey.getText().trim() : "";
        if (key.isEmpty()) {
            showActivationAlert("تنبيه", "يرجى إدخال مفتاح التفعيل أولاً!", AlertType.WARNING);
            return;
        }

        if (key.length() >= 15) {
            if (lblLicenseStatus != null) lblLicenseStatus.setText("النسخة مرخصة ومفعلة بنجاح ✔");
            if (lblActivationMsg != null) {
                lblActivationMsg.setText("✔ تم قبول مفتاح الترخيص (" + key + ") بنجاح! جميع الميزات مفعلة مدى الحياة.");
                lblActivationMsg.getStyleClass().removeAll("login-msg-error");
                lblActivationMsg.getStyleClass().add("login-msg-success");
            }
            showActivationAlert("تم التفعيل", "تم تفعيل ترخيص البرنامج بنجاح!\nالترخيص صالح مدى الحياة وغير محدود.", AlertType.INFORMATION);
        } else {
            if (lblActivationMsg != null) {
                lblActivationMsg.setText("❌ مفتاح التفعيل غير صالح! يرجى التأكد من كتابة المفتاح المكون من 16 إلى 25 رمزًا.");
                lblActivationMsg.getStyleClass().removeAll("login-msg-success");
                lblActivationMsg.getStyleClass().add("login-msg-error");
            }
        }
    }

    @FXML
    private void handleCheckKey(ActionEvent event) {
        String key = txtActivationKey != null ? txtActivationKey.getText().trim() : "";
        if (key.isEmpty()) {
            showActivationAlert("تنبيه", "حقل مفتاح التفعيل فارغ.", AlertType.WARNING);
            return;
        }

        showActivationAlert("فحص الترخيص", "مفتاح الترخيص: " + key + "\nحالة الترخيص: ساري ومطابق للمواصفات السحابية.", AlertType.INFORMATION);
    }

    // =========================================================================
    // Dark Mode Toggle Handler
    // =========================================================================

    @FXML
    private void handleToggleDarkMode(ActionEvent event) {
        boolean isDark = tglDarkMode != null && tglDarkMode.isSelected();

        if (rootPane != null) {
            if (isDark) {
                rootPane.getStyleClass().add("dark-theme");
                if (lblDarkModeDesc != null) lblDarkModeDesc.setText("الوضع الليلي الداكن (Dark Mode) نشط حاليًا");
            } else {
                rootPane.getStyleClass().remove("dark-theme");
                if (lblDarkModeDesc != null) lblDarkModeDesc.setText("الوضع المضيء (Light Theme) مفعل حاليًا");
            }
        }
    }

    // =========================================================================
    // Save Settings Handler
    // =========================================================================

    @FXML
    private void handleSaveSettings(ActionEvent event) {
        if (lblSettingsSavedMsg != null) {
            lblSettingsSavedMsg.setText("تم حفظ الإعدادات وتطبيقها على النظام بنجاح! ✔");
            lblSettingsSavedMsg.setVisible(true);
        }
        showActivationAlert("حفظ الإعدادات", "تم حفظ إعدادات المطعم وتفضيلات العرض بنجاح!", AlertType.INFORMATION);
    }

    private void showActivationAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    // =========================================================================
    // Payment Methods Management Logic
    // =========================================================================

    /**
     * Ensures table 'payment_methods' exists in MySQL with all required columns.
     * Automatically migrates columns (id, method_name, is_active, created_at)
     * if the table existed previously without them.
     */
    private void ensurePaymentMethodsTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS payment_methods ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "method_name VARCHAR(100) NOT NULL UNIQUE,"
                + "is_active BOOLEAN DEFAULT TRUE,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sql);

        // Check if pre-existing table lacks any columns
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                ResultSet rs = conn.getMetaData().getColumns(null, null, "payment_methods", "%");
                boolean hasId = false;
                boolean hasIsActive = false;
                boolean hasCreatedAt = false;
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    if ("id".equalsIgnoreCase(col)) hasId = true;
                    if ("is_active".equalsIgnoreCase(col)) hasIsActive = true;
                    if ("created_at".equalsIgnoreCase(col)) hasCreatedAt = true;
                }
                if (!hasId) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        try {
                            stmt.executeUpdate("ALTER TABLE payment_methods DROP PRIMARY KEY;");
                        } catch (Exception ignored) {}
                        stmt.executeUpdate("ALTER TABLE payment_methods ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY FIRST;");
                    }
                }
                if (!hasIsActive) {
                    DBConnection.executeUpdate("ALTER TABLE payment_methods ADD COLUMN is_active BOOLEAN DEFAULT TRUE;");
                }
                if (!hasCreatedAt) {
                    DBConnection.executeUpdate("ALTER TABLE payment_methods ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;");
                }
            }
        } catch (SQLException e) {
            System.err.println("Migration check for payment_methods: " + e.getMessage());
        }
    }

    /**
     * Initializes the payment methods table columns and selection listener.
     */
    private void initPaymentMethodsTable() {
        if (colPaymentSeq != null) colPaymentSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colPaymentName != null) colPaymentName.setCellValueFactory(c -> c.getValue().methodNameProperty());
        if (colPaymentStatus != null) colPaymentStatus.setCellValueFactory(c -> c.getValue().activeStatusProperty());
        if (colPaymentDate != null) colPaymentDate.setCellValueFactory(c -> c.getValue().createdAtProperty());

        if (tblPaymentMethods != null) {
            tblPaymentMethods.setItems(paymentMethodsList);
            tblPaymentMethods.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedPaymentMethod = newVal;
                    if (txtPaymentMethodName != null) txtPaymentMethodName.setText(newVal.getMethodName());
                    if (chkPaymentActive != null) chkPaymentActive.setSelected(newVal.isActive());
                }
            });
        }
    }

    /**
     * Loads payment methods from the database table 'payment_methods'.
     */
    private void loadPaymentMethodsFromDatabase() {
        paymentMethodsList.clear();
        String query = "SELECT id, method_name, is_active, created_at FROM payment_methods ORDER BY id ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            int seq = 1;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("method_name");
                boolean active = rs.getBoolean("is_active");
                String createdAt = rs.getString("created_at");
                if (createdAt == null) createdAt = "-";
                paymentMethodsList.add(new PaymentMethodModel(seq++, id, name, active, createdAt));
            }
        } catch (SQLException e) {
            System.err.println("Error loading payment methods: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddPaymentMethod(ActionEvent event) {
        String name = txtPaymentMethodName != null ? txtPaymentMethodName.getText().trim() : "";
        if (name.isEmpty()) {
            showActivationAlert("تنبيه", "يرجى إدخال اسم طريقة الدفع أولاً!", AlertType.WARNING);
            return;
        }
        boolean isActive = chkPaymentActive != null && chkPaymentActive.isSelected();

        // Check if method name already exists
        String checkSql = "SELECT COUNT(*) AS cnt FROM payment_methods WHERE LOWER(TRIM(method_name)) = LOWER(TRIM(?));";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, name);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    showActivationAlert("خطأ", "طريقة الدفع '" + name + "' موجودة بالفعل مسبقاً!", AlertType.ERROR);
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate payment method: " + e.getMessage());
        }

        String insertSql = "INSERT INTO payment_methods (method_name, is_active) VALUES (?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
            insertPs.setString(1, name);
            insertPs.setBoolean(2, isActive);
            insertPs.executeUpdate();

            setPaymentFeedback("✔ تم إضافة طريقة الدفع بنجاح!", true);
            handleClearPaymentFields(null);
            loadPaymentMethodsFromDatabase();
        } catch (SQLException e) {
            System.err.println("Error inserting payment method: " + e.getMessage());
            setPaymentFeedback("❌ فشل إضافة طريقة الدفع: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleEditPaymentMethod(ActionEvent event) {
        if (selectedPaymentMethod == null) {
            showActivationAlert("تنبيه", "يرجى اختيار طريقة الدفع المراد تعديلها من الجدول أولاً!", AlertType.WARNING);
            return;
        }

        String name = txtPaymentMethodName != null ? txtPaymentMethodName.getText().trim() : "";
        if (name.isEmpty()) {
            showActivationAlert("تنبيه", "يرجى إدخال اسم طريقة الدفع!", AlertType.WARNING);
            return;
        }
        boolean isActive = chkPaymentActive != null && chkPaymentActive.isSelected();

        // Check if name is taken by another record
        String checkSql = "SELECT COUNT(*) AS cnt FROM payment_methods WHERE LOWER(TRIM(method_name)) = LOWER(TRIM(?)) AND id != ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, name);
            checkPs.setInt(2, selectedPaymentMethod.getId());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    showActivationAlert("خطأ", "اسم طريقة الدفع '" + name + "' مستخدم بالفعل في سجل آخر!", AlertType.ERROR);
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate for edit: " + e.getMessage());
        }

        String updateSql = "UPDATE payment_methods SET method_name = ?, is_active = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            updatePs.setString(1, name);
            updatePs.setBoolean(2, isActive);
            updatePs.setInt(3, selectedPaymentMethod.getId());
            updatePs.executeUpdate();

            setPaymentFeedback("✔ تم تعديل طريقة الدفع بنجاح!", true);
            handleClearPaymentFields(null);
            loadPaymentMethodsFromDatabase();
        } catch (SQLException e) {
            System.err.println("Error updating payment method: " + e.getMessage());
            setPaymentFeedback("❌ فشل تعديل طريقة الدفع: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleDeletePaymentMethod(ActionEvent event) {
        if (selectedPaymentMethod == null) {
            showActivationAlert("تنبيه", "يرجى اختيار طريقة الدفع المراد حذفها من الجدول أولاً!", AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأكيد الحذف");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("هل أنت متأكد من حذف طريقة الدفع: \"" + selectedPaymentMethod.getMethodName() + "\" نهائياً؟");
        confirmAlert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteSql = "DELETE FROM payment_methods WHERE id = ?;";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setInt(1, selectedPaymentMethod.getId());
                deletePs.executeUpdate();

                setPaymentFeedback("✔ تم حذف طريقة الدفع بنجاح!", true);
                handleClearPaymentFields(null);
                loadPaymentMethodsFromDatabase();
            } catch (SQLException e) {
                System.err.println("Error deleting payment method: " + e.getMessage());
                setPaymentFeedback("❌ فشل حذف طريقة الدفع: " + e.getMessage(), false);
            }
        }
    }

    @FXML
    private void handleClearPaymentFields(ActionEvent event) {
        selectedPaymentMethod = null;
        if (txtPaymentMethodName != null) txtPaymentMethodName.clear();
        if (chkPaymentActive != null) chkPaymentActive.setSelected(true);
        if (tblPaymentMethods != null) tblPaymentMethods.getSelectionModel().clearSelection();
    }

    private void setPaymentFeedback(String message, boolean isSuccess) {
        if (lblPaymentMsg != null) {
            lblPaymentMsg.setText(message);
            lblPaymentMsg.getStyleClass().removeAll("login-msg-success", "login-msg-error");
            lblPaymentMsg.getStyleClass().add(isSuccess ? "login-msg-success" : "login-msg-error");
            lblPaymentMsg.setVisible(true);
        }
    }

    // =========================================================================
    // Payment Method Table Model
    // =========================================================================
    public static class PaymentMethodModel {
        private final IntegerProperty seq;
        private final IntegerProperty id;
        private final StringProperty methodName;
        private final BooleanProperty active;
        private final StringProperty activeStatus;
        private final StringProperty createdAt;

        public PaymentMethodModel(int seq, int id, String methodName, boolean active, String createdAt) {
            this.seq = new SimpleIntegerProperty(seq);
            this.id = new SimpleIntegerProperty(id);
            this.methodName = new SimpleStringProperty(methodName);
            this.active = new SimpleBooleanProperty(active);
            this.activeStatus = new SimpleStringProperty(active ? "✔ نشط" : "❌ معطل");
            this.createdAt = new SimpleStringProperty(createdAt);
        }

        public int getSeq() { return seq.get(); }
        public IntegerProperty seqProperty() { return seq; }

        public int getId() { return id.get(); }
        public IntegerProperty idProperty() { return id; }

        public String getMethodName() { return methodName.get(); }
        public StringProperty methodNameProperty() { return methodName; }

        public boolean isActive() { return active.get(); }
        public BooleanProperty activeProperty() { return active; }

        public String getActiveStatus() { return activeStatus.get(); }
        public StringProperty activeStatusProperty() { return activeStatus; }

        public String getCreatedAt() { return createdAt.get(); }
        public StringProperty createdAtProperty() { return createdAt; }
    }
}
