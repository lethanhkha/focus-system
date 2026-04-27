# Focus System

Ứng dụng desktop quản lý môn học và nhiệm vụ học tập, được xây dựng bằng JavaFX theo kiến trúc MVC.

## 1) Tổng quan

Focus System giúp bạn:

- Quản lý danh sách môn học theo học kỳ.
- Tạo, sửa, xóa, lọc và tìm kiếm task (nhiệm vụ) học tập.
- Đánh dấu task hoàn thành bằng checkbox.
- Theo dõi trạng thái: quá hạn, hôm nay, hoàn thành.
- Lưu trữ và tải dữ liệu từ file JSON ở local.

## 2) Công nghệ

- **Ngôn ngữ:** Java 17
- **Giao diện:** JavaFX 21 (`javafx-controls`, `javafx-fxml`)
- **Xử lý dữ liệu:** Gson 2.10.1
- **Công cụ build:** Maven

## 3) Cấu trúc dự án

```text
src/main/java/com/focussystem/
|- App.java, Launcher.java      # Điểm bắt đầu của ứng dụng (Entry point)
|- model/                       # Dữ liệu (Task, Subject, Config)
|- view/                        # Giao diện JavaFX View
|- controller/                  # Điều hướng sự kiện và xử lý nghiệp vụ
|- service/                     # DataManager đảm nhiệm đọc/ghi JSON
|- util/                        # AlertHelper và các lớp tiện ích
|- component/                   # Các Custom JavaFX component

src/main/resources/
|- styles.css                   # Định dạng giao diện CSS

Dữ liệu local:
|- tasks.json
|- subjects.json
|- config.json
```

## 4) Yêu cầu môi trường
- JDK 17+
- Maven 3.8+
- Hệ điều hành: Windows, macOS, Linux

Kiểm tra nhanh môi trường của bạn:
```bash
java -version
mvn -v
```

## 5) Cách chạy dự án

**Cách 1: Chạy trực tiếp từ source code**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=com.focussystem.Launcher
```
(Nếu máy chưa cài plugin exec, bạn có thể đóng gói rồi chạy file JAR theo Cách 2).

**Cách 2: Đóng gói và chạy file JAR**
```bash
mvn clean package
java -jar target/focus-system-1.0-SNAPSHOT.jar
```

## 6) Dữ liệu và lưu trữ
- `tasks.json`: Lưu trữ danh sách task.
- `subjects.json`: Lưu trữ danh sách môn học.
- `config.json`: Lưu trữ các cấu hình đơn giản (ví dụ: học kỳ được chọn gần nhất).

**Lưu ý:**
- Ứng dụng sẽ tự động lưu lại mỗi khi có thao tác thay đổi dữ liệu.
- Bạn có thể thực hiện Import/Export task từ file JSON ngay trong giao diện ứng dụng.

## 7) Chức năng chính
**Quản lý task:**

- Thêm task mới (Tiêu đề, Môn học, Ngày bắt đầu, Hạn chót, Deadline).
- Xóa một task bất kỳ hoặc xóa toàn bộ.
- Đánh dấu trạng thái hoàn thành trực quan bằng checkbox.

**Bộ lọc và tìm kiếm:**
- Lọc task theo trạng thái.
- Sắp xếp theo trình tự thời gian.
- Tìm kiếm nhanh bằng từ khóa.

**Trải nghiệm người dùng (UX):**
- Click ra vùng trống để bỏ chọn dòng đang highlight trong bảng.
- Nút Refresh để tải lại dữ liệu mới nhất từ file JSON.

## 8) Build Artifact và các file cần bỏ qua
Không commit các thư mục/file sinh ra trong quá trình build (đã được thiết lập trong `.gitignore`):
- Thư mục `target/`
- Thư mục `dist/`
- Các file log tạm thời (build.log, compile_log.txt, ...)
- Các file data local (`tasks.json`, `config.json`, v.v.)

## 9) Xử lý sự cố (Troubleshooting)
**Lỗi không mở được JavaFX:**
Đảm bảo bạn đang dùng JDK 17+ và các dependency JavaFX đã được Maven resolve đầy đủ.

**Lỗi hiển thị font/ký tự:**
Đảm bảo IDE và Terminal của bạn đang sử dụng encoding UTF-8.

**Lỗi nạp dữ liệu JSON:**
Kiểm tra lại định dạng file JSON xem có hợp lệ (valid) không trước khi tiến hành import.

## 10) Định hướng phát triển tương lai
- Bổ sung Unit Test cho layer Service và Controller.
- Tách các Business Rule (quy tắc nghiệp vụ) phức tạp sang một layer riêng biệt để dễ quản lý.
- Thêm tính năng đồng bộ hóa dữ liệu lên Cloud (tùy chọn).
