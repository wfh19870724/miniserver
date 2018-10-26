package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@EnableAutoConfiguration
@RequestMapping("/")
public class TestBootController {
    @Autowired
    private HttpServletRequest httpRequest;

    @RequestMapping("user")
    public String getUser() {

        return "hello";
    }

    @RequestMapping("json/white/miniprogrom/bind")
    public JSONObject bind() {
        String mobile = httpRequest.getParameter("mobile");
        JSONObject json = new JSONObject();
        json.put("hhhh","ddd");



        return json;
    }
}
