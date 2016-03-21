package examples.test.jms;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class ProduceTest {
    static {
        System.setProperty("jeus.jms.log.level","FINE");
    }

    public static void main(String[] args) {
        try {
            for (int i = 0; i < 1; i++) {
                JMSClient client = new JMSClient("localhost", "9736", "ConnectionFactory");
                client.produce("QUEUE1", 10 * 1000, "bytes", "1024", 100);
            }
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
