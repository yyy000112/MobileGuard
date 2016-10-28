package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * SD卡的缓存清理
 */
public class SDCacheClearActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(getApplicationContext());
        textView.setText("SD卡缓存清理页面");
        setContentView(textView);
    }

}
