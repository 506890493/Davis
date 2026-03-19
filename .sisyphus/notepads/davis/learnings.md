## AreaCascader Implementation
- Created a 3-level cascader for China administrative divisions.
- Data stored in `ruoyi-ui/src/assets/json/china-area.json` (~28KB).
- Component `AreaCascader` wraps `el-cascader` and supports `v-model` with a slash-separated string format (e.g., '浙江省/杭州市/西湖区').
- Used `watch` with `immediate: true` to handle initial value sync.
Task 13 completed: Added dialogs for return/negotiate/terminate in task/index.vue
Task 13 completed: Added dialogs for return/negotiate/terminate in task/index.vue
