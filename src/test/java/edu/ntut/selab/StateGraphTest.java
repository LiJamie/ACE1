package edu.ntut.selab;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.data.Point;
import edu.ntut.selab.event.*;
import edu.ntut.selab.util.Config;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class StateGraphTest {
    Point point;
    String testStr, twoXMLSource, threeXMLSource, fourXMLSource;
    AndroidEvent checkEvent, scrollEvent, swipeEvent, editTextEvent, clickEvent, longClickEvent, backKeyEvent, menuKeyEvent;
    SAXReader reader;
    Document document;
    GUIState firstState, secondState, thirdState;

    @Before
    //firstState -> menuKeyEvent -> thirdState
    public void setup() throws DocumentException {
        reader = new SAXReader();
        point = new Point(0, 0);
        testStr = "test";
        EventData eventData = new EventData("[0,0][100,100]", this.testStr);
        checkEvent = new ClickEvent(eventData, ClickEvent.Type.Check);
        eventData.setSwipeDirection(SwipeEvent.Direction.RIGHT);
        scrollEvent = new SwipeEvent(eventData, SwipeEvent.Type.SCROLL);
        swipeEvent = new SwipeEvent(eventData, SwipeEvent.Type.SWIPE);
        this.initialEditTextEvent();
        this.initialClickEvent(point, testStr);
        longClickEvent = new LongClickEvent(eventData);
        backKeyEvent = new BackKeyEvent();
        menuKeyEvent = new MenuKeyEvent();
        twoXMLSource = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>" +
                "<hierarchy rotation=\"0\"><node index=\"0\" text=\"\" resource-id=\"\" " +
                "class=\"android.widget.FrameLayout\" package=\"com.example.oil.myapplication2\" " +
                "content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" " +
                "focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" " +
                "selected=\"false\" bounds=\"[0,0][1080,1920]\" /></hierarchy>";
        threeXMLSource = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation=\"0\"><node index=\"0\" " +
                "text=\"\" resource-id=\"\" class=\"android.widget.FrameLayout\" package=\"com.example.oil.myapplication4\" " +
                "content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" " +
                "focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" " +
                "bounds=\"[0,0][1080,1920]\" /></hierarchy>";
        fourXMLSource = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>" +
                "<hierarchy rotation=\"0\"><node index=\"0\" text=\"\" resource-id=\"\" " +
                "class=\"android.widget.FrameLayout\" package=\"com.google.android.wearable.app\" " +
                "content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" " +
                "enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" " +
                "long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,0][320,320]\" /></hierarchy>";
        document = DocumentHelper.parseText(twoXMLSource);
        firstState = new GUIState(document, new ArrayList<AndroidEvent>());
        document = DocumentHelper.parseText(threeXMLSource);
        secondState = new GUIState(document, new ArrayList<AndroidEvent>());
        document = DocumentHelper.parseText(fourXMLSource);
        thirdState = new GUIState(document, new ArrayList<AndroidEvent>());
    }

    private void initialClickEvent(Point point, String testStr) {
        EventData eventData = new EventData();
        eventData.setCenterPoint(point);
        eventData.setTempLabel(testStr);
        clickEvent = new ClickEvent(eventData, ClickEvent.Type.Click);
    }

    private void initialEditTextEvent() {
        EventData eventData = new EventData("[0,0][100,100]", "editText");
        eventData.setBackspaceCount(0);
        eventData.setValue("create editText");
        List<EventData> eventDatas = new ArrayList<>();
        eventDatas.add(eventData);
        this.editTextEvent = new EditTextEvent(eventDatas);
    }

    @Test
    public void testReverseElementsInEventSequence() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method reverseElementsInEventSequence = StateGraph.class.getDeclaredMethod("reverseElementsInEventSequence", List.class);
        reverseElementsInEventSequence.setAccessible(true);
        StateGraph stateGraph = new StateGraph();
        List<AndroidEvent> unReverseEvents = new ArrayList<>();
        unReverseEvents.add(menuKeyEvent);
        unReverseEvents.add(clickEvent);
        unReverseEvents.add(longClickEvent);
        unReverseEvents.add(scrollEvent);
        unReverseEvents.add(swipeEvent);
        unReverseEvents.add(backKeyEvent);
        unReverseEvents.add(checkEvent);
        unReverseEvents.add(editTextEvent);
        List<AndroidEvent> reverseEvents = new ArrayList<>();
        reverseEvents.add(editTextEvent);
        reverseEvents.add(checkEvent);
        reverseEvents.add(backKeyEvent);
        reverseEvents.add(swipeEvent);
        reverseEvents.add(scrollEvent);
        reverseEvents.add(longClickEvent);
        reverseEvents.add(clickEvent);
        reverseEvents.add(menuKeyEvent);
        List<AndroidEvent> actualEvents = (List<AndroidEvent>) reverseElementsInEventSequence.invoke(stateGraph, unReverseEvents);
        assertEquals(reverseEvents, actualEvents);
    }

    @Test
    public void testGetTypeOfActivityNames() throws NoSuchMethodException, DocumentException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method typeOfActivityNames = StateGraph.class.getDeclaredMethod("getTypeOfActivityNames");
        typeOfActivityNames.setAccessible(true);
        StateGraph stateGraph = new StateGraph();
        List<GUIState> states = new ArrayList<>();

        Field activity = GUIState.class.getDeclaredField("activityName");
        activity.setAccessible(true);
        activity.set(firstState, "HomePage");
        activity.set(secondState, "Activity1");
        activity.set(thirdState, "Activity2");

        states.add(firstState);
        states.add(secondState);
        states.add(thirdState);

        Field field = StateGraph.class.getDeclaredField("states");
        field.setAccessible(true);
        field.set(stateGraph, states);
        List<String> actual = (List<String>) typeOfActivityNames.invoke(stateGraph);
        assertEquals(3, actual.size());
        assertEquals("HomePage", actual.get(0));
        assertEquals("Activity1", actual.get(1));
        assertEquals("Activity2", actual.get(2));
    }

    @Test
    public void testDuplicateGetTypeOfActivityNames() throws NoSuchMethodException, DocumentException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method typeOfActivityNames = StateGraph.class.getDeclaredMethod("getTypeOfActivityNames");
        typeOfActivityNames.setAccessible(true);
        StateGraph stateGraph = new StateGraph();
        List<GUIState> states = new ArrayList<>();

        Field activity = GUIState.class.getDeclaredField("activityName");
        activity.setAccessible(true);
        activity.set(firstState, "HomePage");
        activity.set(secondState, "Activity1");
        activity.set(thirdState, "Activity1");

        states.add(firstState);
        states.add(secondState);
        states.add(thirdState);

        Field field = StateGraph.class.getDeclaredField("states");
        field.setAccessible(true);
        field.set(stateGraph, states);
        List<String> actual = (List<String>) typeOfActivityNames.invoke(stateGraph);
        assertEquals(2, actual.size());
        assertEquals("HomePage", actual.get(0));
        assertEquals("Activity1", actual.get(1));
    }

    @Test
    public void testGetShortestUnfiredPath() throws DocumentException, NoSuchFieldException, IllegalAccessException {
        StateGraph stateGraph = new StateGraph();
        ArrayList<AndroidEvent> events = new ArrayList<>();
        events.add(menuKeyEvent);
        events.add(clickEvent);
        events.add(longClickEvent);
        List<AndroidEvent> expected = new ArrayList<>();
        expected.add(menuKeyEvent);

        GUIState currentState = new GUIState(document, events);
        List<GUIState> states = new ArrayList<>();
        states.add(currentState);
        Field firstStateField = StateGraph.class.getDeclaredField("states");
        firstStateField.setAccessible(true);
        firstStateField.set(stateGraph, states);
        assertEquals(expected, stateGraph.getShortestUnfiredPath(currentState));
    }

    @Test
    public void testGetShortestUnfiredPathWhenEventIsVisited() throws DocumentException, NoSuchFieldException, IllegalAccessException {
        StateGraph stateGraph = new StateGraph();
        List<GUIState> states = new ArrayList<>();
        ArrayList<AndroidEvent> firstEvents = new ArrayList<>();
        ArrayList<AndroidEvent> secondEvents = new ArrayList<>();
        firstEvents.add(clickEvent);
        firstEvents.add(menuKeyEvent);
        firstEvents.add(longClickEvent);
        secondEvents.add(clickEvent);

        Field firstEventsVisitField = AndroidEvent.class.getDeclaredField("isVisited");
        firstEventsVisitField.setAccessible(true);
        firstEventsVisitField.set(firstEvents.get(0), true);
        Field firstEventsToStateField = AndroidEvent.class.getDeclaredField("toState");
        firstEventsToStateField.setAccessible(true);
        firstEventsToStateField.set(firstEvents.get(0), secondState);
        Field firstStateEventsField = GUIState.class.getDeclaredField("events");
        firstStateEventsField.setAccessible(true);
        firstStateEventsField.set(firstState, firstEvents);

        Field secondStateEventsField = GUIState.class.getDeclaredField("events");
        secondStateEventsField.setAccessible(true);
        secondStateEventsField.set(secondState, secondEvents);
        Field secondStateCrossAppDepth = GUIState.class.getDeclaredField("crossAppDepth");
        secondStateCrossAppDepth.setAccessible(true);
        secondStateCrossAppDepth.set(secondState, 5);

        states.add(firstState);
        Field firstStatesField = StateGraph.class.getDeclaredField("states");
        firstStatesField.setAccessible(true);
        firstStatesField.set(stateGraph, states);

        List<AndroidEvent> expected = new ArrayList<>();
        expected.add(menuKeyEvent);
        assertEquals(expected, stateGraph.getShortestUnfiredPath(firstState));
    }

    @Test
    public void testGetShortestUnfiredPathWhenAllPathFired() throws DocumentException, NoSuchFieldException, IllegalAccessException {
        StateGraph stateGraph = new StateGraph();
        GUIState firstState = new GUIState(document, new ArrayList<AndroidEvent>());
        ArrayList<AndroidEvent> events = new ArrayList<>();
        List<GUIState> states = new ArrayList<>();

        Field firstStateEventsField = GUIState.class.getDeclaredField("events");
        firstStateEventsField.setAccessible(true);
        firstStateEventsField.set(firstState, events);
        states.add(firstState);
        Field firstStatesField = StateGraph.class.getDeclaredField("states");
        firstStatesField.setAccessible(true);
        firstStatesField.set(stateGraph, states);

        List<AndroidEvent> expected = new ArrayList<>();
        assertEquals(expected, stateGraph.getShortestUnfiredPath(firstState));
    }

    @Test
    public void testAttemptCountRemoveCrossAppEvent() throws DocumentException, CloneNotSupportedException, NoSuchFieldException, IllegalAccessException {
        StateGraph stateGraph = new StateGraph();
        List<GUIState> states = new ArrayList<>();

        ArrayList<AndroidEvent> events = new ArrayList<>();
        events.add(clickEvent);
        events.add(backKeyEvent);
        events.add(longClickEvent);

        Field firstEventsToStateField = AndroidEvent.class.getDeclaredField("toState");
        firstEventsToStateField.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            firstEventsToStateField.set(events.get(i), secondState);
        }

        Field firstCreaseAttempCountField = AndroidEvent.class.getDeclaredField("attemptCount");
        firstCreaseAttempCountField.setAccessible(true);
        firstCreaseAttempCountField.set(events.get(0), 10);

        Field firstStateEventsField = GUIState.class.getDeclaredField("events");
        firstStateEventsField.setAccessible(true);
        firstStateEventsField.set(firstState, events);

        states.add(firstState);
        Field firstStatesField = StateGraph.class.getDeclaredField("states");
        firstStatesField.setAccessible(true);
        firstStatesField.set(stateGraph, states);

        StateGraph actualRemoveCrossAppEvent = stateGraph.removeCrossAppEvent();

        Field getActualAllState = StateGraph.class.getDeclaredField("states");
        getActualAllState.setAccessible(true);
        List<GUIState> actualAllStates = (List<GUIState>) getActualAllState.get(actualRemoveCrossAppEvent);

        Field getActualAllStatesEvents = GUIState.class.getDeclaredField("events");
        getActualAllStatesEvents.setAccessible(true);
        List<AndroidEvent> actualAllStatesEvents = (List<AndroidEvent>) getActualAllStatesEvents.get(actualAllStates.get(0));

        assertEquals(backKeyEvent, actualAllStatesEvents.get(0));
        assertEquals(longClickEvent, actualAllStatesEvents.get(1));
    }

    @Test
    public void testCrossAppEventThresholdRemoveCrossAppEvent() throws DocumentException, CloneNotSupportedException, NoSuchFieldException, IllegalAccessException {
        StateGraph stateGraph = new StateGraph();
        List<GUIState> states = new ArrayList<>();
        ArrayList<AndroidEvent> events = new ArrayList<>();
        events.add(clickEvent);
        events.add(backKeyEvent);
        events.add(longClickEvent);

        Field firstEventsToStateField = AndroidEvent.class.getDeclaredField("toState");
        firstEventsToStateField.setAccessible(true);
        firstEventsToStateField.set(events.get(0), secondState);
        for (int i = 1; i < 3; i++) {
            firstEventsToStateField.set(events.get(i), thirdState);
        }

        Field secondStateCrossAppDepth = GUIState.class.getDeclaredField("crossAppDepth");
        secondStateCrossAppDepth.setAccessible(true);
        secondStateCrossAppDepth.set(secondState, Config.CROSS_APP_EVENT_THRESHHOLD);

        Field firstStateEventsField = GUIState.class.getDeclaredField("events");
        firstStateEventsField.setAccessible(true);
        firstStateEventsField.set(firstState, events);

        states.add(firstState);
        Field firstStatesField = StateGraph.class.getDeclaredField("states");
        firstStatesField.setAccessible(true);
        firstStatesField.set(stateGraph, states);

        StateGraph actualRemoveCrossAppEvent = stateGraph.removeCrossAppEvent();
        List<GUIState> actualAllStates = (List<GUIState>) firstStatesField.get(actualRemoveCrossAppEvent);
        Field getActualAllStatesEvents = GUIState.class.getDeclaredField("events");
        getActualAllStatesEvents.setAccessible(true);
        List<AndroidEvent> actualAllStatesEvents = (List<AndroidEvent>) getActualAllStatesEvents.get(actualAllStates.get(0));

        assertEquals(backKeyEvent, actualAllStatesEvents.get(0));
        assertEquals(longClickEvent, actualAllStatesEvents.get(1));
    }

    @Test
    public void testUpdateStatesCrossAppDepthWhenLessThanFromState() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        StateGraph stateGraph = new StateGraph();
        List<AndroidEvent> eventSequence = new ArrayList<>();
        menuKeyEvent.setToState(thirdState);
        menuKeyEvent.setFromState(firstState);
        eventSequence.add(menuKeyEvent);

        Field firstStateEvents = GUIState.class.getDeclaredField("events");
        firstStateEvents.setAccessible(true);
        firstStateEvents.set(secondState, eventSequence);

        Field stateCrossApp = GUIState.class.getDeclaredField("crossAppDepth");
        stateCrossApp.setAccessible(true);
        stateCrossApp.set(firstState, 0);
        stateCrossApp.set(thirdState, 2);

        List<GUIState> states = new ArrayList<>();
        states.add(firstState);
        states.add(secondState);
        states.add(thirdState);
        Field stateField = StateGraph.class.getDeclaredField("states");
        stateField.setAccessible(true);
        stateField.set(stateGraph, states);

        Method computeCrossAppDepth = StateGraph.class.getDeclaredMethod("updateStatesCrossAppDepth", GUIState.class);
        computeCrossAppDepth.setAccessible(true);
        computeCrossAppDepth.invoke(stateGraph, thirdState);
        Field field = GUIState.class.getDeclaredField("crossAppDepth");
        field.setAccessible(true);
        assertEquals(1, (int) field.get(thirdState));
    }

    @Test
    public void testUpdateStatesCrossAppDepthWhenEqualFromState() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        StateGraph stateGraph = new StateGraph();
        List<AndroidEvent> eventSequence = new ArrayList<>();
        menuKeyEvent.setToState(thirdState);
        menuKeyEvent.setFromState(firstState);
        eventSequence.add(menuKeyEvent);

        Field firstStateEvents = GUIState.class.getDeclaredField("events");
        firstStateEvents.setAccessible(true);
        firstStateEvents.set(secondState, eventSequence);

        Field stateCrossApp = GUIState.class.getDeclaredField("crossAppDepth");
        stateCrossApp.setAccessible(true);
        stateCrossApp.set(firstState, 0);
        stateCrossApp.set(thirdState, 0);

        List<GUIState> states = new ArrayList<>();
        states.add(firstState);
        states.add(secondState);
        states.add(thirdState);
        Field stateField = StateGraph.class.getDeclaredField("states");
        stateField.setAccessible(true);
        stateField.set(stateGraph, states);

        Method computeCrossAppDepth = StateGraph.class.getDeclaredMethod("updateStatesCrossAppDepth", GUIState.class);
        computeCrossAppDepth.setAccessible(true);
        computeCrossAppDepth.invoke(stateGraph, thirdState);
        Field field = GUIState.class.getDeclaredField("crossAppDepth");
        field.setAccessible(true);
        assertEquals(0, (int) field.get(thirdState));
    }

    @Test
    public void testUpdateStatesCrossAppDepthWhenMoreThanFromState() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        StateGraph stateGraph = new StateGraph();
        List<AndroidEvent> eventSequence = new ArrayList<>();
        menuKeyEvent.setToState(thirdState);
        menuKeyEvent.setFromState(firstState);
        eventSequence.add(menuKeyEvent);

        Field firstStateEvents = GUIState.class.getDeclaredField("events");
        firstStateEvents.setAccessible(true);
        firstStateEvents.set(secondState, eventSequence);

        Field stateCrossApp = GUIState.class.getDeclaredField("crossAppDepth");
        stateCrossApp.setAccessible(true);
        stateCrossApp.set(firstState, 2);
        stateCrossApp.set(thirdState, 1);

        List<GUIState> states = new ArrayList<>();
        states.add(firstState);
        states.add(secondState);
        states.add(thirdState);
        Field stateField = StateGraph.class.getDeclaredField("states");
        stateField.setAccessible(true);
        stateField.set(stateGraph, states);

        Method computeCrossAppDepth = StateGraph.class.getDeclaredMethod("updateStatesCrossAppDepth", GUIState.class);
        computeCrossAppDepth.setAccessible(true);
        computeCrossAppDepth.invoke(stateGraph, thirdState);
        Field field = GUIState.class.getDeclaredField("crossAppDepth");
        field.setAccessible(true);
        assertEquals(1, (int) field.get(thirdState));
    }

    @Test
    public void testGuideEvents() throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        if (Config.EVENT_ORDER_CUSTOMIZE) {
            final String FILE_PATH = "test_gui_pages/test.xml";

            // create test.xml file, which is used for convert
            final String XML_SOURCE = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><states><state id=\"1\">" +
                    "<events><event>BackKey</event><event>MenuKey</event><event bounds=\"[0,0][1080,1776]\">Check</event>" +
                    "<event bounds=\"[0,0][1080,1776]\">Click</event><event bounds=\"[0,0][1080,1776]\">LongClick</event>" +
                    "<event direction=\"[0,0][100,100]\">Swipe</event><event direction=\"[0,0][100,100]\">Scroll</event>" +
                    "<event>EditText</event>"+
                    "</events></state><state id=\"2\"><events><event>BackKey</event><event>MenuKey</event>" +
                    "<event bounds=\"[0,0][1080,1776]\">Check</event><event bounds=\"[0,0][1080,1776]\">Click</event>" +
                    "<event bounds=\"[0,0][1080,1776]\">LongClick</event><event direction=\"[0,0][100,100]\">Swipe</event>" +
                    "<event direction=\"[0,0][100,100]\">Scroll</event></events></state></states>";
            FileWriter fw = new FileWriter(FILE_PATH);
            fw.write(XML_SOURCE);
            fw.close();
            File xmlFile = new File(FILE_PATH);

            List<GUIState> states = new ArrayList<>();
            firstState.setId(1);
            states.add(firstState);
            secondState.setId(2);
            states.add(secondState);
            secondState.addEvent(new BackKeyEvent());

            StateGraph stateGraph = new StateGraph();
            Field field = StateGraph.class.getDeclaredField("states");
            field.setAccessible(true);
            field.set(stateGraph, states);

            stateGraph.guideEvents();

            List<GUIState> actual = (List<GUIState>) field.get(stateGraph);
            assertEquals(8, actual.get(0).getEvents().size());
            assertEquals(8, actual.get(1).getEvents().size());
            xmlFile.delete();
            assertFalse(xmlFile.exists());
        }
    }
}