package com.printclub.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.printclub.module.user.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Member Mapper
 *
 * @author D
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 统计某成员参与的项目数（通过 project_member 表）
     */
    @Select("SELECT COUNT(DISTINCT pm.project_id) FROM project_member pm " +
            "WHERE pm.member_id = #{studentId} AND pm.status = 1")
    Long countProjectsByStudentId(String studentId);

    /**
     * 统计某成员的作品数（通过 artwork 表）
     */
    @Select("SELECT COUNT(*) FROM artwork WHERE author_id = #{studentId}")
    Long countArtworksByStudentId(String studentId);
}