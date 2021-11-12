package com.demo.urlShortener.services;

import com.demo.urlShortener.entities.UrlInfo;
import com.demo.urlShortener.repository.UrlInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UrlInfoService {

    private static final String URL_KEY_PREFIX = "short_url::";
    private static final String DOMAIN = "https://tinyurl.com/";

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UrlInfoRepository urlInfoRepository;

    @Value("${spring.url.expiry_duration}")
    private int expiryDuration; //1 week

    private final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public String shortenUrl(String longURL){
        //First check if this exists in Redis/db
        String key = URL_KEY_PREFIX + longURL;
        String shortUrl = (String) redisTemplate.opsForValue().get(key);

        if(StringUtil.isNullOrEmpty(shortUrl)){
            //First create a short url from long url
            shortUrl = DOMAIN + uniqueStringGenerator();
            
            // This logic is wrong. If redis does not have the long to short url mapping, that does not mean you will simply generate a new one. 
            // You would need to check in mysql also because it may happen the url mapping is present in db but in redis it has expired.
            // Correct flow should be check in redis, then in db, if not found create a new url add in mysql and then in redis

            //Insert into DB amd return
            UrlInfo urlInfo = UrlInfo.builder()
                    .longUrl(longURL)
                    .shortUrl(shortUrl)
                    .build();

            urlInfoRepository.save(urlInfo);

            //Insert into Redis
            redisTemplate.opsForValue().set(key, shortUrl, expiryDuration, TimeUnit.MILLISECONDS);
        }
        return shortUrl;
    }

    public String expandURL(String shortURL){
        return urlInfoRepository.getLongUrlFromShortUrl(shortURL);
    }

    private String uniqueStringGenerator(){
        SimpleDateFormat sdfDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        Date now = new Date();
        String currentDate = sdfDateFormat.format(now);
        StringBuilder uniqueWord = new StringBuilder("");

        for(int i=0;i<currentDate.length()-1;i=i+2){
            int num = Integer.parseInt(currentDate.substring(i,i+2));
            uniqueWord.append(toBase64[num]);
        }
        uniqueWord.append(currentDate.substring(currentDate.length()-1));
        return uniqueWord.toString();
    }

}
