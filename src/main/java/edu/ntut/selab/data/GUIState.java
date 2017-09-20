package edu.ntut.selab.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.ntut.selab.event.EditTextEvent;
import edu.ntut.selab.event.EventOrder;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import edu.ntut.selab.util.XMLEventParser;
import org.dom4j.*;

import edu.ntut.selab.event.AndroidEvent;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;


public class GUIState implements Serializable {
    private String activityName;
    private ArrayList<AndroidEvent> events = null;
    private boolean isVisited;
    private int Id = -1;
    private Document content = null;
    private boolean isEquivalentState = false;
    private int crossAppDepth = 0;
    private boolean containListOrGrid = false; // list and grid equivalent
    private boolean isDynamicListAndGrid = false;
    private int equivalentStateCount = 0;
    private ArrayList<String> imageList = new ArrayList<String>();
    private int gridSize = 0;
    private int listSize = 0;
    private GUIState parentState = null;
    private boolean isUnrecognizableState = false;
    private boolean isProgressBarTimeoutState;
    private String tag = "";
    private int nafCount = 0, nafEquivalentCounter = 0, compareStateNotNAFButSelfNAFCounter = 0;

    public GUIState(Document content, List<AndroidEvent> eventList) {
        // TODO Auto-generated constructor stub
        //Id = stateId;
        this.setEvents(eventList);
        this.content = (Document) content.clone();
        this.isVisited = false;
        this.activityName = null;
        this.isUnrecognizableState = false;
        this.isProgressBarTimeoutState = false;
    }

    public String getPackageName() throws NullPackageNameException {
        if (this.isUnrecognizableState() || this.isProgressBarTimeoutState())
            return Config.PACKAGE_NAME;
        Element element = this.contentClone().getRootElement();
        List nodes = element.selectNodes("//node");
        for (Object node : nodes) {
            Element e = (Element) node;
            if (e.attribute(NodeAttribute.Package) != null)
                return e.attribute(NodeAttribute.Package).getText();
        }
        throw new NullPackageNameException();
    }

    public String getActivityName() {
        if (this.isUnrecognizableState())
            return "UnrecognizableStateActivity";
        else if (this.isProgressBarTimeoutState)
            return "ProgressBarTimeoutStateActivity";
        return this.activityName;
    }

    public void setActivityName(String activity) {
        this.activityName = activity;
    }

    public void setUnrecognizableState(boolean isUnrecognizable) {
        this.isUnrecognizableState = isUnrecognizable;
    }

    public boolean isUnrecognizableState() {
        return this.isUnrecognizableState;
    }

    public boolean isProgressBarTimeoutState() {
        return this.isProgressBarTimeoutState;
    }

    public boolean isCrashState() {
        String[] message = Config.CRASH_MESSAGE.split(" ");
        Element rootElement = this.content.getRootElement();
        List nodes = rootElement.selectNodes("//node");
        for (Iterator iterator = nodes.iterator(); iterator.hasNext(); ) {
            Element node = (Element) iterator.next();
            String textAttribute = node.attributeValue(NodeAttribute.Text);
            if (textAttribute != null && textAttribute.contains(message[0]) && textAttribute.contains(message[1]))
                return true;
        }
        return false;
    }

    public boolean isExactlyEquivalentTo(GUIState state) {
        List compNodes = this.contentClone().selectNodes("//node");
        List newNodes = state.contentClone().selectNodes("//node");
        if (compNodes.size() != newNodes.size())
            return false;
        Iterator compNodesIterator = compNodes.iterator();
        Iterator newNodesIterator = newNodes.iterator();
        while (compNodesIterator.hasNext()) {
            Element compNode = (Element) compNodesIterator.next();
            Element newNode = (Element) newNodesIterator.next();
            if (!this.areAttributesEquals(compNode, newNode))
                return false;
        }
        return true;
    }

    public boolean isSimilarTo(GUIState state) {
        Document selfElement = this.contentClone();
        Document element = state.contentClone();
        return this.areElementsSimilar(selfElement.getRootElement(), element.getRootElement());
    }

    private boolean areElementsSimilar(Element element1, Element element2) {
        if (element1.elements().size() != element2.elements().size())
            return false;
        for (int i = 0; i < element1.elements().size(); i++) {
            if (!this.areElementsSimilar((Element) element1.elements().get(i),
                    (Element) element2.elements().get(i)))
                return false;
        }
        return true;
    }

    public int calculateDistance(GUIState newState) {
        Element selfElement = this.contentClone().getRootElement();
        Element newStateElement = newState.contentClone().getRootElement();
        return this.calculateElementDistance(selfElement, newStateElement);
    }

    private int calculateElementDistance(Element selfElement, Element newElement) {
        int distance = 0;
        if (!this.areAttributesEquals(selfElement, newElement)) {
            distance += 1;
        }
        distance += Math.abs(selfElement.elements().size() - newElement.elements().size());
        for (int i = 0; i < Utility.getMin(selfElement.elements().size(), newElement.elements().size()); i++) {
            distance += this.calculateElementDistance((Element) selfElement.elements().get(i), (Element) newElement.elements().get(i));
        }
        return distance;
    }

    private List getElementExcludeListAndGrid(Document document) {
        final String LIST_VIEW_XPATH = "//node[@class='android.widget.ListView']";
        final String GRID_VIEW_XPATH = "//node[@class='android.widget.GridView']";
        List nodes = document.selectNodes("//node");
        List listGridNodes = document.selectNodes(LIST_VIEW_XPATH);
        listGridNodes.addAll(document.selectNodes(GRID_VIEW_XPATH));
        for (Object node : listGridNodes) {
            Element e = (Element) node;
            nodes.removeAll(e.elements());
            nodes.remove(node);
        }
        return nodes;
    }

    private boolean areNodesAttributeEquals(List selfNodes, List stateNodes) {
        if (selfNodes.size() != stateNodes.size())
            return false;
        Iterator selfNodesIterator = selfNodes.iterator();
        Iterator stateNodesIterator = stateNodes.iterator();
        while (selfNodesIterator.hasNext()) {
            Element selfElement = (Element) selfNodesIterator.next();
            Element stateElement = (Element) stateNodesIterator.next();
            if (!this.areAttributesEquals(selfElement, stateElement))
                return false;
        }
        return true;
    }

    public boolean areTheSameExcludeListAndGrid(GUIState state) {
        List selfNodes = this.getElementExcludeListAndGrid(this.contentClone());
        List stateNodes = this.getElementExcludeListAndGrid(state.contentClone());
        return this.areNodesAttributeEquals(selfNodes, stateNodes);
    }

    private boolean areAttributesEquals(Element element, Element newElement) {
        List<String> excludeAttribute = new ArrayList<>();
        excludeAttribute.add(NodeAttribute.Focused);
        excludeAttribute.add(NodeAttribute.Selected);
        if (Config.IGNORE_BOUNDS_ATTRIBUTE) {
            excludeAttribute.add(NodeAttribute.Bounds);
        }
        if (Config.IGNORE_NAF)
            return areAttributeEqualsWhenIgnoreNAF(element, newElement, excludeAttribute);
        else
            return areAttributeEquals(element, newElement, excludeAttribute);
    }

    private boolean areAttributeEqualsWhenIgnoreNAF(Element element, Element newElement, List<String> excludeAttribute) {
        if (element.attributeValue("NAF") == null && newElement.attributeValue("NAF") == null)
            return this.areAttributeEquals(element, newElement, excludeAttribute);
        else if (element.attribute("NAF") != null && newElement.attributeValue("NAF") == null)
            this.compareStateNotNAFButSelfNAFCounter++;
        int elementAttributeSize = element.attributeValue("NAF") != null && element.attributeValue("NAF").compareTo("true") == 0 ? element.attributeCount() - 1 : element.attributeCount();
        int newElementAttributeSize = newElement.attributeValue("NAF") != null && newElement.attributeValue("NAF").compareTo("true") == 0 ? newElement.attributeCount() - 1 : newElement.attributeCount();
        if (elementAttributeSize != newElementAttributeSize)
            return false;
        if (this.specificAttributeNotEqual(element, newElement))
            return false;
        Iterator attributeIterator = element.attributeIterator();
        while (attributeIterator.hasNext()) {
            Attribute attribute = (Attribute) attributeIterator.next();
            if (attribute.getName() == "NAF")
                continue;
            if (newElement.attributeValue(attribute.getName()) == null)
                return false;
        }
        this.nafEquivalentCounter++;
        return true;
    }

    private boolean specificAttributeNotEqual(Element element, Element newElement) {
        String compareAttribute = "";
        String elementClassName = element.attributeValue(NodeAttribute.Class);
        if (elementClassName.contains(NodeAttribute.TextView) || elementClassName.contains(NodeAttribute.EditText))
            compareAttribute = NodeAttribute.Text;
        else if (elementClassName.contains(NodeAttribute.ImageView) || elementClassName.contains(NodeAttribute.Button))
            compareAttribute = NodeAttribute.ResourceId;
        else
            return false;
        // some states have imageView but no resource-id
        if (element.attributeValue(compareAttribute) != null && newElement.attributeValue(compareAttribute) != null)
            return element.attributeValue(compareAttribute).compareTo(newElement.attributeValue(compareAttribute)) != 0;
        else    // if states has imageView but no resource-id
            return false;
    }

    private boolean areAttributeEquals(Element element, Element newElement, List<String> excludeAttribute) {
        if (element.attributes().size() != newElement.attributes().size())
            return false;
        for (Object attribute : element.attributes()) {
            String key = ((DefaultAttribute) attribute).getName();
            if (excludeAttribute.contains(key))
                continue;
            if (!element.attributeValue(key).equals(newElement.attributeValue(key)))
                return false;
        }
        return true;
    }

    public boolean isOverCrossAppEventThreshold() {
        return this.crossAppDepth >= Config.CROSS_APP_EVENT_THRESHHOLD;
    }

    public void addEvent(AndroidEvent event) {
        this.events.add(event);
    }

    public List<AndroidEvent> getEvents() {
        return this.events;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public boolean isVisited() {
        return this.isVisited;
    }

    public static GUIState convertDeviceXMLToGUIState(final String XMLFilePath, EventOrder eventOrder, final String ACTIVITY_NAME) throws DocumentException, ClickTypeErrorException, NullPackageNameException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(XMLFilePath);
        XMLEventParser xmlEventParser = new XMLEventParser(document);
        List<AndroidEvent> events = xmlEventParser.parseEvents();
        List<AndroidEvent> orderedEvents = eventOrder.order(events);
        GUIState state = new GUIState(document, orderedEvents);
        state.setNafCount(xmlEventParser.getNAFCount());
        if (state.isCrashState())
            state.clearEvents();
        if (!state.getPackageName().equals(Config.PACKAGE_NAME))
            state.setCrossAppDepth(Config.CROSS_APP_EVENT_THRESHHOLD);
        state.setActivityName(ACTIVITY_NAME);
        return state;
    }

//    public boolean isListEquivalentTo(GUIState state, final int THRESHOLD) {
//      TODO : implement
//    }
//
//    public boolean isGridEquivalentTo(GUIState state, final int THRESHOLD) {
//      TODO : implement
//    }

    public Document contentClone() {
        return (Document) content.clone();
    }

    public void setEvents(List<AndroidEvent> eventList) {
        this.events = (ArrayList<AndroidEvent>) ((ArrayList<AndroidEvent>) eventList).clone();
    }

    public void clearEvents() {
        this.events.clear();
    }

    public void setId(int stateId) {
        this.Id = stateId;
    }

    public int getId() {
        return Id;
    }

    public void setParent(GUIState parent) {
        this.parentState = parent;
    }

    public GUIState getParent() {
        return this.parentState;
    }

    public void setCrossAppDepth(int crossAppDepth) {
        this.crossAppDepth = crossAppDepth;
    }

    public int getCrossAppDepth() {
        return this.crossAppDepth;
    }

    @Deprecated
    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    public int getListSize() throws MultipleListOrGridException {
        List nodes = this.contentClone().getRootElement().selectNodes("//node[@class='android.widget.ListView']");
        if (nodes.isEmpty())
            return 0;
        else if (nodes.size() == 1)
            return ((Element) nodes.get(0)).elements().size();
        else
            throw new MultipleListOrGridException();
    }

    @Deprecated
    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getGridSize() throws MultipleListOrGridException {
        List nodes = this.contentClone().getRootElement().selectNodes("//node[@class='android.widget.GridView']");
        if (nodes.isEmpty())
            return 0;
        else if (nodes.size() == 1)
            return ((Element) nodes.get(0)).elements().size();
        else
            throw new MultipleListOrGridException();
    }

    public void setIsEquivalentState(boolean isEquivalentState) {
        this.isEquivalentState = isEquivalentState;
    }

    public boolean isEquivalentState() {
        return this.isEquivalentState;
    }

    @Deprecated
    public void setIsListAndGrid(boolean isListAndGrid) {
        this.containListOrGrid = isListAndGrid;
    }

    public boolean containList() {
        List nodes = this.contentClone().getRootElement().selectNodes("//node");
        for (Object node : nodes) {
            Element element = (Element) node;
            String className = element.attributeValue(NodeAttribute.Class);
            if (className != null && className.equals(NodeAttribute.ListView))
                return true;
        }
        return false;
    }

    public boolean containGrid() {
        List nodes = this.contentClone().getRootElement().selectNodes("//node");
        for (Object node : nodes) {
            Element element = (Element) node;
            String className = element.attributeValue(NodeAttribute.Class);
            if (className != null && className.equals(NodeAttribute.GridView))
                return true;
        }
        return false;
    }

    public boolean containListOrGrid() {
        return (this.containList() || this.containGrid());
    }

    public void setIsDynamicListAndGrid(boolean isDynamicListAndGrid) {
        this.isDynamicListAndGrid = isDynamicListAndGrid;
    }

    public boolean isDynamicListAndGrid() {
        return this.isDynamicListAndGrid;
    }

    public int getEquivalentStateCount() {
        return this.equivalentStateCount;
    }

    public void increaseEquivalentStateCount() {
        this.equivalentStateCount++;
    }

    public void setImage() {
        this.imageList.add(this.Id + ".png"); // 0.png, 1.png, 2.png....
    }

    // TODO : refactor
    public void addImage(String image) {
        this.imageList.add(image);
    }

    public ArrayList<String> getImagelist() {
        return imageList;
    }

    public void changeImage(String image) {
        this.imageList.remove(0);
        this.imageList.add(0, image);
    }

    public boolean hasFewerElementThan(GUIState state) {
        return this.hasFewerElement(this.contentClone().getRootElement(), state.contentClone().getRootElement());
    }

    // element1 = currentState XML file, element2 = one of stateList's state XML file
    private boolean hasFewerElement(Element element1, Element element2) {
        if (element1.elements().size() < element2.elements().size())
            return true;
        for (int i = 0; i < Math.min(element1.elements().size(), element2.elements().size()); i++) {
            if (this.hasFewerElement((Element) element1.elements().get(i), (Element) element2.elements().get(i)))
                return true;
        }
        return false;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public void setNafCount(int nafCount) {
        this.nafCount = nafCount;
    }

    public int getNafCount() {
        return this.nafCount;
    }

    public int getNafEquivalentCounter() {
        return this.nafEquivalentCounter;
    }

    public int getCompareStateNotNAFButSelfNAFCounter() {
        return this.compareStateNotNAFButSelfNAFCounter;
    }

    // for guide
    public void updateEditTextEvents(List<AndroidEvent> editTextEvents) {
        List<AndroidEvent> removeEvent = new ArrayList<AndroidEvent>();
        for (AndroidEvent event : this.getEvents()) {
            if (event instanceof EditTextEvent)
                removeEvent.add(event);
        }
        this.events.removeAll(removeEvent);
        this.events.addAll(editTextEvents);
    }

    public void setProgressBarTimeoutState(boolean isProgressBarTimeoutState) {
        this.isProgressBarTimeoutState = isProgressBarTimeoutState;
    }
}
