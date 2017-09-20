package edu.ntut.selab.util;

import edu.ntut.selab.XMLReader;

public class Config {
    public static final String ADB_PATH = XMLReader.getConfigurationValue("adb");
    public static final String GRAPHVIZ_LAYOUT_PATH = XMLReader.getConfigurationValue("graphvizLayout");
    public static final String PACKAGE_NAME = XMLReader.getConfigurationValue("packageName");
    public static final String LAUNCHABLE_ACTIVITY = XMLReader.getConfigurationValue("launchableActivity");
    public static final double EVENT_SLEEP_TIMESECOND = Double.valueOf(XMLReader.getConfigurationValue("eventSleepTimeSecond"));
    public static final int START_APP_SLEEP_TIMESECOND = Integer.valueOf(XMLReader.getConfigurationValue("startAppSleepTimeSecond"));
    public static final int CLOSE_APP_SLEEP_TIMESECOND = Integer.valueOf(XMLReader.getConfigurationValue("closeAppSleepTimeSecond"));
    public static final int CROSS_APP_EVENT_THRESHHOLD = Integer.valueOf(XMLReader.getConfigurationValue("crossAppEventThreshold"));
    public static final int LIST_GRID_SIZE_THRESHOLD = Integer.valueOf(XMLReader.getConfigurationValue("listGridSizeThreshold"));
    public static final int CONTENT_SENSITIVE_LIST_GRID_SIZE_THRESHOLD = Integer.valueOf(XMLReader.getConfigurationValue("contentSensitiveListGridSizeThreshold"));
    public static final int HEAD_TAIL_LIST_GRID_SIZE_THRESHOLD = Integer.valueOf(XMLReader.getConfigurationValue("headTailListGridSizeThreshold"));
    public static final int ATTEMPT_COUNT_THRESHOLD = Integer.valueOf(XMLReader.getConfigurationValue("attemptCountThreshold"));
    public static final long TIMEOUT_SECOND = Long.valueOf(XMLReader.getConfigurationValue("timeoutSecond"));
    public static final int MAX_OCCURS_OF_COMPONENT_VALUE = Integer.valueOf(XMLReader.getConfigurationValue("maxOccursOfComponentValue"));
    public static final String CRASH_MESSAGE = XMLReader.getConfigurationValue("crashMessage");
    public static final boolean OUTPUT_LAYOUT_MULTIPLE_TRANSITION_AGGREGATION = isTrue("outputLayoutMultipleTransitionAggregation");
    public static final boolean OUTPUT_LAYOUT_MULTIPLE_SELF_LOOP_AGGREGATION = isTrue("outputLayoutMultipleSelfLoopAggregation");
    public static final boolean BLOCK_NONDETERMINISTIC_EVENT = isTrue("blockNondeterministicEvent");
    public static final String CRAWLING_ALGORITHM = XMLReader.getConfigurationValue("crawlingAlgorithm");
    public static final boolean DISPLAY_EVENT_EXCUTION_ORDER = isTrue("displayEventExcutionOrder");
    public static final boolean ENABLE_MENUKEY_EVENT = isTrue("enableMenukeyEvent");
    public static final boolean ENABLE_BACKKEY_EVENT = isTrue("enableBackkeyEvent");
    public static final boolean TEXTVIEW_CLICKABLE = isTrue("textViewClickable");
    public static final boolean IMAGEVIEW_CLICKABLE = isTrue("imageViewClickable");
    public static final boolean SCROLL_EVERYVIEW = isTrue("scrollEveryView");
    public static final boolean IGNORE_BOUNDS_ATTRIBUTE = isTrue("ignoreBoundsAttribute");
    public static final boolean WAIT_FOR_PROGRESS_BAR = isTrue("waitForProgressBar");
    public static final long WAIT_FOR_PROGRESS_BAR_TIMESECOND = Integer.valueOf(XMLReader.getConfigurationValue("waitForProgressBarTimeSec"));
    public static final String DEV_KILL_APP_KEYCODE = XMLReader.getConfigurationValue("deviceKillAppKeyCode");
    public static final String KILL_APP_KEYCODE = XMLReader.getConfigurationValue(DEV_KILL_APP_KEYCODE);
    public static final boolean APP_INSTRUMENTED = isTrue("appInstrumented");
    public static final String EQUIVALENT_LEVEL = XMLReader.getConfigurationValue("equivalentLevel");
    public static final boolean IMPORT_SERIALIZATION_FILE = isTrue("importSerializationFile");
    public static final long AT_THE_SAME_STATE_TIMEOUT = Long.valueOf(XMLReader.getConfigurationValue("atTheSameStateTimeoutSecond"));
    public static final long CROSS_APP_STATE_TIMEOUT = Long.valueOf(XMLReader.getConfigurationValue("crossAppStateTimeoutSecond"));
    public static final boolean INTEGRATE_EDIT_TEXT = isTrue("integrateEditText");
    public static final String SERIALIZATION_DATA_PATH = XMLReader.getConfigurationValue("serializationDataPath");
    public static final boolean IGNORE_NAF = isTrue("ignoreNAF");
    public static final boolean EVENT_ORDER_CUSTOMIZE = isTrue("eventOrderCustomize");
    public static final String GENERATE_TEST_CASE_ALOGORITHM = XMLReader.getConfigurationValue("generateTestCaseAlgorithm");
    public static final boolean GENERATE_NONDETERMINISTIC_PATH = isTrue("generateNondeterministicPath");

    private String serailNum = null;

    private static boolean isTrue(String config) throws IllegalArgumentException {
        if(XMLReader.getConfigurationValue(config).equals("true"))
            return true;
        else if(XMLReader.getConfigurationValue(config).equals("false"))
            return false;
        throw new IllegalArgumentException();
    }

    public final String getDeviceSerialNum() {
        if(this.serailNum == null) {
            this.serailNum = String.valueOf(XMLReader.getConfigurationValue("deviceSerialNumber"));
        }
        return this.serailNum;
    }
}
