package com.ruoyi.system.service;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import com.ruoyi.system.domain.CmsKbFile;

/**
 * 知识库-文件Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbFileService {
    /**
     * 根据ID查询
     */
    CmsKbFile selectById(Long id);

    /**
     * 注册文件（计算 SHA-256，秒传去重，写入磁盘）
     */
    CmsKbFile registerFile(String originalName, InputStream is) throws IOException;

    /**
     * 加载为 Spring Resource
     */
    Resource loadAsResource(Long id);

    /**
     * 构建原始响应（含 MIME / Content-Disposition / Accept-Ranges）
     */
    ResponseEntity<Resource> buildRawResponse(Long id);
}
