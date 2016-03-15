package examples.test;

import examples.monitoring.JMSDestMonitor;
import examples.monitoring.JMSEngineBootListener;
import examples.monitoring.MonitorExecutor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author donghwan
 */
public class TestExecutor {
    private static Process jeusProcess;

    private static final AtomicInteger testSequence = new AtomicInteger(0);

    public static void log (String message) {
        System.out.println("[" + new Date(System.currentTimeMillis()) + "] " + message);
    }
    
    public static String timeLongToString(long time) {
        String result = "";
        long mod = time % 1000;
        if (mod != 0)
            result = mod  + "ms";
        time = time / 1000;
        if (time == 0)
            return result;
        
        mod = time % 60;
        if (mod != 0)
            result = mod  + "s " + result;
        time = time / 60;
        if (time == 0)
            return result;

        mod = time % 60;
        if (mod != 0)
            result = mod  + "m " + result;
        time = time / 60;
        if (time == 0)
            return result;

        result = mod  + "h " + result;

        return result;
    }

    public static void main(String[] args) {
        try {
            // start test
            printEnv();
            doTest();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printEnv() throws IOException {
        log("================== START TEST ===================");
        log("DURATION: " + timeLongToString(TestEnvironment.DURATION));
        log("MESSAGE SIZE: " + TestEnvironment.MESSAGE_SIZE);
        log("SENDER: " + TestEnvironment.SEND_SESSION_COUNT);
        log("RECEIVER: "+ TestEnvironment.RECEIVE_SESSION_COUNT);
        log("WARM-UP TIME: " + timeLongToString(TestEnvironment.WARMING_UP_TIME));
        log("JEUS HOME: " + TestEnvironment.JEUS_HOME);
        log("==================================================");
    }

    private static void doTest() throws IOException, InterruptedException {
        startJeus();

        Thread.sleep(TestEnvironment.WARMING_UP_TIME);
        MonitorExecutor monitor = new MonitorExecutor("TEST", System.currentTimeMillis());
        for (int i = 0; i < TestEnvironment.DESTINATION_COUNT; i++)
            monitor.registerMonitor(new JMSDestMonitor(TestEnvironment.DEST_LIST[i]));
        monitor.setJMSEngineListener(new JMSEngineMonitor());
        monitor.start();

        Thread.sleep(TestEnvironment.DURATION);

        synchronized (testSequence) {
            while (1 == testSequence.get()) {
                try {
                    testSequence.wait(10000);
                    log("TEST is progressing...");
                } catch (InterruptedException ignore) {
                }
            }
        }

        monitor.quit();
    }

    public static void startJeus() throws IOException {
        if (jeusProcess != null)
            stopJeus();
        truncateStore();
        String option = "";
        if (TestEnvironment.USE_SINGLE_HANDLER)
            option += "-Djeus.jms.server.use-single-handler=true ";
        if (TestEnvironment.USE_SINGLE_THREAD)
            option += "-Djeus.jms.server.use-single-thread=true ";
        if (!TestEnvironment.USE_DISRUPTOR) {
            option = "-Djeus.jms.server.produce.use-disruptor=false";
        }

        String command = TestEnvironment.JEUS_HOME + "/bin/" + TestEnvironment.START_JEUS + " " + option;
        log("STARTING JEUS: " + command);
        jeusProcess = Runtime.getRuntime().exec(command);
        log("JEUS is started: " + jeusProcess);
    }

    public static void stopJeus() throws IOException {
        if (jeusProcess == null)
            return;

        log("STOPPING JEUS");
        Runtime.getRuntime().exec(TestEnvironment.JEUS_HOME + "/bin/jeusadmin -u "+TestEnvironment.USER_NAME+" -p "+TestEnvironment.PASSWORD+" 'local-shutdown'");
        try {
            jeusProcess.destroy();
        } catch (Throwable ignore) {
        } finally {
            jeusProcess = null;
        }

    }

    public static Process runJava(String options, String className) throws IOException {
        List<String> commands = new ArrayList<>(3);
        commands.add(System.getenv("JAVA7") + "/bin/java");
        if (options != null)
            commands.add(options);
        if (className != null)
            commands.add(className);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        return processBuilder.start();
    }

    private static Process runJava(String classPath, String className, String... parameters) throws IOException {
        List<String> commands = new ArrayList<>(5);
        commands.add(System.getenv("JAVA7") + "/bin/java");
        if (classPath != null) {
            commands.add("-classpath");
            commands.add(classPath);
        }
        if (className != null)
            commands.add(className);

        if (parameters != null) {
            Collections.addAll(commands, parameters);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        log("EXECUTING JAVA: " + className);
        return processBuilder.start();
    }

    public static Process startSend(String destName) throws IOException {
        String classPath = TestEnvironment.WORKSPACE+"/jms-test/target/jms-test-1.0-SNAPSHOT.jar:"+TestEnvironment.JEUS_HOME+"/lib/client/jclient.jar:"+TestEnvironment.JEUS_HOME+"/lib/system/jms.jar";
        String className = "examples.test.PerformanceTest";

        return runJava(classPath, className,
                "dest=" + destName,
                "duration=" + TestEnvironment.DURATION,
                "type=bytes",
                "content=" + TestEnvironment.MESSAGE_SIZE,
                "sessions=" + TestEnvironment.SEND_SESSION_COUNT,
                "send=true",
                "interval=" + TestEnvironment.PRODUCE_INTERVAL);

    }



    private static Process startReceive(String destName) throws IOException {
        String classPath = TestEnvironment.WORKSPACE+"/jms-test/target/jms-test-1.0-SNAPSHOT.jar:"+TestEnvironment.JEUS_HOME+"/lib/client/jclient.jar:"+TestEnvironment.JEUS_HOME+"/lib/system/jms.jar";
        String className = "examples.test.PerformanceTest";

        return runJava(classPath, className,
                "dest=" + destName,
                "duration=" + TestEnvironment.DURATION,
                "type=bytes",
                "content=" + TestEnvironment.MESSAGE_SIZE,
                "sessions=" + TestEnvironment.RECEIVE_SESSION_COUNT,
                "send=false",
                "async=" + TestEnvironment.ASYNC_RECEIVE);

    }

    private static void truncateStore() {
        String storePath=TestEnvironment.JEUS_HOME+"/domains/"+TestEnvironment.DOMAIN_NAME+"/servers/"+TestEnvironment.SERVER_NAME+"/.workspace/jeusmq";

        try {
            Process p = Runtime.getRuntime().exec("rm -rf " + storePath);
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void startTestSet() {
        String dest = "";
        for (int i = 0 ; i < TestEnvironment.DESTINATION_COUNT; i++)
            dest += TestEnvironment.DEST_LIST[i];

        try {
            Process lastProcess = null;
            log("STARTING RECEIVERS: " + TestEnvironment.RECEIVE_CLIENT_COUNT);
            for (int i = 0; i < TestEnvironment.RECEIVE_CLIENT_COUNT; i++) {
                lastProcess = startReceive(dest);
                log("i:" + lastProcess);
            }

            TestCompleteMonitor monitor = new TestCompleteMonitor(lastProcess);

            log("STARTING SENDERS: " + TestEnvironment.SEND_CLIENT_COUNT);
            for (int i = 0; i < TestEnvironment.SEND_CLIENT_COUNT; i++) {
                lastProcess = startSend(dest);
                log("i:" + lastProcess);
            }

            if (TestEnvironment.RECEIVE_CLIENT_COUNT <= 0)
                monitor = new TestCompleteMonitor(lastProcess);

            monitor.start();

        } catch (Throwable t) {
          t.printStackTrace(System.out);
        }
    }

    private static class TestCompleteMonitor extends Thread {
        Process testProcess;

        public TestCompleteMonitor(Process testProcess) {
            this.testProcess = testProcess;
        }

        public void run() {
            try {
                log("WAIT FOR LAST TEST PROCESS: " + testProcess);
                if (testProcess != null)
                    testProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                onComplete();
            }
        }

        public void onComplete() {
            try {
                log("=== TEST DONE (but another consumer can be remain) ===");
                Thread.sleep(TestEnvironment.WARMING_UP_TIME);
                stopJeus();
                synchronized (testSequence) {
                    int seq = testSequence.getAndSet(-1);
                    log("TEST-" + seq + " is done.");
                    testSequence.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class JMSEngineMonitor implements JMSEngineBootListener {

        public void onEngineStart() {
            try {
                log("== JMS STARTED ==");
                Thread.sleep(10*1000);
                startTestSet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onEngineStop() {
            log("== JMS STOPPED ==");
        }
    }
}
