import javax.swing.*; // GUI
import javax.swing.border.Border; // Style
import javax.swing.border.EmptyBorder; // Style
import javax.swing.table.DefaultTableModel; // Table Management
import java.awt.*; // Style/Graphics
import java.awt.event.WindowAdapter; // event handling
import java.awt.event.WindowEvent; // event handling
import java.sql.*; // database itself
import java.text.ParseException; // error handling
import java.text.SimpleDateFormat; // date/time format
import java.util.Date; // general date before converted to SQL
import java.util.Locale; // localization to PH
import java.text.NumberFormat; // number formatting to currency

public class MaintenanceLog extends JFrame {

// connection to db
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";
    private Connection conn;

// gui variables
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;

// variables for the LogNewIssue to allow typing in information
    private JComboBox<EquipmentItem> equipmentCombo;
    private JComboBox<String> issueTypeCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<KitchenStaffItem> reportedByCombo;
    private JTextArea descriptionArea;
    private JButton submitButton;

// buttons to allow editing the information in a report, delete a report,
// refreshing the list, and a back button to the menu.
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton backButton;

// designing the gui with good fonts and colors
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color COLOR_PANEL_BG = new Color(245, 245, 245);
    private static final Border PADDING_BORDER = new EmptyBorder(15, 15, 15, 15);

// parent class is the main menu, also used to unhide the main menu when this frame is closed
    private Main parentMenu;

    // used in case maintenance log is launched as standalone, like when ran on IntelliJ instead of main men
    public MaintenanceLog() {
        super("Maintenance Log"); //
        this.parentMenu = null; // running standalone

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // close the window when user clicks x,
        // doesnt kill program yet

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try { // check if database is connected
                    if (conn != null && !conn.isClosed()) { // if yes, then close it to free resources
                        conn.close();
                    }
                } catch (SQLException ex) { // if closing fails, print error in case for debugging
                    ex.printStackTrace();
                }
                System.exit(0); // exits the application
            }
        });

        initialize(); // starts/initializes the buttons, tables, etc
        setLocationRelativeTo(null); // centers the window, just to make it look good lol
    }

    public MaintenanceLog(Main parentMenu) { // runs when launched from main menu
        super("Maintenance Log"); // window title same as above
        this.parentMenu = parentMenu; // saved to go back to menu if needed/pressed go back
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // same as above

        addWindowListener(new WindowAdapter() { // runs when user closes window
            @Override
            public void windowClosed(WindowEvent e) {
                // close DB connection when this window is closed
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                parentMenu.setVisible(true); // Go back to main menu/makes main menu visible again
            }
        });

        initialize(); // same as above
        setLocationRelativeTo(parentMenu); // same as above
    }

    private void initialize() {
        try { // look like a windows app or mac app
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { // print error if fail but continue running
            e.printStackTrace();
        }

        setSize(1200, 800); // set size
        setLayout(new BorderLayout(10, 10)); // set border layout and pixel gaps between sections

        try {
            Class.forName(DB_DRIVER); // load driver so java can "speak" to MySQL
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); // uses the set variables to connect to the DB

            initComponents(); // creates buttons, text fields, tables, etc

            loadMaintenanceLogs(); // defined later
            loadEquipmentDropdown(); // defined later
            loadStaffDropdown(); // defined later

        } catch (ClassNotFoundException e) { // error handling
            showErrorDialog("Database Error", "MySQL JDBC Driver not found!"); // if driver file is missing
            if (parentMenu == null) System.exit(1); // If running alone, quit. If part of MainMenu, stay open
        } catch (SQLException e) { // if password is wrong or DB is not found or offline
            showErrorDialog("Connection Failed", "Could not connect to database:\n" + e.getMessage());
            if (parentMenu == null) System.exit(1);
        } catch (Exception e) { // any other errors
            showErrorDialog("Error", "An unexpected error occurred:\n" + e.getMessage());
        }
    }

    private void initComponents() {

        String[] columnNames = {"Report ID", "Equipment", "Issue Type", "Status", "Priority", "Reported By", "Report Date", "Maint. Date", "Cost"};
        // set column names for the table

        tableModel = new DefaultTableModel(columnNames, 0) { // holds the actual data
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // cell is not directly editable, have to use the edit button.
            }
        };
        maintenanceTable = new JTable(tableModel); // displays data as a table
        maintenanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // one row at a time, no multi-select
        maintenanceTable.setFont(FONT_COMPONENT); // set font for rows and headers
        maintenanceTable.setRowHeight(25); // row taller for readability/no cut off
        maintenanceTable.getTableHeader().setFont(FONT_LABEL); // set font for rows and headers

        // Make columns wider
        maintenanceTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Equipment column; set width
        maintenanceTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Reported By column; set width

        JScrollPane tableScrollPane = new JScrollPane(maintenanceTable); // to allow scrolling for many logs
        tableScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10)); //padding, cleanliness
        add(tableScrollPane, BorderLayout.CENTER); // table is centered on the main layout

        JPanel inputPanel = new JPanel(new GridBagLayout()); // new panel and layout
        inputPanel.setBackground(COLOR_PANEL_BG); // flexible grid (rows & columns) of inputs
        inputPanel.setBorder(BorderFactory.createTitledBorder( // add border with "log new issue", make it cleaner looking
                new EmptyBorder(10, 10, 10, 10), "Log New Issue",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FONT_LABEL));

        GridBagConstraints gbc = new GridBagConstraints(); // position of components in the grid
        gbc.insets = new Insets(5, 10, 5, 10); // padding between items
        gbc.anchor = GridBagConstraints.WEST; // align items to the left

        gbc.gridx = 0; gbc.gridy = 0; // equipment
        inputPanel.add(new JLabel("Equipment:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; // equipment list; width as well
        equipmentCombo = new JComboBox<>();
        equipmentCombo.setFont(FONT_COMPONENT);
        inputPanel.add(equipmentCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // priority
        inputPanel.add(new JLabel("Priority:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; // priority list
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityCombo.setFont(FONT_COMPONENT);
        priorityCombo.setSelectedItem("Medium"); // default "value" or setting (?)
        inputPanel.add(priorityCombo, gbc);

        // Row 1: Issue Type and Reported By
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // issue type:
        inputPanel.add(new JLabel("Issue Type:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; // reported by:
        issueTypeCombo = new JComboBox<>(new String[]{"Cleaning", "Repair", "Replacement", "Others (Description)"}); // not sure what other options there are lol
        issueTypeCombo.setFont(FONT_COMPONENT);
        inputPanel.add(issueTypeCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // reported by
        inputPanel.add(new JLabel("Reported By:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; // staff list
        reportedByCombo = new JComboBox<>();
        reportedByCombo.setFont(FONT_COMPONENT);
        inputPanel.add(reportedByCombo, gbc);

        // Row 2: Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST; // top left description
        inputPanel.add(new JLabel("Description:") {{ setFont(FONT_LABEL); }}, gbc);

        // big because descriptions can get lengthyyyyyyy
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; // Span 3 columns
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 20); // 3 rows
        descriptionArea.setFont(FONT_COMPONENT);
        descriptionArea.setLineWrap(true); // auto wrap text to next line
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea); // scroll bar if text gets too long; if even needed lol
        inputPanel.add(descScrollPane, gbc);

        // Row 3: Submit Button
        gbc.gridx = 3; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; // go right
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.weighty = 0.0; gbc.gridwidth = 1;
        submitButton = new JButton("Submit New Log");
        submitButton.setFont(FONT_LABEL);
        inputPanel.add(submitButton, gbc);

        add(inputPanel, BorderLayout.SOUTH); // all of this to the south of the gui

        JPanel actionPanel = new JPanel(); // new panel yay
        // box Layout to stack buttons vertically from top to bottom
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 0, 10, 10)); // padding for ncier look

        editButton = new JButton("Edit Selected"); // button, self explanatory
        deleteButton = new JButton("Delete Selected"); // button, self explanatory
        refreshButton = new JButton("Refresh List"); // button, self explanatory
        backButton = new JButton("Go Back"); // button, self explanatory

        // consistent, same size for buttons
        Dimension buttonSize = new Dimension(150, 40);
        editButton.setMaximumSize(buttonSize);
        deleteButton.setMaximumSize(buttonSize);
        refreshButton.setMaximumSize(buttonSize);
        backButton.setMaximumSize(buttonSize);

        editButton.setFont(FONT_LABEL); // bold font
        deleteButton.setFont(FONT_LABEL); // bold font
        refreshButton.setFont(FONT_LABEL); // bold font
        backButton.setFont(FONT_LABEL); // bold font

        // add buttons to panel, use rigidarea to create gap between buttons, nicer look
        actionPanel.add(editButton);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(deleteButton);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(refreshButton);
        actionPanel.add(Box.createVerticalStrut(30)); // bigger gap to separate, design choice
        actionPanel.add(backButton);

        add(actionPanel, BorderLayout.EAST); // action panel to the right of the gui

        // link buttons to their methods, when clicked, run the method assigned to them
        submitButton.addActionListener(e -> logNewIssue());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedLog());
        refreshButton.addActionListener(e -> loadMaintenanceLogs());
        backButton.addActionListener(e -> dispose());
    }

    private void loadMaintenanceLogs() {
        tableModel.setRowCount(0); // Clear table, set row count to 0

        Locale phLocale = new Locale("en", "PH"); // peso format, eng lang
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(phLocale);
        // command to set DB, joins 3 tables; read as SQL command
        String sql = "SELECT m.ReportID, e.EquipmentName, m.IssueType, m.Status, m.Priority, " +
                "CONCAT(k.FirstName, ' ', k.LastName) AS ReportedByName, " +
                "m.ReportDate, m.MaintenanceDate, m.MaintenanceCost " +
                "FROM MaintenanceTracker m " +
                "JOIN Equipments e ON m.EquipmentID = e.EquipmentID " +
                "LEFT JOIN KitchenStaff k ON m.ReportedBy = StaffID " +
                "ORDER BY m.ReportDate DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) { // moves to next row of data in DB; loops until no more rows

                double cost = rs.getDouble("MaintenanceCost"); // raw number
                String formattedCost = (cost == 0) ? "N/A" : currencyFormatter.format(cost); // show n/a instead of 0.0 cuz cleaner

                tableModel.addRow(new Object[]{ // add row to table
                        rs.getInt("ReportID"), // column 0
                        rs.getString("EquipmentName"), // column 1 and so forth
                        rs.getString("IssueType"),
                        rs.getString("Status"),
                        rs.getString("Priority"),
                        rs.getString("ReportedByName"),
                        rs.getDate("ReportDate"),
                        rs.getDate("MaintenanceDate"),
                        formattedCost
                });
            }

        } catch (SQLException e) { // if error, show message why as well
            showErrorDialog("Error Loading Logs", "Could not load maintenance history from database:\n" + e.getMessage());
        }
    }

    private void loadEquipmentDropdown() { // fills equipment dropdown list with data from DB
        equipmentCombo.removeAllItems(); // Clear existing items from dropdown to avoid dupes
        String sql = "SELECT EquipmentID, EquipmentName, Category FROM Equipments ORDER BY EquipmentName";
        // ask db for info of all equipment and sort alphabetically by name, easier to find in the list
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { // loop through results, for every piece of equipment found in DB
                equipmentCombo.addItem(new EquipmentItem( // stores the ID and name/category
                        rs.getInt("EquipmentID"),
                        rs.getString("EquipmentName"),
                        rs.getString("Category")
                ));
            }
        } catch (SQLException e) { // error handling
            showErrorDialog("Error Loading Equipment", "Could not load equipment list:\n" + e.getMessage());
        }
    }

    private void loadStaffDropdown() { // does the same thing on load equipment dropdown, this time for staff members; just different variables
        reportedByCombo.removeAllItems();
        String sql = "SELECT StaffID, FirstName, LastName FROM KitchenStaff WHERE EmploymentStatus = 'Active' ORDER BY FirstName";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reportedByCombo.addItem(new KitchenStaffItem(
                        rs.getInt("StaffID"),
                        rs.getString("FirstName") + " " + rs.getString("LastName")
                ));
            }
        } catch (SQLException e) {
            showErrorDialog("Error Loading Staff", "Could not load staff list:\n" + e.getMessage());
        }
    }

    private void logNewIssue() {
        // retrieve selected values from components like buttons, text boxes, etc
        // also grab equipment & staff id
        EquipmentItem selectedEquipment = (EquipmentItem) equipmentCombo.getSelectedItem();
        String issueType = (String) issueTypeCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        KitchenStaffItem selectedStaff = (KitchenStaffItem) reportedByCombo.getSelectedItem();
        String description = descriptionArea.getText();

        if (selectedEquipment == null || selectedStaff == null) { // input validation
            showMessage("Warning", "Please select an equipment and a reporter.", JOptionPane.WARNING_MESSAGE);
            return; // stop if required fields are missing
        }
        if (description.trim().isEmpty()) { // checks if desc is empty
            showMessage("Warning", "Please add a description for the issue.", JOptionPane.WARNING_MESSAGE);
            return; // if it is empty, stop execution
        }

        // prepares to insert a new row into the table; ? are placeholders for values to be inserted later
        String sql = "INSERT INTO MaintenanceTracker (EquipmentID, IssueType, Priority, ReportDate, Status, Description, ReportedBy) " +
                "VALUES (?, ?, ?, NOW(), 'In Progress', ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // execute; bind values to placeholders
            pstmt.setInt(1, selectedEquipment.getId());
            pstmt.setString(2, issueType);
            pstmt.setString(3, priority);
            pstmt.setString(4, description);
            pstmt.setInt(5, selectedStaff.getId());

            int rowsAffected = pstmt.executeUpdate(); // execute insert command and get number of rows affected (should only be 1, duh)

            if (rowsAffected > 0) { // if true, then success!!!
                showMessage("Success", "New issue logged successfully!", JOptionPane.INFORMATION_MESSAGE);
                loadMaintenanceLogs(); // Refresh the table
                descriptionArea.setText(""); // Clear description text area for next entry
            }

        } catch (SQLException e) { // error handling + details
            showErrorDialog("Error Logging Issue", "Could not save new log to database:\n" + e.getMessage());
        }
    }

    private void openEditDialog() {
        int selectedRow = maintenanceTable.getSelectedRow(); // get index of selected row in table
        if (selectedRow == -1) { // -1 means no selection
            showMessage("No Selection", "Please select a log from the table to edit.", JOptionPane.WARNING_MESSAGE);
            return; // message explains it
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0); // get report id

        EditLogDialog editDialog = new EditLogDialog(this, conn, reportId);
        // create new instance of editlogdialog
        // this: gives the current MaintenanceLog window as the parent.
        // conn: gives the active database connection; allows dialog to read/write data.
        // reportId: gives the specific ID like 1005 of the log entry to be edited.
        editDialog.setVisible(true); // set visible to user

        // After the dialog is closed, refresh the table
        loadMaintenanceLogs();
    }

    private void deleteSelectedLog() {
        int selectedRow = maintenanceTable.getSelectedRow(); // same check as above
        if (selectedRow == -1) {
            showMessage("No Selection", "Please select a log from the table to delete.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // get unique id of report needed for delete command
        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        String equipName = (String) tableModel.getValueAt(selectedRow, 1); // for clarity and confirmation

        int choice = JOptionPane.showConfirmDialog(this, // confirmation dialog
                "Are you sure you want to delete this log?\n\nReport ID: " + reportId + "\nEquipment: " + equipName,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM MaintenanceTracker WHERE ReportID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // delete command
                pstmt.setInt(1, reportId);
                int rowsAffected = pstmt.executeUpdate(); // actually deletes it
                if (rowsAffected > 0) { // checks if deleted
                    showMessage("Success", "Log " + reportId + " deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                    loadMaintenanceLogs(); // Refresh table to remove deleted row from gui
                }
            } catch (SQLException e) { // error handling
                showErrorDialog("Delete Error", "Could not delete log:\n" + e.getMessage());
            }
        }
    }

    // methods for showing messages and errors
    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private class EquipmentItem {
        private int id;
        private String name;
        private String category;

        public EquipmentItem(int id, String name, String category) { // new equipment item object
            this.id = id; this.name = name; this.category = category;
        }

        public int getId() { return id; } // getter
        public String getName() { return name; } // getter
        public String getCategory() { return category; } // getter

        @Override
        public String toString() { return name; } // displays the name
    }

    private class KitchenStaffItem {
        private int id;
        private String name;

        public KitchenStaffItem(int id, String name) { // create new staff item
            this.id = id; this.name = name;
        }

        public int getId() { return id; } // gets staffid

        @Override
        public String toString() { return name; } // display the name
    }

    public static void main(String[] args) { // the main code woahhh
        SwingUtilities.invokeLater(() -> {
            MaintenanceLog standaloneWindow = new MaintenanceLog(); // create instance of maintenancelog window
            standaloneWindow.setVisible(true); // make it visible
        });
    }
}

class EditLogDialog extends JDialog {

    private Connection conn;
    private int reportId;

    // Fonts
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);

    // hold data for user modification
    private JComboBox<String> statusCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<KitchenStaffItem> reportedByCombo;
    private JTextField costField;
    private JTextField maintDateField; // For YYYY-MM-DD
    private JTextArea descriptionArea;
    private JButton updateButton;
    private JButton cancelButton;

    public EditLogDialog(JFrame parent, Connection conn, int reportId) { // the dialog box that allows the user to edit a row's data
        super(parent, "Edit Maintenance Log (ID: " + reportId + ")", true); // 'true' makes it modal;
        // meaning the user MUST CLOSE THIS DIALOG BOX before interacting with the main window again
        this.conn = conn;
        this.reportId = reportId;

        setSize(600, 500);
        setLayout(new BorderLayout()); // layout management
        setLocationRelativeTo(parent); // center dialog box over main window

        JPanel formPanel = new JPanel(new GridBagLayout()); // the dialog box
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Status
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Status:") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        statusCombo = new JComboBox<>(new String[]{"In Progress", "Resolved"});
        statusCombo.setFont(FONT_COMPONENT);
        formPanel.add(statusCombo, gbc);

        // Row 1: Priority
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Priority:") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityCombo.setFont(FONT_COMPONENT);
        formPanel.add(priorityCombo, gbc);

        // Row 2: Reported By
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Reported By:") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        reportedByCombo = new JComboBox<>(); // filled with loadstaffdropdown() peopple
        reportedByCombo.setFont(FONT_COMPONENT);
        formPanel.add(reportedByCombo, gbc);

        // Row 3: Maintenance Cost
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Maint. Cost:") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        costField = new JTextField();
        costField.setFont(FONT_COMPONENT);
        formPanel.add(costField, gbc);

        // Row 4: Maintenance Date
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Maint. Date (YYYY-MM-DD):") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        maintDateField = new JTextField();
        maintDateField.setFont(FONT_COMPONENT);
        formPanel.add(maintDateField, gbc);

        // Row 5: Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:") {{ setFont(FONT_LABEL); }}, gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setFont(FONT_COMPONENT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea); // scroll bar for lengthy descriptions
        formPanel.add(descScrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        // buttons on the BOTTOM RIGHT of the panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(230, 230, 230));
        updateButton = new JButton("Update");
        updateButton.setFont(FONT_LABEL);
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(FONT_LABEL);
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose()); // close the dialog
        updateButton.addActionListener(e -> saveChanges()); // save changes, duh

        loadStaffDropdown(); // load staff
        loadLogData(); // load other data associated with said row
    }

    private void loadLogData() {
        String sql = "SELECT * FROM MaintenanceTracker WHERE ReportID = ?"; // makes sure we get a specific row.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reportId); // set ? to the report id
            ResultSet rs = pstmt.executeQuery(); // execute the query and get the results

            if (rs.next()) { // if a row was returned
                statusCombo.setSelectedItem(rs.getString("Status")); //  set status dropdown to match database value to match item on list
                priorityCombo.setSelectedItem(rs.getString("Priority")); // same here except priority
                costField.setText(String.valueOf(rs.getDouble("MaintenanceCost"))); // same here except it converts the double from the DB to a string
                descriptionArea.setText(rs.getString("Description")); // set multilines description text area

                Date maintDate = rs.getDate("MaintenanceDate"); // handles date and null handling
                if (maintDate != null) { // format it into the format required if date exists
                    maintDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(maintDate));
                } else {
                    // If the date is null, leave the date field blank for the user to fill up
                    maintDateField.setText("");
                }

                // Set 'Reported By' Combo
                int reportedById = rs.getInt("ReportedBy"); // get staffid of user who reported
                for (int i = 0; i < reportedByCombo.getItemCount(); i++) {
                    if (reportedByCombo.getItemAt(i).getId() == reportedById) { // check if id matches from db
                        reportedByCombo.setSelectedIndex(i); // if matches, set this item to be currently selected one in dropdown
                        break; // stop searching when match found
                    }
                }
            }
        } catch (SQLException e) { // error handling
            JOptionPane.showMessageDialog(this, "Error loading log data:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStaffDropdown() { // populates the reported by dropdown with active kitchen staff
        reportedByCombo.removeAllItems(); // clear any items in case called more than once
        String sql = "SELECT StaffID, FirstName, LastName FROM KitchenStaff WHERE EmploymentStatus = 'Active' ORDER BY FirstName";
        // fetches info, ordering them by first name for easier user selection
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { // for each staff member
                reportedByCombo.addItem(new KitchenStaffItem(// extract the staff id and concatenate the name
                        rs.getInt("StaffID"),
                        rs.getString("FirstName") + " " + rs.getString("LastName")
                ));
            }
        } catch (SQLException e) { // error handling again
            JOptionPane.showMessageDialog(this, "Error loading staff list:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() { // handles input validation and performs the final db update
        // Get values from the forms
        String status = (String) statusCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        KitchenStaffItem staff = (KitchenStaffItem) reportedByCombo.getSelectedItem();
        String description = descriptionArea.getText();

        double cost;
        try { // checks for input error;  attempt to change characters in cost field into a double
            cost = Double.parseDouble(costField.getText());
        } catch (NumberFormatException e) { // error handling for texts n such
            JOptionPane.showMessageDialog(this, "Invalid cost. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.sql.Date maintSqlDate = null;
        String dateText = maintDateField.getText().trim();
        if (!dateText.isEmpty()) { // if field isnt empty (not NULL)
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // strict parser for format
                sdf.setLenient(false); // Strict parsing; strict adherance to format
                Date utilDate = sdf.parse(dateText); // user's string into java date object
                maintSqlDate = new java.sql.Date(utilDate.getTime()); // Convert to java.sql.Date
            } catch (ParseException e) { // erorr handling
                JOptionPane.showMessageDialog(this, "Invalid date. Please use YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (staff == null) { // checks if combo box item is actually selected
            JOptionPane.showMessageDialog(this, "Please select a 'Reported By' staff.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // run DB update
        String sql = "UPDATE MaintenanceTracker SET " +
                "Status = ?, Priority = ?, ReportedBy = ?, " +
                "MaintenanceCost = ?, MaintenanceDate = ?, Description = ? " +
                "WHERE ReportID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // bind user values to placeholder question marks
            pstmt.setString(1, status);
            pstmt.setString(2, priority);
            pstmt.setInt(3, staff.getId());
            pstmt.setDouble(4, cost);

            if (maintSqlDate != null) { // Set the Date object if it exists
                pstmt.setDate(5, maintSqlDate);
            } else { // set a SQL NULL value.
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setString(6, description); // specify which row to update
            pstmt.setInt(7, reportId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) { // check if update was successful
                JOptionPane.showMessageDialog(this, "Update Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close this dialog now that changes are saved
            }
        } catch (SQLException e) { //error handling
            JOptionPane.showMessageDialog(this, "Error saving changes:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class KitchenStaffItem { // display name and also stores id
        private int id;
        private String name;

        public KitchenStaffItem(int id, String name) {
            this.id = id; this.name = name;
        }

        public int getId() { return id; } // getter

        @Override
        public String toString() { return name; } // displays name and nothing else
    }
}