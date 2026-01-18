<template>
  <div class="app-container">
    <!--queryParam -->
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="合同编号" prop="contractCode">
        <el-input
          v-model="queryParams.contractCode"
          placeholder="请输入合同编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公司名称" prop="contractName">
        <el-input
          v-model="queryParams.contractName"
          placeholder="请输入公司名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson">
        <el-input
          v-model="queryParams.contactPerson"
          placeholder="请输入联系人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          placeholder="请输入联系电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会计" prop="ownerId">
        <el-input
          v-model="queryParams.ownerId"
          placeholder="请输入会计"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="催交状态" prop="reminderStatus">
        <el-select
          v-model="queryParams.reminderStatus"
          placeholder="请选择催交状态"
          clearable
        >
          <el-option
            v-for="dict in dict.type.cms_reminder_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-tabs v-model="viewMode" @tab-click="handleTabClick">
      <el-tab-pane label="所有合同" name="all"></el-tab-pane>
      <el-tab-pane label="待审批合同" name="pending"></el-tab-pane>
    </el-tabs>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:contract:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:contract:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:contract:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:contract:export']"
          >导出</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="openImport"
          v-hasPermi="['system:contract:import']"
          >批量导入</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <!-- 批量导入对话框 -->
    <el-dialog
      title="批量导入合同"
      :visible.sync="importOpen"
      width="500px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="Excel文件">
          <el-upload
            ref="importUpload"
            :auto-upload="false"
            :limit="1"
            accept=".xls,.xlsx"
            :file-list="importFileList"
            :on-change="onImportFileChange"
            :on-remove="onImportFileRemove"
            :http-request="() => {}"
          >
            <el-button slot="trigger" type="primary" size="small"
              >选择文件</el-button
            >
            <div class="el-upload__tip" slot="tip">
              <el-link
                type="primary"
                :underline="false"
                style="font-size: 12px"
                @click="importTemplate"
                >下载模板</el-link
              >
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="更新已有数据">
          <el-switch v-model="importUpdateSupport" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="importing" @click="submitImport"
          >开始导入</el-button
        >
        <el-button @click="importOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-table
      v-loading="loading"
      :data="contractList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="合同编号" align="center" prop="contractCode" />
      <el-table-column label="公司名称" align="center" prop="contractName" />
      <el-table-column
        label="开始日期"
        align="center"
        prop="startDate"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startDate, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="结束日期"
        align="center"
        prop="endDate"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endDate, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      
      <!--代账合同-->
      <template v-if="currentType === accountingType">
        <el-table-column label="法人" align="center" prop="legalPerson" />
        <el-table-column
          label="成立日期"
          align="center"
          prop="establishmentDate"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{
              parseTime(scope.row.establishmentDate, "{y}-{m}-{d}")
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收费标准" align="center" prop="amount" />
        <el-table-column
          label="收款日期"
          align="center"
          prop="paymentDate"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.paymentDate, "{y}-{m}-{d}") }}</span>
          </template>
        </el-table-column>
        <el-table-column label="税务类型" align="center" prop="taxType">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.cms_tax_type"
              :value="scope.row.taxType"
            />
          </template>
        </el-table-column>
        <el-table-column label="联系邮箱" align="center" prop="contactEmail" />
        <el-table-column label="付款周期" align="center" prop="paymentCycle">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.cms_pay_cycle"
              :value="scope.row.paymentCycle"
            />
          </template>
        </el-table-column>

        <el-table-column label="收款方式" align="center" prop="paymentMethod">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.cms_pay_method"
              :value="scope.row.paymentMethod"
            />
          </template>
        </el-table-column>
      </template>
      <!--地址合同-->
      <template v-else-if="currentType === rentType">
        <el-table-column label="租赁地址" align="center" prop="rentalAddress" />
        <el-table-column label="是否已出租" align="center" prop="isRented" />
        <el-table-column label="利润" align="center" prop="profit" />
        <el-table-column label="联系人" align="center" prop="contactPerson" />
        <el-table-column label="联系电话" align="center" prop="contactPhone" />
      </template>

      <el-table-column label="会计" align="center" prop="ownerName" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.cms_contract_status"
            :value="scope.row.status"
        /></template>
      </el-table-column>
      <el-table-column label="催交状态" align="center" prop="reminderStatus">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.cms_reminder_status"
            :value="scope.row.reminderStatus"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        :width="viewMode === 'pending' ? '200' : '150'"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['system:contract:query']"
            >详情</el-button
          >
          <el-button
            v-if="viewMode !== 'pending' && (scope.row.contractType === '1' || scope.row.contractType === '2')"
            size="mini"
            type="text"
            icon="el-icon-money"
            @click="handleCollection(scope.row)"
            v-hasPermi="['system:contract:import']"
            >催收</el-button
          >
          <template v-if="viewMode === 'pending'">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-check"
              @click="handleApprove(scope.row)"
              v-hasPermi="['cms:contract:audit']"
              >通过</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              @click="handleReject(scope.row)"
              v-hasPermi="['cms:contract:audit']"
              >驳回</el-button
            >
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 发起催收对话框 -->
    <el-dialog
      title="发起催收"
      :visible.sync="collectionOpen"
      width="500px"
      append-to-body
    >
      <el-form ref="collectionForm" :model="collectionForm" label-width="80px">
        <el-form-item label="指派给" prop="assignedTo">
          <el-select
            v-model="collectionForm.assignedTo"
            placeholder="请选择指派人"
          >
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            clearable
            v-model="collectionForm.deadline"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择截止日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="collectionForm.remark"
            type="textarea"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCollection">确 定</el-button>
        <el-button @click="collectionOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog
      title="驳回原因"
      :visible.sync="rejectDialogOpen"
      width="500px"
      append-to-body
    >
      <el-form ref="rejectForm" :model="rejectForm" label-width="80px">
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listContract,
  getContract,
  delContract,
  addContract,
  updateContract,
  auditContract,
} from "@/api/system/contract";
import { listUser } from "@/api/system/user";
import { createCollectionTask } from "@/api/system/task";
import { getToken } from "@/utils/auth";
import axios from "axios";

export default {
  name: "Contract",
  dicts: [
    "cms_tax_type",
    "cms_reminder_status",
    "cms_contract_type",
    "cms_contract_status",
    "cms_pay_cycle",
    "cms_pay_method",
  ],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      contractList: [],
      title: "",
      open: false,
      daterangeEstablishmentDate: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractCode: null,
        contractName: null,
        contractType: null,
        contactPerson: null,
        contactPhone: null,
        ownerId: null,
        status: null,
        reminderStatus: null,
        auditStatus: null,
      },
      // 这里的 form 只是为了防止报错，实际上不再用于弹窗表单
      form: {},
      currentType: null,
      dialogType: null,
      accountingType: "Accounting",
      rentType: "Rent",
      dictAccounting: "1",
      dictRent: "2",
      upload: {
        isUploading: false,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/common/uploads",
        fileList: [],
      },
      importOpen: false,
      importFileList: [],
      importUpdateSupport: true,
      importing: false,
      collectionOpen: false,
      collectionForm: {},
      userList: [],
      viewMode: "all",
      rejectDialogOpen: false,
      rejectForm: {
        reason: "",
      },
    };
  },
  created() {
    this.syncTypeFromRoute(true);
    this.getUserList();
  },
  watch: {
    $route() {
      this.syncTypeFromRoute();
    },
  },
  methods: {
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (
        null != this.daterangeEstablishmentDate &&
        "" != this.daterangeEstablishmentDate
      ) {
        this.queryParams.params["beginEstablishmentDate"] =
          this.daterangeEstablishmentDate[0];
        this.queryParams.params["endEstablishmentDate"] =
          this.daterangeEstablishmentDate[1];
      }
      listContract(this.queryParams).then((response) => {
        this.contractList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {};
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.daterangeEstablishmentDate = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.contractId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push("/system/contract/add");
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      const contractId = row.contractId || this.ids;
      this.$router.push("/system/contract/edit/" + contractId);
    },
    handleDetail(row) {
      const id = row.contractId;
      if (!id) return;
      this.$router.push({ path: `/system/contract/detail/${id}` });
    },
    handleDelete(row) {
      const contractIds = row.contractId || this.ids;
      this.$modal
        .confirm('是否确认删除合同管理编号为"' + contractIds + '"的数据项？')
        .then(function () {
          return delContract(contractIds);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    handleExport() {
      this.download(
        "system/contract/export",
        {
          ...this.queryParams,
        },
        `contract_${new Date().getTime()}.xlsx`
      );
    },
    handleTypeChange(val) {
      this.currentType = val;
      this.handleQuery();
    },
    syncDialogType(val) {
      this.dialogType = this.mapDictToView(val);
    },
    syncTypeFromRoute(initial = false) {
      const name = this.$route.name;
      if (name === this.accountingType || name === this.rentType) {
        this.currentType = name;
        this.queryParams.contractType = this.mapViewToDict(name);
        if (initial) {
          this.queryParams.pageNum = 1;
          this.getList();
        } else {
          this.handleQuery();
        }
      } else {
        if (initial) this.getList();
      }
    },
    mapViewToDict(view) {
      if (view === this.accountingType) return this.dictAccounting;
      if (view === this.rentType) return this.dictRent;
      return null;
    },
    mapDictToView(val) {
      if (val === this.dictAccounting) return this.accountingType;
      if (val === this.dictRent) return this.rentType;
      return null;
    },
    importTemplate() {
      this.download(
        "system/contract/importTemplate",
        {},
        `contract_template_${new Date().getTime()}.xlsx`
      );
    },
    openImport() {
      this.importFileList = [];
      this.importUpdateSupport = false;
      this.importOpen = true;
    },
    onImportFileChange(file, fileList) {
      this.importFileList = fileList.slice(-1);
    },
    onImportFileRemove(file, fileList) {
      this.importFileList = fileList;
    },
    async submitImport() {
      if (!this.importFileList.length || !this.importFileList[0].raw) {
        this.$message.warning("请先选择Excel文件");
        return;
      }
      this.importing = true;
      const fd = new FormData();
      fd.append("file", this.importFileList[0].raw);
      fd.append("updateSupport", this.importUpdateSupport);
      try {
        const res = await axios.post(
          process.env.VUE_APP_BASE_API + "/system/contract/importData",
          fd,
          {
            headers: {
              "Content-Type": "multipart/form-data",
              Authorization: "Bearer " + getToken(),
            },
          }
        );
        const data = res.data || {};
        if (data.code === 200) {
          this.$modal.msgSuccess(data.msg || "导入成功");
          this.importOpen = false;
          this.handleQuery();
        } else {
          this.$modal.msgError(data.msg || "导入失败");
        }
      } catch (e) {
        this.$modal.msgError("服务器错误");
      } finally {
        this.importing = false;
      }
    },
    getUserList() {
      listUser().then((response) => {
        this.userList = response.rows;
      });
    },
    handleCollection(row) {
      this.collectionForm = {
        sourceContractId: row.contractId,
        assignedTo: null,
        deadline: null,
        remark: null,
      };
      this.collectionOpen = true;
    },
    submitCollection() {
      this.$refs["collectionForm"].validate((valid) => {
        if (valid) {
          createCollectionTask(this.collectionForm).then((response) => {
            this.$modal.msgSuccess("催收任务创建成功");
            this.collectionOpen = false;
          });
        }
      });
    },
    handleTabClick(tab) {
      this.viewMode = tab.name;
      this.queryParams.auditStatus = this.viewMode === "pending" ? "0" : null;
      this.handleQuery();
    },
    handleApprove(row) {
      this.$modal
        .confirm('是否确认通过合同"' + row.contractName + '"的审批？')
        .then(() => {
          return auditContract({
            contractId: row.contractId,
            auditStatus: "1",
          });
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("审批通过");
        })
        .catch(() => {});
    },
    handleReject(row) {
      this.rejectForm = {
        contractId: row.contractId,
        reason: "",
      };
      this.rejectDialogOpen = true;
    },
    submitReject() {
      auditContract({ contractId: this.rejectForm.contractId, auditStatus: '2', remark: this.rejectForm.reason }).then(() => {
        this.getList();
        this.rejectDialogOpen = false;
        this.$modal.msgSuccess("审批驳回");
      });
    },
  },
};
</script>