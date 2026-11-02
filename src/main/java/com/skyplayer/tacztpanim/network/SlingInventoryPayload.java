package com.skyplayer.tacztpanim.network;

import com.skyplayer.tacztpanim.TaczTpAnim;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.item.ItemStack;

public record SlingInventoryPayload(int entityId, int selected, List<ItemStack> hotbar) implements CustomPacketPayload {
    public static final int HOTBAR_SIZE = 9;
    public static final Type<SlingInventoryPayload> TYPE = new Type<>(TaczTpAnim.id("sling_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlingInventoryPayload> STREAM_CODEC = StreamCodec.of(
        SlingInventoryPayload::write, SlingInventoryPayload::read
    );

    public SlingInventoryPayload(int entityId, int selected, List<ItemStack> hotbar) {
        if (hotbar.size() != 9) {
            throw new IllegalArgumentException("A sling snapshot must contain exactly 9 hotbar stacks");
        } else {
            hotbar = List.copyOf(hotbar);
            this.entityId = entityId;
            this.selected = selected;
            this.hotbar = hotbar;
        }
    }

    private static void write(RegistryFriendlyByteBuf buffer, SlingInventoryPayload payload) {
        buffer.writeVarInt(payload.entityId);
        buffer.writeVarInt(payload.selected);

        for (ItemStack stack : payload.hotbar) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
    }

    private static SlingInventoryPayload read(RegistryFriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int selected = buffer.readVarInt();
        List<ItemStack> hotbar = IntStream.range(0, 9).mapToObj(ignored -> ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)).toList();
        return new SlingInventoryPayload(entityId, selected, hotbar);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
