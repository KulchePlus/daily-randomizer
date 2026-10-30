package ru.raiffeisen.daily_randomizer.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Duration
import java.util.Properties
import java.util.jar.JarFile

/** GitHub Releases — только стабильные версии и исполняемый fat jar. */
class UpdateService(
    val currentVersion: String = AppVersion.current,
    private val directory: Path = Path.of(System.getProperty("user.home"), ".daily-randomizer"),
    private val repository: String = Properties().apply {
        AppVersion.javaClass.getResourceAsStream("/update-repository.properties")?.use { load(it) }
    }.getProperty("repository", "KulchePlus/daily-randomizer"),
    private val fetch: (URI) -> ByteArray = { GitHubDownloads().fetch(it) },
) {
    data class Release(val version: String, val downloadUrl: URI, val checksumUrl: URI)

    var autoUpdates: Boolean
        get() = loadPreferences().getProperty("autoUpdates", "false").toBoolean()
        set(enabled) {
            val properties = loadPreferences().apply { setProperty("autoUpdates", enabled.toString()) }
            Files.createDirectories(directory)
            Files.newOutputStream(directory.resolve("updates.properties")).use { properties.store(it, null) }
        }

    private fun loadPreferences() = Properties().apply {
        val file = directory.resolve("updates.properties")
        if (Files.exists(file)) Files.newInputStream(file).use { load(it) }
    }

    fun check(): Release? {
        require(repository.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) { "Неверный адрес репозитория" }
        val bytes = try {
            fetch(URI("https://api.github.com/repos/$repository/releases/latest"))
        } catch (e: GitHubDownloadException) {
            if (e.status == 404) throw IllegalStateException("Релизы не найдены или нет доступа к репозиторию", e)
            throw e
        }
        return parseRelease(bytes.toString(Charsets.UTF_8), currentVersion)
    }

    fun download(release: Release): Path {
        Files.createDirectories(directory.resolve("updates"))
        val expected = fetch(release.checksumUrl).toString(Charsets.UTF_8).trim().split(Regex("\\s+"))[0]
        require(expected.matches(Regex("[a-fA-F0-9]{64}"))) { "Некорректная контрольная сумма обновления" }
        val bytes = fetch(release.downloadUrl)
        val actual = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        check(actual.equals(expected, ignoreCase = true)) { "Контрольная сумма обновления не совпала. Повторите загрузку." }
        val temp = Files.createTempFile(directory.resolve("updates"), "download-", ".jar")
        try {
            Files.write(temp, bytes)
            JarFile(temp.toFile()).use { jar ->
                check(jar.manifest?.mainAttributes?.getValue("Main-Class") == "org.springframework.boot.loader.launch.JarLauncher") {
                    "Файл обновления не является приложением Daily Randomizer"
                }
                check(jar.getJarEntry("BOOT-INF/classes/ru/raiffeisen/daily_randomizer/DailyRandomizerApplicationKt.class") != null) {
                    "В обновлении не найдена точка входа приложения"
                }
            }
            val destination = directory.resolve("updates/daily-randomizer-${release.version}.jar")
            Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING)
            return destination
        } finally {
            Files.deleteIfExists(temp)
        }
    }

    companion object {
        internal fun parseRelease(json: String, currentVersion: String): Release? {
            val root = ObjectMapper().readTree(json)
            if (root.path("draft").asBoolean() || root.path("prerelease").asBoolean()) return null
            val version = root.path("tag_name").asText().removePrefix("v")
            if (!isNewer(version, currentVersion)) return null
            val assets = root.path("assets")
            val filename = "daily-randomizer-$version.jar"
            fun assetUrl(name: String): URI {
                val asset = assets.firstOrNull { it.path("name").asText() == name }
                    ?: throw IllegalStateException("В релизе отсутствует файл $name")
                // API URL работает и с приватными релизами при наличии токена.
                val uri = URI(asset.path("url").asText())
                require(uri.scheme == "https" && uri.host == "api.github.com" && uri.path.startsWith("/repos/")) {
                    "Некорректный адрес файла обновления"
                }
                return uri
            }
            return Release(version, assetUrl(filename), assetUrl("$filename.sha256"))
        }

        internal fun isNewer(candidate: String, current: String): Boolean {
            fun parts(value: String): List<java.math.BigInteger> {
                require(value.matches(Regex("\\d+\\.\\d+\\.\\d+"))) { "Неизвестный формат версии: $value" }
                return value.split('.').map { it.toBigInteger() }
            }
            return parts(candidate).zip(parts(current)).firstOrNull { (a, b) -> a != b }?.let { (a, b) -> a > b } ?: false
        }
    }
}

class GitHubDownloadException(val status: Int) : IllegalStateException(
    when (status) {
        403, 429 -> "GitHub ограничил запросы. Попробуйте позже."
        401 -> "Нет доступа к обновлениям. Проверьте GitHub-токен."
        else -> "Не удалось получить обновление: HTTP $status"
    },
)

private class GitHubDownloads {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()

    fun fetch(uri: URI): ByteArray {
        var location = uri
        repeat(6) {
            require(location.scheme == "https") { "Обновления доступны только по HTTPS" }
            val builder = HttpRequest.newBuilder(location).timeout(Duration.ofMinutes(3))
                .header("User-Agent", "Daily-Randomizer")
                .header("Accept", if (location.path.contains("/releases/assets/")) "application/octet-stream" else "application/vnd.github+json")
            if (location.host == "api.github.com") {
                System.getenv("DAILY_RANDOMIZER_GITHUB_TOKEN")?.takeIf { it.isNotBlank() }?.let {
                    builder.header("Authorization", "Bearer $it")
                }
            }
            val response = client.send(builder.GET().build(), HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() in listOf(301, 302, 303, 307, 308)) {
                location = location.resolve(response.headers().firstValue("location").orElseThrow())
            } else {
                if (response.statusCode() != 200) throw GitHubDownloadException(response.statusCode())
                return response.body()
            }
        }
        error("Слишком много перенаправлений при загрузке обновления")
    }
}
