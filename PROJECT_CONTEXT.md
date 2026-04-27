# Focus System - Project Context

_Cập nhật lần cuối: 2026-03-23 sau khi hoàn thành Refactor Task 1-5._

## Overview
Desktop application quản lý Task học tập (JavaFX 21). Người dùng tạo/xóa task theo môn học và học kỳ, theo dõi tiến độ, lọc/tìm kiếm và xuất/nhập dữ liệu JSON.

## Tech Stack
| Thành phần | Công nghệ |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21 (`javafx-controls`) |
| JSON | Gson 2.10.1 |
| Build | Maven + `maven-shade-plugin` (FAT jar) |

## Architecture: MVC
```
src/main/java/com/focussystem/
├── App.java / Launcher.java     – Entry points
├── model/
│   └── Task.java                – Data model (xem thay đổi bên dưới)
├── view/
│   └── TaskView.java            – UI layout & component setup
├── controller/
│   └── TaskController.java      – Event handling & business logic
├── service/
│   └── DataManager.java         – Đọc/ghi JSON (Gson)
└── util/
    └── AlertHelper.java         – Dialog helpers (warning, confirm, success)
```

## Data Persistence
- `tasks.json` – Danh sách Task (tự động save sau mỗi thao tác)
- `subjects.json` – Danh sách môn học
- `config.json` – Cấu hình (học kỳ được chọn lần cuối)

---

## Thay đổi sau Refactor (Task 1–5)

### Task 1 — CSS Contrast Fix (`styles.css`)
- Đã thêm CSS cho `.table-row-cell:selected` với nền tối/trắng tương phản cao.
- Các pseudo-class `:overdue`, `:completed`, `:today` được giữ nguyên nhưng không override màu chữ khi `:selected`.

### Task 2 — Checkbox + Strikethrough (`Task.java`, `TaskView.java`, `DataManager.java`, `TaskController.java`)
**Model `Task.java`:**
- Giữ nguyên field `String status` ("Chưa làm" / "Hoàn thành") để tương thích Gson.
- Thêm `transient BooleanProperty completed` — **không serialize** bởi Gson.
- `initCompleted()`: khởi tạo property sau khi Gson load, bind 2 chiều với `status`.
- `completedProperty()`: trả về BooleanProperty để bind với CheckBoxTableCell.

**View `TaskView.java`:**
- Cột `colStatus` đổi type sang `TableColumn<Task, Boolean>`, dùng `CheckBoxTableCell.forTableColumn()`.
- Cột `colTitle` có custom `TableCell` với strikethrough + opacity 0.55 khi task hoàn thành.

**DataManager.java:** Gọi `task.initCompleted()` sau mỗi lần Gson deserialize (cả `loadTasks` lẫn `importTasksFromJson`).

**Controller `TaskController.java`:**
- Không dùng `setOnEditCommit` cho cột Status nữa.
- `attachCheckboxListener(task)`: gắn listener vào `completedProperty()` → tự động `saveAndRefresh()` khi tick.
- `setupCheckboxListeners()`: gắn listener cho tất cả task hiện có khi khởi tạo.
- `ListChangeListener` trên `taskList`: tự động gắn/gỡ listener khi task được thêm/xóa.

### Task 3 — Click Deselect UX (`TaskController.java`)
- `TableView.setOnMouseClicked`: deselect khi click vùng trống (empty area dưới các dòng).
- `Layout.setOnMouseClicked`: deselect khi click ra ngoài bảng.
- Helper `isInsideNode(node, parent)`: duyệt cây cha con để kiểm tra node thuộc TableView hay không.

### Task 4 — Nút Refresh (`TaskView.java`, `TaskController.java`)
- **Nút mới:** `🔄 Cập nhật` trên Filter Bar (bên trái `⬆ Xuất File`).
- `handleRefresh()`: `taskList.clear()` → `dataManager.loadTasks()` → `taskList.addAll()` → `AlertHelper.showSuccess()`.

### Task 5 — Nút Xóa tất cả (`TaskView.java`, `TaskController.java`)
- **Nút mới:** `🗑 Xóa tất cả` (đỏ đậm `#c0392b`) trong Input Form, cạnh `✕ Xóa Task`.
- `handleDeleteAll()`: Hiển thị Confirmation Dialog → nếu xác nhận: `taskList.clear()` → `saveAndRefresh()`.

---

## UI Layout (sau refactor)

```
┌─────────────────────────────────────────────────────────────┐
│  📋 Danh sách nhiệm vụ & Bộ lọc                            │
├─────────────────────────────────────────────────────────────┤
│  [Tìm:___] [Trạng thái▼] [Thời gian▼] [⟳ Xóa lọc]        │
│                                    [🔄 Cập nhật][⬆][⬇]    │
├─────────────────────────────────────────────────────────────┤
│  TableView: ✔ | Nội dung | Môn | Bắt đầu | Hạn | Còn lại  │
│  (row: strikethrough khi hoàn thành, deselect khi click ngoài) │
├─────────────────────────────────────────────────────────────┤
│  [HK▼] [Môn▼]                                              │
│  [Tiêu đề] [Mô tả] [Bắt đầu] → [Hạn] [✚ Thêm][✕ Xóa][🗑] │
├─────────────────────────────────────────────────────────────┤
│  📊 Tổng: N task  |  ⚠ Quá hạn: N  |  ✅ Hoàn thành: N   │
└─────────────────────────────────────────────────────────────┘
```

## Conventions
- Tất cả save đều đi qua `dataManager.saveTasks(new ArrayList<>(taskList))`.
- AlertHelper chuẩn hóa: `showWarning`, `showSuccess`, `showConfirmation(title, message) → boolean`.
- CSS dùng JavaFX `-fx-*` properties — các IDE warning về "standard property" là bình thường, không cần sửa.
