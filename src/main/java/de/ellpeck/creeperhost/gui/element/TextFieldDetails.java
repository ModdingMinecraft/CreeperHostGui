package de.ellpeck.creeperhost.gui.element;

import de.ellpeck.creeperhost.gui.DefferedValidation;
import de.ellpeck.creeperhost.gui.GuiPersonalDetails;
import net.minecraft.client.gui.GuiTextField;

import de.ellpeck.creeperhost.common.IOrderValidation;
import de.ellpeck.creeperhost.common.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class TextFieldDetails extends GuiTextField {

    private final GuiPersonalDetails gui;
    private final String displayString;
    private final boolean canBeFocused;
    private final int ourID;
    private String censorText = "";
    public boolean isValidated;
    private boolean isChangeValidated = false;
    public String validationError = "";
    private String acceptString = new String(Character.toChars(10004));
    private String denyString = new String(Character.toChars(10006));
    private ArrayList<IOrderValidation> validators;
    private boolean doNotValidate = false;
    private DefferedValidation pendingValidation = null;

    public TextFieldDetails(GuiPersonalDetails gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators, boolean canBeFocused){
        super(gui.mc.fontRenderer, x, y, width, height);
        
        this.ourID = id;

        this.validators = validators;
        this.gui = gui;
        this.canBeFocused = canBeFocused;
        this.displayString = displayString;

        this.setText(def);

        setFocused(true);
        setFocused(false); // make sure focused trigger ran

        this.setMaxStringLength(64);
    }

    public TextFieldDetails(GuiPersonalDetails gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators, String censorText){
        this(gui, id, displayString, def, x, y, width, height, validators);
        this.censorText = censorText;
    }

    public TextFieldDetails(GuiPersonalDetails gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators) {
        this(gui, id, displayString, def, x, y, width, height, validators, true);
    }

    public void checkPendingValidations() {
        if (pendingValidation != null && pendingValidation.isDone()) {
            gui.validationChangedDeferred(this, pendingValidation);
            isValidated = pendingValidation.isValid("");
            validationError = pendingValidation.getValidationMessage();
            pendingValidation.reset();
            pendingValidation = null;
        }
    }

    public int getId() {
        return ourID;
    }

    @Override
    public void drawTextBox(){
        if(!this.censorText.isEmpty()){
            String text = this.getText();

            double censorLength = censorText.length();

            double mainLength = text.length();

            double timesRaw = mainLength / censorLength;

            int times = (int) Math.ceil(timesRaw);

            String obscure = new String(new char[times]).replace("\0", censorText).substring(0, (int)mainLength);
            boolean oldNotValidate = doNotValidate;
            doNotValidate = true;
            this.setText(obscure);
            super.drawTextBox();

            this.setText(text);
            doNotValidate = oldNotValidate;
        }
        else{
            super.drawTextBox();
        }

        int startX = (this.xPosition + this.width + 3) / 2;
        int startY = (this.yPosition + 4) / 2;

        GL11.glScalef(2.0F, 2.0F, 2.0F);

        if (isValidated) {
            this.drawString(this.gui.mc.fontRenderer, acceptString, startX, startY, 0x00FF00);
        } else {
            this.drawString(this.gui.mc.fontRenderer, denyString, startX, startY, 0xFF0000);
        }

        GL11.glScalef(0.5F, 0.5F, 0.5F);

        if(!this.isFocused() && this.getText().trim().isEmpty()){
            int x = this.xPosition+4;
            int y = this.yPosition+(this.height-8)/2;

            this.gui.mc.fontRenderer.drawStringWithShadow("\u00A7o"+this.displayString, x, y, 14737632);
        }
    }

    public boolean canBeFocused()
    {
        return canBeFocused;
    }

    private Pair<Boolean, IOrderValidation> validateAtPhase(IOrderValidation.ValidationPhase phase, String string, boolean ignoreAsync)
    {
        if (pendingValidation != null || doNotValidate)
            return new Pair(false, null);
        boolean validatorsExist = false;
        for(IOrderValidation validator : validators) {
            if (!validatorsExist && !isChangeValidated && phase.equals(IOrderValidation.ValidationPhase.FOCUSLOST)) {
                return new Pair(false, null);
            }
            if (validator.validationCheckAtPhase(phase)) {
                if (validator.isAsync()) {
                    if (ignoreAsync) {
                        continue;
                    }
                    this.setEnabled(false);
                    pendingValidation = (DefferedValidation) validator;
                    pendingValidation.setPhase(phase);
                    pendingValidation.doAsync(string);
                }
                validatorsExist = true;
                if (validator.isValid(string)) {
                    continue;
                } else {
                    gui.validationChanged(this, false, validator, phase);
                    return new Pair(true, validator);
                }
            }
        }
        if (validatorsExist) {
            gui.validationChanged(this, true, null, phase);
        }
        return new Pair(validatorsExist, null);
    }

    private Pair<Boolean, IOrderValidation> validateAtPhase(IOrderValidation.ValidationPhase phase, String string) {
        return validateAtPhase(phase, string, false);
    }

    @Override
    public void setFocused(boolean focused)
    {
        if (focused) {
            gui.focusedField = this;
            if (!canBeFocused)
                return; // to prevent weirdness, we set focused anyway so that tab works as expected
        } else if (this.isFocused()) {
            Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.FOCUSLOST, getText());
            if (validatorPair.getLeft())
            {
                IOrderValidation validator = validatorPair.getRight();
                if (validator != null)
                {
                    validationError = validator.getValidationMessage();
                    isValidated = false;
                } else
                {
                    validationError = "This is fine";
                    isValidated = true;
                }
            }
        }
        super.setFocused(focused);
    }

    @Override
    public void writeText(String string) {
        super.writeText(string);
        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getText());
        if (validatorPair.getLeft())
        {
            IOrderValidation validator = validatorPair.getRight();
            if (validator != null)
            {
                validationError = validator.getValidationMessage();
                isValidated = false;
                isChangeValidated = false;
            } else
            {
                validationError = "This is fine";
                isValidated = true;
                isChangeValidated = true;
            }
        }
        return;
    }

    @Override
    public void deleteFromCursor(int num) {
        super.deleteFromCursor(num);
        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getText());
        if (validatorPair.getLeft())
        {
            IOrderValidation validator = validatorPair.getRight();
            if (validator != null)
            {
                validationError = validator.getValidationMessage();
                isValidated = false;
                isChangeValidated = false;
            } else
            {
                validationError = "This is fine";
                isValidated = true;
                isChangeValidated = true;
            }
        }
        return;
    }

    public void setText(String string) {
        super.setText(string);
        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getText());
        if (validatorPair.getLeft())
        {
            IOrderValidation validator = validatorPair.getRight();
            if (validator != null)
            {
                validationError = validator.getValidationMessage();
                isValidated = false;
                isChangeValidated = false;
            } else
            {
                validationError = "This is fine";
                isValidated = true;
                isChangeValidated = true;
            }
        }
        return;
    }
}
