package me.eldodebug.soar.management.account.microsoft;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.account.AccountManager;
import sun.net.www.protocol.https.Handler;

public class MicrosoftLoginWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private Runnable afterLogin;
	
    public MicrosoftLoginWindow(Runnable afterLogin) {
        this.setTitle("Connect with Microsoft");
        this.setSize(600, 700);
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        this.setContentPane(new JFXPanel());
        Platform.runLater(this::loadScene);
        this.overrideWindow();
        this.afterLogin = afterLogin;
    }

    public void start() {
        Platform.runLater(this::loadScene);
    }

    private void loadScene() {

        WebView webView = new WebView();
        JFXPanel content = (JFXPanel) this.getContentPane();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	SoarLogger.error("User closed window");
                setVisible(false);
            }
        });

        webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        webView.getEngine().load("https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=XboxLive.signin%20offline_access&prompt=login");
        content.setScene(new Scene(webView, this.getWidth(), this.getHeight()));
        this.setVisible(true);
    }

    public void overrideWindow() {
        try {
            URL.setURLStreamHandlerFactory(protocol -> {

                if (!protocol.equals("https")) return null;
                return new Handler()
                {
                    @Override
                    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException
                    {
                        HttpURLConnection connection = (HttpURLConnection) super.openConnection(url, proxy);

                        if (url.toString().contains("denied")) {
                        	SoarLogger.error("Denied Connection");
                            setVisible(false);
                        } else if (url.toString().contains("https://login.live.com/oauth20_desktop.srf?code")) {
                            getMicrosoftToken(url);
                            setVisible(false);
                        }
                        return connection;
                    }
                };
            });
        } catch (Error ignored) {
        	SoarLogger.error("Override already applied");
        }
    }

    private void getMicrosoftToken(URL url) {
    	
    	AccountManager accountManager = Soar.getInstance().getAccountManager();
    	
    	accountManager.getAuthenticator().loginWithUrl(url.toString());
    	accountManager.save();
    	afterLogin.run();
    }
}
