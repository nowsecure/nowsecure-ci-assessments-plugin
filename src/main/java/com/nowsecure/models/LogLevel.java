package com.nowsecure.models;

public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public static LogLevel from(String level) {
        switch (level.toLowerCase()) {
            case "debug":
                return DEBUG;
            case "info":
                return INFO;
            case "warn":
                return WARN;
            case "error":
                return ERROR;
            default:
                throw new IllegalArgumentException("LogLevel must be one of [debug, info, warn, error]");
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
