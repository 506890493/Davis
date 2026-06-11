<template>
  <el-dialog :title="title" :visible.sync="visible" width="800px" @closed="reset" :close-on-click-modal="false">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="类型">
        <el-tag v-if="form.docType===1">文件</el-tag>
        <el-tag v-else>富文本</el-tag>
      </el-form-item>
      <el-form-item label="目录" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择目录" style="width:100%">
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="新员工必读">
        <el-switch v-model="form.isRequired" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="逗号分隔" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item v-if="form.docType===1" label="文件" prop="primaryFileId">
        <el-upload
          :http-request="customUpload"
          :show-file-list="false"
          :before-upload="beforeUpload"
        >
          <el-button size="small">点击上传</el-button>
        </el-upload>
        <div v-if="form.primaryFileId" class="uploaded-tip">
          已上传文件 ID: {{ form.primaryFileId }}
          <el-link type="primary" :href="rawFileUrl(form.primaryFileId)" target="_blank">预览</el-link>
        </div>
      </el-form-item>
      <el-form-item v-else label="正文" prop="content">
        <wang-editor v-model="form.content" :height="400" />
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="visible=false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import WangEditor from '@/components/WangEditor';
import { addDocument, updateDocument } from '@/api/system/kb/document';
import { uploadFile, rawFileUrl } from '@/api/system/kb/file';

export default {
  name: 'DocumentForm',
  components: { WangEditor },
  props: { categoryOptions: { type: Array, default: () => [] } },
  data() {
    return {
      visible: false, saving: false, title: '',
      form: this.initial()
    };
  },
  methods: {
    initial() {
      return { id: null, docType: 1, title: '', categoryId: null, isRequired: 0, tags: '', summary: '', content: '', primaryFileId: null };
    },
    reset() { this.form = this.initial(); },
    open(docType, doc) {
      this.reset();
      this.form.docType = docType || 1;
      if (doc) {
        this.form = { ...this.form, ...doc };
        this.title = '编辑文档';
      } else {
        this.title = docType === 1 ? '上传文件' : '撰写文章';
      }
      this.visible = true;
    },
    beforeUpload(file) {
      if (file.size > 200 * 1024 * 1024) {
        this.$message.error('文件不能超过 200 MB');
        return false;
      }
      return true;
    },
    async customUpload(req) {
      try {
        const res = await uploadFile(req.file);
        if (res.code === 200) {
          this.form.primaryFileId = res.data.id;
          this.$message.success('上传成功');
        } else this.$message.error(res.msg || '上传失败');
      } catch (e) {
        this.$message.error('上传失败');
      }
    },
    rawFileUrl(id) { return rawFileUrl(id); },
    async submit() {
      await this.$refs.form.validate();
      this.saving = true;
      try {
        const payload = { ...this.form };
        const res = this.form.id ? await updateDocument(payload) : await addDocument(payload);
        if (res.code === 200) {
          this.$message.success('保存成功');
          this.visible = false;
          this.$emit('saved');
        } else this.$message.error(res.msg || '保存失败');
      } finally { this.saving = false; }
    }
  }
};
</script>

<style scoped>
.uploaded-tip { margin-top: 8px; color: #67c23a; }
</style>
