package com.nyeonjae.meltube.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nyeonjae.meltube.entities.MusicEntity;
import com.nyeonjae.meltube.entities.UserEntity;
import com.nyeonjae.meltube.results.CommonResult;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.services.MusicService;
import com.nyeonjae.meltube.vos.ResultVo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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


    @RequestMapping(value = "/", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteIndex(@SessionAttribute(value = "user", required = false) UserEntity user,
                              @RequestParam(value = "indexes", required = false) int[] indexes) {
        Result result = this.musicService.withdrawInquiries(user, indexes);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());

        return response.toString();
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


    @RequestMapping(value = "/cover", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getCover(@RequestParam(value = "index", required = false) Integer index) {
        MusicEntity music = this.musicService.getMusicByIndex(index, true);
        if (music == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(music.getCoverContentType()))
                .contentLength(music.getCoverData().length)
                .body(music.getCoverData());
    }

    @RequestMapping(value = "/crawl-melon", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MusicEntity getCrawlMelon(@RequestParam(value = "id", required = false) String id) throws IOException {

        return this.musicService.crawlMelon(id);
    }


    @RequestMapping(value = "/inquiries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getInquiries(@SessionAttribute(value = "user", required = false) UserEntity user) throws JsonProcessingException {
        ResultVo<Result, MusicEntity[]> result = this.musicService.getMusicInquiriesByUser(user);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.getResult().nameToLower());
        if (result.getResult() == CommonResult.SUCCESS) {
            JSONArray musics = new JSONArray();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            // ObjectMapper 는 기본적으로 Java에 내장된 LocalDateTime 및 LocalDate 를 변환하는 기능을 내포하지 않음
            // Mapping 된 메서드에서 사용하는 ObjectMapper 는 스프링의 설정에 의해서 JavaTimeModule 을 기본적으로
            // 등록하지만, 위 처럼 수동으로 객체화하여 사용할때에는 기본적으로 등록되지 않음으로, 위 처럼 별도의 조치가 필요함.
            // 요약 : 변환 대상에 LocalDateTime, LocalDate 있으면 < objectMapper.registerModule(new JavaTimeModule(); > 해야함

            for (MusicEntity music : result.getPayload()) {
                String musicString = objectMapper.writeValueAsString(music);
                // MusicEntity -> (JSON 형식의) String
                JSONObject musicObject = new JSONObject(musicString);
                // (JSON 형식의 ) String -> JSONObject
                musics.put(musicObject);
            }
            response.put("musics", musics);
        }
        return response.toString();
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
