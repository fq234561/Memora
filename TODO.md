# Memora MVP — 待办清单

> 当前为 Mock/本地开发模式，以下事项需按优先级逐步补齐。

---

## 1. 支付与商业化

| # | 任务 | 说明 |
|---|------|------|
| 1.1 | 注册 Stripe 账户 | 申请商家账号，完成身份验证 |
| 1.2 | 创建 Stripe Products & Prices | 创建 3 个产品：Preview Pack ($2.99)、HD Unlock ($9.99)、Full Pack ($14.99)，记录 Price ID |
| 1.3 | 配置 Railway 环境变量 | `STRIPE_SECRET_KEY`、`STRIPE_PRICE_PREVIEW_PACK`、`STRIPE_PRICE_HD_UNLOCK`、`STRIPE_PRICE_FULL_PACK` |
| 1.4 | 配置 Stripe Webhook | 在 Stripe Dashboard 添加 Webhook Endpoint（`https://<backend>/api/stripe/webhook`），获取 `STRIPE_WEBHOOK_SECRET` |
| 1.5 | 配置 `WEB_APP_URL` | 生产环境前端域名，用于 Stripe success/cancel 回调 |
| 1.6 | 移除 Mock 支付模式 | 配置完真实 Stripe 后，删除或禁用 `createMockCheckoutSession` 的 fallback 逻辑 |
| 1.7 | 测试完整支付流程 | 用 Stripe Test Mode 信用卡（4242 4242 4242 4242）跑通端到端 |

---

## 2. AI 图片生成

| # | 任务 | 说明 |
|---|------|------|
| 2.1 | 申请 OpenAI API Key | 获取 API Key 并充值余额 |
| 2.2 | 配置 `OPENAI_API_KEY` | 添加到 Railway 环境变量 |
| 2.3 | 配置 `IMAGE_GENERATION_PROVIDER` | 设置为 `openai`（当前默认为 `mock`） |
| 2.4 | 调优 Prompt | 根据真实生成效果调整 `openaiProvider.ts` 中的 system prompt 和参数 |
| 2.5 | 替换 HD 占位图 | `fulfillCheckout` 中当前用 `picsum.photos` 占位，需接入真实 HD 放大/重绘逻辑 |
| 2.6 | 图片存储 | 生成后的图片需持久化到 R2/S3，而非临时 URL |

---

## 3. 文件存储（R2 / S3）

| # | 任务 | 说明 |
|---|------|------|
| 3.1 | 创建 Cloudflare R2 或 AWS S3 Bucket | 用于存储用户上传的照片和 AI 生成结果 |
| 3.2 | 配置存储环境变量 | `STORAGE_ENDPOINT`、`STORAGE_BUCKET`、`STORAGE_ACCESS_KEY_ID`、`STORAGE_SECRET_ACCESS_KEY`、`STORAGE_PUBLIC_DOMAIN` |
| 3.3 | 替换本地存储逻辑 | 当前可能使用本地磁盘或 SQLite BLOB，需切到对象存储 |

---

## 4. 前端部署

| # | 任务 | 说明 |
|---|------|------|
| 4.1 | 部署到 Vercel / Netlify | 将 `web/` 目录部署到生产环境 |
| 4.2 | 配置生产环境变量 | `NEXT_PUBLIC_API_BASE_URL` 指向 Railway 生产域名 |
| 4.3 | 配置自定义域名（可选） | 如 `app.memora.family` |
| 4.4 | 更新 CORS | Railway 后端 `ALLOWED_ORIGINS` 需加入前端生产域名 |

---

## 5. 安全与监控

| # | 任务 | 说明 |
|---|------|------|
| 5.1 | 配置 JWT Secret | 生产环境 `JWT_SECRET` 需使用强随机字符串 |
| 5.2 | 配置 Sentry | `SENTRY_DSN`、`SENTRY_ENVIRONMENT`，用于前端/后端错误追踪 |
| 5.3 | 数据库备份策略 | SQLite 文件定期备份到云存储 |
| 5.4 | Rate Limiting | 为上传、生成等接口添加限流 |

---

## 6. Google Play 内购（Android App）

| # | 任务 | 说明 |
|---|------|------|
| 6.1 | 配置 Google Play Service Account | `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_BASE64` |
| 6.2 | 在 Google Play Console 创建内购商品 | 与 Stripe 价格对齐 |
| 6.3 | 验证 Google Play 购买收据 | 后端 `purchases.ts` 中补充 Google Play 验证逻辑 |

---

## 7. 功能完善

| # | 任务 | 说明 |
|---|------|------|
| 7.1 | 邮件通知 | 支付成功、生成完成时发送邮件 |
| 7.2 | 用户删除账号 / GDPR | 数据删除接口 |
| 7.3 | 客服与反馈 | 完善 Contact 表单后端处理 |
| 7.4 | 相册（Album）功能 | 当前有接口但前端未实现 |
| 7.5 | 视频渲染 | `video-renderer/` 目录待集成 |
| 7.6 | 漏斗分析接入 | `analytics.ts` 已创建，需接入真实分析平台（PostHog / Mixpanel / Amplitude） |

---

## 8. 测试与质量

| # | 任务 | 说明 |
|---|------|------|
| 8.1 | E2E 测试 | 用 Playwright 跑通完整用户流程 |
| 8.2 | 单元测试 | 为 store、stripeService、auth 等核心模块补测试 |
| 8.3 | 性能测试 | AI 生成耗时、图片上传大小限制优化 |

---

## 当前 Mock 模式行为

- **支付**：点击购买直接成功，不经过 Stripe，项目状态自动更新
- **图片生成**：依赖 `IMAGE_GENERATION_PROVIDER` 设置，当前默认为 `mock`
- **存储**：若未配置 R2，可能使用本地文件系统
