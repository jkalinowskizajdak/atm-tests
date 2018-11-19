package atm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import atm.dto.AccountCreateDTO;
import atm.dto.AccountDTO;
import atm.dto.OperationDTO;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * @author Jakub Kalinowski-Zajdak
 */
public class AtmTest {
	
	private static final String pin_1 = "1245";
	private static final String pin_2 = "6789";
	private static final double balance_1 = 50000;
	private static final double balance_2 = 80000;
	private static final long limit_1 = 500;
	private static final long limit_2 = 1000;
	private static final UUID ownerId_1 = UUID.randomUUID();
	private static final UUID ownerId_2 = UUID.randomUUID();
	private static final AccountCreateDTO testAccount_1 = new AccountCreateDTO(pin_1, ownerId_1.toString(), limit_1, balance_1);  
	private static final AccountCreateDTO testAccount_2 = new AccountCreateDTO(pin_2, ownerId_2.toString(), limit_2, balance_2);
    private static final int OK = 200;
    private static final int NO_CONTENT = 204;
    private static final int ERROR = 500;
    private static final String ATM_PATH = "/rest/atm";
    
	static {
		RestAssured.config = RestAssuredConfig.config()
				.objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> {
					ObjectMapper objectMapper = new ObjectMapper();
					return objectMapper;
				}));
	}

	private static final String HOST = System.getProperty("HOST", "localhost");
	private static final int PORT = Integer.parseInt(System.getProperty("PORT", "8080"));

	public static RequestSpecification given() {
		return RestAssured.given().baseUri("http://" + HOST).port(PORT).contentType(ContentType.JSON);
	}
	
	private static String accountId_1;
	private static String accountId_2;
	
	@BeforeClass
	public static void addAccounts() {
		accountId_1 = addAccount(testAccount_1);
		accountId_2 = addAccount(testAccount_2);
	}
	
	@AfterClass
	public static void deleteAccounts() {
		deleteAccount(accountId_1);
		deleteAccount(accountId_2);
	}
	
	private static String addAccount(AccountCreateDTO account) {
		return given()
			.body(account)
	        .post(ATM_PATH)
	        .then()
	        .assertThat()
	        .statusCode(OK)
	        .extract()
	        .asString();
	}
	
	private static void deleteAccount(String accountId) {
		given()
			.pathParam("accountId", accountId)
			.delete(ATM_PATH + "/{accountId}")
			.then()
			.assertThat()
			.statusCode(NO_CONTENT);
	}
	
	@Test
	public void checkAccountsBalanceTest() {
		checkRequestWihInvalidPin(accountId_1, pin_2);
		checkRequestWihInvalidPin(accountId_2, pin_1);
		checkBalance(accountId_1, pin_1, balance_1);
		checkBalance(accountId_2, pin_2, balance_2);
	}
	
	private void checkRequestWihInvalidPin(String id, String pin) {
		given()
			.pathParam("accountId", id)
			.queryParam("pin", pin)
			.get(ATM_PATH + "/{accountId}/balance")
			.then()
			.assertThat()
			.statusCode(ERROR);
	}
	
	private void checkBalance(String id, String pin, double balance) {
		double requestBalance = given()
				.pathParam("accountId", id)
				.queryParam("pin", pin)
				.get(ATM_PATH + "/{accountId}/balance")
				.then()
				.extract()
				.as(Double.class);
			Assertions.assertThat(requestBalance).isEqualTo(balance);
	}
	
	@Test
	public void checkAccountsTest() {
		AccountDTO[] requestAccounts = given()
			.get(ATM_PATH + "/accounts")
			.then()
			.extract()
			.as(AccountDTO[].class);
		List<String> accountIds = new ArrayList<>(requestAccounts.length); 
		for (AccountDTO dto : requestAccounts) {
			accountIds.add(dto.id);
		}
		Assertions.assertThat(accountIds).contains(accountId_1, accountId_2);
		AccountDTO[] owner_1_dtos = getOwnerAccounts(ownerId_1.toString());
		List<String> owner_1_accounts= new ArrayList<>(owner_1_dtos.length); 
		for (AccountDTO dto : owner_1_dtos) {
			owner_1_accounts.add(dto.id);
		}
		Assertions.assertThat(owner_1_accounts).contains(accountId_1);
		AccountDTO[] owner_2_dtos = getOwnerAccounts(ownerId_2.toString());
		List<String> owner_2_accounts= new ArrayList<>(owner_2_dtos.length); 
		for (AccountDTO dto : owner_2_dtos) {
			owner_2_accounts.add(dto.id);
		}
		Assertions.assertThat(owner_2_accounts).contains(accountId_2);
	}
	
	private AccountDTO[] getOwnerAccounts(String ownerId) {
		return given()
				.pathParam("ownerId", ownerId)
				.get(ATM_PATH + "/{ownerId}/accounts")
				.then()
				.extract()
				.as(AccountDTO[].class);
	}
	
	@Test
	public void checkWithdrawTest() {
		withdrawInvalidValue(accountId_1, pin_1, 345);
		int value = 100;
		double newBalance_1 = withdraw(accountId_1, pin_1, value);
		Assertions.assertThat(newBalance_1).isEqualTo(balance_1 - value);
		withdrawOverTheLimit(accountId_1, pin_1, (int)limit_1 + 1);
		withdrawOverTheLimit(accountId_2, pin_2, (int)limit_2 + 1);
		withdrawOverTheLimit(accountId_1, pin_1, (int)balance_1 + 1);
		withdrawOverTheLimit(accountId_2, pin_2, (int)balance_2 + 1);
	}
	
	@Test
	public void checkDepositTest() {
		depositInvalidValue(accountId_1, pin_1, 345);
		int value = 100;
		double newBalance_2 = deposit(accountId_2, pin_2, value);
		Assertions.assertThat(newBalance_2).isEqualTo(balance_2 + value);
	}
	
	private void withdrawOverTheLimit(String accountId, String pin, int value) {
		withdrawInvalidValue(accountId, pin, value);
	}
	
	private double withdraw(String accountId, String pin, int value) {
		return given()
			.body(new OperationDTO("withdraw", value))
			.pathParam("accountId", accountId)
			.queryParam("pin", pin)
			.put(ATM_PATH + "/{accountId}")
			.then()
			.assertThat()
			.statusCode(OK)
			.extract()
			.as(Double.class);
	}
	
	private double deposit(String accountId, String pin, int value) {
		return given()
			.body(new OperationDTO("deposit", value))
			.pathParam("accountId", accountId)
			.queryParam("pin", pin)
			.put(ATM_PATH + "/{accountId}")
			.then()
			.assertThat()
			.statusCode(OK)
			.extract()
			.as(Double.class);
	}
	
	private void depositInvalidValue(String accountId, String pin, int value) {
		given()
			.body(new OperationDTO("deposit", value))
			.pathParam("accountId", accountId)
			.queryParam("pin", pin)
			.put(ATM_PATH + "/{accountId}")
			.then()
			.assertThat()
			.assertThat()
			.statusCode(ERROR);
	}
	
	private void withdrawInvalidValue(String accountId, String pin, int value) {
		given()
			.body(new OperationDTO("withdraw", value))
			.pathParam("accountId", accountId)
			.queryParam("pin", pin)
			.put(ATM_PATH + "/{accountId}")
			.then()
			.assertThat()
			.statusCode(ERROR);
	}

}
