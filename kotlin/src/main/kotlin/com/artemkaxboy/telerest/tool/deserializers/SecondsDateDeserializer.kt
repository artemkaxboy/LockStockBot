package com.artemkaxboy.telerest.tool.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.text.ParseException
import java.util.Date

/**
 * Custom date serializer to convert unix-epoch seconds value to [Date]
 *
 * **See also** [spring documentation](https://www.baeldung.com/jackson-serialize-dates#13-custom-date-deserializer)
 */
class SecondsDateDeserializer(vc: Class<*>? = null) : StdDeserializer<Date>(vc) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Date {
        try {
            return Date("${p.text}000".toLong())
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }
}
