package com.maomao.community.controller;

import com.google.code.kaptcha.Producer;
import com.maomao.community.entity.User;
import com.maomao.community.service.UserService;
import com.maomao.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    // 创建日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的注册邮箱发送了激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }

    }


    // http://localhost:8080/community/activation/101/code 激活路径
   @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功！");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "该账号已经激活过了！");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
   }
   // 请求返回验证码图片
   // 验证码不能存到浏览器端（敏感信息）
   @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
   public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
       String text = kaptchaProducer.createText();
       BufferedImage image = kaptchaProducer.createImage(text);

       // 将验证码存入session
       session.setAttribute("kaptcha", text);

       // 将图片输出给浏览器
       response.setContentType("image/png");
       try {
           OutputStream os = response.getOutputStream();
           ImageIO.write(image, "png", os);
       } catch (IOException e) {
           logger.error("相应验证码失败" + e.getMessage());
       }
   }

   @RequestMapping(path = "/login", method = RequestMethod.POST)
   public String login(String username, String password, String code, boolean rememberme,
                       Model model, HttpSession session, HttpServletResponse response) {
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 验证码为空、输入为空、验证码输入错误（验证码不区分大小写），返回验证码错误，返回登录页面
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        // 验证账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECOND : DEFAULT_EXPIRED_SECOND;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        // cookie存储ticket
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
   }

   @RequestMapping(path = "/logout", method = RequestMethod.GET)
   public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
   }
}
