package android.ye.mobileguard.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.ye.mobileguard.R;

/**
 * 建立设置中心的自定义控件
 *
 */
public class ClickItemView extends RelativeLayout {


    private TextView msetDes;
    private TextView msetTitle;


    public ClickItemView(Context context) {
        this(context, null);
    }

    public ClickItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view =View.inflate(context, R.layout.setting_click_view,this);
        msetTitle = (TextView) findViewById(R.id.set_title);
        msetDes = (TextView) findViewById(R.id.set_des);
    }

    /**
     *
     * @param title 设置标题
     */
    public void setTitle(String title){
        msetTitle.setText(title);

    }

    /**
     *
     * @param des 设置描述
     */
    public void setDes(String des){
        msetDes.setText(des);
    }




}
