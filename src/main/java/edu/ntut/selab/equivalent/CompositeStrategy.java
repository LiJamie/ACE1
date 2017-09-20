package edu.ntut.selab.equivalent;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.exception.MultipleListOrGridException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class CompositeStrategy extends EquivalentStateStrategy implements Serializable {
    protected GUIState eqGUIState = null;
    protected List<EquivalentStateStrategy> eqStateStrategy = null;

    protected CompositeStrategy() {
        this.eqStateStrategy = new ArrayList<>();
    }

    public abstract boolean isEquivalent(GUIState newState, StateGraph stateGraph) throws MultipleListOrGridException;

    protected boolean eqCompare(GUIState newState, StateGraph stateGraph) throws MultipleListOrGridException {
        for (EquivalentStateStrategy eqState : eqStateStrategy) {
            if (eqState.isEquivalent(newState, stateGraph)) {
                this.eqGUIState = eqState.getEquivalentState();
                return true;
            }
        }
        return false;
    }

    @Override
    public GUIState getEquivalentState() {
        return this.eqGUIState;
    }
}
