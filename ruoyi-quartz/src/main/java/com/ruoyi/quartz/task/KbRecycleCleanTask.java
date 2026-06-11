package com.ruoyi.quartz.task;

import com.ruoyi.system.service.ICmsKbDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;

/**
 * 知识库回收站定时清理任务。
 *
 * <p>物理删除 30 天前进入回收站的文档（连同 version、attachment）。</p>
 * <p>cron: 0 0 2 * * ?（每日 02:00），由 Quartz 任务"知识库回收站清理"触发。</p>
 * <p>对应 SQL：{@code sys_job.invoke_target = 'kbRecycleCleanTask.cleanDaily()'}</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Component("kbRecycleCleanTask")
public class KbRecycleCleanTask {

    private static final Logger log = LoggerFactory.getLogger(KbRecycleCleanTask.class);

    /** 回收站保留天数（超过此天数未恢复则物理删除） */
    private static final int RECYCLE_KEEP_DAYS = 30;

    @Autowired
    private ICmsKbDocumentService documentService;

    /**
     * 物理删除 30 天前进入回收站的文档（连同 version、attachment）。
     * Quartz 调用入口，无参无返回值。
     */
    public void cleanDaily() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -RECYCLE_KEEP_DAYS);
        Date cutoff = cal.getTime();
        int n = documentService.purgeExpired(cutoff);
        log.info("[KbRecycleCleanTask] cleaned {} expired documents (cutoff={})", n, cutoff);
    }
}
