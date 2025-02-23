package com.zero.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.domain.dto.TaskAssigneeDTO;
import com.zero.admin.base.core.domain.model.TaskAssigneeBody;
import com.zero.admin.base.core.service.TaskAssigneeService;
import com.zero.admin.base.core.utils.StreamUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.system.domain.SysDept;
import com.zero.admin.system.domain.SysPost;
import com.zero.admin.system.domain.SysRole;
import com.zero.admin.system.domain.SysUser;
import com.zero.admin.system.domain.vo.SysDeptVo;
import com.zero.admin.system.domain.vo.SysPostVo;
import com.zero.admin.system.domain.vo.SysRoleVo;
import com.zero.admin.system.domain.vo.SysUserVo;
import com.zero.admin.system.mapper.SysDeptMapper;
import com.zero.admin.system.mapper.SysPostMapper;
import com.zero.admin.system.mapper.SysRoleMapper;
import com.zero.admin.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Akai
 */
@RequiredArgsConstructor
@Service
public class SysTaskAssigneeServiceImpl implements TaskAssigneeService {

    private final SysPostMapper postMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectRolesByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        wrapper.eq("r.del_flag", SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(taskQuery.getHandlerCode()), "r.role_name", taskQuery.getHandlerCode())
            .like(StringUtils.isNotBlank(taskQuery.getHandlerName()), "r.role_key", taskQuery.getHandlerName())
            .between(StringUtils.isNotBlank(taskQuery.getBeginTime()) && StringUtils.isNotBlank(taskQuery.getEndTime()),
                "r.create_time", taskQuery.getBeginTime(), taskQuery.getEndTime())
            .orderByAsc("r.role_sort").orderByAsc("r.create_time");
        Page<SysRoleVo> page = roleMapper.selectPageRoleList(pageQuery.build(), wrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRecords(),
            SysRoleVo::getRoleId, SysRoleVo::getRoleKey, SysRoleVo::getRoleName, null, SysRoleVo::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectPostsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        LambdaQueryWrapper<SysPost> wrapper = Wrappers.<SysPost>lambdaQuery()
            .like(StringUtils.isNotBlank(taskQuery.getHandlerCode()), SysPost::getPostCategory, taskQuery.getHandlerCode())
            .like(StringUtils.isNotBlank(taskQuery.getHandlerName()), SysPost::getPostName, taskQuery.getHandlerName())
            .between(StringUtils.isNotBlank(taskQuery.getBeginTime()) && StringUtils.isNotBlank(taskQuery.getEndTime()),
                SysPost::getCreateTime, taskQuery.getBeginTime(), taskQuery.getEndTime());
        if (StringUtils.isNotBlank(taskQuery.getGroupId())) {
            Long belongDeptId = Long.valueOf(taskQuery.getGroupId());
            wrapper.and(x -> {
                List<SysDept> deptList = deptMapper.selectListByParentId(belongDeptId);
                List<Long> deptIds = StreamUtils.toList(deptList, SysDept::getDeptId);
                deptIds.add(belongDeptId);
                x.in(SysPost::getDeptId, deptIds);
            });
        }
        Page<SysPostVo> page = postMapper.selectPagePostList(pageQuery.build(), wrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRecords(),
            SysPostVo::getPostId, SysPostVo::getPostCategory, SysPostVo::getPostName, SysPostVo::getDeptId, SysPostVo::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectDeptsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        LambdaQueryWrapper<SysDept> wrapper = Wrappers.<SysDept>lambdaQuery()
            .eq(SysDept::getDelFlag, SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(taskQuery.getHandlerCode()), SysDept::getDeptCategory, taskQuery.getHandlerCode())
            .like(StringUtils.isNotBlank(taskQuery.getHandlerName()), SysDept::getDeptName, taskQuery.getHandlerName())
            .between(StringUtils.isNotBlank(taskQuery.getBeginTime()) && StringUtils.isNotBlank(taskQuery.getEndTime()),
                SysDept::getCreateTime, taskQuery.getBeginTime(), taskQuery.getEndTime())
            .orderByAsc(SysDept::getAncestors)
            .orderByAsc(SysDept::getParentId)
            .orderByAsc(SysDept::getOrderNum)
            .orderByAsc(SysDept::getDeptId);
        if (StringUtils.isNotBlank(taskQuery.getGroupId())) {
            //部门树搜索
            wrapper.and(x -> {
                Long parentId = Long.valueOf(taskQuery.getGroupId());
                List<SysDept> deptList = deptMapper.selectListByParentId(parentId);
                List<Long> deptIds = StreamUtils.toList(deptList, SysDept::getDeptId);
                deptIds.add(parentId);
                x.in(SysDept::getDeptId, deptIds);
            });
        }
        Page<SysDeptVo> page = deptMapper.selectPageDeptList(pageQuery.build(), wrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRecords(),
            SysDeptVo::getDeptId, SysDeptVo::getDeptCategory, SysDeptVo::getDeptName, SysDeptVo::getParentId, SysDeptVo::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }


    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectUsersByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(taskQuery.getHandlerCode()), "u.user_name", taskQuery.getHandlerCode())
            .like(StringUtils.isNotBlank(taskQuery.getHandlerName()), "u.nick_name", taskQuery.getHandlerName())
            .between(taskQuery.getBeginTime() != null && taskQuery.getEndTime() != null,
                "u.create_time", taskQuery.getBeginTime(), taskQuery.getEndTime())
            .orderByAsc("u.user_id");
        if (StringUtils.isNotBlank(taskQuery.getGroupId())) {
            //部门树搜索
            wrapper.and(x -> {
                Long parentId = Long.valueOf(taskQuery.getGroupId());
                List<SysDept> deptList = deptMapper.selectListByParentId(parentId);
                List<Long> deptIds = StreamUtils.toList(deptList, SysDept::getDeptId);
                deptIds.add(parentId);
                x.in("u.dept_id", deptIds);
            });
        }
        Page<SysUserVo> page = userMapper.selectPageUserList(pageQuery.build(), wrapper);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRecords(),
            SysUserVo::getUserId, SysUserVo::getUserName, SysUserVo::getNickName, SysUserVo::getDeptId, SysUserVo::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

}
