import com.google.common.base.Ticker;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@ToString
public class Customer {

    private String name;
    private BrokerDealer executingBroker;
    private BrokerDealer primeBroker;

    public Customer(CustomerCsvFields customerCsvFields, HashMap<String, BrokerDealer> mapOfBrokerDealers, InitialValues initialValues) {
        this.name = customerCsvFields.getClientName();
        this.executingBroker = mapOfBrokerDealers.get(customerCsvFields.getExecutingBroker());
        this.primeBroker = mapOfBrokerDealers.get(customerCsvFields.getPrimeBroker());
        this.primeBroker.addCustomer(name);
        this.primeBroker.initialiseCustomerAccount(initialValues);
    }

    public void deliverCash(CashTradeDetails cashForDelivery){
        this.primeBroker.deliverCash(cashForDelivery);
    }

    public void receiveCash(CashTradeDetails cashToReceive){
        this.primeBroker.receiveCash(cashToReceive);
    }

    public void deliverShares(SharesTradeDetails sharesToDeliver){
        this.primeBroker.deliverShares(sharesToDeliver);
    }

    public void receiveShares(SharesTradeDetails sharesToReceive){
        this.primeBroker.receiveShares(sharesToReceive);
    }

    public boolean checkSufficientCash(CashCheck cashCheck){
        if (this.primeBroker.getCustomerCash(cashCheck) < cashCheck.getCash() ) return false;
        return true;
    }

    public boolean checkSufficientShares(TickerQuantityPair tickerQuantityPair){
        if (this.primeBroker.getCustomerShares(tickerQuantityPair) == -1 ||
            this.primeBroker.getCustomerShares(tickerQuantityPair) < tickerQuantityPair.getQuantity())
            return false;

        return true;
    }


}
