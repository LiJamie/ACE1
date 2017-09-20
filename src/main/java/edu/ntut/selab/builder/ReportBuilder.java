package edu.ntut.selab.builder;

import edu.ntut.selab.CommandHelper;
import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.XMLReader;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import edu.ntut.selab.StateGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public abstract class ReportBuilder {
    protected StateGraph stateGraph  = null;
    protected String timestamp;
    protected String reportPath;
    protected PrintWriter writer;
    protected boolean multipleSelfLoopAggregation = false;
    protected boolean multipleTransitionAggregation = false;
    protected final String DOT_DIR = "/Dot/";
    protected final String DEFAULT_COLOR = "Black";
    protected final String NONDETERMINISTIC_COLOR = "DodgerBlue";
    protected final String TO_EQUIVALENT_STATE_COLOR = "Brown";

    protected ReportBuilder(StateGraph stateGraph) {
        this.timestamp = Utility.getTimestamp();
        this.setStateGraph(stateGraph);
        this.reportPath = Utility.getReportPath();
        this.setMultipleTransitionAggregation();
        this.setSelfLoopAggregation();
    }

    protected abstract void addHeader();

    public abstract void buildDot() throws IOException, InterruptedException, ExecuteCommandErrorException;

    public abstract void buildSVG() throws InterruptedException, ExecuteCommandErrorException, IOException;

    public abstract void buildTxt() throws IOException;

    protected abstract void addFooter();

    public void setStateGraph(StateGraph stateGraph) {
        this.stateGraph = stateGraph;
    }

    private void setMultipleTransitionAggregation() {
        if (Config.OUTPUT_LAYOUT_MULTIPLE_TRANSITION_AGGREGATION)
            multipleTransitionAggregation = true;
        else
            multipleTransitionAggregation = false;
    }

    private void setSelfLoopAggregation() {
        if (Config.OUTPUT_LAYOUT_MULTIPLE_SELF_LOOP_AGGREGATION)
            multipleSelfLoopAggregation = true;
        else
            multipleSelfLoopAggregation = false;
    }

    protected String displayOrder(AndroidEvent event) {
        if (Config.DISPLAY_EVENT_EXCUTION_ORDER) {
            return event.getReportLabel() + ": " + event.getOrder();
        } else {
            return event.getReportLabel();
        }
    }

    protected void createSVGFile(File dotFile, String outputPath) throws IOException, InterruptedException, ExecuteCommandErrorException {
        String dot = Config.GRAPHVIZ_LAYOUT_PATH;
        String dotFilePath = dotFile.getPath();
        System.out.println(dotFilePath);
        String[] cmd = {dot, "-Tsvg", dotFilePath, "-o", outputPath};
        List<String> result = CommandHelper.executeCmd(cmd);
        assertTrue(result.isEmpty());
        assertTrue("targetFile: " + outputPath + " is not exist.", new File(outputPath).exists());
    }

    protected void closeWriter() {
        this.writer.close();
    }

    public List<String> getActivityNames(StateGraph stateGraph) {
        List<String> activityNames = new ArrayList<>();
        for(GUIState guiState : stateGraph.getAllStates()){
            if(!activityNames.contains(guiState.getActivityName())) {
                activityNames.add(guiState.getActivityName());
            }
        }
        return activityNames;
    }
}