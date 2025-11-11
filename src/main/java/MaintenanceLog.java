import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MaintenanceLog extends JFrame {

    // --- In-Memory Data Storage (Replaces the Database) ---
    private List<MaintenanceLogEntry> logEntries;
    private List<EquipmentItem> equipmentList;
    private static int nextReportId = 103;

    // --- GUI Components ---
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<EquipmentItem> equipmentCombo;
    private JComboBox<String> issueTypeCombo;
    private JButton submitButton;

    // --- Fonts and Colors (for the new style) ---
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color COLOR_PANEL_BG = new Color(245, 245, 245); // Light gray background
    private static final Border PADDING_BORDER = new EmptyBorder(15, 15, 15, 15);

    public MaintenanceLog() {
        super("Maintenance Logging System");

        // Set a more modern Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Basic Window Setup ---
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Create the mock data
        createMockData();

        // 2. Build the GUI components
        initComponents();

        // 3. Load the mock data into the GUI
        loadMaintenanceLogs();
        loadEquipmentDropdown();

        // Center the window
        setLocationRelativeTo(null);
        // Make the window visible
        setVisible(true);
    }

    /**
     * Initializes all the in-memory data lists.
     */
    private void createMockData() {
        // ... (Same as before)
        logEntries = new ArrayList<>();
        equipmentList = new ArrayList<>();
        EquipmentItem oven = new EquipmentItem(1, "Industrial Oven", "cooking equipment");
        EquipmentItem freezer = new EquipmentItem(2, "Walk-in Freezer", "Ref");
        EquipmentItem fryer = new EquipmentItem(3, "Deep Fryer", "cooking equipment");
        equipmentList.add(oven);
        equipmentList.add(freezer);
        equipmentList.add(fryer);
        logEntries.add(new MaintenanceLogEntry(101, oven, "need cleaning", new Date(), "in Progress", 0.0));
        logEntries.add(new MaintenanceLogEntry(102, fryer, "repair", new Date(), "in Progress", 0.0));
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
        // We now use GridBagLayout for a clean form
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(COLOR_PANEL_BG);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                new EmptyBorder(10, 10, 10, 10), "Log New Issue",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FONT_LABEL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding around components
        gbc.anchor = GridBagConstraints.WEST; // Left-align labels

        // Row 0: Equipment Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel equipLabel = new JLabel("Equipment:");
        equipLabel.setFont(FONT_LABEL);
        inputPanel.add(equipLabel, gbc);

        // Row 0: Equipment ComboBox
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Make component stretch
        equipmentCombo = new JComboBox<>();
        equipmentCombo.setFont(FONT_COMPONENT);
        inputPanel.add(equipmentCombo, gbc);

        // Row 1: Issue Type Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel issueLabel = new JLabel("Issue Type:");
        issueLabel.setFont(FONT_LABEL);
        inputPanel.add(issueLabel, gbc);

        // Row 1: Issue Type ComboBox
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] issueTypes = {"need cleaning", "repair", "replacement"};
        issueTypeCombo = new JComboBox<>(issueTypes);
        issueTypeCombo.setFont(FONT_COMPONENT);
        inputPanel.add(issueTypeCombo, gbc);

        // Row 2: Submit Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST; // Right-align button
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        submitButton = new JButton("Submit New Log");
        submitButton.setFont(FONT_LABEL);
        inputPanel.add(submitButton, gbc);

        add(inputPanel, BorderLayout.SOUTH);

        // --- 3. Add Event Listeners (The "Actions") ---
        submitButton.addActionListener(e -> logNewIssue());

        // This is the new part: Double-click a row to open the UPDATE dialog
        maintenanceTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int selectedRow = maintenanceTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int reportId = (int) tableModel.getValueAt(selectedRow, 0);

                        // Find the log entry in our list
                        MaintenanceLogEntry entryToUpdate = findEntryById(reportId);

                        if (entryToUpdate != null) {
                            if (entryToUpdate.getStatus().equals("resolved")) {
                                JOptionPane.showMessageDialog(MaintenanceLog.this,
                                        "This issue is already resolved.", "Cannot Update", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                // Open the new, clean update dialog
                                openUpdateDialog(entryToUpdate);
                            }
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
     * OPERATION 1: View Maintenance History (Reads from ArrayList)
     */
    private void loadMaintenanceLogs() {
        // ... (Same as before)
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (MaintenanceLogEntry entry : logEntries) {
            tableModel.addRow(new Object[]{
                    entry.getReportId(),
                    entry.getEquipment().getName(),
                    entry.getEquipment().getCategory(),
                    entry.getIssueType(),
                    sdf.format(entry.getReportDate()),
                    entry.getStatus(),
                    entry.getCost() == 0 ? "N/A" : entry.getCost()
            });
        }
    }

    /**
     * Helper method to populate the Equipment dropdown (Reads from ArrayList)
     */
    private void loadEquipmentDropdown() {
        // ... (Same as before)
        equipmentCombo.removeAllItems();
        for (EquipmentItem item : equipmentList) {
            equipmentCombo.addItem(item);
        }
    }

    /**
     * OPERATION 2: Log a New Issue (Adds to ArrayList)
     */
    private void logNewIssue() {
        // ... (Same as before)
        EquipmentItem selectedEquipment = (EquipmentItem) equipmentCombo.getSelectedItem();
        String issueType = (String) issueTypeCombo.getSelectedItem();
        if (selectedEquipment == null) {
            JOptionPane.showMessageDialog(this, "Please select an equipment.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int newId = nextReportId++;
        MaintenanceLogEntry newEntry = new MaintenanceLogEntry(newId, selectedEquipment, issueType, new Date(), "in Progress", 0.0);
        logEntries.add(newEntry);
        JOptionPane.showMessageDialog(this, "New issue logged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadMaintenanceLogs();
    }

    /**
     * OPERATION 3: Update an Issue (Modifies object in ArrayList)
     * This now opens a dedicated JDialog, which is much cleaner.
     */
    private void openUpdateDialog(MaintenanceLogEntry entryToUpdate) {

        // 1. Create the new dialog window
        JDialog updateDialog = new JDialog(this, "Update Issue (ID: " + entryToUpdate.getReportId() + ")", true); // 'true' makes it modal
        updateDialog.setSize(500, 350);
        updateDialog.setLayout(new BorderLayout());
        updateDialog.setLocationRelativeTo(this); // Center it

        // 2. Create the main form panel (like the screenshot)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(PADDING_BORDER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Add form components
        // -- Row 0: Equipment (Read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel equipLabel = new JLabel("Equipment:");
        equipLabel.setFont(FONT_LABEL);
        formPanel.add(equipLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField equipField = new JTextField(entryToUpdate.getEquipment().getName());
        equipField.setFont(FONT_COMPONENT);
        equipField.setEditable(false); // Read-only
        formPanel.add(equipField, gbc);

        // -- Row 1: Status (Dropdown)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(FONT_LABEL);
        formPanel.add(statusLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"in Progress", "resolved"});
        statusCombo.setFont(FONT_COMPONENT);
        statusCombo.setSelectedItem(entryToUpdate.getStatus());
        formPanel.add(statusCombo, gbc);

        // -- Row 2: Maintenance Cost (Text Field)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel costLabel = new JLabel("Maintenance Cost:");
        costLabel.setFont(FONT_LABEL);
        formPanel.add(costLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField costField = new JTextField(String.valueOf(entryToUpdate.getCost()));
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

                // Update the original object
                entryToUpdate.setStatus(status);
                entryToUpdate.setCost(cost);

                JOptionPane.showMessageDialog(updateDialog, "Update Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                updateDialog.dispose(); // Close this window
                loadMaintenanceLogs(); // Refresh the main table

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(updateDialog, "Invalid cost. Please enter a number (e.g., 150.50).", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> updateDialog.dispose()); // Just close the window

        // 5. Add panels to the dialog and make it visible
        updateDialog.add(formPanel, BorderLayout.CENTER);
        updateDialog.add(buttonPanel, BorderLayout.SOUTH);
        updateDialog.setVisible(true);
    }

    // Helper method to find an entry by its ID
    private MaintenanceLogEntry findEntryById(int reportId) {
        for (MaintenanceLogEntry entry : logEntries) {
            if (entry.getReportId() == reportId) {
                return entry;
            }
        }
        return null; // Should not happen if table is correct
    }


    // --- Inner class to store Equipment (replaces Equipments table) ---
    private class EquipmentItem {
        // ... (Same as before)
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

    // --- Inner class to store a Log Entry (replaces MaintenanceTracker table) ---
    private class MaintenanceLogEntry {
        // ... (Same as before)
        private int reportId;
        private EquipmentItem equipment;
        private String issueType;
        private Date reportDate;
        private String status;
        private double cost;
        public MaintenanceLogEntry(int reportId, EquipmentItem equipment, String issueType, Date reportDate, String status, double cost) {
            this.reportId = reportId; this.equipment = equipment; this.issueType = issueType;
            this.reportDate = reportDate; this.status = status; this.cost = cost;
        }
        public int getReportId() { return reportId; }
        public EquipmentItem getEquipment() { return equipment; }
        public String getIssueType() { return issueType; }
        public Date getReportDate() { return reportDate; }
        public String getStatus() { return status; }
        public double getCost() { return cost; }
        public void setStatus(String status) { this.status = status; }
        public void setCost(double cost) { this.cost = cost; }
    }

    /**
     * This is the main method to run the application.
     */
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new MaintenanceLog());
    }
}