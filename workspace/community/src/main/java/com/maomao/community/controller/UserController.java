package com.maomao.community.controller;

import com.maomao.community.annotaion.LoginRequired;
import com.maomao.community.entity.User;
import com.maomao.community.service.UserService;
import com.maomao.community.util.CommunityUtil;
import com.maomao.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 上传路径
    @Value("${community.path.upload}")
    private String uploadPath;

    // 域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }
        // 避免文件覆盖需要给图片随机生成名字，但是后缀不能改
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        // 生成随机的文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文静
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户头像的路径 web路径
        // http://localhost:8080/community/user/header/xxx/png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // 获取头像
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 解析后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 相应图片
        response.setContentType("image/" + suffix);
        try (
            // io操作加上trycatch
            // 先创建输入流文件输入再输出
            FileInputStream fis = new FileInputStream(fileName);
            OutputStream os = response.getOutputStream();
        ) {
            // 缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }
    // 修改密码
    @RequestMapping(path = "/updatePsw", method = RequestMethod.POST)
    public String updatePsw(Model model, String prePsw, String newPsw, String conPsw) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.UpdatePsw(prePsw, newPsw, conPsw, user.getId());
        if (map == null || map.isEmpty()) {
            model.addAttribute("message", "密码修改成功，正在跳转到首页");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("prePswMs", map.get("prePswMs"));
            model.addAttribute("newPswMs", map.get("newPswMs"));
            model.addAttribute("conPswMs", map.get("conPswMs"));
            return "/site/setting";
        }
    }


}
