package com.nowcoder.community2.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {

    // 替换字符
    private static final String REPLACEMENT = "***";


    // 前缀树 根结点
    private TrieNode root = new TrieNode();

    /**
     * 过滤敏感词
     * @param context
     * @return
     */
    public String replaceSensitiveWords(String context){
        // 指针1
        TrieNode cur = root;
        // 指针2
        int start = 0;
        // 指针3
        int end = 0;

        int len = context.length();
        StringBuilder builder = new StringBuilder();
        while (end < len){

            char c = context.charAt(end);
            TrieNode next = cur.getSubNode(c);

            if(next != null){

                if(next.isEnd()){

                    builder.append(REPLACEMENT);
                    end ++;
                    start = end;
                    cur = root;

                }else{

                    // 用来处理末尾的 不全匹配 的字符串
                    if(end == len - 1){
                        builder.append(context.substring(start,len));
                        break;
                    }

                    end ++;
                    cur = next;

                }

            }else{

                if(cur == root){

                    builder.append(c);
                    end ++;
                    start = end;

                }else{

                    builder.append(context.substring(start,end));
                    start = end;
                    cur = root;

                }

            }

        }


        return  builder.toString();

    }


    /**
     * 构造方法后自动调用
     * 初始化 前缀树
     */
    @PostConstruct
    public void init(){

        try(
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        ){
            String keyword = null;
            while ((keyword = bufferedReader.readLine()) != null){
                // 构造树
                addKeyword(keyword);
            }

        }catch (IOException e){
            log.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 构造树
     * @param keyword
     */
    private void addKeyword(String keyword) {

        TrieNode cur = root;
        for(int i = 0; i < keyword.length(); i ++){
            char c = keyword.charAt(i);
            // 如果字符已存在
            if(cur.getSubNode(c) != null){
                cur = cur.getSubNode(c);
                continue;
            }
            // 如果不存在，创建结点
            TrieNode node = new TrieNode();
            if(i == keyword.length() - 1){
                node.setEnd(true);
            }
            cur.setSubNode(c,node);
            //
            cur = node;
        }
    }




    /**
     * 私有类：前缀树
     */
    private class TrieNode{
        // 是否为叶子结点
        boolean isEnd = false;
        // 子结点（以 map 形式）
        Map<Character,TrieNode> subNode = new HashMap<>();

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }

        public void setSubNode(Character c,TrieNode sub){
            subNode.put(c,sub);
        }


    }
}
