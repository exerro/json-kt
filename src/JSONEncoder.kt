
fun <T> jsonEncodeArray(items: List<T>, fn: JSONEncoder<T> = jsonEncoder)
        = "[${items.joinToString(transform=fn)}]"

fun <T> jsonEncodeSet(items: Set<T>, fn: JSONEncoder<T> = jsonEncoder)
        = "[${items.joinToString(transform=fn)}]"

fun <T> jsonEncodeMap(
        items: Map<String, T>,
        fn: JSONEncoder<T> = jsonEncoder
) = jsonEncodeObject(items) { it.forEach { (k, v) ->
    entry(k, v, fn)
} }

inline fun jsonEncodeObject(fn: JSONObjectEncoder.() -> Unit): String {
    val encoder = JSONObjectEncoder()
    fn(encoder)
    return encoder.toString()
}

inline fun <T> jsonEncodeObject(o: T, fn: JSONObjectEncoder.(T) -> Unit): String {
    val encoder = JSONObjectEncoder()
    fn(encoder, o)
    return encoder.toString()
}

fun <T> jsonEncodeOptional(opt: T?, encoder: JSONEncoder<T> = jsonEncoder)
        = if (opt == null) "null" else encoder(opt)

fun <T> jsonEncode(value: T?) = jsonEncoder(value)

//////////////////////////////////////////////////////////////////////////////////////////

val jsonEncodeString: JSONEncoder<String>
        = { "\"${escapeSpecialCharacters(it)}\"" }

val jsonEncodeInteger: JSONEncoder<Int>
        = { it.toString() }

val jsonEncodeNumber: JSONEncoder<Float>
        = { it.toString() }

val jsonEncodeBoolean: JSONEncoder<Boolean>
        = { it.toString() }

fun <T> jsonEncodeArray(fn: JSONEncoder<T> = jsonEncoder): JSONEncoder<List<T>>
        = { items -> "[${items.joinToString(transform=fn)}]" }

fun <T> jsonEncodeSet(fn: JSONEncoder<T> = jsonEncoder): JSONEncoder<Set<T>>
        = { items -> "[${items.joinToString(transform=fn)}]" }

fun <T> jsonEncodeMap(fn: JSONEncoder<T> = jsonEncoder): JSONEncoder<Map<String, T>>
        = jsonEncodeObject { value -> value.forEach { (k, v) ->
              entry(k, v, fn)
          } }

fun <T> jsonEncodeObject(fn: JSONObjectEncoder.(T) -> Unit): JSONEncoder<T> = {
    val encoder = JSONObjectEncoder()
    fn(encoder, it)
    encoder.toString()
}

fun <T> jsonEncodeOptional(fn: JSONEncoder<T>): JSONEncoder<T?>
        = { if (it == null) "null" else fn(it) }

val jsonEncoder: JSONEncoder<Any?> = { when (it) {
    is String -> jsonEncodeString(it)
    is Int -> jsonEncodeInteger(it)
    is Float -> jsonEncodeNumber(it)
    is Boolean -> jsonEncodeBoolean(it)
    is List<*> -> jsonEncodeArray<Any?>()(it)
    is Set<*> -> jsonEncodeSet<Any?>()(it)
    null -> "null"
    else -> throw JSONEncodeError("Cannot encode type")
} }

//////////////////////////////////////////////////////////////////////////////////////////

typealias JSONEncoder<T> = (T) -> String

class JSONObjectEncoder {
    fun rawEntry(key: String, value: String) {
        entries.add(jsonEncode(key) + ": " + value)
    }

    fun <T> entry(key: String, value: T, encoder: JSONEncoder<T> = jsonEncoder) {
        rawEntry(key, encoder(value))
    }

    operator fun <T> String.minus(value: Pair<T, JSONEncoder<T>>)
            = entry(this, value.first, value.second)
    operator fun <T> String.minus(value: T) = entry(this, value)
    operator fun <T> T.div(encoder: JSONEncoder<T>) = this to encoder

    override fun toString(): String {
        if (entries.isEmpty()) return "{}"
        return "{\n\t" + entries.joinToString(",\n\t") { it.replace("\n", "\n\t") } + "\n}"
    }

    private val entries = mutableListOf<String>()
}

class JSONEncodeError(message: String): Exception(message)

//////////////////////////////////////////////////////////////////////////////////////////

private fun escapeSpecialCharacters(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

//////////////////////////////////////////////////////////////////////////////////////////

// Test JSON encoding
fun main() {
    println(jsonEncodeString("hello"))
    println(jsonEncodeString("\"hello\nhi\\"))
    println(jsonEncodeInteger(1))
    println(jsonEncodeNumber(1.3f))
    println(jsonEncodeBoolean(false))

    println(jsonEncodeArray(listOf(1, 2, 3)))
    println(jsonEncodeSet(setOf(1, 2, 3)))
    println(jsonEncodeMap(mapOf("one" to 1, "two" to 2, "three" to 3)))

    println(jsonEncodeObject("a" to "b") {
        entry("first", it.first)
        entry("second", it.second)
    })
    println(jsonEncodeObject("a" to (1 to 2)) {
        entry("first", it.first)
        entry("second", it.second, jsonEncodeObject { inner ->
            entry("first", inner.first)
            entry("first", inner.second)
        })
        rawEntry("test", "false")
    })

    println(jsonEncodeOptional(null as String?))
    println(jsonEncodeOptional("hi", jsonEncodeString))
    println(jsonEncodeOptional(null, jsonEncodeString))

    println(jsonEncode("str"))
    println(jsonEncode(1))
    println(jsonEncode(1.66f))
    println(jsonEncode(false))
    println(jsonEncode(listOf("l1", "l2")))
    println(jsonEncode(setOf("s1", "s2")))
    println(jsonEncode(null))

    println(jsonEncodeArray(listOf(1 to 2, 3 to 4), jsonEncodeObject { value ->
        entry("a", value.first)
        entry("b", value.second)
    }))

    println(JSONObject(mapOf(
            "a" to JSONNumber(4f),
            "b" to JSONArray(listOf(
                    JSONString("hi"),
                    JSONBoolean(true)
            )),
            "c" to JSONNull
    )))
}
