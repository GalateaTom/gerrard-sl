import java.util.HashMap;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ReadFileArg {
    private final Exchange exchange;
    private final HashMap<String, Customer> customerHashMap;
}
