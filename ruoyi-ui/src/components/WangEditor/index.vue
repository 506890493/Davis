<template>
  <div>
    <Toolbar
      :editor="editor"
      :defaultConfig="toolbarConfig"
      :mode="mode"
      style="border-bottom: 1px solid #ccc"
    />
    <Editor
      :style="{ height: height + 'px', overflowY: 'hidden' }"
      v-model="html"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="onCreated"
      @onChange="onChange"
    />
  </div>
</template>

<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { uploadFile } from './upload'

export default {
  name: 'WangEditor',
  components: { Editor, Toolbar },
  props: {
    value: { type: String, default: '' },
    height: { type: Number, default: 400 },
    mode: { type: String, default: 'default' } // 'default' or 'simple'
  },
  data() {
    return {
      editor: null,
      html: this.value,
      toolbarConfig: {},
      editorConfig: {
        placeholder: '请输入内容...',
        MENU_CONF: {
          uploadImage: {
            customUpload: this.handleUpload,
            base64LimitSize: 5 * 1024 // 5KB 以下转 base64
          },
          uploadVideo: {
            customUpload: this.handleUpload
          }
        }
      }
    }
  },
  watch: {
    value(newVal) {
      if (this.editor && newVal !== this.html) {
        this.editor.setHtml(newVal)
      }
    }
  },
  methods: {
    onCreated(editor) {
      this.editor = editor
    },
    onChange(editor) {
      const newHtml = editor.getHtml()
      this.html = newHtml
      this.$emit('input', newHtml)
      this.$emit('update:value', newHtml)
    },
    async handleUpload(file, insertFn) {
      try {
        const res = await uploadFile(file)
        if (res.code === 200) {
          const url = process.env.VUE_APP_BASE_API + '/kb/file/raw/' + res.data.id
          insertFn(url, file.name, url)
          this.$message.success('上传成功')
        } else {
          this.$message.error(res.msg || '上传失败')
        }
      } catch (e) {
        this.$message.error('上传失败: ' + (e.message || e))
      }
    }
  },
  beforeDestroy() {
    if (this.editor) {
      this.editor.destroy()
      this.editor = null
    }
  }
}
</script>

<style scoped>
@import '@wangeditor/editor/dist/css/style.css';
</style>
