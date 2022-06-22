package com.nowcoder.community2.service;

import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.RedisKeyUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param followerId
     * @param followeeId
     * @return status
     */
    public int follow(int followerId, int followeeId) {


        String followeeKey = RedisKeyUtil.getFolloweeKey(followeeId);
        String followerKey = RedisKeyUtil.getFollowerKey(followerId);

        Boolean isMember = redisTemplate.opsForHash().hasKey(followeeKey, followerKey);


        redisTemplate.execute(new SessionCallback() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                redisTemplate.multi();
                if(isMember){
                    operations.opsForHash().delete(followeeKey,followerId);
                    operations.opsForHash().delete(followerKey,followeeId);
                }else{
                    Date date = new Date();
                    operations.opsForHash().put(followeeKey,followerId,date);
                    operations.opsForHash().put(followerKey,followeeId,date);
                }

                return operations.exec();
            }

        });

        return isMember ? Const.UNFOLLOW_STATUS : Const.FOLLOW_STATUS;
    }

    /**
     * 查找关注的人数
     * @param followerId
     * @return
     */
    public Long followersNum(int followerId){
        String followerKey = RedisKeyUtil.getFollowerKey(followerId);
        return redisTemplate.opsForHash().size(followerKey);
    }

    /**
     * 追随者人数
     * @param followeeId
     * @return
     */
    public Long followeesNum(int followeeId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(followeeId);
        return redisTemplate.opsForHash().size(followeeKey);
    }

    /**
     * 关注的人 或 追随者列表  注意：包含 User + Date
     * 用键值对 K-V 来处理
     * @param userId
     * @param page
     * @param flag 标志位 ——> 0: userId关注的人  1: userId的追随者
     * @return
     */
    public List<Map.Entry> getItems(int flag,int userId, Page page) {

        String key = flag == 0 ? RedisKeyUtil.getFollowerKey(userId) : RedisKeyUtil.getFolloweeKey(userId);

        //按时间降序排列
        TreeSet<Map.Entry> treeSet = new TreeSet<Map.Entry>((e1,e2)-> {
            Date d1 = (Date) e1.getValue();
            Date d2 = (Date) e2.getValue();
            return d1.after(d2) ? -1 : 1;
        });
        treeSet.addAll(redisTemplate.opsForHash().entries(key).entrySet());

        List<Map.Entry> allItems = new ArrayList<>(treeSet);
        List<Map.Entry> pageItems = new ArrayList<>();

        // 分页
        page.setRows(allItems.size());
        for(int i = page.getOffset(); i < page.getOffset() + page.getLimit() && i < allItems.size(); i ++){
            pageItems.add(allItems.get(i));
        }

        return pageItems;
    }

    public boolean hasFollowed(int id, int userId) {
        String followerKey = RedisKeyUtil.getFollowerKey(id);
        return redisTemplate.opsForHash().hasKey(followerKey,userId);
    }
}
