package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.ye.mobileguard.R;

/**
 *
 */
public class Set1Activity extends BaseSetActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set1_activity);


    }

    @Override
    public void showPrePage() {

    }

    @Override
    public void showNextPage() {
        Intent intent = new Intent(getApplicationContext(),Set2Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
    }


}
