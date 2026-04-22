package ranks.bank;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Account {
    private final SimpleIntegerProperty accountId;
    private final SimpleStringProperty ownerName;
    private final SimpleDoubleProperty balance;

    public Account(int accountId, String ownerName, double balance) {
        this.accountId = new SimpleIntegerProperty(accountId);
        this.ownerName = new SimpleStringProperty(ownerName);
        this.balance = new SimpleDoubleProperty(balance);
    }

    public int getAccountId() { return accountId.get(); }
    public SimpleIntegerProperty accountIdProperty() { return accountId; }

    public String getOwnerName() { return ownerName.get(); }
    public SimpleStringProperty ownerNameProperty() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName.set(ownerName); }

    public double getBalance() { return balance.get(); }
    public SimpleDoubleProperty balanceProperty() { return balance; }
    public void setBalance(double balance) { this.balance.set(balance); }
}