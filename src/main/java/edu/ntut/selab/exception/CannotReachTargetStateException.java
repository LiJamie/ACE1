package edu.ntut.selab.exception;

import edu.ntut.selab.event.AndroidEvent;

public class CannotReachTargetStateException extends Exception {
    private AndroidEvent nondeterministicEvent;

    public CannotReachTargetStateException(AndroidEvent event) {
        this.nondeterministicEvent = event;
    }

    public AndroidEvent getNondeterministicEvent() {
        return this.nondeterministicEvent;
    }
}
