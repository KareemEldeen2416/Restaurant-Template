package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Restaurant Login Interface.
 * 
 * Features:
 * - Authentication directly against MySQL database table 'employees' by user_name and user_password.
 * - Passes full_name and access_rights to the main dashboard.
 * - Activates only cards permitted by access_rights.
 * 
 * @author KareemEldeen
 */
public class login implements Initializable {

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTime;

    @FXML
    private JFXTextField txtUsername;

    @FXML
    private JFXPasswordField txtPasswordHidden;

    @FXML
    private JFXTextField txtPasswordVisible;

    @FXML
    private JFXButton btnTogglePassword;

    @FXML
    private SVGPath svgEyeIcon;

    @FXML
    private Label lblMessage;

    @FXML
    private JFXButton btnLogin;

    private boolean isPasswordShown = false;

    // SVG Eye Icons for Show / Hide Password
    private static final String SVG_EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String SVG_EYE_CLOSED = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initPasswordFields();
        setupEnterKeyLogin();
    }

    /**
     * Initializes the live counting clock and date in Arabic for the top bar.
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
     * Binds masked and visible password fields together seamlessly.
     */
    private void initPasswordFields() {
        if (txtPasswordHidden != null && txtPasswordVisible != null) {
            txtPasswordHidden.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
        }
    }

    /**
     * Enables pressing Enter key inside username or password field to trigger login.
     */
    private void setupEnterKeyLogin() {
        if (txtUsername != null) {
            txtUsername.setOnAction(this::handleLogin);
        }
        if (txtPasswordHidden != null) {
            txtPasswordHidden.setOnAction(this::handleLogin);
        }
        if (txtPasswordVisible != null) {
            txtPasswordVisible.setOnAction(this::handleLogin);
        }
    }

    /**
     * Toggles between hidden (masked) and visible password.
     */
    @FXML
    private void handleTogglePassword(ActionEvent event) {
        isPasswordShown = !isPasswordShown;

        if (isPasswordShown) {
            txtPasswordVisible.setVisible(true);
            txtPasswordHidden.setVisible(false);
            if (svgEyeIcon != null) {
                svgEyeIcon.setContent(SVG_EYE_CLOSED);
            }
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordHidden.setVisible(true);
            if (svgEyeIcon != null) {
                svgEyeIcon.setContent(SVG_EYE_OPEN);
            }
            txtPasswordHidden.requestFocus();
            txtPasswordHidden.positionCaret(txtPasswordHidden.getText().length());
        }
    }

    /**
     * Validates input fields and logs in to the main dashboard strictly using MySQL database.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername != null ? txtUsername.getText().trim() : "";
        String password = txtPasswordHidden != null ? txtPasswordHidden.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("يرجى إدخال اسم المستخدم وكلمة المرور!");
            return;
        }

        // Query database table 'employees' for matching user_name and user_password
        String cleanUser = username.replace("'", "''");
        String cleanPass = password.replace("'", "''");
        String query = "SELECT * FROM employees WHERE user_name = '" + cleanUser + "' AND user_password = '" + cleanPass + "' LIMIT 1;";

        String fullName = null;
        String jobTitle = null;
        String accessRights = null;

        ResultSet rs = DBConnection.executeQuery(query);
        if (rs != null) {
            try {
                if (rs.next()) {
                    fullName = rs.getString("full_name");
                    jobTitle = rs.getString("job_title");
                    accessRights = rs.getString("access_rights");
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Database login error: " + e.getMessage());
            }
        }

        // Check for master admin fallback if database is empty
        if (fullName == null) {
            ResultSet countRs = DBConnection.executeQuery("SELECT COUNT(*) FROM employees;");
            boolean isEmptyDB = false;
            if (countRs != null) {
                try {
                    if (countRs.next() && countRs.getInt(1) == 0) {
                        isEmptyDB = true;
                    }
                    countRs.close();
                } catch (SQLException ignored) {}
            }

            if (isEmptyDB && "admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
                fullName = "المدير العام (افتراضي)";
                jobTitle = "مدير النظام";
                accessRights = "11111111"; // Full access
            } else {
                showErrorMessage("اسم المستخدم أو كلمة المرور غير صحيحة!");
                return;
            }
        }

        // Authentication successful - transition to main window
        showSuccessMessage("تم تسجيل الدخول بنجاح! مرحبًا " + fullName);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Interfaces/main.fxml"));
            Parent root = loader.load();

            // Pass user full name, role, and access rights to main dashboard controller
            main mainController = loader.getController();
            if (mainController != null) {
                mainController.setLoggedInUser(fullName, jobTitle, accessRights);
            }

            Scene scene = new Scene(root, 1180, 750);
            Stage mainStage = new Stage();
            mainStage.setTitle("نظام إدارة المطعم - لوحة التحكم الرئيسية");
            mainStage.setMinWidth(960);
            mainStage.setMinHeight(640);
            mainStage.setScene(scene);
            mainStage.centerOnScreen();
            mainStage.show();

            // Stop the login clock and close current login window
            if (clockTimeline != null) {
                clockTimeline.stop();
            }

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }

        } catch (IOException e) {
            showErrorMessage("حدث خطأ أثناء تحميل لوحة التحكم: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorMessage(String msg) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.getStyleClass().removeAll("login-msg-success");
            if (!lblMessage.getStyleClass().contains("login-msg-error")) {
                lblMessage.getStyleClass().add("login-msg-error");
            }
            lblMessage.setVisible(true);
        }
    }

    private void showSuccessMessage(String msg) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.getStyleClass().removeAll("login-msg-error");
            if (!lblMessage.getStyleClass().contains("login-msg-success")) {
                lblMessage.getStyleClass().add("login-msg-success");
            }
            lblMessage.setVisible(true);
        }
    }
}
