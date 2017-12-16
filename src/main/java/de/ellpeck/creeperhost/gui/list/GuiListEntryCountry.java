package de.ellpeck.creeperhost.gui.list;

import net.minecraft.client.renderer.Tessellator;

public class GuiListEntryCountry extends GuiListEntry {

    public final String countryID;
    public final String countryName;

    public GuiListEntryCountry(GuiList list, String countryID, String countryName){
        super(list);
        this.countryID = countryID;
        this.countryName = countryName;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected){
        this.mc.fontRenderer.drawString(this.countryName, x+5, y+5, 16777215);
    }
}
