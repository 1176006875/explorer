package io.trxplorer.syncnode.job;

import com.google.inject.Inject;
import io.trxplorer.syncnode.SyncNodeConfig;
import io.trxplorer.syncnode.service.BlockSyncService;
import io.trxplorer.troncli.TronFullNodeCli;
import org.jooby.quartz.Scheduled;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockSyncFullNode implements StatefulJob {

    private TronFullNodeCli fullNodeClient;
    private BlockSyncService blockSyncService;
    private SyncNodeConfig config;
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncFullNode.class);

    @Inject
    public BlockSyncFullNode(BlockSyncService syncBlockService, TronFullNodeCli tronFullNodeCli, SyncNodeConfig config) {
        this.fullNodeClient = tronFullNodeCli;
        this.blockSyncService = syncBlockService;
        this.config = config;
    }

    @Scheduled("500ms")
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!this.config.isBlockJobEnabled()) {
            return;
        }

        Long lastBlockNum = fullNodeClient.getLastBlock().getBlockHeader().getRawData().getNumber();

        logger.info("current full node block:" + lastBlockNum);

        if (this.blockSyncService.isInitialSync()) {
            logger.info("Initial import ... that might take a moment, grab a coffe ...");
        }

        try {
            this.blockSyncService.syncNodeFull(lastBlockNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
