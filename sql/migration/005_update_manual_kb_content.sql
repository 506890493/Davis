-- ============================================================
-- 迁移脚本：把 12 篇操作手册的 Markdown 转 HTML 后 UPDATE 到 KB
-- 日期：2026-07-03
-- 说明：消除 KbManualInitializer 特殊处理后，手册的 HTML 内容直接嵌入 SQL。
--       运营后续通过 KB 文档管理界面编辑，标准 update 流程产生新版本。
--       此脚本只针对 003_seed_manual_kb.sql 已插入的 12 篇文档做内容更新，
--       文档元数据（id/title/category_id 等）保持不变。
-- 幂等：UPDATE WHERE title IN (...) WHERE content LIKE '%详见系统操作手册%'，
--       只更新占位文字，不会覆盖运营编辑过的真实内容。
-- ============================================================

-- 占位标记（用于幂等判断）
SET @placeholder := '%详见系统操作手册%';

-- 00-术语与概念.md → 00-术语与概念
-- preview: <h1>00 | 术语与概念</h1> <blockquote> <p>阅读本章，理解系统中统一使用的术语和业务概念，再开始操作各模块。</p> </blockquote> <hr /> <h2>1. 合同相关术语</h2> <h3>合同类...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '00-术语与概念'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>00 | 术语与概念</h1>
<blockquote>
<p>阅读本章，理解系统中统一使用的术语和业务概念，再开始操作各模块。</p>
</blockquote>
<hr />
<h2>1. 合同相关术语</h2>
<h3>合同类型</h3>
<table>
<thead>
<tr>
<th>术语</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>代账报税合同</td>
<td><code>contractType = 1</code></td>
<td>代理记账、税务申报类服务合同</td>
</tr>
<tr>
<td>地址租赁合同</td>
<td><code>contractType = 2</code></td>
<td>地址出售或出租类合同</td>
</tr>
</tbody>
</table>
<h3>合同状态（动态计算，不存库）</h3>
<p>合同状态根据合同期限与当前日期实时计算，<strong>不存储在数据库中</strong>，每天定时任务更新。</p>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>计算规则</th>
</tr>
</thead>
<tbody>
<tr>
<td>未开始</td>
<td><code>0</code></td>
<td>当前日期 &lt; 合同开始日期</td>
</tr>
<tr>
<td>进行中</td>
<td><code>1</code></td>
<td>当前日期在合同期内，且距离到期 &gt; 30 天</td>
</tr>
<tr>
<td>即将到期</td>
<td><code>2</code></td>
<td>当前日期距离合同结束日期 ≤ 30 天</td>
</tr>
<tr>
<td>已过期</td>
<td><code>3</code></td>
<td>当前日期 &gt; 合同结束日期</td>
</tr>
</tbody>
</table>
<blockquote>
<p><strong>注意</strong>：修改合同日期后，状态不会立即刷新，需等待定时任务（每天凌晨）重新计算。</p>
</blockquote>
<h3>审核状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>待审批</td>
<td><code>0</code></td>
<td>合同提交后等待审批</td>
</tr>
<tr>
<td>已通过</td>
<td><code>1</code></td>
<td>审批通过，合同生效</td>
</tr>
<tr>
<td>已驳回</td>
<td><code>2</code></td>
<td>审批被拒绝，需修改后重新提交</td>
</tr>
</tbody>
</table>
<h3>催交状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>正常</td>
<td><code>0</code></td>
<td>收款正常</td>
</tr>
<tr>
<td>催收中</td>
<td><code>1</code></td>
<td>已有催收任务进行中</td>
</tr>
<tr>
<td>已完成</td>
<td><code>2</code></td>
<td>催收任务已完成</td>
</tr>
</tbody>
</table>
<hr />
<h2>2. 客户相关术语</h2>
<h3>客户类型</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>公司</td>
<td><code>1</code></td>
<td>有限责任公司、股份有限公司等</td>
</tr>
<tr>
<td>个体户</td>
<td><code>2</code></td>
<td>个体工商户</td>
</tr>
<tr>
<td>合伙企业</td>
<td><code>3</code></td>
<td>合伙企业</td>
</tr>
<tr>
<td>民办非</td>
<td><code>4</code></td>
<td>民办非企业单位</td>
</tr>
</tbody>
</table>
<h3>客户状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>正常</td>
<td><code>0</code></td>
<td>正常合作中</td>
</tr>
<tr>
<td>非正常</td>
<td><code>1</code></td>
<td>已停止合作或其他原因</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 任务相关术语</h2>
<h3>任务类型</h3>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>催收任务</td>
<td><code>1</code></td>
<td>催促客户付款</td>
</tr>
<tr>
<td>续签任务</td>
<td><code>2</code></td>
<td>合同到期前的续签跟进</td>
</tr>
<tr>
<td>终止任务</td>
<td><code>3</code></td>
<td>合作终止相关</td>
</tr>
</tbody>
</table>
<h3>任务状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>待处理</td>
<td><code>0</code></td>
<td>任务刚分配，还未开始处理</td>
</tr>
<tr>
<td>进行中</td>
<td><code>1</code></td>
<td>会计已开始处理</td>
</tr>
<tr>
<td>待审批</td>
<td><code>2</code></td>
<td>会计退回讲价或申请终止，等待经理审批</td>
</tr>
<tr>
<td>已退回</td>
<td><code>3</code></td>
<td>经理拒绝或会计主动退回</td>
</tr>
<tr>
<td>已完成</td>
<td><code>4</code></td>
<td>任务已完成（收款/续签/终止均完成）</td>
</tr>
</tbody>
</table>
<h3>优先级</h3>
<table>
<thead>
<tr>
<th>优先级</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>高</td>
<td><code>1</code></td>
<td>紧急处理</td>
</tr>
<tr>
<td>中</td>
<td><code>2</code></td>
<td>正常处理</td>
</tr>
<tr>
<td>低</td>
<td><code>3</code></td>
<td>可延后</td>
</tr>
</tbody>
</table>
<hr />
<h2>4. 字典说明</h2>
<p>系统中使用数据字典（<code>sys_dict_data</code>）管理的选项值：</p>
<table>
<thead>
<tr>
<th>字典类型</th>
<th>用途</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>cms_contract_type</code></td>
<td>合同类型（代账/地址租赁）</td>
</tr>
<tr>
<td><code>cms_contract_status</code></td>
<td>合同状态（未开始/进行中/即将到期/已过期）</td>
</tr>
<tr>
<td><code>cms_audit_status</code></td>
<td>审核状态（待审批/通过/驳回）</td>
</tr>
<tr>
<td><code>cms_reminder_status</code></td>
<td>催交状态（正常/催收中/已完成）</td>
</tr>
<tr>
<td><code>cms_pay_cycle</code></td>
<td>付款周期（月付/季付/半年付/年付）</td>
</tr>
<tr>
<td><code>cms_pay_method</code></td>
<td>收款方式（微信/支付宝/公户转账）</td>
</tr>
<tr>
<td><code>cms_tax_type</code></td>
<td>纳税人类别（一般纳税人/小规模纳税人）</td>
</tr>
<tr>
<td><code>cms_task_type</code></td>
<td>任务类型（催收/续签/终止）</td>
</tr>
<tr>
<td><code>cms_task_priority</code></td>
<td>任务优先级（高/中/低）</td>
</tr>
<tr>
<td><code>cms_task_status</code></td>
<td>任务状态</td>
</tr>
<tr>
<td><code>cms_approval_type</code></td>
<td>审批类型（新合同/续费/变更）</td>
</tr>
<tr>
<td><code>cms_kb_doc_type</code></td>
<td>文档类型（文件/富文本）</td>
</tr>
<tr>
<td><code>cms_kb_status</code></td>
<td>文档状态（草稿/已发布/已下架）</td>
</tr>
<tr>
<td><code>cms_kb_required</code></td>
<td>是否必读（否/是）</td>
</tr>
</tbody>
</table>
<hr />
<h2>5. 金额相关</h2>
<h3>金额字段</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>所属模块</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>amount</code></td>
<td>合同（代账）</td>
<td>收费标准/合同金额</td>
</tr>
<tr>
<td><code>actualAmount</code></td>
<td>合同（代账）</td>
<td>实际收款金额</td>
</tr>
<tr>
<td><code>originalAmount</code></td>
<td>任务</td>
<td>原合同金额</td>
</tr>
<tr>
<td><code>currentAmount</code></td>
<td>任务</td>
<td>当前协商金额</td>
</tr>
<tr>
<td><code>adjustAmount</code></td>
<td>任务</td>
<td>调整后价格</td>
</tr>
</tbody>
</table>
<h3>金额可见性</h3>
<p><strong>accountant（会计）</strong> 和 <strong>sales（销售）</strong> 角色看不到真实金额，金额字段显示为 <code>***</code>。</p>
<p>这是设计如此，不是 bug。如需查看，请联系 admin 账号登录验证。</p>
<p>详见 <a href="./11-角色权限说明.md">11-角色权限说明</a>。</p>
<hr />
<h2>6. 知识库相关术语</h2>
<h3>文档类型</h3>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>文件</td>
<td><code>docType = 1</code></td>
<td>上传文件（PDF/Word/Excel/图片等）</td>
</tr>
<tr>
<td>富文本</td>
<td><code>docType = 2</code></td>
<td>在线编辑的图文文章</td>
</tr>
</tbody>
</table>
<h3>文档状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>草稿</td>
<td><code>0</code></td>
<td>未发布，仅作者可见</td>
</tr>
<tr>
<td>已发布</td>
<td><code>1</code></td>
<td>正式发布，阅读端可见</td>
</tr>
<tr>
<td>已下架</td>
<td><code>2</code></td>
<td>已下架，不再展示</td>
</tr>
</tbody>
</table>
<h3>回收站规则</h3>
<ul>
<li>删除文档进入回收站（<code>del_flag = 1</code>）</li>
<li><strong>30 天后</strong>自动物理删除（永久删除）</li>
<li>删除后 30 天内可在回收站恢复</li>
</ul>
',
    v.update_time = NOW();

-- 01-快速入门.md → 01-快速入门
-- preview: <h1>01 | 快速入门</h1> <blockquote> <p>首次使用系统？本章帮你快速上手。</p> </blockquote> <hr /> <h2>1. 访问系统</h2> <h3>环境说明</h3> <table> <the...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '01-快速入门'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>01 | 快速入门</h1>
<blockquote>
<p>首次使用系统？本章帮你快速上手。</p>
</blockquote>
<hr />
<h2>1. 访问系统</h2>
<h3>环境说明</h3>
<table>
<thead>
<tr>
<th>环境</th>
<th>地址</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>正式环境</td>
<td><code>http://your-domain/</code></td>
<td>生产环境域名</td>
</tr>
<tr>
<td>本地开发</td>
<td><code>http://localhost:81</code></td>
<td>开发调试地址</td>
</tr>
</tbody>
</table>
<h3>登录账号</h3>
<p>联系系统管理员（admin）获取账号密码。默认测试账号：</p>
<table>
<thead>
<tr>
<th>角色</th>
<th>账号</th>
<th>密码</th>
</tr>
</thead>
<tbody>
<tr>
<td>管理员</td>
<td>admin</td>
<td>自行配置</td>
</tr>
<tr>
<td>经理</td>
<td>manager</td>
<td>自行配置</td>
</tr>
<tr>
<td>会计</td>
<td>accountant</td>
<td>自行配置</td>
</tr>
<tr>
<td>销售</td>
<td>sales</td>
<td>自行配置</td>
</tr>
</tbody>
</table>
<blockquote>
<p><strong>注意</strong>：首次登录后请修改密码。密码通过 cookie 存储登录态，不是 localStorage。</p>
</blockquote>
<hr />
<h2>2. 界面导航</h2>
<h3>顶部导航栏</h3>
<pre><code>┌──────────────────────────────────────────────────────────┐
│ Logo  知识库学习  系统管理  ▼  搜索框  通知(3)  头像 ▼   │
└──────────────────────────────────────────────────────────┘
</code></pre>
<ul>
<li><strong>Logo</strong>：点击回到首页（仪表盘）</li>
<li><strong>知识库学习</strong>：知识库门户首页</li>
<li><strong>系统管理</strong>：下拉菜单，包含合同/客户/任务/账本等业务模块</li>
<li><strong>通知图标</strong>：显示未读通知数量，点击进入通知中心</li>
<li><strong>头像下拉</strong>：个人中心、退出登录</li>
</ul>
<h3>左侧菜单（系统管理下拉后）</h3>
<p>根据当前账号角色，显示以下菜单：</p>
<table>
<thead>
<tr>
<th>菜单</th>
<th>功能</th>
</tr>
</thead>
<tbody>
<tr>
<td>合同管理</td>
<td>代账合同 + 地址租赁合同列表</td>
</tr>
<tr>
<td>客户管理</td>
<td>客户信息管理</td>
</tr>
<tr>
<td>任务管理</td>
<td>催收/续签/终止任务处理</td>
</tr>
<tr>
<td>审批管理</td>
<td>独立审批申请处理</td>
</tr>
<tr>
<td>账本模块</td>
<td>财务统计（仅 admin/manager）</td>
</tr>
<tr>
<td>知识库</td>
<td>目录/文档/版本/回收站管理</td>
</tr>
<tr>
<td>系统配置</td>
<td>用户/角色/菜单/字典（仅 admin）</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 通用操作</h2>
<h3>表格操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>搜索</td>
<td>在输入框输入关键字，按 Enter 或点&quot;查询&quot;</td>
</tr>
<tr>
<td>筛选</td>
<td>选择下拉条件，点&quot;查询&quot;</td>
</tr>
<tr>
<td>重置</td>
<td>点&quot;重置&quot;清除所有筛选条件，恢复默认列表</td>
</tr>
<tr>
<td>翻页</td>
<td>表格底部Pagination组件，点击页码或&quot;上下页&quot;</td>
</tr>
<tr>
<td>导出</td>
<td>点&quot;导出&quot;按钮，导出当前筛选条件下的Excel</td>
</tr>
<tr>
<td>批量选择</td>
<td>勾选左侧复选框，支持批量删除等操作</td>
</tr>
</tbody>
</table>
<h3>新增操作</h3>
<ol>
<li>在列表页点击<strong>新增</strong>按钮（蓝色）</li>
<li>填写表单，必填字段旁有红色 <code>*</code> 标记</li>
<li>点<strong>确定</strong>或<strong>保存</strong>提交</li>
<li>成功后自动关闭弹窗，列表刷新</li>
</ol>
<h3>编辑操作</h3>
<ol>
<li>在列表行点击<strong>编辑</strong>按钮</li>
<li>修改表单内容</li>
<li>点<strong>确定</strong>保存</li>
</ol>
<h3>删除操作</h3>
<ol>
<li>点击<strong>删除</strong>按钮（红色文字）</li>
<li>弹出确认框，显示删除对象信息</li>
<li>点<strong>确定</strong>执行删除</li>
</ol>
<hr />
<h2>4. 表单通用规则</h2>
<h3>必填字段</h3>
<p>表单中带 <strong>红色 <code>*</code></strong> 的字段为必填，不填无法提交。</p>
<h3>日期选择</h3>
<ul>
<li>点击输入框弹出日历选择器</li>
<li>支持手动输入 <code>YYYY-MM-DD</code> 格式</li>
</ul>
<h3>金额输入</h3>
<ul>
<li>保留两位小数</li>
<li>accountant/sales 角色金额字段显示 <code>***</code>，不可见也无法输入</li>
</ul>
<h3>附件上传</h3>
<ol>
<li>点击&quot;点击上传&quot;按钮</li>
<li>选择文件（支持 jpg/png/pdf/doc/docx/xls/xlsx）</li>
<li>文件上传到服务器后，显示已上传文件名</li>
<li>点&quot;预览&quot;可查看（图片）或下载（其他格式）</li>
</ol>
<h3>富文本编辑（撰写文章）</h3>
<ol>
<li>在文档管理点击&quot;撰写文章&quot;</li>
<li>在富文本编辑器中输入正文</li>
<li>支持插入图片（base64 内嵌）</li>
<li>点保存</li>
</ol>
<hr />
<h2>5. 状态标签颜色速查</h2>
<table>
<thead>
<tr>
<th>状态</th>
<th>标签颜色</th>
</tr>
</thead>
<tbody>
<tr>
<td>草稿 / 待处理 / 待审批</td>
<td>灰色</td>
</tr>
<tr>
<td>进行中 / 已发布 / 已通过</td>
<td>绿色</td>
</tr>
<tr>
<td>即将到期 / 催收中 / 进行中</td>
<td>橙色</td>
</tr>
<tr>
<td>已过期 / 已下架 / 已驳回 / 已拒绝</td>
<td>红色</td>
</tr>
<tr>
<td>必读 ★</td>
<td>橙色边框</td>
</tr>
</tbody>
</table>
<hr />
<h2>6. 常见问题</h2>
<table>
<thead>
<tr>
<th>问题</th>
<th>解答</th>
</tr>
</thead>
<tbody>
<tr>
<td>为什么看不到金额？</td>
<td>当前账号是 accountant 或 sales，金额按设计显示为 <code>***</code></td>
</tr>
<tr>
<td>为什么有些按钮看不到？</td>
<td>当前账号没有该操作权限，联系 admin 分配</td>
</tr>
<tr>
<td>修改数据后没生效？</td>
<td>合同状态等字段由定时任务计算，不是实时更新</td>
</tr>
<tr>
<td>怎么退出登录？</td>
<td>点右上角头像 → 退出登录</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 02-仪表盘.md → 02-仪表盘
-- preview: <h1>02 | 仪表盘</h1> <blockquote> <p>登录系统后第一个看到的页面，展示当前角色最关心的数据摘要。</p> </blockquote> <hr /> <h2>访问方式</h2> <p>登录成功后自动进入仪表盘（首...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '02-仪表盘'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>02 | 仪表盘</h1>
<blockquote>
<p>登录系统后第一个看到的页面，展示当前角色最关心的数据摘要。</p>
</blockquote>
<hr />
<h2>访问方式</h2>
<p>登录成功后自动进入仪表盘（首页），也可随时点击顶部 <strong>Logo</strong> 返回。</p>
<hr />
<h2>1. 各角色视图</h2>
<h3>admin / manager（管理员/经理）</h3>
<p>仪表盘展示全部统计卡片：</p>
<h4>合同统计区</h4>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>本月应完成金额</td>
<td>本月到期合同的标准收费总额</td>
</tr>
<tr>
<td>本月实际完成金额</td>
<td>本月已完成任务的实际收款总额</td>
</tr>
<tr>
<td>合同总数</td>
<td>系统内全部有效合同数量</td>
</tr>
<tr>
<td>本月到期合同数</td>
<td>开始日期 ~ 结束日期覆盖当月的合同数</td>
</tr>
</tbody>
</table>
<h4>客户统计区</h4>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>客户总数</td>
<td>系统内全部客户数量</td>
</tr>
<tr>
<td>代账类型客户数</td>
<td><code>customerType = 1</code>（公司）的客户数</td>
</tr>
<tr>
<td>租赁类型客户数</td>
<td><code>customerType = 2</code>（个体户等）的客户数</td>
</tr>
</tbody>
</table>
<h4>必读文档 Banner</h4>
<p>知识库中标记为&quot;必读&quot;的文档，以 Banner 形式展示在仪表盘顶部，提醒新员工阅读。</p>
<h4>快捷操作</h4>
<ul>
<li>点合同编号 → 跳转合同详情</li>
<li>点客户名称 → 跳转客户详情</li>
</ul>
<hr />
<h3>accountant（会计）</h3>
<p>仪表盘展示个人收款相关统计：</p>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>本月应收金额</td>
<td>当前会计名下所有待收款合同金额合计</td>
</tr>
<tr>
<td>本月已收金额</td>
<td>当前会计已完成的任务实际收款合计</td>
</tr>
<tr>
<td>代账收费应完成家数</td>
<td>本月需完成代账服务的客户数</td>
</tr>
<tr>
<td>代账收费已完成家数</td>
<td>本月已完成代账服务的客户数</td>
</tr>
</tbody>
</table>
<blockquote>
<p>注意：会计的仪表盘不显示具体金额数字（金额字段对 accountant 显示为 <code>***</code>），但&quot;家数&quot;统计是可见的。</p>
</blockquote>
<hr />
<h3>sales（销售）</h3>
<p>仪表盘展示客户开发相关统计：</p>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>我的客户总数</td>
<td>当前销售创建的客户总数</td>
</tr>
<tr>
<td>本月新增客户数</td>
<td>本月新创建的客户数量</td>
</tr>
<tr>
<td>我的客户列表</td>
<td>最近创建的客户卡片展示</td>
</tr>
</tbody>
</table>
<hr />
<h2>2. 知识库必读 Banner</h2>
<p>当知识库中存在标记为&quot;必读&quot;的已发布文档时，仪表盘顶部会显示橙色 Banner：</p>
<pre><code>📌 新员工必读  [文档标题]  点击阅读 →
</code></pre>
<p>点击后跳转到知识库门户的必读专区。</p>
<hr />
<h2>3. 底部操作记录（最近活动）</h2>
<p>仪表盘底部展示最近的操作日志或任务动态，按时间倒序排列。</p>
<hr />
<h2>4. 刷新数据</h2>
<p>仪表盘数据由后端实时汇总计算，每次进入页面自动刷新。也可手动点击刷新按钮（如果有）更新数据。</p>
<hr />
<h2>5. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>金额显示 <code>***</code></td>
<td>accountant/sales 角色按设计如此，见 <a href="./11-角色权限说明.md">11-角色权限说明</a></td>
</tr>
<tr>
<td>合同状态不是实时的</td>
<td>合同状态由定时任务每天凌晨计算，不存库，见 <a href="./12-注意事项与常见问题.md">12-注意事项与常见问题</a></td>
</tr>
<tr>
<td>看不到某些统计卡片</td>
<td>当前账号可能不是 admin/manager，部分卡片不展示</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 03-合同管理.md → 03-合同管理
-- preview: <h1>03 | 合同管理</h1> <blockquote> <p>系统的核心业务模块，包含代账报税合同和地址租赁合同两种类型。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <h3>合同类型</h3...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '03-合同管理'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>03 | 合同管理</h1>
<blockquote>
<p>系统的核心业务模块，包含代账报税合同和地址租赁合同两种类型。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<h3>合同类型</h3>
<table>
<thead>
<tr>
<th>类型</th>
<th>说明</th>
<th>典型场景</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>代账报税合同</strong></td>
<td>代理记账、税务申报服务</td>
<td>客户委托代账公司处理税务</td>
</tr>
<tr>
<td><strong>地址租赁合同</strong></td>
<td>地址出售或出租</td>
<td>写字间、办公位出租</td>
</tr>
</tbody>
</table>
<h3>入口</h3>
<pre><code>系统管理 → 合同管理
</code></pre>
<p>或在首页仪表盘点击合同相关统计卡片直接跳转。</p>
<hr />
<h2>2. 列表页说明</h2>
<h3>URL 说明</h3>
<table>
<thead>
<tr>
<th>URL</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>/system/contract</code></td>
<td>全部合同（已通过）</td>
</tr>
<tr>
<td><code>/contract/accounting</code></td>
<td>代账合同列表（已通过）</td>
</tr>
<tr>
<td><code>/contract/rent</code></td>
<td>地址租赁合同列表（已通过）</td>
</tr>
<tr>
<td><code>/contract/pending</code></td>
<td>待审批合同列表</td>
</tr>
<tr>
<td><code>/contract/rejected</code></td>
<td>已驳回合同列表</td>
</tr>
</tbody>
</table>
<h3>表格字段（代账合同）</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>合同编号</td>
<td>自动生成或手动填写</td>
</tr>
<tr>
<td>公司名称</td>
<td>合同关联的客户名称</td>
</tr>
<tr>
<td>法人</td>
<td>公司法人姓名</td>
</tr>
<tr>
<td>成立日期</td>
<td>公司成立日期</td>
</tr>
<tr>
<td>收费标准</td>
<td>合同金额（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>收款日期</td>
<td>约定收款日期</td>
</tr>
<tr>
<td>税务类型</td>
<td>一般纳税人 / 小规模纳税人</td>
</tr>
<tr>
<td>付款周期</td>
<td>月付 / 季付 / 半年付 / 年付</td>
</tr>
<tr>
<td>会计</td>
<td>归属会计姓名</td>
</tr>
<tr>
<td>合同状态</td>
<td>动态计算：未开始 / 进行中 / 即将到期 / 已过期</td>
</tr>
<tr>
<td>审核状态</td>
<td>待审批 / 已通过 / 已驳回</td>
</tr>
<tr>
<td>催交状态</td>
<td>正常 / 催收中 / 已完成</td>
</tr>
</tbody>
</table>
<h3>表格字段（地址租赁合同）</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>合同编号</td>
<td>自动生成或手动填写</td>
</tr>
<tr>
<td>公司名称</td>
<td>合同名称</td>
</tr>
<tr>
<td>租赁地址</td>
<td>地址省市区 + 详细地址</td>
</tr>
<tr>
<td>租金金额</td>
<td>年租金（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>押金金额</td>
<td>押金（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>是否已出租</td>
<td>是 / 否</td>
</tr>
<tr>
<td>利润</td>
<td>收益（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>联系人</td>
<td>联系人姓名</td>
</tr>
<tr>
<td>联系电话</td>
<td>联系电话</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 新增合同</h2>
<h3>入口</h3>
<p>列表页点击 <strong>&quot;新增&quot;</strong> 按钮（蓝色）。</p>
<h3>必填字段</h3>
<p>所有合同必填：</p>
<ul>
<li>合同类型（代账 / 地址租赁）</li>
<li>公司名称</li>
<li>开始日期</li>
<li>结束日期</li>
<li>归属会计</li>
</ul>
<p>代账合同额外必填：</p>
<ul>
<li>收费标准</li>
<li>付款周期</li>
<li>收款日期</li>
<li>收款方式（微信 / 支付宝 / 公户转账）</li>
</ul>
<p>地址租赁合同额外必填：</p>
<ul>
<li>租赁地址（省市区选择）</li>
<li>详细地址</li>
<li>租金金额</li>
</ul>
<h3>操作步骤</h3>
<ol>
<li>选择<strong>合同类型</strong></li>
<li>填写基本信息</li>
<li>选择<strong>归属会计</strong>（下拉，仅 manager + accountant 角色用户可选）</li>
<li>选择关联客户（下拉，支持新增客户）</li>
<li>上传附件（可选，最多 5 个文件，支持 jpg/png/pdf/doc/docx/xls/xlsx）</li>
<li>点<strong>提交审批</strong></li>
</ol>
<hr />
<h2>4. 批量导入 / 导出</h2>
<h3>导出</h3>
<ol>
<li>在列表页设置筛选条件</li>
<li>点<strong>导出</strong>按钮</li>
<li>浏览器下载 <code>contract_{时间戳}.xlsx</code></li>
</ol>
<h3>批量导入</h3>
<ol>
<li>点<strong>导入</strong>按钮</li>
<li>下载代账合同模板 或 地址出售合同模板</li>
<li>按模板格式填写数据</li>
<li>上传文件</li>
<li>勾选&quot;更新已有数据&quot;可覆盖已存在记录</li>
<li>点<strong>开始导入</strong></li>
</ol>
<blockquote>
<p>模板中灰色隐藏列是另一合同类型的字段，不要填写。</p>
</blockquote>
<hr />
<h2>5. 合同状态机（动态计算）</h2>
<p>合同状态<strong>不存库</strong>，每天凌晨定时任务根据当前日期和合同期限重新计算。</p>
<h3>计算规则</h3>
<pre><code>当前日期 &lt; 开始日期          → 未开始
当前日期 &gt;= 开始日期
  且当前日期 &lt;= 结束日期 - 30天 → 进行中
  且当前日期 &gt; 结束日期 - 30天 → 即将到期
当前日期 &gt; 结束日期            → 已过期
</code></pre>
<h3>注意事项</h3>
<ul>
<li>修改合同日期后，状态<strong>不会立即刷新</strong></li>
<li>通常在当天凌晨定时任务跑完后生效</li>
<li>如急需更新状态，需联系 admin 重跑定时任务</li>
</ul>
<hr />
<h2>6. 派发催收任务</h2>
<p>只有<strong>已通过</strong>（<code>auditStatus = 1</code>）的合同可以派发任务。</p>
<h3>操作步骤</h3>
<ol>
<li>在已通过合同列表，点击行尾**&quot;催收&quot;**按钮</li>
<li>弹出派发任务对话框：
<ul>
<li>任务类型：催收任务 / 续签任务</li>
<li>分配会计：选择执行会计</li>
<li>截止日期：选择截止时间</li>
<li>备注：填写说明</li>
</ul>
</li>
<li>点<strong>确定</strong>创建任务</li>
</ol>
<blockquote>
<p>任务标题自动生成为 <code>&quot;催收任务: {合同名}&quot;</code> 或 <code>&quot;续签任务: {合同名}&quot;</code></p>
</blockquote>
<hr />
<h2>7. 查看合同详情</h2>
<p>点击列表行任意位置（或点<strong>查看</strong>按钮）进入详情页：</p>
<ul>
<li><strong>基础信息</strong>：编号、名称、类型、审核状态、合同状态、期限、会计等</li>
<li><strong>客户信息</strong>：关联的客户名称（点击跳转客户详情）</li>
<li><strong>代账信息 / 地址出租信息</strong>：按类型动态显示</li>
<li><strong>附件展示</strong>：图片可直接预览，PDF 弹窗预览，其他格式下载</li>
<li><strong>审批记录</strong>：点击&quot;审批记录&quot;查看审批流水</li>
</ul>
<hr />
<h2>8. 删除合同</h2>
<ol>
<li>勾选要删除的合同（支持批量）</li>
<li>点<strong>删除</strong>按钮（红色）</li>
<li>确认删除后执行逻辑删除（数据不物理删除）</li>
</ol>
<blockquote>
<p>已在执行的合同删除需谨慎，删除后任务不受影响但合同不可恢复。</p>
</blockquote>
<hr />
<h2>9. 角色权限</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>可见范围</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>全部</td>
<td>全部</td>
</tr>
<tr>
<td>manager</td>
<td>全部</td>
<td>新增/编辑/审批/派发/删除</td>
</tr>
<tr>
<td>sales</td>
<td>仅自己创建的</td>
<td>新增（审批通过前可编辑）、删除</td>
</tr>
<tr>
<td>accountant</td>
<td>全部（金额 <code>***</code>）</td>
<td>仅查看，无编辑权限</td>
</tr>
</tbody>
</table>
<hr />
<h2>10. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>金额显示 <code>***</code></td>
<td>accountant/sales 角色按设计如此</td>
</tr>
<tr>
<td>合同状态不实时</td>
<td>动态计算，定时任务更新</td>
</tr>
<tr>
<td>无法删除有任务的合同</td>
<td>合同删除后任务仍存在，不建议删除进行中的合同</td>
</tr>
<tr>
<td>附件上传失败</td>
<td>文件大小上限 200MB，格式需为 jpg/png/pdf/doc/docx/xls/xlsx</td>
</tr>
<tr>
<td>代账合同看不到税务类型列</td>
<td>accountant/sales 角色部分字段不显示</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 04-客户管理.md → 04-客户管理
-- preview: <h1>04 | 客户管理</h1> <blockquote> <p>管理所有客户信息，支持查看每个客户关联的代账合同和地址合同。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>客户是合同的主体，...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '04-客户管理'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>04 | 客户管理</h1>
<blockquote>
<p>管理所有客户信息，支持查看每个客户关联的代账合同和地址合同。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>客户是合同的主体，一个客户可以关联多条代账合同和地址合同。</p>
<h3>入口</h3>
<pre><code>系统管理 → 客户管理
</code></pre>
<hr />
<h2>2. 客户类型</h2>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>公司</td>
<td><code>1</code></td>
<td>有限责任公司、股份有限公司</td>
</tr>
<tr>
<td>个体户</td>
<td><code>2</code></td>
<td>个体工商户</td>
</tr>
<tr>
<td>合伙企业</td>
<td><code>3</code></td>
<td>合伙企业</td>
</tr>
<tr>
<td>民办非</td>
<td><code>4</code></td>
<td>民办非企业单位</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 列表页说明</h2>
<h3>表格字段</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>客户名称</td>
<td>公司/个体户全称，可点击跳转详情</td>
</tr>
<tr>
<td>客户类型</td>
<td>公司 / 个体户 / 合伙企业 / 民办非</td>
</tr>
<tr>
<td>联系人</td>
<td>主要联系人姓名</td>
</tr>
<tr>
<td>联系电话</td>
<td>主要联系电话</td>
</tr>
<tr>
<td>邮箱</td>
<td>联系邮箱</td>
</tr>
<tr>
<td>归属销售</td>
<td>创建该客户的销售人员</td>
</tr>
<tr>
<td>状态</td>
<td>正常 / 非正常</td>
</tr>
<tr>
<td>创建时间</td>
<td>记录创建日期</td>
</tr>
</tbody>
</table>
<h3>行展开</h3>
<p>点击表格行左侧 <strong>展开按钮</strong>，可查看该客户的：</p>
<ul>
<li><strong>代账合同列表</strong>：所有 <code>contractType = 1</code> 的关联合同</li>
<li><strong>地址合同列表</strong>：所有 <code>contractType = 2</code> 的关联合同</li>
</ul>
<p>点击合同编号可直接跳转到合同详情页。</p>
<hr />
<h2>4. 新增客户</h2>
<h3>入口</h3>
<p>列表页点击 <strong>&quot;新增&quot;</strong> 按钮。</p>
<h3>必填字段</h3>
<ul>
<li>客户名称</li>
<li>客户类型（公司/个体户/合伙企业/民办非）</li>
</ul>
<h3>选填字段</h3>
<ul>
<li>联系人</li>
<li>联系电话</li>
<li>邮箱</li>
<li>地址</li>
<li>备注</li>
</ul>
<h3>操作步骤</h3>
<ol>
<li>填写客户名称</li>
<li>选择客户类型</li>
<li>填写其他信息</li>
<li>点<strong>确定</strong>保存</li>
</ol>
<blockquote>
<p>新增客户后，可在该客户详情页直接新增关联合同。</p>
</blockquote>
<hr />
<h2>5. 编辑客户</h2>
<ol>
<li>在列表页点击客户名称，或点击行尾<strong>编辑</strong>按钮</li>
<li>修改信息</li>
<li>点<strong>确定</strong>保存</li>
</ol>
<hr />
<h2>6. 删除客户</h2>
<h3>删除条件</h3>
<ul>
<li>客户<strong>没有任何关联合同</strong>才可删除</li>
<li>若有关联合同，系统拒绝删除并提示</li>
</ul>
<h3>操作步骤</h3>
<ol>
<li>点击行尾<strong>删除</strong>按钮</li>
<li>确认删除</li>
</ol>
<blockquote>
<p>删除前请先删除客户的关联合同，或将客户状态改为&quot;非正常&quot;而非物理删除。</p>
</blockquote>
<hr />
<h2>7. 客户详情页</h2>
<h3>入口</h3>
<p>点击列表中的客户名称。</p>
<h3>内容结构</h3>
<ul>
<li><strong>基本信息</strong>：客户名称、类型、联系人、电话、邮箱、地址、状态等</li>
<li><strong>代账合同 Tab</strong>：该客户的代账合同列表</li>
<li><strong>地址合同 Tab</strong>：该客户的地址租赁合同列表</li>
</ul>
<h3>快捷操作</h3>
<ul>
<li>点合同编号 → 跳转合同详情</li>
<li>点<strong>新增合同</strong> → 进入新增合同页（自动带入客户信息）</li>
</ul>
<hr />
<h2>8. 角色权限</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>可见范围</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>全部客户</td>
<td>新增/编辑/删除</td>
</tr>
<tr>
<td>manager</td>
<td>全部客户</td>
<td>新增/编辑/删除</td>
</tr>
<tr>
<td>sales</td>
<td>仅自己创建的</td>
<td>新增/编辑（仅自己创建的）</td>
</tr>
<tr>
<td>accountant</td>
<td>全部（金额 <code>***</code>）</td>
<td>仅查看，无编辑权限</td>
</tr>
</tbody>
</table>
<hr />
<h2>9. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>找不到某客户</td>
<td>可能是 sales 角色，只能看到自己创建的客户</td>
</tr>
<tr>
<td>删除时提示有关联合同</td>
<td>需先删除该客户的全部合同，或将状态改为&quot;非正常&quot;</td>
</tr>
<tr>
<td>客户类型显示为数字</td>
<td>不可能发生，前端已做翻译</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 05-任务管理.md → 05-任务管理
-- preview: <h1>05 | 任务管理</h1> <blockquote> <p>处理催收、续签、终止三大类任务，是合同管理的下游闭环环节。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>任务由合同派发产生，...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '05-任务管理'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>05 | 任务管理</h1>
<blockquote>
<p>处理催收、续签、终止三大类任务，是合同管理的下游闭环环节。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>任务由合同派发产生，一个合同可以派发多个任务。任务分配给会计处理，形成「经理派发 → 会计处理 → 经理审批」的闭环。</p>
<h3>任务类型</h3>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>催收任务</td>
<td><code>1</code></td>
<td>催促客户付款</td>
</tr>
<tr>
<td>续签任务</td>
<td><code>2</code></td>
<td>合同到期前跟进续签</td>
</tr>
<tr>
<td>终止任务</td>
<td><code>3</code></td>
<td>合作终止相关</td>
</tr>
</tbody>
</table>
<h3>入口</h3>
<pre><code>系统管理 → 任务管理
</code></pre>
<hr />
<h2>2. 列表页说明</h2>
<h3>表格字段</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>任务编号</td>
<td>系统自动生成</td>
</tr>
<tr>
<td>任务标题</td>
<td>自动格式：<code>催收任务: {合同名}</code></td>
</tr>
<tr>
<td>关联合同</td>
<td>任务来源合同，可点击跳转</td>
</tr>
<tr>
<td>任务类型</td>
<td>催收 / 续签 / 终止</td>
</tr>
<tr>
<td>优先级</td>
<td>高 / 中 / 低</td>
</tr>
<tr>
<td>原金额</td>
<td>合同原始金额（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>当前协商金额</td>
<td>会计调整后的金额（accountant/sales 显示 <code>***</code>）</td>
</tr>
<tr>
<td>执行人</td>
<td>任务分配的会计</td>
</tr>
<tr>
<td>截止日期</td>
<td>任务截止时间</td>
</tr>
<tr>
<td>状态</td>
<td>待处理 / 进行中 / 待审批 / 已退回 / 已完成</td>
</tr>
<tr>
<td>创建时间</td>
<td>任务创建时间</td>
</tr>
</tbody>
</table>
<h3>状态标签颜色</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>标签颜色</th>
</tr>
</thead>
<tbody>
<tr>
<td>待处理</td>
<td>灰色</td>
</tr>
<tr>
<td>进行中</td>
<td>蓝色</td>
</tr>
<tr>
<td>待审批</td>
<td>橙色</td>
</tr>
<tr>
<td>已退回</td>
<td>红色</td>
</tr>
<tr>
<td>已完成</td>
<td>绿色</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 会计操作流程</h2>
<blockquote>
<p>适用于 accountant 角色。</p>
</blockquote>
<h3>步骤一：开始处理</h3>
<p>在待处理任务行点击**&quot;开始处理&quot;**按钮。</p>
<ul>
<li>状态从 <code>待处理(0)</code> 变为 <code>进行中(1)</code></li>
<li>执行人确认接单</li>
</ul>
<h3>步骤二：选择处理方式</h3>
<h4>方式 A：确认收款 ✅</h4>
<p>客户确认付款，在任务行点击**&quot;确认收款&quot;**：</p>
<ol>
<li>弹出对话框，输入<strong>实际收款金额</strong></li>
<li>填写<strong>收款备注</strong>（可选）</li>
<li>点<strong>确定</strong></li>
<li>状态变为 <code>已完成(4)</code></li>
<li>系统弹出提示：是否生成续签合同？</li>
</ol>
<blockquote>
<p>选择&quot;是&quot; → 进入新增合同页，关联原合同作为续签</p>
</blockquote>
<h4>方式 B：退回讲价 🔄</h4>
<p>客户要求调整金额，点击**&quot;退回&quot;**：</p>
<ol>
<li>弹出对话框，填写：
<ul>
<li>调整后金额（协商后的新金额）</li>
<li>退回原因（必填）</li>
<li>附件（可选）</li>
</ul>
</li>
<li>点<strong>确定提交</strong></li>
<li>状态变为 <code>待审批(2)</code>，等待经理处理</li>
</ol>
<h4>方式 C：申请终止 🛑</h4>
<p>客户决定终止合作，点击**&quot;申请终止&quot;**：</p>
<ol>
<li>弹出对话框，填写终止原因（必填）</li>
<li>点<strong>确定提交</strong></li>
<li>状态变为 <code>待审批(2)</code>，等待经理审批</li>
</ol>
<hr />
<h2>4. 经理 / 管理员操作流程</h2>
<blockquote>
<p>适用于 manager / admin 角色。</p>
</blockquote>
<h3>步骤一：处理待审批任务</h3>
<p>进入<strong>待审批</strong>任务列表（筛选 <code>status = 待审批</code>）：</p>
<h4>审批讲价申请</h4>
<p>经理看到会计退回的讲价申请：</p>
<ul>
<li>查看原金额 vs 会计提议的新金额</li>
<li>选择**&quot;同意&quot;** 或 <strong>&quot;拒绝&quot;</strong>
<ul>
<li>同意：金额更新，任务变为 <code>进行中(1)</code>，会计可继续处理</li>
<li>拒绝：任务退回会计，状态变为 <code>已退回(3)</code></li>
</ul>
</li>
</ul>
<h4>审批终止申请</h4>
<p>经理看到会计发起的终止申请：</p>
<ul>
<li>选择**&quot;同意&quot;** 或 <strong>&quot;拒绝&quot;</strong>
<ul>
<li>同意：任务完成，合同合作关系终止</li>
<li>拒绝：任务退回会计，状态变为 <code>已退回(3)</code></li>
</ul>
</li>
</ul>
<h3>步骤二：重新派发任务</h3>
<p>当任务被会计退回讲价后，经理可修改金额后重新派发：</p>
<ol>
<li>在已退回任务行点击**&quot;重新派发&quot;**</li>
<li>修改协商金额</li>
<li>点<strong>确定</strong>，任务重新变为 <code>进行中(1)</code></li>
</ol>
<hr />
<h2>5. 操作历史记录</h2>
<p>每个任务都有操作日志，记录所有状态变更：</p>
<ul>
<li>任务创建</li>
<li>开始处理</li>
<li>确认收款 / 退回讲价 / 申请终止</li>
<li>经理审批（同意/拒绝）</li>
<li>重新派发</li>
</ul>
<p>点击任务行&quot;查看详情&quot; → 查看完整操作流水。</p>
<hr />
<h2>6. 任务状态流转图</h2>
<pre><code>[待处理(0)]
    ↓ 点击&quot;开始处理&quot;
[进行中(1)]
    ↓
  ┌──────────┼──────────┐
  ↓          ↓          ↓
确认收款   退回讲价   申请终止
  ↓          ↓          ↓
[已完成]  [待审批]   [待审批]
 (4)       (2)        (2)
            ↓          ↓
         经理审批    经理审批
          ↓    ↓      ↓    ↓
        同意  拒绝  同意  拒绝
          ↓    ↓    ↓    ↓
       [进行中] [已退回(3)] → [进行中]
                   ↑
              重新派发
</code></pre>
<hr />
<h2>7. 防止重复派发规则</h2>
<p>同一合同 + 同一任务类型 + 同一执行人 的组合不允许重复派发。</p>
<p>系统会在派发时检查：是否存在<strong>进行中</strong>（<code>status = 1</code>）的同类任务，有则拒绝派发。</p>
<hr />
<h2>8. 角色权限</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>可见范围</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>全部任务</td>
<td>全部操作</td>
</tr>
<tr>
<td>manager</td>
<td>全部任务</td>
<td>审批/重新派发/审批终止</td>
</tr>
<tr>
<td>accountant</td>
<td>仅分配给自己的</td>
<td>开始处理/确认收款/退回讲价/申请终止</td>
</tr>
<tr>
<td>sales</td>
<td>无任务权限</td>
<td>—</td>
</tr>
</tbody>
</table>
<hr />
<h2>9. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>看不到任务</td>
<td>accountant 只能看到 <code>assigned_to</code> 为自己的任务；sales 看不到任务</td>
</tr>
<tr>
<td>金额显示 <code>***</code></td>
<td>accountant/sales 角色按设计如此</td>
</tr>
<tr>
<td>退回讲价后无法再操作</td>
<td>需等待经理审批后才能继续</td>
</tr>
<tr>
<td>无法派发任务</td>
<td>同一合同+同类型+同执行人已存在进行中任务，需先完成或取消现有任务</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 06-审批管理.md → 06-审批管理
-- preview: <h1>06 | 审批管理</h1> <blockquote> <p>独立的合同审批模块，与任务内的审批子流程是两条独立的线。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>审批管理处理<stro...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '06-审批管理'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>06 | 审批管理</h1>
<blockquote>
<p>独立的合同审批模块，与任务内的审批子流程是两条独立的线。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>审批管理处理<strong>新合同、续签、变更</strong>的申请，与任务模块中的&quot;讲价审批&quot;和&quot;终止审批&quot;是<strong>两条独立的审批流</strong>。</p>
<table>
<thead>
<tr>
<th>审批类型</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新合同</td>
<td>新建代账合同或地址租赁合同后提交审批</td>
</tr>
<tr>
<td>续签</td>
<td>合同到期前申请续签</td>
</tr>
<tr>
<td>变更</td>
<td>合同内容（金额、期限等）变更后申请审批</td>
</tr>
</tbody>
</table>
<h3>入口</h3>
<pre><code>系统管理 → 审批管理
</code></pre>
<hr />
<h2>2. 列表页说明</h2>
<h3>表格字段</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>申请编号</td>
<td>系统自动生成，格式如 <code>APPLY{年}{流水号}</code></td>
</tr>
<tr>
<td>审批类型</td>
<td>新合同 / 续签 / 变更</td>
</tr>
<tr>
<td>申请人</td>
<td>提交申请的用户</td>
</tr>
<tr>
<td>关联合同</td>
<td>关联的合同编号，可点击跳转</td>
</tr>
<tr>
<td>关联任务</td>
<td>关联的任务编号（可选）</td>
</tr>
<tr>
<td>申请时间</td>
<td>提交申请的时间</td>
</tr>
<tr>
<td>审批状态</td>
<td>待审批 / 已通过 / 已驳回</td>
</tr>
<tr>
<td>审批意见</td>
<td>经理/管理员填写的原因</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 审批流程</h2>
<h3>提交申请（会计 / 销售）</h3>
<ol>
<li>在合同详情页点击<strong>提交审批</strong></li>
<li>选择审批类型（新合同/续签/变更）</li>
<li>确认提交</li>
<li>申请进入审批管理列表，等待处理</li>
</ol>
<h3>审批（经理 / 管理员）</h3>
<ol>
<li>进入<strong>审批管理</strong>列表</li>
<li>点击申请行查看详情（申请内容快照）</li>
<li>选择<strong>通过</strong>或<strong>驳回</strong>
<ul>
<li>驳回需填写原因</li>
</ul>
</li>
<li>提交审批结果</li>
</ol>
<h3>审批通过后</h3>
<ul>
<li>合同 <code>audit_status</code> 更新为 <code>1</code>（已通过）</li>
<li>合同状态变为可计算（进入&quot;进行中&quot;等动态状态）</li>
</ul>
<h3>审批驳回后</h3>
<ul>
<li>合同 <code>audit_status</code> 更新为 <code>2</code>（已驳回）</li>
<li>申请人收到通知（站内信）</li>
<li>申请人修改合同后重新提交审批</li>
</ul>
<hr />
<h2>4. 审批状态</h2>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>待审批</td>
<td><code>0</code></td>
<td>等待审批</td>
</tr>
<tr>
<td>已通过</td>
<td><code>1</code></td>
<td>审批通过，合同生效</td>
</tr>
<tr>
<td>已驳回</td>
<td><code>2</code></td>
<td>审批被拒绝</td>
</tr>
</tbody>
</table>
<hr />
<h2>5. 角色权限</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>全部审批操作</td>
</tr>
<tr>
<td>manager</td>
<td>全部审批操作</td>
</tr>
<tr>
<td>accountant</td>
<td>提交审批申请（看任务中的审批），不能操作独立审批模块</td>
</tr>
<tr>
<td>sales</td>
<td>提交审批申请（自己创建的合同）</td>
</tr>
</tbody>
</table>
<blockquote>
<p>独立审批模块（<code>/system/approval</code>）仅 admin / manager 可操作。accountant/sales 在任务详情页完成的审批是任务内审批，不是这个模块。</p>
</blockquote>
<hr />
<h2>6. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>看不到审批管理菜单</td>
<td>当前账号不是 admin/manager，该菜单不显示</td>
</tr>
<tr>
<td>审批意见填写后显示在哪里</td>
<td>审批意见保存在审批记录中，可在详情页查看</td>
</tr>
<tr>
<td>审批通过后合同没变化</td>
<td>确认合同 <code>audit_status</code> 是否已更新为 <code>1</code>，如未更新请联系 admin</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 07-账本模块.md → 07-账本模块
-- preview: <h1>07 | 账本模块</h1> <blockquote> <p>财务统计模块，提供合同金额汇总、按人统计和趋势分析。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>账本模块是财务数据的汇总展...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '07-账本模块'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>07 | 账本模块</h1>
<blockquote>
<p>财务统计模块，提供合同金额汇总、按人统计和趋势分析。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>账本模块是财务数据的汇总展示，供管理者掌握整体经营状况。</p>
<h3>入口</h3>
<pre><code>系统管理 → 账本模块
</code></pre>
<h3>访问权限</h3>
<p><strong>仅 admin / manager 可访问</strong>。</p>
<p>accountant / sales 角色点击该菜单会提示&quot;无权限&quot;或菜单根本不显示。</p>
<hr />
<h2>2. 三个 Tab 页</h2>
<h3>Tab 1：总账概览</h3>
<p>展示合同金额的整体汇总：</p>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>合同总数</td>
<td>系统内全部有效合同数量</td>
</tr>
<tr>
<td>代账合同金额总计</td>
<td>所有代账合同（<code>contractType = 1</code>）的金额合计</td>
</tr>
<tr>
<td>地址租赁金额总计</td>
<td>所有地址租赁合同（<code>contractType = 2</code>）的金额合计</td>
</tr>
<tr>
<td>即将到期合同数</td>
<td>距离到期 ≤ 30 天的合同数量</td>
</tr>
</tbody>
</table>
<p>下方附带<strong>合同列表</strong>，展示各合同的名称、类型、金额、签订日期、到期日期。</p>
<blockquote>
<p>注意：accountant/sales 角色如果通过某种方式访问此页面，金额显示为 <code>***</code>。</p>
</blockquote>
<hr />
<h3>Tab 2：按人汇总</h3>
<p>按会计人员统计：</p>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>会计姓名</td>
<td>系统用户名</td>
</tr>
<tr>
<td>负责合同数</td>
<td>该会计名下的合同总数</td>
</tr>
<tr>
<td>合同总金额</td>
<td>该会计名下所有合同金额合计</td>
</tr>
</tbody>
</table>
<p>便于管理者了解各会计的工作量和业绩。</p>
<hr />
<h3>Tab 3：趋势分析</h3>
<p>ECharts 折线图，展示近 12 个月的收入趋势：</p>
<table>
<thead>
<tr>
<th>指标</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>代账收入趋势线</td>
<td>每月代账合同收款金额折线</td>
</tr>
<tr>
<td>租赁收入趋势线</td>
<td>每月地址租赁收款金额折线</td>
</tr>
</tbody>
</table>
<p>横轴为月份，纵轴为金额（单位：元）。</p>
<hr />
<h2>3. 导出报表</h2>
<p>在&quot;总账概览&quot;或&quot;按人汇总&quot; Tab 页，点击<strong>导出</strong>按钮，可将当前视图数据导出为 Excel 文件。</p>
<hr />
<h2>4. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>看不到账本模块菜单</td>
<td>当前账号是 accountant/sales，该菜单按权限不显示</td>
</tr>
<tr>
<td>金额显示 <code>***</code></td>
<td>按角色权限，accountant/sales 不可见真实金额</td>
</tr>
<tr>
<td>趋势图是空的</td>
<td>当月尚无数据，或历史数据尚未录入</td>
</tr>
<tr>
<td>导出的 Excel 数字不对</td>
<td>导出的金额已经是 accountant/sales 的脱敏状态，无法还原</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 08-知识库.md → 08-知识库
-- preview: <h1>08 | 知识库</h1> <blockquote> <p>内部文档管理与学习平台，支持目录管理、文档编辑、版本历史和门户展示。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>知识库为内部...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '08-知识库'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>08 | 知识库</h1>
<blockquote>
<p>内部文档管理与学习平台，支持目录管理、文档编辑、版本历史和门户展示。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>知识库为内部学习材料管理工具，管理员可以创建分类、上传文件或撰写文章，员工通过门户浏览和学习。</p>
<h3>入口</h3>
<pre><code>系统管理 → 知识库
         → 文档管理
         → 目录管理
         → 回收站
</code></pre>
<p>或从顶部菜单**&quot;知识库学习&quot;**进入门户展示页。</p>
<hr />
<h2>2. 模块组成</h2>
<pre><code>知识库
  ├── 文档管理    # 新增/编辑/删除文档
  ├── 目录管理    # 管理分类目录树
  ├── 回收站      # 已删除文档恢复/永久删除
  └── 知识库学习（门户）  # 员工浏览界面
       ├── 分类导航
       ├── 必读 Banner
       └── 文档列表
</code></pre>
<hr />
<h2>3. 目录管理</h2>
<h3>功能说明</h3>
<p>以树形结构管理文档分类。</p>
<h3>默认分类（系统预置）</h3>
<table>
<thead>
<tr>
<th>目录名称</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>系统操作手册</td>
<td>Davis 系统使用手册</td>
</tr>
<tr>
<td>代账知识</td>
<td>代账业务知识</td>
</tr>
<tr>
<td>会计知识</td>
<td>财务/会计类知识</td>
</tr>
<tr>
<td>工商知识</td>
<td>公司注册/变更/注销类知识</td>
</tr>
</tbody>
</table>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增根目录</td>
<td>点击&quot;新增根目录&quot;</td>
</tr>
<tr>
<td>新增子目录</td>
<td>点击目录右侧&quot;新增&quot;按钮</td>
</tr>
<tr>
<td>编辑目录</td>
<td>点击&quot;编辑&quot;，修改名称/图标/排序/是否必读</td>
</tr>
<tr>
<td>删除目录</td>
<td>点击&quot;删除&quot;，目录下有文档时不允许删除</td>
</tr>
<tr>
<td>设置必读</td>
<td>点击&quot;设必读&quot;，该目录下文档会在必读 Banner 展示</td>
</tr>
</tbody>
</table>
<blockquote>
<p>删除目录前需先清空目录下的文档。</p>
</blockquote>
<hr />
<h2>4. 文档管理</h2>
<h3>文档类型</h3>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>文件</td>
<td><code>docType = 1</code></td>
<td>上传文件（PDF/Word/Excel/图片等）</td>
</tr>
<tr>
<td>富文本</td>
<td><code>docType = 2</code></td>
<td>在线编辑的图文文章</td>
</tr>
</tbody>
</table>
<h3>新增文档</h3>
<h4>方式一：上传文件</h4>
<ol>
<li>点击&quot;上传文件&quot;按钮</li>
<li>填写标题、选择目录</li>
<li>上传附件文件（最多 5 个）</li>
<li>设置是否&quot;新员工必读&quot;</li>
<li>点<strong>保存</strong></li>
</ol>
<h4>方式二：撰写文章</h4>
<ol>
<li>点击&quot;撰写文章&quot;按钮</li>
<li>填写标题、选择目录</li>
<li>在富文本编辑器中编写正文（支持插入图片）</li>
<li>设置是否&quot;新员工必读&quot;</li>
<li>点<strong>保存</strong></li>
</ol>
<h3>文档状态</h3>
<table>
<thead>
<tr>
<th>状态</th>
<th>值</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>草稿</td>
<td><code>0</code></td>
<td>未发布，仅作者可见</td>
</tr>
<tr>
<td>已发布</td>
<td><code>1</code></td>
<td>正式发布，门户可见</td>
</tr>
<tr>
<td>已下架</td>
<td><code>2</code></td>
<td>已下架，不再展示</td>
</tr>
</tbody>
</table>
<blockquote>
<p>新增文档默认是<strong>草稿状态</strong>，需手动<strong>发布</strong>后门户才可见。</p>
</blockquote>
<h3>发布 / 下架</h3>
<ul>
<li>草稿文档点击**&quot;发布&quot;**按钮 → 状态变为 <code>已发布</code></li>
<li>已发布文档点击**&quot;下架&quot;**按钮 → 状态变为 <code>已下架</code></li>
</ul>
<h3>版本历史</h3>
<p>每个文档支持版本管理：</p>
<ol>
<li>点击文档行&quot;<strong>历史</strong>&quot;按钮</li>
<li>弹出版本历史对话框</li>
<li>左侧时间线展示所有版本</li>
<li>点击版本可查看该版本内容</li>
<li>如需回滚，点击版本右侧&quot;<strong>回滚</strong>&quot;按钮</li>
</ol>
<blockquote>
<p>回滚会创建新版本，不会覆盖历史版本。</p>
</blockquote>
<hr />
<h2>5. 回收站</h2>
<h3>功能说明</h3>
<p>存储被删除的文档，<strong>30 天后自动物理删除</strong>。</p>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>恢复</td>
<td>将文档从回收站恢复（状态变为草稿），恢复后需重新发布</td>
</tr>
<tr>
<td>永久删除</td>
<td>立即物理删除（不可恢复）</td>
</tr>
</tbody>
</table>
<blockquote>
<p>删除时系统会提示：&quot;删除后将进入回收站，30 天后自动清理&quot;。</p>
</blockquote>
<hr />
<h2>6. 知识库门户（员工浏览）</h2>
<h3>入口</h3>
<p>顶部菜单 <strong>&quot;知识库学习&quot;</strong> 或直接访问 <code>/view</code>。</p>
<h3>页面结构</h3>
<pre><code>┌─────────────────────────────────────────┐
│  必读 Banner（新员工必读文档横向滚动）   │
├───────────┬─────────────────────────────┤
│  分类导航   │  文档列表                    │
│  （树形）   │  标题/类型/摘要/发布时间    │
│           │  [查看] 按钮                 │
└───────────┴─────────────────────────────┘
</code></pre>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>浏览文档</td>
<td>点击文档标题或&quot;查看&quot;按钮，进入详情页</td>
</tr>
<tr>
<td>切换分类</td>
<td>点击左侧目录树节点，筛选该分类下文档</td>
</tr>
<tr>
<td>查看必读</td>
<td>点击右上角&quot;📌 新员工必读&quot;链接，筛选必读文档</td>
</tr>
<tr>
<td>搜索文档</td>
<td>在顶部搜索框输入关键字，模糊搜索标题</td>
</tr>
</tbody>
</table>
<hr />
<h2>7. 角色权限</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>可见范围</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>全部</td>
<td>全部（目录/文档/版本/回收站）</td>
</tr>
<tr>
<td>manager</td>
<td>全部</td>
<td>全部（目录/文档/版本/回收站）</td>
</tr>
<tr>
<td>accountant</td>
<td>门户可见</td>
<td>仅浏览，不能管理</td>
</tr>
<tr>
<td>sales</td>
<td>门户可见</td>
<td>仅浏览，不能管理</td>
</tr>
</tbody>
</table>
<hr />
<h2>8. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增文档后门户看不到</td>
<td>默认是草稿状态，需点击&quot;发布&quot;后才在门户可见</td>
</tr>
<tr>
<td>回收站文档消失了</td>
<td>超过 30 天被定时任务自动物理删除</td>
</tr>
<tr>
<td>富文本图片上传失败</td>
<td>图片以 base64 内嵌，单张大小建议控制在 500KB 以内</td>
</tr>
<tr>
<td>删除目录失败</td>
<td>目录下还有文档，需先删除或移动文档到其他目录</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 09-通知中心.md → 09-通知中心
-- preview: <h1>09 | 通知中心</h1> <blockquote> <p>站内信通知，接收来自任务派发、合同审批等业务操作的消息推送。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>通知中心是系统内的...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '09-通知中心'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>09 | 通知中心</h1>
<blockquote>
<p>站内信通知，接收来自任务派发、合同审批等业务操作的消息推送。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>通知中心是系统内的消息通知中心，推送来自各业务模块的事件提醒。</p>
<h3>入口</h3>
<p>点击顶部导航栏右侧<strong>通知图标</strong>（显示未读数量红点）。</p>
<hr />
<h2>2. 通知类型</h2>
<table>
<thead>
<tr>
<th>类型</th>
<th>值</th>
<th>触发场景</th>
</tr>
</thead>
<tbody>
<tr>
<td>任务通知</td>
<td><code>task</code></td>
<td>任务被派发给你的会计</td>
</tr>
<tr>
<td>审批通知</td>
<td><code>approval</code></td>
<td>合同/任务审批结果（通过/驳回）</td>
</tr>
<tr>
<td>系统通知</td>
<td><code>system</code></td>
<td>系统公告、管理员推送</td>
</tr>
<tr>
<td>到期提醒</td>
<td><code>reminder</code></td>
<td>合同即将到期提醒</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 列表页说明</h2>
<h3>表格字段</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>类型</td>
<td>通知类型图标和文字标签</td>
</tr>
<tr>
<td>标题</td>
<td>通知标题</td>
</tr>
<tr>
<td>内容</td>
<td>通知内容摘要</td>
</tr>
<tr>
<td>时间</td>
<td>发送时间</td>
</tr>
<tr>
<td>状态</td>
<td>已读 / 未读（未读显示红点）</td>
</tr>
</tbody>
</table>
<h3>列表排序</h3>
<p>按时间倒序排列，最新通知在顶部。</p>
<hr />
<h2>4. 操作说明</h2>
<h3>查看通知</h3>
<ol>
<li>点击通知标题，进入详情弹窗</li>
<li>查看完整内容</li>
<li>自动标记为已读</li>
</ol>
<h3>单条标记已读</h3>
<p>点击通知行右侧**&quot;标记已读&quot;**按钮（或小圆点图标）。</p>
<h3>全部标记已读</h3>
<p>点击列表顶部或右上角**&quot;全部已读&quot;**按钮，一次性将所有未读通知标记为已读。</p>
<hr />
<h2>5. 触发场景一览</h2>
<table>
<thead>
<tr>
<th>场景</th>
<th>通知发送给</th>
<th>内容</th>
</tr>
</thead>
<tbody>
<tr>
<td>任务派发</td>
<td>执行会计</td>
<td>&quot;您有一个新的 {任务类型} 任务：《{合同名}》，请及时处理&quot;</td>
</tr>
<tr>
<td>任务退回讲价</td>
<td>任务派发人（经理）</td>
<td>&quot;会计 {会计名} 退回任务《{合同名}》，请审批&quot;</td>
</tr>
<tr>
<td>任务终止申请</td>
<td>任务派发人（经理）</td>
<td>&quot;会计 {会计名} 申请终止合作《{合同名}》，请审批&quot;</td>
</tr>
<tr>
<td>讲价审批通过</td>
<td>执行会计</td>
<td>&quot;您提出的协商金额已通过，《{合同名}》任务继续进行&quot;</td>
</tr>
<tr>
<td>讲价审批拒绝</td>
<td>执行会计</td>
<td>&quot;您提出的协商金额被拒绝，《{合同名}》任务已退回&quot;</td>
</tr>
<tr>
<td>合同审批通过</td>
<td>合同创建人</td>
<td>&quot;您的合同《{合同名}》已通过审批&quot;</td>
</tr>
<tr>
<td>合同审批驳回</td>
<td>合同创建人</td>
<td>&quot;您的合同《{合同名}》审批被驳回，原因：{原因}&quot;</td>
</tr>
<tr>
<td>合同即将到期</td>
<td>合同会计</td>
<td>&quot;合同《{合同名}》将在 {N} 天后到期，请及时跟进&quot;</td>
</tr>
</tbody>
</table>
<hr />
<h2>6. 角色权限</h2>
<p>通知中心<strong>所有登录用户</strong>均可使用，每个用户只能看到发给自己的通知（按 <code>userId</code> 过滤）。</p>
<table>
<thead>
<tr>
<th>角色</th>
<th>可操作</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin</td>
<td>查看全部、标记已读</td>
</tr>
<tr>
<td>manager</td>
<td>查看全部、标记已读</td>
</tr>
<tr>
<td>accountant</td>
<td>查看发给自己的、标记已读</td>
</tr>
<tr>
<td>sales</td>
<td>查看发给自己的、标记已读</td>
</tr>
</tbody>
</table>
<hr />
<h2>7. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>通知图标没有红点</td>
<td>目前没有未读通知</td>
</tr>
<tr>
<td>点击通知没反应</td>
<td>通知可能已过期或已被删除</td>
</tr>
<tr>
<td>看不到某些通知</td>
<td>通知只推送给对应用户，其他角色看不到</td>
</tr>
<tr>
<td>全部已读后红点还在</td>
<td>可能是页面缓存，刷新后消失</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 10-系统配置.md → 10-系统配置
-- preview: <h1>10 | 系统配置</h1> <blockquote> <p>基于 RuoYi 框架的系统管理功能，仅 admin 账号可操作。</p> </blockquote> <hr /> <h2>1. 功能概述</h2> <p>系统配置用于...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '10-系统配置'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>10 | 系统配置</h1>
<blockquote>
<p>基于 RuoYi 框架的系统管理功能，仅 admin 账号可操作。</p>
</blockquote>
<hr />
<h2>1. 功能概述</h2>
<p>系统配置用于管理用户账号、角色权限、菜单路由和数据字典。</p>
<h3>入口</h3>
<pre><code>系统管理 → 系统配置
</code></pre>
<p>或左侧菜单底部（admin 专属）。</p>
<blockquote>
<p><strong>注意</strong>：manager / accountant / sales 角色看不到此菜单。</p>
</blockquote>
<hr />
<h2>2. 用户管理</h2>
<h3>功能说明</h3>
<p>管理系统中的用户账号。</p>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增用户</td>
<td>填写账号、用户名、手机号、邮箱，分配角色</td>
</tr>
<tr>
<td>编辑用户</td>
<td>修改用户信息、角色、状态</td>
</tr>
<tr>
<td>删除用户</td>
<td>物理删除（谨慎操作）</td>
</tr>
<tr>
<td>重置密码</td>
<td>将密码重置为默认密码（联系 admin）</td>
</tr>
<tr>
<td>分配角色</td>
<td>给用户绑定一个或多个角色</td>
</tr>
</tbody>
</table>
<h3>字段说明</h3>
<table>
<thead>
<tr>
<th>字段</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>用户账号</td>
<td>登录用户名，唯一</td>
</tr>
<tr>
<td>用户名</td>
<td>显示名称</td>
</tr>
<tr>
<td>手机号</td>
<td>联系方式</td>
</tr>
<tr>
<td>归属部门</td>
<td>用户所属部门（用于数据权限隔离）</td>
</tr>
<tr>
<td>角色</td>
<td>绑定权限角色（admin/manager/accountant/sales）</td>
</tr>
<tr>
<td>状态</td>
<td>正常（可用）/ 停用（禁止登录）</td>
</tr>
</tbody>
</table>
<hr />
<h2>3. 角色管理</h2>
<h3>功能说明</h3>
<p>管理角色及其拥有的权限标识。</p>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增角色</td>
<td>填写角色名称、角色标识，选择权限</td>
</tr>
<tr>
<td>编辑角色</td>
<td>修改角色名/标识，调整权限</td>
</tr>
<tr>
<td>删除角色</td>
<td>删除角色（该角色下的用户需先改角色）</td>
</tr>
<tr>
<td>分配权限</td>
<td>给角色勾选权限标识（<code>system:contract:add</code> 等）</td>
</tr>
</tbody>
</table>
<h3>角色标识</h3>
<table>
<thead>
<tr>
<th>角色</th>
<th>标识</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>管理员</td>
<td>admin</td>
<td>全部权限</td>
</tr>
<tr>
<td>经理</td>
<td>manager</td>
<td>业务管理权限</td>
</tr>
<tr>
<td>会计</td>
<td>accountant</td>
<td>任务处理权限</td>
</tr>
<tr>
<td>销售</td>
<td>sales</td>
<td>合同/客户增删改权限</td>
</tr>
</tbody>
</table>
<hr />
<h2>4. 菜单管理</h2>
<h3>功能说明</h3>
<p>管理前端路由和权限标识的对应关系。</p>
<blockquote>
<p><strong>注意</strong>：修改菜单后需要<strong>刷新路由</strong>（重新登录或清除缓存）才能生效。</p>
</blockquote>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增菜单</td>
<td>填写菜单名称、路由路径、组件路径、权限标识</td>
</tr>
<tr>
<td>编辑菜单</td>
<td>修改菜单属性</td>
</tr>
<tr>
<td>删除菜单</td>
<td>删除菜单（谨慎操作，可能影响功能）</td>
</tr>
</tbody>
</table>
<h3>组件路径说明</h3>
<p>菜单的 <code>component</code> 字段对应前端 <code>.vue</code> 文件路径：</p>
<table>
<thead>
<tr>
<th>component 值</th>
<th>对应文件</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>system/kb/document</code></td>
<td><code>ruoyi-ui/src/views/system/kb/document.vue</code></td>
</tr>
<tr>
<td><code>system/contract/index</code></td>
<td><code>ruoyi-ui/src/views/system/contract/index.vue</code></td>
</tr>
</tbody>
</table>
<h3>权限标识格式</h3>
<pre><code>模块:子模块:操作
</code></pre>
<p>示例：</p>
<table>
<thead>
<tr>
<th>权限标识</th>
<th>含义</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>system:contract:list</code></td>
<td>合同 - 列表查询</td>
</tr>
<tr>
<td><code>system:contract:add</code></td>
<td>合同 - 新增</td>
</tr>
<tr>
<td><code>system:contract:edit</code></td>
<td>合同 - 编辑</td>
</tr>
<tr>
<td><code>system:task:handle</code></td>
<td>任务 - 处理任务</td>
</tr>
<tr>
<td><code>kb:document:publish</code></td>
<td>知识库 - 发布文档</td>
</tr>
</tbody>
</table>
<hr />
<h2>5. 字典管理</h2>
<h3>功能说明</h3>
<p>维护系统中使用的下拉选项值（数据字典）。</p>
<h3>字典类型列表</h3>
<table>
<thead>
<tr>
<th>字典类型</th>
<th>用途</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>cms_contract_type</code></td>
<td>合同类型（1=代账 2=地址租赁）</td>
</tr>
<tr>
<td><code>cms_contract_status</code></td>
<td>合同状态（0~3）</td>
</tr>
<tr>
<td><code>cms_audit_status</code></td>
<td>审核状态（0=待审批 1=通过 2=驳回）</td>
</tr>
<tr>
<td><code>cms_reminder_status</code></td>
<td>催交状态</td>
</tr>
<tr>
<td><code>cms_pay_cycle</code></td>
<td>付款周期</td>
</tr>
<tr>
<td><code>cms_pay_method</code></td>
<td>收款方式</td>
</tr>
<tr>
<td><code>cms_tax_type</code></td>
<td>税务类型</td>
</tr>
<tr>
<td><code>cms_task_type</code></td>
<td>任务类型（1=催收 2=续签 3=终止）</td>
</tr>
<tr>
<td><code>cms_task_priority</code></td>
<td>任务优先级</td>
</tr>
<tr>
<td><code>cms_task_status</code></td>
<td>任务状态</td>
</tr>
<tr>
<td><code>cms_approval_type</code></td>
<td>审批类型</td>
</tr>
<tr>
<td><code>cms_customer_type</code></td>
<td>客户类型</td>
</tr>
<tr>
<td><code>cms_kb_doc_type</code></td>
<td>知识库文档类型</td>
</tr>
<tr>
<td><code>cms_kb_status</code></td>
<td>知识库文档状态</td>
</tr>
<tr>
<td><code>cms_kb_required</code></td>
<td>是否必读</td>
</tr>
</tbody>
</table>
<h3>操作</h3>
<table>
<thead>
<tr>
<th>操作</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>新增字典类型</td>
<td>创建新的字典分类（如新的下拉选项组）</td>
</tr>
<tr>
<td>新增字典数据</td>
<td>在某字典类型下添加具体选项值</td>
</tr>
<tr>
<td>编辑字典数据</td>
<td>修改选项的标签/值/排序</td>
</tr>
<tr>
<td>删除字典数据</td>
<td>删除选项（谨慎操作）</td>
</tr>
</tbody>
</table>
<hr />
<h2>6. 注意事项</h2>
<table>
<thead>
<tr>
<th>现象</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr>
<td>看不懂菜单管理</td>
<td>建议在 admin 指导下操作，错误配置可能导致功能异常</td>
</tr>
<tr>
<td>删除角色后用户还在</td>
<td>用户还在，但失去了该角色权限，需重新分配</td>
</tr>
<tr>
<td>改了字典值程序报错</td>
<td>部分字典值被代码硬引用，修改前请确认影响范围</td>
</tr>
<tr>
<td>修改菜单后没生效</td>
<td>需退出重新登录，或联系 admin 清除缓存</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

-- 11-角色权限说明.md → 11-角色权限说明
-- preview: <h1>11 | 角色权限说明</h1> <blockquote> <p>了解系统中四类角色的权限边界和数据可见范围。</p> </blockquote> <hr /> <h2>1. 角色总览</h2> <table> <thead> <t...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '11-角色权限说明'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>11 | 角色权限说明</h1>
<blockquote>
<p>了解系统中四类角色的权限边界和数据可见范围。</p>
</blockquote>
<hr />
<h2>1. 角色总览</h2>
<table>
<thead>
<tr>
<th>角色</th>
<th>角色ID</th>
<th>数据范围</th>
<th>可操作模块</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>admin</strong></td>
<td>1</td>
<td>全部数据</td>
<td>全部业务 + 系统配置</td>
</tr>
<tr>
<td><strong>manager</strong></td>
<td>2</td>
<td>全部数据</td>
<td>全部业务模块（不含系统配置）</td>
</tr>
<tr>
<td><strong>accountant</strong></td>
<td>3</td>
<td>仅 <code>assigned_to = 自己</code> 的任务</td>
<td>任务处理、通知查看、仪表盘（部分）</td>
</tr>
<tr>
<td><strong>sales</strong></td>
<td>4</td>
<td>仅 <code>create_by = 自己</code> 的合同和客户</td>
<td>合同增删改（审批通过前）、客户增删改、仪表盘（部分）</td>
</tr>
</tbody>
</table>
<hr />
<h2>2. 数据权限隔离规则</h2>
<h3>销售（sales）</h3>
<p>只能看到<strong>自己创建</strong>的合同和客户：</p>
<pre><code>WHERE create_by = \'当前用户名\'
</code></pre>
<ul>
<li>新增合同时，<code>create_by</code> 自动填充为当前用户名</li>
<li>编辑/删除只对自己创建的合同有效</li>
<li>审批通过后的合同，销售无法再编辑</li>
</ul>
<h3>会计（accountant）</h3>
<p>只能看到<strong>分配给自己的</strong>任务：</p>
<pre><code>WHERE assigned_to = \'当前用户ID\'
</code></pre>
<ul>
<li>任务列表按执行人过滤</li>
<li>任务详情中的关联合同金额显示为 <code>***</code></li>
<li>不能新增、编辑、删除合同和客户</li>
</ul>
<h3>经理（manager）和管理员（admin）</h3>
<p>无数据过滤，全量可见。</p>
<hr />
<h2>3. 金额可见性规则</h2>
<blockquote>
<p><strong>重要</strong>：这是设计如此，不是 bug。</p>
</blockquote>
<h3>受影响字段</h3>
<table>
<thead>
<tr>
<th>模块</th>
<th>字段</th>
</tr>
</thead>
<tbody>
<tr>
<td>合同</td>
<td><code>amount</code>（收费标准）、<code>actualAmount</code>（实际收款）</td>
</tr>
<tr>
<td>任务</td>
<td><code>originalAmount</code>（原金额）、<code>currentAmount</code>（协商金额）、<code>adjustAmount</code>（调整后价格）</td>
</tr>
</tbody>
</table>
<h3>角色显示情况</h3>
<table>
<thead>
<tr>
<th>角色</th>
<th>金额显示</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin / manager</td>
<td>真实金额数值</td>
</tr>
<tr>
<td>accountant</td>
<td><code>***</code></td>
</tr>
<tr>
<td>sales</td>
<td><code>***</code></td>
</tr>
</tbody>
</table>
<h3>实现原理</h3>
<p>前端通过权限指令控制：</p>
<pre><code class="language-html">&lt;!-- accountant/sales 角色看不到这段内容 --&gt;
&lt;el-table-column label=&quot;收费标准&quot; v-if=&quot;checkRole([\'admin\', \'manager\'])&quot;&gt;
  {{ row.amount }}
&lt;/el-table-column&gt;
</code></pre>
<p>是<strong>整段不渲染</strong>，不是把数字替换成 <code>***</code>。因此 accountant/sales 在页面上看不到金额列，**而不是看到 &quot;123.00 *<strong>&quot; 这样的拼接字符串</strong>。</p>
<h3>业务原因</h3>
<ul>
<li><strong>保密性</strong>：会计和销售不需要知道合同金额，金额是商业敏感信息</li>
<li><strong>防篡改</strong>：不知道金额的情况下，更难伪造记录</li>
</ul>
<hr />
<h2>4. 权限标识（权限字符）</h2>
<p>每个操作对应一个权限标识，在系统配置 → 菜单管理中维护：</p>
<table>
<thead>
<tr>
<th>权限标识</th>
<th>可操作功能</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>system:contract:list</code></td>
<td>查看合同列表</td>
</tr>
<tr>
<td><code>system:contract:query</code></td>
<td>查看合同详情</td>
</tr>
<tr>
<td><code>system:contract:add</code></td>
<td>新增合同</td>
</tr>
<tr>
<td><code>system:contract:edit</code></td>
<td>编辑合同</td>
</tr>
<tr>
<td><code>system:contract:remove</code></td>
<td>删除合同</td>
</tr>
<tr>
<td><code>system:contract:export</code></td>
<td>导出合同Excel</td>
</tr>
<tr>
<td><code>system:contract:import</code></td>
<td>导入合同Excel</td>
</tr>
<tr>
<td><code>cms:contract:audit</code></td>
<td>审批合同</td>
</tr>
<tr>
<td><code>system:customer:list</code></td>
<td>查看客户列表</td>
</tr>
<tr>
<td><code>system:customer:add</code></td>
<td>新增客户</td>
</tr>
<tr>
<td><code>system:customer:edit</code></td>
<td>编辑客户</td>
</tr>
<tr>
<td><code>system:customer:remove</code></td>
<td>删除客户</td>
</tr>
<tr>
<td><code>system:task:list</code></td>
<td>查看任务列表</td>
</tr>
<tr>
<td><code>cms:task:handle</code></td>
<td>处理任务（开始/退回/完成等）</td>
</tr>
<tr>
<td><code>system:ledger:view</code></td>
<td>查看账本（仅 admin/manager）</td>
</tr>
<tr>
<td><code>system:notification:list</code></td>
<td>查看通知列表</td>
</tr>
<tr>
<td><code>system:notification:read</code></td>
<td>标记通知已读</td>
</tr>
<tr>
<td><code>kb:document:add</code></td>
<td>新增文档</td>
</tr>
<tr>
<td><code>kb:document:edit</code></td>
<td>编辑文档</td>
</tr>
<tr>
<td><code>kb:document:remove</code></td>
<td>删除文档</td>
</tr>
<tr>
<td><code>kb:category:list</code></td>
<td>查看目录</td>
</tr>
<tr>
<td><code>kb:portal:view</code></td>
<td>访问知识库门户</td>
</tr>
</tbody>
</table>
<hr />
<h2>5. 审批与派发的权限要求</h2>
<table>
<thead>
<tr>
<th>操作</th>
<th>所需角色</th>
</tr>
</thead>
<tbody>
<tr>
<td>审批合同（通过/驳回）</td>
<td>manager / admin</td>
</tr>
<tr>
<td>派发催收任务</td>
<td>manager / admin</td>
</tr>
<tr>
<td>审批任务讲价（同意/拒绝）</td>
<td>manager / admin</td>
</tr>
<tr>
<td>审批任务终止（同意/拒绝）</td>
<td>manager / admin</td>
</tr>
<tr>
<td>重新派发任务</td>
<td>manager / admin</td>
</tr>
<tr>
<td>系统配置（用户/角色/菜单/字典）</td>
<td>admin</td>
</tr>
</tbody>
</table>
<hr />
<h2>6. 如何确认当前账号角色</h2>
<h3>方法一：看首页仪表盘</h3>
<table>
<thead>
<tr>
<th>角色</th>
<th>仪表盘内容</th>
</tr>
</thead>
<tbody>
<tr>
<td>admin / manager</td>
<td>本月目标金额、到期合同数、客户总数</td>
</tr>
<tr>
<td>accountant</td>
<td>应收金额、已收金额、代账家数</td>
</tr>
<tr>
<td>sales</td>
<td>我的客户数、新增客户统计</td>
</tr>
</tbody>
</table>
<h3>方法二：看侧边栏菜单</h3>
<ul>
<li><strong>有&quot;系统配置&quot;菜单</strong> → admin</li>
<li><strong>有&quot;账本模块&quot;菜单</strong> → admin / manager</li>
<li><strong>没有&quot;系统配置&quot;和&quot;账本模块&quot;</strong> → accountant / sales</li>
<li><strong>有&quot;合同管理&quot;且可新增</strong> → sales（自己创建的合同）</li>
</ul>
<h3>方法三：看金额列</h3>
<ul>
<li><strong>真实金额</strong> → admin / manager</li>
<li><strong>显示 <code>***</code> 或没有金额列</strong> → accountant / sales</li>
</ul>
',
    v.update_time = NOW();

-- 12-注意事项与常见问题.md → 12-注意事项与常见问题
-- preview: <h1>12 | 注意事项与常见问题</h1> <blockquote> <p>汇总使用系统时需要注意的关键事项和常见疑问答案。</p> </blockquote> <hr /> <h2>1. 金额脱敏机制</h2> <h3>问：为什么合同...
UPDATE cms_kb_document_version v
INNER JOIN cms_kb_document d ON v.document_id = d.id
   AND d.category_id = (SELECT id FROM cms_kb_category WHERE name = '系统操作手册' AND del_flag = 0 LIMIT 1)
   AND d.title = '12-注意事项与常见问题'
   AND d.del_flag = 0
   AND v.is_current = 1
   AND (v.content LIKE '%详见系统操作手册%' OR LENGTH(v.content) < 200)
SET v.content = '<h1>12 | 注意事项与常见问题</h1>
<blockquote>
<p>汇总使用系统时需要注意的关键事项和常见疑问答案。</p>
</blockquote>
<hr />
<h2>1. 金额脱敏机制</h2>
<h3>问：为什么合同金额和任务金额显示为 <code>***</code>？</h3>
<p><strong>答</strong>：这是系统设计的一部分，不是 bug。</p>
<p>以下角色<strong>看不到真实金额</strong>：</p>
<ul>
<li><strong>accountant（会计）</strong></li>
<li><strong>sales（销售）</strong></li>
</ul>
<p>前端通过权限指令控制，整段内容<strong>不渲染</strong>，不是数字替换为 <code>***</code>。</p>
<p>这是商业保密要求：会计和销售不需要知道合同金额，只需执行工作任务。</p>
<p>如需查看真实金额，请使用 <strong>admin</strong> 或 <strong>manager</strong> 账号登录。</p>
<hr />
<h2>2. 合同状态动态计算</h2>
<h3>问：为什么修改了合同日期，状态没有立即变化？</h3>
<p><strong>答</strong>：合同状态<strong>不存储在数据库中</strong>，每天凌晨定时任务根据当前日期重新计算。</p>
<p>修改日期后，状态通常在<strong>当天凌晨定时任务跑完后</strong>才更新（最迟次日生效）。</p>
<h3>计算规则（供参考）</h3>
<pre><code>当前日期 &lt; 开始日期  → 未开始
开始日期 ≤ 当前日期 ≤ 结束日期 - 30天  → 进行中
结束日期 - 30天 &lt; 当前日期 ≤ 结束日期  → 即将到期
当前日期 &gt; 结束日期  → 已过期
</code></pre>
<h3>问：为什么合同状态是&quot;进行中&quot;但实际已到期？</h3>
<p><strong>答</strong>：定时任务每天凌晨执行一次，如果合同在当天到期，状态要到次日凌晨才更新为&quot;已过期&quot;。</p>
<hr />
<h2>3. 任务防重复派发</h2>
<h3>问：为什么无法派发任务，系统提示&quot;任务已存在&quot;？</h3>
<p><strong>答</strong>：同一合同 + 同一任务类型 + 同一执行人的组合，系统不允许重复派发。</p>
<p>例如：对同一合同不能再派发第二个&quot;催收任务&quot;给同一个会计。</p>
<p><strong>解决方式</strong>：</p>
<ul>
<li>等待现有任务完成（状态变为&quot;已完成&quot;）后再派发</li>
<li>或者派发给不同的执行会计</li>
</ul>
<hr />
<h2>4. 客户与合同关联</h2>
<h3>问：为什么客户删除不了？</h3>
<p><strong>答</strong>：客户有关联合同（代账合同或地址合同）时，系统禁止删除。</p>
<p><strong>解决方式</strong>：</p>
<ol>
<li>先删除该客户的所有关联合同</li>
<li>或者将客户状态改为&quot;非正常&quot;（不是物理删除，只是标记）</li>
</ol>
<hr />
<h2>5. 附件上传</h2>
<h3>支持格式</h3>
<table>
<thead>
<tr>
<th>格式</th>
<th>是否支持</th>
</tr>
</thead>
<tbody>
<tr>
<td>图片</td>
<td>jpg、png</td>
</tr>
<tr>
<td>文档</td>
<td>pdf、doc、docx</td>
</tr>
<tr>
<td>表格</td>
<td>xls、xlsx</td>
</tr>
<tr>
<td>其他</td>
<td>不支持</td>
</tr>
</tbody>
</table>
<h3>大小限制</h3>
<p>单个文件最大 <strong>200MB</strong>。</p>
<h3>常见问题</h3>
<table>
<thead>
<tr>
<th>问题</th>
<th>解答</th>
</tr>
</thead>
<tbody>
<tr>
<td>上传失败</td>
<td>检查文件格式和大小是否符合要求</td>
</tr>
<tr>
<td>PDF 无法预览</td>
<td>部分浏览器不支持，尝试下载后查看</td>
</tr>
<tr>
<td>附件显示不了</td>
<td>检查网络是否正常，或文件是否已从服务器删除</td>
</tr>
</tbody>
</table>
<hr />
<h2>6. Excel 批量导入</h2>
<h3>模板下载</h3>
<p>在合同列表页点击&quot;导入&quot; → 选择&quot;下载代账模板&quot;或&quot;下载地址出售模板&quot;。</p>
<h3>格式要求</h3>
<ul>
<li>严格按照模板列顺序填写</li>
<li>日期格式：<code>YYYY-MM-DD</code></li>
<li>金额：数字，不带货币符号</li>
<li>下拉选项：填写字典值（不是显示文本），如&quot;代账报税合同&quot;对应 <code>1</code></li>
</ul>
<h3>更新已有数据</h3>
<p>勾选&quot;更新已有数据&quot;后，系统根据合同编号匹配已有记录，覆盖更新。</p>
<h3>常见错误</h3>
<table>
<thead>
<tr>
<th>错误</th>
<th>原因</th>
</tr>
</thead>
<tbody>
<tr>
<td>导入后数据对不上</td>
<td>模板中填写了另一类型的字段（如代账模板填了地址字段）</td>
</tr>
<tr>
<td>必填字段报空</td>
<td>未填写必填字段（如合同编号、公司名称等）</td>
</tr>
</tbody>
</table>
<hr />
<h2>7. 登录与密码</h2>
<h3>问：为什么退出登录后重新登录提示&quot;已过期&quot;？</h3>
<p><strong>答</strong>：登录态通过 cookie 存储（<code>Admin-Token</code>），不是 localStorage。</p>
<p>请确认浏览器允许 cookies。</p>
<h3>问：如何重置密码？</h3>
<p>联系 admin 账号，在<strong>系统配置 → 用户管理</strong>中重置密码。</p>
<hr />
<h2>8. 操作日志追溯</h2>
<p>系统中每条数据的创建人和创建时间都有记录：</p>
<ul>
<li><code>create_by</code> - 创建人用户名</li>
<li><code>create_time</code> - 创建时间</li>
<li><code>update_by</code> - 最后更新人</li>
<li><code>update_time</code> - 最后更新时间</li>
</ul>
<p>可通过数据详情的&quot;操作信息&quot;区块查看。</p>
<hr />
<h2>9. 常见错误码</h2>
<table>
<thead>
<tr>
<th>错误提示</th>
<th>含义</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>KB_DOC_NOT_FOUND</code></td>
<td>知识库文档不存在或未发布</td>
</tr>
<tr>
<td><code>CONTRACT_HAS_TASK</code></td>
<td>该合同存在进行中的任务，无法删除</td>
</tr>
<tr>
<td><code>CUSTOMER_HAS_CONTRACT</code></td>
<td>该客户有关联合同，无法删除</td>
</tr>
<tr>
<td><code>TASK_DUPLICATE</code></td>
<td>同一合同+同类型+同执行人的任务已存在</td>
</tr>
<tr>
<td><code>PERMISSION_DENIED</code></td>
<td>当前账号无此操作权限</td>
</tr>
</tbody>
</table>
<hr />
<h2>10. 知识库回收站</h2>
<ul>
<li>删除文档 → 进入回收站（<code>del_flag = 1</code>）</li>
<li><strong>30 天后</strong>自动物理删除（永久删除）</li>
<li>删除后 30 天内可在回收站恢复</li>
<li>恢复后需重新<strong>发布</strong>才能在门户可见</li>
</ul>
<hr />
<h2>11. 联系方式</h2>
<table>
<thead>
<tr>
<th>问题类型</th>
<th>联系谁</th>
</tr>
</thead>
<tbody>
<tr>
<td>账号/密码问题</td>
<td>admin</td>
</tr>
<tr>
<td>权限配置问题</td>
<td>admin</td>
</tr>
<tr>
<td>业务操作问题</td>
<td>直属上级或经理</td>
</tr>
<tr>
<td>系统故障/Bug</td>
<td>admin 或技术负责人</td>
</tr>
</tbody>
</table>
',
    v.update_time = NOW();

