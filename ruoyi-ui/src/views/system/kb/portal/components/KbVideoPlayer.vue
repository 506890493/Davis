<template>
  <video :src="src" controls preload="metadata" :poster="poster" style="max-width: 100%; display: block;">
    您的浏览器不支持 HTML5 视频，请<a :href="src">下载</a>查看。
  </video>
</template>

<script>
export default {
  name: 'KbVideoPlayer',
  props: {
    src: { type: String, required: true },
    poster: { type: String, default: '' }
  }
};
</script>
