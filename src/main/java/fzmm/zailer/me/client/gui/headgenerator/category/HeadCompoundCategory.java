package fzmm.zailer.me.client.gui.headgenerator.category;

import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class HeadCompoundCategory implements IHeadCategory {
    public static final String CATEGORY_ID = "compound";

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.category.compound";
    }

    @Override
    public boolean isCategory(AbstractHeadEntry entry, String categoryId) {
        return categoryId.equals(CATEGORY_ID);
    }

    @Override
    public Text getText() {
        return Text.translatable(this.getTranslationKey() + ".message").setStyle(Style.EMPTY.withColor(0x4492BB));
    }

}
