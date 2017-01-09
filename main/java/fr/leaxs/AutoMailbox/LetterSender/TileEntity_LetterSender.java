package fr.leaxs.AutoMailbox.LetterSender;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import forestry.mail.items.ItemStamps;
import fr.leaxs.AutoMailbox.MailProcess.SendingManager;
import fr.leaxs.AutoMailbox.MailProcess.SendingManager.State;
import fr.leaxs.AutoMailbox.Utils.TileEntityContainer;

public class TileEntity_LetterSender extends TileEntityContainer implements IPeripheral, IPeripheralTile
{
	private String name = "[AMB]Unknow";
	private ItemStack[] lockSlotItem;	//Used to check if incoming item can go in the slot and to display the item if there is no item in "real" slot
	private boolean isLocked;
	private SendingManager sendingManager;
	private String message = "";

	public TileEntity_LetterSender() 
	{
		super("letterSender", 30, 64);
		lockSlotItem = new ItemStack[30];
		sendingManager = new SendingManager(this);
	}

	//Manage the slots locking
	public void toggleLock()
	{
		isLocked = !isLocked;
		if(isLocked)
			for(int i=0;i<18;i++)
				lockSlotItem[i] = getStackInSlot(i+12);
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public void removeItemLock(int slotID) 
	{
		lockSlotItem[slotID] = null;	
	}

	public boolean isLocked()
	{
		return isLocked;
	}

	public ItemStack[] getLockSlotItem() {
		return lockSlotItem;
	}

	//Setter and getter of block owner
	public void setName(String newName)
	{
		this.name = newName;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack item) 
	{
		if(slotID<6)
			return ItemStamps.class.isAssignableFrom(item.getItem().getClass());
		else if(slotID<12)
			return item.isItemEqual(new ItemStack(forestry.plugins.PluginMail.items.letters));
		else if(isLocked)
			return lockSlotItem[slotID-12] != null && lockSlotItem[slotID-12].isItemEqual(item);
		else
			return true;
	}

	@Override
	public String getType() 
	{
		return "Letter_sender";
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[]{"send", "addAttachment", "removeAttachment", "getAttachement",
				"setMessage", "setName", "getName", "reset"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] args) throws InterruptedException,
			LuaException {
		switch(method)
		{
		case 0:	//Send(String receiver)
			if (args.length == 0)
				throwError("You must specify the adressee");
			else
			{
				if(!(args[0] instanceof String))
					throwError("Wrong argument type, must be String");
				State state = sendingManager.send(name, (String)args[0]);
				if(state != State.DONE)
					throwError(state == State.ERROR_DURING_SENDING ? 
							"Error during sending : "+sendingManager.getErrorMessage():
								"Error : "+state.toString().toLowerCase());
				return new Object[]{"Letter send to "+(String)args[0]};

			}
			return null;
		case 1:	//addAttachment(int slot [,int amount])
			if(args.length == 0)
				throwError("You must specify at least the slot number that's between 0 and 17");
			if(!(args[0] instanceof Number))
				throwError("Wrong first argument type, must be Integer");
			if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>17)
				throwError("The slot number must be between 0 and 17");
			if(args.length == 1)
			{
				final State result = sendingManager.addItemToPackage(((Number)args[0]).intValue()+12, -1);
				if(result != State.DONE)
					throwError("Error :"+result.toString().toLowerCase());
				return null;
			}
			else if(args[1] instanceof Number)
			{
				if(((Number)args[1]).intValue()<0)
					throwError("You cannot attach a negative amount of item!");
				final State result = sendingManager.addItemToPackage(((Number)args[0]).intValue()+12, ((Number)args[1]).intValue());
				if(result != State.DONE)
					throwError("Error :"+result.toString().toLowerCase());
				return null;
			}
			else
				throwError("Wrong second argument type, must be Integer");
			return null;
		case 2:	//removeAttachment(int slotID [,int amount])
			if(args.length == 0)
				throwError("You must specify at least the slot number that's between 0 and 17");
			if(!(args[0] instanceof Number))
				throwError("Wrong first argument type, must be Integer");
			if(((Number)args[0]).intValue()<0 || ((Number)args[0]).intValue()>17)
				throwError("The slot number must be between 0 and 17");
			if(args.length == 1)
				return new Object[]{sendingManager.removeItemFromPackage(((Number)args[0]).intValue()+12,-1)};
			else if(args[1] instanceof Number)
			{
				if(((Number)args[1]).intValue()<0)
					throwError("You cannot remove a negative amount of item!");
				return new Object[]{sendingManager.removeItemFromPackage(((Number)args[0]).intValue()+12,((Number)args[1]).intValue())};
			}
			else
				throwError("Wrong second argument type, must be Integer");
			return null;
		case 3:	//getAttachement([int index])
			if(args.length == 0)
				return sendingManager.getAttachementList(-1);
			else if(args[0] instanceof Number)
				return sendingManager.getAttachementList(((Number)args[0]).intValue()+12);
			else
				throwError("The slot number must be between 0 and 17");
		case 4:	//setMessage(String message)
			if (args.length == 0)
				throwError("You must specify the message");
			else
			{
				if(args[0] instanceof String)
					message = (String)args[0];
				else
					throwError("Wrong argument type, must be String");
			}
			return null;
		case 5:	//setName(String name)
			if (args.length == 0)
				throwError("You must specify the new name");
			else
			{
				if(args[0] instanceof String)
					name = "[AMB]"+(String)args[0];
				else
					throwError("Wrong argument type, must be String rename(\"...\")");
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			return null;
		case 6:	//getName()
			return new Object[]{name};
		case 7:	//reset()
			sendingManager.reset();
			return null;
		}
		return null;
	}

	private void throwError(final String msg) throws LuaException
	{
		sendingManager.reset();
		throw new LuaException(msg);
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) 
	{
		return other instanceof TileEntity_LetterSender;
	}

	public String getMessage() 
	{
		return message;
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
		return "Letter sender";
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
	public void writeToNBT(NBTTagCompound cmpd) 
	{
		final NBTTagList items = new NBTTagList();
		for(int i = 0; i < 18; i++)
		{
			final ItemStack stack = lockSlotItem[i];
			if(stack != null)
			{
				final NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte)i);
				stack.writeToNBT(item);
				items.appendTag(item);
			}
		}
		cmpd.setTag("Item_lockSlot",items);
		
		cmpd.setBoolean("Lock_state", isLocked);
		cmpd.setString("LS_name", name);
		super.writeToNBT(cmpd);
	}

	@Override
	public void readFromNBT(NBTTagCompound cmpd) 
	{
		final NBTTagList items = cmpd.getTagList("Item_lockSlot", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < items.tagCount(); i++)
		{
			final NBTTagCompound item = items.getCompoundTagAt(i);
			final int x = item.getByte("Slot");

			if(x >= 0 && x < 18)
				lockSlotItem[x] = ItemStack.loadItemStackFromNBT(item);
		}

		isLocked = cmpd.hasKey("Lock_state") ? cmpd.getBoolean("Lock_state") : isLocked;
		name = cmpd.hasKey("LS_name") ? cmpd.getString("LS_name") : name;
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
}
