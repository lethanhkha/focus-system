package com.focussystem.model;

public class Subject {
    private String semester; // VD: HK2_2025
    private String code;     // VD: SE104
    private String name;     // VD: Kỹ thuật phần mềm
    private int credits;     // Số tín chỉ
    private boolean isCompulsory; // true = Bắt buộc, false = Tự chọn
    private boolean isActive;     // true = Đang học môn này (đã đăng ký)

    // Constructor cho việc nhập tay hoặc mặc định
    public Subject(String semester, String code, String name, int credits, boolean isCompulsory) {
        this.semester = semester;
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.isCompulsory = isCompulsory;

        // Logic: Nếu là Bắt buộc -> Tự động Active. Nếu Tự chọn -> Chờ người dùng tích chọn.
        this.isActive = isCompulsory;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    // Getters & Setters
    public String getSemester() { return semester; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getCredits() { return credits; }
    public boolean isCompulsory() { return isCompulsory; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper để hiển thị trên bảng cho đẹp
    public String getTypeString() { return isCompulsory ? "Bắt buộc" : "Tự chọn"; }
}