import lombok.Builder;
import lombok.Setter;
import lombok.Value;

@Builder
@Value
public class Agreement {

    private final Customer buyCustomer;
    private final Customer sellCustomer;
    private final String ticker;
    private final int matchQuantity;
    private final double matchPrice;
    private final int dateOfAgreement;

    public String toString(){
        String sellCust = sellCustomer.getName();
        String ticker = this.ticker;
        String matchQ = Integer.toString(this.matchQuantity);
        return this.buyCustomer.getName()+","
                +this.sellCustomer.getName()+","
                +this.ticker+ ","
                +Integer.toString(this.matchQuantity)
                +","+Double.toString(this.matchPrice)
                +","+this.dateOfAgreement;
    }


}
