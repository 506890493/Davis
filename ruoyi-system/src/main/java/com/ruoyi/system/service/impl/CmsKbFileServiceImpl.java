package com.ruoyi.system.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsKbFile;
import com.ruoyi.system.mapper.CmsKbFileMapper;
import com.ruoyi.system.service.ICmsKbFileService;

/**
 * 知识库-文件Service业务层
 *
 * <p>关键设计：</p>
 * <ul>
 *   <li>秒传去重：基于 SHA-256 哈希</li>
 *   <li>后端代理：根据 MIME 决定 inline（图片/视频）或 attachment（其它）</li>
 *   <li>支持断点续传：响应头包含 Accept-Ranges: bytes</li>
 * </ul>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbFileServiceImpl implements ICmsKbFileService {

    @Autowired
    private CmsKbFileMapper fileMapper;

    @Value("${kb.upload-path:/app/uploadPath}")
    private String uploadPath;

    @Override
    public CmsKbFile selectById(Long id) {
        return fileMapper.selectById(id);
    }

    @Override
    @Transactional
    public CmsKbFile registerFile(String originalName, InputStream is) throws IOException {
        // 1. 计算 SHA-256（同时把流读入内存）
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 不可用", e);
        }
        byte[] buf = new byte[8192];
        int n;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((n = is.read(buf)) > 0) {
            digest.update(buf, 0, n);
            baos.write(buf, 0, n);
        }
        String sha = toHex(digest.digest());
        byte[] bytes = baos.toByteArray();

        // 2. 秒传去重
        CmsKbFile existing = fileMapper.selectBySha(sha);
        if (existing != null) {
            return existing;
        }

        // 3. 落盘
        String yyyymm = new SimpleDateFormat("yyyyMM").format(new Date());
        String ext = FilenameUtils.getExtension(originalName);
        if (ext == null) {
            ext = "";
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String relPath = "kb/" + yyyymm + "/" + stored;
        File dest = new File(uploadPath, relPath);
        try {
            org.apache.commons.io.FileUtils.forceMkdirParent(dest);
            Files.write(dest.toPath(), bytes);
        } catch (IOException e) {
            throw new IOException("写入文件失败: " + dest.getAbsolutePath(), e);
        }

        // 4. 探测 MIME
        String mime;
        try {
            mime = Files.probeContentType(dest.toPath());
        } catch (IOException e) {
            mime = null;
        }
        if (mime == null) {
            mime = "application/octet-stream";
        }

        // 5. 入库
        CmsKbFile f = new CmsKbFile();
        f.setOriginalName(originalName);
        f.setStoredName(stored);
        f.setRelPath(relPath);
        f.setFileSize((long) bytes.length);
        f.setMimeType(mime);
        f.setSha256(sha);
        f.setBucket("kb");
        fileMapper.insert(f);
        return f;
    }

    @Override
    public Resource loadAsResource(Long id) {
        CmsKbFile f = fileMapper.selectById(id);
        if (f == null) {
            return null;
        }
        File file = new File(uploadPath, f.getRelPath());
        if (!file.exists()) {
            return null;
        }
        return new FileSystemResource(file);
    }

    @Override
    public ResponseEntity<Resource> buildRawResponse(Long id) {
        CmsKbFile f = fileMapper.selectById(id);
        if (f == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(uploadPath, f.getRelPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);

        boolean inline = f.getMimeType() != null
            && (f.getMimeType().startsWith("image/") || f.getMimeType().startsWith("video/"));
        String disposition = inline ? "inline" : "attachment";
        String encoded;
        try {
            encoded = URLEncoder.encode(f.getOriginalName(), "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            encoded = "file";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(f.getMimeType()))
            .contentLength(f.getFileSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encoded + "\"")
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .body(resource);
    }

    /**
     * 字节数组转小写 hex 字符串（避免依赖 commons-codec）。
     */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
