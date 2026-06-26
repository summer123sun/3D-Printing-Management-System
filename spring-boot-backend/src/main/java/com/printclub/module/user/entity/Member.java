package com.printclub.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成员实体（对应 member 表）
 *
 * @author D
 */
@Data
@TableName("member")
public class Member implements Serializable {

    @TableId(value = "student_id", type = IdType.INPUT)
    private String studentId;

    private String name;

    private String password;

    /** 角色：1社长 2技术骨干 3普通社员 4新成员 */
    private Integer role;

    /** 技能等级：0未入门 1Tinkercad 2Fusion360 3Blender 4调机熟练 */
    private Integer skillLevel;

    @TableField("join_date")
    private LocalDate joinDate;

    @TableField("total_prints")
    private Integer totalPrints;

    private String phone;

    private String email;

    private String avatar;

    /** 状态：1正常 2退出 */
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}