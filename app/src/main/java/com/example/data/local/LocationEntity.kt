package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.LocationItem

@Entity(tableName = "saved_locations")
data class LocationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
    val lastViewedTimestamp: Long = System.currentTimeMillis()
) {
    fun toLocationItem(): LocationItem = LocationItem(
        id = id,
        name = name,
        country = country,
        admin1 = admin1,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromLocationItem(item: LocationItem): LocationEntity = LocationEntity(
            id = item.id,
            name = item.name,
            country = item.country,
            admin1 = item.admin1,
            latitude = item.latitude,
            longitude = item.longitude
        )
    }
}
