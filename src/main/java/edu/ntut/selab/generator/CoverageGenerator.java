package edu.ntut.selab.generator;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.data.NodeAttribute;
import edu.ntut.selab.exception.*;
import edu.ntut.selab.iterator.CrawlerTransitionCoverageIterator;
import edu.ntut.selab.iterator.StateCoverageIterator;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.iterator.TestCaseIterator;
import edu.ntut.selab.iterator.TransitionCoverageIterator;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CoverageGenerator {
    private int time = 0;
    private TestCaseIterator iterator;
    private final String TESTCASE_FILE_PATH = "Test_Case/";
    private final String TESTCASE_FILE = Utility.getCreateFilePackageName() + "_" + Utility.getTimestamp() + "_" + Config.GENERATE_TEST_CASE_ALOGORITHM;
    private File testcaseFile, testCaseTxt;
    private PrintWriter writer, txtWriter;
    private List<List<AndroidEvent>> eventSequenceList;
    List<AndroidEvent> eventSquence;
    private boolean tag = false;

    public CoverageGenerator(StateGraph stateGraph, AndroidCrawler androidCrawler) throws FileNotFoundException, UnsupportedEncodingException {
        if (Config.GENERATE_TEST_CASE_ALOGORITHM.equals("StateCoverage"))
            this.iterator = new StateCoverageIterator(stateGraph,androidCrawler);
        else if (Config.GENERATE_TEST_CASE_ALOGORITHM.equals("TransitionCoverage"))
            this.iterator = new TransitionCoverageIterator(stateGraph,androidCrawler);
        else if (Config.GENERATE_TEST_CASE_ALOGORITHM.equals("CrawlerTransitionCoverage"))
            this.iterator = new CrawlerTransitionCoverageIterator(stateGraph,androidCrawler);
        new File (TESTCASE_FILE_PATH + TESTCASE_FILE).mkdirs();
        this.testcaseFile = new File(this.TESTCASE_FILE_PATH + "/" + this.TESTCASE_FILE + "/" + this.TESTCASE_FILE + ".java");
        this.writer = new PrintWriter(testcaseFile, "UTF-8");
        this.eventSequenceList = new ArrayList<List<AndroidEvent>>();
        this.eventSequenceList = new ArrayList<List<AndroidEvent>>();
    }

    public void generateStateCoveragePath() throws IOException, NullPackageNameException, MultipleListOrGridException, InterruptedException, ProgressBarTimeoutException, ClickTypeErrorException, EquivalentStateException, DocumentException, ExecuteCommandErrorException, CrawlerControllerInitialErrorException, EventFromStateErrorException, CannotReachTargetStateException {
        addTestCaseHeader();
        for(iterator.first(); !iterator.isDone(); iterator.next())
            addEventSequence();
        addEventSequence();
        System.out.println(this.eventSequenceList.size());
        stateCoveragePath(eventSequenceList);
        writeFinalReport();
        addFindCurrentStateExuteEventFunction();
        addExcuteRotationFunction();
        addCheckFileExists();
        addExcuteHomeKeyBackApp();
        addDetectCrashState();
        addDetectCurrentStateTriggerSystemEvent();
        addExcuteWifiEnable();
        addExcuteWifiDisable();
        addGetSaltString();
        addGetPositiveString();
        addTurnOffSoftKeyboard();
        addRunFiredEvent();
        addScrollDown();
        addCreateEditTextEvents();
        addClearStateGraphVisited();
        addTestCaseFooter();
    }

    private void addEventSequence() throws NullPackageNameException {
        if(iterator.checkThisPathFinished()){
            this.eventSquence = iterator.getEventSequence();
            if(this.eventSquence != null && !this.eventSquence.equals("") && !this.eventSquence.isEmpty())
                eventSequenceList.add(this.eventSquence);
        }
    }

    private void writeTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcase0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcase0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null) {");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
    }

    private void writeFinalReport(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcaseZFinalReport() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException {");
        this.writer.println("        for (GUIState state : stateGraph.getAllStates())");
        this.writer.println("           if(state.isCrashState())");
        this.writer.println("               state.clearEvents();");
        this.writer.println("        stateGraph.buildReport();");
        this.writer.println("        LogGenerator logGenerator = new LogGenerator(androidCrawler,stateGraph);");
        this.writer.println("        logGenerator.generateLog();");
        this.writer.println("    }");
    }

    private void writeTestCaseTxt(int i) throws FileNotFoundException, UnsupportedEncodingException {
        System.out.println("eventSquence :");
        this.testCaseTxt = new File(this.TESTCASE_FILE_PATH + "/" + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt");
        this.txtWriter = new PrintWriter(this.testCaseTxt, "UTF-8");
        for(AndroidEvent event : eventSequenceList.get(i)){
            GUIState state = event.getFromState();
            this.txtWriter.println(event.getReportLabel());
            System.out.println(" " + event.getReportLabel());
            if (checkStateEditText(state) && !state.getTag().contains("EditTextVisited")) {
                if(!tag && event.isToOriginalState()){
                    DuplicateRandomTextTestCase();
                    DuplicateNonInputTextTestCase();
                    DuplicateZeroTextTestCase();
                    DuplicatePositiveTextTestCase();
                    DuplicateNegativeTextTestCase();
                }
                state.setTag("EditTextVisited");
            }
        }
        this.txtWriter.close();
    }

    private boolean checkStateEditText(GUIState state){
        Document document = state.contentClone();
        List nodes = document.getRootElement().selectNodes("//node");
        for (Object node : nodes) {
            Element element = (Element) node;
            if(element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.EditText))
                return true;
        }
        return false;
    }

    private void stateCoveragePath(List<List<AndroidEvent>> eventSequenceList) throws FileNotFoundException, UnsupportedEncodingException {
        int count = 0;
        for(int i = 0 ; i < eventSequenceList.size() ; i++) {
            count += eventSequenceList.get(i).size();
            this.time++;
            writeTestCase();
            writeTestCaseTxt(i);
            this.tag = false;
        }
        System.out.println("coverage event count : " + count);
    }

    private void addTestCaseHeader(){
        addImportHeader();
        this.writer.println("");
        this.writer.println("@FixMethodOrder(MethodSorters.NAME_ASCENDING)");
        this.writer.println("public class " + this.TESTCASE_FILE + " {");
        this.writer.println("    private Device device = new Device(new Config().getDeviceSerialNum());");
        this.writer.println("    private StateGraph stateGraph = new StateGraph();");
        this.writer.println("    private SingleDeviceController singleDeviceController;");
        this.writer.println("    private AndroidCrawler androidCrawler;");
        this.writer.println("    private AndroidEvent androidEvent;");
        this.writer.println("    private FileReader fileReader;");
        this.writer.println("    private BufferedReader bufferedReader;");
        this.writer.println("    private List<String> events;");
        this.writer.println("    private GUIState currentState;");
        this.writer.println("    private String[] command = null;");
        this.writer.println("    private boolean tag = false;");
        this.writer.println("    private List<EventData> editTextEventDatas;");
        this.writer.println("    private AndroidEventFactory eventFactory;");
        this.writer.println("    private String bounds = \"\";");
        this.writer.println("    private String testcaseName = \"\";");
        this.writer.println("    private EquivalentStateStrategy equivalentStateStrategy;");
        this.writer.println("    final String SERIALIZATION_PATH = System.getProperty(\"user.dir\") + \"/\" + Utility.getReportPath() + \"/serialData\";");
        this.writer.println("    final String SERIALIZATION_FILE = \"serializeStateGraphData.ser\";");
        this.writer.println("    final String SERIALIZATION_ANDROIDCRAWLER_PATH = System.getProperty(\"user.dir\") + \"/\" + Utility.getReportPath() + \"/serialData\";");
        this.writer.println("    final String SERIALIZATION_ANDROIDCRAWLER_FILE = \"serializeAndroidCrawlerData.ser\";");
        this.writer.println("    File serializationFile = new File(SERIALIZATION_PATH + \"/\" + SERIALIZATION_FILE);");
        this.writer.println("    File serializationAndroidCrawler = new File(SERIALIZATION_ANDROIDCRAWLER_PATH + \"/\" + SERIALIZATION_ANDROIDCRAWLER_FILE);");
        this.writer.println("");
        this.writer.println("    @Before");
        this.writer.println("    public void setup() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, ClassNotFoundException, ProgressBarTimeoutException  {");
        this.writer.println("        this.eventFactory = new AndroidEventFactory();");
        this.writer.println("        events = new ArrayList<>();");
        this.writer.println("        equivalentStateStrategy = new EquivalentStateStrategyFactory().createStrategy();");
        this.writer.println("        singleDeviceController = new SingleDeviceController();");
        this.writer.println("        if(!checkFileExists()) {");
        this.writer.println("            this.stateGraph = new StateGraph();");
        this.writer.println("            this.stateGraph.setEquivalentStragegy(equivalentStateStrategy);");
        this.writer.println("            this.androidCrawler = new AndroidCrawler(device, stateGraph);");
        this.writer.println("            this.androidCrawler.startExplore();");
        this.writer.println("        }");
        this.writer.println("        else if (serializationFile.exists() && serializationAndroidCrawler.exists()) {");
        this.writer.println("            this.stateGraph = (StateGraph) this.singleDeviceController.deSerializeFile(serializationFile);");
        this.writer.println("            this.androidCrawler = (AndroidCrawler) this.singleDeviceController.deSerializeFile(serializationAndroidCrawler);");
        this.writer.println("            this.androidCrawler.setStateGraph(this.stateGraph);");
        this.writer.println("        }");
        this.writer.println("        this.stateGraph.isInStateList(this.androidCrawler.dumpCurrentState());");
        this.writer.println("        this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        detectCurrentStateTriggerSystemEvent();");
        this.writer.println("    }");
    }

    private  void addImportHeader(){
        this.writer.println("package " + TESTCASE_FILE + ";");
        this.writer.println("");
        this.writer.println("import edu.ntut.selab.controller.SingleDeviceController;");
        this.writer.println("import edu.ntut.selab.data.NodeAttribute;");
        this.writer.println("import edu.ntut.selab.equivalent.EquivalentStateStrategy;");
        this.writer.println("import edu.ntut.selab.equivalent.EquivalentStateStrategyFactory;");
        this.writer.println("import edu.ntut.selab.exception.*;");
        this.writer.println("import edu.ntut.selab.util.Utility;");
        this.writer.println("import edu.ntut.selab.util.Config;");
        this.writer.println("import edu.ntut.selab.generator.LogGenerator;");
        this.writer.println("import org.dom4j.Document;");
        this.writer.println("import org.dom4j.DocumentException;");
        this.writer.println("import static org.junit.Assert.*;");
        this.writer.println("import org.junit.Test;");
        this.writer.println("import org.junit.Before;");
        this.writer.println("import edu.ntut.selab.entity.Device;");
        this.writer.println("import edu.ntut.selab.event.*;");
        this.writer.println("import edu.ntut.selab.ExecuteCommandErrorException;");
        this.writer.println("import java.io.*;");
        this.writer.println("import edu.ntut.selab.AndroidCrawler;");
        this.writer.println("import edu.ntut.selab.StateGraph;");
        this.writer.println("import edu.ntut.selab.data.GUIState;");
        this.writer.println("import java.io.FileReader;");
        this.writer.println("import java.io.BufferedReader;");
        this.writer.println("import edu.ntut.selab.TimeHelper;");
        this.writer.println("import java.util.List;");
        this.writer.println("import java.util.concurrent.TimeUnit;");
        this.writer.println("import java.util.ArrayList;");
        this.writer.println("import org.dom4j.Element;");
        this.writer.println("import java.util.concurrent.ThreadLocalRandom;");
        this.writer.println("import org.junit.FixMethodOrder;");
        this.writer.println("import org.junit.runners.MethodSorters;");
    }

    private void addTestCaseFooter(){
        this.writer.println("");
        this.writer.println("}");
        this.writer.close();
    }

    private void addFindCurrentStateExuteEventFunction(){
        this.writer.println("");
        this.writer.println("    private AndroidEvent findCurrentStateExcuteEvent(String eventReportLabel) {");
        this.writer.println("        GUIState state = this.currentState;");
        this.writer.println("        for(AndroidEvent event : state.getEvents())");
        this.writer.println("            if(event.getReportLabel().contains(eventReportLabel))");
        this.writer.println("                return event;");
        this.writer.println("        return null;");
        this.writer.println("    }");
    }

    private void addCheckFileExists(){
        this.writer.println("");
        this.writer.println("    private Boolean checkFileExists() {");
        this.writer.println("        File file = new File(System.getProperty(\"user.dir\") + \"/gui_pages\");");
        this.writer.println("        File [] fileList = file.listFiles();");
        this.writer.println("        for(int i = 0 ; i < fileList.length;i++){");
        this.writer.println("            String name = \"gui_pages\" + \"/\" + fileList[i].getName();");
        this.writer.println("            if(name.equals(Utility.getReportPath()))");
        this.writer.println("                 return true;");
        this.writer.println("            }");
        this.writer.println("        return false;");
        this.writer.println("    }");
    }

    private void addExcuteHomeKeyBackApp(){
        this.writer.println("");
        this.writer.println("    private void excuteHomeKeyAndBackApp() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, EquivalentStateException, DocumentException, MultipleListOrGridException, ClickTypeErrorException, ProgressBarTimeoutException {");
        this.writer.println("        GUIState state = this.androidCrawler.dumpCurrentState();");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("        HomeKeyAndBackAppEvent homeKeyEvent = new HomeKeyAndBackAppEvent();");
        this.writer.println("        homeKeyEvent.setFromState(this.currentState);");
        this.writer.println("        homeKeyEvent.homeKey();");
        this.writer.println("        this.command = homeKeyEvent.getCommand();");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.stateGraph.increaseTotalExecutedEventCount();");
        this.writer.println("        this.currentState.addEvent(homeKeyEvent);");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        homeKeyEvent.setToState(this.currentState);");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        HomeKeyAndBackAppEvent backAppEvents = new HomeKeyAndBackAppEvent();");
        this.writer.println("        backAppEvents.setFromState(this.currentState);");
        this.writer.println("        this.currentState.addEvent(backAppEvents);");
        this.writer.println("        List<String[]> commands = backAppEvents.backApplication();");
        this.writer.println("        for(String[] backAppCommand : commands){");
        this.writer.println("            this.device.executeADBCommand(backAppCommand);");
        this.writer.println("            TimeUnit.SECONDS.sleep(2);");
        this.writer.println("        }");
        this.writer.println("        turnOffSoftKeyboard(this.device);");
        this.writer.println("        if(state.containListOrGrid() && !this.tag)");
        this.writer.println("            scrollerDown();");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        backAppEvents.setToState(this.currentState);");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        this.events.add(\"HomeKeyAndThenBackApp\");");
        this.writer.println("        if(detectCrashState(backAppEvents) || !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("            if(!detectCrashState(backAppEvents) && !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("               System.out.print(\"Error Path : \");");
        this.writer.println("               System.out.print(\"Start App\");");
        this.writer.println("               for(String eventReportLabel : events)");
        this.writer.println("                   System.out.print(\" -> \" + eventReportLabel);");
        this.writer.println("               System.out.println(\" -> Error\");");
        this.writer.println("            }");
        this.writer.println("            homeKeyEvent.setReportLabel(this.testcaseName + \" press(\\\\\\\"HomeKey Error\\\\\\\")\");");
        this.writer.println("            backAppEvents.setReportLabel(this.testcaseName + \" press(\\\\\\\"BackApplicationKey Error\\\\\\\")\");");
        this.writer.println("            this.androidCrawler.increaseGUIIndex();");
        this.writer.println("            this.androidCrawler.restartApp();");
        this.writer.println("            clearStateGraphVisited();");
        this.writer.println("            assertFalse(true);");
        this.writer.println("        }");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("    }");
    }

    private void addExcuteRotationFunction(){
        this.writer.println("");
        this.writer.println("    private void excuteRotation() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, EquivalentStateException, DocumentException, MultipleListOrGridException, ClickTypeErrorException, ProgressBarTimeoutException {");
        this.writer.println("        this.command = new String[]{\"shell\", \"content\", \"insert\", \"--uri\", \"content://settings/system\",  \"--bind\", \"name:s:accelerometer_rotation\", \"--bind\", \"value:i:0\"};");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.stateGraph.increaseTotalExecutedEventCount();");
        this.writer.println("        GUIState state = this.androidCrawler.dumpCurrentState();");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("        RotationEvent rotationLandscape = new RotationEvent();");
        this.writer.println("        rotationLandscape.setFromState(this.currentState);");
        this.writer.println("        rotationLandscape.rotaionLandscape();");
        this.writer.println("        this.command = rotationLandscape.getCommand();");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.currentState.addEvent(rotationLandscape);");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        rotationLandscape.setToState(this.currentState);");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        RotationEvent rotationPortrait = new RotationEvent();");
        this.writer.println("        rotationPortrait.setFromState(this.currentState);");
        this.writer.println("        rotationPortrait.rotationPortrait();");
        this.writer.println("        this.command = rotationPortrait.getCommand();");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.currentState.addEvent(rotationPortrait);");
        this.writer.println("        turnOffSoftKeyboard(this.device);");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        rotationPortrait.setToState(this.currentState);");
        this.writer.println("        this.events.add(\"Rotation\");");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        if(detectCrashState(rotationPortrait) || !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("            if(!detectCrashState(rotationPortrait) && !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("               System.out.print(\"Error Path : \");");
        this.writer.println("               System.out.print(\"Start App\");");
        this.writer.println("               for(String eventReportLabel : events)");
        this.writer.println("                   System.out.print(\" -> \" + eventReportLabel);");
        this.writer.println("               System.out.println(\" -> Error\");");
        this.writer.println("            }");
        this.writer.println("            rotationLandscape.setReportLabel(this.testcaseName + \" Rotation(\\\\\\\"Landscape Error\\\\\\\")\");");
        this.writer.println("            rotationPortrait.setReportLabel(this.testcaseName + \" Rotation(\\\\\\\"Portrait Error\\\\\\\")\");");
        this.writer.println("            this.androidCrawler.increaseGUIIndex();");
        this.writer.println("            this.androidCrawler.restartApp();");
        this.writer.println("            clearStateGraphVisited();");
        this.writer.println("            assertFalse(true);");
        this.writer.println("        }");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("    }");
    }

    private void addDetectCrashState(){
        this.writer.println("");
        this.writer.println("    private Boolean detectCrashState(AndroidEvent event) throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, EquivalentStateException, DocumentException, MultipleListOrGridException, ClickTypeErrorException, ProgressBarTimeoutException {");
        this.writer.println("        if(this.currentState.isCrashState()){");
        this.writer.println("            this.command = new String[]{\"shell\" , \"input\" , \"keyevent\" , \"19\"};");
        this.writer.println("            this.device.executeADBCommand(this.command);");
        this.writer.println("            this.command = new String[]{\"shell\" , \"input\" , \"keyevent\" , \"23\"};");
        this.writer.println("            this.device.executeADBCommand(this.command);");
        this.writer.println("            System.out.print(\"Error Path : \");");
        this.writer.println("            System.out.print(\"Start App\");");
        this.writer.println("            for(String eventReportLabel : events)");
        this.writer.println("                System.out.print(\" -> \" + eventReportLabel);");
        this.writer.println("            System.out.println(\" -> Error\");");
        this.writer.println("            return true;");
        this.writer.println("        }");
        this.writer.println("        else if(Config.APP_INSTRUMENTED && this.androidCrawler.isDesktopState(this.currentState)");
        this.writer.println("                && !event.getReportLabel().contains(\"BackKey\") && !event.getReportLabel().contains(\"HomeKey\")){");
        this.writer.println("            System.out.print(\"Error Path : \");");
        this.writer.println("            System.out.print(\"Start App\");");
        this.writer.println("            for(String eventReportLabel : events)");
        this.writer.println("                System.out.print(\" -> \" + eventReportLabel);");
        this.writer.println("            System.out.println(\" -> Error\");");
        this.writer.println("            return true;");
        this.writer.println("        }");
        this.writer.println("        return false;");
        this.writer.println("    }");
    }

    private void addDetectCurrentStateTriggerSystemEvent(){
        this.writer.println("");
        this.writer.println("    private void detectCurrentStateTriggerSystemEvent() throws NullPackageNameException, MultipleListOrGridException, InterruptedException, ProgressBarTimeoutException, IOException, EquivalentStateException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException {");
        this.writer.println("        if(!this.androidCrawler.isDesktopState(this.currentState) && this.equivalentStateStrategy.isEquivalent(this.currentState,this.androidCrawler.getStateGraph())");
        this.writer.println("            && !this.currentState.isCrashState() && this.currentState.getCrossAppDepth() < Config.CROSS_APP_EVENT_THRESHHOLD){");
        this.writer.println("            GUIState state = this.equivalentStateStrategy.getEquivalentState();");
        this.writer.println("            for(AndroidEvent event : state.getEvents()){");
        this.writer.println("                String string = this.currentState.getTag();");
        this.writer.println("                if(event.getReportLabel().contains(\"Rotation\"))");
        this.writer.println("                    this.currentState.setTag(string + \"Rotation \");");
        this.writer.println("                else if (event.getReportLabel().contains(\"HomeKey\"))");
        this.writer.println("                    this.currentState.setTag(string + \"HomeKey \");");
        this.writer.println("                else if (event.getReportLabel().contains(\"Enable\"))");
        this.writer.println("                    this.currentState.setTag(string + \"Wifi_Enable \");");
        this.writer.println("                else if (event.getReportLabel().contains(\"Disable\"))");
        this.writer.println("                    this.currentState.setTag(string + \"Wifi_Disable \");");
        this.writer.println("            }");
        this.writer.println("            if ((!this.currentState.getTag().contains(\"Rotation\") || !this.currentState.getTag().contains(\"HomeKey\")");
        this.writer.println("               || !this.currentState.getTag().contains(\"Wifi_Enable\") || !this.currentState.getTag().contains(\"Wifi_Disable\"))){");
        this.writer.println("                if(!this.currentState.getTag().contains(\"Rotation\"))");
        this.writer.println("                      this.excuteRotation();");
        this.writer.println("                if(!this.currentState.getTag().contains(\"HomeKey\"))");
        this.writer.println("                     this.excuteHomeKeyAndBackApp();");
        this.writer.println("                if(!this.currentState.getTag().contains(\"Wifi_Disable\"))");
        this.writer.println("                    this.excuteWifiDisable();");
        this.writer.println("                if(!this.currentState.getTag().contains(\"Wifi_Enable\"))");
        this.writer.println("                    this.excuteWifiEnable();");
        this.writer.println("            }");
        this.writer.println("        }");
        this.writer.println("    }");
    }

    private void addExcuteWifiEnable(){
        this.writer.println("");
        this.writer.println("    private void excuteWifiEnable() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, EquivalentStateException, DocumentException, MultipleListOrGridException, ClickTypeErrorException, ProgressBarTimeoutException {");
        this.writer.println("        GUIState state = this.androidCrawler.dumpCurrentState();");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("        WifiEvent wifiEvent = new WifiEvent();");
        this.writer.println("        wifiEvent.setFromState(this.currentState);");
        this.writer.println("        this.currentState.addEvent(wifiEvent);");
        this.writer.println("        wifiEvent.EnableWifi();");
        this.writer.println("        this.command = wifiEvent.getCommand();");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.stateGraph.increaseTotalExecutedEventCount();");
        this.writer.println("        TimeUnit.SECONDS.sleep(5);");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        wifiEvent.setToState(this.currentState);");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        if(detectCrashState(wifiEvent) || !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("            wifiEvent.setReportLabel(this.testcaseName + \" Wifi(\\\\\\\"Enable Error\\\\\\\")\");");
        this.writer.println("            this.androidCrawler.increaseGUIIndex();");
        this.writer.println("            this.androidCrawler.restartApp();");
        this.writer.println("            clearStateGraphVisited();");
        this.writer.println("            assertFalse(true);");
        this.writer.println("        }");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("    }");
    }

    private void addExcuteWifiDisable(){
        this.writer.println("");
        this.writer.println("    private void excuteWifiDisable() throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, EquivalentStateException, DocumentException, MultipleListOrGridException, ClickTypeErrorException, ProgressBarTimeoutException {");
        this.writer.println("        GUIState state = this.androidCrawler.dumpCurrentState();");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("        WifiEvent wifiEvent = new WifiEvent();");
        this.writer.println("        wifiEvent.setFromState(this.androidCrawler.getCurrentState());");
        this.writer.println("        this.currentState.addEvent(wifiEvent);");
        this.writer.println("        wifiEvent.DisableWifi();");
        this.writer.println("        this.command = wifiEvent.getCommand();");
        this.writer.println("        this.device.executeADBCommand(this.command);");
        this.writer.println("        this.stateGraph.increaseTotalExecutedEventCount();");
        this.writer.println("        TimeUnit.SECONDS.sleep(5);");
        this.writer.println("        try{");
        this.writer.println("            this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("            this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("            this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("        }catch (EquivalentStateException e) {");
        this.writer.println("            System.out.println(\"EquivalentStateException\");");
        this.writer.println("        }");
        this.writer.println("        wifiEvent.setToState(this.currentState);");
        this.writer.println("        TimeHelper.sleep(TimeHelper.getWaitingTime(\"eventSleepTimeSecond\"));");
        this.writer.println("        if(detectCrashState(wifiEvent) || !this.androidCrawler.dumpCurrentState().areTheSameExcludeListAndGrid(state)){");
        this.writer.println("            wifiEvent.setReportLabel(this.testcaseName + \" Wifi(\\\\\\\"Disable Error\\\\\\\")\");");
        this.writer.println("            this.androidCrawler.increaseGUIIndex();");
        this.writer.println("            this.androidCrawler.restartApp();");
        this.writer.println("            clearStateGraphVisited();");
        this.writer.println("            assertFalse(true);");
        this.writer.println("        }");
        this.writer.println("        this.androidCrawler.increaseGUIIndex();");
        this.writer.println("    }");
    }

    private void DuplicateRandomTextTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcaseRandomEditText0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcaseRandomEditText0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            editTextEventDatas = createStateEditText(this.currentState);");
        this.writer.println("            if(editTextEventDatas.size() != 0 && !this.currentState.isVisited()){");
        this.writer.println("                for(EventData eventData : editTextEventDatas)");
        this.writer.println("                    eventData.setValue(getSaltString());");
        this.writer.println("                this.androidEvent = eventFactory.createAndroidEvent(\"EditText\", editTextEventDatas);");
        this.writer.println("                this.currentState.addEvent(this.androidEvent);");
        this.writer.println("                this.currentState.setVisited(true);");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null && !this.androidEvent.getReportLabel().contains(\"EditText\")) ");
        this.writer.println("               runFiredEvent();");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        clearStateGraphVisited();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
        this.tag = true;
    }

    private void DuplicateNonInputTextTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcaseNonInputEditText0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcaseNonInputEditText0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            editTextEventDatas = createStateEditText(this.currentState);");
        this.writer.println("            if(editTextEventDatas.size() != 0 && !this.currentState.isVisited()){");
        this.writer.println("                for(EventData eventData : editTextEventDatas)");
        this.writer.println("                    eventData.setValue(\"\");");
        this.writer.println("                this.androidEvent = eventFactory.createAndroidEvent(\"EditText\", editTextEventDatas);");
        this.writer.println("                this.currentState.addEvent(this.androidEvent);");
        this.writer.println("                this.currentState.setVisited(true);");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null && !this.androidEvent.getReportLabel().contains(\"EditText\"))");
        this.writer.println("               runFiredEvent();");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        clearStateGraphVisited();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
        this.tag = true;
    }

    private void DuplicateZeroTextTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcaseEditZeroText0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcaseEditZeroText0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            editTextEventDatas = createStateEditText(this.currentState);");
        this.writer.println("            if(editTextEventDatas.size() != 0 && !this.currentState.isVisited()){");
        this.writer.println("                for(EventData eventData : editTextEventDatas)");
        this.writer.println("                    eventData.setValue(\"0\");");
        this.writer.println("                this.androidEvent = eventFactory.createAndroidEvent(\"EditText\", editTextEventDatas);");
        this.writer.println("                this.currentState.addEvent(this.androidEvent);");
        this.writer.println("                this.currentState.setVisited(true);");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null && !this.androidEvent.getReportLabel().contains(\"EditText\")) ");
        this.writer.println("               runFiredEvent();");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        clearStateGraphVisited();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
    }

    private void DuplicatePositiveTextTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcasePositiveEditText0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcasePositiveEditText0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            editTextEventDatas = createStateEditText(this.currentState);");
        this.writer.println("            if(editTextEventDatas.size() != 0 && !this.currentState.isVisited()){");
        this.writer.println("                for(EventData eventData : editTextEventDatas)");
        this.writer.println("                    eventData.setValue(getPositiveString());");
        this.writer.println("                this.androidEvent = eventFactory.createAndroidEvent(\"EditText\", editTextEventDatas);");
        this.writer.println("                this.currentState.addEvent(this.androidEvent);");
        this.writer.println("                this.currentState.setVisited(true);");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null && !this.androidEvent.getReportLabel().contains(\"EditText\")) ");
        this.writer.println("               runFiredEvent();");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        clearStateGraphVisited();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
    }

    private void DuplicateNegativeTextTestCase(){
        this.writer.println("");
        this.writer.println("    @Test");
        this.writer.println("    public void testcaseNegativeEditText0" + this.time + "()" + " throws InterruptedException, ExecuteCommandErrorException, IOException, NullPackageNameException, ClickTypeErrorException, MultipleListOrGridException, DocumentException, EquivalentStateException, EventFromStateErrorException, CannotReachTargetStateException, ProgressBarTimeoutException, ClassNotFoundException  {");
        this.writer.println("        this.fileReader = new FileReader(\"" + this.TESTCASE_FILE_PATH  + this.TESTCASE_FILE + "/" + "testcase0" + this.time + ".txt\"" + ");");
        this.writer.println("        this.testcaseName = \"" + "testcaseNegativeEditText0" + this.time + "\"" + ";");
        this.writer.println("        this.bufferedReader = new BufferedReader(fileReader);");
        this.writer.println("        while (bufferedReader.ready()) {");
        this.writer.println("            editTextEventDatas = createStateEditText(this.currentState);");
        this.writer.println("            if(editTextEventDatas.size() != 0 && !this.currentState.isVisited()){");
        this.writer.println("                for(EventData eventData : editTextEventDatas)");
        this.writer.println("                    eventData.setValue(\"-\" + getPositiveString());");
        this.writer.println("                this.androidEvent = eventFactory.createAndroidEvent(\"EditText\", editTextEventDatas);");
        this.writer.println("                this.currentState.addEvent(this.androidEvent);");
        this.writer.println("                this.currentState.setVisited(true);");
        this.writer.println("                runFiredEvent();");
        this.writer.println("            }");
        this.writer.println("            this.androidEvent = this.findCurrentStateExcuteEvent(bufferedReader.readLine());");
        this.writer.println("            if(this.androidEvent != null && !this.androidEvent.getReportLabel().contains(\"EditText\")) ");
        this.writer.println("               runFiredEvent();");
        this.writer.println("        }");
        this.writer.println("        fileReader.close();");
        this.writer.println("        clearStateGraphVisited();");
        this.writer.println("        this.androidCrawler.restartApp();");
        this.writer.println("        assertTrue(true);");
        this.writer.println("    }");
    }

    private void addGetPositiveString(){
        this.writer.println("");
        this.writer.println("    protected static String getPositiveString() {");
        this.writer.println("        int randomNumeber = (int) Math.round(Math.random() * 10);");
        this.writer.println("        StringBuilder builder = new StringBuilder(randomNumeber);");
        this.writer.println("        for (int i = 0; i < randomNumeber; i++) {");
        this.writer.println("            builder.append((char) (ThreadLocalRandom.current().nextInt(48, 57)));");
        this.writer.println("        }");
        this.writer.println("        return builder.toString();");
        this.writer.println("    }");
    }

    private void addGetSaltString(){
        this.writer.println("");
        this.writer.println("    protected static String getSaltString() {");
        this.writer.println("        int randomNumeber = (int) Math.round(Math.random() * 100);");
        this.writer.println("        StringBuilder builder = new StringBuilder(randomNumeber);");
        this.writer.println("        for (int i = 0; i < randomNumeber; i++) {");
        this.writer.println("            char randomChar = (char) (ThreadLocalRandom.current().nextInt(33, 128));");
        this.writer.println("            if(randomChar != '\\\"' && randomChar != '\\\\' && randomChar != '?')");
        this.writer.println("                builder.append(randomChar);");
        this.writer.println("        }");
        this.writer.println("        return builder.toString();");
        this.writer.println("    }");
    }

    private void addTurnOffSoftKeyboard(){
        this.writer.println("");
        this.writer.println("    private void turnOffSoftKeyboard(Device device) throws InterruptedException, ExecuteCommandErrorException, IOException {");
        this.writer.println("        String[] command = {\"shell\", \"dumpsys\", \"input_method\", \"|\", \"grep\", \"\\\"mInputShown=true\\\"\"};");
        this.writer.println("        List<String> feedBack = device.executeADBCommand(command);");
        this.writer.println("        if (feedBack.size() != 0) {");
        this.writer.println("            (new BackKeyEvent()).executeOn(device);");
        this.writer.println("        }");
        this.writer.println("    }");
    }

    private void addRunFiredEvent(){
        this.writer.println("");
        this.writer.println("   private void runFiredEvent() throws IOException, NullPackageNameException, InterruptedException, ClickTypeErrorException, EquivalentStateException, ExecuteCommandErrorException, MultipleListOrGridException, ProgressBarTimeoutException, DocumentException {");
        this.writer.println("       if (this.androidEvent.getReportLabel().contains(\"Restart\"))");
        this.writer.println("           this.androidCrawler.restartApp();");
        this.writer.println("       else {");
        this.writer.println("           this.androidEvent.setFromState(this.currentState);");
        this.writer.println("           this.androidEvent.executeOn(this.device);");
        this.writer.println("           this.stateGraph.increaseTotalExecutedEventCount();");
        this.writer.println("           turnOffSoftKeyboard(this.device);");
        this.writer.println("           this.events.add(this.androidEvent.getReportLabel());");
        this.writer.println("           try{");
        this.writer.println("               this.androidCrawler.updateStateGraph(this.androidCrawler.dumpCurrentState());");
        this.writer.println("               this.stateGraph.isInStateList(this.androidCrawler.getCurrentState());");
        this.writer.println("               this.currentState = this.stateGraph.getState(this.androidCrawler.getCurrentState());");
        this.writer.println("           }catch (EquivalentStateException e) {");
        this.writer.println("               System.out.println(\"EquivalentStateException\");");
        this.writer.println("           }");
        this.writer.println("               this.androidEvent.setToState(this.currentState);");
        this.writer.println("           if(detectCrashState(androidEvent)){");
        this.writer.println("               clearStateGraphVisited();");
        this.writer.println("               this.androidCrawler.restartApp();");
        this.writer.println("               assertFalse(true);");
        this.writer.println("           }");
        this.writer.println("           if(this.androidEvent.getReportLabel().contains(\"scroll\") && this.androidEvent.getReportLabel().contains(\"Up\")){");
        this.writer.println("               this.tag = true;");
        this.writer.println("               EventData eventData = this.androidEvent.getEventData();");
        this.writer.println("               this.bounds = eventData.getBounds();");
        this.writer.println("           }");
        this.writer.println("           detectCurrentStateTriggerSystemEvent();");
        this.writer.println("       }");
        this.writer.println("    }");
    }

    private void addScrollDown(){
        this.writer.println("");
        this.writer.println("   private void scrollerDown() throws ExecuteCommandErrorException, InterruptedException, IOException {");
        this.writer.println("       AndroidEvent swipeEvent;");
        this.writer.println("       String newBounds = this.bounds;");
        this.writer.println("       EventData eventData = new EventData(newBounds, \"scroll down\");");
        this.writer.println("       eventData.setSwipeDirection(SwipeEvent.Direction.DOWN);");
        this.writer.println("       swipeEvent = new SwipeEvent(eventData, SwipeEvent.Type.SCROLL);");
        this.writer.println("       swipeEvent.executeOn(device);");
        this.writer.println("    }");
    }

    private void addCreateEditTextEvents(){
        this.writer.println("");
        this.writer.println("    private List<EventData> createStateEditText(GUIState state){");
        this.writer.println("        Document document = state.contentClone();");
        this.writer.println("        List nodes = document.getRootElement().selectNodes(\"//node\");");
        this.writer.println("        List<EventData> eventDatas = new ArrayList<>();");
        this.writer.println("        for (Object node : nodes) {");
        this.writer.println("            Element element = (Element) node;");
        this.writer.println("            if(element.attribute(NodeAttribute.Class).getText().equals(NodeAttribute.EditText)) {");
        this.writer.println("                EventData eventData = new EventData(element);");
        this.writer.println("                eventDatas.add(eventData);");
        this.writer.println("            }");
        this.writer.println("        }");
        this.writer.println("        return eventDatas;");
        this.writer.println("    }");
    }

    private void addClearStateGraphVisited(){
        this.writer.println("");
        this.writer.println("    private void clearStateGraphVisited(){");
        this.writer.println("        for(GUIState state : this.stateGraph.getAllStates())");
        this.writer.println("            state.setVisited(false);");
        this.writer.println("    }");
    }
}
