/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.cli;

import de.monticore.lang.monticar.clustering.cli.algorithms.AlgorithmCliParameters;
import de.monticore.lang.monticar.clustering.cli.algorithms.dynamic.DynamicAlgorithmCliParameters;
import de.monticore.lang.monticar.clustering.cli.algorithms.dynamic.DynamicParameter;
import de.monticore.lang.monticar.clustering.cli.algorithms.dynamic.DynamicSpectralClusteringCliParameters;
import de.monticore.lang.monticar.clustering.cli.algorithms.dynamic.ListParameter;
import de.monticore.lang.monticar.clustering.qualityMetric.Metric;
import de.monticore.lang.monticar.clustering.qualityMetric.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClusteringParameters {
    private Integer numberOfClusters;
    private Boolean flatten;
    private Integer flattenLevel;
    private ResultChoosingStrategy chooseBy = ResultChoosingStrategy.bestWithFittingN;
    private List<DynamicAlgorithmCliParameters> algorithmParameters = new ArrayList<>();
    private MetricType metric;

    public ClusteringParameters() {
    }

    public Optional<Integer> getNumberOfClusters() {
        return Optional.ofNullable(numberOfClusters);
    }

    public ResultChoosingStrategy getChooseBy() {
        return chooseBy;
    }

    public List<DynamicAlgorithmCliParameters> getDynamicAlgorithmCliParameters(){
        if(getNumberOfClusters().isPresent()){
            DynamicParameter n = new ListParameter(getNumberOfClusters().get().doubleValue());
            algorithmParameters.stream()
                    .filter(a -> a.getName().equals(AlgorithmCliParameters.TYPE_SPECTRAL_CLUSTERING))
                    .forEach(a -> ((DynamicSpectralClusteringCliParameters)a).setNumberOfClusters(n));
        }
        return algorithmParameters;
    }

    public List<AlgorithmCliParameters> getAlgorithmParameters() {
        return getDynamicAlgorithmCliParameters()
                .stream()
                .flatMap(d -> d.getAll().stream())
                .collect(Collectors.toList());
    }

    public boolean getFlatten(){
        return flatten == null ? false : flatten;
    }

    public Optional<Integer> getFlattenLevel() {
        return Optional.ofNullable(flattenLevel);
    }

    public Metric getMetric() {
        if(metric == null){
            return MetricType.CommunicationCost.toMetric();
        }else{
            return metric.toMetric();
        }
    }
}
