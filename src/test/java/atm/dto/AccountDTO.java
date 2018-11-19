package atm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Jakub Kalinowski-Zajdak
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccountDTO {

	public String pin;
	public String ownerId;
	public String id;
	public long singleWithdrawLimit;
	public double balance;
	
	public AccountDTO() {
		
	}
	
	public AccountDTO(String pId, String pPin, String pOwnerId, long pSingleWithdrawLimit, double pBalance) {
		id = pId;
		pin = pPin;
		ownerId = pOwnerId;
		singleWithdrawLimit = pSingleWithdrawLimit;
		balance = pBalance;
	}
}
