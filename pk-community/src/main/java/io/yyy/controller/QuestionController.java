package io.yyy.controller;

import io.yyy.entity.QuestionModel;
import io.yyy.service.QuestionService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/app/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 获取随机题目
     *
     * @return
     */
    @GetMapping("/list")
    public List<QuestionModel> listRandomQuestion() {
        List<QuestionModel> questionModels = questionService.listRandomQuestion(5);
        return questionModels;
    }
}
