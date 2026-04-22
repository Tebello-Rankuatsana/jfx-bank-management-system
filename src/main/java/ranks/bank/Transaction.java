package ranks.bank;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class Transaction {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty fromAccountId;
    private final SimpleIntegerProperty toAccountId;
    private final SimpleDoubleProperty amount;
    private final SimpleObjectProperty<Timestamp> date;

    public Transaction(int id, int fromAccountId, int toAccountId, double amount, Timestamp date) {
        this.id = new SimpleIntegerProperty(id);
        this.fromAccountId = new SimpleIntegerProperty(fromAccountId);
        this.toAccountId = new SimpleIntegerProperty(toAccountId);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleObjectProperty<>(date);
    }

    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }
    public int getFromAccountId() { return fromAccountId.get(); }
    public SimpleIntegerProperty fromAccountIdProperty() { return fromAccountId; }
    public int getToAccountId() { return toAccountId.get(); }
    public SimpleIntegerProperty toAccountIdProperty() { return toAccountId; }
    public double getAmount() { return amount.get(); }
    public SimpleDoubleProperty amountProperty() { return amount; }
    public Timestamp getDate() { return date.get(); }
    public SimpleObjectProperty<Timestamp> dateProperty() { return date; }
}
