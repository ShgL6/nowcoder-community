package com.nowcoder.community2.controller;

import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.Message;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.MessageService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import com.nowcoder.community2.utils.SensitiveFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @LoginRequired
    @GetMapping("/letter/list/{userId}")
    public String getMessages(@PathVariable("userId") int userId, Page page, Model model){

        // 设置分页
        page.setPath("/message/letter/"+userId);
        page.setRows(messageService.findConversationCount(userId));

        // 私信总未读消息数 属性添加
        int allUncheckedLetterCount = messageService.findLetterCount(userId, null, 0);
        model.addAttribute("allUncheckedCount", allUncheckedLetterCount);


        // 装填视图层的对象
        List<String> conversationIds = messageService.findConversationIds(userId, page.getOffset(), page.getLimit());

        if(conversationIds != null){

            List<Map<String,Object>> conversationCardVos = new ArrayList<>();
            for(String conversationId : conversationIds){
                Map<String,Object> ccvo = new HashMap<>();

                // 该会话中的最新消息
                Message lastLetter = messageService.findLetters(conversationId,0,1).get(0);
                // 该会话中的未读消息数
                int uncheckedLetterCount = messageService.findLetterCount(userId, conversationId, 0);
                // 该会话中的所有消息数
                int allLettersCount = messageService.findLetterCount(0,conversationId,-1);
                // 该会话的对象
                int fromId = userId == lastLetter.getFromId() ? lastLetter.getToId() : lastLetter.getFromId();
                User fromUser = userService.findUserById(fromId);

                ccvo.put("lastLetter",lastLetter);
                ccvo.put("uncheckedLetterCount",uncheckedLetterCount);
                ccvo.put("allLetterCount", allLettersCount);
                ccvo.put("fromUser",fromUser);

                conversationCardVos.add(ccvo);

            }
            model.addAttribute("conversations",conversationCardVos);

        }else {

            model.addAttribute("conversations",null);

        }

        return "/site/letter";
    }

    @LoginRequired
    @GetMapping("/letter/detail/{conversationId}")
    public String letterDetails(@PathVariable("conversationId") String conversationId, Page page,Model model){

        // 分页
        page.setPath("/message/letter/detail"+conversationId);
        page.setRows(messageService.findLetterCount(0,conversationId,-1));

        // 获取,装填会话对象
        User user = hostHolder.get();
        int userId= user.getId();
        String[] strings = conversationId.split("_");
        int targetId = Integer.valueOf(strings[0]) == userId ? Integer.valueOf(strings[1]) : Integer.valueOf(strings[0]);
        User target = userService.findUserById(targetId);
        model.addAttribute("target",target);

        // 获取信息列表,更新信息为已读
        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        if(letters != null){

            List<Map<String,Object>> letterVos = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Message letter : letters){

                Map<String,Object> lvo = new HashMap<>();

                // fromUser
                lvo.put("fromUser",letter.getFromId() == userId ? user : target);
                // letter
                lvo.put("letter",letter);

                // 获取待更新已读的信息的 id ,注意 toId 条件
                if(letter.getStatus() == 0 && letter.getToId() == userId){
                    ids.add(letter.getId());
                }

                letterVos.add(lvo);
            }

            //更新为已读
            messageService.read(ids);
            model.addAttribute("letters",letterVos);

        }else{
            model.addAttribute("letters",null);
        }

        return "/site/letter-detail";
    }

    @LoginRequired
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toUsername,String content){

        if(StringUtils.isBlank(toUsername)){
            return CommonUtils.getJSONString(101,Const.TITLE_EMPTY.getInfo());
        }

        User toUser = userService.findUserByName(toUsername);
        if(toUser == null){
            return CommonUtils.getJSONString(102,Const.USER_NOT_EXIST.getInfo());
        }

        Message message = new Message();
        // 转义&过滤敏感词
        message.setContent(sensitiveFilter.replaceSensitiveWords(HtmlUtils.htmlEscape(content)));
        message.setFromId(hostHolder.get().getId());
        message.setToId(toUser.getId());
        message.setCreateTime(new Date());
        message.setStatus(0);
        String conversationId = Math.min(message.getFromId(),message.getToId()) + "_" + Math.max(message.getFromId(),message.getToId());
        message.setConversationId(conversationId);

        messageService.saveMessage(message);

        return CommonUtils.getJSONString(0, Const.SEND_SUCCESS.getInfo());
    }


}
