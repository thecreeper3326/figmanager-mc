package net.johnseagull.figManagerMC;

import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

/**
     * Special class that can be used to add 1-2 liens of formatted text in the config menu.
     * <br>
     * If provided with 2 strings, both will be rendered on separate lines.
     * */
    public class DividerFig {
        public boolean multiline;
        public String value1;
        public String value2;
        public ChatFormatting color;
        public boolean bold = false;
        public boolean italic = false;
        public boolean underline = false;
        /**
         * Constructor for single-line labels
         *
         * @param value1 The string to be displayed
         * @param color Color in the form of a ChatFormatting Object<br>
         * <code>bold, italic, underline</code> - Define extra formatting
         * */
        public DividerFig(String value1, @Nullable ChatFormatting color, boolean bold, boolean italic, boolean underline ) {
            this.value1 = value1;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            multiline = false;
        }
        /**
         * Contructor for multi-line labels
         *
         * @param value1 The string to be displayed on the top line
         * @param value2 The string to be displayed on the bottom line
         * @param color Color in the form of a ChatFormatting Object<br>
         * <code>bold, italic, underline</code> - Define extra formatting
         * */
        public DividerFig(String value1, String value2, @Nullable ChatFormatting color, boolean bold, boolean italic, boolean underline ) {
            this.value1 = value1;
            this.value2 = value2;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            multiline = true;
        }


    }