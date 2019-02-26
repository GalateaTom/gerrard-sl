import java.text.DecimalFormat;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Settlement {

    private final Customer buyCustomer;
    private final String ticker;
    private final int matchQuantity;
    private final Customer sellCustomer;
    private final int dateOfSettlement;
    private final double matchPrice;

    public String toString(){
        DecimalFormat numberFormat = new DecimalFormat("#.00");

        String buyPbFee = numberFormat.format(this.buyCustomer.getPrimeBroker().getPrimeBrokerFees()*matchPrice);
        String buyEbFee = numberFormat.format(this.buyCustomer.getExecutingBroker().getExecutingBrokerFees()*matchPrice);

        String sellPbFee = numberFormat.format(this.sellCustomer.getPrimeBroker().getPrimeBrokerFees()*matchPrice);
        String sellEbFee = numberFormat.format(this.sellCustomer.getExecutingBroker().getExecutingBrokerFees()*matchPrice);

        double deductedCashRecieved = this.matchPrice-((this.sellCustomer.getPrimeBroker().getPrimeBrokerFees()*this.matchPrice) +
            (sellCustomer.getExecutingBroker().getExecutingBrokerFees()*matchPrice));
        String deductedCashRecievedString = numberFormat.format(deductedCashRecieved);

        return this.buyCustomer.getName()+","+
            this.ticker+ ","+
            Integer.toString(this.matchQuantity)+","+
            buyCustomer.getPrimeBroker().getName()+":"+
            buyPbFee+","+
            buyCustomer.getExecutingBroker().getName()+":"+
            buyEbFee+","+

            this.sellCustomer.getName()+","+

            deductedCashRecievedString+","+

            sellCustomer.getPrimeBroker().getName()+":"+
            sellPbFee+","+
            sellCustomer.getExecutingBroker().getName()+":"+
            sellEbFee+","+

            Integer.toString(this.dateOfSettlement);
    }

}
