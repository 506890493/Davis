<template>
  <div class="app-container">
    <el-page-header @back="$router.back()" content="合同详情" />

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
            <dict-tag
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
      </div>
    </el-card>
  </div>
</template>

<script>
import { getContract } from "@/api/system/contract";

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
      dictAccounting: "AGENCY",
      dictRent: "RENT",
      annexList: [],
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
