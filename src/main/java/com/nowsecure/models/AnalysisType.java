package com.nowsecure.models;

public enum AnalysisType {
    STATIC,
    FULL;

    public static AnalysisType from(String type) {
        switch (type.toLowerCase()) {
            case "static":
                return STATIC;
            case "full":
                return FULL;
            default:
                throw new IllegalArgumentException("AnalysisType must be 'static' or 'full'");
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
