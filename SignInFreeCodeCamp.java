import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignInFreeCodeCamp {
    private static CloseableHttpClient client;
    private final static String SUCCESS_SIGN_IN_URL_STRING = "challenges/learn-how-free-code-camp-works",
            FAILED_TEXT = "Sign in";

    @BeforeClass()
    public void beforeClass() {
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)    // to avoid "Invalid cookie header" warning
                .build();
        client = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())    // to follow redirects
                .setDefaultRequestConfig(globalConfig)
                .build();
    }

    @AfterClass()
    public void afterClass() throws IOException {
        client.close();
    }

    // TODO: need to fill in values for email and password for different test cases
    @DataProvider(name = "dataForTest")
    public Object[][] dataForTest() throws URISyntaxException {
        final String FAILED_REDIRECT = "https://www.freecodecamp.com/email-signin";
        return new Object[][] {
                // postive test - correct email and pwd
                {"", "", "Welcome to Free Code Camp.",
                        new URI("https://www.freecodecamp.com/" + SUCCESS_SIGN_IN_URL_STRING) },
                // negative test - for null email and pwd
                {null, null, FAILED_TEXT, new URI(FAILED_REDIRECT)},
                // negative - empty string email and pwd
                {"", "", FAILED_TEXT, new URI(FAILED_REDIRECT)},
                // negative - non-empty wrong email and correct pwd
                {"abc@d.com", "", FAILED_TEXT, new URI(FAILED_REDIRECT)},
                // negative - correct email and non-empty wrong pwd
                {"", "123", FAILED_REDIRECT, new URI(FAILED_REDIRECT)},
                // negative - correct email and wrong pwd with special characters
                {"", ":/?=&", FAILED_REDIRECT, new URI(FAILED_REDIRECT)},
        };
    }

    @Test(dataProvider = "dataForTest")
    public static void LogInPost(String email, String pwd,
                                 String msg, URI redirectURL) throws IOException, URISyntaxException {
        logInPost(email, pwd, msg, redirectURL);

    }

    // minor test - without signing in, signout should still work
    @Test()
    public static void SignOut() throws IOException {
        signOut();
    }

    private static void logInPost(String email, String pwd, String msg, URI redirctURL)
            throws IOException, URISyntaxException {
        final String PARAM1_NAME = "email", PARAM2_NAME = "password",
                POST_URL = "https://www.freecodecamp.com/api/users/login";
        boolean signedIn = false;

        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair(PARAM1_NAME , email));
        formParams.add(new BasicNameValuePair(PARAM2_NAME , pwd));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams);

        HttpPost postRequset = new HttpPost(POST_URL);
        postRequset.setEntity(entity);

        HttpClientContext context = HttpClientContext.create();

        CloseableHttpResponse postResponse = client.execute(postRequset, context);

        try {
            // check that Post request eventually returns status code 200
            Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Status code should be 200 but isn't");

            // check after signing in, the response html contains the expected message
            // sign in success - "Welcome to Free Code Camp."
            // sign in failure - "Sign In"
            HttpEntity responseEntity = postResponse.getEntity();
            long len = responseEntity.getContentLength();
            if (len >= 0 && len <= 2048) {
                Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains(msg),
                        "After attempting to sign in, page should contain text " + msg + " but doesn't");
            }

            // check the final URL after redirect(s)
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(postRequset.getURI(), target, redirectLocations);
            Assert.assertEquals(location, redirctURL, "Final URL should be " + redirctURL.getPath() + ", but isn't");
            if (redirctURL.getPath().contains(SUCCESS_SIGN_IN_URL_STRING)) {
                signedIn = true;
            }
        } finally {
            if (postResponse != null) {
                postResponse.close();
            }
        }
        if (signedIn) {
            signOut();
        }
    }

    private static void signOut() throws IOException {
        final String SIGNOUT_URL = "https://www.freecodecamp.com/signout";
        HttpGet getRequest = new HttpGet(SIGNOUT_URL);
        HttpResponse getResponse = client.execute(getRequest);

        // check response code is 200
        Assert.assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Status code should be 200");

        // check the response html contains "Sign in"
        HttpEntity responseEntity = getResponse.getEntity();
        long len = responseEntity.getContentLength();
        if (len >= 0 && len <= 2048) {
            Assert.assertTrue(EntityUtils.toString(responseEntity).contains(FAILED_TEXT),
                    "After sign out, page should contain the wording " + FAILED_TEXT + " but doesn't");
        }
    }
}
