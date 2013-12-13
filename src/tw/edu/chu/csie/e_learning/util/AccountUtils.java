/*
 * 無所不在學習架構與學習導引機制
 * A Hybrid Ubiquitous Learning Framework and its Navigation Support Mechanism
 * 
 * FileName:	AccountUtils.java
 * 
 * Description: 帳號控制管理（登入、驗證、登入狀況...）的程式
 * 
 */
package tw.edu.chu.csie.e_learning.util;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import tw.edu.chu.csie.e_learning.R;
import tw.edu.chu.csie.e_learning.config.Config;
import tw.edu.chu.csie.e_learning.provider.ClientDBProvider;
import tw.edu.chu.csie.e_learning.server.BaseSettings;
import tw.edu.chu.csie.e_learning.server.ServerAPIs;
import tw.edu.chu.csie.e_learning.server.ServerUser;
import tw.edu.chu.csie.e_learning.server.exception.HttpException;
import tw.edu.chu.csie.e_learning.server.exception.LoginCodeException;
import tw.edu.chu.csie.e_learning.server.exception.LoginException;
import tw.edu.chu.csie.e_learning.server.exception.PostNotSameException;
import tw.edu.chu.csie.e_learning.server.exception.ServerException;

public class AccountUtils {
	
	private Context context;
	private boolean isLogined;
	private String loginedId;
	private String loginCode;
	private ClientDBProvider clientdb;
	private ServerAPIs server;
	
	public AccountUtils(Context context) {
		this.context = context;
		clientdb = new ClientDBProvider(this.context);
		
		// 伺服端連線物件建立
		BaseSettings srvbs = new BaseSettings();
		srvbs.setBaseUrl(Config.REMOTE_BASE_URL);
		server = new ServerAPIs(srvbs);
	}
	
	/**
	 * 是否是已登入狀態
	 */
	public boolean islogin(){
		return isLogined;
	}
	
	/**
	 * 察看已登入的ID
	 */
	public String getLoginId() {
		return loginedId;
	}
	
	/**
	 * 取得登入碼
	 */
	public String getLoginCode() {
		return loginCode;
	}
	
	/**
	 * 登入帳號
	 * @param inputLoginId 使用者輸入的ID
	 * @param inputLoginPasswd 使用者輸入的密碼
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws JSONException 
	 * @throws PostNotSameException 
	 * @throws HttpException 
	 * @throws ServerException 
	 * 
	 * TODO ClientProtocolException, IOException, JSONException 例外整理
	 * @throws LoginCodeException 
	 */
	public void loginUser(String inputLoginId, String inputLoginPasswd) 
			throws ClientProtocolException, IOException, JSONException, LoginException, PostNotSameException, HttpException, ServerException, LoginCodeException
	{
		this.loginCode = this.server.userLogin(inputLoginId, inputLoginPasswd);
		ServerUser userinfo = this.server.userGetInfo(this.loginCode);
		String nickName = userinfo.getNickName();
		String loginTime = userinfo.getLoginTime();
		
		Log.d("nickName",nickName );
		Log.d("loginTime", loginTime);
		Log.d("loginCode", loginCode);
		Log.d("ID", userinfo.getID());
		
		//將傳回來的資料寫入SQLite裡
		this.clientdb.user_insert(userinfo.getID(), nickName, this.loginCode, loginTime);
		this.isLogined = true;
	}
	
	/**
	 * 登出帳號
	 * @param loginCode
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws HttpException
	 * @throws JSONException
	 * @throws PostNotSameException
	 * @throws LoginCodeException
	 * @throws ServerException
	 */
	public void logoutUser(String loginCode) throws ClientProtocolException, IOException, HttpException, JSONException, PostNotSameException, LoginCodeException, ServerException{
		//將使用者的學習狀態傳送至後端
		this.server.userLogout(loginCode);
		
		//清除登入資訊
		clientdb.delete("ID = "+loginCode, "user");
		isLogined = false;
	}
	
	public void showLoginDialog() {
		//LoginDialog dialog = new LoginDialog(context);
		LoginDialog dialog = new LoginDialog(context);
		dialog.show();
	}
}

class LoginDialog extends Dialog implements android.view.View.OnClickListener {

	// TODO 視窗標題 -> R.String...
	String dialogTitle = "登入視窗";
	
	EditText uidView, upasswdView;
	Button okView, cancelView;
	
	public LoginDialog(Context context) {
		super(context);
		setTitle(dialogTitle);
		setContentView(R.layout.dialog_user_login);
		
		uidView = (EditText)findViewById(R.id.dialog_login_uid);
		upasswdView = (EditText)findViewById(R.id.dialog_login_upasswd);
		
		okView = (Button)findViewById(R.id.dialog_login_ok);
		okView.setOnClickListener(this);
		cancelView = (Button)findViewById(R.id.dialog_login_cancel);
		cancelView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.dialog_login_cancel:
			dismiss();
		}
	}
	
}
