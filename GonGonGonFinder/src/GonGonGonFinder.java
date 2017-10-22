import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class GonGonGonFinder extends JFrame {

	public JTextArea area;
	public HashMap<String,String> raidList = new HashMap<String,String>();
    public JLabel label2;
    public JLabel label3;
    public JCheckBox checkBox;
    public static GonGonGonFinder finder;
    // 認証画面用フレーム
    public static JFrame subFrame;
    public AudioClip ac = null;




    public static void main( String[] args ) {
    	TwitterStream twStream = new TwitterStreamFactory().getInstance();
        AccessToken accessToken = loadAccessToken(twStream);
        // アクセストークンが存在しない場合は作成する
        if (accessToken.getToken().isEmpty() ||accessToken.getTokenSecret().isEmpty()) {
        	createAccessToken(twStream);
        } else {
	        twStream.setOAuthAccessToken(accessToken);

	        // GUI作成
	        finder = new GonGonGonFinder("ごんごんファインダー", twStream);
	        finder.setResizable(false);
	        finder.setVisible(true);
        }
        // シャットダウンフック
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    	    public void run() { twStream.shutdown();}
    	});

    }

    // コンストラクタ
    private GonGonGonFinder(String title, TwitterStream twStream) {
    	URL url;
		try {
			url = new URL("file:./sound.wav");
	    	ac = Applet.newAudioClip(url);
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		setTitle(title);
        setBounds(100, 100, 320, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextField text1 = new JTextField(20);
        JTextField text2 = new JTextField(20);
        JLabel label1;

        area = new JTextArea();
        area.setLineWrap(true);

        // 上部の検索結果部分
        JPanel p = new JPanel();
        JScrollPane scrollpane = new JScrollPane(area);
        scrollpane.setPreferredSize(new Dimension(315, 110));
        area.setRows(5);
        area.setText("検索中...\n少々お待ちください...");
        p.add(scrollpane);

        // メイン部分
        JPanel bottomp = new JPanel();
        bottomp.setLayout(new GridLayout(8, 1));
        JComboBox<String> cb1 = new JComboBox<String>();
        checkBox = new JCheckBox("音を鳴らす");
        // レイドリストを作成
        setRaidList(cb1);

        // アクションリスナーの設定
        cb1.addActionListener(
        	new ActionListener(){
        		public void actionPerformed(ActionEvent event){
        		      JComboBox<?> cb = (JComboBox<?>)event.getSource();
        		      System.out.println(cb.getSelectedItem());
        		      text1.setText((String) cb.getSelectedItem());
        		      text2.setText(raidList.get(cb.getSelectedItem()));
                }
            }
        );

        text1.setText((String) cb1.getSelectedItem());
	   	text2.setText(raidList.get(cb1.getSelectedItem()));

        search(twStream, text1.getText(), text2.getText());

        label1 = new JLabel("検索中ワード");
        label2 = new JLabel("　１：" + text1.getText());
        label3 = new JLabel("　２：" + text2.getText());

        JButton button1 = new JButton("検索条件変更");
        button1.addActionListener(
          new ActionListener(){
            public void actionPerformed(ActionEvent event){

                // ツイート検索
                // 検索用のフィルターを作ります
                FilterQuery filterQuery = new FilterQuery();
                // 検索する文字列を設定します。 複数設定することも出来て、配列で渡します
                filterQuery.track(new String[] {text1.getText(), text2.getText()});
                // フィルターします
                twStream.filter(filterQuery);
                // 検索状態更新
                label2.setText("　１：" + text1.getText());
                label3.setText("　２：" + text2.getText());
                area.setText("検索中...\n少々お待ちください...");
            }
          }
        );
        bottomp.add(checkBox);
        bottomp.add(cb1);
        bottomp.add(label1);
        bottomp.add(label2);
        bottomp.add(label3);
        bottomp.add(text1);
        bottomp.add(text2);
        bottomp.add(button1);

        Container contentPane = getContentPane();
        contentPane.add(p, BorderLayout.CENTER);
        contentPane.add(bottomp, BorderLayout.SOUTH);
      }

	// Twitter検索
	public void search (TwitterStream twitterStream, String text1, String text2) {
        //Query query = new Query(word);
        //QueryResult result = twitter.search(query);
        //for (Status status : result.getTweets()) {
        //    System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
        //    System.out.println("");
        //}
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                String text = status.getText();
                area.setText(text);
                setClipboardString(text.substring(text.indexOf(" :")-8 ,text.indexOf(" :")));
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        // リスナーを登録します
        twitterStream.addListener(listener);
        // 検索用のフィルターを作ります
        FilterQuery filterQuery = new FilterQuery();
        // 検索する文字列を設定します。 複数設定することも出来て、配列で渡します
        filterQuery.track(new String[] {text1,text2});
        // フィルターします
        twitterStream.filter(filterQuery);

//        // 5分間だけ全裸で待機します
//        try {
//            Thread.sleep(300 * 1000L);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // ストリームをやめます
//            twitterStream.shutdown();
//        }
//
	}

	// 文字列をクリップボードにコピーする
	public void setClipboardString(String str) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		StringSelection ss = new StringSelection(str);
		clip.setContents(ss, ss);
		playSound();
	}

	// 音を鳴らす
	public void playSound() {
		if (checkBox.isSelected()) {
			ac.play();
		}
	//	clip.start();
	}

	// プルダウンにリストをセットする
	public void setRaidList(JComboBox<String> cb1) {
		BufferedReader br = null;
		FileInputStream fs = null;
		try {
	    	fs = new FileInputStream("raidList.txt");
	    	InputStreamReader isr = new InputStreamReader(fs, "SJIS");
	        br = new BufferedReader(isr);
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] word = line.split(",");
	    		cb1.addItem(word[0]);
	    		raidList.put(word[0], word[1]);
	        }
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            br.close();
	            fs.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

	// ファイル読み込み
	public String fileRead(String filePath) {
	    FileReader fr = null;
	    BufferedReader br = null;
	    try {
	        fr = new FileReader(filePath);
	        br = new BufferedReader(fr);

	        String line;
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	            return line;
	        }
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            br.close();
	            fr.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		return filePath;
	}

	// アクセストークンを読み込む
	private static AccessToken loadAccessToken(TwitterStream twStream){
		FileReader fr = null;
		String[] tokenArray = new String[1000];
		tokenArray[0] = "";
		tokenArray[1] = "";
			BufferedReader br = null;
			try {
				fr = new FileReader("token.properties");
				br = new BufferedReader(fr);
				String line;
				int i = 0;
				while ((line = br.readLine()) != null) {
					tokenArray[i] = line;
					i++;
			    }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		return new AccessToken(tokenArray[0], tokenArray[1]);
	}

	// アクセストークンを作成する
	private static void createAccessToken(TwitterStream twStream){
		try {
			RequestToken requestToken = twStream.getOAuthRequestToken();
			 subFrame = new JFrame("Twitter連携初期設定");
			 // 閉じるボタン押下時のアプリケーションの振る舞いを決定
			 subFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 // ウィンドウの初期サイズ（幅、高さ）をピクセル単位で設定
			 subFrame.setSize(550, 200);
			 // ウィンドウの表示場所を規定
			 subFrame.setLocationRelativeTo(null);

			 // subFrameのContentPaneを取得する
			 Container contentPane = subFrame.getContentPane();
			 // テキストエリアのインスタンスを生成
			 final JTextArea textArea = new JTextArea(4,0);

			 // スクロールペインにテキストエリアを追加
			 JScrollPane scrollPane = new JScrollPane(textArea);
			 // textFieldから文字列を取得し、取得した文字列に改行コードを加え、textAreaに追加
			 textArea.append("以下のURLにアクセスしてPINコードを取得してください。" + "\n");
			 textArea.append(requestToken.getAuthorizationURL() + "\r\n");
			 textArea.append("取得したPINコードを下のテキストエリアに入力したあと認証ボタンを押してください。" + "\n");

			 JPanel center = new JPanel();
			 JLabel label = new JLabel("PINコード");
			 // テキストフィールドのインスタンスを生成
			 final JTextField textField = new JTextField();
			 center.setLayout(new GridLayout(2, 1));
			 center.add(label);
			 center.add(textField);


			 // ボタンのインスタンスを生成
			 JButton button = new JButton("認証する");
			 // アクションの定義
			 button.addActionListener(new AbstractAction(){
			 private static final long serialVersionUID = 1L;

			 // 認証ボタン押下時
			 public void actionPerformed(ActionEvent arg0) {
				 String pin = textField.getText();
				 // 入力されたコードが正しいかチェック
				 if (pin.matches("^[1-9]?[0-9]+$") && pin.length() == 7) {
					 try {
						AccessToken accessToken = twStream.getOAuthAccessToken(requestToken, pin);
						subFrame.setVisible(false);
						saveAccessToken(accessToken);
				        twStream.setOAuthAccessToken(accessToken);
				        // GUI作成
				        finder = new GonGonGonFinder("ごんごんファインダー", twStream);
				        finder.setResizable(false);
				        finder.setVisible(true);
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			 }
			 });
			 // パネルをコンポーネントに配置
			 contentPane.add(scrollPane, BorderLayout.NORTH);
			 contentPane.add(center, BorderLayout.CENTER);
			 contentPane.add(button, BorderLayout.SOUTH);


			 // ウィンドウを表示
			 subFrame.setVisible(true);
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	// アクセストークンを保存する
	private static void saveAccessToken(AccessToken accessToken){
		try{
			File file = new File("token.properties");
			FileWriter filewriter = new FileWriter(file);

			filewriter.write(accessToken.getToken() +"\r\n");
			filewriter.write(accessToken.getTokenSecret());

			filewriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}

