package ranks.bank;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class Transaction {
    private final SimpleIntegerProperty transactionId;
    private final SimpleIntegerProperty originAccountId;
    private final SimpleIntegerProperty destinationAccountId;
    private final SimpleDoubleProperty amount;
    private final SimpleObjectProperty<Timestamp> date;

    public Transaction(int transactionId, int originAccountId, int destinationAccountId, double amount, Timestamp date) {
        this.transactionId = new SimpleIntegerProperty(transactionId);
        this.originAccountId = new SimpleIntegerProperty(originAccountId);
        this.destinationAccountId = new SimpleIntegerProperty(destinationAccountId);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleObjectProperty<>(date);
    }

    public int getTransactionId() { return transactionId.get(); }
    public SimpleIntegerProperty transactionIdProperty() { return transactionId; }

    public int getOriginAccountId() { return originAccountId.get(); }
    public SimpleIntegerProperty originAccountIdProperty() { return originAccountId; }

    public int getDestinationAccountId() { return destinationAccountId.get(); }
    public SimpleIntegerProperty destinationAccountIdProperty() { return destinationAccountId; }

    public double getAmount() { return amount.get(); }
    public SimpleDoubleProperty amountProperty() { return amount; }

    public Timestamp getDate() { return date.get(); }
    public SimpleObjectProperty<Timestamp> dateProperty() { return date; }
}