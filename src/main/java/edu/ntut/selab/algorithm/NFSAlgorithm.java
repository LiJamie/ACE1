package edu.ntut.selab.algorithm;

import edu.ntut.selab.CommandHelper;
import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.criteria.Timeout;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.equivalent.Equivalent;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.event.AndroidEventFactory;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.EventFromStateErrorException;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NFSAlgorithm extends CrawlingAlgorithm {
    private AndroidEventFactory androidEventFactory;
    protected ArrayList<String> logList;
    protected boolean allStatesAreFinished;
    protected GUIState rootState;
    protected GUIState currentState;
    protected String xmlReaderTimestamp;
    protected Timeout timeout;

    protected String inputData = null; //�ΨӴ��ըϥΪ��Ѽ�
    protected ArrayList<GUIState> testStateList = new ArrayList<GUIState>(); //�ΨӴ��ըϥΪ��Ѽ�
    protected int testCount = 0;
    protected int stateIndex = 0;
    protected int totalDistanceEquivalentStateCount = 0;
    protected int totalListGridEquivalentStateCount = 0;

    public NFSAlgorithm() throws FileNotFoundException{
        super();
        androidEventFactory = new AndroidEventFactory();

        rootState = null;
        currentState = null;

        allStatesAreFinished = false;
        timeout = new Timeout();

        xmlReaderTimestamp = xmlReader.getTimeStampClone();
        logList = new ArrayList<String>();
    }

    @Override
    public void execute() throws IOException, InterruptedException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, NullPackageNameException, MultipleListOrGridException {
        // TODO Auto-generated method stub
        long currentExecuteTime = 0, startTime = 0, endTime = 0;
        startTime = System.currentTimeMillis();

        System.out.println("<<Crawling App>>");
        List<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        System.out.println("Start");

        super.setUp();

        //restartApp();

        currentState = getGUIState();
        rootState = currentState;
        while (!allStatesAreFinished && !timeout.check(currentExecuteTime)) {
            stateGraph.setLoggerStore(loggerStore);
            eventSequence = stateGraph.getShortestUnfiredPath(currentState);
            loggerStore = stateGraph.getLoggerStore();
            //eventSequence = getShortestUnfiredEventSequence(currentState);
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
                    currentState = getGUIState();
                    /*
					if(currentState.isEquivalentState()) {
						e.setToState(currentState);
						e.setIsVisited(true);
						restartApp();
						currentState = getGUIState();
						break;
					}
					*/
                    // assert(!e.isVisited || e.isVisited && e.toState == currentState)
                    if (!e.isVisited()) {
                        e.setToState(currentState);
                        e.setVisited(true);
                    } else if (e.isVisited() && !equivalentRule.checkTwoStateIsEquivalent(e.getToState(), currentState)) { // e.getToState() != currentState

                        //System.out.println("e.getToState id = " + e.getToState().getId());
                        //System.out.println("currentState id = " + currentState.getId());
                        //throw new AssertionError();
                        //super.loggerStore.addErrorMessage("FromState'id = " + e.getFromState().getId() + " Event " + e.getReportLabel() + " has two possible toStates: " + e.getToState().getId() + " , " + currentState.getId());
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
                        this.setEventOrder(duplicatedEvent,e.getFromState().getEvents());
                        break;
                    }
                    this.setEventOrder(e,e.getFromState().getEvents());
                }
            } else {
                if (equivalentRule.checkTwoStateIsEquivalent(currentState, rootState))
                    allStatesAreFinished = true;
                else {
                    restartApp();
                    currentState = getGUIState();
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
        stateGraph.setLogs(logList);
        stateGraph.setTotalExecutedEventCount(totalFireEventCount);
        stateGraph.setTotalDistanceEquivalentStateCount(totalDistanceEquivalentStateCount);
        stateGraph.setTotalListGridEquivalentStateCount(totalListGridEquivalentStateCount);
        /*
        crawlerResult.setRestartCount(restartCount);
        */
    }

    /*protected ArrayList<AndroidEvent> getShortestUnfiredEventSequence(GUIState currentState) {
        Queue<AndroidEvent> eventQueue = new LinkedList();
        ArrayList<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();

        for (GUIState s : stateList) {
            //s.setCrossAppDepth(0); �p��cross app depth ����bcomputeCrossAppDepth()�̭��p��
            s.setVisited(false);
        }
        currentState.setVisited(true);
        for (AndroidEvent e : currentState.getEvents()) {

            if (Config.BLOCK_NONDETERMINISTIC_EVENT) {
                if (!e.isNonDeterministic()) { // ����nondeterministic event
                    eventQueue.add(e);
                    e.setTempPreviousEvent(null); // e��preEvent�O�_��ر��p���n��l�٬O�ŦXnondeterministic��event����l?
                }
            } else {
                eventQueue.add(e);
                e.setTempPreviousEvent(null);
            }
            //e.setPreEvent(null); // e��preEvent�O�_��ر��p���n��l�٬O�ŦXnondeterministic��event����l?
        }
        if (!currentState.getPackageName().equals(Config.PACKAGE_NAME))
            computeCrossAppDepth(currentState);

        while (!eventQueue.isEmpty() && currentState.getCrossAppDepth() < Config.CROSS_APP_EVENT_THRESHHOLD) {
            AndroidEvent e = (AndroidEvent) eventQueue.poll();
            if (!e.isVisited()) {  // event is not visited
                if (!unreachedEvent.isEventAttemptCountAtLimit(e.getAttemptCount())) {
                    eventSequence.add(e);
                    while (e.getTempPreviousEvent() != null) {
                        eventSequence.add(e.getTempPreviousEvent());
                        e = e.getTempPreviousEvent();
                    }
                    //reverse the elements in the eventSequence

                    // eventSequence�̭��|�]�t�@�s�ꪺevent�A�u�|���@��event�O�����X�A�o�ӥ����X��event�u�|�b�̫�@��
                    // �����X���A��event�e�����|�O�w�g���X�L��event�A���|��1�ӥH�W�����X��event
                    // eventSequence��size�i�H��1(�u���@��event�åB�������X���A)

                    eventSequence = reverseElementsInEventSequence(eventSequence);
                    break;
                } else {
                    super.loggerStore.addErrorMessage(e.getReportLabel() + " cannot be visited within " + unreachedEvent.getEventAttemptCountLimit() + " times");
                    continue;
                }
            } else {  // event has visited
                if (e.getToState() == null) {
                    throw new AssertionError();
                }
                if (!e.getToState().isVisited()) {
                    //if(!e.getToState().getPackageName().equals(XMLReader.getConfigurationValue(ConfigurationType.PackageName)))
                    //	e.getToState().setCrossAppDepth(e.getFromState().getCrossAppDepth()+1);
                    if (e.getToState().getCrossAppDepth() < Config.CROSS_APP_EVENT_THRESHHOLD) {
                        e.getToState().setVisited(true);
                        for (AndroidEvent ev : e.getToState().getEvents()) {

                            if (Config.BLOCK_NONDETERMINISTIC_EVENT) {
                                if (!ev.isNonDeterministic()) { // ����nondeterministic event
                                    eventQueue.add(ev);
                                    ev.setTempPreviousEvent(e);
                                }
                            } else {
                                eventQueue.add(ev);
                                ev.setTempPreviousEvent(e);
                            }
                            //ev.setPreEvent(e); // set preEvent�O�_�n���}�B�z�٬O���B�z?
                        }
                    }
                }
            }
        }


        return eventSequence;
    }*/

    protected void computeCrossAppDepth(GUIState state) {
        for (GUIState s : stateGraph.getAllStates()) {
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

    // element1 = currentState XML file, element2 = one of stateList's state XML file
    protected boolean haveFewerElement(Element element1, Element element2) {
        if (element1.elements().size() < element2.elements().size())
            return true;
        for (int i = 0; i < Math.min(element1.elements().size(), element2.elements().size()); i++) {
            if (haveFewerElement((Element) element1.elements().get(i), (Element) element2.elements().get(i)))
                return true;
        }
        return false;
    }

    protected boolean isExistActivity(String activity) {
        for (String a : activityList) {
            if (a.equals(activity))
                return true;
        }
        return false;
    }

    protected ArrayList<AndroidEvent> reverseElementsInEventSequence(ArrayList<AndroidEvent> eventSequence) {
        ArrayList<AndroidEvent> reversedEventSequence = new ArrayList<AndroidEvent>();
        for (int i = 0; i < eventSequence.size(); i++) {
            reversedEventSequence.add(0, eventSequence.get(i));
        }

        return reversedEventSequence;
    }

	protected void cleanBackGroundApplication()
	{
		String[] command = {"\"" +
				Config.ADB_PATH +
				"\"", "shell", "am", "kill-all"};
		try {
			CommandHelper.executeCommand(command);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

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

}
