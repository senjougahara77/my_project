package com.maomao.community.service;

import com.maomao.community.dao.AlphaDao;
import com.maomao.community.dao.DiscussPostMapper;
import com.maomao.community.dao.UserMapper;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.User;
import com.maomao.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    public String find() {
        return alphaDao.select();
    }

    // 配置事物的隔离等级和传播机制
    // REQUIRED：支持当前事务（外部事务），如果不存在就创建新事务。
    // REQUIRES_NEW：创建新的事务，并且暂停当前事务（外部事务）。
    // NESTED：如果当期存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则就和REQUIRED一样。
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123") + user.getSalt());
        user.setEmail("alpha@qq.com");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("baodao");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 制造一个错误 检查回滚
        Integer.valueOf("abc");
        return "ok";
    }
    // 演示编程式事务

    public Object save2() {
        // 调用这个bean内部的api设置隔离等级和传播机制
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123") + user.getSalt());
                user.setEmail("beat@qq.com");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("hi");
                post.setContent("bbb");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                // 制造一个错误 检查回滚
                Integer.valueOf("abc");

                return "ok";
            }
        });
    }
}
