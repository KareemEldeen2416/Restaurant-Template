package Interfaces;

import com.jfoenix.controls.JFXButton;
import java.awt.Desktop;
import java.net.URI;
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
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Controller for the Technical Support and Contact Information Window.
 * Features:
 * - Company Logo display as an image.
 * - Direct contact info (Phone number, WhatsApp number) with icon indicators.
 * - Social Media links (Facebook, Instagram, LinkedIn).
 * - Official Website link (www.kareeemeldeen.com).
 * 
 * @author KareemEldeen
 */
public class support implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML Support Elements
    // =========================================================================
    @FXML private ImageView imgCompanyLogo;
    @FXML private JFXButton btnCallPhone;
    @FXML private JFXButton btnOpenWhatsApp;
    @FXML private JFXButton btnOpenWebsite;
    @FXML private JFXButton btnFacebook;
    @FXML private JFXButton btnInstagram;
    @FXML private JFXButton btnLinkedIn;

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
    // Contact & Social Media Action Handlers
    // =========================================================================

    @FXML
    private void handleCallPhone(ActionEvent event) {
        openUrlOrAlert("tel:+201012345678", "اتصال هاتفي", "أرقام الدعم الفني:\n📞 +20 101 234 5678\n📞 +20 122 345 6789\nمتاح يوميًا من 09:00 ص حتى 11:00 م");
    }

    @FXML
    private void handleOpenWhatsApp(ActionEvent event) {
        openUrlOrAlert("https://wa.me/201012345678", "واتساب الدعم الفني", "رابط محادثة واتساب المباشرة:\nhttps://wa.me/201012345678\nرقم الواتساب: +201012345678");
    }

    @FXML
    private void handleOpenWebsite(ActionEvent event) {
        openUrlOrAlert("https://www.kareeemeldeen.com", "الموقع الرسمي", "الموقع الإلكتروني الرسمي:\nhttps://www.kareeemeldeen.com");
    }

    @FXML
    private void handleOpenFacebook(ActionEvent event) {
        openUrlOrAlert("https://facebook.com/kareemeldeen.dev", "صفحة فيسبوك", "رابط الصفحة الرسمية على فيسبوك:\nhttps://facebook.com/kareemeldeen.dev");
    }

    @FXML
    private void handleOpenInstagram(ActionEvent event) {
        openUrlOrAlert("https://instagram.com/kareemeldeen.tech", "حساب انستغرام", "رابط الحساب الرسمي على انستغرام:\nhttps://instagram.com/kareemeldeen.tech");
    }

    @FXML
    private void handleOpenLinkedIn(ActionEvent event) {
        openUrlOrAlert("https://linkedin.com/in/kareemeldeen", "حساب لينكد إن", "رابط الحساب المهني على لينكد إن:\nhttps://linkedin.com/in/kareemeldeen");
    }

    private void openUrlOrAlert(String url, String title, String fallbackMsg) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showInfo(title, fallbackMsg);
            }
        } catch (Exception e) {
            showInfo(title, fallbackMsg);
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }
}
