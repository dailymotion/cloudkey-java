/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dmcloud.cloudkey;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import net.dmcloud.util.DCArray;
import net.dmcloud.util.DCObject;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ghilas
 */
public class CloudKeyTest {

    public static String user_id = "YOUR USER ID";
    public static String api_key = "YOUR API KEY";
    public static String video_id = "YOUR VIDEO ID";
    public static String http_proxy_host = "YOUR PROXY HOST NAME OR IP";
    public static int http_proxy_port = -1;

    /**
     * Test of mediaGetEmbedUrl method, of class CloudKey.
     */
    @Test
    public void testMediaGetEmbedUrl_String() {
        try {
            CloudKey cloud = new CloudKey(user_id, api_key, http_proxy_host, http_proxy_port);
            String[] referers = {"http://test.dmcloud.net"};
            String mediaEmbedUrl = cloud.mediaGetEmbedUrl(CloudKey.API_URL, video_id, CloudKey.SECLEVEL_REFERER, "", "", "", null, referers, 0);
            assertTrue(mediaEmbedUrl.startsWith("http://api.dmcloud.net/embed/" + user_id + "/" + video_id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of mediaGetStreamUrl method, of class CloudKey.
     */
    @Test
    public void testMediaGetStreamUrl_String() {
        try {
            CloudKey cloud = new CloudKey(user_id, api_key, http_proxy_host, http_proxy_port);
            final String mediaStreamUrl = cloud.mediaGetStreamUrl(video_id);
            assertTrue(mediaStreamUrl.startsWith("http://cdn.dmcloud.net/route/" + user_id + "/" + video_id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of CloudKey call method, of class CloudKey.
     */
    @Test
    public void testCloudKey() {
        try {
            CloudKey cloud = new CloudKey(user_id, api_key, http_proxy_host, http_proxy_port);
            DCObject result = cloud.call(
                    "media.list",
                    DCObject.create()
                    .push("fields", DCArray.create().push("id").push("meta.title")));
            DCArray list = DCArray.create((ArrayList) result.get("list"));
            for (int i = 0; i < list.size(); i++) {
                DCObject item = DCObject.create((Map) list.get(i));
                assertEquals((!item.pull("meta.title").equals("")), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of CloudKey call method, of class CloudKey.
     */
    @Test
    public void testCloudKeyError() {
        Boolean error = false;
        try {
            CloudKey cloud = new CloudKey(user_id.substring(0, 2), api_key, http_proxy_host, http_proxy_port);
            cloud.call(
                    "media.list",
                    DCObject.create()
                    .push("fields", DCArray.create().push("id").push("meta.title")));
        } catch (Exception e) {
            error = true;
            assertEquals(((DCException) e).getCode(), 400);
        } finally {
            assertEquals(error.booleanValue(), true);
        }
    }


 /**
     * Test of fileUpload method, of class CloudKey.
     */
    @Test
    public void testFileUpload() {
        try {
            CloudKey cloud = new CloudKey(user_id, api_key, http_proxy_host, http_proxy_port);
            final String videoPath = CloudKeyTest.class.getResource("Diaporama.mp4").getPath();
            File f = new File(videoPath);
            String media_id = cloud.mediaCreate(f);
            assertNotNull(media_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}