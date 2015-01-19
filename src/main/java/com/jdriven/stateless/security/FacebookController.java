package com.jdriven.stateless.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FacebookController {

    @Autowired
    Facebook facebook;

    @RequestMapping(value = "/api/facebook/details", method = RequestMethod.GET)
    public FacebookProfile getSocialDetails() {
        return facebook.userOperations().getUserProfile();
    }
}
