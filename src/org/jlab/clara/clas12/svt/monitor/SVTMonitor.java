/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clara.clas12.svt.monitor;

import java.util.Date;
import java.util.HashMap;
import org.jlab.clara.base.ClaraUtil;
import org.jlab.io.base.DataEvent;
import sys.ClasServiceEngine;
import org.jlab.io.base.DataBank;

/**
 *
 * @author kenjo
 */
public class SVTMonitor extends ClasServiceEngine {

    public final int nEventsToIntegrate = 3000;
    public final String[] bankNames = {"CVTRec::Cosmics", "BSTRec::Hits", "BSTRec::Clusters", "BSTRec::Crosses"};
    public final HashMap<String, Integer> nRows = new HashMap<>();
    public final HashMap<String, Integer> nCurrent = new HashMap<>();

    /**
     * Constructor.
     */
    public SVTMonitor() {
        super("SVT Multiplicities", "kenjo-gurjyan", "1.0", "dev");
    }
;
    @Override
    public Object processDataEvent(DataEvent event) {
        for (String bname : bankNames) {
            if (event.hasBank(bname)) {
                DataBank bnk = event.getBank(bname);
                int nrws = bnk.rows();

                nRows.put(bname, nRows.getOrDefault(bname, 0) + nrws);
                nCurrent.put(bname, nCurrent.getOrDefault(bname, 0) + 1);

                if (bname.contains("BSTRec")) {
                    for (int irow = 0; irow < nrws; irow++) {
                        if (bnk.getInt("trkID", irow) != -1) {
                            nRows.put(bname + "::id", nRows.getOrDefault(bname + "::id", 0) + 1);
                        }
                    }
                    nCurrent.put(bname + "::id", nCurrent.getOrDefault(bname + "::id", 0) + 1);
                }
            }
        }

        int iObservables = 0;
        for (String keyname : nCurrent.keySet()) {
            if (nCurrent.get(keyname).intValue() > nEventsToIntegrate) {
                float multiplicity = (float) nRows.get(keyname);
                multiplicity /= nEventsToIntegrate;
                addTsObservable(keyname, multiplicity);
                System.out.println(new Date() + ": reporting SVT Monitor data... " + keyname);

                nRows.put(keyname, 0);
                nCurrent.put(keyname, 0);

                iObservables++;
            }
        }

        if (iObservables > 0) {
            addTsObservable("snapshot_time", ClaraUtil.getCurrentTime());

            publishTsObservables();
        }

        return event;
    }

    @Override
    public boolean userInit(String json) {
        return true;
    }
}
