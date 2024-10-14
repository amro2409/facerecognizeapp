package me.dev.is.mllibrary.data.database;

import androidx.room.TypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;

public class Converters {

    @TypeConverter
    public static Date dateFromLong(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long longFromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static float[] floatArrayFromByteArray(byte[] bytes) {
        float[] floats = null;

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            floats = new float[bytes.length / 4];//4 bytes per float
            for (int i = 0; i < floats.length; i++) {
                floats[i] = dataInputStream.readFloat();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return floats;
    }


    @TypeConverter
    public static byte[] byteArrayFromFloatArray(float[] floats) {
        byte[] bytes1 = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            for (float aFloat : floats) {
                dataOutputStream.writeFloat(aFloat);
            }
            bytes1=byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes1;
    }
}
