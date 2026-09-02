package Interfaces;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

/**
 * Controller for the Employees Management Window.
 * 
 * All CRUD Operations (Add, Edit, Delete, Search) are executed directly against the MySQL database.
 * TableView info is retrieved strictly from the database table: employees.
 * 
 * Database Mapping (Table: employees):
 * - n_id           : الرقم القومي (Primary Key)
 * - full_name      : الاسم الكامل
 * - job_title      : المسمى الوظيفي
 * - department     : القسم
 * - phone_one      : رقم الهاتف الاساسي
 * - phone_two      : رقم الهاتف الاضافي
 * - address        : العنوان
 * - date_of_birth  : تاريخ الميلاد
 * - salary         : الراتب الاساسي
 * - date_of_salary : تاريخ استحقاق الراتب
 * - user_name      : اسم المستخدم للنظام
 * - user_password  : كلمة المرور للنظام
 * - access_rights  : صلاحيات الوصول (8 characters: 1=Cashier, 2=Settings, 3=Reports, 4=Sales, 5=Employees, 6=Customers, 7=Reservations, 8=Inventory)
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
    @FXML private JFXButton btnManageJobTitle;
    @FXML private JFXComboBox<String> cbDepartment;
    @FXML private JFXButton btnManageDepartment;
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
    // FXML Access Rights (8 Checkboxes + Select All)
    // 1: Cashier, 2: Settings, 3: Reports, 4: Sales, 5: Employees, 6: Customers, 7: Reservations, 8: Inventory
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
    // FXML Action Buttons & Status
    // =========================================================================
    @FXML private JFXButton btnAdd;
    @FXML private JFXButton btnEdit;
    @FXML private JFXButton btnDelete;
    @FXML private JFXButton btnClear;
    @FXML private Label lblFormStatus;
    @FXML private Label lblActionMessage;

    // =========================================================================
    // FXML Search & TableView
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
    private final ObservableList<String> departmentsList = FXCollections.observableArrayList();
    private final ObservableList<String> jobTitlesList = FXCollections.observableArrayList();

    private static final String OPTION_MANAGE_DEPTS = "➕ إضافة / إدارة الأقسام...";
    private static final String OPTION_MANAGE_JOBS = "➕ إضافة / إدارة المسميات...";

    private Timeline clockTimeline;
    private final Locale arabicLocale = Locale.forLanguageTag("ar");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", arabicLocale);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLiveDateTime();
        initUserSessionDisplay();
        createTableIfNotExists();
        setupFieldLimitersAndTriggers();
        loadDepartmentsFromDatabase();
        loadJobTitlesFromDatabase();
        initTableColumns();
        setupAccessRightsListeners();
        loadEmployeesFromDatabase(null);
        setupTableSelection();
        setupSearchFilter();
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
     * Sets up strict character limits, numeric formatting, and Click triggers.
     */
    private void setupFieldLimitersAndTriggers() {
        // 1. National ID: Digits only, max 14 characters
        if (txtNationalId != null) {
            txtNationalId.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 14) {
                    filtered = filtered.substring(0, 14);
                }
                if (!filtered.equals(newVal)) {
                    txtNationalId.setText(filtered);
                }
            });
        }

        // 2. Phone 1: Digits only, max 11 characters
        if (txtPhone1 != null) {
            txtPhone1.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 11) {
                    filtered = filtered.substring(0, 11);
                }
                if (!filtered.equals(newVal)) {
                    txtPhone1.setText(filtered);
                }
            });
        }

        // 3. Phone 2: Digits only, max 11 characters
        if (txtPhone2 != null) {
            txtPhone2.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 11) {
                    filtered = filtered.substring(0, 11);
                }
                if (!filtered.equals(newVal)) {
                    txtPhone2.setText(filtered);
                }
            });
        }

        // 4. ComboBox Click and selection triggers
        if (cbDepartment != null) {
            cbDepartment.setOnMouseClicked(e -> openLookupManagerDialog(true));
            cbDepartment.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_DEPTS.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbDepartment.setValue(oldVal);
                        openLookupManagerDialog(true);
                    });
                }
            });
        }

        if (cbJobTitle != null) {
            cbJobTitle.setOnMouseClicked(e -> openLookupManagerDialog(false));
            cbJobTitle.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (OPTION_MANAGE_JOBS.equals(newVal)) {
                    javafx.application.Platform.runLater(() -> {
                        cbJobTitle.setValue(oldVal);
                        openLookupManagerDialog(false);
                    });
                }
            });
        }
    }

    /**
     * Loads departments from MySQL database table 'depts'.
     */
    private void loadDepartmentsFromDatabase() {
        departmentsList.clear();
        departmentsList.add(OPTION_MANAGE_DEPTS);

        String query = "SELECT * FROM depts;";
        ResultSet rs = DBConnection.executeQuery(query);
        int count = 0;
        if (rs != null) {
            try {
                while (rs.next()) {
                    String d = null;
                    try {
                        d = rs.getString("dept_name");
                    } catch (Exception ignored) {}
                    if (d == null) {
                        d = rs.getString(1);
                    }
                    if (d != null && !d.trim().isEmpty()) {
                        String clean = d.trim();
                        if (!departmentsList.contains(clean)) {
                            departmentsList.add(clean);
                            count++;
                        }
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (count == 0) {
            String[] defaults = {"الإدارة العامة", "المطبخ", "الكاشير والصالة", "المخازن والمشتريات", "خدمة التوصيل", "المحاسبة والمالية"};
            for (String d : defaults) {
                if (!departmentsList.contains(d)) {
                    departmentsList.add(d);
                    DBConnection.executeUpdate("INSERT IGNORE INTO depts (dept_name) VALUES ('" + escapeSql(d) + "');");
                }
            }
        }

        if (cbDepartment != null) {
            cbDepartment.setItems(departmentsList);
        }
    }

    /**
     * Loads job titles from MySQL database table 'job_titles'.
     */
    private void loadJobTitlesFromDatabase() {
        jobTitlesList.clear();
        jobTitlesList.add(OPTION_MANAGE_JOBS);

        String query = "SELECT * FROM job_titles;";
        ResultSet rs = DBConnection.executeQuery(query);
        int count = 0;
        if (rs != null) {
            try {
                while (rs.next()) {
                    String j = null;
                    try {
                        j = rs.getString("job_title");
                    } catch (Exception ignored) {}
                    if (j == null) {
                        j = rs.getString(1);
                    }
                    if (j != null && !j.trim().isEmpty()) {
                        String clean = j.trim();
                        if (!jobTitlesList.contains(clean)) {
                            jobTitlesList.add(clean);
                            count++;
                        }
                    }
                }
                rs.close();
            } catch (SQLException ignored) {}
        }

        if (count == 0) {
            String[] defaults = {"مدير مطعم", "شيف عمومي", "كاشير", "ويتر / صالة", "أمين مخزن", "سائق دليفري", "محاسب"};
            for (String j : defaults) {
                if (!jobTitlesList.contains(j)) {
                    jobTitlesList.add(j);
                    DBConnection.executeUpdate("INSERT IGNORE INTO job_titles (job_title) VALUES ('" + escapeSql(j) + "');");
                }
            }
        }

        if (cbJobTitle != null) {
            cbJobTitle.setItems(jobTitlesList);
        }
    }

    @FXML
    private void handleOpenDepartmentManager(ActionEvent event) {
        openLookupManagerDialog(true);
    }

    @FXML
    private void handleOpenJobTitleManager(ActionEvent event) {
        openLookupManagerDialog(false);
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

        if (tblEmployees != null) {
            tblEmployees.setItems(employeeList);
        }
        updateTotalCount();
    }

    /**
     * Ensures MySQL tables 'employees', 'depts', and 'job_titles' exist with all matching columns.
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS employees ("
                + "n_id VARCHAR(50) PRIMARY KEY,"
                + "full_name VARCHAR(100) NOT NULL,"
                + "job_title VARCHAR(100),"
                + "department VARCHAR(100),"
                + "phone_one VARCHAR(50),"
                + "phone_two VARCHAR(50),"
                + "address VARCHAR(255),"
                + "date_of_birth VARCHAR(50),"
                + "salary VARCHAR(50),"
                + "date_of_salary VARCHAR(50),"
                + "user_name VARCHAR(100),"
                + "user_password VARCHAR(100),"
                + "access_rights VARCHAR(20)"
                + ") DEFAULT CHARSET=utf8mb4;";
        DBConnection.executeUpdate(sql);

        String sqlDepts = "CREATE TABLE IF NOT EXISTS depts ("
                + "dept_name VARCHAR(100) PRIMARY KEY"
                + ") DEFAULT CHARSET=utf8mb4;";
        DBConnection.executeUpdate(sqlDepts);

        String sqlJobs = "CREATE TABLE IF NOT EXISTS job_titles ("
                + "job_title VARCHAR(100) PRIMARY KEY"
                + ") DEFAULT CHARSET=utf8mb4;";
        DBConnection.executeUpdate(sqlJobs);
    }

    /**
     * Retrieves employees from the MySQL database table 'employees' (or searches by keyword)
     * and fills the TableView directly.
     * 
     * @param keyword Search query or null for all records
     */
    private void loadEmployeesFromDatabase(String keyword) {
        employeeList.clear();

        String query;
        if (keyword == null || keyword.trim().isEmpty()) {
            query = "SELECT * FROM employees;";
        } else {
            String cleanKey = escapeSql(keyword.trim());
            query = "SELECT * FROM employees WHERE "
                    + "full_name LIKE '%" + cleanKey + "%' OR "
                    + "n_id LIKE '%" + cleanKey + "%' OR "
                    + "job_title LIKE '%" + cleanKey + "%' OR "
                    + "department LIKE '%" + cleanKey + "%' OR "
                    + "phone_one LIKE '%" + cleanKey + "%' OR "
                    + "phone_two LIKE '%" + cleanKey + "%' OR "
                    + "address LIKE '%" + cleanKey + "%' OR "
                    + "user_name LIKE '%" + cleanKey + "%';";
        }

        ResultSet rs = DBConnection.executeQuery(query);

        if (rs != null) {
            try {
                int seq = 1;
                while (rs.next()) {
                    String nId = rs.getString("n_id");
                    String fullName = rs.getString("full_name");
                    String jobTitle = rs.getString("job_title");
                    String department = rs.getString("department");
                    String phone1 = rs.getString("phone_one");
                    String phone2 = rs.getString("phone_two");
                    String address = rs.getString("address");
                    String birthDate = rs.getString("date_of_birth");
                    String salary = rs.getString("salary");
                    String salaryDate = rs.getString("date_of_salary");
                    String username = rs.getString("user_name");
                    String password = rs.getString("user_password");
                    String accessRights = rs.getString("access_rights");

                    boolean[] rights = parseAccessRights(accessRights);

                    EmployeeModel emp = new EmployeeModel(
                            seq++, nId, fullName, jobTitle, department,
                            phone1, phone2, address, birthDate,
                            formatSalaryForUI(salary), salaryDate, username, password, "",
                            rights[0], rights[1], rights[2], rights[3],
                            rights[4], rights[5], rights[6], rights[7]
                    );

                    employeeList.add(emp);
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error reading employees from database: " + e.getMessage());
            }
        }

        refreshSequenceNumbers();
        updateTotalCount();
    }

    /**
     * Inserts an employee record into MySQL table 'employees'.
     */
    private int insertEmployeeToDB(EmployeeModel emp) {
        String accessStr = buildAccessRightsStringFromBooleans(
                emp.isAccessCashier(), emp.isAccessSettings(), emp.isAccessReports(), emp.isAccessSales(),
                emp.isAccessEmployees(), emp.isAccessCustomers(), emp.isAccessReservations(), emp.isAccessInventory()
        );

        String sql = "INSERT INTO employees (n_id, full_name, job_title, department, phone_one, phone_two, address, date_of_birth, salary, date_of_salary, user_name, user_password, access_rights) "
                + "VALUES ('" + escapeSql(emp.getNationalId()) + "', '"
                + escapeSql(emp.getName()) + "', '"
                + escapeSql(emp.getJobTitle()) + "', '"
                + escapeSql(emp.getDepartment()) + "', '"
                + escapeSql(emp.getPhone1()) + "', "
                + getSqlNullable(emp.getPhone2()) + ", '"
                + escapeSql(emp.getAddress()) + "', '"
                + escapeSql(emp.getBirthDate()) + "', '"
                + cleanSalary(emp.getSalary()) + "', '"
                + escapeSql(emp.getSalaryDate()) + "', '"
                + escapeSql(emp.getUsername()) + "', '"
                + escapeSql(emp.getPassword()) + "', '"
                + accessStr + "') "
                + "ON DUPLICATE KEY UPDATE "
                + "full_name = VALUES(full_name), job_title = VALUES(job_title), department = VALUES(department), "
                + "phone_one = VALUES(phone_one), phone_two = VALUES(phone_two), address = VALUES(address), "
                + "date_of_birth = VALUES(date_of_birth), salary = VALUES(salary), date_of_salary = VALUES(date_of_salary), "
                + "user_name = VALUES(user_name), user_password = VALUES(user_password), access_rights = VALUES(access_rights);";

        return DBConnection.executeUpdate(sql);
    }

    /**
     * Updates an existing employee in MySQL table 'employees'.
     */
    private int updateEmployeeInDB(EmployeeModel emp) {
        String accessStr = buildAccessRightsStringFromBooleans(
                emp.isAccessCashier(), emp.isAccessSettings(), emp.isAccessReports(), emp.isAccessSales(),
                emp.isAccessEmployees(), emp.isAccessCustomers(), emp.isAccessReservations(), emp.isAccessInventory()
        );

        String sql = "UPDATE employees SET "
                + "full_name = '" + escapeSql(emp.getName()) + "', "
                + "job_title = '" + escapeSql(emp.getJobTitle()) + "', "
                + "department = '" + escapeSql(emp.getDepartment()) + "', "
                + "phone_one = '" + escapeSql(emp.getPhone1()) + "', "
                + "phone_two = " + getSqlNullable(emp.getPhone2()) + ", "
                + "address = '" + escapeSql(emp.getAddress()) + "', "
                + "date_of_birth = '" + escapeSql(emp.getBirthDate()) + "', "
                + "salary = '" + cleanSalary(emp.getSalary()) + "', "
                + "date_of_salary = '" + escapeSql(emp.getSalaryDate()) + "', "
                + "user_name = '" + escapeSql(emp.getUsername()) + "', "
                + "user_password = '" + escapeSql(emp.getPassword()) + "', "
                + "access_rights = '" + accessStr + "' "
                + "WHERE n_id = '" + escapeSql(emp.getNationalId()) + "';";

        return DBConnection.executeUpdate(sql);
    }

    /**
     * Deletes an employee from MySQL table 'employees'.
     */
    private int deleteEmployeeFromDB(String nationalId) {
        String sql = "DELETE FROM employees WHERE n_id = '" + escapeSql(nationalId) + "';";
        return DBConnection.executeUpdate(sql);
    }

    /**
     * Cleans salary string to a pure numeric format (e.g. 18000.00) for MySQL insertion.
     */
    private static String cleanSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return "0.00";
        }
        String cleaned = salaryStr.replace("ج.م", "").replace("EGP", "").replace(",", "").replaceAll("[^0-9.]", "").trim();
        if (cleaned.isEmpty()) {
            return "0.00";
        }
        try {
            double val = Double.parseDouble(cleaned);
            return String.format(Locale.US, "%.2f", val);
        } catch (NumberFormatException e) {
            return cleaned;
        }
    }

    /**
     * Formats raw numeric salary from database into user-friendly display format.
     */
    private static String formatSalaryForUI(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return "0.00 ج.م";
        }
        String cleaned = salaryStr.replace("ج.م", "").replace("EGP", "").replace(",", "").replaceAll("[^0-9.]", "").trim();
        try {
            double val = Double.parseDouble(cleaned);
            return String.format(Locale.US, "%,.2f ج.م", val);
        } catch (NumberFormatException e) {
            return salaryStr.endsWith("ج.م") ? salaryStr : salaryStr + " ج.م";
        }
    }

    /**
     * Formats nullable optional SQL fields (returns NULL literal if empty, or quoted escaped string).
     */
    private static String getSqlNullable(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "NULL";
        }
        return "'" + escapeSql(val.trim()) + "'";
    }

    // =========================================================================
    // Access Rights Helper Functions (8-character 0/1 representation)
    // 1st: Cashier, 2nd: Settings, 3rd: Reports, 4th: Sales,
    // 5th: Employees, 6th: Customers, 7th: Reservations, 8th: Inventory
    // =========================================================================

    private String buildAccessRightsStringFromBooleans(boolean c, boolean s, boolean r, boolean sa, boolean e, boolean cu, boolean res, boolean inv) {
        StringBuilder sb = new StringBuilder();
        sb.append(c ? "1" : "0");
        sb.append(s ? "1" : "0");
        sb.append(r ? "1" : "0");
        sb.append(sa ? "1" : "0");
        sb.append(e ? "1" : "0");
        sb.append(cu ? "1" : "0");
        sb.append(res ? "1" : "0");
        sb.append(inv ? "1" : "0");
        return sb.toString();
    }

    private boolean[] parseAccessRights(String str) {
        boolean[] rights = new boolean[8];
        if (str == null || str.length() < 8) {
            return rights;
        }
        for (int i = 0; i < 8; i++) {
            rights[i] = str.charAt(i) == '1';
        }
        return rights;
    }

    private void setupAccessRightsListeners() {
        JFXCheckBox[] boxes = {chkCashier, chkSettings, chkReports, chkSales, chkEmployees, chkCustomers, chkReservations, chkInventory};
        for (JFXCheckBox box : boxes) {
            if (box != null) {
                box.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllState());
            }
        }
    }

    @FXML
    private void handleSelectAllPermissions(ActionEvent event) {
        boolean selected = chkSelectAll != null && chkSelectAll.isSelected();
        JFXCheckBox[] boxes = {chkCashier, chkSettings, chkReports, chkSales, chkEmployees, chkCustomers, chkReservations, chkInventory};
        for (JFXCheckBox box : boxes) {
            if (box != null) {
                box.setSelected(selected);
            }
        }
    }

    private void updateSelectAllState() {
        if (chkSelectAll == null) return;
        boolean all = (chkCashier != null && chkCashier.isSelected())
                && (chkSettings != null && chkSettings.isSelected())
                && (chkReports != null && chkReports.isSelected())
                && (chkSales != null && chkSales.isSelected())
                && (chkEmployees != null && chkEmployees.isSelected())
                && (chkCustomers != null && chkCustomers.isSelected())
                && (chkReservations != null && chkReservations.isSelected())
                && (chkInventory != null && chkInventory.isSelected());

        chkSelectAll.setSelected(all);
    }

    // =========================================================================
    // Table Selection & Search Filtering (Executed directly in Database)
    // =========================================================================

    private void setupTableSelection() {
        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    populateFieldsFromModel(newVal);
                }
            });
        }
    }

    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                loadEmployeesFromDatabase(newValue);
            });
        }
    }

    // =========================================================================
    // CRUD Action Handlers (Applied Directly to Database)
    // =========================================================================

    /**
     * 1. Add Employee (INSERT INTO employees in Database)
     */
    @FXML
    private void handleAddEmployee(ActionEvent event) {
        String nationalId = getSafeText(txtNationalId);
        String name = getSafeText(txtName);
        String jobTitle = getComboBoxValue(cbJobTitle);
        if (jobTitle.isEmpty() || OPTION_MANAGE_JOBS.equals(jobTitle)) jobTitle = "موظف";
        String department = getComboBoxValue(cbDepartment);
        if (department.isEmpty() || OPTION_MANAGE_DEPTS.equals(department)) department = "عام";
        String phone1 = getSafeText(txtPhone1);
        String phone2 = getSafeText(txtPhone2);
        String address = getSafeText(txtAddress);
        LocalDate birthDate = dpBirthDate != null ? dpBirthDate.getValue() : null;
        String username = getSafeText(txtUsername);
        String password = getSafeText(txtPassword);
        String salaryStr = getSafeText(txtSalary);
        LocalDate salaryDate = dpSalaryDate != null ? dpSalaryDate.getValue() : null;

        if (nationalId.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty() || salaryStr.isEmpty()) {
            showNotification("يرجى ملء الحقول الإجبارية (الرقم القومي، الاسم، اسم المستخدم، كلمة المرور، والراتب)!", true);
            return;
        }

        // 1. National ID: Exactly 14 digits
        if (nationalId.length() != 14) {
            showNotification("يجب أن يتكون الرقم القومي من 14 رقماً بالضبط (الحالي: " + nationalId.length() + " أرقام)!", true);
            return;
        }

        // 2. Primary Phone: Exactly 11 digits
        if (phone1.isEmpty() || phone1.length() != 11) {
            showNotification("يجب أن يتكون رقم الهاتف الأساسي من 11 رقماً بالضبط (الحالي: " + phone1.length() + " أرقام)!", true);
            return;
        }

        // 3. Secondary Phone: Exactly 11 digits if entered
        if (!phone2.isEmpty() && phone2.length() != 11) {
            showNotification("يجب أن يتكون رقم الهاتف الإضافي من 11 رقماً بالضبط (الحالي: " + phone2.length() + " أرقام)!", true);
            return;
        }

        // 4. Check for duplicate national ID in database
        for (EmployeeModel emp : employeeList) {
            if (emp.getNationalId().equalsIgnoreCase(nationalId)) {
                showNotification("الرقم القومي (" + nationalId + ") مسجل مسبقًا للموظف (" + emp.getName() + ")!", true);
                return;
            }
        }

        // 5. Check for duplicate username in database
        String userChkSql = "SELECT full_name FROM employees WHERE user_name = '" + escapeSql(username) + "';";
        ResultSet rsUser = DBConnection.executeQuery(userChkSql);
        if (rsUser != null) {
            try {
                if (rsUser.next()) {
                    String existingName = rsUser.getString("full_name");
                    rsUser.close();
                    showNotification("اسم المستخدم (" + username + ") مستخدم بالفعل للموظف (" + (existingName != null ? existingName : "") + ")! يرجى اختيار اسم مستخدم آخر.", true);
                    return;
                }
                rsUser.close();
            } catch (SQLException ignored) {}
        }

        String birthDateStr = birthDate != null ? birthDate.toString() : "";
        String salaryDateStr = salaryDate != null ? salaryDate.toString() : "";
        String formattedSalary = cleanSalary(salaryStr);

        boolean c = chkCashier != null && chkCashier.isSelected();
        boolean s = chkSettings != null && chkSettings.isSelected();
        boolean r = chkReports != null && chkReports.isSelected();
        boolean sa = chkSales != null && chkSales.isSelected();
        boolean e = chkEmployees != null && chkEmployees.isSelected();
        boolean cu = chkCustomers != null && chkCustomers.isSelected();
        boolean res = chkReservations != null && chkReservations.isSelected();
        boolean inv = chkInventory != null && chkInventory.isSelected();

        int newSeq = employeeList.size() + 1;
        EmployeeModel newEmp = new EmployeeModel(
                newSeq, nationalId, name, jobTitle, department,
                phone1, phone2, address, birthDateStr,
                formattedSalary, salaryDateStr, username, password, "",
                c, s, r, sa, e, cu, res, inv
        );

        // Apply INSERT to Database
        insertEmployeeToDB(newEmp);

        // Reload TableView strictly from Database
        loadEmployeesFromDatabase(txtSearch != null ? txtSearch.getText() : null);

        showNotification("تمت إضافة الموظف (" + name + ") وحفظه في قاعدة البيانات بنجاح! ✔", false);
    }

    /**
     * 2. Edit Employee (UPDATE employees in Database)
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
        String jobTitle = getComboBoxValue(cbJobTitle);
        if (jobTitle.isEmpty() || OPTION_MANAGE_JOBS.equals(jobTitle)) jobTitle = selected.getJobTitle();
        String department = getComboBoxValue(cbDepartment);
        if (department.isEmpty() || OPTION_MANAGE_DEPTS.equals(department)) department = selected.getDepartment();
        String phone1 = getSafeText(txtPhone1);
        String phone2 = getSafeText(txtPhone2);
        String address = getSafeText(txtAddress);
        LocalDate birthDate = dpBirthDate != null ? dpBirthDate.getValue() : null;
        String username = getSafeText(txtUsername);
        String password = getSafeText(txtPassword);
        String salaryStr = getSafeText(txtSalary);
        LocalDate salaryDate = dpSalaryDate != null ? dpSalaryDate.getValue() : null;

        if (nationalId.isEmpty() || name.isEmpty() || username.isEmpty() || salaryStr.isEmpty()) {
            showNotification("لا يمكن ترك الرقم القومي، الاسم، اسم المستخدم، أو الراتب فارغًا!", true);
            return;
        }

        // 1. National ID: Exactly 14 digits
        if (nationalId.length() != 14) {
            showNotification("يجب أن يتكون الرقم القومي من 14 رقماً بالضبط (الحالي: " + nationalId.length() + " أرقام)!", true);
            return;
        }

        // 2. Primary Phone: Exactly 11 digits
        if (phone1.isEmpty() || phone1.length() != 11) {
            showNotification("يجب أن يتكون رقم الهاتف الأساسي من 11 رقماً بالضبط (الحالي: " + phone1.length() + " أرقام)!", true);
            return;
        }

        // 3. Secondary Phone: Exactly 11 digits if entered
        if (!phone2.isEmpty() && phone2.length() != 11) {
            showNotification("يجب أن يتكون رقم الهاتف الإضافي من 11 رقماً بالضبط (الحالي: " + phone2.length() + " أرقام)!", true);
            return;
        }

        // 4. Check for duplicate national ID if changed
        if (!nationalId.equalsIgnoreCase(selected.getNationalId())) {
            for (EmployeeModel emp : employeeList) {
                if (emp.getNationalId().equalsIgnoreCase(nationalId)) {
                    showNotification("الرقم القومي (" + nationalId + ") مسجل مسبقًا لموظف آخر (" + emp.getName() + ")!", true);
                    return;
                }
            }
        }

        // 5. Check for duplicate username in database for other employees
        String userChkSql = "SELECT full_name FROM employees WHERE user_name = '" + escapeSql(username) + "' AND n_id != '" + escapeSql(selected.getNationalId()) + "';";
        ResultSet rsUser = DBConnection.executeQuery(userChkSql);
        if (rsUser != null) {
            try {
                if (rsUser.next()) {
                    String existingName = rsUser.getString("full_name");
                    rsUser.close();
                    showNotification("اسم المستخدم (" + username + ") مستخدم بالفعل للموظف (" + (existingName != null ? existingName : "") + ")! يرجى اختيار اسم مستخدم آخر.", true);
                    return;
                }
                rsUser.close();
            } catch (SQLException ignored) {}
        }

        selected.setNationalId(nationalId);
        selected.setName(name);
        selected.setJobTitle(jobTitle);
        selected.setDepartment(department);
        selected.setPhone1(phone1);
        selected.setPhone2(phone2);
        selected.setAddress(address);
        if (birthDate != null) selected.setBirthDate(birthDate.toString());
        selected.setUsername(username);
        if (!password.isEmpty()) selected.setPassword(password);
        selected.setSalary(salaryStr);
        if (salaryDate != null) selected.setSalaryDate(salaryDate.toString());

        selected.setAccessCashier(chkCashier != null && chkCashier.isSelected());
        selected.setAccessSettings(chkSettings != null && chkSettings.isSelected());
        selected.setAccessReports(chkReports != null && chkReports.isSelected());
        selected.setAccessSales(chkSales != null && chkSales.isSelected());
        selected.setAccessEmployees(chkEmployees != null && chkEmployees.isSelected());
        selected.setAccessCustomers(chkCustomers != null && chkCustomers.isSelected());
        selected.setAccessReservations(chkReservations != null && chkReservations.isSelected());
        selected.setAccessInventory(chkInventory != null && chkInventory.isSelected());

        // Apply UPDATE to Database
        updateEmployeeInDB(selected);

        // Reload TableView strictly from Database
        loadEmployeesFromDatabase(txtSearch != null ? txtSearch.getText() : null);

        showNotification("تم تحديث وحفظ بيانات الموظف (" + name + ") في قاعدة البيانات بنجاح! ✔", false);
    }

    /**
     * 3. Delete Employee (DELETE FROM employees in Database)
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
        confirm.setContentText("هل أنت متأكد من حذف هذا الموظف نهائيًا من النظام وقاعدة البيانات؟");
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedName = selected.getName();
            String deletedNationalId = selected.getNationalId();

            // Apply DELETE to Database
            deleteEmployeeFromDB(deletedNationalId);

            // Reload TableView strictly from Database
            loadEmployeesFromDatabase(txtSearch != null ? txtSearch.getText() : null);
            handleClearFields(null);

            showNotification("تم حذف الموظف (" + deletedName + ") نهائيًا من قاعدة البيانات! 🗑️", false);
        }
    }

    /**
     * 4. Clear Form Fields
     */
    @FXML
    private void handleClearFields(ActionEvent event) {
        if (txtNationalId != null) txtNationalId.clear();
        if (txtName != null) txtName.clear();
        if (cbJobTitle != null) {
            cbJobTitle.setValue(null);
            if (cbJobTitle.getEditor() != null) cbJobTitle.getEditor().clear();
        }
        if (cbDepartment != null) {
            cbDepartment.setValue(null);
            if (cbDepartment.getEditor() != null) cbDepartment.getEditor().clear();
        }
        if (txtPhone1 != null) txtPhone1.clear();
        if (txtPhone2 != null) txtPhone2.clear();
        if (txtAddress != null) txtAddress.clear();
        if (dpBirthDate != null) dpBirthDate.setValue(null);
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtSalary != null) txtSalary.clear();
        if (dpSalaryDate != null) dpSalaryDate.setValue(null);
        if (txtBankAccount != null) txtBankAccount.clear();

        JFXCheckBox[] boxes = {chkSelectAll, chkCashier, chkSettings, chkReports, chkSales, chkEmployees, chkCustomers, chkReservations, chkInventory};
        for (JFXCheckBox box : boxes) {
            if (box != null) {
                box.setSelected(false);
            }
        }

        if (tblEmployees != null) {
            tblEmployees.getSelectionModel().clearSelection();
        }

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم تفريغ الحقول. جاهز لإدخال موظف جديد.");
        }
    }

    /**
     * 5. Search Button Handler (Queries MySQL database directly)
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearch != null ? txtSearch.getText() : "";
        loadEmployeesFromDatabase(query);
    }

    /**
     * 6. Clear Search Handler (Reloads all records from MySQL database)
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (txtSearch != null) {
            txtSearch.clear();
        }
        loadEmployeesFromDatabase(null);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void populateFieldsFromModel(EmployeeModel model) {
        if (txtNationalId != null) txtNationalId.setText(model.getNationalId());
        if (txtName != null) txtName.setText(model.getName());
        if (cbJobTitle != null) {
            cbJobTitle.setValue(model.getJobTitle());
            if (cbJobTitle.getEditor() != null) cbJobTitle.getEditor().setText(model.getJobTitle());
        }
        if (cbDepartment != null) {
            cbDepartment.setValue(model.getDepartment());
            if (cbDepartment.getEditor() != null) cbDepartment.getEditor().setText(model.getDepartment());
        }
        if (txtPhone1 != null) txtPhone1.setText(model.getPhone1());
        if (txtPhone2 != null) txtPhone2.setText(model.getPhone2());
        if (txtAddress != null) txtAddress.setText(model.getAddress());
        if (txtUsername != null) txtUsername.setText(model.getUsername());
        if (txtPassword != null) txtPassword.setText(model.getPassword());
        if (txtSalary != null) txtSalary.setText(cleanSalary(model.getSalary()));

        if (dpBirthDate != null && model.getBirthDate() != null && !model.getBirthDate().isEmpty()) {
            try {
                dpBirthDate.setValue(LocalDate.parse(model.getBirthDate()));
            } catch (Exception ignored) {}
        } else if (dpBirthDate != null) {
            dpBirthDate.setValue(null);
        }

        if (dpSalaryDate != null && model.getSalaryDate() != null && !model.getSalaryDate().isEmpty()) {
            try {
                dpSalaryDate.setValue(LocalDate.parse(model.getSalaryDate()));
            } catch (Exception ignored) {}
        } else if (dpSalaryDate != null) {
            dpSalaryDate.setValue(null);
        }

        if (chkCashier != null) chkCashier.setSelected(model.isAccessCashier());
        if (chkSettings != null) chkSettings.setSelected(model.isAccessSettings());
        if (chkReports != null) chkReports.setSelected(model.isAccessReports());
        if (chkSales != null) chkSales.setSelected(model.isAccessSales());
        if (chkEmployees != null) chkEmployees.setSelected(model.isAccessEmployees());
        if (chkCustomers != null) chkCustomers.setSelected(model.isAccessCustomers());
        if (chkReservations != null) chkReservations.setSelected(model.isAccessReservations());
        if (chkInventory != null) chkInventory.setSelected(model.isAccessInventory());

        updateSelectAllState();

        if (lblFormStatus != null) {
            lblFormStatus.setText("تم عرض بيانات الموظف: " + model.getName());
        }
    }

    private void refreshSequenceNumbers() {
        for (int i = 0; i < employeeList.size(); i++) {
            employeeList.get(i).setSeq(i + 1);
        }
    }

    private void updateTotalCount() {
        if (lblTotalCount != null) {
            lblTotalCount.setText(String.valueOf(employeeList.size()));
        }
    }

    private String getSafeText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private String getComboBoxValue(JFXComboBox<String> cb) {
        if (cb == null) return "";
        if (cb.isEditable() && cb.getEditor() != null && cb.getEditor().getText() != null) {
            return cb.getEditor().getText().trim();
        }
        return cb.getValue() != null ? cb.getValue().trim() : "";
    }

    private static String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    private void showNotification(String message, boolean isError) {
        if (lblActionMessage != null) {
            lblActionMessage.setText(message);
            lblActionMessage.getStyleClass().removeAll("login-msg-error", "login-msg-success");
            lblActionMessage.getStyleClass().add(isError ? "login-msg-error" : "login-msg-success");
            lblActionMessage.setVisible(true);
        }
    }

    /**
     * Opens a modal popup window for managing Departments (table: depts, col: dept_name)
     * or Job Titles (table: job_titles, col: job_title).
     */
    private void openLookupManagerDialog(boolean isDepartment) {
        String entityTitle = isDepartment ? "الأقسام والإدارات" : "المسميات الوظيفية";
        String singleTitle = isDepartment ? "القسم" : "المسمى الوظيفي";
        String tableName = isDepartment ? "depts" : "job_titles";
        String columnName = isDepartment ? "dept_name" : "job_title";

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("إدارة " + entityTitle);
        dialog.setHeaderText("إضافة، تعديل، حذف، وبحث في قائمة " + entityTitle);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(12.0);
        content.setPrefWidth(520.0);
        content.setPrefHeight(480.0);
        content.setPadding(new Insets(12.0));

        // Form Box
        VBox formBox = new VBox(10.0);
        formBox.setStyle("-fx-background-color: #FFF8F4; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #FFDEC9; -fx-border-radius: 8px;");

        VBox nameBox = new VBox(4.0);
        Label lblInputPrompt = new Label("اسم " + singleTitle + " *:");
        lblInputPrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");
        JFXTextField txtNameInput = new JFXTextField();
        txtNameInput.setPromptText("اكتب اسم " + singleTitle + "...");
        txtNameInput.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        txtNameInput.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));
        nameBox.getChildren().addAll(lblInputPrompt, txtNameInput);

        HBox btnBox = new HBox(8.0);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        JFXButton btnAddLookup = new JFXButton("➕ إضافة");
        btnAddLookup.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnEditLookup = new JFXButton("✏️ تعديل");
        btnEditLookup.setStyle("-fx-background-color: #E65100; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnDeleteLookup = new JFXButton("🗑️ حذف");
        btnDeleteLookup.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        JFXButton btnClearLookup = new JFXButton("🔄 تفريغ");
        btnClearLookup.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");

        btnBox.getChildren().addAll(btnAddLookup, btnEditLookup, btnDeleteLookup, btnClearLookup);
        formBox.getChildren().addAll(nameBox, btnBox);

        // Search Field
        JFXTextField txtSearchLookup = new JFXTextField();
        txtSearchLookup.setPromptText("🔍 بحث في قائمة " + entityTitle + "...");
        txtSearchLookup.setFocusColor(javafx.scene.paint.Color.web("#FF6B00"));
        txtSearchLookup.setUnFocusColor(javafx.scene.paint.Color.web("#FFDEC9"));

        // TableView
        TableView<LookupItem> tblLookup = new TableView<>();
        tblLookup.setStyle("-fx-background-radius: 8px;");
        VBox.setVgrow(tblLookup, Priority.ALWAYS);

        TableColumn<LookupItem, Number> colSeqL = new TableColumn<>("م");
        colSeqL.setCellValueFactory(c -> c.getValue().seqProperty());
        colSeqL.setPrefWidth(50.0);

        TableColumn<LookupItem, String> colNameL = new TableColumn<>("اسم " + singleTitle);
        colNameL.setCellValueFactory(c -> c.getValue().nameProperty());
        colNameL.setPrefWidth(420.0);

        tblLookup.getColumns().addAll(colSeqL, colNameL);

        ObservableList<LookupItem> lookupData = FXCollections.observableArrayList();
        tblLookup.setItems(lookupData);

        // Load data from DB
        Runnable loadData = () -> {
            lookupData.clear();
            String q = txtSearchLookup.getText() != null ? txtSearchLookup.getText().trim() : "";
            String sql = "SELECT * FROM " + tableName + ";";
            ResultSet rs = DBConnection.executeQuery(sql);
            if (rs != null) {
                try {
                    ResultSetMetaData meta = rs.getMetaData();
                    int count = meta.getColumnCount();
                    int seq = 1;
                    while (rs.next()) {
                        String n = null;
                        try {
                            n = rs.getString(columnName);
                        } catch (Exception ignored) {}
                        if (n == null) {
                            for (int i = 1; i <= count; i++) {
                                String cn = meta.getColumnName(i);
                                if (!cn.equalsIgnoreCase("id")) {
                                    n = rs.getString(i);
                                    if (n != null) break;
                                }
                            }
                        }

                        if (n != null && !n.trim().isEmpty()) {
                            if (q.isEmpty() || n.toLowerCase().contains(q.toLowerCase())) {
                                lookupData.add(new LookupItem(seq++, n.trim()));
                            }
                        }
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }
        };

        txtSearchLookup.textProperty().addListener((obs, oldVal, newVal) -> loadData.run());
        loadData.run();

        // Selection listener
        tblLookup.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNameInput.setText(newVal.getName());
            }
        });

        // Double-click row to select into main form
        tblLookup.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblLookup.getSelectionModel().getSelectedItem() != null) {
                String selectedVal = tblLookup.getSelectionModel().getSelectedItem().getName();
                if (isDepartment) {
                    if (cbDepartment != null) cbDepartment.setValue(selectedVal);
                } else {
                    if (cbJobTitle != null) cbJobTitle.setValue(selectedVal);
                }
                dialog.close();
            }
        });

        // Add Action
        btnAddLookup.setOnAction(e -> {
            String val = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (val.isEmpty()) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى إدخال اسم " + singleTitle + " أولاً!");
                return;
            }

            // Duplicate check
            String chkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = '" + escapeSql(val) + "';";
            ResultSet rs = DBConnection.executeQuery(chkSql);
            if (rs != null) {
                try {
                    if (rs.next() && rs.getInt(1) > 0) {
                        rs.close();
                        showSimpleAlert(AlertType.WARNING, "تنبيه", "اسم " + singleTitle + " (" + val + ") مسجل مسبقًا في قاعدة البيانات!");
                        return;
                    }
                    rs.close();
                } catch (SQLException ignored) {}
            }

            String insSql = "INSERT INTO " + tableName + " (" + columnName + ") VALUES ('" + escapeSql(val) + "');";
            DBConnection.executeUpdate(insSql);
            txtNameInput.clear();
            loadData.run();

            if (isDepartment) {
                loadDepartmentsFromDatabase();
                if (cbDepartment != null) cbDepartment.setValue(val);
            } else {
                loadJobTitlesFromDatabase();
                if (cbJobTitle != null) cbJobTitle.setValue(val);
            }
        });

        // Edit Action
        btnEditLookup.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى اختيار عنصر من الجدول لتعديله!");
                return;
            }
            String newVal = txtNameInput.getText() != null ? txtNameInput.getText().trim() : "";
            if (newVal.isEmpty()) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "لا يمكن ترك الاسم فارغًا!");
                return;
            }
            String oldVal = sel.getName();

            DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            String updSql = "UPDATE " + tableName + " SET " + columnName + " = '" + escapeSql(newVal) + "' WHERE " + columnName + " = '" + escapeSql(oldVal) + "';";
            DBConnection.executeUpdate(updSql);

            // Update employees table references
            String empCol = isDepartment ? "department" : "job_title";
            String updEmp = "UPDATE employees SET " + empCol + " = '" + escapeSql(newVal) + "' WHERE " + empCol + " = '" + escapeSql(oldVal) + "';";
            DBConnection.executeUpdate(updEmp);

            loadData.run();
            if (isDepartment) {
                loadDepartmentsFromDatabase();
                if (cbDepartment != null) cbDepartment.setValue(newVal);
            } else {
                loadJobTitlesFromDatabase();
                if (cbJobTitle != null) cbJobTitle.setValue(newVal);
            }
        });

        // Delete Action
        btnDeleteLookup.setOnAction(e -> {
            LookupItem sel = tblLookup.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(AlertType.WARNING, "تنبيه", "يرجى اختيار عنصر من الجدول لحذفه!");
                return;
            }
            String delVal = sel.getName();

            Alert confirm = new Alert(AlertType.CONFIRMATION);
            confirm.setTitle("تأكيد الحذف");
            confirm.setHeaderText("حذف " + singleTitle + ": " + delVal);
            confirm.setContentText("هل أنت متأكد من حذف هذا العنصر نهائيًا من جدول (" + tableName + ")؟");
            confirm.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                DBConnection.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
                String delSql = "DELETE FROM " + tableName + " WHERE " + columnName + " = '" + escapeSql(delVal) + "';";
                DBConnection.executeUpdate(delSql);
                txtNameInput.clear();
                loadData.run();
                if (isDepartment) {
                    loadDepartmentsFromDatabase();
                    if (cbDepartment != null && delVal.equals(cbDepartment.getValue())) cbDepartment.setValue(null);
                } else {
                    loadJobTitlesFromDatabase();
                    if (cbJobTitle != null && delVal.equals(cbJobTitle.getValue())) cbJobTitle.setValue(null);
                }
            }
        });

        // Clear Action
        btnClearLookup.setOnAction(e -> {
            txtNameInput.clear();
            tblLookup.getSelectionModel().clearSelection();
        });

        content.getChildren().addAll(formBox, txtSearchLookup, tblLookup);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showSimpleAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    public static class LookupItem {
        private final SimpleIntegerProperty seq;
        private final SimpleStringProperty name;

        public LookupItem(int seq, String name) {
            this.seq = new SimpleIntegerProperty(seq);
            this.name = new SimpleStringProperty(name);
        }

        public SimpleIntegerProperty seqProperty() { return seq; }
        public int getSeq() { return seq.get(); }
        public void setSeq(int val) { this.seq.set(val); }

        public SimpleStringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public void setName(String val) { this.name.set(val); }
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
        private final SimpleStringProperty salary;
        private final SimpleStringProperty salaryDate;

        private SimpleStringProperty username;
        private SimpleStringProperty password;
        private SimpleStringProperty bankAccount;

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
                             String salary, String salaryDate, String username, String password, String bankAccount,
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
            this.salary = new SimpleStringProperty(salary);
            this.salaryDate = new SimpleStringProperty(salaryDate);

            this.username = new SimpleStringProperty(username);
            this.password = new SimpleStringProperty(password);
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
