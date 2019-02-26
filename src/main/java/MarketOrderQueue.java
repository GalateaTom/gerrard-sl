import java.util.HashMap;
import java.util.LinkedList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MarketOrderQueue extends TriggerRules {

    private LinkedList<Order> marketOrderQueue;
    private HashMap<String, Double> lastExecutedPriceMap;

    public MarketOrderQueue(){
        this.marketOrderQueue = new LinkedList<Order>();
        this.lastExecutedPriceMap = new HashMap<String, Double>();
    }

    public int size(){
        return marketOrderQueue.size();
    }

    public void addOrderToQueue(Order newOrder) {
        this.marketOrderQueue.add(newOrder);

    }

    private boolean tickerMatch(Order newOrder, Order iteratorOrder) {
        return iteratorOrder.getTicker().equals(newOrder.getTicker());
    }

    public Order findMatchingMarketOrder(Order newOrder) {
        int index = 0;
        for(Order order : marketOrderQueue) {
            PotentialMatchedOrders potentialMatchedOrders = PotentialMatchedOrders.builder()
                .newOrder(newOrder)
                .matchedOrder(order)
                .bestPrice(0.0)
                .build();

            if (tickerMatch(newOrder, order) && triggerPriceSatisfied(potentialMatchedOrders)) {
                return secureAndRemoveOrder(order, index);
            }
            index++;
        }
        return createDummyOrder();
    }

    private Order secureAndRemoveOrder(Order iteratorOrder, int index) {
        Order matchedOrder = iteratorOrder;
        this.marketOrderQueue.remove(index);
        return matchedOrder;
    }

}
