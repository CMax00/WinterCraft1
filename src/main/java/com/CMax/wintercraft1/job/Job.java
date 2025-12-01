package com.CMax.wintercraft1.job;

public enum Job {
    NONE,
    FARMER,
    FISCHER,
    SCHMIED,
    EXPLORER,
    HOLZHACKER,
    MINER,
    ALCHEMIST,
    MAGIER;

    public static Job fromString(String s) {
        try {
            return Job.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
