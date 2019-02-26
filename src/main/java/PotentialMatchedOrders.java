import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PotentialMatchedOrders {

    private final Order newOrder;
    private final Order matchedOrder;
    private double bestPrice;

}
