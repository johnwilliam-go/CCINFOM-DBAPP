import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class EditStaffRole extends javax.swing.JFrame {

    //Fields
    private javax.swing.JPanel panel1, staffViewPanel;
    private javax.swing.JPanel flowLayoutPanel1;
    private javax.swing.JButton addRole;
    private javax.swing.JButton editRole;
    private javax.swing.JButton removeRole;
    private javax.swing.JButton updateEmployment;
    private javax.swing.JLabel editStaffRoles;
    private javax.swing.JPanel flowLayoutPanel2;
    private javax.swing.JButton goBack, goBack2;
    private javax.swing.JButton viewStaff;
    private Main main;

    //Database Connection
    private static final String DB_uRL = "jdbc:mysql://127.0.0.1:3306/ccinfomdb";
    private static final String user = "root";
    private static final String PASS = "12345678";

    //Methods for Database Connection
    public static Connection getConnection() throws SQLException{
        try{
            //This will load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_uRL, user, PASS);
        }

        catch(ClassNotFoundException e){
            System.err.println("MySQL JDBC driver not found!");
            e.printStackTrace();
            throw new SQLException("Driver not found",e);
        }
    }

    //Event handlers
    private void addRole_Click(ActionEvent evt) {
        String firstName = JOptionPane.showInputDialog(this, "Enter First Name:");
        if (firstName == null || firstName.trim().isEmpty()){
            return;
        }

        String lastName = JOptionPane.showInputDialog(this, "Enter Last Name:");
        if (lastName == null || lastName.trim().isEmpty()){
            return;
        }

        //Role selection
        String[] roles = {"Cook","Server","Cleaner", "Assistant Head Chef", "Head Chef"};
        String role = (String) JOptionPane.showInputDialog(this, "Select Role", "Add Staff",JOptionPane.QUESTION_MESSAGE,
                null, roles, roles[0]);
        if (role == null) {
            return;
        }

        //Status selection
        String[] statuses = {"Active", "On Leave", "Resigned"};
        String status = (String) JOptionPane.showInputDialog(this, "Select Employment Status:", "Add Staff:",
                JOptionPane.QUESTION_MESSAGE,null,statuses, statuses[0]);
        if (status == null){
            return;
        }

        //Overwriting SQL Query
        String sql = "INSERT INTO KitchenStaff (UserID, FirstName, LastName, Role, EmploymentStatus) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ppstmt = conn.prepareStatement(sql);){

            //Setting values for ?
            ppstmt.setString(1, firstName + "." + lastName);
            ppstmt.setString(2, firstName);
            ppstmt.setString(3, lastName);
            ppstmt.setString(4, role);
            ppstmt.setString(5, status);

            //Execute query
            int rowsAffected = ppstmt.executeUpdate();

            //Show Feedback
            if (rowsAffected > 0){
                JOptionPane.showMessageDialog(this, "New staff member added successfully");
            }
            else {
                JOptionPane.showMessageDialog(this, "Failed to add staff member.");
            }
        }
        catch (SQLException e){
            if(e.getErrorCode() == 1062) {//1062 is the error code for duplicate keys
                JOptionPane.showMessageDialog(this,"A staff member with Staff ID already exists.", "Error",JOptionPane.ERROR_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        }
    }

    private void editRole_Click(ActionEvent evt){
        String staffIdString = JOptionPane.showInputDialog(this, "Enter New Staff ID:");
        if (staffIdString == null || staffIdString.trim().isEmpty()){
            return;
        }
        String[] roles = {"Cook","Server","Cleaner", "Assistant Head Chef", "Head Chef"};
        String newRole = (String) JOptionPane.showInputDialog(this, "Select Role", "Add Staff",JOptionPane.QUESTION_MESSAGE,
                null, roles, roles[0]);
        if (newRole == null) {
            return;
        }

        //SQL Query
        String sql = "UPDATE KitchenStaff SET Role = ? WHERE StaffID = ?";

        //Database Connection
        try (Connection conn = getConnection(); PreparedStatement ppstmt = conn.prepareStatement(sql);){
            //Set values of ?
            ppstmt.setString(1, newRole);
            ppstmt.setString(2, staffIdString);

            //Executing the SQL query
            int rowsAffected = ppstmt.executeUpdate();

            //Show feedback
            if (rowsAffected > 0){
                JOptionPane.showMessageDialog(this, "Staff role updated successfully");
            }
            else {
                JOptionPane.showMessageDialog(this, "Staff ID not found. No changes made");
            }
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void removeRole_Click(ActionEvent evt) throws SQLException {
        String staffIdString = JOptionPane.showInputDialog(this, "Enter New Staff ID:");
        if (staffIdString == null || staffIdString.trim().isEmpty()){
            return;
        }
        //Confirmation to avoid accidental deletion
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove Staff ID: " + staffIdString + "\nThis action cannot be undone.", "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION){
            return; //User cancellation
        }

        //SQL Query
        String sqlOrders = "UPDATE orderentries SET PreparedBy = NULL WHERE PreparedBy = ?";
        try (Connection conn = getConnection(); PreparedStatement s = conn.prepareStatement(sqlOrders);){
            s.setInt(1, Integer.parseInt(staffIdString));
            s.executeUpdate();
        }

        String sqlMaintenance = "UPDATE maintenancetracker SET ReportedBy = NULL WHERE ReportedBy = ?";
        try (Connection conn = getConnection(); PreparedStatement s = conn.prepareStatement(sqlMaintenance);){
            s.setInt(1, Integer.parseInt(staffIdString));
            s.executeUpdate();
        }
        String sql =
                "DELETE FROM kitchenstaff WHERE StaffID = ?";

        //Connecting to the database

        try (Connection conn = getConnection(); PreparedStatement ppstmt = conn.prepareStatement(sql);){
            //Set values for ?
            ppstmt.setString(1, staffIdString);

            //Execute the query
            int rowsAffected = ppstmt.executeUpdate();

            //Show feedback
            if (rowsAffected > 0){
                JOptionPane.showMessageDialog(this,"Staff Member removed successfully!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Staff ID not found. No staff member removed.");
            }
        }
        catch(SQLException e) {
            JOptionPane.showMessageDialog(this, "Error" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateEmployment_Click(ActionEvent evt){
        //Get input from user
        String staffIdStr = JOptionPane.showInputDialog(this, "Enter Staff ID to update");
        if (staffIdStr == null || staffIdStr.trim().isEmpty()){
            return;
        }

        String[] statuses = {"Active","On Leave","Resigned"};
        String newStatus = (String) JOptionPane.showInputDialog(this,"Select new employment status:","Update status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);

        if (newStatus==null){
            return;
        }

        //SQL Query
        //? is for placeholders
        String sql = "UPDATE KitchenStaff SET EmploymentStatus = ? WHERE StaffID = ?";

        //Connecting to SQL Database
        try (Connection conn = getConnection();PreparedStatement ppstmt = conn.prepareStatement(sql);){

            //Set values for ?
            ppstmt.setString(1, newStatus);
            ppstmt.setString(2,staffIdStr);

            //Execute the query
            int rowsAffected = ppstmt.executeUpdate();

            //Show feedback
            if (rowsAffected > 0){
                JOptionPane.showMessageDialog(this, "Staff status updated successfully!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Staff ID not found. No changes made");
            }
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //Initialize the Components
    public void initializeComponents(){
        panel1 = new javax.swing.JPanel();
        flowLayoutPanel1 = new javax.swing.JPanel();
        addRole = new javax.swing.JButton(new ImageIcon("src/main/resources/addstaff.png"));
        editRole = new javax.swing.JButton(new ImageIcon("src/main/resources/updateroles.png"));
        removeRole = new javax.swing.JButton(new ImageIcon("src/main/resources/removestaff.png"));
        updateEmployment = new javax.swing.JButton(new ImageIcon("src/main/resources/updateemployment.png"));
        editStaffRoles = new javax.swing.JLabel();
        flowLayoutPanel2 = new javax.swing.JPanel();
        goBack = new javax.swing.JButton(new ImageIcon("src/main/resources/backstaff.png"));
        goBack2 = new javax.swing.JButton();
        viewStaff = new javax.swing.JButton(new ImageIcon("src/main/resources/viewstaffroles.png"));


        //For JPanel

        panel1.setLayout(null);
        panel1.setBounds(0,0,800,600);

        viewStaff.setBounds(0, 0, 266, 285);
        viewStaff.setContentAreaFilled(false);

        addRole.setBounds(266, 0, 266, 285);
        addRole.setContentAreaFilled(false);


        editRole.setBounds(532, 0, 266, 285);
        editRole.setContentAreaFilled(false);

        removeRole.setBounds(0, 285, 266, 285);
        removeRole.setContentAreaFilled(false);

        //For button "Update Employment Status"
        updateEmployment.setBounds(266, 285, 266, 285);
        updateEmployment.setContentAreaFilled(false);

        goBack.setBounds(532, 285, 266, 285);
        goBack.setContentAreaFilled(false);

        //Adding flowLayoutPanel1 to panel1 and filling the whole panel1
        panel1.add(viewStaff);
        panel1.add(addRole);
        panel1.add(editRole);
        panel1.add(removeRole);
        panel1.add(updateEmployment);
        panel1.add(goBack);


        //For Event Listeners
        addRole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRole_Click(evt);
            }
        });

        editRole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editRole_Click(evt);
            }
        });

        removeRole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    removeRole_Click(evt);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        updateEmployment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateEmployment_Click(evt);
            }
        });

        goBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                main.showMainmenu();
            }
        });

        viewStaff.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewStaff(evt);
                revalidate();
                repaint();
            }
        });

        main.setContentPane(panel1);

    }

    private void viewStaff(ActionEvent evt) {
        goBack2 = new JButton("exit");
        goBack2.setBounds(300, 500, 200, 50);

        staffViewPanel = new JPanel(null);
        staffViewPanel.setSize(800,600);

        String[] columnNames = {"Staff ID", "User ID", "First Name", "Last Name", "Role", "Employment Status"};
        DefaultTableModel staffTableModel = new DefaultTableModel(columnNames, 0);

        JTable staffJTable = new JTable(staffTableModel);
        staffJTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(staffJTable);
        scrollPane.setBounds(0, 0, 800, 500);

        staffViewPanel.add(scrollPane);
        staffViewPanel.add(goBack2);

        String sql = "SELECT * FROM KitchenStaff";


        try (Connection conn = getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             java.sql.ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("StaffID"),
                        rs.getString("UserID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Role"),
                        rs.getString("EmploymentStatus")
                };
                staffTableModel.addRow(row);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        main.setContentPane(staffViewPanel);


        goBack2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                main.showMainmenu();
            }
        });

    }

    EditStaffRole(Main main){
        this.main = main;
    }



//    public static void main(String[] args){
//        new EditStaffRole().setVisible(true);
//    }
}