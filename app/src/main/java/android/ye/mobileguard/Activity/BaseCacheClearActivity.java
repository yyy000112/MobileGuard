package android.ye.mobileguard.Activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.ye.mobileguard.R;

/**
 * 选项卡Activity
 */
public class BaseCacheClearActivity extends TabActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_cache_clear_activity);
        //生成选项卡
        TabHost.TabSpec tab1= getTabHost().newTabSpec("clear_cache").setIndicator("缓存清理");
        TabHost.TabSpec tab2 = getTabHost().newTabSpec("sd_cache_clear").setIndicator("SD卡清理");

        //设置选项卡的点击事件
        tab1.setContent(new Intent(this,CacheClearActivity.class));
        tab2.setContent(new Intent(this,SDCacheClearActivity.class));
        //将选项卡添加到tabHost中
        getTabHost().addTab(tab1);
        getTabHost().addTab(tab2);
    }
}
