package examples.test.jms;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class ConsumeTest {
    public static void main(String[] args) {
        try {
            System.setProperty("jeus.jms.level", "FINE");
            JMSClient client = new JMSClient("localhost", "9736", "ConnectionFactory");
            client.consume("ExamplesQueue", 30 * 1000, "bytes", "1024", true);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
