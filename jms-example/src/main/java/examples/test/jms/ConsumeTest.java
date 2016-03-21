package examples.test.jms;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class ConsumeTest {
    static {
        System.setProperty("jeus.jms.log.level","FINE");
    }
    public static void main(String[] args) {
        try {
            JMSClient client = new JMSClient("localhost", "9736", "ConnectionFactory");
            client.consume("QUEUE1", 30 * 1000, "bytes", "1024", false, 500);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
