package com.test.github.data.entity

import androidx.room.TypeConverter
import java.util.*

class UUIDConverter() {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String = uuid.toString()

    @TypeConverter
    fun toUUID(field: String): UUID = UUID.fromString(field)
}