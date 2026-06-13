package royale.screens.clickgui.impl.background.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import royale.IMinecraft;
import royale.Initialization;
import royale.modules.module.ModuleRepository;
import royale.modules.module.ModuleStructure;

public class SearchHandler implements IMinecraft {
    private boolean searchActive = false;
    private String searchText = "";
    private int searchCursorPosition = 0;
    private int searchSelectionStart = -1;
    private int searchSelectionEnd = -1;

    private float searchCursorBlink = 0.0F;
    private float searchBoxAnimation = 0.0F;
    private float searchFocusAnimation = 0.0F;
    private float searchPanelAlpha = 0.0F;
    private float normalPanelAlpha = 1.0F;
    private float searchSelectionAnimation = 0.0F;

    private List<ModuleStructure> searchResults = new ArrayList<>();
    private Map<ModuleStructure, Float> searchResultAnimations = new HashMap<>();
    private Map<ModuleStructure, Long> searchResultAnimStartTimes = new HashMap<>();

    private float searchScrollOffset = 0.0F;
    private float searchTargetScroll = 0.0F;
    private int hoveredSearchIndex = -1;
    private ModuleStructure selectedSearchModule = null;

    private static final float SEARCH_ANIM_SPEED = 8.0F;
    private static final float PANEL_FADE_SPEED = 15.0F;
    private static final float SEARCH_RESULT_HEIGHT = 18.0F;
    private static final float SEARCH_RESULT_ANIM_DURATION = 200.0F;

    public boolean isSearchActive() {
        return this.searchActive;
    }

    public String getSearchText() {
        return this.searchText;
    }

    public int getSearchCursorPosition() {
        return this.searchCursorPosition;
    }

    public float getSearchCursorBlink() {
        return this.searchCursorBlink;
    }

    public float getSearchBoxAnimation() {
        return this.searchBoxAnimation;
    }

    public float getSearchFocusAnimation() {
        return this.searchFocusAnimation;
    }

    public float getSearchPanelAlpha() {
        return this.searchPanelAlpha;
    }

    public float getNormalPanelAlpha() {
        return this.normalPanelAlpha;
    }

    public float getSearchSelectionAnimation() {
        return this.searchSelectionAnimation;
    }

    public List<ModuleStructure> getSearchResults() {
        return this.searchResults;
    }

    public Map<ModuleStructure, Float> getSearchResultAnimations() {
        return this.searchResultAnimations;
    }

    public Map<ModuleStructure, Long> getSearchResultAnimStartTimes() {
        return this.searchResultAnimStartTimes;
    }

    public float getSearchScrollOffset() {
        return this.searchScrollOffset;
    }

    public float getSearchTargetScroll() {
        return this.searchTargetScroll;
    }

    public int getHoveredSearchIndex() {
        return this.hoveredSearchIndex;
    }

    public ModuleStructure getSelectedSearchModule() {
        return this.selectedSearchModule;
    }

    public void setSearchActive(boolean active) {
        if (active && !this.searchActive) {
            this.searchText = "";
            this.searchCursorPosition = 0;
            clearSearchSelection();
            this.searchResults.clear();
            this.searchResultAnimations.clear();
            this.searchResultAnimStartTimes.clear();
            this.searchScrollOffset = 0.0F;
            this.searchTargetScroll = 0.0F;
            this.hoveredSearchIndex = -1;
            this.selectedSearchModule = null;
        }
        this.searchActive = active;
    }

    public void updateAnimations(float deltaTime) {
        float searchTarget = this.searchActive ? 1.0F : 0.0F;
        this.searchBoxAnimation = updateAnimation(this.searchBoxAnimation, searchTarget, SEARCH_ANIM_SPEED, deltaTime);
        this.searchFocusAnimation = updateAnimation(this.searchFocusAnimation, searchTarget, SEARCH_ANIM_SPEED, deltaTime);
        this.searchPanelAlpha = updateAnimation(this.searchPanelAlpha, searchTarget, PANEL_FADE_SPEED, deltaTime);
        this.normalPanelAlpha = updateAnimation(this.normalPanelAlpha, this.searchActive ? 0.0F : 1.0F, PANEL_FADE_SPEED, deltaTime);
        this.searchSelectionAnimation = updateAnimation(
                this.searchSelectionAnimation,
                hasSearchSelection() ? 1.0F : 0.0F,
                SEARCH_ANIM_SPEED,
                deltaTime
        );

        if (this.searchActive || this.searchPanelAlpha > 0.01F) {
            this.searchCursorBlink += deltaTime * 2.0F;
            if (this.searchCursorBlink > 1.0F) {
                this.searchCursorBlink -= 1.0F;
            }
            updateResultAnimations();
            updateScrollAnimation(deltaTime);
            return;
        }

        if (!this.searchResults.isEmpty()) {
            this.searchResults.clear();
            this.searchResultAnimations.clear();
            this.searchResultAnimStartTimes.clear();
            this.searchScrollOffset = 0.0F;
            this.searchTargetScroll = 0.0F;
            this.selectedSearchModule = null;
        }
    }

    private float updateAnimation(float current, float target, float speed, float deltaTime) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001F) {
            return target;
        }

        float updated = current + diff * speed * deltaTime;
        if (target > current) {
            return Math.min(updated, target);
        }
        return Math.max(updated, target);
    }

    private void updateResultAnimations() {
        long currentTime = System.currentTimeMillis();
        for (ModuleStructure module : this.searchResults) {
            Long startTime = this.searchResultAnimStartTimes.get(module);
            if (startTime == null) {
                continue;
            }

            float elapsed = currentTime - startTime;
            float progress = Math.min(1.0F, Math.max(0.0F, elapsed / SEARCH_RESULT_ANIM_DURATION));
            this.searchResultAnimations.put(module, easeOutCubic(progress));
        }
    }

    private void updateScrollAnimation(float deltaTime) {
        float scrollDiff = this.searchTargetScroll - this.searchScrollOffset;
        if (Math.abs(scrollDiff) < 0.5F) {
            this.searchScrollOffset = this.searchTargetScroll;
        } else {
            this.searchScrollOffset += scrollDiff * 12.0F * deltaTime;
        }
    }

    private float easeOutCubic(float x) {
        return 1.0F - (float) Math.pow(1.0F - x, 3.0D);
    }

    public boolean hasSearchSelection() {
        return this.searchSelectionStart != -1
                && this.searchSelectionEnd != -1
                && this.searchSelectionStart != this.searchSelectionEnd;
    }

    public int getSearchSelectionStart() {
        return Math.min(this.searchSelectionStart, this.searchSelectionEnd);
    }

    public int getSearchSelectionEnd() {
        return Math.max(this.searchSelectionStart, this.searchSelectionEnd);
    }

    private void clearSearchSelection() {
        this.searchSelectionStart = -1;
        this.searchSelectionEnd = -1;
    }

    private void selectAllSearchText() {
        this.searchSelectionStart = 0;
        this.searchSelectionEnd = this.searchText.length();
        this.searchCursorPosition = this.searchText.length();
    }

    private void deleteSelectedSearchText() {
        if (!hasSearchSelection()) {
            return;
        }

        int start = getSearchSelectionStart();
        int end = getSearchSelectionEnd();
        this.searchText = this.searchText.substring(0, start) + this.searchText.substring(end);
        this.searchCursorPosition = start;
        clearSearchSelection();
        updateSearchResults();
    }

    private String getSelectedSearchText() {
        if (!hasSearchSelection()) {
            return "";
        }
        return this.searchText.substring(getSearchSelectionStart(), getSearchSelectionEnd());
    }

    private void copySearchToClipboard() {
        if (hasSearchSelection()) {
            GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), getSelectedSearchText());
        }
    }

    private void pasteToSearch() {
        String clipboardText = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
        if (clipboardText == null || clipboardText.isEmpty()) {
            return;
        }

        clipboardText = clipboardText.replaceAll("[\\n\\r\\t]", "");
        if (clipboardText.isEmpty()) {
            return;
        }

        if (hasSearchSelection()) {
            deleteSelectedSearchText();
        }

        this.searchText = this.searchText.substring(0, this.searchCursorPosition)
                + clipboardText
                + this.searchText.substring(this.searchCursorPosition);
        this.searchCursorPosition += clipboardText.length();
        updateSearchResults();
    }

    private boolean isControlDown() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private boolean isShiftDown() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    public void updateSearchResults() {
        if (this.searchText.isEmpty()) {
            this.searchResults.clear();
            this.searchResultAnimations.clear();
            this.searchResultAnimStartTimes.clear();
            this.searchScrollOffset = 0.0F;
            this.searchTargetScroll = 0.0F;
            this.selectedSearchModule = null;
            return;
        }

        String query = this.searchText.toLowerCase();
        List<ModuleStructure> newResults = new ArrayList<>();
        Map<ModuleStructure, Float> oldAnimations = new HashMap<>(this.searchResultAnimations);

        try {
            ModuleRepository repo = Initialization.getInstance().getManager().getModuleRepository();
            if (repo != null) {
                for (ModuleStructure module : repo.modules()) {
                    if (module.getName().toLowerCase().contains(query)) {
                        newResults.add(module);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        this.searchResultAnimations.clear();
        this.searchResultAnimStartTimes.clear();
        long currentTime = System.currentTimeMillis();
        int newIndex = 0;

        for (ModuleStructure module : newResults) {
            Float oldProgress = oldAnimations.get(module);
            if (oldProgress != null) {
                this.searchResultAnimations.put(module, Math.max(oldProgress, 0.5F));
                this.searchResultAnimStartTimes.put(module, currentTime - 170L);
            } else {
                this.searchResultAnimations.put(module, 0.0F);
                this.searchResultAnimStartTimes.put(module, currentTime + newIndex * 40L);
                newIndex++;
            }
        }

        this.searchResults = newResults;
        if (!this.searchResults.isEmpty()) {
            if (this.selectedSearchModule == null || !this.searchResults.contains(this.selectedSearchModule)) {
                this.selectedSearchModule = this.searchResults.get(0);
            }
        } else {
            this.selectedSearchModule = null;
        }

        clampTargetScroll();
    }

    public boolean handleSearchChar(char chr) {
        if (!this.searchActive || Character.isISOControl(chr)) {
            return false;
        }

        if (hasSearchSelection()) {
            deleteSelectedSearchText();
        }

        this.searchText = this.searchText.substring(0, this.searchCursorPosition)
                + chr
                + this.searchText.substring(this.searchCursorPosition);
        this.searchCursorPosition++;
        clearSearchSelection();
        updateSearchResults();
        return true;
    }

    public boolean handleSearchKey(int keyCode) {
        if (!this.searchActive) {
            return false;
        }

        if (isControlDown()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_A -> {
                    selectAllSearchText();
                    return true;
                }
                case GLFW.GLFW_KEY_C -> {
                    copySearchToClipboard();
                    return true;
                }
                case GLFW.GLFW_KEY_V -> {
                    pasteToSearch();
                    return true;
                }
                case GLFW.GLFW_KEY_X -> {
                    if (hasSearchSelection()) {
                        copySearchToClipboard();
                        deleteSelectedSearchText();
                    }
                    return true;
                }
                default -> {
                }
            }
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (hasSearchSelection()) {
                    deleteSelectedSearchText();
                } else if (this.searchCursorPosition > 0) {
                    this.searchText = this.searchText.substring(0, this.searchCursorPosition - 1)
                            + this.searchText.substring(this.searchCursorPosition);
                    this.searchCursorPosition--;
                    updateSearchResults();
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (hasSearchSelection()) {
                    deleteSelectedSearchText();
                } else if (this.searchCursorPosition < this.searchText.length()) {
                    this.searchText = this.searchText.substring(0, this.searchCursorPosition)
                            + this.searchText.substring(this.searchCursorPosition + 1);
                    updateSearchResults();
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                handleLeftKey();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                handleRightKey();
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                handleHomeKey();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                handleEndKey();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (!this.searchResults.isEmpty() && this.selectedSearchModule != null) {
                    int currentIndex = this.searchResults.indexOf(this.selectedSearchModule);
                    if (currentIndex > 0) {
                        this.selectedSearchModule = this.searchResults.get(currentIndex - 1);
                    }
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (!this.searchResults.isEmpty() && this.selectedSearchModule != null) {
                    int currentIndex = this.searchResults.indexOf(this.selectedSearchModule);
                    if (currentIndex < this.searchResults.size() - 1) {
                        this.selectedSearchModule = this.searchResults.get(currentIndex + 1);
                    }
                }
                return true;
            }
            case GLFW.GLFW_KEY_ENTER -> {
                if (this.selectedSearchModule != null) {
                    this.selectedSearchModule.switchState();
                }
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                setSearchActive(false);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void handleLeftKey() {
        if (hasSearchSelection() && !isShiftDown()) {
            this.searchCursorPosition = getSearchSelectionStart();
            clearSearchSelection();
            return;
        }

        if (this.searchCursorPosition > 0) {
            if (isShiftDown()) {
                if (this.searchSelectionStart == -1) {
                    this.searchSelectionStart = this.searchCursorPosition;
                }
                this.searchCursorPosition--;
                this.searchSelectionEnd = this.searchCursorPosition;
            } else {
                this.searchCursorPosition--;
                clearSearchSelection();
            }
        }
    }

    private void handleRightKey() {
        if (hasSearchSelection() && !isShiftDown()) {
            this.searchCursorPosition = getSearchSelectionEnd();
            clearSearchSelection();
            return;
        }

        if (this.searchCursorPosition < this.searchText.length()) {
            if (isShiftDown()) {
                if (this.searchSelectionStart == -1) {
                    this.searchSelectionStart = this.searchCursorPosition;
                }
                this.searchCursorPosition++;
                this.searchSelectionEnd = this.searchCursorPosition;
            } else {
                this.searchCursorPosition++;
                clearSearchSelection();
            }
        }
    }

    private void handleHomeKey() {
        if (isShiftDown()) {
            if (this.searchSelectionStart == -1) {
                this.searchSelectionStart = this.searchCursorPosition;
            }
            this.searchCursorPosition = 0;
            this.searchSelectionEnd = 0;
        } else {
            this.searchCursorPosition = 0;
            clearSearchSelection();
        }
    }

    private void handleEndKey() {
        int end = this.searchText.length();
        if (isShiftDown()) {
            if (this.searchSelectionStart == -1) {
                this.searchSelectionStart = this.searchCursorPosition;
            }
            this.searchCursorPosition = end;
            this.searchSelectionEnd = end;
        } else {
            this.searchCursorPosition = end;
            clearSearchSelection();
        }
    }

    public void handleSearchScroll(double vertical, float panelHeight) {
        if (!this.searchActive || this.searchResults.isEmpty()) {
            return;
        }

        this.searchTargetScroll += (float) (vertical * 25.0D);
        clampTargetScroll(panelHeight);
    }

    private void clampTargetScroll() {
        clampTargetScroll(204.0F);
    }

    private void clampTargetScroll(float panelHeight) {
        float maxScroll = Math.max(0.0F, this.searchResults.size() * (SEARCH_RESULT_HEIGHT + 2.0F) - panelHeight + 10.0F);
        this.searchTargetScroll = Math.max(-maxScroll, Math.min(0.0F, this.searchTargetScroll));
        this.searchScrollOffset = Math.max(-maxScroll, Math.min(0.0F, this.searchScrollOffset));
    }

    public float getSearchResultHeight() {
        return SEARCH_RESULT_HEIGHT;
    }

    public void setHoveredSearchIndex(int index) {
        this.hoveredSearchIndex = index;
    }
}
