package io.trxplorer.service.dto.account;

public class AssetBalanceDTO {

	private String assetName;
	
	private Long balance;

	public String getAssetName() {
		return assetName;
	}
	
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}
	
}
