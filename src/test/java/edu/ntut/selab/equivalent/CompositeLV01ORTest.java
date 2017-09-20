package edu.ntut.selab.equivalent;

import edu.ntut.selab.StateGraph;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.exception.MultipleListOrGridException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CompositeLV01ORTest {
    private CompositeLV01OR handler;
    private String strCompare;

    @Before
    public void setUp() throws Exception {
        this.handler = new CompositeLV01OR();
        this.strCompare = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n" +
                "<hierarchy rotation=\"0\">\n" +
                "\t<node bounds=\"[0,0][320,320]\" checkable=\"false\" checked=\"false\"" +
                " content-desc=\"\">\n" +
                "\t</node>\n" +
                "</hierarchy>";
    }

    @Test
    public void testEqStateStrategy() throws NoSuchFieldException, IllegalAccessException {
        Field field = CompositeStrategy.class.getDeclaredField("eqStateStrategy");
        field.setAccessible(true);
        List<EquivalentStateStrategy> eqStateStrategies = (List<EquivalentStateStrategy>) field.get(this.handler);
        assertEquals(3, eqStateStrategies.size());
        List<String> expectStrategies = new ArrayList<>();
        expectStrategies.add("ExactlyEquivalentStrategy");
        expectStrategies.add("ListGridViewEquivalentStrategy");
        expectStrategies.add("DistanceEquivalentStrategy");
        for (EquivalentStateStrategy strategy : eqStateStrategies) {
            expectStrategies.contains(strategy.getClass().toString());
        }
    }

    @Test
    public void testIsEquivalent_True() throws DocumentException, MultipleListOrGridException {
        Document documentA = DocumentHelper.parseText(this.strCompare);
        GUIState stateA = new GUIState(documentA, new ArrayList<AndroidEvent>());
        StateGraph testState = new StateGraph();
        testState.addState(stateA);
        assertTrue(this.handler.isEquivalent(stateA, testState));
    }

    @Test
    public void testGetEquivalentState() throws Exception {
        Document document = DocumentHelper.parseText(this.strCompare);
        GUIState state = new GUIState(document, new ArrayList<AndroidEvent>());
        Field field = CompositeLV01OR.class.getDeclaredField("eqState");
        field.setAccessible(true);
        field.set(this.handler, state);
        assertEquals(state, this.handler.getEquivalentState());
    }
}