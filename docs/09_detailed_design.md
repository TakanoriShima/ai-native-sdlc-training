# 詳細設計書

## 1. 本ドキュメントの目的

本ドキュメントでは、`08_basic_design.md` で定義した基本設計をもとに、MVP を実装するために必要な詳細設計を定義する。

本書では主に以下を定義する。

- アプリケーション構成
- パッケージ構成
- Entity
- Enum
- Repository
- Service
- Controller
- DTO / Form
- URL
- バリデーション
- ステータス遷移
- 権限制御
- DB 制約
- 例外処理
- トランザクション
- テスト方針
- AI Coding Agent の実装境界

本書は Claude Code による実装、および Codex によるレビューの基準資料として利用する。

---

# 2. 実装基本方針

MVP は以下の構成で実装する。

    Browser
       ↓
    Controller
       ↓
    Service
       ↓
    Repository
       ↓
    PostgreSQL

View は Thymeleaf を利用する。

基本原則：

- Controller に業務ロジックを書かない
- Service に業務ルールを集約する
- Repository はデータアクセスに集中する
- Entity を直接フォーム入力として利用しない
- 権限制御は画面だけでなくサーバー側でも実施する
- 業務上重要な処理にはテストを作成する
- DB スキーマは Flyway で管理する

---

# 3. パッケージ構成

基本パッケージ名を以下とする。

    com.example.salesmanagement

初期構成：

    com.example.salesmanagement

    ├── auth
    │   ├── config
    │   └── service
    │
    ├── user
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── customer
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── project
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── quotation
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── work
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── delivery
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── invoice
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    ├── payment
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── repository
    │   └── service
    │
    └── common
        ├── exception
        └── web

実装時に不必要なパッケージ分割が発生する場合は簡略化してよい。

ただし、責務の分離は維持する。

---

# 4. 共通設計

## 4.1 ID

DB 内部 ID は `Long` を基本とする。

例：

    id BIGSERIAL PRIMARY KEY

業務番号である見積番号・請求番号は DB ID と分離する。

---

## 4.2 日時

日時：

    LocalDateTime

日付：

    LocalDate

を基本とする。

---

## 4.3 金額

金額は Java では、

    BigDecimal

を使用する。

`double` / `float` は使用しない。

DB では、

    NUMERIC(15, 2)

を基本候補とする。

---

## 4.4 createdAt / updatedAt

主要 Entity には、

- createdAt
- updatedAt

を保持する。

JPA Auditing または Entity Lifecycle Callback 等を利用できる。

具体的実装方式は Claude Code に裁量を与えるが、プロジェクト内で統一する。

---

# 5. Enum 設計

## 5.1 Role

    SALES
    SALES_MANAGER
    WORK_MANAGER
    WORKER
    ACCOUNTING
    ADMIN

Spring Security 上では、

    ROLE_SALES
    ROLE_SALES_MANAGER
    ROLE_WORK_MANAGER
    ROLE_WORKER
    ROLE_ACCOUNTING
    ROLE_ADMIN

として扱う。

---

## 5.2 ProjectStatus

    INQUIRY
    QUOTING
    QUOTE_APPROVAL_PENDING
    QUOTE_SUBMITTED
    ORDERED
    WORK_PREPARING
    WORKING
    DELIVERED
    INVOICED
    PAID
    COMPLETED
    ON_HOLD
    LOST
    CANCELLED

DB へ保存する場合は文字列形式を基本とする。

JPA では、

    @Enumerated(EnumType.STRING)

を利用する。

---

## 5.3 QuotationStatus

    DRAFT
    APPROVAL_PENDING
    APPROVED
    REJECTED
    SUBMITTED

---

## 5.4 ApprovalStatus

    PENDING
    APPROVED
    REJECTED

---

## 5.5 PaymentStatus

MVP では部分入金を扱わないため、以下の単純な状態とする。

    UNPAID
    PAID

将来、

    PARTIALLY_PAID

等を追加できる構成とする。

---

# 6. User Entity

主なフィールド：

| Field        | Type          | Constraint        |
| ------------ | ------------- | ----------------- |
| id           | Long          | PK                |
| name         | String        | NOT NULL          |
| email        | String        | NOT NULL / UNIQUE |
| passwordHash | String        | NOT NULL          |
| role         | Role          | NOT NULL          |
| enabled      | boolean       | NOT NULL          |
| createdAt    | LocalDateTime | NOT NULL          |
| updatedAt    | LocalDateTime | NOT NULL          |

---

# 7. Customer Entity

| Field     | Type          | Constraint |
| --------- | ------------- | ---------- |
| id        | Long          | PK         |
| name      | String        | NOT NULL   |
| address   | String        | NULL       |
| phone     | String        | NULL       |
| email     | String        | NULL       |
| notes     | String/Text   | NULL       |
| createdAt | LocalDateTime | NOT NULL   |
| updatedAt | LocalDateTime | NOT NULL   |

MVP では顧客担当者を別 Entity に分離しない。

必要になった場合は Phase 2 で、

    CustomerContact

を追加する。

---

# 8. Project Entity

| Field               | Type          | Constraint |
| ------------------- | ------------- | ---------- |
| id                  | Long          | PK         |
| name                | String        | NOT NULL   |
| customer            | Customer      | NOT NULL   |
| salesRepresentative | User          | NOT NULL   |
| description         | Text          | NULL       |
| amount              | BigDecimal    | NULL       |
| dueDate             | LocalDate     | NULL       |
| status              | ProjectStatus | NOT NULL   |
| createdAt           | LocalDateTime | NOT NULL   |
| updatedAt           | LocalDateTime | NOT NULL   |

初期ステータス：

    INQUIRY

とする。

---

# 9. Quotation Entity

| Field           | Type            | Constraint        |
| --------------- | --------------- | ----------------- |
| id              | Long            | PK                |
| project         | Project         | NOT NULL          |
| quotationNumber | String          | NOT NULL / UNIQUE |
| amount          | BigDecimal      | NOT NULL          |
| quotationDate   | LocalDate       | NOT NULL          |
| validUntil      | LocalDate       | NULL              |
| status          | QuotationStatus | NOT NULL          |
| createdBy       | User            | NOT NULL          |
| createdAt       | LocalDateTime   | NOT NULL          |
| updatedAt       | LocalDateTime   | NOT NULL          |

1 案件に複数の見積を登録可能とする。

---

# 10. QuotationApproval Entity

| Field           | Type           | Constraint |
| --------------- | -------------- | ---------- |
| id              | Long           | PK         |
| quotation       | Quotation      | NOT NULL   |
| applicant       | User           | NOT NULL   |
| approver        | User           | NULL       |
| status          | ApprovalStatus | NOT NULL   |
| rejectionReason | Text           | NULL       |
| requestedAt     | LocalDateTime  | NOT NULL   |
| decidedAt       | LocalDateTime  | NULL       |

承認履歴を保持できるよう、Quotation と 1:N とする。

---

# 11. WorkHandoff Entity

| Field           | Type          | Constraint        |
| --------------- | ------------- | ----------------- |
| id              | Long          | PK                |
| project         | Project       | NOT NULL / UNIQUE |
| workDescription | Text          | NOT NULL          |
| dueDate         | LocalDate     | NOT NULL          |
| specialNotes    | Text          | NULL              |
| handedOffBy     | User          | NOT NULL          |
| handedOffAt     | LocalDateTime | NOT NULL          |

MVP では 1 案件につき 1 つの最新引き継ぎ情報を基本とする。

---

# 12. WorkAssignment Entity

| Field      | Type          | Constraint |
| ---------- | ------------- | ---------- |
| id         | Long          | PK         |
| project    | Project       | NOT NULL   |
| worker     | User          | NOT NULL   |
| assignedBy | User          | NOT NULL   |
| assignedAt | LocalDateTime | NOT NULL   |

MVP では 1 案件 1 作業担当者を基本とする。

DB モデル上は将来の複数担当者対応を考慮できる構成としてもよい。

---

# 13. Delivery Entity

| Field        | Type          | Constraint        |
| ------------ | ------------- | ----------------- |
| id           | Long          | PK                |
| project      | Project       | NOT NULL / UNIQUE |
| deliveryDate | LocalDate     | NOT NULL          |
| description  | Text          | NOT NULL          |
| notes        | Text          | NULL              |
| registeredBy | User          | NOT NULL          |
| createdAt    | LocalDateTime | NOT NULL          |

MVP では 1 案件 1 納品を基本とする。

---

# 14. Invoice Entity

| Field          | Type          | Constraint        |
| -------------- | ------------- | ----------------- |
| id             | Long          | PK                |
| project        | Project       | NOT NULL          |
| invoiceNumber  | String        | NOT NULL / UNIQUE |
| invoiceDate    | LocalDate     | NOT NULL          |
| amount         | BigDecimal    | NOT NULL          |
| paymentDueDate | LocalDate     | NOT NULL          |
| createdAt      | LocalDateTime | NOT NULL          |
| updatedAt      | LocalDateTime | NOT NULL          |

DB モデルは 1 Project : N Invoice を許容する。

MVP の UI では 1 案件 1 請求を基本とする。

---

# 15. Payment Entity

| Field        | Type          | Constraint |
| ------------ | ------------- | ---------- |
| id           | Long          | PK         |
| invoice      | Invoice       | NOT NULL   |
| paymentDate  | LocalDate     | NOT NULL   |
| amount       | BigDecimal    | NOT NULL   |
| status       | PaymentStatus | NOT NULL   |
| registeredBy | User          | NOT NULL   |
| createdAt    | LocalDateTime | NOT NULL   |

MVP では 1 請求 1 入金を基本とする。

---

# 16. Repository 設計

Spring Data JPA を利用する。

基本 Repository：

    UserRepository
    CustomerRepository
    ProjectRepository
    QuotationRepository
    QuotationApprovalRepository
    WorkHandoffRepository
    WorkAssignmentRepository
    DeliveryRepository
    InvoiceRepository
    PaymentRepository

すべて、

    JpaRepository<Entity, Long>

を基本とする。

---

# 17. Repository 主要検索

## UserRepository

    Optional<User> findByEmail(String email)

## CustomerRepository

顧客名検索を提供する。

例：

    findByNameContainingIgnoreCase(...)

## ProjectRepository

以下の検索を提供する。

- 案件名
- 顧客
- ステータス
- 営業担当者

必要に応じて Pageable を利用する。

## QuotationRepository

    List<Quotation> findByProjectId(...)

## InvoiceRepository

納品済み・未請求案件確認については、Project / Delivery / Invoice の責務を考慮し、Service から適切な Query を利用する。

複雑な Query を Controller に記述しない。

---

# 18. Service 一覧

主要 Service：

    UserService
    CustomerService
    ProjectService
    QuotationService
    QuotationApprovalService
    WorkService
    DeliveryService
    InvoiceService
    PaymentService

---

# 19. UserService

主な責務：

- ユーザー登録
- ユーザー編集
- ユーザー無効化
- ユーザー取得
- メールアドレス重複確認
- パスワードハッシュ化

パスワードハッシュ化には `PasswordEncoder` を利用する。

---

# 20. CustomerService

主な責務：

- 顧客一覧取得
- 顧客検索
- 顧客詳細取得
- 顧客登録
- 顧客更新

存在しない顧客 ID を指定した場合、

    ResourceNotFoundException

等の共通例外を使用する。

---

# 21. ProjectService

主な責務：

- 案件一覧
- 案件検索
- 案件詳細
- 案件登録
- 案件編集
- ステータス変更
- 営業担当者設定

特に、

    changeStatus()

は業務ルールを Service 層で検証する。

---

# 22. ProjectStatusTransition

ステータス遷移ロジックを Controller へ直接記述しない。

以下のいずれかの方式を採用する。

- ProjectService 内で管理
- ProjectStatusTransitionService を作成
- ProjectStatus enum に遷移判定ロジックを持たせる

Claude Code は最も単純で保守しやすい方法を提案し、人間が確認して採用する。

---

# 23. 通常ステータス遷移表

基本的な許可遷移：

| Current                | Next                   |
| ---------------------- | ---------------------- |
| INQUIRY                | QUOTING                |
| QUOTING                | QUOTE_APPROVAL_PENDING |
| QUOTING                | QUOTE_SUBMITTED        |
| QUOTE_APPROVAL_PENDING | QUOTING                |
| QUOTE_APPROVAL_PENDING | QUOTE_SUBMITTED        |
| QUOTE_SUBMITTED        | ORDERED                |
| QUOTE_SUBMITTED        | LOST                   |
| ORDERED                | WORK_PREPARING         |
| WORK_PREPARING         | WORKING                |
| WORKING                | DELIVERED              |
| DELIVERED              | INVOICED               |
| INVOICED               | PAID                   |
| PAID                   | COMPLETED              |

必要に応じて、

    ON_HOLD
    CANCELLED

への遷移を許可する。

例外状態から通常フローへ戻すルールは実装前に決定する。

---

# 24. 不正ステータス遷移

許可されていない状態変更の場合、

    InvalidStatusTransitionException

を発生させる。

例：

    QUOTING
        ↓
    PAID

は拒否する。

---

# 25. QuotationService

主な責務：

- 見積登録
- 見積編集
- 見積取得
- 案件別見積一覧
- 見積番号生成
- 見積状態管理

---

# 26. 見積番号

MVP の基本形式を以下とする。

    Q-YYYY-NNNN

例：

    Q-2026-0001

ただし並行処理時の重複を防止する必要がある。

単純な、

    count() + 1

による採番は使用しない。

具体的な採番方式は実装時に DB 制約を含めて設計する。

---

# 27. 見積承認条件

基本ルール：

    amount > 1,000,000

の場合、承認を必要とする。

    amount <= 1,000,000

の場合、MVP では承認なしで提出可能とする（1,000,000 円ちょうどを含む）。

緊急時における承認スキップ機能は MVP では設けない。

このルールは、

    QuotationApprovalService

等の業務層に集約する。

Controller や View にのみ記述しない。

---

# 28. QuotationApprovalService

主な責務：

- 承認申請
- 承認
- 差し戻し
- 承認状態確認

承認可能ロール：

    SALES_MANAGER
    ADMIN

---

# 29. 承認申請

承認申請時：

1. 見積が存在することを確認
2. 見積が申請可能状態であることを確認
3. QuotationApproval を作成
4. status = PENDING
5. Quotation.status = APPROVAL_PENDING
6. Project.status = QUOTE_APPROVAL_PENDING

一連の処理をトランザクション内で行う。

---

# 30. 見積承認

承認時：

1. 承認待ちデータを取得
2. 操作者の権限確認
3. ApprovalStatus = APPROVED
4. approver を記録
5. decidedAt を記録
6. Quotation.status = APPROVED

見積提出処理時に、

    Project.status = QUOTE_SUBMITTED

へ変更する。

---

# 31. 見積差し戻し

差し戻し時：

1. ApprovalStatus = REJECTED
2. rejectionReason を必須とする
3. approver を記録
4. decidedAt を記録
5. Quotation.status = REJECTED
6. Project.status = QUOTING

---

# 32. 承認済み見積変更

承認済み見積の金額を変更した場合は、承認状態を無効化し、再承認を必要とする。

MVP では少なくとも、

    amount

変更時を対象とする。

---

# 33. WorkService

主な責務：

- 引き継ぎ登録
- 引き継ぎ取得
- 作業担当設定
- 担当案件取得
- 作業開始

---

# 34. 作業引き継ぎ

引き継ぎ登録可能条件：

    Project.status == ORDERED

引き継ぎ完了後：

    Project.status = WORK_PREPARING

へ変更する。

---

# 35. 作業担当設定

設定可能ロール：

    WORK_MANAGER
    ADMIN

設定対象ユーザーは、

    WORKER

ロールを基本とする。

---

# 36. 作業開始

作業開始可能条件：

- WorkHandoff が存在する
- WorkAssignment が存在する
- Project.status == WORK_PREPARING

開始後：

    Project.status = WORKING

---

# 37. DeliveryService

主な責務：

- 納品登録
- 納品情報取得

納品登録可能条件：

    Project.status == WORKING

登録後：

    Project.status = DELIVERED

とする。

Delivery 登録と Project 更新は同一トランザクションで実行する。

---

# 38. InvoiceService

主な責務：

- 請求対象案件取得
- 請求登録
- 請求取得
- 請求番号生成

---

# 39. 請求対象

基本条件：

    Project.status == DELIVERED

かつ、

    対象案件に請求が存在しない

MVP では 1 案件 1 請求を基本とするため、既存請求がある場合は重複登録を拒否する。

---

# 40. 請求登録

登録時：

1. Project が DELIVERED であること
2. 既存 Invoice が存在しないこと
3. 請求金額が 0 以上であること
4. 支払期限が請求日以降であること
5. Invoice を保存
6. Project.status = INVOICED

同一トランザクションで処理する。

---

# 41. 請求番号

MVP の基本形式：

    INV-YYYY-NNNN

例：

    INV-2026-0001

見積番号と同様、並行処理による重複を防止する。

---

# 42. PaymentService

主な責務：

- 入金登録
- 入金情報取得
- 未入金案件取得

---

# 43. 入金登録

登録可能条件：

- Invoice が存在する
- Project.status == INVOICED
- 入金金額が 0 以上

MVP では部分入金を扱わないため、

    payment.amount == invoice.amount

を基本条件とする。

登録後：

    Payment.status = PAID
    Project.status = PAID

とする。

---

# 44. 案件完了

案件完了可能条件：

- Project.status == PAID
- 必要な Delivery が存在する
- Invoice が存在する
- Payment が PAID

条件を満たす場合、

    Project.status = COMPLETED

へ変更できる。

---

# 45. Controller 一覧

主要 Controller：

    LoginController
    DashboardController
    UserController
    CustomerController
    ProjectController
    QuotationController
    QuotationApprovalController
    WorkController
    DeliveryController
    InvoiceController
    PaymentController

Spring Security の標準ログインを利用する場合、LoginController を不要とすることもできる。

---

# 46. URL 設計

MVP の基本 URL を以下とする。

## Authentication

    GET  /login
    POST /login
    POST /logout

## Dashboard

    GET /dashboard

## Users

    GET  /admin/users
    GET  /admin/users/new
    POST /admin/users
    GET  /admin/users/{id}/edit
    POST /admin/users/{id}

## Customers

    GET  /customers
    GET  /customers/new
    POST /customers
    GET  /customers/{id}
    GET  /customers/{id}/edit
    POST /customers/{id}

## Projects

    GET  /projects
    GET  /projects/new
    POST /projects
    GET  /projects/{id}
    GET  /projects/{id}/edit
    POST /projects/{id}

## Quotations

    GET  /projects/{projectId}/quotations/new
    POST /projects/{projectId}/quotations
    GET  /quotations/{id}
    GET  /quotations/{id}/edit
    POST /quotations/{id}

## Approval

    POST /quotations/{id}/approval-request
    POST /quotations/{id}/approve
    POST /quotations/{id}/reject

## Work

    GET  /projects/{id}/handoff
    POST /projects/{id}/handoff

    GET  /projects/{id}/assignment
    POST /projects/{id}/assignment

    POST /projects/{id}/work/start

## Delivery

    GET  /projects/{id}/delivery
    POST /projects/{id}/delivery

## Invoice

    GET  /projects/{id}/invoice
    POST /projects/{id}/invoice

## Payment

    GET  /invoices/{id}/payment
    POST /invoices/{id}/payment

## Project completion

    POST /projects/{id}/complete

---

# 47. HTTP Method 方針

Thymeleaf の HTML Form を中心とするため、MVP では、

    GET
    POST

を基本とする。

REST API の構築は MVP の必須要件としない。

将来 SPA や外部システム連携が必要になった場合に REST API を追加する。

---

# 48. DTO / Form 方針

Entity を直接 HTML Form へバインドしない。

例：

    CustomerForm
    ProjectForm
    QuotationForm
    ApprovalRejectForm
    WorkHandoffForm
    WorkAssignmentForm
    DeliveryForm
    InvoiceForm
    PaymentForm
    UserForm

入力用 Form と表示用 DTO は必要に応じて分離する。

過度な DTO 増加は避ける。

---

# 49. CustomerForm

主な項目：

    name
    address
    phone
    email
    notes

Validation：

    name
        @NotBlank

    email
        @Email

---

# 50. ProjectForm

主な項目：

    name
    customerId
    salesRepresentativeId
    description
    amount
    dueDate

Validation：

    name
        @NotBlank

    customerId
        @NotNull

    salesRepresentativeId
        @NotNull

    amount
        @DecimalMin("0.00")

---

# 51. QuotationForm

主な項目：

    amount
    quotationDate
    validUntil

Validation：

    amount
        @NotNull
        @DecimalMin("0.00")

    quotationDate
        @NotNull

Service 層で、

    validUntil >= quotationDate

を確認する。

---

# 52. WorkHandoffForm

主な項目：

    workDescription
    dueDate
    specialNotes

Validation：

    workDescription
        @NotBlank

    dueDate
        @NotNull

---

# 53. DeliveryForm

主な項目：

    deliveryDate
    description
    notes

Validation：

    deliveryDate
        @NotNull

    description
        @NotBlank

---

# 54. InvoiceForm

主な項目：

    invoiceDate
    amount
    paymentDueDate

Validation：

    invoiceDate
        @NotNull

    amount
        @NotNull
        @DecimalMin("0.00")

    paymentDueDate
        @NotNull

Service 層で、

    paymentDueDate >= invoiceDate

を確認する。

---

# 55. PaymentForm

主な項目：

    paymentDate
    amount

Validation：

    paymentDate
        @NotNull

    amount
        @NotNull
        @DecimalMin("0.00")

Service 層で Invoice 金額との一致を確認する。

---

# 56. Controller 処理パターン

登録画面の基本パターン：

    GET
       ↓
    Form 初期化
       ↓
    View 表示

登録処理：

    POST
       ↓
    @Valid
       ↓
    BindingResult
       │
       ├── Error
       │     ↓
       │   Form 再表示
       │
       └── Success
             ↓
           Service
             ↓
           Redirect

POST 成功後は、

    Post / Redirect / Get

パターンを利用する。

---

# 57. 例外クラス

最低限、以下を想定する。

    ResourceNotFoundException
    BusinessRuleViolationException
    InvalidStatusTransitionException
    DuplicateResourceException

必要以上に例外クラスを増やさない。

---

# 58. GlobalExceptionHandler

`@ControllerAdvice` を利用して共通例外処理を行う。

例：

    ResourceNotFoundException
        → 404

    AccessDeniedException
        → 403

    BusinessRuleViolationException
        → 業務エラー画面または元画面

    Exception
        → 500

利用者に Stack Trace や内部 DB 情報を表示しない。

---

# 59. DB 制約

アプリケーション側の Validation だけに依存しない。

最低限以下を DB 制約として設定する。

### users

    email UNIQUE
    name NOT NULL
    password_hash NOT NULL
    role NOT NULL

### customers

    name NOT NULL

### projects

    name NOT NULL
    customer_id NOT NULL
    sales_representative_id NOT NULL
    status NOT NULL

### quotations

    quotation_number UNIQUE
    project_id NOT NULL
    amount NOT NULL

### invoices

    invoice_number UNIQUE
    project_id NOT NULL
    amount NOT NULL

必要な Foreign Key を設定する。

---

# 60. 削除方針

MVP では主要な業務データについて物理削除を積極的に提供しない。

対象：

- User
- Customer
- Project
- Quotation
- Invoice
- Payment

業務履歴保持を優先する。

ユーザーについては、

    enabled = false

による無効化を基本とする。

---

# 61. Flyway

マイグレーションファイル例：

    V1__create_users.sql
    V2__create_customers.sql
    V3__create_projects.sql
    V4__create_quotations.sql
    V5__create_quotation_approvals.sql
    V6__create_work_handoffs.sql
    V7__create_work_assignments.sql
    V8__create_deliveries.sql
    V9__create_invoices.sql
    V10__create_payments.sql

実際には依存関係を考慮し、Claude Code が適切な SQL を作成する。

---

# 62. 初期ユーザー

開発・デモ環境では、各ロールの動作確認用ユーザーを用意する。

例：

    sales@example.com
    sales-manager@example.com
    work-manager@example.com
    worker@example.com
    accounting@example.com
    admin@example.com

実在するメールアドレスは使用しない。

パスワードは本番用途ではないデモ用とし、README 等に本番認証情報と誤認されない形で記載する。

---

# 63. SecurityConfig

Spring Security で以下を設定する。

- ログイン
- ログアウト
- URL 権限
- PasswordEncoder
- CSRF
- AccessDenied 処理

例：

    /admin/**
        ADMIN

    /quotations/*/approve
        SALES_MANAGER, ADMIN

    /projects/*/assignment
        WORK_MANAGER, ADMIN

    /invoices/**
        ACCOUNTING, ADMIN

    /payments/**
        ACCOUNTING, ADMIN

URL のみで完全な業務認可を実現しようとせず、必要な箇所では Service / Method Security も利用する。

---

# 64. CSRF

Spring Security の CSRF 保護を基本的に有効とする。

Thymeleaf Form では CSRF Token を利用する。

利便性のために安易に、

    csrf.disable()

としない。

---

# 65. パスワード

BCrypt 等の `PasswordEncoder` を利用する。

以下は禁止する。

- 平文保存
- ログ出力
- Git への本番パスワード登録

---

# 66. トランザクション対象

最低限以下をトランザクション対象とする。

### 見積承認申請

    QuotationApproval 作成
    +
    Quotation 更新
    +
    Project 更新

### 見積承認

    Approval 更新
    +
    Quotation 更新

### 作業引き継ぎ

    WorkHandoff 作成
    +
    Project 更新

### 納品

    Delivery 作成
    +
    Project 更新

### 請求

    Invoice 作成
    +
    Project 更新

### 入金

    Payment 作成
    +
    Project 更新

---

# 67. テスト構成

テストを以下に分類する。

    Unit Test
    Repository Test
    MVC / Security Test
    Integration Test

---

# 68. Service 単体テスト

優先度を高くする。

主な対象：

### ProjectService

- 正常ステータス遷移
- 不正ステータス遷移

### QuotationApprovalService

- 100 万円超で承認必要
- 100 万円以下
- 承認
- 差し戻し
- 差し戻し理由
- 金額変更後の再承認

### WorkService

- 受注前の引き継ぎ拒否
- 担当設定
- 作業開始条件

### DeliveryService

- 作業中案件の納品
- 不正状態での納品拒否

### InvoiceService

- 納品済み案件の請求
- 納品前の請求拒否
- 二重請求拒否

### PaymentService

- 正常入金
- 金額不一致
- 請求前の入金拒否

---

# 69. Security テスト

最低限以下を確認する。

- 未認証ユーザーが業務画面へアクセスできない
- SALES が見積承認できない
- SALES_MANAGER が承認できる
- WORKER が請求登録できない
- ACCOUNTING が請求登録できる
- 非 ADMIN がユーザー管理できない

---

# 70. Controller テスト

主要 Controller について以下を確認する。

- 正常表示
- Validation Error
- Redirect
- 404
- 403

すべての単純 CRUD を過剰にテストするのではなく、業務上重要な箇所を優先する。

---

# 71. Integration Test

主要業務フローを結合テストする。

最低限：

    Customer
       ↓
    Project
       ↓
    Quotation
       ↓
    Approval
       ↓
    Order
       ↓
    WorkHandoff
       ↓
    WorkAssignment
       ↓
    Delivery
       ↓
    Invoice
       ↓
    Payment
       ↓
    Complete

可能であれば Testcontainers 等を利用して PostgreSQL を使った統合テストを検討する。

ただし MVP の進行を阻害する場合は段階導入する。

---

# 72. 要件とテストの関連付け

テスト名またはコメント、Issue 等から要件 ID を追跡できるようにする。

例：

    FR-090
    AC-011

に対して、

    InvoiceServiceTest
    InvoiceControllerTest

等を関連付ける。

---

# 73. GitHub Issue との関連

実装作業は GitHub Issue 単位に分割する。

Issue には最低限、

- 目的
- 対象要件 ID
- 実装対象
- 受け入れ条件
- テスト観点
- Definition of Done

を記載する。

---

# 74. Definition of Done

各機能の完了条件を以下とする。

- 対象要件を満たしている
- ビルド成功
- 自動テスト成功
- 必要な Validation が存在する
- 権限制御を確認
- Secret が含まれていない
- Claude Code 自己レビュー済み
- Codex レビュー済み
- 重大なレビュー指摘を解消
- CI 成功

---

# 75. Claude Code の実装ルール

Claude Code は以下を守る。

1. 要件定義書を確認する
2. 基本設計書を確認する
3. 詳細設計書を確認する
4. 対象 GitHub Issue を確認する
5. 変更範囲を説明する
6. 実装する
7. テストを追加する
8. テストを実行する
9. 変更内容を自己レビューする
10. 結果を人間へ報告する

---

# 76. Claude Code が独自判断してはいけない事項

以下について、設計資料に答えがない場合は人間へ確認する。

- 業務ルール変更
- 新しいロール追加
- 新しいステータス追加
- 承認条件変更
- データ削除方針変更
- セキュリティ要件緩和
- MVP スコープ変更
- 外部サービス追加
- 大規模なアーキテクチャ変更

---

# 77. Claude Code に裁量を与える事項

以下については要件・設計に反しない範囲で Claude Code が提案・判断してよい。

- private メソッド名
- 局所的なリファクタリング
- テストヘルパー
- Thymeleaf の細かな HTML 構造
- CSS の軽微な構成
- Repository の具体的な Spring Data メソッド
- 内部 DTO の追加
- 重複コードの整理

ただし大きな構造変更は事前に人間へ説明する。

---

# 78. Codex レビュー観点

Codex は以下を重点的に確認する。

### Requirement

- 要件漏れ
- 要件と異なる動作

### Architecture

- レイヤー責務
- 不必要な依存
- 過度な複雑化

### Business Logic

- ステータス遷移
- 承認
- 請求
- 入金

### Security

- 認証
- 認可
- CSRF
- 入力検証
- Secret

### Database

- FK
- UNIQUE
- NOT NULL
- トランザクション
- データ整合性

### Test

- 重要ロジックのテスト漏れ
- 境界値
- 異常系

---

# 79. AI 間で意見が異なる場合

Claude Code と Codex の意見が異なる場合、どちらかを自動的に正解としない。

以下の順序で判断する。

    要件定義
       ↓
    基本設計
       ↓
    詳細設計
       ↓
    業務ルール
       ↓
    技術的妥当性
       ↓
    人間の最終判断

必要であれば設計書自体を更新する。

---

# 80. 実装開始前の判断事項（決定済み）

実装開始前に確定すべきとしていた以下の事項は、人間により決定された。

### 技術バージョン

- Java: 25 (LTS)
- Spring Boot: 4.1.1
- PostgreSQL: 18
- ビルドツール: Maven Wrapper

### Project

- 初期ステータス: `INQUIRY`

### Approval

- `1,000,000円ちょうど` の扱い: 承認不要（`amount > 1,000,000` の場合のみ承認必須）
- 緊急承認例外: MVP では設けない

### Invoice

- MVP では 1 案件につき請求書は最大 1 件とする

### Payment

- MVP では 1 請求につき入金は 1 件とし、入金額は請求額と一致するものとする（分割入金・過入金・不足入金は MVP 対象外）

### Deployment

- Render（Spring Boot アプリケーションは Docker で Web Service としてデプロイし、データベースは Render PostgreSQL を利用する）

### Quotation（参考）

- 見積テンプレート管理・バージョン管理: Phase 2（MVP 対象外）
- 見積書 PDF 出力: Phase 2（MVP 対象外）

なお、見積番号採番方式、承認後の金額変更時の再承認要否、E2E テストツール等、上記以外の未確定事項は引き続き人間の判断待ちである（41 章参照）。

---

# 81. 本工程の学習ポイント

詳細設計では、

**「実装者が何を作ればよいか判断できる粒度」**

まで設計を具体化する。

例えば要件定義では、

    FR-091
    経理担当者は案件に
    請求情報を登録できること

と定義した。

基本設計では、

    Invoice
    InvoiceService
    SCR-080

という構成を決めた。

詳細設計ではさらに、

    POST /projects/{id}/invoice

    InvoiceService

    条件：
    Project.status == DELIVERED

    Invoice 保存
        +
    Project.status = INVOICED

    同一トランザクション

まで具体化した。

一方で、

    変数名
    private method の分割
    Stream API を使うか
    if 文を使うか

等まで詳細設計書で固定する必要はない。

その部分は実装者、今回の場合は Claude Code に裁量を残す。

---

# 82. 詳細設計と AI Coding Agent

AI Coding Agent を利用する場合でも、設計が不要になるわけではない。

設計が存在しない状態で、

    「いい感じに案件管理システムを作って」

と依頼すると、AI が多数の仕様を推測する必要がある。

一方、

    Requirements
       ↓
    Basic Design
       ↓
    Detailed Design
       ↓
    GitHub Issue
       ↓
    Claude Code

という情報を与えることで、AI の推測範囲を減らすことができる。

本プロジェクトでは、

**AI にコードを書く裁量は与えるが、業務仕様を勝手に決める裁量は与えない**

ことを基本方針とする。

---

# 83. 次の工程

詳細設計完了後、実装をすぐ開始するのではなく、AI Coding Agent が継続的に参照する開発コンテキストを整備する。

次に作成するもの：

    CLAUDE.md

主な内容：

- プロジェクト概要
- 技術スタック
- 参照すべき設計資料
- アーキテクチャルール
- コーディングルール
- セキュリティルール
- テストルール
- 禁止事項
- 作業手順

その後、

    GitHub Issues
       ↓
    Branch
       ↓
    Claude Code
       ↓
    Pull Request
       ↓
    Codex Review
       ↓
    Human Decision
       ↓
    Merge

という AI ネイティブ開発フローへ移行する。
