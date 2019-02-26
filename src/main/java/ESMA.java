import java.util.HashMap;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ESMA {


    public Settlement facilitateTransaction(Agreement agreement){
        LOGGER.info("New mission to be settled: {}", agreement.toString());
        buySideCash(agreement);
        sellSideCash(agreement);
        sellSideShares(agreement);
        buySideShares(agreement);

        LOGGER.info("Trade details successfully settled");
        return createSettlement(agreement);
    }

    public Settlement createSettlement(Agreement agreement) {
        return Settlement.builder()
            .buyCustomer(agreement.getBuyCustomer())
            .sellCustomer(agreement.getSellCustomer())
            .matchQuantity(agreement.getMatchQuantity())
            .ticker(agreement.getTicker())
            .matchPrice(agreement.getMatchPrice())
            .dateOfSettlement(agreement.getDateOfAgreement()+2)
            .build();
    }

    public void sellSideShares(Agreement agreement) {
        SharesTradeDetails sharesToDeliver = SharesTradeDetails.builder()
            .customer(agreement.getSellCustomer().getName())
            .quantity(agreement.getMatchQuantity())
            .ticker(agreement.getTicker())
            .build();

        agreement.getSellCustomer().deliverShares(sharesToDeliver);
    }

    public void buySideShares(Agreement agreement){
        SharesTradeDetails sharesToReceive = SharesTradeDetails.builder()
            .customer(agreement.getBuyCustomer().getName())
            .quantity(agreement.getMatchQuantity())
            .ticker(agreement.getTicker())
            .build();

        agreement.getBuyCustomer().receiveShares(sharesToReceive);
    }

    public void buySideCash(Agreement agreement){
        Customer buyCustomer = agreement.getBuyCustomer();
        double matchPrice = agreement.getMatchPrice();

        CashTradeDetails cashForDelivery = CashTradeDetails.builder()
            .customer(buyCustomer.getName())
            .cash(matchPrice)
            .build();

        LOGGER.debug("Customer buying shares: {}", buyCustomer.getName().toString());
        buyCustomer.deliverCash(cashForDelivery);
        double buyPrimeBrokerFees = buyCustomer.getPrimeBroker().getPrimeBrokerFees();
        double buyExecutingBrokerFees = buyCustomer.getExecutingBroker().getExecutingBrokerFees();
        double buyCustodialFees = matchPrice*buyPrimeBrokerFees;
        double buyAgencyFees = matchPrice*buyExecutingBrokerFees;
        buyCustomer.getPrimeBroker().custodialFees(buyCustodialFees);
        buyCustomer.getExecutingBroker().agencyFees(buyAgencyFees);
    }

    public void sellSideCash(Agreement agreement){
        Customer sellCustomer = agreement.getSellCustomer();
        double matchPrice = agreement.getMatchPrice();

        LOGGER.debug("Customer selling shares: {}", sellCustomer.getName().toString());

        double sellPrimeBrokerFees = sellCustomer.getPrimeBroker().getPrimeBrokerFees();
        double sellExecutingBrokerFees = sellCustomer.getExecutingBroker().getExecutingBrokerFees();
        double sellCustodialFees = matchPrice*sellPrimeBrokerFees;
        double sellAgencyFees = matchPrice*sellExecutingBrokerFees;
        sellCustomer.getPrimeBroker().custodialFees(sellCustodialFees);
        sellCustomer.getExecutingBroker().agencyFees(sellAgencyFees);

        double remainingCashPayment = matchPrice - sellCustodialFees - sellAgencyFees;

        CashTradeDetails cashToReceive = CashTradeDetails.builder()
            .customer(sellCustomer.getName())
            .cash(remainingCashPayment)
            .build();

        sellCustomer.receiveCash(cashToReceive);
    }


}
