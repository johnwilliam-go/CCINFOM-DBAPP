import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class AddAnOrder extends JFrame implements ActionListener {

    JFrame frame;
    JButton  button;
    JTextField name, orderNumber, tableNumber, orderDetails;
    JComboBox orderType, paymentMethod;
    public AddAnOrder(){
        frame = new JFrame("Adding an Order");
        frame.setSize(800,600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon background = new ImageIcon("src/main/resources/kitchen.jpg");
        JLabel backgroundLabel = new JLabel(background);
        backgroundLabel.setBounds(0, 0, 800, 600);

        JLabel title = new JLabel("Add an Order");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBounds(50, 50, 800, 50);


        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setBounds(50, 100, 800, 50);

        JLabel orderNumberLabel = new JLabel("Order Number:");
        orderNumberLabel.setForeground(Color.WHITE);
        orderNumberLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderNumberLabel.setBounds(50, 150, 800, 50);

        JLabel tableNumberLabel = new JLabel("Table Number:");
        tableNumberLabel.setForeground(Color.WHITE);
        tableNumberLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        tableNumberLabel.setBounds(50, 200, 800, 50);

        JLabel orderTypeLabel = new JLabel("Order Type:");
        orderTypeLabel.setForeground(Color.WHITE);
        orderTypeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderTypeLabel.setBounds(50, 250, 800, 50);

        JLabel paymentMethodLabel = new JLabel("Payment Method:");
        paymentMethodLabel.setForeground(Color.WHITE);
        paymentMethodLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        paymentMethodLabel.setBounds(50, 300, 800, 50);

        JLabel orderDetailsLabel = new JLabel("Order Details:");
        orderDetailsLabel.setForeground(Color.WHITE);
        orderDetailsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderDetailsLabel.setBounds(50, 350, 800, 50);

        //text fields for each button
        name = new JTextField();
        name.setBounds(200, 110, 200, 30);

        orderNumber = new JTextField();
        orderNumber.setBounds(200, 160, 200, 30);

        tableNumber = new JTextField();
        tableNumber.setBounds(200, 210, 200, 30);

        String[] optionOrderType = {"Dine in", "Take out"};
        orderType = new JComboBox(optionOrderType);
        orderType.setBounds(200, 260, 200, 30);
        orderType.addActionListener(this);

        String[] optionPaymentMethod = {"Cash", "Credit Card", "Gcash"};
        paymentMethod = new JComboBox(optionPaymentMethod);
        paymentMethod.setBounds(200, 310, 200, 30);
        paymentMethod.addActionListener(this);

        orderDetails = new JTextField();
        orderDetails.setBounds(200, 360, 200, 30);

        button = new JButton("Add Customer Details");
        button.setBounds(200, 410, 200, 30);
        button.addActionListener(this);

        frame.setLayout(null);
        frame.setVisible(true);
        frame.add(title);

        frame.add(nameLabel);
        frame.add(orderNumberLabel);
        frame.add(tableNumberLabel);
        frame.add(orderTypeLabel);
        frame.add(paymentMethodLabel);
        frame.add(orderDetailsLabel);

        // Text Fields
        frame.add(name);
        frame.add(orderNumber);
        frame.add(tableNumber);
        frame.add(orderType);
        frame.add(paymentMethod);
        frame.add(orderDetails);

        // Button
        frame.add(button);

        frame.add(backgroundLabel);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            try {
                String customerNameField = name.getText();
                int orderNumberField = Integer.parseInt(orderNumber.getText());
                int tableNumberField = Integer.parseInt(tableNumber.getText());
                String orderTypeField = String.valueOf(orderType.getSelectedIndex());
                String paymentMethodField = String.valueOf(paymentMethod.getSelectedIndex());
                String orderDetailsField = orderDetails.getText();

                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
                        "root",
                        "12345678"
                );

                String sql = "INSERT INTO kitchenorderticket (CustomerName, OrderNumber, TableNumber, OrderType, PaymentMethod, OrderDetails) " +
                        "VALUES (?,?,?,?,?,?)";

                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, customerNameField); // For CustomerName
                statement.setInt(2, Integer.parseInt(String.valueOf(orderNumberField)));     // For OrderNumber (parsed int)
                statement.setInt(3, Integer.parseInt(String.valueOf(tableNumberField))); // For TableNumber
                statement.setString(4, orderTypeField);   // For OrderType
                statement.setString(5, paymentMethodField); // For PaymentMethod
                statement.setString(6, orderDetailsField);

                statement.executeUpdate();

                JOptionPane.showMessageDialog(null, "Customer Added Successfully!");

                statement.close();
                connection.close();

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    //    public static void main(String[] args) throws Exception {
//        Connection connection = DriverManager.getConnection(
//                "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
//                "root",
//                "12345678"
//        );
//        //hard coded so everytime a user inputs a record, these data will be generated (just an example)
//        Statement statement = connection.createStatement();
//        String sql = "INSERT INTO equipments (EquipmentName, Category, Brand, Description, SupplierName, ContactNumber, EmailAddress) " +
//                "VALUES ('Grill #1', 'Grill', 'ShineLong', '', '', '09372738203', 'shinelongph@gmail.com')";
//        statement.executeUpdate(sql);
//        statement.close();
//        connection.close();
//
//        new AddAnOrder();
//    }

}

    //Alsoo heres the plan for the creation of the app
    //
    //Each of us split the task for creating the functions of the buttons.
    //
    //Each button is basically your transactions and you are gonna code it so that when user inputs the data it adds into the database.
    //
    //If you wanna see how it works, check AddAnOrder.java (on the github i sent) to see the hardcoded process, everytime when you run main, it adds data into the equipments table.
    //
    //Now you are gonna integrate it with a GUI using swing.
// Test update
