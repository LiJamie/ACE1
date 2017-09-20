package edu.ntut.selab;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommandHelper {

    public CommandHelper() {
        // TODO Auto-generated constructor stub
    }

    public static void executeCommand(String[] command) throws IOException {
//		getCommandValue(command);
        executeAndGetFeedBack(command);
    }

//  deprecated
//	protected static String getCommandValue (String[] command) {
//		final String space = " ";
//		String commandValue = "";
//		for(int i = 0 ; i<command.length ; i++) {
//			commandValue += command[i];
//			if(i<command.length-1) {
//				commandValue += space;
//			}
//		}
//		return commandValue;
//	}

    public static String executeAndGetFeedBack(String[] command) throws IOException {
        String feedBack = null;
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        feedBack = bufferedReader.readLine();
        process.destroy();
        return feedBack;
    }

    public static List<String> executeCmd(String... cmd) throws IOException, InterruptedException, ExecuteCommandErrorException {
        ProcessBuilder proc = new ProcessBuilder(cmd);
//		proc.environment().put("ANDROID_HOME", CoreOptions.ANDROID_HOME);
        Process p = proc.start();
        p.waitFor();
        List<String> output = parseResult(p.getInputStream());

//        // error handler
//        List<String> errOutput = parseResult(p.getErrorStream());
//        if (!errOutput.isEmpty())
//            throw new ExecuteCommandErrorException(errOutput.toString());
        return output;
    }

    private static List<String> parseResult(InputStream is) throws IOException {
        List<String> result = new ArrayList<>();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bReader = new BufferedReader(reader);
        String line = null;
        while ((line = bReader.readLine())!=null){
            result.add(line);
        }
        return result;
    }
}
