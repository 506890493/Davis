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
          <el-descriptions-item label="审核状态">
            <el-tag v-if="detail.auditStatus === '0'" type="warning">待审批</el-tag>
            <el-tag v-else-if="detail.auditStatus === '2'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="detail.auditStatus === '1'" type="success">已通过</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="合同状态">
            <dict-tag v-if="detail.auditStatus === '1'" :options="dict.type.cms_contract_status" :value="detail.status" />
            <span v-else>-</span>
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
          }}          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions v-if="detail.customerId" class="mt20" title="客户信息" :column="2" border>
          <el-descriptions-item label="客户名称">{{ detail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="客户ID">{{ detail.customerId }}</el-descriptions-item>
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
          <el-descriptions-item label="收费标准">
            <span v-if="showAmount">{{ detail.amount }} 元</span>
            <span v-else>***</span>
          </el-descriptions-item>
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
          <ul class="annex-list">
            <li
              v-for="(file, idx) in annexList"
              :key="idx"
              class="annex-item"
              :title="file.name"
            >
              <el-image
                v-if="file.isImage"
                :src="file.url"
                :preview-src-list="imageUrlList"
                :initial-index="imageIndexInList(file)"
                class="annex-thumb"
                fit="cover"
              />
              <i v-else class="el-icon-document annex-icon" />
              <el-link
                :underline="false"
                type="primary"
                class="annex-name"
                @click="handlePreview(file)"
              >
                {{ file.name }}
              </el-link>
              <el-button
                type="text"
                icon="el-icon-download"
                size="mini"
                class="annex-download"
                @click="handleDownload(file)"
                >下载</el-button
              >
            </li>
          </ul>
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

    <!-- PDF 预览对话框 -->
    <el-dialog
      :title="pdfPreviewName"
      :visible.sync="pdfPreviewVisible"
      width="80%"
      top="5vh"
      append-to-body
      destroy-on-close
    >
      <iframe
        v-if="pdfPreviewVisible"
        :src="pdfPreviewUrl"
        class="pdf-iframe"
      />
    </el-dialog>

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
              :options="dict.type.cms_audit_status"
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
import download from "@/plugins/download";

const IMAGE_EXTS = ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"];

export default {
  name: "ContractDetail",
  dicts: [
    "cms_contract_type",
    "cms_tax_type",
    "cms_pay_cycle",
    "cms_pay_method",
    "cms_reminder_status",
    "cms_contract_status",
    "cms_audit_status",
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
      // PDF 预览
      pdfPreviewVisible: false,
      pdfPreviewUrl: "",
      pdfPreviewName: "",
    };
  },
  computed: {
    isAgency() {
      return this.detail.contractType === this.dictAccounting;
    },
isRent() {
return this.detail.contractType === this.dictRent;
    },
    showAmount() {
      const roles = this.$store.getters.roles || [];
      if (roles.includes("admin")) {
        return true;
      }
      if (roles.includes("accountant") || roles.includes("sales")) {
        return false;
      }
      return true;
    },
    // 仅图片 URL（用于 el-image 预览列表）
    imageUrlList() {
      return this.annexList.filter((f) => f.isImage).map((f) => f.url);
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
    /** 计算 el-image 在 imageUrlList 中的索引 */
    imageIndexInList(file) {
      return this.imageUrlList.indexOf(file.url);
    },
    /** 预览入口：图片由 el-image 自身触发；PDF 弹 iframe；其他文件直接下载 */
    handlePreview(file) {
      if (file.isPdf) {
        this.pdfPreviewUrl = file.url;
        this.pdfPreviewName = file.name;
        this.pdfPreviewVisible = true;
      } else if (!file.isImage) {
        this.handleDownload(file);
      }
      // 图片预览交给 el-image 的内置 preview 行为
    },
    /** 触发附件下载（走后端 /common/download/resource） */
    handleDownload(file) {
      if (!file || !file.resourcePath) return;
      download.resource(file.resourcePath);
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
              this.annexList = urls.map((url, i) => {
                const name = names[i] || `附件${i + 1}`;
                const ext = (name.split(".").pop() || "").toLowerCase();
                // 从完整 url 中提取 /profile/... 部分，用于走后端 /common/download/resource
                let resourcePath = url;
                const profileIdx = url.indexOf("/profile/");
                if (profileIdx > -1) {
                  resourcePath = url.substring(profileIdx);
                } else if (url.startsWith("/")) {
                  resourcePath = url;
                }
                return {
                  url,
                  name,
                  ext,
                  isImage: IMAGE_EXTS.includes(ext),
                  isPdf: ext === "pdf",
                  resourcePath,
                };
              });
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
.annex-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.annex-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 110px;
  padding: 8px 4px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
  cursor: default;
  transition: box-shadow 0.2s;
}
.annex-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.annex-thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
  cursor: pointer;
}
.annex-icon {
  width: 80px;
  height: 80px;
  font-size: 48px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  flex-shrink: 0;
}
.annex-name {
  margin-top: 6px;
  width: 100%;
  text-align: center;
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
.annex-download {
  margin-top: 2px;
  padding: 2px 0;
  font-size: 12px;
}
.pdf-iframe {
  width: 100%;
  height: 75vh;
  border: 0;
  display: block;
}
</style>
