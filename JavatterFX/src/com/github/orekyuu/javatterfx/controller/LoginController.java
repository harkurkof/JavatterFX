package com.github.orekyuu.javatterfx.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.github.orekyuu.javatterfx.account.AccountManager;
import com.github.orekyuu.javatterfx.account.TwitterManager;

public class LoginController implements Initializable{

	@FXML
	private BorderPane root;
    @FXML
    private BorderPane border;
    @FXML
    private TextField text;
    @FXML
    private Button button;

    private RequestToken token;

    private Twitter twitter;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		twitter=TwitterManager.getInstance().getTwitter();
		try {
			token=twitter.getOAuthRequestToken();
			Desktop.getDesktop().browse(new URL(token.getAuthenticationURL()).toURI());
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void authentication(ActionEvent event) {
		String pin=text.getText();
		AccessToken t;
		try {
			t = twitter.getOAuthAccessToken(this.token, pin);
			AccountManager.getInstance().setAccessToken(t);
			TwitterManager.getInstance().authentication(t);
			TwitterManager.getInstance().getTwitter().setOAuthAccessToken(t);
			button.getScene().getWindow().hide();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

}
