package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.CandidateBuilder;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.CandidateEvaluationResult;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingConf;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import java.util.List;

// For at most n iterations:
//    generate possible components (Exactly the same amount of layers as the biggest component in the network or one more)
//    for all components:
//        add component to network in parallel
//        train network
//        evaluate network
//        remove component from network
//    select best component
//    add best component to network in parallel
//    if network with best component is not better than network without best component:
//        stop adaNet and return network without best component

public class AdaNetAlgorithm extends NeuralArchitectureSearch {
    private final CandidateFinder candidateFinder;
    private final CandidateBuilder candidateBuilder;
    private boolean stopAlgorithm = false;
    private CandidateEvaluationResult bestCandidateResult;
    private ArchitectureSymbol architectureSymbol;

    public AdaNetAlgorithm() {
        this.candidateBuilder = new CandidateBuilder();
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(componentFinder);
    }

    public AdaNetAlgorithm(CandidateFinder candidateFinder, CandidateBuilder candidateBuilder) {
        super();
        this.candidateFinder = candidateFinder;
        this.candidateBuilder = candidateBuilder;
    }


    public ArchitectureSymbol getArchitectureSymbol() {
        return architectureSymbol;
    }

    public CandidateEvaluationResult getBestCandidateResult() {
        return bestCandidateResult;
    }


    @Override
    public ArchitectureSymbol execute(ArchitectureSymbol startNetwork) throws IllegalStateException {
        this.architectureSymbol = startNetwork;

        if (getTrainPipeline() == null) {
            throw new IllegalStateException("Train pipeline not set");
        }

        createFirstCandidate();
        executeIterations();

        setCandidateForArchitecture(bestCandidateResult.getCandidate());
        System.out.println();
        System.out.println();
        System.out.println("Best architecture: " + bestCandidateResult.getScore());
        printArchitecture(architectureSymbol);
        return architectureSymbol;
    }
    private void setCandidateForArchitecture(AdaNetCandidate bestCandidate) {
        architectureSymbol = candidateBuilder.build(bestCandidate, architectureSymbol);
    }

    private void createFirstCandidate() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate candidate = new AdaNetCandidate(component, null);

        this.bestCandidateResult = evaluateCandidate(candidate);
    }

    private void executeIterations() {
        for (int i = 0; i < AdaNetConfig.MAX_ITERATIONS; i++) {
            System.out.println();
            System.out.println("Iteration " + (i + 1) + " of " + AdaNetConfig.MAX_ITERATIONS);
            executeIteration();
            if (stopAlgorithm) {
                break;
            }
        }
    }

    private void executeIteration() {
        AdaNetCandidate bestCandidate = bestCandidateResult.getCandidate();
        List<AdaNetCandidate> candidates = candidateFinder.findCandidates(bestCandidate);
        CandidateEvaluationResult bestNewCandidate = selectBestCandidate(candidates);
        if (bestNewCandidate.getScore() <= bestCandidateResult.getScore()) {
            this.stopAlgorithm = true;
            return;
        }
        bestCandidateResult = bestNewCandidate;
    }

    private void printArchitecture(ArchitectureSymbol architectureSymbol) {
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        System.out.println(printer.prettyPrint(architectureSymbol));
    }

    private void removeCandidateFromArchitecture() {
        architectureSymbol = candidateBuilder.removeCandidateFromArchitecture(architectureSymbol);
    }

    private CandidateEvaluationResult evaluateCandidate(AdaNetCandidate candidate) {
        setCandidateForArchitecture(candidate);
        printArchitecture(architectureSymbol);

        getRunTracker().startNewRun();
        getRunTracker().logTag("AutoML Stage", "NAS: AdaNet");
        getRunTracker().logParams(ASTConfLangHelper.getParametersFromConfiguration(this.getTrainPipeline().getTrainingConfiguration()));
        getRunTracker().getArtifactHandler().setPlaintext(this.getTrainPipeline().getPrettyPrintedNetwork()).setFileName("network.txt").log();
        ConfigurationTrackingManager.executePipeline(getTrainPipeline(), "NAS: " + this.getClass().getSimpleName());
        getRunTracker().endRun();

        float score;
        if (ConfigurationTrackingConf.isEnabled()) {
            score = ConfigurationTrackingManager.getArtifact().getAccuracy();
        } else {
            score = this.getTrainPipeline().getTrainedAccuracy();
        }
        removeCandidateFromArchitecture();

        return new CandidateEvaluationResult(candidate, score);
    }

    private CandidateEvaluationResult selectBestCandidate(List<AdaNetCandidate> candidates) {
        CandidateEvaluationResult bestCandidate = new CandidateEvaluationResult(null, 0);
        for (int i = 0; i < candidates.size(); i++) {
            System.out.println("Evaluating candidate " + (i + 1) + " of " + candidates.size());
            AdaNetCandidate candidate = candidates.get(i);
            CandidateEvaluationResult evaluationResult = evaluateCandidate(candidate);
            if (evaluationResult.getScore() > bestCandidate.getScore()) {
                bestCandidate = evaluationResult;
            }
        }

        return bestCandidate;
    }
}
