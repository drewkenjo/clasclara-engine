package org.jlab.clara.clas12.dc.monitor;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.coda.xmsg.excp.xMsgException;
import sys.ClasRingListener;


/**
 * CDR listener example.
 * @author gurjyan
 */
public class DcTLRingClient extends ClasRingListener {

    /**
     * Constructor.
     *
     * @param name of this client
     * @param ringHost host of the CDR
     * @param domain of the ring topic
     * @param subject of the ring topic
     * @param type of the ring topic
     * @throws xMsgException in case connection fails
     */
    private DcTLRingClient(String name,
                           String ringHost,
                           String domain,
                           String subject,
                           String type) throws xMsgException {
        super(name, ringHost, domain, subject, type);
    }

    @Override
    public void processData(Object data) {
        float[] receivedData = (float[])data;
        System.out.println("Ring data: " + ClaraUtil.getCurrentTime());
        for(float f: receivedData){
            System.out.println(f);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            new DcTLRingClient(args[0],
                    args[1],
                    args[2],
                    "*",
                    "*");
        } catch (xMsgException e) {
            e.printStackTrace();
        }
    }
}
