<template>
  <div class="kb-required">
    <el-page-header @back="$router.back()" content="新员工必读" style="margin-bottom: 12px" />
    <el-alert title="📌 以下内容为新员工入职必读" type="warning" :closable="false" show-icon style="margin-bottom: 16px" />

    <!-- 已打开文档的多页签 -->
    <el-card v-if="openedDocs.length" class="kb-tabs-card" style="margin-bottom: 16px">
      <el-tabs v-model="activeDocId" type="card" closable @tab-remove="closeDoc">
        <el-tab-pane
          v-for="d in openedDocs"
          :key="d.id"
          :label="d.title"
          :name="String(d.id)"
        >
          <div v-loading="d.loading" class="doc-pane">
            <div v-if="d.docType==2 || d.docType==='2'" v-html="d.content" class="rich-content"></div>
            <div v-else-if="d.primaryFileId" class="file-pane">
              <i class="el-icon-document" style="font-size:48px;color:#409eff"></i>
              <el-link type="primary" :href="rawFileUrl(d.primaryFileId)" target="_blank" style="margin-left:12px">下载 / 在线预览</el-link>
            </div>
            <el-empty v-else description="该文档暂无内容" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="d in list" :key="d.id" :span="6" style="margin-bottom: 16px">
        <el-card shadow="hover" class="required-card" @click.native="openDoc(d)">
          <div class="card-title">
            <el-tag type="warning" size="mini" style="margin-right:4px">★</el-tag>
            {{ d.title }}
          </div>
          <div class="card-meta">
            <el-tag v-if="d.docType===1" size="mini" type="info">文件</el-tag>
            <el-tag v-else size="mini">富文本</el-tag>
            <span style="margin-left: 8px">{{ d.publishedTime }}</span>
          </div>
          <div v-if="d.summary" class="card-summary">{{ d.summary }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listRequired, getDetail } from '@/api/system/kb/portal';
import { rawFileUrl } from '@/api/system/kb/file';

export default {
  name: 'KbRequired',
  data() {
    return {
      list: [],
      openedDocs: [], // { id, title, docType, content, primaryFileId, loading }
      activeDocId: null
    };
  },
  async created() {
    const res = await listRequired({ limit: 20 });
    if (res.code === 200) {
      this.list = res.data || [];
    }
  },
  methods: {
    rawFileUrl(id) { return rawFileUrl(id); },
    async openDoc(row) {
      const existing = this.openedDocs.find(d => d.id === row.id);
      if (existing) {
        this.activeDocId = String(row.id);
        return;
      }
      const placeholder = {
        id: row.id,
        title: row.title,
        docType: row.docType,
        content: '',
        primaryFileId: row.primaryFileId || null,
        loading: true
      };
      this.openedDocs.push(placeholder);
      this.activeDocId = String(row.id);
      try {
        // portal.js 的 getDetail 已手动解包 res.data，直接返回 doc 对象
        const doc = await getDetail(row.id);
        if (doc && doc.id) {
          const idx = this.openedDocs.findIndex(d => d.id === row.id);
          if (idx >= 0) {
            this.openedDocs.splice(idx, 1, {
              id: doc.id,
              title: doc.title || row.title,
              docType: doc.docType,
              content: doc.content || '',
              primaryFileId: doc.primaryFileId,
              loading: false
            });
          }
        }
      } catch (e) {
        const idx = this.openedDocs.findIndex(d => d.id === row.id);
        if (idx >= 0) {
          this.openedDocs[idx].loading = false;
          this.openedDocs[idx].content = '<p style="color:#f56c6c">加载失败：' + (e.message || e) + '</p>';
        }
      }
    },
    closeDoc(targetId) {
      const idx = this.openedDocs.findIndex(d => String(d.id) === targetId);
      if (idx < 0) return;
      this.openedDocs.splice(idx, 1);
      if (String(this.activeDocId) === targetId) {
        if (this.openedDocs.length) {
          const newIdx = Math.min(idx, this.openedDocs.length - 1);
          this.activeDocId = String(this.openedDocs[newIdx].id);
        } else {
          this.activeDocId = null;
        }
      }
    }
  }
};
</script>

<style scoped>
.kb-required { padding: 16px; }
.required-card { cursor: pointer; min-height: 120px; }
.card-title { font-size: 16px; font-weight: 500; margin-bottom: 8px; }
.card-meta { color: #999; font-size: 12px; margin-bottom: 6px; }
.card-summary { color: #666; font-size: 13px; }
.kb-tabs-card >>> .el-tabs__header { margin-bottom: 8px; }
.doc-pane { padding: 12px 0; min-height: 240px; }
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
.rich-content :deep(h1) { font-size: 26px; margin: 24px 0 16px; padding-bottom: 8px; border-bottom: 2px solid #409eff; }
.rich-content :deep(h2) { font-size: 20px; margin: 22px 0 12px; padding-bottom: 6px; border-bottom: 1px solid #ebeef5; }
.rich-content :deep(h3) { font-size: 17px; margin: 18px 0 10px; }
.rich-content :deep(p) { margin: 10px 0; }
.rich-content :deep(ul), .rich-content :deep(ol) { padding-left: 28px; margin: 10px 0; }
.rich-content :deep(a) { color: #409eff; text-decoration: none; }
.rich-content :deep(blockquote) {
    border-left: 4px solid #c0c4cc; padding: 6px 14px; color: #606266;
    background: #f5f7fa; margin: 12px 0; border-radius: 0 4px 4px 0;
}
.rich-content :deep(code) {
    background: #f5f7fa; padding: 2px 6px; border-radius: 3px;
    font-family: 'Courier New', Consolas, monospace; font-size: 13px; color: #e96900;
}
.rich-content :deep(pre) {
    background: #f5f7fa; padding: 14px 16px; border-radius: 4px;
    overflow-x: auto; margin: 12px 0; border: 1px solid #ebeef5;
}
.rich-content :deep(table) {
    border-collapse: collapse; width: 100%; margin: 14px 0;
    font-size: 13px; background: #fff; border: 1px solid #ebeef5; border-radius: 4px;
}
.rich-content :deep(table th) {
    background: #f5f7fa; font-weight: 600; text-align: left;
    padding: 10px 14px; border: 1px solid #ebeef5;
}
.rich-content :deep(table td) {
    padding: 10px 14px; border: 1px solid #ebeef5; color: #606266;
}
.rich-content :deep(table tr:nth-child(even)) { background: #fafbfc; }
.rich-content :deep(hr) { border: none; border-top: 1px solid #ebeef5; margin: 20px 0; }
.file-pane { display: flex; align-items: center; padding: 24px; }
</style>