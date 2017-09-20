package edu.ntut.selab.equivalent;

import edu.ntut.selab.util.Config;

public class EquivalentStateStrategyFactory {
    public EquivalentStateStrategy createStrategy() {
        final String config = Config.EQUIVALENT_LEVEL.toLowerCase();
        if (config.equals("exactlyequivalentstrategy"))
            return new ExactlyEquivalentStrategy();
        else if (config.equals("level1"))
            return new CompositeLV01OR();
        else if(config.equals("activitynameequivalentstrategy"))
            return new ActivityEquivalentStrategy();
        else
            return null;
    }
}
