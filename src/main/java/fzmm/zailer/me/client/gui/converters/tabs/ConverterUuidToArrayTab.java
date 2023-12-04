package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ConfigTextBoxRow;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class ConverterUuidToArrayTab implements IScreenTab {
    private static final String UUID_FIELD_ID = "uuidField";
    private static final String RANDOM_ID = "uuidToArray.random";
    private static final String COPY_ID = "uuidToArray.copy";

    @Override
    public String getId() {
        return "uuidToArray";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setupComponents(FlowLayout rootComponent) {
        ConfigTextBox uuidField = ConfigTextBoxRow.setup(rootComponent, UUID_FIELD_ID, "");
        uuidField.applyPredicate(s -> {
            try {
                UUID ignored = UUID.fromString(s);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(RANDOM_ID), true,
                button -> {
                    uuidField.setText(UUID.randomUUID().toString());
                    uuidField.setCursor(Position.ZERO);

                });
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ID), true, button -> {
            if (!uuidField.isValid())
                return;

            String stringOfUuidArray = this.stringOfUUIDtoArray(uuidField.getText());
            MinecraftClient.getInstance().keyboard.setClipboard(stringOfUuidArray);
        });
    }

    public static int[] UUIDtoArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        int[] intArray = new int[4];

        intArray[0] = (int) (msb >> 32);
        intArray[1] = (int) (msb);
        intArray[2] = (int) (lsb >> 32);
        intArray[3] = (int) (lsb);

        return intArray;
    }


    public String stringOfUUIDtoArray(String uuidString) {
        int[] uuidArray = UUIDtoArray(UUID.fromString(uuidString));

        return String.format("[I;%s,%s,%s,%s]", uuidArray[0], uuidArray[1], uuidArray[2], uuidArray[3]);
    }
}
