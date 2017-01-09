package fr.leaxs.AutoMailbox.LetterSender;

import forestry.mail.items.ItemLetter;
import forestry.mail.items.ItemStamps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class Container_LetterSender extends Container
{
	
	public Container_LetterSender(InventoryPlayer inventoryPlayer,	TileEntity_LetterSender tels) 
	{
		//Slot stamp
		for(int x=0;x<3;x++)
			for(int y=0; y<2; y++)
				addSlotToContainer(new Slot_LetterSender(tels, 0, 3*y+x, 6 + 18 * x, 24+18*y));
		//Slot letter
		for(int x=0;x<3;x++)
			for(int y=0; y<2; y++)
				addSlotToContainer(new Slot_LetterSender(tels, 1, 3*y+x+6, 66 + 18 * x, 24+18*y));
		//Slot Attachments
		for(int y = 0; y < 2; y++)	
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot_LetterSender(tels, 2, x + y * 9 + 12, 8 + 18 * x, 73 + 18 * y));
		
		//Player slot
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + 18 * x, 130 + 18 * y));

		for(int x = 0; x<9;x++)
			addSlotToContainer(new Slot(inventoryPlayer, x, 8 + 18 * x, 188));
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) 
	{
		ItemStack itemOld = null;
		if(getSlot(slot)!=null && getSlot(slot).getHasStack())
		{
			ItemStack item = getSlot(slot).getStack().copy();
			itemOld = item.copy();
			if (slot < inventorySlots.size()-36)	//From LS to player inventory
			{
				if(!mergeItemStack(item, inventorySlots.size()-36, inventorySlots.size(), true))
				{
					return null;
				}
				getSlot(slot).onSlotChange(item, itemOld);
			}
			else	//From player inventory to LS
			{
				//Check if items are stamps
				if(ItemStamps.class.isAssignableFrom(item.getItem().getClass()))
				{
					if (!mergeItemStack(item, 0, 6, false))
					{
						if (!mergeItemStack(item, 12, 30, false))
						{
							return null;
						}
					}
				}
				//Check if items are stamps
				if(ItemLetter.class.isAssignableFrom(item.getItem().getClass()))
				{
					if (!mergeItemStack(item, 6, 12, false))
					{
						if (!mergeItemStack(item, 12, 30, false))
						{
							return null;
						}
					}
				}
				else
				{
					if (!mergeItemStack(item, 12, 30, false))
					{
						return null;
					}
				}
			}
			if (item.stackSize == 0)
			{
				getSlot(slot).putStack((ItemStack)null);
			}
			else
			{
				getSlot(slot).onSlotChanged();
			}

			if (item.stackSize == itemOld.stackSize)
			{
				return null;
			}

			getSlot(slot).onPickupFromSlot(player, item);
		}
		return itemOld;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return true;
	}

}
