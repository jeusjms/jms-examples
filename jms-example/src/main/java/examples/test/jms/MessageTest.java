package examples.test.jms;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class MessageTest {
    static {
        System.setProperty("jeus.jms.log.level","SEVERE");
        System.setProperty("examples.test.consume-performance", "false");
        System.setProperty("jms.test.debug", "true");
    }
    public static void main(String[] args) {

        try {

            JMSClient client2 = new JMSClient("localhost", "9736", "ConnectionFactory");
            for (int i = 0; i < 2; i++) {
                client2.consume("QUEUE1", 5 * 1000, "bytes", "1024", true, 0);
            }

            JMSClient client1 = new JMSClient("localhost", "9736", "ConnectionFactory");
            for (int i = 0; i < 2; i++) {
                client1.produce("QUEUE1", 5 * 1000, "bytes", "102400", 0);
            }

            client1.close();
            client2.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
