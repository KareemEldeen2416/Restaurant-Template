package restauranttemplate;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Launcher for Restaurant Management System
 * Starts with the Login window and transitions to Main Dashboard.
 * 
 * @author KareemEldeen
 */
public class RestaurantTemplate extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Interfaces/reservations.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 700);

            primaryStage.setTitle("نظام إدارة المطعم - تسجيل الدخول");
            primaryStage.setMinWidth(850);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException ex) {
            System.err.println("Error loading login.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
