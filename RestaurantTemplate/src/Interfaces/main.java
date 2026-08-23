package Interfaces;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main Controller for Restaurant Management System Dashboard with JFoenix Material Cards.
 * Handles live Arabic clock & date, responsive cards, user info, and module navigation.
 *
 * @author KareemEldeen
 */
public class main implements Initializable {

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTime;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private JFXButton cardCashier;

    @FXML
    private JFXButton cardSales;

    @FXML
    private JFXButton cardProducts;

    @FXML
    private JFXButton cardInventory;

    @FXML
    private JFXButton cardCustomers;

    @FXML
    private JFXButton cardEmployees;

    @FXML
    private JFXButton cardReservations;

    @FXML
    private JFXButton cardReports;

    @FXML
    private JFXButton cardSettings;

    @FXML
    private JFXButton cardSupport;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
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

    /**
     * Updates the date and live time labels.
     */
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
     * Set the current active user name and role dynamically.
     *
     * @param userName Name of the user
     * @param userRole Role description
     */
    public void setUserInfo(String userName, String userRole) {
        if (lblUserName != null && userName != null && !userName.trim().isEmpty()) {
            lblUserName.setText(userName);
        }
        if (lblUserRole != null && userRole != null && !userRole.trim().isEmpty()) {
            lblUserRole.setText(userRole);
        }
    }

    // =========================================================================
    // Card Navigation Action Handlers
    // =========================================================================

    @FXML
    private void handleOpenCashier(ActionEvent event) {
        openModule("Cashier.fxml", "الكاشير ونقاط البيع");
    }

    @FXML
    private void handleOpenSales(ActionEvent event) {
        openModule("Sales.fxml", "المبيعات والفواتير");
    }

    @FXML
    private void handleOpenProducts(ActionEvent event) {
        openModule("Products.fxml", "المنتجات وقائمة الطعام");
    }

    @FXML
    private void handleOpenInventory(ActionEvent event) {
        openModule("Inventory.fxml", "إدارة المخزون");
    }

    @FXML
    private void handleOpenCustomers(ActionEvent event) {
        openModule("Customers.fxml", "إدارة العملاء");
    }

    @FXML
    private void handleOpenEmployees(ActionEvent event) {
        openModule("employees.fxml", "شؤون الموظفين");
    }

    @FXML
    private void handleOpenReservations(ActionEvent event) {
        openModule("Reservations.fxml", "الحجوزات والطاولات");
    }

    @FXML
    private void handleOpenReports(ActionEvent event) {
        openModule("Reports.fxml", "التقارير والإحصائيات");
    }

    @FXML
    private void handleOpenSettings(ActionEvent event) {
        openModule("Settings.fxml", "إعدادات النظام");
    }

    @FXML
    private void handleOpenSupport(ActionEvent event) {
        openModule("Support.fxml", "الدعم الفني والمساعدة");
    }

    /**
     * Generic loader for opening child modules safely with friendly Arabic feedback.
     *
     * @param fxmlName Name of the target FXML file (e.g. employees.fxml)
     * @param windowTitle Arabic title for the window
     */
    private void openModule(String fxmlName, String windowTitle) {
        String resourcePath = "/Interfaces/" + fxmlName;
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            resource = getClass().getResource("/Interfaces/" + fxmlName.toLowerCase());
        }

        if (resource != null) {
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle(windowTitle + " - نظام إدارة المطعم");
                stage.initModality(Modality.WINDOW_MODAL);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMinWidth(960);
                stage.setMinHeight(640);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                showInfoAlert(windowTitle, "حدث خطأ أثناء تحميل واجهة: " + windowTitle + "\n" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Module interface file is not yet created - show elegant info dialog
            showInfoAlert(windowTitle, "تم الضغط على نافذة [" + windowTitle + "].\nواجهة العمل الخاصة بهذا القسم قيد التطوير وستعمل فور إضافتها إلى المشروع.");
        }
    }

    /**
     * Displays a styled informational dialog in Arabic.
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("قسم " + title);
        alert.setContentText(message);

        // Apply theme styling to alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        try {
            URL cssUrl = getClass().getResource("/Interfaces/style.css");
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }

        alert.showAndWait();
    }
}
