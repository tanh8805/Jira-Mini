# Task-Module.md — Jira Mini

---

## Tổng quan

Module **Task** cho phép thành viên project tạo, lọc, cập nhật và xoá task.
Mọi thao tác đều yêu cầu người dùng là **thành viên của project** tương ứng.
Mọi thao tác ghi (create / update / delete) đều tự động ghi vào `audit_logs`.

---

## API Endpoints

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/projects/{projectId}/tasks` | Lấy danh sách task (có filter + pagination) | ✅ Member |
| POST | `/api/projects/{projectId}/tasks` | Tạo task mới | ✅ Member |
| PUT | `/api/projects/{projectId}/tasks/{taskId}` | Cập nhật task | ✅ Member |
| DELETE | `/api/projects/{projectId}/tasks/{taskId}` | Xoá task | ✅ Member |

---

## Chi tiết từng endpoint

### GET `/api/projects/{projectId}/tasks`

**Query params (tất cả optional):**

| Param | Type | Mô tả |
|---|---|---|
| `status` | `TODO` \| `IN_PROGRESS` \| `DONE` | Filter theo trạng thái |
| `priority` | `LOW` \| `MEDIUM` \| `HIGH` | Filter theo độ ưu tiên |
| `assigneeId` | UUID | Filter theo người được assign |
| `page` | int (default 0) | Số trang |
| `size` | int (default 20) | Số phần tử/trang |
| `sort` | string (default `createdAt`) | Trường sắp xếp |

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
- `assigneeId` phải là member của project nếu truyền lên
- Sau khi lưu thành công → ghi `AuditLog` với `action = CREATED`

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
- Snapshot `oldValue` được lấy **trước** khi các field bị ghi đè
- Sau khi lưu thành công → ghi `AuditLog` với `action = UPDATED`

**Response `200 OK`:** TaskResponse object

---

### DELETE `/api/projects/{projectId}/tasks/{taskId}`

- Task phải thuộc đúng `projectId` trong URL
- Snapshot `oldValue` được lấy **trước** khi delete
- Sau khi delete thành công → ghi `AuditLog` với `action = DELETED`
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
└── TaskRepository.java
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
| Cập nhật task | ✅ | ✅ | ✅ |
| Xoá task | ✅ | ✅ | ✅ |

> Mọi thao tác yêu cầu người dùng phải là **member** của project.
> User không phải member sẽ nhận `403 Forbidden`.

---

## Tích hợp Audit

`TaskService` inject `AuditLogRepository` trực tiếp. Hai helper nội bộ xử lý việc ghi log:

```
toSnapshot(task)
→ Serialize 4 field: title, status, priority, assigneeId thành JSON string
→ Dùng String.format (không dùng ObjectMapper)

saveAuditLog(action, entityId, oldValue, newValue, actor)
→ Tạo AuditLog entity và lưu vào DB
```

**Thứ tự gọi trong từng method:**

| Method | Thứ tự |
|---|---|
| `createTask` | save task → toSnapshot(saved) → saveAuditLog(CREATED, null, snapshot) |
| `updateTask` | toSnapshot(task) trước → set fields → save → saveAuditLog(UPDATED, old, new) |
| `deleteTask` | toSnapshot(task) trước → delete → saveAuditLog(DELETED, snapshot, null) |

---

## Error Responses

| HTTP | Trường hợp |
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

```sql
tasks
├── id          UUID, PK
├── project_id  FK → projects  (ON DELETE CASCADE)
├── assignee_id FK → users     (nullable, ON DELETE SET NULL)
├── created_by  FK → users     (ON DELETE RESTRICT)
├── title       VARCHAR(200), NOT NULL
├── description TEXT
├── status      VARCHAR(20)  DEFAULT 'TODO'
├── priority    VARCHAR(20)  DEFAULT 'MEDIUM'
├── due_date    DATE
├── created_at  TIMESTAMPTZ  DEFAULT NOW()
└── updated_at  TIMESTAMPTZ  DEFAULT NOW()
```

**Index:**
```sql
CREATE INDEX idx_tasks_project_id  ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_status      ON tasks(status);
```

---

## Ghi chú kỹ thuật

- **Filter động**: `TaskRepository` dùng JPQL với điều kiện `IS NULL OR field = :param`
- **Pagination**: Dùng Spring Data `Pageable` — client truyền `?page=0&size=20&sort=createdAt,desc`
- **Assignee validation**: Kiểm tra trong `TaskService` qua `ProjectMemberRepository`
- **Project isolation**: `assertTaskBelongsToProject()` ngăn update/delete task của project khác
- **Audit inject**: `AuditLogRepository` inject thẳng vào `TaskService`, không tách service riêng