package ru.home.swap.wallet.converters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.convertToJson
import ru.home.swap.wallet.contract.fromJson

class ValueTypeAdapter: TypeAdapter<Value>() {

    companion object {
        const val JSON = "json"
    }

    override fun write(out: JsonWriter, value: Value) {
        // annotations are not used due its high cost in serialisation\deserialization
        // and because name of properties are under our full control
        out.beginObject()
        out.name(JSON).value(value.convertToJson())
        out.endObject()
    }

    override fun read(`in`: JsonReader): Value {
        var result = Value()
        `in`.beginObject()
        while (`in`.hasNext()) {
            when(`in`.nextName()) {
                JSON -> { result = result.fromJson(`in`.nextString()) }
                else -> { /* no op */ }
            }
        }
        `in`.endObject()
        return result
    }
}