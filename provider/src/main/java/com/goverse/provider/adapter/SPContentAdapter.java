package com.goverse.provider.adapter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.goverse.provider.ContentAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("JavadocReference")
public abstract class SPContentAdapter<T> extends ContentAdapter {

    protected final String TAG = this.getClass().getSimpleName();

    private Gson mGson = new Gson();

    public SPContentAdapter(ContentProvider contentProvider) {
        super(contentProvider);
    }

    /**
     * set ContentValues
     * 插入或更新 sharedPreference存储对象的值
     * @param t 更新对象
     * @param contentValues 插入或更新字段，对应于对象属性
     */
    public void setContentValues(T t, ContentValues contentValues) {

        Set<String> keys = contentValues.keySet();
        for (String key : keys) {
            try {
                Field field = t.getClass().getField(key);
                Class<?> type = field.getType();
                String name = field.getName();
                if (type == Long.TYPE) {
                    Long asLong = contentValues.getAsLong(name);
                    if (asLong != null) {
                        field.setLong(t, asLong);
                    }
                } else if (type == Integer.TYPE) {
                    Integer asInteger = contentValues.getAsInteger(name);
                    if (asInteger != null) {
                        field.setInt(t, asInteger);
                    }
                }  else if (type == Double.TYPE) {
                    Double asDouble = contentValues.getAsDouble(name);
                    if (asDouble != null) {
                        field.setDouble(t, asDouble);
                    }
                } else if (type == Boolean.TYPE) {
                    Boolean asBoolean = contentValues.getAsBoolean(name);
                    if (asBoolean != null) {
                        field.setBoolean(t, asBoolean);
                    }
                } else if (type == String.class) {
                    field.set(t, Objects.toString(contentValues.getAsString(name),""));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Cursor buildCursor(String[] projection, T t) {

        Log.d(TAG, "buildCursor");
        String[] columns = null;
        List<Object> rows = new ArrayList<>();
        List<String> columnList = new ArrayList<>();
        //projection为null, 默认查询全部字段
        if (projection == null) {
            Field[] fields = t.getClass().getFields();
            for (int i = 0 ; i < fields.length; i ++) {
                columnList.add(fields[i].getName());
                rows.add(getFieldValue(fields[i], t));
            }
        } else {
            for (int i =0; i < projection.length; i ++) {
                String proj = projection[i];
                columnList.add(projection[i]);
                Field field = null;
                try {
                    field = t.getClass().getField(proj);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                rows.add(getFieldValue(field, t));
            }
        }
        if (rows.size() != 0) {
            String[] tempList = new String[columnList.size()];
            columns = columnList.toArray(tempList);
            MatrixCursor matrixCursor = new MatrixCursor(columns, 1);
            matrixCursor.addRow(rows);
            return matrixCursor;
        }
        return null;
    }

    private Object getFieldValue(Field field, Object object) {

        if (field == null || object == null) return null;
        Class<?> type = field.getType();
        Object value = null;
        try {
            if (type == Long.TYPE) {
                value = field.getLong(object);
            } else if (type == Integer.TYPE) {
                value = field.getInt(object);
            } else if (type == Double.TYPE) {
                value = field.getDouble(object);
            } else if (type == Boolean.TYPE) {
                value = field.getBoolean(object);
            } else if (type == String.class) {
                value = field.get(object);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return value;
    }

    /**
     * sharedPreference 通用加密算法
     * @param encryptStr 加密字符串
     * @param secret secret
     * @param iv vector
     * @return 加密后字符串
     * @throws Exception
     */
    protected String encrypt(String encryptStr, byte[] secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        int blockSize = Math.max(1, cipher.getBlockSize());

        byte[] dataBytes = encryptStr.trim().getBytes("UTF-8");
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }

        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

        SecretKeySpec keyspec = new SecretKeySpec(secret, "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        byte[] encrypted = cipher.doFinal(plaintext);
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    /**
     * sharedPreference 通用解密算法
     * @param decodeStr 解码字符串
     * @param secret secret
     * @param iv vector
     * @return 解密后字符串
     * @throws Exception
     */
    protected String decode(String decodeStr, byte[] secret, byte[] iv) throws Exception {
        byte[] encrypted = Base64.decode(decodeStr, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keyspec = new SecretKeySpec(secret, "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        byte[] original = cipher.doFinal(encrypted);
        String originalString = new String(original,"UTF-8").trim();
        return originalString;
    }

    /**
     * sharedPreference 序列化方法
     * @param t 序列化对象
     * @param spName sharedPreference文件名
     * @param key sharedPreference key值
     */
    protected void serialize(T t, String spName, String key) {

        String jsonT = mGson.toJson(t);
        Log.d(TAG, "serialize---jsonT: " + jsonT);
        if (!TextUtils.isEmpty(jsonT)) {
            SharedPreferences sp = getContentProvider().getContext().getSharedPreferences("", Context.MODE_PRIVATE);
            sp.edit().putString(key, jsonT).commit();
        }
    }

    /**
     * sharedPreference 反序列化方法
     * @param spName sharedPreference文件名
     * @param key sharedPreference key值
     * @return 序列化对象
     */
    protected T deSerialize(String spName, String key, Class<T> cls) {

        Log.d(TAG, "deSerialize");
        SharedPreferences sp = getContentProvider().getContext().getSharedPreferences("", Context.MODE_PRIVATE);
        String json = sp.getString(key, null);
        T t = null;
        if (!TextUtils.isEmpty(json)) {
            t = mGson.fromJson(json, cls);
        }
        return t;
    }


}
