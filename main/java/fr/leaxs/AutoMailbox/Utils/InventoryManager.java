package fr.leaxs.AutoMailbox.Utils;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class InventoryManager 
{
	private ArrayList<NearbyInventory> nearbyInventories = new ArrayList<NearbyInventory>();
	public InventoryManager(TileEntityContainer tec) 
	{
		for(final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			final TileEntity tile = tec.getWorldObj().getTileEntity(tec.xCoord + dir.offsetX, tec.yCoord + dir.offsetY, tec.zCoord + dir.offsetZ);
			if(tile == null)
				continue;
			if(tile instanceof IInventory)
				nearbyInventories.add(new NearbyInventory((IInventory) tile,dir.getOpposite()));
		}
	}

	public ItemStack storeItemInNearbyInventory(ItemStack item)
	{
		for(NearbyInventory ninv : nearbyInventories)
		{
			for(int i=0;i<ninv.getInventory().getSizeInventory();i++)
			{
				if(ninv.getInventory().getStackInSlot(i) == null)
				{
					ninv.getInventory().setInventorySlotContents(i,item);
					return null;
				}
				else if(ItemStack.areItemStackTagsEqual(ninv.getInventory().getStackInSlot(i), item))
				{
					int injectable = ninv.getInventory().getStackInSlot(i).getMaxStackSize() - ninv.getInventory().getStackInSlot(i).stackSize;
					if(injectable>0)
					{
						ItemStack invItem = ninv.getInventory().getStackInSlot(i).copy();
						invItem.stackSize += Math.min(injectable,item.stackSize);
						item.stackSize -= Math.min(injectable,item.stackSize);
						ninv.getInventory().setInventorySlotContents(i,invItem);
						if(item.stackSize <=0)
							return null;
					}
				}
			}
		}
		return item;
	}

	public int canStoreItemInNearbyInventory(ItemStack item)
	{
		for(NearbyInventory ninv : nearbyInventories)
		{
			for(int i=0;i<ninv.getInventory().getSizeInventory();i++)
			{
				if(ninv.getInventory().getStackInSlot(i) == null)
					return 0;
				else if(ItemStack.areItemStackTagsEqual(ninv.getInventory().getStackInSlot(i), item))
				{
					int injectable = ninv.getInventory().getStackInSlot(i).getMaxStackSize() - ninv.getInventory().getStackInSlot(i).stackSize;
					if(injectable>0)
					{
						item.stackSize -= Math.min(injectable,item.stackSize);
						if(item.stackSize <=0)
							return 0;
					}
				}
			}
		}
		return item.stackSize;
	}

	class NearbyInventory
	{
		private final IInventory inventory;
		private int[] slot;

		public NearbyInventory(final IInventory inv, final ForgeDirection dir)
		{
			inventory = inv;
			slot = new int[inv.getSizeInventory()];
			for(int i = 0;i<slot.length;i++)
			{
				slot[i] = i;
			}
			if(inv instanceof ISidedInventory)
				slot = ((ISidedInventory) inv).getAccessibleSlotsFromSide(dir.flag);
		}

		public IInventory getInventory()
		{
			return inventory;
		}

		public int[] getValidSlot()
		{
			return slot;
		}
	}
}
