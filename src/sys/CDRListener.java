package sys;


import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.orchestrators.EngineReportHandler;

import java.util.Set;


/**
 * Abstract class for all CDR listeners.
 *
 * @author gurjyan
 */
public abstract class CDRListener implements EngineReportHandler {

    /**
     * Process data off the ring.
     *
     * @param data off the CDR.
     */
    public abstract void processRingEvent(Object data);

    /**
     * Returns ring data type.
     *
     * @return {@link EngineDataType} object
     */
    public abstract EngineDataType getExpectedDataType();

    @Override
    public void handleEvent(EngineData event) {
        System.out.printf("%s: received %s [%s] from %s%n",
                ClaraUtil.getCurrentTime(),
                event.getExecutionState(),
                event.getMimeType(),
                event.getEngineName());
        processRingEvent(event.getData());
    }

    @Override
    public Set<EngineDataType> dataTypes() {
        return ClaraUtil.buildDataTypes(getExpectedDataType());
    }
}

