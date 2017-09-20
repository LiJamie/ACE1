package edu.ntut.selab.iterator;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.data.GUIState;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;

import edu.ntut.selab.exception.*;
import edu.ntut.selab.util.Printer;
import org.dom4j.DocumentException;

import java.io.IOException;


public class DFSIterator extends CrawlingIterator {
    private StateGraph stateGraph;
    private List<AndroidEvent> eventSequence = null;
    private AndroidCrawler crawler;
    private Stack<GUIState> visitedStack;

    public DFSIterator(StateGraph stateGraph, AndroidCrawler crawler) {
        this.stateGraph = stateGraph;
        this.crawler = crawler;
        this.visitedStack = new Stack<GUIState>();
    }

    @Override
    public void first() throws IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException, EventFromStateErrorException, CannotReachTargetStateException {
        this.crawler.startExplore();
        this.eventSequence = new ArrayList<AndroidEvent>();
        GUIState state = this.crawler.getCurrentState();
        this.visitedStack.push(state);
        this.eventSequence.add(this.getUnfiredEvent(state));
    }

    @Override
    public boolean isDone() {
        return this.crawler.getCurrentState().isExactlyEquivalentTo(this.crawler.getRootState()) && allEventFired(this.crawler.getCurrentState());
    }

    @Override
    public void next() throws IndexOutOfBoundsException, IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        GUIState state = this.crawler.getCurrentState();
        if (allEventFired(state) || (this.visitedStack.contains(state) && state != this.visitedStack.peek()))
            this.backtrackToPreviousState();
        else if (!this.visitedStack.contains(state))
            this.visitedStack.push(state);
        this.eventSequence = new ArrayList<AndroidEvent>();
        AndroidEvent unfiredEvent = this.getUnfiredEvent(this.crawler.getCurrentState());
        // unfired event will be null if all event are fire and current state is root state, which also means is done = true
        if (unfiredEvent != null)
            this.eventSequence.add(unfiredEvent);
    }

    @Override
    public List<AndroidEvent> getEventSequence() {
        return this.eventSequence;
    }

    private void backtrackToPreviousState() throws IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        // restart
        this.crawler.restartApp();
        // find previous node which has unfired event
        this.popVisitedStateUntilLastUnvisitedEventExist();
        if (!this.visitedStack.empty()) {
            List<AndroidEvent> toTargetStateEventSequence = this.getEventSequenceFromRootStateToTargetState(this.visitedStack.peek());
            if (toTargetStateEventSequence.isEmpty() && this.crawler.getCurrentState() != this.visitedStack.peek()) { // not route to target state (route contains overAttempCountThreshold event)
                this.visitedStack.pop();
                this.backtrackToPreviousState();
            }
            try {
                this.crawler.executeEvents(toTargetStateEventSequence);
            } catch (CannotReachTargetStateException e) {
                e.getNondeterministicEvent().increaseAttemptCount();
                new Printer().dfsBacktrackNondeterministicRetry();
                this.backtrackToPreviousState();
            }
        }

    }

    private List<AndroidEvent> getEventSequenceFromRootStateToTargetState(GUIState targetState) {
        Queue<AndroidEvent> eventQueue = new LinkedList();
        ArrayList<GUIState> visitedStateList = new ArrayList<GUIState>();
        List<AndroidEvent> eventSequence = new ArrayList<AndroidEvent>();
        visitedStateList.add(this.crawler.getCurrentState());
        for (AndroidEvent e : this.crawler.getCurrentState().getEvents()) {
            if (e.isVisited() && e.getFromState() != e.getToState()) { // not nondeterministic event
                eventQueue.add(e);
                e.setTempPreviousEvent(null); // should both two situation need initial e.prePreEvent? or only when e is nonDeterministic
            }
        }
        while (!eventQueue.isEmpty()) {
            AndroidEvent e = (AndroidEvent) eventQueue.poll();
            if (!e.isOverAttemptCountThreshold()) {
                if (e.getToState() == targetState) {
                    eventSequence.add(e);
                    //System.out.println("e.getToState() == targetState");
                    while (e.getTempPreviousEvent() != null) {
                        eventSequence.add(e.getTempPreviousEvent());
                        e = e.getTempPreviousEvent();
                        //System.out.println("e reportlabel : " + e.getReportLabel());
                    }
                    eventSequence = stateGraph.reverseElementsInEventSequence(eventSequence);
                    break;
                } else if (e.getToState() != null) {
                    for (AndroidEvent ev : e.getToState().getEvents()) {
                        //System.out.println("e.getToState() != null");
                        if (ev.isVisited() &&
                                !visitedStateList.contains(ev.getToState()) &&
                                !isToStateExistInPreviousEvent(e, ev.getToState())) {
                            visitedStateList.add(ev.getToState());
                            eventQueue.add(ev);
                            ev.setTempPreviousEvent(e);// set previousEvent
                        }
                    }
                }
            }
        }
        return eventSequence;
    }

    private void popVisitedStateUntilLastUnvisitedEventExist() {
        while (!this.visitedStack.isEmpty()) {
            GUIState topState = this.visitedStack.peek();
            if (allEventFired(topState) || topState.isOverCrossAppEventThreshold()) {
                this.visitedStack.pop();
                continue;
            }
            break;
        }
    }

    private boolean isToStateExistInPreviousEvent(AndroidEvent e, GUIState s) {
        while (e != null) {
            if (e.getFromState() == s)
                return true;
            e = e.getTempPreviousEvent();
        }
        return false;
    }

    private AndroidEvent getUnfiredEvent(GUIState currentState) throws IOException, MultipleListOrGridException, EventFromStateErrorException, NullPackageNameException, InterruptedException, ClickTypeErrorException, EquivalentStateException, ExecuteCommandErrorException, CrawlerControllerInitialErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, DocumentException {
        AndroidEvent event = null;
        if (!currentState.isOverCrossAppEventThreshold()) {
            for (AndroidEvent e : currentState.getEvents()) {
                if (!e.isVisited() && !e.isOverAttemptCountThreshold()) {
                    return e;
                }
            }
        } else
            this.backtrackToPreviousState();
        return event;
    }

    private boolean allEventFired(GUIState state) {
        List<AndroidEvent> events = state.getEvents();
        for (AndroidEvent event : events) {
            if (!event.isVisited())
                return false;
        }
        return true;
    }
}