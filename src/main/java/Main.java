import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.*;

public class Main extends JFrame implements ActionListener {
    Main(){
        setTitle("Cloud Kitchen Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(800,600);
        setVisible(true);

        JButton orders =  new JButton("");
        orders.setBounds(0,0,800,140);
        orders.addActionListener(this);
        add(orders);

        JButton reports =  new JButton("");
        reports.setBounds(0,140,800,140);
        reports.addActionListener(this);
        add(reports);

        JButton staffs =  new JButton("");
        staffs.setBounds(0,280,800,140);
        staffs.addActionListener(this);
        add(staffs);

        JButton maintenance =  new JButton("");
        maintenance.setBounds(0,420,800,140);
        maintenance .addActionListener(this);
        add(maintenance);



    }


    public static void main(String[] args) throws Exception {
        new Main();


    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
