# CHỈ DẪN CỐT LÕI CHO DỰ ÁN FOCUS-SYSTEM

## 1. NGUYÊN TẮC HOẠT ĐỘNG CỦA AGENT (ROUTER)

- **Cơ chế Router:** Trước khi thực hiện bất kỳ thay đổi nào, hãy kiểm tra thư mục `.agentskills/` để tìm các hướng dẫn chuyên biệt. Nếu nhiệm vụ trùng với tên thư mục, bạn phải đọc file `SKILL.md` bên trong đó trước khi code.
- **Xác nhận ngữ cảnh:** Luôn sử dụng kỹ năng trong `.agentskills/update-context/SKILL.md` để đảm bảo bạn nắm vững trạng thái hiện tại của dự án `focus-system`.
- **An toàn dữ liệu:** Tuyệt đối không xóa file mà không liệt kê danh sách và lý do để người dùng phê duyệt.

## 2. KỸ NĂNG & NGUYÊN TẮC CỐT LÕI (ENGINEERING BEST PRACTICES)

- **Đọc trước khi sửa (Read before editing):** Luôn phân tích ngữ cảnh của toàn bộ file hoặc kiến trúc liên quan trước khi đề xuất code. Tuyệt đối không đoán mò.
- **Lập kế hoạch trước (Think step-by-step):** Trước khi đưa ra một đoạn code lớn hoặc tái cấu trúc, hãy viết ra một kế hoạch ngắn gọn bằng gạch đầu dòng về những gì định làm để duyệt trước.
- **Không "Over-engineer":** Chỉ giải quyết đúng vấn đề được yêu cầu. Không tự ý thêm tính năng thừa. Bản vá lỗi (bug fix) không đi kèm dọn dẹp code xung quanh trừ khi được yêu cầu.
- **Bình luận có chủ đích (Intentional Commenting):** Chỉ comment để giải thích chữ "TẠI SAO" (WHY): ràng buộc ẩn, cách khắc phục tạm thời, hoặc logic phức tạp. Không giải thích những code hiển nhiên.
- **Security First:** Dùng Parameterized Queries cho DB (chống SQL Injection). Không hardcode API keys/passwords (dùng biến môi trường). Validate mọi input đầu vào.
- **Root-cause analysis:** Khi xử lý lỗi, phân tích log/stack trace để giải thích rõ nguyên nhân gốc rễ trước khi đưa ra cách sửa. Dùng try/catch đầy đủ.
- **Trình bày Code:** Cung cấp code hoàn chỉnh, không dùng `// ... existing code ...` trừ khi file trên 500 dòng. Tuân thủ nguyên tắc DRY.

## 3. QUY ĐỊNH KỸ THUẬT CHUYÊN BIỆT (DOMAIN SKILLS)

Dựa trên kho tàng kỹ năng hiện có, bạn phải tuân thủ:

- **Phát triển Tính năng:** Tuân thủ `.agentskills/generate-mvc-feature/SKILL.md` (MVC) và `.agentskills/java17-refactoring/SKILL.md` (Java 17).
- **Xử lý Dữ liệu:** Đọc/ghi JSON qua `.agentskills/gson-data-handler/SKILL.md`.
- **Giao diện (JavaFX):** Áp dụng CSS tại `.agentskills/javafx-css-guidelines/SKILL.md` và UI Design tại `.agentskills/javafx-ui-styling/SKILL.md`. Tuyệt đối không dùng inline-style.
- **Quản lý Dự án:** Cấu hình thư viện qua `.agentskills/maven-build-helper/SKILL.md`.

## 4. QUY TRÌNH PHẢN HỒI MẶC ĐỊNH

1. Phân tích yêu cầu và thông báo các file `SKILL.md` bạn đang áp dụng.
2. Lập kế hoạch thực hiện (Chain-of-Thought).
3. Chờ xác nhận (nếu tác vụ lớn) hoặc tiến hành triển khai code.
