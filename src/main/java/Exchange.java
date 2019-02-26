import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager.Limit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Exchange {

    private MarketOrderQueue buyMarketOrders;
    private MarketOrderQueue sellMarketOrders;
    private LimitOrderList buyLimitOrders;
    private LimitOrderList sellLimitOrders;
    private Map<Integer, Order> stopOrderList;
    private int date;

    public Exchange() {
        this.buyMarketOrders = new MarketOrderQueue();
        this.sellMarketOrders = new MarketOrderQueue();
        this.buyLimitOrders = new LimitOrderList();
        this.sellLimitOrders = new LimitOrderList();
        this.stopOrderList = new HashMap<Integer, Order>();
        this.date = 1;
    }

    /**
     * Take in newly created Order to determine whether there is a matching order which can be turned into an Agreement
     * @param newOrder
     * @return
     */
    public String findMatchingOrder(Order newOrder) {
        Order matchedOrder = createDummyOrder();
        if (newOrder.getType().equals("MARKET")) {
            LOGGER.info("MARKET ORDER Input");
            matchedOrder = marketOrderInput(newOrder);
        } else if (newOrder.getType().equals("LIMIT")) {
            LOGGER.info("LIMIT ORDER Input");
            matchedOrder = limitOrderInput(newOrder);
        }

        if (matchExists(matchedOrder)) {
            return checkedNewOrderNowStopOrders(newOrder, matchedOrder);
        } else {
            return noMatchFound(newOrder);
        }
    }

    /**
     * If an order is matched there will be a new 'last executed price', which in turn may have activated other orders,
     * so these newly activated orders are checked to see if they can be matched before any new orders are added to the
     * exchange
     * @param newOrder
     * @param matchedOrder
     * @return
     */
    private String checkedNewOrderNowStopOrders(Order newOrder, Order matchedOrder) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matchedOrdersToString(newOrder, matchedOrder));
        stringBuilder.append(checkForNewlyActivatedStopOrders());
        LOGGER.info("Size of lists: LIMIT ORDER BUY {}, LIMIT ORDER SELL {}, MARKET ORDER BUY {}, MARKET ORDER SELL {}",
            Integer.toString(buyLimitOrders.size()), Integer.toString(sellLimitOrders.size()) ,
            Integer.toString(buyMarketOrders.size()), Integer.toString(sellMarketOrders.size()) );
        return stringBuilder.toString();
    }

    private String matchedOrdersToString(Order newOrder, Order matchedOrder) {
        ArrayList<Agreement> listOfAgreements = new ArrayList<Agreement>();

        int matchQuantity = newOrder.getQuantity();
        double matchPrice = getMatchedPrice(newOrder, matchedOrder);
        updateLastExecutedPrices(newOrder, matchPrice);

        if (newOrder.getDirection().equals("BUY")) {
            Order buyOrder = newOrder;
            Order sellOrder = matchedOrder;
            Agreement newAgreement = Agreement.builder()
                .buyCustomer(buyOrder.getCustomer())
                .sellCustomer(sellOrder.getCustomer())
                .ticker(newOrder.getTicker())
                .matchQuantity(matchQuantity)
                .matchPrice(matchPrice)
                .dateOfAgreement(this.date)
                .build();
            return newAgreement.toString();
        } else {
            Order sellOrder = newOrder;
            Order buyOrder = matchedOrder;

            Agreement newAgreement = Agreement.builder()
                .buyCustomer(buyOrder.getCustomer())
                .sellCustomer(sellOrder.getCustomer())
                .ticker(newOrder.getTicker())
                .matchQuantity(matchQuantity)
                .matchPrice(matchPrice)
                .dateOfAgreement(this.date)
                .build();
            return newAgreement.toString();
        }

    }

    private String checkForNewlyActivatedStopOrders() {
        StringBuilder stringBuilder = new StringBuilder();
        LOGGER.debug("Checking for newly activated STOP ORDERS");
        for (Map.Entry<Integer, Order> entry : stopOrderList.entrySet()) {
            Order value = entry.getValue();
            Order pseudoOrder = createPseudoOrder(value);
            String newlyMatchedOrder = findMatchingOrder(pseudoOrder);
            if (!newlyMatchedOrder.equals("No Trades Matched")) {
                stringBuilder.append('\n');
                stringBuilder.append(newlyMatchedOrder);

                String csvSplitBy = ",";
                String[] csvLine = newlyMatchedOrder.split(csvSplitBy);
                int buyOrderId = Integer.parseInt(csvLine[0]);
                int sellOrderId = Integer.parseInt(csvLine[1]);
                if (this.stopOrderList.containsKey(buyOrderId)) this.stopOrderList.remove(buyOrderId);
                if (this.stopOrderList.containsKey(sellOrderId)) this.stopOrderList.remove(sellOrderId);
            }
        }

        return stringBuilder.toString();
    }

    private Order limitOrderInput(Order newOrder) {
        LOGGER.debug("Query MARKET ORDER List");
        Order marketMatchedOrder = queryMarketOrderQueues(newOrder);
        if (matchExists(marketMatchedOrder)) {
            LOGGER.debug("Trade matched in MARKET ORDER queue");
            return marketMatchedOrder;
        }
        LOGGER.debug("No suitable trades matched in MARKET ORDER queue");
        return marketOrderInput(newOrder);
    }

    private Order marketOrderInput(Order newOrder) {
        LOGGER.debug("Query LIMIT ORDER List");
        Order limitMatchedOrder = queryLimitOrderLists(newOrder);
        if (matchExists(limitMatchedOrder)) {
            LOGGER.debug("Trade matched in LIMIT ORDER list");
            return limitMatchedOrder;
        }
        LOGGER.debug("No suitable trades matched in LIMIT ORDER list");
        return createDummyOrder();
    }

    private Order queryMarketOrderQueues(Order newOrder) {
        if (newOrder.getDirection().equals("BUY")) {
            LOGGER.debug("BUY LIMIT ORDER input, look into SELL MARKET ORDER queue");
            return sellMarketOrders.findMatchingMarketOrder(newOrder);
        } else if (newOrder.getDirection().equals("SELL")) {
            LOGGER.debug("SELL LIMIT ORDER input, look into BUY MARKET ORDER queue");
            return buyMarketOrders.findMatchingMarketOrder(newOrder);
        } else {
            //Error
            return createDummyOrder();
        }
    }

    private Order queryLimitOrderLists(Order newOrder) {
        if (newOrder.getDirection().equals("BUY")) {
            return this.sellLimitOrders.findMatchingLimitOrder(newOrder);
        } else if (newOrder.getDirection().equals("SELL")) {
            return this.buyLimitOrders.findMatchingLimitOrder(newOrder);
        } else {
            //Error
            return createDummyOrder();
        }
    }

    private void addOrderToExchange(Order newOrder) {
        if (newOrder.getType().equals("MARKET")) addMarketOrderToExchange(newOrder);
        else if (newOrder.getType().equals("LIMIT")) addLimitOrderToExchange(newOrder);
        else {
            //error
        }
    }

    private void addMarketOrderToExchange(Order newOrder) {
        if (newOrder.getDirection().equals("BUY")) buyMarketOrders.addOrderToQueue(newOrder);
        else if (newOrder.getDirection().equals("SELL")) sellMarketOrders.addOrderToQueue(newOrder);
        else {
            //error
        }
        LOGGER.info("MARKET ORDER added to Exchange");
    }

    private void addLimitOrderToExchange(Order newOrder) {
        if (newOrder.getDirection().equals("BUY")) buyLimitOrders.addOrderToList(newOrder);
        else if (newOrder.getDirection().equals("SELL")) sellLimitOrders.addOrderToList(newOrder);
        else {
            //error
        }
        LOGGER.info("LIMIT ORDER added to Exchange");
    }

    public boolean matchExists(Order matchedOrder) {
        if (matchedOrder.getOrderId() != createDummyOrder().getOrderId()) {
            LOGGER.info("ORDER successfully matched");
            return true;
        }
        return false;
    }

    private static Order createPseudoOrder(Order newOrder){
        LOGGER.debug("Creating pseudo Order object from existing order");
        Order pseudoOrder = Order.builder()
            .orderId(newOrder.getOrderId())
            .direction(newOrder.getDirection())
            .quantity(newOrder.getQuantity())
            .ticker(newOrder.getTicker())
            .type(newOrder.getType())
            .limitPrice(newOrder.getLimitPrice())
            .timeInForce("FOK")
            .triggerPrice(newOrder.getTriggerPrice())
            .build();

        return pseudoOrder;
    }

    private void addToStopOrderList(Order newOrder) {
        if (newOrder.getTriggerPrice() != -1.0 && !newOrder.getTimeInForce().equals("FOK")) {
            LOGGER.debug("Order added to stop order list");
            this.stopOrderList.put(newOrder.getOrderId(), newOrder);
        }
    }

    private void fillOrKill(Order newOrder) {
        if (!newOrder.getTimeInForce().equals("FOK")) addOrderToExchange(newOrder);
    }

    private String noMatchFound(Order newOrder) {
        fillOrKill(newOrder);
        addToStopOrderList(newOrder);
        LOGGER.debug("Size of lists: LIMIT ORDER BUY {}, LIMIT ORDER SELL {}, MARKET ORDER BUY {}, MARKET ORDER SELL {}",
            Integer.toString(buyLimitOrders.size()), Integer.toString(sellLimitOrders.size()) ,
            Integer.toString(buyMarketOrders.size()), Integer.toString(sellMarketOrders.size()) );
        return "No Trades Matched";
    }

    private Order createDummyOrder() {
        Order order = Order.builder()
            .orderId(-1)
            .build();
        return order;
    }

    private double getMatchedPrice(Order newOrder, Order matchedOrder) {
        double matchPrice = matchedOrder.getLimitPrice();
        if (matchPrice == -1.0)  matchPrice = newOrder.getLimitPrice();
        return matchPrice;
    }

    private void updateLastExecutedPrices(Order newOrder, double lastExecutedPrice) {
        this.buyMarketOrders.updateLastExecutedPrice(newOrder.getTicker(), lastExecutedPrice);
        this.sellMarketOrders.updateLastExecutedPrice(newOrder.getTicker(), lastExecutedPrice);
        this.buyLimitOrders.updateLastExecutedPrice(newOrder.getTicker(), lastExecutedPrice);
        this.sellLimitOrders.updateLastExecutedPrice(newOrder.getTicker(), lastExecutedPrice);
        LOGGER.info("Last executed price updated to: {}", lastExecutedPrice);
    }

    public int getDate(){
        return this.date;
    }

    public void incrementDate(){
        this.date++;
    }
}
