package com.maomao.community.service;

import com.maomao.community.dao.LoginTicketMapper;
import com.maomao.community.dao.UserMapper;
import com.maomao.community.entity.LoginTicket;
import com.maomao.community.entity.User;
import com.maomao.community.util.CommunityConstant;
import com.maomao.community.util.CommunityUtil;
import com.maomao.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理，抛出参数异常
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已经存在！");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        // 对密码加密 首先生成五位随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 注册为普通用户
        user.setType(0);
        // 未激活
        user.setStatus(0);
        // 给用户一个激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 随机分配一个头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 注册时间
        user.setCreateTime(new Date());
        // 写回数据库
        userMapper.insertUser(user);

        // 发激活邮件
        // thymeleaf 下面的方法 用于传输变量
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code 激活路径

        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    // 登录业务
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值判断
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        // 验证密码 同样使用加密因为之前数据库存的也是加密之后的密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 生成登录凭证

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headUrl) {
        return userMapper.updateHeader(userId, headUrl);
    }

    public Map<String, Object> UpdatePsw(String prePsw, String newPsw, String conPsw, int userId) {
        HashMap<String, Object> map = new HashMap<>();
        if (prePsw == null) {
            map.put("prePswMs", "原密码不能为空！");
            return map;
        }
        if (newPsw == null) {
            map.put("newPswMs", "新密码不能为空！");
            return map;
        }
        if (!newPsw.equals(conPsw)) {
            map.put("conPswMs", "两次密码不一致！");
            return map;
        }

        //确认密码的判断逻辑是在前端完成的
        //到这一步信息填写完整，下面进行原密码的验证
        User user = userMapper.selectById(userId);
        if (!(user.getPassword().equals(CommunityUtil.md5(prePsw + user.getSalt())))) {
            map.put("prePswMs", "原密码不正确");
            return map;
        }
        //到这里就完成所有错误情况的处理了，下面进行修改密码的操作
        //记得加盐和md5的加密操作，同时要将新生成的salt同步到用户表中
        //String salt = CommunityUtil.generateUUID();
        newPsw = CommunityUtil.md5(newPsw + user.getSalt());
        userMapper.updatePassword(user.getId(), newPsw);
        //于是controller就可以根据map中是否有内容，来选择转发请求回修改界面还是重定向到首页

        return map;

    }
}

