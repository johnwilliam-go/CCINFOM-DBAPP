import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ViewReportsPage extends JPanel implements ActionListener {
    JPanel container, buttonsPanel, datePanel, rightPanel;
    CardLayout cardLayout;
    JComboBox<String> yearCombo, monthCombo;
    JButton avgOrderPrepTime, salesReveneue, frequentStation, goBack;
    Main main;
    MaintenanceLog maintenanceLog;
    MaintenanceReport maintenanceReport;
    JButton viewReports;
    String option = "";
    JTextArea text;


    ViewReportsPage(Main main) {
        this.main = main;
    }

    public void showViewReportsButton() {

        container = new JPanel(new BorderLayout());

        buttonsPanel = new JPanel(null);
        buttonsPanel.setPreferredSize(new Dimension(200, 600));

        avgOrderPrepTime = new JButton("Average preptime");
        avgOrderPrepTime.setBounds(0, 100, 150, 50);
        avgOrderPrepTime.addActionListener(this);
        buttonsPanel.add(avgOrderPrepTime);

        salesReveneue = new JButton("Sales reveneue");
        salesReveneue.setBounds(0, 200, 150, 50);
        salesReveneue.addActionListener(this);
        buttonsPanel.add(salesReveneue);

        frequentStation = new JButton("Frequent station");
        frequentStation.setBounds(0, 300, 150, 50);
        frequentStation.addActionListener(this);
        buttonsPanel.add(frequentStation);

        goBack = new JButton("Go Back");
        goBack.setBounds(0, 400, 150, 50);
        goBack.addActionListener(this);
        buttonsPanel.add(goBack);

        rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBackground(Color.BLACK);
        rightPanel.setPreferredSize(new Dimension(500, 500));


        container.add(buttonsPanel, BorderLayout.WEST);
        container.add(rightPanel, BorderLayout.CENTER);

        datePanel = new JPanel();
        datePanel.setPreferredSize(new Dimension(500, 100));

        String[] years = new String[20];
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = 0; i < years.length; i++) years[i] = String.valueOf(currentYear-i);
        yearCombo = new JComboBox<>(years);
        yearCombo.setBounds(0, 0, 150, 30);
        datePanel.add(yearCombo);

        String[] months = {"All","1","2","3","4","5","6","7","8","9","10","11","12"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setBounds(150, 0, 150, 30);
        datePanel.add(monthCombo);

        viewReports = new JButton("View Reports");
        viewReports.setBounds(300, 0, 150, 30);
        viewReports.addActionListener(this);
        datePanel.add(viewReports);

        container.add(datePanel, BorderLayout.SOUTH);




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

        if (e.getSource() == goBack) {
            main.showMainmenu();
        }

        if (e.getSource() == viewReports && option.equals("Average Preptime")) {
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

            StringBuilder message = new StringBuilder("Average Preparation Time per Item:\n\n");

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
                        String name = rs.getString("ItemName");
                        String avgTime = rs.getString("AveragePrepTime");
                        if (avgTime == null) avgTime = "N/A";
                        message.append(String.format("%-25s : %s%n", name, avgTime));
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                message = new StringBuilder("Error retrieving data: " + ex.getMessage());
            }
            text.setText(message.toString());
        }

        if (e.getSource() == viewReports && option.equals("Sales reveneue")) {
            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonthIndex = monthCombo.getSelectedIndex(); // 0 = All
            int selectedMonth = selectedMonthIndex; // if "All", keep 0

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

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-25s %-10s %s%n", "Item Name", "Qty Sold", "Revenue"));
                sb.append("--------------------------------------------------\n");

                while (rs.next()) {
                    String name = rs.getString("ItemName");
                    String qty = rs.getString("TotalSold");
                    String revenue = rs.getString("Revenue");

                    if (qty == null) qty = "-";   // for TOTAL row
                    if (revenue == null) revenue = "0";

                    sb.append(String.format("%-25s %-10s ₱%s%n", name, qty, revenue));
                }

                text.setText(sb.toString());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if(e.getSource() == viewReports && option.equals("Frequent station")) {
            int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
            int selectedMonthIndex = monthCombo.getSelectedIndex();
            int selectedMonth = selectedMonthIndex;

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

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-25s %s%n", "Station Name", "Usage Count"));
                sb.append("----------------------------------------\n");

                while (rs.next()) {
                    String station = rs.getString("StationName");
                    int count = rs.getInt("UsageCount");
                    sb.append(String.format("%-25s %d%n", station, count));
                }

                text.setText(sb.toString());

            } catch (SQLException ex) {
                ex.printStackTrace();
                text.setText("Error fetching frequent station report: " + ex.getMessage());
            }
        }
    }



}

