package com.liato.bankdroid.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.urllib.Urllib;

public class Statoil extends Bank {
	private static final String TAG = "Statoil";
	private static final String NAME = "Statoil";
	private static final String NAME_SHORT = "statoil";
	private static final String URL = "https://applications.sebkort.com/nis/external/stse/login.do";
	private static final int BANKTYPE_ID = Bank.STATOIL;

	private Pattern reAccounts = Pattern.compile("class=\"Right\">([^<]+)<", Pattern.CASE_INSENSITIVE);
	//private Pattern reTransactions = Pattern.compile("trans-date\">([^<]+)</div>.*?trans-subject\">([^<]+)</div>.*?trans-amount\">([^<]+)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	public Statoil(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Statoil(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		Urllib urlopen = new Urllib(true);
		String response = null;
		try {
			response = urlopen.open("https://applications.sebkort.com/nis/external/stse/login.do");
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("choice", "PWD"));
			postData.add(new BasicNameValuePair("uname", username));
			postData.add(new BasicNameValuePair("PASSWORD", password));
			postData.add(new BasicNameValuePair("target", "/nis/stse/main.do"));
			postData.add(new BasicNameValuePair("prodgroup", "0122"));
			postData.add(new BasicNameValuePair("USERNAME", "0122"+username));
			postData.add(new BasicNameValuePair("METHOD", "LOGIN"));
			postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));
			response = urlopen.open("https://applications.sebkort.com/nis/external/stse/login.do", postData);
			if (response.contains("du loggar in till")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		finally {
		}
		return urlopen;
	}

	@Override
	public void update() throws BankException, LoginException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		Urllib urlopen = login();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://applications.sebkort.com/nis/stse/main.do");
			matcher = reAccounts.matcher(response);
			if (matcher.find()) {
				accounts.add(new Account("Statoil MasterCard" , Helpers.parseBalance(matcher.group(1)), "1"));
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
			}
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		finally {
			urlopen.close();
		}

	}
	
	/*@Override
	public void updateTransactions(Account account, Urllib urlopened) throws LoginException, BankException {
		super.updateTransactions(account, urlopened);
		Urllib urlopen = null;
		if (urlopened == null) {
			urlopen = login();
		}
		else {
			urlopen = urlopened;
		}
		if (!urlopen.acceptsInvalidCertificates()) {
			urlopen = login();
		}
		String response = null;
		Matcher matcher;
		try {
			Log.d(TAG, "Opening: https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction("20"+matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
			}
			account.setTransactions(transactions);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (urlopened == null) {
			urlopen.close();
		}
	}	*/
}