import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString
@Value
@Builder
public class TickerQuantityPair {
    private final Customer customer;
    private final String ticker;
    private final int quantity;
}
