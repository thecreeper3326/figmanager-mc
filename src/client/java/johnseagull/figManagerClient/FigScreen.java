package johnseagull.figManagerClient;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import johnseagull.figManager.Fig;
import johnseagull.figManager.FigGroup;
import johnseagull.figManager.FigManager;
import johnseagull.figManagerMC.DividerFig;
import johnseagull.figManagerMC.FigPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.logging.log4j.core.util.ReflectionUtil;

public class FigScreen<T extends AbstractWidget & Renderable> extends Screen {
    private static final boolean CUSTOM_GRADIENT = true;
    public static final boolean INVERT = false;
    public static final boolean AUTO_GRADIENT = false;
    private static int BACKGROUND_1 = -16777216;
    private static int BACKGROUND_2 = -1442840576;
    private static int LIGHT_1 = -7303024;
    private static int LIGHT_2 = -11513776;
    private static int DARK_1 = -10461088;
    private static int DARK_2 = -13619152;
    private static int BORDER_1 = -15395563;
    private static int BORDER_2 = -16777216;
    private static int PANEL_1 = -13619152;
    private static int PANEL_2 = -15724528;
    private static int PANEL_DARK_1 = -1442840576;
    private static int PANEL_DARK_2 = 1426063360;
    private static int TEXT = -1;
    private static int TEXT_INVALID = -57312;
    private static int SCROLL_1 = -15724528;
    private static int SCROLL_2 = -14671840;
    private static int BAD_1 = -8372160;
    private static int BAD_2 = -11522000;
    private static int HEADER = 22;
    public static int SPACING = 22;
    public boolean HAS_SERVER = true;
    public boolean canSave = true;
    public List<String> thingsKeepingYouFromSaving = new ArrayList();
    private int howFarYouveScrolled = 0;
    private int amountOfWidgetsOnScreen = 0;
    private int rows = 0;
    private Field[] fieldsInFigs;
    private List<T> coolListOfOptionWidgets = Lists.newArrayList();
    private final Screen parent;
    private int widthOfTheWidget;
    public float widthRatioFloat;
    public Map<String, Map<String, Object>> tempOptions = new HashMap();
    public Map<String, List<String>> tempStringList = new HashMap();
    FigButton save;
    private static final int tw = 6;
    int h;
    int a;
    FigBox thumb;

    public FigScreen(Component title, float optionWidth, Object figs, Screen parent, boolean hasServer) {
        super(title);
        this.save = new FigButton(this.width - 90, 7, 80, 20, Component.literal("Save"), (btn) -> {
            try {
                this.save(this.coolListOfOptionWidgets);
            } catch (IllegalAccessException e) {
                FigManagerClient.clientLogger.error(e.getMessage());
            }

        }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 2);
        this.thumb = new FigBox(this.width - 8, HEADER, 6, Math.max(20, (int)((double)this.height / (double)this.a * (double)this.h)), DARK_1, DARK_2, PANEL_DARK_1, PANEL_DARK_2, 1);
        this.HAS_SERVER = hasServer;
        this.parent = parent;
        if (optionWidth < 1.0F) {
            this.widthOfTheWidget = (int)(optionWidth * (float)this.width);
        } else {
            this.widthOfTheWidget = (int)optionWidth;
        }

        this.widthRatioFloat = optionWidth;
        Class<?> FIGCLASS = figs.getClass();
        this.fieldsInFigs = FIGCLASS.getDeclaredFields();
    }

    public void addOptions() throws IllegalAccessException {
        int y = HEADER;
        int x = 5;

        for(Field field : this.fieldsInFigs) {
            field.setAccessible(true);
            Object value = field.get(FigManager.FIGS);
            if (!field.getName().equals("instance")) {
                if (value instanceof FigGroup) {
                    FigGroup t = (FigGroup)value;
                    int c = t.columns;
                    int w = (this.width - c * 5) / c;
                    int widthOfTheWidgetw = (int)((float)w * t.ratio);
                    int wc = 1;

                    for(String s : t.value) {
                        int wx = 5 + (wc - 1) * w;

                        for(Field f : this.fieldsInFigs) {
                            if (f.getName().equals(s)) {
                                try {
                                    Field v = FigManager.FIGS.getClass().getField(s);
                                    Object vv = v.get(FigManager.FIGS);
                                    if (vv instanceof Fig) {
                                        Fig k = (Fig)vv;
                                        StringWidget label;
                                        if (t.hL) {
                                            label = new StringWidget(wx + widthOfTheWidgetw + 5, y + SPACING, w - widthOfTheWidgetw - 5, 20, Component.literal(k.name), this.font);
                                        } else {
                                            label = new StringWidget(wx, y + SPACING, w - 5, 20, Component.literal(k.name), this.font);
                                        }

                                        label.setTooltip(Tooltip.create(Component.literal(k.description)));
                                        this.addRenderableWidget(label);
                                        this.addWidget(f.getName(), k, wx, y + SPACING, widthOfTheWidgetw, 20);
                                    }
                                } catch (Exception e) {
                                    FigManagerClient.clientLogger.debug(e.getMessage());
                                }
                            }
                        }

                        ++wc;
                        if (!t.hL) {
                            if (wc > c) {
                                y += 44;
                                wc = 1;
                                this.amountOfWidgetsOnScreen += 2;
                                this.rows += 2;
                            }
                        } else if (wc > c) {
                            y += SPACING;
                            wc = 1;
                            ++this.amountOfWidgetsOnScreen;
                            ++this.rows;
                        }
                    }

                    if (wc > 1) {
                        y += SPACING;
                        ++this.amountOfWidgetsOnScreen;
                    }
                }

                if (value instanceof DividerFig) {
                    DividerFig t = (DividerFig)value;
                    y += SPACING;
                    if (field.getType() == DividerFig.class) {
                        Style style = Style.EMPTY;
                        style = style.withColor(t.color).withBold(t.bold).withItalic(t.italic).withUnderlined(t.underline);
                        if (!t.multiline) {
                            StringWidget label = new StringWidget(x, y, this.width - 30, 20, Component.literal(t.value).withStyle(style), this.font);
                            label.active = false;
                            this.addRenderableWidget(label);
                        }

                        if (t.multiline) {
                            StringWidget label = new StringWidget(x, y - 5, this.width - 30, 20, Component.literal(t.value).withStyle(style), this.font);
                            label.active = false;
                            this.addRenderableWidget(label);
                            StringWidget label2 = new StringWidget(x, y + 5, this.width - 30, 20, Component.literal(t.value2).withStyle(style), this.font);
                            label2.active = false;
                            this.addRenderableWidget(label2);
                        }

                        ++this.amountOfWidgetsOnScreen;
                    }
                }

                if (value instanceof Fig.ListFig) {
                    Fig.ListFig t = (Fig.ListFig)value;
                    t.rendered = true;
                    List<String> list = new ArrayList(t.value);
                    y += SPACING;
                    FigBox bg = new FigBox(0, y, this.width, 44 + t.dispLength * SPACING, PANEL_1, PANEL_2);
                    bg.active = false;
                    bg.inList = true;
                    StringWidget label = new StringWidget(x, y, this.width - 170, 20, Component.literal(t.name), this.font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                    EditBox k = new EditBox(this.font, x, y + SPACING, this.width - 14 - 5, 20, Component.literal(field.getName()));
                    k.setTooltip(Tooltip.create(Component.literal(t.valueDesc)));
                    FigList a = new FigList(x, y + SPACING * 2, this.width - 14, t.dispLength * SPACING, Component.empty(), false);
                    a.col1 = PANEL_DARK_1;
                    a.col2 = PANEL_DARK_2;
                    a.clearEntries();
                    a.list.addAll(list);
                    a.active = false;
                    this.coolListOfOptionWidgets.add((T) a);
                    String var10001 = t.dataType;
                    a.setMessage(Component.literal(var10001 + field.getName()));
                    FigButton clear = new FigButton(this.width - 65, y + 2, 50, 16, Component.literal("Clear"), (button) -> {
                        list.clear();
                        a.clearEntries();
                    }, BAD_1, BAD_2, BORDER_1, BORDER_2, 1);
                    clear.inList = true;
                    clear.setTooltip(Tooltip.create(Component.literal("Clears the whole list, be careful!").withStyle(ChatFormatting.RED)));
                    clear.setTooltipDelay(Duration.ofMillis(200L));
                    FigButton remove = new FigButton(this.width - 115, y + 2, 50, 16, Component.literal("Remove"), (button) -> {
                        list.remove(k.getValue());
                        a.clearEntries();

                        for(String entry : list) {
                            a.listAdd(entry);
                        }

                    }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 1);
                    remove.inList = true;
                    this.addRenderableWidget(clear);
                    this.addRenderableWidget(remove);
                    this.addRenderableWidget(k);
                    FigButton set = new FigButton(this.width - 165, y + 2, 50, 16, Component.literal("Set"), (button) -> {
                    }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 1);
                    set.inList = true;
                    this.addRenderableWidget(set);
                    y += SPACING;
                    set.onPress = (button) -> {
                        if (!k.getValue().equals("")) {
                            list.add(k.getValue());
                            a.clearEntries();

                            for(Object entry : list) {
                                a.listAdd(entry.toString());
                            }
                        }

                    };
                    y += t.dispLength * SPACING;
                    this.amountOfWidgetsOnScreen += t.dispLength + 2;
                    this.addRenderableWidget(a);
                }

                if (value instanceof Fig.MapFig) {
                    Fig.MapFig t = (Fig.MapFig)value;
                    t.rendered = true;
                    Map<String, String> map = new HashMap(t.value);
                    y += SPACING;
                    FigBox bg = new FigBox(0, y, this.width, 44 + t.dispLength * SPACING, PANEL_1, PANEL_2);
                    bg.active = false;
                    bg.inList = true;
                    StringWidget label = new StringWidget(x, y, this.width - 170, 20, Component.literal(t.name), this.font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                    EditBox k = new EditBox(this.font, x, y + SPACING, (this.width - 14) / 2 - 5, 20, Component.literal(field.getName()));
                    k.setTooltip(Tooltip.create(Component.literal(t.keyDesc)));
                    EditBox v = new EditBox(this.font, x + (this.width - 14) / 2, y + SPACING, (this.width - 14) / 2 - 5, 20, Component.literal(field.getName()));
                    v.setTooltip(Tooltip.create(Component.literal(t.valueDesc)));
                    FigList a = new FigList(x, y + SPACING * 2, this.width - 14, t.dispLength * SPACING, Component.empty(), true);
                    a.col1 = PANEL_DARK_1;
                    a.col2 = PANEL_DARK_2;
                    a.clearEntries();
                    a.map.putAll(map);
                    a.active = false;
                    this.coolListOfOptionWidgets.add((T) a);
                    String var56 = t.dataType;
                    a.setMessage(Component.literal(var56 + field.getName()));
                    FigButton clear = new FigButton(this.width - 65, y + 2, 50, 16, Component.literal("Clear"), (button) -> {
                        map.clear();
                        a.clearEntries();
                    }, BAD_1, BAD_2, BORDER_1, BORDER_2, 1);
                    clear.inList = true;
                    clear.setTooltip(Tooltip.create(Component.literal("Clears the whole list, be careful!").withStyle(ChatFormatting.RED)));
                    clear.setTooltipDelay(Duration.ofMillis(200L));
                    FigButton remove = new FigButton(this.width - 115, y + 2, 50, 16, Component.literal("Remove"), (button) -> {
                        map.remove(k.getValue());
                        a.clearEntries();
                        a.map.putAll(map);
                    }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 1);
                    remove.inList = true;
                    this.addRenderableWidget(clear);
                    this.addRenderableWidget(remove);
                    this.addRenderableWidget(k);
                    this.addRenderableWidget(v);
                    FigButton set = new FigButton(this.width - 165, y + 2, 50, 16, Component.literal("Set"), (button) -> {
                    }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 1);
                    set.inList = true;
                    this.addRenderableWidget(set);
                    y += SPACING;
                    set.onPress = (button) -> {
                        if (!k.getValue().equals("")) {
                            if (t.itemType.equals("string")) {
                                a.mapAdd(k.getValue(), v.getValue());
                            }

                            if (t.itemType.equals("int")) {
                                try {
                                    int tempV = Integer.parseInt(v.getValue());
                                    a.mapAdd(k.getValue(), String.valueOf(tempV));
                                } catch (NumberFormatException var8) {
                                    this.minecraft.gui.toastManager().addToast(new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Error").withStyle(ChatFormatting.RED), Component.nullToEmpty("Not a valid integer!")));
                                }
                            }

                            if (t.itemType.equals("float")) {
                                try {
                                    float tempV = Float.parseFloat(v.getValue());
                                    a.mapAdd(k.getValue(), String.valueOf(tempV));
                                } catch (NumberFormatException var7) {
                                    this.minecraft.gui.toastManager().addToast(new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Error").withStyle(ChatFormatting.RED), Component.nullToEmpty("Not a valid float!")));
                                }
                            }

                            if (t.itemType.equals("boolean")) {
                                if (!v.getValue().equalsIgnoreCase("true") && !v.getValue().equalsIgnoreCase("false")) {
                                    this.minecraft.gui.toastManager().addToast(new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Error").withStyle(ChatFormatting.RED), Component.nullToEmpty("Not a valid boolean!")));
                                } else {
                                    a.mapAdd(k.getValue(), v.getValue().toLowerCase());
                                }
                            }
                        }

                    };
                    y += t.dispLength * SPACING;
                    this.amountOfWidgetsOnScreen += t.dispLength + 2;
                    this.addRenderableWidget(a);
                }

                if (value instanceof Fig) {
                    Fig t = (Fig)value;
                    if (!t.inGroup && !(value instanceof Fig.CollectionFig)) {
                        y += SPACING;
                        this.addWidget(field.getName(), t, x, y, this.widthOfTheWidget, 20);
                        this.addLabel(t, y);
                        ++this.amountOfWidgetsOnScreen;
                    }
                }
            }
        }

    }

    public void setResponder(EditBox box, String type, Object min, Object max, String name) {
        box.setResponder((e) -> {
            try {
                if (type.equals("int")) {
                    int intMin = (Integer)min;
                    int intMax = (Integer)max;
                    if (Integer.parseInt(box.getValue()) <= intMax && Integer.parseInt(box.getValue()) >= intMin) {
                        box.setTextColor(TEXT);
                        this.thingsKeepingYouFromSaving.remove(name);
                    } else {
                        box.setTextColor(TEXT_INVALID);
                        if (!this.thingsKeepingYouFromSaving.contains(name)) {
                            this.thingsKeepingYouFromSaving.add(name);
                        }
                    }
                }

                if (type.equals("float")) {
                    float floatMin = (Float)min;
                    float floatMax = (Float)max;
                    if (!(Float.parseFloat(box.getValue()) > floatMax) && !(Float.parseFloat(box.getValue()) < floatMin)) {
                        box.setTextColor(TEXT);
                        this.thingsKeepingYouFromSaving.remove(name);
                    } else {
                        box.setTextColor(TEXT_INVALID);
                        if (!this.thingsKeepingYouFromSaving.contains(name)) {
                            this.thingsKeepingYouFromSaving.add(name);
                        }
                    }
                }

                if (type.equals("string")) {
                    int stringMax = (Integer)max;
                    box.setResponder((s) -> {
                        if (s.length() > stringMax) {
                            box.setTextColor(TEXT_INVALID);
                            if (!this.thingsKeepingYouFromSaving.contains(name)) {
                                this.thingsKeepingYouFromSaving.add(name);
                            }
                        } else {
                            box.setTextColor(TEXT);
                            this.thingsKeepingYouFromSaving.remove(name);
                        }

                    });
                }
            } catch (Exception var9) {
                if (!this.thingsKeepingYouFromSaving.contains(name)) {
                    this.thingsKeepingYouFromSaving.add(name);
                }

                box.setTextColor(TEXT_INVALID);
            }

        });
    }

    public void addWidget(String name, Fig fig, int x, int y, int w, int h) {
        if (!fig.rendered) {
            if (fig.widgetType.equals("box")) {
                EditBox box = new EditBox(this.font, x, y, w, h, Component.literal(name));
                box.setMessage(Component.literal(fig.dataType + name));
                box.setMaxLength(1024);
                if (fig.dataType.equals("int_")) {
                    Fig.IntFig t = (Fig.IntFig)fig;
                    box.setValue(String.valueOf(t.value));
                    this.setResponder(box, "int", t.min, t.max, name);
                    this.coolListOfOptionWidgets.add((T) box);
                }

                if (fig.dataType.equals("float_")) {
                    Fig.FloatFig t = (Fig.FloatFig)fig;
                    box.setValue(String.valueOf(t.value));
                    box.setMessage(Component.literal(t.dataType + name));
                    this.setResponder(box, "float", t.min, t.max, name);
                    this.coolListOfOptionWidgets.add((T) box);
                }

                if (fig.dataType.equals("string_")) {
                    assert fig instanceof Fig.StringFig;

                    Fig.StringFig t = (Fig.StringFig)fig;
                    box.setValue(t.value);
                    this.setResponder(box, "string", 0, t.max, name);
                    box.setMessage(Component.literal(t.dataType + name));
                    this.coolListOfOptionWidgets.add((T) box);
                }

                this.addRenderableWidget(box);
                fig.rendered = true;
            }

            if (fig.widgetType.equals("check")) {
                Fig.BooleanFig t = (Fig.BooleanFig)fig;
                FigCheckbox toggle = FigCheckbox.builder(Component.literal(name), this.font).selected(t.value).build();
                toggle.setX(x);
                toggle.setY(y + 3);
                toggle.w = w;
                toggle.col1 = LIGHT_1;
                toggle.col2 = LIGHT_2;
                toggle.bdr1 = PANEL_DARK_1;
                toggle.bdr2 = PANEL_DARK_2;
                this.coolListOfOptionWidgets.add((T) toggle);
                this.addRenderableWidget(toggle);
                fig.rendered = true;
            }
        }

        fig.inGroup = true;
    }

    public void addLabel(Fig t, int y) {
        StringWidget label = new StringWidget(Math.round((float)this.widthOfTheWidget) + 10, y + 1, this.width - Math.round((float)this.widthOfTheWidget) - 30, 20, Component.literal(t.name), this.font);
        label.setTooltip(Tooltip.create(Component.literal(t.description)));
        this.addRenderableWidget(label);
    }

    public void save(List<T> options) throws IllegalAccessException {
        Field[] fields = FigManager.FIGS.getClass().getDeclaredFields();

        for(T option : options) {
            for(Field field : fields) {
                try {
                    Object value = field.get(FigManager.FIGS);
                    String msg = option.getMessage().getString();
                    if (option instanceof FigList l) {
                        msg = option.getMessage().getString();

                        try {
                            if (value instanceof Fig.ListFig f) {
                                String var10001 = f.dataType;
                                if (msg.equals(var10001 + field.getName())) {
                                    Fig.ListFig tempFig = new Fig.ListFig(f.name, f.description, f.maxLength, f.dispLength, f.valueDesc);
                                    tempFig.value.addAll(l.list);
                                    ReflectionUtil.setFieldValue(field, FigManager.FIGS, tempFig);
                                    break;
                                }
                            }

                            if (value instanceof Fig.MapFig f) {
                                String var35 = f.dataType;
                                if (msg.equals(var35 + field.getName())) {
                                    Fig.MapFig tempFig = new Fig.MapFig(f.name, f.description, f.maxLength, f.dispLength, f.itemType, f.keyDesc, f.valueDesc);
                                    tempFig.value.putAll(l.map);
                                    ReflectionUtil.setFieldValue(field, FigManager.FIGS, tempFig);
                                    break;
                                }
                            }
                        } catch (NumberFormatException var15) {
                            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Format error for " + String.valueOf(option.getMessage())).withStyle(ChatFormatting.RED));
                        }
                    }

                    if (option instanceof EditBox) {
                        try {
                            msg = option.getMessage().getString();
                            if (value instanceof Fig.IntFig) {
                                Fig.IntFig f = (Fig.IntFig)value;
                                String var36 = f.dataType;
                                if (msg.equals(var36 + field.getName())) {
                                    ReflectionUtil.setFieldValue(field, FigManager.FIGS, new Fig.IntFig(f.name, f.description, Integer.parseInt(((EditBox)option).getValue()), f.min, f.max));
                                    break;
                                }
                            }

                            if (value instanceof Fig.FloatFig) {
                                Fig.FloatFig f = (Fig.FloatFig)value;
                                String var37 = f.dataType;
                                if (msg.equals(var37 + field.getName())) {
                                    ReflectionUtil.setFieldValue(field, FigManager.FIGS, new Fig.FloatFig(f.name, f.description, Float.parseFloat(((EditBox)option).getValue()), f.min, f.max));
                                    break;
                                }
                            }

                            if (value instanceof Fig.StringFig) {
                                Fig.StringFig f = (Fig.StringFig)value;
                                String var38 = f.dataType;
                                if (msg.equals(var38 + field.getName())) {
                                    ReflectionUtil.setFieldValue(field, FigManager.FIGS, new Fig.StringFig(f.name, f.description, ((EditBox)option).getValue(), f.max));
                                    break;
                                }
                            }
                        } catch (NumberFormatException var16) {
                            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Format error for " + String.valueOf(option.getMessage())).withStyle(ChatFormatting.RED));
                        }
                    }

                    if (option instanceof FigCheckbox) {
                        msg = option.getMessage().getString();
                        if (value instanceof Fig.BooleanFig) {
                            Fig.BooleanFig f = (Fig.BooleanFig)value;
                            if (msg.equals(field.getName()) && field.getType() == Fig.BooleanFig.class) {
                                ReflectionUtil.setFieldValue(field, FigManager.FIGS, new Fig.BooleanFig(f.name, f.description, ((FigCheckbox)option).selected()));
                                break;
                            }
                        }
                    }
                } catch (NullPointerException | IllegalAccessException var17) {
                }
            }
        }

        List<Object> newFigs = FigManager.validate(FigManager.FIGS);
        Object correctedFigs = newFigs.get(0);
        int errorCount = (Integer)newFigs.get(1);
        List<String> errors = (List)newFigs.get(2);
        if (this.HAS_SERVER) {
            if (errorCount != 0) {
                this.minecraft.gui.toastManager().addToast(new SystemToast(SystemToastId.NARRATOR_TOGGLE, Component.literal("Error").withStyle(ChatFormatting.RED), Component.nullToEmpty(errorCount + " errors occurred, see chat/logs")));
            }

            try {
                if (errorCount == 0) {
                    this.onClose();
                } else {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal(errorCount + " options failed to process:").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}));
                    FigManagerClient.clientLogger.error(errorCount + " errors occured:");

                    for(String error : errors) {
                        FigManagerClient.clientLogger.error(error);
                    }

                    Minecraft.getInstance().player.sendSystemMessage(Component.literal(""));

                    for(String error : errors) {
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal(error).withStyle(ChatFormatting.RED));
                    }

                    Minecraft.getInstance().player.sendSystemMessage(Component.literal(""));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Figs that were invalid were reset.").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}));
                }

                ClientPlayNetworking.send(new FigPacket(FigManager.toString(correctedFigs)));
                FigManager.FIGS = correctedFigs;
                FigManager.save(FigManager.name);
            } catch (NullPointerException | IllegalStateException var14) {
                if (errorCount != 0) {
                    this.minecraft.gui.toastManager().addToast(new SystemToast(SystemToastId.NARRATOR_TOGGLE, Component.literal("Warning").withStyle(ChatFormatting.YELLOW), Component.nullToEmpty("Saved valid figs, invalid ones were reset")));
                } else {
                    this.onClose();
                }

                FigManager.FIGS = correctedFigs;
                FigManager.save(FigManager.name);
            }
        } else {
            FigManager.FIGS = correctedFigs;
            FigManager.save(FigManager.name);
            this.onClose();
        }

        this.tempStringList.clear();
    }

    protected void init() {
        for(Field field : this.fieldsInFigs) {
            field.setAccessible(true);

            try {
                Object value = field.get(FigManager.FIGS);
                if (value instanceof Fig f) {
                    f.rendered = false;
                    f.inGroup = false;
                }
            } catch (Exception var10) {
                FigManagerClient.clientLogger.error("Resetting fig render states failed, may fail to render on next open.");
            }
        }

        FigManager.rebuildIDs();
        if (this.widthRatioFloat < 1.0F) {
            this.widthOfTheWidget = (int)(this.widthRatioFloat * (float)this.width);
        } else {
            this.widthOfTheWidget = (int)this.widthRatioFloat;
        }

        this.amountOfWidgetsOnScreen = 0;
        this.howFarYouveScrolled = 0;
        ((FigBox)this.addRenderableWidget(new FigBox(0, 0, this.width, this.height / 2, BACKGROUND_1, BACKGROUND_2))).active = false;
        ((FigBox)this.addRenderableWidget(new FigBox(0, this.height / 2, this.width, this.height / 2, BACKGROUND_2, BACKGROUND_1))).active = false;

        try {
            this.addOptions();
        } catch (IllegalAccessException var9) {
        }

        ((FigBox)this.addRenderableWidget(new FigBox(0, this.height - HEADER, this.width, 15, 0, -1442840576))).active = false;
        ((FigBox)this.addRenderableWidget(new FigBox(0, HEADER, this.width, 15, -1442840576, 0))).active = false;
        ((FigBox)this.addRenderableWidget(new FigBox(this.width - 10, 0, 10, this.height, SCROLL_1, SCROLL_2))).active = false;
        ((FigBox)this.addRenderableWidget(new FigBox(0, 0, this.width, HEADER, PANEL_1, PANEL_2))).active = false;
        ((FigBox)this.addRenderableWidget(new FigBox(0, this.height - 17, this.width, 17, PANEL_1, PANEL_2))).active = false;
        int w = this.width;
        FigButton close = new FigButton(this.width - 165, Math.max(2, HEADER / 2 - 8), 75, 16, Component.literal("Discard"), (btn) -> {
            this.tempOptions.clear();
            this.tempStringList.clear();
            this.onClose();
        }, BAD_1, BAD_2, BORDER_1, BORDER_2, 1);
        this.addRenderableWidget(close);
        int th = this.height - HEADER - 20;
        int ch = this.amountOfWidgetsOnScreen * SPACING;
        int tth = Math.max(10, (int)((float)th / (float)ch * (float)th));
        this.thumb = new FigBox(this.width - 8, HEADER, 6, tth, DARK_1, DARK_2, BORDER_1, BORDER_2, 1);
        this.addRenderableWidget(this.thumb);
        this.save = new FigButton(this.width - 90, Math.max(2, HEADER / 2 - 8), 75, 16, Component.literal("Save"), (btn) -> {
            try {
                this.save(this.coolListOfOptionWidgets);
            } catch (IllegalAccessException e) {
                FigManagerClient.clientLogger.error(e.getMessage());
            }

        }, LIGHT_1, LIGHT_2, BORDER_1, BORDER_2, 1);
        this.addRenderableWidget(this.save);
        StringWidget title = new StringWidget(10, Math.max(2, HEADER / 2 - 8), 1000, 15, Component.literal(FigManager.name + " - Fig menu"), this.font);
        StringWidget subtitle = new StringWidget(10, this.height - 13, 1000, 10, Component.literal(FigManager.name + FigManager.version).withStyle(ChatFormatting.GRAY), this.font);
        StringWidget credit = new StringWidget(this.width - 100, this.height - 13, 1000, 10, Component.literal("TheCreeper3326").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}), this.font);
        title.active = true;
        subtitle.active = true;
        credit.active = true;
        this.addRenderableWidget(credit);
        this.addRenderableWidget(title);
        this.addRenderableWidget(subtitle);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean didTheListEatTheScrollEvent = false;

        for(Object child : this.children()) {
            if (child instanceof FigList w) {
                if (w.hovered) {
                    didTheListEatTheScrollEvent = w.shift(scrollY);
                    break;
                }
            }
        }

        int max = this.amountOfWidgetsOnScreen * SPACING - (this.height - HEADER - 20) + 40;
        if (!didTheListEatTheScrollEvent) {
            if (scrollY < (double)0.0F && Math.abs(this.howFarYouveScrolled) < max) {
                this.shift((int)scrollY * SPACING);
            } else if (scrollY > (double)0.0F && this.howFarYouveScrolled < 0) {
                this.shift((int)scrollY * SPACING);
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(KeyEvent event) {
        int max = this.amountOfWidgetsOnScreen * SPACING - (this.height - 55) + 40;
        if (event.isDown() && Math.abs(this.howFarYouveScrolled) < max) {
            this.shift(SPACING);
        } else if (event.isUp() && this.howFarYouveScrolled < 0) {
            this.shift(-SPACING);
        }

        return super.keyPressed(event);
    }

    public void shift(int offset) {
        this.howFarYouveScrolled += offset;

        for(Object child : this.children()) {
            if (child instanceof EditBox widget) {
                widget.setY(widget.getY() + offset);
            }

            if (child instanceof FigCheckbox widget) {
                widget.setY(widget.getY() + offset);
            }

            if (child instanceof StringWidget widget) {
                if (!widget.active) {
                    widget.setY(widget.getY() + offset);
                }
            }

            if (child instanceof FigButton widget) {
                if (widget.inList) {
                    widget.setY(widget.getY() + offset);
                }
            }

            if (child instanceof FigToggleButton widget) {
                if (widget.inList) {
                    widget.setY(widget.getY() + offset);
                }
            }

            if (child instanceof FigList widget) {
                widget.Y += offset;
            }

            if (child instanceof FigBox widget) {
                if (widget.inList) {
                    widget.y += offset;
                }
            }

            int max = this.amountOfWidgetsOnScreen * SPACING - (this.height - (HEADER + 20)) + 40;
            float progress = (float)this.howFarYouveScrolled / (float)max;
            int trackSpace = this.height - (HEADER + 20) - this.thumb.height;
            this.thumb.y = HEADER - (int)(Math.min(progress, 1.0F) * (float)trackSpace);
        }

    }

    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (this.thingsKeepingYouFromSaving.isEmpty()) {
            this.save.col1 = LIGHT_1;
            this.save.col2 = LIGHT_2;
            this.save.setTooltip(Tooltip.create(Component.literal("")));
            this.save.active = true;
        } else {
            this.save.col1 = (new Color(LIGHT_1, true)).darker().getRGB();
            this.save.col2 = (new Color(LIGHT_2, true)).darker().getRGB();
            this.save.setTooltip(Tooltip.create(Component.literal("Invalid Fig Values, check the following: " + String.valueOf(this.thingsKeepingYouFromSaving))));
            this.save.active = false;
        }

        for(Object c : this.children()) {
            if (c instanceof StringWidget w) {
                if (!w.active) {
                    if (w.getY() < 20) {
                        w.setTooltipDelay(Duration.ofDays(100000L));
                    } else {
                        w.setTooltipDelay(Duration.ofMillis(0L));
                    }
                }
            }

            if (c instanceof EditBox w) {
                if (w.getY() < 30) {
                    w.visible = false;
                } else {
                    w.visible = true;
                }
            }

            if (c instanceof FigCheckbox w) {
                if (w.getY() < 30) {
                    w.visible = false;
                } else {
                    w.visible = true;
                }
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}
