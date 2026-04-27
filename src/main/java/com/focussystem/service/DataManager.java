package com.focussystem.service;

import com.focussystem.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataManager {
    private static final String FILE_PATH = "tasks.json";
    private static final String SUBJECT_FILE = "subjects.json";
    private static final String CONFIG_FILE = "config.json";
    private Gson gson;

    // Singleton Pattern
    private static DataManager instance;

    private DataManager() {
        // Cấu hình Gson để xử lý LocalDate đẹp đẽ (yyyy-MM-dd)
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void saveTasks(List<Task> tasks) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Task> loadTasks() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<Task>>() {
            }.getType();
            List<Task> tasks = gson.fromJson(reader, listType);
            if (tasks != null) {
                tasks.forEach(Task::initCompleted); // Khởi tạo BooleanProperty sau khi Gson deserialize
                return tasks;
            }
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Class con để giúp Gson hiểu LocalDate
    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate date, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(date.toString()); // Lưu dạng chuỗi "2023-10-20"
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString()); // Đọc chuỗi thành Date
        }
    }

    public void saveSubjects(List<Subject> subjects) {
        try (Writer writer = new FileWriter(SUBJECT_FILE)) {
            gson.toJson(subjects, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Subject> loadSubjects() {
        File file = new File(SUBJECT_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(SUBJECT_FILE)) {
            Type listType = new TypeToken<ArrayList<Subject>>() {
            }.getType();
            List<Subject> subjects = gson.fromJson(reader, listType);
            return subjects != null ? subjects : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Subject> importSubjectsFromTextFile(File file) {
        List<Subject> newSubjects = new ArrayList<>();
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty() || line.startsWith("#")) continue; // Bỏ qua dòng trống

                // Cắt chuỗi theo dấu chấm phẩy
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    String hk = parts[0].trim();
                    String code = parts[1].trim();
                    String name = parts[2].trim();
                    int credits = Integer.parseInt(parts[3].trim());
                    boolean isBB = parts[4].trim().equalsIgnoreCase("BB");

                    newSubjects.add(new Subject(hk, code, name, credits, isBB));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newSubjects;
    }

    public void saveConfig(Config config) {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return new Config(""); // Mặc định rỗng

        try (Reader reader = new FileReader(CONFIG_FILE)) {
            Config config = gson.fromJson(reader, Config.class);
            return config != null ? config : new Config("");
        } catch (IOException e) {
            return new Config("");
        }
    }

    // --- IMPORT / EXPORT TASK ---

    // Xuất danh sách Task ra file tùy chọn
    public void exportTasksToJson(File file, List<Task> tasks) {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Nhập danh sách Task từ file tùy chọn
    public List<Task> importTasksFromJson(File file) {
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, listType);
            if (tasks != null) {
                tasks.forEach(Task::initCompleted); // Khởi tạo BooleanProperty sau khi Gson deserialize
                return tasks;
            }
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
