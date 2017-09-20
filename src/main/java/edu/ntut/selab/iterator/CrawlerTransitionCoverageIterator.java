package edu.ntut.selab.iterator;


import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class CrawlerTransitionCoverageIterator implements TestCaseIterator{
    private StateGraph stateGraph;
    private List<AndroidEvent> eventSequence = null;
    private Stack<GUIState> visitedStack;
    private GUIState guiState;
    private AndroidCrawler androidCrawler;
    private AndroidEvent unfiredEvent = null , nondeterministicEvent = null;
    private int index = 1;

    public CrawlerTransitionCoverageIterator(StateGraph stateGraph, AndroidCrawler androidCrawler){
        this.androidCrawler = androidCrawler;
        this.stateGraph = stateGraph;
        this.androidCrawler.setStateGraph(this.stateGraph);
        this.androidCrawler.setRootState(this.stateGraph.getAllStates().get(1));
        this.visitedStack = new Stack<GUIState>();
        this.eventSequence = new ArrayList<AndroidEvent>();
    }

    public void first(){
        this.guiState = this.androidCrawler.getRootState();
        this.guiState.setTag("visited");
        this.unfiredEvent = this.findCrawlerOrderEvent(this.index, this.guiState);
        this.unfiredEvent.setTag("visited");
        this.eventSequence.add(this.unfiredEvent);
        this.visitedStack.push(this.guiState);
        this.index++;
    }

    public boolean isDone(){
        return allStateEventVisited(this.stateGraph);
    }


    //this.guiState.isExactlyEquivalentTo(this.androidCrawler.getRootState()) && (getTestCaseUnfiredEvent(this.guiState) == null) &&
    public void next() throws NullPackageNameException, MultipleListOrGridException {
        if (this.unfiredEvent != null){
            this.guiState = this.unfiredEvent.getToState();
            this.guiState.setTag("visited");
        }
        if (checkThisPathFinished()){
            this.eventSequence = new ArrayList<AndroidEvent>();
            this.guiState = this.androidCrawler.getRootState();
        }
        else if (!this.visitedStack.contains(this.guiState))
            this.visitedStack.push(this.guiState);
        this.unfiredEvent = this.findCrawlerOrderEvent(this.index, this.guiState);
        if (unfiredEvent != null) {
            if(!nondeterministicLoopToSelf())
                this.eventSequence.add(this.unfiredEvent);
            this.index++;
            this.unfiredEvent.setTag("visited");
        }
    }

    public List<AndroidEvent> getEventSequence() {
        return this.eventSequence;
    }

    public boolean checkThisPathFinished(){
        if (this.unfiredEvent == null
                || findCrawlerOrderEvent(this.index, this.unfiredEvent.getToState()) == null
                || allStateEventVisited(this.stateGraph))
            return true;
        return false;
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

    private AndroidEvent findCrawlerOrderEvent(int index, GUIState state){
        String[] orderString;
        for(AndroidEvent event : state.getEvents()){
            orderString = event.getOrder().split(", ");
            if(checkStringArrayContainOrderString(orderString, Integer.toString(index)))
                return event;
        }
        return null;
    }

    private Boolean nondeterministicLoopToSelf() throws MultipleListOrGridException {
        if(this.unfiredEvent.isNonDeterministic() && this.guiState.isExactlyEquivalentTo(this.unfiredEvent.getToState()))
            return true;
        return false;
    }

    private Boolean checkStringArrayContainOrderString(String[] strings, String index){
        return Arrays.asList(strings).contains(index);
    }
}
