# Kimi Code 操作步骤：AI 家庭纪念合照 Android App

这份文档给 Kimi Code 使用。目标是用分阶段、可验证的方式完成一个 Android-first 的 AI 家庭纪念合照 App，而不是一次性生成整个项目。

## 0. 项目目标

开发一个面向国际用户的 Android App：

- 用户上传活动/合照底图与要补入的人物参考照片
- 云端用 `gpt-image-2` 生成 AI 家庭合照
- 免费展示低清水印预览
- 用户通过 Google Play Billing 付费
- 付费后下载高清照片
- 可选生成一段家庭纪念相册式 MP4 微动视频

第一版重点是私密家庭纪念，不做娱乐化 deepfake。

## 1. 技术决策

必须遵守：

- Android：Kotlin + Jetpack Compose
- 架构：MVVM + Repository
- 登录：Google 登录
- 图片选择：Android Photo Picker
- 支付：Google Play Billing
- 后端：Node.js / TypeScript
- 数据库：Postgres，推荐 Supabase
- 存储：Supabase Storage 或 Cloudflare R2
- 图片生成：服务端调用 OpenAI `gpt-image-2`
- 视频：服务端 Remotion + FFmpeg
- App 不直接调用 OpenAI
- App 不保存任何服务端密钥

## 2. 严格禁止

不要实现以下能力：

- 不做人物动作复现或 deepfake 视频
- 不做语音克隆
- 不做聊天机器人
- 不做完整人物动作复现
- 不做 AI 换脸视频
- 不做公开作品广场
- 不做名人/公众人物生成
- 不将生成结果描述为真实历史照片

不要把这些密钥写入 Android App：

- OpenAI API key
- Supabase service role key
- Google Play Developer API 私钥
- S3 / R2 secret key

如果需要密钥，只能通过后端环境变量读取。

## 3. 推荐目录结构

请创建或维护以下结构：

```text
memorial-app/
  android/
  backend/
  video-renderer/
  docs/
  README.md
```

说明：

- `android/`：原生 Android App
- `backend/`：Node.js / TypeScript API 和 Worker
- `video-renderer/`：Remotion + FFmpeg 视频渲染模块
- `docs/`：产品规格、API 文档、任务记录

## 4. 工作方式

每次只完成一个里程碑。

每个里程碑完成后，必须输出：

1. 修改了哪些文件
2. 实现了哪些功能
3. 如何运行或测试
4. 已运行的测试/构建结果
5. 未完成或存在风险的地方

不要修改与当前任务无关的文件。

如果发现产品规格不清楚，先提出问题，不要自行扩大范围。

## 5. 第一次启动 Prompt

把下面这段发给 Kimi Code：

```text
请先阅读 docs/product-spec.md。如果该文件不存在，请根据当前项目说明创建 docs/product-spec.md，并保持内容简洁可执行。

我要开发一个 Android-first 的 AI 纪念合照 App。请先创建项目骨架，不要实现真实 OpenAI 调用，不要加入任何真实密钥。

要求：
- 根目录包含 android/、backend/、video-renderer/、docs/
- Android 使用 Kotlin + Jetpack Compose
- 后端使用 Node.js + TypeScript
- 视频模块使用 Remotion + FFmpeg
- 创建 .env.example，不要创建真实 .env
- 创建 README.md，说明本地开发步骤
- 创建 docs/api.md，先定义 API 契约
- 创建 docs/tasks.md，记录后续里程碑

完成后请：
1. 列出修改的文件
2. 说明项目如何启动
3. 运行能运行的初始化检查
4. 不要修改与项目骨架无关的内容
```

## 6. Milestone 1：Android 基础壳

目标：

- Android App 可以启动
- 有基础导航
- 有主要页面占位
- 暂不接真实后端

发给 Kimi：

```text
请实现 Android App 的基础壳。

要求：
- 使用 Kotlin + Jetpack Compose
- 使用 Navigation Compose
- 建立 MVVM 基础结构
- 页面包括：
  - LoginScreen
  - HomeScreen
  - CreateProjectScreen
  - UploadPhotosScreen
  - StyleSelectionScreen
  - ConsentScreen
  - PreviewScreen
  - PurchaseScreen
  - DownloadScreen
  - SettingsScreen
- 使用 mock 数据
- 不接真实后端
- 不接真实支付
- 不接真实 OpenAI
- UI 文案使用英文
- 保持设计克制、温和、适合纪念类产品

完成后请运行 Android 构建检查，并列出修改文件。
```

验收标准：

- App 能启动
- 页面之间能跳转
- 无真实密钥
- 构建通过

## 7. Milestone 2：照片选择与本地校验

目标：

- 用户可以选择照片
- 本地校验图片格式和大小
- 暂不上传真实后端

发给 Kimi：

```text
请实现 Android Photo Picker 选择照片能力。

要求：
- 使用 Android Photo Picker，不请求全相册权限
- 支持选择活动底图照片和要补入的人物参考照片
- 支持 JPG、PNG、WebP
- 拒绝过小、损坏或过大的图片
- 显示选择后的缩略图
- 使用本地 mock repository 保存选择状态
- 不上传真实后端

完成后请运行相关构建或测试，并列出修改文件。
```

验收标准：

- 能选择图片
- 不请求 READ_MEDIA_IMAGES 或全相册权限
- UI 能展示缩略图
- 错误图片有提示

## 8. Milestone 3：API 契约与后端骨架

目标：

- 建立后端 API 结构
- 先用 mock 实现
- Android 可以按 API 契约对接

发给 Kimi：

```text
请实现 backend/ 的 Node.js + TypeScript API 骨架。

要求：
- 使用 Express 或 Fastify，选择一个并说明理由
- 所有配置来自环境变量
- 创建 .env.example
- 不使用真实密钥
- 实现以下 mock API：
  - POST /auth/google
  - POST /projects
  - POST /uploads/sign
  - POST /projects/:id/preview
  - GET /projects/:id
  - POST /billing/google/verify
  - POST /projects/:id/finalize
  - DELETE /projects/:id
  - POST /reports
- 更新 docs/api.md
- 返回结构要稳定，方便 Android 对接

完成后请运行类型检查或测试，并列出修改文件。
```

验收标准：

- 后端可启动
- mock API 可访问
- API 文档与实现一致
- 无真实密钥

## 9. Milestone 4：Android 对接 Mock API

目标：

- Android 端不再只用本地 mock
- 可以调用后端 mock API
- 打通项目创建、预览状态、下载状态流程

发给 Kimi：

```text
请让 Android App 对接 backend mock API。

要求：
- 使用 Retrofit 或 Ktor Client，选择一个并说明理由
- 建立 API client、DTO、Repository
- 支持创建项目
- 支持获取上传 URL
- 支持请求预览生成
- 支持轮询项目状态
- 支持展示 mock 预览图
- 支持错误状态和 loading 状态
- 不接真实 OpenAI
- 不接真实支付

完成后请运行 Android 构建检查，并列出修改文件。
```

验收标准：

- Android 可调用本地后端
- loading、success、error 状态完整
- 页面流程可走通

## 10. Milestone 5：真实图片生成服务

目标：

- 后端调用 OpenAI `gpt-image-2`
- 生成低清水印预览
- 仍然不开放高清下载

发给 Kimi：

```text
请在 backend/ 中实现真实图片预览生成。

要求：
- OpenAI API key 只能从后端环境变量读取
- Android App 不能出现 OpenAI key
- 使用 `gpt-image-2`
- 输入为用户上传照片和选择的风格
- 输出 2-4 张低清水印预览图
- 预览图必须加明显水印
- 生成任务需要有状态：
  - queued
  - running
  - succeeded
  - failed
- 失败要记录错误，但不要把敏感错误暴露给客户端
- 更新 docs/api.md 和 .env.example

完成后请运行后端测试或手动调用验证，并列出修改文件。
```

验收标准：

- 后端能生成预览图
- 预览图带水印
- Android 只能看到预览图 URL
- OpenAI key 不在客户端

## 11. Milestone 6：Google Play Billing

目标：

- Android 接入 Google Play Billing
- 后端校验 purchase token
- 购买后解锁项目权益

发给 Kimi：

```text
请实现 Google Play Billing 的基础购买流程。

要求：
- Android 使用 Google Play Billing Library
- 商品 ID：
  - memorial_photo_hd_1
  - memorial_photo_video_1
- Android 完成购买后，将 purchaseToken 发给后端
- 后端通过 Google Play Developer API 校验 purchaseToken
- 后端校验成功后绑定 project entitlement
- Android 在服务端确认成功后 acknowledge purchase
- 订单处理必须幂等
- 不要加入外部 Stripe 或网页付款入口
- 更新 docs/api.md 和 README.md 的测试说明

完成后请运行可运行的构建/测试，并列出修改文件。
```

验收标准：

- 支付流程代码完整
- 后端有 token 校验接口
- 订单不会重复解锁
- App 内没有外部支付入口

## 12. Milestone 7：高清图生成

目标：

- 付费后生成高清照片
- 未付费不能访问高清资产

发给 Kimi：

```text
请实现付费后的高清照片生成流程。

要求：
- 只有已验证订单的项目可以调用 finalize
- 使用 `gpt-image-2` 生成高清照片
- 高清资产需要访问控制
- 未付费用户不能拿到高清 URL
- 生成完成后 Android 展示下载按钮
- 下载 URL 应短期有效
- 保留 AI-generated 标识

完成后请运行后端和 Android 相关检查，并列出修改文件。
```

验收标准：

- 未付费不可访问高清图
- 付费后可下载高清图
- 状态流转正确

## 13. Milestone 8：视频动效模块

目标：

- 用 Remotion + FFmpeg 根据高清图生成 MP4
- 不做人脸动作，不做说话

发给 Kimi：

```text
请实现 video-renderer/ 模块。

要求：
- 使用 Remotion + FFmpeg
- 输入：一张 final_image.png
- 输出：final_video.mp4
- 默认规格：
  - 1080x1920
  - 30fps
  - 6秒
  - H.264 MP4
- 动效包括：
  - 淡入
  - 缓慢镜头推进
  - 轻微左右平移
  - 柔和光影
  - 轻微胶片颗粒
  - 结尾淡出
  - 角落标注 AI-generated family memory video
- 不实现眨眼、说话、表情变化或人物动作
- 提供命令行调用方式
- 提供一个 sample 输入图和输出验证说明

完成后请运行一次本地渲染测试，并列出修改文件。
```

验收标准：

- 输入图片可以生成 MP4
- MP4 可播放
- 无人物说话、表情驱动或动作复现
- 有 AI-generated 标识

## 14. Milestone 9：视频接入后端和 Android

目标：

- 购买视频套餐后生成 MP4
- Android 展示、播放和下载视频

发给 Kimi：

```text
请把 video-renderer 接入 backend 和 Android。

要求：
- 只有购买 memorial_photo_video_1 的项目才生成视频
- 后端创建 final_video generation job
- 渲染完成后上传到 storage
- Android 轮询状态并展示视频
- Android 支持播放、下载和分享 MP4
- 视频 URL 必须有访问控制或短期签名

完成后请运行相关检查，并列出修改文件。
```

验收标准：

- 购买视频套餐后能生成视频
- Android 能播放和下载
- 未购买视频套餐不能访问视频

## 15. Milestone 10：隐私、删除与举报

目标：

- 满足基础信任和 Google Play AI 内容要求

发给 Kimi：

```text
请实现隐私、删除和举报相关功能。

要求：
- Android 设置页提供数据删除入口
- 后端 DELETE /projects/:id 删除项目和资产
- Android 提供反馈/举报入口
- 后端 POST /reports 记录举报
- 所有生成结果显示 AI-generated
- 创建 privacy-policy.md 草稿
- 创建 play-console-notes.md，说明给 Google Play 审核人员的测试方式

完成后请运行相关检查，并列出修改文件。
```

验收标准：

- 用户可删除项目
- 用户可举报生成结果
- AI 标识可见
- 有隐私政策草稿
- 有 Google Play 审核说明

## 16. 最终上线前检查清单

上线前必须确认：

- Android 构建通过
- 后端测试通过
- 没有真实密钥提交到代码库
- App 内没有 Stripe 或外部付款入口
- OpenAI key 只存在后端环境变量
- Google Play purchase token 由后端校验
- 用户可删除数据
- 用户可举报 AI 内容
- 预览图有明显水印
- 高清图只有付费后可访问
- 视频只有购买视频套餐后可访问
- 所有输出标注 AI-generated
- Play Console 测试商品配置完成
- Play Console 测试账号配置完成
- 隐私政策 URL 准备完成

## 17. 给 Kimi 的通用任务尾巴

每次发任务时，都在最后附上：

```text
完成后请：
1. 列出所有修改文件
2. 简要说明实现内容
3. 运行相关测试、类型检查或构建
4. 粘贴关键结果
5. 如果有无法完成或无法验证的内容，请明确说明
6. 不要修改与当前任务无关的文件
