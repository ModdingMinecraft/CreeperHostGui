package de.ellpeck.chgui.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSlot;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class GuiWell extends GuiSlot
{
    private List<String> lines;
    private boolean centered;
    private String title;

    public GuiWell(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn, String title, List<String> linesToDraw, boolean centred)
    {
        super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
        this.title = title;
        this.lines = linesToDraw;
        this.centered = centred;
    }

    @Override
    protected int getSize()
    {
        return 0;
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
    {

    }

    @Override
    protected boolean isSelected(int slotIndex)
    {
        return false;
    }

    @Override
    protected void drawBackground()
    {

    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn)
    {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, (width / 2) - (titleWidth / 2), this.top + 2, 0xFFFFFF);

        int topStart = this.top + 30;

        for (String line: lines)
        {
            if (centered) {
                int stringWidth = fontRenderer.getStringWidth(line);
                fontRenderer.drawStringWithShadow(line, (width / 2) - (stringWidth / 2), topStart, 0xFFFFFF);
            } else {
                fontRenderer.drawStringWithShadow(line, 0, topStart, 0xFFFFFF);
            }
            topStart+= 10;
        }

    }
}
