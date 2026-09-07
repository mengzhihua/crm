package com.mengzhihua.crm.common.enums;

public enum CasePriority {
    LOW, MEDIUM, HIGH, URGENT;

    public CasePriority next() {
        if (this == LOW) {
            return MEDIUM;
        }
        if (this == MEDIUM) {
            return HIGH;
        }
        if (this == HIGH) {
            return URGENT;
        }
        return URGENT;
    }

    public int slaHours() {
        switch (this) {
            case URGENT:
                return 4;
            case HIGH:
                return 8;
            case MEDIUM:
                return 24;
            default:
                return 72;
        }
    }
}
