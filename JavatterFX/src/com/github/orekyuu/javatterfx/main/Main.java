package com.github.orekyuu.javatterfx.main;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import orekyuu.plugin.loader.PluginLoader;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.github.orekyuu.javatterfx.account.AccountManager;
import com.github.orekyuu.javatterfx.account.TwitterManager;
import com.github.orekyuu.javatterfx.column.MensionsColumnFactory;
import com.github.orekyuu.javatterfx.column.TimeLineColumnFactory;
import com.github.orekyuu.javatterfx.controller.JavatterUserStream;
import com.github.orekyuu.javatterfx.controller.LoginController;
import com.github.orekyuu.javatterfx.event.system.EventStatusLoadEnd;
import com.github.orekyuu.javatterfx.event.system.EventStatusLoadStart;
import com.github.orekyuu.javatterfx.listeners.TweetObjectListener;
import com.github.orekyuu.javatterfx.managers.ColumnManager;
import com.github.orekyuu.javatterfx.managers.EventManager;
import com.github.orekyuu.javatterfx.managers.SaveDataManager;
import com.github.orekyuu.javatterfx.util.JavatterConfig;
import com.github.orekyuu.javatterfx.util.TwitterUtil;
import com.github.orekyuu.javatterfx.view.JavatterCss;
import com.github.orekyuu.javatterfx.view.JavatterFxmlLoader;


public class Main extends Application{

	private static Stage stage;

	private static JavatterUserStream userstream;

	private static Application app;

	public static final boolean DEBUG=false;

	public static final int VERSION = 1;

	@Override
	public void start(Stage stage) throws Exception {
		Main.stage=stage;
		Main.app=this;
		SaveDataManager.getInstance().init();
		JavatterConfig.getInstance().init();
		PluginLoader pluginloader=new PluginLoader();
		pluginloader.load();
		userstream=new JavatterUserStream();
		ColumnManager.INSTANCE.put(new TimeLineColumnFactory());
		ColumnManager.INSTANCE.put(new MensionsColumnFactory());

		{
			Parent parent=JavatterFxmlLoader.load("Window.fxml");
			Scene scene=new Scene(parent);
			scene.getStylesheets().addAll(JavatterCss.getCssPath());
			stage.setTitle("JavatterFX");
			stage.setScene(scene);
			stage.show();
		}

		if(AccountManager.getInstance().isLogined()){
			AccessToken token = AccountManager.getInstance().getAccessToken();
			TwitterManager.getInstance().getTwitter().setOAuthAccessToken(token);
			TwitterManager.getInstance().authentication(token);
		}else{
			JavatterFxmlLoader<LoginController> loader=new JavatterFxmlLoader<>();
			Parent parent=loader.loadFxml("Login.fxml");
			loader.getController().browse(this);
			Scene scene=new Scene(parent);
			scene.getStylesheets().addAll(JavatterCss.getCssPath());
			Stage stage1=new Stage();
			stage1.setTitle("Login");
			stage1.setScene(scene);
			stage1.centerOnScreen();
			stage1.showAndWait();
		}

		if(!DEBUG)
		{
			startingLoadAndStartUserStream();
		}
		registerListeners();
	}

	private void registerListeners(){
		EventManager.INSTANCE.addEventListener(new TweetObjectListener());
	}

	private void startingLoadAndStartUserStream(){
		BlockingQueue<Runnable> queue=new LinkedBlockingQueue<>();
		ThreadPoolExecutor executor=new ThreadPoolExecutor(3, 3, 1, TimeUnit.MINUTES, queue,new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread th=new Thread(r);
				th.setDaemon(true);
				return th;
			}
		});
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					EventManager.INSTANCE.eventFire(new EventStatusLoadStart());//StatusLoadStartイベントを発火
					TwitterUtil.loadHomeTimeLine(TwitterManager.getInstance().getTwitter());
					TwitterUtil.loadMensions(TwitterManager.getInstance().getTwitter());
					EventManager.INSTANCE.eventFire(new EventStatusLoadEnd());//StatusLoadStartイベントを発火
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				Main.getUserStream().start();
			}
		});
		executor.shutdown();
	}

	public static void main(String[] args){
		if (System.getProperty("os.name").equals("Mac OS X")) {
			System.setProperty("javafx.macosx.embedded", "true");
		    java.awt.Toolkit.getDefaultToolkit();
		}

		launch(args);
	}

	public static Stage getStage(){
		return stage;
	}

	public static Application getApplication(){
		return app;
	}

	public static JavatterUserStream getUserStream(){
		return userstream;
	}

}
