package edu.ntut.selab.equivalent;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.StateGraph;


public class ExactlyEquivalentStrategy extends EquivalentStateStrategy {
    private GUIState eqGUIState;

    public boolean isEquivalent(GUIState newState, StateGraph stateGraph) {
        for (GUIState compareState : stateGraph.getAllStates()) {
            if (compareState.isExactlyEquivalentTo(newState)) {
                this.eqGUIState = compareState;
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