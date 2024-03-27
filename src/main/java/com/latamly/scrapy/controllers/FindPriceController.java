package com.latamly.scrapy.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.latamly.scrapy.utils.FindML;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/price")
public class FindPriceController {

    @Autowired
    FindML findML;

    @GetMapping(path = "/{nombre}")
    public String buscar(@PathVariable String nombre) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(findML.findML(nombre));
        System.out.println(json);
        return json;
    }
}
