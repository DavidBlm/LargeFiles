package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import junit.framework.TestCase;

public class CandidateEvaluationResultTest extends TestCase {

    public void testConstructor() {
        CandidateEvaluationResult result = new CandidateEvaluationResult(null, 0.5f);
        assertNotNull(result);
    }

    public void testGetScore() {
        CandidateEvaluationResult result = new CandidateEvaluationResult(null, 0.5f);
        assertEquals(0.5f, result.getScore());
    }

    public void testGetCandidate() {
        AdaNetCandidate candidate = new AdaNetCandidate();
        CandidateEvaluationResult result = new CandidateEvaluationResult(candidate, 0.5f);
        assertEquals(candidate, result.getCandidate());
    }
}