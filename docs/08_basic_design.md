# 基本設計書

## 1. 本ドキュメントの目的

本ドキュメントでは、`07_requirements_definition.md` で定義した要件を、どのようなシステム構成・画面・データ・処理で実現するかを基本設計として整理する。

本書では主に以下を定義する。

- システム構成
- 技術スタック
- アプリケーション構成
- 画面一覧
- 画面遷移
- 主要画面仕様
- データモデル
- ER 概念設計
- 権限設計
- 案件ステータス設計
- 見積承認設計
- 主要業務シーケンス
- バリデーション方針
- エラー処理方針
- セキュリティ方針
- ログ方針
- テスト方針

詳細なクラス構成、メソッド、SQL、HTML 実装等については詳細設計および実装工程で決定する。

---

# 2. システム概要

本システムは、営業・作業部門・経理が案件情報を共有する社内向け Web アプリケーションとする。

主な業務対象は以下である。

    顧客
      ↓
    案件
      ↓
    見積
      ↓
    承認
      ↓
    受注
      ↓
    作業引き継ぎ
      ↓
    作業
      ↓
    納品
      ↓
    請求
      ↓
    入金
      ↓
    完了

案件を中心として関連情報を管理する。

---

# 3. 技術スタック

## 3.1 基本方針

MVP では、過度に複雑なフロントエンド・バックエンド分離構成を採用せず、Spring Boot を中心としたサーバーサイド Web アプリケーションとする。

理由：

- MVP の業務機能実装に集中できる
- REST API と SPA を別々に構築するより構成が単純
- 認証・認可を Spring Security に集約できる
- テストしやすい
- CI/CD を構築しやすい
- AI Coding Agent がプロジェクト全体を把握しやすい
- 本プロジェクトの目的である SDLC 学習に適している

---

## 3.2 採用技術

| 分類              | 技術                            |
| ----------------- | ------------------------------- |
| Language          | Java 25 (LTS)                   |
| Backend           | Spring Boot 4.1.1               |
| MVC               | Spring MVC                      |
| View              | Thymeleaf                       |
| Security          | Spring Security                 |
| ORM               | Spring Data JPA / Hibernate     |
| Database          | PostgreSQL 18                   |
| Migration         | Flyway                          |
| Build             | Maven（Maven Wrapper を使用）   |
| Test              | JUnit 5                         |
| Mock              | Mockito                         |
| Integration Test  | Spring Boot Test                |
| Version Control   | Git / GitHub                    |
| CI                | GitHub Actions                  |
| Container         | Docker / Docker Compose         |
| AI Implementation | Claude Code                     |
| AI Review         | Codex                           |

上記バージョンは実装開始前に人間により決定済みである。

---

# 4. システム構成

基本構成を以下とする。

    ┌──────────────────┐
    │   Web Browser    │
    │ Chrome / Edge 等 │
    └────────┬─────────┘
             │
             │ HTTPS / HTTP
             ▼
    ┌──────────────────┐
    │   Spring Boot    │
    │                  │
    │ Controller       │
    │ Service          │
    │ Repository       │
    │ Security         │
    │ Thymeleaf        │
    └────────┬─────────┘
             │
             │ JPA
             ▼
    ┌──────────────────┐
    │   PostgreSQL     │
    └──────────────────┘

MVP ではモノリシックアプリケーションとして構築する。

マイクロサービス構成は採用しない。

---

# 5. アプリケーション内部構成

基本的に以下のレイヤー構成とする。

    Controller
        ↓
    Service
        ↓
    Repository
        ↓
    Database

各レイヤーの責務を以下とする。

## Controller

- HTTP リクエスト受付
- 入力値受け取り
- バリデーション結果処理
- Service 呼び出し
- View 選択

Controller に複雑な業務ロジックを記述しない。

## Service

- 業務ロジック
- ステータス変更判断
- 権限を伴う業務処理
- トランザクション管理

## Repository

- データベースアクセス
- Entity の取得・保存
- 検索

## View

Thymeleaf を利用して HTML を生成する。

---

# 6. パッケージ構成方針

初期案として機能単位のパッケージ構成を採用する。

例：

    com.example.salesmanagement

    ├── auth
    ├── user
    ├── customer
    ├── project
    ├── quotation
    ├── handoff
    ├── work
    ├── delivery
    ├── invoice
    ├── payment
    └── common

各機能配下に必要に応じて、

    controller
    service
    repository
    entity
    dto

等を配置する。

最終的なパッケージ構成は詳細設計で決定する。

---

# 7. 画面一覧

MVP の主要画面を以下とする。

| 画面 ID | 画面名             | 主な利用者       |
| ------- | ------------------ | ---------------- |
| SCR-001 | ログイン           | 全利用者         |
| SCR-010 | ダッシュボード     | 全利用者         |
| SCR-020 | ユーザー一覧       | 管理者           |
| SCR-021 | ユーザー登録・編集 | 管理者           |
| SCR-030 | 顧客一覧           | 全利用者         |
| SCR-031 | 顧客詳細           | 全利用者         |
| SCR-032 | 顧客登録・編集     | 営業・管理者     |
| SCR-040 | 案件一覧           | 全利用者         |
| SCR-041 | 案件詳細           | 全利用者         |
| SCR-042 | 案件登録・編集     | 営業・管理者     |
| SCR-050 | 見積詳細           | 営業・営業責任者 |
| SCR-051 | 見積登録・編集     | 営業             |
| SCR-052 | 見積承認           | 営業責任者       |
| SCR-060 | 作業引き継ぎ       | 営業・作業部門   |
| SCR-061 | 作業担当設定       | 作業部門責任者   |
| SCR-062 | 作業進捗更新       | 作業担当者       |
| SCR-070 | 納品登録           | 作業部門         |
| SCR-080 | 請求登録           | 経理             |
| SCR-090 | 入金登録           | 経理             |

---

# 8. 画面遷移

主要な画面遷移を以下とする。

    ログイン
       │
       ▼
    ダッシュボード
       │
       ├──────── 顧客一覧
       │              │
       │              ├── 顧客詳細
       │              └── 顧客登録・編集
       │
       ├──────── 案件一覧
       │              │
       │              ├── 案件登録
       │              │
       │              └── 案件詳細
       │                    │
       │                    ├── 見積
       │                    ├── 承認
       │                    ├── 引き継ぎ
       │                    ├── 作業担当
       │                    ├── 納品
       │                    ├── 請求
       │                    └── 入金
       │
       └──────── ユーザー管理
                      │
                      └── ユーザー登録・編集

案件詳細画面を業務の中心画面とする。

---

# 9. ダッシュボード

## SCR-010 ダッシュボード

ログイン後の初期画面とする。

MVP では高度な BI 機能を実装せず、業務上確認が必要な情報への入口とする。

表示候補：

- 自分の担当案件
- 承認待ち見積
- 作業担当未設定案件
- 納品済み・未請求案件
- 未入金案件

表示内容はロールによって変更する。

---

# 10. 顧客画面

## SCR-030 顧客一覧

表示項目：

- 顧客名
- 電話番号
- メールアドレス
- 更新日時

操作：

- 詳細表示
- 新規登録
- 検索

## SCR-031 顧客詳細

表示項目：

- 顧客情報
- 関連案件

## SCR-032 顧客登録・編集

入力項目：

- 顧客名
- 住所
- 電話番号
- メールアドレス
- 備考

顧客名を必須とする。

---

# 11. 案件画面

## SCR-040 案件一覧

表示項目：

- 案件名
- 顧客
- 営業担当者
- 作業担当者
- ステータス
- 納期
- 請求状態
- 入金状態

検索・絞り込み：

- 案件名
- 顧客名
- ステータス

---

## SCR-041 案件詳細

案件詳細画面を本システムの中心画面とする。

以下の情報をまとめて確認できることを目標とする。

### 基本情報

- 案件名
- 顧客
- 営業担当者
- 案件概要
- 金額
- 納期
- ステータス

### 見積

- 見積番号
- 金額
- 状態
- 承認状態

### 作業

- 引き継ぎ内容
- 作業担当者
- 作業状態
- 納品情報

### 請求

- 請求番号
- 請求日
- 請求金額
- 支払期限

### 入金

- 入金日
- 入金金額
- 入金状態

案件詳細から、利用者の権限および案件状態に応じて各操作画面へ遷移する。

---

# 12. データモデル

主要エンティティを以下とする。

| Entity            | 内容           |
| ----------------- | -------------- |
| User              | システム利用者 |
| Customer          | 顧客           |
| Project           | 案件           |
| Quotation         | 見積           |
| QuotationApproval | 見積承認       |
| WorkHandoff       | 作業引き継ぎ   |
| WorkAssignment    | 作業担当       |
| Delivery          | 納品           |
| Invoice           | 請求           |
| Payment           | 入金           |

---

# 13. ER 概念設計

概念的な関連を以下とする。

    User
      │
      │ salesRepresentative
      ▼
    Project ───────────── Customer
      │
      ├──── Quotation
      │        │
      │        └──── QuotationApproval
      │
      ├──── WorkHandoff
      │
      ├──── WorkAssignment ─── User
      │
      ├──── Delivery
      │
      └──── Invoice
               │
               └──── Payment

主な関係：

- Customer 1 : N Project
- User 1 : N Project（営業担当）
- Project 1 : N Quotation
- Quotation 1 : 0..N QuotationApproval
- Project 1 : 0..1 WorkHandoff
- Project 1 : 0..N WorkAssignment
- Project 1 : 0..1 Delivery
- Project 1 : 0..N Invoice
- Invoice 1 : 0..N Payment

MVP では、1 案件につき請求書は最大 1 件、1 請求につき入金は 1 件（入金額は請求額と一致）とすることが決定済みである。分割請求・分割入金・過入金・不足入金は MVP 対象外とする。

DB モデルは将来の Phase 2 拡張（分割請求・部分入金対応）を考慮し、Invoice と Payment を 1:N で許容できる構造としてよいが、MVP のアプリケーションロジックでは上記の制約を適用する。

---

# 14. 主要エンティティ設計

## 14.1 User

主な属性：

- id
- name
- email
- passwordHash
- role
- enabled
- createdAt
- updatedAt

---

## 14.2 Customer

主な属性：

- id
- name
- address
- phone
- email
- notes
- createdAt
- updatedAt

---

## 14.3 Project

主な属性：

- id
- name
- customerId
- salesRepresentativeId
- description
- amount
- dueDate
- status
- createdAt
- updatedAt

---

## 14.4 Quotation

主な属性：

- id
- projectId
- quotationNumber
- amount
- quotationDate
- validUntil
- status
- createdBy
- createdAt
- updatedAt

---

## 14.5 QuotationApproval

主な属性：

- id
- quotationId
- applicantId
- approverId
- status
- rejectionReason
- requestedAt
- approvedAt

---

## 14.6 WorkHandoff

主な属性：

- id
- projectId
- workDescription
- dueDate
- specialNotes
- handedOffBy
- handedOffAt

---

## 14.7 WorkAssignment

主な属性：

- id
- projectId
- workerId
- assignedBy
- assignedAt

---

## 14.8 Delivery

主な属性：

- id
- projectId
- deliveryDate
- description
- notes
- registeredBy

---

## 14.9 Invoice

主な属性：

- id
- projectId
- invoiceNumber
- invoiceDate
- amount
- paymentDueDate
- createdAt
- updatedAt

---

## 14.10 Payment

主な属性：

- id
- invoiceId
- paymentDate
- amount
- status
- registeredBy
- createdAt

---

# 15. ID・採番方針

データベース内部 ID は、アプリケーション内部で一意となる方式を採用する。

見積番号、請求番号等の業務番号は内部 ID と分離する。

例：

    DB ID
    123

    見積番号
    Q-2026-0001

    請求番号
    INV-2026-0001

正式な採番ルールは詳細設計で決定する。

---

# 16. 案件ステータス設計

基本的な状態遷移を以下とする。

    INQUIRY
       ↓
    QUOTING
       ↓
    QUOTE_APPROVAL_PENDING
       ↓
    QUOTE_SUBMITTED
       ↓
    ORDERED
       ↓
    WORK_PREPARING
       ↓
    WORKING
       ↓
    DELIVERED
       ↓
    INVOICED
       ↓
    PAID
       ↓
    COMPLETED

例外状態：

    ON_HOLD
    LOST
    CANCELLED

日本語表示：

| 内部値                 | 表示         |
| ---------------------- | ------------ |
| INQUIRY                | 問い合わせ   |
| QUOTING                | 見積作成中   |
| QUOTE_APPROVAL_PENDING | 見積承認待ち |
| QUOTE_SUBMITTED        | 見積提出済   |
| ORDERED                | 受注         |
| WORK_PREPARING         | 作業準備中   |
| WORKING                | 作業中       |
| DELIVERED              | 納品済       |
| INVOICED               | 請求済       |
| PAID                   | 入金済       |
| COMPLETED              | 完了         |
| ON_HOLD                | 保留         |
| LOST                   | 失注         |
| CANCELLED              | キャンセル   |

---

# 17. ステータス遷移制御

任意の状態から自由に変更できる設計にはしない。

Service 層で許可された遷移を判定する。

例：

    QUOTING
       ↓
    QUOTE_APPROVAL_PENDING

は許可する。

一方、

    QUOTING
       ↓
    PAID

のような通常業務上成立しない遷移は拒否する。

詳細な状態遷移表は詳細設計で定義する。

---

# 18. 見積承認設計

基本フロー：

    営業
      │
      │ 見積作成
      ▼
    見積
      │
      │ 承認申請
      ▼
    承認待ち
      │
      ▼
    営業責任者
      │
      ├── 承認
      │      ↓
      │    承認済
      │
      └── 差し戻し
             ↓
          差し戻し
             ↓
          営業修正

見積金額が 1,000,000 円を超える場合のみ、営業責任者による承認を必須とする。

境界条件：

    999,999円   承認不要
    1,000,000円 承認不要
    1,000,001円 承認必須

    amount > 1,000,000

の場合に承認を必須とする。

緊急時における承認スキップ機能は MVP では設けない。

---

# 19. 承認後の見積変更

承認済み見積の重要項目を変更した場合、承認状態をそのまま維持すると、承認した内容と実際の内容が異なる可能性がある。

そのため、

- 金額
- 主要条件

等を変更した場合は再承認を必要とする設計を基本候補とする。

詳細な対象項目は詳細設計で決定する。

---

# 20. 作業引き継ぎシーケンス

    営業担当
       │
       │ 受注登録
       ▼
     Project
       │
       │ 引き継ぎ情報登録
       ▼
    WorkHandoff
       │
       ▼
    作業部門責任者
       │
       │ 作業担当設定
       ▼
    WorkAssignment
       │
       ▼
    作業担当者

作業担当者設定後、案件を作業準備中とする。

---

# 21. 納品・請求シーケンス

    作業担当者
       │
       │ 納品登録
       ▼
     Delivery
       │
       ▼
    Project = DELIVERED
       │
       ▼
    経理担当者
       │
       │ 請求登録
       ▼
     Invoice
       │
       ▼
    Project = INVOICED

これにより、

**営業担当者から経理担当者への個別連絡を必須としなくても、経理担当者が納品済み案件を確認できる**

状態を実現する。

---

# 22. 入金シーケンス

    経理担当者
       │
       │ 入金確認
       ▼
     Payment
       │
       ▼
    Project = PAID
       │
       │ 業務完了確認
       ▼
    Project = COMPLETED

MVP では銀行との自動連携は行わない。

経理担当者が手動で入金情報を登録する。

---

# 23. 権限設計

Spring Security を利用したロールベースアクセス制御を基本とする。

想定ロール：

    ROLE_SALES
    ROLE_SALES_MANAGER
    ROLE_WORK_MANAGER
    ROLE_WORKER
    ROLE_ACCOUNTING
    ROLE_ADMIN

---

# 24. URL アクセス制御方針

例：

    /login
        全利用者

    /customers/**
        認証済み利用者

    /projects/**
        認証済み利用者

    /admin/**
        ROLE_ADMIN

    /quotations/*/approve
        ROLE_SALES_MANAGER
        ROLE_ADMIN

    /work/assign/**
        ROLE_WORK_MANAGER
        ROLE_ADMIN

    /invoices/**
        ROLE_ACCOUNTING
        ROLE_ADMIN

    /payments/**
        ROLE_ACCOUNTING
        ROLE_ADMIN

実際の URL は詳細設計で確定する。

---

# 25. 画面とサーバー双方での権限制御

権限制御は、

- UI
- Controller / Service

の双方で実施する。

例：

営業担当者には「見積承認」ボタンを表示しない。

ただし、URL を直接入力した場合にもサーバー側で拒否する。

UI 非表示だけをセキュリティ対策としない。

---

# 26. 入力バリデーション

Spring Validation を利用する。

主な検証例：

### Customer

- 顧客名：必須
- メールアドレス：形式確認

### Project

- 案件名：必須
- 顧客：必須
- 金額：0 以上
- 納期：日付形式

### Quotation

- 見積金額：0 以上
- 見積日：必須
- 有効期限：見積日以降

### Invoice

- 請求金額：0 以上
- 請求日：必須
- 支払期限：請求日以降

### Payment

- 入金金額：0 以上
- 入金日：必須

詳細な文字数制限等は詳細設計で決定する。

---

# 27. トランザクション方針

複数データの整合性を維持する必要がある業務処理について、Service 層でトランザクション管理を行う。

例：

    納品登録
       +
    案件ステータス更新

途中で処理に失敗した場合、両方をロールバックする。

Spring の `@Transactional` を基本として利用する。

---

# 28. エラー処理方針

エラーを以下に分類する。

## 入力エラー

例：

- 必須項目未入力
- 不正な金額
- 不正な日付

利用者が修正可能なメッセージを表示する。

## 業務エラー

例：

- 許可されていないステータス遷移
- 承認されていない見積で後続処理を実行
- 納品前に請求登録

業務上実行できない理由を表示する。

## システムエラー

例：

- データベース障害
- 想定外例外

詳細な内部情報を利用者へ表示しない。

ログへ必要情報を記録する。

---

# 29. 例外処理

共通例外処理を利用する。

Spring MVC の、

    @ControllerAdvice
    @ExceptionHandler

等を利用して、共通的なエラー処理を行うことを基本とする。

---

# 30. セキュリティ設計方針

以下を基本とする。

- Spring Security を利用する
- パスワードを平文保存しない
- BCrypt 等による安全なハッシュ化を行う
- CSRF 対策を有効にする
- サーバー側で認可を行う
- 入力値検証を行う
- SQL を文字列連結で構築しない
- 機密情報をログへ出力しない
- Secret を Git リポジトリへコミットしない

---

# 31. Secret 管理

以下の情報をソースコードへ直接記述しない。

- DB パスワード
- API Key
- Secret
- 本番認証情報

ローカル環境では環境変数等を利用する。

GitHub Actions では GitHub Secrets 等を利用する。

`.env` 等の秘密情報ファイルは `.gitignore` の対象とする。

---

# 32. データベースマイグレーション

Flyway を利用する。

DB スキーマ変更を SQL ファイルとしてバージョン管理する。

例：

    src/main/resources/db/migration/

    V1__create_users.sql
    V2__create_customers.sql
    V3__create_projects.sql

本番・テスト環境でスキーマ差異が発生しにくい構成を目指す。

---

# 33. ログ設計

主要なシステムエラーをログへ記録する。

必要に応じて、

- ログイン失敗
- 権限エラー
- 業務処理エラー
- 想定外例外

等を記録する。

ただし、

- パスワード
- セッション情報
- Secret

等の機密情報をログへ出力しない。

---

# 34. テスト設計方針

テストは以下のレベルで実施する。

## 単体テスト

主に Service の業務ロジックを対象とする。

例：

- ステータス遷移
- 見積承認条件
- 入金処理

## Repository テスト

必要に応じてデータアクセスを確認する。

## Controller / Security テスト

以下を確認する。

- HTTP レスポンス
- バリデーション
- ロール制御

## 結合テスト

主要な業務シナリオを確認する。

---

# 35. 最重要 End-to-End シナリオ

本システムの主要な受け入れシナリオを以下とする。

    営業としてログイン
        ↓
    顧客登録
        ↓
    案件登録
        ↓
    見積作成
        ↓
    承認申請
        ↓
    営業責任者として承認
        ↓
    受注
        ↓
    作業引き継ぎ
        ↓
    作業部門責任者が担当者設定
        ↓
    作業担当者が作業開始
        ↓
    納品登録
        ↓
    経理担当者が請求登録
        ↓
    入金登録
        ↓
    案件完了

このシナリオが一連で動作することを MVP の重要な完成条件とする。

---

# 36. CI 方針

GitHub Actions を利用する。

最低限、

    Checkout
       ↓
    Java Setup
       ↓
    Maven Build
       ↓
    Automated Test
       ↓
    Result

を自動実行する。

Pull Request 時にも CI を実行することを目標とする。

---

# 37. Docker 方針

開発環境の再現性を高めるため Docker Compose を利用する。

最低限 PostgreSQL をコンテナで起動できる構成とする。

MVP のデプロイ先は Render とする。

Spring Boot アプリケーションは Docker イメージ化し、Render の Web Service としてデプロイする。

データベースは Render PostgreSQL を利用する。

---

# 38. AI Coding Agent を利用する際の設計原則

Claude Code は、本書および要件定義書を基準として実装する。

Claude Code が独自判断で、

- 新しい業務要件を追加する
- 既存要件を削除する
- 業務ルールを変更する

ことは避ける。

不明点がある場合は人間へ確認する。

---

# 39. Codex レビュー方針

Claude Code が実装した主要機能について、Codex を独立レビュー担当として利用する。

レビュー観点：

- 要件との整合性
- 設計との整合性
- バグ
- セキュリティ
- 認可漏れ
- データ整合性
- テスト不足
- 保守性
- 不必要な複雑化

Claude Code と Codex の意見が異なる場合、人間が要件・設計を確認して最終判断する。

---

# 40. 要件トレーサビリティ

要件 ID を設計、Issue、実装、テストへ関連付ける。

例：

    FR-090
    請求対象確認
       ↓
    SCR-080
    請求登録
       ↓
    InvoiceService
       ↓
    GitHub Issue
       ↓
    InvoiceServiceTest
       ↓
    AC-011

これにより、要件からコード・テストまで追跡できる状態を目指す。

---

# 41. 未確定事項

詳細設計へ進む前に、以下を必要に応じて確定する。

## 業務

- 営業担当者未定案件を許可するか
- 複数営業担当者を許可するか
- 複数作業担当者を許可するか
- 承認後の変更で再承認が必要となる項目

## 見積

- 見積番号採番方式

見積 PDF 出力・見積テンプレート機能は Phase 2 とし、MVP 対象外とすることが決定済みである。

## 技術

- E2E テストツール

Java / Spring Boot / PostgreSQL のバージョン、ビルドツール、デプロイ先は決定済みである（3.2 章参照）。

MVP における請求・入金の件数制約（1 案件 1 請求、1 請求 1 入金）についても決定済みである（13 章参照）。

---

# 42. 基本設計上の判断記録

本基本設計では、以下の主要判断を行った。

### ADR 候補 1：Spring Boot + Thymeleaf

MVP では SPA と REST API を分離せず、Spring Boot + Thymeleaf を採用する。

理由：

- 構成を単純化できる
- 業務ロジックへ集中できる
- 認証・認可を統合しやすい
- SDLC 全体を経験する目的に適している

### ADR 候補 2：モノリス

MVP ではマイクロサービスを採用しない。

理由：

30 名規模を想定した業務システムの MVP に対して、マイクロサービス化による複雑性を導入する必要性が低いため。

### ADR 候補 3：案件を中心としたデータモデル

各部門別のデータを中心とせず、Project を中心として見積・作業・請求・入金を関連付ける。

理由：

As-Is 分析で確認した「部門ごとの情報分断」を解消することが、本システムの主要目的だからである。

---

# 43. 本工程の学習ポイント

要件定義では、

**何を実現するか**

を定義した。

基本設計では、

**それをどのようなシステムとして実現するか**

を決める。

例：

    要件

    FR-090
    経理担当者は
    納品済み・未請求案件を確認できること

から、

    基本設計

    Project
        ↓
    Delivery
        ↓
    DELIVERED
        ↓
    経理向け一覧
        ↓
    SCR-080
        ↓
    Invoice 登録

へ具体化する。

ただし、まだ、

    InvoiceService.createInvoice()

の中身を 1 行単位で設計する段階ではない。

それは詳細設計・実装工程で扱う。

---

# 44. 次の工程

次の工程では詳細設計を行う。

詳細設計では主に、

- クラス構成
- DTO
- Entity 詳細
- Repository
- Service
- Controller
- URL
- HTTP Method
- バリデーション詳細
- 状態遷移表
- DB 制約
- マイグレーション
- 例外クラス
- テストケース
- コーディング規約

を具体化する。

その後、

    要件定義
       ↓
    基本設計
       ↓
    詳細設計
       ↓
    CLAUDE.md
       ↓
    GitHub Issues
       ↓
    Claude Code 実装
       ↓
    Codex レビュー
       ↓
    テスト
       ↓
    CI/CD
       ↓
    リリース

へ進む。
