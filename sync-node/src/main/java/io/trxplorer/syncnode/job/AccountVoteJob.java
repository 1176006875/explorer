package io.trxplorer.syncnode.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.trxplorer.syncnode.SyncNodeConfig;
import io.trxplorer.syncnode.service.AccountSyncService;
import lombok.extern.slf4j.Slf4j;
import org.jooby.quartz.Scheduled;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

@Singleton
@Slf4j
public class AccountVoteJob implements StatefulJob {

    private AccountSyncService accountSyncService;
    private SyncNodeConfig config;

    @Inject
    public AccountVoteJob(AccountSyncService accountSyncService, SyncNodeConfig config) {
        this.accountSyncService = accountSyncService;
        this.config = config;
    }

    @Override
    @Scheduled("20ms")
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!this.config.isAccountJobEnabled()) {
            return;
        }
        try {
            this.accountSyncService.syncAccountVote();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
