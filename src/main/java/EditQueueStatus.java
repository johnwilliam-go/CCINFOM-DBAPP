import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class EditQueueStatus implements ActionListener {
    // Database connection for MySQL
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    private JTable orderTable;
    private JPanel editQueueStatusPanel;
    private JButton updateButton, refreshButton, backButton, showOrderHistory, deleteButton;
    private DefaultTableModel model;
    private Main main; // to go back

    public EditQueueStatus(Main main) {
        this.main = main;
    }

    /**
     * create Jtable for display order info, and load ALL active orders,
     * Adds all action buttons (update, refresh, delete, history)
     */
    public void showEditQueueStatus() {
        editQueueStatusPanel = new JPanel(new BorderLayout());
        editQueueStatusPanel.setSize(800, 600);

        model = new javax.swing.table.DefaultTableModel(
                new String[]{"OrderEntryID", "Customer", "Ordered Item", "Quantity", "Assigned Staff", "OrderType", "Status"},

                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent user from editing or changing any columns manually
            }
        };

        orderTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        editQueueStatusPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();

        //Add Title Bar
        JLabel title = new JLabel("Manage Active Orders", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        editQueueStatusPanel.add(title, BorderLayout.NORTH);

        updateButton = new JButton("Update Status");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back to Menu");
        showOrderHistory = new JButton("Show Order History");
        deleteButton = new JButton("Delete Order");

        buttonPanel.add(updateButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        buttonPanel.add(showOrderHistory);
        buttonPanel.add(deleteButton);
        editQueueStatusPanel.add(buttonPanel, BorderLayout.SOUTH);

        updateButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);
        showOrderHistory.addActionListener(this);
        deleteButton.addActionListener(this);


        loadActiveOrders();

        main.setContentPane(editQueueStatusPanel);
        main.revalidate();
        main.repaint();
    }
    /**
     * Loads all active orders (OrderStatus = 'In The Kitchen'),displays them in the JTable.
     * This method is called on page load and every time we refresh.
     */

    private void loadActiveOrders() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT \n" +
                    "    ent.KOTItemID,\n" +
                    "    kot.CustomerName,\n" +
                    "    kot.OrderType,\n" +
                    "    mi.ItemName,\n" +
                    "    ent.Quantity,\n" +
                    "    ent.OrderStatus,\n" +
                    "    CONCAT(ks.FirstName, ' ', ks.LastName) AS AssignedStaff\n" +
                    "FROM KitchenOrderTicket kot\n" +
                    "JOIN OrderEntries ent ON kot.KotID = ent.KotID\n" +
                    "JOIN MenuItems mi ON ent.ItemID = mi.ItemID\n" +
                    "LEFT JOIN KitchenStaff ks ON ent.PreparedBy = ks.StaffID\n" +
                    "WHERE ent.OrderStatus = 'In The Kitchen'";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderEntryID = rs.getInt("KotItemID");
                String customer = rs.getString("CustomerName");
                String orderType = rs.getString("OrderType");
                String itemName = rs.getString("ItemName");
                int quantity = rs.getInt("Quantity");
                String status = rs.getString("OrderStatus");
                String assignedStaff = rs.getString("AssignedStaff");

                model.addRow(new Object[]{orderEntryID, customer, itemName,quantity, assignedStaff, orderType, status});
            }

            rs.close();
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(editQueueStatusPanel, "No active orders found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Database error: " + e.getMessage());
        }
    }

    /**
     * updating an order's status to 'Completed',
     * Update OrderEntries, insert into ActivityLog for history tracking
     */
    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Please select an order first.");
            return;
        }

        String currentStatus = (String) model.getValueAt(selectedRow, 6);
        String nextStatus = getNextStatus(currentStatus);

        if (nextStatus.equals(currentStatus)) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "This order is already completed!");
            return;
        }

        int orderEntryID = (int) model.getValueAt(selectedRow, 0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            int kotID = -1;
            ResultSet rsKot = stmt.executeQuery("SELECT KotID FROM OrderEntries WHERE KOTItemID = " + orderEntryID);
            if (rsKot.next()) {
                kotID = rsKot.getInt("KotID");
            }
            rsKot.close();

            if (kotID == -1) {
                JOptionPane.showMessageDialog(editQueueStatusPanel, "No matching KotID found for this entry.");
                return;
            }

            String updateOrder =
                    "UPDATE OrderEntries SET OrderStatus = '" + nextStatus + "', TimeCompleted = NOW() " +
                            "WHERE KOTItemID = " + orderEntryID;
            stmt.executeUpdate(updateOrder);

            String insertLog =
                    "INSERT INTO ActivityLog (KotID, OrderEntryID, ItemID, Quantity, StationID, UsageTime) " +
                            "SELECT e.KotID, e.KOTItemID, e.ItemID, e.Quantity, m.StationID, NOW() " +
                            "FROM OrderEntries e JOIN MenuItems m ON e.ItemID = m.ItemID " +
                            "WHERE e.KOTItemID = " + orderEntryID;
            stmt.executeUpdate(insertLog);

            String sql2 =
                    "UPDATE OrderEntries oe\n" +
                    "JOIN KitchenOrderTicket kot ON oe.KotID = kot.KotID\n" +
                    "SET oe.PreparationTime = \n" +
                    "    SEC_TO_TIME(TIMESTAMPDIFF(SECOND, kot.OrderTime, oe.TimeCompleted))\n" +
                    "WHERE oe.TimeCompleted IS NOT NULL";
            stmt.executeUpdate(sql2);

            JOptionPane.showMessageDialog(editQueueStatusPanel,
                    "Order #" + orderEntryID + " has been marked as " + nextStatus + ".");
            loadActiveOrders();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Database error: " + ex.getMessage());
        }
    }
    /**
     * Loads the full list of all orders (active and completed).
     */
    private void showOrdersHistory() throws SQLException {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT \n" +
                    "    ent.KOTItemID,\n" +
                    "    kot.CustomerName,\n" +
                    "    kot.OrderType,\n" +
                    "    mi.ItemName,\n" +
                    "    ent.Quantity,\n" +
                    "    ent.OrderStatus,\n" +
                    "    CONCAT(ks.FirstName, ' ', ks.LastName) AS AssignedStaff\n" +
                    "FROM KitchenOrderTicket kot\n" +
                    "JOIN OrderEntries ent ON kot.KotID = ent.KotID\n" +
                    "JOIN MenuItems mi ON ent.ItemID = mi.ItemID\n" +
                    "LEFT JOIN KitchenStaff ks ON ent.PreparedBy = ks.StaffID\n";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderEntryID = rs.getInt("KotItemID");
                String customer = rs.getString("CustomerName");
                String orderType = rs.getString("OrderType");
                String itemName = rs.getString("ItemName");
                int quantity = rs.getInt("Quantity");
                String status = rs.getString("OrderStatus");
                String assignedStaff = rs.getString("AssignedStaff");

                model.addRow(new Object[]{orderEntryID, customer, itemName,quantity, assignedStaff, orderType, status});
            }


            rs.close();
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(editQueueStatusPanel, "No active orders found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Database error: " + e.getMessage());
        }
    }

    /**
     * deteles a mistakenly entered order entry
     */
    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Please select an order to delete.");
            return;
        }

        int orderEntryID = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                editQueueStatusPanel,
                "Are you sure you want to delete OrderEntryID #" + orderEntryID + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            conn.setAutoCommit(false); // start transaction

            //delete activity log first (because of FK)
            String deleteLog = "DELETE FROM ActivityLog WHERE OrderEntryID = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteLog)) {
                ps.setInt(1, orderEntryID);
                ps.executeUpdate();
            }

            //Get KOT ID before deleting the entry
            int kotID = -1;
            String fetchKot = "SELECT KotID FROM OrderEntries WHERE KOTItemID = ?";
            try (PreparedStatement ps = conn.prepareStatement(fetchKot)) {
                ps.setInt(1, orderEntryID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) kotID = rs.getInt("KotID");
            }

            //Delete order entry
            String deleteEntry = "DELETE FROM OrderEntries WHERE KOTItemID = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteEntry)) {
                ps.setInt(1, orderEntryID);
                ps.executeUpdate();
            }

            //If this was last entry in kot, delete the KitchenOrderTicket
            if (kotID != -1) {
                String count = "SELECT COUNT(*) FROM OrderEntries WHERE KotID = ?";
                try (PreparedStatement ps = conn.prepareStatement(count)) {
                    ps.setInt(1, kotID);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {

                        String deleteTicket = "DELETE FROM KitchenOrderTicket WHERE KotID = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(deleteTicket)) {
                            ps2.setInt(1, kotID);
                            ps2.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();

            JOptionPane.showMessageDialog(editQueueStatusPanel,
                    "OrderEntryID #" + orderEntryID + " has been deleted.");

            loadActiveOrders(); // refresh table

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(editQueueStatusPanel, "Delete failed: " + ex.getMessage());
        }
    }

    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case "In The Kitchen":
                return "Completed";
            default:
                return currentStatus;
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            main.showMainmenu();

        } else if (e.getSource() == refreshButton) {
            loadActiveOrders();

        } else if (e.getSource() == updateButton) {
            updateOrderStatus();

        } else if (e.getSource() == deleteButton) {
            deleteOrder();

        } else if (e.getSource() == showOrderHistory) {
            try {
                showOrdersHistory();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
