/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.helpers;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAConnectorSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAPortBuilder;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAPortSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceBuilder;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.resolving.ResolvingFilter;
import de.se_rwth.commons.logging.Log;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class ClusterFromTagsHelper {

    private ClusterFromTagsHelper() {
    }

    public static List<Set<EMAComponentInstanceSymbol>> getClusters(EMAComponentInstanceSymbol componentInstanceSymbol) {
        Graph<EMAComponentInstanceSymbol, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        componentInstanceSymbol.getSubComponents().forEach(graph::addVertex);
        graph.addVertex(componentInstanceSymbol);

        componentInstanceSymbol.getConnectorInstances().stream()
                .filter(c -> !(c.getSourcePort().getMiddlewareSymbol().isPresent() && c.getTargetPort().getMiddlewareSymbol().isPresent()))
                .forEach(c -> {
                    EMAComponentInstanceSymbol compSource = c.getSourcePort().getComponentInstance();
                    EMAComponentInstanceSymbol compTarget = c.getTargetPort().getComponentInstance();
                    if (compSource == null || compTarget == null) {
                        Log.error("ComponentInstance of source or target not found!");
                    }
                    if (!compSource.equals(compTarget)) {
                        graph.addEdge(compSource, compTarget);
                    }
                });

        ConnectivityInspector<EMAComponentInstanceSymbol, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        int instanceSetSize = connectivityInspector.connectedSetOf(componentInstanceSymbol).size();
        if (instanceSetSize != 1) {
            Log.warn("0x8EFC3: The supercomponent can only be connected to subcomponents via middleware ports!");
            return new ArrayList<>();
        }
        //All subcomps are connected via non mw -> only highest level needs to be generated
        if (instanceSetSize != componentInstanceSymbol.getSubComponents().size() + 1)
            graph.removeVertex(componentInstanceSymbol);

        List<Set<EMAComponentInstanceSymbol>> res = connectivityInspector.connectedSets();
        return res;
    }

    public static List<EMAComponentInstanceSymbol> getClusterSubcomponents(EMAComponentInstanceSymbol componentInstanceSymbol) {
        List<Set<EMAComponentInstanceSymbol>> clusters = getClusters(componentInstanceSymbol);
        List<EMAComponentInstanceSymbol> res = new ArrayList<>();
        int[] i = {0};
        clusters.forEach(c -> {
            if (c.size() == 1) {
                res.add(c.iterator().next());
            } else {
                String clusterPrefix = componentInstanceSymbol.getName() + "_cluster";
                while (componentInstanceSymbol.getSubComponent(clusterPrefix + i[0]).isPresent()) {
                    i[0]++;
                }
                res.add(createECISFromCluster(componentInstanceSymbol, c, clusterPrefix + i[0]));
                i[0]++;
            }
        });
        return res;
    }


    //    Cases: Connector Super,Sub -> Super,Sub
//    Super normal -> *: 0x8EFC3
//    Super Ros -> * normal: 0x8EFC3
//    Super Ros -> Super Ros: remove and warn
//    Super Ros -> Sub Ros: handled(let ros connect)
//    Sub normal -> * Ros: disallowed, RosTargetRosSender CoCo
//    Sub Ros -> Super normal: 0x8EFC3
//    Sub Ros -> Super Ros: handled(let ros connect)
//    Sub Ros -> Sub Ros: do nothing, wrapper and therefore the mw does not affect this level
//    Sub Ros -> Sub normal: handled(let comp connect)
    private static EMAComponentInstanceSymbol createECISFromCluster(EMAComponentInstanceSymbol inst, Set<EMAComponentInstanceSymbol> cluster, String clusterName) {
        Set<EMAPortInstanceSymbol> curClusterPorts = cluster.stream().flatMap(ecis -> ecis.getPortInstanceList().stream()).collect(Collectors.toSet());
        Set<EMAPortInstanceSymbol> combinedPorts = new HashSet<>();
        combinedPorts.addAll(curClusterPorts);
        combinedPorts.addAll(inst.getPortInstanceList());

        Set<EMAConnectorSymbol> tmpConnectiors = inst.getConnectorInstances().stream()
                //remove all connections that use subcomponents not in cluster
                .filter(c -> combinedPorts.contains(c.getSourcePort()) && combinedPorts.contains(c.getTargetPort()))
                //remove all connections from super -> super and warn
                .filter(c -> {
                    if (inst.containsPort(c.getSourcePort()) && inst.containsPort(c.getTargetPort())) {
                        Log.warn("Direct connections from super comp to super comp are not supported!");
                        return false;
                    }
                    return true;
                })
                //remove all connections super <-> cluster
                .filter(c -> !(inst.getPortInstanceList().contains(c.getSourcePort()) || (inst.getPortInstanceList().contains(c.getTargetPort()))))
                .collect(Collectors.toSet());

        Collection<EMAPortInstanceSymbol> mwPorts = curClusterPorts.stream()
                .filter(p -> p.getMiddlewareSymbol().isPresent())
                .collect(Collectors.toSet());

        //TODO: use EMAPortSymbol and EMAConnectorSymbol
        List<EMAPortSymbol> tmpPorts = mwPorts.stream()
                .filter(p -> p.getMiddlewareSymbol().isPresent())
                .map(p -> {
                    String sourcePortName;
                    String targetPortName;
                    String subName = p.getComponentInstance().getName();
                    if (p.isIncoming()) {
                        sourcePortName = p.getName();
                        targetPortName = subName + "." + p.getName();
                    } else {
                        sourcePortName = subName + "." + p.getName();
                        targetPortName = p.getName();
                    }
                    EMAConnectorSymbol tmpConnector = EMAConnectorSymbol.builder()
                            .setSource(sourcePortName)
                            .setTarget(targetPortName)
                            .build();
                    tmpConnectiors.add(tmpConnector);
                    return EMAPortBuilder.clone(p);
                })
                .collect(Collectors.toList());

        Set<ResolvingFilter<? extends Symbol>> resolvingFilters = inst.getSpannedScope().getResolvingFilters();
        List<EMAComponentInstanceSymbol> tmpSubcomps = cluster.stream().map(EMAComponentInstanceBuilder::clone).collect(Collectors.toList());
        tmpSubcomps.forEach(sc -> ((CommonScope) sc.getSpannedScope()).setResolvingFilters(resolvingFilters));
        EMAComponentInstanceSymbol res = new EMAComponentInstanceBuilder()
                .setName(clusterName)
                .setPackageName(inst.getPackageName())
                .setSymbolReference(inst.getComponentType())
                .addPorts(tmpPorts)
                .addConnectors(tmpConnectiors)
                .addSubComponents(tmpSubcomps)
                .addResolutionDeclarationSymbols(inst.getResolutionDeclarationSymbols())
                .build();

        ((CommonScope) res.getSpannedScope()).setResolvingFilters(resolvingFilters);
        res.setEnclosingScope((MutableScope) inst.getEnclosingScope());
        return res;
    }

}
