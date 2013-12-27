package net.dmcloud.cloudkey;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.Deflater;
import org.apache.commons.codec.binary.Base64;

public class Helpers {

    public static String curl(String targetURL, String urlParameters) {
        return curl(targetURL, urlParameters, null, -1);
    }

    public static String curl(String targetURL, String urlParameters, String httpProxyHost, int httpProxyPort) {
        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetURL);

            if (httpProxyHost != null && !httpProxyHost.isEmpty() && httpProxyPort > 0) {
                Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
                connection = (HttpURLConnection) url.openConnection(p);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes("UTF-8").length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(urlParameters.getBytes("UTF-8"));
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static public String normalize(Map data) {
        String normalize = "";
        data = new TreeMap(data);
        Iterator iter = data.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            normalize += entry.getKey();
            normalize += normalizing(entry.getValue());
        }
        return normalize;
    }

    static public String normalize(ArrayList data) {
        String normalize = "";
        for (int i = 0; i < data.size(); i++) {
            normalize += normalizing(data.get(i));
        }
        return normalize;
    }

    static private String normalizing(Object o) {
        if (o instanceof Map) {
            return normalize((Map) o);
        } else if (o instanceof ArrayList) {
            return normalize((ArrayList) o);
        } else {
            return o.toString();
        }
    }

    static public String sign_url(String url, String secret) throws DCException {
        return sign_url(url, secret, CloudKey.SECLEVEL_NONE, "", "", "", null, null, 0);
    }

    static public String sign_url(String url, String secret, int seclevel, String asnum, String ip, String useragent, String[] countries, String[] referers, int expires) throws DCException {
        expires = (expires == 0) ? (int) ((new Date().getTime() / 1000) + 7200) : expires;

        // Compute digest
        String[] tokens = url.split("\\?");
        String query = "";
        if (tokens.length > 1) {
            url = tokens[0];
            query = tokens[1];
        }

        String secparams = "";
        ArrayList<String> public_secparams = new ArrayList<String>();
        if ((seclevel & CloudKey.SECLEVEL_DELEGATE) == 0) {
            if ((seclevel & CloudKey.SECLEVEL_ASNUM) > 0) {
                if (asnum.equals("")) {
                    throw new DCException("IP security level required and no IP address provided.");
                }
                secparams += asnum;
            }
            if ((seclevel & CloudKey.SECLEVEL_IP) > 0) {
                if (asnum.equals("")) {
                    throw new DCException("IP security level required and no IP address provided.");
                }
                secparams += ip;
            }
            if ((seclevel & CloudKey.SECLEVEL_USERAGENT) > 0) {
                if (asnum.equals("")) {
                    throw new DCException("USERAGENT security level required and no user-agent provided.");
                }
                secparams += useragent;
            }
            if ((seclevel & CloudKey.SECLEVEL_COUNTRY) > 0) {
                if (countries == null || countries.length == 0) {
                    throw new DCException("COUNTRY security level required and no country list provided.");
                }
                public_secparams.add("cc=" + implode(",", countries).toLowerCase());
            }
            if ((seclevel & CloudKey.SECLEVEL_REFERER) > 0 || (seclevel & CloudKey.SECLEVEL_REFERER_STRICT) > 0) {
                if (referers == null || referers.length == 0) {
                    throw new DCException("REFERER security level required and no referer list provided.");
                }
                try {
                    StringBuilder ref = new StringBuilder();
                    for (int i = 0; i < referers.length; i++) {
                        if (i > 0) {
                            ref.append(" ");
                        }
                        ref.append(referers[i].replace(" ", "%20"));
                    }
                    public_secparams.add("rf=" + URLEncoder.encode(ref.toString(), "UTF-8"));
                } catch (Exception e) {
                    throw new DCException(e.getMessage());
                }
            }
        }
        String public_secparams_encoded = "";

        if (public_secparams.size() > 0) {
            try {
                public_secparams_encoded = Base64.encodeBase64URLSafeString(gzcompress(implode("&", public_secparams)));
            } catch (Exception e) {
                throw new DCException(e.getMessage());
            }
        }
        String rand = "";
        String letters = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 8; i++) {
            int index = (int) Math.round(Math.random() * 35);
            rand += letters.substring(index, index + 1);
        }

        String digest = md5(seclevel + url + expires + rand + secret + secparams + public_secparams_encoded);

        return url + "?" + (!query.equals("") ? query + '&' : "") + "auth=" + expires + "-" + seclevel + "-" + rand + "-" + digest + (!"".equals(public_secparams_encoded) ? "-" + public_secparams_encoded : "");
    }

    private static String implode(String delim, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    private static String implode(String delim, ArrayList<String> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    public static String md5(String password) {
        byte[] uniqueKey = password.getBytes();
        byte[] hash = null;

        try {
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("No MD5 support in this VM.");
        }

        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }

    private static byte[] gzcompress(String inputString) throws Exception {
        byte[] input = inputString.getBytes("UTF-8");
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!compresser.finished()) {
            int count = compresser.deflate(buf);
            bos.write(buf, 0, count);
        }
        bos.close();

        return bos.toByteArray();
    }
}
