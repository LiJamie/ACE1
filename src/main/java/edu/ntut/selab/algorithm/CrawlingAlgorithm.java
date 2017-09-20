package edu.ntut.selab.algorithm;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.XMLReader;
import edu.ntut.selab.criteria.Timeout;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.equivalent.Equivalent;
import edu.ntut.selab.equivalent.EquivalentStateHandler;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.event.BackKeyEvent;
import edu.ntut.selab.event.EventExecutor;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.log.LoggerStore;
import edu.ntut.selab.util.Config;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class CrawlingAlgorithm {
    protected final String PACKAGE_NAME = Config.PACKAGE_NAME;
    protected final String ACTIVITY_NAME = Config.LAUNCHABLE_ACTIVITY;
    Timeout timeout = new Timeout();
    protected LoggerStore loggerStore = null;
    protected Device device;
    protected GUIState deskState = null;
    protected XMLReader xmlReader;
    protected Equivalent equivalentRule;
    protected List<GUIState> stateList;
    protected List<String> activityList;
    protected boolean firstCrashState = false;
    protected GUIState crashState;
    protected Stack<GUIState> visitedStateStack;
    protected int restartCount = 0; // parameter for test
    protected int totalFireEventCount = 0;
    protected int currentIndex = 0; // parameter for test
    protected EventExecutor eventExecutor;
    protected int eventOrderCount = 0;
    protected StateGraph stateGraph;

    public CrawlingAlgorithm() throws FileNotFoundException {
        Config config = new Config();
        this.device = new Device(config.getDeviceSerialNum());
        this.eventExecutor = new EventExecutor(this.device);
        this.stateList = new ArrayList<GUIState>();
        this.activityList = new ArrayList<String>();
        this.xmlReader = new XMLReader(this.device);
        this.equivalentRule = new EquivalentStateHandler();
        this.visitedStateStack = new Stack<GUIState>();
    }

    public void setLoggerStore(LoggerStore loggerStore) {
        this.loggerStore = loggerStore;
    }

    public void setUp() throws IOException, InterruptedException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException {
        // restart app, �]�wdeskState
        this.device.stopApp(PACKAGE_NAME);
        this.device.clearAppData(PACKAGE_NAME);
        this.device.pressHome();
        this.deskState = getGUIState();
        this.deskState.clearEvents();
        this.device.startApp(PACKAGE_NAME, ACTIVITY_NAME);
    }

    public abstract void execute() throws Exception;

    public abstract void setStateEquivalent(Equivalent equivalent);

    public abstract String getTimeString();

    public void setStateGraph(StateGraph stateGraph){this.stateGraph = stateGraph;}

    protected GUIState getGUIState() throws DocumentException, IOException, InterruptedException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException {
        GUIState screenState = this.xmlReader.read();
        this.xmlReader.setStateId(screenState);
        screenState.setImage(); // screen state ��imageList�[�J�ۤv���Ϥ�
        //xmlReader.getScreenPic(); // ����new state��visited state���I��
        if (this.deskState != null && screenState.getPackageName().equals(this.deskState.getPackageName()) &&
                screenState.getActivityName().equals(this.deskState.getActivityName())) {
            return this.deskState;
        } else if (!this.equivalentRule.checkStateIsInStateList(stateGraph.getAllStates(), screenState)) { // new state
            //else if (!this.equivalentRule.checkStateIsInStateList(this.stateList, screenState))
            System.out.println("new state");
            if (screenState.isCrashState()) { // crash state
                if (!this.firstCrashState) { // crash state�u���Ĥ@����intial
                    //xmlReader.setStateId(screenState);
                    //screenState.setImage(); // screen state ��imageList�[�J�ۤv���Ϥ�
                    //xmlReader.getScreenPic();
                    this.stateGraph.addState(screenState);
                    //stateList.add(screenState);
                    if (!isExistActivity(screenState.getActivityName()))
                        this.activityList.add(screenState.getActivityName());
                    this.crashState = screenState;
                    this.firstCrashState = true;
                }
                this.xmlReader.guiIndexPlus();
                return this.crashState;
            } else { // not crash state
                //xmlReader.setStateId(screenState);
                //screenState.setImage();
                //xmlReader.getScreenPic();
                this.stateGraph.addState(screenState);
                //this.stateList.add(screenState);
                if (!isExistActivity(screenState.getActivityName()))
                    this.activityList.add(screenState.getActivityName());

                for (AndroidEvent e : screenState.getEvents())
                    e.setVisited(false);
            }
            this.visitedStateStack.add(screenState);
            this.xmlReader.guiIndexPlus();
            return screenState;
        } else { // visited state
            System.out.println("visited state");
            //GUIState state = this.equivalentRule.getStateInStateList(this.stateList, screenState);
            GUIState state = this.equivalentRule.getStateInStateList(stateGraph.getAllStates(),screenState);
            if (state.isEquivalentState()) {
                if (haveFewerElement(screenState.contentClone().getRootElement(), state.contentClone().getRootElement())) {
                    //xmlReader.setStateId(screenState); // ��screenState�ثe��id�A�åB��id�ǵ�state�AscreenState�ѩ�S���s��bstateList�̴N���ޥ��F
                    //xmlReader.getScreenPic();
                    //state.setId(screenState.getId());
                    if (!state.getImagelist().contains(screenState.getImagelist().get(0)))
                        state.changeImage(screenState.getImagelist().get(0));
                }
            }
            this.xmlReader.guiIndexPlus();
            return state;
        }
    }

    protected void restartApp() throws IOException, InterruptedException, ExecuteCommandErrorException {
        System.out.println("<<restart app>>");
        currentIndex = 0;
        this.device.stopApp(PACKAGE_NAME);
        this.device.clearAppData(PACKAGE_NAME);
        // In bmi calculator, there are some situation cannot go back to root node, send backkey event can help restart
        BackKeyEvent back = new BackKeyEvent();
        executeEvent(back);
        this.totalFireEventCount--;

		/*
        String[] command = {"\"" +
				XMLReader.getConfigurationValue("adb") +
				"\"", "shell", "input", "keyevent", "KEYCODE_HOME"};
		try {
			CommandHelper.executeCommand(command);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
        this.device.pressHome();
        this.device.startApp(PACKAGE_NAME, ACTIVITY_NAME);

        this.restartCount++;
        System.out.println("<<restart app>>");
    }

    protected void executeEvent(AndroidEvent e) throws InterruptedException, ExecuteCommandErrorException, IOException {
        totalFireEventCount++;
        System.out.println("Event text = " + e.getReportLabel());
        eventExecutor.run(e);
        this.device.turnOffSoftKeyboard();
    }

    private boolean isExistActivity(String activity) {
        for (String a : activityList) {
            if (a.equals(activity))
                return true;
        }
        return false;
    }

    // element1 = currentState XML file, element2 = one of stateList's state XML file
    private boolean haveFewerElement(Element element1, Element element2) {
        if (element1.elements().size() < element2.elements().size())
            return true;
        for (int i = 0; i < Math.min(element1.elements().size(), element2.elements().size()); i++) {
            if (haveFewerElement((Element) element1.elements().get(i), (Element) element2.elements().get(i)))
                return true;
        }
        return false;
    }

    protected void setEventOrder(AndroidEvent e, List<AndroidEvent> eventList) {
        for (AndroidEvent orderEvent : eventList) {
            System.out.println("orderEventType : " + orderEvent.getName());
            System.out.println("eEventType : " + e.getName());
            if (orderEvent.getName().equals(e.getName())) {
                if (orderEvent.getReportLabel().equals(e.getReportLabel())) {
                    if (orderEvent.getToState() == e.getToState()) {
                        eventOrderCount++;
                        orderEvent.setOrder(Integer.toString(eventOrderCount));
                        break;
                    }
                }
            }
        }
    }
}
