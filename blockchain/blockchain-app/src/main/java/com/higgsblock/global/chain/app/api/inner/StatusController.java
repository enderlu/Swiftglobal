package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.vo.ConnectionVO;
import com.higgsblock.global.chain.app.api.vo.DposGroupVO;
import com.higgsblock.global.chain.app.api.vo.PeerVO;
import com.higgsblock.global.chain.app.api.vo.SimpleBlockVO;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * status info controller
 *
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/status")
@RestController
@Slf4j
public class StatusController {

    @Autowired
    private IScoreService scoreService;
    @Autowired
    private IDposService dposService;
    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SystemStatusManager systemStatusManager;

    /**
     * query state
     *
     * @return
     */
    @RequestMapping("info")
    public Object info() {
        return systemStatusManager.getSystemStatus();
    }

    /**
     * query connections of current peer
     *
     * @return
     */
    @RequestMapping("/connections")
    public Object connections() {
        return connectionManager.getAllConnections().stream()
                .map(connection -> {
                    ConnectionVO info = new ConnectionVO();
                    info.setChannelId(connection.getChannelId());
                    info.setPeerId(connection.getPeerId());
                    info.setIp(connection.getIp());
                    info.setPort(connection.getPort());
                    info.setAge(TimeUnit.MILLISECONDS.toSeconds(connection.getAge()) + "s");
                    info.setActivated(connection.isActivated());
                    info.setType(connection.getType());
                    info.setConnectionLevel(connection.getConnectionLevel());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * query score by address
     *
     * @param address
     * @return
     */
    @RequestMapping("/score")
    public Object score(String address) {
        return scoreService.get(address);
    }

    /**
     * query miners
     *
     * @param blockHash
     * @return
     */
    @RequestMapping("/miners")
    public Object miners(String blockHash) {
        if (StringUtils.isEmpty(blockHash)) {
            return null;
        }
        Block block = blockService.getBlockByHash(blockHash);
        if (block == null) {
            LOGGER.warn("the block not found by blockhash:{}", blockHash);
            return null;
        }
        long height = block.getHeight();
        long startHeight = dposService.getStartHeight(height);
        DposGroupVO dposGroupVO = buildDposGroup(block);

        while (height-- > startHeight) {
            block = blockService.getBlockByHash(block.getPrevBlockHash());
            if (block == null) {
                break;
            }
            SimpleBlockVO simpleBlockVO = new SimpleBlockVO(block);
            dposGroupVO.getBlockVOS().add(simpleBlockVO);
            dposGroupVO.getLeftDposNodes().remove(simpleBlockVO.getMinerAddress());
        }
        dposGroupVO.getBlockVOS().sort((o1, o2) -> (int) (o1.getHeight() - o2.getHeight()));

        return dposGroupVO;
    }

    /**
     * query info of current peer
     *
     * @return
     */
    @RequestMapping("/peer")
    public Object peer() {
        Peer self = peerManager.getSelf();
        PeerVO vo = new PeerVO();
        vo.setAddress(self.getId());
        vo.setHttpServerPort(self.getHttpServerPort());
        vo.setSocketServerPort(self.getSocketServerPort());
        vo.setIp(self.getIp());
        vo.setPubKey(self.getPubKey());
        return vo;
    }


    private DposGroupVO buildDposGroup(Block block) {
        long sn = dposService.getSn(block.getHeight());
        DposGroupVO dposGroupVO = new DposGroupVO();
        dposGroupVO.setSn(sn);
        dposGroupVO.setStartHeight(dposService.getStartHeight(block.getHeight()));
        dposGroupVO.setEndHeight(dposService.getEndHeight((block.getHeight())));
        dposGroupVO.getBlockVOS().add(new SimpleBlockVO(block));
        dposGroupVO.setDposNodes(dposService.getDposGroupBySn(sn));
        dposGroupVO.getLeftDposNodes().addAll(dposGroupVO.getDposNodes());
        dposGroupVO.getLeftDposNodes().remove(dposGroupVO.getBlockVOS().get(0).getMinerAddress());
        return dposGroupVO;
    }
}
