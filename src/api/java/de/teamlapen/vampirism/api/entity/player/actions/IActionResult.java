package de.teamlapen.vampirism.api.entity.player.actions;

import de.teamlapen.vampirism.api.entity.player.ISkillPlayer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public interface IActionResult {

    IActionResult SUCCESS = new Custom(true, Component.empty());
    IActionResult ON_COOLDOWN = new Custom(false, Component.translatable("text.vampirism.action.cooldown_not_over"));
    IActionResult RESTRICTED = new Custom(false, Component.translatable("text.vampirism.action.restricted"));
    IActionResult NOT_UNLOCKED = new Custom(false, Component.translatable("text.vampirism.action.not_unlocked"));
    IActionResult DISALLOWED_PERMISSION = new Custom(false, Component.translatable("text.vampirism.action.permission_disallowed"), false);
    IActionResult DISABLED_CONFIG = new Custom(false, Component.translatable("text.vampirism.action.deactivated_by_serveradmin"), false);
    IActionResult DISALLOWED_FACTION = new Custom(false, Component.translatable("text.vampirism.action.invalid_faction"), false);


    boolean successful();

    Component message();

    boolean sendToStatusBar();

    record Custom(boolean successful, Component message, boolean sendToStatusBar) implements IActionResult {

        Custom(boolean successful, Component message) {
            this(successful, message, true);
        }
    }

    static IActionResult disallowed(Component message) {
        return new Custom(false, message);
    }

    static <T extends ISkillPlayer<T>> IActionResult otherAction(IActionHandler<T> handler, Holder<? extends ILastingAction<T>> otherAction) {
        return handler.isActionActive(otherAction) ? new Custom(false, Component.translatable("text.vampirism.action.other_action", Component.translatable(Util.makeDescriptionId("action", otherAction.unwrapKey().map(ResourceKey::location).orElseThrow())))) : SUCCESS;
    }
}
