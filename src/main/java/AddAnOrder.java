import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Objects;
import javax.swing.*;

public class AddAnOrder extends JPanel implements ActionListener {
    private Main main;
    private JPanel addCustomer;
    private JPanel addOrderEntries;
    private JButton button, buttonAddOrderItem, buttonGoBack, buttonGoBack2;
    private JTextField name, orderNumber, tableNumber, orderDetails, quantity, cookingNotes;
    private JComboBox orderType, paymentMethod, itemList;
    private JLabel title, nameLabel, orderNumberLabel, tableNumberLabel, orderTypeLabel, paymentMethodLabel, orderDetailsLabel;

    String[] items = {
            "Grilled Chicken Sandwich",
            "BBQ Pork Ribs",
            "Beef Burger",
            "Fish and Chips",
            "French Fries",
            "Onion Rings",
            "Caesar Salad",
            "Greek Salad",
            "Chicken Noodle Soup",
            "Mushroom Soup",
            "Chocolate Cake",
            "Vanilla Cupcake",
            "Iced Coffee",
            "Lemon Iced Tea",
            "Margherita Pizza",
            "Pepperoni Pizza",
            "Spaghetti Bolognese",
            "Carbonara",
            "Pancakes",
            "Scrambled Eggs",
            "Chicken Wrap",
            "Tuna Sandwich",
            "Fried Rice",
            "Sweet and Sour Pork",
            "Steamed Dumplings",
            "Steamed Buns",
            "Garlic Bread",
            "Croissant",
            "Roast Chicken",
            "Lasagna"
    };

    public AddAnOrder(Main main) {
        this.main = main;
    }

    public void customerDetails() {
        addCustomer = new JPanel(null);
        addCustomer.setSize(800, 600);

        title = new JLabel("Add an Order");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.BLACK);
        title.setBounds(50, 50, 800, 50);
        addCustomer.add(title);

        nameLabel = new JLabel("Customer Name:");
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setBounds(50, 100, 800, 50);
        addCustomer.add(nameLabel);

        orderNumberLabel = new JLabel("Order Number:");
        orderNumberLabel.setForeground(Color.BLACK);
        orderNumberLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderNumberLabel.setBounds(50, 150, 800, 50);
        addCustomer.add(orderNumberLabel);

        tableNumberLabel = new JLabel("Table Number:");
        tableNumberLabel.setForeground(Color.BLACK);
        tableNumberLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        tableNumberLabel.setBounds(50, 200, 800, 50);
        addCustomer.add(tableNumberLabel);

        orderTypeLabel = new JLabel("Order Type:");
        orderTypeLabel.setForeground(Color.BLACK);
        orderTypeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderTypeLabel.setBounds(50, 250, 800, 50);
        addCustomer.add(orderTypeLabel);

        paymentMethodLabel = new JLabel("Payment Method:");
        paymentMethodLabel.setForeground(Color.BLACK);
        paymentMethodLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        paymentMethodLabel.setBounds(50, 300, 800, 50);
        addCustomer.add(paymentMethodLabel);

        orderDetailsLabel = new JLabel("Order Details:");
        orderDetailsLabel.setForeground(Color.BLACK);
        orderDetailsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        orderDetailsLabel.setBounds(50, 350, 800, 50);
        addCustomer.add(orderDetailsLabel);

        name = new JTextField();
        name.setBounds(200, 110, 200, 30);
        addCustomer.add(name);

        orderNumber = new JTextField();
        orderNumber.setBounds(200, 160, 200, 30);
        addCustomer.add(orderNumber);

        tableNumber = new JTextField();
        tableNumber.setBounds(200, 210, 200, 30);
        addCustomer.add(tableNumber);

        String[] optionOrderType = {"Dine in", "Take out"};
        orderType = new JComboBox(optionOrderType);
        orderType.setBounds(200, 260, 200, 30);
        orderType.addActionListener(this);
        addCustomer.add(orderType);

        String[] optionPaymentMethod = {"Cash", "Credit Card", "Gcash"};
        paymentMethod = new JComboBox(optionPaymentMethod);
        paymentMethod.setBounds(200, 310, 200, 30);
        paymentMethod.addActionListener(this);
        addCustomer.add(paymentMethod);

        orderDetails = new JTextField();
        orderDetails.setBounds(200, 360, 200, 30);
        addCustomer.add(orderDetails);

        button = new JButton("Add Customer Details");
        button.setBounds(200, 410, 200, 30);
        button.addActionListener(this);
        addCustomer.add(button);

        buttonGoBack2 = new JButton("Go Back");
        buttonGoBack2.setBounds(500, 500, 200, 30);
        buttonGoBack2.addActionListener(this);
        addCustomer.add(buttonGoBack2);


        main.setContentPane(addCustomer);
    }


    public void customerOrderEntries() {
        addOrderEntries = new JPanel(null);
        addOrderEntries.setSize(800, 600);
        addOrderEntries.setLayout(null);

        JLabel title = new JLabel("Insert Order Details");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.BLACK);
        title.setBounds(50, 50, 800, 50);

        JLabel customerNameDisplay = new JLabel("For Customer: " + name.getText());
        customerNameDisplay.setFont(new Font("Arial", Font.PLAIN, 16));
        customerNameDisplay.setForeground(Color.BLACK);
        customerNameDisplay.setBounds(50, 80, 800, 50);

        JLabel selectItemLabel = new JLabel("Select Item:");
        selectItemLabel.setForeground(Color.BLACK);
        selectItemLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        selectItemLabel.setBounds(50, 140, 800, 50);
        itemList = new JComboBox(items);
        itemList.setBounds(200, 150, 200, 30);
        itemList.addActionListener(this);


        JLabel quantityLabel = new JLabel("Quantity: ");
        quantityLabel.setForeground(Color.BLACK);
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        quantityLabel.setBounds(50, 190, 800, 50);
        quantity = new JTextField();
        quantity.setBounds(200, 200, 200, 30);

        JLabel cookingNotesLabel = new JLabel("Cooking Notes: ");
        cookingNotesLabel.setForeground(Color.BLACK);
        cookingNotesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        cookingNotesLabel.setBounds(50, 240, 800, 50);
        cookingNotes = new JTextField();
        cookingNotes.setBounds(200, 250, 200, 30);

        buttonAddOrderItem = new JButton("Add Item To Order");
        buttonAddOrderItem.setBounds(200, 300, 200, 30);
        buttonAddOrderItem.addActionListener(this);


        buttonGoBack = new JButton("Go Back");
        buttonGoBack.setBounds(500, 500, 200, 30);
        buttonGoBack.addActionListener(this);
        addOrderEntries.add(title);
        addOrderEntries.add(buttonGoBack);
        addOrderEntries.add(buttonAddOrderItem);
        addOrderEntries.add(customerNameDisplay);
        addOrderEntries.add(selectItemLabel);
        addOrderEntries.add(quantityLabel);
        addOrderEntries.add(cookingNotesLabel);
        addOrderEntries.add(itemList);
        addOrderEntries.add(quantity);
        addOrderEntries.add(cookingNotes);

        main.setContentPane(addOrderEntries);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            try {
                String customerNameField = name.getText();
                int orderNumberField = Integer.parseInt(orderNumber.getText());
                int tableNumberField = Integer.parseInt(tableNumber.getText());
                String orderTypeField = Objects.requireNonNull(orderType.getSelectedItem()).toString();
                String paymentMethodField = Objects.requireNonNull(paymentMethod.getSelectedItem()).toString();
                String orderDetailsField = orderDetails.getText();

                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
                        "root",
                        "12345678"
                );

                String sql = "INSERT INTO kitchenorderticket (CustomerName, OrderNumber, TableNumber, OrderType, PaymentMethod, OrderDetails) " +
                        "VALUES (?,?,?,?,?,?)";

                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, customerNameField);
                statement.setInt(2, orderNumberField);
                statement.setInt(3, tableNumberField);
                statement.setString(4, orderTypeField);
                statement.setString(5, paymentMethodField);
                statement.setString(6, orderDetailsField);

                statement.executeUpdate();


                JOptionPane.showMessageDialog(null, "Customer Added Successfully!");

                customerOrderEntries();

                statement.close();
                connection.close();

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else if (e.getSource() == buttonAddOrderItem) {
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
                        "root",
                        "12345678"
                );

                String sql = "INSERT INTO orderentries (KotID, ItemID, Quantity, CookingNotes) " +
                        "VALUES (?,?,?,?)";

                // since we need the kot id value by using the customer name given to us...
                String kotid_statement = "SELECT KotID FROM kitchenorderticket WHERE CustomerName = ? ORDER BY KotID DESC LIMIT 1";
                String itemID_statement = "SELECT itemID FROM menuitems WHERE ItemName = ?";
                PreparedStatement s1 = connection.prepareStatement(kotid_statement);
                PreparedStatement s2 = connection.prepareStatement(itemID_statement);

                s1.setString(1, name.getText());
                ResultSet kotidOfCustomer = s1.executeQuery();

                int kotID = -1;
                if (kotidOfCustomer.next()) {
                    kotID = kotidOfCustomer.getInt("KotID");
                } else {
                    JOptionPane.showMessageDialog(null, "KotID not found for customer!");
                    return;
                }

                kotidOfCustomer.close();


                String selectedItem = (String) itemList.getSelectedItem();// get the item from combo box
                s2.setString(1, selectedItem);

                ResultSet itemIDofMenuItem = s2.executeQuery();
                itemIDofMenuItem.next();
                String itemID = itemIDofMenuItem.getString("ItemID");


                s1.close();
                s2.close();

                // we need the item id value


                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, kotID);
                statement.setString(2, itemID);
                statement.setString(3, quantity.getText());
                statement.setString(4, cookingNotes.getText());

                statement.executeUpdate();


                JOptionPane.showMessageDialog(null, "Order Added Successfully!");
                statement.close();
                connection.close();

            } catch (SQLException ex) {
                throw new RuntimeException(ex);

            }
        } else if (e.getSource() == buttonGoBack) {
            main.showMainmenu();
        } else if (e.getSource() == buttonGoBack2) {
            main.showMainmenu();
        }

    }

}
