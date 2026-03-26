# Scentory3

香水作り体験ワークショップ向けの Spring Boot 管理システムです。

## 実装済み

- ログイン画面
- ダッシュボード
- 予約管理
- 顧客管理
- 顧客詳細画面
- スケジュール管理
- 商品・メニュー管理
- コンテンツ管理
- スタッフ管理
- レポート・集計
- システム設定
- アカウント
- 公開予約ページ連携 API (`/public/api/reservations`)

## ログイン情報

- 管理者: `admin / admin123`
- スタッフ: `staff / staff123`
- 閲覧用: `viewer / viewer123`

## ローカル起動

1. `c:\academia\src\portfolio\Scentory3` を開く
2. Maven が入っている環境で `mvn spring-boot:run` を実行する
3. `http://localhost:8080/login` にアクセスする

## DB設定

- ローカル既定値: `jdbc:h2:file:./data/scentory3`
- 本番: 環境変数で PostgreSQL などに差し替え可能

使用する環境変数:

- `DATABASE_URL`
- `DATABASE_DRIVER`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `PORT`
- `JPA_DDL_AUTO`
- `H2_CONSOLE_ENABLED`

PostgreSQL 例:

- `DATABASE_URL=jdbc:postgresql://<host>:5432/<db>`
- `DATABASE_DRIVER=org.postgresql.Driver`
- `DATABASE_USERNAME=<username>`
- `DATABASE_PASSWORD=<password>`

## 公開予約ページとの連携

- 公開側の予約ページは `POST /public/api/reservations` に送信します
- GitHub Pages から使う場合は、管理画面 API を外部公開した URL へ向ける必要があります
- 例:
  `https://mikunakamura-39.github.io/Scentory/LP/Reservation.html?apiBase=https://your-api.example.com`

## 補足

- この環境ではこちらから起動確認まではできていません
- ローカルでは H2 ファイルDB、本番では PostgreSQL などへ切り替える想定です
- 通知、CSV、返金処理などは次フェーズ想定です
