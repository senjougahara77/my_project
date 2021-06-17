package com.maomao.community.dao;

import com.maomao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 用户名便于查询个人主页 每一页的起始行数和总数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // param注解起名 便于sql
    // 如果该方法只有一个参数 且<if>动态sql里使用，必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);
}
