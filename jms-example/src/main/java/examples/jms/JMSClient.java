package examples.jms;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.*;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class JMSClient {
    Context jndiContext = null;
    ConnectionFactory connectionFactory = null;
    Connection connection;
    private final HashMap<String, Destination> destinations = new HashMap<String, Destination>();
    private final HashSet<JMSMessageHandler> sessions = new HashSet<JMSMessageHandler>();

    private long estimatedCompleteTime = 0;

    public JMSClient(String host, String port, String connectionFactoryName) throws NamingException, JMSException {
        this(host, port, connectionFactoryName, "jeus", "jeus");
    }

    public JMSClient(String host, String port, String connectionFactoryName, String user, String passwd) throws NamingException, JMSException {
        this("jeus.jndi.JNSContextFactory", "jeus.jndi.jns.url", host, port, connectionFactoryName, "jeus", "jeus");
    }

    public JMSClient(String initContextFac, String packagePrefix, String host, String port, String connectionFactoryName, String user, String passwd)
            throws NamingException, JMSException {
        try {
            Hashtable ht = new Hashtable();
            ht.put(Context.INITIAL_CONTEXT_FACTORY, initContextFac);
            ht.put(Context.URL_PKG_PREFIXES, packagePrefix);
            ht.put(Context.PROVIDER_URL, host + ":" + port );
            ht.put(Context.SECURITY_PRINCIPAL, user);
            ht.put(Context.SECURITY_CREDENTIALS, passwd);

            jndiContext = new InitialContext(ht);

            connectionFactory = (ConnectionFactory) jndiContext.lookup(connectionFactoryName);
            createConnection();
        } catch (NamingException e) {
            System.out.println("Could not create JNDI API " + "context: " + e.toString());
            throw e;
        }
    }

    private Connection createConnection() throws JMSException {
        if (connectionFactory == null)
            throw new JMSException("Connection factory is not ready");
        connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    public Destination getDestination(String destName) throws NamingException {
        Destination destination = destinations.get(destName);
        if (destination == null) {
            synchronized (destinations) {
                destination = destinations.get(destName);
                if (destination == null) {

                    try {
                        destination = (Destination) jndiContext.lookup(destName);
                        destinations.put(destName, destination);
                    } catch (NamingException e) {
                        System.out.println("Could not look up destination: " + destName);
                        throw e;
                    }
                }
            }
        }

        return destination;
    }


    public void produce(String destName, long duration, String type, String contents) throws NamingException, JMSException {
        produce(destName, duration, type, contents, -1);
    }

    public void produce(String destName, long duration, String type, String contents, long interval) throws NamingException, JMSException {
        JMSProducer producer = new JMSProducer(connection);
        producer.setInterval(interval);
        producer.init(getDestination(destName));

        sessions.add(producer);
        estimatedCompleteTime = Math.max(estimatedCompleteTime, System.currentTimeMillis() + duration);

        producer.startProcess(duration, type, contents);
    }

    public void consume(String destName, long duration, String type, String contents, boolean async) throws NamingException, JMSException, FileNotFoundException {
        consume(destName, duration, type, contents, async, -1);
    }

    public void consume(String destName, long duration, String type, String contents, boolean async, long interval) throws NamingException, JMSException, FileNotFoundException {
        JMSConsumer consumer = new JMSConsumer(connection, async);
        consumer.setInterval(interval);
        consumer.init(getDestination(destName));

        sessions.add(consumer);
        estimatedCompleteTime = Math.max(estimatedCompleteTime, System.currentTimeMillis() + duration);

        consumer.startProcess(duration, type, contents);
    }

    public void close() {
        close(false);
    }

    public void close(boolean force) {
        long current = System.currentTimeMillis();

        boolean isEnded = false;
        int count = 0;
        while (!isEnded) {
            try {
                long sleepTime = 1000;
                if (estimatedCompleteTime > current)
                    sleepTime = estimatedCompleteTime - current;

                Thread.sleep(sleepTime);
                System.out.println("checking remain jobs");
                isEnded = true;
                if (!force) {
                    for (JMSMessageHandler session : sessions) {
                        if (session.isEnd()) {
                            count += session.getCount();
                        } else {
                            isEnded = false;
                        }

                    }
                }
            } catch (InterruptedException ignore) {
            }

            current = System.currentTimeMillis();
        }

        sessions.clear();
        System.out.println("done-" + count);

        try {
            connection.close();
            destinations.clear();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
