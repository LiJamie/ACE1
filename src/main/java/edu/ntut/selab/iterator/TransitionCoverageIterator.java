package edu.ntut.selab.iterator;

import edu.ntut.selab.StateGraph;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.RestartEvent;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import sun.jvm.hotspot.runtime.amd64.AMD64CurrentFrameGuess;

import java.util.*;

public class TransitionCoverageIterator implements TestCaseIterator {
    private StateGraph stateGraph;
    private List<AndroidEvent> eventSequence = null;
    private Stack<GUIState> visitedStack;
    private GUIState guiState;
    private AndroidCrawler androidCrawler;
    private AndroidEvent testCaseUnfiredEvent = null , nondeterministicEvent = null;
    private List<List<AndroidEvent>> events;

    public TransitionCoverageIterator(StateGraph stateGraph ,AndroidCrawler androidCrawler){
        this.androidCrawler = androidCrawler;
        this.stateGraph = stateGraph;
        this.androidCrawler.setStateGraph(this.stateGraph);
        this.androidCrawler.setRootState(this.stateGraph.getAllStates().get(1));
        this.visitedStack = new Stack<GUIState>();
        this.eventSequence = new ArrayList<AndroidEvent>();
        this.events = new ArrayList<>();
    }

    public void first(){
        this.guiState = this.androidCrawler.getRootState();
        this.guiState.setTag("visited");
        this.testCaseUnfiredEvent = this.getTestCaseUnfiredEvent(this.guiState);
        this.testCaseUnfiredEvent.setTag("visited");
        this.eventSequence.add(this.testCaseUnfiredEvent);
        this.visitedStack.push(this.guiState);
        this.guiState = this.testCaseUnfiredEvent.getToState();
        this.guiState.setTag("visited");
    }

    public boolean isDone(){
        return allStateEventVisited(this.stateGraph) || (getTestCaseUnfiredEvent(this.androidCrawler.getRootState()) == null && events.isEmpty());
    }


    //this.guiState.isExactlyEquivalentTo(this.androidCrawler.getRootState()) && (getTestCaseUnfiredEvent(this.guiState) == null) &&
    public void next() throws NullPackageNameException, MultipleListOrGridException {
        if (checkThisPathFinished())
            this.eventSequence = this.backtrackToPreviousState();
        else if (!this.visitedStack.contains(this.guiState)){
            this.visitedStack.push(this.guiState);
        }
        if (this.eventSequence == null && getTestCaseUnfiredEvent(this.androidCrawler.getRootState()) == null
                && !events.isEmpty()) {
            //else if(androidEvents.get(androidEvents.size()-1).getTag().equals("visited"))
            this.guiState = this.androidCrawler.getRootState();
            List<AndroidEvent> androidEvents;
            while (!events.isEmpty()){
                androidEvents = events.get(0);
                if(androidEvents.size() == 0
                        || androidEvents.isEmpty()
                        || androidEvents == null)
                    events.remove(0);
                else if (getTestCaseUnfiredEvent(androidEvents.get(androidEvents.size()-1).getToState()) == null){
                    androidEvents.get(androidEvents.size()-1).setTag("visited");
                    events.remove(0);
                }
                else
                    break;
            }
            if(!events.isEmpty()){
                androidEvents = new ArrayList<>();
                for(AndroidEvent event : events.get(0))
                    androidEvents.add(event);
                this.eventSequence = androidEvents;
                this.visitedStack.clear();
                this.visitedStack.push(this.androidCrawler.getRootState());
                for (AndroidEvent event : this.eventSequence) {
                    if (!this.visitedStack.contains(event.getToState()))
                        this.visitedStack.push(event.getToState());
                    this.guiState = event.getToState();
                    this.guiState.setTag("visited");
                    event.setTag("visited");
                }
            }
        }
        this.testCaseUnfiredEvent = this.getTestCaseUnfiredEvent(this.guiState);
        if (testCaseUnfiredEvent != null){
            nonDeterministicPath();
            if(!testCaseUnfiredEvent.getTag().contains("nondeterministic")){
                this.testCaseUnfiredEvent.setTag("visited");
                this.guiState = this.testCaseUnfiredEvent.getToState();
                this.guiState.setTag("visited");
                this.eventSequence.add(this.testCaseUnfiredEvent);
            }
        }
    }

    public List<AndroidEvent> getEventSequence() {
        return this.eventSequence;
    }

    public boolean checkThisPathFinished(){
        //this.testCaseUnfiredEvent.getToState().isEquivalentState()
        //this.visitedStack.contains(this.guiState)
        if (this.testCaseUnfiredEvent == null || getTestCaseUnfiredEvent(this.testCaseUnfiredEvent.getToState()) == null
                || !this.testCaseUnfiredEvent.isToOriginalState() || testCaseUnfiredEvent.getTag().contains("nondeterministic"))
            return true;
        return false;
    }

    private List<AndroidEvent> getEventSequenceFromRootStateToTargetState(GUIState targetState){
        GUIState state = this.androidCrawler.getRootState();
        List<AndroidEvent> events = new ArrayList<>();
        if(!targetState.isExactlyEquivalentTo(this.androidCrawler.getRootState())){
            for(AndroidEvent event : this.eventSequence){
                if(!event.getToState().isExactlyEquivalentTo(state) || event.getReportLabel().contains("EditText"))
                    events.add(event);
                state = event.getToState();
                if(event.getToState().isExactlyEquivalentTo(targetState))
                    break;
            }
        }
        return events;
    }

    private List<AndroidEvent> backtrackToPreviousState(){
        this.guiState = this.androidCrawler.getRootState();
        this.popVisitedStateUntilLastUnvisitedEventExist();
        List<AndroidEvent> toTargetStateEventSequence = null;
        if (!this.visitedStack.empty()) {
            toTargetStateEventSequence = this.getEventSequenceFromRootStateToTargetState(this.visitedStack.peek());
            for(AndroidEvent event : toTargetStateEventSequence){
                this.guiState = event.getToState();
            }
        }
        return toTargetStateEventSequence;
    }

    private void popVisitedStateUntilLastUnvisitedEventExist() {
        while (!this.visitedStack.isEmpty()) {
            GUIState topState = this.visitedStack.peek();
            if (getTestCaseUnfiredEvent(topState) == null) {
                this.visitedStack.pop();
                continue;
            }
            break;
        }
    }

    private AndroidEvent getTestCaseUnfiredEvent(GUIState currentState) {
        List<AndroidEvent> events = currentState.getEvents();
        for (AndroidEvent event : events) {
            if (!event.getTag().contains("visited") && event.isVisited())
                return event;
        }
        return null;
    }

    private void nonDeterministicPath() throws NullPackageNameException, MultipleListOrGridException {
        String[] orderString;
        GUIState state = this.androidCrawler.getRootState();
        List<AndroidEvent> eventList = new ArrayList<>();
        if(this.testCaseUnfiredEvent.isNonDeterministic() && !this.testCaseUnfiredEvent.getToState().isExactlyEquivalentTo(this.guiState)){
            for(AndroidEvent event : this.guiState.getEvents()){
                if(event.equals(this.testCaseUnfiredEvent))
                    break;
                else if(event.getReportLabel().equals(this.testCaseUnfiredEvent.getReportLabel()) && !this.testCaseUnfiredEvent.getTag().contains("nondeterministic")){
                    this.testCaseUnfiredEvent.setTag("nondeterministic_visited");
                    this.testCaseUnfiredEvent.getToState().setTag("nondeterministic_visited");
                    orderString = this.testCaseUnfiredEvent.getOrder().split(", ");
                    for (int i = 1; i <= Integer.parseInt(orderString[0]);) {
                        AndroidEvent androidEvent = findOrderEventToNondeterministicState(i, state);
                        if (androidEvent != null) {
                            if (!nondeterministicLoopToSelf(androidEvent, state))
                                eventList.add(androidEvent);
                            state = androidEvent.getToState();
                            i++;
                        } else {
                            if (Config.GENERATE_NONDETERMINISTIC_PATH) {
                                state = this.androidCrawler.getRootState();
                                eventList.clear();
                            } else {
                                RestartEvent restartEvent = new RestartEvent();
                                restartEvent.setFromState(state);
                                restartEvent.setToState(this.androidCrawler.getRootState());
                                eventList.add(restartEvent);
                                state = this.androidCrawler.getRootState();
                            }
                        }
                    }
                    this.events.add(eventList);
                }
            }
        } else if (this.testCaseUnfiredEvent.isNonDeterministic()){
            this.testCaseUnfiredEvent.setTag("nondeterministic_visited");
        }
    }

    private AndroidEvent findOrderEventToNondeterministicState(int index, GUIState state) {
        for(AndroidEvent event : state.getEvents()){
            String[] orderString = event.getOrder().split(", ");
            if(checkStringArrayContainOrderString(orderString, Integer.toString(index)))
                return event;
        }
        return null;
    }

    private boolean allStateEventVisited(StateGraph stateGraph){
        List<GUIState> guiStates = stateGraph.getAllStates();
        for (GUIState guiState : guiStates) {
            for(AndroidEvent event : guiState.getEvents()){
                if (!event.getTag().contains("visited") && event.isVisited())
                    return false;
            }
        }
        return true;
    }

    private Boolean checkStringArrayContainOrderString(String[] strings, String index){
        return Arrays.asList(strings).contains(index);
    }

    private Boolean nondeterministicLoopToSelf(AndroidEvent event, GUIState state) throws MultipleListOrGridException {
        if(event.isNonDeterministic() && state.isExactlyEquivalentTo(event.getToState()))
            return true;
        return false;
    }
}