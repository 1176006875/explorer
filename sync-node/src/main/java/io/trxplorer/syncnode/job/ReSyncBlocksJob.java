package io.trxplorer.syncnode.job;

import com.google.inject.Inject;
import io.trxplorer.syncnode.SyncNodeConfig;
import io.trxplorer.syncnode.service.BlockService;
import io.trxplorer.troncli.TronFullNodeCli;
import org.jooby.quartz.Scheduled;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.trxplorer.model.Tables.BLOCK;
import static io.trxplorer.model.Tables.BLOCK_RESYNC;

public class ReSyncBlocksJob implements StatefulJob {

    private DSLContext dslContext;
    private BlockService blockService;
    private TronFullNodeCli fullNodeCli;
    private SyncNodeConfig config;
    private static final Logger logger = LoggerFactory.getLogger(ReSyncBlocksJob.class);

    @Inject
    public ReSyncBlocksJob(DSLContext dslContext, BlockService blockService, TronFullNodeCli fullNodeCli, SyncNodeConfig config) {
        this.dslContext = dslContext;
        this.blockService = blockService;
        this.fullNodeCli = fullNodeCli;
        this.config = config;
    }

    @Scheduled("1m")
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        if (!this.config.isResyncJobEnabled()) {
            return;
        }

        List<ULong> blocks = this.dslContext.select(BLOCK_RESYNC.NUM)
                .from(BLOCK_RESYNC)
                .limit(500)
                .fetchInto(ULong.class);

        for (ULong blockNum : blocks) {

            logger.info("[RESYNC] => Resyncing block:" + blockNum);

            this.dslContext.deleteFrom(BLOCK).where(BLOCK.NUM.eq(blockNum)).execute();
            try {
                this.blockService.importBlock(this.fullNodeCli.getBlockByNum(blockNum.longValue()));
                this.blockService.confirmBlock(blockNum.longValue());
                this.dslContext.deleteFrom(BLOCK_RESYNC).where(BLOCK_RESYNC.NUM.eq(blockNum)).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
