import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@ToString
@Builder
public class CustomerCsvFields {
    private final String clientName;
    private final String executingBroker;
    private final String primeBroker;
}
