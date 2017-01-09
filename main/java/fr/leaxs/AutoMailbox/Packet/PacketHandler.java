package fr.leaxs.AutoMailbox.Packet;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import fr.leaxs.AutoMailbox.AutoMailbox;

public class PacketHandler 
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(AutoMailbox.MODID);
	public PacketHandler() 
	{
		INSTANCE.registerMessage(PacketLetterSender.Handler.class, PacketLetterSender.class, 0, Side.SERVER);
		INSTANCE.registerMessage(PacketLetterReceiver.Handler.class, PacketLetterReceiver.class, 1, Side.SERVER);
	}
}
