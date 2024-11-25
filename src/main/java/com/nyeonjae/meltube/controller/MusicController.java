package com.nyeonjae.meltube.controller;

import com.nyeonjae.meltube.entities.MusicEntity;
import com.nyeonjae.meltube.entities.UserEntity;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.services.MusicService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping(value = "/music")
public class MusicController {
    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(
            @SessionAttribute(value = "user", required = false)UserEntity user,@RequestParam(value = "_cover", required = false) MultipartFile _cover,
            MusicEntity music) throws IOException, InterruptedException {

        Result result = this.musicService.addMusic(user, music, _cover);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();

    }

    @RequestMapping(value = "/crawl-melon", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MusicEntity getCrawlMelon(@RequestParam(value = "id", required = false) String id) throws IOException {

        return this.musicService.crawlMelon(id);
    }


    @RequestMapping(value = "/search-melon", method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MusicEntity[] geySearchMelon(@RequestParam(value = "keyword", required = false) String keyword) throws IOException, InterruptedException {

        return this.musicService.searchMelon(keyword);

    }



    @RequestMapping(value = "/verify-youtube-id", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getVerifyYoutubeId(@RequestParam(value = "id", required = false) String id) throws IOException, InterruptedException {
        boolean result = this.musicService.verifyYoutubeId(id);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }






}
