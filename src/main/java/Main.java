import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Main extends JFrame implements ActionListener {
    JButton orders, reports, staffs, maintenance;
    JPanel mainmenu;
    AddAnOrder addAnOrder;
    OrderManagement orderManagement;
    ViewReportsPage viewreports;
    EditStaffRole editStaffRole;
    Main(){
        setTitle("Cloud Kitchen Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(800,600);
        setLayout(null);
        mainmenu = new JPanel(null);
        mainmenu.setSize(800,600);

        addAnOrder = new AddAnOrder(this);
        orderManagement = new OrderManagement(this);
        viewreports = new ViewReportsPage(this);
        editStaffRole = new EditStaffRole(this);

        ImageIcon icon = new ImageIcon("src/main/resources/order.png");
        orders =  new JButton(icon);
        orders.setContentAreaFilled(false);
        orders.setBounds(0,0,800,140);
        orders.addActionListener(this);
        mainmenu.add(orders);

        ImageIcon iconReports = new ImageIcon("src/main/resources/reports.png");
        reports = new JButton(iconReports);
        reports.setContentAreaFilled(false);
        reports.setBounds(0,140,800,140);
        reports.addActionListener(this);
        mainmenu.add(reports);

        ImageIcon iconStaffs = new ImageIcon("src/main/resources/staff.png");
        staffs = new JButton(iconStaffs);
        staffs.setContentAreaFilled(false);
        staffs.setBounds(0,280,800,140);
        staffs.addActionListener(this);
        mainmenu.add(staffs);

        ImageIcon iconMaintenance = new ImageIcon("src/main/resources/maintenance.png");
        maintenance = new JButton(iconMaintenance);
        maintenance.setContentAreaFilled(false);
        maintenance.setBounds(0,420,800,140);
        maintenance .addActionListener(this);
        mainmenu.add(maintenance);

        showMainmenu();
        setVisible(true);
    }

    public void showMainmenu(){
        setContentPane(mainmenu);
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == orders){
            orderManagement.showOrderManagementPage();
        }
        if(e.getSource() == maintenance){
            this.setVisible(false);
            MaintenanceLog maintenanceWindow = new MaintenanceLog(this);
            maintenanceWindow.setVisible(true);
        }
        if(e.getSource() == reports){
            viewreports.showViewReportsButton();
        }
        if(e.getSource() == staffs){
            editStaffRole.initializeComponents();
        }
    }
}
