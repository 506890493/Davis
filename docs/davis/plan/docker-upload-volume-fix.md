# Docker 部署文件持久化修复方案

## 背景
当前 `docker-compose.yml` 中 web 服务没有挂载 volume 到 `/app/uploadPath` 目录。
每次 `docker compose up -d` 重新创建容器时，旧容器内的上传文件（头像、导入文件等）会丢失。

## 根因分析
1. **配置**：`application.yml` 中 `ruoyi.profile=/app/uploadPath`，所有上传文件存在容器内部的 `/app/uploadPath/`
2. **问题**：`docker-compose.yml` 没有将 `/app/uploadPath` 映射到宿主机目录
3. **结果**：容器重建后，所有上传文件（头像、附件、导入模板等）全部丢失

## 方案

### 修改 `docker-compose.yml`
在 web 服务的 `volumes` 配置中添加宿主机目录映射：

```yaml
services:
  web:
    # ... 现有配置 ...
    volumes:
      - ./data/upload:/app/uploadPath
```

### 影响范围
| 受影响功能 | 存储目录 | 影响 |
|-----------|---------|------|
| 头像上传 | `/app/uploadPath/avatar/` | 头像丢失 |
| 通用文件上传 | `/app/uploadPath/upload/` | 附件丢失 |
| 导入模板 | `/app/uploadPath/import/` | 模板文件丢失 |
| 下载文件 | `/app/uploadPath/download/` | 下载文件丢失 |

### 兼容性
- ✅ 向后兼容：首次部署时自动创建 `./data/upload` 目录
- ✅ 对现有部署无破坏性影响
- ✅ `.gitignore` 需要添加 `data/`

## 验证方法
1. 部署后上传头像 → 重启容器 → 验证头像仍存在
2. 部署后上传附件 → `docker compose down && docker compose up -d` → 验证文件仍可访问
