package fr.leaxs.AutoMailbox.GUIUtils;

import java.util.ArrayList;

import net.minecraft.inventory.Slot;

public class Container_Tab 
{
	private ArrayList<Slot> inventorySlots = new ArrayList<Slot>();
	
	public Slot addSlotToContainer(final Slot par1Slot)
	{
		inventorySlots.add(par1Slot);
		return par1Slot;
	}

	public ArrayList<Slot> getSlot()
	{
		return inventorySlots;
	}
}
