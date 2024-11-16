package me.eldodebug.soar.utils.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.eldodebug.soar.logger.SoarLogger;

public class HttpUtils {

    private static final int PUNYCODE_TMIN = 1;
    private static final int PUNYCODE_TMAX = 26;
    private static final int PUNYCODE_SKEW = 38;
    private static final int PUNYCODE_DAMP = 700;
    private static final int PUNYCODE_INITIAL_BIAS = 72;
    private static final int PUNYCODE_INITIAL_N = 128;
    
    private static String ACCEPTED_RESPONSE = "application/json";
    private static Gson gson = new Gson();
    
    public static JsonObject readJson(HttpURLConnection connection) {
        return gson.fromJson(readResponse(connection), JsonObject.class);
    }
	
    public static JsonObject postJson(String url, Object request) {
    	
        HttpURLConnection connection = setupConnection(url, UserAgents.MOZILLA, 5000, false);
        connection.setDoOutput(true);
        connection.addRequestProperty("Content-Type", ACCEPTED_RESPONSE);
        connection.addRequestProperty("Accept", ACCEPTED_RESPONSE);

        try {
            connection.setRequestMethod("POST");
            connection.getOutputStream().write(gson.toJson(request).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
        	SoarLogger.error("Failed to post json", e);
        }
        
        return readJson(connection);
    }

    public static String readResponse(HttpURLConnection connection) {
    	
        String redirection = connection.getHeaderField("Location");
        
        if (redirection != null) {
        	return readResponse(setupConnection(redirection, UserAgents.MOZILLA, 5000, false));
        }

        StringBuilder response = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line).append('\n');
            }
        } catch (IOException e) {
        	SoarLogger.error("Failed to read response", e);
        }

        return response.toString();
    }
    
	public static JsonObject readJson(String url, Map<String, String> headers, String userAgents) {
		
        try {
            HttpURLConnection connection = setupConnection(url, userAgents, 5000, false);

            if (headers != null) {
                for (String header : headers.keySet()) {
                    connection.addRequestProperty(header, headers.get(header));
                }
            }

            InputStream is = connection.getResponseCode() != 200 ? connection.getErrorStream() : connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            
            return gson.fromJson(readResponse(rd), JsonObject.class);
        } catch (IOException e) {
        	SoarLogger.error("Failed to read json", e);
        }
        
        return null;
	}
	
	public static JsonObject readJson(String url, Map<String, String> headers) {
		return readJson(url, headers, UserAgents.MOZILLA);
	}
	
    private static String readResponse(BufferedReader br) {
    	
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
            	sb.append(line);
            }
            
            return sb.toString();
        } catch (IOException e) {
        	SoarLogger.error("Failed to read response", e);
        }
        
        return null;
    }
	
	public static boolean downloadFile(String url, File outputFile, String userAgent, int timeout, boolean useCaches) {
		
        url = url.replace(" ", "%20");
        
        try (FileOutputStream fileOut = new FileOutputStream(outputFile); BufferedInputStream in = new BufferedInputStream(setupConnection(url, userAgent, timeout, useCaches).getInputStream())) {
        	org.apache.commons.io.IOUtils.copy(in, fileOut);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
	}
	
	public static boolean downloadFile(String url, File outputFile, String userAgents) {
		return downloadFile(url, outputFile, userAgents, 5000, false);
	}
	
	public static boolean downloadFile(String url, File outputFile) {
		return downloadFile(url, outputFile, UserAgents.MOZILLA, 5000, false);
	}
	
	public static HttpURLConnection setupConnection(String url, String userAgent, int timeout, boolean useCaches) {
		
		try {
			HttpURLConnection connection = ((HttpURLConnection) new URL(url).openConnection());
			
	        connection.setRequestMethod("GET");
	        connection.setUseCaches(useCaches);
	        connection.addRequestProperty("User-Agent", userAgent);
	        connection.setRequestProperty("Accept-Language", "en-US");
	        connection.setRequestProperty("Accept-Charset","UTF-8");
	        connection.setReadTimeout(timeout);
	        connection.setConnectTimeout(timeout);
	        connection.setDoOutput(true);
	        
	        return connection;
		} catch (Exception e) {
			SoarLogger.error("Failed to setup connection");
		}
        
		return null;
	}
	
	public static String encode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public static String decode(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
    public static String punycode(String url) {
    	
        int protoEnd = url.indexOf("://");

        if(protoEnd < 0) {
            protoEnd = 0;
        } else {
            protoEnd += 3;
        }

        int hostEnd = url.indexOf('/', protoEnd);
        
        if(hostEnd < 0) {
            hostEnd = url.length();
        }

        String hostname = url.substring(protoEnd, hostEnd);
        boolean doTransform = false;

        for(int i = 0; i < hostname.length(); i++) {
            if(hostname.charAt(i) >= 128) {
                doTransform = true;
                break;
            }
        }

        if(!doTransform) {
            return url;
        }

        String[] parts = hostname.split("\\.");
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        sb.append(url, 0, protoEnd);

        for(String p: parts) {
        	
            doTransform = false;

            for(int i = 0; i < p.length(); i++) {
                if(p.charAt(i) >= 128) {
                    doTransform = true;
                    break;
                }
            }

            if(first)
                first = false;
            else
                sb.append('.');

            if(doTransform)
                sb.append(punycodeEncodeString(p.codePoints().toArray()));
            else
                sb.append(p);
        }

        sb.append(url, hostEnd, url.length());
        return sb.toString();
    }

    private static int punycodeBiasAdapt(int delta, int numPoints, boolean firstTime) {
    	
        if(firstTime) {
            delta /= PUNYCODE_DAMP;
        } else {
            delta /= 2;
        }

        int k = 0;
        
        delta = delta + delta / numPoints;

        while(delta > ((36 - PUNYCODE_TMIN) * PUNYCODE_TMAX) / 2) {
            delta /= 36 - PUNYCODE_TMIN;
            k += 36;
        }

        return k + ((36 - PUNYCODE_TMIN + 1) * delta) / (delta + PUNYCODE_SKEW);
    }

    private static void punycodeEncodeNumber(StringBuilder dst, int q, int bias) {
    	
        boolean keepGoing = true;

        for(int k = 36; keepGoing; k += 36) {
        	
            int t = k - bias;
            
            if(t < PUNYCODE_TMIN) {
                t = PUNYCODE_TMIN;
            } else if(t > PUNYCODE_TMAX) {
                t = PUNYCODE_TMAX;
            }

            int digit;
            
            if(q < t) {
                digit = q;
                keepGoing = false;
            } else {
                digit = t + (q - t) % (36 - t);
                q = (q - t) / (36 - t);
            }

            if(digit < 26) {
                dst.append((char) ('a' + digit));
            } else {
                dst.append((char) ('0' + digit - 26));
            }
        }
    }

    private static String punycodeEncodeString(int[] input) {
    	
        StringBuilder output = new StringBuilder();

        for(int i = 0; i < input.length; i++) {
            if(input[i] < 128) {
                output.append((char) input[i]);
            }
        }

        int n = PUNYCODE_INITIAL_N;
        int delta = 0;
        int bias = PUNYCODE_INITIAL_BIAS;
        int h = output.length();
        int b = h;

        if(b > 0)
            output.append('-');

        while(h < input.length) {
        	
            int m = Integer.MAX_VALUE;
            
            for(int i = 0; i < input.length; i++) {
                if(input[i] >= n && input[i] < m)
                    m = input[i];
            }

            delta = delta + (m - n) * (h + 1);
            n = m;

            for(int i = 0; i < input.length; i++) {
            	
                int c = input[i];

                if(c < n)
                    delta++;

                if(c == n) {
                    punycodeEncodeNumber(output, delta, bias);
                    bias = punycodeBiasAdapt(delta, h + 1, h == b);
                    delta = 0;
                    h++;
                }
            }

            delta++;
            n++;
        }

        return "xn--" + output.toString();
    }
}
