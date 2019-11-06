
sealed class JSONValue

internal object JSONNull: JSONValue() {
    override fun toString() = "null"
}

internal data class JSONString(val value: String): JSONValue() {
    override fun toString() = jsonEncodeString(value)
}

internal data class JSONNumber(val value: Float): JSONValue() {
    override fun toString() = jsonEncodeNumber(value)
}

internal data class JSONBoolean(val value: Boolean): JSONValue() {
    override fun toString() = jsonEncodeBoolean(value)
}

internal data class JSONArray(val values: List<JSONValue> = listOf()): JSONValue() {
    override fun toString() = jsonEncodeArray(values) { it.toString() }
}

internal data class JSONObject(val entries: Map<String, JSONValue> = mapOf()): JSONValue() {
    override fun toString() = jsonEncodeMap(entries) { it.toString() }
}
