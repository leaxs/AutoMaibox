package fr.leaxs.AutoMailbox.LetterReceiver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.proxy.Proxies;
import forestry.mail.network.packets.PacketPOBoxInfoUpdate;
import fr.leaxs.AutoMailbox.GUIUtils.Container_Tab;

public class Container_LetterReceiver extends Container 
{
	private Container_Tab[] tabs;
	private int activeTabID = 0;
	private InventoryPlayer invPlayer;
	private Container_Tab[] letterUpBar;
	private int letterUpBarID = 0;
	private TileEntity_LetterReceiver telr;

	public Container_LetterReceiver(InventoryPlayer inventoryPlayer, TileEntity_LetterReceiver telr) 
	{
		invPlayer = inventoryPlayer;
		this.telr = telr;
		tabs = new Container_Tab[]
				{
				new Container_Tab(),	//Tab_LetterViewer
				new Container_Tab(),	//Tab_LetterOverview
				new Container_Tab()		//Tab_LetterHistory
				};
		letterUpBar = new Container_Tab[7];
		for(int i=0;i<letterUpBar.length;i++)
		{
			letterUpBar[i] = new Container_Tab();
			for(int x=0;x<12;x++)
				letterUpBar[i].addSlotToContainer(new SlotOutput(telr.getMailBoxInventory(), x+i*12, 6+x*18, 6));
		}
		//Tab_LetterViewer
		int id = 0;
		for(int x=0;x<4;x++)
		{
			for(int y=0; y<4;y++)
			{
				tabs[0].addSlotToContainer(new SlotOutput(telr, id, 150+x*18, 30+y*18));
				id++;
			}
		}
		tabs[0].addSlotToContainer(new SlotOutput(telr, id, 150, 102));
		tabs[0].addSlotToContainer(new SlotOutput(telr, id+1, 168, 102));

		//Tab_LetterOverview
		id = 0;
		for(int y=0; y<7;y++)
		{
			for(int x=0;x<12;x++)
			{
				tabs[1].addSlotToContainer(new SlotOutput(telr.getMailBoxInventory(), id, 6+x*18, 6+y*18));
				id++;
			}
		}
		UpdateContainer();
	}

	public void changeTab(int newTabID)
	{
		activeTabID = newTabID;
		UpdateContainer();
	}

	public void updateLetterUpBar(int newLUBID)
	{
		letterUpBarID = newLUBID;
		UpdateContainer();
	}

	private void UpdateContainer()
	{
		inventoryItemStacks.clear();
		inventorySlots.clear();
		if(activeTabID == 0)
			for(Slot slot : letterUpBar[letterUpBarID].getSlot())
				addSlotToContainer(slot);

		for(Slot slot : tabs[activeTabID].getSlot())
			addSlotToContainer(slot);		
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(invPlayer, x + y * 9 + 9, 8 + 18 * x, 149 + 18 * y));

		for(int x = 0; x<9;x++)
			addSlotToContainer(new Slot(invPlayer, x, 8 + 18 * x, 207));
	}

	public void emptyingLetter()
	{
		int id = 0;
		for(int i=0;i<invPlayer.mainInventory.length;i++)
		{
			while(tabs[0].getSlot().get(id).getStack() == null)
			{
				id++;
				if(id>=tabs[0].getSlot().size())
					return;
			}
			if(invPlayer.getStackInSlot(i) == null)
				invPlayer.setInventorySlotContents(i,tabs[0].getSlot().get(id).decrStackSize(
						tabs[0].getSlot().get(id).getStack().stackSize));
			else if(invPlayer.getStackInSlot(i).isItemEqual(tabs[0].getSlot().get(id).getStack()))
			{
				int amount = invPlayer.getStackInSlot(i).getMaxStackSize() -
						invPlayer.getStackInSlot(i).stackSize;
				if(amount>0)
				{
					tabs[0].getSlot().get(id).decrStackSize(amount);
					ItemStack item = invPlayer.getStackInSlot(i);
					item.stackSize += amount;
					invPlayer.setInventorySlotContents(i,item);
				}
			}
		}
	}
	
	@Override
	public ItemStack slotClick(int p_75144_1_, int p_75144_2_, int p_75144_3_,EntityPlayer player) 
	{
		Proxies.net.sendToPlayer(new PacketPOBoxInfoUpdate(telr.getPOBoxInfo()), telr.getOwner());
		return super.slotClick(p_75144_1_, p_75144_2_, p_75144_3_, player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) 
	{
		ItemStack itemOld = null;
		if(getSlot(slot)!=null && getSlot(slot).getHasStack())
		{
			ItemStack item = getSlot(slot).getStack().copy();
			itemOld = item.copy();
			if (slot < inventorySlots.size()-36)	//From LR to player
			{
				if(!mergeItemStack(item, inventorySlots.size()-36, inventorySlots.size(), true))
				{
					return null;
				}
				getSlot(slot).onSlotChange(item, itemOld);
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
