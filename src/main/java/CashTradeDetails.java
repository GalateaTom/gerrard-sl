import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CashTradeDetails {
    private final String customer;
    private final double cash;
}
