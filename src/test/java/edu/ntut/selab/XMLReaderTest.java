package edu.ntut.selab;

import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class XMLReaderTest {
    private XMLReader xmlReader;
    private Device device;
    private final String CONFIG_FILE_PATH = "test_gui_pages/0.xml";
    private final String SLASH = "/";
    private final String REPORT_PATH = System.getProperty("user.dir") + SLASH + Utility.getReportPath();

    @Before
    public void setup() throws FileNotFoundException {
        Config config = new Config();
        this.device = new Device(config.getDeviceSerialNum());
        this.xmlReader = new XMLReader(this.device);
    }

    @Test
    public void testCreatePath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String path = "test_create_path";
        Method method = XMLReader.class.getDeclaredMethod("createPath", String.class);
        method.setAccessible(true);
        method.invoke(this.xmlReader, path);
        File file = new File(path);
        assertTrue(file.exists());
        file.delete();
        assertFalse(file.exists());
    }

    @Test
    public void testCreateReportPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = XMLReader.class.getDeclaredMethod("createReportPath");
        method.setAccessible(true);
        method.invoke(this.xmlReader);
        File file = new File(REPORT_PATH);
        assertTrue(file.exists());
    }

    @Test
    public void testCreateDotPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String DOT_PATH = REPORT_PATH + SLASH + "Dot";
        Method method = XMLReader.class.getDeclaredMethod("createDotPath");
        method.setAccessible(true);
        method.invoke(this.xmlReader);
        File file = new File(DOT_PATH);
        assertTrue(file.exists());
    }

    @Test
    public void testCreateASDPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String ASD_PATH = REPORT_PATH + SLASH + "ActivitySubstateDiagram";
        Method method = XMLReader.class.getDeclaredMethod("createASDPath");
        method.setAccessible(true);
        method.invoke(this.xmlReader);
        File file = new File(ASD_PATH);
        assertTrue(file.exists());
    }

    @Test
    public void testCreateStateFolder() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String STATE_FOLDER = REPORT_PATH + SLASH + "States";
        Method method = XMLReader.class.getDeclaredMethod("createStateFolder");
        method.setAccessible(true);
        method.invoke(this.xmlReader);
        File file = new File(STATE_FOLDER);
        assertTrue(file.exists());
    }

    @After
    public void teardown() {
        File reportPath = new File(REPORT_PATH);
        this.deleteFilesInDirectory(reportPath);
        reportPath.delete();
        assertFalse(reportPath.exists());
    }

    private void deleteFilesInDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                this.deleteFilesInDirectory(file);
            file.delete();
        }
    }
}
