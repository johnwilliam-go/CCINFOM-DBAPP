import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;

public class ViewReportsPage extends JPanel implements ActionListener {
    JPanel container, buttonsPanel, datePanel, rightPanel;
    JComboBox<String> yearCombo, monthCombo;
    JButton avgOrderPrepTime, salesReveneue, frequentStation, goBack, mostReportedEquipment;
    Main main;
    JButton viewReports;
    String option = "";
    JTextArea text;
    JTable table;
    DefaultTableModel model;
    JScrollPane scrollPane;


    ViewReportsPage(Main main) {
        this.main = main;
    }

    public void showViewReportsButton() {

        container = new JPanel(null);

        buttonsPanel = new JPanel(null);
        buttonsPanel.setBounds(0,0,250,600);

        avgOrderPrepTime = new JButton("Average preptime");
        avgOrderPrepTime.setBounds(20, 50, 175, 50);
        avgOrderPrepTime.addActionListener(this);
        buttonsPanel.add(avgOrderPrepTime);

        salesReveneue = new JButton("Sales revenue");
        salesReveneue.setBounds(20, 150, 175, 50);
        salesReveneue.addActionListener(this);
        buttonsPanel.add(salesReveneue);

        frequentStation = new JButton("Frequent station");
        frequentStation.setBounds(20, 250, 175, 50);
        frequentStation.addActionListener(this);
        buttonsPanel.add(frequentStation);

        mostReportedEquipment = new JButton("Maintenance Reports");
        mostReportedEquipment.setBounds(20, 350, 175, 50);
        mostReportedEquipment.addActionListener(this);
        buttonsPanel.add(mostReportedEquipment);

        goBack = new JButton("Go Back");
        goBack.setBounds(20, 450, 175, 50);
        goBack.addActionListener(this);
        buttonsPanel.add(goBack);

        rightPanel = new JPanel(null);
        rightPanel.setBounds(250, 30, 500, 450);

        model = new DefaultTableModel();
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(0, 0, 500, 450);
        rightPanel.add(scrollPane);


        container.add(buttonsPanel);
        container.add(rightPanel);

        datePanel = new JPanel(null);
        datePanel.setBounds(300, 500, 500, 100);

        String[] years = new String[20];
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = 0; i < years.length; i++) years[i] = String.valueOf(currentYear-i);
        yearCombo = new JComboBox<>(years);
        yearCombo.setBounds(0, 0, 100, 30);
        datePanel.add(yearCombo);

        String[] months = {"All","1","2","3","4","5","6","7","8","9","10","11","12"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setBounds(120, 0, 100, 30);
        datePanel.add(monthCombo);

        viewReports = new JButton("View Reports");
        viewReports.setBounds(250, 0, 150, 30);
        viewReports.addActionListener(this);
        datePanel.add(viewReports);

        container.add(datePanel);



        text =  new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBounds(0, 0, 500, 450);
        rightPanel.add(scroll);

        main.setContentPane(container);
        main.revalidate();
        main.repaint();
    }

    private void loadTable(String[] columns) {
        model.setColumnIdentifiers(columns);
        model.setRowCount(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == avgOrderPrepTime) {
            option = "Average Preptime";
        }

        if (e.getSource() == salesReveneue) {
            option = "Sales reveneue";
        }

        if (e.getSource() == frequentStation) {
            option = "Frequent station";
        }

        if (e.getSource() == mostReportedEquipment) {
            option = "mostReportedEquipment";
        }

        if (e.getSource() == goBack) {
            main.showMainmenu();
        }

        if (e.getSource() == viewReports && option.equals("Average Preptime")) {

            loadTable(new String[]{"Item Name", "Average Prep Time"});

            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonthIndex = monthCombo.getSelectedIndex(); // 0 = All, 1 = Jan

            String sql =
                    "SELECT mi.ItemName, SEC_TO_TIME(AVG(TIME_TO_SEC(oe.PreparationTime))) AS AveragePrepTime " +
                            "FROM MenuItems mi LEFT JOIN OrderEntries oe ON oe.ItemID = mi.ItemID " +
                            "AND YEAR(oe.OrderDate) = ? ";

            if (selectedMonthIndex != 0) {
                sql += " AND MONTH(oe.OrderDate) = ? ";
            }

            sql += " GROUP BY mi.ItemID, mi.ItemName ORDER BY AveragePrepTime IS NULL, AveragePrepTime DESC";


            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
                    "root",
                    "12345678"
            );
                 PreparedStatement s = conn.prepareStatement(sql)) {

                s.setInt(1, selectedYear);
                if (selectedMonthIndex != 0) s.setInt(2, selectedMonthIndex);

                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString("ItemName"), rs.getString("AveragePrepTime")});
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (e.getSource() == viewReports && option.equals("Sales reveneue")) {
            DecimalFormat df = new DecimalFormat("0.00");
            loadTable(new String[]{"Item Name", "Qty Sold", "Revenue"});
            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonth = monthCombo.getSelectedIndex(); // 0 = All


            String sql = """
                (
                    SELECT 
                        mi.ItemName AS ItemName,
                        SUM(oe.Quantity) AS TotalSold,
                        SUM(oe.Quantity * mi.Priceperitem) AS Revenue
                    FROM MenuItems mi
                    LEFT JOIN OrderEntries oe 
                        ON mi.ItemID = oe.ItemID
                        AND YEAR(oe.OrderDate) = ?
                        AND (? = 0 OR MONTH(oe.OrderDate) = ?)
                    GROUP BY mi.ItemID, mi.ItemName
                )
                UNION ALL
                (
                    SELECT 
                        'TOTAL' AS ItemName,
                        NULL AS TotalSold,
                        SUM(oe.Quantity * mi.Priceperitem) AS Revenue
                    FROM MenuItems mi
                    JOIN OrderEntries oe 
                        ON mi.ItemID = oe.ItemID
                    WHERE YEAR(oe.OrderDate) = ?
                      AND (? = 0 OR MONTH(oe.OrderDate) = ?)
                );
                """;


            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/ccinfomdb", "root", "12345678");
                 PreparedStatement s = conn.prepareStatement(sql)) {

                s.setInt(1, selectedYear);
                s.setInt(2, selectedMonth);
                s.setInt(3, selectedMonth);
                s.setInt(4, selectedYear);
                s.setInt(5, selectedMonth);
                s.setInt(6, selectedMonth);

                ResultSet rs = s.executeQuery();

                while (rs.next()) {
                model.addRow(new Object[]{
                            rs.getString(1),
                            rs.getString(2) == null ? "-" : rs.getString(2),
                            "P" + df.format(rs.getDouble(3)),
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if(e.getSource() == viewReports && option.equals("Frequent station")) {
            loadTable(new String[]{"Station Name", "Usage Count"});
            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonth = monthCombo.getSelectedIndex();

            String sql = """
                SELECT
                    ks.StationName,
                    COUNT(al.StationID) AS UsageCount
                FROM
                    activitylog al
                INNER JOIN
                    kitchenstations ks ON al.StationID = ks.StationID
                WHERE
                    YEAR(al.UsageTime) = ?
                    AND (? = 0 OR MONTH(al.UsageTime) = ?)
                GROUP BY
                    ks.StationID, ks.StationName
                ORDER BY
                    UsageCount DESC;
            """;

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/ccinfomdb", "root", "12345678");
                 PreparedStatement s = conn.prepareStatement(sql)) {

                s.setInt(1, selectedYear);
                s.setInt(2, selectedMonth);
                s.setInt(3, selectedMonth);

                ResultSet rs = s.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getInt(2)
                    });
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if(e.getSource() == viewReports && option.equals("mostReportedEquipment")) {
            loadTable(new String[]{"Equipment Name", "Report Count", "Maintenance Cost"});

            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonth = monthCombo.getSelectedIndex();

            String sql =
                    "SELECT e.EquipmentName, COUNT(m.EquipmentID) AS reportCounts,\n" +
                            "SUM(m.MaintenanceCost) AS totalCost\n" +
                            "FROM maintenancetracker m\n" +
                            "JOIN equipments e ON m.EquipmentID = e.EquipmentID\n" +
                            "WHERE (YEAR(m.reportDate) = ? OR ? = 0)\n" +
                            "AND (MONTH(m.reportDate) = ? OR ? = 0)\n" +
                            "GROUP BY e.EquipmentName"+
                            " UNION ALL " +
                            "SELECT 'TOTAL' AS EquipmentName, COUNT(m.EquipmentID) AS reportCounts, SUM(m.MaintenanceCost) AS totalCost " +
                            "FROM MaintenanceTracker m " +
                            "WHERE (YEAR(m.ReportDate) = ? OR ? = 0) " +
                            "AND (MONTH(m.ReportDate) = ? OR ? = 0)";
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/ccinfomdb", "root", "12345678");
                 PreparedStatement s = conn.prepareStatement(sql)) {

                s.setInt(1, selectedYear);
                s.setInt(2, selectedYear);
                s.setInt(3, selectedMonth);
                s.setInt(4, selectedMonth);
                s.setInt(5, selectedYear);
                s.setInt(6, selectedYear);
                s.setInt(7, selectedMonth);
                s.setInt(8, selectedMonth);

                ResultSet rs = s.executeQuery();

                while (rs.next()) {
                    String name = rs.getString(1);
                    int count = rs.getInt(2);
                    double cost = rs.getDouble(3);

                    model.addRow(new Object[]{
                            rs.getString(1),
                            rs.getInt(2),
                            rs.getFloat(3)
                    });
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fetching report: " + ex.getMessage());
            }
        }
    }
}