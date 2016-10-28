package android.ye.mobileguard.Permit;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;

public class PermissionManager {
    private static int callbackKey = 1;//=requestCode used as key for callback for each request
    private static PermissionManager manager;

    private HashMap<Integer, PermissionCallBack> mCallBacks;

    public static PermissionManager getInstance(){
        if(manager == null){
            manager = new PermissionManager();
        }
        return manager;
    }

    private PermissionManager(){
        mCallBacks = new HashMap<>();
    }

    public boolean hasPermission(Activity activity, String permission){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED : true;
    }

    public boolean shouldShowRequestPermissionRationale(Activity activity, String permission){
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public void requestPermission(Activity activity, String permission, PermissionCallBack permissionCallBack){
        requestPermissions(activity, new String[]{permission}, permissionCallBack);
    }

    public void requestPermissions(Activity activity, String[] permissions, PermissionCallBack permissionCallBack) {
        ActivityCompat.requestPermissions(activity, permissions, callbackKey);
        setPermissionCallBack(permissionCallBack);
    }

    public void setPermissionCallBack(PermissionCallBack permissionCallBack) {
        mCallBacks.put(callbackKey, permissionCallBack);

        callbackKey++;
        if(callbackKey > 255){//result code has to be 0-255
            callbackKey = 1;
        }
    }



    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionCallBack permissionCallBack = mCallBacks.get(requestCode);
        if (permissionCallBack != null) {
            if (isSuccess(grantResults)) {
                permissionCallBack.onGranted(permissions, grantResults);
            } else {
                permissionCallBack.onFailed(permissions, grantResults);
            }
            mCallBacks.remove(requestCode);
        }
    }

    private boolean isSuccess(int[] grantResults) {
        boolean result = true;
        for(int i : grantResults){
            if(i != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return result;
    }
}

