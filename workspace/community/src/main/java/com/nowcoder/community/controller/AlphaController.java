package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    // 常用的两个请求对象和响应对象
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod());
        // 请求路径
        System.out.println(request.getServletPath());
        // 通过迭代器得到请求的key 消息头
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        System.out.println(request.getParameter("code"));

        // 返回相应数据 html网页
        response.setContentType("text/html;charset=utf-8");
        try (
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>牛客网<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // GET请求 用于获取数据

    // /student?current=1&limit=20
    // 声明请求的路径和GET方法
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            // 对参数进行注解便于返回默认值，匹配路径中的参数名
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);

        return "some students";
    }

    // 查询某一个人学生
    // /student/123
    // id为变量
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    // @PathVariable注解获取路径变量
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // POST请求 浏览器向服务器提交数据
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应动态HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    // ModelAndView 类封装了model和view的两类数据
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        // 向模板加入参数
        modelAndView.addObject("name", "zhangsan");
        modelAndView.addObject("age", 30);
        // templates 下模板的路径和名字 thymeleaf内无需后缀
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    // 利用model对象传递参数
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        // 添加的方法与ModelAndView不同
        model.addAttribute("name", "pku");
        model.addAttribute("age", 80);
        return "/demo/view";
    }

    // 向浏览器相应JSON数据（异步请求）
    // Java对象 -> JSON字符串 -> JS对象

    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 30);
        map.put("salary", 8000.00);
        return map;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 30);
        map.put("salary", 8000.00);
        list.add(map);

        map = new HashMap<>();
        map.put("name", "lisi");
        map.put("age", 30);
        map.put("salary", 12000.00);
        list.add(map);

        return list;
    }

}