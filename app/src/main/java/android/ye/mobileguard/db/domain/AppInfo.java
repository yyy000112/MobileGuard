package android.ye.mobileguard.db.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用信息
 */
public class AppInfo {
    public String packName;
    public String name;
    public Drawable icon;
    public boolean isSD;
    public boolean isSys;

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSD() {
        return isSD;
    }

    public void setIsSD(boolean isSD) {
        this.isSD = isSD;
    }

    public boolean isSys() {
        return isSys;
    }

    public void setIsSys(boolean isSys) {
        this.isSys = isSys;
    }
}
