package ru.levrost.rtu_map_app.data.dataSource.room.entites

import androidx.room.TypeConverter
import java.util.Arrays
import java.util.stream.Collectors

class ListConvertor {
    @TypeConverter
    fun toString(value: List<String>): String {
        var output = ""
        for (place in value) {
            output = "$output$place,"
        }
        return output
    }

    @TypeConverter
    fun fromString(data: String): List<String> {
        return Arrays.stream(data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()).collect(Collectors.toList())
    }
}