package cn.citprobe.ServerOfflineAuth;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private String playerName;
    private String hashedPassword;
    private long registeredTime;
    private String lastIp;
    private String lastDimension;   // 例如 "minecraft:overworld"
    private double lastX, lastY, lastZ;
    private float lastYRot, lastXRot;
    private int gameMode;
    private boolean mayFly;
    private boolean flying;

    // 必须有无参构造（JSON 反序列化）
    public PlayerData() {}

    public PlayerData(UUID uuid, String hashedPassword, long registeredTime, String lastIp) {
        this.uuid = uuid;
        this.hashedPassword = hashedPassword;
        this.registeredTime = registeredTime;
        this.lastIp = lastIp;
        this.gameMode = 0;
        this.mayFly = false;
        this.flying = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public long getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(long registeredTime) {
        this.registeredTime = registeredTime;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public String getLastDimension() {
        return lastDimension;
    }

    public void setLastDimension(String lastDimension) {
        this.lastDimension = lastDimension;
    }

    public double getLastX() {
        return lastX;
    }

    public void setLastX(double lastX) {
        this.lastX = lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    public double getLastZ() {
        return lastZ;
    }

    public void setLastZ(double lastZ) {
        this.lastZ = lastZ;
    }

    public float getLastYRot() {
        return lastYRot;
    }

    public void setLastYRot(float lastYRot) {
        this.lastYRot = lastYRot;
    }

    public float getLastXRot() {
        return lastXRot;
    }

    public void setLastXRot(float lastXRot) {
        this.lastXRot = lastXRot;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public boolean isMayFly() {
        return mayFly;
    }

    public void setMayFly(boolean mayFly) {
        this.mayFly = mayFly;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }
}