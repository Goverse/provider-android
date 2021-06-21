package com.goverse.provider.adapter.open;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import androidx.annotation.Nullable;
import com.goverse.provider.adapter.SPContentAdapter;
import java.util.Calendar;
import java.util.TimeZone;

public class OpenDataAdapter extends SPContentAdapter<OpenDataAdapter.OpenData> {

    private final String KEY_OPEN = "sport";

    private final String PREFERENCE_OPEN_PROVIDER = "open_provider_preference";

    public static final String READ_SCOPE = "READ_OPEN_DATA";


    static class OpenData {
        public long timeStamp = System.currentTimeMillis();
        public long step;
        public double distance; // unit: km
        public double calorie; // unit: 大卡
        public long stepGoal;
        public double duration;
        public void reset() {
            timeStamp = System.currentTimeMillis();
            step = 0L;
            distance = 0.0D;
            calorie = 0.0D;
            duration = 0;
        }

        @Override
        public String toString() {
            return "timeStamp: " + timeStamp + ",step: " + step + ",distance: " + distance + ",calorie: " + calorie + ",stepGoal: " + stepGoal+ ",duration: " + duration;
        }
    }

    public OpenDataAdapter(ContentProvider contentProvider) {
        super(contentProvider);
    }

    @Override
    public Cursor query(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        OpenData openData = deSerialize(PREFERENCE_OPEN_PROVIDER, KEY_OPEN, OpenData.class);
        Log.d(TAG, "query---sportData: " + openData);
        if (openData == null) openData = new OpenData();
        if (!checkDateValid(openData.timeStamp)) {
            openData.reset();
            serialize(openData, PREFERENCE_OPEN_PROVIDER, KEY_OPEN);
        }
        Cursor cursor = buildCursor(projection, openData);
        return cursor;
    }

    /**
     * checkDateValid
     * check if the save time is same day compared to today
     * @param saveTime saveTime
     * @return isDateValid
     */
    private boolean checkDateValid(long saveTime) {

        long saveStartTime = getStartTimeOfDay(saveTime, TimeZone.getDefault());
        long currStartTime = getStartTimeOfDay(System.currentTimeMillis(), TimeZone.getDefault());
        Log.d(TAG, "checkDateValid---saveStartTime: " + saveStartTime + ",currStartTime: " + currStartTime);
        if (saveStartTime == currStartTime) return true;
        return false;
    }

    private long getStartTimeOfDay(long time, TimeZone timeZone) {

        Log.d(TAG, "getStartTimeOfDay---time: " + time);
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean insert(@Nullable ContentValues values) {

        Log.d(TAG, "insert");
        if (values == null) return false;
        OpenData openData = deSerialize(PREFERENCE_OPEN_PROVIDER, KEY_OPEN, OpenData.class);
        if (openData == null) {
            openData = new OpenData();
        }
        try {
            openData.timeStamp = System.currentTimeMillis();
            setContentValues(openData, values);
            serialize(openData, PREFERENCE_OPEN_PROVIDER, KEY_OPEN);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public int update(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update");

        if (values == null) return 0;
        OpenData openData = deSerialize(PREFERENCE_OPEN_PROVIDER, KEY_OPEN, OpenData.class);
        Log.d(TAG, "sportData：" + openData);
        if (openData == null) {
            openData = new OpenData();
        }
        try {
            openData.timeStamp = System.currentTimeMillis();
            setContentValues(openData, values);
            serialize(openData, PREFERENCE_OPEN_PROVIDER, KEY_OPEN);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @Override
    public int delete(@Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "delete");
        serialize(null, PREFERENCE_OPEN_PROVIDER, KEY_OPEN);
        return 1;
    }

    @Override
    public String readScope() {
        return READ_SCOPE;
    }

    @Override
    public String desc() {
        return "开放数据";
    }

}
