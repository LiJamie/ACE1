package edu.ntut.selab;

import java.io.*;
import java.util.*;

import edu.ntut.selab.data.NodeAttribute;
import edu.ntut.selab.equivalent.*;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.event.AndroidEventFactory;
import edu.ntut.selab.event.EventData;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.log.LoggerStore;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import edu.ntut.selab.builder.ASDBuilder;
import edu.ntut.selab.builder.ATDBuilder;
import edu.ntut.selab.builder.ReportBuilder;
import edu.ntut.selab.builder.STDBuilder;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.util.XMLEventParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;


public class StateGraph implements Serializable {
    private List<GUIState> states;
    private int totalEventCount = 0;
    private String imagePath = null;
    private List<String> types = null, logs = null;
    private LoggerStore loggerStore = null;
    private int totalDistanceEquivalentStateCount = 0, totalListGridEquivalentStateCount = 0;
    private EquivalentStateStrategy compositeStrategy;
    private int stateRecordBeforeRestart = 0;
    private int restartCount = 0;

    public StateGraph() {
        this.states = new ArrayList<GUIState>();
    }

    // for clone
    public StateGraph(List<GUIState> states, int totalEventCount) {
        this.states = states;
        this.totalEventCount = totalEventCount;
    }

    public void increaseTotalExecutedEventCount() {
        this.totalEventCount++;
    }

    public GUIState getState(GUIState state) {
        return compositeStrategy.getEquivalentState();
    }

    public List<GUIState> getAllStates() {
        return this.states;
    }

    public boolean isInStateList(GUIState state) throws MultipleListOrGridException {
        if (this.states.isEmpty())
            return false;
        return compositeStrategy.isEquivalent(state, this);
    }

    // add state
    public void addState(GUIState state) {
        this.states.add(state);
    }

    @Override
    protected StateGraph clone() throws CloneNotSupportedException {
        return new StateGraph(this.states, this.totalEventCount);
    }

    public StateGraph removeCrossAppEvent() throws CloneNotSupportedException {
        StateGraph stateGraph = this.clone();
        for (GUIState state : stateGraph.getAllStates()) {
            List<AndroidEvent> removeEvents = new ArrayList<AndroidEvent>();
            for (AndroidEvent event : state.getEvents()) {
                if (event.isOverAttemptCountThreshold() || event.getToState().isOverCrossAppEventThreshold())
                    removeEvents.add(event);
            }
            state.getEvents().removeAll(removeEvents);
        }
        return stateGraph;
    }

    public List<AndroidEvent> getShortestUnfiredPath(GUIState currentState) {
        for (GUIState s : this.getAllStates()) {
            s.setVisited(false);
        }
        currentState.setVisited(true);
        Queue<AndroidEvent> eventQueue = new LinkedList<AndroidEvent>();
        List<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        for (AndroidEvent e : currentState.getEvents()) {
            // initial all event's pre event of current state
            if (!Config.BLOCK_NONDETERMINISTIC_EVENT || !e.isNonDeterministic()) {
                eventQueue.add(e);
                e.setTempPreviousEvent(null);
            }
        }
        while (!eventQueue.isEmpty()) {
            AndroidEvent event = eventQueue.poll();
            if (!event.isVisited()) {  // event is not visited
                if (!event.isOverAttemptCountThreshold()) {
                    eventSequence.add(event);
                    while (event.getTempPreviousEvent() != null) {
                        eventSequence.add(event.getTempPreviousEvent());
                        event = event.getTempPreviousEvent();
                    }
                    return reverseElementsInEventSequence(eventSequence); // only the last event is unvisited
                } else {
                    // TODO : log event is reach attempt count threshold
                    this.loggerStore.addErrorMessage(event.getName() + " cannot be visited within " + Config.ATTEMPT_COUNT_THRESHOLD + " times");
                    continue;
                }
            } else {  // event has visited
                if (event.getToState() == null) {
                    throw new AssertionError();
                }
                if (!event.isOverAttemptCountThreshold() && !event.getToState().isVisited()) {
                    if (!event.getToState().isOverCrossAppEventThreshold()) {
                        event.getToState().setVisited(true);
                        for (AndroidEvent ev : event.getToState().getEvents()) {
                            if (!Config.BLOCK_NONDETERMINISTIC_EVENT || !ev.isNonDeterministic()) {
                                eventQueue.add(ev);
                                ev.setTempPreviousEvent(event);
                            }
                        }
                    }
                }
            }
        }
        return eventSequence;
    }

    public List<AndroidEvent> reverseElementsInEventSequence(List<AndroidEvent> eventSequence) {
        List<AndroidEvent> reversedEventSequence = new ArrayList<AndroidEvent>();
        for (int i = 0; i < eventSequence.size(); i++) {
            reversedEventSequence.add(0, eventSequence.get(i));
        }
        return reversedEventSequence;
    }

    public void setLoggerStore(LoggerStore loggerStore) {
        this.loggerStore = loggerStore;
    }

    public LoggerStore getLoggerStore() {
        return this.loggerStore;
    }

    public void setTotalExecutedEventCount(int totalEventCount) {
        this.totalEventCount = totalEventCount;
    }

    public int getTotalExecutedEventCount() {
        return this.totalEventCount;
    }

    public void setTotalDistanceEquivalentStateCount(int totalDistanceEquivalentStateCount) {
        this.totalDistanceEquivalentStateCount = totalDistanceEquivalentStateCount;
    }

    public int getTotalDistanceEquivalentStateCount() {
        return this.totalDistanceEquivalentStateCount;
    }

    public void setTotalListGridEquivalentStateCount(int totalListGridEquivalentStateCount) {
        this.totalListGridEquivalentStateCount = totalListGridEquivalentStateCount;
    }

    public int getTotalListGridEquivalentStateCount() {
        return this.totalListGridEquivalentStateCount;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public List<String> getLogs() {
        return this.logs;
    }

    public void buildReport() throws InterruptedException, ExecuteCommandErrorException, IOException {
        imagePath = Utility.getReportPath();
        System.out.println("imagePath = " + this.imagePath);
        this.types = getTypeOfActivityNames();
        ReportBuilder stdBuilder = new STDBuilder(this);// build STD Report
        stdBuilder.buildDot();
        stdBuilder.buildSVG();
        stdBuilder.buildTxt();
        ReportBuilder asdBuilder = new ASDBuilder(this);// build ASD Report
        asdBuilder.buildDot();
        asdBuilder.buildSVG();
        ReportBuilder atdBuilder = new ATDBuilder(this);// build ATD Report
        atdBuilder.buildDot();
        atdBuilder.buildSVG();

        modifySVGFileImage();//modify picture path
    }

    public void updateStatesCrossAppDepth(GUIState currentState) {
        for (GUIState guiState : states) {
            if (guiState != currentState) {
                for (AndroidEvent event : guiState.getEvents()) {
                    if (event.getToState() == currentState) {
                        if ((event.getFromState().getCrossAppDepth() + 1) < currentState.getCrossAppDepth())
                            currentState.setCrossAppDepth((event.getFromState().getCrossAppDepth() + 1));
                    }
                }
            }
        }
    }

    private List<String> getTypeOfActivityNames() {
        List<String> types = new ArrayList<String>();
        types.clear();
        String activityName = null;
        for (int i = 0; i < states.size(); i++) {
            activityName = states.get(i).getActivityName();
            if (!types.contains(activityName)) {
                types.add(activityName);
            }
        }
        return types;
    }

    private void modifySVGFileImage() throws IOException {
        for (String activity : this.types) {
            File SVGFile = new File(imagePath + "/ActivitySubstateDiagram/" + activity + ".svg");
            File _SVGFile = new File(imagePath + "/ActivitySubstateDiagram/" + activity + "_" + ".svg");
            BufferedReader br = null;
            String line = null;
            PrintWriter out = null;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(SVGFile), "UTF-8"));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_SVGFile), "UTF-8")));
            while ((line = br.readLine()) != null) {
                if (line.contains("image") && line.contains("States")) {
                    out.println(line.replace("States", "../States"));
                } else {
                    out.println(line);
                }
            }
            out.close();
            br.close();
            SVGFile.delete();
            _SVGFile.renameTo(SVGFile);
        }
    }

    public void setEquivalentStragegy(EquivalentStateStrategy equivalentStateStrategy) {
        this.compositeStrategy = equivalentStateStrategy;
    }

    private GUIState getState(int id) {
        for (GUIState state : this.getAllStates())
            if (state.getId() == id)
                return state;
        return null;
    }

    public void guideEvents() throws DocumentException {
        File eventXml = new File("inputFieldData/eventXml.xml");
        if (Config.EVENT_ORDER_CUSTOMIZE && eventXml.exists()) {
            Document document = new SAXReader().read(eventXml);
            List stateNodes = document.getRootElement().selectNodes("//state");
            Map<Integer, List<AndroidEvent>> map = new HashMap<>();
            for (Object stateNode : stateNodes) {
                Element stateElement = (Element) stateNode;
                GUIState state = this.getState(Integer.parseInt(stateElement.attributeValue("id")));
                List<AndroidEvent> events = this.parseEvents(stateElement, state);
                state.getEvents().addAll(events);
            }
        }
    }

    private List<AndroidEvent> parseEvents(Element stateElement, GUIState state) {
        List eventNodes = ((Element) stateElement.content().get(0)).content();
        List<AndroidEvent> events = new ArrayList<>();
        AndroidEventFactory eventFactory = new AndroidEventFactory();
        for (Object eventNode : eventNodes) {
            Element eventElement = (Element) eventNode;
            // add default bounds for backkey and menukey
            if (eventElement.attributeValue(NodeAttribute.Bounds) == null)
                eventElement.add(new DefaultAttribute(NodeAttribute.Bounds, "[0,0][100,100]"));
            eventElement.add(new DefaultAttribute("text", ""));
            EventData eventData = new EventData(eventElement);
            if (eventElement.getText().compareTo("EditText") == 0) {
                // add edittext after guide parse
                XMLEventParser xmlEventParser = new XMLEventParser(state.contentClone());
                List<AndroidEvent> editTextEvents = xmlEventParser.parseEditTextEvent();
                events.addAll(editTextEvents);
            } else
                events.add(eventFactory.createAndroidEvent(eventElement.getText(), eventData));
        }
        return events;
    }
}
