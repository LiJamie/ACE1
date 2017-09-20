package edu.ntut.selab.iterator;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.exception.*;
import org.dom4j.DocumentException;
import edu.ntut.selab.event.AndroidEvent;
import java.io.IOException;
import java.util.List;

public interface Iterator {
    void first() throws IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException, EventFromStateErrorException, CannotReachTargetStateException;
    boolean isDone();
    void  next() throws IndexOutOfBoundsException, IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException;
    List<AndroidEvent> getEventSequence();
}
