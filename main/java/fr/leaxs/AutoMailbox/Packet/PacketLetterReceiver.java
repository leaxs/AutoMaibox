package fr.leaxs.AutoMailbox.Packet;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.leaxs.AutoMailbox.LetterReceiver.Container_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.TileEntity_LetterReceiver;

public class PacketLetterReceiver implements IMessage
{
	public static class Handler implements IMessageHandler<PacketLetterReceiver, IMessage>
	{
		@Override
		public IMessage onMessage(PacketLetterReceiver packet_ls, final MessageContext ctx)
		{
			final Container_LetterReceiver clr = (Container_LetterReceiver) ctx.getServerHandler().playerEntity.openContainer;
			final TileEntity_LetterReceiver telr = (TileEntity_LetterReceiver) (ctx.getServerHandler().playerEntity.worldObj.getTileEntity(packet_ls.x, packet_ls.y, packet_ls.z)
					instanceof TileEntity_LetterReceiver ? ctx.getServerHandler().playerEntity.worldObj.getTileEntity(packet_ls.x, packet_ls.y, packet_ls.z) : null);
			
			switch(packet_ls.eventID)
			{
			case 0:
				clr.changeTab(packet_ls.event);
				break;
			case 1:
				telr.changeViewedLetter(packet_ls.event);
				clr.updateLetterUpBar(telr.getCurrentIndex()/12);
				break;
			case 2:
				clr.emptyingLetter();
				break;
			case 3:
				telr.changeMode();
				break;
			case 5:
				telr.removeHistoryIndex(packet_ls.event);
			}
			return null;
		}
	}

	private int x,y,z,eventID,event=0;
	public PacketLetterReceiver() {}
	public PacketLetterReceiver(TileEntity_LetterReceiver telr, int eventID, int event) 
	{
		x = telr.xCoord;
		y = telr.yCoord;
		z = telr.zCoord;
		this.eventID = eventID;
		this.event = event;
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		eventID = buf.readInt();
		event = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(eventID);
		buf.writeInt(event);
	}



}
