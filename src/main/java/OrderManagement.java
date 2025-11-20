import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderManagement extends JPanel implements ActionListener {
    JPanel orderManagementPage;
    JButton addOrderButton, viewPendingOrdersButton, backButton;
    Main main;
    AddAnOrder addAnOrder;
    EditQueueStatus editQueueStatus;


    OrderManagement(Main main){
        this.main = main;
    }

    public void showOrderManagementPage(){
        addAnOrder = new AddAnOrder(main);
        editQueueStatus = new EditQueueStatus(main);
        orderManagementPage = new JPanel();
        orderManagementPage.setSize(800,600);
        orderManagementPage.setLayout(null);

        ImageIcon icon = new ImageIcon("src/main/resources/add.png");
        addOrderButton = new JButton(icon);
        addOrderButton.setContentAreaFilled(false);
        addOrderButton.setBounds(0,0,270,569);
        addOrderButton.addActionListener(this);
        orderManagementPage.add(addOrderButton);

        ImageIcon icon2 = new ImageIcon("src/main/resources/view.png");
        viewPendingOrdersButton = new JButton(icon2);
        viewPendingOrdersButton.setContentAreaFilled(false);
        viewPendingOrdersButton.setBounds(270,0,265,569);
        viewPendingOrdersButton.addActionListener(this);
        orderManagementPage.add(viewPendingOrdersButton);

        main.setContentPane(orderManagementPage);

        ImageIcon icon3 = new ImageIcon("src/main/resources/back.png");
        backButton = new JButton(icon3);
        backButton.setContentAreaFilled(false);
        backButton.setBounds(535,0,266,569);
        backButton.addActionListener(this);
        orderManagementPage.add(backButton);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addOrderButton){
            addAnOrder.customerDetails();
        }

        if  (e.getSource() == viewPendingOrdersButton){
            editQueueStatus.showEditQueueStatus();
        }

        if (e.getSource() == backButton){
            main.showMainmenu();
        }
    }
}
