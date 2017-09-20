package edu.ntut.selab.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.XMLReader;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;

public class LogGenerator {
    //    List<String> logList = null;
    List<String> errorMessageList = null;
    protected String xmlReaderTimestamp = null;
    List<GUIState> guiStateList = null;
    String filePackageName = "";
    long executeTime = 0;
    int restartCount = 0;
    protected int totalFireEventCount = 0;
    protected int totalDistanceEquivalentStateCount = 0;
    protected int totalListGridEquivalentStateCount = 0;
    private int nafCount, nafStateCount, totalNafEquivalentCounter, totalCompareStateNotNAFButSelfNAFCounter;

    public LogGenerator(AndroidCrawler androidCrawler, StateGraph stateGraph) {
        this.xmlReaderTimestamp = androidCrawler.getStartTime();
        this.guiStateList = stateGraph.getAllStates();
//        this.logList = stateGraph.getLogs();
        this.executeTime = System.currentTimeMillis() - Long.parseLong(this.xmlReaderTimestamp);
        this.errorMessageList = stateGraph.getLoggerStore().getErrorList();
        this.restartCount = androidCrawler.getRestartCount();
        this.totalDistanceEquivalentStateCount = stateGraph.getTotalDistanceEquivalentStateCount();
        this.totalFireEventCount = stateGraph.getTotalExecutedEventCount();
        this.totalListGridEquivalentStateCount = stateGraph.getTotalListGridEquivalentStateCount();
        this.filePackageName = getCreateFilePackageName();
        this.nafCount = androidCrawler.getNafCount();
        this.nafStateCount = androidCrawler.getNafStateCount();
        this.totalNafEquivalentCounter = this.getTotalNafEquivalentCounter(stateGraph);
        this.totalCompareStateNotNAFButSelfNAFCounter = this.getTotalCompareStateNotNAFButSelfNAFCounter(stateGraph);
    }


    public void generateLog() throws NullPackageNameException {
        File file = new File(Utility.getReportPath() + "/Log.txt");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        int stateCount = this.guiStateList.size();
        int edgeCount = calculateTotalEdgeCount(); // ���]�tnondeterministic event
        int nonDeterministicEdge = calculateNondeterministicEvent(); // �u�p��nondeterministic event
        int totalEquivalentStateCount = calculateTotalEquivalentStateCount();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            String packageName = Config.PACKAGE_NAME;
            String launchableActivityName = Config.LAUNCHABLE_ACTIVITY;

            bw.write("Package Name is: " + packageName);
            bw.newLine();
            bw.write("Launchable Activity Name is: " + launchableActivityName);
            bw.newLine();
            bw.write("Total execute time is: " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executeTime),
                    TimeUnit.MILLISECONDS.toSeconds(executeTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executeTime))
            ));
            bw.newLine();
            bw.write("Restart count is: " + restartCount);
            bw.newLine();
            bw.write("Total state count is: " + stateCount);
            bw.newLine();
            bw.write("Total equivalent state count is: " + totalEquivalentStateCount);
            bw.newLine();
            bw.write("Total edge count is: " + edgeCount);
            bw.newLine();
            bw.write("Total nondeterministic edge count is: " + nonDeterministicEdge);
            bw.newLine();
            bw.write("Total distance equivalent state count is: " + totalDistanceEquivalentStateCount);
            bw.newLine();
            bw.write("Total list&grid equivalent state count is: " + totalListGridEquivalentStateCount);
            bw.newLine();
            bw.write("Total fire event count is: " + totalFireEventCount);
            bw.newLine();
            bw.write("Total discarded event count is: " + calculateTotalDiscardedEvent());
            bw.newLine();
            bw.write("Total crash count is: " + calculateTotalCrashCount());
            bw.newLine();
            bw.write("Total cross app state count is: " + calculateTotalCrossApp());
            bw.newLine();
            bw.write("Total naf state count is: " + this.nafStateCount);
            bw.newLine();
            bw.write("Total naf count is: " + this.nafCount);
            bw.newLine();
            bw.write("Total naf equivalent node count is: " + this.totalNafEquivalentCounter);
            bw.newLine();
            bw.write("Total first is naf but second not is: " + this.totalCompareStateNotNAFButSelfNAFCounter);
            bw.newLine();

            this.logConfig(bw);

            bw.newLine();
            bw.write("*************************Exception****************************");
            bw.newLine();
            for (int i = 0; i < errorMessageList.size(); i++) {
                bw.write(errorMessageList.get(i));
                bw.newLine();
            }
            bw.write("**************************************************************");
            bw.newLine();

//            for (int i = 0; i < logList.size(); i++) {
//                bw.write(logList.get(i));
//
//                bw.newLine();
//            }

            bw.newLine();
            bw.write("State: ");
            bw.newLine();
            for (GUIState s : guiStateList) {
                bw.write("state" + s.getId() + ":");
                bw.newLine();
                for (AndroidEvent e : s.getEvents()) {
                    bw.write("event" + e.getReportLabel());
                    bw.write("-->");
                    if (e.getToState() != null)
                        bw.write("state" + e.getToState().getId());
                    else
                        bw.write("null");

                    bw.newLine();
                }
            }
            bw.flush();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void logConfig(BufferedWriter bw) throws IOException {
        List<String> ignoreConfig = new ArrayList<>();
        ignoreConfig.add("adb");
        ignoreConfig.add("graphvizLayout");
        ignoreConfig.add("monkeyRunner");
        ignoreConfig.add("crashMessage");
        ignoreConfig.add("packageName");
        ignoreConfig.add("launchableActivity");

        bw.newLine();
        bw.write("*************************Crawler Configuration****************************");
        bw.newLine();
        Map<String, String> configMap = XMLReader.getConfigMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            if (ignoreConfig.contains(entry.getKey()))
                continue;
            bw.write(entry.getKey() + " : " + entry.getValue());
            bw.newLine();
        }
//        bw.write("Event sleep time in second: " + XMLReader.getConfigurationValue("eventSleepTimeSecond"));
//        bw.newLine();
//        bw.write("Start app sleep time in second: " + XMLReader.getConfigurationValue("startAppSleepTimeSecond"));
//        bw.newLine();
//        bw.write("Close app sleep time in second: " + XMLReader.getConfigurationValue("closeAppSleepTimeSecond"));
//        bw.newLine();
//        bw.write("Cross app event threshold: " + XMLReader.getConfigurationValue("crossAppEventThreshold"));
//        bw.newLine();
//        bw.write("ListView&GridView size threshold: " + XMLReader.getConfigurationValue("listGridSizeThreshold"));
//        bw.newLine();
//        bw.write("Event's attemptCount threshold: " + XMLReader.getConfigurationValue("attemptCountThreshold"));
//        bw.newLine();
//        bw.write("Timeout in second: " + XMLReader.getConfigurationValue("timeoutSecond"));
//        bw.newLine();
//        bw.write("Max occurs of component value: " + XMLReader.getConfigurationValue("maxOccursOfComponentValue"));
//        bw.newLine();
//        bw.write("Crash message: " + XMLReader.getConfigurationValue("crashMessage"));
//        bw.newLine();
//        bw.write("Output layout multiple transition aggregation: " + XMLReader.getConfigurationValue("outputLayoutMultipleTransitionAggregation"));
//        bw.newLine();
//        bw.write("Output layout multiple selfloop aggregation: " + XMLReader.getConfigurationValue("outputLayoutMultipleSelfLoopAggregation"));
//        bw.newLine();
//        bw.write("Block non-deterministic event: " + XMLReader.getConfigurationValue("blockNondeterministicEvent"));
//        bw.newLine();
        bw.write("**************************************************************************");
        bw.newLine();
    }

    //�p��Dnondeterministic event�ƶq
    protected int calculateTotalEdgeCount() {
        int edgeCount = 0;
        for (GUIState s : guiStateList) {
            for (AndroidEvent e : s.getEvents()) {
                if (e.getToState() != null)
                    if (!e.isNonDeterministic())
                        edgeCount++;
            }
        }
        return edgeCount;
    }

    //�p��nondeterministic event�ƶq
    protected int calculateNondeterministicEvent() {
        int edgeCount = 0;
        for (GUIState s : guiStateList) {
            for (AndroidEvent e : s.getEvents()) {
                if (e.getToState() != null)
                    if (e.isNonDeterministic())
                        edgeCount++;
            }
        }
        return edgeCount;
    }

    protected int calculateTotalEquivalentStateCount() {
        int totalCount = 0;
        for (GUIState s : guiStateList) {
            if (s.isEquivalentState())
                totalCount++;
        }
        return totalCount;
    }

    protected int calculateTotalDiscardedEvent() {
        int totalDiscarded = 0;
        int threshold = Config.ATTEMPT_COUNT_THRESHOLD;
        for (GUIState s : guiStateList) {
            for (AndroidEvent e : s.getEvents()) {
                if (e.getAttemptCount() >= threshold)
                    totalDiscarded++;
            }
        }
        return totalDiscarded;
    }

    /*
     * �p�⦳�X��transition�����crash state
     */
    protected int calculateTotalCrashCount() {
        int totalCrash = 0;
        for (GUIState s : guiStateList) {
            for (AndroidEvent e : s.getEvents()) {
                if (e.getToState() != null && e.getToState().isCrashState())
                    totalCrash++;
            }
        }
        return totalCrash;
    }

    protected int calculateTotalCrossApp() throws NullPackageNameException {
        int totalCrossAppStateCount = 0;
        String packageName = Config.PACKAGE_NAME;
        for (GUIState s : guiStateList) {
            if (!s.getPackageName().equals(packageName) && !s.getPackageName().contains("launcher")) {
                totalCrossAppStateCount++;
            }
        }
        return totalCrossAppStateCount;
    }

    protected String getCreateFilePackageName() {
        String[] name = Config.PACKAGE_NAME.split("\\.");
        return name[name.length - 1];
    }

    public int getTotalNafEquivalentCounter(StateGraph stateGraph) {
        int count = 0;
        for (GUIState state : stateGraph.getAllStates()) {
            count += state.getNafEquivalentCounter();
        }
        return count;
    }

    public int getTotalCompareStateNotNAFButSelfNAFCounter(StateGraph stateGraph) {
        int count = 0;
        for (GUIState state : stateGraph.getAllStates()) {
            count += state.getCompareStateNotNAFButSelfNAFCounter();
        }
        return count;
    }
}
