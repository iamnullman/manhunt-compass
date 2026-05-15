package dev.nullman.compass.event;

import dev.nullman.compass.ManhuntCompassMod;
import dev.nullman.compass.item.CompassHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerLifecycleHandler {
    private PlayerLifecycleHandler() {
    }

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            CompassHelper.giveCompass(newPlayer);
            ManhuntCompassMod.getTrackingService().clearTarget(newPlayer.getUuid());
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                stripCompassFromInventory(player);
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                ManhuntCompassMod.getTrackingService().clearTarget(player.getUuid());
            }
        });
    }

    private static void stripCompassFromInventory(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (CompassHelper.isManhuntCompass(stack)) {
                player.getInventory().setStack(slot, ItemStack.EMPTY);
            }
        }
    }
}
