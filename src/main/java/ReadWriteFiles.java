import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.validation.constraints.Null;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Builder
@Log4j2
public class ReadWriteFiles {

    private final Exchange exchange;
    private final HashMap<String, Customer> customerHashMap;
    private final HashMap<String, BrokerDealer> brokerDealerHashMap;
    private final HashSet<String> tickerSet;
    private final HashSet<Integer> orderIdSet;

    /**
     * Imports the csv file as a String and iterates through line-by-line, creating Order objects and determining
     * whether the inputs are valid. If the order is valid then it will be added to the exchange - returning either a
     * matched order pair (agreements), or no match. The agreements are then assembled into an ArrayList which is
     * returned.
     * The reason that more than one matched order can be returned from a single order input is from a new last
     * executed price activating new trigger prices, so newly activated orders are checked to see if they can be matched
     *
     * @param ordersFileName: the csv file contained within a single String variable
     * @return stringToAgreement(String matchedOrders): the matched orders output from the exchange is a concatenated
     * string (similar to the ordersFileName input), therefore stringToAgreement method is used to decompose the
     * concatenated string into Agreement objects, which are contained within an ArrayList
     * @throws IOException
     */
    public ArrayList<Agreement> readOrdersFile(String ordersFileName) throws IOException {
        int date = this.exchange.getDate();

        BufferedReader br = null;
        String line = "";
        String inputCsvFile = ordersFileName + Integer.toString(date) + ".csv";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            LOGGER.debug("Reading file: {}", inputCsvFile);
            br = new BufferedReader(new FileReader(inputCsvFile));

            boolean skipHeader = false;
            while ((line = br.readLine()) != null) {

                if (skipHeader == false) {
                    LOGGER.debug("Skipping Header");
                    skipHeader = true;
                    continue;
                }

                stringBuilder = orderToExchange(line, stringBuilder);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("File not found: {}", inputCsvFile);
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

        return stringToAgreements(stringBuilder.toString());
    }

    /**
     * Once it has been established that the inputs are valid for the order, the order is added to the exchange
     * @param line
     * @param stringBuilder
     * @return
     */
    public  StringBuilder orderToExchange(String line, StringBuilder stringBuilder) {
        try {
            Order newOrder = extractOrderData(line);
            if (checkCustomerInventory(newOrder) == true) {
                String matchingOrderResult = this.exchange.findMatchingOrder(newOrder);
                LOGGER.info("Output Trade: {}", matchingOrderResult);

                if (!matchingOrderResult.equals("No Trades Matched")) {
                    if (stringBuilder.length() != 0) stringBuilder.append(";");
                    stringBuilder.append(matchingOrderResult);
                }

            } else {
                LOGGER.error("Invalid order: {}", newOrder.toString());
            }

        } catch (IllegalArgumentException e){
            LOGGER.error("Order not processed due to unrecognised input");
        }

        return stringBuilder;
    }

    private  boolean checkCustomerInventory(Order newOrder) {
        String direction = newOrder.getDirection();
        if (direction.equals("BUY")) {
            LOGGER.debug("Checking BUY direction viability");
            CashCheck cashCheck = CashCheck.builder()
                .cash(newOrder.getLimitPrice())
                .customer(newOrder.getCustomer())
                .build();
            return newOrder.getCustomer().checkSufficientCash(cashCheck);
        } else if (direction.equals("SELL")) {
            LOGGER.debug("Checking SELL direction viability");
            TickerQuantityPair tickerQuantityPair = TickerQuantityPair.builder()
                .customer(newOrder.getCustomer())
                .quantity(newOrder.getQuantity())
                .ticker(newOrder.getTicker())
                .build();
            return newOrder.getCustomer().checkSufficientShares(tickerQuantityPair);
        } else return false;
    }

    private  Order extractOrderData(String line) throws IllegalArgumentException {
        try {
            String csvSplitBy = ",";
            String[] csvLine = line.split(csvSplitBy);
            if (csvLine.length != 9) throw new IllegalArgumentException("Exactly 9 arguments required, "
                        + csvLine.length + " arguments inputted" );
            String customerName = csvLine[1];

            LOGGER.debug("customerName, {}", customerName);
            Customer customer = this.customerHashMap.get(customerName);

            LOGGER.debug("Creating new Order object from extracted row data");
            Order newOrder = createOrder(csvLine, customer);

            if (!determineInputValidity(newOrder)) throw new IllegalArgumentException();
            this.orderIdSet.add(newOrder.getOrderId());
            LOGGER.info("New Order object created from extracted row data, {}", newOrder.toString());
            return newOrder;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    private  Order createOrder(String[] csvLine, Customer customer) {
        Order newOrder = Order.builder()
            .customer(customer)
            .orderId(stringToInteger(csvLine[0]))
            .direction(csvLine[2])
            .quantity(stringToInteger(csvLine[3]))
            .ticker(csvLine[4])
            .type(typeToString(csvLine[5]))
            .limitPrice(stringToDouble(csvLine[6]))
            .timeInForce(csvLine[7])
            .triggerPrice(stringToDouble(csvLine[8]))
            .build();
        return newOrder;
    }

    private int stringToInteger(String inputString) {
        try {
            return Integer.parseInt(inputString);
        } catch( NumberFormatException e ) {
            LOGGER.error("Invalid string to integer conversion: {}", inputString);
            throw new IllegalArgumentException();
        }
    }

    private  double stringToDouble(String inputString) {
        double returnVal = 0.0;
        if (!inputString.equals("NULL")) {
            try {
                returnVal = Double.parseDouble(inputString);
            } catch( NumberFormatException e ) {
                LOGGER.error("Invalid price input format: {}", inputString);
                throw new IllegalArgumentException();
            }
        } else {
            returnVal = -1.0; // Default value for Market orders
        }
        return returnVal;
    }

    private  String typeToString(String type) {
        if (type.equals("STOP-LIMIT")) {
            return "LIMIT";
        } else if (type.equals("STOP-MARKET")) {
            return "MARKET";
        } else if (type.equals("MARKET") || type.equals("LIMIT") ){
            return type;
        } else {
            LOGGER.error("Invalid order type input: {}", type);
            throw new IllegalArgumentException();
        }

    }

    private  boolean determineInputValidity(Order newOrder) {
        if (!validOrderId(newOrder)) return false;
        if (!validCustomerId(newOrder)) return false;
        if (!validDirection(newOrder)) return false;
        if (!validQuantity(newOrder)) return false;
        if (!validTicker(newOrder)) return false;
        if (!validType(newOrder)) return false;
        if (!validLimitPrice(newOrder)) return false;
        if (!validTimeInForce(newOrder)) return false;
        if (!validTriggerPrice(newOrder)) return false;
        return true;
    }

    private  boolean validOrderId(Order newOrder) {
        try {
            if (orderIdSet.contains(newOrder.getOrderId())) {
                LOGGER.error("Duplicate order id input: {}", newOrder.getOrderId());
                throw new IllegalArgumentException();
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }

    private boolean validLimitPrice(Order newOrder) {
        if (newOrder.getLimitPrice() <= 0 && newOrder.getLimitPrice() != -1) {
            LOGGER.error("Invalid limit price {}", newOrder.getLimitPrice());
            throw new IllegalArgumentException();
        } else {
            return true;
        }
    }

    private boolean validTriggerPrice(Order newOrder) {
        if (newOrder.getTriggerPrice() <= 0 && newOrder.getTriggerPrice() != -1) {
            LOGGER.error("Invalid trigger price {}", newOrder.getTriggerPrice());
            throw new IllegalArgumentException();
        } else {
            return true;
        }
    }

    private boolean validTicker(Order newOrder) {
        try {
            if (!this.tickerSet.contains(newOrder.getTicker())) {
                LOGGER.error("Unrecognised ticker input: {}", newOrder.getTicker());
                throw new IllegalArgumentException();
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }

    private  boolean validCustomerId(Order newOrder) {
        try {
            if (newOrder.getCustomer() == null) {
                LOGGER.error("Unrecognised customer id input: {}", newOrder.getCustomer().getName());
                throw new IllegalArgumentException();
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }

    private  boolean validDirection(Order newOrder) {
        if (!newOrder.getDirection().equals("BUY") && !newOrder.getDirection().equals("SELL")) {
            LOGGER.error("Invalid Direction input: {}", newOrder.getDirection());
            return false;
        }
        return true;
    }

    private  boolean validType(Order newOrder) {
        if (!newOrder.getType().equals("LIMIT") && !newOrder.getType().equals("MARKET")) {
            LOGGER.error("Invalid Type: {}", newOrder.getType());
            return false;
        }
        return true;
    }

    private  boolean validQuantity(Order newOrder) {
        if (newOrder.getQuantity() <= 0) {
            LOGGER.error("Invalid Quantity: {}", newOrder.getQuantity());
            return false;
        }
        return true;
    }

    private  boolean validTimeInForce(Order newOrder) {
        if (!newOrder.getTimeInForce().equals("FOK") && !newOrder.getTimeInForce().equals("GTC")) {
            LOGGER.error("Invalid Time In Force: {}", newOrder.getTimeInForce());
            return false;
        }
        return true;
    }

    public  ArrayList<Agreement> stringToAgreements(String matchedOrders) {
        ArrayList<Agreement> agreementsList = new ArrayList<Agreement>();
        String csvSplitBy = ";";
        String[] csvLine = matchedOrders.split(csvSplitBy);

        if (matchedOrders.length() == 0) return agreementsList;
        for (String agreement : csvLine) {
            String[] agreementLine = agreement.split(",");
            Agreement newAgreement = createAgreement(agreementLine);
            agreementsList.add(newAgreement);
        }
        return agreementsList;
    }

    private  Agreement createAgreement(String[] csvLine) {
        Agreement newAgreement = Agreement.builder()
                .buyCustomer(this.customerHashMap.get(csvLine[0]))
                .sellCustomer(this.customerHashMap.get(csvLine[1]))
                .ticker(csvLine[2])
                .matchQuantity(Integer.parseInt(csvLine[3]))
                .matchPrice(Double.parseDouble(csvLine[4]))
                .dateOfAgreement(Integer.parseInt(csvLine[5]))
                .build();
        return newAgreement;
    }

    /**
     * A pre-existing missions csv file exists which is added to after orders are matched and agreements are made. This
     * method reads that file to determine whether any trades are due settlement.
     * @return
     * @throws IOException
     */
    public  ArrayList<Agreement> readMissionsFile() throws IOException {
        int date = this.exchange.getDate();
        ArrayList<Agreement> missionsList = new ArrayList<Agreement>();
        BufferedReader br = null;
        String line = "";
        String inputCsvFile =
            "src/main/resources/Program Arguments/Missions/missions"
                + Integer.toString(date-1) + ".csv";

        try {
            LOGGER.debug("Reading file: {}", inputCsvFile);
            br = new BufferedReader(new FileReader(inputCsvFile));
            boolean skipHeader = false;
            while ((line = br.readLine()) != null) {
                if (skipHeader == false) {
                    LOGGER.debug("Skipping Header");
                    skipHeader = true;
                    continue;
                }

                String csvSplitBy = ",";
                String[] csvLine = line.split(csvSplitBy);
                Agreement newAgreement = createAgreement(csvLine);
                missionsList.add(newAgreement);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("File not found: {}", inputCsvFile);
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

        return missionsList;
    }

    /**
     * The new agreements are added to the missions file, which will be settled once they mature at the appropriate date
     * @param missionsArrayList
     * @throws IOException
     */
    public  void writeMissionsToFile(ArrayList<Agreement> missionsArrayList) throws IOException {
        FileWriter writer = setupMissionWriter();
        LOGGER.debug("Missions list size: {}", missionsArrayList.size());
        for (Agreement mission : missionsArrayList) {
            writer.append(mission.toString());
            writer.append('\n');
        }
        closeMissionWriter(writer);
    }

    private  FileWriter setupMissionWriter() throws IOException  {
        int date = this.exchange.getDate();
        String outputCsvFile =
            "src/main/resources/Program Arguments/Missions/missions"
                + Integer.toString(date) + ".csv";
        LOGGER.debug("Opening FileWriter for: {}", outputCsvFile);
        FileWriter writer = new FileWriter(outputCsvFile);
        writer.append("BUY CUSTOMER,SELL CUSTOMER,TICKER,MATCH QUANTITY,MATCH PRICE,DATE OF AGREEMENT");
        writer.append('\n');

        return writer;
    }

    private  void closeMissionWriter(FileWriter writer)  throws IOException {
        writer.flush();
        writer.close();
        LOGGER.debug("Closing FileWriter");
    }

    /**
     * Iterates through missions csv file to check whether the current date is equal to T+2. If so, then the mission is
     * settled and added to a separate csv file for settlements
     * @param agreementArrayList
     * @return
     * @throws IOException
     */
    public  ArrayList<Agreement> settleAgreements(ArrayList<Agreement> agreementArrayList) throws IOException {
        int date = this.exchange.getDate();
        FileWriter writer = setupSettlementWriter(date);
        ESMA esma = new ESMA();
        ArrayList<Agreement> missionsForRemoval = new ArrayList<Agreement>();
        for (Agreement agreement : agreementArrayList) {
            if (agreement.getDateOfAgreement()+2 == date) {
                writer.append(esma.facilitateTransaction(agreement).toString());
                writer.append('\n');
                missionsForRemoval.add(agreement);
            }
        }
        closeSettlementWriter(writer);

        for (Agreement mission : missionsForRemoval) agreementArrayList.remove(mission);
        return agreementArrayList;
    }

    private  FileWriter setupSettlementWriter(int date) throws IOException  {
        String outputCsvFile =
            "src/main/resources/Program Arguments/Settlements/settlements"
                + Integer.toString(date) + ".csv";
        LOGGER.debug("Opening Settlement FileWriter for: {}", outputCsvFile);
        FileWriter writer = new FileWriter(outputCsvFile);
        writer.append("BUY ID,TICKER,MATCH QUANTITY,BUY PB,BUY EB,SELL ID,SELL RECEIVED,SELL PB,SELL EB,SETTLEMENT DATE");
        writer.append('\n');
        return writer;
    }

    private  void closeSettlementWriter(FileWriter writer)  throws IOException {
        writer.flush();
        writer.close();
        LOGGER.debug("Closing Settlement FileWriter");
    }
}
