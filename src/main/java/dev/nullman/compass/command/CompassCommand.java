package dev.nullman.compass.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.nullman.compass.ManhuntCompassMod;
import dev.nullman.compass.item.CompassHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class CompassCommand {
    private CompassCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(buildRoot());
        dispatcher.register(literal("compass").redirect(dispatcher.getRoot().getChild("pusula")));
        dispatcher.register(literal("target").redirect(dispatcher.getRoot().getChild("pusula")));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildRoot() {
        return literal("pusula")
                .executes(CompassCommand::giveCompass)
                .then(literal("reload")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(CompassCommand::reloadConfig));
    }

    private static int giveCompass(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("sadece oyuncular kullanabilir"));
            return 0;
        }

        CompassHelper.giveCompass(player);
        //context.getSource().sendFeedback(() -> Text.literal("§a"), false);
        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ManhuntCompassMod.reloadConfig();
        context.getSource().sendFeedback(() -> Text.literal("reload basarili"), true);
        return 1;
    }
}
