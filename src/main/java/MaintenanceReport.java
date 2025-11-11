import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Calendar;

public class MaintenanceReport extends JFrame {

    // --- Database Connection ---
    // These settings are from your MaintenanceLog.java file
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    private Connection conn;

    // --- GUI Components ---
    private JComboBox<String> reportTypeCombo;
    private JComboBox<Integer> yearCombo;
    private JComboBox<String> monthCombo;
    private JButton generateButton;
    private JButton backButton;

    // Report Display Components
    private JTable mostReportedTable;
    private DefaultTableModel mostReportedModel;
    private JTable allLogsTable;
    private DefaultTableModel allLogsModel;
    private JLabel totalCostLabel;

    // --- Fonts and Colors ---
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TOTAL_COST = new Font("Segoe UI", Font.BOLD, 18);
    private static final Color COLOR_PANEL_BG = new Color(245, 245, 245);

    private MainMenu parentMenu;

    /**
     * Constructor 1: For Standalone Mode.
     */
    public MaintenanceReport() {
        super("Equipment Maintenance Report (Standalone Mode)");
        this.parentMenu = null;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Standalone mode: close DB and exit.
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        System.out.println("Report DB connection closed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        initialize();
        setLocationRelativeTo(null);
    }

    /**
     * Constructor 2: For Module Mode.
     * @param parentMenu The MainMenu object that is launching this window.
     */
    public MaintenanceReport(MainMenu parentMenu) {
        super("Equipment Maintenance Report (Janret Galvez)");
        this.parentMenu = parentMenu;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Module mode: close DB and show parent.
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        System.out.println("Report DB connection closed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                parentMenu.setVisible(true);
            }
        });

        initialize();
        setLocationRelativeTo(parentMenu);
    }

    /**
     * Common initialization method.
     */
    private void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));

        try {
            // --- 1. Create OWN Database Connection ---
            Class.forName(DB_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("MaintenanceReport DB connection successful.");

            // --- 2. Build the GUI components ---
            initComponents();

            // --- 3. Load initial data (e.g., "All Time" report) ---
            generateReport();

        } catch (ClassNotFoundException e) {
            showErrorDialog("Database Error", "MySQL JDBC Driver not found!");
            if (parentMenu == null) System.exit(1);
        } catch (SQLException e) {
            showErrorDialog("Connection Failed", "Could not connect to database:\n" + e.getMessage());
            if (parentMenu == null) System.exit(1);
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred:\n" + e.getMessage());
        }
    }

    /**
     * Creates and lays out all the Swing GUI components.
     */
    private void initComponents() {

        // --- 1. The Filter Panel (NORTH) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(COLOR_PANEL_BG);
        filterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        filterPanel.add(new JLabel("Report Type:") {{ setFont(FONT_LABEL); }});
        reportTypeCombo = new JComboBox<>(new String[]{"All Time", "Yearly", "Monthly"});
        reportTypeCombo.setFont(FONT_COMPONENT);
        filterPanel.add(reportTypeCombo);

        filterPanel.add(new JLabel("Year:") {{ setFont(FONT_LABEL); }});
        yearCombo = new JComboBox<>();
        yearCombo.setFont(FONT_COMPONENT);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 2020; i--) {
            yearCombo.addItem(i);
        }
        filterPanel.add(yearCombo);

        filterPanel.add(new JLabel("Month:") {{ setFont(FONT_LABEL); }});
        monthCombo = new JComboBox<>(new String[]{"01 - Jan", "02 - Feb", "03 - Mar", "04 - Apr", "05 - May", "06 - Jun", "07 - Jul", "08 - Aug", "09 - Sep", "10 - Oct", "11 - Nov", "12 - Dec"});
        monthCombo.setFont(FONT_COMPONENT);
        filterPanel.add(monthCombo);

        generateButton = new JButton("Generate Report");
        generateButton.setFont(FONT_LABEL);
        filterPanel.add(generateButton);

        // --- 2. The Go Back Button (EAST) ---
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 0, 10, 10));

        backButton = new JButton("Go Back");
        backButton.setFont(FONT_LABEL);
        backButton.setMaximumSize(new Dimension(150, 40));
        actionPanel.add(backButton);

        // --- 3. The Report Display (CENTER) ---
        String[] mostReportedCols = {"Equipment", "Issue Type", "Total Reports"};
        mostReportedModel = new DefaultTableModel(mostReportedCols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        mostReportedTable = new JTable(mostReportedModel);
        mostReportedTable.setFont(FONT_COMPONENT);
        mostReportedTable.getTableHeader().setFont(FONT_LABEL);
        JScrollPane mostReportedPane = new JScrollPane(mostReportedTable);
        mostReportedPane.setBorder(BorderFactory.createTitledBorder("Most Reported Equipment"));

        String[] allLogsCols = {"Report ID", "Equipment", "Issue Type", "Status", "Report Date", "Maint. Date", "Cost"};
        allLogsModel = new DefaultTableModel(allLogsCols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        allLogsTable = new JTable(allLogsModel);
        allLogsTable.setFont(FONT_COMPONENT);
        allLogsTable.getTableHeader().setFont(FONT_LABEL);
        JScrollPane allLogsPane = new JScrollPane(allLogsTable);
        allLogsPane.setBorder(BorderFactory.createTitledBorder("All Logs in Period"));

        // Use a Split Pane to show both tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mostReportedPane, allLogsPane);
        splitPane.setResizeWeight(0.33); // Give top table 1/3 of the space

        // --- 4. The Total Cost Display (SOUTH) ---
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBackground(COLOR_PANEL_BG);
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        totalCostLabel = new JLabel("Total Cost of Resolved Maintenance: ");
        totalCostLabel.setFont(FONT_TOTAL_COST);
        summaryPanel.add(totalCostLabel);

        // --- 5. Add all panels to frame ---
        add(filterPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
        add(actionPanel, BorderLayout.EAST);

        // --- 6. Add Listeners ---
        reportTypeCombo.addActionListener(e -> updateFilterControls());
        generateButton.addActionListener(e -> generateReport());
        backButton.addActionListener(e -> handleGoBack());

        // Initialize filter controls
        updateFilterControls();
    }

    /**
     * Hides/shows the Month/Year dropdowns based on the Report Type.
     */
    private void updateFilterControls() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        if (reportType == null) return;

        if (reportType.equals("Monthly")) {
            yearCombo.setEnabled(true);
            monthCombo.setEnabled(true);
        } else if (reportType.equals("Yearly")) {
            yearCombo.setEnabled(true);
            monthCombo.setEnabled(false);
        } else { // All Time
            yearCombo.setEnabled(false);
            monthCombo.setEnabled(false);
        }
    }

    /**
     * Handles the "Go Back" button click.
     */
    private void handleGoBack() {
        // Just call dispose(). The WindowListeners set up in the
        // constructors will handle the rest (showing parent or exiting)
        // and will also close the DB connection.
        dispose();
    }

    /**
     * The main logic. Fetches all report data from the database.
     */
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        int year = (int) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1; // 1-based index (1=Jan, 12=Dec)

        // Clear all old data
        mostReportedModel.setRowCount(0);
        allLogsModel.setRowCount(0);
        totalCostLabel.setText("Total Cost of Resolved Maintenance: ");

        // We will build a dynamic WHERE clause
        String whereClause = "";
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (reportType.equals("Monthly")) {
            whereClause = "WHERE YEAR(m.ReportDate) = ? AND MONTH(m.ReportDate) = ?";
            params.add(year);
            params.add(month);
        } else if (reportType.equals("Yearly")) {
            whereClause = "WHERE YEAR(m.ReportDate) = ?";
            params.add(year);
        }
        // If "All Time", whereClause remains "" and params is empty.

        try {
            // Run all three report queries
            loadMostReported(whereClause, params);
            loadAllLogs(whereClause, params);
            loadTotalCost(whereClause.replace("m.", ""), params); // Cost query only uses one table

        } catch (SQLException e) {
            showErrorDialog("Report Generation Failed", "Error running SQL queries:\n" + e.getMessage());
        }
    }

    /**
     * Query 1: Fulfills "Most reported equipment"
     */
    private void loadMostReported(String whereClause, java.util.List<Object> params) throws SQLException {
        String sql = "SELECT e.EquipmentName, m.IssueType, COUNT(*) as ReportCount " +
                "FROM MaintenanceTracker m " +
                "JOIN Equipments e ON m.EquipmentID = e.EquipmentID " +
                whereClause + // Add the dynamic filter
                " GROUP BY e.EquipmentName, m.IssueType " +
                "ORDER BY ReportCount DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set query parameters (if any)
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mostReportedModel.addRow(new Object[]{
                        rs.getString("EquipmentName"),
                        rs.getString("IssueType"),
                        rs.getInt("ReportCount")
                });
            }
        }
    }

    /**
     * Query 2: Shows the detailed log for the period.
     */
    private void loadAllLogs(String whereClause, java.util.List<Object> params) throws SQLException {
        String sql = "SELECT m.ReportID, e.EquipmentName, m.IssueType, m.Status, m.ReportDate, m.MaintenanceDate, m.MaintenanceCost " +
                "FROM MaintenanceTracker m " +
                "JOIN Equipments e ON m.EquipmentID = e.EquipmentID " +
                whereClause + // Add the dynamic filter
                " ORDER BY m.ReportDate DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set query parameters (if any)
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                allLogsModel.addRow(new Object[]{
                        rs.getInt("ReportID"),
                        rs.getString("EquipmentName"),
                        rs.getString("IssueType"),
                        rs.getString("Status"),
                        rs.getDate("ReportDate"),
                        rs.getDate("MaintenanceDate"),
                        rs.getDouble("MaintenanceCost") == 0 ? "N/A" : rs.getDouble("MaintenanceCost")
                });
            }
        }
    }

    /**
     * Query 3: Fulfills "Total cost of equipment maintenance"
     */
    private void loadTotalCost(String whereClause, java.util.List<Object> params) throws SQLException {
        // We add an extra condition: only sum costs for *Resolved* issues.
        String costWhereClause = (whereClause.isEmpty() ? "WHERE" : whereClause + " AND") + " Status = 'Resolved'";

        String sql = "SELECT SUM(MaintenanceCost) as TotalCost " +
                "FROM MaintenanceTracker m " + // 'm' alias for consistency
                costWhereClause;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set query parameters (if any)
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double totalCost = rs.getDouble("TotalCost");
                Locale phLocale = new Locale("en", "PH");
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(phLocale);

                totalCostLabel.setText("Total Cost of Resolved Maintenance: " + currencyFormatter.format(totalCost));
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
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MaintenanceReport standaloneWindow = new MaintenanceReport();
            standaloneWindow.setVisible(true);
        });
    }
}