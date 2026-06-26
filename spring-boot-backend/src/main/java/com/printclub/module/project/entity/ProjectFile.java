package com.printclub.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目文件实体（对应 project_file 表）
 *
 * <p>文件类型：1设计图 2STL文件 3照片 4文档 5其他</p>
 *
 * @author E
 */
@Data
@TableName("project_file")
public class ProjectFile implements Serializable {

    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileId;

    @TableField("project_id")
    private Integer projectId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    /** 类型：1设计图 2STL文件 3照片 4文档 5其他 */
    @TableField("file_type")
    private Integer fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("uploader_id")
    private String uploaderId;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    public static final int TYPE_DESIGN   = 1;
    public static final int TYPE_STL      = 2;
    public static final int TYPE_PHOTO    = 3;
    public static final int TYPE_DOC      = 4;
    public static final int TYPE_OTHER    = 5;
}