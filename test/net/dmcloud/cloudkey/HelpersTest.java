/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dmcloud.cloudkey;

import net.dmcloud.util.DCArray;
import net.dmcloud.util.DCObject;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ghilas
 */
public class HelpersTest {

    /**
     * Test of normalize method, of class Helpers.
     */
    @Test
    public void testCloudKey_Normalize() {
        assertEquals(
                Helpers.normalize(
                DCArray.create()
                .push("foo")
                .push(42)
                .push("bar")), "foo42bar");

        assertEquals(
                Helpers.normalize(
                DCObject.create()
                .push("yellow", 1)
                .push("red", 2)
                .push("pink", 3)), "pink3red2yellow1");

        assertEquals(
                Helpers.normalize(
                DCArray.create()
                .push("foo")
                .push(42)
                .push(
                DCObject.create()
                .push("yellow", 1)
                .push("red", 2)
                .push("pink", 3))
                .push("bar")), "foo42pink3red2yellow1bar");
    }

    /**
     * Test of sign_url method, of class Helpers.
     */
    @Test
    public void testCloudKeySign_UrlError() {
        Boolean error = false;
        try {
            Helpers.sign_url("", "", CloudKey.SECLEVEL_COUNTRY, "", "", "", null, null, 0);
        } catch (Exception e) {
            error = true;
        } finally {
            assertEquals(error.booleanValue(), true);
        }
    }

    /**
     * Test of md5 method, of class Helpers.
     */
    @Test
    public void testMd5() {
        System.out.println("md5");
        String password = "mypassword123";
        String expResult = "9c87baa223f464954940f859bcf2e233";
        String result = Helpers.md5(password);
        assertEquals(expResult, result);
    }
}
