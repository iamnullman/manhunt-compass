package dev.nullman.compass.item;

import dev.nullman.compass.ManhuntCompassMod;
import dev.nullman.compass.config.ModConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public final class CompassHelper {
    public static final String MARKER_LORE = "manhunt_compass_marker";

    private CompassHelper() {
    }

    public static ItemStack createCompass() {
        ModConfig config = ManhuntCompassMod.getConfig();
        ItemStack stack = new ItemStack(Items.COMPASS);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(config.getCompassName()));
        stack.set(
                DataComponentTypes.LORE,
                new LoreComponent(List.of(Text.literal(MARKER_LORE).formatted(Formatting.DARK_GRAY)))
        );
        return stack;
    }

    public static boolean isManhuntCompass(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isOf(Items.COMPASS)) {
            return false;
        }

        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return false;
        }

        for (Text line : lore.lines()) {
            if (line.getString().contains(MARKER_LORE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHoldingManhuntCompass(ServerPlayerEntity player) {
        return getCompassHand(player).isPresent();
    }

    public static Optional<Hand> getCompassHand(ServerPlayerEntity player) {
        if (isManhuntCompass(player.getMainHandStack())) {
            return Optional.of(Hand.MAIN_HAND);
        }
        if (isManhuntCompass(player.getOffHandStack())) {
            return Optional.of(Hand.OFF_HAND);
        }
        return Optional.empty();
    }

    /**
     * Updates the lodestone tracker on the held compass and syncs it to the client.
     *
     * @return true if the ItemStack was modified
     */
    public static boolean updateCompassTarget(ServerPlayerEntity tracker, ServerPlayerEntity target) {
        Optional<Hand> hand = getCompassHand(tracker);
        if (hand.isEmpty()) {
            return false;
        }

        ItemStack current = tracker.getStackInHand(hand.get());
        BlockPos targetPos = target.getBlockPos();
        RegistryKey<World> dimension = target.getWorld().getRegistryKey();
        GlobalPos desired = new GlobalPos(dimension, targetPos);

        LodestoneTrackerComponent trackerComponent = current.get(DataComponentTypes.LODESTONE_TRACKER);
        if (trackerComponent != null
                && !trackerComponent.tracked()
                && trackerComponent.target().isPresent()
                && trackerComponent.target().get().equals(desired)) {
            return false;
        }

        ItemStack updated = current.copy();
        updated.set(
                DataComponentTypes.LODESTONE_TRACKER,
                new LodestoneTrackerComponent(Optional.of(desired), false)
        );

        tracker.setStackInHand(hand.get(), updated);
        tracker.getInventory().markDirty();
        if (tracker.currentScreenHandler != null) {
            tracker.currentScreenHandler.syncState();
        }
        return true;
    }

    public static void giveCompass(ServerPlayerEntity player) {
        if (hasCompass(player)) {
            return;
        }
        player.getInventory().offerOrDrop(createCompass());
    }

    public static boolean hasCompass(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (isManhuntCompass(player.getInventory().getStack(slot))) {
                return true;
            }
        }
        return false;
    }

    public static void stripCompassFromInventory(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (isManhuntCompass(stack)) {
                player.getInventory().setStack(slot, ItemStack.EMPTY);
            }
        }
        player.getInventory().markDirty();
    }
}
