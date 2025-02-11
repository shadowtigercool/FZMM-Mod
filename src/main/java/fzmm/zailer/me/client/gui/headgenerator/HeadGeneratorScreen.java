package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.headgenerator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.headgenerator.components.AbstractHeadListEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadCompoundComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.options.SkinPreEditOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.headGenerator.TextureOverlap;
import fzmm.zailer.me.client.logic.headGenerator.model.HeadModelEntry;
import fzmm.zailer.me.utils.*;
import fzmm.zailer.me.utils.list.IListEntry;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends BaseFzmmScreen implements IMementoScreen {
    private static final int COMPOUND_HEAD_LAYOUT_WIDTH = 60;
    public static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String SKIN_PRE_EDIT_OPTION_ID = "skinPreEdit";
    private static final String SEARCH_ID = "search";
    private static final String CONTENT_ID = "content";
    private static final String HEADS_LAYOUT_ID = "heads-layout";
    private static final String CONTENT_PARENT_LAYOUT_ID = "content-parent-layout";
    private static final String COMPOUND_HEADS_LAYOUT_ID = "compound-heads-layout";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String HEAD_CATEGORY_ID = "head-category-collapsible";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private static HeadGeneratorMemento memento = null;
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageRowsElements skinElements;
    private TextFieldWidget headNameField;
    private EnumWidget skinPreEditOption;
    private TextFieldWidget searchField;
    private List<HeadComponentEntry> headComponentEntries;
    private List<HeadCompoundComponentEntry> headCompoundComponentEntries;
    private FlowLayout contentLayout;
    private FlowLayout compoundHeadsLayout;
    private ButtonWidget toggleFavoriteList;
    private boolean showFavorites;
    private BufferedImage baseSkin;
    private BufferedImage gridBaseSkinOriginalBody;
    private BufferedImage gridBaseSkinEditedBody;
    private String previousSkinName;
    private IHeadCategory selectedCategory;
    private ButtonComponent giveButton;
    private Animation.Composed compoundExpandAnimation;

    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
        this.favoritesHeadsOnOpenScreen = Set.copyOf(FzmmClient.CONFIG.headGenerator.favoriteSkins());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.headComponentEntries = new ArrayList<>();
        this.headCompoundComponentEntries = new ArrayList<>();
        this.baseSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.gridBaseSkinOriginalBody = this.baseSkin;
        this.gridBaseSkinEditedBody = this.baseSkin;
        //general
        this.skinElements = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, SkinMode.NAME);
        this.skinElements.imageButton().setButtonCallback(this::imageCallback);
        this.previousSkinName = "";
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "", 512);
        this.skinElements.valueField().onChanged().subscribe(this::onChangeSkinField);
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);
        this.compoundHeadsLayout = rootComponent.childById(FlowLayout.class, COMPOUND_HEADS_LAYOUT_ID);
        checkNull(this.compoundHeadsLayout, "flow-layout", COMPOUND_HEADS_LAYOUT_ID);

        FlowLayout contentParentLayout = rootComponent.childById(FlowLayout.class, CONTENT_PARENT_LAYOUT_ID);
        checkNull(contentParentLayout, "flow-layout", CONTENT_PARENT_LAYOUT_ID);
        FlowLayout headsLayout = rootComponent.childById(FlowLayout.class, HEADS_LAYOUT_ID);
        checkNull(headsLayout, "flow-layout", HEADS_LAYOUT_ID);
        // owo-lib doesn't let to make Sizing.fill and Sizing.fill animations,
        // so I have to remove the percentage of compoundHeadsWidth on the screen size
        // note: this means that if the screen resolution changes it will be wrongly resized while expanded
        Window window = this.client.getWindow();
        int contentGap = (int) Math.floor(window.getScaleFactor() * contentParentLayout.gap());
        int newHeadsLayoutWidth = 99 - (int) Math.floor(((COMPOUND_HEAD_LAYOUT_WIDTH + contentGap * contentParentLayout.children().size() - 1) / (float) window.getScaledWidth()) * 100);
        Animation<Sizing> headsLayoutAnimation = headsLayout.horizontalSizing().animate(800, Easing.CUBIC, Sizing.fill(newHeadsLayoutWidth));
        Animation<Sizing> compoundHeadsLayoutAnimation = this.compoundHeadsLayout.horizontalSizing().animate(800, Easing.CUBIC, Sizing.fixed(COMPOUND_HEAD_LAYOUT_WIDTH));
        this.compoundExpandAnimation = Animation.compose(headsLayoutAnimation, compoundHeadsLayoutAnimation);

        //bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));

        // nav var
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", 128, s -> this.applyFilters());
        this.skinPreEditOption = EnumRow.setup(rootComponent, SKIN_PRE_EDIT_OPTION_ID, SkinPreEditOption.OVERLAP, true, button -> {
            if (this.skinElements.imageButton().hasImage())
                this.updatePreviews();
        });
        int maxSkinPreEditOptionWidth = 0;
        for (var skinPreEditOption : SkinPreEditOption.values())
            maxSkinPreEditOptionWidth = Math.max(maxSkinPreEditOptionWidth, this.textRenderer.getWidth(Text.translatable(skinPreEditOption.getTranslationKey())));

        this.skinPreEditOption.horizontalSizing(Sizing.fixed(maxSkinPreEditOptionWidth + BaseFzmmScreen.BUTTON_TEXT_PADDING));

        CollapsibleContainer headCategoryCollapsible = rootComponent.childById(CollapsibleContainer.class, HEAD_CATEGORY_ID);
        checkNull(headCategoryCollapsible, "collapsible", HEAD_CATEGORY_ID);
        DropdownComponent headCategoryDropdown = Components.dropdown(Sizing.content());

        for (var category : IHeadCategory.NATURAL_CATEGORIES) {
            headCategoryDropdown.button(Text.translatable(category.getTranslationKey()), dropdownComponent -> {
                this.selectedCategory = category;
                this.applyFilters();
            });
        }
        this.selectedCategory = IHeadCategory.NATURAL_CATEGORIES[0];
        headCategoryCollapsible.child(headCategoryDropdown);
        headCategoryDropdown.zIndex(300);
        headCategoryDropdown.children().get(0).mouseDown().subscribe((mouseX, mouseY, button) -> true);


        this.toggleFavoriteList = ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, buttonComponent -> this.toggleFavoriteListExecute());
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = Math.max(this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_DISABLED_TEXT), this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, buttonComponent -> this.wikiExecute());

        this.tryLoadHeadEntries(rootComponent);
    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null)
            return;


        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = ImageUtils.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase);
            this.skinElements.imageButton().setImage(skinBase);
        }

        // this is necessary as the models need consistency in the size of the arms
        // if you don't want to have a model for each arm size
//        if (ImageUtils.isAlexModel(1, skinBase))
//            skinBase = ImageUtils.convertInSteveModel(skinBase, 1);

        this.baseSkin = skinBase;
        this.gridBaseSkinEditedBody = this.baseSkin;
        this.gridBaseSkinOriginalBody= this.baseSkin;

        this.updatePreviews();
    }

    private void tryLoadHeadEntries(FlowLayout rootComponent) {
        if (this.contentLayout.children().isEmpty()) {
            List<AbstractHeadEntry> headEntriesList = HeadResourcesLoader.getPreloaded();

            if (headEntriesList.size() == 0) {
                this.addNoResultsMessage(rootComponent);
                return;
            }

            List<HeadComponentEntry> headEntries = new ArrayList<>(headEntriesList.size());
            for (AbstractHeadEntry entry : headEntriesList) {
                headEntries.add(new HeadComponentEntry(entry, this));

                if (entry instanceof HeadModelEntry modelEntry) {
                    modelEntry.reset();
                }
            }

            this.headComponentEntries.addAll(headEntries);
            this.applyFilters();
        }

        this.updatePreviews();
    }

    private void addNoResultsMessage(FlowLayout parent) {
        Component label = Components.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                        .setStyle(Style.EMPTY.withColor(0xD83F27)))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.fill(100), Sizing.content())
                .margins(Insets.top(4));
        FlowLayout layout = parent.childById(FlowLayout.class, "no-results-label-layout");
        checkNull(layout, "flow-layout", "no-results-label-layout");
        layout.child(label);
    }


    private void updatePreviews() {
        assert this.client != null;

        this.client.execute(() -> {
            boolean compoundEntriesEditingSkinBody = this.headCompoundComponentEntries.stream()
                    .anyMatch(entry -> entry.getValue().isEditingSkinBody());

            SkinPreEditOption skinPreEditOption = this.skinPreEdit();
            BufferedImage previousPreview = this.skinPreEdit(this.baseSkin, skinPreEditOption, compoundEntriesEditingSkinBody);
            boolean isSlim = ImageUtils.isAlexModel(1, this.baseSkin);

            for (var headEntry : this.headCompoundComponentEntries) {
                headEntry.update(previousPreview, isSlim);
                previousPreview = new TextureOverlap(headEntry.getPreview())
                        .overlap(compoundEntriesEditingSkinBody)
                        .getHeadTexture();
            }

            this.gridBaseSkinOriginalBody = this.skinPreEdit(previousPreview, skinPreEditOption, false);
            this.gridBaseSkinEditedBody = this.skinPreEdit(previousPreview, skinPreEditOption, true);

            for (var headEntry : this.headComponentEntries) {
                headEntry.update(this.getGridBaseSkin(headEntry.getValue().isEditingSkinBody()), isSlim);
            }
        });

    }

    public BufferedImage skinPreEdit(BufferedImage preview, SkinPreEditOption skinPreEditOption, boolean editBody) {
        BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        result = skinPreEditOption.getPreEdit().execute(result, preview, List.of(SkinPart.HEAD));
        result = (editBody ? skinPreEditOption : SkinPreEditOption.NONE).getPreEdit().execute(result, preview, SkinPart.BODY_PARTS);

        return result;
    }

    public BufferedImage getGridBaseSkin(boolean editBody) {
        return editBody ? this.gridBaseSkinEditedBody : this.gridBaseSkinOriginalBody;
    }

    private void closeTextures() {
        if (this.contentLayout == null)
            return;

        assert this.client != null;
        this.client.execute(() -> {
            this.closeTextures(this.headComponentEntries);
            this.closeTextures(this.headCompoundComponentEntries);
        });
    }

    private void closeTextures(List<? extends AbstractHeadListEntry> entries) {
        for (var entry : entries) {
            entry.close();
        }
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();

        for (var entry : this.headComponentEntries) {
            entry.filter(searchValue, this.showFavorites, this.selectedCategory);
        }

        List<Component> newResults = new ArrayList<>(this.headComponentEntries);
        newResults.removeIf(component -> component instanceof HeadComponentEntry entry && entry.isHide());
        this.contentLayout.clearChildren();
        this.contentLayout.children(newResults);
    }

    public void giveHead(BufferedImage image, String textureName) {
        assert this.client != null;
        this.client.execute(() -> {
            this.setUndefinedDelay();
            String headName = this.getHeadName();

            new HeadUtils().uploadHead(image, headName + " + " + textureName).thenAccept(headUtils -> {
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                HeadBuilder builder = headUtils.getBuilder();
                if (!headName.isBlank())
                    builder.headName(headName);

                FzmmUtils.giveItem(builder.get());
                this.client.execute(() -> this.setDelay(delay));
            });
        });
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButton(waitMessage, false);
    }

    public void setDelay(int seconds) {
        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButton(message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> this.updateButton(HeadComponentEntry.GIVE_BUTTON_TEXT, true));
    }

    public void updateButton(Text message, boolean active) {
        if (this.giveButton != null) {
            this.giveButton.setMessage(message);
            this.giveButton.active = active;
        }
    }

    public void setCurrentGiveButton(ButtonComponent currentGiveButton) {
        if (this.giveButton != null) {
            Text message = this.giveButton.getMessage();
            boolean active = this.giveButton.active;
            this.giveButton = currentGiveButton;
            this.updateButton(message, active);
        } else {
            this.giveButton = currentGiveButton;
        }
    }

    public String getHeadName() {
        return this.headNameField.getText();
    }

    public void addCompound(AbstractHeadEntry headData) {
        assert this.client != null;

        List<Component> compoundHeads = this.compoundHeadsLayout.children();
        if (compoundHeads.isEmpty()) {
            this.compoundExpandAnimation.forwards();
            this.compoundHeadsLayout.surface(Surface.DARK_PANEL);
        }

        HeadCompoundComponentEntry entry = new HeadCompoundComponentEntry(headData, this.compoundHeadsLayout, this);

        this.headCompoundComponentEntries.add(entry);
        this.compoundHeadsLayout.child(entry);
        this.updatePreviews();
    }

    public void removeCompound(HeadCompoundComponentEntry entry) {
        assert this.parent != null;
        entry.close();
        this.compoundHeadsLayout.removeChild(entry);
        this.headCompoundComponentEntries.remove(entry);

        if (this.headCompoundComponentEntries.isEmpty()) {
            this.compoundExpandAnimation.backwards();
            this.compoundHeadsLayout.surface(Surface.BLANK);
        }

        this.updatePreviews();
    }

    private void toggleFavoriteListExecute() {
        this.showFavorites = !this.showFavorites;
        this.updateToggleFavoriteText();
        this.applyFilters();
    }

    private void updateToggleFavoriteText() {
        this.toggleFavoriteList.setMessage(this.showFavorites ? HeadComponentEntry.FAVORITE_ENABLED_TEXT : HeadComponentEntry.FAVORITE_DISABLED_TEXT);
    }

    private void wikiExecute() {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK);

            this.client.setScreen(this);
        }, FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK, true));
    }

    public SkinPreEditOption skinPreEdit() {
        return (SkinPreEditOption) this.skinPreEditOption.getValue();
    }

    public void upCompoundEntry(AbstractHeadListEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadListEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.upEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    public void downCompoundEntry(AbstractHeadListEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadListEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.downEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    @Override
    public void close() {
        super.close();
        this.closeTextures();

        if (!this.favoritesHeadsOnOpenScreen.equals(FzmmClient.CONFIG.headGenerator.favoriteSkins()))
            FzmmClient.CONFIG.save();
    }

    private void onChangeSkinField(String value) {
        EnumWidget mode = this.skinElements.mode();
        if (mode == null)
            return;
        if (((SkinMode) mode.getValue()).isHeadName() && this.headNameField.getText().equals(this.previousSkinName)) {
            this.headNameField.setText(value);
            this.headNameField.setCursor(0);
        }

        this.previousSkinName = value;
    }

    @Override
    public void setMemento(IMementoObject memento) {
        HeadGeneratorScreen.memento = (HeadGeneratorMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new HeadGeneratorMemento(
                this.headNameField.getText(),
                (SkinMode) this.skinElements.mode().getValue(),
                this.skinElements.valueField().getText(),
                this.showFavorites,
                this.skinPreEdit(),
                this.selectedCategory,
                this.searchField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        HeadGeneratorMemento memento = (HeadGeneratorMemento) mementoObject;
        this.skinElements.mode().setValue(memento.skinMode);
        this.skinElements.valueField().setText(memento.skinRowValue);
        this.skinElements.valueField().setCursor(0);
        this.headNameField.setText(memento.headName);
        this.headNameField.setCursor(0);
        if (memento.showFavorites)
            this.toggleFavoriteListExecute();
        this.skinPreEditOption.setValue(memento.skinPreEditOption);
        this.selectedCategory = memento.category;
        this.searchField.setText(memento.search);
        this.searchField.setCursor(0);
    }

    private record HeadGeneratorMemento(String headName, SkinMode skinMode, String skinRowValue, boolean showFavorites,
                                        SkinPreEditOption skinPreEditOption, IHeadCategory category,
                                        String search) implements IMementoObject {
    }
}