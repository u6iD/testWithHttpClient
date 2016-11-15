* This exercise tests that one can successfully sign into Free Code Camp [freeCodeCamp](https://www.freecodecamp.com/) with email and password and then signs out. It follows the steps in [Client HTTP Programming Primer](https://hc.apache.org/httpcomponents-client-ga/primer.html).
* Running it requires Apache HTTP Client and TestNG. I used HTTP Client 4.5.2.
  * RedirectStrategy is set to LaxRedirectStrategy; otherwise redirect is not followed and the logInPost test response status code is 302
  * CookieSpec resets to Standard (from Default). With Default CookieSpec, a warning that looks like below occurs and the logInPost test fails
    ResponseProcessCookies - Invalid cookie header: "set-cookie: access_token=***; Max-Age=31556.926; Path=/; Expires=Sun, 13 Nov 2016 05:46:12 GMT". Invalid 'expires' attribute: Sun, 13 Nov 2016 05:46:12 GMT
* Running it also requires filling in values for CORRECT_EMAIL and CORRECT_PWD in logInPost test.
* To debug, run with [full wire + context logging](https://hc.apache.org/httpcomponents-client-ga/logging.html)