package io.trxplorer.syncnode.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.trxplorer.syncnode.SyncNodeConfig;
import io.trxplorer.syncnode.service.AccountSyncService;
import io.trxplorer.syncnode.service.ServiceException;
import org.jooby.quartz.Scheduled;

@Singleton
public class AccountSyncJob {

    private AccountSyncService accountSyncService;
    private SyncNodeConfig config;

    @Inject
    public AccountSyncJob(AccountSyncService accountSyncService, SyncNodeConfig config) {
        this.accountSyncService = accountSyncService;
        this.config = config;
    }

    //10ms的话，数据库性能跟不上
//	@Scheduled("50ms")
//	public void syncAccount() throws ServiceException {
//		log.info("syncAccount50-start");
//		if (!this.config.isAccountJobEnabled()) {
//			return;
//		}
//
//		this.accountSyncService.syncAccounts();
//		log.info("syncAccount50-end");
//	}

    @Scheduled("20ms")
    public void syncAccountVote() throws ServiceException {

        if (!this.config.isAccountJobEnabled()) {
            return;
        }

        this.accountSyncService.syncAccountVote();

    }

    @Scheduled("30ms")
    public void syncAccountResync() throws ServiceException {

        if (!this.config.isAccountJobEnabled()) {
            return;
        }

        this.accountSyncService.syncAccountResync();

    }


    @Scheduled("5m")
    public void removeLocks() {
        this.accountSyncService.removeLocks();
    }

    public void syncGenesisAccounts() {
        //TODO:
        //Genesis accounts might be used without any transactions appearing on blockchain : for example block rewarding
        // These accounts are updated here

    }

}
