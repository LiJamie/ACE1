package edu.ntut.selab.iterator;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.exception.*;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;

public abstract class CrawlingIterator implements Iterator {
    @Override
    public abstract void first() throws IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException, EventFromStateErrorException, CannotReachTargetStateException;

    @Override
    public abstract boolean isDone();

    @Override
    public abstract void next() throws IndexOutOfBoundsException, IOException, InterruptedException, CrawlerControllerInitialErrorException, DocumentException, ExecuteCommandErrorException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException;

    @Override
    public abstract List<AndroidEvent> getEventSequence();
}
