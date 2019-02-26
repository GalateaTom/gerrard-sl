import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SharesTradeDetails {
    private final String customer;
    private final String ticker;
    private final int quantity;
}
