import lombok.Builder;
import lombok.Setter;
import lombok.Value;

@Builder
@Value
public class Order {

    private final Customer customer;
    private final int orderId;
    private final String direction;
    private final int quantity;
    private final String ticker;
    private final String type;
    private final double limitPrice;
    private final String timeInForce;
    private final double triggerPrice;

    @Override
    public String toString() {
        return "customer = " + this.customer.getName()
        +    ", orderId = " + Integer.toString(this.orderId)
        + ", direction = " + this.direction
        + ", quantity = " + Integer.toString(this.quantity)
        + ", ticker = " + this.ticker
        + ", type = " + this.type
        + ", limitPrice = " + Double.toString(this.limitPrice)
        + ", timeInForce = " + this.timeInForce
        + ", triggerPrice = " + Double.toString(this.triggerPrice);
    }

}
