package atm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Jakub Kalinowski-Zajdak
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccountCreateDTO {

	public String pin;
	public String ownerId;
	public long singleWithdrawLimit;
	public double balance;
	
	public AccountCreateDTO() {
		
	}
	
	public AccountCreateDTO(String pPin, String pOwnerId, long pSingleWithdrawLimit, double pBalance) {
		pin = pPin;
		ownerId = pOwnerId;
		singleWithdrawLimit = pSingleWithdrawLimit;
		balance = pBalance;
	}
}
