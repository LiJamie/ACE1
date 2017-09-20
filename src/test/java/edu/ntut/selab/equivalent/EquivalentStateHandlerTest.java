package edu.ntut.selab.equivalent;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EquivalentStateHandlerTest {
    private EquivalentStateHandler handler;

    @Before
    public void setup() {
        this.handler = new EquivalentStateHandler();
    }

    @Test
    public void testAreTheSame() throws DocumentException {
        final String path = "test_gui_pages/test_attribute_equal/";
        Element oldElement = (Element) this.readDocument(path + "8.xml").getRootElement().elementIterator().next();
        Element newElement = (Element) this.readDocument(path + "10.xml").getRootElement().elementIterator().next();
        System.out.print(oldElement.toString());
        boolean actual = this.handler.areTheSame(oldElement, newElement);
        assertFalse(actual);
    }

    @Test
    public void testIsAttributeEqual() throws DocumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String path = "test_gui_pages/test_attribute_equal/";
        Element oldElement = (Element) this.readDocument(path + "8.xml").getRootElement().elementIterator().next();
        Element newElement = (Element) this.readDocument(path + "10.xml").getRootElement().elementIterator().next();
        Method method = EquivalentStateHandler.class.getDeclaredMethod("isAttributeEqual", Element.class, Element.class);
        method.setAccessible(true);
        boolean actual = (boolean) method.invoke(this.handler, oldElement, newElement);
        assertTrue(actual);
    }

    @Test
    public void testMin() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("min", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 1,2);
        assertEquals(1, actual);
    }

    @Test
    public void testMinWhenEqual() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("min", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 1,1);
        assertEquals(1, actual);
    }

    @Test
    public void testMinWhenSecondParameterIsMin() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("min", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 2,1);
        assertEquals(1, actual);
    }

    @Test
    public void testMax() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("max", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 1,2);
        assertEquals(2, actual);
    }

    @Test
    public void testMaxWhenEqual() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("max", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 1,1);
        assertEquals(1, actual);
    }

    @Test
    public void testMaxWhenSecondParameterIsMax() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = EquivalentStateHandler.class.getDeclaredMethod("max", int.class, int.class);
        method.setAccessible(true);
        int actual = (int) method.invoke(this.handler, 2,1);
        assertEquals(2, actual);
    }

    private Document readDocument(String dotPath) throws DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(dotPath);
    }
}
