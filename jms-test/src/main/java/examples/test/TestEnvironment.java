package examples.test;

/**
 * @author donghwan
 */
public class TestEnvironment {
    public static final String JEUS_HOME;
    public static final String DOMAIN_NAME;
    public static final String SERVER_NAME;
    public static final String USER_NAME;
    public static final String PASSWORD;
    public static final String START_JEUS;
    public static final boolean USE_DISRUPTOR;
    public static final boolean USE_SINGLE_THREAD;
    public static final boolean USE_SINGLE_HANDLER;
    public static final boolean PROFILING;
    public static final boolean FORCE_WEAKEN;
    public static final boolean BATCH_DISTRIBUTE;
    public static final String WORKSPACE;

    public static final String HOST;
    public static final String PORT;

    public static final String INITIAL_CONTEXT;
    public static final String URL_PKG_PREFIXES;

    public static final String CONNECTION_FACTORY;

    public static final long DURATION;// = 10 * 60 * 1000;
    public static final int MESSAGE_SIZE;// = 200 * 1024;
    public static final int SEND_SESSION_COUNT;// = 64;
    public static final int SEND_CLIENT_COUNT;
    public static final int RECEIVE_SESSION_COUNT;// = 64;
    public static final int RECEIVE_CLIENT_COUNT;
    public static final int DESTINATION_COUNT;
    public static final long WARMING_UP_TIME;// = 30 * 1000;
    public static final int PRODUCE_INTERVAL;
    public static final boolean ASYNC_RECEIVE;
    public static final int RING_BUFFER_SIZE;

    public static final String[] DEST_LIST = {"QUEUE1", "QUEUE2", "QUEUE3", "QUEUE4"};

    static {
        WORKSPACE = System.getProperty("examples.test.workspace", "/home/donghwan/workspace/examples");
        JEUS_HOME = System.getProperty("examples.test.jeus-home", System.getenv("JEUS_HOME"));
        DOMAIN_NAME = System.getProperty("examples.test.domain-name", "domain1");
        SERVER_NAME = System.getProperty("examples.test.server-name", "adminServer");
        USER_NAME = System.getProperty("examples.test.user-name", "jeus");
        PASSWORD = System.getProperty("examples.test.password", "jeus");
        START_JEUS = "startDomainAdminServer -domain "+DOMAIN_NAME+" -server "+SERVER_NAME+" -u "+USER_NAME+" -p "+PASSWORD+" -verbose";

        HOST = System.getProperty("examples.test.host", "localhost");
        PORT = System.getProperty("examples.test.port", "9736");

        INITIAL_CONTEXT = System.getProperty("examples.test.initial-context", "jeus.jndi.JNSContextFactory");
        URL_PKG_PREFIXES = System.getProperty("examples.test.url-pkg-prefix", "jeus.jndi.jns.url");

        CONNECTION_FACTORY = System.getProperty("examples.test.connection-factory", "ConnectionFactory");

        USE_DISRUPTOR = Boolean.parseBoolean(System.getProperty("examples.test.use-disruptor", "true"));
        USE_SINGLE_THREAD = Boolean.parseBoolean(System.getProperty("examples.test.use-single-thread", "true"));
        USE_SINGLE_HANDLER = Boolean.parseBoolean(System.getProperty("examples.test.use-single-handler", "true"));
        PROFILING = Boolean.parseBoolean(System.getProperty("examples.test.profiling", "false"));
        FORCE_WEAKEN = Boolean.parseBoolean(System.getProperty("examples.test.force-weaken", "false"));
        BATCH_DISTRIBUTE = Boolean.parseBoolean(System.getProperty("examples.test.batch-distribute", "true"));

        DURATION = Long.parseLong(System.getProperty("examples.test.duration", "300000"));
        MESSAGE_SIZE = Integer.parseInt(System.getProperty("examples.test.message-size", "2048"));
        SEND_SESSION_COUNT = Integer.parseInt(System.getProperty("examples.test.send-session-count", "8"));
        SEND_CLIENT_COUNT = Integer.parseInt(System.getProperty("examples.test.send-client-count", "2"));
        RECEIVE_SESSION_COUNT = Integer.parseInt(System.getProperty("examples.test.receive-session-count", "8"));
        RECEIVE_CLIENT_COUNT = Integer.parseInt(System.getProperty("examples.test.receive-client-count", "2"));
        int destCount = Integer.parseInt(System.getProperty("examples.test.destination-count", "1"));
        DESTINATION_COUNT = destCount < 1 ? 1 : destCount > 4 ? 4 : destCount;
        WARMING_UP_TIME = Long.parseLong(System.getProperty("examples.test.warm-up-time", "60000"));
        PRODUCE_INTERVAL = Integer.parseInt(System.getProperty("examples.test.produce-interval", "10"));
        ASYNC_RECEIVE = Boolean.parseBoolean(System.getProperty("examples.test.async-receive", "true"));
        RING_BUFFER_SIZE = Integer.parseInt(System.getProperty("examples.test.ring-buffer-size", "8"));
    }
}
