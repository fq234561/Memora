# Memora 海外 MVP 部署方案

> 版本：v1.0 | 日期：2026-05-03  
> 目标：稳定、低成本、可扩展的海外生产环境

---

## 1. 部署架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用户层 (Users)                                  │
│  ┌──────────────┐  ┌──────────────┐                                         │
│  │ Android App  │  │  Web Admin   │                                         │
│  └──────┬───────┘  └──────┬───────┘                                         │
└─────────┼─────────────────┼─────────────────────────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Cloudflare 层                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  DNS / CDN / HTTPS (yourdomain.com)                                  │    │
│  │  • A 记录 → Railway 公网 IP / 域名                                   │    │
│  │  • R2 自定义域 (images.yourdomain.com) → Signed URL 回源             │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Railway 应用层 ($5-15/月)                            │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Node.js API (Docker / Nixpacks)                                    │    │
│  │  • Express + TypeScript                                             │    │
│  │  • 内存：512MB-1GB (足够 Express + 图片流式上传)                     │    │
│  │  • 健康检查：GET /api/health                                        │    │
│  └────────────────┬────────────────────────────────────────────────────┘    │
│                   │                                                         │
│  ┌────────────────┴────────────────┐    ┌──────────────────────────────┐    │
│  │       Sentry SDK (@sentry/node)  │    │   Trigger.dev (预留，暂不接入) │    │
│  │       错误监控 + 性能追踪         │    │   等 OpenAI 真实生成跑通后接入  │    │
│  └─────────────────────────────────┘    └──────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
          │
          ├────────────────────────────┬────────────────────────────┐
          ▼                            ▼                            ▼
┌──────────────────┐  ┌──────────────────────────┐  ┌─────────────────────────┐
│   Supabase       │  │    Cloudflare R2         │  │   Sentry.io             │
│   Postgres       │  │    私有对象存储            │  │   监控平台               │
│   ($0 起步)      │  │    (~$0.015/GB/月)       │  │   (免费额度)             │
│                  │  │                          │  │                         │
│  • users         │  │  Bucket: memora-uploads  │  │  • 后端错误聚合          │
│  • projects      │  │    - raw/                │  │  • Android 崩溃报告      │
│  • purchases     │  │    - previews/           │  │  • 性能追踪              │
│  • generations   │  │    - final/              │  │                         │
│                  │  │                          │  │                         │
│  Connection      │  │  Signed URL (15min TTL)  │  │                         │
│  Pool: pg/node   │  │  • 上传：服务端预签名 PUT │  │                         │
│                  │  │  • 下载：服务端预签名 GET │  │                         │
└──────────────────┘  └──────────────────────────┘  └─────────────────────────┘
```

### 架构要点

| 组件 | 方案 | 月成本预估 |
|------|------|-----------|
| 域名 + DNS + HTTPS | Cloudflare | ~$10/年 (域名) + $0 (DNS/HTTPS) |
| 后端 API | Railway (Starter Plan) | ~$5-15/月 |
| 数据库 | Supabase Postgres (Free Tier) | $0 (500MB, 2M 行限制) |
| 对象存储 | Cloudflare R2 | ~$0-1/月 (10GB 内几乎免费) |
| 错误监控 | Sentry (Developer Plan) | $0 (5k errors/月) |
| **合计** | | **~$5-15/月 + $10/年** |

---

## 2. 需要注册的账号

### 2.1 Cloudflare（必需）
- **用途**：域名注册 / DNS 管理 / HTTPS 证书 / R2 对象存储 / R2 自定义域
- **注册**：https://dash.cloudflare.com/sign-up
- **需要操作**：
  1. 注册/转移域名（如 `memora.app`）
  2. 开启 Proxied DNS（橙色云图标）
  3. 创建 R2 Bucket：`memora-uploads`
  4. 配置 R2 自定义域：`images.yourdomain.com`
  5. 生成 R2 API Token（S3 兼容）：`Account ID` + `Access Key ID` + `Secret Access Key`

### 2.2 Railway（必需）
- **用途**：托管 Node.js 后端 API
- **注册**：https://railway.app/
- **推荐 Plan**：Starter（$5/月，512MB RAM，共享 CPU）
- **需要操作**：
  1. 连接 GitHub Repo
  2. 配置 Environment Variables
  3. 添加自定义域（绑定 Cloudflare DNS）

### 2.3 Supabase（必需）
- **用途**：Postgres 数据库（仅数据库，不用 Storage）
- **注册**：https://supabase.com/
- **推荐 Plan**：Free Tier（500MB 存储，2M 行，共享 CPU）
- **需要操作**：
  1. 创建 Project，选择最近的 Region（如 `us-east-1` 或 `ap-southeast-1`）
  2. 获取 `DATABASE_URL`（Connection Pooling 推荐，端口 5432 / 6543）
  3. 在 Database → Extensions 启用：`uuid-ossp`, `pgcrypto`

### 2.4 Sentry（必需）
- **用途**：错误监控与崩溃报告
- **注册**：https://sentry.io/
- **推荐 Plan**：Developer（免费，5,000 errors/月，1 席位）
- **需要操作**：
  1. 创建 Organization + Project（`memora-backend`, `memora-android`）
  2. 获取 Backend DSN 和 Android DSN

### 2.5 Google Cloud Platform（已有，保留）
- **用途**：Google Sign-In OAuth 2.0 + Google Play Billing 验证
- **已有配置**：OAuth Client ID、Play Developer API Service Account

---

## 3. 环境变量清单

### 3.1 Railway 后端环境变量

```bash
# ========== 基础配置 ==========
NODE_ENV=production
PORT=3000

# ========== 数据库 (Supabase Postgres) ==========
# 使用 Connection Pooler (PgBouncer) 模式，端口 6543
DATABASE_URL=postgresql://postgres.xxxxx:password@aws-0-us-east-1.pooler.supabase.com:6543/postgres?pgbouncer=true
# 直连端口 5432（仅 migrations / 管理用途）
DATABASE_URL_DIRECT=postgresql://postgres.xxxxx:password@db.xxxxx.supabase.co:5432/postgres

# ========== Cloudflare R2 (S3 兼容) ==========
R2_ENDPOINT=https://xxxxxxxxxxxxxxxxxxxxxxxxxxxx.r2.cloudflarestorage.com
R2_ACCESS_KEY_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
R2_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
R2_BUCKET_NAME=memora-uploads
R2_PUBLIC_DOMAIN=https://images.yourdomain.com          # R2 自定义域，用于 Signed URL
R2_SIGNED_URL_EXPIRES_SECONDS=900                       # 15 分钟

# ========== JWT ==========
JWT_SECRET=your-256-bit-secret-min-32-chars-long!!!!!
JWT_EXPIRES_IN=7d

# ========== Google Auth / Play Billing ==========
GOOGLE_CLIENT_ID=xxxxxxxx.apps.googleusercontent.com
GOOGLE_PLAY_SERVICE_ACCOUNT_KEY_BASE64=base64_encoded_json_key

# ========== Sentry ==========
SENTRY_DSN=https://xxxxxxxxxxxxxxxxxxxxxxxxxxxx@xxxxxxx.ingest.sentry.io/xxxxxxx
SENTRY_ENVIRONMENT=production

# ========== AI 生成 (预留) ==========
# OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# TRIGGER_API_KEY=tr_dev_xxxxxxxx (暂不接入)

# ========== 功能开关 ==========
USE_MOCK_AUTH=false
MOCK_GENERATION=false
```

### 3.2 Android 环境变量 / Build Config

```kotlin
// local.properties (开发机) / CI secrets (GitHub Actions)
SENTRY_DSN_ANDROID=https://xxxxxxxxxxxxxxxxxxxxxxxxxxxx@xxxxxxx.ingest.sentry.io/xxxxxxx
API_BASE_URL=https://yourdomain.com/api
```

---

## 4. 数据库表设计（Postgres 版）

> 从 SQLite 迁移到 Supabase Postgres，主要变更：
> - `id` 从 INTEGER 自增 → `uuid` (gen_random_uuid())
> - 新增 `updated_at` 触发器自动更新
> - 新增 `storage_key` 替代本地文件路径（R2 对象键）
> - 图片 URL 字段存储 R2 `storage_key`，客户端通过 Signed URL 访问

### 4.1 完整 Schema

```sql
-- 启用必要扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 自动更新 updated_at 的函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========== users ==========
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100),
    avatar_url TEXT,
    google_id VARCHAR(100) UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_google_id ON users(google_id);
CREATE TRIGGER tr_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========== projects ==========
CREATE TYPE project_status AS ENUM (
    'draft', 'uploaded', 'generating', 'preview_ready',
    'failed', 'purchased', 'completed'
);

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL DEFAULT 'Untitled',
    style VARCHAR(50) DEFAULT 'natural_family',

    -- R2 存储键（非公开 URL）
    photo_deceased_storage_key TEXT,
    photo_living_storage_key TEXT,

    status project_status DEFAULT 'draft',
    consent_given BOOLEAN DEFAULT FALSE,
    consent_at TIMESTAMPTZ,

    -- AI 生成相关
    prompt_text TEXT,
    generation_started_at TIMESTAMPTZ,
    generation_completed_at TIMESTAMPTZ,
    generation_error TEXT,

    -- 候选图存储键数组（4张预览图）
    candidate_storage_keys TEXT[],
    selected_candidate_index INTEGER DEFAULT -1,

    -- 最终高清图
    final_photo_storage_key TEXT,

    -- 购买相关
    purchased_product_id VARCHAR(50),
    purchase_at TIMESTAMPTZ,

    -- 重生次数限制
    regeneration_count INTEGER DEFAULT 0,
    max_regenerations INTEGER DEFAULT 2,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_user_status ON projects(user_id, status);
CREATE TRIGGER tr_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========== purchases ==========
CREATE TYPE purchase_status AS ENUM ('pending', 'completed', 'refunded', 'failed');

CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id VARCHAR(50) NOT NULL, -- preview_pack / hd_unlock / full_pack
    purchase_token VARCHAR(255) NOT NULL,
    order_id VARCHAR(100),
    status purchase_status DEFAULT 'pending',
    verified_at TIMESTAMPTZ,
    raw_receipt JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_purchases_project_id ON purchases(project_id);
CREATE INDEX idx_purchases_token ON purchases(purchase_token);
CREATE INDEX idx_purchases_user ON purchases(user_id);

-- ========== generation_history ==========
CREATE TABLE generation_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL DEFAULT 'initial', -- initial / regeneration
    prompt_text TEXT,
    -- 本次生成的候选图存储键
    candidate_storage_keys TEXT[],
    status VARCHAR(50) DEFAULT 'started', -- started / success / failed
    error_message TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_generation_history_project ON generation_history(project_id);

-- ========== contact_messages (反馈/举报/删除请求) ==========
CREATE TABLE contact_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type VARCHAR(50) NOT NULL, -- feedback / report / deletion
    email VARCHAR(255),
    message TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_contact_messages_user ON contact_messages(user_id);
CREATE INDEX idx_contact_messages_type ON contact_messages(type);
```

### 4.2 与 SQLite Schema 的关键差异对照

| 字段 | SQLite（旧） | Postgres（新） | 说明 |
|------|-------------|---------------|------|
| `id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | `UUID PRIMARY KEY DEFAULT gen_random_uuid()` | 全局唯一，防枚举 |
| `photo_deceased_url` | 本地文件路径 `./uploads/xxx.jpg` | `photo_deceased_storage_key` | 存储 R2 对象键，如 `raw/2026/05/xxx.jpg` |
| `candidate_urls` | TEXT (JSON 数组) | `candidate_storage_keys TEXT[]` | Postgres 原生数组类型 |
| `status` | TEXT 字符串 | `project_status` ENUM | 类型安全 |
| `created_at` | INTEGER (Unix timestamp) | `TIMESTAMPTZ` | 时区安全 |
| `updated_at` | 无 | 触发器自动更新 | 乐观锁/审计需要 |

---

## 5. R2 Bucket 设计

### 5.1 Bucket 结构

```
memora-uploads (Bucket)
│
├── raw/                          # 用户原始上传图（私有）
│   └── {user_id}/
│       └── {project_id}/
│           ├── deceased_{timestamp}_{uuid}.jpg
│           └── living_{timestamp}_{uuid}.jpg
│
├── previews/                     # AI 生成的 4 张候选预览图（私有）
│   └── {project_id}/
│       ├── candidate_0_{uuid}.jpg
│       ├── candidate_1_{uuid}.jpg
│       ├── candidate_2_{uuid}.jpg
│       └── candidate_3_{uuid}.jpg
│
├── final/                        # 用户购买的最终高清图（私有）
│   └── {project_id}/
│       └── final_{uuid}.jpg
│
└── temp/                         # 临时文件，Lifecycle 规则定期清理
    └── {uuid}.jpg
```

### 5.2 访问控制策略

**Bucket 设置**：
- **Public Access**：❌ 完全关闭（所有对象默认不可公开访问）
- **CORS 规则**：允许 `https://yourdomain.com` 和 `memorialapp://` 的 GET/PUT
- **Lifecycle 规则**：
  - `temp/*`：7 天后自动删除
  - `previews/*`：项目完成后 30 天自动删除（保留 final/ 即可）

**Signed URL 策略**：

```typescript
// 上传用 Presigned PUT URL（有效期 15 分钟）
const putCommand = new PutObjectCommand({
  Bucket: R2_BUCKET_NAME,
  Key: `raw/${userId}/${projectId}/deceased_${timestamp}.jpg`,
  ContentType: 'image/jpeg',
});
const uploadUrl = await getSignedUrl(s3Client, putCommand, {
  expiresIn: 900, // 15 min
});

// 下载用 Presigned GET URL（有效期 15 分钟）
const getCommand = new GetObjectCommand({
  Bucket: R2_BUCKET_NAME,
  Key: project.photo_deceased_storage_key,
});
const downloadUrl = await getSignedUrl(s3Client, getCommand, {
  expiresIn: 900,
});
```

**API 返回给客户端的数据结构**：

```json
{
  "project": {
    "id": "uuid",
    "title": "给妈妈的纪念照",
    "status": "preview_ready",
    "photos": {
      "deceased": {
        "storage_key": "raw/user-uuid/proj-uuid/deceased_1746230400_a1b2.jpg",
        "signed_url": "https://images.yourdomain.com/raw/...?X-Amz-Algorithm=...&X-Amz-Expires=900",
        "expires_at": "2026-05-03T03:15:00Z"
      },
      "living": { ... }
    },
    "candidates": [
      {
        "index": 0,
        "signed_url": "https://images.yourdomain.com/previews/...?...",
        "expires_at": "2026-05-03T03:15:00Z"
      }
    ],
    "final_photo": null
  }
}
```

**客户端处理逻辑**：
- Android App 接收到 `signed_url` 后直接使用 Coil 加载图片
- `signed_url` 过期后（15 分钟），调用后端 `GET /api/projects/:id` 刷新获取新的 Signed URL
- 无需在客户端处理任何认证头，Signed URL 本身即包含临时授权

---

## 6. Railway 部署步骤

### 6.1 准备工作

1. **代码仓库准备**
   ```bash
   cd backend/
   # 确保 tsconfig.json 的 outDir 是 "./dist"
   # 确保 package.json 有 "start": "node dist/index.js"
   git add .
   git commit -m "chore: production-ready for Railway"
   git push origin main
   ```

2. **创建必要文件**

   **`Dockerfile`**（Railway 推荐，比 Nixpacks 更可控）：
   ```dockerfile
   # backend/Dockerfile
   FROM node:20-alpine AS builder
   WORKDIR /app
   COPY package*.json ./
   RUN npm ci --only=production=false
   COPY . .
   RUN npm run build

   FROM node:20-alpine AS runner
   WORKDIR /app
   ENV NODE_ENV=production
   COPY package*.json ./
   RUN npm ci --only=production
   COPY --from=builder /app/dist ./dist
   EXPOSE 3000
   HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
     CMD node -e "require('http').get('http://localhost:3000/api/health', (r) => r.statusCode === 200 ? process.exit(0) : process.exit(1))"
   CMD ["node", "dist/index.js"]
   ```

   **`.dockerignore`**：
   ```
   node_modules
   npm-debug.log
   .env
   .env.local
   dist
   memora.db
   uploads
   coverage
   .git
   ```

   **`railway.json`**（可选，用于精细配置）：
   ```json
   {
     "$schema": "https://railway.app/railway.schema.json",
     "build": {
       "builder": "DOCKERFILE"
     },
     "deploy": {
       "startCommand": "node dist/index.js",
       "healthcheckPath": "/api/health",
       "healthcheckTimeout": 30,
       "restartPolicyType": "ON_FAILURE",
       "restartPolicyMaxRetries": 5
     }
   }
   ```

### 6.2 Railway 面板操作

**Step 1: 创建项目**
1. 登录 Railway Dashboard → New Project → Deploy from GitHub repo
2. 选择 `memora-backend` repo → 选择 `main` 分支

**Step 2: 添加 Postgres（或连接外部 Supabase）**
- **方案 A（Railway 内置 Postgres）**：New → Database → Add PostgreSQL
  - 自动生成 `DATABASE_URL`，但需要后续迁移到 Supabase
- **方案 B（推荐，连接 Supabase）**：
  - Variables → New Variable → `DATABASE_URL`
  - 填入 Supabase Connection Pooling URL（端口 6543）

**Step 3: 配置环境变量**
- 进入 Project → Variables → Raw Editor
- 粘贴第 3 节中的环境变量清单
- **特别注意**：
  - `USE_MOCK_AUTH=false`
  - `DATABASE_URL` 使用 Supabase Pooler URL（端口 6543）

**Step 4: 配置域名**
1. Settings → Domains → Generate Domain（获得 `xxx.up.railway.app`）
2. 或 Custom Domain → 输入 `api.yourdomain.com`
3. 在 Cloudflare 添加 CNAME：`api` → `xxx.up.railway.app`（关闭 Proxy，仅 DNS）
4. 回到 Railway 验证域名

**Step 5: 部署**
1. 点击 Deploy，等待构建完成
2. 查看 Logs 确认启动成功
3. 访问 `https://api.yourdomain.com/api/health` 验证

### 6.3 数据库 Migration

由于从 SQLite 迁移到 Postgres，需要重写数据访问层。推荐步骤：

1. **安装 pg 驱动**
   ```bash
   npm uninstall better-sqlite3
   npm install pg
   npm install -D @types/pg
   ```

2. **创建 migration 脚本** `src/scripts/migrate.ts`：
   ```typescript
   import { Pool } from 'pg';
   import fs from 'fs';
   import path from 'path';

   const pool = new Pool({
     connectionString: process.env.DATABASE_URL,
   });

   async function migrate() {
     const sql = fs.readFileSync(
       path.join(__dirname, '../../migrations/001_init.sql'),
       'utf-8'
     );
     await pool.query(sql);
     console.log('Migration completed');
     await pool.end();
   }

   migrate().catch(console.error);
   ```

3. **在 Railway 执行 migration**
   ```bash
   # Railway CLI
   railway login
   railway link
   railway run npm run migrate
   ```

---

## 7. Sentry 接入方案

### 7.1 后端接入（Node.js / Express）

```bash
npm install @sentry/node @sentry/profiling-node
```

```typescript
// src/instrument.ts
import * as Sentry from '@sentry/node';
import { nodeProfilingIntegration } from '@sentry/profiling-node';

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.SENTRY_ENVIRONMENT || 'development',
  integrations: [
    nodeProfilingIntegration(),
    Sentry.httpIntegration(),
    Sentry.expressIntegration(),
  ],
  tracesSampleRate: 1.0,
  profilesSampleRate: 1.0,
});

// src/app.ts
import './instrument';
import express from 'express';

export function createApp() {
  const app = express();

  // Sentry request handler 必须在所有其他中间件之前
  app.use(Sentry.Handlers.requestHandler());
  app.use(Sentry.Handlers.tracingHandler());

  // ... 你的路由

  // Sentry error handler 必须在所有路由和错误处理之后
  app.use(Sentry.Handlers.errorHandler());

  return app;
}
```

### 7.2 Android 接入

```kotlin
// app/build.gradle.kts
implementation("io.sentry:sentry-android:7.8.0")
```

```xml
<!-- AndroidManifest.xml -->
<application>
    <meta-data
        android:name="io.sentry.dsn"
        android:value="https://xxx@xxx.ingest.sentry.io/xxx" />
    <meta-data
        android:name="io.sentry.environment"
        android:value="production" />
    <meta-data
        android:name="io.sentry.traces.sample-rate"
        android:value="1.0" />
</application>
```

```kotlin
// MemorialApp.kt 或 MainActivity.kt
import io.sentry.android.core.SentryAndroid

SentryAndroid.init(context) { options ->
    options.dsn = BuildConfig.SENTRY_DSN
    options.environment = if (BuildConfig.DEBUG) "debug" else "production"
    options.tracesSampleRate = 1.0
    options.attachScreenshot = true
    options.attachViewHierarchy = true
}

// 手动上报错误
Sentry.captureException(exception)
Sentry.captureMessage("User deleted all data", SentryLevel.INFO)
```

---

## 8. 迁移检查清单

### 第一阶段：基础设施准备（1-2 天）
- [ ] 注册 Cloudflare 账号，购买/转移域名
- [ ] 注册 Supabase，创建 Project，获取 DATABASE_URL
- [ ] 注册 Railway，连接 GitHub Repo
- [ ] 注册 Sentry，创建 Backend + Android Project
- [ ] Cloudflare R2：创建 Bucket，配置自定义域，生成 API Key

### 第二阶段：后端改造（3-5 天）
- [ ] 替换 `better-sqlite3` → `pg` + `pg-pool`
- [ ] 重写 `src/services/db.ts` 和 `src/services/store.ts`
- [ ] 替换 Multer 本地存储 → R2 Presigned PUT URL 流式上传
- [ ] 重写图片获取逻辑：本地路径 → R2 Presigned GET URL
- [ ] 添加 Sentry SDK
- [ ] 编写并执行 Postgres Migration
- [ ] 编写 Dockerfile + railway.json
- [ ] 本地测试通过后推送到 Railway

### 第三阶段：Android 适配（1-2 天）
- [ ] 更新 API Base URL 为生产域名
- [ ] 图片加载：确认 Coil 可直接加载 Presigned URL（无需认证头）
- [ ] 添加 Sentry Android SDK
- [ ] 签名打包 Release APK，上传到 Google Play Console

### 第四阶段：生产验证（1 天）
- [ ] End-to-end 测试：注册 → 创建项目 → 上传照片 → 生成 → 购买 → 下载
- [ ] 验证 R2 Signed URL 过期刷新机制
- [ ] 验证 Sentry 错误上报
- [ ] 压测：并发上传、数据库连接池稳定性

---

## 9. 成本明细（MVP 阶段，月活 < 1,000）

| 服务 | 免费额度 | 预估使用量 | 月费用 |
|------|---------|-----------|--------|
| Railway Starter | N/A | 512MB RAM, 共享 CPU | **$5** |
| Supabase Free | 500MB 存储, 2GB 带宽 | <100MB DB, <10GB 带宽 | **$0** |
| Cloudflare R2 | 10GB 存储, 1M Class A 操作 | ~2GB 图片, ~10K 操作 | **$0** |
| Cloudflare Bandwidth | 无限 | ~50GB/月 | **$0** |
| Sentry Developer | 5K errors | ~500 errors | **$0** |
| 域名 | N/A | .app / .com | **~$0.8** |
| **总计** | | | **~$5.8/月** |

> 当月活增长到 10,000+ 时，主要升级点：
> - Railway → Pro Plan ($20+/月) 或迁移到 Fly.io
> - Supabase → Pro Plan ($25/月) 或自托管 Postgres
> - R2 → 超出 10GB 后约 $0.015/GB/月，极其便宜
> - Sentry → Team Plan ($26/月)
