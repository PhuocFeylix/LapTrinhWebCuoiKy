# Logistic UTEExpress

Website quản lý chuỗi giao nhận Logistic UTEExpress — đồ án môn Lập trình Web.

## Thông tin về lịch sử Git

Trong quá trình phát triển, dự án từng có hai nhánh với lịch sử commit độc lập:

- `main`: chứa các commit cập nhật giao diện và một số chức năng, ví dụ cart/product và kết nối API.
- `Duy_CompletedProject`: chứa phiên bản dự án hoàn thiện tại commit `25e879d` (`Project hoàn thiện`).

Hai nhánh ban đầu không có commit tổ tiên chung. Điều này thường xảy ra khi một nhánh được tạo trong một repository Git khác, hoặc repository được khởi tạo lại; vì vậy Git không thể tự xem chúng là hai nhánh phát triển từ cùng một lịch sử.

Để thống nhất nhánh chính, `main` đã được chuyển sang commit của `Duy_CompletedProject`. Vì thế, trang lịch sử của `main` hiện hiển thị lịch sử của phiên bản hoàn thiện, thay vì toàn bộ chuỗi commit cũ của `main`.

**Lịch sử cũ không bị xóa:** nhánh `backup-main-20261026` giữ lại commit `050562c` và lịch sử `main` trước khi chuyển nhánh. Nhánh `backup-duy-current` cũng được giữ làm bản dự phòng của phiên bản hoàn thiện.

## Các nhánh liên quan

| Nhánh | Mục đích |
| --- | --- |
| `main` | Nhánh chính, hiện trỏ đến phiên bản hoàn thiện |
| `Duy_CompletedProject` | Giữ nhánh dự án hoàn thiện ban đầu |
| `backup-main-20261026` | Giữ lịch sử `main` cũ |
| `backup-duy-current` | Bản dự phòng của phiên bản hoàn thiện |
| `master` | Nhánh cũ khác, được giữ nguyên |

## Về việc phân chia commit

Commit phản ánh các lần ghi nhận thay đổi vào Git, không nhất thiết tương ứng một-một với từng thành viên hoặc từng chức năng. Do lịch sử hai nhánh được tạo độc lập và sau đó nhánh chính được chuyển sang phiên bản hoàn thiện, không nên dựa riêng vào biểu đồ commit hiện tại để kết luận ai đã làm từng phần.

Để trình bày đóng góp chính xác, nên đối chiếu nội dung thay đổi, lịch sử commit ở các nhánh backup và phân công thực tế của nhóm. Không nên gán tác giả hoặc công việc cho thành viên nếu chưa có bằng chứng trong lịch sử Git.

## Lưu ý

README này giải thích vì sao lịch sử hiển thị trên `main` tập trung vào phiên bản hoàn thiện và chỉ ra nơi giữ lịch sử cũ. README **không tự gộp** hai lịch sử commit thành một đồ thị liên tục. Nếu cần biểu đồ commit của cả hai lịch sử trong cùng một nhánh, cần tạo merge commit có sử dụng `--allow-unrelated-histories` và kiểm tra kỹ nội dung sau khi merge.
