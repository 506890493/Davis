# index.html 多语言切换实现计划

## 背景

`D:\GitHub\ruoyi-davis\index.html` 是杭州拓荒牛企业服务有限公司的对外宣传单页，目前全中文。需要添加语言切换按钮，支持：
- 中文（默认）
- 英文（English）
- 阿拉伯语（العربية）

业务目标：服务外籍客户（特别是中东地区客户），扩大公司宣传覆盖范围。

## 根因分析

1. **现状**：单文件静态 HTML，所有文本硬编码在中文
2. **缺失**：无任何 i18n（国际化）机制，无 RTL（右到左）布局处理
3. **影响**：无法服务非中文用户，特别是阿拉伯语用户（文字方向不通，UI 布局需要镜像）

## 方案

### 实现策略：纯原生 JS + localStorage（零依赖）

不引入任何框架或构建工具，保持 `index.html` 为单文件可独立部署。翻译文本用 JavaScript 对象维护，存储在 `<script>` 标签内。

### 修改 `D:\GitHub\ruoyi-davis\index.html`

#### 1. HTML 结构改造

**1.1 `<html>` 标签**：根据当前语言动态设置 `lang` 和 `dir` 属性
```html
<html lang="zh-CN" dir="ltr">
```

**1.2 顶部导航栏**：在「员工工作台」按钮之前插入语言切换下拉菜单
```html
<div class="lang-switcher">
    <button class="lang-btn" id="langBtn">
        🌐 <span id="currentLang">中文</span> ▾
    </button>
    <ul class="lang-menu" id="langMenu">
        <li data-lang="zh-CN">中文</li>
        <li data-lang="en">English</li>
        <li data-lang="ar">العربية</li>
    </ul>
</div>
```

**1.3 所有可见文本添加 `data-i18n` 标识**
- 导航：`首页` / `业务中心` / `联系我们`
- Hero：`杭州拓荒牛企业服务` / `深耕企服，勤勉开拓 — 为您的企业成长保驾护航`
- 业务卡片：5 个标题 + 5 段描述
- 页脚：标题（不含地址电话）
- `<title>` 标签

#### 2. CSS 样式新增

```css
/* 语言切换器 */
.lang-switcher { position: relative; margin-left: 20px; }
.lang-btn { background: transparent; border: 1px solid #ddd; padding: 6px 12px; border-radius: 16px; cursor: pointer; font-size: 0.9rem; color: #555; display: flex; align-items: center; gap: 4px; }
.lang-btn:hover { border-color: var(--primary); color: var(--primary); }
.lang-menu { position: absolute; top: 100%; right: 0; background: white; border: 1px solid #eee; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); list-style: none; padding: 6px 0; margin-top: 6px; min-width: 140px; display: none; z-index: 1001; }
.lang-menu.open { display: block; }
.lang-menu li { padding: 8px 16px; cursor: pointer; font-size: 0.9rem; }
.lang-menu li:hover { background: var(--light); color: var(--primary); }
.lang-menu li.active { background: var(--primary); color: white; }

/* RTL 适配 */
[dir="rtl"] .hero h1 { letter-spacing: 0; }
[dir="rtl"] .lang-menu { right: auto; left: 0; }
```

#### 3. 阿拉伯语字体（仅 RTL 时加载）

```html
<head>
    ...
    <link id="arFont" rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+Arabic:wght@400;700&display=swap" disabled>
</head>
```

JS 切换语言时，根据 `dir === 'rtl'` 启用/禁用字体加载。

#### 4. JavaScript 国际化核心代码

```javascript
const I18N = {
    'zh-CN': {
        title: '杭州拓荒牛企业服务有限公司 - 专业一站式企业服务平台',
        nav_home: '首页',
        nav_services: '业务中心',
        nav_contact: '联系我们',
        hero_title: '杭州拓荒牛企业服务',
        hero_subtitle: '深耕企服，勤勉开拓 — 为您的企业成长保驾护航',
        services_title: '核心业务',
        // ... 5 张卡片
        footer_title: '联系负责人',
        lang_label: '中文'
    },
    'en': { /* English translations */ },
    'ar': { /* Arabic translations */ }
};

function setLanguage(lang) {
    localStorage.setItem('davis-lang', lang);
    document.documentElement.lang = lang;
    document.documentElement.dir = (lang === 'ar') ? 'rtl' : 'ltr';
    document.getElementById('arFont').disabled = (lang !== 'ar');
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (I18N[lang][key]) el.textContent = I18N[lang][key];
    });
    document.getElementById('currentLang').textContent = I18N[lang].lang_label;
    document.title = I18N[lang].title;
    // 更新菜单激活项样式
    document.querySelectorAll('.lang-menu li').forEach(li => {
        li.classList.toggle('active', li.dataset.lang === lang);
    });
}

// 初始化
const savedLang = localStorage.getItem('davis-lang') || 'zh-CN';
setLanguage(savedLang);

// 菜单切换
document.getElementById('langBtn').addEventListener('click', () => {
    document.getElementById('langMenu').classList.toggle('open');
});
document.querySelectorAll('.lang-menu li').forEach(li => {
    li.addEventListener('click', () => {
        setLanguage(li.dataset.lang);
        document.getElementById('langMenu').classList.remove('open');
    });
});
// 点击外部关闭菜单
document.addEventListener('click', (e) => {
    if (!e.target.closest('.lang-switcher')) {
        document.getElementById('langMenu').classList.remove('open');
    }
});
```

### 影响范围

| 受影响元素 | 翻译内容 |
|----------|---------|
| `<title>` | 3 种语言 |
| 导航 3 项 | 首页/业务中心/联系我们 |
| Hero 标题+副标题 | 各 1 条 |
| 5 张业务卡片 | 标题+描述各 5 条 = 10 条 |
| 页脚 | 仅「联系负责人」标题翻译，电话/地址/备案号保留中文 |
| `<html>` 属性 | lang + dir |
| Google Font 加载 | 仅阿拉伯语时启用 |

### 兼容性

- ✅ 不引入任何外部依赖（Google Fonts 除外，仅阿拉伯语时按需加载）
- ✅ localStorage 持久化用户语言偏好
- ✅ 默认语言中文，向后兼容（无 localStorage 时回退中文）
- ✅ 备案号保持中文不变（按用户要求）
- ✅ 阿拉伯语 `<html dir="rtl">` 由浏览器自动镜像布局

### 文件改动清单

| 文件 | 改动类型 | 说明 |
|------|---------|------|
| `D:\GitHub\ruoyi-davis\index.html` | 修改 | 添加 CSS、HTML 标签、JS 代码 |

无后端、无构建配置、无依赖更新。

## 验证方法

1. **静态测试**：
   - 浏览器打开 `index.html`，默认显示中文
   - 点击语言切换按钮 → 选择 English → 页面文本切换为英文，`<html dir="ltr">`
   - 选择 العربية → 文本切换为阿拉伯文，`<html dir="rtl">`，整个布局镜像，Google Font 加载
   - 刷新页面 → 语言偏好保持

2. **回归测试**：
   - 中文状态下，备案号、地址、电话保持中文不变
   - 英文状态下，联系信息可读性合理（地址保留专有名词）
   - 阿拉伯语状态下，业务卡片、Hero 区方向正确（从右到左）

3. **浏览器兼容性**：
   - Chrome / Edge / Firefox / Safari
   - 移动端浏览器（iOS Safari, Android Chrome）

## 风险评估

| 风险 | 等级 | 应对 |
|------|------|------|
| 翻译文本质量 | 中 | 建议业务方复核，专业术语需统一 |
| 阿拉伯语排版细节 | 低 | 浏览器 RTL 处理成熟，仅需注意 `<html dir="rtl">` 触发 |
| Google Fonts CDN 不可访问 | 低 | 提供降级到系统字体的 CSS fallback |
| 切换时闪烁 | 低 | JS 在 `DOMContentLoaded` 之前初始化即可避免 |
