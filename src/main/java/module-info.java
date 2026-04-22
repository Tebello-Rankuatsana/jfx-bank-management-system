module ranks.bank {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ranks.bank to javafx.fxml;
    exports ranks.bank;
}