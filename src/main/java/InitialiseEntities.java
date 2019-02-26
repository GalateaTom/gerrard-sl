import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class InitialiseEntities {

    /**
     * Utility class used for initialising Customers and Broker/Dealers as each has a unique relationship a client,
     * which must be predefined before orders are added to the exchange. This is so that custodial and execution duties
     * can be correctly delegated and shares and cash flow accurately between participating parties.
     */
    private InitialiseEntities(){ }

    /**
     * Initialises Broker/Dealers: reads a csv of list of Broker Dealers and their cash and stock inventories
     * @return
     */
    public static HashMap<String, BrokerDealer> addBrokerDealers(){
        LOGGER.info("Initialising Broker/Dealer entities");
        HashMap<String, BrokerDealer> brokerDealersHashMap = new HashMap<String, BrokerDealer>();
        String brokerDealerCsvFile = "src/main/resources/Program Arguments/ListOfBrokerDealers.csv";
        BufferedReader br = null;
        String line = "";
        try {
            LOGGER.debug("Reading file: {}", brokerDealerCsvFile);
            br = new BufferedReader(new FileReader(brokerDealerCsvFile));
            boolean skipHeader = false;
            while ((line = br.readLine()) != null) {
                if (skipHeader == false) {
                    LOGGER.debug("Skipping Header");
                    skipHeader = true;
                    continue;
                }

                BrokerDealer brokerDealer = extractBrokerDealers(line);
                brokerDealersHashMap.put(brokerDealer.getName(), brokerDealer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("File not found: {}", brokerDealerCsvFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return brokerDealersHashMap;
    }

    private static BrokerDealer extractBrokerDealers(String inputLine){
        String csvSplitBy = ",";
        String[] csvLine = inputLine.split(csvSplitBy);

        String initialCash = csvLine[1];
        String initialStock = csvLine[2];
        InitialValues initialCashAndStock = InitialValues.builder()
            .clientName(csvLine[0])
            .initialCash(initialCash)
            .initialStock(initialStock)
            .build();

        LOGGER.debug("Creating new BrokerDealer object from extracted row data");
        BrokerDealer brokerDealer = new BrokerDealer(initialCashAndStock);
        LOGGER.debug("New BrokerDealer object created from extracted row data, {}", brokerDealer.toString());

        return brokerDealer;
    }

    /**
     * Initialises Customers: reads a csv of list of customers, with their prime broker, executing broker, and
     * cash/stock inventories
     * @return
     */
    public static HashMap<String, Customer> addCustomers(HashMap<String, BrokerDealer> brokerDealersHashMap){
        LOGGER.info("Initialising Customer entities");
        HashMap<String, Customer> customerHashMap = new HashMap<String, Customer>();

        String customerCsvFile = "src/main/resources/Program Arguments/ListOfCustomers.csv";
        BufferedReader br = null;
        String line = "";
        try {
            LOGGER.debug("Reading file: {}", customerCsvFile);
            br = new BufferedReader(new FileReader(customerCsvFile));
            boolean skipHeader = false;
            while ((line = br.readLine()) != null) {
                if (skipHeader == false) {
                    LOGGER.debug("Skipping Header");
                    skipHeader = true;
                    continue;
                }

                Customer customer = extractCustomers(line, brokerDealersHashMap);
                customerHashMap.put(customer.getName(), customer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("File not found: {}", customerCsvFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return customerHashMap;
    }

    private static Customer extractCustomers(String inputLine, HashMap<String, BrokerDealer> brokerDealersHashMap){
        String csvSplitBy = ",";
        String[] csvLine = inputLine.split(csvSplitBy);

        String clientName = csvLine[0];
        String executingBroker = csvLine[1];
        String primeBroker = csvLine[2];

        CustomerCsvFields customerCsvFields = CustomerCsvFields.builder()
            .clientName(clientName)
            .executingBroker(executingBroker)
            .primeBroker(primeBroker)
            .build();


        String initialCash = csvLine[3];
        String initialStock = csvLine[4];

        InitialValues initialCashAndStock = InitialValues.builder()
            .clientName(clientName)
            .initialCash(initialCash)
            .initialStock(initialStock)
            .build();

        LOGGER.debug("Creating new Customer object from extracted row data");
        Customer customer = new Customer(customerCsvFields, brokerDealersHashMap, initialCashAndStock);
        LOGGER.debug("New Order object Customer from extracted row data, {}", customer.toString());


        return customer;
    }

    public static HashSet<String> addTickers(){
        LOGGER.info("Initialising tickers");
        HashSet<String> tickerSet = new HashSet<>();
        String tickerCsvFile = "src/main/resources/Program Arguments/ListOfTickers.csv";
        BufferedReader br = null;
        String line = "";
        try {
            LOGGER.debug("Reading file: {}", tickerCsvFile);
            br = new BufferedReader(new FileReader(tickerCsvFile));
            boolean skipHeader = false;
            while ((line = br.readLine()) != null) {
                if (skipHeader == false) {
                    LOGGER.debug("Skipping Header");
                    skipHeader = true;
                    continue;
                }
                String ticker = line;
                tickerSet.add(ticker);
                LOGGER.debug("Creating new ticker: {}", ticker);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("File not found: {}", tickerCsvFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return tickerSet;
    }
}
