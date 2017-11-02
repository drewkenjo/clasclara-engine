package org.jlab.clara.clas12.dc.monitor;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.io.base.DataEvent;
import sys.ClasServiceEngine;
import sys.util.TimerFlag;

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
        super("DcTimeLine", "gurjyan", "1.0", "test");
    }

    @Override
    public Object processDataEvent(DataEvent event) {

        if (timerFlag.isUp()) {
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

            publishTsObservables();
            System.out.println(new Date() + ": reporting for ........... ");
            timerFlag.reset();
        }
        return event;
    }

    @Override
    public boolean userInit(String json) {
        trackingMon = new TrackingMon();
        timerFlag = new TimerFlag(5);
        return true;
    }
}
