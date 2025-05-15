package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class AdaNetCandidateTest extends TestCase {

    public void testConstructor() {
        AdaNetCandidate adanetCandidate = new AdaNetCandidate();
        assertNotNull(adanetCandidate);
    }

    public void testConstructorWithParameters() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertNotNull(adanetCandidate);
    }

    public void testConstructorCreatesEmptyPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertNotNull(adanetCandidate.getPreviousComponents());
        assertEquals(0, adanetCandidate.getPreviousComponents().size());
    }

    public void testGetComponent() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertEquals(component, adanetCandidate.getComponent());
    }

    public void testSetComponent() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate();
        adanetCandidate.setComponent(component);
        assertEquals(component, adanetCandidate.getComponent());
    }

    public void testGetPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, previousComponents);
        assertEquals(previousComponents, adanetCandidate.getPreviousComponents());
    }

    public void testSetPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        adanetCandidate.setPreviousComponents(previousComponents);
        assertEquals(previousComponents, adanetCandidate.getPreviousComponents());
    }

    public void testGetAllComponentsWithEmptyPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        List<AdaNetComponent> allComponents = adanetCandidate.getAllComponents();
        assertEquals(1, allComponents.size());
        assertEquals(component, allComponents.get(0));
    }

    public void testGetAllComponentsWithNonEmptyPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetComponent previousComponent = new AdaNetComponent(2);
        previousComponents.add(previousComponent);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, previousComponents);

        List<AdaNetComponent> allComponents = adanetCandidate.getAllComponents();

        assertEquals(2, allComponents.size());
        assertEquals(previousComponent, allComponents.get(0));
        assertEquals(component, allComponents.get(1));
    }

    public void testGetParallelCandidateLayers() {
        AdaNetComponent component = new AdaNetComponent(2, 20);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetComponent previousComponent = new AdaNetComponent(1, 10);
        previousComponents.add(previousComponent);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, previousComponents);

        List<ParallelCandidateLayer> parallelCandidateLayers = adanetCandidate.getParallelCandidateLayers();
        ParallelCandidateLayer parallelCandidateLayer1 = parallelCandidateLayers.get(0);
        ParallelCandidateLayer parallelCandidateLayer2 = parallelCandidateLayers.get(1);

        assertEquals(2, parallelCandidateLayers.size());
        assertEquals(2, parallelCandidateLayer1.getElements().size());
        assertEquals(1, parallelCandidateLayer2.getElements().size());
        assertEquals(10, parallelCandidateLayer1.getElements().get(0).getUnits());
        assertEquals(20, parallelCandidateLayer1.getElements().get(1).getUnits());
        assertEquals(20, parallelCandidateLayer2.getElements().get(0).getUnits());
    }
}