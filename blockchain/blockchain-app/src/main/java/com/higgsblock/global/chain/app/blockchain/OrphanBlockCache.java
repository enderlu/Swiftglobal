package com.higgsblock.global.chain.app.blockchain;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
public class OrphanBlockCache {

    private Map<String, BlockFullInfo> map = Maps.newHashMap();

    private int limit;

    public OrphanBlockCache(int limit) {
        this.limit = limit;
    }

    public synchronized void add(BlockFullInfo blockFullInfo) {
        String hash = blockFullInfo.getBlock().getHash();
        map.put(hash, blockFullInfo);
        if (size() <= limit) {
            return;
        }

        List<String> list = map.values().stream()
                .sorted((o1, o2) -> (int) (o1.getBlock().getHeight() - o2.getBlock().getHeight()))
                .map(obj -> obj.getBlock().getHash())
                .skip(limit)
                .collect(Collectors.toList());

        for (String item : list) {
            remove(item);
        }
    }

    public synchronized BlockFullInfo remove(String key) {
        return map.remove(key);
    }

    public synchronized BlockFullInfo get(String key) {
        return map.get(key);
    }

    public synchronized Collection<BlockFullInfo> values() {
        return map.values();
    }

    public synchronized int size() {
        return map.size();
    }
}
