package com.maomao.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换敏感词
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                // IO流读取 需要关闭因此放在try后面的括号内
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流转化为字符流再转化为缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        }   catch (IOException e){
                logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }
    //将一个敏感词加入前缀树
    private void addKeyword(String keyword) {
        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指针指向子节点
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤过后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 三个指针
        TrieNode tempNode = root;
        int begin = 0;
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 指针处于根位置，符号计入结果，指针2向下走
                if (tempNode == root) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            // 检查下级几点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = root;
            } else if (tempNode.isEnd()) {
                // 发现敏感词
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = root;
            } else {
                position++;
            }
        }
        // 最后结尾字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号 且不在东亚文字范围
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
    // 前缀树
    private class TrieNode {

        // 结束表示
        private boolean isEnd = false;

        // 子节点 key是下级字符 value是下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        // 添加子节点的方法
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }


    }
}
