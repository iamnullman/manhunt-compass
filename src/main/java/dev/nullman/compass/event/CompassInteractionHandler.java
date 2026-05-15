package dev.nullman.compass.event;

import dev.nullman.compass.ManhuntCompassMod;
import dev.nullman.compass.item.CompassHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public final class CompassInteractionHandler {
    private CompassInteractionHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (!CompassHelper.isManhuntCompass(player.getStackInHand(hand))) {
                return ActionResult.PASS;
            }

            player.playSoundToPlayer();

            ManhuntCompassMod.getTrackingService().cycleTarget(serverPlayer);
            return ActionResult.SUCCESS;
        });
    }
}
