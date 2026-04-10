package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsTaskLog;

public interface ICmsTaskLogService {
    public List<CmsTaskLog> selectCmsTaskLogList(CmsTaskLog cmsTaskLog);
    public CmsTaskLog selectCmsTaskLogByLogId(Long logId);
    public int insertCmsTaskLog(CmsTaskLog cmsTaskLog);
    public int deleteCmsTaskLogByLogId(Long logId);
    public List<CmsTaskLog> selectCmsTaskLogByTaskId(Long taskId);
}
