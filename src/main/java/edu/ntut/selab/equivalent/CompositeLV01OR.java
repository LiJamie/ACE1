package edu.ntut.selab.equivalent;

import edu.ntut.selab.StateGraph;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.exception.MultipleListOrGridException;


public class CompositeLV01OR extends CompositeStrategy {
    private GUIState eqState = null;

    public CompositeLV01OR() {
        this.eqStateStrategy.add(new ExactlyEquivalentStrategy());
        this.eqStateStrategy.add(new ListGridViewEquivalentStrategy());
        this.eqStateStrategy.add(new DistanceEquivalentStrategy());
    }

    public boolean isEquivalent(GUIState newState, StateGraph stateGraph) throws MultipleListOrGridException {
        if (eqCompare(newState, stateGraph)) {
            this.eqState = eqGUIState;
            return true;
        } else
            return false;
    }

    @Override
    public GUIState getEquivalentState() {
        return this.eqState;
    }
}
