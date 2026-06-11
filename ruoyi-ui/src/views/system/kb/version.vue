<template>
  <el-dialog :title="'版本历史 - ' + (doc ? doc.title : '')" :visible.sync="visible" width="900px">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-timeline>
          <el-timeline-item
            v-for="v in versions"
            :key="v.id"
            :timestamp="'v' + v.versionNo + ' · ' + v.createTime"
            :type="v.isCurrent===1?'primary':'info'"
            placement="top"
          >
            <span :class="{ 'current-version': v.isCurrent===1 }">v{{ v.versionNo }}</span>
            <span class="save-reason">{{ v.saveReason }}</span>
            <div style="margin-top:6px">
              <el-button size="mini" @click="select(v)">查看</el-button>
              <el-button v-if="v.isCurrent!==1" size="mini" type="warning" @click="rollback(v)">回滚</el-button>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-col>
      <el-col :span="16">
        <div v-if="selected">
          <h4>v{{ selected.versionNo }} ({{ selected.saveReason }})</h4>
          <div v-if="doc && doc.docType===2" v-html="selected.content" class="rich-preview"></div>
          <div v-else-if="selected.primaryFileId">
            <el-link :href="rawFileUrl(selected.primaryFileId)" target="_blank">下载主文件 (ID: {{ selected.primaryFileId }})</el-link>
          </div>
          <div v-else class="empty-tip">（无内容）</div>
        </div>
        <div v-else class="empty-tip">点击左侧版本查看详情</div>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script>
import { listVersions, getVersion, rollbackVersion } from '@/api/system/kb/version';
import { rawFileUrl } from '@/api/system/kb/file';

export default {
  name: 'KbVersionDialog',
  data() {
    return { visible: false, doc: null, versions: [], selected: null };
  },
  methods: {
    rawFileUrl(id) { return rawFileUrl(id); },
    async open(doc) {
      this.doc = doc;
      this.versions = [];
      this.selected = null;
      this.visible = true;
      const res = await listVersions(doc.id);
      if (res.code === 200) this.versions = res.data || [];
    },
    async select(v) {
      const res = await getVersion(this.doc.id, v.versionNo);
      if (res.code === 200) this.selected = res.data;
    },
    async rollback(v) {
      await this.$confirm('回滚到 v' + v.versionNo + ' 将产生新版本，确认?');
      const res = await rollbackVersion(this.doc.id, v.versionNo);
      if (res.code === 200) {
        this.$message.success('已回滚');
        this.open(this.doc);
      } else this.$message.error(res.msg || '回滚失败');
    }
  }
};
</script>

<style scoped>
.current-version { font-weight: bold; color: #409eff; }
.save-reason { color: #999; margin-left: 8px; font-size: 12px; }
.rich-preview { padding: 12px; background: #f5f5f5; border-radius: 4px; max-height: 400px; overflow-y: auto; }
.empty-tip { color: #999; text-align: center; padding: 30px; }
</style>
