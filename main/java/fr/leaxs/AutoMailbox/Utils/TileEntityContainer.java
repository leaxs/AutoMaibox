package fr.leaxs.AutoMailbox.Utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public abstract class TileEntityContainer extends TileEntity implements IInventory
{
	private String tileName;
	private int slotMaxSize;
	protected ItemStack[] items;
	
	/**
	 * Generic Tile Entity with container support.
	 * @param name of the inventory
	 * @param slotCount : the number of the slot of this inventory
	 * @param slotSize : the number of item that can hold each slot
	 * @author leaxs
	 */
	public TileEntityContainer(String name, int slotCount, int slotSize) 
	{
		items = new ItemStack[slotCount];
		tileName = name;
		slotMaxSize = slotSize;
	}
	
	@Override
	public int getSizeInventory() 
	{
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		return items[slotID];
	}

	@Override
	public ItemStack decrStackSize(int slotID, int numberOfItem) 
	{
		ItemStack item = getStackInSlot(slotID);
		if(item != null)
		{
			if(item.stackSize <= numberOfItem)
				setInventorySlotContents(slotID,null);
			else
				item = item.splitStack(numberOfItem);
		}
		return item;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID) 
	{
		ItemStack item = getStackInSlot(slotID);
		setInventorySlotContents(slotID, null);
		return item;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack item) 
	{
		items[slotID] = item;
		if(item != null && item.stackSize > getInventoryStackLimit())
			item.stackSize = getInventoryStackLimit();		
	}

	@Override
	public String getInventoryName() 
	{
		return tileName;
	}

	@Override
	public boolean hasCustomInventoryName() 
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return slotMaxSize;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack items) 
	{
		return true;
	}
	
	// Read and save function of the inventory
	@Override
	public void readFromNBT(final NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		synchronized(items)
		{
			final NBTTagList items_ = compound.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < items_.tagCount(); i++)
			{
				final NBTTagCompound item = items_.getCompoundTagAt(i);
				final int slot = item.getByte("Slot");

				if(slot >= 0 && slot < getSizeInventory())
					setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		synchronized(items)
		{
			final NBTTagList items_ = new NBTTagList();
			for(int i = 0; i < getSizeInventory(); i++)
			{
				final ItemStack stack = getStackInSlot(i);
				if(stack != null)
				{
					final NBTTagCompound item = new NBTTagCompound();
					item.setByte("Slot", (byte)i);
					stack.writeToNBT(item);
					items_.appendTag(item);
				}
			}
			compound.setTag("ItemInventory",items_);
		}
	}

}
