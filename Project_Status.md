# Focus System - Project Status

## 1. Cấu trúc thư mục hiện tại
Dựa vào hệ thống file, dự án có cấu trúc thư mục tiêu chuẩn của một ứng dụng Java Maven:
- **`.agentskills/`, `.git/`, `.github/`, `.idea/`**: Chứa cấu hình môi trường, version control và workflow.
- **`src/`**: Mã nguồn chính của dự án.
- **`target/`, `dist/`**: Thư mục chứa file build và executable (FAT jar).
- **Các file JSON (`tasks.json`, `subjects.json`, `config.json`)**: Dữ liệu local của ứng dụng.
- **Tài liệu**: `README.md`, `PROJECT_CONTEXT.md` (chứa bối cảnh dự án), `REFACTOR_TASKS.md` (chứa các công việc cần thực hiện).
- **`pom.xml`**: Cấu hình dependencies (Gson, JavaFX) và build file.

## 2. Kiến trúc hệ thống
Hệ thống được thiết kế theo mô hình **MVC** dùng giao diện **JavaFX 21**:
- **Entry points**: `App.java` / `Launcher.java`.
- **Model (`src/main/java/com/focussystem/model/`)**: Define các đối tượng dữ liệu như `Task`, `Subject`. Dữ liệu được serialize sử dụng Gson.
- **View (`src/main/java/com/focussystem/view/`)**: Khởi tạo UI component, binding, table config (`TaskView.java`).
- **Controller (`src/main/java/com/focussystem/controller/`)**: Xử lý logic nghiệp vụ, tiếp nhận event từ UI và gọi logic, quản lý filter (`TaskController.java`).
- **Service (`src/main/java/com/focussystem/service/`)**: Quản lý thao tác đọc/ghi file như local JSON file (`DataManager.java`).
- **Utility (`src/main/java/com/focussystem/util/`)**: Các helper chung như `AlertHelper.java` để xử lý các Dialog.

Hệ thống lưu trữ (Persistence) hoạt động offline local:
- `tasks.json` để lưu trữ danh sách Tasks
- `subjects.json` quản lý các loại Mon học/Subject
- `config.json` giữ các tuỳ chỉnh User preferences

## 3. Các tính năng đã hoàn thiện
Theo tài liệu `PROJECT_CONTEXT.md`, đã hoàn tất quá trình refactor từ Task 1 đến 5:
- **Cải thiện CSS TableView (Task 1)**: Cải thiện độ tương phản khi bôi đen row, sửa màu UI bị chìm.
- **Nâng cấp tính năng Trạng thái (Task 2)**: Đổi Status string thành UI Checkbox. Sau khi tick, task được hiển thị gạch ngang (Strikethrough) và bị làm mờ 50%.
- **Cải thiện UX khi click (Task 3)**: Người dùng click ra vùng trống mặc định TableView sẽ huỷ select item (Deselect).
- **Làm mới dữ liệu (Task 4)**: Cung cấp tính năng reload dữ liệu thủ công từ file `tasks.json` xuống ứng dụng (Refresh).
- **Xóa tất cả (Task 5)**: Bổ sung hành động xoá toàn bộ danh sách task (kèm theo Warning confirmation box).
- Import / Export Tasks JSON thông qua các Input form. 

## 4. Trạng thái Code dở dang / TODO
Qua quá trình rà soát toàn bộ source code (`src/`):
- Không tìm thấy bất kì keyword `TODO` hay `FIXME` nào trong source code Java. Nghĩa là code base hiện tại **không có file nào đang code dở dang**.
- **Tuy nhiên**, trong tài liệu `REFACTOR_TASKS.md`, phần checkbox công việc của Task 1 đến Task 6 đều đang để nháp chưa đánh dấu hoàn thành (`[ ]`) mắc dù code đã được implement xong. Đây là file có thể xem như còn cần được "cập nhật" (Task 6) để tick xanh các thay đổi.
