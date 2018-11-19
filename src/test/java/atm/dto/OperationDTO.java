package atm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Jakub Kalinowski-Zajdak
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OperationDTO {
	
    public String type;
    public int amount;
    
    public OperationDTO() {
    	
    }
    
    public OperationDTO(String pType, int pAmount) {
    	type = pType;
    	amount = pAmount;
    }
}
