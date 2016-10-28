package android.ye.mobileguard.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.ye.mobileguard.R;

/**
 * 建立设置中心的自定义控件
 *
 */
public class SetItemView extends RelativeLayout {

    private static final String NAMESPACE = "http://schemas.android.com/apk/res/android.ye.mobileguard";
    private CheckBox mcbox;
    private TextView msetDes;
    private TextView msetTitle;
    private String mDestitle;
    private String mDesoff;
    private String mDeson;

    public SetItemView(Context context) {
        this(context, null);
    }

    public SetItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view =View.inflate(context, R.layout.setting_item,this);
        msetTitle = (TextView) findViewById(R.id.set_title);
        msetDes = (TextView) findViewById(R.id.set_des);
        mcbox = (CheckBox) findViewById(R.id.cb_box);
        //初始化属性
        initAttrs(attrs);
        //获取自定义字符串
        msetTitle.setText(mDestitle);

    }

    private void initAttrs(AttributeSet attrs) {
       /* Log.i("test","attrs.getAttributeCount()="+attrs.getAttributeCount());

        for (int i = 0;i<attrs.getAttributeCount();i++){
            Log.i("test","name="+attrs.getAttributeName(i));
            Log.i("test","values="+attrs.getAttributeValue(i));

        }*/
        //通过命名空间和属性名称获取属性值
        mDestitle = attrs.getAttributeValue(NAMESPACE, "destitle");
        mDesoff = attrs.getAttributeValue(NAMESPACE, "desoff");
        mDeson = attrs.getAttributeValue(NAMESPACE, "deson");
    }

    /**
     * 返回checkbox状态
     * @return
     */
    public boolean isCheck(){
        //选中checkbox，决定条目是否开启
        
        return mcbox.isChecked();
    }

    /**
     * 设置选中或未选中的状态相应的改变
     */
    public void setCheck(boolean isCheck){
        //checkbox选中状态跟随变化
        mcbox.setChecked(isCheck);
        if (isCheck()){
            msetDes.setText(mDeson);
        }
        else {
            msetDes.setText(mDesoff);
        }
    }
}
