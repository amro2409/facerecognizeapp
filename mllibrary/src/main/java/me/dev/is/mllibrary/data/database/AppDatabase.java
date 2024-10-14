package me.dev.is.mllibrary.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities =
        {Entities.FaceEmbeddingPersonEntity.class,
                Entities.PersonEntity.class,
        },
        exportSchema = false,
        version = 1
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract Dao.FaceEmbeddingDoa faceEmbeddingDoa();

    public abstract Dao.PersonDoa personDoa();


    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "face_Employers_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }


}
