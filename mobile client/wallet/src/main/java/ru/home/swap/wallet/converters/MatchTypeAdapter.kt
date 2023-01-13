package ru.home.swap.wallet.converters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.contract.convertToJson
import ru.home.swap.wallet.contract.fromJson

class MatchTypeAdapter: TypeAdapter<Match>() {

    companion object {
        const val JSON = "json"
    }

    override fun write(out: JsonWriter, value: Match) {
        out.beginObject()
        out.name(ValueTypeAdapter.JSON).value(value.convertToJson())
        out.endObject()
    }

    override fun read(`in`: JsonReader): Match {
        var result = Match()
        `in`.beginObject()
        while (`in`.hasNext()) {
            when(`in`.nextName()) {
                ValueTypeAdapter.JSON -> { result = result.fromJson(`in`.nextString()) }
                else -> { /* no op */ }
            }
        }
        `in`.endObject()
        return result
    }
}