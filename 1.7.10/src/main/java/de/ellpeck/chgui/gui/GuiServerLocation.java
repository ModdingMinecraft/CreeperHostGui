package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.gui.list.GuiList;
import de.ellpeck.chgui.gui.list.GuiListEntry;
import de.ellpeck.chgui.gui.list.GuiListEntryLocation;
import de.ellpeck.chgui.paul.Callbacks;
import de.ellpeck.chgui.paul.Order;

import java.io.IOException;
import java.util.Map;

public class GuiServerLocation extends GuiGetServer{

    private GuiList list;

    public GuiServerLocation(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public void initGui(){
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height-36, 36);

        Map<Integer, String> locations = Callbacks.getAllServerLocations();
        for(Map.Entry<Integer, String> entry : locations.entrySet()){
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            this.list.addEntry(listEntry);

            if(this.order.serverLocation == listEntry.locationName.toLowerCase()){
                this.list.setCurrSelected(listEntry);
            }
        }
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.server_location");
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        this.buttonNext.enabled = this.list.getCurrSelected() != null;
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();

        GuiListEntry entry = this.list.getCurrSelected();
        if(entry instanceof GuiListEntryLocation){
            this.order.serverLocation = ((GuiListEntryLocation)entry).locationName.toLowerCase();
        }
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        //this.list.handleMouseInput(); // fuck knows
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        this.list.drawScreen(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.func_148179_a(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        this.list.func_148181_b(mouseX, mouseY, state);
    }
}