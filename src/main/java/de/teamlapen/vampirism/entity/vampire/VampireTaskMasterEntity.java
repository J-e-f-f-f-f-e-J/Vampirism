package de.teamlapen.vampirism.entity.vampire;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.config.BalanceMobProps;
import de.teamlapen.vampirism.entity.IDefaultTaskMasterEntity;
import de.teamlapen.vampirism.entity.goals.FleeSunVampireGoal;
import de.teamlapen.vampirism.entity.goals.ForceLookEntityGoal;
import de.teamlapen.vampirism.entity.goals.LookAtClosestVisibleGoal;
import de.teamlapen.vampirism.entity.goals.RestrictSunVampireGoal;
import de.teamlapen.vampirism.entity.hunter.HunterBaseEntity;
import de.teamlapen.vampirism.inventory.container.TaskBoardContainer;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class VampireTaskMasterEntity extends VampireBaseEntity implements IDefaultTaskMasterEntity {

    private static final DataParameter<String> BIOME_TYPE = EntityDataManager.defineId(VampireTaskMasterEntity.class, DataSerializers.STRING);

    public static AttributeModifierMap.MutableAttribute getAttributeBuilder() {
        return VampireBaseEntity.getAttributeBuilder()
                .add(Attributes.MAX_HEALTH, BalanceMobProps.mobProps.VAMPIRE_MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BalanceMobProps.mobProps.VAMPIRE_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, BalanceMobProps.mobProps.VAMPIRE_SPEED);
    }
    @Nullable
    private PlayerEntity interactor;

    public VampireTaskMasterEntity(EntityType<? extends VampireBaseEntity> type, World world) {
        super(type, world, false);
        this.peaceful = true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (interactor != null && !(interactor.isAlive() && interactor.containerMenu instanceof TaskBoardContainer)) {
            this.interactor = null;
        }
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        ILivingEntityData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        this.setBiomeType(VillagerType.byBiome(worldIn.getBiomeName(this.blockPosition())));
        return data;
    }

    @Override
    public VillagerType getBiomeType() {
        String key = this.entityData.get(BIOME_TYPE);
        ResourceLocation id = new ResourceLocation(key);
        return Registry.VILLAGER_TYPE.get(id);
    }

    protected void setBiomeType(VillagerType type) {
        this.entityData.set(BIOME_TYPE, Registry.VILLAGER_TYPE.getKey(type).toString());
    }

    @Nonnull
    @Override
    public Optional<PlayerEntity> getForceLookTarget() {
        return Optional.ofNullable(this.interactor);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldShowName() {
        return Helper.isVampire(Minecraft.getInstance().player);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BIOME_TYPE, Registry.VILLAGER_TYPE.getDefaultKey().toString());
    }

    @Override
    protected ActionResultType mobInteract(@Nonnull PlayerEntity playerEntity, @Nonnull Hand hand) {
        if (this.level.isClientSide)
            return Helper.isVampire(playerEntity) ? ActionResultType.SUCCESS : ActionResultType.PASS;
        if (Helper.isVampire(playerEntity) && interactor == null) {
            if (this.processInteraction(playerEntity, this)) {
                this.getNavigation().stop();
                this.interactor = playerEntity;
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, CreatureEntity.class, 10, 1.0, 1.1, VampirismAPI.factionRegistry().getPredicate(getFaction(), false, true, false, false, VReference.HUNTER_FACTION)));
        this.goalSelector.addGoal(2, new RestrictSunVampireGoal<>(this));
        this.goalSelector.addGoal(2, new ForceLookEntityGoal<>(this));
        this.goalSelector.addGoal(3, new FleeSunVampireGoal<>(this, 0.9, false));
        this.goalSelector.addGoal(8, new MoveThroughVillageGoal(this, 0.6, true, 600, () -> false));
        this.goalSelector.addGoal(9, new RandomWalkingGoal(this, 0.7));
        this.goalSelector.addGoal(10, new LookAtClosestVisibleGoal(this, PlayerEntity.class, 20F, 0.6F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, HunterBaseEntity.class, 17F));
        this.goalSelector.addGoal(10, new LookRandomlyGoal(this));

        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }
}
