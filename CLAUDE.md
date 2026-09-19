# CLAUDE.md

## Project Overview

このリポジトリは、AI ネイティブなソフトウェア開発ライフサイクル（SDLC）を学習するための研修用プロジェクトです。

題材として、中小企業向けの「案件・見積・作業・請求管理システム」を開発します。

本プロジェクトの目的は、単にアプリケーションを完成させることではありません。

以下の一連の開発プロセスを、人間と AI Coding Agent が協働して実践することを目的とします。

- ヒアリング
- As-Is 業務整理
- 業務課題分析
- To-Be 業務設計
- システム化範囲・MVP 決定
- 要件定義
- 基本設計
- 詳細設計
- 実装
- テスト
- コードレビュー
- CI/CD
- リリース
- 振り返り

人間が最終的な意思決定を行い、Claude Code は実装を担当する AI エンジニアとして利用します。

---

## Source of Truth

実装や設計判断を行う前に、`README.md` および `docs/` 配下の関連ドキュメントを確認してください。

現在の主要ドキュメントは以下です。

1. `docs/01_initial_hearing.md`

   - 初回ヒアリング

2. `docs/02_hearing_retrospective.md`

   - ヒアリングの振り返り

3. `docs/03_as_is_business_flow.md`

   - 現行業務（As-Is）

4. `docs/04_business_issues_analysis.md`

   - 業務課題分析

5. `docs/05_to_be_business_flow.md`

   - 将来業務（To-Be）

6. `docs/06_system_scope_and_mvp.md`

   - システム化範囲と MVP

7. `docs/07_requirements_definition.md`

   - 要件定義

8. `docs/08_basic_design.md`

   - 基本設計

9. `docs/09_detailed_design.md`
   - 詳細設計

実装時には、特に以下を優先して参照してください。

1. 要件定義
2. 基本設計
3. 詳細設計
4. 関連する業務ルール
5. GitHub Issue

上位ドキュメントと下位ドキュメントに矛盾がある場合は、独自判断で解決せず、人間に確認してください。

---

## Human Decision Policy

Claude Code は、未決定の業務要件を独自に決定してはいけません。

以下の場合は実装を進める前に人間へ確認してください。

- 要件が未定義である
- 複数の解釈が可能である
- ドキュメント間に矛盾がある
- 業務ルールの変更が必要になる
- MVP の範囲を変更する必要がある
- セキュリティや権限に関する重要な判断が必要である
- データモデルを大きく変更する必要がある

「実装しやすいから」という理由だけで要件や設計を変更しないでください。

---

## MVP Scope

MVP では、顧客から入金完了までの主要な業務フローを一通り実行できることを目標とします。

基本的な業務フローは以下です。

```text
ログイン
  ↓
顧客登録
  ↓
案件登録
  ↓
見積作成
  ↓
必要な場合は見積承認
  ↓
見積提出
  ↓
受注
  ↓
作業部門への引き継ぎ
  ↓
作業担当者割り当て
  ↓
作業
  ↓
納品
  ↓
請求
  ↓
入金確認
  ↓
案件完了
```

MVP に含まれない機能を、独自判断で追加しないでください。

---

## Out of Scope for MVP

以下は原則として MVP 対象外です。

- メールの自動取得
- AI によるメール返信生成
- AI による案件要約
- Slack 連携
- Microsoft Teams 連携
- 銀行 API 連携
- 会計システム連携
- CRM/SFA 連携
- 高度な KPI ダッシュボード
- ガントチャート
- 高度な通知・リマインド
- 人事評価機能

将来拡張として検討することはできますが、Issue や要件に明示されていない限り実装しないでください。

---

## Architecture

MVP はモノリシックなサーバーサイド Web アプリケーションとして構築します。

予定している主要技術は以下です。

- Java 25 (LTS)
- Spring Boot 4.1.1
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL 18
- Flyway
- Maven（Maven Wrapper を使用）
- JUnit 5
- Mockito
- Spring Boot Test
- Docker / Docker Compose
- Git
- GitHub
- GitHub Actions

SPA やマイクロサービス構成は MVP では採用しません。

デプロイ先は Render とします。Spring Boot アプリケーションは Docker で Web Service としてデプロイし、データベースは Render PostgreSQL を利用します。

上記以外でバージョン等が未決定の場合は、勝手に決定せず確認してください。

---

## Application Architecture

基本的に以下のレイヤ構成を使用します。

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

画面表示には Thymeleaf を使用します。

パッケージは原則として機能単位で構成します。

```text
com.example.salesmanagement

├── auth
├── user
├── customer
├── project
├── quotation
├── work
├── delivery
├── invoice
├── payment
└── common
```

---

## Business Rules

実装時には `docs/07_requirements_definition.md` および `docs/09_detailed_design.md` のビジネスルールを必ず確認してください。

特に以下を独自判断で変更しないでください。

- 案件を中心として情報を管理する
- 見積は案件に紐づく
- 作業引き継ぎは受注後に行う
- 請求は原則として納品後に行う
- 入金は請求情報に基づいて管理する
- 案件ステータスの変更は Service 層で制御する
- 権限制御は画面表示だけでなくサーバー側でも行う

見積承認条件など、未決定事項が残っている場合は人間へ確認してください。

---

## Security Rules

以下を必ず守ってください。

- パスワードを平文で保存しない
- Spring Security を使用する
- BCrypt 等の適切な方式でパスワードをハッシュ化する
- 認証だけでなく認可も実装する
- サーバー側で権限チェックを行う
- CSRF 対策を無効化しない
- 入力値を検証する
- API キーやパスワードなどの秘密情報を Git へコミットしない
- ログへ秘密情報を出力しない

セキュリティ機能を簡略化する必要がある場合は、独自判断せず人間へ確認してください。

---

## Testing Rules

機能を実装する際は、対応するテストも作成してください。

主に以下を対象とします。

- Service 層の単体テスト
- Repository テスト
- Controller / MVC テスト
- Spring Security の権限テスト
- 主要業務フローの統合テスト

特に重要なビジネスルールについては、自動テストを優先してください。

テストを削除・無効化してビルドを通すことは禁止します。

既存テストが失敗した場合は、原因を確認してください。

---

## Git and GitHub Rules

GitHub Issue を単位として実装を進めます。

Issue には可能な限り以下を含めます。

- 目的
- 関連する要件 ID
- 実装対象
- 受け入れ条件
- テスト内容
- Definition of Done

実装前に関連 Issue と要件を確認してください。

原則として、Claude Code が人間の明示的な指示なしに以下を実行しないでください。

- `git commit`
- `git push`
- ブランチ削除
- force push
- Git 履歴の書き換え
- GitHub Issue のクローズ
- Pull Request のマージ

ファイル変更後は、変更内容とテスト結果を人間へ報告してください。

---

## AI Coding Workflow

基本的な開発フローは以下です。

```text
要件・設計
    ↓
GitHub Issue
    ↓
人間が実装対象を決定
    ↓
Claude Codeが関連資料を確認
    ↓
Claude Codeが実装
    ↓
Claude Codeがテスト
    ↓
Claude Codeが自己レビュー
    ↓
Codexによる独立レビュー
    ↓
人間がレビュー結果を確認
    ↓
必要に応じて修正
    ↓
CI
    ↓
人間がマージ判断
```

Claude Code と Codex の意見が異なる場合、どちらかが自動的に正しいとは判断しません。

最終判断は人間が行います。

---

## Implementation Policy

実装時には以下を守ってください。

1. まず関連ドキュメントと Issue を読む
2. 要件を確認する
3. 不明点があれば実装前に質問する
4. 必要最小限の変更を行う
5. 不要なリファクタリングを同時に行わない
6. テストを作成または更新する
7. テストを実行する
8. 変更内容を自己レビューする
9. 人間へ結果を報告する

Issue に関係のないコードを不用意に変更しないでください。

---

## Definition of Done

Issue を完了候補とするため、原則として以下を満たしてください。

- 要件を満たしている
- 設計との重大な矛盾がない
- ビルドが成功する
- 必要な自動テストが存在する
- テストが成功する
- セキュリティ上の重大な問題がない
- 不要な変更が含まれていない
- Claude Code による自己レビューが完了している
- 変更内容が人間に説明されている

最終的な Issue 完了およびマージ判断は人間が行います。

---

## Important Unresolved Decisions

以下の事項は 2026-09-19 に人間により決定されました。

- Java / Spring Boot / PostgreSQL のバージョン → Java 25 (LTS) / Spring Boot 4.1.1 / PostgreSQL 18
- ビルドツール → Maven Wrapper を使用
- 案件作成時の初期ステータス → `INQUIRY`
- 100 万円ちょうどの見積の承認要否 → 承認不要（`1,000,000` 円を超える場合のみ承認必須）
- 緊急時の承認例外 → MVP では承認スキップ機能を設けない
- MVP で 1 案件 1 請求とするか → 1 案件につき請求書は最大 1 件
- MVP で 1 請求 1 入金とするか → 1 請求につき入金は 1 件とし、入金額は請求額と一致（分割入金・過入金・不足入金は対象外）
- デプロイ先 → Render（Spring Boot アプリケーションを Docker で Web Service としてデプロイ、データベースは Render PostgreSQL）
- 見積テンプレート管理を MVP へ含めるか → Phase 2（MVP 対象外）
- 見積 PDF 出力を MVP へ含めるか → Phase 2（MVP 対象外）

上記以外に未決定事項が判明した場合、Claude Code が独自に仕様を確定してはいけません。

実装上決定が必要になった時点で、人間へ確認してください。

---

## Training Purpose

このプロジェクトでは、AI が大量のコードを生成すること自体を目的としていません。

重要なのは、

- 人間が業務を理解する
- 人間が要件を決定する
- AI へ適切なコンテキストを与える
- AI が実装する
- 別の AI がレビューする
- 人間が最終判断する

という AI ネイティブな開発プロセスを実践することです。

Claude Code は人間の判断を置き換えるのではなく、開発作業を支援する AI エンジニアとして振る舞ってください。
