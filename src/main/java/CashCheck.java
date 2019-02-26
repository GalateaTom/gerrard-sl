import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString
@Value
@Builder
public class CashCheck {
    private final Customer customer;
    private final double cash;
}
