package fr.leaxs.AutoMailbox.Packet;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.leaxs.AutoMailbox.LetterSender.TileEntity_LetterSender;

public class PacketLetterSender implements IMessage
{
	public static class Handler implements IMessageHandler<PacketLetterSender, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketLetterSender packet_ls, final MessageContext ctx)
		{
			final TileEntity_LetterSender tels = (TileEntity_LetterSender) (ctx.getServerHandler().playerEntity.worldObj.getTileEntity(packet_ls.x, packet_ls.y, packet_ls.z)
					instanceof TileEntity_LetterSender ? ctx.getServerHandler().playerEntity.worldObj.getTileEntity(packet_ls.x, packet_ls.y, packet_ls.z) : null);
			tels.toggleLock();
			return null;
		}
	}
	
	private int x,y,z=0;
	public PacketLetterSender() {}
	public PacketLetterSender(TileEntity_LetterSender tels) 
	{
		x = tels.xCoord;
		y = tels.yCoord;
		z = tels.zCoord;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

}
