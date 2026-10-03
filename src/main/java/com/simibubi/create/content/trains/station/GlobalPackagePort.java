package com.simibubi.create.content.trains.station;

import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class GlobalPackagePort {
	public String address = "";
	public OfflineBuffer offlineBuffer = new OfflineBuffer();
	public boolean primed = false;
	private boolean restoring = false;

	public static class OfflineBuffer extends ItemStackHandler {
		public OfflineBuffer() {
			super(18);
		}

		public CompoundTag serializeNBT(HolderLookup.Provider registries) {
			TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
			serialize(output);
			return output.buildResult();
		}

		public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
			deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, nbt));
		}
	}

	public void restoreOfflineBuffer(IItemHandlerModifiable inventory) {
		if (!primed) return;

		restoring = true;

		for (int slot = 0; slot < offlineBuffer.getSlots(); slot++) {
			inventory.setStackInSlot(slot, offlineBuffer.getStackInSlot(slot));
		}

		restoring = false;
		primed = false;
	}

	public void saveOfflineBuffer(IItemHandlerModifiable inventory) {
		/*
		 * Each time restoreOfflineBuffer changes a slot, the inventory
		 * calls this method. We must filter out those calls to prevent
		 * overwriting later slots which haven't been restored yet and
		 * to avoid unnecessary work.
		 */
		if (restoring) return;

		// TODO: Call save method on individual slots rather than iterating
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			offlineBuffer.setStackInSlot(slot, inventory.getStackInSlot(slot));
		}

		Create.RAILWAYS.markTracksDirty();
	}
}
