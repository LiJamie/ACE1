package edu.ntut.selab.iterator;

import edu.ntut.selab.StateGraph;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.RestartEvent;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;

import java.util.*;

public class StateCoverageIterator implements TestCaseIterator{
    private StateGraph stateGraph;
    private List<AndroidEvent> eventSequence = null;
    private Stack<GUIState> visitedStack;
    private GUIState guiState;
    private AndroidCrawler androidCrawler;
    private AndroidEvent unVisitedStateEvent;
    private List<List<AndroidEvent>> events;

    public StateCoverageIterator(StateGraph stateGraph, AndroidCrawler androidCrawler){
        this.stateGraph = stateGraph;
        this.androidCrawler = androidCrawler;
        this.androidCrawler.setStateGraph(this.stateGraph);
        this.androidCrawler.setRootState(this.stateGraph.getAllStates().get(1));
        this.visitedStack = new Stack<GUIState>();
        this.eventSequence = new ArrayList<AndroidEvent>();
        this.events = new ArrayList<>();
    }

    public void first(){
        this.guiState = this.androidCrawler.getRootState();
        this.unVisitedStateEvent = chooseIsNotVisitAndToStateIsNotMine(this.guiState);
        this.unVisitedStateEvent.setTag("visited");
        this.eventSequence.add(this.unVisitedStateEvent);
        this.visitedStack.push(this.guiState);
        this.guiState.setTag("visited");
        this.guiState = this.unVisitedStateEvent.getToState();
        this.guiState.setTag("visited");
    }

    public boolean isDone() throws NullPackageNameException {
        return (allStateVisited(this.stateGraph) || checkOnlyDesktopUnvisited());
    }

    public void next() throws NullPackageNameException, MultipleListOrGridException {
        if(checkThisPathFinished())
            this.eventSequence = this.backTrackToPreviousState();
        else if(!this.visitedStack.contains(this.guiState))
            this.visitedStack.push(this.guiState);
        if(this.eventSequence == null && chooseIsNotVisitAndToStateIsNotMine(this.androidCrawler.getRootState()) == null
                && !events.isEmpty()){
            this.guiState = this.androidCrawler.getRootState();
            List<AndroidEvent> androidEvents;
            while (!events.isEmpty()){
                androidEvents = events.get(0);
                if(androidEvents.size() == 0
                        || androidEvents.isEmpty()
                        || androidEvents == null
                        || androidEvents.get(androidEvents.size()-1).getToState().getTag().equals("visited"))
                    events.remove(0);
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
        this.unVisitedStateEvent = chooseIsNotVisitAndToStateIsNotMine(this.guiState);
        if(this.unVisitedStateEvent != null) {
            //this.nonDeterministicPath();
            if(!this.unVisitedStateEvent.getTag().contains("nondeterministic")){
                this.unVisitedStateEvent.setTag("visited");
                this.guiState = this.unVisitedStateEvent.getToState();
                this.guiState.setTag("visited");
                this.eventSequence.add(this.unVisitedStateEvent);
            }
        }
    }

    private AndroidEvent chooseIsNotVisitAndToStateIsNotMine(GUIState guiState){
        List<AndroidEvent> events = guiState.getEvents();
        for(AndroidEvent event : events){
            if(event.getToState() != null && event.getToState().getId() != guiState.getId()
                    && !event.getToState().getTag().equals("visited") && !event.getTag().contains("visited")){
                return event;
            }
        }
        return null;
    }

    public List<AndroidEvent> getEventSequence() {
        return this.eventSequence;
    }

    public boolean checkThisPathFinished() throws NullPackageNameException {
        if((this.visitedStack.contains(this.guiState) && this.guiState.isExactlyEquivalentTo(this.visitedStack.peek()) &&
                chooseIsNotVisitAndToStateIsNotMine(this.guiState) == null) || allStateVisited(this.stateGraph) || checkOnlyDesktopUnvisited())
            return true;
        return false;
    }

    private List<AndroidEvent> backTrackToPreviousState(){
        this.guiState = this.androidCrawler.getRootState();
        this.popVisitedStateUntilLastUnvisitedStateExist();
        List<AndroidEvent> toTargetStateEventSequence = null;
        if (!this.visitedStack.empty()) {
            toTargetStateEventSequence = this.getEventSequenceFromRootStateToTargetState(this.visitedStack.peek());
            for(AndroidEvent event : toTargetStateEventSequence){
                this.guiState = event.getToState();
            }
        }
        return toTargetStateEventSequence;
    }

    private List<AndroidEvent> getEventSequenceFromRootStateToTargetState(GUIState targetState) {
        GUIState state = this.androidCrawler.getRootState();
        List<AndroidEvent> events = new ArrayList<>();
        if(!targetState.isExactlyEquivalentTo(this.androidCrawler.getRootState())){
            for(AndroidEvent event : this.eventSequence){
                if(!event.getToState().isExactlyEquivalentTo(state) || event.getReportLabel().contains("EditText"))
                    events.add(event);
                state = event.getToState();
                if(event.getToState().isExactlyEquivalentTo(targetState) && event.getToState().getId() == targetState.getId())
                    break;
            }
        }
        return events;
    }

    private boolean allStateVisited(StateGraph stateGraph){
        List<GUIState> guiStates = stateGraph.getAllStates();
        for (GUIState guiState : guiStates) {
            if (!guiState.getTag().equals("visited"))
                return false;
        }
        return true;
    }

    private void popVisitedStateUntilLastUnvisitedStateExist(){
        while (!this.visitedStack.isEmpty()) {
            GUIState topState = this.visitedStack.peek();
            if (chooseIsNotVisitAndToStateIsNotMine(topState) == null) {
                this.visitedStack.pop();
                continue;
            }
            break;
        }
    }

    private boolean checkOnlyDesktopUnvisited() throws NullPackageNameException {
        GUIState state = androidCrawler.getRootState(), tempState = null;
        for(GUIState guiState : stateGraph.getAllStates()){
            if(!guiState.getTag().equals("visited"))
                tempState = guiState;
        }
        if(!androidCrawler.isDesktopState(tempState))
            return false;
        for(AndroidEvent event : state.getEvents()){
            //if(event.getToState() != null)
                if(androidCrawler.isDesktopState(event.getToState()))
                    return false;
        }
        return true;
    }

    private void nonDeterministicPath() throws NullPackageNameException, MultipleListOrGridException {
        String[] orderString;
        GUIState state = this.androidCrawler.getRootState();
        List<AndroidEvent> eventList = new ArrayList<>();
        if(this.unVisitedStateEvent.isNonDeterministic() && !this.unVisitedStateEvent.getToState().isExactlyEquivalentTo(this.guiState)
                && !this.unVisitedStateEvent.getToState().getTag().contains("visited")){
            for(AndroidEvent event : this.guiState.getEvents()){
                if(event.equals(this.unVisitedStateEvent))
                    break;
                else if(event.getReportLabel().equals(this.unVisitedStateEvent.getReportLabel()) && !this.unVisitedStateEvent.getTag().contains("nondeterministic")){
                    this.unVisitedStateEvent.setTag("nondeterministic_visited");
                    this.unVisitedStateEvent.getToState().setTag("nondeterministic_visited");
                    orderString = this.unVisitedStateEvent.getOrder().split(", ");
                    for (int i = 1; i <= Integer.parseInt(orderString[0]);) {
                        AndroidEvent androidEvent = findOrderEventToNondeterministicState(i, state);
                        if (androidEvent != null){
                            if(!nondeterministicLoopToSelf(androidEvent,state))
                                eventList.add(androidEvent);
                            state = androidEvent.getToState();
                            i++;
                        }
                        else {
                            if(Config.GENERATE_NONDETERMINISTIC_PATH){
                                state = this.androidCrawler.getRootState();
                                eventList.clear();
                            }
                            else {
                                RestartEvent restartEvent = new RestartEvent();
                                restartEvent.setFromState(state);
                                restartEvent.setToState(this.androidCrawler.getRootState());
                                eventList.add(restartEvent);
                                state = this.androidCrawler.getRootState();
                            }
                        }
                    }
                    this.events.add(eventList);
                    break;
                }
            }
        }
    }

    private AndroidEvent findOrderEventToNondeterministicState(int index, GUIState state) {
        String[] orderString;
        for(AndroidEvent event : state.getEvents()){
            orderString = event.getOrder().split(", ");
            if(checkStringArrayContainOrderString(orderString, Integer.toString(index)))
                return event;
        }
        return null;
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
