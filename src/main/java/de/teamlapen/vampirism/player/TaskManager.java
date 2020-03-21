package de.teamlapen.vampirism.player;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.*;
import de.teamlapen.vampirism.core.ModRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskManager implements ITaskManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final @Nonnull PlayerEntity player;
    private final @Nonnull IPlayableFaction<? extends IFactionPlayer> faction;
    private final @Nonnull Set<Task> completedTasks = Sets.newHashSet();
    private final @Nonnull Set<Task> availableTasks = Sets.newHashSet();
    private final @Nonnull Map<Task, Map<EntityType<?>, Integer>> killStats = Maps.newHashMap();

    public TaskManager(@Nonnull PlayerEntity player, @Nonnull IPlayableFaction<? extends IFactionPlayer> faction) {
        this.faction = faction;
        this.player = player;
        this.reset();
    }

    @Override
    public void completeTask(@Nonnull Task task) {
        if (!(this.faction.equals(task.getFaction()) || task.getFaction() == null)) return;
        this.completedTasks.add(task);
        this.availableTasks.remove(task);
    }

    public Set<Task> getCompletedTasks() {
        return completedTasks;
    }

    public Set<Task> getAvailableTasks() {
        return availableTasks;
    }

    public boolean canCompleteTask(Task task) {
        if (!this.player.getEntityWorld().isRemote()) return false;
        for (TaskRequirement requirement : task.getRequirements()) {
            if (requirement.getType().equals(TaskRequirement.Type.KILLS)) {
                EntityType<?> entityType = ((KillRequirement) requirement).getEntityType();
                int amount = ((KillRequirement) requirement).getAmount();
                if (((ServerPlayerEntity) this.player).getStats().getValue(Stats.ENTITY_KILLED.get(entityType)) < killStats.get(task).get(entityType) + amount)
                    return false;
            } else if (requirement.getType().equals(TaskRequirement.Type.ITEMS)) {
                ItemStack stack = ((ItemRequirement) requirement).getItemRequirement();
                if (this.player.inventory.count(stack.getItem()) < stack.getCount()) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reset() {
        this.completedTasks.clear();
        this.availableTasks.addAll(ModRegistries.TASKS.getValues().stream().filter(task -> faction.equals(task.getFaction()) || task.getFaction() == null).collect(Collectors.toList()));
        this.killStats.clear();
        this.updateKillStats();
    }

    private void updateKillStats() {
        if (!player.getEntityWorld().isRemote()) return;
        for (Task task : this.availableTasks) {
            if (this.killStats.containsKey(task)) continue;
            Map<EntityType<?>, Integer> stats = null;
            for (TaskRequirement requirement : task.getRequirements()) {
                if (!requirement.getType().equals(TaskRequirement.Type.KILLS)) continue;
                EntityType<?> entityType = ((KillRequirement) requirement).getEntityType();
                if (stats == null) {
                    stats = Maps.newHashMap();
                }
                stats.put(entityType, ((ServerPlayerEntity) this.player).getStats().getValue(Stats.ENTITY_KILLED.get(entityType)));
            }
            if (stats != null) {
                this.killStats.put(task, stats);
            }
        }
    }

    public void writeNBT(CompoundNBT compoundNBT) {
        if (this.completedTasks.isEmpty()) return;
        CompoundNBT tasks = new CompoundNBT();
        this.completedTasks.forEach(task -> tasks.putBoolean(task.getRegistryName().toString(), true));
        compoundNBT.put("tasks", tasks);
    }

    public void readNBT(CompoundNBT compoundNBT) {
        if (!compoundNBT.contains("tasks")) return;
        compoundNBT.getCompound("tasks").keySet().forEach(taskId -> {
            Task task = ModRegistries.TASKS.getValue(new ResourceLocation(taskId));
            if (task != null) {
                this.completeTask(task);
            }
        });
    }
}