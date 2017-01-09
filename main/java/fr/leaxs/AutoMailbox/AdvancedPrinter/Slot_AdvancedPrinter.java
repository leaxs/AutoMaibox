package fr.leaxs.AutoMailbox.AdvancedPrinter;

import dan200.computercraft.shared.media.items.ItemPrintout;
import fr.leaxs.AutoMailbox.Utils.InkType;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class Slot_AdvancedPrinter extends Slot
{
	private int restriction = 0;
	public Slot_AdvancedPrinter(IInventory inv, int restriction, int id, int x, int y) 
	{
		super(inv, id, x, y);
		this.restriction = restriction;
	}

	public static boolean isItemValidForSlot(ItemStack item, int slotNumber, int restriction)
	{
		switch(restriction)
		{
		case 0:
			return  item.isItemEqual(new ItemStack(Items.paper))||
					(item.getItem() instanceof ItemPrintout && ItemPrintout.getType(item) == ItemPrintout.Type.Single);
		case 1:
			return InkType.isInkValid(item, InkType.order[slotNumber-6]);
		case 2:
			return false;
		default:
			return true;
		}
	}
	
	@Override
	public boolean isItemValid(ItemStack item) 
	{
		return isItemValidForSlot(item,slotNumber,restriction);
	}
}
