package fr.leaxs.AutoMailbox.LetterReceiver;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;

import com.mojang.authlib.GameProfile;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.PostManager;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.proxy.Proxies;
import forestry.mail.Letter;
import forestry.mail.LetterProperties;
import forestry.mail.POBoxInfo;
import forestry.mail.PostRegistry;
import forestry.mail.items.ItemLetter;
import forestry.mail.network.packets.PacketPOBoxInfoUpdate;
import forestry.mail.tiles.IMailContainer;
import fr.leaxs.AutoMailbox.Utils.InventoryManager;
import fr.leaxs.AutoMailbox.Utils.TileEntityContainer;

public class TileEntity_LetterReceiver extends TileEntityContainer  implements IMailContainer,IPeripheral, IPeripheralTile
{
	private ArrayList<ILetter> history = new ArrayList<ILetter>();
	private GameProfile profil = new GameProfile(null, "leaxs");
	private int currentIndex = -1;
	private String letterMessage = "";
	private boolean isLinked = false;
	private int mode = 0;	//Never active, always active, active with redstone current
	private String senderName = "";

	public TileEntity_LetterReceiver() 
	{
		super("LetterReceiver", 18, 64); 
	}

	@Override
	public void updateEntity() 
	{
		//Check if it's the server side
		if(!worldObj.isRemote)
		{
			//Link the mailbox to the player
			if(!isLinked)
			{
				getMailBoxInventory();
				isLinked = true;
			}
			//Function to process incoming letter ( if always active or active with redstone current)
			if(hasMail() && (mode == 1)||(mode == 2 && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)))
			{
				//Create InventoryManager link to this tile
				InventoryManager invManag = new InventoryManager(this);
				for(int i=0;i<getMailBoxInventory().getSizeInventory();i++)
				{
					//If no letter it check another slot
					if(getMailBoxInventory().getStackInSlot(i)==null)
						continue;
					processLetter(i,invManag);
				}
			}
		}
		super.updateEntity();
	}

	/**
	 * Try to put all the attachment to the near inventory
	 * @param i index of the Mailbox slot
	 * @param invManag an InventoryManager
	 * @return
	 */
	private boolean processLetter(int i, InventoryManager invManag)
	{
		//Get the item and check if it's a letter then create a ILetter
		ItemStack item = getOrCreateMailInventory(profil).getStackInSlot(i);
		ILetter letter = null;
		if(item != null && ItemLetter.class.isAssignableFrom(item.getItem().getClass()))
			letter = new Letter(item.getTagCompound());
		if(letter != null)
		{
			//Try to put the attachment on the adjacent inventory
			for(int j=0;j<letter.getAttachments().length;j++)
			{
				if(letter.getStackInSlot(j) != null)
					letter.setInventorySlotContents(j, invManag.storeItemInNearbyInventory(letter.getStackInSlot(j)));
			}
			//If all the attachment is moved on the adj. inv., it try to put the letter
			if(letter.countAttachments()<=0 &&invManag.canStoreItemInNearbyInventory(
					PostManager.postRegistry.createLetterStack(letter)) == 0)
			{
				//Open the letter (GUI aspect)
				ItemStack newLetterStack = PostManager.postRegistry.createLetterStack(letter);
				LetterProperties.openLetter(newLetterStack);
				//Store the letter and put it in the adj. inv.
				invManag.storeItemInNearbyInventory(newLetterStack);
				history.add(new Letter(item.getTagCompound()));
				//Removing it from MailBox inventory
				getOrCreateMailInventory(profil).setInventorySlotContents(i, null);
				//Updating client HUD
				if(getOwner() != null)
					Proxies.net.sendToPlayer(new PacketPOBoxInfoUpdate(getPOBoxInfo()), getOwner());
				return true;
			}
			//Else we update letter on MailBox inventory
			else
				getOrCreateMailInventory(profil).setInventorySlotContents(i, PostManager.postRegistry.createLetterStack(letter));
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		//Update letter receiver GUI
		if(i == currentIndex)
			changeViewedLetter(-1);
		return false;
	}

	/** Change the current read letter
	 * @param index : new index to view (-1 to only update)
	 */
	public void changeViewedLetter(int index) 
	{
		if(index == -1)
			index = currentIndex;
		ItemStack item = getOrCreateMailInventory(profil).getStackInSlot(index);
		ILetter letter = null;
		if(item != null && ItemLetter.class.isAssignableFrom(item.getItem().getClass()))
			letter = new Letter(item.getTagCompound());
		//Set item in the internal MailBoxes Slot
		for(int i=0; i<18;i++)
			setInventorySlotContents(i, letter != null ? letter.getStackInSlot(i) : null);	
		//Set letter message
		letterMessage = letter != null ? letter.getText():"";
		currentIndex = index;
		senderName  = letter != null ? letter.getSender().getName() : "";
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	/**Get the message from current viewed letter
	 * @return the message of the letter
	 */
	public String getMessage()
	{
		return letterMessage;
	}

	/** Get the current viewed index
	 * @return index
	 */
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	//History function
	public ILetter getLetterHistoryViewed(int index) 
	{
		if(history.size()<index || history.size() == 0)
			return null;
		else
			return history.get(index);
	}

	public int getHistorySize()
	{
		return history.size();
	}

	public void removeHistoryIndex(int index)
	{
		if(history.size()>index)
			history.remove(index);
	}

	//Updating Mailboxes inventory
	@Override
	public ItemStack decrStackSize(int slotID, int numberOfItem) 
	{		
		ItemStack item = getOrCreateMailInventory(profil).getStackInSlot(currentIndex);
		ILetter letter = null;
		if(item != null && ItemLetter.class.isAssignableFrom(item.getItem().getClass()))
			letter = new Letter(item.getTagCompound());
		if(letter == null)
			return super.decrStackSize(slotID, numberOfItem);
		super.decrStackSize(slotID, numberOfItem);
		item = letter.decrStackSize(slotID, numberOfItem);
		getOrCreateMailInventory(profil).setInventorySlotContents(currentIndex, PostManager.postRegistry.createLetterStack(letter));
		return item;
	}

	public IInventory getOrCreateMailInventory(GameProfile playerProfile)
	{
		if (worldObj.isRemote)
			return new InventoryAdapter(84, "Letters").disableAutomation();

		final IMailAddress address = PostManager.postRegistry.getMailAddress(playerProfile);
		return PostRegistry.getOrCreatePOBox(worldObj, address);
	}

	public IInventory getMailBoxInventory() 
	{
		return getOrCreateMailInventory(profil);
	}

	//Check if the mailbox is empty
	@Override
	public boolean hasMail() 
	{
		IInventory mailInventory = getOrCreateMailInventory(profil);
		for (int i = 0; i < mailInventory.getSizeInventory(); i++) 
		{
			if (mailInventory.getStackInSlot(i) != null)
				return true;
		}
		return false;
	}

	//Function to get LR mode, info and owner
	public void changeMode()
	{
		mode = mode+1>2?0:mode+1;
	}

	public int getMode()
	{
		return mode;
	}

	public POBoxInfo getPOBoxInfo()
	{
		int playerLetters = 0;
		int tradeLetters = 0;
		for (int i = 0; i < getOrCreateMailInventory(profil).getSizeInventory(); i++) {
			if (getOrCreateMailInventory(profil).getStackInSlot(i) != null)
			{
				ILetter letter = new Letter(getOrCreateMailInventory(profil).getStackInSlot(i).getTagCompound());
				if (letter.getSender().isPlayer()) {
					playerLetters++;
				} else {
					tradeLetters++;
				}
			}
		}
		return new POBoxInfo(playerLetters, tradeLetters);
	}

	public void setOwner(GameProfile gameProfile) 
	{
		profil = gameProfile;
	}

	public EntityPlayer getOwner()
	{
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayerMP player : allPlayers) 
		{
			if (player.getGameProfile().getName().equals(profil.getName()))
				return player;
		}
		return null;
	}

	//Save function
	@Override
	public void writeToNBT(NBTTagCompound cmpd) 
	{
		cmpd.setString("Owner", profil.getName());
		cmpd.setInteger("currentIndex", currentIndex);
		cmpd.setString("Message", letterMessage);
		cmpd.setInteger("Mode", mode);
		NBTTagList Hist_Letter = new NBTTagList();
		for(final ILetter l : history)
		{
			final NBTTagCompound letter = new NBTTagCompound();
			l.writeToNBT(letter);
			Hist_Letter.appendTag(letter);
		}
		cmpd.setTag("History",Hist_Letter);
		super.writeToNBT(cmpd);
	}

	@Override
	public void readFromNBT(NBTTagCompound cmpd) 
	{
		profil = cmpd.hasKey("Owner") ? new GameProfile(null, cmpd.getString("Owner")) : profil;
		currentIndex = cmpd.hasKey("currentIndex") ? cmpd.getInteger("oldIndex") : currentIndex;
		letterMessage = cmpd.hasKey("Message") ? cmpd.getString("Message") : letterMessage;
		mode = cmpd.hasKey("Mode") ? cmpd.getInteger("Mode") : mode;
		if(cmpd.hasKey("History"))
		{
			history.clear();
			NBTTagList Hist_Letter = cmpd.getTagList("History", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < Hist_Letter.tagCount(); i++)
				history.add(new Letter(Hist_Letter.getCompoundTagAt(i)));
		}
		super.readFromNBT(cmpd);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		readFromNBT(pkt.func_148857_g());
		super.onDataPacket(net, pkt);
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound NTBTag = new NBTTagCompound();
		writeToNBT(NTBTag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, NTBTag);
	}

	@Override
	public int getDirection()
	{
		return 0;
	}

	@Override
	public void setDirection(int arg0) {}

	@Override
	public String getLabel() 
	{
		return "Letter Receiver";
	}

	@Override
	public IPeripheral getPeripheral(int arg0) 
	{
		return this;
	}

	@Override
	public PeripheralType getPeripheralType() 
	{
		return PeripheralType.Printer;
	}

	@Override
	public String getType() 
	{
		return "Letter_Sender";
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[]{"processLetter","readLetter","getAttachment","hasLetter",
				"getHistoryCount","readHistoryLetter","getHistoryAttachment","deleteHistoryLetter","getSender","getHistorySender"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] args) throws InterruptedException,
			LuaException {
		InventoryManager invManag = new InventoryManager(this);
		switch(method)
		{
		case 0:	//processLetter([int index])
			if(!hasMail())
				return new Object[]{false};
			if(args.length==0)
			{
				for(int i=0;i<getMailBoxInventory().getSizeInventory();i++)
				{
					//If no letter it check another slot
					if(getMailBoxInventory().getStackInSlot(i)==null)
						continue;
					if(processLetter(i,invManag))
						return new Object[]{true};
				}
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>this.getMailBoxInventory().getSizeInventory())
					new LuaException("ID of the slot must be between 0 and "+this.getMailBoxInventory().getSizeInventory());
				else 
				{
					if(getMailBoxInventory().getStackInSlot(((Number)args[0]).intValue())==null)
						return new Object[]{false};
					if(processLetter(((Number)args[0]).intValue(),invManag))
					{
						changeViewedLetter(-1);
						return new Object[]{true};
					}
					else
						return new Object[]{false};
				}
			}
		case 1://readLetter([int index])
			if(!hasMail())
				return new Object[]{""};
			if(args.length==0)
			{
				for(int i=0;i<getMailBoxInventory().getSizeInventory();i++)
				{
					//If no letter it check another slot
					if(getMailBoxInventory().getStackInSlot(i)==null)
						continue;
					else
					{
						changeViewedLetter(i);
						return new Object[]{letterMessage};
					}
				}				
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>this.getMailBoxInventory().getSizeInventory())
					new LuaException("ID of the slot must be between 0 and "+this.getMailBoxInventory().getSizeInventory());
				else
				{
					changeViewedLetter(((Number)args[0]).intValue());
					return new Object[]{letterMessage};
				}

			}
		case 2://getAttachment([int index])
			if(!hasMail())
				return new Object[]{};
			changeViewedLetter(-1);
			if(args.length != 0)
			{
				if(!(args[0] instanceof Number))
					new LuaException("Wrong first argument type, must be Integer");
				if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>this.getMailBoxInventory().getSizeInventory())
					new LuaException("ID of the slot must be between 0 and "+this.getMailBoxInventory().getSizeInventory());
				if(getMailBoxInventory().getStackInSlot(((Number)args[0]).intValue())==null)
					return new Object[]{};
				changeViewedLetter(((Number)args[0]).intValue());
			}

			ArrayList<String> itemName = new ArrayList<String>();
			ArrayList<Integer> itemCount = new ArrayList<Integer>();
			for(ItemStack item : items)
			{
				if(item != null)
				{
					itemName.add(item.getDisplayName());
					itemCount.add(item.stackSize);
				}
			}
			Object[] result = new Object[itemName.size()];
			for(int i=0;i<itemName.size();i++)
			{
				result[i]=itemCount.get(i)+"_"+itemName.get(i);
			}
			return result;
		case 3://hasLetter()
			return new Object[]{hasMail()};
		case 4://getHistoryCount()
			return new Object[]{history.size()};
		case 5://readHistoryLetter([int index])
			if(history.size()<=0)
				return new Object[]{""};
			if(args.length==0)
			{
				return new Object[]{history.get(0).getText()};				
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>history.size())
					return new Object[]{history.get(0).getText()};	
				else
					return new Object[]{history.get(((Number)args[0]).intValue()).getText()};	
			}
		case 6://getHistoryAttachment([int index])
			if(history.size()<=0)
				return new Object[]{""};
			int index = 0;
			if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			if(!(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>history.size()))
				index = ((Number)args[0]).intValue();

			itemName = new ArrayList<String>();
			itemCount = new ArrayList<Integer>();
			for(ItemStack item : history.get(index).getAttachments())
			{
				if(item != null)
				{
					itemName.add(item.getDisplayName());
					itemCount.add(item.stackSize);
				}
			}
			result = new Object[itemName.size()];
			for(int i=0;i<itemName.size();i++)
			{
				result[i]=itemCount.get(i)+"_"+itemName.get(i);
			}
			return result;
		case 7://deleteHistoryLetter([int index])
			if(history.size()<=0)
				return new Object[]{false};
			if(args.length==0)
			{
				return new Object[]{history.remove(0) != null};				
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()>history.size())
					new LuaException("ID of letter must be between 0 and "+history.size());
				if(((Number)args[0]).intValue()<0)
					history.clear();
				else
					history.remove(((Number)args[0]).intValue());	
				return new Object[]{true};
			}
		case 8://getSender([int index])
			if(!hasMail())
				return new Object[]{""};
			if(args.length==0)
			{
				for(int i=0;i<getMailBoxInventory().getSizeInventory();i++)
				{
					//If no letter it check another slot
					if(getMailBoxInventory().getStackInSlot(i)==null)
						continue;
					else
					{
						changeViewedLetter(i);
						return new Object[]{senderName};
					}
				}				
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>this.getMailBoxInventory().getSizeInventory())
					new LuaException("ID of the slot must be between 0 and "+this.getMailBoxInventory().getSizeInventory());
				else
				{
					changeViewedLetter(((Number)args[0]).intValue());
					return new Object[]{senderName};
				}
			}
		case 9://getHistorySender([int index])
			if(history.size()<=0)
				return new Object[]{""};
			if(args.length==0)
			{
				return new Object[]{history.get(0).getSender().getName()};					
			}
			else if(!(args[0] instanceof Number))
				new LuaException("Wrong first argument type, must be Integer");
			else
			{
				if(((Number)args[0]).intValue()>=0||((Number)args[0]).intValue()<=history.size())
					return new Object[]{history.get(((Number)args[0]).intValue()).getSender().getName()};		
			}
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) 
	{
		return other instanceof TileEntity_LetterReceiver;
	}
}
