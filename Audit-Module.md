# Audit-Module.md — Jira Mini

---

## Tổng quan

Module **Audit** ghi lại lịch sử thay đổi của **Task** trong hệ thống.
Mọi thao tác tạo, cập nhật, xoá task đều được ghi lại kèm actor, thời gian và snapshot giá trị thay đổi.

---

## API Endpoints

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/audit-logs?entityType=TASK&entityId=...` | Xem lịch sử thay đổi của task | ✅ Member |

---

## Chi tiết endpoint

### GET `/api/audit-logs`

**Query params:**

| Param | Type | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `entityType` | `TASK` | ✅ | Loại entity — hiện chỉ hỗ trợ `TASK` |
| `entityId` | UUID | ✅ | ID của task cần xem lịch sử |
| `page` | int (default 0) | ❌ | Số trang |
| `size` | int (default 20) | ❌ | Số phần tử/trang |

**Phân quyền:** Caller phải là **member của project** chứa task đó.

**Luồng xử lý trong `AuditLogService`:**
```
Validate entityType == "TASK"  →  400 nếu không hợp lệ
    ↓
Tìm Task theo entityId         →  404 nếu không tồn tại
    ↓
Lấy project từ task
    ↓
Kiểm tra caller là member      →  403 nếu không phải member
    ↓
Query AuditLogRepository (entityType, entityId, pageable)
    ↓
Map → Page<AuditLogResponse>
```

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "uuid",
      "action": "UPDATED",
      "actor": {
        "id": "uuid",
        "fullName": "Nguyen Van A"
      },
      "createdAt": "2025-01-01T10:00:00",
      "oldValue": "{\"title\":\"Fix bug\",\"status\":\"TODO\",\"priority\":\"HIGH\",\"assigneeId\":null}",
      "newValue": "{\"title\":\"Fix bug\",\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\",\"assigneeId\":\"uuid\"}"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

> `actor` có thể là `null` nếu user đã bị xoá khỏi hệ thống (do `ON DELETE SET NULL`).

---

## Snapshot JSON

Chỉ ghi 4 field quan trọng của task:

```json
{
  "title": "Fix login bug",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assigneeId": "uuid-hoặc-null"
}
```

| Action | oldValue | newValue |
|---|:---:|:---:|
| `CREATED` | `null` | snapshot task vừa tạo |
| `UPDATED` | snapshot trước update | snapshot sau update |
| `DELETED` | snapshot trước delete | `null` |

---

## Cấu trúc file

```
controller/Audit/
└── AuditLogController.java

service/Audit/
└── AuditLogService.java

dto/Audit/
└── AuditLogResponse.java        ← gồm nested ActorDto

entity/
├── AuditLog.java
└── enums/AuditAction.java

repository/
└── AuditLogRepository.java
```

---

## Entity — `AuditLog`

| Field | Type | Map với DB |
|---|---|---|
| `id` | UUID | PK, auto-generate |
| `actor` | User | FK `actor_id`, LAZY, nullable |
| `entityType` | String | `entity_type` VARCHAR(50) |
| `entityId` | UUID | `entity_id` |
| `action` | AuditAction | `action` VARCHAR(20) |
| `oldValue` | String | `old_value` JSONB |
| `newValue` | String | `new_value` JSONB |
| `createdAt` | LocalDateTime | `created_at`, `@CreationTimestamp` |

> `oldValue` / `newValue` map kiểu `String` trong Java, annotate `@Column(columnDefinition = "jsonb")`.

---

## Enum — `AuditAction`

```java
// entity/enums/AuditAction.java
CREATED, UPDATED, DELETED
```

---

## Repository — `AuditLogRepository`

```java
Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
    String entityType, UUID entityId, Pageable pageable);
```

Kết quả trả về mặc định theo thứ tự `createdAt DESC` — log mới nhất lên đầu.

---

## Cách ghi Audit Log

Ghi trực tiếp trong `TaskService` — không dùng AOP. `AuditLogRepository` được inject thẳng vào `TaskService`.

### Helper trong `TaskService`

**`toSnapshot(Task task)`** — serialize 4 field thành JSON string:
```java
private String toSnapshot(Task task) {
    String assigneeId = task.getAssignee() != null
            ? "\"" + task.getAssignee().getId() + "\""
            : "null";
    return String.format(
            "{\"title\":\"%s\",\"status\":\"%s\",\"priority\":\"%s\",\"assigneeId\":%s}",
            task.getTitle(), task.getStatus(), task.getPriority(), assigneeId
    );
}
```

**`saveAuditLog(...)`** — tạo và lưu `AuditLog`:
```java
private void saveAuditLog(AuditAction action, UUID entityId,
                           String oldValue, String newValue, User actor) {
    auditLogRepository.save(AuditLog.builder()
            .entityType("TASK")
            .entityId(entityId)
            .action(action)
            .oldValue(oldValue)
            .newValue(newValue)
            .actor(actor)
            .build());
}
```

---

## Database

```sql
audit_logs
├── id           UUID, PK
├── actor_id     UUID, FK → users  (nullable, ON DELETE SET NULL)
├── entity_type  VARCHAR(50)  NOT NULL
├── entity_id    UUID         NOT NULL
├── action       VARCHAR(20)  NOT NULL
├── old_value    JSONB        nullable
├── new_value    JSONB        nullable
└── created_at   TIMESTAMPTZ  NOT NULL  DEFAULT NOW()
```

**Index:**
```sql
CREATE INDEX idx_audit_logs_entity     ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_id   ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
```

---

## Error Responses

| HTTP | Trường hợp |
|---|---|
| `400` | `entityType` không phải `TASK` |
| `401` | Chưa đăng nhập |
| `403` | Không phải member của project chứa task |
| `404` | Task không tồn tại |

**Format chuẩn:**
```json
{
  "status": 403,
  "message": "You are not a member of this project"
}
```

---

## Ghi chú thiết kế

| Quyết định | Lý do |
|---|---|
| Không dùng AOP | Cần kiểm soát thời điểm snapshot `oldValue` trước khi field bị ghi đè |
| `old_value`/`new_value` kiểu JSONB | Tận dụng schema sẵn có, hỗ trợ query theo field nếu cần mở rộng |
| Snapshot bỏ `description` | Field dài, ít giá trị audit |
| `entityType` hardcode `"TASK"` | Scope hiện tại chỉ audit task — mở rộng sau nếu cần |
| Audit log immutable | Không update, không delete record này |
| `actor` nullable | Khi user bị xoá khỏi hệ thống — `ON DELETE SET NULL` |
| Inject `AuditLogRepository` vào `TaskService` | Tránh circular dependency, đơn giản hơn khi chỉ có 1 service ghi log |