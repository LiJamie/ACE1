package edu.ntut.selab;

import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.data.NodeAttribute;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.event.*;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import edu.ntut.selab.util.XMLEventParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static edu.ntut.selab.TimeHelper.sleep;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class XMLReader {
    protected int guiIndex;
    protected Document document;
    protected CommandHelper commandHelper;
    //    protected String reportPath;
    protected String guiPagesPath; // put gui page file folder in reportPath
    protected String timeStamp;
    protected final String SLASH = "/";
    protected final String adbPath = Config.ADB_PATH;
    protected String filePackageName = "";
    protected String crashMessage = "";
    private Device device;
    private final String DOT_DIR = "Dot";
    private final String REPORT_PATH = System.getProperty("user.dir") + SLASH + Utility.getReportPath();
    private static Map<String, String> configMap = new HashMap<>();
    private boolean detectProgressBarFirstTime = false;
    private long firstDetectProgressBarT0;

    public XMLReader(Device device) throws FileNotFoundException {
        this.device = device;
        guiIndex = 0;
        document = null;
        commandHelper = null;
        timeStamp = Utility.getTimestamp();
        filePackageName = Utility.getCreateFilePackageName();
        guiPagesPath = new String("gui_pages" + SLASH + timeStamp + "_" + filePackageName + "_" + Config.CRAWLING_ALGORITHM + "/States");

        crashMessage = Config.CRASH_MESSAGE;

        this.createReportPath();
        this.createDotPath();
        this.createASDPath();
        this.createStateFolder();
    }

    private void createReportPath() throws FileNotFoundException {
        this.createPath(REPORT_PATH);
    }

    // store .dot file
    private void createDotPath() throws FileNotFoundException {
        final String DOT_PATH = REPORT_PATH + SLASH + DOT_DIR;
        this.createPath(DOT_PATH);
    }

    // store ASD file
    private void createASDPath() throws FileNotFoundException {
        final String ASD_PATH = REPORT_PATH + SLASH + "ActivitySubstateDiagram";
        this.createPath(ASD_PATH);
    }

    // create state folder : put gui page file folder
    private void createStateFolder() throws FileNotFoundException {
        final String STATE_FOLDER = REPORT_PATH + SLASH + "States";
        this.createPath(STATE_FOLDER);
    }

    private void createPath(String path) throws FileNotFoundException {
        File file = new File(path);
        file.mkdirs();
        if (!file.exists())
            throw new FileNotFoundException("create path error : " + path);
    }

    public static String getConfigurationValue(String elementName) {
        return getConfigMap().get(elementName);
    }

    public static Map<String, String> getConfigMap() {
        if (configMap.isEmpty()) {
            readConfig();
        }
        return configMap;
    }

    private static void readConfig() {
        File pathConfigFile;
        Element element, rootElement = null;
        final String CONFIG_DIR_PATH = "configuration/";
        File folderConfig = new File(CONFIG_DIR_PATH);
        String[] listConfig = folderConfig.list();
        int cntConfigList = 0;
        while (cntConfigList < listConfig.length) {
            String CONFIG_FILE_PATH = listConfig[cntConfigList];
            pathConfigFile = new File(CONFIG_DIR_PATH + CONFIG_FILE_PATH);
            try {
                Document document = (new SAXReader()).read(pathConfigFile);
                rootElement = (Element) document.getRootElement().clone();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < rootElement.elements().size(); i++) {
                element = (Element) rootElement.elements().get(i);
                configMap.put(element.getName(), element.getText());
            }
            cntConfigList++;
        }
    }

	/*
    public static ElementAndSiblings getNextElementAndSiblings (Element element, ArrayList<List<?>> siblingElements) throws ArithmeticException {
		List<?> elements = null;
		int siblingSize = 0, elementsSize = 0;
		if(element.elementIterator().hasNext()) {
			if(element.elements().size()>1) {
				siblingElements.add(element.elements());
			}
			element = (Element)element.elementIterator().next();
		}
		else if(siblingElements.size()>0) {
			siblingSize = siblingElements.size();
			siblingElements.get(siblingSize-1).remove(0);
			elementsSize = siblingElements.get(siblingSize-1).size();
			elements = siblingElements.get(siblingSize-1);
			element = (Element)elements.get(0);
			if(elementsSize==1) {
				siblingElements.remove(siblingSize-1);
			}
		}
		else {
			//Should be the end of XML
			throw new ArithmeticException();
		}
		return new ElementAndSiblings(element, siblingElements);
	}
	*/

    /*
     * read screen xml
     */
    public GUIState read() throws DocumentException, IOException, InterruptedException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException {
        int crossAppDepthThreshold = Config.CROSS_APP_EVENT_THRESHHOLD;
        getXML();
        getScreenPic();
        GUIState resultState = parseXML();
        // if resultState have err msg, set activity as CrashActivity
        if (resultState.isCrashState())
            resultState.clearEvents();
        resultState.setActivityName(this.device.getActivityName());
        if (!resultState.getPackageName().equals(Config.PACKAGE_NAME))
            resultState.setCrossAppDepth(crossAppDepthThreshold);

        return resultState;
    }

    public void guiIndexMinor() {
        guiIndex--;
    }

    public void guiIndexPlus() {
        guiIndex++;
    }

    public void setStateId(GUIState state) {
        state.setId(guiIndex);
    }

    public boolean isCrashState(Element e) {
        String[] message = crashMessage.split(" ");
        if (e.attributeValue(NodeAttribute.Text) != null &&
                e.attributeValue(NodeAttribute.Text).contains(message[0]) &&
                e.attributeValue(NodeAttribute.Text).contains(message[1]))
            return true;
        for (int i = 0; i < e.elements().size(); i++) {
            if (isCrashState((Element) e.elements().get(i)))
                return true;
        }
        return false;
    }

    protected void downloadXML() {
        String[] dumpUIXMLCmd = {adbPath, "shell",
                "uiautomator", "dump",
                "/data/local/tmp/" + guiIndex + ".xml"},
                downloadUIXMLCmd = {adbPath, "pull",
                        "/data/local/tmp/" + guiIndex + ".xml",
                        guiPagesPath},
                removeUIXMLInDevice = {adbPath, "shell",
                        "rm", "/data/local/tmp/" + guiIndex + ".xml"},
                screenshotCmd = {adbPath, "shell",
                        "screencap", "-p", "/data/local/tmp/" +
                        guiIndex + ".png"},
                downloadScreenshotCmd = {adbPath, "pull",
                        "/data/local/tmp/" + guiIndex + ".png",
                        guiPagesPath},
                removeScreenshotInDevice = {adbPath, "shell",
                        "rm", "/data/local/tmp/" + guiIndex + ".png"};
        new File(
                System.getProperty("user.dir") +
                        SLASH + guiPagesPath).mkdirs();
        try {
            CommandHelper.executeCommand(dumpUIXMLCmd);
            CommandHelper.executeCommand(screenshotCmd);
            CommandHelper.executeCommand(downloadUIXMLCmd);
            CommandHelper.executeCommand(downloadScreenshotCmd);
            CommandHelper.executeCommand(removeUIXMLInDevice);
            CommandHelper.executeCommand(removeScreenshotInDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected GUIState parseXML() throws DocumentException, ClickTypeErrorException {
        final String dotXML = ".xml";
        ArrayList<AndroidEvent> androidEventList =
                new ArrayList<AndroidEvent>();
        SAXReader reader = new SAXReader();
        Element element = null;
        document = reader.read(guiPagesPath + SLASH + guiIndex + dotXML); // read xml and get document
        //root: <hierarchy
        element = document.getRootElement().createCopy(); // get document root node
        element = (Element) element.elementIterator().next();
        XMLEventParser eventParser = new XMLEventParser(document);
        androidEventList = (ArrayList<AndroidEvent>) eventParser.parseEvents();

        return new GUIState(document, androidEventList);
    }

    public void getXML() throws IOException, InterruptedException, ExecuteCommandErrorException {

        new File(System.getProperty("user.dir") + SLASH + guiPagesPath).mkdirs();
        this.dumpUIXML();
        this.downloadUIXML();
        this.removeUIXMLInDevice();
    }

    public static boolean filterFileContent(String filePath, String filterData) throws IOException {
        String filterContent;
        File filterFile = new File(filePath);
        FileReader fr = new FileReader(filterFile);
        BufferedReader br = new BufferedReader(fr);
        Pattern pattern = Pattern.compile(filterData);
        while ((filterContent = br.readLine()) != null) {
            if (pattern.matcher(filterContent).find()) {
                return true;
            }
        }
        return false;
    }

    private void dumpUIXML() throws IOException, InterruptedException, ExecuteCommandErrorException {
        String[] dumpUIXMLCmd = {adbPath, "shell",
                "uiautomator", "dump",
                "/data/local/tmp/" + guiIndex + ".xml"};

        List<String> result = CommandHelper.executeCmd(dumpUIXMLCmd);
        assertFalse("result size = " + result.size(), result.isEmpty());

        while (result.get(0).contains("ERROR: null root node returned by UiTestAutomationBridge.")) {
            result = CommandHelper.executeCmd(dumpUIXMLCmd);
        }
        assertTrue("result.get(0) = " + result.get(0), result.get(0).contains("UI hierchary dumped to: "));
    }

    private void downloadUIXML() throws IOException, InterruptedException, ExecuteCommandErrorException {
        String[] downloadUIXMLCmd = {adbPath, "pull",
                "/data/local/tmp/" + guiIndex + ".xml",
                guiPagesPath};

        List<String> result = CommandHelper.executeCmd(downloadUIXMLCmd);
//        assertTrue("result.get(0) = " + result.get(0), result.get(0).contains("[100%]"));
        assertTrue(new File(this.guiPagesPath + "/" + guiIndex + ".xml").exists());
        if (Config.WAIT_FOR_PROGRESS_BAR) {
            checkProgressBarExist();
        }
    }

    private void checkProgressBarExist() throws IOException, InterruptedException, ExecuteCommandErrorException {
        long detectProgressbarTn;
        boolean progressBarExist = false;
        String guiXmlPath = this.guiPagesPath + "/" + guiIndex + ".xml";
        String filterContent = "android.widget.ProgressBar";
        if (filterFileContent(guiXmlPath, filterContent)) {
            progressBarExist = true;
        }

        if (progressBarExist) {
            if ((!detectProgressBarFirstTime)) {
                detectProgressBarFirstTime = true;
                firstDetectProgressBarT0 = System.currentTimeMillis();
            } else {
                detectProgressbarTn = System.currentTimeMillis();
                long timeElapsed = (detectProgressbarTn - firstDetectProgressBarT0) / 1000;
                if (timeElapsed > Config.WAIT_FOR_PROGRESS_BAR_TIMESECOND)
                    throw new InterruptedException();
            }
            File delGuiXmlFile;
            delGuiXmlFile = new File(guiXmlPath.toString());
            delGuiXmlFile.delete();
            sleep(1000);
            this.dumpUIXML();
            this.downloadUIXML();
        }
    }

    private void removeUIXMLInDevice() throws IOException, InterruptedException, ExecuteCommandErrorException {
        String[] removeUIXMLInDeviceCmd = {adbPath, "shell",
                "rm", "/data/local/tmp/" + guiIndex + ".xml"};
        List<String> result = CommandHelper.executeCmd(removeUIXMLInDeviceCmd);
        assertTrue(result.isEmpty());
    }

    public void getScreenPic() throws IOException {
        String[] screenshotCmd = {adbPath, "shell",
                "screencap", "-p", "/data/local/tmp/" +
                guiIndex + ".png"},
                downloadScreenshotCmd = {adbPath, "pull",
                        "/data/local/tmp/" + guiIndex + ".png",
                        guiPagesPath},
                removeScreenshotInDevice = {adbPath, "shell",
                        "rm", "/data/local/tmp/" + guiIndex + ".png"};

        new File(System.getProperty("user.dir") + SLASH + guiPagesPath).mkdirs();
        CommandHelper.executeCommand(screenshotCmd);
        CommandHelper.executeCommand(downloadScreenshotCmd);
        CommandHelper.executeCommand(removeScreenshotInDevice);
    }

//    protectget

	/*
     * get state package name
	 */
    /*
    protected String getPackageName(GUIState state) {
		Document content = state.contentClone();
		Element element = content.getRootElement();
		ArrayList<List<?>> siblingElements = new ArrayList<List<?>>();
		element = XMLReader.getNextElementAndSiblings(element, siblingElements).element;
		return element.attribute(NodeAttribute.Package).getText();
	}
	*/

    /*
     * get state package name
     */
    public String getPackageName(Element e) {
        if (e.attribute(NodeAttribute.Package) != null)
            return e.attribute(NodeAttribute.Package).getText();
        for (int i = 0; i < e.elements().size(); i++) {
            return getPackageName((Element) e.elements().get(i));
        }
        return "This state don't have package name";
    }

    public String getTimeStampClone() {
        return new String(timeStamp);
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
