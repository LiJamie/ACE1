package edu.ntut.selab.equivalent;


import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.exception.MultipleListOrGridException;

import java.util.List;

public interface Equivalent {
    boolean checkTwoStateIsEquivalent(GUIState state1, GUIState state2);

    GUIState getStateInStateList(List<GUIState> stateList, GUIState state) throws MultipleListOrGridException;

    boolean checkStateIsInStateList(List<GUIState> stateList, GUIState state) throws MultipleListOrGridException;

    int getTotalDistanceEquivalentStateCount();

    int getTotalListGridEquivalentStateCount();
}
