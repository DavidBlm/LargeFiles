package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;

import java.util.ArrayList;
import java.util.List;

public class CandidateFinder {
    private final AdaNetComponentFinder componentFinder;
    private int minDepth;

    public CandidateFinder() {
        this.componentFinder = new AdaNetComponentFinder();
        this.minDepth = 1;
    }

    public CandidateFinder(AdaNetComponentFinder componentFinder) {
        this.componentFinder = componentFinder;
    }

    public List<AdaNetCandidate> findCandidates(AdaNetCandidate lastIterationBestCandidate) {
        this.minDepth = lastIterationBestCandidate.getComponent().getDepth();
        List<AdaNetComponent> adanetComponents = componentFinder.findComponents(minDepth);
        List<AdaNetCandidate> candidates = createCandidatesFromComponents(adanetComponents, lastIterationBestCandidate);

        return candidates;
    }

    private List<AdaNetCandidate> createCandidatesFromComponents(
            List<AdaNetComponent> adanetComponents,
            AdaNetCandidate lastIterationBestCandidate) {
        List<AdaNetCandidate> candidates = new ArrayList<>();
        for (AdaNetComponent component : adanetComponents) {
            AdaNetCandidate candidate = new AdaNetCandidate(component, lastIterationBestCandidate.getAllComponents());
            candidates.add(candidate);
        }
        return candidates;
    }


    public AdaNetComponentFinder getComponentFinder() {
        return componentFinder;
    }
}
