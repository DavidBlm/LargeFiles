package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.cnnarch.generator.decomposition.BackendDecomposer;
import de.monticore.lang.monticar.cnnarch.generator.decomposition.NetworkStructure;
import de.monticore.lang.monticar.cnnarch.gluongenerator.decomposition.GluonDecomposer;
import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.lang.monticar.emadl.generator.FileHandler;
import de.se_rwth.commons.logging.Log;

import java.util.HashMap;

public class NetworkDecomposer {

    private Backend backend = null;
    private FileHandler fileHandler = null;

    public NetworkDecomposer(Backend backend, FileHandler fileHandler){
        this.backend = backend;
        this.fileHandler = fileHandler;
    }

    public void decomposeNetwork(String modelPath, NetworkStructure composedNetworkStructure, String[] decomposeNetworkList){
        BackendDecomposer backendDecomposer;



        String backendString = Backend.getBackendString(this.backend);
        switch (backendString){
            case "GLUON":
                backendDecomposer = new GluonDecomposer(fileHandler.getPythonPath());
                break;
            default:
                backendDecomposer = null;
        }

        if (backendDecomposer == null) {
            Log.warn("Decomposition for current backend is not supported (yet)");
        } else {
            backendDecomposer.decomposeNetwork(modelPath, composedNetworkStructure);
        }


    }

    public void decomposeNetworks(String modelPath, HashMap<String, NetworkStructure> composedNetworkStructures, String[] decomposeNetworkList){
        for (String key : composedNetworkStructures.keySet()){
            NetworkStructure composedNetworkStructure = composedNetworkStructures.get(key);

            if (decomposeNetworkList.length > 0) composedNetworkStructure.setDecompositionControl(decomposeNetworkList);

            for (String innerKey : composedNetworkStructures.keySet()){
                if (key.equals(innerKey)) continue;
                NetworkStructure innerComposedNetworkStructure = composedNetworkStructures.get(innerKey);

                if (composedNetworkStructure.hasSubnet(innerComposedNetworkStructure) && innerComposedNetworkStructure.isDecompositionAllowed() ) composedNetworkStructure.reassignSubnet(innerComposedNetworkStructure);
            }
        }

        for (String key : composedNetworkStructures.keySet()){
            NetworkStructure composedNetworkStructure = composedNetworkStructures.get(key);
            decomposeNetwork(modelPath, composedNetworkStructure, decomposeNetworkList);
        }
    }



}
