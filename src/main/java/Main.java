import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/ccinfomdb",
                "root",
                "12345678"
        );


        //hard coded so everytime a user inputs a record, these data will be generated (just an example)
        Statement statement = connection.createStatement();
        String sql = "INSERT INTO equipments (EquipmentName, Category, Brand, Description, SupplierName, ContactNumber, EmailAddress) " +
                "VALUES ('Grill #1', 'Grill', 'ShineLong', '', '', '09372738203', 'shinelongph@gmail.com')";
        statement.executeUpdate(sql);
        statement.close();
        connection.close();

        System.out.println("Data inserted successfully!");
    }
}
