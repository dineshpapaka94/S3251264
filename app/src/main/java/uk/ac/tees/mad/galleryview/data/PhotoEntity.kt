package uk.ac.tees.mad.galleryview.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageData

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val filePath: String,
    val tags: String = "",
    val description: String = "",
    val timestamp: Long,
    val location: String = ""
)

fun PhotoEntity.toImageData(): ImageData {
    return ImageData(
        id = id,
        imageUrl = filePath,
        tags = tags.split(","),
        description = description,
        timestamp = timestamp,
        location = location
    )
}

fun ImageData.toPhotoEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        filePath = imageUrl,
        tags = tags.joinToString(","),
        description = description,
        timestamp = timestamp,
        location = location
    )
}



