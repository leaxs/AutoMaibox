package fr.leaxs.AutoMailbox;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftFactory;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.ItemPeripheral;
import fr.leaxs.AutoMailbox.AdvancedPrinter.Block_AdvancedPrinter;
import fr.leaxs.AutoMailbox.AdvancedPrinter.TileEntity_AdvancedPrinter;
import fr.leaxs.AutoMailbox.AutoTradingStation.Block_MailStation;
import fr.leaxs.AutoMailbox.GUIUtils.GuiHandler;
import fr.leaxs.AutoMailbox.LetterReceiver.Block_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.TileEntity_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterSender.Block_LetterSender;
import fr.leaxs.AutoMailbox.LetterSender.TileEntity_LetterSender;
import fr.leaxs.AutoMailbox.Packet.PacketHandler;

@Mod(modid = AutoMailbox.MODID, name = AutoMailbox.MODNAME, version = AutoMailbox.VERSION, dependencies="required-after:ComputerCraft;required-after:Forestry")

public class AutoMailbox 
{
	public static final String MODID = "automailbox";
	public static final String MODNAME = "Auto-Mailbox";
	public static final String VERSION = "2.0";
	
	@Instance(value = AutoMailbox.MODID)
	public static AutoMailbox instance;
	
	//Blocks initialization 
	public static Block_LetterReceiver letter_receiver;
	public static Block_LetterSender letter_sender;
	public static Block_MailStation mail_station;
	public static Block_AdvancedPrinter advanced_printer;
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		System.out.println("AMB INIT: Version( "+VERSION+" )");
		
		//Declaration of blocks
		letter_receiver = new Block_LetterReceiver(Material.iron);
		letter_sender = new Block_LetterSender(Material.iron);
		//mail_station = new Block_MailStation(Material.iron);
		advanced_printer = new Block_AdvancedPrinter(Material.iron);
		
		//Declaration of TileEntitiy
		GameRegistry.registerTileEntity(TileEntity_LetterSender.class,"LetterSender");
		GameRegistry.registerTileEntity(TileEntity_LetterReceiver.class,"LetterReceiver");
		GameRegistry.registerTileEntity(TileEntity_AdvancedPrinter.class,"AdvancedPrinter");
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		//Block registration 
		GameRegistry.registerBlock(letter_receiver, AutoMailbox.MODID + letter_receiver.getUnlocalizedName());
		GameRegistry.registerBlock(letter_sender, AutoMailbox.MODID + letter_sender.getUnlocalizedName());
		//GameRegistry.registerBlock(mail_station, AutoMailbox.MODID + mail_station.getUnlocalizedName());
		GameRegistry.registerBlock(advanced_printer, AutoMailbox.MODID + advanced_printer.getUnlocalizedName());
		
		new GuiHandler();
		new PacketHandler();
		//Creation of recipes
		
		//#LetterSender
		GameRegistry.addShapedRecipe(new ItemStack(AutoMailbox.letter_sender), new Object[]
		{
			"ICI",
			"RMR",
			"III",

			Character.valueOf('I'), Items.iron_ingot,
			Character.valueOf('C'), Blocks.chest,
			Character.valueOf('R'), Items.redstone,
			Character.valueOf('M'), forestry.plugins.PluginMail.blocks.mail
		});
		
		//#Auto-Receiver
		GameRegistry.addShapedRecipe(new ItemStack(AutoMailbox.letter_receiver), new Object[]
		{
			"IRI",
			"RMR",
			"IPI",

			Character.valueOf('I'), Items.iron_ingot,
			Character.valueOf('P'), Items.comparator,
			Character.valueOf('R'), Items.redstone,
			Character.valueOf('M'), forestry.plugins.PluginMail.blocks.mail
		});
		
		//#Mail Station
		/*<GameRegistry.addShapedRecipe(new ItemStack(AutoMailbox.mail_station), new Object[]
		{
			"IRI",
			"MCA",
			"IRI",

			Character.valueOf('I'), Items.iron_ingot,
			Character.valueOf('C'), Blocks.chest,
			Character.valueOf('R'), Items.redstone,
			Character.valueOf('M'), AutoMailbox.letter_receiver,
			Character.valueOf('A'), AutoMailbox.letter_sender
		});
		
		GameRegistry.addShapedRecipe(new ItemStack(AutoMailbox.mail_station), new Object[]
		{
			"IRI",
			"ACM",
			"IRI",

			Character.valueOf('I'), Items.iron_ingot,
			Character.valueOf('C'), Blocks.chest,
			Character.valueOf('R'), Items.redstone,
			Character.valueOf('M'), AutoMailbox.letter_receiver,
			Character.valueOf('A'), AutoMailbox.letter_sender
		});*/
		
		final ItemPeripheral ip = (ItemPeripheral)Item.getItemFromBlock(ComputerCraft.Blocks.peripheral);
		
		//#Advanced printer
		GameRegistry.addShapedRecipe(new ItemStack(AutoMailbox.advanced_printer), new Object[]
		{
			"ITI",
			"RPR",
			"III",

			Character.valueOf('I'), Items.iron_ingot,
			Character.valueOf('T'), BuildCraftFactory.tankBlock,
			Character.valueOf('R'), Items.redstone,
			Character.valueOf('P'), ip.create(PeripheralType.Printer,"craft",1),
		});
	}
	
}
