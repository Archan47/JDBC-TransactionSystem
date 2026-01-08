package TransactionSystem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {
    private final Connection connection;
    private final Scanner scanner;

    public void checkAccountNum(String account_num) throws InvalidAccountNumException{
        if (account_num.length() != 11){
            throw new InvalidAccountNumException("Invalid Account Number!!\n Please enter correct account number.");
        }
    }

    public User(Connection connection, Scanner scanner) {
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

    // OPTION 2 - View Account Balance
    public void ViewBalance() {

        System.out.println("Enter your Account Number : ");
        String account_no = scanner.next();

        try {
            checkAccountNum(account_no);

            String query = "SELECT name, balance FROM user WHERE account_no = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, account_no);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("Account not found !");
                return;
            }

            String name = resultSet.getString("name");
            double balance = resultSet.getDouble("balance");

            System.out.println("+-----------------+---------------+");
            System.out.println("| Name            | Balance       |");
            System.out.println("+-----------------+---------------+");
            System.out.printf("| %-15s | %-13.2f |\n", name, balance);
            System.out.println("+-----------------+---------------+");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());
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

    // OPTION 4 - Deposit Money
    public void depositAmount(){

        System.out.println("Enter your Account Number : ");
        String account_no = scanner.next();

        System.out.println("Enter your PIN : ");
        int pin = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter your amount : ");
        double amount = scanner.nextDouble();


        try{
            checkAccountNum(account_no);

            connection.setAutoCommit(false);

            String checkAccountQuery = "SELECT id from user WHERE account_no = ? AND pin = ?";

            PreparedStatement checkStatement = connection.prepareStatement(checkAccountQuery);
            checkStatement.setString(1,account_no);
            checkStatement.setInt(2,pin);

            ResultSet resultSet = checkStatement.executeQuery();
            if (!resultSet.next()){
                System.out.println("Invalid account number or PIN !!");
            }


            int userId = resultSet.getInt("id");

            String updateAmountQuery = "UPDATE user SET balance = balance + ? WHERE id = ? ";

            PreparedStatement updateBalanceStatement = connection.prepareStatement(updateAmountQuery);
            updateBalanceStatement.setDouble(1,amount);
            updateBalanceStatement.setInt(2,userId);
            updateBalanceStatement.executeUpdate();


            String transactionQuery = "INSERT INTO transaction (type,account_id,amount) VALUES (?,?,?)";

            PreparedStatement transactionStatement = connection.prepareStatement(transactionQuery);
            transactionStatement.setString(1,"DEPOSIT");
            transactionStatement.setInt(2,userId);
            transactionStatement.setDouble(3,amount);

            int affectedRows = transactionStatement.executeUpdate();
            if (affectedRows>0){
                System.out.println("Deposit Transaction made successfully...");
                connection.commit();
            }
            else{
                System.out.println("Transaction Failed !!");
            }


        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // OPTION 5 - Withdraw Amount
    public void withdrawAmount() {

        System.out.println("Enter your Account number : ");
        String account_no = scanner.next();

        System.out.println("Enter your PIN : ");
        int pin = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.println("Enter your amount : ");
        double amount = scanner.nextDouble();

        try {
            checkAccountNum(account_no);

            connection.setAutoCommit(false);

            // Verify account, pin & get balance
            String checkQuery = "SELECT id, balance FROM user WHERE account_no = ? AND pin = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, account_no);
            checkStmt.setInt(2, pin);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                System.out.println("Invalid account number or PIN !!");
                connection.rollback();
                return;
            }

            int userId = resultSet.getInt("id");
            double balance = resultSet.getDouble("balance");

            // STEP 2: Check sufficient balance
            if (balance < amount) {
                System.out.println("Insufficient balance!");
                connection.rollback();
                return;
            }

            // Update balance
            String updateBalanceQuery = "UPDATE user SET balance = balance - ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateBalanceQuery);
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();

            // Insert transaction record
            String txnQuery = "INSERT INTO transaction (type, account_id, amount) VALUES (?, ?, ?)";
            PreparedStatement txnStmt = connection.prepareStatement(txnQuery);
            txnStmt.setString(1, "WITHDRAW");
            txnStmt.setInt(2, userId);
            txnStmt.setDouble(3, amount);
            txnStmt.executeUpdate();

            connection.commit();
            System.out.println("Withdrawal successful!");

        } catch (InvalidAccountNumException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}



