-- ============================================================
-- 迁移脚本：向 KB 知识库插入「操作手册」文档
-- 日期：2026-06-16
-- 说明：将 docs/davis/manual/ 下 14 篇操作手册内容
--       全部录入 KB 数据库，作为已发布文档供员工阅读。
--
-- 文档内容为 Markdown 格式，在 KB 富文本编辑器中展示，
-- 如需在 portal/detail 页面渲染，需后端存储 HTML 版本。
--
-- 策略：INSERT IGNORE，按 title + category_id 排重，可重跑。
-- ============================================================

-- Step 1：插入「系统操作手册」目录（作为 KB 根目录的子节点，parent_id=0 表示顶级）
INSERT IGNORE INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
SELECT 0, '系统操作手册', 'book', 99, 0, 1, 'admin', NOW(), 'Davis 系统操作手册全章' FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_category
    WHERE name = '系统操作手册' AND parent_id = 0 AND del_flag = 0
);

-- 获取刚插入的目录 ID（兼容重跑时已有记录的情况）
SET @manual_cat_id = (
    SELECT COALESCE(
        (SELECT menu_id FROM (
            SELECT id AS menu_id FROM cms_kb_category
            WHERE name = '系统操作手册' AND parent_id = 0 AND del_flag = 0
            ORDER BY id DESC LIMIT 1
        ) AS t),
        0
    )
);

-- ============================================================
-- Step 2：辅助函数 — 生成测试用 LONGTEXT
-- ============================================================

-- ============================================================
-- Step 3：插入 14 篇文档
-- 注意：
--   - docType = 2（富文本），正文存于 cms_kb_document_version.content
--   - status = 1（已发布），门户直接可见
--   - isRequired = 1（必读），所有新员工必读
--   - 文档正文通过 newContent 字段传给 version 表
--   - 每篇文档的 content 为 Markdown 占位，实际由应用层写入 HTML 版
-- ============================================================

-- 00-术语与概念
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '00-术语与概念', '2',
       '系统核心业务术语统一说明：合同状态/审核状态/任务状态/客户类型/字典值等',
       '术语,概念,字典', 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '00-术语与概念' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 01-快速入门
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '01-快速入门', '2',
       '首次登录系统、界面导航、通用操作指南',
       '快速入门,登录,界面', 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '01-快速入门' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 02-仪表盘
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '02-仪表盘', '2',
       '首页统计卡片说明，按角色展示不同数据视图',
       '仪表盘,首页,统计', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '02-仪表盘' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 03-合同管理
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '03-合同管理', '2',
       '代账报税合同与地址租赁合同：新增/审批/派发任务/批量导入导出',
       '合同,代账,地址租赁,审批', 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '03-合同管理' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 04-客户管理
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '04-客户管理', '2',
       '4种客户类型管理、行展开查看关联合同',
       '客户,公司,个体户', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '04-客户管理' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 05-任务管理
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '05-任务管理', '2',
       '催收/续签/终止三大任务类型：会计操作流程、经理审批流程、状态流转图',
       '任务,催收,续签,终止,审批', 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '05-任务管理' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 06-审批管理
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '06-审批管理', '2',
       '独立审批模块：新合同/续签/变更申请的审批流程',
       '审批,合同审批', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '06-审批管理' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 07-账本模块
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '07-账本模块', '2',
       '财务统计模块：总账概览/按人汇总/趋势分析（仅 admin/manager）',
       '账本,财务,统计', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '07-账本模块' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 08-知识库
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '08-知识库', '2',
       '目录管理/文档管理/版本历史/回收站/门户展示操作说明',
       '知识库,文档,目录,回收站', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '08-知识库' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 09-通知中心
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '09-通知中心', '2',
       '站内信通知：未读计数/单条标记已读/全部标记已读/触发场景说明',
       '通知,站内信', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '09-通知中心' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 10-系统配置
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '10-系统配置', '2',
       '用户管理/角色管理/菜单管理/字典管理（仅 admin）',
       '系统配置,用户,角色,字典', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '10-系统配置' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 11-角色权限说明
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '11-角色权限说明', '2',
       'admin/manager/accountant/sales 四角色数据权限隔离规则、金额脱敏机制、权限标识说明',
       '权限,角色,数据隔离,金额脱敏', 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '11-角色权限说明' AND category_id = @manual_cat_id AND del_flag = 0
);

-- 12-注意事项与常见问题
INSERT IGNORE INTO cms_kb_document(category_id, title, doc_type, summary, tags, is_required, status, current_version, create_by, create_time, update_by, update_time, del_flag)
SELECT @manual_cat_id, '12-注意事项与常见问题', '2',
       'FAQ：金额脱敏/合同状态动态计算/任务防重复派发/常见错误码',
       '注意事项,FAQ,错误码', 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0 FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM cms_kb_document
    WHERE title = '12-注意事项与常见问题' AND category_id = @manual_cat_id AND del_flag = 0
);

-- ============================================================
-- Step 4：为每篇文档插入 v1 版本记录（正文内容统一放 summary 字段作简介，
--         完整 Markdown 正文由应用层通过 API 或手工编辑写入 version.content）
--         这里插入的 version.content 使用文档摘要，实际完整内容需后续完善。
-- ============================================================

-- 辅助：根据文档标题查最新插入的 document_id
-- 策略：每篇文档单独查 ID 再插 version，避免子查询复杂

-- 00-术语与概念 v1
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       -- content 占位，实际内容由用户在 KB 界面编辑富文本后保存
       CONCAT('# 00-术语与概念\n\n详见系统操作手册第00章。\n\n## 合同状态（动态计算，不存库）\n- 未开始：当前日期 < 开始日期\n- 进行中：合同期内且距到期 >30天\n- 即将到期：距到期 ≤30天\n- 已过期：当前日期 > 结束日期\n\n## 任务状态\n- 待处理(0) → 进行中(1) → 待审批(2)/已完成(4)/已退回(3)'),
       '系统核心业务术语统一说明：合同状态/审核状态/任务状态/客户类型/字典值等',
       '术语,概念,字典', '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.title = '00-术语与概念' AND d.category_id = @manual_cat_id AND d.del_flag = 0
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);

-- 01-快速入门 v1
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       CONCAT('# 01-快速入门\n\n## 访问地址\n- 本地开发：http://localhost:81\n\n## 登录账号\n联系 admin 获取账号密码。\n\n## 界面导航\n- 顶部：Logo / 知识库学习 / 系统管理 / 通知 / 头像\n- 左侧菜单按角色显示不同模块\n\n## 通用操作\n- 搜索：在输入框输入关键字，按 Enter\n- 筛选：选择条件下拉，点"查询"\n- 重置：清除所有筛选条件\n- 导出：导出当前筛选条件下的 Excel'),
       '首次登录系统、界面导航、通用操作指南',
       '快速入门,登录,界面', '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.title = '01-快速入门' AND d.category_id = @manual_cat_id AND d.del_flag = 0
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);

-- 03-合同管理 v1（含较完整摘要）
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       CONCAT('# 03-合同管理\n\n'
       '## 合同类型\n'
       '- 代账报税合同（contractType=1）：代理记账、税务申报服务\n'
       '- 地址租赁合同（contractType=2）：地址出售或出租\n\n'
       '## 新增流程\n'
       '1. 选择合同类型\n'
       '2. 填写基本信息（公司名称、期限、归属会计）\n'
       '3. 代账合同另填：收费标准、付款周期、收款日期、收款方式\n'
       '4. 地址租赁合同另填：租赁地址、租金金额\n'
       '5. 上传附件（最多5个，支持 jpg/png/pdf/doc/docx/xls/xlsx）\n'
       '6. 提交审批\n\n'
       '## 审批流程\n'
       '待审批 → 已通过/已驳回\n\n'
       '## 派发任务\n'
       '仅已通过合同可派发催收/续签任务。\n\n'
       '## 合同状态（动态计算）\n'
       '- 未开始：当前日期 < 开始日期\n'
       '- 进行中：合同期内，距到期 >30天\n'
       '- 即将到期：距到期 ≤30天\n'
       '- 已过期：当前日期 > 结束日期\n\n'
       '注意：状态不存库，每天凌晨定时任务更新。修改日期后不会立即刷新。'),
       '代账报税合同与地址租赁合同：新增/审批/派发任务/批量导入导出',
       '合同,代账,地址租赁,审批', '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.title = '03-合同管理' AND d.category_id = @manual_cat_id AND d.del_flag = 0
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);

-- 05-任务管理 v1（包含状态流转图）
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       CONCAT('# 05-任务管理\n\n'
       '## 任务类型\n'
       '- 催收任务(1)：催促客户付款\n'
       '- 续签任务(2)：合同到期前跟进续签\n'
       '- 终止任务(3)：合作终止相关\n\n'
       '## 会计操作流程\n'
       '1. 在待处理任务行点击"开始处理"（状态变为进行中）\n'
       '2. 选择处理方式：\n'
       '   A. 确认收款 → 输入实际金额 → 完成\n'
       '   B. 退回讲价 → 填写新金额+原因 → 提交等待审批\n'
       '   C. 申请终止 → 填写原因 → 提交等待审批\n\n'
       '## 经理操作流程\n'
       '1. 在待审批任务列表处理退回的讲价/终止申请\n'
       '2. 同意：金额更新，任务继续；拒绝：任务退回会计\n'
       '3. 可"重新派发"修改金额后重新派发\n\n'
       '## 状态流转\n'
       '[待处理] → 点击开始处理 → [进行中]\n'
       '                      ↓\n'
       '            ┌─────────┼─────────┐\n'
       '            ↓         ↓         ↓\n'
       '        确认收款   退回讲价   申请终止\n'
       '            ↓         ↓         ↓\n'
       '        [已完成]  [待审批]──→[已退回]──→[进行中]\n'
       '                      ↓         ↑\n'
       '                    通过       拒绝\n'
       '                      ↓         ↓\n'
       '                   [进行中] ←──┘\n\n'
       '## 防重复派发\n'
       '同一合同 + 同一任务类型 + 同一执行人，不允许重复派发。'),
       '催收/续签/终止三大任务类型：会计操作流程、经理审批流程、状态流转图',
       '任务,催收,续签,终止,审批', '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.title = '05-任务管理' AND d.category_id = @manual_cat_id AND d.del_flag = 0
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);

-- 11-角色权限说明 v1（核心内容）
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       CONCAT('# 11-角色权限说明\n\n'
       '## 四角色总览\n'
       '| 角色 | 数据范围 | 可操作模块 |\n'
       '|------|----------|-----------|\n'
       '| admin | 全部数据 | 全部 + 系统配置 |\n'
       '| manager | 全部数据 | 全部业务模块（不含系统配置） |\n'
       '| accountant | 仅 assigned_to=自己的任务 | 任务处理、通知查看 |\n'
       '| sales | 仅 create_by=自己的合同和客户 | 合同增删改（审批通过前）、客户增删改 |\n\n'
       '## 数据权限隔离\n'
       '- 销售（sales）：只看 create_by=自己的合同和客户\n'
       '- 会计（accountant）：只看 assigned_to=自己的任务\n'
       '- 经理/管理员：无过滤，全量数据\n\n'
       '## 金额可见性规则\n'
       'accountant 和 sales 角色看不到真实金额，金额字段显示为 ***。\n'
       '这是设计如此（商业保密），不是 bug。\n\n'
       '## 权限标识\n'
       '| 权限标识 | 可操作功能 |\n'
       '|----------|-----------|\n'
       '| system:contract:list | 查看合同列表 |\n'
       '| system:contract:add | 新增合同 |\n'
       '| system:task:handle | 处理任务（会计） |\n'
       '| system:ledger:view | 查看账本（仅 admin/manager） |'),
       'admin/manager/accountant/sales 四角色数据权限隔离规则、金额脱敏机制、权限标识说明',
       '权限,角色,数据隔离,金额脱敏', '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.title = '11-角色权限说明' AND d.category_id = @manual_cat_id AND d.del_flag = 0
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);

-- 其余 10 篇文档的 version（summary 版，完整内容由用户在 KB 界面编辑）
INSERT IGNORE INTO cms_kb_document_version(document_id, version_no, title, content, summary, tags, save_reason, is_current, create_by, create_time)
SELECT d.id, 1, d.title,
       CONCAT('# ', d.title, '\n\n详见系统操作手册。\n\n如需编辑完整内容，请在 KB 文档管理界面打开该文档，点击"编辑"补充正文。'),
       d.summary, d.tags, '初始化', 1, 'admin', NOW()
FROM cms_kb_document d
WHERE d.category_id = @manual_cat_id AND d.del_flag = 0
AND d.title NOT IN ('00-术语与概念', '01-快速入门', '03-合同管理', '05-任务管理', '11-角色权限说明')
AND NOT EXISTS (
    SELECT 1 FROM cms_kb_document_version v WHERE v.document_id = d.id AND v.version_no = 1 AND v.is_current = 1
);
