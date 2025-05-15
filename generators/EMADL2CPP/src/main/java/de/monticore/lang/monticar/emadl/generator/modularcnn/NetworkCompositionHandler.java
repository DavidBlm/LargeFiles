package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.decomposition.NetworkStructure;
import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.lang.monticar.emadl.modularcnn.compositions.NetworkStructureInformation;
import de.monticore.lang.monticar.emadl.modularcnn.tools.ComposedNetworkFileHandler;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class NetworkCompositionHandler {
    private String composedNetworkFilePath;
    private ArrayList<NetworkStructureInformation> composedNetworks;
    private NetworkComposer networkComposer;
    private Set<EMAComponentInstanceSymbol> instanceVault = null;
    private LinkedHashMap<String, ArchitectureSymbol> cachedComposedArchitectureSymbols = null;
    private LinkedHashMap<String, NetworkStructure> composedNetworkStructures = null;
    private String modelPath = null;


    public NetworkCompositionHandler(String composedNetworkFilePath, String modelPath, LinkedHashMap<String,ArchitectureSymbol> cachedComposedArchitectureSymbols, Backend backend, LinkedHashMap<String, NetworkStructure> composedNetworkStructures) {
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.modelPath = modelPath;
        this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
        this.networkComposer = new NetworkComposer(this, cachedComposedArchitectureSymbols, composedNetworkStructures);
        this.cachedComposedArchitectureSymbols = cachedComposedArchitectureSymbols;


    }

    public NetworkCompositionHandler(String composedNetworkFilePath, String modelPath, Set<EMAComponentInstanceSymbol> instanceVault, LinkedHashMap<String,ArchitectureSymbol> cachedComposedArchitectureSymbols, Backend backend, LinkedHashMap<String, NetworkStructure> composedNetworkStructures) {
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.modelPath = modelPath;
        this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
        this.networkComposer = new NetworkComposer(this, instanceVault, cachedComposedArchitectureSymbols, composedNetworkStructures);
        this.instanceVault = instanceVault;
        this.cachedComposedArchitectureSymbols = cachedComposedArchitectureSymbols;

    }

    public String findConfigFileName(EMAComponentInstanceSymbol instanceSymbol){
        if (this.composedNetworks == null) return null;

        for (NetworkStructureInformation networkStructureInformation: composedNetworks){
            if (networkStructureInformation.isInstancePartOfNetwork(instanceSymbol)) {
                return networkStructureInformation.getNetworkName();
            }
        }
        return null;
    }

    public boolean isComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.getSymbolReference() != null
                    && instanceSymbol.getComponentType().getName().equals(networkStructureInformation.getSymbolReference().getName())
                    && !networkStructureInformation.isAtomic()) {
                return true;
            }
        }
        return false;
    }

    public boolean isComposedNetAndHasConfig(EMAComponentInstanceSymbol instanceSymbol){
        return isComposedNet(instanceSymbol) && hasConfigFile(instanceSymbol, modelPath);
    }

    public Optional<ArchitectureSymbol> constructComposedNetworkArchitectureSymbol(EMAComponentInstanceSymbol instanceSymbol){

        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null
                    && networkStructureInformation.getSymbolReference() != null
                    && instanceSymbol.getComponentType().getReferencedSymbol().equals(networkStructureInformation.getSymbolReference().getReferencedSymbol())) {
                ArchitectureSymbol constructedSymbol = composeNetwork(networkStructureInformation,instanceSymbol);
                if (constructedSymbol != null){
                    return Optional.of(constructedSymbol);
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public boolean isPartOfComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        return isSubnetInstanceOfComposedNet(instanceSymbol);
    }

    public boolean isSubnetInstanceOfComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        Scope enclosingScope = instanceSymbol.getEnclosingScope();
        if (enclosingScope != null){
            if (enclosingScope instanceof CommonScope) {
                CommonScope commonScope = (CommonScope) enclosingScope;
                Optional<EMAComponentInstanceSymbol> symbolOpt = (Optional<EMAComponentInstanceSymbol>) commonScope.getSpanningSymbol();
                if (symbolOpt.isPresent()){
                    EMAComponentInstanceSymbol symbol = symbolOpt.get();
                    if (isComposedNet(symbol)){
                        return true;
                    } else {
                        isSubnetInstanceOfComposedNet(symbol);
                    }
                }
            }
            return false;
        }
        return false;
    }

    public Optional<ArchitectureSymbol> resolveArchitectureSymbolOfInstance(EMAComponentInstanceSymbol componentInstance){
        if (isComposedNet(componentInstance)){
            return constructComposedNetworkArchitectureSymbol(componentInstance);
        } else {
            return componentInstance.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        }
    }

    public Optional<ArchitectureSymbol> resolveArchitectureSymbolOfReferencedSymbol(EMAComponentInstanceSymbol componentInstance){
        if (isComposedNet(componentInstance)){
            return constructComposedNetworkArchitectureSymbol(componentInstance);
        } else {
            return componentInstance.getComponentType().getReferencedSymbol().getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        }
    }

    public ArrayList<EMAComponentInstanceSymbol> processComponentInstances(Set<EMAComponentInstanceSymbol> componentInstances){
        ArrayList<EMAComponentInstanceSymbol> networks = new ArrayList<>();

        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);

        for (EMAComponentInstanceSymbol instanceSymbol: componentInstances){
            EMAComponentSymbol component = instanceSymbol.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            if (architecture.isPresent()){
                networks.add(instanceSymbol);
            }
        }

        for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
            networkStructureInformation.addInstancesAndSymbolReference(componentInstances);
        }

        for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
            networks.addAll(networkStructureInformation.getInstances());
        }

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");
        return networks;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }

    public ArchitectureSymbol composeNetwork(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance){
        ArchitectureSymbol composedNetwork = networkComposer.generateComposedNetwork(networkStructureInformation, fromInstance);
        return composedNetwork;
    }

    public ArrayList<ArchitectureSymbol> fetchSubnetworkArchitectureSymbols(EMAComponentInstanceSymbol instanceSymbol){
        ArrayList<ArchitectureSymbol> architectureSymbols = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> foundInstanceSymbols = new ArrayList<>();

        if (instanceVault == null || instanceVault.size() == 0) return architectureSymbols;



        NetworkStructureInformation networkStructureInformation = null;

        for (NetworkStructureInformation net : this.composedNetworks){
            if (instanceSymbol.getComponentType().getReferencedSymbol()
                    .getName().equals(instanceSymbol.getComponentType().getReferencedSymbol().getName())){
                networkStructureInformation = net;
                for (NetworkStructureInformation subnet: networkStructureInformation.getSubNetworks()){
                    Map<String, Collection<Symbol>> symbols = instanceSymbol.getSpannedScope().getLocalSymbols();
                    ArrayList<Symbol> symbolArrayList = (ArrayList<Symbol>) symbols.get(subnet.getInstanceSymbolName());
                    for (Symbol sym : symbolArrayList){
                        EMAComponentInstanceSymbol foundInstanceSymbol = (EMAComponentInstanceSymbol) sym;
                        if (instanceSymbol.getName().equals(networkStructureInformation.getInstanceSymbolName()));{
                            foundInstanceSymbols.add(foundInstanceSymbol);
                            break;
                        }
                    }
                }
            }
        }

        for (EMAComponentInstanceSymbol componentInstanceSymbol : foundInstanceSymbols){
            for (EMAComponentInstanceSymbol vaultSymbol : instanceVault){
                if (componentInstanceSymbol.getName().equals(vaultSymbol.getName())) {
                    Optional<ArchitectureSymbol> optArch = componentInstanceSymbol.getSpannedScope().resolve("",ArchitectureSymbol.KIND);
                    if (optArch.isPresent()) architectureSymbols.add(optArch.get());
                }
            }
        }

        return architectureSymbols;
    }

    private boolean hasConfigFile(EMAComponentInstanceSymbol instanceSymbol, String modelPath){
        Log.info("Searching Config for Network: " + instanceSymbol.getName(),"NETWORK_COMPOSITION_CONFIG_SEARCH");

        boolean configExists = false;

        String fullName = instanceSymbol.getFullName().replace("\\.","/");


        if (Files.exists(Paths.get(modelPath + fullName + ".conf")) || Files.exists(Paths.get(modelPath + fullName + "_" + instanceSymbol.getName()+ ".conf"))) {
            configExists = true;
        }

        return configExists;
    }
}
