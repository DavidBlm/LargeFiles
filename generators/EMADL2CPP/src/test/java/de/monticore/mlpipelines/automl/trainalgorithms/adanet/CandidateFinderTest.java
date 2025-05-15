package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;

import java.util.List;

public class CandidateFinderTest extends TestCase {

    public void testConstructor() {
        CandidateFinder candidateFinder = new CandidateFinder();
        assertNotNull(candidateFinder);
    }

    public void testConstructorSetsComponentFinder() {
        CandidateFinder candidateFinder = new CandidateFinder();
        assertNotNull(candidateFinder.getComponentFinder());
    }

    public void testFindCandidatesReturnsTwoCandidates() {
        AdaNetComponent startComponent = new AdaNetComponent(1);
        AdaNetCandidate startCandidate = new AdaNetCandidate(startComponent, null);
        CandidateFinder candidateFinder = new CandidateFinder();
        List<AdaNetCandidate> candidates = candidateFinder.findCandidates(startCandidate);
        assertEquals(2, candidates.size());
    }

    public void testFindCandidatesReturnsCorrectCandidates() {
        AdaNetComponent startComponent = new AdaNetComponent(1);
        AdaNetCandidate startCandidate = new AdaNetCandidate(startComponent, null);
        CandidateFinder candidateFinder = new CandidateFinder();
        List<AdaNetCandidate> candidates = candidateFinder.findCandidates(startCandidate);
        assertEquals(1, candidates.get(0).getComponent().getDepth());
        assertEquals(2, candidates.get(1).getComponent().getDepth());
    }
}