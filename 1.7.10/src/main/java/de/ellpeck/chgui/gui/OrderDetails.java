package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.CreeperHostGui;
import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.paul.Callbacks;
import de.ellpeck.chgui.paul.Order;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Created by Aaron on 02/05/2017.
 */
public class OrderDetails extends GuiGetServer
{
    private boolean placingOrder = false;
    private boolean placedOrder = false;
    private boolean creatingAccount = false;
    private boolean createdAccount = false;
    private String createdAccountError = "";
    private int orderNumber;
    private String invoiceID;
    private String placedOrderError = "";
    private GuiButton buttonInvoice;


    public OrderDetails(int stepId, Order order)
    {
        super(stepId, order);
        if (order.clientID != null && !order.clientID.isEmpty()) {
            creatingAccount = false;
            createdAccount = true;
        }
    }

    @Override
    public String getStepName()
    {
        return Util.localize("gui.order");
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonNext.visible = false;
        buttonInvoice = new GuiButton(80000085, this.width/2-40, (this.height/2) + 30, 80, 20, Util.localize("button.invoice"));
        this.buttonList.add(buttonInvoice);
        buttonInvoice.visible = false;
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        if (button.id == 80000085) {
                try
                {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI("https://billing.creeperhost.net/viewinvoice.php?id=" + invoiceID)});
                }
                catch (Throwable throwable)
                {
                    CreeperHostGui.logger.error("Couldn\'t open link", throwable);
                }
        }
    }

    public void updateScreen() {
        super.updateScreen();
        if (!createdAccount && !creatingAccount) {
            if (!createdAccountError.isEmpty()) {
                return;
            }
            creatingAccount = true;
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    String result = Callbacks.createAccount(order);
                    String[] resultSplit = result.split(":");
                    if (resultSplit[0].equals("success"))
                    {
                        order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                        order.clientID = resultSplit[2] != null ? resultSplit[2] : "0"; // random test account fallback

                    } else {
                        createdAccountError = result;
                        createdAccount = true;
                    }
                    creatingAccount = false;
                    createdAccount = true;
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } else if (creatingAccount)
        {
            return;
        } else if (!createdAccountError.isEmpty()) {
            return;
        } else if (!placingOrder && !placedOrder) {
            placingOrder = true;
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    String result = Callbacks.createOrder(order);
                    String[] resultSplit = result.split(":");
                    if (resultSplit[0].equals("success"))
                    {
                        invoiceID = resultSplit[1] != null ? resultSplit[1] : "0";
                    } else {
                        placedOrderError = result;
                    }
                    placedOrder = true;
                    placingOrder = false;
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } else if(placingOrder)
        {
            return;
        } else if(placedOrderError.isEmpty()) {
            buttonInvoice.visible = true;
            return;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (creatingAccount)
        {
            drawCenteredString(fontRendererObj, "Please wait. Account being created", this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!createdAccountError.isEmpty()) {
            drawCenteredString(fontRendererObj, "Error occurred while creating account. Error:", this.width / 2, this.height / 2, 0xFFFFFF);
            drawCenteredString(fontRendererObj, createdAccountError, this.width / 2, (this.height / 2) + 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "Please go back and correct the errors", this.width / 2, this.height / 2 + 20, 0xFFFFFF);
        } else if (placingOrder) {
            drawCenteredString(fontRendererObj, "Please wait. Order being placed", this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!placedOrderError.isEmpty()) {
            drawCenteredString(fontRendererObj, "Error occurred while placing order. Error:", this.width / 2, this.height / 2, 0xFFFFFF);
            drawCenteredString(fontRendererObj, placedOrderError, this.width / 2, (this.height / 2) + 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "Please contact support if the problem persists", this.width / 2, (this.height / 2) + 20, 0xFFFFFF);
        } else {
            drawCenteredString(fontRendererObj, "Order successful! Please click the button below to pay", this.width / 2, this.height / 2, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}