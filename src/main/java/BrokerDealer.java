import com.google.common.base.Ticker;
import com.sun.org.apache.xml.internal.security.Init;
import java.util.HashMap;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@ToString
public class BrokerDealer {

    private String name;
    private HashMap<String, Account> customerAccounts;
    private Account brokerDealerAccount;
    private double primeBrokerFees=0.10;
    private double executingBrokerFees=0.05;

    public BrokerDealer(InitialValues initialValues) {
        this.name = initialValues.getClientName();
        this.customerAccounts = new HashMap<String, Account>();
        this.brokerDealerAccount = new Account();
        initialiseBrokerDealerAccount(initialValues);
    }

    public void initialiseBrokerDealerAccount(InitialValues initialValues){
        this.brokerDealerAccount.cashInitialise(initialValues.getInitialCash());
        this.brokerDealerAccount.stockInitialise(initialValues.getInitialStock());
    }

    public void addCustomer(String customerName) {
        customerAccounts.put(customerName, new Account());
    }

    public void initialiseCustomerAccount(InitialValues clientInitialValues){
        Account customerAccount = this.customerAccounts.get(clientInitialValues.getClientName());
        customerAccount.cashInitialise(clientInitialValues.getInitialCash());
        customerAccount.stockInitialise(clientInitialValues.getInitialStock());
    }

    public void agencyFees(double fees){
        LOGGER.debug("Agency fee for: {}", name);
        this.brokerDealerAccount.addCash(fees);
    }

    public void custodialFees(double fees){
        LOGGER.debug("Custodial fee for: {}", name);
        this.brokerDealerAccount.addCash(fees);
    }

    public void deliverCash(CashTradeDetails cashForDelivery){
        LOGGER.debug("Customer delivering cash: {}", cashForDelivery.getCustomer());
        Account customerAccount = this.customerAccounts.get(cashForDelivery.getCustomer());
        customerAccount.removeCash(cashForDelivery.getCash());
    }

    public void receiveCash(CashTradeDetails cashToReceive){
        LOGGER.debug("Customer receiving cash: {}", cashToReceive.getCustomer());
        Account customerAccount = this.customerAccounts.get(cashToReceive.getCustomer());
        customerAccount.addCash(cashToReceive.getCash());
    }

    public void deliverShares(SharesTradeDetails sharesToDeliver){
        LOGGER.debug("Customer delivering shares: {}", sharesToDeliver.getCustomer());

        Account customerAccount = this.customerAccounts.get(sharesToDeliver.getCustomer());

        customerAccount.removeShares(sharesToDeliver);
    }

    public void receiveShares(SharesTradeDetails sharesToReceive){
        LOGGER.debug("Customer receiving shares: {}", sharesToReceive.getCustomer());
        Account customerAccount = this.customerAccounts.get(sharesToReceive.getCustomer());
        customerAccount.addShares(sharesToReceive);
    }

    public double getCustomerCash(CashCheck cashCheck){
        return customerAccounts.get(cashCheck.getCustomer().getName()).getCashInventory();
    }

    public int getCustomerShares(TickerQuantityPair tickerQuantityPair){
        try {
            int actualQuantity = customerAccounts.get(tickerQuantityPair.getCustomer().getName())
                .getStockInventory()
                .get(tickerQuantityPair.getTicker());
            return actualQuantity;
        } catch (NullPointerException e) {
            LOGGER.warn("No shares matching the input ticker \"{}\" were found in the inventory of customer \"{}\"", tickerQuantityPair.getTicker(),tickerQuantityPair.getCustomer().getName());
            return -1;
        }
    }

}
