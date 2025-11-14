import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditStaffRole extends JFrame {

    // Fields
    private JPanel panel1;
    private JPanel flowLayoutPanel1;
    private JButton addRole;
    private JButton editRole;
    private JButton removeRole;
    private JButton updateEmployment;
    private JLabel editStaffRoles;
    private JPanel flowLayoutPanel2;
    private JButton goBack;

    public EditStaffRole() {
        initializeComponents();
    }

    private void initializeComponents() {
        panel1 = new JPanel();
        flowLayoutPanel1 = new JPanel();
        addRole = new JButton();
        editRole = new JButton();
        removeRole = new JButton();
        updateEmployment = new JButton();
        editStaffRoles = new JLabel();
        flowLayoutPanel2 = new JPanel();
        goBack = new JButton();

        // JFrame setup
        setTitle("Edit Staff Roles");
        setSize(711, 466);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        setLocationRelativeTo(null);

        // Main panel
        panel1.setOpaque(false);
        panel1.setLayout(new BorderLayout());
        panel1.setBounds(212, 147, 245, 249);

        // Inner panel layout
        flowLayoutPanel1.setLayout(new BoxLayout(flowLayoutPanel1, BoxLayout.Y_AXIS));
        flowLayoutPanel1.setOpaque(false);

        // Add New Kitchen Staff button
        addRole.setText("Add New Kitchen Staff");
        addRole.setBackground(SystemColor.activeCaption);
        addRole.setFont(new Font("Modern No. 20", Font.BOLD, 10));
        addRole.setPreferredSize(new Dimension(240, 56));
        addRole.setMaximumSize(new Dimension(240, 56));
        addRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Update Roles button
        editRole.setText("Update Roles");
        editRole.setBackground(SystemColor.activeCaption);
        editRole.setFont(new Font("Modern No. 20", Font.BOLD, 12));
        editRole.setPreferredSize(new Dimension(240, 56));
        editRole.setMaximumSize(new Dimension(240, 56));
        editRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Remove Staff Member button
        removeRole.setText("Remove Staff Member");
        removeRole.setBackground(SystemColor.activeCaption);
        removeRole.setFont(new Font("Modern No. 20", Font.BOLD, 12));
        removeRole.setPreferredSize(new Dimension(240, 56));
        removeRole.setMaximumSize(new Dimension(240, 56));
        removeRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Update Employment Status button
        updateEmployment.setText("Update Employment Status");
        updateEmployment.setBackground(SystemColor.activeCaption);
        updateEmployment.setFont(new Font("Modern No. 20", Font.BOLD, 11));
        updateEmployment.setPreferredSize(new Dimension(240, 56));
        updateEmployment.setMaximumSize(new Dimension(240, 56));
        updateEmployment.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label
        editStaffRoles.setText("Edit Staff Roles");
        editStaffRoles.setFont(new Font("Mongolian Baiti", Font.BOLD, 36));
        editStaffRoles.setBounds(136, 38, 420, 64);
        editStaffRoles.setHorizontalAlignment(SwingConstants.CENTER);

        // Back button
        goBack.setText("Back");
        goBack.setBackground(SystemColor.activeCaption);
        goBack.setFont(new Font("Mongolian Baiti", Font.BOLD, 8));
        goBack.setPreferredSize(new Dimension(67, 25));

        // flowLayoutPanel2
        flowLayoutPanel2.setBounds(12, 12, 83, 63);
        flowLayoutPanel2.setOpaque(false);

        // Add components to flowLayoutPanel1
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0, 3)));
        flowLayoutPanel1.add(addRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0, 3)));
        flowLayoutPanel1.add(editRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0, 6)));
        flowLayoutPanel1.add(removeRole);
        flowLayoutPanel1.add(Box.createRigidArea(new Dimension(0, 6)));
        flowLayoutPanel1.add(updateEmployment);

        // Add flowLayoutPanel1 to panel1
        panel1.add(flowLayoutPanel1, BorderLayout.CENTER);

        // Add Back button to flowLayoutPanel2
        flowLayoutPanel2.add(goBack);

        // Add all to JFrame
        getContentPane().add(flowLayoutPanel2);
        getContentPane().add(editStaffRoles);
        getContentPane().add(panel1);

        // Event listeners
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
                goBack_Click(evt);
            }
        });
    }

    // Placeholder event methods so it compiles
    private void addRole_Click(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Add New Kitchen Staff clicked!");
    }

    private void editRole_Click(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Update Roles clicked!");
    }

    private void removeRole_Click(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Remove Staff Member clicked!");
    }

    private void updateEmployment_Click(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Update Employment Status clicked!");
    }

    private void goBack_Click(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Going back!");
        dispose(); // close window
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditStaffRole().setVisible(true));
    }
}
