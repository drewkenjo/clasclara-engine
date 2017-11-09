package org.jlab.clara.clas12.dc.monitor;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import sys.ClasDataTypes;
import sys.ClasEngineException;
import sys.ClasServiceEngine;
import sys.util.TimerFlag;

import java.util.ArrayList;
import java.util.Date;


/**
 * An example engine.
 *
 * @author gurjyan
 */
public class DcTimeLineEngine extends ClasServiceEngine {

    private TimerFlag timerFlag;
    private TrackingMon trackingMon;

    /**
     * Constructor.
     */
    public DcTimeLineEngine() {
        super("DcTimeLine", "ziegler-gurjyan", "1.0", "test");
    }

    @Override
    public Object processDataEvent(DataEvent event) {

        // Here goes code that processes single event
        // myProcess(event);

        if (timerFlag.isUp()) {
            trackingMon.fetch_Trks(event);
            addTsObservable("snapshot_time", ClaraUtil.getCurrentTime());
            addTsObservable("NbHBTracks", trackingMon.getNbHBTracks());
            addTsObservable("NbTBTracks", trackingMon.getNbTBTracks());
            addTsObservable("NbCTTracks", trackingMon.getNbCTTracks());
            addTsObservable("NbHBHits", trackingMon.getNbHBHits());
            addTsObservable("NbTBHits", trackingMon.getNbTBHits());
            addTsObservable("NbCTHits", trackingMon.getNbCTHits());
            addTsObservable("NbHBHitsOnTrack", trackingMon.getNbHBHitsOnTrack());
            addTsObservable("NbTBHitsOnTrack", trackingMon.getNbTBHitsOnTrack());
            addTsObservable("NbCTHitsOnTrack", trackingMon.getNbCTHitsOnTrack());
            addTsObservable("TimeResidual-1", trackingMon.getTimeResidual()[0]);
            addTsObservable("TimeResidual-2", trackingMon.getTimeResidual()[1]);
            addTsObservable("TimeResidual-3", trackingMon.getTimeResidual()[2]);

            // Report monitor data to InfluxDB
            System.out.println(new Date() + ": reporting DC timeline data to InfluxDB... ");
            publishTsObservables();

            // Create and report monitor data to the Clara Data Ring

            // e.g. float array on the ring
            ArrayList<Float> tmp = new ArrayList<>();
            tmp.add((float)trackingMon.getNbHBTracks());
            tmp.add((float)trackingMon.getNbTBTracks());
            tmp.add((float)trackingMon.getNbCTTracks());
            tmp.add((float)trackingMon.getNbHBHits());
            tmp.add((float)trackingMon.getNbTBHits());
            tmp.add((float)trackingMon.getNbCTHits());
            tmp.add((float)trackingMon.getNbHBHitsOnTrack());
            tmp.add((float)trackingMon.getNbTBHitsOnTrack());
            tmp.add((float)trackingMon.getNbCTHitsOnTrack());
            tmp.add(trackingMon.getTimeResidual()[0]);
            tmp.add(trackingMon.getTimeResidual()[1]);
            tmp.add(trackingMon.getTimeResidual()[2]);

            System.out.println(new Date() + ": reporting DC timeline data to CDRing... ");
            setRingOutDataType(ClasDataTypes.ARRAY_FLOAT);
            setRingTopic("myTestTopicOnTheRing");


            // e.g. hipoEvent on the ring
//            HipoDataEvent myHipoBanks = new HipoDataEvent()
//            setRingTopic("myTopicOnTheRing");
//            return myHipoBanks;

            timerFlag.reset();
            return tmp.toArray(new Float[tmp.size()]);
        }

        return event;
    }

    @Override
    public boolean userInit(String json) {
//        System.out.println(prettyPrintJson(json));
//        System.out.println("DDD ===============================");

//        try {
//            System.out.println(getIntConfigParameter(json, "ccdb", "run"));
//            System.out.println(getIntConfigParameter(json, "vvar"));
//        } catch (ClasEngineException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        trackingMon = new TrackingMon();
        timerFlag = new TimerFlag(5);
        return true;
    }
}
