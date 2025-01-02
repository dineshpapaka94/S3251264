package uk.ac.tees.mad.galleryview.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Upsert
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}
