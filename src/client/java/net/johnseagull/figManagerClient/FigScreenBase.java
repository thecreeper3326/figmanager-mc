package net.johnseagull.figManagerClient;


import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.johnseagull.figManager.Fig;
import net.johnseagull.figManager.FigGroup;
import net.johnseagull.figManager.FigManager;
import net.johnseagull.figManagerMC.DividerFig;
import net.johnseagull.figManagerMC.FigPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.awt.*;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;
import static net.johnseagull.figManagerClient.FigManagerClient.clientLogger;
import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;

@SuppressWarnings("unchecked")
public class FigScreenBase<T extends AbstractWidget & Renderable> extends Screen {

    private static final boolean CUSTOM_GRADIENT = true;
    public static final boolean INVERT = false;
    public static final boolean AUTO_GRADIENT = false;

    private static int BACKGROUND_1 = 0xFF000000;
    private static int BACKGROUND_2 = 0xAA000000;

    private static int LIGHT_1 = 0xFF909090;
    private static int LIGHT_2 = 0xFF505050;

    private static int DARK_1 = 0xFF606060;
    private static int DARK_2 = 0xFF303030;

    private static int BORDER_1 = 0xFF151515;
    private static int BORDER_2 = 0xFF000000;

    private static int PANEL_1 = 0xFF303030;
    private static int PANEL_2 = 0xFF101010;

    private static int PANEL_DARK_1 = 0xAA000000;
    private static int PANEL_DARK_2 = 0x55000000;

    private static int TEXT = 0xFFFFFFFF;
    private static int TEXT_INVALID = 0xFFFF2020;

    private static int SCROLL_1 = 0xFF101010;
    private static int SCROLL_2 = 0xFF202020;

    private static int BAD_1 = 0xFF804040;
    private static int BAD_2 = 0xFF503030;

    private static int HEADER = 22;

    public static int SPACING = 22;

    public boolean HAS_SERVER = true;

    public boolean canSave = true;
    public List<String> thingsKeepingYouFromSaving = new ArrayList<>();
    private int howFarYouveScrolled = 0;
    private int amountOfWidgetsOnScreen = 0;
    private int rows = 0;
    private Field[] fieldsInFigs;
    public List<T> coolListOfOptionWidgets = Lists.newArrayList();
    private final Screen parent;
    public int widthOfTheWidget;
    public float widthRatioFloat;
    public Map<String, Map<String,Object>> tempOptions = new HashMap<>();
    public Map<String, List<String>> tempStringList =  new HashMap<>();
    public String name = "";

    private static final int tw = 6;
    int h;
    int a;
    FigBox thumb = new FigBox(
            width - 8,
            HEADER,
            6,
            max(20, (int) ((double) height / a * h)), DARK_1, DARK_2, PANEL_DARK_1, PANEL_DARK_2, 1);

    public FigScreenBase(Component title, float optionWidth, Object figs, Screen parent, boolean hasServer) {
        super(title);
        this.HAS_SERVER = hasServer;
        this.parent = parent;
        if (optionWidth < 1f) {
            widthOfTheWidget = (int) (optionWidth * width);
        } else {
            widthOfTheWidget = (int) optionWidth;
        }
        widthRatioFloat = optionWidth;
        Class<?> FIGCLASS = figs.getClass();
        fieldsInFigs = FIGCLASS.getDeclaredFields();
        if (!CUSTOM_GRADIENT) {
            BACKGROUND_2 = BACKGROUND_1;
            LIGHT_2 = LIGHT_1;
            BORDER_2 = BORDER_1;
            DARK_2 = DARK_1;
            PANEL_2 = PANEL_1;
            BAD_2 = BAD_1;
            PANEL_DARK_2 = PANEL_DARK_1;
        }
        if (AUTO_GRADIENT) {
            BACKGROUND_2 = new Color(BACKGROUND_1, true).darker().getRGB();
            LIGHT_2 = new Color(LIGHT_1, true).darker().getRGB();
            BORDER_2 = new Color(BORDER_1, true).darker().getRGB();
            DARK_2 = new Color(DARK_1, true).darker().getRGB();
            PANEL_2 = new Color(PANEL_1, true).darker().getRGB();
            BAD_2 = new Color(BAD_1, true).darker().getRGB();
            PANEL_DARK_2 = new Color(PANEL_DARK_1, true).darker().getRGB();
        }
    }

    public void addOptions() throws IllegalAccessException {

    }
    public void setResponder(EditBox box, String type, Object min, Object max, String name) {
        box.setResponder(e -> {
            try {
                if (type.equals("int")) {
                    int intMin = (int) min;
                    int intMax = (int) max;
                    if (Integer.parseInt(box.getValue()) > intMax || Integer.parseInt(box.getValue()) < intMin) {
                        box.setTextColor(TEXT_INVALID);
                        if (!thingsKeepingYouFromSaving.contains(name)) {
                            thingsKeepingYouFromSaving.add(name);
                        }
                    } else {
                        box.setTextColor(TEXT);
                        thingsKeepingYouFromSaving.remove(name);
                    }
                }
                if (type.equals("float")) {
                    float floatMin = (float) min;
                    float floatMax = (float) max;
                    if (Float.parseFloat(box.getValue()) > floatMax || Float.parseFloat(box.getValue()) < floatMin) {
                        box.setTextColor(TEXT_INVALID);
                        if (!thingsKeepingYouFromSaving.contains(name)) {
                            thingsKeepingYouFromSaving.add(name);
                        }
                    } else {
                        box.setTextColor(TEXT);
                        thingsKeepingYouFromSaving.remove(name);
                    }
                }
                if (type.equals("string")) {
                    int stringMax = (int) max;
                    box.setResponder(s -> {
                        if (s.length() > stringMax) {
                            box.setTextColor(TEXT_INVALID);
                            if (!thingsKeepingYouFromSaving.contains(name)) {
                                thingsKeepingYouFromSaving.add(name);
                            }
                        } else {
                            box.setTextColor(TEXT);
                            thingsKeepingYouFromSaving.remove(name);

                        }
                    });
                }

            } catch (Exception ex) {
                if (!thingsKeepingYouFromSaving.contains(name)) {
                    thingsKeepingYouFromSaving.add(name);
                }
                box.setTextColor(TEXT_INVALID);
            }
        });
    }


    protected void init() {

        for (Field field : fieldsInFigs) {
            field.setAccessible(true);
            try {
                Object value = field.get(FigManager.FIGS);
                if (value instanceof Fig f) {
                    f.rendered = false;
                    f.inGroup = false;
                }
            } catch (Exception e) {
                clientLogger.error("Resetting fig render states failed, may fail to render on next open.");
            }
        }

        FigManager.rebuildIDs();
        if (widthRatioFloat < 1f) {
            widthOfTheWidget = (int) (widthRatioFloat * width);
        } else {
            widthOfTheWidget = (int) widthRatioFloat;
        }
        amountOfWidgetsOnScreen = 0;
        howFarYouveScrolled = 0;
        if (INVERT) {
            this.addRenderableWidget(new FigBox(0, 0, width, height / 2, BACKGROUND_2, BACKGROUND_1)).active = false;
            this.addRenderableWidget(new FigBox(0, height / 2, width, height / 2, BACKGROUND_1, BACKGROUND_2)).active = false;
        } else {
            this.addRenderableWidget(new FigBox(0, 0, width, height / 2, BACKGROUND_1, BACKGROUND_2)).active = false;
            this.addRenderableWidget(new FigBox(0, height / 2, width, height / 2, BACKGROUND_2, BACKGROUND_1)).active = false;
        }
        try {
            addOptions();

        } catch (IllegalAccessException e) {
        }
        this.addRenderableWidget(new FigBox(0, height - HEADER, width, 15, 0x00000000, 0xAA000000)).active = false;
        this.addRenderableWidget(new FigBox(0, HEADER, width, 15, 0xAA000000, 0x00000000)).active = false;
        this.addRenderableWidget(new FigBox(width - 10, 0, 10, height, SCROLL_1, SCROLL_2)).active = false;
        this.addRenderableWidget(new FigBox(0, 0, width, HEADER, PANEL_1, PANEL_2)).active = false;
        this.addRenderableWidget(new FigBox(0, height - 17, width, 17, PANEL_1, PANEL_2)).active = false;

        int w = width;
        int th = height - HEADER - 20;
        int ch = amountOfWidgetsOnScreen * SPACING;
        int tth = Math.max(10, (int)((float) th / ch * th));
        thumb = new FigBox(
                width - 8,
                HEADER,
                6,
                tth, DARK_1, DARK_2, BORDER_1, BORDER_2, 1);
        this.addRenderableWidget(thumb);

        StringWidget title = new StringWidget(10, Math.max(2,((HEADER/2)-8)), 1000, 15, Component.literal(name), font);
        StringWidget subtitle = new StringWidget(10, height - 13, 1000, 10, Component.literal(FigManager.name + FigManager.version).withStyle(ChatFormatting.GRAY), font);
        StringWidget credit = new StringWidget(width - 100, height - 13, 1000, 10, Component.literal("TheCreeper3326").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), font);
        title.active = true;
        subtitle.active = true;
        credit.active = true;
        this.addRenderableWidget(credit);
        this.addRenderableWidget(title);
        this.addRenderableWidget(subtitle);



    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {


        boolean didTheListEatTheScrollEvent = false;
        for (Object child : this.children()) {
            if (child instanceof FigList w && w.hovered) {
                didTheListEatTheScrollEvent = w.shift(scrollY);
                break;
            }
        }

        int max = (amountOfWidgetsOnScreen * SPACING) - (height - HEADER-20) + 40;
        if (!didTheListEatTheScrollEvent) {
            if (scrollY < 0 && abs(howFarYouveScrolled) < max) {
                shift((int) scrollY * SPACING);
            } else if (scrollY > 0 && howFarYouveScrolled < 0) {
                shift((int) scrollY * SPACING);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int max = (amountOfWidgetsOnScreen * SPACING) - (height - 55) + 40;
        if (event.isDown() && abs(howFarYouveScrolled) < max) {
            shift(SPACING);
        } else if (event.isUp() && howFarYouveScrolled < 0) {
            shift(-SPACING);
        }
        return super.keyPressed(event);
    }

    public void shift(int offset) {

        howFarYouveScrolled += offset;
        for (Object child : this.children()) {
            if (child instanceof EditBox widget) {
                widget.setY((widget.getY() + offset));
            }
            if (child instanceof FigCheckbox widget) {
                widget.setY((widget.getY() + offset));
            }
            if (child instanceof StringWidget widget) {
                if (!widget.active) {
                    widget.setY((widget.getY() + offset));
                }
            }
            if (child instanceof FigButton widget) {
                if (widget.inList) {
                    widget.setY((widget.getY() + offset));
                }
            }
            if (child instanceof FigToggleButton widget) {
                if (widget.inList) {
                    widget.setY((widget.getY() + offset));
                }
            }
            if (child instanceof FigList widget) {
               widget.Y += offset;
            }
            if (child instanceof FigBox widget) {
                if(widget.inList){
                    widget.y += offset;
                }
            }


            int max = ((amountOfWidgetsOnScreen) * SPACING) - (height - (HEADER+20)) +40;
            float progress = ((float) howFarYouveScrolled / max);
            int trackSpace = (height - (HEADER + 20)) - thumb.height;
            thumb.y = HEADER - (int) (min(progress, 1.0f) * trackSpace);
        }
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        for (Object c : this.children()) {
            if (c instanceof StringWidget w) {
                if (!w.active) {
                    if (w.getY()<20) {
                        w.setTooltipDelay(Duration.ofDays(100000));
                    } else {
                        w.setTooltipDelay(Duration.ofMillis(0));
                    }

                }
            }
            if (c instanceof EditBox w) {
                if (w.getY()<30) {
                    w.visible = false;
                } else {
                    w.visible = true;
                }
            }
            if (c instanceof FigCheckbox w) {
                if (w.getY()<30) {
                    w.visible = false;
                } else {
                    w.visible = true;
                }

            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);

    }
}
