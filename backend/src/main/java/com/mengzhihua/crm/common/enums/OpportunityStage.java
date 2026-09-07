package com.mengzhihua.crm.common.enums;

public enum OpportunityStage {
    QUALIFICATION(10),
    NEEDS_ANALYSIS(20),
    PROPOSAL(50),
    NEGOTIATION(75),
    CLOSED_WON(100),
    CLOSED_LOST(0);

    private final int defaultProbability;

    OpportunityStage(int defaultProbability) {
        this.defaultProbability = defaultProbability;
    }

    public int getDefaultProbability() {
        return defaultProbability;
    }
}
