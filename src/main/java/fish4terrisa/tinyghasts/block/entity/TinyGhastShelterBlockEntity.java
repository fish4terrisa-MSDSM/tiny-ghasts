package fish4terrisa.tinyghasts.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import fish4terrisa.tinyghasts.TinyGhasts;

public class TinyGhastShelterBlockEntity extends BlockEntity {

    private NbtCompound ghastData;

    public TinyGhastShelterBlockEntity(BlockPos pos, BlockState state) {
        super(TinyGhasts.TINYGHAST_SHELTER_BE, pos, state);
        this.ghastData = new NbtCompound();
    }

    public NbtCompound getGhastData() {
        return this.ghastData;
    }

    public void setGhastData(NbtCompound data) {
        this.ghastData = data;
        markDirty(); // Important: syncs the data
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (this.ghastData != null) {
            nbt.put("TinyGhastInside", this.ghastData);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("TinyGhastInside")) {
            this.ghastData = nbt.getCompound("TinyGhastInside").orElse(null);
        }
    }
    
    // This is needed to sync data to the client for rendering
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
