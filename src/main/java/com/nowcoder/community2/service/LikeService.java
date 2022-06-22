package com.nowcoder.community2.service;

import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.RedisKeyUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;


    // 点赞
    public void like(int entityType,int entityId,int userId,int targetUserId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(targetUserId);
                // 是否已点赞
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);

                redisTemplate.multi();

                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }


                return operations.exec();
            }
        });


    }

    // 查询实体点赞数量
    public int getUserLikeCount(int userId){

        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);

        return count == null ? 0 : count;
    }


    public long getEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查看点赞状态
    public int getEntityLikeStatus(int entityType, int entityId, int userId) {

        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? Const.LIKE_STATUS : Const.UNLIKE_STATUS;
    }
}
