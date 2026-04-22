module ranks.bank {
    requires javafx.controls;
    requires javafx.fxml;


    opens ranks.bank to javafx.fxml;
    exports ranks.bank;
}