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
            <el-card shadow="hover" class="related-card" @click.native="$router.push('/kb/view/detail/' + r.id)">
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
  created() { this.load(); },
  methods: {
    rawFileUrl(id) { return rawFileUrl(id); },
    async load() {
      const id = this.$route.params.id;
      this.loading = true;
      try {
        // getDetail 返回的是 doc 对象本身（axios 拦截器已解包 AjaxResult.data）
        const res = await getDetail(id);
        if (res && res.id) {
          this.doc = res;
          if (this.doc.docType === 2) {
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
.rich-content { padding: 16px; background: #fafafa; border-radius: 4px; min-height: 200px; }
.rich-content :deep(img) { max-width: 100%; }
.rich-content :deep(video) { max-width: 100%; }
.att-card { padding: 8px 12px; }
.att-row { display: flex; align-items: center; }
.related-card { cursor: pointer; }
.related-title { font-size: 14px; font-weight: 500; }
.related-meta { color: #999; font-size: 12px; margin-top: 4px; }
</style>
