<template>
  <div class="app-container">
    <el-page-header @back="$router.back()" content="合同详情">
      <template slot="extra">
        <el-button type="primary" size="mini" @click="handleApprovalHistory"
          >审批记录</el-button
        >
      </template>
    </el-page-header>

    <el-card class="mt20">
      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else>
        <el-descriptions title="基础信息" :column="2" border>
          <el-descriptions-item label="合同编号">{{
            detail.contractCode
          }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{
            detail.contractName
          }}</el-descriptions-item>
          <el-descriptions-item label="合同类型">
            <dict-tag
              :options="dict.type.cms_contract_type"
              :value="detail.contractType"
            />
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detail.auditStatus === '0'" type="warning">待审批</el-tag>
            <dict-tag
              v-else
              :options="dict.type.cms_contract_status"
              :value="detail.status"
            />
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">{{
            parseTime(detail.startDate, "{y}-{m}-{d}")
          }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{
            parseTime(detail.endDate, "{y}-{m}-{d}")
          }}</el-descriptions-item>
          <el-descriptions-item label="会计">{{
            detail.ownerName
          }}</el-descriptions-item>
          <el-descriptions-item label="催交状态">
            <dict-tag
              :options="dict.type.cms_reminder_status"
              :value="detail.reminderStatus"
            />
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{
            detail.remark
          }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions
          v-if="isAgency"
          class="mt20"
          title="代账信息"
          :column="2"
          border
        >
          <el-descriptions-item label="法人">{{
            detail.legalPerson
          }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{
            detail.contactPerson
          }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
            detail.contactPhone
          }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{
            detail.contactEmail
          }}</el-descriptions-item>
          <el-descriptions-item label="收费标准">{{
            detail.amount
          }}</el-descriptions-item>
          <el-descriptions-item label="付款周期">
            <dict-tag
              :options="dict.type.cms_pay_cycle"
              :value="detail.paymentCycle"
            />
          </el-descriptions-item>
          <el-descriptions-item label="收款日期">{{
            parseTime(detail.paymentDate, "{y}-{m}-{d}")
          }}</el-descriptions-item>
          <el-descriptions-item label="收款方式">
            <dict-tag
              :options="dict.type.cms_pay_method"
              :value="detail.paymentMethod"
            />
          </el-descriptions-item>
          <el-descriptions-item label="税务类型">
            <dict-tag
              :options="dict.type.cms_tax_type"
              :value="detail.taxType"
            />
          </el-descriptions-item>
          <el-descriptions-item label="成立日期">{{
            parseTime(detail.establishmentDate, "{y}-{m}-{d}")
          }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions
          v-if="isRent"
          class="mt20"
          title="地址出租信息"
          :column="2"
          border
        >
          <el-descriptions-item label="租赁地址">{{
            detail.rentalAddress
          }}</el-descriptions-item>
          <el-descriptions-item label="是否已出租">{{
            detail.isRented
          }}</el-descriptions-item>
          <el-descriptions-item label="利润">{{
            detail.profit
          }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{
            detail.contactPerson
          }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
            detail.contactPhone
          }}</el-descriptions-item>
        </el-descriptions>

        <el-card v-if="annexList.length" class="mt20" shadow="never">
          <div slot="header">附件</div>
          <el-timeline>
            <el-timeline-item
              v-for="(file, idx) in annexList"
              :key="idx"
              :timestamp="file.name"
            >
              <a :href="file.url" target="_blank" rel="noopener">{{
                file.url
              }}</a>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-descriptions class="mt20" title="操作信息" :column="2" border>
          <el-descriptions-item label="创建者">{{
            detail.createBy
          }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{
            parseTime(detail.createTime, "{y}-{m}-{d} {h}:{i}:{s}")
          }}</el-descriptions-item>
          <el-descriptions-item label="更新者">{{
            detail.updateBy
          }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{
            parseTime(detail.updateTime, "{y}-{m}-{d} {h}:{i}:{s}")
          }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 审批记录对话框 -->
    <el-dialog
      title="审批记录"
      :visible.sync="approvalOpen"
      width="700px"
      append-to-body
    >
      <el-table v-loading="approvalLoading" :data="approvalList">
        <el-table-column label="审批人" align="center" prop="approverId" />
        <el-table-column label="审批状态" align="center" prop="status">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.cms_contract_status"
              :value="scope.row.status"
            />
          </template>
        </el-table-column>
        <el-table-column label="审批意见" align="center" prop="approvalMsg" />
        <el-table-column
          label="审批时间"
          align="center"
          prop="approvalTime"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.approvalTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getContract } from "@/api/system/contract";
import { listApproval } from "@/api/system/approval";

export default {
  name: "ContractDetail",
  dicts: [
    "cms_contract_type",
    "cms_tax_type",
    "cms_pay_cycle",
    "cms_pay_method",
    "cms_reminder_status",
    "cms_contract_status",
  ],
  data() {
    return {
      loading: true,
      detail: {},
      // 与列表页保持一致
      dictAccounting: "1",
      dictRent: "2",
      annexList: [],
      approvalOpen: false,
      approvalList: [],
      approvalLoading: false,
    };
  },
  computed: {
    isAgency() {
      return this.detail.contractType === this.dictAccounting;
    },
    isRent() {
      return this.detail.contractType === this.dictRent;
    },
  },
  created() {
    this.fetch();
  },
  methods: {
    /** 查看审批记录 */
    handleApprovalHistory() {
      this.approvalOpen = true;
      this.approvalLoading = true;
      listApproval({ contractId: this.detail.contractId }).then((response) => {
        this.approvalList = response.rows;
        this.approvalLoading = false;
      });
    },
    fetch() {
      const id = this.$route.params.id;
      if (!id) {
        this.loading = false;
        return;
      }
      getContract(id)
        .then((res) => {
          this.detail = res.data || {};
          // 解析附件
          const annex = this.detail.annex;
          try {
            const obj =
              typeof annex === "string"
                ? JSON.parse(annex || "{}")
                : annex || {};
            if (obj && obj.urls) {
              const urls = Array.isArray(obj.urls)
                ? obj.urls
                : String(obj.urls || "")
                    .split(",")
                    .filter(Boolean);
              const names = Array.isArray(obj.originalFilenames)
                ? obj.originalFilenames
                : String(obj.originalFilenames || "").split(",");
              this.annexList = urls.map((url, i) => ({
                url,
                name: names[i] || `附件${i + 1}`,
              }));
            } else {
              this.annexList = [];
            }
          } catch (e) {
            this.annexList = [];
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
  },
};
</script>

<style scoped>
.mt20 {
  margin-top: 20px;
}
</style>
