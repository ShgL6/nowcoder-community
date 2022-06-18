package com.nowcoder.community2.service;


import com.nowcoder.community2.dao.LoginTicketMapper;
import com.nowcoder.community2.entity.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginTicketService {

    @Autowired
    private LoginTicketMapper ticketMapper;

    public int saveLoginTicket(LoginTicket ticket){
        return ticketMapper.insertLoginTicket(ticket);
    }

    public int modifyStatus(String ticket,int status){
        return ticketMapper.updateTicketStatus(ticket, status);
    }

    public LoginTicket findByTicket(String ticket){
        return ticketMapper.selectByTicket(ticket);
    }
}
