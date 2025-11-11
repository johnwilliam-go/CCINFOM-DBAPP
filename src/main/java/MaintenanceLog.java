import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.Date;

public class MaintenanceLog extends JFrame {

    // --- Database Connection ---
    // !!! CHANGE THESE TO YOUR DATABASE !!!
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String DB_USER = "root"; // e.g., "root"
    private static final String DB_PASS = "12345678";

    // This connection is created by this class, not MainMenu
    private Connection conn;

    // --- GUI Components ---
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<EquipmentItem> equipmentCombo;
    private JComboBox<String> issueTypeCombo;
    private JButton submitButton;

    // --- Fonts and Colors ---
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color COLOR_PANEL_BG = new Color(245, 245, 245);
    private static final Border PADDING_BORDER = new EmptyBorder(15, 15, 15, 15);

    // --- Reference to the Main Menu (can be null if run standalone) ---
    private MainMenu parentMenu;

    /**
     * Constructor 1: For Standalone Mode.
     * Called by the main() method in THIS file.
     */
    public MaintenanceLog() {
        super("Maintenance Logging (Standalone Mode)");
        this.parentMenu = null; // No parent menu
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Run the common GUI setup
        initialize();

        // Center on the screen
        setLocationRelativeTo(null);
    }

    /**
     * Constructor 2: For Module Mode.
     * Called by MainMenu.java.
     * @param parentMenu The MainMenu object that is launching this window.
     */
    public MaintenanceLog(MainMenu parentMenu) {
        super("Maintenance Logging System (Janret Galvez)");
        this.parentMenu = parentMenu; // Store the reference
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Add the listener to re-show the MainMenu when this window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // We must also close our connection when this window is disposed
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        System.out.println("MaintenanceLog DB connection closed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                parentMenu.setVisible(true);
            }
        });

        // Run the common GUI setup
        initialize();

        // Center relative to the main menu
        setLocationRelativeTo(parentMenu);
    }

    /**
     * Common initialization method.
     * Contains all the setup code shared by both constructors.
     * This is where the database connection is now made.
     */
    private void initialize() {
        // Set a more modern Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Basic Window Setup ---
        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));

        try {
            // --- 1. Create OWN Database Connection ---
            Class.forName(DB_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("MaintenanceLog DB connection successful.");

            // --- 2. Build the GUI components ---
            initComponents();

            // --- 3. Load initial data from SQL Database ---
            loadMaintenanceLogs();
            loadEquipmentDropdown();

        } catch (ClassNotFoundException e) {
            showErrorDialog("Database Error", "MySQL JDBC Driver not found!");
            System.exit(1);
        } catch (SQLException e) {
            showErrorDialog("Connection Failed", "Could not connect to database:\n" + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred:\n" + e.getMessage());
        }
    }


    /**
     * Creates and lays out all the Swing GUI components.
     */
    private void initComponents() {

        // --- 1. The "View History" Table (CENTER) ---
        String[] columnNames = {"Report ID", "Equipment Name", "Category", "Issue Type", "Report Date", "Status", "Cost"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        maintenanceTable.setFont(FONT_COMPONENT);
        maintenanceTable.setRowHeight(25);
        maintenanceTable.getTableHeader().setFont(FONT_LABEL);

        JScrollPane tableScrollPane = new JScrollPane(maintenanceTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // --- 2. The "Log New Issue" Form (SOUTH) ---
        // ... (This GUI code is identical to the previous in-memory version)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(COLOR_PANEL_BG);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                new EmptyBorder(10, 10, 10, 10), "Log New Issue",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FONT_LABEL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Equipment
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel equipLabel = new JLabel("Equipment:");
        equipLabel.setFont(FONT_LABEL);
        inputPanel.add(equipLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        equipmentCombo = new JComboBox<>();
        equipmentCombo.setFont(FONT_COMPONENT);
        inputPanel.add(equipmentCombo, gbc);

        // Row 1: Issue Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel issueLabel = new JLabel("Issue Type:");
        issueLabel.setFont(FONT_LABEL);
        inputPanel.add(issueLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] issueTypes = {"need cleaning", "repair", "replacement"};
        issueTypeCombo = new JComboBox<>(issueTypes);
        issueTypeCombo.setFont(FONT_COMPONENT);
        inputPanel.add(issueTypeCombo, gbc);

        // Row 2: Submit Button
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        submitButton = new JButton("Submit New Log");
        submitButton.setFont(FONT_LABEL);
        inputPanel.add(submitButton, gbc);

        add(inputPanel, BorderLayout.SOUTH);

        // --- 3. Add Event Listeners (The "Actions") ---
        submitButton.addActionListener(e -> logNewIssue());

        maintenanceTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int selectedRow = maintenanceTable.getSelectedRow();
                    if (selectedRow != -1) {

                        // Get data directly from the table model
                        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
                        String equipName = (String) tableModel.getValueAt(selectedRow, 1);
                        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);

                        // Get cost, handling "N/A"
                        double currentCost = 0.0;
                        Object costObj = tableModel.getValueAt(selectedRow, 6);
                        if(costObj instanceof Number) {
                            currentCost = ((Number) costObj).doubleValue();
                        }

                        if (currentStatus.equals("resolved")) {
                            showMessage("Cannot Update", "This issue is already resolved.", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            openUpdateDialog(reportId, equipName, currentStatus, currentCost);
                        }
                    }
                }
            }
        });

        JLabel helpLabel = new JLabel("  Tip: Double-click a row in the table to mark it as 'resolved' and add a cost.");
        helpLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        add(helpLabel, BorderLayout.NORTH);
    }

    /**
     * OPERATION 1: View Maintenance History (Reads from SQL)
     */
    private void loadMaintenanceLogs() {
        tableModel.setRowCount(0); // Clear existing table

        // Your SQL query based on the proposal
        String sql = "SELECT m.ReportID, e.EquipmentName, e.Category, m.IssueType, m.ReportDate, m.Status, m.MaintenanceCost " +
                "FROM MaintenanceTracker m " +
                "JOIN Equipments e ON m.EquipmentID = e.EquipmentID";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int reportId = rs.getInt("ReportID");
                String equipName = rs.getString("EquipmentName");
                String category = rs.getString("Category");
                String issueType = rs.getString("IssueType");
                Date reportDate = rs.getDate("ReportDate");
                String status = rs.getString("Status");
                double cost = rs.getDouble("MaintenanceCost");

                tableModel.addRow(new Object[]{
                        reportId,
                        equipName,
                        category,
                        issueType,
                        reportDate,
                        status,
                        cost == 0 ? "N/A" : cost
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

        String sql = "SELECT EquipmentID, EquipmentName, Category FROM Equipments";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Create an EquipmentItem (from our inner class) and add it
                EquipmentItem item = new EquipmentItem(
                        rs.getInt("EquipmentID"),
                        rs.getString("EquipmentName"),
                        rs.getString("Category")
                );
                equipmentCombo.addItem(item);
            }

        } catch (SQLException e) {
            showErrorDialog("Error Loading Equipment", "Could not load equipment list from database:\n" + e.getMessage());
        }
    }

    /**
     * OPERATION 2: Log a New Issue (Writes to SQL)
     */
    private void logNewIssue() {
        EquipmentItem selectedEquipment = (EquipmentItem) equipmentCombo.getSelectedItem();
        String issueType = (String) issueTypeCombo.getSelectedItem();

        if (selectedEquipment == null) {
            showMessage("Warning", "Please select an equipment.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use a PreparedStatement to prevent SQL injection
        String sql = "INSERT INTO MaintenanceTracker (EquipmentID, IssueType, ReportDate, Status, MaintenanceCost) " +
                "VALUES (?, ?, ?, 'in Progress', 0.0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedEquipment.getId());
            pstmt.setString(2, issueType);
            pstmt.setDate(3, new java.sql.Date(new Date().getTime())); // Today's date

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showMessage("Success", "New issue logged successfully!", JOptionPane.INFORMATION_MESSAGE);
                loadMaintenanceLogs(); // Refresh the main table
            }

        } catch (SQLException e) {
            showErrorDialog("Error Logging Issue", "Could not save new log to database:\n" + e.getMessage());
        }
    }

    /**
     * OPERATION 3: Update an Issue (Modifies SQL)
     */
    private void openUpdateDialog(int reportId, String equipName, String currentStatus, double currentCost) {

        // 1. Create the new dialog window
        JDialog updateDialog = new JDialog(this, "Update Issue (ID: " + reportId + ")", true);
        updateDialog.setSize(500, 350);
        updateDialog.setLayout(new BorderLayout());
        updateDialog.setLocationRelativeTo(this);

        // 2. Create the main form panel
        // ... (GUI code is identical to in-memory version)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(PADDING_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // -- Row 0: Equipment (Read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel equipLabel = new JLabel("Equipment:");
        equipLabel.setFont(FONT_LABEL);
        formPanel.add(equipLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField equipField = new JTextField(equipName);
        equipField.setFont(FONT_COMPONENT);
        equipField.setEditable(false);
        formPanel.add(equipField, gbc);

        // -- Row 1: Status (Dropdown)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(FONT_LABEL);
        formPanel.add(statusLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"in Progress", "resolved"});
        statusCombo.setFont(FONT_COMPONENT);
        statusCombo.setSelectedItem(currentStatus);
        formPanel.add(statusCombo, gbc);

        // -- Row 2: Maintenance Cost (Text Field)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel costLabel = new JLabel("Maintenance Cost:");
        costLabel.setFont(FONT_LABEL);
        formPanel.add(costLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField costField = new JTextField(String.valueOf(currentCost));
        costField.setFont(FONT_COMPONENT);
        formPanel.add(costField, gbc);

        // 3. Create the bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(230, 230, 230));
        JButton updateButton = new JButton("Update");
        updateButton.setFont(FONT_LABEL);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(FONT_LABEL);
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        // 4. Add Action Listeners for the dialog buttons
        updateButton.addActionListener(e -> {
            try {
                // Get values from the form
                double cost = Double.parseDouble(costField.getText());
                String status = (String) statusCombo.getSelectedItem();

                // --- THIS IS THE SQL UPDATE ---
                String sql = "UPDATE MaintenanceTracker SET Status = ?, MaintenanceCost = ? WHERE ReportID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, status);
                    pstmt.setDouble(2, cost);
                    pstmt.setInt(3, reportId);

                    pstmt.executeUpdate();

                    showMessage("Success", "Update Successful!", JOptionPane.INFORMATION_MESSAGE);
                    updateDialog.dispose(); // Close this window
                    loadMaintenanceLogs(); // Refresh the main table
                } catch (SQLException ex) {
                    showErrorDialog("Database Update Error", "Could not update the log:\n" + ex.getMessage());
                }

            } catch (NumberFormatException ex) {
                showMessage("Input Error", "Invalid cost. Please enter a number (e.g., 150.50).", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> updateDialog.dispose());

        // 5. Add panels to the dialog and make it visible
        updateDialog.add(formPanel, BorderLayout.CENTER);
        updateDialog.add(buttonPanel, BorderLayout.SOUTH);
        updateDialog.setVisible(true);
    }

    // --- Helper methods for showing messages ---
    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Inner class to store Equipment (still useful for the ComboBox).
     * This class maps directly to your Equipments table.
     */
    private class EquipmentItem {
        private int id;
        private String name;
        private String category;

        public EquipmentItem(int id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }

        // This is what the JComboBox displays
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * This main method is for standalone testing.
     * It calls the no-argument constructor.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MaintenanceLog standaloneWindow = new MaintenanceLog();
            standaloneWindow.setVisible(true); // Make it visible
        });
    }
}