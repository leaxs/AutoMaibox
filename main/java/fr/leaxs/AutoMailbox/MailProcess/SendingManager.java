package fr.leaxs.AutoMailbox.MailProcess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalState;
import forestry.api.mail.PostManager;
import forestry.mail.MailAddress;
import forestry.mail.items.ItemStamps;
import fr.leaxs.AutoMailbox.LetterSender.TileEntity_LetterSender;
/**
 * This class manages all the sending operation for LS and ATS
 * @author leaxs
 */
public class SendingManager 
{
	public enum State
	{
		NO_LETTER, NO_STAMP, PACKAGE_FULL, NO_ITEM_ON_SLOT, NOT_ENOUGH_ITEM, NOT_ENOUGH_STAMPS, DONE, NO_RECEIVER, ERROR_DURING_SENDING;
	}	

	private ArrayList<int[]> itemToSend = new ArrayList<int[]>();//{slot,count}
	private TileEntity_LetterSender tels;
	private ILetter letter;
	private IPostalState sendingMessage;

	public SendingManager(TileEntity_LetterSender tels) 
	{
		this.tels = tels;
	}

	/**
	 * Add a slot to attach to the letter
	 * @param slotID
	 * @param amount
	 * @return the attachment state DONE if successful attach or an error message
	 */
	public State addItemToPackage(int slotID, int amount)
	{
		if(itemToSend.size()>=18)
			return State.PACKAGE_FULL;
		else if(tels.getStackInSlot(slotID) == null)
			return State.NO_ITEM_ON_SLOT;
		else
		{
			int ID = getSameSlot(slotID);
			ItemStack attachaments = tels.getStackInSlot(slotID);
			if(amount == -1)	//If amount equals -1, LS add all the stack.
				amount = attachaments.stackSize;
			if(ID!=-1)
			{
				amount += itemToSend.get(ID)[1];
				if(attachaments.stackSize < amount)
					return State.NOT_ENOUGH_ITEM;
				itemToSend.set(ID, new int[]{slotID,amount});
			}
			else
			{
				if(attachaments.stackSize < amount)
					return State.NOT_ENOUGH_ITEM;
				itemToSend.add(new int[]{slotID,amount});
			}
		}
		return State.DONE;
	}

	/**
	 * Remove the specified slot from the package
	 * @param slotID
	 * @return true if the item was remove, false otherwise
	 */
	public boolean removeItemFromPackage(int slotID, int amount)
	{
		for(int i=0;i<itemToSend.size();i++)
		{
			if(itemToSend.get(i)[0] == slotID)
			{
				if(itemToSend.get(i)[1]>amount && amount > 0)
					itemToSend.set(i, new int[]{slotID,(itemToSend.get(i)[1]-amount)});
				else
					itemToSend.remove(itemToSend.get(i));
				return true;
			}
		}
		return false;
	}
	
	private int getSameSlot(int slotID)
	{
		for(int i = 0;i < itemToSend.size();i++)
		{
			if(itemToSend.get(i)[0]==slotID)
				return i;
		}
		return -1;
	}

	private boolean payLetter(int cost, boolean isSimulation)
	{	
		TreeMap<Integer,Integer> stamps = new TreeMap<Integer,Integer>();

		int maximumValue = 0;
		//Create a TreeMap with the value and the number of stamp available
		for(int i=0;i<6;i++)
		{
			if(tels.getStackInSlot(i) == null)
				continue;
			
			ItemStack stamp = tels.getStackInSlot(i).copy();
			int value = ((ItemStamps)stamp.getItem()).getPostage(stamp).getValue();
			maximumValue += value*stamp.stackSize;
			if(stamps.containsKey(value))
				stamps.replace(value, stamps.get(value+stamp.stackSize));
			else
				stamps.put(value, stamp.stackSize);
		}
		//Check if LS has enough stamp to pay the letter
		if(cost>maximumValue)
			return false;

		@SuppressWarnings("unchecked")
		TreeMap<Integer,Integer> stampToDecrease = (TreeMap<Integer, Integer>) stamps.clone();//value,count

		//Decrease stamps on TreeMap only if the stamp value is higher than cost
		Iterator<Integer> itr = stamps.descendingKeySet().iterator();
		while(itr.hasNext())
		{
			int stampValue = itr.next();
			int stampCount = stamps.get(stampValue).intValue();
			if(stampCount<=0)
				continue;
			int stampCost = Math.min(((Integer)cost/stampValue),stampCount);
			cost -= stampCost*stampValue;
			stamps.replace(stampValue,stamps.get(stampValue).intValue()-stampCost);
			if(cost<=0)
				break;
		}
		//If it remains something to pay, this pay with a higher stamp value
		if(cost>0)
		{
			itr = stamps.navigableKeySet().iterator();
			while(cost>0)
			{
				int key = itr.next();
				int stampCount = stamps.get(key).intValue();
				if(stampCount<=0)
					continue;
				cost-=key;
				stamps.replace(key,stamps.get(key).intValue()-1);
			}
		}
		//Modify the stampToDecrease to remove the item from inventory
		itr = stamps.descendingKeySet().iterator();
		while(itr.hasNext())
		{
			int key = itr.next();
			stampToDecrease.replace(key,stampToDecrease.get(key).intValue()-stamps.get(key).intValue());
		}
		
		/*LS puts stamp on the letter if we simulate the pay.
		 * Otherwise if we don't simulate, LS removes used stamps from their slot 
		 */
		for(int i=0;i<6;i++)
		{
			if(tels.getStackInSlot(i) == null)
				continue;
			
			ItemStack stamp = tels.getStackInSlot(i).copy();
			int key = ((ItemStamps)stamp.getItem()).getPostage(stamp).getValue();
			int toRemoveFromSlot = Math.min(stamp.stackSize, stampToDecrease.get(key).intValue());
			if(isSimulation)
				letter.addStamps(stamp.splitStack(toRemoveFromSlot));
			else
				tels.decrStackSize(i, toRemoveFromSlot);
			stampToDecrease.replace(key, stampToDecrease.get(key).intValue()-toRemoveFromSlot);
		}	
		return true;
	}
	
	public State send(String senderName, String receiverName)
	{
		//Check if the LS has at least one letter/stamp in his inventory
		State canSend = canSend();
		if(canSend != State.DONE)
			return canSend;

		//get the sender and receiver address 
		IMailAddress sender = new MailAddress(senderName);
		GameProfile receiverProfil = MinecraftServer.getServer().func_152358_ax().func_152655_a(receiverName);
		if(receiverProfil == null)
			return State.NO_RECEIVER;
		IMailAddress receiver = PostManager.postRegistry.getMailAddress(receiverProfil);

		letter = PostManager.postRegistry.createLetter(sender, receiver);
		//Put the attachment to the letter
		for(int[] item : itemToSend)
		{
			ItemStack itemToAttach = tels.getStackInSlot(item[0]).copy();
			if(itemToAttach.stackSize < item[1])
				return State.NOT_ENOUGH_ITEM;
			letter.addAttachment(itemToAttach.splitStack(item[1]));	
		}
		letter.setText(tels.getMessage());
		//Try to pay (without remove stamp from inventory) the letter
		if(!payLetter(letter.requiredPostage(), true))
			return State.NOT_ENOUGH_STAMPS;

		//Try to send the letter
		sendingMessage = PostManager.postRegistry.getPostOffice(tels.getWorldObj()).lodgeLetter(
				tels.getWorldObj(), PostManager.postRegistry.createLetterStack(letter), true);
		//Check if the sending was done
		if(!sendingMessage.isOk())
			return State.ERROR_DURING_SENDING;
		else
		{
			//Really pay the letter
			payLetter(letter.requiredPostage(), false);
			//remove attachments from LS inventory
			for(int[] item : itemToSend)
				tels.decrStackSize(item[0], item[1]);
			//Remove one letter from LS inventory
			for(int i=6;i<12;i++)
			{
				if(tels.getStackInSlot(i)!= null)
				{
					tels.decrStackSize(i, 1);
					break;
				}
			}
			//remove the attachment list
			reset();
			return State.DONE;
		}
	}

	private State canSend()
	{
		boolean canSend = false;
		for(int i=0;i<6;i++)
		{
			if(tels.getStackInSlot(i) != null)
			{
				canSend = true;
				break;
			}
		}
		if(!canSend)
			return State.NO_STAMP;
		canSend = false;
		for(int i=6;i<12;i++)
		{
			if(tels.getStackInSlot(i) != null)
			{
				canSend = true;
				break;
			}
		}
		if(!canSend)
			return State.NO_LETTER;
		return State.DONE;
	}

	/**
	 * Alloy to get the sending error. Useful if a ERROR_WHILE_SENDING was throw.
	 * @return IPostalState 
	 */
	public String getErrorMessage()
	{
		return sendingMessage.getIdentifier();
	}
	
	public Object[] getAttachementList(int slotID)
	{
		if(itemToSend.isEmpty())
			return new Object[]{"No attachment"};
		else if(slotID == -1)
		{
			Object[] slotUsed = new Object[itemToSend.size()];
			for(int i=0;i<itemToSend.size();i++)
				slotUsed[i] = itemToSend.get(i)[0]-12;
			return slotUsed;
		}
		else
		{
			for(int[] info : itemToSend)
				if(info[0] == slotID)
					return new Object[]{info[1]+"_"+tels.getStackInSlot(info[0]).getDisplayName()};
			return new Object[]{"No attachment on this slot"};
		}
		
	}
	
	public void reset()
	{
		itemToSend.clear();
	}
}
