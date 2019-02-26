import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class main {

    public static void main(String[] args) throws IOException {
        LOGGER.info("Initialising participating entities");
        HashMap<String, BrokerDealer> brokerDealersHashMap = InitialiseEntities.addBrokerDealers();
        HashMap<String, Customer> customerHashMap = InitialiseEntities.addCustomers(brokerDealersHashMap);
        HashSet<String> tickerSet = InitialiseEntities.addTickers();
        HashSet<Integer> orderIdSet = new HashSet<>();
        Exchange exchange = new Exchange();

        ReadWriteFiles readWriteFiles = ReadWriteFiles.builder()
            .brokerDealerHashMap(brokerDealersHashMap)
            .customerHashMap(customerHashMap)
            .tickerSet(tickerSet)
            .orderIdSet(orderIdSet)
            .exchange(exchange)
            .build();

        String ordersFileName = "src/main/resources/Program Arguments/Orders/orders";
        for (int i = 1; i <= 4; i++ ) { //Simulates the change of days
            LOGGER.info("Start of market day");
            ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);
            LOGGER.info("Agreements completed for the day");
            ArrayList<Agreement> missionsArrayList = readWriteFiles.readMissionsFile();
            ArrayList<Agreement> remainingMissionsArrayList = readWriteFiles.settleAgreements(missionsArrayList);
            LOGGER.info("Settlements completed for the day");
            remainingMissionsArrayList.addAll(agreementArrayList);
            readWriteFiles.writeMissionsToFile(remainingMissionsArrayList);
            LOGGER.info("Missions completed for the day");
            LOGGER.info("End of market day");
            exchange.incrementDate();
        }

        LOGGER.info("End of simulation");
    }

}

