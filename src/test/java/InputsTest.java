import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
@Log4j2
public class InputsTest {

    public ReadWriteFiles testInputsSetup(){
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

        return readWriteFiles;
    }

    @Test
    public void testOrderId() throws IOException {
        /*
        Test to assert that two orders cannot share the same orderId, anticipated output will be
        that the VANGUARD will match a trade with MAN and not BLUECREST, due to this conflict

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        1,CLIENT2,SELL,100,IBM,MARKET,NULL,GTC,NULL
        2,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testOrderId";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testCustomerId() throws IOException {
        /*
        Test to assert that the customer ID must have been statically initialised in order for it to
        be used in an order. CLIENTT is not a valid customer ID. OrderId 1 should match with 3

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENTT,SELL,100,IBM,MARKET,NULL,GTC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testCustomerId";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT2,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testDirection() throws IOException {
        /*
        Test to assert that the direction given is only accepted if either BUY or SELL

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SEL,100,IBM,MARKET,NULL,GTC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testDirection";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testQuantity() throws IOException {
        /*
        Test to assert that the quantity given is valid, '100G' is used, as it should not be possible
        to convert from String to Integer

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SELL,100G,IBM,MARKET,NULL,GTC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testQuantity";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testTicker() throws IOException {
        /*
        Test to assert that only orders with valid tickers are allowed onto the exchange, IBM and GOOG
        are contained within the initialise file, however, T is not, therefore orderId 2 should
        not be added to the exchange:

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SELL,100,T,MARKET,NULL,GTC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testTicker";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testType() throws IOException {
        /*
        Test to assert that only types such as (STOP-)LIMIT, (STOP-)MARKET are permissible

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SELL,100,IBM,MARK,NULL,GTC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testType";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testTimeInForce() throws IOException {
        /*
        Test to assert that only GTC and FOK are permissible inputs for Time in Force

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SELL,100,IBM,MARKET,NULL,GTCC,NULL
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,FOK,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testTimeInForce";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT1,CLIENT3,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testLimitPrice() throws IOException {
        /*

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,SELL,100,IBM,MARKET,NULL,GTC,NULL
        2,CLIENT2,BUY,100,IBM,LIMIT,-102.93,GTC,NULL
        3,CLIENT3,BUY,100,IBM,LIMIT,102.93,GTC,NULL
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testLimitPrice";
        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT3,CLIENT1,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(0).toString());
    }

    @Test
    public void testTriggerPrice() throws IOException {
        /*
        Test to assert that only trigger prices

        ORDER ID,CUSTOMER NAME,DIRECTION,QUANTITY,TICKER,TYPE,LIMIT PRICE,TIME IN FORCE,TRIGGER PRICE
        1,CLIENT1,BUY,100,IBM,LIMIT,102.93,GTC,NULL
        2,CLIENT2,SELL,100,IBM,MARKET,NULL,GTC,-5.0
        3,CLIENT3,SELL,100,IBM,MARKET,NULL,GTC,NULL
        4,CLIENT4,BUY,100,IBM,LIMIT,102.93,GTC,102.92
        5,CLIENT5,SELL,100,IBM,MARKET,NULL,GTC,102.94
         */
        //Given
        ReadWriteFiles readWriteFiles = testInputsSetup();
        String ordersFileName = "src/test/resources/Program Arguments/Orders/testTriggerPrice";

        //When
        ArrayList<Agreement> agreementArrayList = readWriteFiles.readOrdersFile(ordersFileName);

        //Then
        String expectedString = "CLIENT4,CLIENT5,IBM,100,102.93,1";
        Assert.assertEquals(expectedString, agreementArrayList.get(1).toString());
    }

}
