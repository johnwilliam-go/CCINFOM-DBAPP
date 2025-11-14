import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;



public class EditQueueStatus extends JFrame implements ActionListener {
    // Database connection constants
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    private JTable orderTable;
    private JButton updateButton, refreshButton, backButton;
    private DefaultTableModel model;
    private MainMenu mainMenu; // to go back

    public EditQueueStatus(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        setTitle("Manage Active Orders");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new javax.swing.table.DefaultTableModel(
                new String[]{"OrderEntryID", "Customer", "OrderType", "Status"},

                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent user from editing or changing any columns manually
            }
        };

        orderTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();

        //Add Title Bar
        JLabel title = new JLabel("Manage Active Orders", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        updateButton = new JButton("Update Status");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back to Menu");

        buttonPanel.add(updateButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        updateButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);

        setVisible(true);
        loadActiveOrders();

    }

    private void loadActiveOrders() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            String sql =
                    "SELECT ent.KOTItemID, kot.CustomerName, kot.OrderType, ent.OrderStatus " +
                            "FROM KitchenOrderTicket kot " +
                            "JOIN OrderEntries ent ON kot.KotID = ent.KotID " +
                            "WHERE ent.OrderStatus = 'In The Kitchen'";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderEntryID = rs.getInt("KOTItemID");
                String customer = rs.getString("CustomerName");
                String orderType = rs.getString("OrderType");
                String status = rs.getString("OrderStatus");
                model.addRow(new Object[]{orderEntryID, customer, orderType, status});
            }

            rs.close();
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No active orders found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }

        String currentStatus = (String) model.getValueAt(selectedRow, 3);
        String nextStatus = getNextStatus(currentStatus);

        if (nextStatus.equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This order is already completed!");
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
                JOptionPane.showMessageDialog(this, "No matching KotID found for this entry.");
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

            JOptionPane.showMessageDialog(this,
                    "Order #" + orderEntryID + " has been marked as " + nextStatus + ".");
            loadActiveOrders();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
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
            this.dispose();
            mainMenu.setVisible(true);

        }else if (e.getSource() == refreshButton) {
            loadActiveOrders();
        }
        else if (e.getSource() == updateButton) {
            updateOrderStatus();
        }


    }
    // For standalone testing only
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EditQueueStatus(null).setVisible(true);
        });
    }

    //DB correction: preptime must be in the orderentries not kitchenorder ticket

    //DB changes: considering to remove usage time.


    // my comments...
    //subtract ordertime and ordercompleted and you will have your preperation time in the orderentries (make it automatic)
    //one of the column should be orderentryid since similar kotid edits status at the same time


}
