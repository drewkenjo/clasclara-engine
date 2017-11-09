package sys;

import org.jlab.coda.xmsg.core.xMsg;
import org.jlab.coda.xmsg.core.xMsgCallBack;
import org.jlab.coda.xmsg.core.xMsgTopic;
import org.jlab.coda.xmsg.core.xMsgUtil;
import org.jlab.coda.xmsg.core.xMsgMessage;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.coda.xmsg.net.xMsgProxyAddress;
import org.jlab.coda.xmsg.net.xMsgRegAddress;

/**
 * Clara data ring listener class.
 *
 * @author gurjyan
 */
public abstract class ClasRingListener extends xMsg {

    /**
     * Process the received data off the Clara data ring.
     *
     * @param data from the ring
     */
    public abstract void processData(Object data);


    /**
     * Constructor.
     *
     * @param name     of this listener
     * @param ringHost host of the CDR
     * @param domain   of the topic on the CDR
     * @param subject  of the topic on the CDR
     * @param type     of the topic on the CDR
     * @throws xMsgException in case connection fails
     */
    public ClasRingListener(String name, String ringHost,
                            String domain,
                            String subject,
                            String type) throws xMsgException {
        super(name,
//                new xMsgProxyAddress(ringHost, ClaraConstants.CDR_PORT),
                new xMsgProxyAddress(ringHost, 9000),
                new xMsgRegAddress(), 1);
        listen(domain, subject, type);
    }

    private void listen(String domain,
                        String subject,
                        String type) throws xMsgException {
        xMsgTopic topic = xMsgTopic.build(domain, subject, type);
        // subscribe
        subscribe(topic, new ClasRingListener.MyCallBack());
        System.out.printf("Subscribed to = %s%n", topic.toString());
        xMsgUtil.keepAlive();
    }

    /**
     * Private callback class.
     */
    private class MyCallBack implements xMsgCallBack {

        @Override
        public void callback(xMsgMessage msg) {
            processData(xMsgMessage.parseData(msg));
        }
    }
}
