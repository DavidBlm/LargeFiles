/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

public class DebugLog extends Log {

    public static void init() {
        Log debugLog = new DebugLog();
        Log.setLog(debugLog);
    }

    @Override
    protected boolean doIsDebugEnabled(String logName) {
        return true;
    }


}
