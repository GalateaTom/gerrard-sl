import java.util.HashMap;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class TriggerRules {
    private HashMap<String, Double> lastExecutedPriceMap;

    public TriggerRules() {
        this.lastExecutedPriceMap = new HashMap<String, Double>();
    }


    protected boolean triggerPriceSatisfied(PotentialMatchedOrders potentialMatchedOrders) {
        // Trying to minimise losses; BUY order only becomes active when trigger price falls BELOW lastExecutedPrice
        // Provided lastExecutedPrice has been initialised
        if (newStopOrderOnly(potentialMatchedOrders)) {
            LOGGER.debug("New order IS a STOP ORDER and proposed matched order is NOT a STOP ORDER");
            if (potentialMatchedOrders.getNewOrder().getDirection().equals("BUY"))
                return buyOnlyStopOrder(potentialMatchedOrders.getNewOrder());
            else  if (potentialMatchedOrders.getNewOrder().getDirection().equals("SELL"))
                return sellOnlyStopOrder(potentialMatchedOrders.getNewOrder());

        } else if (matchedStopOrderOnly(potentialMatchedOrders)) {
            LOGGER.debug("New order is NOT a STOP ORDER and proposed matched order IS a STOP ORDER");
            if (potentialMatchedOrders.getNewOrder().getDirection().equals("BUY"))
                return sellOnlyStopOrder(potentialMatchedOrders.getMatchedOrder());
            else  if (potentialMatchedOrders.getNewOrder().getDirection().equals("SELL"))
                return buyOnlyStopOrder(potentialMatchedOrders.getMatchedOrder());

        } else if (neitherStopOrders(potentialMatchedOrders)) {
            LOGGER.debug("New order is NOT a STOP ORDER and proposed matched order is NOT a STOP ORDER");
            return true;

        } else if (bothStopOrders(potentialMatchedOrders)) {
            LOGGER.debug("New order IS a STOP ORDER and proposed matched order IS a STOP ORDER");
            if (potentialMatchedOrders.getNewOrder().getDirection().equals("BUY"))
                return buyAndSellStopOrders(potentialMatchedOrders.getNewOrder(),
                    potentialMatchedOrders.getMatchedOrder());
            else  if (potentialMatchedOrders.getNewOrder().getDirection().equals("SELL"))
                return buyAndSellStopOrders(potentialMatchedOrders.getMatchedOrder(),
                    potentialMatchedOrders.getNewOrder());
        }
        return false;
    }

    private boolean buyOrderTriggerCondition(Order order) {
        // Trying to minimise losses; BUY order only becomes active when trigger price falls BELOW lastExecutedPrice
        if (order.getTriggerPrice() < getLastExecutedPrice(order.getTicker())) return true;
        return false;
    }

    private boolean sellOrderTriggerCondition(Order order) {
        // Trying to maximise gains; SELL orders only become active when trigger price rises ABOVE lastExecutedPrice
        if (order.getTriggerPrice() > getLastExecutedPrice(order.getTicker())) return true;
        return false;
    }

    private boolean buyOnlyStopOrder(Order order) {
        if (buyOrderTriggerCondition(order) && isLastExecutedPriceInitialised(order)) return true;
        return false;
    }

    private boolean sellOnlyStopOrder(Order order) {
        if (sellOrderTriggerCondition(order) && isLastExecutedPriceInitialised(order)) return true;
        return false;
    }

    private boolean buyAndSellStopOrders(Order buyOrder, Order sellOrder) {
        if (buyOrderTriggerCondition(buyOrder) && sellOrderTriggerCondition(sellOrder) &&
            isLastExecutedPriceInitialised(buyOrder)) return true;
        return false;
    }

    private boolean isLastExecutedPriceInitialised(Order order) {
        String ticker = order.getTicker();
        if  (getLastExecutedPrice(ticker) != -1.0) return true;
        return false;
    }

    private boolean newStopOrderOnly(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getTriggerPrice() != -1.0 &&
            potentialMatchedOrders.getMatchedOrder().getTriggerPrice() == -1.0) return true;
        return false;
    }

    private boolean matchedStopOrderOnly(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getTriggerPrice() == -1.0 &&
            potentialMatchedOrders.getMatchedOrder().getTriggerPrice() != -1.0) return true;
        return false;
    }

    private boolean bothStopOrders(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getTriggerPrice() != -1.0 &&
            potentialMatchedOrders.getMatchedOrder().getTriggerPrice() != -1.0) return true;
        return false;
    }

    private boolean neitherStopOrders(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getTriggerPrice() == -1.0 &&
            potentialMatchedOrders.getMatchedOrder().getTriggerPrice() == -1.0) return true;
        return false;
    }

    protected Order createDummyOrder() {
        Order order = Order.builder()
            .orderId(-1)
            .build();
        return order;
    }

    protected void updateLastExecutedPrice(String ticker, double lastExecutedPrice){
        if (this.lastExecutedPriceMap.get(ticker) == null) {
            this.lastExecutedPriceMap.put(ticker, lastExecutedPrice);
        } else {
            this.lastExecutedPriceMap.replace(ticker, lastExecutedPrice);
        }
    }

    protected double getLastExecutedPrice(String ticker) {
        if (this.lastExecutedPriceMap.get(ticker) == null) {
            return -1.0;
        } else {
            return this.lastExecutedPriceMap.get(ticker);
        }
    }
}
