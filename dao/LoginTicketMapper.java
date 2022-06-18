package com.nowcoder.community2.dao;

import com.nowcoder.community2.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LoginTicketMapper {


    @Insert({"insert into login_ticket(user_id,ticket,status,expired)"
            ,"values(#{userId},#{ticket},#{status},#{expired})"})
    @Options(useGeneratedKeys = true)
    int insertLoginTicket(LoginTicket ticket);

    @Select({"select id,user_id,ticket,status,expired from login_ticket where ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);

//    @Select({"select id,user_id,ticket,status,expired from login_ticket where userId = #{userId}"})
//    LoginTicket selectByUserId(int userId);

    @Update({"update login_ticket set status = #{status} where ticket = #{ticket}"})
    int updateTicketStatus(String ticket,int status);
}
