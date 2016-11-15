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
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SignInFreeCodeCamp {
    static CloseableHttpClient client;

    @BeforeClass()
    public void beforeClass() {
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        client = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(globalConfig)
                .build();
    }

    @AfterClass()
    public void afterClass() throws IOException {
        signOut();
        client.close();
    }

    @Test()
    public static void signOut() throws IOException {
        final String SIGNOUT_URL = "https://www.freecodecamp.com/signout";
        HttpGet getRequest = new HttpGet(SIGNOUT_URL);
        HttpResponse getResponse = client.execute(getRequest);

        // check response code is 200
        Assert.assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Status code should be 200");

        // check the returned html page contains "Sign in"
        HttpEntity responseEntity = getResponse.getEntity();
        long len = responseEntity.getContentLength();
        if (len >= 0 && len <= 2048) {
            Assert.assertTrue(EntityUtils.toString(responseEntity).contains("Sign in"),
                    "After sign out, page should contain the wording \"Sign in\" but doesn't");
        }
    }

    @Test()
    // need to fill in values for CORRECT_EMAIL and CORRECT_PWD before the test can pass
    public static void logInPost() throws IOException, URISyntaxException {
        final String PARAM1_NAME = "email", PARAM2_NAME = "password",
                CORRECT_EMAIL = "", CORRECT_PWD = "",
                POST_URL = "https://www.freecodecamp.com/api/users/login";
        final URI REDIRECT_URL = new URI("https://www.freecodecamp.com/challenges/learn-how-free-code-camp-works");

        List<NameValuePair> formParams = new ArrayList();
        formParams.add(new BasicNameValuePair(PARAM1_NAME , CORRECT_EMAIL));
        formParams.add(new BasicNameValuePair(PARAM2_NAME , CORRECT_PWD));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams);

        HttpPost postRequset = new HttpPost(POST_URL);
        postRequset.setEntity(entity);

        HttpClientContext context = HttpClientContext.create();

        CloseableHttpResponse postResponse = client.execute(postRequset, context);

        try {
            // check that Post request eventually returns status code 200
            Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Status code should be 200 but isn't");

            // check after signing in, the returned html page contains the welcome message
            // "Welcome to Free Code Camp."
            HttpEntity responseEntity = postResponse.getEntity();
            long len = responseEntity.getContentLength();
            if (len >= 0 && len <= 2048) {
                Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Welcome to Free Code Camp."),
                        "After sign in page should contain the welcome message but doesn't");
            }

            // check the final URL after redirects is /challenges/learn-how-free-code-camp-works
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(postRequset.getURI(), target, redirectLocations);
            Assert.assertEquals(location, REDIRECT_URL, "Final URL should be \"/challenges/learn-how-free-code-camp-works\", but isn't");
        } finally {
            if (postResponse != null) {
                postResponse.close();
            }
        }
    }
}
