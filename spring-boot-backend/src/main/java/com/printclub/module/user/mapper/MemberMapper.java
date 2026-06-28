package com.printclub.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.printclub.module.user.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * v2 重构：批量查 member，返回 studentId → name 的 Map
     * 替代 5 个 service 里复制粘贴的 selectBatchIds 循环翻译逻辑
     * 空集合直接返回空 Map（不查库）
     */
    default Map<String, String> selectIdNameMap(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        Map<String, String> map = new HashMap<>(ids.size());
        for (Member m : selectBatchIds(ids)) {
            map.put(m.getStudentId(), m.getName());
        }
        return map;
    }
}