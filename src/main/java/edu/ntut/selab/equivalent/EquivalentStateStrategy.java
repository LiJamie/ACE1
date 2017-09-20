package edu.ntut.selab.equivalent;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.data.NodeAttribute;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.util.Config;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EquivalentStateStrategy implements Serializable {
    private List<String> excludeAttribute;
    private Element compareNodeData, newNodeData;
    private int listGridSizeThreshold = 0;
    protected int totalEquivalentStateCount = 0;


    public abstract boolean isEquivalent(GUIState newState, StateGraph stateGraph) throws MultipleListOrGridException;

    public abstract GUIState getEquivalentState();

    public EquivalentStateStrategy() {
        this.listGridSizeThreshold = Config.LIST_GRID_SIZE_THRESHOLD;
        this.iniExcludeAttribute();
    }


    protected boolean areAttributesEqual(Element element, Element newElement) {
        if (element.attributes().size() != newElement.attributes().size())
            return false;
        for (Object attribute : element.attributes()) {
            String key = ((DefaultAttribute) attribute).getName();
            if (this.excludeAttribute.contains(key))
                continue;
            if (!element.attributeValue(key).equals(newElement.attributeValue(key)))
                return false;
        }
        return true;
    }

    private void iniExcludeAttribute() {
        this.excludeAttribute = new ArrayList<>();
        excludeAttribute.add(NodeAttribute.Focused);
        excludeAttribute.add(NodeAttribute.Selected);
        if (Config.IGNORE_BOUNDS_ATTRIBUTE) {
            excludeAttribute.add(NodeAttribute.Bounds);
        }
    }

    private int calculateListOrGridEquivalentCount(Document document1, Document document2, String selectNodes) {
        List document1ListNodes = document1.selectNodes(selectNodes);
        List document2ListNodes = document2.selectNodes(selectNodes);
        List document1ListSubElements = new ArrayList();
        for (Object node : document1ListNodes) {
            document1ListSubElements.addAll(((Element) node).elements());
        }
        List document2ListSubElements = new ArrayList();
        for (Object node : document2ListNodes) {
            document2ListSubElements.addAll(((Element) node).elements());
        }
        int count = 0;
        for (Object document1Node : document1ListSubElements) {
            for (Object document2Node : document2ListSubElements) {
                if (this.areAttributesEqual((Element) document1Node, (Element) document2Node)) {
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    @Deprecated
    protected boolean areEquivalent(Element element1, Element element2, HashMap<String, Integer> lengthMap, GUIState state,
                                    GUIState currentState, String checkType) {

        for (int i = 0; i < Math.max(element1.elements().size(), element2.elements().size()); i++) {
            if ((i == element1.elements().size()) || (i == element2.elements().size()))
                return false;
            if (((element1.attributeValue(NodeAttribute.Class) != null) && (element2.attributeValue(NodeAttribute.Class) != null))
                    && (element1.attributeValue(NodeAttribute.Class).equals(checkType))
                    && (element2.attributeValue(NodeAttribute.Class).equals(checkType))) {

                if (lengthMap.get(getCurrentXpath(getStringList(element1))) == null)
                    lengthMap.put(getCurrentXpath(getStringList(element1)), 0);

                if (!areEquivalent((Element) element1.elements().get(i), (Element) element2.elements().get(i), lengthMap, state,
                        currentState, checkType)) {
                    return false;
                } else {
                    int lengthValue = lengthMap.get(getCurrentXpath(getStringList(element1))) + 1;
                    lengthMap.put(getCurrentXpath(getStringList(element1)), lengthValue);
                    if (lengthMap.get(getCurrentXpath(getStringList(element1))) >= this.listGridSizeThreshold) {
                        this.totalEquivalentStateCount++;
                        state.setIsEquivalentState(true);
                        if (!state.getImagelist().contains(currentState.getImagelist().get(0)))
                            state.addImage(currentState.getImagelist().get(0));
                        state.increaseEquivalentStateCount();
                        state.setIsListAndGrid(true);
                        return true;
                    }
                }
            } else {
                if (!areEquivalent((Element) element1.elements().get(i), (Element) element2.elements().get(i), lengthMap, state,
                        currentState, checkType))
                    return false;
            }
        }
        return true;
    }

    private ArrayList<String> getStringList(Element element) {
        ArrayList<String> stringList = new ArrayList<String>();
        ArrayList<String> resultList = new ArrayList<String>();
        while (!element.isRootElement()) {
            stringList.add(element.getName() + "[@'" + element.attributeValue(NodeAttribute.Class) + "']"
                    + "[@'index = " + element.attributeValue(NodeAttribute.Index) + "']");
            if (element.getParent() != null)
                element = element.getParent();
        }
        for (String s : stringList) { // reverse list
            resultList.add(0, s);
        }
        return resultList;
    }

    private String getCurrentXpath(ArrayList<String> elementString) {
        String str = "//";
        boolean first = true;
        for (String s : elementString) {
            if (first)
                str = str + s;
            else
                str = str + "/" + s;
            first = false;
        }
        return str;
    }
}
