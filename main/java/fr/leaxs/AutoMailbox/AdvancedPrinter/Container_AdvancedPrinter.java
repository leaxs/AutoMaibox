package fr.leaxs.AutoMailbox.AdvancedPrinter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import dan200.computercraft.shared.media.items.ItemPrintout;
import fr.leaxs.AutoMailbox.Utils.InkType;

public class Container_AdvancedPrinter extends Container
{
	public Container_AdvancedPrinter(InventoryPlayer inventoryPlayer,TileEntity_AdvancedPrinter teAdvPrinter) 
	{
		//Slot Paper
		for(int x=0;x<6;x++)
			addSlotToContainer(new Slot_AdvancedPrinter(teAdvPrinter, 0, x, 62 + 18 * x, 21));
		//Slot Ink
		for(int y = 0; y < 2; y++)
			for(int x = 0; x < 2; x++)
				addSlotToContainer(new Slot_AdvancedPrinter(teAdvPrinter, 1, x + y * 2 + 6, 11 + 18 * x, 71 + 18 * y));
		//Slot Printed Page
		for(int x=0;x<6;x++)
			addSlotToContainer(new Slot_AdvancedPrinter(teAdvPrinter, 2, x + 10, 62 + 18 * x, 54));
		//Player slot
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + 18 * x, 111 + 18 * y));

		for(int x = 0; x<9;x++)
			addSlotToContainer(new Slot(inventoryPlayer, x, 8 + 18 * x, 169));
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) 
	{
		ItemStack itemOld = null;
		if(getSlot(slot)!=null && getSlot(slot).getHasStack())
		{
			ItemStack item = getSlot(slot).getStack().copy();
			itemOld = item.copy();
			if (slot < inventorySlots.size()-36)	//From AdvPrinter to player inventory
			{
				if(!mergeItemStack(item, inventorySlots.size()-36, inventorySlots.size(), true))
				{
					return null;
				}
				getSlot(slot).onSlotChange(item, itemOld);
			}
			else	//From player inventory to AdvPrinter
			{
				//Check if items are paper
				if(item.isItemEqual(new ItemStack(Items.paper))||
						(item.getItem() instanceof ItemPrintout && ItemPrintout.getType(item) == ItemPrintout.Type.Single))
				{
					if (!mergeItemStack(item, 0, 6, false))
					{
						return null;
					}
				}
				//Check if items are stamps
				if(InkType.isPrinterInk(item))
				{
					if (!mergeItemStack(item, 6, 10, false))
					{
						if (!mergeItemStack(item, 12, 31, false))
						{
							return null;
						}
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
