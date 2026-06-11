package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.CmsKbFile;
import com.ruoyi.system.service.ICmsKbFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库-文件Controller
 *
 * <p>负责上传与后端代理（/raw/{id}）：
 * image/* / video/* 返回 Content-Disposition: inline，
 * 其他类型返回 attachment。所有响应包含 Accept-Ranges: bytes 支持断点续传。</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/file")
public class CmsKbFileController extends BaseController {

    /** 单文件最大 200 MB */
    private static final long MAX_FILE_SIZE = 200L * 1024L * 1024L;

    @Autowired
    private ICmsKbFileService fileService;

    /**
     * 上传文件（计算 SHA-256 秒传去重）
     */
    @PreAuthorize("@ss.hasPermi('kb:file:upload')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return AjaxResult.error("文件不能超过 200 MB");
        }
        CmsKbFile f = fileService.registerFile(file.getOriginalFilename(), file.getInputStream());
        return AjaxResult.success("上传成功", f);
    }

    /**
     * 文件原始内容代理（image/video 内联，其他下载）
     */
    @PreAuthorize("@ss.hasPermi('kb:file:download')")
    @GetMapping("/raw/{id}")
    public ResponseEntity<Resource> raw(@PathVariable("id") Long id) {
        return fileService.buildRawResponse(id);
    }
}
