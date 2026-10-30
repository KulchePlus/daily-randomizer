package ru.raiffeisen.daily_randomizer.service

import org.springframework.boot.system.ApplicationHome
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

/** State lives beyond the settings dialog and survives theme changes. */
class UpdateController(private val service: UpdateService = UpdateService()) {
    val currentVersion = service.currentVersion
    var autoUpdates = runCatching { service.autoUpdates }.getOrDefault(false)
        private set
    var release: UpdateService.Release? = null
        private set
    var busy = false
        private set
    var status = "Нажмите «Проверить обновления»"
        private set
    private var downloaded: Path? = null
    private val listeners = mutableListOf<() -> Unit>()
    private val executor = Executors.newSingleThreadExecutor { task -> Thread(task, "updates").apply { isDaemon = true } }

    fun subscribe(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        listener()
        return { listeners.remove(listener) }
    }

    private fun notifyChanged() = listeners.toList().forEach { it() }

    fun setAutoUpdates(enabled: Boolean) {
        try {
            service.autoUpdates = enabled
            autoUpdates = enabled
            notifyChanged()
            if (enabled && !busy) checkForUpdates()
        } catch (e: Exception) {
            status = "Не удалось сохранить настройку автообновлений"
            notifyChanged()
        }
    }

    fun checkForUpdates() {
        if (busy) return
        busy = true
        status = "Проверяем обновления…"
        notifyChanged()
        executor.execute {
            try {
                val found = service.check()
                SwingUtilities.invokeLater {
                    release = found
                    downloaded = null
                    busy = false
                    status = if (found == null) "Установлена последняя версия" else "Доступна версия ${found.version}"
                    notifyChanged()
                    if (found != null && autoUpdates) download(install = false)
                }
            } catch (e: Exception) {
                failed(e, "Не удалось проверить обновления. Проверьте подключение к интернету.")
            }
        }
    }

    fun update() {
        if (busy || release == null) return
        val ready = downloaded
        if (ready != null) install(ready) else download(install = true)
    }

    private fun download(install: Boolean) {
        val selected = release ?: return
        busy = true
        status = "Скачиваем версию ${selected.version}…"
        notifyChanged()
        executor.execute {
            try {
                val file = service.download(selected)
                SwingUtilities.invokeLater {
                    downloaded = file
                    busy = false
                    status = "Обновление загружено. Нажмите «Обновить до версии ${selected.version}» для перезапуска."
                    notifyChanged()
                    if (install) install(file)
                }
            } catch (e: Exception) {
                failed(e, "Не удалось скачать обновление. Проверьте подключение к интернету.")
            }
        }
    }

    private fun install(staged: Path) {
        try {
            val target = checkNotNull(ApplicationHome(AppVersion.javaClass).source) { "Не найден JAR приложения" }.toPath().toAbsolutePath()
            check(Files.isRegularFile(target) && target.fileName.toString().endsWith(".jar")) {
                "Обновление доступно при запуске собранного JAR. При запуске из IDE пересоберите проект."
            }
            check(Files.isWritable(target) && Files.isWritable(target.parent)) {
                "Нет доступа для записи в папку приложения. Переместите JAR в доступную папку."
            }
            val helper = Files.createTempFile("daily-randomizer-updater-", ".jar")
            val manifest = Manifest().apply {
                mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
                mainAttributes[Attributes.Name.MAIN_CLASS] = "ru.raiffeisen.daily_randomizer.service.UpdateInstaller"
            }
            val resource = "ru/raiffeisen/daily_randomizer/service/UpdateInstaller.class"
            AppVersion.javaClass.getResourceAsStream("/$resource").use { input ->
                checkNotNull(input) { "Не найден установщик обновлений" }
                JarOutputStream(Files.newOutputStream(helper), manifest).use { jar ->
                    jar.putNextEntry(JarEntry(resource))
                    input.copyTo(jar)
                    jar.closeEntry()
                }
            }
            val java = Path.of(System.getProperty("java.home"), "bin", if (System.getProperty("os.name").lowercase().contains("win")) "java.exe" else "java")
            ProcessBuilder(java.toString(), "-jar", helper.toString(), ProcessHandle.current().pid().toString(), target.toString(), staged.toString())
                .redirectErrorStream(true).redirectOutput(helper.resolveSibling("${helper.fileName}.log").toFile()).start()
            exitProcess(0)
        } catch (e: Exception) {
            busy = false
            status = e.message ?: "Не удалось запустить установщик обновления"
            notifyChanged()
        }
    }

    private fun failed(error: Exception, fallback: String) {
        SwingUtilities.invokeLater {
            busy = false
            status = if (error is IllegalStateException || error is IllegalArgumentException) error.message ?: fallback else fallback
            notifyChanged()
        }
    }
}
