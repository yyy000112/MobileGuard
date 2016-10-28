package android.ye.mobileguard.Service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
;

/**
 * 建立定位服务
 */
public class LocationService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        initLocation();
    }

    private void initLocation() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        //设置允许使用流量消费
        criteria.setCostAllowed(true);
        //设置精度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = lm.getBestProvider(criteria, true);

        MyLocationListener myLocationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(bestProvider, 0, 0, myLocationListener);

    }




    private class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            String phone = SpUtil.getString(getApplicationContext(), ConstantValue.PHONE_NUM,"");
            //纬度
            double longitude = location.getLongitude();
            //经度
            double latitude = location.getLatitude();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone,null,"longitude = "+longitude + ",latitude ="+latitude,null,null);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
