package examples.test.jms;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class ProduceTest {
    public static void main(String[] args) {
        try {
            System.setProperty("jeus.jms.level", "FINE");
            JMSClient client = new JMSClient("localhost", "9736", "ConnectionFactory");
            client.produce("ExamplesQueue", 30 * 1000, "bytes", "1024", 200);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}