import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderManagement extends JPanel implements ActionListener {
    JPanel orderManagementPage;
    JButton addOrderButton, viewPendingOrdersButton;
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

        addOrderButton = new JButton("Add Order");
        addOrderButton.setBounds(0,0,300,600);
        addOrderButton.addActionListener(this);
        orderManagementPage.add(addOrderButton);

        viewPendingOrdersButton = new JButton("View Pending Orders");
        viewPendingOrdersButton.setBounds(300,0,300,600);
        viewPendingOrdersButton.addActionListener(this);
        orderManagementPage.add(viewPendingOrdersButton);

        main.setContentPane(orderManagementPage);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addOrderButton){
            addAnOrder.customerDetails();
        }

        if  (e.getSource() == viewPendingOrdersButton){
            editQueueStatus.showEditQueueStatus();
        }
    }
}
