import java.util.HashMap;

import lombok.ToString;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString
public class Account {

    private double cashInventory;
    private HashMap<String, Integer> stockInventory;

    public Account() {
        this.cashInventory = 0.0;
        this.stockInventory = new HashMap<String, Integer>();
    }


    public void cashInitialise(String cash){
        this.cashInventory = Double.parseDouble(cash);
    }

    public void stockInitialise(String stock){
        String stockSplitBy = " ";
        String[] initialStock = stock.split(stockSplitBy);
        for(String tickerValuePair : initialStock) {
            String tickerSplitBy = ":";
            String[] splitTickerValuePair = tickerValuePair.split(tickerSplitBy);
            String ticker = splitTickerValuePair[0];
            int quantity = Integer.parseInt(splitTickerValuePair[1]);

            this.stockInventory.put(ticker, quantity);
        }
    }

    public void addCash(double cash){
        this.cashInventory+=cash;
        LOGGER.debug("Cash added to inventory: {}", cash);
    }

    public void removeCash(double cash){
        this.cashInventory-=cash;
        LOGGER.debug("Cash removed from inventory: {}", cash);
    }

    public void addShares(SharesTradeDetails sharesTradeDetails){
        int currentInventory = this.stockInventory.get(sharesTradeDetails.getTicker());
        this.stockInventory.put(sharesTradeDetails.getTicker(), currentInventory+sharesTradeDetails.getQuantity());
        LOGGER.debug("Shares added to inventory: {} : {}", sharesTradeDetails.getTicker(), sharesTradeDetails.getQuantity());
    }

    public void removeShares(SharesTradeDetails sharesTradeDetails){
        int currentInventory = this.stockInventory.get(sharesTradeDetails.getTicker());
        this.stockInventory.put(sharesTradeDetails.getTicker(), currentInventory+sharesTradeDetails.getQuantity());
        LOGGER.debug("Shares removed from inventory: {} : {}", sharesTradeDetails.getTicker(), sharesTradeDetails.getQuantity());
    }

    public double getCashInventory(){
        return this.cashInventory;
    }

    public HashMap<String, Integer> getStockInventory(){
        return this.stockInventory;
    }
}
