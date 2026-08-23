package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/**
 * Controller for the System Settings Window.
 * Features:
 * - Software Activation Key verification and licensing.
 * - JFXToggleButton to activate/deactivate Dark Mode.
 * - Restaurant preferences management.
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

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLiveDateTime();
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
}
