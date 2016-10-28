package android.ye.mobileguard.Permit;


public interface PermissionCallBack {
    void onGranted(String[] permissions, int[] grantResults);

    void onFailed(String[] permissions, int[] grantResults);

}
