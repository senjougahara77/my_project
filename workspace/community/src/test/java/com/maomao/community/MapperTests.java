package com.maomao.community;

import com.maomao.community.dao.DiscussPostMapper;
import com.maomao.community.dao.UserMapper;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        User user1 = userMapper.selectByEmail("nowcoder21@sina.com");
        System.out.println(user1);
    }

    @Test
    public void insertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("111444");
        user.setSalt("wee");
        user.setEmail("eee@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101/png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102/png");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
