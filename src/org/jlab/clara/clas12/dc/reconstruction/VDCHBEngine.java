package org.jlab.clara.clas12.dc.reconstruction;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossListFinder;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.RoadFinder;
import sys.ClasEngineException;
import sys.ClasServiceEngine;

import java.util.List;

public class VDCHBEngine extends ClasServiceEngine {

    private volatile int run;
    private volatile double solenoid;
    private volatile double torus;
    private volatile boolean isCalib;

    public VDCHBEngine() {
        super("DCHB", "ziegler", "3.0", "dc-hit-based-reconstruction");
    }


    @Override
    public boolean userInit(String json) {
        try {
            run = getIntConfigParameter(json, "ccdb", "run");
            solenoid = getDoubleConfigParameter(json, "magnet", "solenoid");
            torus = getDoubleConfigParameter(json, "magnet", "torus");
            if ((getStringConfigParameter(json, "runmode")).equals("calibration")) isCalib = true;

            System.out.println("DDD =======================");
            System.out.println("run      = " + run);
            System.out.println("solenoid = " + solenoid);
            System.out.println("torus    = " + torus);
            System.out.println("isCalib  = " + isCalib);
            System.out.println("DDD =======================");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Load the Fields
            DCSwimmer.getMagneticFields();

            setRunConditionsParameters();

        } catch (ClasEngineException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Object processDataEvent(DataEvent event) {
        // init SNR
        Clas12NoiseResult results = new Clas12NoiseResult();
        Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();

        int[] rightShifts = Constants.SNR_RIGHTSHIFTS;
        int[] leftShifts = Constants.SNR_LEFTSHIFTS;
        NoiseReductionParameters parameters = new NoiseReductionParameters(
                2, leftShifts,
                rightShifts);

        ClusterFitter cf = new ClusterFitter();
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();

        List<FittedHit> fhits;
        List<FittedCluster> clusters;
        List<Segment> segments;
        List<Cross> crosses;

        List<Track> trkcands;

        //instantiate bank writer
        RecoBankWriter rbc = new RecoBankWriter();


        HitReader hitRead = new HitReader();
        hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

        List<Hit> hits;
        //I) get the hits
        hits = hitRead.get_DCHits();

        //II) process the hits
        //1) exit if hit list is empty
        if (hits.size() == 0) {
            return true;
        }

        fhits = rbc.createRawHitList(hits);


        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);


        if (clusters.size() == 0) {
            rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null);
            return true;
        }

        rbc.updateListsListWithClusterInfo(fhits, clusters);

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();
        segments = segFinder.get_Segments(clusters, event);

        if (segments.size() == 0) { // need 6 segments to make a trajectory
            rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null);
            return true;
        }
        //RoadFinder
        //

        RoadFinder pcrossLister = new RoadFinder();
        List<Segment> pSegments = pcrossLister.findRoads(segments);
        segments.addAll(pSegments);

        //
        //System.out.println("nb trk segs "+pSegments.size());
        CrossMaker crossMake = new CrossMaker();
        crosses = crossMake.find_Crosses(segments);

        if (crosses.size() == 0) {
            rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null);
            return true;
        }

        CrossListFinder crossLister = new CrossListFinder();

        List<List<Cross>> CrossesInSector = crossLister.get_CrossesInSectors(crosses);
        for (int s = 0; s < 6; s++) {
            if (CrossesInSector.get(s).size() > Constants.MAXNBCROSSES) {
                return true;
            }
        }

        CrossList crosslist = crossLister.candCrossLists(crosses, false);

        if (crosslist.size() == 0) {

            rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
            return true;
        }

        //6) find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
        trkcands = trkcandFinder.getTrackCands(crosslist);

        if (trkcands.size() == 0) {

            // no cand found, stop here and save the hits, the clusters, the segments, the crosses
            rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
            return true;
        }
        // track found
        int trkId = 1;
        for (Track trk : trkcands) {
            for (Cross c : trk) {
                for (FittedHit h1 : c.get_Segment1())
                    h1.set_AssociatedHBTrackID(trk.get_Id());
                for (FittedHit h2 : c.get_Segment2())
                    h2.set_AssociatedHBTrackID(trk.get_Id());
            }

        }

        // remove overlaps
        trkcandFinder.removeOverlappingTracks(trkcands);

        for (Track trk : trkcands) {
            // reset the id
            trk.set_Id(trkId);
            for (Cross c : trk) {
                for (FittedHit h1 : c.get_Segment1())
                    h1.set_AssociatedHBTrackID(trk.get_Id());
                for (FittedHit h2 : c.get_Segment2())
                    h2.set_AssociatedHBTrackID(trk.get_Id());
            }
            trkId++;
        }

        rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);

        return event;
    }


    private void setRunConditionsParameters() {

        // Load the constants
        //-------------------
        boolean T2DCalc;

        if (run > 99) {
            Constants.setT0(true);
            Constants.setUseMiniStagger(true);
        }
        T2DCalc = true;
        Constants.setUseMiniStagger(true);

        System.out.println("   SETTING RUN-DEPENDENT CONSTANTS, T0 = " +
                Constants.getT0() +
                " use ministagger " +
                Constants.getUseMiniStagger());
        CalibrationConstantsLoader.Load(run, "default");

        TableLoader.Fill();

        GeometryLoader.Load(run, "default");

        // Load the fields
        //-----------------
        String newConfig = "SOLENOID" + solenoid + "TORUS" + torus + "RUN" + run;
        // Load the Constants
        double TorScale = torus;
        //TorScale = -0.5;

        Constants.Load(T2DCalc, isCalib, TorScale); // set the T2D Grid for Cosmic data only so far....
        // Load the Fields
        DCSwimmer.setMagneticFieldsScales(solenoid, TorScale); // something changed in the configuration ...
    }


}
