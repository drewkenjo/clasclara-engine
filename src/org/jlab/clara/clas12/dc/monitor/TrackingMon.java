package org.jlab.clara.clas12.dc.monitor;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 * Extracting monitor data for the DC.
 *
 * @author ziegler
 * @author gurjyan
 */
public class TrackingMon {

    /**
     * time residual.
     */
    public TrackingMon() {
        timeResidual = new float[3]; //per region
    }

    /**
     * Number of hit based tracks.
     * @return int
     */
    public int getNbHBTracks() {
        return nbHBTracks;
    }

    /**
     * Number of time based tracks.
     * @return int
     */
    public int getNbTBTracks() {
        return nbTBTracks;
    }

    /**
     * Number of central tracks.
     * @return int
     */
    public int getNbCTTracks() {
        return nbCTTracks;
    }

    /**
     * Number of hit based his.
     * @return int
     */
    public int getNbHBHits() {
        return nbHBHits;
    }

    /**
     * Number of time based hits.
     * @return int
     */
    public int getNbTBHits() {
        return nbTBHits;
    }

    /**
     * Number of central hits.
     * @return int
     */
    public int getNbCTHits() {
        return nbCTHits;
    }

    /**
     * Number of hit based hits on the track.
     * @return int
     */
    public int getNbHBHitsOnTrack() {
        return nbHBHitsOnTrack;
    }

    /**
     * Number of time based hits on the track.
     * @return int
     */
    public int getNbTBHitsOnTrack() {
        return nbTBHitsOnTrack;
    }

    /**
     * Number of central hists on the track.
     * @return int
     */
    public int getNbCTHitsOnTrack() {
        return nbCTHitsOnTrack;
    }

    /**
     * Time residuals.
     * @return array of floats.
     */
    public float[] getTimeResidual() {
        return timeResidual;
    }

    private int nbHBTracks;    //number of hit-based tracks per event
    private int nbTBTracks;    //number of time-based tracks per event
    private int nbCTTracks;    //number of central tracks per event
    private int nbHBHits;      //number of hit-based hits per event
    private int nbTBHits;      //number of time-based hits per event
    private int nbCTHits;      //number of central hits per event
    private int nbHBHitsOnTrack; //average number of hit-based hits on track per event
    private int nbTBHitsOnTrack; //average number of time-based hits on track per event
    private int nbCTHitsOnTrack; //average number of central hits on track per event
    private float[] timeResidual; //average time residual per region

    /**
     * Gets the data from banks.
     * @param event {@link DataEvent} object.
     */
    public void fetchTrks(DataEvent event) {
        this.init();
        if (event.hasBank("CVTRec::Tracks")) {
            DataBank bank = event.getBank("CVTRec::Tracks");
            this.nbCTTracks = bank.rows();
        }

        if (event.hasBank("BSTRec::Hits")) {
            DataBank bank = event.getBank("BSTRec::Hits");
            this.nbCTHits += bank.rows();
            for (int i = 0; i < bank.rows(); i++) {
                if (bank.getShort("trkID", i) > -1 && this.nbCTTracks > 0) {
                    this.nbCTHitsOnTrack++;
                }
            }
        }
        if (event.hasBank("BMTRec::Hits")) {
            DataBank bank = event.getBank("BMTRec::Hits");
            this.nbCTHits += bank.rows();
            for (int i = 0; i < bank.rows(); i++) {
                if (bank.getShort("trkID", i) > -1 && this.nbCTTracks > 0) {
                    this.nbCTHitsOnTrack++;
                }
            }
        }
        if (this.nbCTHitsOnTrack > 0 && this.nbCTTracks > 0) {
            this.nbCTHitsOnTrack /= this.nbCTTracks;
        }

        if (event.hasBank("TimeBasedTrkg::TBHits")) {
            DataBank bank = event.getBank("TimeBasedTrkg::TBHits");
            this.nbTBHits = bank.rows();
            for (int i = 0; i < bank.rows(); i++) {
                int region = ((int) bank.getByte("superlayer", i) + 1) / 2;
                this.timeResidual[region - 1] += bank.getFloat("timeResidual", i);
            }
            for (int r = 0; r < 3; r++) {
                this.timeResidual[r] /= bank.rows();
            }
        }

        if (event.hasBank("HitBasedTrkg::HBHits")) {
            DataBank bank = event.getBank("HitBasedTrkg::HBHits");
            this.nbHBHits = bank.rows();
        }

        if (event.hasBank("TimeBasedTrkg::TBTracks")) {
            DataBank bank = event.getBank("TimeBasedTrkg::TBTracks");
            this.nbTBTracks = bank.rows();
            for (int i = 0; i < bank.rows(); i++) {
                this.nbTBHitsOnTrack += bank.getShort("ndf", i) + 6; //ndf+6=nb hits on track
            }
            this.nbTBHitsOnTrack /= bank.rows();
        }

        if (event.hasBank("HitBasedTrkg::HBTracks")) {
            DataBank bank = event.getBank("HitBasedTrkg::HBTracks");
            this.nbHBTracks = bank.rows();
            for (int i = 0; i < bank.rows(); i++) {
                this.nbHBHitsOnTrack += bank.getShort("ndf", i) + 6; //ndf+6=nb hits on track
            }
            this.nbHBHitsOnTrack /= bank.rows();
        }

    }


    private void init() {
        nbHBTracks = 0;
        nbTBTracks = 0;
        nbCTTracks = 0;
        nbHBHits = 0;
        nbTBHits = 0;
        nbCTHits = 0;
        nbHBHitsOnTrack = 0;
        nbTBHitsOnTrack = 0;
        nbCTHitsOnTrack = 0;
        timeResidual[0] = 0;
        timeResidual[1] = 0;
        timeResidual[2] = 0;
    }
}
