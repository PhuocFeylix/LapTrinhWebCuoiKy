# Logistic UTEExpress

Website quản lý chuỗi giao nhận Logistic UTEExpress — đồ án môn Lập trình Web.

## Phân chia nhánh theo thành viên

Nhóm sử dụng các nhánh để phân biệt phần làm việc của từng thành viên và phiên bản cuối cùng:

| Nhánh | Vai trò | Ý nghĩa |
| --- | --- | --- |
| `branch-cua-Phuoc` | Phần của Huỳnh Duy Phước | Lưu phần code và các commit Phước thực hiện trong quá trình phát triển |
| `master` | Phần của Nguyễn Minh Duy | Lưu phần code và các commit Duy thực hiện |
| `main` | Bản cuối cùng của nhóm | Nhánh chính dùng để xem phiên bản dự án hoàn thiện sau khi tổng hợp |

Nhánh `Duy_CompletedProject` giữ phiên bản hoàn thiện do Duy cung cấp; `backup-duy-current` là nhánh dự phòng cùng trỏ đến phiên bản đó.

## Vì sao lịch sử commit trông như chỉ có một lịch sử?

Các nhánh `branch-cua-Phuoc` và `Duy_CompletedProject` ban đầu có lịch sử Git độc lập, tức là không có commit tổ tiên chung. Khi `main` được chuyển sang phiên bản hoàn thiện của Duy, lịch sử hiển thị trên `main` chủ yếu là lịch sử của phiên bản đó. Điều này không có nghĩa là phần Phước làm đã biến mất; phần đó vẫn được giữ trong nhánh `branch-cua-Phuoc`.

Tóm lại:
- **Xem phần Phước làm:** mở nhánh `branch-cua-Phuoc`.
- **Xem phần Duy làm:** mở nhánh `master` (hoặc nhánh phiên bản hoàn thiện `Duy_CompletedProject`).
- **Xem bản cuối cùng của nhóm:** mở nhánh `main`.

GitHub hiển thị lịch sử commit theo từng nhánh. Nếu hai nhánh không có tổ tiên chung, chúng có thể xuất hiện như hai lịch sử riêng biệt thay vì một đường lịch sử liên tục. Việc `main` là bản cuối không tự động gộp toàn bộ commit của các nhánh thành một lịch sử chung.

## Lưu ý khi trình bày báo cáo

Tên nhánh thể hiện cách nhóm tổ chức công việc, còn commit thể hiện các lần ghi nhận thay đổi trong Git. Để giải thích chi tiết từng chức năng do ai thực hiện, nên đối chiếu nội dung code và các commit thực tế trên `branch-cua-Phuoc` và `master`; không nên suy ra toàn bộ phân công chỉ từ tên nhánh.
