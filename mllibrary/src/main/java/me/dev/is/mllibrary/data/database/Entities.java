package me.dev.is.mllibrary.data.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Entities {

    @Entity
    public static class PersonEntity{

        @ColumnInfo(name = "person_id")
        @PrimaryKey(autoGenerate = true)
        private final long id;
        @ColumnInfo(name = "person_name")
        private final String name;
        @ColumnInfo(name = "status")
        private final int status;

        public PersonEntity(long id, String name, int status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getStatus() {
            return status;
        }
    }

    @Entity(tableName = "FaceEmbeddings")
    public static class FaceEmbeddingPersonEntity {

        @ColumnInfo(name = "embedding_id")
        @PrimaryKey(autoGenerate = true)
        private long id;
        @ColumnInfo(name = "person_id_fk")
        private final String personID;
        @ColumnInfo(name = "face_embedding",typeAffinity = ColumnInfo.BLOB)
        private final float[] faceEmbedding;
        @ColumnInfo(name = "face_image",typeAffinity = ColumnInfo.BLOB)
        private final byte[] faceImage;

        public FaceEmbeddingPersonEntity(String personID, float[] faceEmbedding, byte[] faceImage) {
            this.personID = personID;
            this.faceEmbedding = faceEmbedding;
            this.faceImage = faceImage;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getPersonID() {
            return personID;
        }

        public float[] getFaceEmbedding() {
            return faceEmbedding;
        }

        public byte[] getFaceImage() {
            return faceImage;
        }
    }

}
