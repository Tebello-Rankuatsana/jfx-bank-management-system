package ranks.bank;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class BankController {

    // Account Management
    @FXML private TextField ownerNameField;
    @FXML private TextField initialBalanceField;
    @FXML private Button createBtn, updateBtn, deleteBtn;
    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, Integer> colAccountId;
    @FXML private TableColumn<Account, String> colOwnerName;
    @FXML private TableColumn<Account, Double> colBalance;

    // Transfer
    @FXML private ComboBox<String> fromAccountCombo;
    @FXML private ComboBox<String> toAccountCombo;
    @FXML private TextField transferAmountField;
    @FXML private Button transferBtn;
    @FXML private Label transferStatusLabel;

    // Update Transaction
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> colTransId;
    @FXML private TableColumn<Transaction, Integer> colFromAcc;
    @FXML private TableColumn<Transaction, Integer> colToAcc;
    @FXML private TableColumn<Transaction, Double> colTransAmount;
    @FXML private TableColumn<Transaction, Timestamp> colTransDate;
    @FXML private TextField newAmountField;
    @FXML private Button updateTransactionBtn;
    @FXML private Label updateStatusLabel;

    @FXML private Label userInfoLabel;
    private String username;
    private String userPrivilege;

    private ObservableList<Account> accountList = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colAccountId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOwnerName.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colTransId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFromAcc.setCellValueFactory(new PropertyValueFactory<>("fromAccountId"));
        colToAcc.setCellValueFactory(new PropertyValueFactory<>("toAccountId"));
        colTransAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTransDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadAccounts();
        loadTransactions();

        accountTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                ownerNameField.setText(newVal.getOwnerName());
                initialBalanceField.setText(String.valueOf(newVal.getBalance()));
            }
        });

        accountList.addListener((javafx.collections.ListChangeListener<? super Account>) c -> refreshComboBoxes());
    }

    public void setUserInfo(String username, String privilege) {
        this.username = username;
        this.userPrivilege = privilege;
        userInfoLabel.setText("User: " + username + " | Role: " + privilege);
//        if ("VIEW_ONLY".equals(privilege)) {
//            createBtn.setDisable(true);
//            updateBtn.setDisable(true);
//            deleteBtn.setDisable(true);
//            transferBtn.setDisable(true);
//            updateTransactionBtn.setDisable(true);
//        }
    }

    private void refreshComboBoxes() {
        fromAccountCombo.getItems().clear();
        toAccountCombo.getItems().clear();
        for (Account acc : accountList) {
            String item = acc.getId() + " - " + acc.getOwnerName();
            fromAccountCombo.getItems().add(item);
            toAccountCombo.getItems().add(item);
        }
    }

    // creating account
    @FXML
    private void createAccount() {
        String name = ownerNameField.getText();
        String balText = initialBalanceField.getText();
        if (name.isEmpty() || balText.isEmpty()) {
            showAlert("Error", "Fill all fields.");
            return;
        }
        double balance;
        try {
            balance = Double.parseDouble(balText);
            if (balance < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Error", "Balance must be >= 0.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    HelloApplication.DB.openConnection();
                    String query = "INSERT INTO accounts (owner_name, balance) VALUES (?, ?)";
                    HelloApplication.DB.setPstmt(query);
                    PreparedStatement pstmt = HelloApplication.DB.getPstmt();
                    pstmt.setString(1, name);
                    pstmt.setDouble(2, balance);
                    HelloApplication.DB.executeUpdatePstmt();
                    HelloApplication.DB.closeDataLink();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Success", "Account created.");
            ownerNameField.clear();
            initialBalanceField.clear();
            loadAccounts();
            loadTransactions();
        });
        task.setOnFailed(e -> showAlert("DB Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    private void updateAccount() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select an account.");
            return;
        }
        String newName = ownerNameField.getText();
        String newBalText = initialBalanceField.getText();
        if (newName.isEmpty() || newBalText.isEmpty()) {
            showAlert("Error", "Fill all fields.");
            return;
        }
        double newBalance;
        try {
            newBalance = Double.parseDouble(newBalText);
            if (newBalance < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Error", "Balance must be >= 0.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    HelloApplication.DB.openConnection();
                    String query = "UPDATE accounts SET owner_name = ?, balance = ? WHERE account_id = ?";
                    HelloApplication.DB.setPstmt(query);
                    PreparedStatement pstmt = HelloApplication.DB.getPstmt();
                    pstmt.setString(1, newName);
                    pstmt.setDouble(2, newBalance);
                    pstmt.setInt(3, selected.getId());
                    HelloApplication.DB.executeUpdatePstmt();
                    HelloApplication.DB.closeDataLink();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Success", "Account updated.");
            loadAccounts();
            loadTransactions();
        });
        task.setOnFailed(e -> showAlert("DB Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    private void deleteAccount() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select an account.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete account and all its transactions?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            HelloApplication.DB.openConnection();
                            String query = "DELETE FROM accounts WHERE account_id = ?";
                            HelloApplication.DB.setPstmt(query);
                            PreparedStatement pstmt = HelloApplication.DB.getPstmt();
                            pstmt.setInt(1, selected.getId());
                            HelloApplication.DB.executeUpdatePstmt();
                            HelloApplication.DB.closeDataLink();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    showAlert("Success", "Account deleted.");
                    loadAccounts();
                    loadTransactions();
                });
                task.setOnFailed(e -> showAlert("DB Error", task.getException().getMessage()));
                new Thread(task).start();
            }
        });
    }

    // ---------- Transfer (with commit/rollback) ----------
    @FXML
    private void transferMoney() {
        String fromItem = fromAccountCombo.getValue();
        String toItem = toAccountCombo.getValue();
        String amountText = transferAmountField.getText();
        if (fromItem == null || toItem == null || amountText.isEmpty()) {
            transferStatusLabel.setText("Fill all fields.");
            return;
        }
        if (fromItem.equals(toItem)) {
            transferStatusLabel.setText("Cannot transfer to same account.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            transferStatusLabel.setText("Amount must be positive.");
            return;
        }

        int fromId = Integer.parseInt(fromItem.split(" - ")[0]);
        int toId = Integer.parseInt(toItem.split(" - ")[0]);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Connection conn = null;
                try {
                    conn = HelloApplication.DB.getDatabaseLink();
                    conn.setAutoCommit(false);

                    // Check balance using DBConnection style
                    String checkQuery = "SELECT balance FROM accounts WHERE id = ? FOR UPDATE";
                    HelloApplication.DB.setPstmt(checkQuery);
                    PreparedStatement pstmtCheck = HelloApplication.DB.getPstmt();
                    pstmtCheck.setInt(1, fromId);
                    ResultSet rs = pstmtCheck.executeQuery();
                    if (rs.next() && rs.getDouble("balance") < amount) {
                        throw new SQLException("Insufficient funds.");
                    }
                    rs.close();

                    // Withdraw
                    String withdraw = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(withdraw);
                    PreparedStatement pstmtWith = HelloApplication.DB.getPstmt();
                    pstmtWith.setDouble(1, amount);
                    pstmtWith.setInt(2, fromId);
                    HelloApplication.DB.executeUpdatePstmt();

                    // Deposit
                    String deposit = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(deposit);
                    PreparedStatement pstmtDep = HelloApplication.DB.getPstmt();
                    pstmtDep.setDouble(1, amount);
                    pstmtDep.setInt(2, toId);
                    HelloApplication.DB.executeUpdatePstmt();

                    // Record transaction
                    String trans = "INSERT INTO transactions (from_account_id, to_account_id, amount) VALUES (?, ?, ?)";
                    HelloApplication.DB.setPstmt(trans);
                    PreparedStatement pstmtTrans = HelloApplication.DB.getPstmt();
                    pstmtTrans.setInt(1, fromId);
                    pstmtTrans.setInt(2, toId);
                    pstmtTrans.setDouble(3, amount);
                    HelloApplication.DB.executeUpdatePstmt();

                    conn.commit();
                } catch (SQLException e) {
                    if (conn != null) conn.rollback();
                    throw e;
                } finally {
                    if (conn != null) conn.setAutoCommit(true);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            transferStatusLabel.setText("Transfer successful!");
            transferAmountField.clear();
            loadAccounts();
            loadTransactions();
        });
        task.setOnFailed(e -> {
            transferStatusLabel.setText("Transfer failed: " + task.getException().getMessage());
            showAlert("Transfer Error", task.getException().getMessage());
        });
        new Thread(task).start();
    }

    // ---------- Update Transaction (revert old, apply new, rollback on failure) ----------
    @FXML
    private void updateTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatusLabel.setText("Select a transaction.");
            return;
        }
        String newAmountText = newAmountField.getText();
        if (newAmountText.isEmpty()) {
            updateStatusLabel.setText("Enter new amount.");
            return;
        }
        double newAmount;
        try {
            newAmount = Double.parseDouble(newAmountText);
            if (newAmount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            updateStatusLabel.setText("Amount must be positive.");
            return;
        }

        double oldAmount = selected.getAmount();
        int fromId = selected.getFromAccountId();
        int toId = selected.getToAccountId();
        int transId = selected.getId();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Connection conn = null;
                try {
                    conn = HelloApplication.DB.getDatabaseLink();
                    conn.setAutoCommit(false);

                    // Revert old transfer: add back to source, subtract from destination
                    String revertSource = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(revertSource);
                    PreparedStatement pstmtRevSrc = HelloApplication.DB.getPstmt();
                    pstmtRevSrc.setDouble(1, oldAmount);
                    pstmtRevSrc.setInt(2, fromId);
                    HelloApplication.DB.executeUpdatePstmt();

                    String revertDest = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(revertDest);
                    PreparedStatement pstmtRevDst = HelloApplication.DB.getPstmt();
                    pstmtRevDst.setDouble(1, oldAmount);
                    pstmtRevDst.setInt(2, toId);
                    HelloApplication.DB.executeUpdatePstmt();

                    // Check if source has enough for new amount
                    String checkQuery = "SELECT balance FROM accounts WHERE id = ? FOR UPDATE";
                    HelloApplication.DB.setPstmt(checkQuery);
                    PreparedStatement pstmtCheck = HelloApplication.DB.getPstmt();
                    pstmtCheck.setInt(1, fromId);
                    ResultSet rs = pstmtCheck.executeQuery();
                    if (rs.next() && rs.getDouble("balance") < newAmount) {
                        throw new SQLException("Insufficient funds for updated amount.");
                    }
                    rs.close();

                    // Apply new transfer
                    String newWithdraw = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(newWithdraw);
                    PreparedStatement pstmtNewW = HelloApplication.DB.getPstmt();
                    pstmtNewW.setDouble(1, newAmount);
                    pstmtNewW.setInt(2, fromId);
                    HelloApplication.DB.executeUpdatePstmt();

                    String newDeposit = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(newDeposit);
                    PreparedStatement pstmtNewD = HelloApplication.DB.getPstmt();
                    pstmtNewD.setDouble(1, newAmount);
                    pstmtNewD.setInt(2, toId);
                    HelloApplication.DB.executeUpdatePstmt();

                    // Update transaction record
                    String updateTrans = "UPDATE transactions SET amount = ? WHERE id = ?";
                    HelloApplication.DB.setPstmt(updateTrans);
                    PreparedStatement pstmtUp = HelloApplication.DB.getPstmt();
                    pstmtUp.setDouble(1, newAmount);
                    pstmtUp.setInt(2, transId);
                    HelloApplication.DB.executeUpdatePstmt();

                    conn.commit();
                } catch (SQLException e) {
                    if (conn != null) conn.rollback();
                    throw e;
                } finally {
                    if (conn != null) conn.setAutoCommit(true);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            updateStatusLabel.setText("Transaction updated!");
            newAmountField.clear();
            loadAccounts();
            loadTransactions();
        });
        task.setOnFailed(e -> {
            updateStatusLabel.setText("Update failed: " + task.getException().getMessage());
            showAlert("Update Error", task.getException().getMessage());
        });
        new Thread(task).start();
    }

    // ---------- Load data using Statement (no ArrayList) ----------
    private void loadAccounts() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ObservableList<Account> list = FXCollections.observableArrayList();
                try {
                    HelloApplication.DB.openConnection();
                    String query = "SELECT id, owner_name, balance FROM accounts ORDER BY id";
                    HelloApplication.DB.setStmt(query);
                    ResultSet rs = HelloApplication.DB.getStmt().executeQuery(query);
                    while (rs.next()) {
                        list.add(new Account(
                                rs.getInt("id"),
                                rs.getString("owner_name"),
                                rs.getDouble("balance")
                        ));
                    }
                    HelloApplication.DB.closeDataLink();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                final ObservableList<Account> finalList = list;
                Platform.runLater(() -> {
                    accountList.setAll(finalList);
                    accountTable.setItems(accountList);
                    refreshComboBoxes();
                });
                return null;
            }
        };
        task.setOnFailed(e -> showAlert("DB Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void loadTransactions() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ObservableList<Transaction> list = FXCollections.observableArrayList();
                try {
                    HelloApplication.DB.openConnection();
                    String query = "SELECT id, from_account_id, to_account_id, amount, transaction_date FROM transactions ORDER BY transaction_date DESC";
                    HelloApplication.DB.setStmt(query);
                    ResultSet rs = HelloApplication.DB.getStmt().executeQuery(query);
                    while (rs.next()) {
                        list.add(new Transaction(
                                rs.getInt("id"),
                                rs.getInt("from_account_id"),
                                rs.getInt("to_account_id"),
                                rs.getDouble("amount"),
                                rs.getTimestamp("transaction_date")
                        ));
                    }
                    HelloApplication.DB.closeDataLink();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                final ObservableList<Transaction> finalList = list;
                Platform.runLater(() -> {
                    transactionList.setAll(finalList);
                    transactionTable.setItems(transactionList);
                });
                return null;
            }
        };
        task.setOnFailed(e -> showAlert("DB Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bank Management System - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}