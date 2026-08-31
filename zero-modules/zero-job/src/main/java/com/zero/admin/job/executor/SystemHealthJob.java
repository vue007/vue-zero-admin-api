package com.zero.admin.job.executor;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 最小可运行示例任务，用于验证调度中心到主应用的执行链路。
 *
 * @author Akai
 */
@Component
@JobExecutor(name = "systemHealthJob")
public class SystemHealthJob {
    public ExecuteResult execute(JobArgs args) {
        SnailJobLog.LOCAL.info("systemHealthJob executed, args={}", args);
        SnailJobLog.REMOTE.info("Zero Admin health job executed successfully");
        return ExecuteResult.success("Zero Admin is healthy");
    }
}
