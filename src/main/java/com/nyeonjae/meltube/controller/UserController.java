package com.nyeonjae.meltube.controller;


import com.nyeonjae.meltube.entities.EmailTokenEntity;
import com.nyeonjae.meltube.entities.UserEntity;
import com.nyeonjae.meltube.results.CommonResult;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getIndex(UserEntity user, HttpSession session) {


        Result result = this.userService.login(user);
        if (result == CommonResult.SUCCESS) {
            session.setAttribute("user", user);
            // @SessionAttribute 의 user 이름과 동일해야함
        }
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-email", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getRecoverEmail(UserEntity user) {
        Result result = this.userService.recoverEmail(user);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        if (result == CommonResult.SUCCESS) {
            response.put("email", user.getEmail());
        }
        return response.toString();
    }

    @RequestMapping(value = "/recover-password",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelAndView getRecoverPassword(@RequestParam(value = "userEmail", required = false)String userEmail,
                                           @RequestParam(value = "key", required = false) String key) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("userEmail", userEmail);
        modelAndView.addObject("key", key);
        modelAndView.setViewName("user/recoverPassword");
        return modelAndView;
    }

    @RequestMapping(value ="/recover-password", method = RequestMethod.PATCH,  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRecoverPassword(EmailTokenEntity emailToken,
                                       @RequestParam(value = "password", required = false) String password) {

        Result result = this.userService.resolveRecoverPassword(emailToken, password);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }




    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request, UserEntity user) throws MessagingException {
        Result result = this.userService.register(request, user);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPassword(HttpServletRequest request,
            @RequestParam(value = "email", required = false) String email)throws MessagingException {
        Result result = this.userService.provokeRecoverPassword(request, email);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    @RequestMapping(value="/validate-email-token", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getValidateEmailToken(EmailTokenEntity emailToken) {
        Result result = this.userService.validateEmailToken(emailToken);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(Result.NAME, result.nameToLower());
        modelAndView.setViewName("user/validateEmailToken");
        return modelAndView;
    }
}

