import astify.ParseError
import astify.TextStream
import astify.parse
import astify.util.tokenP

fun jsonParse(value: String): JSONValue {
    val stream = TextStream.create(value)

    return try {
        parse(stream, jsonLexer, tokenP { jsonValueParser keepLeft eof })
    }
    catch (e: ParseError) {
        throw JSONDecodeError(e.message)
    }
}

inline fun <T> jsonDecode(value: String, fn: JSONDecoder<T>)
        = fn(jsonParse(value))

//////////////////////////////////////////////////////////////////////////////////////////

val jsonDecodeString: JSONDecoder<String> = { when (it) {
    is JSONString -> it.value
    else -> throw JSONDecodeError("Value '$it' is not a string")
} }

val jsonDecodeInteger: JSONDecoder<Int> = { when (it) {
    is JSONNumber -> it.value.toInt()
    else -> throw JSONDecodeError("Value '$it' is not a number")
} }

val jsonDecodeNumber: JSONDecoder<Float> = { when (it) {
    is JSONNumber -> it.value
    else -> throw JSONDecodeError("Value '$it' is not a number")
} }

val jsonDecodeBoolean: JSONDecoder<Boolean> = { when (it) {
    is JSONBoolean -> it.value
    else -> throw JSONDecodeError("Value '$it' is not a boolean")
} }

fun <T> jsonDecodeArray(fn: JSONDecoder<T>): JSONDecoder<List<T>> = { when (it) {
    is JSONArray -> it.values.map(fn)
    else -> throw JSONDecodeError("Value '$it' is not a list")
} }

fun <T> jsonDecodeSet(fn: JSONDecoder<T>): JSONDecoder<Set<T>> = { when (it) {
    is JSONArray -> it.values.map(fn).toSet()
    else -> throw JSONDecodeError("Value '$it' is not a list")
} }

fun <T> jsonDecodeMap(fn: JSONDecoder<T>): JSONDecoder<Map<String, T>> = { when (it) {
    is JSONObject -> it.entries.mapValues { (_, v) -> fn(v) }
    else -> throw JSONDecodeError("Value '$it' is not an object")
} }

fun <T> jsonDecodeObject(value: JSONValue, fn: JSONObjectDecoder.() -> T): T
        = jsonDecodeObject(fn)(value)

fun <T> jsonDecodeObject(fn: JSONObjectDecoder.() -> T): JSONDecoder<T> = { when (it) {
    is JSONObject -> fn(JSONObjectDecoder(it))
    else -> throw JSONDecodeError("Value '$it' is not an object")
} }

fun <T> jsonDecodeOptional(fn: JSONDecoder<T>): JSONDecoder<T?> = { when (it) {
    is JSONNull -> null
    else -> fn(it)
} }

//////////////////////////////////////////////////////////////////////////////////////////

typealias JSONDecoder<T> = (JSONValue) -> T

class JSONDecodeError(message: String): Exception(message)

class JSONObjectDecoder internal constructor(private val value: JSONObject) {
    fun <T> decodeEntry(
            key: String,
            decoder: JSONDecoder<T>
    ) = when (val entry = value.entries[key]) {
        null -> throw JSONDecodeError("No such key '$key' in object '$value'")
        else -> decoder(entry)
    }

    fun <T> decodeOptionalEntry(
            key: String,
            decoder: JSONDecoder<T>
    ) = when (val entry = value.entries[key]) {
        null -> null
        else -> decoder(entry)
    }

    operator fun <T> String.div(decoder: JSONDecoder<T>)
            = decodeEntry(this, decoder)
}

//////////////////////////////////////////////////////////////////////////////////////////

fun main() {
    println(jsonDecode("\"hello\"", jsonDecodeString))
    // println(jsonDecode("\"\\\\\\\"\"", jsonDecodeString)) // TODO: fix string escapes
    println(jsonDecode("5", jsonDecodeInteger))
    println(jsonDecode("5", jsonDecodeNumber))
    println(jsonDecode("5.6", jsonDecodeNumber))
    println(jsonDecode("true", jsonDecodeBoolean))

    println(jsonDecode("[1, 2, 3]", jsonDecodeArray(jsonDecodeInteger)))
    println(jsonDecode("[1, 2, 3]", jsonDecodeSet(jsonDecodeInteger)))
    println(jsonDecode("{\"one\": 1, \"two\": 2}", jsonDecodeMap(jsonDecodeInteger)))

    println(jsonDecode("{\"first\": 1, \"second\": 2}", jsonDecodeObject {
        decodeEntry("first", jsonDecodeInteger) to decodeEntry("second", jsonDecodeInteger)
    }))
    println(jsonDecode("{\"first\": 1}", jsonDecodeObject {
        decodeEntry("first", jsonDecodeInteger) to decodeOptionalEntry("second", jsonDecodeInteger)
    }))
    println(jsonDecode("{\"first\": 1, \"second\": 2}", jsonDecodeObject {
        decodeEntry("first", jsonDecodeInteger) to decodeOptionalEntry("second", jsonDecodeInteger)
    }))

    println(jsonDecode("null", jsonDecodeOptional(jsonDecodeNumber)))
    println(jsonDecode("5.4", jsonDecodeOptional(jsonDecodeNumber)))
}
