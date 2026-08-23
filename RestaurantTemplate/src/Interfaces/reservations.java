package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Controller for the Reservations & Tables Management Window.
 * Features:
 * - Live Arabic clock & date in the top bar.
 * - Interactive visual table cards for each table showing Table No, Chairs, and Reserved status.
 * - Form to book/edit a table (Table No, Customer Name, Number of Persons, Date, Time, Phone, Notes).
 * - Real-time filtered TableView displaying all active reservations.
 * - Full CRUD & status synchronization between floor plan and reservation lists.
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

    // =========================================================================
    // FXML Table Cards & Badges
    // =========================================================================
    @FXML private FlowPane pnlTableCards;
    @FXML private Label lblAvailableCount;
    @FXML private Label lblReservedCount;
    @FXML private Label lblTotalTables;

    // =========================================================================
    // FXML Form Inputs
    // =========================================================================
    @FXML private JFXComboBox<String> cbTableNo;
    @FXML private JFXTextField txtCustomerName;
    @FXML private JFXTextField txtPersonsCount;
    @FXML private DatePicker dpReservationDate;
    @FXML private JFXComboBox<String> cbReservationTime;
    @FXML private JFXTextField txtCustomerPhone;
    @FXML private JFXTextField txtNotes;

    @FXML private Label lblFormStatus;
    @FXML private Label lblActionMessage;

    // =========================================================================
    // FXML Action Buttons
    // =========================================================================
    @FXML private JFXButton btnBook;
    @FXML private JFXButton btnEdit;
    @FXML private JFXButton btnCancel;
    @FXML private JFXButton btnClear;

    // =========================================================================
    // FXML Search & TableView
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
    private final List<RestaurantTable> tablesList = new ArrayList<>();
    private final ObservableList<ReservationModel> reservationList = FXCollections.observableArrayList();
    private FilteredList<ReservationModel> filteredReservationList;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initFormDropdowns();
        initTableColumns();
        initRestaurantTablesData();
        renderTableCards();
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
     * Initializes dropdown options for table numbers and time slots.
     */
    private void initFormDropdowns() {
        if (cbTableNo != null) {
            ObservableList<String> tableOptions = FXCollections.observableArrayList();
            for (int i = 1; i <= 12; i++) {
                tableOptions.add("طاولة " + i);
            }
            cbTableNo.setItems(tableOptions);
        }

        if (cbReservationTime != null) {
            cbReservationTime.setItems(FXCollections.observableArrayList(
                "01:00 م (غداء)",
                "02:00 م (غداء)",
                "03:30 م (غداء)",
                "05:00 م (شاي ومشروبات)",
                "06:30 م (عشاء مبكر)",
                "07:30 م (عشاء)",
                "08:30 م (عشاء)",
                "09:30 م (عشاء متأخر)",
                "10:30 م (سهرة)"
            ));
        }

        if (dpReservationDate != null) {
            dpReservationDate.setValue(LocalDate.now());
        }
    }

    /**
     * Maps table columns to model properties.
     */
    private void initTableColumns() {
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

        filteredReservationList = new FilteredList<>(reservationList, p -> true);
        if (tblReservations != null) {
            tblReservations.setItems(filteredReservationList);
        }
    }

    /**
     * Initializes the 12 tables with capacities and sample initial bookings.
     */
    private void initRestaurantTablesData() {
        tablesList.clear();
        int[] capacities = {2, 4, 4, 6, 2, 4, 8, 6, 4, 2, 8, 10};

        for (int i = 1; i <= 12; i++) {
            tablesList.add(new RestaurantTable(i, capacities[i - 1]));
        }

        // Add 4 sample bookings
        addMockReservation(1, "طاولة 2", "محمد طارق الأحمدي", "01099887766", 4, 4, LocalDate.now().toString(), "07:30 م (عشاء)", "عشاء عائلي");
        addMockReservation(2, "طاولة 4", "نورهان علاء الدين", "01233445566", 5, 6, LocalDate.now().toString(), "08:30 م (عشاء)", "عيد ميلاد");
        addMockReservation(3, "طاولة 7", "مروان خالد عبد العزيز", "01155667788", 8, 8, LocalDate.now().toString(), "09:30 م (عشاء متأخر)", "اجتماع عمل");
        addMockReservation(4, "طاولة 11", "ياسمين شريف سامي", "01544332211", 6, 8, LocalDate.now().plusDays(1).toString(), "08:00 م (عشاء)", "طاولة بجوار النافذة");

        updateCounts();
    }

    private void addMockReservation(int seq, String tableNo, String custName, String phone, int persons, int chairs, String date, String time, String notes) {
        ReservationModel res = new ReservationModel(seq, tableNo, custName, phone, persons, chairs + " كراسي", date, time, "مؤكد", notes);
        reservationList.add(res);

        int tableIndex = extractTableNumber(tableNo) - 1;
        if (tableIndex >= 0 && tableIndex < tablesList.size()) {
            tablesList.get(tableIndex).setReserved(true);
            tablesList.get(tableIndex).setCurrentReservation(res);
        }
    }

    /**
     * Builds and renders interactive visual table cards in the FlowPane.
     */
    private void renderTableCards() {
        if (pnlTableCards == null) return;
        pnlTableCards.getChildren().clear();

        for (RestaurantTable table : tablesList) {
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
            Label lblChairs = new Label("🪑 سعة " + table.getChairs() + " كراسي");
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

                Label lblResTime = new Label("⏰ " + table.getCurrentReservation().getTime());
                lblResTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #6D4C41;");
                lblResTime.setWrapText(true);
                lblResTime.setMaxWidth(Double.MAX_VALUE);

                infoBox.getChildren().addAll(lblCust, lblResTime);
            } else {
                Label lblAvail = new Label("✔ الطاولة شاغرة ومتاحة للحجز");
                lblAvail.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                lblAvail.setWrapText(true);
                lblAvail.setMaxWidth(Double.MAX_VALUE);
                infoBox.getChildren().add(lblAvail);
            }

            card.getChildren().addAll(header, sub, infoBox);

            // Click Handler on Card
            final int tNo = table.getTableNo();
            card.setOnMouseClicked(e -> {
                handleTableCardClick(tNo);
            });

            pnlTableCards.getChildren().add(card);
        }
    }

    /**
     * Card click handler: selects table in form and highlights data.
     */
    private void handleTableCardClick(int tableNo) {
        if (cbTableNo != null) {
            cbTableNo.setValue("طاولة " + tableNo);
        }

        int index = tableNo - 1;
        if (index >= 0 && index < tablesList.size()) {
            RestaurantTable table = tablesList.get(index);
            if (table.isReserved() && table.getCurrentReservation() != null) {
                populateFormFromModel(table.getCurrentReservation());
                if (tblReservations != null) {
                    tblReservations.getSelectionModel().select(table.getCurrentReservation());
                }
                showNotification("تم تحديد الطاولة (" + tableNo + ") المحجوزة للعميل: " + table.getCurrentReservation().getCustomerName(), false);
            } else {
                handleClearFields(null);
                if (cbTableNo != null) cbTableNo.setValue("طاولة " + tableNo);
                if (txtPersonsCount != null) txtPersonsCount.setText(String.valueOf(table.getChairs()));
                showNotification("تم اختيار طاولة " + tableNo + " (سعة " + table.getChairs() + " كراسي) - يمكنك إدخال بيانات الحجز الآن.", false);
            }
        }
    }

    /**
     * Populates form fields when a reservation row is clicked in the TableView.
     */
    private void setupTableSelection() {
        if (tblReservations != null) {
            tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateFormFromModel(newVal);
                }
            });
        }
    }

    /**
     * Live search filter.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                applySearchQuery(newVal);
            });
        }
    }

    private void applySearchQuery(String query) {
        if (filteredReservationList == null) return;

        filteredReservationList.setPredicate(res -> {
            if (query == null || query.trim().isEmpty()) {
                return true;
            }
            String lower = query.trim().toLowerCase();

            boolean matchCust = res.getCustomerName() != null && res.getCustomerName().toLowerCase().contains(lower);
            boolean matchTable = res.getTableNo() != null && res.getTableNo().toLowerCase().contains(lower);
            boolean matchPhone = res.getCustomerPhone() != null && res.getCustomerPhone().toLowerCase().contains(lower);
            boolean matchNotes = res.getNotes() != null && res.getNotes().toLowerCase().contains(lower);

            return matchCust || matchTable || matchPhone || matchNotes;
        });

        updateCounts();
    }

    // =========================================================================
    // CRUD Action Handlers
    // =========================================================================

    /**
     * 1. Book / Confirm Table Reservation
     */
    @FXML
    private void handleBookTable(ActionEvent event) {
        String tableNo = cbTableNo != null ? cbTableNo.getValue() : null;
        String custName = getSafeText(txtCustomerName);
        String personsStr = getSafeText(txtPersonsCount);
        LocalDate date = dpReservationDate != null ? dpReservationDate.getValue() : null;
        String time = cbReservationTime != null ? cbReservationTime.getValue() : null;
        String phone = getSafeText(txtCustomerPhone);
        String notes = getSafeText(txtNotes);

        if (tableNo == null || custName.isEmpty() || personsStr.isEmpty() || date == null || time == null) {
            showNotification("يرجى ملء كافة الحقول الإجبارية (رقم الطاولة، اسم العميل، عدد الأفراد، تاريخ وموعد الحجز)!", true);
            return;
        }

        int persons = 2;
        try {
            persons = Integer.parseInt(personsStr);
        } catch (NumberFormatException e) {
            showNotification("يرجى إدخال عدد أفراد صحيح!", true);
            return;
        }

        int tIndex = extractTableNumber(tableNo) - 1;
        if (tIndex >= 0 && tIndex < tablesList.size()) {
            RestaurantTable table = tablesList.get(tIndex);
            if (table.isReserved()) {
                showNotification("الطاولة (" + tableNo + ") محجوزة بالفعل حاليًا! يرجى اختيار طاولة أخرى أو تعديل الحجز.", true);
                return;
            }

            int newSeq = reservationList.size() + 1;
            ReservationModel newRes = new ReservationModel(newSeq, tableNo, custName, phone, persons, table.getChairs() + " كراسي", date.toString(), time, "مؤكد", notes);
            reservationList.add(newRes);

            table.setReserved(true);
            table.setCurrentReservation(newRes);

            renderTableCards();
            updateCounts();

            if (tblReservations != null) {
                tblReservations.getSelectionModel().select(newRes);
                tblReservations.scrollTo(newRes);
            }

            showNotification("تم تأكيد حجز " + tableNo + " بنجاح للعميل (" + custName + ")! ✔", false);
        }
    }

    /**
     * 2. Edit Selected Reservation
     */
    @FXML
    private void handleEditReservation(ActionEvent event) {
        ReservationModel selected = tblReservations != null ? tblReservations.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى تحديد حجز من الجدول لتعديله!", true);
            return;
        }

        String tableNo = cbTableNo != null ? cbTableNo.getValue() : null;
        String custName = getSafeText(txtCustomerName);
        String personsStr = getSafeText(txtPersonsCount);
        LocalDate date = dpReservationDate != null ? dpReservationDate.getValue() : null;
        String time = cbReservationTime != null ? cbReservationTime.getValue() : null;
        String phone = getSafeText(txtCustomerPhone);
        String notes = getSafeText(txtNotes);

        if (tableNo == null || custName.isEmpty() || date == null || time == null) {
            showNotification("لا يمكن ترك بيانات الحجز الأساسية فارغة!", true);
            return;
        }

        int persons = selected.getPersonsCount();
        try {
            if (!personsStr.isEmpty()) persons = Integer.parseInt(personsStr);
        } catch (NumberFormatException ignored) {}

        // If table changed, free old table and reserve new
        if (!selected.getTableNo().equals(tableNo)) {
            int oldIndex = extractTableNumber(selected.getTableNo()) - 1;
            if (oldIndex >= 0 && oldIndex < tablesList.size()) {
                tablesList.get(oldIndex).setReserved(false);
                tablesList.get(oldIndex).setCurrentReservation(null);
            }
            int newIndex = extractTableNumber(tableNo) - 1;
            if (newIndex >= 0 && newIndex < tablesList.size()) {
                tablesList.get(newIndex).setReserved(true);
                tablesList.get(newIndex).setCurrentReservation(selected);
                selected.setChairs(tablesList.get(newIndex).getChairs() + " كراسي");
            }
        }

        selected.setTableNo(tableNo);
        selected.setCustomerName(custName);
        selected.setCustomerPhone(phone);
        selected.setPersonsCount(persons);
        selected.setDate(date.toString());
        selected.setTime(time);
        selected.setNotes(notes);

        if (tblReservations != null) {
            tblReservations.refresh();
        }

        renderTableCards();
        updateCounts();

        showNotification("تم تحديث بيانات الحجز بنجاح! ✔", false);
    }

    /**
     * 3. Cancel Reservation / Free Table
     */
    @FXML
    private void handleCancelReservation(ActionEvent event) {
        ReservationModel selected = tblReservations != null ? tblReservations.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى تحديد حجز من الجدول لإلغائه وتحرير الطاولة!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد إلغاء الحجز");
        confirm.setHeaderText("إلغاء حجز " + selected.getTableNo() + " للعميل " + selected.getCustomerName());
        confirm.setContentText("هل أنت متأكد من رغبتك في إلغاء هذا الحجز وتحرير الطاولة لتصبح متاحة؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int tIndex = extractTableNumber(selected.getTableNo()) - 1;
            if (tIndex >= 0 && tIndex < tablesList.size()) {
                tablesList.get(tIndex).setReserved(false);
                tablesList.get(tIndex).setCurrentReservation(null);
            }

            reservationList.remove(selected);
            refreshSequenceNumbers();
            renderTableCards();
            updateCounts();
            handleClearFields(null);

            showNotification("تم إلغاء الحجز وتحرير الطاولة بنجاح! 🗑️", false);
        }
    }

    /**
     * 4. Clear Form Fields
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        if (cbTableNo != null) cbTableNo.setValue(null);
        if (txtCustomerName != null) txtCustomerName.clear();
        if (txtPersonsCount != null) txtPersonsCount.clear();
        if (dpReservationDate != null) dpReservationDate.setValue(LocalDate.now());
        if (cbReservationTime != null) cbReservationTime.setValue(null);
        if (txtCustomerPhone != null) txtCustomerPhone.clear();
        if (txtNotes != null) txtNotes.clear();

        if (tblReservations != null) {
            tblReservations.getSelectionModel().clearSelection();
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال حجز جديد.");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearch != null ? txtSearch.getText() : "";
        applySearchQuery(query);
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        applySearchQuery("");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void populateFormFromModel(ReservationModel model) {
        if (cbTableNo != null) cbTableNo.setValue(model.getTableNo());
        if (txtCustomerName != null) txtCustomerName.setText(model.getCustomerName());
        if (txtPersonsCount != null) txtPersonsCount.setText(String.valueOf(model.getPersonsCount()));
        if (dpReservationDate != null && model.getDate() != null) {
            try {
                dpReservationDate.setValue(LocalDate.parse(model.getDate()));
            } catch (Exception ignored) {}
        }
        if (cbReservationTime != null) cbReservationTime.setValue(model.getTime());
        if (txtCustomerPhone != null) txtCustomerPhone.setText(model.getCustomerPhone());
        if (txtNotes != null) txtNotes.setText(model.getNotes());

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض حجز: " + model.getTableNo() + " - " + model.getCustomerName());
        }
    }

    private void refreshSequenceNumbers() {
        for (int i = 0; i < reservationList.size(); i++) {
            reservationList.get(i).setSeq(i + 1);
        }
    }

    private void updateCounts() {
        int reserved = 0;
        for (RestaurantTable t : tablesList) {
            if (t.isReserved()) reserved++;
        }
        int total = tablesList.size();
        int available = total - reserved;

        if (lblAvailableCount != null) lblAvailableCount.setText(String.valueOf(available));
        if (lblReservedCount != null) lblReservedCount.setText(String.valueOf(reserved));
        if (lblTotalTables != null) lblTotalTables.setText(String.valueOf(total));
        if (lblTableTotalCount != null) {
            int count = filteredReservationList != null ? filteredReservationList.size() : reservationList.size();
            lblTableTotalCount.setText(String.valueOf(count));
        }
    }

    private int extractTableNumber(String tableNoStr) {
        if (tableNoStr == null) return 1;
        String digits = tableNoStr.replaceAll("\\D+", "");
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 1;
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
    // Table & Reservation Model Classes
    // =========================================================================
    public static class RestaurantTable {
        private final int tableNo;
        private final int chairs;
        private boolean isReserved;
        private ReservationModel currentReservation;

        public RestaurantTable(int tableNo, int chairs) {
            this.tableNo = tableNo;
            this.chairs = chairs;
            this.isReserved = false;
        }

        public int getTableNo() { return tableNo; }
        public int getChairs() { return chairs; }
        public boolean isReserved() { return isReserved; }
        public void setReserved(boolean reserved) { isReserved = reserved; }
        public ReservationModel getCurrentReservation() { return currentReservation; }
        public void setCurrentReservation(ReservationModel currentReservation) { this.currentReservation = currentReservation; }
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
}
