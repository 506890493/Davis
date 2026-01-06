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
      <!-- <el-form-item label="合同类型" prop="contractType">
        <el-select
          v-model="queryParams.contractType"
          placeholder="请选择合同类型"
          clearable
          @change="handleTypeChange"
        >
          <el-option
            v-for="dict in dict.type.cms_contract_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item> -->
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

    <!-- <el-alert
      v-if="currentType"
      :title="
        currentType === accountingType
          ? '当前为代账合同视图'
          : '当前为地址出租合同视图'
      "
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    /> -->

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
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="contractList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="合同ID" align="center" prop="contractId" /> -->
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
      <!-- <el-table-column label="合同类型" align="center" prop="contractType">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.cms_contract_type"
            :value="scope.row.contractType"
          />
        </template>
      </el-table-column> -->
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
        <!-- <el-table-column label="联系人" align="center" prop="contactPerson" /> -->
        <!-- <el-table-column label="联系电话" align="center" prop="contactPhone" /> -->
        <!-- <el-table-column label="联系邮箱" align="center" prop="contactEmail" /> -->
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

      <el-table-column label="会计" align="center" prop="ownerId" />
      <!-- <el-table-column label="部门ID" align="center" prop="deptId" /> -->
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.cms_contract_status"
            :value="scope.row.status"
        /></template>
      </el-table-column>
      <!-- <el-table-column label="备注" align="center" prop="remark" /> -->
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
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:contract:edit']"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:contract:remove']"
            >删除</el-button
          >
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

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="合同编号" prop="contractCode">
          <el-input v-model="form.contractCode" placeholder="请输入合同编号" />
        </el-form-item>
        <el-form-item label="公司名称" prop="contractName">
          <el-input v-model="form.contractName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="合同类型" prop="contractType">
          <el-select
            v-model="form.contractType"
            placeholder="请选择合同类型"
            @change="syncDialogType"
          >
            <el-option
              v-for="dict in dict.type.cms_contract_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <template v-if="dialogType === accountingType">
          <el-form-item label="法人" prop="legalPerson">
            <el-input v-model="form.legalPerson" placeholder="请输入法人" />
          </el-form-item>
          <el-form-item label="联系人" prop="contactPerson">
            <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
            />
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input
              v-model="form.contactEmail"
              placeholder="请输入联系邮箱"
            />
          </el-form-item>
          <el-form-item label="收费标准" prop="amount">
            <el-input v-model="form.amount" placeholder="请输入收费标准" />
          </el-form-item>
          <el-form-item label="付款周期" prop="paymentCycle">
            <el-input
              v-model="form.paymentCycle"
              placeholder="请输入付款周期"
            />
          </el-form-item>
          <el-form-item label="收款日期" prop="paymentDate">
            <el-date-picker
              clearable
              v-model="form.paymentDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择收款日期"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item label="收款方式" prop="paymentMethod">
            <el-input
              v-model="form.paymentMethod"
              placeholder="请输入收款方式"
            />
          </el-form-item>
          <el-form-item label="税务类型" prop="taxType">
            <el-select v-model="form.taxType" placeholder="请选择税务类型">
              <el-option
                v-for="dict in dict.type.cms_tax_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="成立日期" prop="establishmentDate">
            <el-date-picker
              clearable
              v-model="form.establishmentDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择成立日期"
            >
            </el-date-picker>
          </el-form-item>
        </template>
        <template v-else-if="dialogType === rentType">
          <el-form-item label="租赁地址" prop="rentalAddress">
            <el-input
              v-model="form.rentalAddress"
              placeholder="请输入租赁地址"
            />
          </el-form-item>
          <el-form-item label="是否已出租" prop="isRented">
            <el-input v-model="form.isRented" placeholder="请输入是否已出租" />
          </el-form-item>
          <el-form-item label="利润" prop="profit">
            <el-input v-model="form.profit" placeholder="请输入利润" />
          </el-form-item>
          <el-form-item label="联系人" prop="contactPerson">
            <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
            />
          </el-form-item>
        </template>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            clearable
            v-model="form.startDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择合同开始日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            clearable
            v-model="form.endDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择合同结束日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="会计" prop="ownerId">
          <el-input v-model="form.ownerId" placeholder="请输入会计" />
        </el-form-item>
        <el-form-item label="部门ID" prop="deptId">
          <el-input v-model="form.deptId" placeholder="请输入部门ID" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入内容"
          />
        </el-form-item>
        <el-form-item label="催交状态" prop="reminderStatus">
          <el-radio-group v-model="form.reminderStatus">
            <el-radio
              v-for="dict in dict.type.cms_reminder_status"
              :key="dict.value"
              :label="dict.value"
              >{{ dict.label }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item label="附件列表" prop="annex">
          <el-upload
            ref="upload"
            multiple
            :limit="3"
            accept=".jpg, .png"
            action="#"
            :http-request="() => {}"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-change="handleChange"
            :on-remove="handleRemove"
            :auto-upload="false"
          >
            <el-button slot="trigger" size="small" type="primary"
              >选取文件</el-button
            >
            <el-button
              style="margin-left: 10px"
              size="small"
              type="success"
              :loading="upload.isUploading"
              @click="submitUpload"
              >上传到服务器</el-button
            >

            <div slot="tip" class="el-upload__tip">
              只能上传jpg/png文件，且不超过500kb
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
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
} from "@/api/system/contract";
import { getToken } from "@/utils/auth";
import axios from "axios"; // 或者是 import request from '@/utils/request'
export default {
  name: "Contract",
  dicts: [
    "cms_tax_type",
    "cms_reminder_status",
    "cms_contract_type",
    "cms_contract_status",
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
      },
      form: {},
      rules: {
        contractCode: [
          { required: true, message: "合同编号不能为空", trigger: "blur" },
        ],
        contractName: [
          { required: true, message: "公司名称不能为空", trigger: "blur" },
        ],
        contractType: [
          { required: true, message: "合同类型不能为空", trigger: "change" },
        ],
        contactPerson: [
          { required: true, message: "联系人不能为空", trigger: "blur" },
        ],
        contactPhone: [
          { required: true, message: "联系电话不能为空", trigger: "blur" },
        ],
        startDate: [
          { required: true, message: "合同开始日期不能为空", trigger: "blur" },
        ],
        endDate: [
          { required: true, message: "合同结束日期不能为空", trigger: "blur" },
        ],
      },
      currentType: null,
      dialogType: null,
      // 视图层类型（用于 UI 切换）
      accountingType: "Accounting", // 代账合同（路由名）
      rentType: "Rent", // 地址出租合同（路由名）
      // 后端字典值（用于接口查询与保存）——请按你的字典值调整
      dictAccounting: "1",
      dictRent: "2",
      // 上传参数
      upload: {
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/common/uploads",
        // 上传的文件列表
        fileList: [],
      },
    };
  },
  created() {
    // 根据路由名同步合同类型并加载列表
    this.syncTypeFromRoute(true);
  },
  watch: {
    // 路由变化时按类型筛选
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
      this.form = {
        contractId: null,
        contractCode: null,
        contractName: null,
        contractType: null,
        legalPerson: null,
        contactPerson: null,
        contactPhone: null,
        contactEmail: null,
        amount: null,
        paymentCycle: null,
        paymentDate: null,
        paymentMethod: null,
        startDate: null,
        endDate: null,
        taxType: null,
        establishmentDate: null,
        rentalAddress: null,
        isRented: null,
        profit: null,
        ownerId: null,
        deptId: null,
        status: null,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        reminderStatus: null,
        annex: null,
      };
      this.resetForm("form");
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
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加合同";
      // 弹窗显示的分组依据路由（视图类型）
      this.dialogType = this.currentType;
      // 预填合同类型为后端字典值，便于直接提交
      this.form.contractType = this.mapViewToDict(this.currentType);
      this.upload.fileList = [];
    },
    handleUpdate(row) {
      this.reset();
      const contractId = row.contractId || this.ids;

      getContract(contractId).then((response) => {
        this.form = response.data;

        // --- 核心修改开始 ---

        // 1. 清空文件列表，防止显示上一次的数据
        this.upload.fileList = [];

        // 2. 解析 annex 字段
        if (this.form.annex) {
          try {
            // 如果后端传回的是字符串，解析为对象
            let annexObj = this.form.annex;
            if (typeof annexObj === "string") {
              annexObj = JSON.parse(annexObj);
              // 解析后重新赋值给 form，方便后续编辑操作使用对象
              this.form.annex = annexObj;
            }

            // 3. 将 annex 数据映射回 ElementUI 的 fileList 格式
            // 假设 annex 结构包含 urls 和 originalFilenames 两个对应数组
            if (annexObj.urls && annexObj.urls.length > 0) {
              this.upload.fileList = annexObj.urls.map((url, index) => {
                return {
                  // 获取对应的文件名，如果没有就取 URL 最后一段，再不行叫 "附件"
                  name:
                    annexObj.originalFilenames &&
                    annexObj.originalFilenames[index]
                      ? annexObj.originalFilenames[index]
                      : "附件" + (index + 1),
                  url: url,
                  status: "success", // 设为 success 才能显示绿钩和删除按钮
                };
              });
            }
          } catch (e) {
            console.error("附件JSON解析失败:", e);
            // 如果解析出错，视为空对象
            this.form.annex = {};
          }
        } else {
          // 如果 annex 为 null，初始化为空对象
          this.form.annex = {};
        }
        // --- 核心修改结束 ---

        this.open = true;
        this.title = "修改合同";

        // 根据表单中的字典值映射到视图类型，用于切换表单项
        this.dialogType =
          this.mapDictToView(this.form.contractType) || this.currentType;
      });
    },
    handleDetail(row) {
      const id = row.contractId;
      if (!id) return;
      this.$router.push({ path: `/system/contract/detail/${id}` });
    },
    // 1. 监控文件变化，同步到 data.upload.fileList
    handleChange(file, fileList) {
      this.upload.fileList = fileList;
    },
    // 2. 移除文件时，同步更新
    handleRemove(file, fileList) {
      this.upload.fileList = fileList;
    },
    // 文件提交处理
    submitUpload() {
      if (this.upload.fileList.length === 0) {
        this.$message.warning("请先选择文件");
        return;
      }

      this.upload.isUploading = true;
      const formData = new FormData();

      // 循环添加文件
      this.upload.fileList.forEach((item) => {
        // 如果文件之前已经上传过（状态是success），可能不需要重复添加
        // 这里假设所有在列表里的都重新传一次，或者你可以加判断 item.raw 存在才传
        if (item.raw) {
          formData.append("files", item.raw);
        }
      });

      axios({
        method: "post",
        url: this.upload.url,
        headers: {
          ...this.upload.headers,
          "Content-Type": "multipart/form-data",
        },
        data: formData,
      })
        .then((response) => {
          // 注意：这里要适配你后端 RuoYi AjaxResult 的返回结构
          const res = response.data;

          // RuoYi 通常 code === 200 表示成功
          if (res.code === 200) {
            this.$message.success("上传成功");

            // 1. 保存后端返回的路径 (根据你之前的后端代码，返回的是 keys: urls, fileNames...)
            // 之前的后端代码：ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            this.form.annex = {
              urls: res.urls.split(","),
              newFileNames: res.newFileNames.split(","),
              fileNames: res.fileNames.split(","),
              originalFilenames: res.originalFilenames.split(","),
            };

            // 2. 【关键】不要清空 fileList，而是将它们的状态改为 'success'
            // 这样前端就会显示绿色的对钩，且文件保留在列表中
            this.upload.fileList.forEach((item) => {
              item.status = "success";
            });

            // console.log("Uploaded file paths:", this.form.annex);
            // console.log("Uploaded file names:", res.fileNames);
            res.originalFilenames;
            console.log("Full upload response:", res);

            // 如果你想清空，用 this.upload.fileList = []，但你的需求是保留，所以不要写这句。
          } else {
            this.$message.error(res.msg || "上传失败");
            // 失败时，可以将状态改为 exception (红色)
            this.upload.fileList.forEach((item) => {
              item.status = "exception";
            });
          }
          this.upload.isUploading = false;
        })
        .catch((err) => {
          console.error(err);
          this.$message.error("服务器错误");
          this.upload.isUploading = false;
          this.upload.fileList.forEach((item) => {
            item.status = "exception";
          });
        });
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;
      this.form.filePath = response.url;
      this.msgSuccess(response.msg);
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          // 1. 深拷贝 form，防止修改影响界面显示
          let submitData = JSON.parse(JSON.stringify(this.form));

          // 2. 【核心修改】将 annex 对象转为 JSON 字符串
          if (submitData.annex && typeof submitData.annex === "object") {
            // 直接把整个对象变成字符串： '{"urls":["..."],"fileNames":["..."]}'
            submitData.annex = JSON.stringify(submitData.annex);
          } else {
            // 如果是空的或者已经被转成字符串了，做个兜底
            if (!submitData.annex) {
              submitData.annex = null; // 或者 ""
            }
          }

          console.log("提交给后端的 JSON 字符串:", submitData.annex);

          // 3. 发送请求 (参数传 submitData)
          if (this.form.contractId != null) {
            updateContract(submitData).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addContract(submitData).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
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
      // 选择框返回的是字典值，需映射成视图类型以控制 UI
      this.dialogType = this.mapDictToView(val);
    },
    // 从当前路由名同步类型并触发查询
    syncTypeFromRoute(initial = false) {
      const name = this.$route.name;
      if (name === this.accountingType || name === this.rentType) {
        // 视图类型
        this.currentType = name;
        // 查询参数使用后端字典值
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
    // 视图类型 -> 字典值
    mapViewToDict(view) {
      if (view === this.accountingType) return this.dictAccounting;
      if (view === this.rentType) return this.dictRent;
      return null;
    },
    // 字典值 -> 视图类型
    mapDictToView(val) {
      if (val === this.dictAccounting) return this.accountingType;
      if (val === this.dictRent) return this.rentType;
      return null;
    },
  },
};
</script>
