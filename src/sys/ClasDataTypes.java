package sys;

import org.jlab.clara.engine.ClaraSerializer;
import org.jlab.clara.engine.EngineDataType;

/**
 * Clas12 specific data types.
 * @author gurjyan
 */
public class ClasDataTypes extends EngineDataType {

    /**
     * EVIO data format definition.
     */
    static final String EVIO = "binary/data-evio";
    /**
     * HIPO data format definition.
     */
    static final String HIPO = "binary/data-hipo";

    /**
     * Creates a new user data type.
     * The data type is identified by its mime-type string.
     * The serializer will be used in order to send data through the network,
     * or to a different language DPE.
     *
     * @param mimeType   the name of this data-type
     * @param serializer the custom serializer for this data-type
     */
    public ClasDataTypes(String mimeType, ClaraSerializer serializer) {
        super(mimeType, serializer);
    }
}
