package TransactionSystem;
import java.sql.*;
import java.util.Scanner;

public class TransactionSystemMain {
    private static final String url = "jdbc:mysql://localhost:3306/ATM";
    private static final String username = "root";
    private static final String password = "Archan@2002";

    public static void main(String[] args) throws SQLException {

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(System.in);

        try{
            Connection connection = DriverManager.getConnection(url,username,password);
            User user = new User(connection,sc);
            Transaction txn = new Transaction(connection,sc,user);
            boolean running = true;
            while (running){
                System.out.println();
                System.out.println("--------------- WELCOME TO YOUR OWN TRANSACTION SYSTEM -------------");
                System.out.println("Choose the options below according to your need");
                System.out.println("1. Open Account");
                System.out.println("2. Forgot PIN ? Reset new PIN");
                System.out.println("3. Deposit Money");
                System.out.println("4. Withdraw Money");
                System.out.println("5. View Balance");
                System.out.println("6. View Transaction History");
                System.out.println("7. Exit");

                System.out.println("Enter your choice : ");
                int choice = sc.nextInt();

                switch (choice){
                    case 1:
                        user.openAccount();
                        System.out.println();
                        break;
                    case 2:
                        user.setNewPin();
                        System.out.println();
                        break;
                    case 3:
                        user.depositAmount();
                        System.out.println();
                        break;
                    case 4:
                        user.withdrawAmount();
                        System.out.println();
                        break;
                    case 5:
                        user.ViewBalance();
                        System.out.println();
                        break;
                    case 6:
                        txn.viewTransactionHistory();
                        System.out.println();
                        break;
                    case 7:
                        System.out.println("THANK YOU FOR USING TRANSACTION SYSTEM");
                        running=false;
                        break;
                    default:
                        System.out.println("Please enter valid choice.");
                        return;
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
