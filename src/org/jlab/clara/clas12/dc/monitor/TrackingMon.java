package org.jlab.clara.clas12.dc.monitor;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class TrackingMon {

    public TrackingMon() {
        TimeResidual = new float[3]; //per region
    }

    public int getNbHBTracks() {
        return NbHBTracks;
    }

    public int getNbTBTracks() {
        return NbTBTracks;
    }

    public int getNbCTTracks() {
        return NbCTTracks;
    }

    public int getNbHBHits() {
        return NbHBHits;
    }

    public int getNbTBHits() {
        return NbTBHits;
    }

    public int getNbCTHits() {
        return NbCTHits;
    }

    public int getNbHBHitsOnTrack() {
        return NbHBHitsOnTrack;
    }

    public int getNbTBHitsOnTrack() {
        return NbTBHitsOnTrack;
    }

    public int getNbCTHitsOnTrack() {
        return NbCTHitsOnTrack;
    }

    public float[] getTimeResidual() {
        return TimeResidual;
    }

    private int NbHBTracks;    //number of hit-based tracks per event
    private int NbTBTracks;    //number of time-based tracks per event
    private int NbCTTracks;    //number of central tracks per event
    private int NbHBHits;      //number of hit-based hits per event
    private int NbTBHits;      //number of time-based hits per event
    private int NbCTHits;      //number of central hits per event
    private int NbHBHitsOnTrack; //average number of hit-based hits on track per event
    private int NbTBHitsOnTrack; //average number of time-based hits on track per event
    private int NbCTHitsOnTrack; //average number of central hits on track per event
    private float[] TimeResidual;//average time residual per region

    public void fetch_Trks(DataEvent event) {
        this.init();
        if (event.hasBank("CVTRec::Tracks")) {
            DataBank bank = event.getBank("CVTRec::Tracks");
            this.NbCTTracks = bank.rows();
        }

        if (event.hasBank("BSTRec::Hits")) {
            DataBank bank = event.getBank("BSTRec::Hits");
            this.NbCTHits += bank.rows();
            for(int i = 0; i < bank.rows(); i++) {
                if(bank.getShort("trkID", i)>-1 && this.NbCTTracks >0)
                    this.NbCTHitsOnTrack++;
            }
        }
        if (event.hasBank("BMTRec::Hits")) {
            DataBank bank = event.getBank("BMTRec::Hits");
            this.NbCTHits += bank.rows();
            for(int i = 0; i < bank.rows(); i++) {
                if(bank.getShort("trkID", i)>-1 && this.NbCTTracks >0)
                    this.NbCTHitsOnTrack++;
            }
        }
        if(this.NbCTHitsOnTrack >0 && this.NbCTTracks >0)
            this.NbCTHitsOnTrack /=this.NbCTTracks;

        if (event.hasBank("TimeBasedTrkg::TBHits")) {
            DataBank bank = event.getBank("TimeBasedTrkg::TBHits");
            this.NbTBHits = bank.rows();
            for(int i = 0; i < bank.rows(); i++) {
                int region = ((int)bank.getByte("superlayer", i) + 1) / 2;
                this.TimeResidual[region-1]+=bank.getFloat("timeResidual", i);
            }
            for(int r = 0; r < 3; r++)
                this.TimeResidual[r]/=bank.rows();
        }

        if (event.hasBank("HitBasedTrkg::HBHits")) {
            DataBank bank = event.getBank("HitBasedTrkg::HBHits");
            this.NbHBHits = bank.rows();
        }

        if (event.hasBank("TimeBasedTrkg::TBTracks")) {
            DataBank bank = event.getBank("TimeBasedTrkg::TBTracks");
            this.NbTBTracks = bank.rows();
            for(int i = 0; i < bank.rows(); i++) {
                this.NbTBHitsOnTrack +=bank.getShort("ndf", i)+6;//ndf+6=nb hits on track
            }
            this.NbTBHitsOnTrack /=bank.rows();
        }

        if (event.hasBank("HitBasedTrkg::HBTracks")) {
            DataBank bank = event.getBank("HitBasedTrkg::HBTracks");
            this.NbHBTracks = bank.rows();
            for(int i = 0; i < bank.rows(); i++) {
                this.NbHBHitsOnTrack +=bank.getShort("ndf", i)+6;//ndf+6=nb hits on track
            }
            this.NbHBHitsOnTrack /=bank.rows();
        }

    }


    private void init(){
        NbHBTracks = 0;
        NbTBTracks = 0;
        NbCTTracks = 0;
        NbHBHits = 0;
        NbTBHits = 0;
        NbCTHits = 0;
        NbHBHitsOnTrack = 0;
        NbTBHitsOnTrack = 0;
        NbCTHitsOnTrack = 0;
        TimeResidual[0] = 0;
        TimeResidual[1] = 0;
        TimeResidual[2] = 0;
    }
}
