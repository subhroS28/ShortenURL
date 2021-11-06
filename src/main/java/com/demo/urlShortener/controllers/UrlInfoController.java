package com.demo.urlShortener.controllers;

import com.demo.urlShortener.services.UrlInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlInfoController {

    @Autowired
    UrlInfoService urlInfoService;

    @GetMapping("/getShortenUrl")
    public String getShortenUrl(@RequestParam("longUrl") String longUrl){
        String shortUrl = urlInfoService.shortenUrl(longUrl);
        return shortUrl;
    }

    @GetMapping("/getOriginalUrl")
    public String getOriginalUrl(@RequestParam("shortUrl") String shortUrl){
        String originalUrl = urlInfoService.expandURL(shortUrl);
        return originalUrl;
    }
}
