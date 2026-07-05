<template>
  <div class="kb-portal">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div slot="header" class="clearfix">
            <span>分类导航</span>
            <el-link type="primary" :underline="false" style="float:right" @click="$router.push('/view/required')">📌 新员工必读</el-link>
          </div>
          <el-input v-model="searchKw" placeholder="搜索文档" clearable size="small" style="margin-bottom: 8px" />
          <el-tree
            :data="tree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            :expand-on-click-node="false"
            @node-click="onSelectCategory"
          />
        </el-card>
      </el-col>
      <el-col :span="18">
        <!-- 已打开的文档：内嵌 tabs，不离开阅读端列表 -->
        <el-card class="kb-tabs-card" style="margin-bottom: 16px">
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
          <!-- 无打开文档时的空状态提示 -->
          <div v-if="!openedDocs.length" slot="header" class="kb-tabs-empty">
            <span class="kb-tabs-empty-text">暂无打开的文档，请从右侧列表选择阅读</span>
          </div>
        </el-card>

        <!-- 文档列表 -->
        <el-card>
          <div slot="header" class="clearfix">
            <span>{{ currentCategoryName || '全部文档' }}</span>
            <el-tag style="margin-left: 8px" v-if="searchKw">搜索: {{ searchKw }}</el-tag>
            <el-button
              type="text"
              size="mini"
              style="float:right"
              @click="toggleList"
            >
              {{ listCollapsed ? '▼ 展开列表' : '▲ 收起列表' }}
            </el-button>
          </div>
          <div v-show="!listCollapsed">
            <el-table v-loading="loading" :data="list" border>
              <el-table-column label="标题" min-width="200">
                <template v-slot="{row}">
                  <el-link type="primary" :underline="false" @click="openDoc(row)">
                    <el-tag v-if="row.isPinned===1" type="danger" size="mini" style="margin-right:4px">📌</el-tag>
                    <el-tag v-if="row.isRequired===1" type="warning" size="mini" style="margin-right:4px">★</el-tag>
                    {{ row.title }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="80">
                <template v-slot="{row}">
                  <el-tag v-if="row.docType===1" size="mini" type="info">文件</el-tag>
                  <el-tag v-else size="mini">富文本</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="摘要" prop="summary" show-overflow-tooltip min-width="200" />
              <el-table-column label="发布时间" prop="publishedTime" width="160" />
              <el-table-column label="阅读" width="80" align="center">
                <template v-slot="{row}">
                  <el-button size="mini" type="text" icon="el-icon-view" @click="openDoc(row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="total>0"
              :total="total"
              :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize"
              @pagination="loadList"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { treeCategory, listPublished, searchDocs, getDetail } from '@/api/system/kb/portal';
import { rawFileUrl } from '@/api/system/kb/file';

export default {
  name: 'KbPortal',
  data() {
    return {
      tree: [],
      list: [],
      total: 0,
      loading: false,
      queryParams: { pageNum: 1, pageSize: 10, categoryId: null },
      currentCategoryName: '',
      searchKw: '',
      // 内嵌多页签状态
      openedDocs: [], // { id, title, docType, content, primaryFileId, loading }
      activeDocId: null,
      // 列表折叠状态（computed 驱动，但保留手动切换能力）
      listCollapsed: false
    };
  },
  created() {
    this.loadTree();
    this.loadList();
  },
  watch: {
    searchKw: {
      handler() { this.queryParams.pageNum = 1; this.loadList(); }
    },
    // 有打开的文档时自动折叠列表
    openedDocs: {
      handler(val) {
        if (val.length > 0) {
          this.listCollapsed = true;
        }
      },
      deep: true
    }
  },
  methods: {
    rawFileUrl(id) { return rawFileUrl(id); },
    async loadTree() {
      const res = await treeCategory();
      if (res.code === 200) this.tree = this.buildTree(res.data || []);
    },
    buildTree(list) {
      const map = {}; const roots = [];
      list.forEach(c => { map[c.id] = { ...c, children: [] }; });
      list.forEach(c => {
        if (c.parentId === 0 || c.parentId === null) roots.push(map[c.id]);
        else if (map[c.parentId]) map[c.parentId].children.push(map[c.id]);
      });
      return roots;
    },
    onSelectCategory(data) {
      this.queryParams.categoryId = data.id;
      this.currentCategoryName = data.name;
      this.queryParams.pageNum = 1;
      this.loadList();
    },
    async loadList() {
      this.loading = true;
      try {
        let res;
        if (this.searchKw) res = await searchDocs({ keyword: this.searchKw, ...this.queryParams });
        else res = await listPublished(this.queryParams);
        this.list = res.rows || [];
        this.total = res.total || 0;
      } finally { this.loading = false; }
    },
    /**
     * 在阅读端内嵌 tabs 中打开一篇文档：
     *  - 已打开则切换到该 tab（不重复拉取）
     *  - 未打开则调 /kb/portal/detail/{id} 拿正文后插入 tabs
     */
    async openDoc(row) {
      const existing = this.openedDocs.find(d => d.id === row.id);
      if (existing) {
        this.activeDocId = String(row.id);
        return;
      }
      // 先插入占位（loading 态），再异步拉详情
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
      // 有打开文档时自动折叠列表（在 watch openedDocs 里处理）
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
    /** 关闭一个 tab。关闭后如果无打开的文档则自动展开列表。 */
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
          // 关闭所有 tab 后自动展开列表
          this.listCollapsed = false;
        }
      }
    },
    /** 手动折叠/展开列表 */
    toggleList() {
      this.listCollapsed = !this.listCollapsed;
    }
  }
};
</script>

<style scoped>
.kb-portal { padding: 16px; }
.kb-tabs-card >>> .el-tabs__header { margin-bottom: 8px; }
.kb-tabs-empty {
    padding: 2px 0;
    border-top: 1px solid #f0f0f0;
    margin-top: 4px;
}
.kb-tabs-empty-text { color: #909399; font-size: 13px; }
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
