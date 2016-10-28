package android.ye.mobileguard.db.domain;

import android.graphics.drawable.Drawable;

/**
 * 进程
 * packageName 进程包名
 * name 进程名
 * icon 进程图标
 * memSize 进程内存大小
 * isSDCAR 是否为SD卡进程
 * isSystem 是否为系统进程
 * isCheck 是否被选中
 */
public class ProcessInfo {
    public String packageName;
    public String name;
    public Drawable icon;
    public boolean isSDCAR;
    public boolean isSystem;
    public long memSize;
    public boolean isCheck;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public boolean isSDCAR() {
        return isSDCAR;
    }

    public void setIsSDCAR(boolean isSDCAR) {
        this.isSDCAR = isSDCAR;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    public long getMemSize() {
        return memSize;
    }

    public void setMemSize(long memSize) {
        this.memSize = memSize;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
}
