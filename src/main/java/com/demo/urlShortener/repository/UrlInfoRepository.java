package com.demo.urlShortener.repository;

import com.demo.urlShortener.entities.UrlInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UrlInfoRepository extends JpaRepository<UrlInfo, Long> {

    @Query("select u.longUrl from UrlInfo u where u.shortUrl = ?1")
    String getLongUrlFromShortUrl(String shortUrl);
}
