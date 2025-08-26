package com.cubefury.vendingmachine.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Future;

import net.minecraft.util.ChatAllowedCharacters;

import com.cubefury.vendingmachine.VendingMachine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public class FileIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();

    public static JsonObject ReadFromFile(File file) {
        Future<JsonObject> task = ThreadedIO.INSTANCE.enqueue(() -> {
            if (
                file == null || !file.exists()
                    || file.getName()
                        .contains(".DS_Store")
                    || file.getName()
                        .contains("malformed_")
            ) {
                return new JsonObject();
            }

            try (FileInputStream fis = new FileInputStream(file);
                InputStreamReader fr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                java.io.BufferedReader br = new java.io.BufferedReader(fr, 32768)) { // 32KB buffer
                return GSON.fromJson(br, JsonObject.class);
            } catch (Exception e) {
                VendingMachine.LOG.error("Error reading JSON from file: ", e);
                File backup = new File(file.getParent(), "malformed_" + file.getName() + ".json");

                VendingMachine.LOG.error("Creating backup at: {}", backup.getAbsolutePath());
                CopyPaste(file, backup);
                return new JsonObject();
            }
        });

        try {
            return task.get(); // Wait for other scheduled file ops to finish
        } catch (Exception e) {
            VendingMachine.LOG.error("Unable to read from file {}", file, e);
            return new JsonObject();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Future<Void> WriteToFile(File file, JsonHelper.IOConsumer<JsonWriter> jObj) {
        final File tmp = new File(file.getAbsolutePath() + ".tmp");

        return ThreadedIO.DISK_IO.enqueue(() -> {
            try {
                if (tmp.exists()) tmp.delete();
                else if (tmp.getParentFile() != null) tmp.getParentFile()
                    .mkdirs();

                tmp.createNewFile();
            } catch (Exception e) {
                VendingMachine.LOG.error("An error occurred while saving JSON to file (Directory setup):", e);
                return null;
            }

            // NOTE: These are now split due to an edge case in the previous implementation where resource leaking can
            // occur should the outer constructor fail
            try (FileOutputStream fos = new FileOutputStream(tmp);
                OutputStreamWriter fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                Writer buffer = new BufferedWriter(fw);
                JsonWriter json = new JsonWriter(buffer)) {
                json.setIndent("\t");
                jObj.accept(json);
            } catch (Exception e) {
                VendingMachine.LOG.error("An error occurred while saving JSON to file (File write):", e);
                return null;
            }

            try {
                Files.move(
                    tmp.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                try {
                    Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    VendingMachine.LOG.error("An error occurred while saving JSON to file (Temp copy):", e);
                }
            } catch (Exception e) {
                VendingMachine.LOG.error("An error occurred while saving JSON to file (Temp copy):", e);
            }
            return null;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void CopyPaste(File fileIn, File fileOut) {
        if (!fileIn.exists()) return;

        try {
            if (fileOut.getParentFile() != null) fileOut.getParentFile()
                .mkdirs();
            Files.copy(fileIn.toPath(), fileOut.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            VendingMachine.LOG.error("Failed copy paste", e);
        }
    }

    public static String makeFileNameSafe(String s) {
        for (char c : ChatAllowedCharacters.allowedCharacters) {
            s = s.replace(c, '_');
        }

        return s;
    }

}
