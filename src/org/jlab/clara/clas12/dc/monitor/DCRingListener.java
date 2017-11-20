package org.jlab.clara.clas12.dc.monitor;

import org.jlab.clara.base.DataRingAddress;
import org.jlab.clara.base.DataRingTopic;
import org.jlab.clara.std.orchestrators.MonitorOrchestrator;


/**
 * Example of the ring listener.
 *
 * @author gurjyan
 */
public final class DCRingListener {

    private DCRingListener() {
        // not called
    }

    public static void main(String[] args) throws Exception {
        // Define the ring
        DataRingAddress ringAddress = new DataRingAddress(args[0], Integer.parseInt(args[1]));

        // Define the topic to listen
        DataRingTopic topic = new DataRingTopic("myTestTopicOnTheRing");

        // Create a monitor Clara orchestrator
        MonitorOrchestrator monitor = new MonitorOrchestrator(ringAddress);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> monitor.close()));

        // Start listening reports on the ring
        monitor.listenEngineReports(topic, new DCRingCallBack());

    }
}
