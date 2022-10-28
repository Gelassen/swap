package com.example.wallet.debug.storage

import androidx.room.TypeConverter
import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.contract.convertToJson
import com.example.wallet.debug.contract.fromJson
import java.math.BigInteger

class Converters {

    @TypeConverter
    fun valueFromDomainToStorage(value: Value): String {
        return value.convertToJson()
    }

    @TypeConverter
    fun valueFromStorageToDomain(str: String): Value {
        // we can not create an empty instance of value as TypeDecoder.java should call non-empty constructor
        return Value(
            "just a stub",
            BigInteger.valueOf(0),
            BigInteger.valueOf(0),
            false,
            BigInteger.valueOf(0)
        )
            .fromJson(str)
    }
}