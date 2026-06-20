# CampusLink API 接口文档

- 基础路径：`/api`
- 鉴权方式：除登录 / 注册外，请求头携带 `Authorization: Bearer <token>`
- 统一返回体：

```json
{ "code": 200, "message": "success", "data": {} }
```

> `code`：200 成功；401 未登录 / token 失效；403 无权限；400 参数错误；500 服务异常。
> 分页返回体 `data`：`{ "records": [], "total": 0, "current": 1, "size": 10 }`

---

## 模块 1：用户模块

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录，返回 token | 否 |
| POST | `/api/auth/logout` | 退出登录 | 是 |
| GET | `/api/user/me` | 获取当前登录用户信息 | 是 |
| GET | `/api/user/{id}` | 查看用户主页 | 是 |
| PUT | `/api/user/me` | 更新个人资料 | 是 |
| GET | `/api/user/me/skills` | 我的技能标签 | 是 |
| POST | `/api/user/me/skills/{skillId}` | 添加技能标签 | 是 |
| DELETE | `/api/user/me/skills/{skillId}` | 移除技能标签 | 是 |
| GET | `/api/skill` | 技能库列表 | 是 |

### 注册 `POST /api/auth/register`
```json
{ "username": "alice", "password": "123456", "nickname": "Alice",
  "email": "a@hit.edu.cn", "college": "软件学院", "major": "软件工程", "grade": 2023 }
```

### 登录 `POST /api/auth/login`
```json
// 请求
{ "username": "alice", "password": "123456" }
// 响应 data
{ "token": "eyJ...", "user": { "id": 1, "username": "alice", "role": "STUDENT" } }
```

---

## 模块 2：竞赛模块

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/competition` | 竞赛列表（type、keyword、分页筛选） | 是 |
| GET | `/api/competition/{id}` | 竞赛详情 | 是 |
| POST | `/api/competition` | 新增竞赛 | ADMIN |
| PUT | `/api/competition/{id}` | 编辑竞赛 | ADMIN |
| DELETE | `/api/competition/{id}` | 删除竞赛 | ADMIN |

### 列表 `GET /api/competition?type=PROGRAM&keyword=蓝桥&current=1&size=10`

---

## 模块 3：队伍模块

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/team` | 队伍列表（多维筛选 + 分页） | 是 |
| GET | `/api/team/{id}` | 队伍详情（含招募岗位、成员） | 是 |
| POST | `/api/team` | 创建队伍（创建者成为队长） | 是 |
| PUT | `/api/team/{id}` | 编辑队伍 | LEADER |
| DELETE | `/api/team/{id}` | 解散队伍 | LEADER |
| GET | `/api/team/mine` | 我创建 / 加入的队伍 | 是 |

### 列表筛选参数
`competitionId`、`college`、`grade`、`skillId`、`keyword`、`status`、`current`、`size`

### 创建队伍 `POST /api/team`
```json
{ "name": "代码先锋队", "competitionId": 1, "intro": "冲刺国一",
  "totalSize": 4, "college": "软件学院",
  "recruits": [ { "position": "Vue 开发", "count": 1 }, { "position": "UI 设计", "count": 1 } ] }
```

---

## 模块 4：申请加入模块（已实现）

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/apply` | 学生提交申请（自我介绍/技能说明/主页链接） | 是 |
| GET | `/api/apply/team/{teamId}` | 队长查看本队申请 | LEADER |
| GET | `/api/apply/mine` | 我提交的申请 | 是 |
| PUT | `/api/apply/{id}/audit` | 队长审核（`{approved, reason}`），通过则加入成员，结果以站内消息推送 | LEADER |

## 模块 5：队内协作模块（已实现）

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/task/team/{teamId}` | 看板任务列表（含负责人） | 成员 |
| GET | `/api/task/team/{teamId}/stat` | 进度统计（各状态数量） | 成员 |
| POST | `/api/task` | 新建任务（可指派负责人，触发站内消息） | 成员 |
| PUT | `/api/task/{id}` | 编辑任务 | 成员 |
| PUT | `/api/task/{id}/status` | 看板拖拽：更新状态与排序（`{status, sortOrder}`） | 成员 |
| DELETE | `/api/task/{id}` | 删除任务 | 成员 |

## 模块 6：公告模块（已实现）

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/notice/team/{teamId}` | 队伍公告列表（含发布者） | 成员 |
| POST | `/api/notice` | 队长发布公告，向其余成员推送站内消息 | LEADER |
| DELETE | `/api/notice/{id}` | 队长删除公告 | LEADER |

## 模块 7-8（Day9-10 实现，先约定接口）

| 模块 | 关键接口 |
| --- | --- |
| 消息中心 | `GET /api/message`、`GET /api/message/unread/count`、`PUT /api/message/{id}/read` |
| 评价 | `POST /api/evaluation`、`GET /api/evaluation/user/{userId}` |

> 注：站内消息的**写入**能力（申请/审核/任务指派/公告推送）已在 Day6-8 随业务实现，
> 消息中心的查询 / 已读 / 未读计数接口在 Day9-10 提供。
