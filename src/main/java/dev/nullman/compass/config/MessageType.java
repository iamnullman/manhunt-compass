package dev.nullman.compass.config;

public enum MessageType {
    ACTION_BAR,
    CHAT;

    public static MessageType fromString(String value) {
        if (value == null) {
            return ACTION_BAR;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return ACTION_BAR;
        }
    }
}
