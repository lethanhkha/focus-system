package com.focussystem.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Task {
    private String title;       // Tên công việc
    private String subject;     // Tên môn học
    private String description; // Ghi chú
    private LocalDate startDate; // Ngày bắt đầu
    private LocalDate dueDate;   // Hạn chót
    private Priority priority;   // Mức độ ưu tiên
    private LocalDate deadline;  // Ngày đến hạn
    private LocalDate completedAt; // Ngày hoàn thành
    private String status;      // Lưu vào JSON: "Chưa hoàn thành" | "Hoàn thành"

    // JavaFX property for CheckBoxTableCell binding (transient – NOT serialized by Gson)
    private transient BooleanProperty completed;

    // Constructor cập nhật 5 tham số
    public Task(String title, String subject, LocalDate startDate, LocalDate dueDate, LocalDate deadline, Priority priority, String description) {
        this.title = title;
        this.subject = subject;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.deadline = deadline;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.description = description;
        this.status = "Chưa hoàn thành"; // Mặc định trạng thái ban đầu
        initCompleted();
    }

    /**
     * Khởi tạo BooleanProperty từ trường status.
     * Phải gọi sau khi Gson deserialize (vì Gson không gọi constructor).
     * Listener hai chiều: completed <-> status đồng bộ nhau.
     */
    public void initCompleted() {
        // Cập nhật các trạng thái cũ khi load JSON
        if ("Chưa làm".equals(this.status) || "Đang làm".equals(this.status) || this.status == null) {
            this.status = "Chưa hoàn thành";
        }

        // Tương thích ngược: Nếu task cũ chưa có deadline, gán bằng dueDate
        if (this.deadline == null) {
            this.deadline = this.dueDate;
        }

        // Tương thích ngược: Mức độ ưu tiên mặc định cho task cũ
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        
        boolean isTick = "Hoàn thành".equals(this.status);
        completed = new SimpleBooleanProperty(isTick);
        completed.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                this.status = "Hoàn thành";
            } else {
                this.status = "Chưa hoàn thành";
            }
        });
    }

    // --- Logic tính ngày ---
    public long getDaysRemaining() {
        if (dueDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    // --- isCompleted helpers ---
    public BooleanProperty completedProperty() {
        if (completed == null) initCompleted(); // Safety: Gson bypass constructor
        return completed;
    }

    public boolean isCompleted() {
        return completedProperty().get();
    }

    public void setCompleted(boolean value) {
        completedProperty().set(value);
    }

    // --- Getters & Setters (Bắt buộc phải có đủ) ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDate completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        // Đồng bộ lại BooleanProperty
        if (completed != null) {
            completed.set("Hoàn thành".equals(status));
        }
    }
}