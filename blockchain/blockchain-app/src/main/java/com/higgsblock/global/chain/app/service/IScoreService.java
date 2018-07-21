package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;

import java.util.List;
import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
public interface IScoreService {

    /**
     * init score
     */
    int INIT_SCORE = 600;

    /**
     * old score strategy
     */
    int MINUS_SCORE_PACKAGED_BEST = -20;

    //new score strategy
    /**
     * init score for lucky miner
     */
    int SELECTED_DPOS_SET_SCORE = 600;
    /**
     * lucky miner score
     */
    int MINED_BLOCK_SET_SCORE = 800;
    /**
     * offline miner score
     */
    int OFFLINE_MINER_SET_SCORE = 0;
    /**
     * plus score
     */
    int ONE_BLOCK_ADD_SCORE = 1;
    /**
     * get score by address
     *
     * @param address
     * @return
     */
    Integer get(String address);

    /**
     * set score
     *
     * @param address
     * @param score
     * @return
     */
    void put(String address, Integer score);

    /**
     * batch update
     *
     * @param addressList
     * @param score
     * @return
     */
    int updateBatch(List<String> addressList, int score);

    /**
     * Score all the records
     * add or sub score
     *
     * @param score
     * @return
     */
    int plusAll(Integer score);

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     */
    void putIfAbsent(String address, Integer score);

    /**
     * remove score
     *
     * @param address
     * @return
     */
    void remove(String address);

    /**
     * load all score
     *
     * @return
     */
    Map<String, Integer> loadAll();

    List<ScoreEntity> all();
}
