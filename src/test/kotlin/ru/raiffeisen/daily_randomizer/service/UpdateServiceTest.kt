package ru.raiffeisen.daily_randomizer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateServiceTest {
    @TempDir lateinit var directory: Path

    private fun json(version: String = "2.2.0", prerelease: Boolean = false) = """{
        "tag_name":"v$version", "draft":false, "prerelease":$prerelease,
        "assets":[
          {"name":"daily-randomizer-$version.jar", "url":"https://api.github.com/repos/owner/repo/releases/assets/1"},
          {"name":"daily-randomizer-$version.jar.sha256", "url":"https://api.github.com/repos/owner/repo/releases/assets/2"}
        ]
    }"""

    @Test
    fun `versions compare numeric components and never downgrade`() {
        assertTrue(UpdateService.isNewer("2.10.0", "2.9.9"))
        assertTrue(UpdateService.isNewer("3.0.0", "2.99.99"))
        assertFalse(UpdateService.isNewer("2.1.0", "2.1.0"))
        assertFalse(UpdateService.isNewer("2.0.9", "2.1.0"))
        assertThrows<IllegalArgumentException> { UpdateService.isNewer("../../file", "2.1.0") }
    }

    @Test
    fun `check finds newer stable version and ignores older or prerelease`() {
        val service = UpdateService("2.1.0", directory, "owner/repo") { uri ->
            assertEquals("https://api.github.com/repos/owner/repo/releases/latest", uri.toString())
            json().toByteArray()
        }
        assertEquals("2.2.0", service.check()?.version)
        assertNull(UpdateService.parseRelease(json("2.0.0"), "2.1.0"))
        assertNull(UpdateService.parseRelease(json(prerelease = true), "2.1.0"))
        assertThrows<IllegalStateException> { UpdateService.parseRelease("""{"tag_name":"v3.0.0","assets":[]}""", "2.1.0") }
    }

    @Test
    fun `auto updates default off and preference survives restart`() {
        val service = UpdateService("2.1.0", directory)
        assertFalse(service.autoUpdates)
        service.autoUpdates = true
        assertTrue(UpdateService("2.1.0", directory).autoUpdates)
        service.autoUpdates = false
        assertFalse(UpdateService("2.1.0", directory).autoUpdates)
    }

    private fun jar(): ByteArray {
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = "org.springframework.boot.loader.launch.JarLauncher"
        }
        val output = ByteArrayOutputStream()
        JarOutputStream(output, manifest).use {
            it.putNextEntry(JarEntry("BOOT-INF/classes/ru/raiffeisen/daily_randomizer/DailyRandomizerApplicationKt.class"))
            it.write(byteArrayOf(1, 2, 3))
            it.closeEntry()
        }
        return output.toByteArray()
    }

    private fun release() = UpdateService.Release("2.2.0", URI("https://api.github.com/jar"), URI("https://api.github.com/checksum"))

    @Test
    fun `download verifies checksum and executable jar`() {
        val bytes = jar()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        val service = UpdateService("2.1.0", directory, "owner/repo") {
            if (it.path == "/checksum") "$digest  daily-randomizer-2.2.0.jar\n".toByteArray() else bytes
        }
        val downloaded = service.download(release())
        assertEquals(directory.resolve("updates/daily-randomizer-2.2.0.jar"), downloaded)
        assertTrue(Files.readAllBytes(downloaded).contentEquals(bytes))
    }

    @Test
    fun `corrupt download preserves existing downloaded version`() {
        val destination = directory.resolve("updates/daily-randomizer-2.2.0.jar")
        Files.createDirectories(destination.parent)
        Files.writeString(destination, "existing")
        val service = UpdateService("2.1.0", directory, "owner/repo") {
            if (it.path == "/checksum") "0".repeat(64).toByteArray() else jar()
        }
        assertThrows<IllegalStateException> { service.download(release()) }
        assertEquals("existing", Files.readString(destination))
    }

    @Test
    fun `untrusted asset URLs are rejected`() {
        assertThrows<IllegalArgumentException> {
            UpdateService.parseRelease(json().replace("https://api.github.com", "https://example.com"), "2.1.0")
        }
    }
}
