package com.example.wallet.debug.storage

import androidx.room.TypeConverter
import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.contract.convertToJson
import com.example.wallet.debug.contract.fromJson

class Converters {

    @TypeConverter
    fun valueFromDomainToStorage(value: Value): String {
        return value.convertToJson()
    }

    @TypeConverter
    fun valueFromStorageToDomain(str: String): Value {
        return Value().fromJson(str)
    }
}