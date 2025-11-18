import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.NumberFormat;

public class MaintenanceLog extends JFrame {

// Connection to Database
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
        inputPanel.setBorder(BorderFactory.createTitledBorder( // add border with "log new issue"
                new EmptyBorder(10, 10, 10, 10), "Log New Issue",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FONT_LABEL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Equipment & Priority
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Equipment:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        equipmentCombo = new JComboBox<>();
        equipmentCombo.setFont(FONT_COMPONENT);
        inputPanel.add(equipmentCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Priority:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityCombo.setFont(FONT_COMPONENT);
        priorityCombo.setSelectedItem("Medium"); // Default
        inputPanel.add(priorityCombo, gbc);


        // Row 1: Issue Type & Reported By
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Issue Type:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        issueTypeCombo = new JComboBox<>(new String[]{"Cleaning", "Repair", "Replacement", "Others (In Description)"}); // From proposal
        issueTypeCombo.setFont(FONT_COMPONENT);
        inputPanel.add(issueTypeCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Reported By:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        reportedByCombo = new JComboBox<>();
        reportedByCombo.setFont(FONT_COMPONENT);
        inputPanel.add(reportedByCombo, gbc);

        // Row 2: Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        inputPanel.add(new JLabel("Description:") {{ setFont(FONT_LABEL); }}, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; // Span 3 columns
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 20); // 3 rows
        descriptionArea.setFont(FONT_COMPONENT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        inputPanel.add(descScrollPane, gbc);

        // Row 3: Submit Button
        gbc.gridx = 3; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.weighty = 0.0; gbc.gridwidth = 1;
        submitButton = new JButton("Submit New Log");
        submitButton.setFont(FONT_LABEL);
        inputPanel.add(submitButton, gbc);

        add(inputPanel, BorderLayout.SOUTH);

// --- 3. The new "Actions" Panel (EAST) ---
        JPanel actionPanel = new JPanel();
        // Use a Box Layout to stack buttons vertically
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 0, 10, 10)); // Top, Left, Bottom, Right padding

        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        refreshButton = new JButton("Refresh List");

        // --- Create the Go Back button ---
        backButton = new JButton("Go Back");

        // Set consistent size for buttons
        Dimension buttonSize = new Dimension(150, 40);
        editButton.setMaximumSize(buttonSize);
        deleteButton.setMaximumSize(buttonSize);
        refreshButton.setMaximumSize(buttonSize);
        backButton.setMaximumSize(buttonSize); // Set size

        editButton.setFont(FONT_LABEL);
        deleteButton.setFont(FONT_LABEL);
        refreshButton.setFont(FONT_LABEL);

        // --- Set font for back button (no colors) ---
        backButton.setFont(FONT_LABEL);

        actionPanel.add(editButton);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 10px spacer
        actionPanel.add(deleteButton);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(refreshButton);

        // --- Add a larger spacer and the back button ---
        actionPanel.add(Box.createVerticalStrut(30));
        actionPanel.add(backButton);

        add(actionPanel, BorderLayout.EAST);

        // --- 4. Add Event Listeners (The "Actions") ---
        submitButton.addActionListener(e -> logNewIssue());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedLog());
        refreshButton.addActionListener(e -> loadMaintenanceLogs()); // Simple refresh

        // --- NEW: Add action listener for the back button ---
        backButton.addActionListener(e -> handleGoBack());
    }


    private void handleGoBack() {
        // Just call dispose(). The WindowListeners we set up in the
        // constructors will handle the rest (either showing parent or exiting)
        // and will also close the DB connection.
        dispose();
    }

    /**
     * OPERATION 1: View Maintenance History (Reads from SQL)
     */
    private void loadMaintenanceLogs() {
        tableModel.setRowCount(0); // Clear existing table

        // --- THIS IS THE NEW PART ---
        // Create a currency formatter for Philippine Pesos (₱)
        Locale phLocale = new Locale("en", "PH");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(phLocale);

        String sql = "SELECT m.ReportID, e.EquipmentName, m.IssueType, m.Status, m.Priority, " +
                "CONCAT(k.FirstName, ' ', k.LastName) AS ReportedByName, " +
                "m.ReportDate, m.MaintenanceDate, m.MaintenanceCost " +
                "FROM MaintenanceTracker m " +
                "JOIN Equipments e ON m.EquipmentID = e.EquipmentID " +
                "LEFT JOIN KitchenStaff k ON m.ReportedBy = StaffID " +
                "ORDER BY m.ReportDate DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                double cost = rs.getDouble("MaintenanceCost");
                // Format the cost as ₱ currency. If cost is 0, show "N/A".
                String formattedCost = (cost == 0) ? "N/A" : currencyFormatter.format(cost);

                tableModel.addRow(new Object[]{
                        rs.getInt("ReportID"),
                        rs.getString("EquipmentName"),
                        rs.getString("IssueType"),
                        rs.getString("Status"),
                        rs.getString("Priority"),
                        rs.getString("ReportedByName"),
                        rs.getDate("ReportDate"),
                        rs.getDate("MaintenanceDate"),
                        formattedCost // Use the new formatted string
                });
            }

        } catch (SQLException e) {
            showErrorDialog("Error Loading Logs", "Could not load maintenance history from database:\n" + e.getMessage());
        }
    }

    /**
     * Helper method to populate the Equipment dropdown (Reads from SQL)
     */
    private void loadEquipmentDropdown() {
        equipmentCombo.removeAllItems(); // Clear existing
        String sql = "SELECT EquipmentID, EquipmentName, Category FROM Equipments ORDER BY EquipmentName";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipmentCombo.addItem(new EquipmentItem(
                        rs.getInt("EquipmentID"),
                        rs.getString("EquipmentName"),
                        rs.getString("Category")
                ));
            }
        } catch (SQLException e) {
            showErrorDialog("Error Loading Equipment", "Could not load equipment list:\n" + e.getMessage());
        }
    }

    /**
     * Helper method to populate the "Reported By" dropdown
     */
    private void loadStaffDropdown() {
        reportedByCombo.removeAllItems(); // Clear existing
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

    /**
     * OPERATION 2: Log a New Issue (Writes to SQL)
     */
    private void logNewIssue() {
        EquipmentItem selectedEquipment = (EquipmentItem) equipmentCombo.getSelectedItem();
        String issueType = (String) issueTypeCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        KitchenStaffItem selectedStaff = (KitchenStaffItem) reportedByCombo.getSelectedItem();
        String description = descriptionArea.getText();

        if (selectedEquipment == null || selectedStaff == null) {
            showMessage("Warning", "Please select an equipment and a reporter.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (description.trim().isEmpty()) {
            showMessage("Warning", "Please add a description for the issue.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO MaintenanceTracker (EquipmentID, IssueType, Priority, ReportDate, Status, Description, ReportedBy) " +
                "VALUES (?, ?, ?, NOW(), 'In Progress', ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedEquipment.getId());
            pstmt.setString(2, issueType);
            pstmt.setString(3, priority);
            pstmt.setString(4, description);
            pstmt.setInt(5, selectedStaff.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showMessage("Success", "New issue logged successfully!", JOptionPane.INFORMATION_MESSAGE);
                loadMaintenanceLogs(); // Refresh the main table
                descriptionArea.setText(""); // Clear form
            }

        } catch (SQLException e) {
            showErrorDialog("Error Logging Issue", "Could not save new log to database:\n" + e.getMessage());
        }
    }

    /**
     * OPERATION 3: Update an Issue (Modifies SQL)
     */
    private void openEditDialog() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("No Selection", "Please select a log from the table to edit.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);

        // Launch the dialog
        // We pass 'this' (the MaintenanceLog JFrame) as the parent
        // And the reportId to fetch
        EditLogDialog editDialog = new EditLogDialog(this, conn, reportId);
        editDialog.setVisible(true);

        // After the dialog is closed, refresh the table
        loadMaintenanceLogs();
    }

    /**
     * OPERATION 4: Delete an Issue (Deletes from SQL)
     */
    private void deleteSelectedLog() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("No Selection", "Please select a log from the table to delete.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        String equipName = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this log?\n\nReport ID: " + reportId + "\nEquipment: " + equipName,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM MaintenanceTracker WHERE ReportID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, reportId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showMessage("Success", "Log " + reportId + " deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                    loadMaintenanceLogs(); // Refresh table
                }
            } catch (SQLException e) {
                showErrorDialog("Delete Error", "Could not delete log:\n" + e.getMessage());
            }
        }
    }

    // --- Helper methods for showing messages ---
    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Inner class to store Equipment (for the ComboBox).
     */
    private class EquipmentItem {
        private int id;
        private String name;
        private String category;

        public EquipmentItem(int id, String name, String category) {
            this.id = id; this.name = name; this.category = category;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }

        @Override
        public String toString() { return name; }
    }

    /**
     * Inner class to store Kitchen Staff (for the ComboBox).
     */
    private class KitchenStaffItem {
        private int id;
        private String name;

        public KitchenStaffItem(int id, String name) {
            this.id = id; this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return name; }
    }

    /**
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MaintenanceLog standaloneWindow = new MaintenanceLog();
            standaloneWindow.setVisible(true);
        });
    }
}

/**
 * Separate class for the Edit Dialog.
 * This makes the code much cleaner.
 * It's a JDialog that "pops up" over the main window.
 */
class EditLogDialog extends JDialog {

    private Connection conn;
    private int reportId;

    // Fonts
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);

    // Fields
    private JComboBox<String> statusCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<KitchenStaffItem> reportedByCombo;
    private JTextField costField;
    private JTextField maintDateField; // For YYYY-MM-DD
    private JTextArea descriptionArea;
    private JButton updateButton;
    private JButton cancelButton;

    /**
     * Constructor for the Edit Dialog.
     * @param parent The JFrame (MaintenanceLog) that is opening this dialog.
     * @param conn The database connection.
     * @param reportId The ID of the log to edit.
     */
    public EditLogDialog(JFrame parent, Connection conn, int reportId) {
        super(parent, "Edit Maintenance Log (ID: " + reportId + ")", true); // 'true' makes it modal
        this.conn = conn;
        this.reportId = reportId;

        setSize(600, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // --- 1. Build the Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
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
        reportedByCombo = new JComboBox<>();
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
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descScrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- 2. Build the Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(230, 230, 230));
        updateButton = new JButton("Update");
        updateButton.setFont(FONT_LABEL);
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(FONT_LABEL);
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- 3. Add Listeners ---
        cancelButton.addActionListener(e -> dispose()); // Just close the dialog
        updateButton.addActionListener(e -> saveChanges());

        // --- 4. Load Data ---
        loadStaffDropdown();
        loadLogData();
    }

    /**
     * Loads the existing data for this log from the DB into the form fields.
     */
    private void loadLogData() {
        String sql = "SELECT * FROM MaintenanceTracker WHERE ReportID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                statusCombo.setSelectedItem(rs.getString("Status"));
                priorityCombo.setSelectedItem(rs.getString("Priority"));
                costField.setText(String.valueOf(rs.getDouble("MaintenanceCost")));
                descriptionArea.setText(rs.getString("Description"));

                // Set Maintenance Date (handling NULL)
                Date maintDate = rs.getDate("MaintenanceDate");
                if (maintDate != null) {
                    maintDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(maintDate));
                }

                // Set 'Reported By' Combo
                int reportedById = rs.getInt("ReportedBy");
                for (int i = 0; i < reportedByCombo.getItemCount(); i++) {
                    if (reportedByCombo.getItemAt(i).getId() == reportedById) {
                        reportedByCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading log data:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the staff list for the "Reported By" dropdown.
     */
    private void loadStaffDropdown() {
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
            JOptionPane.showMessageDialog(this, "Error loading staff list:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Runs the UPDATE query to save changes to the database.
     */
    private void saveChanges() {
        // Get values from form
        String status = (String) statusCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        KitchenStaffItem staff = (KitchenStaffItem) reportedByCombo.getSelectedItem();
        String description = descriptionArea.getText();

        // --- Validation ---
        double cost;
        try {
            cost = Double.parseDouble(costField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid cost. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.sql.Date maintSqlDate = null;
        String dateText = maintDateField.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                // Parse java.util.Date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false); // Strict parsing
                Date utilDate = sdf.parse(dateText);
                // Convert to java.sql.Date
                maintSqlDate = new java.sql.Date(utilDate.getTime());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date. Please use YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (staff == null) {
            JOptionPane.showMessageDialog(this, "Please select a 'Reported By' staff.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Run UPDATE ---
        String sql = "UPDATE MaintenanceTracker SET " +
                "Status = ?, Priority = ?, ReportedBy = ?, " +
                "MaintenanceCost = ?, MaintenanceDate = ?, Description = ? " +
                "WHERE ReportID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, priority);
            pstmt.setInt(3, staff.getId());
            pstmt.setDouble(4, cost);

            if (maintSqlDate != null) {
                pstmt.setDate(5, maintSqlDate);
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setString(6, description);
            pstmt.setInt(7, reportId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Update Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close this dialog
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving changes:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inner class for the staff dropdown (needs to be here or in the parent).
     */
    private class KitchenStaffItem {
        private int id;
        private String name;

        public KitchenStaffItem(int id, String name) {
            this.id = id; this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return name; }
    }
}