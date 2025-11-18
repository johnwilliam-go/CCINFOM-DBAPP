import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainMenu extends JFrame implements ActionListener {

    JButton button1, button2, button3,  button4, AvgOrderPrep, OrdersAndSales, FrequentStation, MaintenanceReport;
    public MainMenu() {
        ImageIcon background = new ImageIcon("src/main/resources/kitchen.jpg");
        JLabel backgroundLabel = new JLabel(background);

        backgroundLabel.setBounds(0, 0, 800, 600);
        backgroundLabel.setLayout(null);

        JLabel title = new JLabel("KITCHEN OPERATIONS DATABASE");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBounds(50, 50, 800, 50);

        button1 = new JButton("Add an order");
        button2 = new JButton("Update existing order status");
        button3 = new JButton("Edit Staff Roles");
        button4 = new JButton("Log a Maintenance");
        AvgOrderPrep = new JButton("Avg Order Prep");
        OrdersAndSales = new JButton("Orders and Sales");
        FrequentStation = new JButton("Frequent Station");
        MaintenanceReport = new JButton("Maintenance Report");

        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        AvgOrderPrep.addActionListener(this);
        OrdersAndSales.addActionListener(this);
        FrequentStation.addActionListener(this);
        MaintenanceReport.addActionListener(this);


        button1.setBounds(100, 150, 200, 50);
        button2.setBounds(100, 225, 200, 50);
        button3.setBounds(100, 300, 200, 50);
        button4.setBounds(100, 375, 200, 50);
        AvgOrderPrep.setBounds(500, 150, 200, 50);
        OrdersAndSales.setBounds(500, 225, 200, 50);
        FrequentStation.setBounds(500, 300, 200, 50);
        MaintenanceReport.setBounds(500, 375, 200, 50);



        backgroundLabel.add(title);
        backgroundLabel.add(button1);
        backgroundLabel.add(button2);
        backgroundLabel.add(button3);
        backgroundLabel.add(button4);
        backgroundLabel.add(AvgOrderPrep);
        backgroundLabel.add(OrdersAndSales);
        backgroundLabel.add(FrequentStation);
        backgroundLabel.add(MaintenanceReport);



        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,600);
        this.setContentPane(backgroundLabel);
        this.setVisible(true);
        this.setResizable(false);
    }


    public static void main(String[] args) {
        new MainMenu();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button1) {
            this.setVisible(false);
        }
        else if (e.getSource() == button3) {
            JOptionPane.showMessageDialog(null,
                    "Insert equipmentid, issuetype, priority, description, and reported by" +
                    "\nTable affected: maintenancetracker");
        }
//        else if (e.getSource() == button4) {
//            this.setVisible(false);
//            MaintenanceLog maintenanceWindow = new MaintenanceLog(this);
//            maintenanceWindow.setVisible(true);
//        }
        else if (e.getSource() == MaintenanceReport) { // Or whatever your button is named
            this.setVisible(false);

            // This calls the "module" constructor for your new report
            MaintenanceReport reportWindow = new MaintenanceReport(null);
            reportWindow.setVisible(true);
        }

        // Report #1
        else if (e.getSource() == AvgOrderPrep) {

        }

    }
}

// 5/8 functions done (2 faulty)


