# BUSINESS_RULES.md — Jira Mini

---

## Rules cứng — KHÔNG được bỏ qua

### Auth
- Password PHẢI hash bằng BCrypt trước khi lưu, KHÔNG lưu plain text
- Access token hết hạn sau 15 phút
- Refresh token hết hạn sau 7 ngày
- Refresh token phải được lưu DB để có thể revoke (logout)

## Các module chính

| Module | Chức năng |
|---|---|
| **Authentication** | Đăng ký, đăng nhập, refresh token, bảo mật JWT |
| **Project** | Tạo, cập nhật project; quản lý thành viên theo role |
| **Issue / Task** | Tạo, cập nhật, assign issue; theo dõi trạng thái |
| **Audit** | Ghi lại lịch sử thay đổi toàn hệ thống |
 
---

## Quy tắc phân quyền

| Hành động | OWNER | MANAGER | MEMBER |
|---|:---:|:---:|:---:|
| Tạo project | ✅ | — | — |
| Cập nhật project | ✅ | ❌ | ❌ |
| Thêm thành viên | ✅ | ✅ | ❌ |
| Tạo / cập nhật issue | ✅ | ✅ | ✅ |
| Xem issue trong project | ✅ | ✅ | ✅ |

### User / Member
- Chỉ ADMIN mới xem được danh sách tất cả users
- User không thể tự thay đổi role của mình
- Không hard delete user — dùng soft delete (deletedAt)

---

## Validate input (áp dụng toàn bộ)
- Email: format hợp lệ, max 255 ký tự
- Password: min 8 ký tự, có ít nhất 1 chữ số
- Tên (fullName, project name): không rỗng, max 100 ký tự
- Description: max 2000 ký tự
- Tất cả validate bằng `@Valid` + Bean Validation annotation trên DTO

---

## Security
- KHÔNG trả về password trong bất kỳ response nào
- User chỉ thấy project mà họ là member

---