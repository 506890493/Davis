package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CmsKbFile;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库-文件Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface CmsKbFileMapper {

    /**
     * 根据ID查询
     */
    CmsKbFile selectById(@Param("id") Long id);

    /**
     * 根据 SHA-256 查询（去重用）
     */
    CmsKbFile selectBySha(@Param("sha256") String sha256);

    /**
     * 新增文件记录
     */
    int insert(CmsKbFile file);

    /**
     * 软删除文件记录
     */
    int softDelete(@Param("id") Long id);

    /**
     * 物理删除（批量）
     */
    int hardDelete(@Param("ids") Long[] ids);
}
