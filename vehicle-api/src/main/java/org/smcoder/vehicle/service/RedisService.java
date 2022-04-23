package org.smcoder.vehicle.service;

import org.smcoder.vehicle.redis.RedisKeyTimeRegion;
import org.smcoder.vehicle.redis.RedisValueTrajectorPathList;
import org.smcoder.vehicle.utils.VOConverter;
import org.smcoder.vehicle.vo.VehiclePathVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<RedisKeyTimeRegion, RedisValueTrajectorPathList> redisTemplate;

    public List<VehiclePathVO> getRedisTimeRegionValue(Long startTime, Long endTime, Double topLatitude, Double bottomLatitude, Double leftLongitude, Double rightLongitude) {
        RedisKeyTimeRegion timeRegionKey = new RedisKeyTimeRegion(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude);
        RedisValueTrajectorPathList redisValueTrajectorPathList = redisTemplate.opsForValue().get(timeRegionKey);
        if (redisValueTrajectorPathList != null) {
            return VOConverter.convertRedisTrajectorPathArray2TrajectorPathVOList(redisValueTrajectorPathList.getPaths());
        }
        return null;
    }

    public void putRedisTimeRegionValue(Long startTime, Long endTime, Double topLatitude, Double bottomLatitude, Double leftLongitude, Double rightLongitude, List<VehiclePathVO> vehiclePathVOList) {
        RedisKeyTimeRegion timeRegionKey = new RedisKeyTimeRegion(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude);
        RedisValueTrajectorPathList trajectorPathListValue;
        trajectorPathListValue = new RedisValueTrajectorPathList(VOConverter.convertTrajectorPathVOList2RedisTrajectorPathArray(vehiclePathVOList));
        redisTemplate.opsForValue().set(timeRegionKey, trajectorPathListValue);
    }
}
