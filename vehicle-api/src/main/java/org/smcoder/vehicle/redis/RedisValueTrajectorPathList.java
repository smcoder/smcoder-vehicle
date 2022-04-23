package org.smcoder.vehicle.redis;

import java.io.Serializable;

public class RedisValueTrajectorPathList implements Serializable {
    private RedisTrajectorPath[] paths;

    public RedisValueTrajectorPathList(RedisTrajectorPath[] paths) {
        this.paths = paths;
    }

    public RedisTrajectorPath[] getPaths() {
        return paths;
    }

    public void setPaths(RedisTrajectorPath[] paths) {
        this.paths = paths;
    }
}
