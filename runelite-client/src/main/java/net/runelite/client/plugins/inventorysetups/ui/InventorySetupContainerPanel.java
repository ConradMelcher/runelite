package net.runelite.client.plugins.inventorysetups.ui;

import net.runelite.api.Item;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.inventorysetups.InventorySetupConfig;
import net.runelite.client.plugins.inventorysetups.InventorySetupItem;
import net.runelite.client.plugins.inventorysetups.InventorySetupPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.http.api.loottracker.GameItem;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InventorySetupContainerPanel extends JPanel
{

	protected ItemManager itemManager;

	protected JPanel containerPanel;

	protected JPanel emptyContainerPanel;

	protected JLabel emptyContainerLabel;

	protected final Color originalLabelColor;

	private final InventorySetupPlugin plugin;

	public InventorySetupContainerPanel(final ItemManager itemManager, final InventorySetupPlugin plugin, String captionText, final String emptyContainerText)
	{
		this.itemManager = itemManager;
		this.plugin = plugin;
		this.containerPanel = new JPanel();
		this.emptyContainerPanel = new JPanel();
		this.emptyContainerLabel = new JLabel(emptyContainerText);
		this.originalLabelColor = emptyContainerLabel.getForeground();

		emptyContainerPanel.add(emptyContainerLabel);

		final JPanel containerSlotsPanel = new JPanel();

		setupContainerPanel(containerSlotsPanel);

		// caption
		final JLabel caption = new JLabel(captionText);
		caption.setHorizontalAlignment(JLabel.CENTER);
		caption.setVerticalAlignment(JLabel.CENTER);

		// panel that holds the caption and any other graphics
		final JPanel captionPanel = new JPanel();
		captionPanel.add(caption);

		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(captionPanel, BorderLayout.NORTH);
		containerPanel.add(containerSlotsPanel, BorderLayout.CENTER);
	}

	protected void setContainerSlot(int index,
	                             final InventorySetupSlot containerSlot,
	                             final ArrayList<InventorySetupItem> items,
                                 final AtomicBoolean hasItems)
	{
		if (index >= items.size() || items.get(index).getId() == -1)
		{
			containerSlot.setImageLabel(null, null);
			return;
		}

		hasItems.set(true);

		int itemId = items.get(index).getId();
		int quantity = items.get(index).getQuantity();
		final String itemName = items.get(index).getName();
		AsyncBufferedImage itemImg = itemManager.getImage(itemId, quantity, quantity > 1);
		String toolTip = itemName;
		if (quantity > 1)
		{
			toolTip += " (" + String.valueOf(quantity) + ")";
		}
		containerSlot.setImageLabel(toolTip, itemImg);
	}

	protected void modifyNoContainerCaption(final ArrayList<InventorySetupItem> currContainer)
	{
		// inventory setup is empty but the current inventory is not, make the text change color
		boolean hasDifference = false;
		for  (int i = 0; i < currContainer.size(); i++)
		{
			if (currContainer.get(i).getId() != -1)
			{
				hasDifference = true;
				break;
			}
		}

		if (hasDifference)
		{
			final Color highlightColor = plugin.getConfig().getHighlightColor();
			emptyContainerLabel.setForeground(highlightColor);
		}
		else
		{
			emptyContainerLabel.setForeground(this.originalLabelColor);
		}

	}

	protected void highlightDifferentSlotColor(InventorySetupItem savedItem,
	                                           InventorySetupItem currItem,
	                                           final InventorySetupSlot containerSlot)
	{
		// important note: do not use item names for comparisons
		// they are all empty to avoid clientThread usage when highlighting

		final InventorySetupConfig config = plugin.getConfig();
		final Color highlightColor = config.getHighlightColor();

		if (config.getStackDifference() && currItem.getQuantity() != savedItem.getQuantity())
		{
			containerSlot.setBackground(highlightColor);
			return;
		}

		int currId = currItem.getId();
		int checkId = savedItem.getId();

		if (!config.getVariationDifference())
		{
			currId = ItemVariationMapping.map(currId);
			checkId = ItemVariationMapping.map(checkId);
		}

		if (currId != checkId)
		{
			containerSlot.setBackground(highlightColor);
			return;
		}

		// set the color back to the original, because they match
		containerSlot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
	}

	abstract public void setupContainerPanel(final JPanel containerSlotsPanel);

}
