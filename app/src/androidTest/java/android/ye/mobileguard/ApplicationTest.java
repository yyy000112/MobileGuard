package android.ye.mobileguard;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.ye.mobileguard.db.dao.BlackNumberDao;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void insert(){
        BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
        dao.insert("122", "1");
    }

    public void delete(){
        BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
        dao.delete("122");
    }
}