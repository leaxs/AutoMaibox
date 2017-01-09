package fr.leaxs.AutoMailbox.LetterSender;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import forestry.mail.items.ItemLetter;
import forestry.mail.items.ItemStamps;

public class Slot_LetterSender extends Slot
{
	private int restriction = 2;
	/**
	 * Manage the letter sender gui slot.
	 * @param inv (tileEntity_letterSender) Inventory of the block
	 * @param itemsRestriction	0:letter; 1:Stamp, 2:Attachments
	 * @param id ID of the slot
	 * @param x position
	 * @param y position
	 * @author leaxs
	 */
	private TileEntity_LetterSender tels;
	public Slot_LetterSender(TileEntity_LetterSender inv, int itemsRestriction,  int id, int x, int y) 
	{
		super(inv, id, x, y);
		restriction = itemsRestriction;
		tels = inv;
	}
	
	@Override
	public boolean isItemValid(ItemStack item) 
	{
		switch(restriction)
		{
			case 0:
				return ItemStamps.class.isAssignableFrom(item.getItem().getClass());
			case 1:
				return ItemLetter.class.isAssignableFrom(item.getItem().getClass());
			case 2:
				if(tels.isLocked())//)
					return tels.getLockSlotItem()[this.slotNumber-12] != null ? (item.isItemEqual(tels.getLockSlotItem()[this.slotNumber-12])): false;
				else
					return true;
			default:
				return super.isItemValid(item);
		}
	}
	
	@Override
	public void onSlotChanged() 
	{
		if(!tels.isLocked() && restriction == 2)
			tels.removeItemLock(this.slotNumber-12);
		super.onSlotChanged();
	}
}
