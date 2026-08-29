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
 * Main Controller for Restaurant Management System Dashboard.
 * 
 * Features:
 * - Displays active user's full name and role in the top header bar.
 * - Dynamically activates/deactivates window cards based on user's 8-bit access_rights.
 * - Live Arabic clock and calendar date.
 * - Modular window routing.
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

    private String userFullName = "المدير العام";
    private String userRole = "مدير النظام";
    private String userAccessRights = "11111111";

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        applyAccessRights(userAccessRights);
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
     * Sets the logged in user full name, role, and applies access rights to dashboard cards.
     *
     * @param fullName Full name of the user to display in the header bar
     * @param role Job title or department
     * @param accessRights 8-character string of '0' and '1' representing permissions
     */
    public void setLoggedInUser(String fullName, String role, String accessRights) {
        this.userFullName = fullName != null && !fullName.trim().isEmpty() ? fullName : "المستخدم";
        this.userRole = role != null && !role.trim().isEmpty() ? role : "موظف";
        this.userAccessRights = accessRights != null && accessRights.length() >= 8 ? accessRights : "11111111";

        if (lblUserName != null) {
            lblUserName.setText(this.userFullName);
        }
        if (lblUserRole != null) {
            lblUserRole.setText(this.userRole);
        }

        applyAccessRights(this.userAccessRights);
    }

    /**
     * Backwards-compatible overload for setting user info.
     */
    public void setUserInfo(String userName, String userRole) {
        setLoggedInUser(userName, userRole, "11111111");
    }

    /**
     * Activates only cards for windows allowed by access_rights:
     * 1st: Cashier, 2nd: Settings, 3rd: Reports, 4th: Sales,
     * 5th: Employees, 6th: Customers, 7th: Reservations, 8th: Inventory
     */
    public void applyAccessRights(String rights) {
        if (rights == null || rights.length() < 8) {
            rights = "11111111"; // Default full access if not provided
        }

        boolean allowCashier      = rights.charAt(0) == '1';
        boolean allowSettings     = rights.charAt(1) == '1';
        boolean allowReports      = rights.charAt(2) == '1';
        boolean allowSales        = rights.charAt(3) == '1';
        boolean allowEmployees    = rights.charAt(4) == '1';
        boolean allowCustomers    = rights.charAt(5) == '1';
        boolean allowReservations = rights.charAt(6) == '1';
        boolean allowInventory    = rights.charAt(7) == '1';

        setCardState(cardCashier, allowCashier);
        setCardState(cardSettings, allowSettings);
        setCardState(cardReports, allowReports);
        setCardState(cardSales, allowSales);
        setCardState(cardEmployees, allowEmployees);
        setCardState(cardCustomers, allowCustomers);
        setCardState(cardReservations, allowReservations);
        setCardState(cardInventory, allowInventory);

        // Products card is active if either Cashier or Inventory is allowed
        setCardState(cardProducts, allowCashier || allowInventory);

        // Support card is always active for general help and contact
        setCardState(cardSupport, true);
    }

    /**
     * Sets active/deactivated state and styling on a dashboard card.
     */
    private void setCardState(JFXButton card, boolean enabled) {
        if (card != null) {
            card.setDisable(!enabled);
            card.setOpacity(enabled ? 1.0 : 0.35);
            if (!enabled) {
                if (!card.getStyleClass().contains("card-disabled")) {
                    card.getStyleClass().add("card-disabled");
                }
            } else {
                card.getStyleClass().remove("card-disabled");
            }
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
