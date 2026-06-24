
package net.johnseagull.figManagerClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FigList extends AbstractWidget {
    public List<String> widgets = new ArrayList();
    public List<String> list = new ArrayList();
    public Map<String, String> map = new HashMap();
    public String msg;
    private int y = 0;
    private int X = 0;
    public int Y = 0;
    public int count = 0;
    public int offset = 0;
    private int mx = 0;
    private int my = 0;
    public int columns = 1;
    public boolean isMap = false;
    boolean hovered = false;
    public int col1 = -16777216;
    public int col2 = 285212672;
    boolean temp = false;

    public FigList(int x, int yy, int width, int height, Component message, boolean isMap) {
        super(x, yy, width, height, message);
        this.Y = yy;
        this.X = x;
        this.isMap = isMap;
    }



    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        int tempC1 = this.col1;
        int tempC2 = this.col2;
        FigBox bgTop = new FigBox(0, this.Y, this.width + 4, this.height / 2, tempC1, tempC2);
        bgTop.renderWidget(graphics, mouseX, mouseY, a);
        FigBox bgBottom = new FigBox(0, this.Y + this.height / 2, this.width + 4, this.height / 2, tempC2, tempC1);
        bgBottom.renderWidget(graphics, mouseX, mouseY, a);
        this.y = this.Y + this.offset + 5;
        if (this.isMap) {
            this.count = this.map.size();
        } else {
            this.count = this.list.size();
        }

        if (this.isMap) {
            for(Map.Entry<String, String> entry : this.map.entrySet()) {
                if (this.y >= this.Y && this.y <= this.Y + this.height) {
                    graphics.drawString(Minecraft.getInstance().font, (String)entry.getKey(), this.X, this.y, -1);
                    graphics.drawString(Minecraft.getInstance().font, (String)entry.getValue(), this.X + this.width / 2, this.y, -1);
                }

                this.y += FigScreen.SPACING;
            }
        } else {
            for(String s : this.list) {
                if (this.y >= this.Y && this.y <= this.Y + this.height) {
                    graphics.drawString(Minecraft.getInstance().font, s, this.X, this.y, -1);
                }

                this.y += FigScreen.SPACING;
            }
        }

        this.mx = mouseX;
        this.my = mouseY;
        FigBox thumb = new FigBox(this.width - 8, this.Y, 6, 20, -10461088, -13619152, -14671840, -16777216, 1);
        int max = this.count * FigScreen.SPACING - this.height;
        float progress = (float)this.offset / (float)max;
        int trackSpace = this.height - thumb.height;
        thumb.y = Math.min(this.Y - (int)(Math.min(progress, 1.0F) * (float)trackSpace), this.Y + this.height - thumb.height);
        if (this.count > this.height / FigScreen.SPACING) {
            thumb.renderWidget(graphics, mouseX, mouseY, a);
        }

        if (this.my > this.Y && this.my < this.Y + this.height) {
            this.hovered = true;
        } else {
            this.hovered = false;
        }

    }

    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public boolean shift(double amount) {
        if (amount > (double)0.0F && this.offset >= 0) {
            return false;
        } else if (amount < (double)0.0F && this.offset <= -this.count * FigScreen.SPACING + this.height) {
            return false;
        } else {
            if (amount > (double)0.0F) {
                this.offset += (int)amount * FigScreen.SPACING;
            } else {
                this.offset += (int)amount * FigScreen.SPACING;
            }

            return true;
        }
    }

    public void mapAdd(String key, String value) {
        this.map.put(key, value);
    }

    public void mapSet(Map<String, String> map) {
        this.clearEntries();
        this.map.putAll(map);
    }

    public void mapRemove(String key) {
        this.map.remove(key);
    }

    public void listAdd(String txt) {
        this.list.add(txt);
    }

    public void clearEntries() {
        this.map.clear();
        this.list.clear();
    }
}
