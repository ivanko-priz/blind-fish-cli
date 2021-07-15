package com.mycompany.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsHandler {
    public static void createSettingsFile() throws IOException {        
        Files.createDirectory(Path.of(Settings.DIR));
        Files.createFile(Path.of(Settings.DIR, Settings.FILE));
    }

    public static Settings getSettingsFile(Path path) throws IOException, ClassNotFoundException {
        Settings settings;

        try(
            FileInputStream fis = new FileInputStream(path.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            settings = (Settings)ois.readObject();
        } catch(IOException e) {
            throw new IOException(e);
        } catch(ClassNotFoundException e) {
            throw new ClassNotFoundException("Cannot access settings, check if it present");
        }

        return settings;
    }

    public static void serializeSettings(Settings settings, Path path) throws IOException {
        try(
            FileOutputStream fos = new FileOutputStream(path.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(settings);
        } catch(IOException e) {
            throw new IOException(e);
        }
    }
}
