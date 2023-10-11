package de.teamlapen.vampirism.blockentity;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.blocks.mother.IRemainsBlock;
import de.teamlapen.vampirism.blocks.mother.MotherTreeStructure;
import de.teamlapen.vampirism.core.ModParticles;
import de.teamlapen.vampirism.core.ModSounds;
import de.teamlapen.vampirism.core.ModTiles;
import de.teamlapen.vampirism.network.ClientboundPlayEventPacket;
import de.teamlapen.vampirism.particle.FlyingBloodParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class MotherBlockEntity extends BlockEntity {

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("block.vampirism.mother"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private boolean isFrozen = false;
    private int freezeTimer = 0;

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, MotherBlockEntity e) {

        IS_A_MOTHER_LOADED_UNRELIABLE = true;
        //Handle fight ---------------------------------
        if (e.isFrozen && e.freezeTimer-- <= 0) {
            e.unFreezeFight(level, blockPos, blockState);
        }
        if (!e.isFrozen && e.level != null && e.isIntact()) {
            if (e.level.getRandom().nextInt(50) == 0) {
                e.updateFightStatus();
                if (!e.bossEvent.getPlayers().isEmpty()) {

                    List<Triple<BlockPos, BlockState, IRemainsBlock>> vuls = e.getTreeStructure(false).getVerifiedVulnerabilities(level).filter(t -> t.getRight().isVulnerable(t.getMiddle())).toList();
                    if (!vuls.isEmpty()) {
                        for (ServerPlayer player : e.bossEvent.getPlayers()) {
                            if (player.getAbilities().invulnerable) {
                                continue;
                            }
                            BlockPos p = vuls.get(e.level.getRandom().nextInt(vuls.size())).getLeft();
                            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 5 * 20, 2));
                            ModParticles.spawnParticlesServer(player.level(), new FlyingBloodParticleOptions(100, false, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5), player.getX(), player.getY() + player.getEyeHeight() / 2, player.getZ(), 10, 0.1f, 0.1f, 0.1f, 0);
                        }
                    }
                }

            }
        }
        //Handle destruction --------------------------------
        if (e.level != null && e.destructionTimer > 0) {
            if (e.destructionTimer++ % 3 == 0) {
                MotherTreeStructure structure = e.getTreeStructure(false);
                Optional<Set<BlockPos>> hierarchy = structure.popHierarchy();
                if (hierarchy.isPresent()) {
                    for (BlockPos p : hierarchy.get()) {
                        if (level.getBlockState(p).getBlock() instanceof IRemainsBlock) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                            ModParticles.spawnParticlesServer(level, new DustParticleOptions(new Vector3f(0.7f, 0.7f, 0.7f), 1), p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5f, 20, 0.3, 0.3, 0.3, 0.01);
                            e.level.playSound(null, p, ModSounds.REMAINS_DESTROYED.get(), SoundSource.BLOCKS, 0.2f, 1f);
                        }
                    }
                } else {
                    //Destruction complete
                    e.destructionTimer = -1;
                    if (e.level != null) {
                        e.level.playSound(null, blockPos, ModSounds.MOTHER_DEATH.get(), SoundSource.BLOCKS, 2f, 0.8f);
                    }
                }
            }
        }
    }


    //Destruction
    /**
     * Caches structure. Should be mostly valid because blocks cannot be destroyed by survival player. Is (unreliably) invalidated when a block is destroyed nonetheless.
     */
    private MotherTreeStructure cachedStructure;

    /**
     * Indicate whether a mother block is loaded in the world.
     * Should be acceptably accurate as there is only every one mother nearby. But don't use for anything important for gameplay
     */
    public static boolean IS_A_MOTHER_LOADED_UNRELIABLE = false;

    public MotherBlockEntity(BlockPos pos, BlockState state) {
        super(ModTiles.MOTHER.get(), pos, state);
        bossEvent.setProgress(1);
        bossEvent.setPlayBossMusic(true);
    }
    /**
     * Describes destruction process
     * == 0: Intact
     * > 0: Increasing, destruction in progress
     * == -1: Structure, destroyed, mother is vulnerable
     */
    private int destructionTimer = 0;

    public boolean isCanBeBroken() {
        return this.destructionTimer == -1;
    }

    public boolean isIntact() {
        return destructionTimer == 0;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.destructionTimer = tag.getInt("destruction_timer");
    }

    /**
     * Call this when a block that belongs to this structure is removed
     */
    public void onStructureBlockRemoved() {
        this.cachedStructure = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.bossEvent.removeAllPlayers();
        IS_A_MOTHER_LOADED_UNRELIABLE = false;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        IS_A_MOTHER_LOADED_UNRELIABLE = false;
    }

    private void addPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            this.bossEvent.addPlayer(serverPlayer);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onVulnerabilityHit(LivingEntity entity, boolean destroyed) {
        if (entity instanceof Player player){
            addPlayer(player);
        }
        updateFightStatus();
        if (destroyed && isIntact()) {
            freezeFight();
        }
    }

    public void updateFightStatus() {
        List<Triple<BlockPos, BlockState, IRemainsBlock>> vuls = getTreeStructure(false).getVerifiedVulnerabilities(level).toList();
        int remainingVulnerabilities = vuls.stream().filter(vul -> vul.getRight().isVulnerable(vul.getMiddle())).map(s -> level.getBlockEntity(s.getLeft())).filter(VulnerableRemainsBlockEntity.class::isInstance).map(VulnerableRemainsBlockEntity.class::cast).mapToInt(VulnerableRemainsBlockEntity::getHealth).sum();
        if (remainingVulnerabilities > 0) {
            this.bossEvent.setProgress(remainingVulnerabilities / ((float) vuls.size() * VulnerableRemainsBlockEntity.MAX_HEALTH));
        } else {
            this.bossEvent.setProgress(0);
            this.endFight();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("destruction_timer", this.destructionTimer);
    }

    private void endFight() {
        this.bossEvent.getPlayers().forEach(p -> VampirismMod.dispatcher.sendTo(new ClientboundPlayEventPacket(2, getBlockPos(), 0), p));
        this.bossEvent.removeAllPlayers();
        this.initiateDestruction();
    }

    private void freezeFight() {
        this.isFrozen = true;
        this.freezeTimer = 20 * 20;
        getTreeStructure(false).getVerifiedVulnerabilities(this.level).forEach(vul -> vul.getRight().freeze(level, vul.getLeft(), vul.getMiddle()));
        this.bossEvent.setColor(BossEvent.BossBarColor.WHITE);
    }

    @NotNull
    private MotherTreeStructure getTreeStructure(boolean forceRefresh) {
        if (forceRefresh || this.cachedStructure == null) {
            this.cachedStructure = MotherTreeStructure.getTreeView(this.level, this.worldPosition);
        }
        return cachedStructure;
    }

    private void initiateDestruction() {
        this.getTreeStructure(true);
        this.destructionTimer = 1;
    }

    private void unFreezeFight(Level level, BlockPos blockPos, BlockState blockState) {
        this.isFrozen = false;
        getTreeStructure(false).getVerifiedVulnerabilities(this.level).forEach(vul -> vul.getRight().unFreeze(level, vul.getLeft(), vul.getMiddle()));
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
    }

    public void informAboutAttacker(ServerPlayer serverPlayer) {
        addPlayer(serverPlayer);
    }

    public Collection<ServerPlayer> involvedPlayers() {
        return this.bossEvent.getPlayers();
    }
}
