<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true">
      <el-form-item>
        <el-input v-model="queryParams.title" placeholder="搜索标题" clearable @clear="loadList" @keyup.enter.native="loadList" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="loadList">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="ID" prop="id" width="60" />
      <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" prop="docType" width="80">
        <template v-slot="{row}">
          <el-tag v-if="row.docType===1" type="info" size="mini">文件</el-tag>
          <el-tag v-else size="mini">富文本</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="删除时间" prop="deleteTime" width="180" />
      <el-table-column label="剩余天数" width="100">
        <template v-slot="{row}">
          <el-tag :type="daysLeft(row) <= 3 ? 'danger' : 'warning'" size="mini">{{ daysLeft(row) }} 天</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template v-slot="{row}">
          <el-button size="mini" type="text" @click="handleRestore(row)">恢复</el-button>
          <el-button size="mini" type="text" style="color:red" @click="handlePurge(row)">永久删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="loadList" />
  </div>
</template>

<script>
import { listRecycle, restoreRecycle, purgeRecycle } from '@/api/system/kb/recycle';

export default {
  name: 'KbRecycle',
  data() {
    return { loading: false, list: [], total: 0, queryParams: { pageNum: 1, pageSize: 10, title: null } };
  },
  created() { this.loadList(); },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const res = await listRecycle(this.queryParams);
        this.list = res.rows || [];
        this.total = res.total || 0;
      } finally { this.loading = false; }
    },
    daysLeft(row) {
      if (!row.deleteTime) return 30;
      const diff = Date.now() - new Date(row.deleteTime).getTime();
      return Math.max(0, 30 - Math.floor(diff / (1000 * 60 * 60 * 24)));
    },
    async handleRestore(row) {
      await this.$confirm('恢复「' + row.title + '」?');
      const res = await restoreRecycle([row.id]);
      if (res.code === 200) { this.$message.success('已恢复'); this.loadList(); }
    },
    async handlePurge(row) {
      await this.$confirm('永久删除「' + row.title + '」将不可恢复，确认?');
      const res = await purgeRecycle([row.id]);
      if (res.code === 200) { this.$message.success('已永久删除'); this.loadList(); }
      else this.$message.error(res.msg || '删除失败');
    }
  }
};
</script>
