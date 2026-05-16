// core/model/PopupMenuInfo.java
package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация о контекстном меню (PopupMenu)
 */
public class PopupMenuInfo {

    private final String name;
    private final List<MenuItem> rootItems;

    public PopupMenuInfo(String name) {
        this.name = name;
        this.rootItems = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MenuItem> getRootItems() {
        return rootItems;
    }

    public void addItem(MenuItem item) {
        this.rootItems.add(item);
    }

    /**
     * Пункт меню
     */
    public static class MenuItem {
        private String caption;
        private String name;
        private boolean fromAutoPopup;
        private String autoPopupName;
        private final List<MenuItem> children;

        public MenuItem() {
            this.children = new ArrayList<>();
            this.fromAutoPopup = false;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isFromAutoPopup() {
            return fromAutoPopup;
        }

        public void setFromAutoPopup(boolean fromAutoPopup) {
            this.fromAutoPopup = fromAutoPopup;
        }

        public String getAutoPopupName() {
            return autoPopupName;
        }

        public void setAutoPopupName(String autoPopupName) {
            this.autoPopupName = autoPopupName;
        }

        public List<MenuItem> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public void addChild(MenuItem child) {
            this.children.add(child);
        }

        public String getDisplayCaption() {
            if (caption != null && !caption.isEmpty()) {
                return "\"" + caption + "\"";
            }
            if (name != null && !name.isEmpty()) {
                return "name=\"" + name + "\"";
            }
            return "(без названия)";
        }

        public String getPrefix() {
            if (fromAutoPopup && autoPopupName != null) {
                return "(AutoPopup \"" + autoPopupName + "\") ";
            }
            return "";
        }
    }
}