package edu.ntut.selab.iterator;


import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.exception.*;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;

public interface TestCaseIterator {
    void first() throws IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException;
    boolean isDone() throws NullPackageNameException;
    void  next() throws IndexOutOfBoundsException, IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException;
    List<AndroidEvent> getEventSequence();
    boolean checkThisPathFinished() throws NullPackageNameException;
}
