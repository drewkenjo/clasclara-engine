/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clara.clas12.analysis.electron;

import org.jlab.io.base.DataEvent;
import sys.ClasServiceEngine;
import org.jlab.io.base.DataBank;

/**
 *
 * @author kenjo
 */
public class ElectronPID extends ClasServiceEngine {


    /**
     * Constructor.
     */
    public ElectronPID() {
        super("Electron PID", "kenjo", "1.0", "dev");
    }

    @Override
    public Object processDataEvent(DataEvent event) {
        if(event.hasBank("REC::Particle")){
            DataBank ebank = event.getBank("REC::Particle");
            for(int irow=0;irow<ebank.rows();irow++){
                if(ebank.getByte("charge",irow)<0){
                    //1. modify bank
                    //2. add another bank PHYS::Particle
                }
            }
        }
        return event;
    }

    @Override
    public boolean userInit(String json) {
        return true;
    }
}
