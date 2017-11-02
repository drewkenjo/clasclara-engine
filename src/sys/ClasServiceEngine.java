package sys;

import org.influxdb.dto.Point;
import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.base.core.ClaraConstants;
import org.jlab.clara.engine.Engine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.engine.EngineStatus;
import org.jlab.clas.reco.Clas12Types;
import org.jlab.coda.jinflux.JinFlux;
import org.jlab.coda.jinflux.JinFluxException;
import org.jlab.coda.jinflux.JinTime;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.hipo.HipoDataEvent;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CLAS12 plugin specific service engine class.
 *
 * @author gurjyan
 * @author gavalian
 */
public abstract class ClasServiceEngine implements Engine {

    private volatile ConcurrentHashMap<String, ConstantsManager>
            constManagerMap = new ConcurrentHashMap<>();
    private volatile SchemaFactory engineDictionary;

    private AtomicReference<EngineStatus> status;
    private String name;
    private String author;
    private String version;
    private String description;
    private String ringTopic = ClasServiceConstants.DEFAULT_TOPIC;
    private EngineDataType ringOutDataType = EngineDataType.JSON;
    private AtomicBoolean isRingReady = new AtomicBoolean(false);

    private JinFlux jinFlux;
    private boolean jinFxConnected;
    private ThreadLocal<HashMap<String, String>> tags = new ThreadLocal<>();
    private String hostName;

    private ThreadLocal<HashMap<String, Object>> tsObservables = new ThreadLocal<>();

    /**
     * User engine method to process Clara transient data.
     *
     * @param event transient data.
     * @return result of the engine processing.
     */
    public abstract Object processDataEvent(DataEvent event);

    /**
     * User engine configuration method.
     *
     * @param json configuration JSon object.
     * @return configuration status.
     */
    public abstract boolean userInit(String json);


    /**
     * Constructor.
     *
     * @param name        the name of the engine.
     * @param author      the author of the engine.
     * @param version     the version of the engine.
     * @param description the description of the engine.
     */
    public ClasServiceEngine(String name, String author, String version, String description) {
        status = new AtomicReference<>(EngineStatus.INFO);
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        constManagerMap = new ConcurrentHashMap<>();
        engineDictionary = new SchemaFactory();
        engineDictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        // connect to the time-series database
        connectTsDatabase(ClasServiceConstants.DEFAULT_DB_HOST);
    }

    /**
     * Method helps to extract configuration parameters defined in the Clara YAML file.
     *
     * @param jsonString JSon configuration object (passed to the userInit method).
     * @param key        the key of the config parameter.
     * @return value of the config parameter.
     */
    public Object getConfigParameter(String jsonString, String key) {
        JSONObject base = new JSONObject(jsonString);
        return base.get(key);
    }

    /**
     * Call this method in case you got an error condition during the execution of
     * the engine.
     */
    public void setErrorFlag(){
        status.set(EngineStatus.ERROR);
    }

    /**
     * Sets the topic of the data-publication on the Clara ring.
     *
     * @param topic publication topic.
     */
    public synchronized void setRingTopic(String topic) {
        ringTopic = topic;
    }

    /**
     * Sets the type of the data going to be published on the Clara ring.
     *
     * @param dataType Clara engine data type:  {@link EngineDataType}
     */
    public synchronized void setRingOutDataType(EngineDataType dataType) {
        ringOutDataType = dataType;
    }

    /**
     * Sets the flag indicating readiness to publish data on the ring.
     */
    public void ringPublish() {
        isRingReady.set(true);
    }

    private HashMap<String, Object> getThreadLocalObservablesMap() {
        HashMap<String, Object> map = tsObservables.get();
        if (map == null) {
            map = new HashMap<>();
            tsObservables.set(map);
        }
        return tsObservables.get();
    }

    private HashMap<String, String> getThreadLocalTagsMap() {
        HashMap<String, String> map = tags.get();
        if (map == null) {
            map = new HashMap<>();
            tags.set(map);
        }
        return tags.get();
    }

    private void connectTsDatabase(String dbNode) {
        try {
            jinFlux = new JinFlux(dbNode);
            String dbName = ClasServiceConstants.DEFAULT_DB_NAME;
            if (!jinFlux.existsDB(dbName)) {
                jinFlux.createDB(dbName, 1, JinTime.HOURE);
            }
            jinFxConnected = true;
        } catch (JinFluxException e) {
            jinFxConnected = false;
            e.printStackTrace();
        }
    }

    private void resetTsObservables() {
        getThreadLocalObservablesMap().clear();
    }

    /**
     * method helps to construct time series observable list that is going
     * to be published on the ring.
     *
     * @param name  the name of the observable.
     * @param value the value of the observable.
     */
    protected void addTsObservable(String name, Object value) {
        getThreadLocalObservablesMap().put(name, value);
    }

    /**
     * Method publishes list of observables into the Clara InfluxDB database.
     */
    protected void publishTsObservables() {

        if (jinFxConnected) {
            HashMap<String, String> map = getThreadLocalTagsMap();
            map.clear();
            map.put(ClaraConstants.SESSION, hostName);
            map.put("mon-name", name);
            map.put("mon-author", getAuthor());
            Point.Builder p;
            HashMap<String, Object> dataMap = getThreadLocalObservablesMap();
            p = jinFlux.openTB("timeline", map);

            for (String s : dataMap.keySet()) {
                jinFlux.addDP(p, s, dataMap.get(s));
            }
            try {
                jinFlux.write(ClasServiceConstants.DEFAULT_DB_NAME, p);
            } catch (JinFluxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public EngineData execute(EngineData engineData) {

        String dataType = engineData.getMimeType();
        // Reset ring-publish flag
        isRingReady.set(false);
        resetTsObservables();
        try {
            if (dataType.equals(ClasDataTypes.HIPO)) {
                HipoEvent hipoEvent = (HipoEvent) engineData.getData();
                HipoDataEvent hipoDataEvent = new HipoDataEvent(hipoEvent);
                hipoDataEvent.initDictionary(engineDictionary);

                Object result = processDataEvent(hipoDataEvent);
                // Check to see if service engine needs to publish data to the ring
                if (isRingReady.get()) {
                    if (result instanceof HipoDataEvent) {
                        engineData.setData(dataType, ((HipoDataEvent) result).getHipoEvent());
                    } else {
                        engineData.setData(ringOutDataType, result);
                        engineData.setExecutionState(ringTopic);
                    }
                } else {
                    // No ring publishing. Send data across the chain
                    engineData.setData(dataType, ((HipoDataEvent) result).getHipoEvent());
                }

            } else if (dataType.equals(ClasDataTypes.EVIO)) {
                ByteBuffer bb = (ByteBuffer) engineData.getData();
                byte[] buffer = bb.array();
                ByteOrder endianness = bb.order();
                EvioDataEvent evioDataEvent =
                        new EvioDataEvent(buffer, endianness, EvioFactory.getDictionary());
                Object result = processDataEvent(evioDataEvent);
                // Check to see if service engine needs to publish data to the ring
                if (isRingReady.get()) {
                    if (result instanceof EvioDataEvent) {
                        engineData.setData(dataType, ((EvioDataEvent) result).getEventBuffer());
                    } else {
                        engineData.setData(ringOutDataType, result);
                        engineData.setExecutionState(ringTopic);
                    }
                } else {
                    // No ring publishing. Send data across the chain
                    engineData.setData(dataType, ((EvioDataEvent) result).getEventBuffer());
                }
            }
            engineData.setStatus(status.get());

        } catch (Exception e) {
            String msg = String.format("Error reading input event%n%n%s",
                    ClaraUtil.reportException(e));
            engineData.setStatus(EngineStatus.ERROR);
            engineData.setDescription(msg);
        }
        return engineData;
    }


    @Override
    public EngineData configure(EngineData engineData) {

        if (engineData.getMimeType().equals(EngineDataType.JSON.toString())) {
            if (!userInit((String) engineData.getData())) {
                engineData.setStatus(EngineStatus.ERROR);
            }
        } else {
            userInit("");
        }
        return engineData;
    }

    @Override
    public EngineData executeGroup(Set<EngineData> set) {
        return null;
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
    }

    @Override
    public Set<String> getStates() {
        return null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void reset() {

    }

    @Override
    public void destroy() {

    }

    /**
     * Gagik's method.
     *
     * @param tables list of table names.
     */
    public void requireConstants(List<String> tables) {
        if (!constManagerMap.containsKey(this.getClass().getName())) {
            System.out.println("[ConstantsManager] ---> create a new one for module : "
                    + this.getClass().getName());
            ConstantsManager manager = new ConstantsManager();
            manager.init(tables);
            constManagerMap.putIfAbsent(this.getClass().getName(), manager);
        }
    }

}
