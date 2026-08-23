package Interfaces;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller for the Employees Management Window.
 * Features:
 * - Live Arabic clock & date in the top bar.
 * - 13 Form Fields for employee data.
 * - 8 Checkboxes for access rights permissions.
 * - Search by Name, Username, and National ID.
 * - CRUD operations (Add, Edit, Delete, Clear).
 * - Filtered TableView showing employee data except sensitive/security fields.
 * 
 * @author KareemEldeen
 */
public class employees implements Initializable {

    // =========================================================================
    // FXML Header Controls
    // =========================================================================
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    // =========================================================================
    // FXML Form Inputs (13 Fields)
    // =========================================================================
    @FXML private JFXTextField txtNationalId;
    @FXML private JFXTextField txtName;
    @FXML private JFXComboBox<String> cbJobTitle;
    @FXML private JFXComboBox<String> cbDepartment;
    @FXML private JFXTextField txtPhone1;
    @FXML private JFXTextField txtPhone2;
    @FXML private JFXTextField txtAddress;
    @FXML private DatePicker dpBirthDate;
    @FXML private JFXTextField txtUsername;
    @FXML private JFXTextField txtPassword;
    @FXML private JFXTextField txtSalary;
    @FXML private DatePicker dpSalaryDate;
    @FXML private JFXTextField txtBankAccount;

    // =========================================================================
    // FXML Access Rights (8 JFoenix Checkboxes + Select All)
    // =========================================================================
    @FXML private JFXCheckBox chkSelectAll;
    @FXML private JFXCheckBox chkCashier;
    @FXML private JFXCheckBox chkSettings;
    @FXML private JFXCheckBox chkReports;
    @FXML private JFXCheckBox chkSales;
    @FXML private JFXCheckBox chkEmployees;
    @FXML private JFXCheckBox chkCustomers;
    @FXML private JFXCheckBox chkReservations;
    @FXML private JFXCheckBox chkInventory;

    // =========================================================================
    // FXML Action Buttons & Status (JFoenix Buttons)
    // =========================================================================
    @FXML private JFXButton btnAdd;
    @FXML private JFXButton btnEdit;
    @FXML private JFXButton btnDelete;
    @FXML private JFXButton btnClear;
    @FXML private Label lblFormStatus;
    @FXML private Label lblActionMessage;

    // =========================================================================
    // FXML Search & TableView (JFoenix Controls)
    // =========================================================================
    @FXML private JFXTextField txtSearch;
    @FXML private JFXButton btnSearch;
    @FXML private JFXButton btnClearSearch;
    @FXML private Label lblTotalCount;

    @FXML private TableView<EmployeeModel> tblEmployees;
    @FXML private TableColumn<EmployeeModel, Number> colSeq;
    @FXML private TableColumn<EmployeeModel, String> colNationalId;
    @FXML private TableColumn<EmployeeModel, String> colName;
    @FXML private TableColumn<EmployeeModel, String> colJobTitle;
    @FXML private TableColumn<EmployeeModel, String> colDepartment;
    @FXML private TableColumn<EmployeeModel, String> colPhone1;
    @FXML private TableColumn<EmployeeModel, String> colPhone2;
    @FXML private TableColumn<EmployeeModel, String> colAddress;
    @FXML private TableColumn<EmployeeModel, String> colBirthDate;
    @FXML private TableColumn<EmployeeModel, String> colSalary;
    @FXML private TableColumn<EmployeeModel, String> colSalaryDate;

    // =========================================================================
    // Data Structures & Timers
    // =========================================================================
    private final ObservableList<EmployeeModel> employeeList = FXCollections.observableArrayList();
    private FilteredList<EmployeeModel> filteredEmployeeList;

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initComboBoxes();
        initTableColumns();
        loadInitialMockData();
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
     * Fills Department and Job Title Combo Boxes with common restaurant values.
     */
    private void initComboBoxes() {
        if (cbDepartment != null) {
            cbDepartment.setItems(FXCollections.observableArrayList(
                "الإدارة العامة",
                "الصالة والخدمة",
                "المطبخ والتحضير",
                "الكاشير والحسابات",
                "المخازن والمشتريات",
                "الدليفري والتوصيل",
                "الصيانة والنظافة"
            ));
        }

        if (cbJobTitle != null) {
            cbJobTitle.setItems(FXCollections.observableArrayList(
                "مدير الفرع",
                "شيف عمومي (Executive Chef)",
                "مساعد شيف",
                "كابتن صالة (Head Waiter)",
                "مقدم طعام (ويتر)",
                "كاشير رئيسي",
                "كاشير",
                "باريستا",
                "محاسب مالي",
                "أمين مخزن",
                "مندوب توصيل"
            ));
        }
    }

    /**
     * Maps table columns to EmployeeModel properties.
     */
    private void initTableColumns() {
        if (colSeq != null) colSeq.setCellValueFactory(cellData -> cellData.getValue().seqProperty());
        if (colNationalId != null) colNationalId.setCellValueFactory(cellData -> cellData.getValue().nationalIdProperty());
        if (colName != null) colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        if (colJobTitle != null) colJobTitle.setCellValueFactory(cellData -> cellData.getValue().jobTitleProperty());
        if (colDepartment != null) colDepartment.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        if (colPhone1 != null) colPhone1.setCellValueFactory(cellData -> cellData.getValue().phone1Property());
        if (colPhone2 != null) colPhone2.setCellValueFactory(cellData -> cellData.getValue().phone2Property());
        if (colAddress != null) colAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        if (colBirthDate != null) colBirthDate.setCellValueFactory(cellData -> cellData.getValue().birthDateProperty());
        if (colSalary != null) colSalary.setCellValueFactory(cellData -> cellData.getValue().salaryProperty());
        if (colSalaryDate != null) colSalaryDate.setCellValueFactory(cellData -> cellData.getValue().salaryDateProperty());

        filteredEmployeeList = new FilteredList<>(employeeList, p -> true);
        if (tblEmployees != null) {
            tblEmployees.setItems(filteredEmployeeList);
        }
        updateTotalCount();
    }

    /**
     * Loads sample employee records into the list.
     */
    private void loadInitialMockData() {
        employeeList.add(new EmployeeModel(
            1, "29801011234567", "أحمد محمود العطار", "مدير الفرع", "الإدارة العامة",
            "01012345678", "01198765432", "القاهرة - مدينة نصر", "1990-05-15",
            "ahmed.manager", "admin@2026", "12500", "2026-09-01", "EG500025001122334455",
            true, true, true, true, true, true, true, true
        ));

        employeeList.add(new EmployeeModel(
            2, "29505122345678", "محمود عبد الرحمن", "كاشير رئيسي", "الكاشير والحسابات",
            "01223456789", "01055566677", "الجيزة - الدقي", "1995-11-20",
            "mahmoud.pos", "pos@123", "7200", "2026-09-01", "EG500025009988776655",
            true, false, true, true, false, true, false, false
        ));

        employeeList.add(new EmployeeModel(
            3, "29304153456789", "حسام الدين فهمي", "شيف عمومي (Executive Chef)", "المطبخ والتحضير",
            "01144455566", "", "الإسكندرية - سموحة", "1993-04-10",
            "chef.hossam", "chef@2026", "9800", "2026-09-01", "EG500025004433221100",
            false, false, false, false, false, false, false, true
        ));

        employeeList.add(new EmployeeModel(
            4, "29908184567890", "سارة إبراهيم خليل", "كابتن صالة (Head Waiter)", "الصالة والخدمة",
            "01555566677", "01233344455", "القاهرة - المعادي", "1999-08-18",
            "sara.service", "service@123", "6500", "2026-09-01", "EG500025007766554433",
            false, false, false, false, false, true, true, false
        ));

        refreshSequenceNumbers();
        updateTotalCount();
    }

    /**
     * When selecting a row in the TableView, populates all 13 fields and checkboxes.
     */
    private void setupTableSelection() {
        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateFieldsFromModel(newVal);
                }
            });
        }
    }

    /**
     * Sets up live real-time search filtering.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                applySearchQuery(newValue);
            });
        }
    }

    private void applySearchQuery(String query) {
        if (filteredEmployeeList == null) return;

        filteredEmployeeList.setPredicate(emp -> {
            if (query == null || query.trim().isEmpty()) {
                return true;
            }
            String lowerCaseFilter = query.trim().toLowerCase();

            // Search by Name, Username, and National ID
            boolean matchName = emp.getName() != null && emp.getName().toLowerCase().contains(lowerCaseFilter);
            boolean matchUsername = emp.getUsername() != null && emp.getUsername().toLowerCase().contains(lowerCaseFilter);
            boolean matchNationalId = emp.getNationalId() != null && emp.getNationalId().toLowerCase().contains(lowerCaseFilter);

            return matchName || matchUsername || matchNationalId;
        });

        updateTotalCount();
    }

    // =========================================================================
    // CRUD Action Handlers
    // =========================================================================

    /**
     * 1. Add New Employee
     */
    @FXML
    private void handleAddEmployee(ActionEvent event) {
        String nationalId = getSafeText(txtNationalId);
        String name = getSafeText(txtName);
        String jobTitle = cbJobTitle != null && cbJobTitle.getValue() != null ? cbJobTitle.getValue().trim() : "";
        String department = cbDepartment != null && cbDepartment.getValue() != null ? cbDepartment.getValue().trim() : "";
        String phone1 = getSafeText(txtPhone1);

        if (nationalId.isEmpty() || name.isEmpty() || jobTitle.isEmpty() || department.isEmpty() || phone1.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (الرقم القومي، الاسم، المسمى الوظيفي، القسم، ورقم الهاتف)!", true);
            return;
        }

        // Check for duplicate national ID
        for (EmployeeModel emp : employeeList) {
            if (emp.getNationalId().equals(nationalId)) {
                showNotification("الرقم القومي (" + nationalId + ") مسجل مسبقًا لموظف آخر!", true);
                return;
            }
        }

        int newSeq = employeeList.size() + 1;
        EmployeeModel newEmployee = createModelFromForm(newSeq);
        employeeList.add(newEmployee);
        refreshSequenceNumbers();
        updateTotalCount();

        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().select(newEmployee);
            tblEmployees.scrollTo(newEmployee);
        }

        showNotification("تمت إضافة الموظف (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 2. Edit Selected Employee
     */
    @FXML
    private void handleEditEmployee(ActionEvent event) {
        EmployeeModel selected = tblEmployees != null ? tblEmployees.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار موظف من الجدول لتعديل بياناته!", true);
            return;
        }

        String nationalId = getSafeText(txtNationalId);
        String name = getSafeText(txtName);
        if (nationalId.isEmpty() || name.isEmpty()) {
            showNotification("لا يمكن ترك الرقم القومي أو الاسم فارغًا!", true);
            return;
        }

        // Update properties
        selected.setNationalId(nationalId);
        selected.setName(name);
        selected.setJobTitle(cbJobTitle != null && cbJobTitle.getValue() != null ? cbJobTitle.getValue().trim() : "");
        selected.setDepartment(cbDepartment != null && cbDepartment.getValue() != null ? cbDepartment.getValue().trim() : "");
        selected.setPhone1(getSafeText(txtPhone1));
        selected.setPhone2(getSafeText(txtPhone2));
        selected.setAddress(getSafeText(txtAddress));
        selected.setBirthDate(dpBirthDate != null && dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : "");
        selected.setUsername(getSafeText(txtUsername));
        selected.setPassword(getSafeText(txtPassword));
        selected.setSalary(getSafeText(txtSalary));
        selected.setSalaryDate(dpSalaryDate != null && dpSalaryDate.getValue() != null ? dpSalaryDate.getValue().toString() : "");
        selected.setBankAccount(getSafeText(txtBankAccount));

        // Update permissions
        selected.setAccessCashier(chkCashier != null && chkCashier.isSelected());
        selected.setAccessSettings(chkSettings != null && chkSettings.isSelected());
        selected.setAccessReports(chkReports != null && chkReports.isSelected());
        selected.setAccessSales(chkSales != null && chkSales.isSelected());
        selected.setAccessEmployees(chkEmployees != null && chkEmployees.isSelected());
        selected.setAccessCustomers(chkCustomers != null && chkCustomers.isSelected());
        selected.setAccessReservations(chkReservations != null && chkReservations.isSelected());
        selected.setAccessInventory(chkInventory != null && chkInventory.isSelected());

        if (tblEmployees != null) {
            tblEmployees.refresh();
        }

        showNotification("تم حفظ وتحديث بيانات الموظف (" + name + ") بنجاح! ✔", false);
    }

    /**
     * 3. Delete Selected Employee
     */
    @FXML
    private void handleDeleteEmployee(ActionEvent event) {
        EmployeeModel selected = tblEmployees != null ? tblEmployees.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showNotification("يرجى اختيار موظف من الجدول لحذفه!", true);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("حذف الموظف: " + selected.getName());
        confirm.setContentText("هل أنت متأكد من حذف بيانات هذا الموظف نهائيًا من النظام؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedName = selected.getName();
            employeeList.remove(selected);
            refreshSequenceNumbers();
            updateTotalCount();
            handleClearFields(null);
            showNotification("تم حذف الموظف (" + deletedName + ") بنجاح! 🗑️", false);
        }
    }

    /**
     * 4. Clear Form Fields
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        if (txtNationalId != null) txtNationalId.clear();
        if (txtName != null) txtName.clear();
        if (cbJobTitle != null) cbJobTitle.setValue(null);
        if (cbDepartment != null) cbDepartment.setValue(null);
        if (txtPhone1 != null) txtPhone1.clear();
        if (txtPhone2 != null) txtPhone2.clear();
        if (txtAddress != null) txtAddress.clear();
        if (dpBirthDate != null) dpBirthDate.setValue(null);
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtSalary != null) txtSalary.clear();
        if (dpSalaryDate != null) dpSalaryDate.setValue(null);
        if (txtBankAccount != null) txtBankAccount.clear();

        // Clear permissions
        setAllPermissions(false);

        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().clearSelection();
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال موظف جديد.");
        }
    }

    /**
     * 5. Search Button Handler
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearch != null ? txtSearch.getText() : "";
        applySearchQuery(query);
    }

    /**
     * 6. Clear Search Handler
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        applySearchQuery("");
    }

    /**
     * 7. Select / Deselect All Permissions
     */
    @FXML
    private void handleSelectAllPermissions(ActionEvent event) {
        boolean selected = chkSelectAll != null && chkSelectAll.isSelected();
        setAllPermissions(selected);
    }

    private void setAllPermissions(boolean state) {
        if (chkSelectAll != null) chkSelectAll.setSelected(state);
        if (chkCashier != null) chkCashier.setSelected(state);
        if (chkSettings != null) chkSettings.setSelected(state);
        if (chkReports != null) chkReports.setSelected(state);
        if (chkSales != null) chkSales.setSelected(state);
        if (chkEmployees != null) chkEmployees.setSelected(state);
        if (chkCustomers != null) chkCustomers.setSelected(state);
        if (chkReservations != null) chkReservations.setSelected(state);
        if (chkInventory != null) chkInventory.setSelected(state);
    }

    // =========================================================================
    // Helpers & Model Creation
    // =========================================================================

    private EmployeeModel createModelFromForm(int seq) {
        return new EmployeeModel(
            seq,
            getSafeText(txtNationalId),
            getSafeText(txtName),
            cbJobTitle != null && cbJobTitle.getValue() != null ? cbJobTitle.getValue().trim() : "",
            cbDepartment != null && cbDepartment.getValue() != null ? cbDepartment.getValue().trim() : "",
            getSafeText(txtPhone1),
            getSafeText(txtPhone2),
            getSafeText(txtAddress),
            dpBirthDate != null && dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : "",
            getSafeText(txtUsername),
            getSafeText(txtPassword),
            getSafeText(txtSalary),
            dpSalaryDate != null && dpSalaryDate.getValue() != null ? dpSalaryDate.getValue().toString() : "",
            getSafeText(txtBankAccount),
            chkCashier != null && chkCashier.isSelected(),
            chkSettings != null && chkSettings.isSelected(),
            chkReports != null && chkReports.isSelected(),
            chkSales != null && chkSales.isSelected(),
            chkEmployees != null && chkEmployees.isSelected(),
            chkCustomers != null && chkCustomers.isSelected(),
            chkReservations != null && chkReservations.isSelected(),
            chkInventory != null && chkInventory.isSelected()
        );
    }

    private void populateFieldsFromModel(EmployeeModel model) {
        if (txtNationalId != null) txtNationalId.setText(model.getNationalId());
        if (txtName != null) txtName.setText(model.getName());
        if (cbJobTitle != null) cbJobTitle.setValue(model.getJobTitle());
        if (cbDepartment != null) cbDepartment.setValue(model.getDepartment());
        if (txtPhone1 != null) txtPhone1.setText(model.getPhone1());
        if (txtPhone2 != null) txtPhone2.setText(model.getPhone2());
        if (txtAddress != null) txtAddress.setText(model.getAddress());
        
        if (dpBirthDate != null) {
            try {
                dpBirthDate.setValue(model.getBirthDate() != null && !model.getBirthDate().isEmpty() ? LocalDate.parse(model.getBirthDate()) : null);
            } catch (Exception e) {
                dpBirthDate.setValue(null);
            }
        }

        if (txtUsername != null) txtUsername.setText(model.getUsername());
        if (txtPassword != null) txtPassword.setText(model.getPassword());
        if (txtSalary != null) txtSalary.setText(model.getSalary());

        if (dpSalaryDate != null) {
            try {
                dpSalaryDate.setValue(model.getSalaryDate() != null && !model.getSalaryDate().isEmpty() ? LocalDate.parse(model.getSalaryDate()) : null);
            } catch (Exception e) {
                dpSalaryDate.setValue(null);
            }
        }

        if (txtBankAccount != null) txtBankAccount.setText(model.getBankAccount());

        // Checkboxes
        if (chkCashier != null) chkCashier.setSelected(model.isAccessCashier());
        if (chkSettings != null) chkSettings.setSelected(model.isAccessSettings());
        if (chkReports != null) chkReports.setSelected(model.isAccessReports());
        if (chkSales != null) chkSales.setSelected(model.isAccessSales());
        if (chkEmployees != null) chkEmployees.setSelected(model.isAccessEmployees());
        if (chkCustomers != null) chkCustomers.setSelected(model.isAccessCustomers());
        if (chkReservations != null) chkReservations.setSelected(model.isAccessReservations());
        if (chkInventory != null) chkInventory.setSelected(model.isAccessInventory());

        boolean allSelected = model.isAccessCashier() && model.isAccessSettings() && model.isAccessReports() &&
                              model.isAccessSales() && model.isAccessEmployees() && model.isAccessCustomers() &&
                              model.isAccessReservations() && model.isAccessInventory();
        if (chkSelectAll != null) chkSelectAll.setSelected(allSelected);

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض بيانات: " + model.getName() + " (المعرف: " + model.getNationalId() + ")");
        }
    }

    private void refreshSequenceNumbers() {
        for (int i = 0; i < employeeList.size(); i++) {
            employeeList.get(i).setSeq(i + 1);
        }
    }

    private void updateTotalCount() {
        if (lblTotalCount != null) {
            int count = filteredEmployeeList != null ? filteredEmployeeList.size() : employeeList.size();
            lblTotalCount.setText(String.valueOf(count));
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
    // Employee Data Model Class
    // =========================================================================
    public static class EmployeeModel {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty nationalId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty jobTitle;
        private final SimpleStringProperty department;
        private final SimpleStringProperty phone1;
        private final SimpleStringProperty phone2;
        private final SimpleStringProperty address;
        private final SimpleStringProperty birthDate;
        private final SimpleStringProperty username;
        private final SimpleStringProperty password;
        private final SimpleStringProperty salary;
        private final SimpleStringProperty salaryDate;
        private final SimpleStringProperty bankAccount;

        // 8 Access Rights
        private boolean accessCashier;
        private boolean accessSettings;
        private boolean accessReports;
        private boolean accessSales;
        private boolean accessEmployees;
        private boolean accessCustomers;
        private boolean accessReservations;
        private boolean accessInventory;

        public EmployeeModel(int seq, String nationalId, String name, String jobTitle, String department,
                             String phone1, String phone2, String address, String birthDate,
                             String username, String password, String salary, String salaryDate, String bankAccount,
                             boolean accessCashier, boolean accessSettings, boolean accessReports, boolean accessSales,
                             boolean accessEmployees, boolean accessCustomers, boolean accessReservations, boolean accessInventory) {
            this.seq = new SimpleIntegerProperty(seq);
            this.nationalId = new SimpleStringProperty(nationalId);
            this.name = new SimpleStringProperty(name);
            this.jobTitle = new SimpleStringProperty(jobTitle);
            this.department = new SimpleStringProperty(department);
            this.phone1 = new SimpleStringProperty(phone1);
            this.phone2 = new SimpleStringProperty(phone2);
            this.address = new SimpleStringProperty(address);
            this.birthDate = new SimpleStringProperty(birthDate);
            this.username = new SimpleStringProperty(username);
            this.password = new SimpleStringProperty(password);
            this.salary = new SimpleStringProperty(salary);
            this.salaryDate = new SimpleStringProperty(salaryDate);
            this.bankAccount = new SimpleStringProperty(bankAccount);

            this.accessCashier = accessCashier;
            this.accessSettings = accessSettings;
            this.accessReports = accessReports;
            this.accessSales = accessSales;
            this.accessEmployees = accessEmployees;
            this.accessCustomers = accessCustomers;
            this.accessReservations = accessReservations;
            this.accessInventory = accessInventory;
        }

        // Properties for TableView
        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty nationalIdProperty() { return nationalId; }
        public String getNationalId() { return nationalId.get(); }
        public void setNationalId(String val) { this.nationalId.set(val); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String val) { this.name.set(val); }

        public SimpleStringProperty jobTitleProperty() { return jobTitle; }
        public String getJobTitle() { return jobTitle.get(); }
        public void setJobTitle(String val) { this.jobTitle.set(val); }

        public SimpleStringProperty departmentProperty() { return department; }
        public String getDepartment() { return department.get(); }
        public void setDepartment(String val) { this.department.set(val); }

        public SimpleStringProperty phone1Property() { return phone1; }
        public String getPhone1() { return phone1.get(); }
        public void setPhone1(String val) { this.phone1.set(val); }

        public SimpleStringProperty phone2Property() { return phone2; }
        public String getPhone2() { return phone2.get(); }
        public void setPhone2(String val) { this.phone2.set(val); }

        public SimpleStringProperty addressProperty() { return address; }
        public String getAddress() { return address.get(); }
        public void setAddress(String val) { this.address.set(val); }

        public SimpleStringProperty birthDateProperty() { return birthDate; }
        public String getBirthDate() { return birthDate.get(); }
        public void setBirthDate(String val) { this.birthDate.set(val); }

        public SimpleStringProperty salaryProperty() { return salary; }
        public String getSalary() { return salary.get(); }
        public void setSalary(String val) { this.salary.set(val); }

        public SimpleStringProperty salaryDateProperty() { return salaryDate; }
        public String getSalaryDate() { return salaryDate.get(); }
        public void setSalaryDate(String val) { this.salaryDate.set(val); }

        // Non-table / security fields
        public String getUsername() { return username.get(); }
        public void setUsername(String val) { this.username.set(val); }

        public String getPassword() { return password.get(); }
        public void setPassword(String val) { this.password.set(val); }

        public String getBankAccount() { return bankAccount.get(); }
        public void setBankAccount(String val) { this.bankAccount.set(val); }

        // Access Rights
        public boolean isAccessCashier() { return accessCashier; }
        public void setAccessCashier(boolean val) { this.accessCashier = val; }

        public boolean isAccessSettings() { return accessSettings; }
        public void setAccessSettings(boolean val) { this.accessSettings = val; }

        public boolean isAccessReports() { return accessReports; }
        public void setAccessReports(boolean val) { this.accessReports = val; }

        public boolean isAccessSales() { return accessSales; }
        public void setAccessSales(boolean val) { this.accessSales = val; }

        public boolean isAccessEmployees() { return accessEmployees; }
        public void setAccessEmployees(boolean val) { this.accessEmployees = val; }

        public boolean isAccessCustomers() { return accessCustomers; }
        public void setAccessCustomers(boolean val) { this.accessCustomers = val; }

        public boolean isAccessReservations() { return accessReservations; }
        public void setAccessReservations(boolean val) { this.accessReservations = val; }

        public boolean isAccessInventory() { return accessInventory; }
        public void setAccessInventory(boolean val) { this.accessInventory = val; }
    }
}
