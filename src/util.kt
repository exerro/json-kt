import java.nio.file.Files
import java.nio.file.Paths

fun readFile(file: String)
        = String(Files.readAllBytes(Paths.get(file)))

fun <T> readJSONFile(file: String, decoder: JSONDecoder<T>)
        = decoder(jsonParse(readFile(file)))
