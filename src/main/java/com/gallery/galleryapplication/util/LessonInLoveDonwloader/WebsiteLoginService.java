package com.gallery.galleryapplication.util.LessonInLoveDonwloader;

import lombok.Getter;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Imports are unchanged
@Component
@PropertySource("classpath:setting.properties")
public class WebsiteLoginService {
    @Getter
    final private WebClient webClient;
    @Value("${website.login.url}")
    private String loginUrl;
    @Value("${website.username}")
    private String username;
    @Value("${website.password}")
    private String password;
    public WebsiteLoginService() {
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setDownloadImages(false);
    }

    public boolean getLoginStatus(){
        try {
            return webClient.getPage("https://lessonsinlovegame.com/galleries/requests/").getUrl().toString().equals("***REMOVED***");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loginToWebsite() throws IOException {
        if (getLoginStatus()){
            HtmlPage loginPage = webClient.getPage(loginUrl);
            HtmlForm loginForm = loginPage.getForms().get(0);
            HtmlTextInput usernameInput = loginForm.getInputByName("loginModel.Username");
            HtmlPasswordInput passwordInput = loginForm.getInputByName("loginModel.Password");
            usernameInput.setValue(username);
            passwordInput.setValue(password);
            HtmlButton loginButton = loginForm.getFirstByXPath("//button[@class='btn btn-primary']");
            HtmlPage loggedInPage = loginButton.click();
        }
    }
}
