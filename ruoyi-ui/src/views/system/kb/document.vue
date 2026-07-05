<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="目录" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable>
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下架" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题" clearable @keyup.enter.native="loadList" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-upload" size="mini" @click="openForm(1)">上传文件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-edit" size="mini" @click="openForm(2)">撰写文章</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="ID" prop="id" width="60" />
      <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" prop="docType" width="80">
        <template v-slot="{row}">
          <el-tag v-if="row.docType===1" type="info" size="mini">文件</el-tag>
          <el-tag v-else size="mini">富文本</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目录" prop="categoryId" width="100" />
      <el-table-column label="状态" prop="status" width="100">
        <template v-slot="{row}">
          <el-tag v-if="row.status===0" type="info" size="mini">草稿</el-tag>
          <el-tag v-else-if="row.status===1" type="success" size="mini">已发布</el-tag>
          <el-tag v-else type="danger" size="mini">已下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="必读" prop="isRequired" width="60">
        <template v-slot="{row}">
          <el-tag v-if="row.isRequired===1" type="warning" size="mini">★</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="publishedTime" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template v-slot="{row}">
          <el-button size="mini" type="text" @click="openForm(row.docType, row)">编辑</el-button>
          <el-button size="mini" type="text" @click="openVersion(row)">历史</el-button>
          <el-button v-if="row.status!==1" size="mini" type="text" @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status===1" size="mini" type="text" @click="handleOffline(row)">下架</el-button>
          <el-button size="mini" type="text" style="color:red" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="loadList" />

    <document-form ref="formRef" @saved="loadList" :category-options="categoryOptions" />
    <version-dialog ref="versionRef" />
  </div>
</template>

<script>
import { listDocument, getDocument, delDocument, publishDocument, offlineDocument } from '@/api/system/kb/document';
import { listCategory } from '@/api/system/kb/category';
import DocumentForm from './documentForm.vue';
import VersionDialog from './version.vue';

export default {
  name: 'KbDocument',
  components: { DocumentForm, VersionDialog },
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, categoryId: null, status: null, title: null },
      categoryOptions: []
    };
  },
  created() {
    this.loadList();
    this.loadCategoryOptions();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const res = await listDocument(this.queryParams);
        this.list = res.rows || [];
        this.total = res.total || 0;
      } finally { this.loading = false; }
    },
    async loadCategoryOptions() {
      const res = await listCategory({});
      if (res.code === 200) {
        this.categoryOptions = res.rows || [];
      }
    },
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10, categoryId: null, status: null, title: null };
      this.loadList();
    },
    async openForm(docType, doc) {
      // 编辑模式（doc.id 存在）：先调 getDocument 拉完整 doc（含当前版本正文），
      // 列表接口 selectList 不返回 content，直接传 row 会导致编辑框空白
      if (doc && doc.id) {
        const res = await getDocument(doc.id);
        if (res && res.data) {
          this.$refs.formRef.open(docType, res.data);
          return;
        }
      }
      // 新增模式（docType=1 文件 / docType=2 富文本，无 doc）
      this.$refs.formRef.open(docType, doc);
    },
    openVersion(row) {
      this.$refs.versionRef.open(row);
    },
    async handlePublish(row) {
      await this.$confirm('确认发布「' + row.title + '」?');
      const res = await publishDocument({ id: row.id });
      if (res.code === 200) { this.$message.success('已发布'); this.loadList(); }
    },
    async handleOffline(row) {
      await this.$confirm('确认下架「' + row.title + '」?');
      const res = await offlineDocument({ id: row.id });
      if (res.code === 200) { this.$message.success('已下架'); this.loadList(); }
    },
    async handleDelete(row) {
      await this.$confirm('删除「' + row.title + '」将进入回收站（30 天后清理），确认?');
      const res = await delDocument(row.id);
      if (res.code === 200) { this.$message.success('已删除'); this.loadList(); }
    }
  }
};
</script>
