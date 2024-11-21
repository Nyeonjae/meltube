package com.nyeonjae.meltube.controller;

import com.nyeonjae.meltube.entities.MusicEntity;
import com.nyeonjae.meltube.services.MusicService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping(value = "/music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @RequestMapping(value = "/crawl-melon", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MusicEntity getCrawlMelon(@RequestParam(value = "id", required = false) String id) throws IOException {

        return this.musicService.crawlMelon(id);
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
