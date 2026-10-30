package ru.raiffeisen.daily_randomizer.service;

import java.nio.file.Files;
import java.nio.file.Path;

/** A small executable jar used to exercise the real updater process. */
public final class UpdatedApplicationFixture {
    public static void main(String[] args) throws Exception {
        Path jar = Path.of(UpdatedApplicationFixture.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Files.writeString(jar.getParent().resolve("updated.marker"), "started");
    }
}
