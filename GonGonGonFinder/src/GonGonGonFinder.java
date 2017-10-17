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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
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
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class GonGonGonFinder extends JFrame {

	public JTextArea area;
	public HashMap<String,String> raidList = new HashMap<String,String>();
    public JLabel label2;
    public JLabel label3;



    public static void main( String[] args ) {
        // Twitter twitter = new TwitterFactory().getInstance();
        TwitterStream twStream = new TwitterStreamFactory().getInstance();

        // GUI作成
        GonGonGonFinder finder = new GonGonGonFinder("ごんごんファインダー", twStream);
        finder.setResizable(false);
        finder.setVisible(true);

        // シャットダウンフック
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    	    public void run() { twStream.shutdown();}
    	});

    }

    // コンストラクタ
    private GonGonGonFinder(String title, TwitterStream twStream) {

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
        p.add(scrollpane);

        // メイン部分
        JPanel bottomp = new JPanel();
        bottomp.setLayout(new GridLayout(7, 1));
        JComboBox<String> cb1 = new JComboBox<String>();
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

        text1.setText("Lv100 マキュラ・マリウス");

        text2.setText("Lvl 100 Macula Marius");

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
            }
          }
        );
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
	}

	// プルダウンにリストをセットする
	public void setRaidList(JComboBox<String> cb1) {
		cb1.addItem("Lv100 マキュラ・マリウス");
		raidList.put("Lv100 マキュラ・マリウス", "Lvl 100 Macula Marius");

		cb1.addItem("Lv100 フラム＝グラス");
		raidList.put("Lv100 フラム＝グラス", "Lvl 100 Twin Elements");

		cb1.addItem("Lv75 シュヴァリエ・マグナ");
		raidList.put("Lv75 シュヴァリエ・マグナ", "Lvl 75 Luminiera Omega");
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
}

