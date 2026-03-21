# TASK-Module.md — Jira Mini

---

## Tổng quan

Module **Task (Issue)** cho phép thành viên project tạo, lọc, cập nhật và xoá task.
Mọi thao tác đều yêu cầu người dùng là **thành viên của project** tương ứng.

---

## API Endpoints

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/projects/{projectId}/tasks` | Lấy danh sách task (có filter + pagination) | ✅ Member |
| POST | `/api/projects/{projectId}/tasks` | Tạo task mới | ✅ Member |
| PUT | `/api/projects/{projectId}/tasks/{taskId}` | Cập nhật task (status / assign / fields) | ✅ Member |
| DELETE | `/api/projects/{projectId}/tasks/{taskId}` | Xoá task | ✅ Member |

---

## Chi tiết từng endpoint

### GET `/api/projects/{projectId}/tasks`

**Query params (tất cả optional):**

| Param | Type                                             | Mô tả                       |
|---|--------------------------------------------------|-----------------------------|
| `status` | `TODO` \| `IN_PROGRESS` \| `DONE` | Filter theo trạng thái |
| `priority` | `LOW` \| `MEDIUM` \| `HIGH`                      | Filter theo độ ưu tiên      |
| `assigneeId` | UUID                                             | Filter theo người được assign |
| `page` | int (default 0)                                  | Số trang                    |
| `size` | int (default 20)                                 | Số phần tử/trang            |
| `sort` | string (default `createdAt`)                     | Trường sắp xếp              |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Fix login bug",
      "description": "...",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "projectId": "uuid",
      "assigneeId": "uuid",
      "assigneeName": "Nguyen Van A",
      "createdAt": "2025-01-01T10:00:00"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

### POST `/api/projects/{projectId}/tasks`

**Request body:**
```json
{
  "title": "Fix login bug",
  "description": "Token không refresh đúng cách",
  "priority": "HIGH",
  "assigneeId": "uuid (optional)"
}
```

**Validate:**
- `title`: không rỗng, max 100 ký tự
- `description`: max 2000 ký tự
- `priority`: bắt buộc (`LOW` / `MEDIUM` / `HIGH`)
- `assigneeId`: nếu có, phải là member của project

**Business rules:**
- `status` luôn khởi tạo là `TODO` — client không được tự set lúc tạo
- `assigneeId` phải là member của project (nếu truyền lên)

**Response `201 Created`:** TaskResponse object

---

### PUT `/api/projects/{projectId}/tasks/{taskId}`

**Request body (tất cả fields đều optional — chỉ update field có giá trị):**
```json
{
  "title": "Fix login bug v2",
  "description": "Updated description",
  "status": "DONE",
  "priority": "MEDIUM",
  "assigneeId": "uuid"
}
```

**Business rules:**
- Task phải thuộc đúng `projectId` trong URL
- `assigneeId` nếu có phải là member của project
- Để unassign, dùng endpoint riêng hoặc gửi `assigneeId: null` cùng với sentinel flag

**Response `200 OK`:** TaskResponse object

---

### DELETE `/api/projects/{projectId}/tasks/{taskId}`

- Task phải thuộc đúng `projectId` trong URL
- Hard delete (không soft delete cho task)

**Response `204 No Content`**

---

## Cấu trúc file

```
controller/Task/
└── TaskController.java

service/Task/
└── TaskService.java

dto/Task/
├── CreateTaskRequest.java
├── UpdateTaskRequest.java
└── TaskResponse.java

entity/
├── Task.java
└── enums/
    ├── TaskStatus.java
    └── TaskPriority.java

repository/
└── TaskRepository.java          ← custom JPQL với filter động

exception/
├── TaskNotFoundException.java
├── TaskNotInProjectException.java
└── AssigneeNotMemberException.java
```

---

## Enums

### TaskStatus
| Value | Ý nghĩa |
|---|---|
| `TODO` | Chưa bắt đầu |
| `IN_PROGRESS` | Đang thực hiện |
| `DONE` | Hoàn thành |

### TaskPriority
| Value | Ý nghĩa |
|---|---|
| `LOW` | Ưu tiên thấp |
| `MEDIUM` | Ưu tiên trung bình |
| `HIGH` | Ưu tiên cao |

---

## Quy tắc phân quyền

| Hành động | OWNER | MANAGER | MEMBER |
|---|:---:|:---:|:---:|
| Xem danh sách task | ✅ | ✅ | ✅ |
| Tạo task | ✅ | ✅ | ✅ |
| Cập nhật task (status/assign) | ✅ | ✅ | ✅ |
| Xoá task | ✅ | ✅ | ✅ |

> Mọi thao tác yêu cầu người dùng phải là **member** của project.
> User không phải member sẽ nhận `403 Forbidden`.

---

## Error Responses

| HTTP Status | Trường hợp |
|---|---|
| `400` | Validation thất bại, assignee không phải member |
| `401` | Chưa đăng nhập / token hết hạn |
| `403` | Không phải member của project |
| `404` | Project / Task không tồn tại, task không thuộc project |

**Format chuẩn:**
```json
{
  "status": 404,
  "message": "Task not found"
}
```

---

## Database

Sử dụng lại bảng `issues` đã có trong schema:

```sql
issues
├── id          UUID, PK
├── project_id  FK → projects
├── assignee_id FK → users (nullable)
├── title       VARCHAR(100), NOT NULL
├── description VARCHAR(2000)
├── status      VARCHAR(20) — TODO | IN_PROGRESS | DONE
├── priority    VARCHAR(20) — LOW | MEDIUM | HIGH
└── created_at  TIMESTAMP
```

**Index được khuyến nghị thêm** để tăng hiệu năng filter:
```sql
CREATE INDEX idx_issues_project_status   ON issues(project_id, status);
CREATE INDEX idx_issues_project_priority ON issues(project_id, priority);
CREATE INDEX idx_issues_assignee         ON issues(assignee_id);
```

---

## Ghi chú kỹ thuật

- **Filter động**: `TaskRepository` dùng JPQL với điều kiện `IS NULL OR field = :param` để hỗ trợ filter tuỳ ý mà không cần viết nhiều method.
- **Pagination**: Dùng Spring Data `Pageable` — client truyền `?page=0&size=20&sort=createdAt,desc`.
- **Assignee validation**: Được kiểm tra ngay trong `TaskService` bằng cách query `ProjectMemberRepository`, không cần thêm join phức tạp.
- **Bảo mật project isolation**: `assertTaskBelongsToProject()` ngăn việc update/delete task của project khác dù biết taskId.