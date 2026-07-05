<template>
  <div class="kb-detail">
    <el-page-header @back="$router.back()" content="文档详情" style="margin-bottom: 12px" />
    <el-card v-loading="loading">
      <h2 slot="header">
        <el-tag v-if="doc && doc.isRequired===1" type="warning" size="mini" style="margin-right:6px">★ 必读</el-tag>
        {{ doc ? doc.title : '加载中...' }}
      </h2>
      <div v-if="doc">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="类型">
            <el-tag v-if="doc.docType===1" type="info" size="mini">文件</el-tag>
            <el-tag v-else size="mini">富文本</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目录">{{ doc.categoryId }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ doc.publishedTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 文件型：主文件下载 -->
        <div v-if="doc.docType===1">
          <el-card shadow="never">
            <div class="file-card">
              <i class="el-icon-document" style="font-size: 48px; color: #409eff"></i>
              <div style="margin-left: 16px; flex: 1">
                <div>主文件 ID: {{ doc.primaryFileId }}</div>
                <el-link type="primary" :href="rawFileUrl(doc.primaryFileId)" target="_blank">下载 / 在线预览</el-link>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 富文本型：渲染 + 附件 -->
        <div v-else>
          <div v-html="richContent" class="rich-content"></div>

          <el-divider v-if="attachments && attachments.length" content-position="left">附件</el-divider>
          <el-row v-if="attachments && attachments.length" :gutter="12">
            <el-col v-for="att in attachments" :key="att.id" :span="8" style="margin-bottom: 12px">
              <el-card shadow="hover" class="att-card">
                <div class="att-row">
                  <i class="el-icon-paperclip"></i>
                  <span style="flex:1; margin-left: 8px">{{ att.displayName || '附件' }}</span>
                  <el-link type="primary" :href="rawFileUrl(att.fileId)" target="_blank">下载</el-link>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <el-divider v-if="related && related.length" content-position="left">相关推荐</el-divider>
        <el-row v-if="related && related.length" :gutter="12">
          <el-col v-for="r in related" :key="r.id" :span="6" style="margin-bottom: 12px">
            <el-card shadow="hover" class="related-card" @click.native="$router.push('/view/detail/' + r.id)">
              <div class="related-title">{{ r.title }}</div>
              <div class="related-meta">{{ r.publishedTime }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDetail, listPublished } from '@/api/system/kb/portal';
import { rawFileUrl } from '@/api/system/kb/file';
import KbImagePreview from './components/KbImagePreview.vue';
import KbVideoPlayer from './components/KbVideoPlayer.vue';

export default {
  name: 'KbDetail',
  components: { KbImagePreview, KbVideoPlayer },
  data() {
    return { loading: false, doc: null, richContent: '', attachments: [], related: [] };
  },
  created() {    console.log(this.$route.params.id);this.load(); },
  methods: {
    rawFileUrl(id) { return rawFileUrl(id); },
    async load() {
      // KB 详情页加载: getDetail 走 axios 拦截器, res 已是 doc 对象
      const id = this.$route.params.id;
      this.loading = true;
      try {
        // getDetail 返回的是 doc 对象本身（axios 拦截器已解包 AjaxResult.data）
        const res = await getDetail(id);
        if (res && res.id) {
          this.doc = res;
          // docType 在 Java 实体是 String，后端可能返回 "2"（字符串）或 2（数字）
          // 用宽松相等 + 显式判断两种值，确保富文本分支能命中
          const docType = this.doc.docType;
          if (docType == 2 || docType === '2' || docType === 2) {
            this.processRichText();
          }
          // 加载同分类下其他文档（listPublished 是 TableDataInfo，拦截器解包后直接是 {code,rows,total}）
          const relRes = await listPublished({ categoryId: this.doc.categoryId, pageNum: 1, pageSize: 5 });
          if (relRes && relRes.rows) {
            this.related = (relRes.rows || []).filter(r => r.id !== this.doc.id).slice(0, 4);
          }
        } else {
          this.$message.error('文档不存在');
        }
      } finally { this.loading = false; }
    },
    processRichText() {
      // 简化的富文本处理：直接将 /kb/file/raw 嵌入
      this.richContent = this.doc.content || '';
    }
  }
};
</script>

<style scoped>
.kb-detail { padding: 16px; }
.file-card { display: flex; align-items: center; padding: 16px; }
.rich-content {
    padding: 20px 24px;
    background: #fafafa;
    border-radius: 4px;
    min-height: 200px;
    line-height: 1.7;
    color: #303133;
    font-size: 14px;
}
.rich-content :deep(img) { max-width: 100%; }
.rich-content :deep(video) { max-width: 100%; }

/* 标题层级 */
.rich-content :deep(h1) {
    font-size: 26px;
    margin: 24px 0 16px;
    padding-bottom: 8px;
    border-bottom: 2px solid #409eff;
    color: #303133;
}
.rich-content :deep(h1):first-child { margin-top: 0; }
.rich-content :deep(h2) {
    font-size: 20px;
    margin: 22px 0 12px;
    padding-bottom: 6px;
    border-bottom: 1px solid #ebeef5;
    color: #303133;
}
.rich-content :deep(h3) { font-size: 17px; margin: 18px 0 10px; color: #303133; }
.rich-content :deep(h4) { font-size: 15px; margin: 14px 0 8px; color: #303133; }
.rich-content :deep(h5),
.rich-content :deep(h6) { font-size: 14px; margin: 12px 0 6px; color: #606266; }

/* 段落 / 强调 */
.rich-content :deep(p) { margin: 10px 0; }
.rich-content :deep(strong) { color: #303133; font-weight: 600; }
.rich-content :deep(em) { color: #606266; }

/* 列表 */
.rich-content :deep(ul),
.rich-content :deep(ol) { padding-left: 28px; margin: 10px 0; }
.rich-content :deep(li) { margin: 4px 0; }
.rich-content :deep(ul li)::marker { color: #409eff; }
.rich-content :deep(ol li)::marker { color: #409eff; font-weight: 600; }

/* 任务列表 */
.rich-content :deep(ul.task-list),
.rich-content :deep(ul.contains-task-list) { padding-left: 0; list-style: none; }
.rich-content :deep(ul.task-list li),
.rich-content :deep(ul.contains-task-list li) { margin: 4px 0; }
.rich-content :deep(ul.task-list input[type="checkbox"]),
.rich-content :deep(ul.contains-task-list input[type="checkbox"]) {
    margin-right: 6px;
    vertical-align: middle;
}

/* 链接 */
.rich-content :deep(a) { color: #409eff; text-decoration: none; }
.rich-content :deep(a:hover) { text-decoration: underline; }

/* 引用 */
.rich-content :deep(blockquote) {
    border-left: 4px solid #c0c4cc;
    padding: 6px 14px;
    color: #606266;
    background: #f5f7fa;
    margin: 12px 0;
    border-radius: 0 4px 4px 0;
}
.rich-content :deep(blockquote p) { margin: 4px 0; }

/* 行内代码 / 代码块 */
.rich-content :deep(code) {
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', Consolas, monospace;
    font-size: 13px;
    color: #e96900;
    margin: 0 2px;
}
.rich-content :deep(pre) {
    background: #f5f7fa;
    padding: 14px 16px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 12px 0;
    border: 1px solid #ebeef5;
}
.rich-content :deep(pre code) {
    background: transparent;
    padding: 0;
    color: #303133;
    margin: 0;
}

/* 表格（GFM） */
.rich-content :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 14px 0;
    font-size: 13px;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;
}
.rich-content :deep(table th) {
    background: #f5f7fa;
    font-weight: 600;
    text-align: left;
    padding: 10px 14px;
    border: 1px solid #ebeef5;
    color: #303133;
}
.rich-content :deep(table td) {
    padding: 10px 14px;
    border: 1px solid #ebeef5;
    color: #606266;
}
.rich-content :deep(table tr:nth-child(even)) { background: #fafbfc; }
.rich-content :deep(table tr:hover) { background: #f5f7fa; }

/* 水平线 */
.rich-content :deep(hr) {
    border: none;
    border-top: 1px solid #ebeef5;
    margin: 20px 0;
}

/* 图片 */
.rich-content :deep(img) {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 8px 0;
    border-radius: 4px;
}

/* 删除线 */
.rich-content :deep(del),
.rich-content :deep(s) {
    color: #909399;
    text-decoration: line-through;
}

.att-card { padding: 8px 12px; }
.att-row { display: flex; align-items: center; }
.related-card { cursor: pointer; }
.related-title { font-size: 14px; font-weight: 500; }
.related-meta { color: #999; font-size: 12px; margin-top: 4px; }
</style>
