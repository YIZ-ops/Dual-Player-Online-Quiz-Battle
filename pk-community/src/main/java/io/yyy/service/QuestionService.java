package io.yyy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import io.yyy.entity.QuestionModel;

import java.util.List;

public interface QuestionService extends IService<QuestionModel> {


    /**
     * 随机获取题目
     *
     * @param number 题目数量
     * @return
     */
    List<QuestionModel> listRandomQuestion(Integer number);
}
