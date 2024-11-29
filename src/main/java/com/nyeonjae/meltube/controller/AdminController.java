package com.nyeonjae.meltube.controller;

import com.nyeonjae.meltube.entities.MusicEntity;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.services.AdmInService;
import com.nyeonjae.meltube.services.MusicService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private final AdmInService admInService;

    @Autowired
    public AdminController(AdmInService admInService) {
        this.admInService = admInService;
    }

    //region Music
    @RequestMapping(value = "/music/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MusicEntity[] getMusicIndex() {
        return this.admInService.getMusics();
    }

    @RequestMapping(value = "/music/", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteMusicIndex(@RequestParam("indexes") int[] indexes) {
        Result result = this.admInService.deleteMusics(indexes);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    @RequestMapping(value ="/music/status", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchMusicStatus(@RequestParam(value = "status", required = false) Boolean status,
                                   @RequestParam(value = "indexes", required = false) int[] indexes) {
        Result result = this.admInService.modifyMusicStatuses(status, indexes);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();

    }
    //endregion




}
