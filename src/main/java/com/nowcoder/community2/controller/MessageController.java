package com.nowcoder.community2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.component.EventProducer;
import com.nowcoder.community2.entity.Event;
import com.nowcoder.community2.entity.Message;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.MessageService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.*;
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
        page.setPath("/message/letter/list/"+userId);
        page.setRows(messageService.findConversationCount(userId));

        // 私信总未读消息数 属性添加 ——> 转至 MessageInterceptor

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
        page.setRows(messageService.findLetterCount(0,conversationId,Const.MESSAGE_UNLIMITED));

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
            return CommonUtils.getJSONString(101, Notice.TITLE_EMPTY.getInfo());
        }

        User toUser = userService.findUserByName(toUsername);
        if(toUser == null){
            return CommonUtils.getJSONString(102, Notice.USER_NOT_EXIST.getInfo());
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


        return CommonUtils.getJSONString(0, Notice.SEND_SUCCESS.getInfo());
    }

    @LoginRequired
    @GetMapping("/notice/list/{userId}")
    public String getNotices(@PathVariable("userId") int userId, Model model){

        // comment
        int all_Comment_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_COMMENT, Const.MESSAGE_UNLIMITED);
        int unchecked_Comment_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_COMMENT, Const.MESSAGE_UNCHECKED);
        List<Message> last_Comment_NoticeLetter = messageService.findNoticeLetters(userId, Const.TOPIC_COMMENT, 0, 1);

        // like
        int all_Like_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_LIKE, Const.MESSAGE_UNLIMITED);
        int unchecked_Like_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_LIKE, Const.MESSAGE_UNCHECKED);
        List<Message> last_Like_NoticeLetter = messageService.findNoticeLetters(userId, Const.TOPIC_LIKE, 0, 1);

        // follow
        int all_Follow_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_FOLLOW, Const.MESSAGE_UNLIMITED);
        int unchecked_Follow_Notice_Count = messageService.findLetterCount(userId, Const.TOPIC_FOLLOW, Const.MESSAGE_UNCHECKED);
        List<Message> last_Follow_NoticeLetter = messageService.findNoticeLetters(userId, Const.TOPIC_FOLLOW, 0, 1);

        model.addAttribute("allCommentCount",all_Comment_Notice_Count);
        model.addAttribute("uncheckedCommentCount",unchecked_Comment_Notice_Count);
        model.addAttribute("allLikeCount",all_Like_Notice_Count);
        model.addAttribute("uncheckedLikeCount",unchecked_Like_Notice_Count);
        model.addAttribute("allFollowCount",all_Follow_Notice_Count);
        model.addAttribute("uncheckedFollowCount",unchecked_Follow_Notice_Count);


        // 总未读通知 ——> 转至 MessageInterceptor

        if(last_Comment_NoticeLetter != null && !last_Comment_NoticeLetter.isEmpty()){
            Message message = last_Comment_NoticeLetter.get(0);
            Event event = JSONObject.parseObject(message.getContent(), Event.class);
            User commentUser = userService.findUserById(event.getFromUserId());
            model.addAttribute("lastComment",message);
            model.addAttribute("commentUser",commentUser);

        }
        if(last_Like_NoticeLetter != null && !last_Like_NoticeLetter.isEmpty()){
            Message message = last_Like_NoticeLetter.get(0);
            Event event = JSONObject.parseObject(message.getContent(), Event.class);
            User likeUser = userService.findUserById(event.getFromUserId());
            model.addAttribute("lastLike",message);
            model.addAttribute("likeUser",likeUser);
        }
        if(last_Follow_NoticeLetter != null && !last_Follow_NoticeLetter.isEmpty()){
            Message message = last_Follow_NoticeLetter.get(0);
            Event event = JSONObject.parseObject(message.getContent(), Event.class);
            User followUser = userService.findUserById(event.getFromUserId());
            model.addAttribute("lastFollow",message);
            model.addAttribute("followUser",followUser);
        }

        return "/site/notice";
    }

    @LoginRequired
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetails(@PathVariable("topic") String topic, Page page,Model model){

        User user = hostHolder.get();

        page.setPath("/message/notice/detail/" + topic);
        page.setRows(messageService.findNoticeLetterCount(user.getId(),topic,Const.MESSAGE_UNLIMITED));

        List<Message> noticeLetters = messageService.findNoticeLetters(user.getId(), topic, page.getOffset(), page.getLimit());

        List<HashMap<String,Object>> letters = new ArrayList<>();
        for (Message letter : noticeLetters) {
            HashMap<String,Object> vo = new HashMap<>();
            vo.put("createTime",letter.getCreateTime());

            Event event = JSONObject.parseObject(letter.getContent(), Event.class);
            User fromUser = userService.findUserById(event.getFromUserId());
            vo.put("fromUser",fromUser);

            vo.put("entityId",event.getEntityId());

            letters.add(vo);

        }

        String text;
        if(topic.equals(Const.TOPIC_COMMENT)){
            text = Notice.TEXT_COMMENT.getInfo();
        }else if(topic.equals(Const.TOPIC_LIKE)){
            text = Notice.TEXT_LIKE.getInfo();
        }else{
            text = Notice.TEXT_FOLLOW.getInfo();
        }
        model.addAttribute("text",text);
        model.addAttribute("letters",letters);

        return "/site/notice-detail";
    }



}
