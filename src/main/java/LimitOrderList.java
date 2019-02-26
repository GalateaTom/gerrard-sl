import java.util.HashMap;
import java.util.LinkedList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LimitOrderList extends TriggerRules {
    protected LinkedList<Order> limitOrderList;
    private HashMap<String, Double> lastExecutedPriceMap;

    public LimitOrderList() {
        this.limitOrderList = new LinkedList<Order>();
        this.lastExecutedPriceMap = new HashMap<String, Double>();
    }

    public void addOrderToList(Order newOrder) {
        this.limitOrderList.add(newOrder);
    }

    private void removeOrderFromList(int indexForRemoval) {
        this.limitOrderList.remove(indexForRemoval);
    }

    public int size() {
        return limitOrderList.size();
    }

    public boolean matchingOrderFound(int indexForRemoval) {
        if (indexForRemoval != -1) return true;
        return false;
    }

    private double setStartingPrice(Order newOrder) {
        if (newOrder.getDirection().equals("SELL")) return Double.MIN_VALUE;
        else if (newOrder.getDirection().equals("BUY")) return Double.MAX_VALUE;
        else return 0.0;
    }

    private boolean checkTickerMatch(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getTicker()
            .equals(potentialMatchedOrders.getMatchedOrder().getTicker())) return true;
        return false;
    }

    public Order findMatchingLimitOrder(Order newOrder) {
        double bestPrice = setStartingPrice(newOrder);
        Order matchedOrder = createDummyOrder();
        int index = 0;
        int indexForRemoval = -1;
        double oldPrice = bestPrice;
        for (Order order : this.limitOrderList) {
            PotentialMatchedOrders potentialMatchedOrders = PotentialMatchedOrders.builder()
                .newOrder(newOrder)
                .matchedOrder(order)
                .bestPrice(bestPrice)
                .build();
            if (checkTickerMatch(potentialMatchedOrders)) {

            bestPrice = marketOrLimitOrderInput(potentialMatchedOrders);
            }
            if (bestPrice != oldPrice) { // An updated bestPrice indicates that a more optimum match has been found
                indexForRemoval = index;
                matchedOrder = order;
            }
            index++;
            oldPrice = bestPrice;
        }
        if (matchingOrderFound(indexForRemoval)) removeOrderFromList(indexForRemoval);
        return matchedOrder;
    }

    private double marketOrLimitOrderInput(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getType().equals("LIMIT")) return limitOrderBuyOrSell(potentialMatchedOrders);
        else if (potentialMatchedOrders.getNewOrder().getType().equals("MARKET")) return marketOrderBuyOrSell(potentialMatchedOrders);
        else return potentialMatchedOrders.getBestPrice();
    }

    private double limitOrderBuyOrSell(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getDirection().equals("SELL")) return sellLimitOrderBestPrice(potentialMatchedOrders);
        else if (potentialMatchedOrders.getNewOrder().getDirection().equals("BUY")) return buyLimitOrderBestPrice(potentialMatchedOrders);
        else return potentialMatchedOrders.getBestPrice();
    }

    private double sellLimitOrderBestPrice(PotentialMatchedOrders potentialMatchedOrders) {
        if (isSellLimitPriceSatisfied(potentialMatchedOrders) && triggerPriceSatisfied(potentialMatchedOrders) ) {
            return Double.max(potentialMatchedOrders.getMatchedOrder().getLimitPrice(), potentialMatchedOrders.getBestPrice());
        }
        return potentialMatchedOrders.getBestPrice();
    }

    private double buyLimitOrderBestPrice(PotentialMatchedOrders potentialMatchedOrders) {
        if (isBuyLimitPriceSatisfied(potentialMatchedOrders) && triggerPriceSatisfied(potentialMatchedOrders)) {
            return Double.min(potentialMatchedOrders.getMatchedOrder().getLimitPrice(), potentialMatchedOrders.getBestPrice());
        }
        return potentialMatchedOrders.getBestPrice();
    }

    private boolean isSellLimitPriceSatisfied(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getMatchedOrder().getLimitPrice() >= potentialMatchedOrders.getNewOrder().getLimitPrice() ) return true;
        return false;
    }

    private boolean isBuyLimitPriceSatisfied(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getMatchedOrder().getLimitPrice() <= potentialMatchedOrders.getNewOrder().getLimitPrice() ) return true;
        return false;
    }

    private double marketOrderBuyOrSell(PotentialMatchedOrders potentialMatchedOrders) {
        if (potentialMatchedOrders.getNewOrder().getDirection().equals("SELL")) return sellMarketOrderBestPrice(potentialMatchedOrders);
        else if (potentialMatchedOrders.getNewOrder().getDirection().equals("BUY")) return buyMarketOrderBestPrice(potentialMatchedOrders);
        else return potentialMatchedOrders.getBestPrice();
    }

    private double buyMarketOrderBestPrice(PotentialMatchedOrders potentialMatchedOrders) {
        if (triggerPriceSatisfied(potentialMatchedOrders) ) {
            return Double.min(potentialMatchedOrders.getMatchedOrder().getLimitPrice(), potentialMatchedOrders.getBestPrice());
        }
        return potentialMatchedOrders.getBestPrice();
    }

    private double sellMarketOrderBestPrice(PotentialMatchedOrders potentialMatchedOrders) {
        if (triggerPriceSatisfied(potentialMatchedOrders) ) {
            return Double.max(potentialMatchedOrders.getMatchedOrder().getLimitPrice(), potentialMatchedOrders.getBestPrice());
        }
        return potentialMatchedOrders.getBestPrice();
    }

}
