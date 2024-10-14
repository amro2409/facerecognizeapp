package me.dev.is.mllibrary.data.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

public interface Dao {

    @androidx.room.Dao
    public interface  PersonDoa{

        @Insert
        void insertFaceEmbeddings(List<Entities.PersonEntity>entities);

        @Insert
        void insertFaceEmbedding(Entities.PersonEntity entity);

        @Query("DELETE FROM PersonEntity")
        void deleteAll();
        @Delete
        void delete(Entities.PersonEntity entity);
    }

    @androidx.room.Dao
    public interface  FaceEmbeddingDoa{

        @Insert
        void insertFaceEmbeddings(List<Entities.FaceEmbeddingPersonEntity>entities);

        @Insert
        void insertFaceEmbedding(Entities.FaceEmbeddingPersonEntity entity);

        @Query("DELETE FROM FaceEmbeddings")
        void deleteAll();

        @Delete
        void delete(Entities.FaceEmbeddingPersonEntity entity);
    }

}
