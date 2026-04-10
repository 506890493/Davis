package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsTaskLogMapper;
import com.ruoyi.system.domain.CmsTaskLog;
import com.ruoyi.system.service.ICmsTaskLogService;

@Service
public class CmsTaskLogServiceImpl implements ICmsTaskLogService {
    
    @Autowired
    private CmsTaskLogMapper cmsTaskLogMapper;
    
    @Override
    public List<CmsTaskLog> selectCmsTaskLogList(CmsTaskLog cmsTaskLog) {
        return cmsTaskLogMapper.selectCmsTaskLogList(cmsTaskLog);
    }
    
    @Override
    public CmsTaskLog selectCmsTaskLogByLogId(Long logId) {
        return cmsTaskLogMapper.selectCmsTaskLogByLogId(logId);
    }
    
    @Override
    public int insertCmsTaskLog(CmsTaskLog cmsTaskLog) {
        return cmsTaskLogMapper.insertCmsTaskLog(cmsTaskLog);
    }
    
    @Override
    public int deleteCmsTaskLogByLogId(Long logId) {
        return cmsTaskLogMapper.deleteCmsTaskLogByLogId(logId);
    }
    
    @Override
    public List<CmsTaskLog> selectCmsTaskLogByTaskId(Long taskId) {
        return cmsTaskLogMapper.selectCmsTaskLogByTaskId(taskId);
    }
}
