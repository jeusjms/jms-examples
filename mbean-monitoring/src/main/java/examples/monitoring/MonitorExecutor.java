package examples.monitoring;

import jeus.jms.server.mbean.JMSDestinationResourceMBean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.naming.InitialContext;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author donghwan
 */
public class MonitorExecutor extends Thread {
    List<JEUSMBeanMonitor> monitors = new ArrayList<>();

    boolean running = true;
    long interval = 500;

    String fileNamePrefix = "result/throughput/JEUS-MONITORING";
    long testId = 0;
    String fileNamePostfix;
    PrintWriter writer;

    JMSEngineBootListener listener;

    private static final int LOGGING_INTERVAL = 20;

    public MonitorExecutor(String name, long id) {
        testId = id;
        fileNamePostfix = name;
    }

    public MonitorExecutor() {
        fileNamePostfix = "TEST";
    }

    public static void main(String[] args) throws InterruptedException {
        MonitorExecutor monitor = new MonitorExecutor();
        monitor.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String q = scanner.nextLine();
            if (q.equalsIgnoreCase("q"))
                break;
        }

        monitor.quit();
    }

    public void registerMonitor(JEUSMBeanMonitor monitor) {
        monitors.add(monitor);
    }


    public void quit() {
        System.out.println("%%%%%%%%%%%%%%%%%%% QUIT!!!!!!!! %%%%%%%%%%%%%%%%%%%%%%%%%%%");
        running = false;
    }

    public void setJMSEngineListener(JMSEngineBootListener listener) {
        this.listener = listener;
    }


    public void run() {
        try {
            long startTime = System.nanoTime();
            long prevTime = startTime;
            int i = 0;

            while (running) {
                try {
                    long currTime = System.nanoTime();

                    String currStats = ((currTime - startTime) / 1000000) + ",";
                    for (JEUSMBeanMonitor monitor : monitors) {
                        currStats += monitor.getStat(currTime - prevTime, currTime - startTime) + ",";
                    }

                    writer.println(currStats);
                    prevTime = currTime;
                    if (++i % LOGGING_INTERVAL == 0) {
                        System.out.println(currStats);
                    }

                    Thread.sleep(interval);
                } catch (Throwable e) {
                    System.out.println("FAILED:" + e);
                    if (listener != null)
                        listener.onEngineStop();
                    if (writer != null)
                        writer.close();

                    JEUSMBeanMonitor.close();

                    while (running) {
                        try {
                            System.out.println("trying to conntect...");
                            JEUSMBeanMonitor.connect_jndi();
                            for (JEUSMBeanMonitor monitor : monitors) {
                                monitor.checkConnection();
                            }
                            System.out.println("connected!");
                            if (listener != null)
                                listener.onEngineStart();
                            String fileName = fileNamePrefix + testId + "-" + fileNamePostfix + ".csv";
                            System.out.println("opening output file: " + fileName);
                            writer = new PrintWriter(fileName);
                            break;
                        } catch (Throwable t) {
                            System.out.println("FAILED TO CONNECT:" + t.getMessage());
                            Thread.sleep(interval * 2);
                        }
                    }
                }
            }

            if (writer != null)
                writer.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
