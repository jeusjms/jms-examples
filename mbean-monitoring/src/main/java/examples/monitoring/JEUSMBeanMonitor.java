package examples.monitoring;

import com.sun.management.OperatingSystemMXBean;
import jeus.jms.server.mbean.JMSDestinationResourceMBean;
import jeus.jms.server.mbean.JMSQueueDestinationResourceMBean;
import jeus.jms.server.mbean.JMSTopicDestinationResourceMBean;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author donghwan
 */
public abstract class JEUSMBeanMonitor {
    private static String hostname = "localhost:9736";
    private static String username = "jeus";
    private static String password = "jeus";
    private static String targetName = "adminServer";

    private static InitialContext ctx;
    private static JMXConnector connector;
    protected static MBeanServerConnection mbeanServer;

    public static void main(String[] args) {
        JEUSMBeanMonitor monitor = new JEUSMBeanMonitor() {
            @Override
            public String getStat(long elased, long running) throws Exception {
                return null;
            }
        };

        try {
            JEUSMBeanMonitor.connect_jndi();
            monitor.printAllMBeanList();
            JEUSMBeanMonitor.close();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void printAllMBeanList() throws MalformedObjectNameException, IOException {
        // Step 3. Query
        ObjectName jeusScope = null;
        jeusScope = new ObjectName("JEUS:*");
        Set objectNames = mbeanServer.queryNames(jeusScope, null);

        // Step 4. Handling the Query Result
        for(Iterator i = objectNames.iterator(); i.hasNext();) {
            System.out.println("[MBean] " + i.next());
        }
    }

    protected Set<ObjectName> query(String type, String query) throws MalformedObjectNameException, IOException {
        if (query == null || query.length() == 0)
            query = "*";

        ObjectName queryName = new ObjectName("JEUS:j2eeType=JeusService,jeusType=" + type + ",JMXManager=adminServer," +
                "J2EEServer=adminServer," + query);
        return mbeanServer.queryNames(queryName, null);
    }


    protected static Object newProxyInstance(MBeanServerConnection mbeanserverconnection, ObjectName objectName, Class class1, Class class2) {
        MBeanServerInvocationHandler invocationHandler = new MBeanServerInvocationHandler(mbeanserverconnection, objectName);
        Class[] aclass = new Class[]{class1, class2};
        return Proxy.newProxyInstance(class1.getClassLoader(), aclass, invocationHandler);
    }

    public void checkConnection() throws Exception {
        printAllMBeanList();
    }

    public static void connect_jndi() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "jeus.jndi.JNSContextFactory");
        env.put(Context.PROVIDER_URL, hostname);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        // Step 2. Getting MBeanServerConnection
        ctx = new InitialContext(env);
        connector = (JMXConnector)ctx.lookup("mgmt/rmbs/" + targetName);
        mbeanServer = connector.getMBeanServerConnection();
    }

    public static void connect_jmx() throws IOException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "jeus.jndi.JNSContextFactory");
        env.put(Context.PROVIDER_URL, hostname);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        // Step 2. Getting MBeanServer
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/" +
                "RMIConnector");
        connector = JMXConnectorFactory.newJMXConnector(url,env);
        // connect to JMXConnectorServer
        connector.connect();
    }

    public static void close() throws IOException, NamingException {
        if (connector != null) {
            System.out.println("CLOSING CONNECTION");
            connector.close();
        }

        if (ctx != null) {
            System.out.println("CLOSING CONTEXT");
            ctx.close();
        }
    }

    public abstract String getStat(long elased, long running) throws Exception;
}
