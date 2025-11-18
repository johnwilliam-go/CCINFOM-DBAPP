import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class EditStaffRole extends javax.swing.JFrame {

    //Fields
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel flowLayoutPanel1;
    private javax.swing.JButton addRole;
    private javax.swing.JButton editRole;
    private javax.swing.JButton removeRole;
    private javax.swing.JButton updateEmployment;
    private javax.swing.JLabel editStaffRoles;
    private javax.swing.JPanel flowLayoutPanel2;
    private javax.swing.JButton goBack;

    //Database Connection
    private static final String DB_uRL = "jdbc:mysql://127.0.0.1:3306/ccinfom_db";
    private static final String user = "root";
    private static final String PASS = "Siopao_Haichi_23";

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
        String staffIdString = JOptionPane.showInputDialog(this, "Enter New Staff ID:");
        if (staffIdString == null || staffIdString.trim().isEmpty()){
            return;
        }

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
        String sql = "INSERT INTO KitchenStaff (StaffID, FirstName, LastName, Role, EmploymentStatus) VALUES (?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ppstmt = conn.prepareStatement(sql);){

            //Setting values for ?
            ppstmt.setString(1, staffIdString);
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
                JOptionPane.showMessageDialog(this,"A staff member with Staff ID '" + staffIdString + "' already exists.", "Error",JOptionPane.ERROR_MESSAGE);
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

    private void removeRole_Click(ActionEvent evt) {
        String staffIdString = JOptionPane.showInputDialog(this, "Enter New Staff ID:");
        if (staffIdString == null || staffIdString.trim().isEmpty()){
            return;
        }

        //Confirmation to avoid accidental deletion
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove Staff ID: " + staffIdString + "\nThis action cannot be undone.", "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_NO_CANCEL_OPTION){
            return; //User cancellation
        }

        //SQL Query
        String sql = "DELETE FROM KitchenStaff WHERE StaffID = ?";

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
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    private void initializeComponents(){
        panel1 = new javax.swing.JPanel();
        flowLayoutPanel1 = new javax.swing.JPanel();
        addRole = new javax.swing.JButton();
        editRole = new javax.swing.JButton();
        removeRole = new javax.swing.JButton();
        updateEmployment = new javax.swing.JButton();
        editStaffRoles = new javax.swing.JLabel();
        flowLayoutPanel2 = new javax.swing.JPanel();
        goBack = new javax.swing.JButton();

        //For JFrame
        setTitle("Edit Staff Roles");
        setSize(711,466);
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        getContentPane().setLayout(null);
        setLocationRelativeTo(null);

        //For JPanel
        panel1.setOpaque(false);
        panel1.setLayout(new java.awt.BorderLayout());
        panel1.setBounds(212,147,245,249);

        //Constructing another JPanel within panel1 but the controls within are fixed within panel1
        flowLayoutPanel1.setLayout(new javax.swing.BoxLayout(flowLayoutPanel1, javax.swing.BoxLayout.Y_AXIS));
        flowLayoutPanel1.setOpaque(false);

        //For button "Add New Kitchen Staff"
        addRole.setText("Add New Kitchen Staff");
        addRole.setBackground(SystemColor.activeCaption);
        addRole.setFont(new java.awt.Font("Modern No. 20",1,10));

        //For formatting inside the BoxLayout which is flowLayoutPanel1
        addRole.setPreferredSize(new java.awt.Dimension(240,56));
        addRole.setMaximumSize(new java.awt.Dimension(240,56));
        addRole.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        //For button "Update Roles"
        editRole.setText("Update Roles");
        editRole.setBackground(SystemColor.activeCaption);
        editRole.setFont(new java.awt.Font("Modern No. 20",1,12));
        editRole.setPreferredSize(new java.awt.Dimension(240,56));
        editRole.setMaximumSize(new java.awt.Dimension(240,56));
        editRole.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        //For button "Remove Staff Member"
        removeRole.setText("Remove Staff Member");
        removeRole.setBackground(SystemColor.activeCaption);
        removeRole.setFont(new java.awt.Font("Modern No. 20",1,12));
        removeRole.setPreferredSize(new java.awt.Dimension(240,56));
        removeRole.setMaximumSize(new java.awt.Dimension(240,56));
        removeRole.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        //For button "Update Employment Status"
        updateEmployment.setText("Update Employment Status");
        updateEmployment.setBackground(SystemColor.activeCaption);
        updateEmployment.setFont(new java.awt.Font("Modern No. 20",1,11));
        updateEmployment.setPreferredSize(new java.awt.Dimension(240,56));
        updateEmployment.setMaximumSize(new java.awt.Dimension(240,56));
        updateEmployment.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        //For "Back" button to be contained by flowLayoutPanel2
        goBack.setText("Back");
        goBack.setBackground(SystemColor.activeCaption);
        goBack.setFont(new java.awt.Font("Mongolian Baiti",1,8));
        goBack.setPreferredSize(new java.awt.Dimension(67,25));

        //For JLabel
        editStaffRoles.setText("Edit Staff Roles");
        editStaffRoles.setFont(new java.awt.Font("Mongolian Baiti",1,36));

        //Location
        editStaffRoles.setBounds(136,38,420,64);
        editStaffRoles.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        //For flowLayoutPanel2 which has the control/component Back button
        flowLayoutPanel2.setBounds(12,12,83,63);
        flowLayoutPanel2.setOpaque(false);

        //Adding controls/components to flowLayoutPanel1
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0,3))); //For Top Margin
        flowLayoutPanel1.add(addRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0,3))); //Gap
        flowLayoutPanel1.add(editRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0,6))); //Gap
        flowLayoutPanel1.add(removeRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0,6))); //Gap
        flowLayoutPanel1.add(updateEmployment);

        //Adding flowLayoutPanel1 to panel1 and filling the whole panel1
        panel1.add(flowLayoutPanel1,java.awt.BorderLayout.CENTER);

        //Adding the "Back" button to flowLayoutPanel2
        flowLayoutPanel2.add(goBack);

        //Add the main container to JFrame
        getContentPane().add(flowLayoutPanel2);
        getContentPane().add(editStaffRoles);
        getContentPane().add(panel1);

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
                removeRole_Click(evt);
            }
        });

        updateEmployment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateEmployment_Click(evt);
            }
        });

        goBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //goBack_Click(evt);
            }
        });

    }

    public EditStaffRole(){
        //A function that will initialize the frame
        initializeComponents();
    }



    public static void main(String[] args){
        new EditStaffRole().setVisible(true);
    }
}