package com.liuh.gallerybackend.mananger.sharding;

import cn.hutool.core.util.ObjUtil;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * @Author LiuH
 * @Date 2025/10/11 下午7:50
 * @PackageName com.liuh.gallerybackend.mananger.sharding
 * @ClassName PictureShardingAlgorithm
 * @Version
 * @Description 图片分表实现类
 */

@SuppressWarnings("all")
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     *
     * @param availableTargetNames 所有支持的分表
     * @param preciseShardingValue sharding-column: spaceId 根据 spaceId 动态生成分表名
     * @return
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName(); //逻辑表
        // spaceId 为 null 表示查询所有图片
        if (ObjUtil.isNull(spaceId)) {
            return logicTableName;
        }
        // 根据 spaceId 动态生成分表名
        String realTableName = "picture_" + spaceId;

        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        } else { //在该分表找不到, 则查找所有的表
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
