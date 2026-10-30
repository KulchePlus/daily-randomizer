package ru.raiffeisen.daily_randomizer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateInstallerTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `installer backs up original jar replaces it and launches new application`() {
        val folder = Files.createDirectories(directory.resolve("folder with spaces"))
        val target = folder.resolve("app.jar")
        val staged = folder.resolve("download.jar")
        Files.writeString(target, "previous version")
        val fixture = "ru/raiffeisen/daily_randomizer/service/UpdatedApplicationFixture.class"
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = "ru.raiffeisen.daily_randomizer.service.UpdatedApplicationFixture"
        }
        JarOutputStream(Files.newOutputStream(staged), manifest).use { jar ->
            jar.putNextEntry(JarEntry(fixture))
            javaClass.getResourceAsStream("/$fixture")!!.use { it.copyTo(jar) }
            jar.closeEntry()
        }
        val installerClass = Class.forName("ru.raiffeisen.daily_randomizer.service.UpdateInstaller")
        val classpath = Path.of(installerClass.protectionDomain.codeSource.location.toURI())
        val executable = if (System.getProperty("os.name").lowercase().contains("win")) "java.exe" else "java"
        val java = Path.of(System.getProperty("java.home"), "bin", executable)
        val process = ProcessBuilder(java.toString(), "-cp", classpath.toString(), installerClass.name,
            "99999999", target.toString(), staged.toString()).redirectErrorStream(true)
            .redirectOutput(folder.resolve("install.log").toFile()).start()
        assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Updater must finish")
        assertEquals(0, process.exitValue(), Files.readString(folder.resolve("install.log")))
        assertEquals("previous version", Files.readString(folder.resolve("app.jar.previous")))
        assertTrue(Files.readAllBytes(staged).contentEquals(Files.readAllBytes(target)))
        val marker = folder.resolve("updated.marker")
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (!Files.exists(marker) && System.nanoTime() < deadline) Thread.sleep(25)
        assertTrue(Files.exists(marker), "New application must start")
    }
}
