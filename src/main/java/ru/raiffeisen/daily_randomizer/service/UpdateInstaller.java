package ru.raiffeisen.daily_randomizer.service;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

/** Runs from a separate tiny jar, so Windows can release the application jar. */
public final class UpdateInstaller {
    public static void main(String[] args) {
        Path target = Path.of(args[1]);
        Path staged = Path.of(args[2]);
        Path backup = target.resolveSibling(target.getFileName() + ".previous");
        boolean replaced = false;
        try {
            var running = ProcessHandle.of(Long.parseLong(args[0]));
            if (running.isPresent()) running.get().onExit().get(60, TimeUnit.SECONDS);
            Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.copy(staged, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception failure) {
                Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                throw failure;
            }
            replaced = true;
            start(target);
        } catch (Exception failure) {
            if (replaced) {
                try {
                    Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                    start(target);
                } catch (Exception ignored) { }
            }
            JOptionPane.showMessageDialog(null,
                "Не удалось установить обновление: " + failure.getMessage()
                    + "\nПредыдущая версия: " + backup + "\nЗагруженная версия: " + staged,
                "Обновление Daily Randomizer", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void start(Path jar) throws Exception {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
            "-jar", jar.toString()).inheritIO().start();
    }
}
