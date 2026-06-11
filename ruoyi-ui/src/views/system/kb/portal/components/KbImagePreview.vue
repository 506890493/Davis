<template>
  <el-image
    :src="src"
    :preview-src-list="previewList"
    :initial-index="0"
    fit="contain"
    style="max-width: 100%; cursor: pointer"
  />
</template>

<script>
export default {
  name: 'KbImagePreview',
  props: {
    src: { type: String, required: true },
    previewList: { type: Array, default: () => [] }
  }
};
</script>
