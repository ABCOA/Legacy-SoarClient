package me.eldodebug.soar.mcef;

public final class Mcef {
	
    private static McefApp app;
    private static McefClient client;

    static {
        if (CefUtil.init()) {
            app = new McefApp(CefUtil.getCefApp());
            client = new McefClient(CefUtil.getCefClient());
        }
    }

    public static McefApp getApp() {
    	
    	if(!isInitialized()) {
    		return null;
    	}
    	
        return app;
    }

    public static McefClient getClient() {
    	
    	if(!isInitialized()) {
    		return null;
    	}
    	
        return client;
    }

    public static McefBrowser createBrowser(String url, boolean transparent) {
    	
    	if(!isInitialized()) {
    		return null;
    	}
    	
        McefBrowser browser = new McefBrowser(client, url, transparent, null);
        browser.setCloseAllowed();
        browser.createImmediately();
        
        return browser;
    }

    public static McefBrowser createBrowser(String url, boolean transparent, int width, int height) {
    	
    	if(!isInitialized()) {
    		return null;
    	}
    	
        McefBrowser browser = new McefBrowser(client, url, transparent, null);
        browser.setCloseAllowed();
        browser.createImmediately();
        browser.resize(width, height);
        return browser;
    }

    public static boolean isInitialized() {
        return client != null;
    }

    public static void shutdown() {
        if (isInitialized()) {
            CefUtil.shutdown();
            client = null;
            app = null;
        }
    }
}