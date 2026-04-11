<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
      <i class="el-icon-bell" @click="showDrawer = true"></i>
    </el-badge>
    <el-drawer title="通知中心" :visible.sync="showDrawer" direction="rtl" size="400px" append-to-body>
      <div class="notification-list">
        <el-empty v-if="notifications.length === 0" description="暂无通知"></el-empty>
<div v-for="item in notifications" :key="item.notificationId"
              class="notification-item"
              :class="{ unread: item.isRead === '0' }"
              @click="handleClick(item)">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-content">{{ item.content }}</div>
          <div class="notification-time">{{ parseTime(item.createTime) }}</div>
        </div>
      </div>
      <div class="notification-footer">
        <el-button type="text" @click="handleMarkAllRead">全部标为已读</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getUnreadCount, getNotificationList, markRead, markAllRead } from '@/api/system/notification'

export default {
  name: 'NotificationBell',
  data() {
    return {
      showDrawer: false,
      unreadCount: 0,
      notifications: [],
      timer: null
    }
  },
  created() {
    this.fetchUnreadCount()
    this.timer = setInterval(() => {
      this.fetchUnreadCount()
    }, 60000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  watch: {
    showDrawer(val) {
      if (val) {
        this.fetchNotifications()
      }
    }
  },
  methods: {
    fetchUnreadCount() {
      getUnreadCount().then(res => {
        this.unreadCount = (res.data && res.data.count) || 0
      })
    },
    fetchNotifications() {
      getNotificationList().then(res => {
        this.notifications = res.rows || res.data || []
      })
    },
    handleClick(item) {
      if (item.isRead === '0') {
        markRead(item.notificationId).then(() => {
          this.$set(item, 'isRead', '1')
          this.fetchUnreadCount()
        })
      }

      this.showDrawer = false
    },
    handleMarkAllRead() {
      markAllRead().then(() => {
        this.notifications.forEach(item => {
          this.$set(item, 'isRead', '1')
        })
        this.unreadCount = 0
        this.$message.success('已全部标为已读')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-bell {
  display: inline-block;
  padding: 0 8px;
  height: 100%;
  cursor: pointer;
  transition: background .3s;
  
  &:hover {
    background: rgba(0, 0, 0, .025);
  }

  .notification-badge {
    line-height: 18px;
    
    .el-icon-bell {
      font-size: 20px;
      vertical-align: middle;
    }
  }
}

.notification-list {
  padding: 10px 20px;
  height: calc(100vh - 130px);
  overflow-y: auto;
  
  .notification-item {
    padding: 12px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    &.unread {
      .notification-title {
        font-weight: bold;
        color: #303133;
        
        &::before {
          content: '';
          display: inline-block;
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background-color: #f56c6c;
          margin-right: 6px;
          vertical-align: middle;
        }
      }
    }
    
    .notification-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }
    
    .notification-content {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
      line-height: 1.5;
    }
    
    .notification-time {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}

.notification-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 10px 20px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  background-color: #fff;
}
</style>
