<template>
  <div class="app-container">
    <el-page-header @back="$router.back()" content="客户详情">
    </el-page-header>

    <el-card class="mt20">
      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else>
        <el-descriptions title="客户信息" :column="2" border>
          <el-descriptions-item label="客户名称">{{ detail.customer.customerName }}</el-descriptions-item>
          <el-descriptions-item label="客户类型">{{ detail.customer.customerType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.customer.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.customer.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detail.customer.contactEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.customer.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detail.customer.status === '0'" type="primary">正常</el-tag>
            <el-tag v-else-if="detail.customer.status === '1'" type="danger">非正常</el-tag>
            <el-tag v-else type="info">-</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="归属销售">{{ detail.customer.ownerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ parseTime(detail.customer.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.customer.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card class="mt20">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="代账合同" name="accounting">
          <el-table v-loading="loading" :data="detail.accountingContracts" border>
            <el-table-column label="合同编号" align="center" prop="contractCode" width="150" />
            <el-table-column label="公司名称" align="center" prop="contractName" :show-overflow-tooltip="true" />
            <el-table-column label="金额" align="center" prop="amount" width="100" />
            <el-table-column label="期限" align="center" width="200">
              <template slot-scope="scope">
                {{ scope.row.startDate }} ~ {{ scope.row.endDate }}
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="80">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.auditStatus === '0'" type="warning">待审批</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === '1'" type="success">{{ getStatusText(scope.row.status) }}</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === '2'" type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!detail.accountingContracts || detail.accountingContracts.length === 0" class="empty-tip">
            暂无代账合同
          </div>
        </el-tab-pane>
        <el-tab-pane label="地址合同" name="rental">
          <el-table v-loading="loading" :data="detail.rentalContracts" border>
            <el-table-column label="合同编号" align="center" prop="contractCode" width="150" />
            <el-table-column label="公司名称" align="center" prop="contractName" :show-overflow-tooltip="true" />
            <el-table-column label="租金" align="center" prop="rentAmount" width="100" />
            <el-table-column label="地址" align="center" prop="rentalAddress" :show-overflow-tooltip="true" />
            <el-table-column label="期限" align="center" width="200">
              <template slot-scope="scope">
                {{ scope.row.startDate }} ~ {{ scope.row.endDate }}
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="80">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.auditStatus === '0'" type="warning">待审批</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === '1'" type="success">{{ getStatusText(scope.row.status) }}</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === '2'" type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!detail.rentalContracts || detail.rentalContracts.length === 0" class="empty-tip">
            暂无地址合同
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getCustomerDetail } from "@/api/system/customer";

export default {
  name: "CustomerDetail",
  data() {
    return {
      loading: true,
      activeTab: "accounting",
      detail: {
        customer: {},
        accountingContracts: [],
        rentalContracts: []
      }
    };
  },
  created() {
    this.getDetail();
  },
  methods: {
    getDetail() {
      this.loading = true;
      const customerId = this.$route.params.customerId;
      getCustomerDetail(customerId).then(response => {
        this.detail = response.data;
        this.loading = false;
      });
    },
    parseTime(time, pattern) {
      if (!time) return '-';
      return this.$moment(time).format(pattern || "yyyy-MM-dd");
    },
    getStatusText(status) {
      if (status === '0') return '进行中';
      if (status === '1') return '已完成';
      return '未知';
    }
  }
};
</script>

<style scoped>
.mt20 {
  margin-top: 20px;
}
.empty-tip {
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style>
