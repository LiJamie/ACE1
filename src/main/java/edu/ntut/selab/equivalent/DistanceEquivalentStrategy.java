package edu.ntut.selab.equivalent;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Printer;

import java.util.HashMap;
import java.util.Map;


public class DistanceEquivalentStrategy extends EquivalentStateStrategy {
    private GUIState eqGUIState;
    private HashMap<Integer, GUIState> eqStateMap;

    private Map<Integer, Integer> calculateAllDistance(GUIState newState, StateGraph stateGraph) {
        Map<Integer, Integer> distanceMap = new HashMap<>();
        this.eqStateMap = new HashMap<>();
        for (GUIState state : stateGraph.getAllStates()) {
            if (newState.isSimilarTo(state)) {
                int distance = newState.calculateDistance(state);
                if (!distanceMap.containsKey(distance))
                    eqStateMap.put(distance, state);
                int occurs = distanceMap.containsKey(distance) ? distanceMap.get(distance) + 1 : 1;
                distanceMap.put(distance, occurs);
            }
        }
        return distanceMap;
    }

    @Override
    public boolean isEquivalent(GUIState newState, StateGraph stateGraph) {
        Map<Integer, Integer> distanceMap = this.calculateAllDistance(newState, stateGraph);
        for (int key : distanceMap.keySet()) {
            if (distanceMap.get(key) >= Config.MAX_OCCURS_OF_COMPONENT_VALUE) {
                Printer printer = new Printer();
                printer.equivalentState(this.getClass().getName());
                this.eqGUIState = this.eqStateMap.get(key);
                this.eqGUIState.setIsEquivalentState(true);
                newState.setIsEquivalentState(true);
                this.eqGUIState.addImage(newState.getImagelist().get(0));
                this.eqGUIState.increaseEquivalentStateCount();
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