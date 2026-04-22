package ranks.bank;


import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }
        loginButton.setDisable(true);
        statusLabel.setText("Authenticating...");

        Task<Boolean> authTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                String url = "jdbc:mysql://localhost/banksystem";
                try (Connection conn = DriverManager.getConnection(url, username, password)) {
                    return true;
                } catch (SQLException e) {
                    return false;
                }
            }
        };
        authTask.setOnSucceeded(e -> {
            loginButton.setDisable(false);
            if (authTask.getValue()) {
                HelloApplication.DB = new DBConnection(username, password);
                String privilege = "ALL_PRIVILEGES";
                openMainWindow(username, privilege);
            } else {
                statusLabel.setText("Invalid username or password.");
            }
        });
        authTask.setOnFailed(e -> {
            loginButton.setDisable(false);
            statusLabel.setText("Connection error: " + authTask.getException().getMessage());
        });
        new Thread(authTask).start();
    }

    private void openMainWindow(String username, String privilege) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank.fxml"));
            Scene scene = new Scene(loader.load());
            BankController controller = loader.getController();
            controller.setUserInfo(username, privilege);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bank Management System");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
