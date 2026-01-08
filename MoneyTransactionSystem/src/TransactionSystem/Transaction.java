package TransactionSystem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import TransactionSystem.User;
public class Transaction {

    private final Connection connection;
    private final Scanner scanner;
    private final User user;

    public Transaction(Connection connection, Scanner scanner, User user) {
        this.connection = connection;
        this.scanner = scanner;
        this.user = user;
    }

    public void viewTransactionHistory(){

        System.out.println("Enter your Account number : ");
        String account_no = scanner.next();

        try{
            user.checkAccountNum(account_no);


            String getUserQuery = "SELECT id,name FROM user WHERE account_no = ?";

            PreparedStatement userStatement = connection.prepareStatement(getUserQuery);
            userStatement.setString(1,account_no);

            ResultSet userResultSet = userStatement.executeQuery();
            if (!userResultSet.next()){
                System.out.println("Account not found !!");
            }

            int userId =  userResultSet.getInt("id");
            String name = userResultSet.getString("name");

            String transactionHistoryQuery = "SELECT type,amount,created_at FROM transaction WHERE account_id = ? ORDER BY transaction_id DESC";

            PreparedStatement preparedStatement = connection.prepareStatement(transactionHistoryQuery);
            preparedStatement.setInt(1,userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\nTransaction History for " + name);
            System.out.println("+----------+------------+---------------------+");
            System.out.println("| TYPE     | AMOUNT     | DATE & TIME         |");
            System.out.println("+----------+------------+---------------------+");

            boolean hasTxn = false;

            while (resultSet.next()) {
                hasTxn = true;
                String type = resultSet.getString("type");
                double amount = resultSet.getDouble("amount");
                String date = resultSet.getString("created_at");

                System.out.printf("| %-8s | %-10.2f | %-19s |\n",
                        type, amount, date);
            }

            if (!hasTxn) {
                System.out.println("| No transactions found                          |");
            }

            System.out.println("+----------+------------+---------------------+");


        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
