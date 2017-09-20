package edu.ntut.selab.util;

import edu.ntut.selab.data.NodeAttribute;
import edu.ntut.selab.event.*;
import edu.ntut.selab.event.SwipeEvent.Type;
import edu.ntut.selab.exception.ClickTypeErrorException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class XMLEventParser {
    private Document document = null;
    private AndroidEventFactory eventFactory;

    public XMLEventParser(Document document) {
        this.document = document;
        this.eventFactory = new AndroidEventFactory();
    }

    public int getNAFCount() {
        List nodes = this.document.getRootElement().selectNodes("//node");
        int nafCount = 0;
        for (Object node : nodes) {
            Element element = (Element) node;
            if (element.attributeValue("NAF") != null)
                nafCount++;
        }
        return nafCount;
    }

    public List<AndroidEvent> parseEvents() throws ClickTypeErrorException {
        List nodes = this.document.getRootElement().selectNodes("//node");
        List<AndroidEvent> events = new ArrayList();
        events.addAll(this.parseEditTextEvent());
        for (Object element : nodes) {
            events.addAll(this.getCheckEvents((Element) element));
            events.addAll(this.getScrollEvents((Element) element));
            events.addAll(this.getClickEvents((Element) element));
            events.addAll(this.getLongClickEvents((Element) element));
        }
        if (Config.ENABLE_BACKKEY_EVENT)
            events.add(new BackKeyEvent());
        if (Config.ENABLE_MENUKEY_EVENT)
            events.add(new MenuKeyEvent());
        Element element = (Element) nodes.get(0);
        String bounds = element.attribute(NodeAttribute.Bounds).getText();
        if (!checkIsBoundsInExistEvent(events, bounds)) {
            if (Config.SCROLL_EVERYVIEW)
                addSwipeEvent(events, element, Type.SCROLL);
            else
                addSwipeEvent(events, element, Type.SWIPE);
        }
        return events;
    }

    public List<AndroidEvent> parseEditTextEvent() {
        List<AndroidEvent> events = new ArrayList();
        boolean integrateEditText = Config.INTEGRATE_EDIT_TEXT;
        List nodes = this.document.getRootElement().selectNodes("//node");
        if (integrateEditText)
            events.addAll(getIntegrationEditTextEvent(nodes));
        else {
            for (Object element : nodes) {
                events.addAll(this.getEditTextEvents((Element) element));
            }
        }
        return events;
    }

    private List<AndroidEvent> getCheckEvents(Element element) throws ClickTypeErrorException {
        boolean isCheckable = element.attribute(NodeAttribute.Checkable).getText().equals("true");
        return this.getClickEvents(element, isCheckable, ClickEvent.Type.Check);
    }

    private List<AndroidEvent> getClickEvents(Element element) throws ClickTypeErrorException {
        boolean isClickable = element.attribute(NodeAttribute.Clickable).getText().equals("true");
        final String TEXT_VIEW = "android.widget.TextView";
        final String IMAGE_VIEW = "android.widget.ImageView";
        boolean isTextView = element.attribute(NodeAttribute.Class).getText().equals(TEXT_VIEW);
        boolean isImageView = element.attribute(NodeAttribute.Class).getText().equals(IMAGE_VIEW);
        boolean addTextView = Config.TEXTVIEW_CLICKABLE && isTextView;
        boolean addImageView = Config.IMAGEVIEW_CLICKABLE && isImageView;
        return this.getClickEvents(element, isClickable || addTextView || addImageView, ClickEvent.Type.Click);
    }

    /**
     * @param element   which to get click event
     * @param condition isClickable or isCheckable
     * @return all of click events in this element
     */
    private List<AndroidEvent> getClickEvents(Element element, boolean condition, ClickEvent.Type type) throws ClickTypeErrorException {
        List<AndroidEvent> clickEvents = new ArrayList<AndroidEvent>();
        if (condition) {
            if (element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.ListView)
                    || element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.GridView)) {
                clickEvents.addAll(this.getChildElementCheckEvent(element, type));
            } else {
                clickEvents.add(this.createClickEvent(element, type));
            }
        }
        return clickEvents;
    }

    private List<AndroidEvent> getChildElementCheckEvent(Element element, ClickEvent.Type type) throws ClickTypeErrorException {
        List<AndroidEvent> events = new ArrayList<>();
        for (Object childElement : element.elements()) {
            events.add(this.createClickEvent((Element) childElement, type));
        }
        return events;
    }

    private AndroidEvent createClickEvent(Element element, ClickEvent.Type type) throws ClickTypeErrorException {
        EventData eventData = new EventData(element);
        if (type.equals(ClickEvent.Type.Check))
            return this.eventFactory.createAndroidEvent("Check", eventData);
        else if (type.equals(ClickEvent.Type.Click))
            return this.eventFactory.createAndroidEvent("Click", eventData);
        throw new ClickTypeErrorException();
    }

    private List<AndroidEvent> getScrollEvents(Element element) {
        boolean isScrollable = element.attribute(NodeAttribute.Scrollable).getText().equals("true");
        List<AndroidEvent> events = new ArrayList<>();
        if (isScrollable) {
            EventData eventData = new EventData(element);
            events.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.RIGHT));
            events.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.LEFT));
            events.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.DOWN));
            events.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.UP));
        }
        return events;
    }

    private List<AndroidEvent> getIntegrationEditTextEvent(List nodes) {
        List<EventData> eventDatas = new ArrayList<>();
        for (Object node : nodes) {
            Element element = (Element) node;
            boolean isEditText = element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.EditText);
            if (isEditText) {
                List<String> texts = this.getTextXMLValue(document, element.attributeValue(NodeAttribute.Bounds));
                assertTrue(texts.size() <= 1);
                if (!texts.isEmpty()) {
                    EventData eventData = new EventData(element);
                    eventData.setValue(texts.get(0));
                    eventDatas.add(eventData);
                }
            }
        }
        List<AndroidEvent> events = new ArrayList<>();
        if (!eventDatas.isEmpty())
            events.add(this.eventFactory.createAndroidEvent("EditText", eventDatas));
        return events;
    }

    private List<AndroidEvent> getEditTextEvents(Element element) {
        boolean isEditText = element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.EditText);
        List<AndroidEvent> events = new ArrayList<>();
        if (isEditText) {
            EventData eventData = new EventData(element);
            ArrayList<String> valueList = this.getTextXMLValue(document, eventData.getBounds());
            for (String value : valueList) {
                eventData.setValue(value);
                events.add(this.eventFactory.createAndroidEvent("EditText", eventData));
            }
        }
        return events;
    }

    private List<AndroidEvent> getLongClickEvents(Element element) {
        boolean isLongClickable = element.attribute(NodeAttribute.LongClickable).getText().equals("true");
        List<AndroidEvent> events = new ArrayList<>();
        if (isLongClickable) {
            if (element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.ListView)
                    || element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.GridView)) {//list view state
                for (int i = 0; i < element.elements().size(); i++) {
                    EventData eventData = new EventData((Element) element.elements().get(i));
                    events.add(this.eventFactory.createAndroidEvent("LongClick", eventData));
                }
            } else {
                EventData eventData = new EventData(element);
                events.add(this.eventFactory.createAndroidEvent("LongClick", eventData));
            }
        }
        return events;
    }

    protected ArrayList<String> getTextXMLValue(Document targetDocument, String bounds) {
        //TODO UT
        //TODO add value using number of times
        ArrayList<String> valueList = new ArrayList<String>();
        List<Element> elementList = null;
        try {
            File textXMLFile = new File("inputFieldData/text.xml");
            Document document = (new SAXReader()).read(textXMLFile);
            Element rootElement = document.getRootElement().createCopy();
            elementList = rootElement.elements();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        for (Element valueElement : elementList) {
            String xpathValue = valueElement.attribute("xpath").getText();
            Node tempNode = targetDocument.selectSingleNode(xpathValue);
            String tempBounds = null;
            if (tempNode != null) {
                Element tempElement = (Element) tempNode;
                // �T�{����xpath��element�O�_��"android.widget.EditText"�A�ϥΪ̦��i���J���~
                if (tempElement.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.EditText)) {
                    tempBounds = tempElement.attribute(NodeAttribute.Bounds).getText();
                    // �P�ӭ������i��|���h��edit text�A�o�̧Q��bounds�T�{
                    if (tempBounds.equals(bounds)) {
                        valueList.add(valueElement.getText());
                    }
                }
            }
        }
        return valueList;
    }

    private boolean checkIsBoundsInExistEvent(List<AndroidEvent> eventList, String bounds) {
        for (AndroidEvent e : eventList) {
            if (e.getName().equals("Swipe Event") || e.getName().equals("Scroll Event")) {
                if (e.getEventData().getBounds().equals(bounds))
                    return true;
            }
        }
        return false;
    }

    private void addSwipeEvent(List<AndroidEvent> eventList, Element element, Type type) {
        EventData eventData = new EventData(element);
        if (type == Type.SCROLL) {              // scroll event
            eventList.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.RIGHT));
            eventList.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.LEFT));
            eventList.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.DOWN));
            eventList.add(this.createSwipeEvent(eventData, Type.SCROLL, SwipeEvent.Direction.UP));
        } else if (type == Type.SWIPE) {        // swipe event
            eventList.add(this.createSwipeEvent(eventData, Type.SWIPE, SwipeEvent.Direction.RIGHT));
            eventList.add(this.createSwipeEvent(eventData, Type.SWIPE, SwipeEvent.Direction.LEFT));
        }
    }

    private AndroidEvent createSwipeEvent(EventData eventData, Type type, SwipeEvent.Direction direction) {
        eventData.setSwipeDirection(direction);
        String typeStr = type == Type.SCROLL ? "Scroll" : "Swipe";
        return this.eventFactory.createAndroidEvent(typeStr, eventData);
    }

    public boolean containClass(String className) {
        List nodes = this.document.getRootElement().selectNodes("//node");
        for (Object node : nodes) {
            Element element = (Element) node;
            if (element.attributeValue(NodeAttribute.Class).compareTo(className) == 0)
                return true;
        }
        return false;
    }
}
