package edu.ntut.selab.util;

import edu.ntut.selab.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utility {
    private static String timestamp;

    public static String getTimestamp() {
        if(timestamp == null)
           timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        return timestamp;
    }

    public static String getReportPath() {
        final String REPORT_PATH = "gui_pages/" + getTimestamp() + "_" + getCreateFilePackageName() + "_" + Config.CRAWLING_ALGORITHM;
        return REPORT_PATH;
    }

    public static String getCreateFilePackageName() {
        String[] name = Config.PACKAGE_NAME.split("\\.");
        return name[name.length - 1];
    }

    public static int getMin(int m, int n) {
        if(m < n)
            return m;
        return n;
    }

    public static void copyFolder(String prevDIRPath, String newDIRPath) throws IOException {
        (new File(newDIRPath)).mkdirs();
        File a = new File(prevDIRPath);
        String[] file=a.list();
        File temp;
        for (int i = 0; i < file.length; i++) {
            if (prevDIRPath.endsWith(File.separator))
                temp = new File(prevDIRPath + file[i]);
            else
                temp = new File(prevDIRPath + File.separator + file[i]);

            if (temp.isFile())
                copyFile(temp.getPath().toString(), newDIRPath + File.separator + temp.getName().toString());
        }
    }

    public static void copyFile(String prevFilePath, String newFilePath) throws IOException {
        FileInputStream inputFile = new FileInputStream(prevFilePath);
        FileOutputStream outputFile = new FileOutputStream(newFilePath);
        byte[] b = new byte[1024 * 5];
        int len;
        while ( (len = inputFile.read(b)) != -1) {
            outputFile.write(b, 0, len);
        }
        outputFile.flush();
        outputFile.close();
        inputFile.close();
    }
}
