package de.teamlapen.vampirism.network;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ServerboundRequestSkillTreePacket implements CustomPacketPayload {
    public static final ServerboundRequestSkillTreePacket INSTANCE = new ServerboundRequestSkillTreePacket();
    public static final Type<ServerboundRequestSkillTreePacket> TYPE = new Type<>(VResourceLocation.mod("request_skill_tree"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestSkillTreePacket> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundRequestSkillTreePacket() {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
