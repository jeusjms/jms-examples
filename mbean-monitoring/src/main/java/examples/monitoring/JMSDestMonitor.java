package examples.monitoring;

import jeus.jms.server.mbean.JMSDestinationResourceMBean;
import jeus.jms.server.mbean.JMSQueueDestinationResourceMBean;
import jeus.jms.server.mbean.JMSTopicDestinationResourceMBean;
import jeus.jms.server.mbean.stats.JMSDestinationStats;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author donghwan
 */
public class JMSDestMonitor extends JEUSMBeanMonitor {
    private final String destName;
    private JMSDestinationResourceMBean dest;

    public JMSDestMonitor(String destName) {
        this.destName = destName;
    }

    private void printAllJMSDestList() throws MalformedObjectNameException, IOException {
        Set<ObjectName> objectNames = query("JMSDestinationResource", "*");

        for(Iterator i = objectNames.iterator(); i.hasNext();) {
            System.out.println("[MBean] " + i.next());
        }
    }

    private void getJMSDestResource() throws Exception {
        Set<ObjectName> objectNames = query("JMSDestinationResource", "name=" + destName + ",*");
        for(ObjectName objectName : objectNames) {
            String selectedDest = objectName.getKeyProperty("name");
            if (destName.equals(selectedDest)) {
                dest = (JMSDestinationResourceMBean) newProxyInstance(mbeanServer, objectName, JMSQueueDestinationResourceMBean.class, JMSTopicDestinationResourceMBean.class);
                return;
            }
        }

        throw new Exception(destName + " not found");
    }

//    private JMSQueueDestinationResourceMBean getJMSQueueResource() throws Exception {
//        JMSDestinationResourceMBean mBean = getJMSDestResource(destName);
//        if (mBean.isQueue())
//            return (JMSQueueDestinationResourceMBean) mBean;
//
//        throw new Exception(destName + " is not queue");
//    }
//
//    private JMSTopicDestinationResourceMBean getJMSTopicResource(String destName) throws Exception {
//        JMSDestinationResourceMBean mBean = getJMSDestResource(destName);
//        if (!mBean.isQueue())
//            return (JMSTopicDestinationResourceMBean) mBean;
//
//        throw new Exception(destName + " is not queue");
//    }

    long prevProduced = 0;
    long prevDelivered = 0;

    private static final double SECOND_FACTOR = 1000000000.0;

    @Override
    public String getStat(long elased, long running) throws Exception {
        getJMSDestResource();
        JMSDestinationStats stats = dest.getstats();
        long produced = stats.getMessageCount().getCount();
        long delivered = stats.getDeliveredMessageCount().getCount();

        double produceThroughput = produced * SECOND_FACTOR / running;
        double produceDeltaThroughput = (produced - prevProduced) * SECOND_FACTOR / elased;
        double deliveredThroughput = delivered * SECOND_FACTOR / running;
        double deliveredDeltaThroughput = (delivered - prevDelivered) * SECOND_FACTOR / elased;

        prevProduced = produced;
        prevDelivered = delivered;

        return produceThroughput + "," + produceDeltaThroughput + "," + deliveredThroughput + "," + deliveredDeltaThroughput;
    }

    public void checkConnection() throws Exception {
        getJMSDestResource();
    }
}
