import astify.Token
import astify.monadic.*
import astify.monadic.util.*

typealias JSONParser<Value> = P<ListParserState<Token>, String, Value>
typealias JP<Value> = JSONParser<Value>

internal val jsonValueParser: JP<JSONValue> = p { oneOf(
        stringValue map (::JSONString),
        lazy { numeric },
        symbol("-") keepRight lazy { numeric } map { JSONNumber(-it.value) },
        keyword("true") map { JSONBoolean(true) },
        keyword("false") map { JSONBoolean(false) },
        keyword("null") map { JSONNull },
        lazy { jsonObjectParser },
        lazy { jsonArrayParser }
) }

internal val numeric: JP<JSONNumber> = p {
    numberValue map(::JSONNumber) or (integerValue map { JSONNumber(it.toFloat()) })
}

internal val jsonObjectMemberParser: JP<Pair<String, JSONValue>> = p {
    stringValue keepLeft symbol(":") and jsonValueParser
}

internal val jsonObjectParser: JP<JSONValue> = p {
    wrapDelimitedSymbols(jsonObjectMemberParser, "{", "}") map { JSONObject(it.toMap()) }
}

internal val jsonArrayParser: JP<JSONValue> = p {
    wrapDelimitedSymbols(jsonValueParser, "[", "]") map { JSONArray(it) } }

internal val jsonLexer = lexer(tokenParser(setOf("true", "false", "null")))

//////////////////////////////////////////////////////////////////////////////////////////

const val TEST_STR = "{\n" +
        "\"name\": \"LED\",\n" +
        "\"attributes\": [\n" +
        "{\n" +
        "\"name\": \"pin\",\n" +
        "\"default\": 13\n" +
        "},\n" +
        "{\n" +
        "\"name\": \"initial\",\n" +
        "\"default\": 0\n" +
        "}\n" +
        "],\n" +
        "\"methods\": [\n" +
        "{\n" +
        "\"name\": \"write\",\n" +
        "\"parameters\": [\"on\"],\n" +
        "\"returns\": false\n" +
        "},\n" +
        "{\n" +
        "\"name\": \"on\",\n" +
        "\"parameters\": [],\n" +
        "\"returns\": false\n" +
        "},\n" +
        "{\n" +
        "\"name\": \"off\",\n" +
        "\"parameters\": [],\n" +
        "\"returns\": false\n" +
        "},\n" +
        "{\n" +
        "\"name\": \"toggle\",\n" +
        "\"parameters\": [],\n" +
        "\"returns\": false\n" +
        "}\n" +
        "]\n" +
        "}"

fun main() {
    println(jsonParse(TEST_STR))
}
