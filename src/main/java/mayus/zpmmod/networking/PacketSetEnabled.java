package mayus.zpmmod.networking;

import io.netty.buffer.ByteBuf;
import mayus.zpmmod.blockControllerLarge.ContainerControllerLarge;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetEnabled implements IMessage {

    // A default constructor is always required
    public PacketSetEnabled(){}

    @Override public void toBytes(ByteBuf buf) {}

    @Override public void fromBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketSetEnabled, IMessage> {

        @Override public IMessage onMessage(PacketSetEnabled message, MessageContext ctx) {
            // This is the player the packet was sent to the server from
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

            serverPlayer.getServerWorld().addScheduledTask(() -> {

                if (serverPlayer.openContainer instanceof ContainerControllerLarge) {
                    ContainerControllerLarge openContainer = (ContainerControllerLarge) serverPlayer.openContainer;
                    openContainer.te.isEnabled = openContainer.isEnabled;

                    if (openContainer.redstoneBehaviour == 0) {
                        openContainer.te.redstoneBehaviour = 1;
                    } else if (openContainer.redstoneBehaviour == 1) {
                        openContainer.te.redstoneBehaviour = 2;
                    } else {
                        openContainer.te.redstoneBehaviour = 0;
                    }
//                    openContainer.redstoneBehaviour++;
                    openContainer.te.markDirty();

                    System.out.println("Tile: " + openContainer.te.redstoneBehaviour);
                    System.out.println("Container: " + openContainer.redstoneBehaviour);
                }
            });
            // No response packet
            return null;
        }
    }
}
