package ru.home.swap.wallet.storage

import androidx.room.TypeConverter
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.convertToJson
import ru.home.swap.wallet.contract.fromJson
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