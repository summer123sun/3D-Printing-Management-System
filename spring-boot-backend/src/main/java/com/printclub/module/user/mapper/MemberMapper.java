package com.printclub.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.printclub.module.user.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * Member Mapper
 *
 * @author D
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}