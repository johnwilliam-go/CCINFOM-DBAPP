import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame implements ActionListener {

    JButton button1, button2, button3,  button4;
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
        button3 = new JButton("Record a Maintenance");
        button4 = new JButton("Update Maintenance status");


        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);

        button1.setBounds(300, 150, 200, 50);
        button2.setBounds(300, 225, 200, 50);
        button3.setBounds(300, 300, 200, 50);
        button4.setBounds(300, 375, 200, 50);

        backgroundLabel.add(title);
        backgroundLabel.add(button1);
        backgroundLabel.add(button2);
        backgroundLabel.add(button3);
        backgroundLabel.add(button4);


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
            new AddAnOrder();
        }
        else if (e.getSource() == button2) {
            JOptionPane.showMessageDialog(null,
                    "Set the order status to ready for each order line." +
                            "\ntables affected: orderentries");
        }
        else if (e.getSource() == button3) {
            JOptionPane.showMessageDialog(null,
                    "Insert equipmentid, issuetype, priority, description, and reported by" +
                    "\nTable affected: maintenancetracker");
        }
        else if (e.getSource() == button4) {
            JOptionPane.showMessageDialog(null,
                    "Edit through reportID and add information like description (if you have alr), priority, status" +
                            "\nTable affected: maintenancetracker");
        }
    }
}

