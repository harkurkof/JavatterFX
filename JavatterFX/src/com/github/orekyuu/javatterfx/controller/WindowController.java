package com.github.orekyuu.javatterfx.controller;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import orekyuu.plugin.loader.PluginRegister;
import twitter4j.Status;

import com.github.orekyuu.javatterfx.account.TwitterManager;
import com.github.orekyuu.javatterfx.event.EventHandler;
import com.github.orekyuu.javatterfx.event.Listener;
import com.github.orekyuu.javatterfx.event.user.EventReplyClick;
import com.github.orekyuu.javatterfx.event.user.EventUserTweet;
import com.github.orekyuu.javatterfx.event.user.EventUserTweet.EventType;
import com.github.orekyuu.javatterfx.event.view.EventToolbarCreated;
import com.github.orekyuu.javatterfx.managers.ColumnManager;
import com.github.orekyuu.javatterfx.managers.EventManager;
import com.github.orekyuu.javatterfx.util.JavatterConfig;
import com.github.orekyuu.javatterfx.util.StatusUpdateBuilder;
import com.github.orekyuu.javatterfx.util.TweetDispenser;


public class WindowController implements Initializable, Listener{

	@FXML
	private BorderPane root;
    @FXML
    private VBox topborder;
    @FXML
    private TextArea tweet;
    @FXML
    private ToolBar bar;
    @FXML
    private MenuButton config;
    @FXML
    private MenuButton plugin;
    @FXML
    private Button javabeam;
    @FXML
    private Button tweetbutton;
    @FXML
    private ScrollPane scroll;
    @FXML
    private HBox box;
    @FXML
    private CheckMenuItem useCache;
    @FXML
    private MenuButton column;
    @FXML
    private Button koukoku;

    private Status reply;

    private File file;
    /**
     * 初期化処理
     */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		useCache.setSelected(JavatterConfig.getInstance().getUseLocalCache());
		addChilden(ColumnManager.INSTANCE.getColumFactory("TimeLine").createView());
		addChilden(ColumnManager.INSTANCE.getColumFactory("Mensions").createView());
		EventManager.INSTANCE.addEventListener(this);
		for(String s:ColumnManager.INSTANCE.columList()){
			final MenuItem item=new MenuItem(s);
			item.setOnAction(new javafx.event.EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					addChilden(ColumnManager.INSTANCE.getColumFactory(item.getText()).createView());
				}
			});
			column.getItems().add(item);
		}
		EventToolbarCreated event=new EventToolbarCreated(bar, config, koukoku, tweetbutton);
		EventManager.INSTANCE.eventFire(event);

		plugin.getItems().addAll(PluginRegister.INSTANCE.getPluginConfigs());
	}

	/**
	 * コンポーネントを追加
	 * @param node
	 */
	public void addChilden(Node node){
		box.getChildren().add(node);
	}

	@EventHandler
	public void onReply(EventReplyClick event){
		final Status status=event.getStatus();
		reply=status;
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					StringBuilder builder=new StringBuilder();
					builder.append(tweet.getText()).append("@").append(status.getUser().getScreenName())
					.append(" ");
					tweet.setText(builder.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

    public void onTweet(ActionEvent event) {
		tweet(EventType.BUTTON);
	}

	/**
	 * ショートカットキーでツイート
	 * @param event
	 */
	public void onChangeText(KeyEvent event){
		if(event.isControlDown()){
			if(KeyCode.ENTER==event.getCode()){
				tweet(EventType.SHORTCUT);
				event.consume();
			}
		}
	}

	private void tweet(EventType type){
		EventUserTweet event=new EventUserTweet(tweet.getText(),TwitterManager.getInstance().getUser(),type);
		EventManager.INSTANCE.eventFire(event);
		StatusUpdateBuilder builder=new StatusUpdateBuilder(event.getText()).setReplyID(reply);
		if(file!=null)builder.setImage(file);
		TweetDispenser.tweet(builder.create());
		tweet.setText("");
		reply=null;
		file=null;
	}

	public void onCacheConfig(ActionEvent event){
		JavatterConfig.getInstance().setUseLocalCache(useCache.isSelected());
	}

	public void coukoku(ActionEvent event){
		TweetDispenser.tweet("嘘、私のJavaビーム...弱すぎ？ そんなあなたにJava力トレーニングソフトJavatterFX! 無料でJava力を鍛えて周りのみんなを圧倒しよう！ ダウンロードはこちら→http://www1221uj.sakura.ne.jp/wordpress/ #javatter");
	}

	public void onImageDrop(DragEvent event){
		boolean flag=false;
		Dragboard db=event.getDragboard();
		Pattern p=Pattern.compile("\\.(png|jpg|gif)$");
		if(db.hasFiles()){
			for(File f:db.getFiles()){
				if(!p.matcher(f.getPath()).find())continue;
				file=f;
				break;
			}
		}
		event.setDropCompleted(flag);
		event.consume();
	}

	public void onImageDragOver(DragEvent event){
		Dragboard db=event.getDragboard();
		if(db.hasFiles())
			event.acceptTransferModes(TransferMode.MOVE);
		event.consume();
	}
}
