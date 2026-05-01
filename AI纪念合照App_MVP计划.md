# AI 纪念合照 Android App MVP 计划

## 1. 产品概述

本产品是一款面向国际用户的私密纪念类 Android App。它帮助成年失亲者通过上传自己与逝去亲人的照片，生成一张从未有机会拍摄的 AI 合照，并可进一步生成一段轻微动效纪念视频。

产品的核心不是娱乐化 deepfake，而是帮助用户以温和、私密、可控的方式弥补遗憾。

### 核心定位

- 产品形态：Android-first 原生 App
- 首发平台：Google Play
- 目标用户：成年失亲者
- 核心能力：AI 合照生成 + 纪念相册式微动视频
- 收费方式：免费低清水印预览，付费解锁高清照片或高清照片 + 视频
- 技术路线：Android 原生 Kotlin + 云端生成服务
- 后期扩展：iOS SwiftUI App 复用同一套后端 API

### 不做的事情

- 不做逝者说话
- 不做语音克隆
- 不做聊天机器人
- 不做完整人物动作复现
- 不做公众人物/名人营销案例
- 不做公开作品广场
- 不把生成结果包装成真实历史照片

---

## 2. MVP 功能范围

### 用户主流程

1. 用户打开 App，使用 Google 登录。
2. 创建一个纪念项目。
3. 通过 Android Photo Picker 选择照片：
   - 逝去亲人的参考照片
   - 用户本人或家庭成员照片
4. 选择合照风格：
   - 自然家庭照
   - 老照片修复风
   - 生日纪念照
   - 毕业/婚礼/节日场景
5. 勾选轻量授权声明：
   - 我有权使用上传的照片
   - 该内容仅用于私人纪念
   - 我理解生成结果为 AI 生成
6. 云端生成 2-4 张低清水印预览图。
7. 用户选择满意版本。
8. 通过 Google Play Billing 购买：
   - 高清照片
   - 高清照片 + 微动视频
9. 付费成功后，云端生成高清照片和可选 MP4 视频。
10. 用户在 App 内下载、保存或分享结果。

### 免费与付费边界

免费用户可以：

- 上传照片
- 选择风格
- 生成低清水印预览
- 删除项目和上传数据

付费用户可以：

- 下载高清无大水印照片
- 下载纪念微动视频
- 在项目历史中重新访问已购买结果

---

## 3. 推荐技术架构

### 客户端

推荐使用：

- Kotlin
- Jetpack Compose
- Navigation Compose
- MVVM
- Kotlin Coroutines / Flow
- Google Sign-In
- Google Play Billing
- Android Photo Picker

选择原生 Kotlin 的原因：

- Google Play Billing 集成最稳定
- 相册权限和 Photo Picker 体验最好
- 后台上传、下载、系统分享能力成熟
- 长期上架 Google Play 更稳
- 后期 iOS 可用 SwiftUI 单独开发，复用后端

### 后端

推荐使用：

- Supabase Auth / Postgres / Storage
- 独立 Node.js / TypeScript Worker
- Cloudflare R2 或 Supabase Storage 存储图片和视频
- 队列系统处理生成任务
- OpenAI `gpt-image-2` 生成/编辑合照
- Remotion + FFmpeg 生成微动视频

后端需要承担：

- 用户鉴权
- 上传签名 URL
- 项目和资产管理
- 调用 OpenAI 图片模型
- 生成视频
- 校验 Google Play purchase token
- 发放下载权限
- 数据删除
- 任务重试和失败记录

---

## 4. Android App 页面结构

### 主要页面

- 登录页
- 首页/项目列表页
- 创建项目页
- 照片上传页
- 风格选择页
- 授权声明页
- 生成进度页
- 预览选择页
- 购买页
- 下载结果页
- 项目历史页
- 设置页
- 数据删除页
- 反馈/举报页

### 页面说明

#### 登录页

- 使用 Google 登录
- 登录后由后端签发 App session
- 后续所有 API 请求携带 session token

#### 照片上传页

- 使用 Android Photo Picker
- 不请求全相册权限
- 用户只授权 App 访问自己选择的图片
- 上传前可做本地压缩和尺寸检查

#### 授权声明页

用户必须勾选：

- I have the right to use these photos.
- This is for private memorial use.
- I understand the result is AI-generated.

后端记录：

- 同意文本版本
- 用户 ID
- 项目 ID
- 时间
- IP
- 设备信息

#### 预览页

- 展示 2-4 张低清水印预览
- 用户可选择一个版本付费解锁
- 未付费用户不能访问高清图

#### 下载页

- 展示高清照片
- 展示 MP4 视频生成状态
- 支持保存到本地
- 支持系统分享
- 明确显示 AI-generated memorial image/video

---

## 5. API 设计

### 认证

```http
POST /auth/google
```

用途：

- 接收 Android Google 登录 token
- 后端验证 token
- 创建或查找用户
- 返回 App session

### 创建项目

```http
POST /projects
```

请求内容：

```json
{
  "title": "For Mom",
  "style": "natural_family_photo",
  "locale": "en-US"
}
```

### 获取上传 URL

```http
POST /uploads/sign
```

用途：

- 为用户选择的照片生成短期上传 URL
- 避免客户端直连存储主密钥

### 创建预览任务

```http
POST /projects/{projectId}/preview
```

用途：

- 创建低清水印预览生成任务
- 后端调用 `gpt-image-2`
- 输出 2-4 张预览图

### 查询项目状态

```http
GET /projects/{projectId}
```

返回：

- 项目信息
- 上传资产
- 预览资产
- 订单状态
- 高清资产
- 视频资产
- 生成任务状态

### 校验 Google Play 订单

```http
POST /billing/google/verify
```

请求内容：

```json
{
  "projectId": "project_123",
  "productId": "memorial_photo_video_1",
  "purchaseToken": "google_purchase_token"
}
```

用途：

- 后端向 Google Play Developer API 校验 purchase token
- 校验成功后绑定项目权益
- 返回 entitlement 状态

### 生成高清结果

```http
POST /projects/{projectId}/finalize
```

用途：

- 仅付费用户可调用
- 生成高清照片
- 如用户购买视频套餐，则生成 MP4 微动视频

### 删除项目

```http
DELETE /projects/{projectId}
```

用途：

- 删除项目记录
- 删除原图
- 删除预览图
- 删除高清图
- 删除视频文件

### 用户反馈/举报

```http
POST /reports
```

用途：

- 满足 Google Play 对 AI 生成内容反馈/举报机制的要求
- 允许用户报告不适、冒犯、错误或滥用内容

---

## 6. 数据库设计

### users

存储用户基础信息。

字段建议：

- id
- google_user_id
- email
- display_name
- country
- created_at
- last_login_at

### projects

存储纪念项目。

字段建议：

- id
- user_id
- title
- style
- status
- locale
- retention_until
- created_at
- updated_at

项目状态：

- draft
- uploaded
- preview_queued
- preview_generating
- preview_ready
- payment_pending
- paid
- final_generating
- final_ready
- failed
- deleted

### assets

存储上传图、预览图、高清图、视频等资产。

字段建议：

- id
- project_id
- user_id
- type
- storage_path
- width
- height
- mime_type
- size_bytes
- is_paid_asset
- created_at

资产类型：

- deceased_reference
- living_reference
- preview_image
- final_image
- final_video
- watermark_preview

### generation_jobs

记录生成任务。

字段建议：

- id
- project_id
- job_type
- model
- prompt
- status
- error_message
- retry_count
- estimated_cost
- started_at
- finished_at

任务类型：

- preview_image
- final_image
- final_video

### orders

记录 Google Play 订单。

字段建议：

- id
- user_id
- project_id
- product_id
- purchase_token_hash
- google_order_id
- status
- entitlement
- purchased_at
- verified_at

订单状态：

- pending
- verified
- acknowledged
- refunded
- revoked
- failed

### consent_records

记录授权声明。

字段建议：

- id
- user_id
- project_id
- consent_version
- consent_text
- ip_address
- device_info
- accepted_at

### reports

记录用户反馈或举报。

字段建议：

- id
- user_id
- project_id
- reason
- message
- status
- created_at
- resolved_at

---

## 7. 图片生成方案

### 模型

使用 OpenAI `gpt-image-2`。

用途：

- 根据用户上传照片生成自然合照
- 根据用户选择的场景和风格调整画面
- 输出预览图和高清图

### 预览生成

目标：

- 低成本
- 快速反馈
- 防止用户截图绕过付费

策略：

- 生成 2-4 张低清版本
- 添加明显水印
- 降低分辨率
- 不开放原图下载

### 高清生成

目标：

- 付费后提供高质量结果
- 尽可能稳定保留人物身份特征

策略：

- 使用更高质量设置
- 输出高分辨率图片
- 小水印或元数据标注 AI-generated
- 保留用户可下载资产

### Prompt 方向

Prompt 应强调：

- private memorial photo
- natural family portrait
- respectful, warm, realistic
- preserve facial identity from references
- avoid uncanny expressions
- do not create text artifacts
- do not imply the photo is historical evidence

---

## 8. 视频动效方案

### 推荐方案

使用服务端 Remotion + FFmpeg。

Android App 不在本地生成最终视频，只负责：

- 展示生成状态
- 播放视频
- 下载视频
- 分享视频

### 为什么不放在 Android 端生成

- 设备性能差异大
- 耗电和发热不可控
- App 退出或后台时容易失败
- 生成失败难以恢复
- 后期 iOS 无法复用
- 不利于统一水印和质量控制

### 为什么不用 AI 视频模型做第一版

- 成本更高
- 速度更慢
- 身份漂移风险更高
- 审核和伦理风险更高
- 容易变成逝者复活动作或 deepfake

第一版视频应保持在纪念相册式动效，不做人物复活。

### 视频规格

建议默认：

- 格式：MP4
- 编码：H.264
- 分辨率：1080x1920 竖版
- 可选：1920x1080 横版
- 帧率：30fps
- 时长：6 秒
- 音频：默认无音频，后续可加授权音乐

### 动效内容

第一版模板：

- 淡入
- 缓慢镜头推进
- 轻微左右平移
- 柔和光影扫过
- 轻微胶片颗粒
- 轻微边缘虚化
- 结尾淡出
- 角落标注 AI-generated memorial video

### 生成流程

```text
final_image.png
  -> Remotion composition
  -> camera animation / overlay / watermark
  -> FFmpeg render
  -> final_video.mp4
  -> upload to storage
  -> app receives downloadable URL
```

### 后续增强

V1.5 可以增加：

- 人物/背景分割
- 2.5D parallax
- 背景轻微反向移动
- 前景人物轻微景深变化

但这不是 MVP 必需功能。

---

## 9. Google Play Billing 设计

### 推荐商品

#### memorial_photo_hd_1

解锁一个项目的高清照片。

建议价格：

- USD 9.99

#### memorial_photo_video_1

解锁一个项目的高清照片 + 微动视频。

建议价格：

- USD 14.99

实际价格应在 Play Console 中按地区本地化。

### 购买流程

1. 用户选择预览图。
2. App 调起 Google Play Billing。
3. 用户完成购买。
4. App 将 purchase token 发给后端。
5. 后端调用 Google Play Developer API 校验。
6. 校验成功后，后端创建 entitlement。
7. App acknowledge purchase。
8. 后端启动高清图和视频生成任务。
9. App 轮询项目状态直到结果完成。

### 注意事项

- App 内数字内容应使用 Google Play Billing。
- 不在 App 内引导用户跳转 Stripe 或网页付款。
- 后端必须校验 purchase token，不能只相信客户端。
- 订单处理必须幂等，避免重复发放或重复扣减额度。

---

## 10. 合规与信任边界

### 最低规则

第一版采用轻量规则：

- 用户声明拥有照片使用权
- 用户确认仅作私人纪念
- 用户理解结果为 AI 生成
- 输出标注 AI-generated
- 提供项目删除入口
- 提供反馈/举报入口

### 内容边界

禁止或限制：

- 名人/公众人物生成
- 冒充真实历史照片
- 色情或成人内容
- 仇恨、骚扰、欺骗性内容
- 未经同意的现实人物冒充
- 儿童目标市场
- 逝者说话、语音克隆、聊天复现

### 数据保留

建议默认：

- 原始上传图保留 30 天
- 生成结果保留 30 天
- 付费资产可在项目历史中保留更久，但用户可删除
- 用户删除项目后，数据库和存储资产均删除

---

## 11. 测试计划

### Android 测试

- Google 登录成功/失败
- session 过期后重新登录
- Photo Picker 上传 JPG/PNG/WebP
- 拒绝损坏图片、过小图片、超大图片
- 弱网下上传失败重试
- App 进入后台后恢复项目状态
- 生成任务轮询
- 下载图片和视频
- 系统分享
- 删除项目

### 支付测试

- Google Play Billing 测试商品购买
- 购买取消
- 购买失败
- 重复 purchase token
- webhook/校验重复回调
- 退款后权限撤销
- 未付费用户不能访问高清资产

### 后端测试

- 上传签名 URL 过期
- 用户不能访问他人项目
- 生成任务失败重试
- 幂等订单校验
- 删除项目后资产不可访问
- 存储 URL 过期

### 视频测试

- 竖版视频正确输出
- 横版视频正确输出
- MP4 可在 Android 播放
- 水印位置不遮挡人物脸部
- 视频时长稳定
- 弱网下下载可重试

### 审核测试

- App 内无外部支付引导
- 所有 AI 结果有标识
- 有隐私政策入口
- 有数据删除入口
- 有举报/反馈入口
- Play Console 提供测试账号和测试购买说明

---

## 12. 开发里程碑

### Milestone 1：产品骨架

- Android 原生项目初始化
- Google 登录
- 项目创建
- Photo Picker 上传
- 后端项目和资产表

### Milestone 2：预览生成

- 上传照片到云存储
- 后端调用 `gpt-image-2`
- 生成低清水印预览
- App 展示预览图

### Milestone 3：内购解锁

- Google Play Billing 集成
- 后端校验 purchase token
- entitlement 发放
- 付费后生成高清图

### Milestone 4：视频动效

- Remotion 模板
- FFmpeg 渲染服务
- 生成 MP4
- App 播放和下载

### Milestone 5：合规与上线准备

- 隐私政策
- 数据删除
- 举报反馈
- Play Console 配置
- 测试账号
- 内测发布

---

## 13. 关键决策总结

- 第一版做 Android App，不做 Web-first。
- 开发技术选原生 Kotlin，不选 React Native/Flutter。
- 后端必须存在，不能让客户端直连 OpenAI API。
- 图片生成用 `gpt-image-2`。
- 视频动效用 Remotion + FFmpeg 服务端生成。
- 支付用 Google Play Billing，不在 App 内使用 Stripe。
- 免费预览采用低清水印图。
- 付费解锁高清照片和 MP4 视频。
- 产品定位为私密纪念，不做逝者复活、说话、聊天或完整 deepfake 视频。

