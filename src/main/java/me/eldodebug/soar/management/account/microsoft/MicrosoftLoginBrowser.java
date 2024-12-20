package me.eldodebug.soar.management.account.microsoft;

import com.sun.net.httpserver.HttpServer;
import me.abcoc.soar.utils.netwok.Http;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.account.AccountManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MicrosoftLoginBrowser {

    private static final long serialVersionUID = 1L;

    private Runnable afterLogin;

    public MicrosoftLoginBrowser(Runnable afterLogin) throws URISyntaxException, IOException {
        this.overrideWindow();
        this.afterLogin = afterLogin;
    }


    public void overrideWindow() throws URISyntaxException, IOException {
        Map<String, String> authorizeParams = new HashMap<>();
        authorizeParams.put("client_id", "d1ed1b72-9f7c-41bc-9702-365d2cbd2e38");
        authorizeParams.put("response_type", "code");
        authorizeParams.put("redirect_uri", "http://127.0.0.1:17342");
        authorizeParams.put("scope", "XboxLive.signin offline_access");

        String authorize = Http.buildUrl("https://login.live.com/oauth20_authorize.srf", authorizeParams);
        openIncognitoBrowser(authorize);
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(17342), 0);
        httpServer.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            URL tokenUrl = new URL("http://127.0.0.1:17342/?"+query);
            getMicrosoftToken(tokenUrl);
            String success = "Success! You can now close this window.";
            exchange.sendResponseHeaders(200, success.length());
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(success.getBytes(StandardCharsets.UTF_8));
            responseBody.close();
            httpServer.stop(2);
        });
        httpServer.setExecutor(null);
        httpServer.start();
    }

    private void openIncognitoBrowser(String url) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        if (os.contains("win")) {
            rt.exec(new String[]{"cmd", "/c", "start", "msedge", "--inprivate", "\"" + url + "\""});
        } else if (os.contains("mac")) {
            rt.exec(new String[]{"/usr/bin/open", "-a", "Google Chrome", "--args", "--incognito", url});
        } else if (os.contains("nix") || os.contains("nux")) {
            rt.exec(new String[]{"google-chrome", "--incognito", url});
        } else {
            throw new UnsupportedOperationException("Unsupported operating system.");
        }
    }

    private void getMicrosoftToken(URL url) {
        AccountManager accountManager = Soar.getInstance().getAccountManager();
        accountManager.getAuthenticator().loginWithUrl(url.toString());
        accountManager.save();
        afterLogin.run();
    }
}
