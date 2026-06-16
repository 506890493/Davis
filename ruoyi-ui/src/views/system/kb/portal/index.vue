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
        <el-card>
          <div slot="header">
            <span>{{ currentCategoryName || '全部文档' }}</span>
            <el-tag style="margin-left: 8px" v-if="searchKw">搜索: {{ searchKw }}</el-tag>
          </div>
          <el-table v-loading="loading" :data="list" border>
            <el-table-column label="标题" min-width="200">
              <template v-slot="{row}">
                <el-link type="primary" :underline="false" @click="$router.push('/view/detail/' + row.id)">
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
                <el-button size="mini" type="text" icon="el-icon-view" @click="$router.push('/view/detail/' + row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="loadList" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { treeCategory, listPublished, searchDocs } from '@/api/system/kb/portal';

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
      searchKw: ''
    };
  },
  created() {
    this.loadTree();
    this.loadList();
  },
  watch: {
    searchKw: {
      handler() { this.queryParams.pageNum = 1; this.loadList(); }
    }
  },
  methods: {
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
    }
  }
};
</script>

<style scoped>
.kb-portal { padding: 16px; }
</style>
