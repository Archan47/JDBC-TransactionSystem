package TransactionSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


class InvalidAccountNumException extends Exception {
    public InvalidAccountNumException(String message){
        super(message);
    }
}

public class User {
    private Connection connection;
    private Scanner scanner;

    private void checkAccountNum(String account_num) throws InvalidAccountNumException{
        if (account_num.length() != 11){
            throw new InvalidAccountNumException("Invalid Account Number!!\n Please enter correct account number.");
        }
    }

    public User(Connection connection, Scanner scanner, int userId, String name, String account_num, int pin) {
        this.connection = connection;
        this.scanner = scanner;
    }


    // OPTION 1 - Account Open
    public void openAccount() {
        scanner.nextLine();
        System.out.println("Enter account holder name : ");
        String name = scanner.nextLine();

        System.out.println("Enter account number : ");
        String account_num = scanner.nextLine();

        try {
            checkAccountNum(account_num);

            System.out.println("Enter your pin : ");
            int pin = scanner.nextInt();

            if (pin<1000 || pin>9999){
                System.out.println("PIN must have 4 digits.");
                return;
            }

            String query = "INSERT INTO user(name, account_no, pin) VALUES (?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, account_num);
            preparedStatement.setInt(3, pin);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println(
                        "Congratulations " + name + ", Your account is created successfully !!"
                );
            } else {
                System.out.println("Failed to create the account...");
            }

        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // OPTION 2 - See Account Details
    public void ViewAccount(){
        String query = "SELECT id,name,account_no from user";

        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("+----+-------------------+--------------+");
            System.out.println("| id |       name        |  account_no  |");
            System.out.println("+----+-------------------+--------------+");

            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String account_no = resultSet.getString("account_no");
                System.out.printf("|%-5s|%-20s|%-15s\n",id,name,account_no);
                System.out.println("+----+-------------------+--------------+");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // OPTION 3 - Set new PIN
    public void setNewPin(){
        scanner.nextLine();
        System.out.println("----------- RESET PIN --------------");
        System.out.println("Enter your Account Number ");
        String account_no = scanner.next();

        try{
            checkAccountNum(account_no);

            System.out.println("Enter new Pin : ");
            int pin = scanner.nextInt();

            if (pin<1000 || pin>9999){
                System.out.println("PIN must have 4 digits.");
                return;
            }

            String query = "UPDATE user SET pin = ? WHERE account_no = ? ";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,pin);
            preparedStatement.setString(2,account_no);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows>0){
                System.out.println("PIN updated successfully..!");
            }
            else{
                System.out.println("!! Account not found !!");
            }

        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
