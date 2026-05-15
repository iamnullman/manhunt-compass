package dev.nullman.compass.tracking;

import dev.nullman.compass.ManhuntCompassMod;
import dev.nullman.compass.config.ModConfig;
import dev.nullman.compass.item.CompassHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrackingService {
    private final Map<UUID, UUID> currentTargets = new HashMap<>();
    private final Map<UUID, BlockPos> lastTargetBlockPos = new HashMap<>();
    private int tickCounter;

    public void resetTickCounter() {
        tickCounter = 0;
    }

    public void onServerTick(MinecraftServer server) {
        int interval = ManhuntCompassMod.getConfig().getUpdateInterval();
        tickCounter++;
        if (tickCounter < interval) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!CompassHelper.isHoldingManhuntCompass(player)) {
                continue;
            }
            refreshCurrentTargetPosition(player);
            sendDistanceActionBar(player);
        }
    }

    /**
     * Right-click: cycle to the next online player (excluding self).
     */
    public void cycleTarget(ServerPlayerEntity tracker) {
        MinecraftServer server = tracker.getServer();
        if (server == null) {
            return;
        }

        List<ServerPlayerEntity> candidates = getCycleCandidates(tracker, server);
        if (candidates.isEmpty()) {
            tracker.sendMessage(Text.literal("§caktif oyuncu yok"), true);
            return;
        }

        UUID trackerId = tracker.getUuid();
        UUID currentTargetId = currentTargets.get(trackerId);
        int nextIndex = 0;

        if (currentTargetId != null) {
            for (int index = 0; index < candidates.size(); index++) {
                if (candidates.get(index).getUuid().equals(currentTargetId)) {
                    nextIndex = (index + 1) % candidates.size();
                    break;
                }
            }
        }

        ServerPlayerEntity target = candidates.get(nextIndex);
        currentTargets.put(trackerId, target.getUuid());
        lastTargetBlockPos.remove(trackerId);

        CompassHelper.updateCompassTarget(tracker, target);
        lastTargetBlockPos.put(trackerId, target.getBlockPos().toImmutable());

        tracker.sendMessage(
                Text.literal("§eHedef §f" + target.getName().getString()),
                true
        );
    }

    /**
     * Action bar distance text only — never modifies the compass ItemStack.
     */
    private void sendDistanceActionBar(ServerPlayerEntity tracker) {
        UUID targetId = currentTargets.get(tracker.getUuid());
        if (targetId == null) {
            return;
        }

        MinecraftServer server = tracker.getServer();
        if (server == null) {
            return;
        }

        ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetId);
        if (target == null || !target.isAlive()) {
            return;
        }

        String targetName = target.getName().getString();
        ModConfig config = ManhuntCompassMod.getConfig();
        Text message;

        if (target.getWorld() != tracker.getWorld()) {
            message = Text.literal(String.format("§eHedef: §f%s §7(Başka Dünyada)", targetName));
        } else if (config.isBlockReach()) {
            double distance = Math.sqrt(tracker.squaredDistanceTo(target));
            message = Text.literal(String.format("§eHedef: §f%s §7(%.0f blocks)", targetName, distance));
        } else {
            message = Text.literal(String.format("§eHedef: §f%s", targetName));
        }

        tracker.sendMessage(message, true);
    }

    private void refreshCurrentTargetPosition(ServerPlayerEntity tracker) {
        UUID targetId = currentTargets.get(tracker.getUuid());
        if (targetId == null) {
            return;
        }

        MinecraftServer server = tracker.getServer();
        if (server == null) {
            return;
        }

        ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetId);
        if (target == null || !target.isAlive() || target.getWorld() != tracker.getWorld()) {
            return;
        }

        BlockPos currentPos = target.getBlockPos();
        BlockPos lastPos = lastTargetBlockPos.get(tracker.getUuid());
        if (lastPos != null && lastPos.equals(currentPos)) {
            return;
        }

        if (CompassHelper.updateCompassTarget(tracker, target)) {
            lastTargetBlockPos.put(tracker.getUuid(), currentPos.toImmutable());
        }
    }

    private static List<ServerPlayerEntity> getCycleCandidates(ServerPlayerEntity tracker, MinecraftServer server) {
        List<ServerPlayerEntity> candidates = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player != tracker && player.isAlive()) {
                candidates.add(player);
            }
        }
        candidates.sort(Comparator.comparing(player -> player.getName().getString()));
        return candidates;
    }

    public void clearTarget(UUID trackerId) {
        currentTargets.remove(trackerId);
        lastTargetBlockPos.remove(trackerId);
    }
}
