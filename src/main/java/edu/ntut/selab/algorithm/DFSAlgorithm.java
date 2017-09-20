package edu.ntut.selab.algorithm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import edu.ntut.selab.*;
import edu.ntut.selab.event.AndroidEventFactory;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.EventFromStateErrorException;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import org.dom4j.DocumentException;

import edu.ntut.selab.criteria.Timeout;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.equivalent.Equivalent;
import edu.ntut.selab.event.AndroidEvent;


public class DFSAlgorithm extends CrawlingAlgorithm {
    private AndroidEventFactory androidEventFactory = null;
    protected ArrayList<String> logList;
    protected boolean allStatesAreFinished;
    protected GUIState rootState;
    protected GUIState currentState;
    protected GUIState targetState;
    protected String xmlReaderTimestamp;
    protected Timeout timeout;

    protected String inputData = null; //�ΨӴ��ըϥΪ��Ѽ�
    protected ArrayList<GUIState> testStateList = new ArrayList<GUIState>(); //�ΨӴ��ըϥΪ��Ѽ�
    protected int testCount = 0;

    protected int stateIndex = 0;
    protected int totalDistanceEquivalentStateCount = 0;
    protected int totalListGridEquivalentStateCount = 0;

    public DFSAlgorithm() throws FileNotFoundException {
        super();
        androidEventFactory = new AndroidEventFactory();

        rootState = null;
        currentState = null;
        targetState = null;

        allStatesAreFinished = false;
        timeout = new Timeout();

        xmlReaderTimestamp = xmlReader.getTimeStampClone();
        logList = new ArrayList<String>();
    }

    @Override
    public void execute() throws InterruptedException, ExecuteCommandErrorException, DocumentException, IOException, ClickTypeErrorException, EventFromStateErrorException, NullPackageNameException, MultipleListOrGridException {
        // TODO Auto-generated method stub
        long currentExecuteTime = 0, startTime = 0, endTime = 0;
        startTime = System.currentTimeMillis();
        System.out.println("<<Crawling App>>");
        List<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        System.out.println("Start");

        this.setUp();

        //restartApp();
        currentState = getGUIState();
        rootState = currentState;
        while (!allStatesAreFinished && !timeout.check(currentExecuteTime)) {
            //eventSequence = getShortestUnfiredEventSequence(currentState);
            stateGraph.setLoggerStore(loggerStore);
            eventSequence = stateGraph.getShortestUnfiredPath(currentState);
            loggerStore = stateGraph.getLoggerStore();
            System.out.println("eventSequence.isEmpty : " + eventSequence.isEmpty());
            if (!eventSequence.isEmpty()) {
                System.out.println("fire event");
                for (AndroidEvent e : eventSequence) {
                    // assert(e.fromState == null || e.fromState == currentState)
                    if (e.getFromState() != null && e.getFromState() != currentState) {
                        throw new EventFromStateErrorException("e.getFromState() = " + e.getFromState());
                        //super.loggerStore.addErrorMessage("Event " + e.getReportLabel() + " don't have fromState");
                    }

                    e.setFromState(currentState);
                    executeEvent(e); // fire e
                    currentState = getGUIState(); // get gui state after fire event
                    System.out.println("GetGUIState");
                    // assert(!e.isVisited || e.isVisited && e.toState == currentState)
                    if (!e.isVisited()) {
                        System.out.println("GetIsVisited");
                        e.setToState(currentState);
                        e.setVisited(true);
                    } else if (e.isVisited() && !equivalentRule.checkTwoStateIsEquivalent(e.getToState(), currentState)) { // e.getToState() != currentState

                        //System.out.println("e.getToState id = " + e.getToState().getId());
                        //System.out.println("currentState id = " + currentState.getId());
                        //throw new AssertionError();
                        //super.loggerStore.addErrorMessage("FromState'id = " + e.getFromState().getId() + " Event " + e.getReportLabel() + " has two possible toStates: " + e.getToState().getId() + " , " + currentState.getId());
                        System.out.println("equivalentRule.checkTwoStateIsEquivalent");
                        e.setNondeterministic(true);
                        eventSequence.get(eventSequence.size() - 1).increaseAttemptCount();

                        AndroidEvent duplicatedEvent = androidEventFactory.createAndroidEvent(e.getName(), e.getEventData());
                        duplicatedEvent.setVisited(true);
                        duplicatedEvent.setToState(currentState);
                        duplicatedEvent.setNondeterministic(true);
                        if (!isExistEventInEventList(duplicatedEvent, e.getFromState().getEvents())) {
                            duplicatedEvent.setFromState(e.getFromState());
                            e.getFromState().addEvent(duplicatedEvent);
                        }
                        this.setEventOrder(duplicatedEvent, e.getFromState().getEvents());
                        break;
                    }
                    this.setEventOrder(e, e.getFromState().getEvents());
                    if (!visitedStateStack.isEmpty() && currentState != visitedStateStack.peek()) {
                        restartAppToTargetState();
                    }
                }
            } else {
                if (equivalentRule.checkTwoStateIsEquivalent(currentState, rootState))
                    allStatesAreFinished = true;
                else {
                    restartAppToTargetState();
                    // assert(currentState == rootState)
                    if (currentState != rootState) {
                        super.loggerStore.addErrorMessage("currentState_" + currentState.getId() + "is not rootState");
                        //throw new AssertionError();
                    }
                }
            }
            endTime = System.currentTimeMillis();
            currentExecuteTime += endTime - startTime;
            startTime = endTime;
        }
        super.device.stopApp(PACKAGE_NAME);
        System.out.println("result state list size = " + stateList.size());
        System.out.println("restart count = " + restartCount);
        System.out.println("activity list size = " + activityList.size());

        totalDistanceEquivalentStateCount = equivalentRule.getTotalDistanceEquivalentStateCount();
        totalListGridEquivalentStateCount = equivalentRule.getTotalListGridEquivalentStateCount();
        stateGraph.setTotalExecutedEventCount(totalFireEventCount);
        stateGraph.setTotalDistanceEquivalentStateCount(totalDistanceEquivalentStateCount);
        stateGraph.setTotalListGridEquivalentStateCount(totalListGridEquivalentStateCount);
        /*
        crawlerResult.setRestartCount(restartCount);
        */
    }

    protected void restartAppToTargetState() throws IOException, InterruptedException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException {
        ArrayList<AndroidEvent> toEventSequence = new ArrayList<AndroidEvent>();
        restartApp();
        //System.out.println("restartAppToTargetState");
        currentState = getGUIState();
        for (; ; ) { // from root to target state
            if (!visitedStateStack.isEmpty()) {
                GUIState s = visitedStateStack.peek();
                if (isStateEventListAllVisited(s.getEvents()) || s.getCrossAppDepth() >= Config.CROSS_APP_EVENT_THRESHHOLD) {
                    //System.out.println("pop up");
                    visitedStateStack.pop();
                    continue;
                }
                targetState = visitedStateStack.peek();
                toEventSequence = getEventSequenceFromRootToTargetState(targetState);
                for (AndroidEvent e : toEventSequence) {
                    executeEvent(e);
                }
                currentState = getGUIState();
                if (currentState != targetState) {
                    System.out.println("currentState != targetState");
                    //throw new AssertionError();
                }
                break;
            } else {
                break;
            }

        }
    }


    protected ArrayList<AndroidEvent> getEventSequenceFromRootToTargetState(GUIState targetState) {
        Queue<AndroidEvent> eventQueue = new LinkedList();
        ArrayList<GUIState> visitedStateList = new ArrayList<GUIState>();
        ArrayList<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        visitedStateList.add(currentState);
        for (AndroidEvent e : currentState.getEvents()) {
            if (!e.isNonDeterministic() && e.isVisited() && e.getFromState() != e.getToState()) { // �Dnondeterministic event
                eventQueue.add(e);
                e.setTempPreviousEvent(null);
            }
        }
        while (!eventQueue.isEmpty()) {
            AndroidEvent e = (AndroidEvent) eventQueue.poll();
            if (e.getToState() == targetState) {
                eventSequence.add(e);
                //System.out.println("e.getToState() == targetState");
                while (e.getTempPreviousEvent() != null) {
                    eventSequence.add(e.getTempPreviousEvent());
                    e = e.getTempPreviousEvent();
                    //System.out.println("e reportlabel : " + e.getReportLabel());
                }
                eventSequence = reverseElementsInEventSequence(eventSequence);
                break;
            } else if (e.getToState() != null) {
                for (AndroidEvent ev : e.getToState().getEvents()) {
                    //System.out.println("e.getToState() != null");
                    if (!ev.isNonDeterministic() && ev.isVisited() &&
                            !visitedStateList.contains(ev.getToState()) &&
                            !isToStateExistInPreevent(e, ev.getToState())) {
                        //System.out.println("e.getToState() if �P�_");
                        visitedStateList.add(ev.getToState());
                        eventQueue.add(ev);
                        ev.setTempPreviousEvent(e);
                    }
                }
            }
        }
        return eventSequence;
    }

    protected boolean isToStateExistInPreevent(AndroidEvent e, GUIState s) {
        while (e != null) {
            if (e.getFromState() == s)
                return true;
            e = e.getTempPreviousEvent();
        }
        return false;
    }

    protected ArrayList<AndroidEvent> getShortestUnfiredEventSequence(GUIState currentState) throws NullPackageNameException {
        //Queue<AndroidEvent> eventQueue = new LinkedList();
        ArrayList<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        if (!currentState.getPackageName().equals(Config.PACKAGE_NAME))
            computeCrossAppDepth(currentState);
        if (currentState.getCrossAppDepth() < Config.CROSS_APP_EVENT_THRESHHOLD) {
            for (AndroidEvent e : currentState.getEvents()) {
                if (!e.isVisited()) {
                    eventSequence.add(e);
                    break;
                }
            }
        }

        return eventSequence;
    }

    protected void computeCrossAppDepth(GUIState state) {
        for (GUIState s : stateList) {
            if (s != state) {
                for (AndroidEvent e : s.getEvents()) {
                    if (e.getToState() == state) {
                        if (e.getFromState().getCrossAppDepth() + 1 < state.getCrossAppDepth())
                            state.setCrossAppDepth(e.getFromState().getCrossAppDepth() + 1);
                    }
                }
            }
        }
    }

    protected void recursive(GUIState state, int path, ArrayList<Integer> pathList) throws NullPackageNameException {
        for (AndroidEvent e : state.getEvents()) {
            path++;
            if (e.getFromState().getPackageName().equals(Config.PACKAGE_NAME))
                pathList.add(path);
            else
                recursive(e.getFromState(), path, pathList);
        }
    }

//    protected void startApp() {
//        // currentIndex = 0;
//        long waitingTime = 0;
//        try {
//            waitingTime = TimeHelper.getWaitingTime("startAppSleepTimeSecond");
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        String packageName = XMLReader.getConfigurationValue("packageName"),
//                activityName = XMLReader.getConfigurationValue("launchableActivity"),
//                adb = XMLReader.getConfigurationValue("adb");
//        String[] startCmd = {adb, "shell", "am", "start", "-n",
//                packageName + "/" + activityName};
//        try {
//            CommandHelper.executeCommand(startCmd);
//            TimeHelper.sleep(waitingTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void stopApp() {
//        long waitingTime = 0;
//        try {
//            //�ǤJString��TimeHelper.getWaitingTime(String)�A�îھڶǶi�Ӫ��r��P�_�n��configuration.xml�ɮת����ӰѼ�
//            waitingTime = TimeHelper.getWaitingTime("closeAppSleepTimeSecond");
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        String packageName = XMLReader.getConfigurationValue("packageName"),
//                adb = XMLReader.getConfigurationValue("adb");
//        String[] stopCmd = {adb, "shell", "am", "force-stop", packageName};//����app
//        try {
//            CommandHelper.executeCommand(stopCmd);
//            TimeHelper.sleep(waitingTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    protected void clearAppData() {
//        String adb = XMLReader.getConfigurationValue("adb"),
//                packageName = XMLReader.getConfigurationValue("packageName");
//        String[] command = {adb, "shell", "pm", "clear", packageName};
//        try {
//            CommandHelper.executeCommand(command);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void pressHome() {
//        String[] command = {XMLReader.getConfigurationValue("adb"), "shell", "input", "keyevent", "KEYCODE_HOME"};
//        try {
//            CommandHelper.executeCommand(command);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    protected ArrayList<AndroidEvent> reverseElementsInEventSequence(ArrayList<AndroidEvent> eventSequence) {
        ArrayList<AndroidEvent> reversedEventSequence = new ArrayList<AndroidEvent>();
        for (int i = 0; i < eventSequence.size(); i++) {
            reversedEventSequence.add(0, eventSequence.get(i));
        }

        return reversedEventSequence;
    }

	/*
    protected String getPackageName(GUIState state) {
		Document content = state.contentClone();
		Element element = content.getRootElement();
		ArrayList<List<?>> siblingElements = new ArrayList<List<?>>();
		element = XMLReader.getNextElementAndSiblings(element, siblingElements).element;
		return element.attribute(NodeAttribute.Package).getText();
	}
	*/

    /*
     * �T�{�O�_�w�g�s�b�P�ˤ@��event�beventList�̭��Areturn true�N��s�b
     */
    protected boolean isExistEventInEventList(AndroidEvent event, List<AndroidEvent> eventList) {
        for (AndroidEvent e : eventList) {
            if (e.isNonDeterministic()) {
                if (event.getName().equals(e.getName())) {
                    if (event.getToState() == e.getToState())
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setStateEquivalent(Equivalent equivalent) {
        // TODO Auto-generated method stub
        this.equivalentRule = equivalent;
    }

    @Override
    public String getTimeString() {
        // TODO Auto-generated method stub
        return this.xmlReaderTimestamp;
    }

    protected boolean isStateEventListAllVisited(List<AndroidEvent> eventList) {
        for (AndroidEvent e : eventList) {
            if (!e.isVisited())
                return false;
        }
        return true;
    }

}
