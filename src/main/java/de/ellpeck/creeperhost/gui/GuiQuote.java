package de.ellpeck.creeperhost.gui;

import de.ellpeck.creeperhost.Util;
import de.ellpeck.creeperhost.gui.element.GuiWell;
import de.ellpeck.creeperhost.gui.list.GuiList;
import de.ellpeck.creeperhost.gui.list.GuiListEntryCountry;
import de.ellpeck.creeperhost.paul.Callbacks;
import de.ellpeck.creeperhost.paul.Order;
import de.ellpeck.creeperhost.paul.OrderSummary;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiQuote extends GuiGetServer{

    private GuiList list;
    private boolean countryEnabled = false;
    private GuiWell wellLeft;
    private GuiWell wellRight;
    private GuiWell wellBottom;
    private GuiButton countryButton;
    private boolean refreshing;
    private boolean changed;
    private boolean firstTime = true;
    private boolean countryOnRelease;

    public GuiQuote(int stepId, Order order){
        super(stepId, order);
    }

    public OrderSummary summary;

    @Override
    public void initGui(){
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height-36, 36);

        List<String> vpsIncluded = new ArrayList<String>();
        vpsIncluded.add(Util.localize("quote.vpsincluded1"));
        vpsIncluded.add(Util.localize("quote.vpsincluded2"));
        vpsIncluded.add(Util.localize("quote.vpsincluded3"));
        vpsIncluded.add(Util.localize("quote.vpsincluded4"));
        vpsIncluded.add(Util.localize("quote.vpsincluded5"));
        vpsIncluded.add(Util.localize("quote.vpsincluded6"));
        vpsIncluded.add(Util.localize("quote.vpsincluded7"));

        this.wellLeft = new GuiWell(this.mc, this.width / 2 - 10, 67, this.height - 88, Util.localize("quote.vpsfeatures"), new ArrayList<String>(), true, 0);
        this.wellRight = new GuiWell(this.mc, this.width, 67, this.height - 88, Util.localize("quote.vpsincluded"), vpsIncluded, true, (this.width / 2) + 10);
        this.wellBottom = new GuiWell(this.mc, this.width, this.height - 83, this.height - 36, "", new ArrayList<String>(), true, 0);

        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;

        String name = Callbacks.getCountries().get(order.country);

        countryButton = new GuiButton(8008135, start + middle - 100, this.height - 70, 200, 20, name);

        this.buttonList.add(countryButton);

        if (summary == null) {
            if (!refreshing)
                updateSummary();
            countryButton.visible = false;
       } else {

            this.wellLeft.lines = summary.vpsFeatures;

            Map<String, String> locations = Callbacks.getCountries();
            for(Map.Entry<String, String> entry : locations.entrySet()){
                GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                list.addEntry(listEntry);

                if(order.country.equals(listEntry.countryID)){
                    list.setCurrSelected(listEntry);
                }
            }

        }

    }

    private void updateSummary() {

        countryButton.visible = false;
        refreshing = true;
        summary = null;

        final Order order = this.order;

        Runnable runnable = new Runnable() {

            @Override
            public void run()
            {
                summary = Callbacks.getSummary(order);

                order.productID = summary.productID;
                order.currency = summary.currency;

                if (firstTime) {
                    firstTime = false;
                    Map<String, String> locations = Callbacks.getCountries();
                    for(Map.Entry<String, String> entry : locations.entrySet()){
                        GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                        list.addEntry(listEntry);

                        if(order.country.equals(listEntry.countryID)){
                            list.setCurrSelected(listEntry);
                        }
                    }
                }

                wellLeft.lines = summary.vpsFeatures;
                countryButton.displayString = Callbacks.getCountries().get(order.country);
                countryButton.visible = true;
                refreshing = false;
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.quote");
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        this.buttonNext.enabled = this.list.getCurrSelected() != null && !countryEnabled && !refreshing;
        this.buttonPrev.enabled = !refreshing;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 8008135) {
            countryOnRelease = true;
        }

        if (countryEnabled && button.id == buttonPrev.id) {
            this.countryEnabled = false;
            this.buttonPrev.displayString = Util.localize("button.prev");
            if (changed) {
                changed = false;
                updateSummary();
            } else {
                countryButton.visible = true;
            }
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        if (countryEnabled) {
            this.list.drawScreen(mouseX, mouseY, partialTicks);
        } else {
            if (!refreshing)
            {
                if (!summary.summaryError.isEmpty()) {
                    super.drawScreen(mouseX, mouseY, partialTicks);
                    this.drawCenteredString(this.fontRendererObj, Util.localize("quote.error"), this.width/2, 50, -1);
                    this.drawCenteredString(this.fontRendererObj, Util.localize(summary.summaryError), this.width/2, 60, -1);
                    countryButton.visible = false;
                    buttonNext.visible = false;
                    buttonPrev.visible = false;
                    return;
                }
                this.wellBottom.drawScreen();
                this.wellLeft.drawScreen();
                this.wellRight.drawScreen();


                this.drawCenteredString(this.fontRendererObj, Util.localize("quote.requirements") + summary.vpsDisplay, this.width / 2, 50, -1);

                String formatString = summary.prefix + "%1$.2f " + summary.suffix;

                String subTotalString = Util.localize("quote.subtotal") + ":  ";
                int subTotalWidth = fontRendererObj.getStringWidth(subTotalString);
                String discountString = Util.localize("quote.discount") + ":  ";
                int discountWidth = fontRendererObj.getStringWidth(discountString);
                String taxString = Util.localize("quote.tax") + ":  ";
                int taxWidth = fontRendererObj.getStringWidth(taxString);
                String totalString = Util.localize("quote.total") + ":  ";
                int totalWidth = fontRendererObj.getStringWidth(totalString);

                int headerSize = Math.max(subTotalWidth, Math.max(taxWidth, Math.max(totalWidth, discountWidth)));

                int subTotalValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.subTotal));
                int discountValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.discount));
                int taxValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.tax));
                int totalValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.tax));

                int maxStringSize = headerSize + Math.max(subTotalValueWidth, Math.max(discountValueWidth, Math.max(taxValueWidth, totalValueWidth)));

                int offset = maxStringSize / 2;
                int otherOffset = ((this.width / 2 - 10) / 2) - offset;

                this.drawString(this.fontRendererObj, subTotalString, otherOffset, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, discountString, otherOffset, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, taxString, otherOffset, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, totalString, otherOffset, this.height - 50, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);

                int start = (this.width / 2) + 10;
                int end = this.width;
                int middle = (end - start) / 2;
                int stringStart = this.fontRendererObj.getStringWidth(Util.localize("quote.figures")) / 2;

                this.drawString(this.fontRendererObj, Util.localize("quote.figures"), start + middle - stringStart, this.height - 80, 0xFFFFFF);
            } else {
                this.drawCenteredString(this.fontRendererObj, Util.localize("quote.refreshing"), this.width/2, 50, -1);
            }

        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.countryEnabled)
        {
            this.list.func_148179_a(mouseX, mouseY, mouseButton);
            GuiListEntryCountry country = (GuiListEntryCountry)this.list.getCurrSelected();
            order.country = country.countryID;
            changed = true;
        }

    }

    @Override
    protected void mouseMovedOrUp(int p_146286_1_, int p_146286_2_, int p_146286_3_)
    {
        if (this.countryEnabled) {
            this.list.func_148181_b(p_146286_1_, p_146286_2_, p_146286_3_);
        }
        if (countryOnRelease && p_146286_3_ >= 0) {
            countryOnRelease = false;
            this.countryEnabled = !this.countryEnabled;
            this.buttonPrev.displayString = "Back to quote";
            countryButton.visible = false;
            return;
        }
    }
}
