package dev.nullman.compass.config;

import com.google.gson.annotations.SerializedName;

/**
 * Gson-serializable config model. Field names match keys in {@code config/manhunt-compass.json}
 * for straightforward Cloth Config / ModMenu integration later.
 */
public class ModConfig {
    @SerializedName("compassName")
    private String compassName = "§6Manhunt Pusula";

    @SerializedName("messageType")
    private String messageType = "ACTION_BAR";

    @SerializedName("blockReach")
    private boolean blockReach = true;

    @SerializedName("updateInterval")
    private int updateInterval = 20;

    public void sanitize() {
        if (compassName == null || compassName.isBlank()) {
            compassName = "§6Manhunt Pusula";
        }
        if (updateInterval < 1) {
            updateInterval = 1;
        }
        if (messageType == null || messageType.isBlank()) {
            messageType = "ACTION_BAR";
        }
    }

    public String getCompassName() {
        return compassName;
    }

    public void setCompassName(String compassName) {
        this.compassName = compassName;
    }

    public MessageType getMessageType() {
        return MessageType.fromString(messageType);
    }

    public String getMessageTypeRaw() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isBlockReach() {
        return blockReach;
    }

    public void setBlockReach(boolean blockReach) {
        this.blockReach = blockReach;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }
}
