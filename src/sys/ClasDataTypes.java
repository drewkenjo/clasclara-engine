package sys;

import org.jlab.clara.base.error.ClaraException;
import org.jlab.clara.engine.ClaraSerializer;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.hipo.data.HipoEvent;

import java.nio.ByteBuffer;

/**
 * Clas12 specific data types.
 * @author gurjyan
 */
public final class ClasDataTypes extends EngineDataType {

    /**
     * EVIO data type definition.
     */
    public static final EngineDataType EVIO = buildEvio();
    /**
     * HIPO data type definition.
     */
    public static final EngineDataType HIPO = buildHipo();

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

    private static EngineDataType buildHipo() {
        return new EngineDataType("binary/data-hipo", new HipoSerializer());
    }

    private static EngineDataType buildEvio() {
        return new EngineDataType("binary/data-evio", EngineDataType.BYTES.serializer());
    }

    private static class HipoSerializer implements ClaraSerializer {

        @Override
        public ByteBuffer write(Object data) throws ClaraException {
            HipoEvent event = (HipoEvent) data;
            return ByteBuffer.wrap(event.getDataBuffer());
        }

        @Override
        public Object read(ByteBuffer buffer) throws ClaraException {
            return new HipoEvent(buffer.array());
        }
    }

}
