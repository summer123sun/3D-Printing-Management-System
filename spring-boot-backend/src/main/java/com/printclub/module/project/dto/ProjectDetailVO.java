package com.printclub.module.project.dto;

import com.printclub.module.project.entity.Project;
import com.printclub.module.project.entity.ProjectFile;
import com.printclub.module.project.entity.ProjectMember;
import com.printclub.module.project.entity.ProjectProgress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目详情 VO（聚合 4 张表）
 *
 * @author E
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailVO {

    private Project project;

    private List<ProjectMember> members;

    private List<ProjectProgress> stages;

    private List<ProjectFile> files;

    /** 关联的打印任务列表（前端展示用） */
    private List<?> relatedTasks;
}