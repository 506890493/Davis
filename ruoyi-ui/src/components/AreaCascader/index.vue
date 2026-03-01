<template>
  <el-cascader
    v-model="selectedArea"
    :options="areaOptions"
    :props="cascaderProps"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    filterable
    @change="handleChange"
    style="width: 100%"
  />
</template>

<script>
import areaData from '@/assets/json/china-area.json'

export default {
  name: 'AreaCascader',
  props: {
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请选择省/市/区'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      areaOptions: areaData,
      selectedArea: [],
      cascaderProps: {
        value: 'value',
        label: 'label',
        children: 'children'
      }
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        if (val && val.includes('/')) {
          this.selectedArea = val.split('/')
        } else {
          this.selectedArea = []
        }
      }
    }
  },
  methods: {
    handleChange(val) {
      const result = val ? val.join('/') : ''
      this.$emit('input', result)
      this.$emit('change', result)
    }
  }
}
</script>
