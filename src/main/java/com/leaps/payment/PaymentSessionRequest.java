package com.leaps.payment;

import com.google.gson.JsonObject;
public class PaymentSessionRequest {

	private String merchantAccount;
	private String channel;
	private Amount amount;
	private String reference;
	private String countryCode;
	private String shopperLocale;
	private String token;
	private String returnUrl;

	public PaymentSessionRequest(JsonObject obj) {
		this.channel = obj.get("channel").getAsString();
		this.reference = obj.get("reference").getAsString();
		this.countryCode = obj.get("countryCode").getAsString();
		this.shopperLocale = obj.get("shopperLocale").getAsString();
		this.token = obj.get("token").getAsString();
		this.returnUrl = obj.get("returnUrl").getAsString();
		Amount amount = new Amount();
		amount.setCurrency(obj.get("currency").getAsString());
		amount.setValue(obj.get("value").getAsString());
		this.amount = amount;
	}

	public String getMerchantAccount() {
		return merchantAccount;
	}

	public void setMerchantAccount(String merchantAccount) {
		this.merchantAccount = merchantAccount;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getShopperLocale() {
		return shopperLocale;
	}

	public void setShopperLocale(String shopperLocale) {
		this.shopperLocale = shopperLocale;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

}
