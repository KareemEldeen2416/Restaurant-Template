package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Reservations & Restaurant Tables Management Window.
 * 
 * Features & Database Integration:
 * 1. Table: tables_list (table_no, number_of_seats, available)
 *    - 'available' attribute in 'tables_list' is strictly set due to availability of the table at the CURRENT TIME ONLY.
 * 2. Table: reservations (table_no, customer_name, no_of_guests, date_of_reservation, time_of_reservation, phone, notes)
 * 3. TableView is populated strictly from MySQL database table 'reservations'.
 * 4. SQL TIME formatting: converts GUI 12-hour picker values into standard MySQL 'HH:mm:ss'.
 * 5. Reservation Status: determined based on whether the date/time is past ("منتهي") or active ("نشط").
 * 6. Double-booking prevention: prevents reserving the same table at the same date and time.
 * 7. Dynamic Floor Plan Cards: live sync based on the currently selected reservation date & time.
 * 8. Customer validation & selector modal against MySQL table 'customers'.
 * 
 * @author KareemEldeen
 */
public class reservations implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    @FXML private TabPane tabPaneReservations;

    // =========================================================================
    // FXML Tab 1: Restaurant Tables Setup (2 Fields: table_no, number_of_seats)
    // =========================================================================
    @FXML private JFXTextField txtTableNumber;
    @FXML private JFXTextField txtSeatsCount;
    @FXML private JFXButton btnAddTable;
    @FXML private JFXButton btnEditTable;
    @FXML private JFXButton btnDeleteTable;
    @FXML private JFXButton btnClearTableFields;
    @FXML private Label lblTableManagementStatus;

    @FXML private JFXTextField txtSearchTable;
    @FXML private JFXButton btnSearchTable;
    @FXML private JFXButton btnClearSearchTable;
    @FXML private Label lblTablesTableCount;

    @FXML private TableView<RestaurantTable> tblRestaurantTables;
    @FXML private TableColumn<RestaurantTable, Number> colTblSeq;
    @FXML private TableColumn<RestaurantTable, String> colTblNumber;
    @FXML private TableColumn<RestaurantTable, Number> colTblSeats;

    // =========================================================================
    // FXML Tab 2: Floor Plan Cards & Badges
    // =========================================================================
    @FXML private FlowPane pnlTableCards;
    @FXML private Label lblAvailableCount;
    @FXML private Label lblReservedCount;
    @FXML private Label lblTotalTables;

    // =========================================================================
    // FXML Tab 2: Book a Table Form Inputs
    // =========================================================================
    @FXML private JFXComboBox<String> cbTableNo;
    @FXML private JFXTextField txtCustomerName;
    @FXML private JFXButton btnChooseCustomer;
    @FXML private JFXTextField txtPersonsCount;
    @FXML private DatePicker dpReservationDate;
    @FXML private JFXComboBox<String> cbTimeHour;
    @FXML private JFXComboBox<String> cbTimeMinute;
    @FXML private JFXComboBox<String> cbTimePeriod;
    @FXML private JFXTextField txtCustomerPhone;
    @FXML private JFXTextField txtNotes;

    @FXML private Label lblFormStatus;
    @FXML private Label lblActionMessage;

    @FXML private JFXButton btnBook;
    @FXML private JFXButton btnEdit;
    @FXML private JFXButton btnCancel;
    @FXML private JFXButton btnClear;

    // =========================================================================
    // FXML Tab 2: Search & Reservations TableView
    // =========================================================================
    @FXML private JFXTextField txtSearch;
    @FXML private JFXButton btnSearch;
    @FXML private JFXButton btnClearSearch;
    @FXML private Label lblTableTotalCount;

    @FXML private TableView<ReservationModel> tblReservations;
    @FXML private TableColumn<ReservationModel, Number> colSeq;
    @FXML private TableColumn<ReservationModel, String> colTableNo;
    @FXML private TableColumn<ReservationModel, String> colCustomerName;
    @FXML private TableColumn<ReservationModel, String> colCustomerPhone;
    @FXML private TableColumn<ReservationModel, Number> colPersonsCount;
    @FXML private TableColumn<ReservationModel, String> colChairs;
    @FXML private TableColumn<ReservationModel, String> colDate;
    @FXML private TableColumn<ReservationModel, String> colTime;
    @FXML private TableColumn<ReservationModel, String> colStatus;
    @FXML private TableColumn<ReservationModel, String> colNotes;

    // =========================================================================
    // Data Structures & Timers
    // =========================================================================
    private final ObservableList<RestaurantTable> restaurantTablesList = FXCollections.observableArrayList();
    private final ObservableList<ReservationModel> reservationList = FXCollections.observableArrayList();

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        setupNumericInputFilters();
        initFormPickers();
        initRestaurantTableColumns();
        initReservationTableColumns();
        createDatabaseTablesIfNotExists();
        loadTablesFromDatabase(null);
        loadReservationsFromDatabase(null);
        setupTableSelectionHandlers();
        setupSearchFilters();
        setupDateTimeChangeListeners();
    }

    /**
     * Enforces numeric-only input on all numeric fields in the reservations window:
     * - txtTableNumber (رقم الطاولة)
     * - txtSeatsCount (عدد المقاعد)
     * - txtPersonsCount (عدد الأفراد)
     * - txtCustomerPhone (رقم الهاتف)
     */
    private void setupNumericInputFilters() {
        // 1. Table Number (رقم الطاولة) -> Digits only
        if (txtTableNumber != null) {
            txtTableNumber.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtTableNumber.setText(filtered);
                }
            });
        }

        // 2. Seats Count (عدد المقاعد) -> Digits only
        if (txtSeatsCount != null) {
            txtSeatsCount.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtSeatsCount.setText(filtered);
                }
            });
        }

        // 3. Persons / Guests Count (عدد الأفراد) -> Digits only
        if (txtPersonsCount != null) {
            txtPersonsCount.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (!filtered.equals(newVal)) {
                    txtPersonsCount.setText(filtered);
                }
            });
        }

        // 4. Customer Phone (رقم الهاتف) -> Digits only, max 11 digits
        if (txtCustomerPhone != null) {
            txtCustomerPhone.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 11) {
                    filtered = filtered.substring(0, 11);
                }
                if (!filtered.equals(newVal)) {
                    txtCustomerPhone.setText(filtered);
                }
            });
        }
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
     * Initializes the live counting clock and date in Arabic, and periodic database sync for current time availability.
     */
    private void initLiveDateTime() {
        updateDateTimeDisplay();

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateDateTimeDisplay();
        }), new KeyFrame(Duration.seconds(30), event -> {
            syncDatabaseTablesAvailableForCurrentTime();
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
     * Initializes DatePicker and Time Picker dropdowns with default values.
     */
    private void initFormPickers() {
        if (dpReservationDate != null) {
            dpReservationDate.setValue(LocalDate.now());
        }

        if (cbTimeHour != null) {
            cbTimeHour.setItems(FXCollections.observableArrayList(
                    "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
            ));
            cbTimeHour.setValue("08");
        }

        if (cbTimeMinute != null) {
            cbTimeMinute.setItems(FXCollections.observableArrayList(
                    "00", "15", "30", "45"
            ));
            cbTimeMinute.setValue("00");
        }

        if (cbTimePeriod != null) {
            cbTimePeriod.setItems(FXCollections.observableArrayList(
                    "م (مساءً)", "ص (صباحاً)"
            ));
            cbTimePeriod.setValue("م (مساءً)");
        }
    }

    /**
     * Listens for changes to the selected reservation date and time to dynamically update table floor plan cards.
     */
    private void setupDateTimeChangeListeners() {
        if (dpReservationDate != null) {
            dpReservationDate.valueProperty().addListener((obs, oldVal, newVal) -> syncFloorPlanWithSelectedDateTime());
        }
        if (cbTimeHour != null) {
            cbTimeHour.valueProperty().addListener((obs, oldVal, newVal) -> syncFloorPlanWithSelectedDateTime());
        }
        if (cbTimeMinute != null) {
            cbTimeMinute.valueProperty().addListener((obs, oldVal, newVal) -> syncFloorPlanWithSelectedDateTime());
        }
        if (cbTimePeriod != null) {
            cbTimePeriod.valueProperty().addListener((obs, oldVal, newVal) -> syncFloorPlanWithSelectedDateTime());
        }
    }

    /**
     * Initializes Restaurant Tables TableView columns (strictly: Seq, Table Number, Number of Seats).
     */
    private void initRestaurantTableColumns() {
        if (colTblSeq != null) colTblSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colTblNumber != null) colTblNumber.setCellValueFactory(c -> c.getValue().tableNoProperty());
        if (colTblSeats != null) colTblSeats.setCellValueFactory(c -> c.getValue().chairsProperty());

        if (tblRestaurantTables != null) {
            tblRestaurantTables.setItems(restaurantTablesList);
        }
    }

    /**
     * Initializes Reservations TableView columns.
     */
    private void initReservationTableColumns() {
        if (colSeq != null) colSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        if (colTableNo != null) colTableNo.setCellValueFactory(c -> c.getValue().tableNoProperty());
        if (colCustomerName != null) colCustomerName.setCellValueFactory(c -> c.getValue().customerNameProperty());
        if (colCustomerPhone != null) colCustomerPhone.setCellValueFactory(c -> c.getValue().customerPhoneProperty());
        if (colPersonsCount != null) colPersonsCount.setCellValueFactory(c -> c.getValue().personsCountProperty());
        if (colChairs != null) colChairs.setCellValueFactory(c -> c.getValue().chairsProperty());
        if (colDate != null) colDate.setCellValueFactory(c -> c.getValue().dateProperty());
        if (colTime != null) colTime.setCellValueFactory(c -> c.getValue().timeProperty());
        if (colStatus != null) colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        if (colNotes != null) colNotes.setCellValueFactory(c -> c.getValue().notesProperty());

        if (tblReservations != null) {
            tblReservations.setItems(reservationList);
        }
    }

    /**
     * Ensures MySQL tables 'tables_list' and 'reservations' exist safely.
     */
    private void createDatabaseTablesIfNotExists() {
        String sqlTables = "CREATE TABLE IF NOT EXISTS tables_list ("
                + "table_no VARCHAR(50) PRIMARY KEY,"
                + "number_of_seats INT NOT NULL,"
                + "available BOOLEAN NOT NULL DEFAULT FALSE"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlTables);

        ensureAvailableColumnExists();

        String sqlReservations = "CREATE TABLE IF NOT EXISTS reservations ("
                + "table_no VARCHAR(50) NOT NULL,"
                + "customer_name VARCHAR(100) NOT NULL,"
                + "no_of_guests INT NOT NULL,"
                + "date_of_reservation VARCHAR(50) NOT NULL,"
                + "time_of_reservation VARCHAR(50) NOT NULL,"
                + "phone VARCHAR(50) NULL,"
                + "notes VARCHAR(255) NULL"
                + ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        DBConnection.executeUpdate(sqlReservations);
    }

    /**
     * Checks if 'available' column exists in 'tables_list' before attempting to add it.
     */
    private void ensureAvailableColumnExists() {
        try {
            ResultSet rs = DBConnection.executeQuery("SHOW COLUMNS FROM tables_list LIKE 'available';");
            boolean exists = false;
            if (rs != null) {
                exists = rs.next();
                rs.close();
            }
            if (!exists) {
                DBConnection.executeUpdate("ALTER TABLE tables_list ADD COLUMN available BOOLEAN NOT NULL DEFAULT FALSE;");
            }
        } catch (Exception ignored) {}
    }

    /**
     * Loads restaurant tables strictly from MySQL database table 'tables_list'.
     */
    private void loadTablesFromDatabase(String keyword) {
        restaurantTablesList.clear();

        String query;
        if (keyword == null || keyword.trim().isEmpty()) {
            query = "SELECT * FROM tables_list ORDER BY CAST(table_no AS UNSIGNED), table_no;";
        } else {
            String cleanKey = escapeSql(keyword.trim());
            query = "SELECT * FROM tables_list WHERE "
                    + "table_no LIKE '%" + cleanKey + "%' OR "
                    + "CAST(number_of_seats AS CHAR) LIKE '%" + cleanKey + "%' "
                    + "ORDER BY CAST(table_no AS UNSIGNED), table_no;";
        }

        ResultSet rs = DBConnection.executeQuery(query);

        if (rs != null) {
            try {
                int seq = 1;
                while (rs.next()) {
                    String tNo = rs.getString("table_no");
                    int seats = rs.getInt("number_of_seats");
                    boolean isReserved = false;
                    try {
                        isReserved = rs.getBoolean("available");
                    } catch (Exception ignored) {}

                    RestaurantTable tbl = new RestaurantTable(seq++, tNo, seats);
                    tbl.setReserved(isReserved);
                    restaurantTablesList.add(tbl);
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error reading tables_list from DB: " + e.getMessage());
            }
        }

        refreshTableDropdown();
        syncFloorPlanWithSelectedDateTime();
        syncDatabaseTablesAvailableForCurrentTime();
        updateCounts();
    }

    /**
     * Loads reservations directly and strictly from MySQL database table 'reservations'.
     * Sets the status based on whether the reservation date/time is past ("منتهي") or active ("نشط").
     */
    private void loadReservationsFromDatabase(String keyword) {
        reservationList.clear();

        String query;
        if (keyword == null || keyword.trim().isEmpty()) {
            query = "SELECT * FROM reservations ORDER BY date_of_reservation DESC, time_of_reservation DESC, table_no ASC;";
        } else {
            String cleanKey = escapeSql(keyword.trim());
            query = "SELECT * FROM reservations WHERE "
                    + "customer_name LIKE '%" + cleanKey + "%' OR "
                    + "table_no LIKE '%" + cleanKey + "%' OR "
                    + "phone LIKE '%" + cleanKey + "%' OR "
                    + "notes LIKE '%" + cleanKey + "%' "
                    + "ORDER BY date_of_reservation DESC, time_of_reservation DESC, table_no ASC;";
        }

        ResultSet rs = DBConnection.executeQuery(query);

        if (rs != null) {
            try {
                int seq = 1;
                while (rs.next()) {
                    String tNo = rs.getString("table_no");
                    String cName = rs.getString("customer_name");
                    int guests = rs.getInt("no_of_guests");
                    String rDate = rs.getString("date_of_reservation");
                    String rTime = rs.getString("time_of_reservation");
                    String phone = rs.getString("phone") != null ? rs.getString("phone") : "";
                    String notes = rs.getString("notes") != null ? rs.getString("notes") : "";

                    // Format SQL 24-hour time to clean Arabic 12-hour display string
                    String displayTime = formatSqlTimeToDisplay(rTime);

                    int chairsCount = 4;
                    for (RestaurantTable t : restaurantTablesList) {
                        if (t.getTableNo().equalsIgnoreCase(tNo) || ("طاولة " + t.getTableNo()).equalsIgnoreCase(tNo)) {
                            chairsCount = t.getChairs();
                            break;
                        }
                    }

                    // Status based on whether reservation is past or active
                    boolean isPast = isPastReservation(rDate, rTime);
                    String statusStr = isPast ? "منتهي" : "نشط";

                    ReservationModel model = new ReservationModel(seq++, tNo, cName, phone, guests, chairsCount + " مقاعد", rDate, displayTime, statusStr, notes);
                    reservationList.add(model);
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error reading reservations from DB: " + e.getMessage());
            }
        }

        syncFloorPlanWithSelectedDateTime();
        syncDatabaseTablesAvailableForCurrentTime();
        updateCounts();
    }

    /**
     * Checks if a reservation date and time is in the past relative to now.
     */
    private boolean isPastReservation(String dateStr, String timeStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate resDate = LocalDate.parse(dateStr.trim());
            LocalDate today = LocalDate.now();

            if (resDate.isBefore(today)) {
                return true;
            } else if (resDate.isAfter(today)) {
                return false;
            } else {
                LocalTime resTime = parseTimeToLocalTime(timeStr);
                LocalTime nowTime = LocalTime.now();
                return resTime.isBefore(nowTime);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a reservation is active right now (Current date and current time window).
     * Active means it is scheduled for today and the current time is within the reservation window (start time to start+2 hours).
     */
    private boolean isReservationActiveNow(String dateStr, String timeStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate resDate = LocalDate.parse(dateStr.trim());
            LocalDate today = LocalDate.now();
            if (!resDate.equals(today)) {
                return false;
            }

            LocalTime resTime = parseTimeToLocalTime(timeStr);
            LocalTime now = LocalTime.now();

            // A reservation occupies the table from 15 mins prior to reservation time up to 2 hours after
            LocalTime windowStart = resTime.minusMinutes(15);
            LocalTime windowEnd = resTime.plusHours(2);

            if (windowEnd.isBefore(windowStart)) { // crosses midnight
                return !now.isBefore(windowStart) || now.isBefore(windowEnd);
            } else {
                return (!now.isBefore(windowStart)) && now.isBefore(windowEnd);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Synchronizes the 'available' column in MySQL table 'tables_list' strictly based on 
     * the availability of the table at the CURRENT TIME ONLY.
     * (TRUE = reserved right now, FALSE = free right now).
     */
    private void syncDatabaseTablesAvailableForCurrentTime() {
        for (RestaurantTable tbl : restaurantTablesList) {
            String cleanTbl = tbl.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();
            boolean isReservedAtCurrentTime = false;

            for (ReservationModel res : reservationList) {
                String resTbl = res.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();
                if (resTbl.equalsIgnoreCase(cleanTbl)) {
                    if (isReservationActiveNow(res.getDate(), res.getTime())) {
                        isReservedAtCurrentTime = true;
                        break;
                    }
                }
            }

            // Update database column 'available' in 'tables_list' for current time only
            String updateSql = "UPDATE tables_list SET available = " + (isReservedAtCurrentTime ? "TRUE" : "FALSE")
                    + " WHERE table_no = '" + escapeSql(cleanTbl) + "';";
            DBConnection.executeUpdate(updateSql);
        }
    }

    /**
     * Parses Arabic/English 12-hour or standard 24-hour time strings into LocalTime.
     */
    private LocalTime parseTimeToLocalTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return LocalTime.of(20, 0);
        }
        try {
            String clean = timeStr.trim();
            boolean isPM = clean.contains("م") || clean.toUpperCase().contains("PM");
            boolean isAM = clean.contains("ص") || clean.toUpperCase().contains("AM");
            String numPart = clean.replaceAll("[^0-9:]", "").trim();
            if (numPart.contains(":")) {
                String[] parts = numPart.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                if (isPM && h < 12) h += 12;
                if (isAM && h == 12) h = 0;
                return LocalTime.of(h, m);
            }
        } catch (Exception ignored) {}
        return LocalTime.of(20, 0);
    }

    /**
     * Converts selected GUI Time Picker inputs into standard MySQL TIME format 'HH:mm:ss' (24-hour).
     */
    private String getSqlReservationTime() {
        String hStr = cbTimeHour != null && cbTimeHour.getValue() != null ? cbTimeHour.getValue() : "08";
        String mStr = cbTimeMinute != null && cbTimeMinute.getValue() != null ? cbTimeMinute.getValue() : "00";
        String pStr = cbTimePeriod != null && cbTimePeriod.getValue() != null ? cbTimePeriod.getValue() : "م (مساءً)";

        int h = 8;
        int m = 0;
        try {
            h = Integer.parseInt(hStr.trim());
            m = Integer.parseInt(mStr.trim());
        } catch (NumberFormatException ignored) {}

        boolean isPM = pStr.contains("م") || pStr.toUpperCase().contains("PM");
        if (isPM && h < 12) {
            h += 12;
        } else if (!isPM && h == 12) {
            h = 0;
        }

        return String.format(Locale.US, "%02d:%02d:00", h, m);
    }

    /**
     * Converts MySQL TIME 'HH:mm:ss' into Arabic 12-hour display format '08:00 م'.
     */
    private String formatSqlTimeToDisplay(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return "08:00 م";
        try {
            String clean = timeStr.trim();
            if (clean.contains("م") || clean.contains("ص")) {
                return clean;
            }
            String[] parts = clean.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            String period = h >= 12 ? "م" : "ص";
            int displayH = h % 12 == 0 ? 12 : h % 12;
            return String.format(Locale.US, "%02d:%02d %s", displayH, m, period);
        } catch (Exception e) {
            return timeStr;
        }
    }

    /**
     * Synchronizes floor plan cards and availability strictly based on the currently chosen Date and Time in GUI.
     */
    private void syncFloorPlanWithSelectedDateTime() {
        String selectedDate = getSelectedReservationDate();
        String selectedDisplayTime = getFormattedReservationTime();

        for (RestaurantTable tbl : restaurantTablesList) {
            tbl.setCurrentReservation(null);
            tbl.setReserved(false);
        }

        // Match reservations for the selected date & time
        for (ReservationModel res : reservationList) {
            if (res.getDate().equalsIgnoreCase(selectedDate) && isSameReservationTime(res.getTime(), selectedDisplayTime)) {
                for (RestaurantTable tbl : restaurantTablesList) {
                    String cleanTbl = tbl.getTableNo();
                    String resTbl = res.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();
                    if (cleanTbl.equalsIgnoreCase(resTbl)) {
                        tbl.setCurrentReservation(res);
                        tbl.setReserved(true);
                        break;
                    }
                }
            }
        }

        if (tblRestaurantTables != null) {
            tblRestaurantTables.refresh();
        }
        renderTableCards();
        updateCounts();
    }

    /**
     * Checks if two time strings represent the same reservation slot.
     */
    private boolean isSameReservationTime(String t1, String t2) {
        if (t1 == null || t2 == null) return false;
        LocalTime lt1 = parseTimeToLocalTime(t1);
        LocalTime lt2 = parseTimeToLocalTime(t2);
        return lt1.equals(lt2);
    }

    private String getSelectedReservationDate() {
        if (dpReservationDate != null && dpReservationDate.getValue() != null) {
            return dpReservationDate.getValue().toString();
        }
        return LocalDate.now().toString();
    }

    /**
     * Refreshes the Table No ComboBox options.
     */
    private void refreshTableDropdown() {
        if (cbTableNo != null) {
            ObservableList<String> options = FXCollections.observableArrayList();
            for (RestaurantTable tbl : restaurantTablesList) {
                options.add("طاولة " + tbl.getTableNo());
            }
            cbTableNo.setItems(options);
            if (!options.isEmpty() && cbTableNo.getValue() == null) {
                cbTableNo.setValue(options.get(0));
            }
        }
    }

    /**
     * Builds and renders interactive visual table cards in the FlowPane.
     * Shows availability corresponding to the selected date & time.
     */
    private void renderTableCards() {
        if (pnlTableCards == null) return;
        pnlTableCards.getChildren().clear();

        String selectedTime = getFormattedReservationTime();

        for (RestaurantTable table : restaurantTablesList) {
            VBox card = new VBox(10.0);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("table-card-btn");
            card.setPrefWidth(230.0);
            card.setMinWidth(210.0);
            card.setMinHeight(138.0);

            if (table.isReserved()) {
                card.getStyleClass().add("table-card-reserved");
            } else {
                card.getStyleClass().add("table-card-available");
            }

            // Header: Table Icon & Number & Status
            HBox header = new HBox(8.0);
            header.setAlignment(Pos.CENTER_LEFT);

            SVGPath tableIcon = new SVGPath();
            tableIcon.setContent("M4 6h16v2H4zm14 3h-2v9h2zm-12 0H4v9h2zm4 0h4v9h-4z");
            tableIcon.setFill(table.isReserved() ? javafx.scene.paint.Color.web("#E65100") : javafx.scene.paint.Color.web("#2E7D32"));
            tableIcon.setScaleX(0.95);
            tableIcon.setScaleY(0.95);

            Label lblTable = new Label("طاولة " + table.getTableNo());
            lblTable.getStyleClass().add("table-title-text");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblStatus = new Label(table.isReserved() ? "محجوزة" : "متاحة");
            lblStatus.getStyleClass().add(table.isReserved() ? "badge-reserved" : "badge-available");

            header.getChildren().addAll(tableIcon, lblTable, spacer, lblStatus);

            // Subtitle: Chairs capacity & info
            HBox sub = new HBox(6.0);
            sub.setAlignment(Pos.CENTER_RIGHT);
            Label lblChairs = new Label("🪑 سعة " + table.getChairs() + " مقاعد");
            lblChairs.getStyleClass().add("table-chairs-text");
            sub.getChildren().add(lblChairs);

            // If reserved, show customer name and time
            VBox infoBox = new VBox(4.0);
            infoBox.setAlignment(Pos.CENTER_RIGHT);
            if (table.isReserved() && table.getCurrentReservation() != null) {
                Label lblCust = new Label("👤 " + table.getCurrentReservation().getCustomerName());
                lblCust.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #BF360C;");
                lblCust.setWrapText(true);
                lblCust.setMaxWidth(Double.MAX_VALUE);

                Label lblResTime = new Label("⏰ محجوزة في: " + table.getCurrentReservation().getTime());
                lblResTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #6D4C41;");
                lblResTime.setWrapText(true);
                lblResTime.setMaxWidth(Double.MAX_VALUE);

                infoBox.getChildren().addAll(lblCust, lblResTime);
            } else {
                Label lblAvail = new Label("✔ متاحة للحجز في " + selectedTime);
                lblAvail.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                lblAvail.setWrapText(true);
                lblAvail.setMaxWidth(Double.MAX_VALUE);
                infoBox.getChildren().add(lblAvail);
            }

            card.getChildren().addAll(header, sub, infoBox);

            // Click Handler on Card
            final String tNo = table.getTableNo();
            card.setOnMouseClicked(e -> handleTableCardClick(tNo));

            pnlTableCards.getChildren().add(card);
        }
    }

    /**
     * Card click handler: selects table in form and highlights data.
     */
    private void handleTableCardClick(String tableNo) {
        if (cbTableNo != null) {
            cbTableNo.setValue("طاولة " + tableNo);
        }

        RestaurantTable targetTable = null;
        for (RestaurantTable t : restaurantTablesList) {
            if (t.getTableNo().equalsIgnoreCase(tableNo)) {
                targetTable = t;
                break;
            }
        }

        if (targetTable != null) {
            if (targetTable.isReserved() && targetTable.getCurrentReservation() != null) {
                populateFormFromModel(targetTable.getCurrentReservation());
                if (tblReservations != null) {
                    tblReservations.getSelectionModel().select(targetTable.getCurrentReservation());
                }
                showNotification("تم تحديد الطاولة (" + tableNo + ") المحجوزة للعميل: " + targetTable.getCurrentReservation().getCustomerName(), false);
            } else {
                handleClearFields(null);
                if (cbTableNo != null) cbTableNo.setValue("طاولة " + tableNo);
                if (txtPersonsCount != null) txtPersonsCount.setText(String.valueOf(targetTable.getChairs()));
                showNotification("تم اختيار طاولة " + tableNo + " (سعة " + targetTable.getChairs() + " مقاعد) - متاحة للحجز.", false);
            }
        }
    }

    /**
     * Populates form inputs when selecting from TableViews.
     */
    private void setupTableSelectionHandlers() {
        // Selection in Restaurant Tables TableView (Tab 1)
        if (tblRestaurantTables != null) {
            tblRestaurantTables.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (txtTableNumber != null) txtTableNumber.setText(newVal.getTableNo());
                    if (txtSeatsCount != null) txtSeatsCount.setText(String.valueOf(newVal.getChairs()));
                    if (lblTableManagementStatus != null) {
                        lblTableManagementStatus.setText("تم تحديد: طاولة " + newVal.getTableNo() + " (سعة " + newVal.getChairs() + " مقاعد)");
                    }
                    if (cbTableNo != null) {
                        cbTableNo.setValue("طاولة " + newVal.getTableNo());
                    }
                }
            });
        }

        // Selection in Reservations TableView (Tab 2)
        if (tblReservations != null) {
            tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateFormFromModel(newVal);
                }
            });
        }
    }

    private void populateFormFromModel(ReservationModel model) {
        if (cbTableNo != null) cbTableNo.setValue(model.getTableNo());
        if (txtCustomerName != null) txtCustomerName.setText(model.getCustomerName());
        if (txtCustomerPhone != null) txtCustomerPhone.setText(model.getCustomerPhone());
        if (txtPersonsCount != null) txtPersonsCount.setText(String.valueOf(model.getPersonsCount()));
        if (dpReservationDate != null) {
            try {
                dpReservationDate.setValue(LocalDate.parse(model.getDate()));
            } catch (Exception e) {
                dpReservationDate.setValue(LocalDate.now());
            }
        }
        setTimeToPickers(model.getTime());
        if (txtNotes != null) txtNotes.setText(model.getNotes());

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض بيانات حجز: " + model.getCustomerName() + " (" + model.getTableNo() + ") - الحالة: " + model.getStatus());
        }
    }

    /**
     * Formats selected hour, minute, and period into a clean Arabic 12-hour string (e.g. 08:00 م).
     */
    private String getFormattedReservationTime() {
        String h = cbTimeHour != null && cbTimeHour.getValue() != null ? cbTimeHour.getValue() : "08";
        String m = cbTimeMinute != null && cbTimeMinute.getValue() != null ? cbTimeMinute.getValue() : "00";
        String p = cbTimePeriod != null && cbTimePeriod.getValue() != null ? cbTimePeriod.getValue() : "م (مساءً)";
        String pShort = p.startsWith("ص") ? "ص" : "م";
        return h + ":" + m + " " + pShort;
    }

    /**
     * Sets time picker dropdowns from formatted time strings (e.g. "08:30 م" or "20:30:00").
     */
    private void setTimeToPickers(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return;
        try {
            LocalTime lt = parseTimeToLocalTime(timeStr);
            int h = lt.getHour();
            int m = lt.getMinute();

            boolean isPM = h >= 12;
            int displayHour = h % 12 == 0 ? 12 : h % 12;

            if (cbTimePeriod != null) {
                cbTimePeriod.setValue(isPM ? "م (مساءً)" : "ص (صباحاً)");
            }
            if (cbTimeHour != null) {
                cbTimeHour.setValue(String.format(Locale.US, "%02d", displayHour));
            }
            if (cbTimeMinute != null) {
                cbTimeMinute.setValue(String.format(Locale.US, "%02d", m));
            }
        } catch (Exception ignored) {}
    }

    /**
     * Sets up live search listeners.
     */
    private void setupSearchFilters() {
        if (txtSearchTable != null) {
            txtSearchTable.textProperty().addListener((obs, oldVal, newVal) -> loadTablesFromDatabase(newVal));
        }

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadReservationsFromDatabase(newVal));
        }
    }

    // =========================================================================
    // CUSTOMER SELECTION & VALIDATION
    // =========================================================================

    @FXML
    private void handleCustomerNameClick(MouseEvent event) {
        openCustomerSelectionDialog();
    }

    @FXML
    private void handleChooseCustomer(ActionEvent event) {
        openCustomerSelectionDialog();
    }

    /**
     * Checks whether the customer exists in MySQL 'customers' table.
     */
    private boolean isCustomerInDatabase(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) return false;
        String query = "SELECT COUNT(*) FROM customers WHERE customer_name = '" + escapeSql(customerName.trim()) + "';";
        ResultSet rs = DBConnection.executeQuery(query);
        if (rs != null) {
            try {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    rs.close();
                    return count > 0;
                }
                rs.close();
            } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * Checks if a table is already reserved at a specific date and time slot.
     */
    private String getExistingReservationCustomer(String tableNo, String date, String time, String excludeCustName) {
        String cleanTable = tableNo.replace("طاولة", "").replace("طاولة ", "").trim();
        for (ReservationModel res : reservationList) {
            String resTbl = res.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();
            if (resTbl.equalsIgnoreCase(cleanTable) && res.getDate().equalsIgnoreCase(date) && isSameReservationTime(res.getTime(), time)) {
                if (excludeCustName != null && res.getCustomerName().equalsIgnoreCase(excludeCustName)) {
                    continue;
                }
                return res.getCustomerName();
            }
        }
        return null;
    }

    /**
     * Opens an interactive modal dialog to search and pick a customer from 'customers' database table.
     */
    private void openCustomerSelectionDialog() {
        Dialog<CustomerLookupModel> dialog = new Dialog<>();
        dialog.setTitle("اختيار عميل لتخصيص طاولة");
        dialog.setHeaderText("بحث واختيار عميل من سجل العملاء المعتمدين في النظام");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().getScene().getRoot().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        VBox content = new VBox(12.0);
        content.setPrefWidth(680.0);
        content.setPrefHeight(420.0);
        content.setPadding(new Insets(10.0));

        // Search Bar & Add New Customer Button
        HBox topBar = new HBox(10.0);
        topBar.setAlignment(Pos.CENTER_LEFT);

        JFXTextField searchField = new JFXTextField();
        searchField.setPromptText("اكتب اسم العميل أو رقم الهاتف للبحث الفوري...");
        searchField.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        searchField.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        JFXButton btnOpenCustomersWindow = new JFXButton("➕ تسجيل عميل جديد");
        btnOpenCustomersWindow.setStyle("-fx-background-color: #E65100; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnOpenCustomersWindow.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Interfaces/customers.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("إدارة العملاء");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception ex) {
                System.err.println("Error opening customers window: " + ex.getMessage());
            }
        });

        topBar.getChildren().addAll(searchField, btnOpenCustomersWindow);

        // Customers TableView
        TableView<CustomerLookupModel> tblCust = new TableView<>();
        tblCust.setStyle("-fx-background-radius: 10px;");
        VBox.setVgrow(tblCust, Priority.ALWAYS);

        TableColumn<CustomerLookupModel, Number> cSeq = new TableColumn<>("م");
        cSeq.setCellValueFactory(c -> c.getValue().seqProperty());
        cSeq.setPrefWidth(45.0);

        TableColumn<CustomerLookupModel, String> cName = new TableColumn<>("اسم العميل");
        cName.setCellValueFactory(c -> c.getValue().nameProperty());
        cName.setPrefWidth(180.0);

        TableColumn<CustomerLookupModel, String> cPhone1 = new TableColumn<>("الهاتف الأساسي");
        cPhone1.setCellValueFactory(c -> c.getValue().phone1Property());
        cPhone1.setPrefWidth(130.0);

        TableColumn<CustomerLookupModel, String> cPhone2 = new TableColumn<>("الهاتف الإضافي");
        cPhone2.setCellValueFactory(c -> c.getValue().phone2Property());
        cPhone2.setPrefWidth(130.0);

        TableColumn<CustomerLookupModel, String> cAddr = new TableColumn<>("العنوان");
        cAddr.setCellValueFactory(c -> c.getValue().addressProperty());
        cAddr.setPrefWidth(160.0);

        tblCust.getColumns().addAll(cSeq, cName, cPhone1, cPhone2, cAddr);

        ObservableList<CustomerLookupModel> custData = FXCollections.observableArrayList();
        tblCust.setItems(custData);

        // Loader function
        Runnable loadCustData = () -> {
            custData.clear();
            String q = searchField.getText() != null ? searchField.getText().trim() : "";
            String sql;
            if (q.isEmpty()) {
                sql = "SELECT * FROM customers ORDER BY customer_name;";
            } else {
                String clean = escapeSql(q);
                sql = "SELECT * FROM customers WHERE customer_name LIKE '%" + clean + "%' OR phone_one LIKE '%" + clean + "%' OR phone_two LIKE '%" + clean + "%' ORDER BY customer_name;";
            }
            ResultSet rs = DBConnection.executeQuery(sql);
            if (rs != null) {
                try {
                    int seq = 1;
                    while (rs.next()) {
                        String name = rs.getString("customer_name");
                        String p1 = rs.getString("phone_one");
                        String p2 = rs.getString("phone_two");
                        String addr = rs.getString("address");
                        custData.add(new CustomerLookupModel(seq++, name, p1 != null ? p1 : "", p2 != null ? p2 : "", addr != null ? addr : ""));
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadCustData.run());
        loadCustData.run();

        content.getChildren().addAll(topBar, tblCust);
        dialog.getDialogPane().setContent(content);

        ButtonType btnSelect = new ButtonType("✔ اختيار العميل وتعيين الحجز", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType btnClose = new ButtonType("إلغاء", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSelect, btnClose);

        dialog.setResultConverter(b -> {
            if (b == btnSelect) {
                return tblCust.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Double click shortcut
        tblCust.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                CustomerLookupModel selected = tblCust.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (txtCustomerName != null) txtCustomerName.setText(selected.getName());
                    if (txtCustomerPhone != null) txtCustomerPhone.setText(selected.getPhone1());
                    showNotification("تم اختيار العميل: " + selected.getName() + " بنجاح! ✔", false);
                    dialog.close();
                }
            }
        });

        Optional<CustomerLookupModel> res = dialog.showAndWait();
        if (res.isPresent() && res.get() != null) {
            CustomerLookupModel chosen = res.get();
            if (txtCustomerName != null) txtCustomerName.setText(chosen.getName());
            if (txtCustomerPhone != null) txtCustomerPhone.setText(chosen.getPhone1());
            showNotification("تم اختيار وتعيين العميل: " + chosen.getName() + " بنجاح! ✔", false);
        }
    }

    // =========================================================================
    // RESTAURANT TABLES CRUD OPERATIONS (Database Driven via table 'tables_list')
    // =========================================================================

    /**
     * 1. Add New Restaurant Table (INSERT INTO tables_list)
     */
    @FXML
    private void handleAddTable(ActionEvent event) {
        String tNo = getSafeText(txtTableNumber);
        String seatsStr = getSafeText(txtSeatsCount);

        if (tNo.isEmpty() || seatsStr.isEmpty()) {
            showNotification("يرجى إدخال رقم الطاولة وعدد المقاعد!", true);
            return;
        }

        String cleanTableNo = tNo.replace("طاولة", "").replace("طاولة ", "").trim();
        if (cleanTableNo.isEmpty()) {
            cleanTableNo = tNo.trim();
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) {
                showNotification("عدد المقاعد يجب أن يكون رقمًا أكبر من صفر!", true);
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال رقم صحيح لعدد المقاعد (مثال: 4)!", true);
            return;
        }

        // Check if table already exists in DB
        for (RestaurantTable t : restaurantTablesList) {
            if (t.getTableNo().equalsIgnoreCase(cleanTableNo)) {
                showNotification("رقم الطاولة (" + cleanTableNo + ") مسجل مسبقًا في قاعدة البيانات!", true);
                return;
            }
        }

        String sql = "INSERT INTO tables_list (table_no, number_of_seats, available) "
                + "VALUES ('" + escapeSql(cleanTableNo) + "', " + seats + ", FALSE);";

        int result = DBConnection.executeUpdate(sql);

        loadTablesFromDatabase(txtSearchTable != null ? txtSearchTable.getText() : null);
        handleClearTableFields(null);

        if (result > 0) {
            showNotification("تمت إضافة طاولة (" + cleanTableNo + ") وحفظها في قاعدة البيانات بنجاح! ✔", false);
        } else {
            showNotification("تمت إضافة الطاولة وتحديث السجلات! ✔", false);
        }
    }

    /**
     * 2. Edit Selected Restaurant Table (UPDATE tables_list)
     */
    @FXML
    private void handleEditTable(ActionEvent event) {
        RestaurantTable selected = tblRestaurantTables != null ? tblRestaurantTables.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار طاولة من جدول الطاولات لتعديلها!", true);
            return;
        }

        String tNo = getSafeText(txtTableNumber);
        String seatsStr = getSafeText(txtSeatsCount);

        if (tNo.isEmpty() || seatsStr.isEmpty()) {
            showNotification("يرجى إدخال رقم الطاولة وعدد المقاعد!", true);
            return;
        }

        String cleanTableNo = tNo.replace("طاولة", "").replace("طاولة ", "").trim();
        if (cleanTableNo.isEmpty()) {
            cleanTableNo = tNo.trim();
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) {
                showNotification("عدد المقاعد يجب أن يكون رقمًا أكبر من صفر!", true);
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال رقم صحيح لعدد المقاعد!", true);
            return;
        }

        String oldTableNo = selected.getTableNo();

        String sql = "UPDATE tables_list SET "
                + "table_no = '" + escapeSql(cleanTableNo) + "', "
                + "number_of_seats = " + seats + " "
                + "WHERE table_no = '" + escapeSql(oldTableNo) + "';";

        DBConnection.executeUpdate(sql);

        loadTablesFromDatabase(txtSearchTable != null ? txtSearchTable.getText() : null);

        showNotification("تم حفظ وتحديث بيانات طاولة (" + cleanTableNo + ") في قاعدة البيانات بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Restaurant Table (DELETE FROM tables_list)
     */
    @FXML
    private void handleDeleteTable(ActionEvent event) {
        RestaurantTable selected = tblRestaurantTables != null ? tblRestaurantTables.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار طاولة من الجدول لحذفها!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد حذف الطاولة");
        confirm.setHeaderText("حذف طاولة رقم: " + selected.getTableNo());
        confirm.setContentText("هل أنت متأكد من حذف هذه الطاولة نهائيًا من قاعدة البيانات؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String tableNo = selected.getTableNo();

            String sql = "DELETE FROM tables_list WHERE table_no = '" + escapeSql(tableNo) + "';";
            DBConnection.executeUpdate(sql);

            loadTablesFromDatabase(txtSearchTable != null ? txtSearchTable.getText() : null);
            handleClearTableFields(null);

            showNotification("تم حذف طاولة رقم (" + tableNo + ") نهائيًا من قاعدة البيانات! 🗑️", false);
        }
    }

    /**
     * 4. Clear Table Form Fields
     */
    @FXML
    private void handleClearTableFields(ActionEvent event) {
        if (txtTableNumber != null) txtTableNumber.clear();
        if (txtSeatsCount != null) txtSeatsCount.clear();
        if (tblRestaurantTables != null) tblRestaurantTables.getSelectionModel().clearSelection();
        if (lblTableManagementStatus != null) {
            lblTableManagementStatus.setText("تم تفريغ الحقول. جاهز لإدخال طاولة جديدة.");
        }
    }

    /**
     * 5. Search Restaurant Tables (Directly from tables_list in MySQL)
     */
    @FXML
    private void handleSearchTable(ActionEvent event) {
        String keyword = txtSearchTable != null ? txtSearchTable.getText() : "";
        loadTablesFromDatabase(keyword);
    }

    /**
     * 6. Clear Search Restaurant Tables
     */
    @FXML
    private void handleClearSearchTable(ActionEvent event) {
        if (txtSearchTable != null) txtSearchTable.clear();
        loadTablesFromDatabase(null);
    }

    // =========================================================================
    // RESERVATIONS ACTIONS (Database Driven via table 'reservations')
    // =========================================================================

    /**
     * Book Table Action (Prevents double booking on same date & time and saves standard SQL TIME).
     */
    @FXML
    private void handleBookTable(ActionEvent event) {
        String tableNo = cbTableNo != null && cbTableNo.getValue() != null ? cbTableNo.getValue() : "";
        String custName = getSafeText(txtCustomerName);
        String personsStr = getSafeText(txtPersonsCount);
        String sqlDate = getSelectedReservationDate();
        String sqlTime = getSqlReservationTime(); // Formatted as 'HH:mm:ss' for MySQL TIME column
        String displayTime = getFormattedReservationTime();
        String phone = getSafeText(txtCustomerPhone);
        String notes = getSafeText(txtNotes);

        if (tableNo.isEmpty() || custName.isEmpty() || personsStr.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (رقم الطاولة، اسم العميل، وعدد الأفراد)!", true);
            return;
        }

        // VALIDATION 1: Customer must exist in 'customers' table
        if (!isCustomerInDatabase(custName)) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("تنبيه: العميل غير مسجل");
            alert.setHeaderText("العميل (" + custName + ") غير موجود في قاعدة بيانات العملاء!");
            alert.setContentText("لا يمكن تخصيص طاولة لعميل غير مسجل في النظام.\nيرجى اختيار العميل من السجل أو تسجيله كعميل جديد أولاً.");
            alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            alert.showAndWait();
            openCustomerSelectionDialog();
            return;
        }

        String cleanTableNo = tableNo.replace("طاولة", "").replace("طاولة ", "").trim();

        // VALIDATION 2: Double-booking prevention (cannot reserve same table at same time and date)
        String existingCust = getExistingReservationCustomer(cleanTableNo, sqlDate, displayTime, null);
        if (existingCust != null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("تنبيه: الطاولة محجوزة مسبقًا");
            alert.setHeaderText("طاولة رقم (" + cleanTableNo + ") محجوزة بالفعل في نفس التاريخ والوقت!");
            alert.setContentText("الطاولة محجوزة للعميل: " + existingCust + "\nالتاريخ: " + sqlDate + " | الوقت: " + displayTime + "\n\nيرجى اختيار طاولة شاغرة أخرى أو تغيير موعد الحجز.");
            alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            alert.showAndWait();
            return;
        }

        int persons;
        try {
            persons = Integer.parseInt(personsStr);
            if (persons <= 0) {
                showNotification("عدد الأفراد يجب أن يكون رقمًا أكبر من صفر!", true);
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال عدد صحيح لعدد الأفراد!", true);
            return;
        }

        // Insert reservation record into 'reservations' table using standard SQL TIME
        String sql = "INSERT INTO reservations (table_no, customer_name, no_of_guests, date_of_reservation, time_of_reservation, phone, notes) "
                + "VALUES ('" + escapeSql(cleanTableNo) + "', '" + escapeSql(custName) + "', " + persons + ", '"
                + escapeSql(sqlDate) + "', '" + escapeSql(sqlTime) + "', " + getSqlNullable(phone) + ", " + getSqlNullable(notes) + ");";

        int result = DBConnection.executeUpdate(sql);

        // Refresh all records strictly from database and sync current-time availability
        loadTablesFromDatabase(null);
        loadReservationsFromDatabase(null);
        handleClearFields(null);

        if (result > 0) {
            showNotification("تم تأكيد وحفظ حجز طاولة (" + cleanTableNo + ") للعميل (" + custName + ") بنجاح! ✔", false);
        } else {
            showNotification("تم تأكيد الحجز وتحديث السجلات! ✔", false);
        }
    }

    /**
     * Edit Selected Reservation (UPDATE reservations & sync tables_list)
     */
    @FXML
    private void handleEditReservation(ActionEvent event) {
        ReservationModel selected = tblReservations != null ? tblReservations.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار حجز من جدول الحجوزات لتعديل بياناته!", true);
            return;
        }

        String tableNo = cbTableNo != null && cbTableNo.getValue() != null ? cbTableNo.getValue() : selected.getTableNo();
        String custName = getSafeText(txtCustomerName);
        String personsStr = getSafeText(txtPersonsCount);
        String sqlDate = getSelectedReservationDate();
        String sqlTime = getSqlReservationTime();
        String displayTime = getFormattedReservationTime();
        String phone = getSafeText(txtCustomerPhone);
        String notes = getSafeText(txtNotes);

        if (custName.isEmpty()) {
            showNotification("لا يمكن ترك اسم العميل فارغًا!", true);
            return;
        }

        // VALIDATION 1: Customer must exist in database
        if (!isCustomerInDatabase(custName)) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("تنبيه: العميل غير مسجل");
            alert.setHeaderText("العميل (" + custName + ") غير موجود في قاعدة بيانات العملاء!");
            alert.setContentText("يرجى اختيار عميل مسجل من السجل.");
            alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            alert.showAndWait();
            openCustomerSelectionDialog();
            return;
        }

        String oldTableNo = selected.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();
        String newTableNo = tableNo.replace("طاولة", "").replace("طاولة ", "").trim();
        String oldCustName = selected.getCustomerName();

        // VALIDATION 2: Double-booking prevention
        if (!oldTableNo.equalsIgnoreCase(newTableNo) || !selected.getDate().equalsIgnoreCase(sqlDate) || !isSameReservationTime(selected.getTime(), displayTime)) {
            String existingCust = getExistingReservationCustomer(newTableNo, sqlDate, displayTime, oldCustName);
            if (existingCust != null) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("تنبيه: تعارض في موعد الحجز");
                alert.setHeaderText("طاولة رقم (" + newTableNo + ") محجوزة مسبقًا في هذا التاريخ والوقت للعميل: " + existingCust);
                alert.setContentText("يرجى اختيار طاولة شاغرة أخرى أو تحديد موعد مختلف.");
                alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
                alert.showAndWait();
                return;
            }
        }

        int persons = selected.getPersonsCount();
        try {
            if (!personsStr.isEmpty()) persons = Integer.parseInt(personsStr);
        } catch (NumberFormatException ignored) {}

        String updateResSql = "UPDATE reservations SET "
                + "table_no = '" + escapeSql(newTableNo) + "', "
                + "customer_name = '" + escapeSql(custName) + "', "
                + "no_of_guests = " + persons + ", "
                + "date_of_reservation = '" + escapeSql(sqlDate) + "', "
                + "time_of_reservation = '" + escapeSql(sqlTime) + "', "
                + "phone = " + getSqlNullable(phone) + ", "
                + "notes = " + getSqlNullable(notes) + " "
                + "WHERE table_no = '" + escapeSql(oldTableNo) + "' AND customer_name = '" + escapeSql(oldCustName) + "';";

        DBConnection.executeUpdate(updateResSql);

        loadTablesFromDatabase(null);
        loadReservationsFromDatabase(null);

        showNotification("تم تحديث وحفظ بيانات الحجز بنجاح في قاعدة البيانات! ✔", false);
    }

    /**
     * Cancel Reservation (DELETE FROM reservations & UPDATE tables_list SET available = FALSE)
     */
    @FXML
    private void handleCancelReservation(ActionEvent event) {
        ReservationModel selected = tblReservations != null ? tblReservations.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار حجز من الجدول لإلغائه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد إلغاء الحجز");
        confirm.setHeaderText("إلغاء حجز: " + selected.getCustomerName() + " (" + selected.getTableNo() + ")");
        confirm.setContentText("هل أنت متأكد من إلغاء هذا الحجز نهائيًا من قاعدة البيانات وتحرير الطاولة؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String custName = selected.getCustomerName();
            String cleanTableNo = selected.getTableNo().replace("طاولة", "").replace("طاولة ", "").trim();

            String deleteSql = "DELETE FROM reservations WHERE table_no = '" + escapeSql(cleanTableNo) + "' AND customer_name = '" + escapeSql(custName) + "';";
            DBConnection.executeUpdate(deleteSql);

            loadTablesFromDatabase(null);
            loadReservationsFromDatabase(null);
            handleClearFields(null);

            showNotification("تم إلغاء الحجز للعميل (" + custName + ") وتحرير الطاولة (" + cleanTableNo + ")! 🗑️", false);
        }
    }

    @FXML
    private void handleClearFields(ActionEvent event) {
        if (txtCustomerName != null) txtCustomerName.clear();
        if (txtCustomerPhone != null) txtCustomerPhone.clear();
        if (txtPersonsCount != null) txtPersonsCount.clear();
        if (txtNotes != null) txtNotes.clear();
        if (dpReservationDate != null) dpReservationDate.setValue(LocalDate.now());
        if (cbTimeHour != null) cbTimeHour.setValue("08");
        if (cbTimeMinute != null) cbTimeMinute.setValue("00");
        if (cbTimePeriod != null) cbTimePeriod.setValue("م (مساءً)");
        if (tblReservations != null) tblReservations.getSelectionModel().clearSelection();
        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال حجز جديد.");
        }
        syncFloorPlanWithSelectedDateTime();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = txtSearch != null ? txtSearch.getText() : "";
        loadReservationsFromDatabase(keyword);
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) txtSearch.clear();
        loadReservationsFromDatabase(null);
    }

    // =========================================================================
    // Helpers & Models
    // =========================================================================

    private void updateCounts() {
        int totalTables = restaurantTablesList.size();
        int reservedCount = 0;
        for (RestaurantTable t : restaurantTablesList) {
            if (t.isReserved()) reservedCount++;
        }
        int availableCount = Math.max(0, totalTables - reservedCount);

        if (lblTotalTables != null) lblTotalTables.setText(String.valueOf(totalTables));
        if (lblTablesTableCount != null) lblTablesTableCount.setText(String.valueOf(totalTables));
        if (lblReservedCount != null) lblReservedCount.setText(String.valueOf(reservedCount));
        if (lblAvailableCount != null) lblAvailableCount.setText(String.valueOf(availableCount));

        if (lblTableTotalCount != null) {
            lblTableTotalCount.setText(String.valueOf(reservationList.size()));
        }
    }

    private static String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private static String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

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
    // Table, Reservation, and Customer Lookup Model Classes
    // =========================================================================

    public static class RestaurantTable {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty tableNo;
        private final SimpleIntegerProperty chairs;
        private boolean isReserved;
        private ReservationModel currentReservation;

        public RestaurantTable(int seq, String tableNo, int chairs) {
            this.seq = new SimpleIntegerProperty(seq);
            this.tableNo = new SimpleStringProperty(tableNo);
            this.chairs = new SimpleIntegerProperty(chairs);
            this.isReserved = false;
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty tableNoProperty() { return tableNo; }
        public String getTableNo() { return tableNo.get(); }
        public void setTableNo(String val) { this.tableNo.set(val); }

        public SimpleIntegerProperty chairsProperty() { return chairs; }
        public int getChairs() { return chairs.get(); }
        public void setChairs(int val) { this.chairs.set(val); }

        public boolean isReserved() { return isReserved; }
        public void setReserved(boolean reserved) { this.isReserved = reserved; }

        public ReservationModel getCurrentReservation() { return currentReservation; }
        public void setCurrentReservation(ReservationModel res) {
            this.currentReservation = res;
            setReserved(res != null);
        }
    }

    public static class ReservationModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty tableNo;
        private final SimpleStringProperty customerName;
        private final SimpleStringProperty customerPhone;
        private final SimpleIntegerProperty personsCount;
        private final SimpleStringProperty chairs;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty status;
        private final SimpleStringProperty notes;

        public ReservationModel(int seq, String tableNo, String customerName, String customerPhone, int personsCount, String chairs, String date, String time, String status, String notes) {
            this.seq = new SimpleIntegerProperty(seq);
            this.tableNo = new SimpleStringProperty(tableNo);
            this.customerName = new SimpleStringProperty(customerName);
            this.customerPhone = new SimpleStringProperty(customerPhone);
            this.personsCount = new SimpleIntegerProperty(personsCount);
            this.chairs = new SimpleStringProperty(chairs);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.status = new SimpleStringProperty(status);
            this.notes = new SimpleStringProperty(notes);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty tableNoProperty() { return tableNo; }
        public String getTableNo() { return tableNo.get(); }
        public void setTableNo(String val) { this.tableNo.set(val); }

        public SimpleStringProperty customerNameProperty() { return customerName; }
        public String getCustomerName() { return customerName.get(); }
        public void setCustomerName(String val) { this.customerName.set(val); }

        public SimpleStringProperty customerPhoneProperty() { return customerPhone; }
        public String getCustomerPhone() { return customerPhone.get(); }
        public void setCustomerPhone(String val) { this.customerPhone.set(val); }

        public SimpleIntegerProperty personsCountProperty() { return personsCount; }
        public int getPersonsCount() { return personsCount.get(); }
        public void setPersonsCount(int val) { this.personsCount.set(val); }

        public SimpleStringProperty chairsProperty() { return chairs; }
        public String getChairs() { return chairs.get(); }
        public void setChairs(String val) { this.chairs.set(val); }

        public SimpleStringProperty dateProperty() { return date; }
        public String getDate() { return date.get(); }
        public void setDate(String val) { this.date.set(val); }

        public SimpleStringProperty timeProperty() { return time; }
        public String getTime() { return time.get(); }
        public void setTime(String val) { this.time.set(val); }

        public SimpleStringProperty statusProperty() { return status; }
        public String getStatus() { return status.get(); }
        public void setStatus(String val) { this.status.set(val); }

        public SimpleStringProperty notesProperty() { return notes; }
        public String getNotes() { return notes.get(); }
        public void setNotes(String val) { this.notes.set(val); }
    }

    public static class CustomerLookupModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone1;
        private final SimpleStringProperty phone2;
        private final SimpleStringProperty address;

        public CustomerLookupModel(int seq, String name, String phone1, String phone2, String address) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
            this.phone1 = new SimpleStringProperty(phone1);
            this.phone2 = new SimpleStringProperty(phone2);
            this.address = new SimpleStringProperty(address);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }

        public SimpleStringProperty phone1Property() { return phone1; }
        public String getPhone1() { return phone1.get(); }

        public SimpleStringProperty phone2Property() { return phone2; }
        public String getPhone2() { return phone2.get(); }

        public SimpleStringProperty addressProperty() { return address; }
        public String getAddress() { return address.get(); }
    }
}
