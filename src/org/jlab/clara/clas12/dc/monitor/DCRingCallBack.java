package org.jlab.clara.clas12.dc.monitor;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.EngineDataType;
import sys.CDRListener;


/**
 * CDR listener callback. Listens DC monitor data.
 *
 * @author gurjyan
 */
public class DCRingCallBack extends CDRListener {

    @Override
    public void processRingEvent(Object data) {
        System.out.println(ClaraUtil.getCurrentTime() + " : data off the ring...");
        Float[] d = (Float[]) data;
        for (float f: d) {
            System.out.println(f);
        }
        System.out.println("=====================");
    }

    @Override
    public EngineDataType getExpectedDataType() {
        return EngineDataType.ARRAY_FLOAT;
    }

}
