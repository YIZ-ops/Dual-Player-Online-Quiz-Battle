package io.yyy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.yyy.dao.QuestionDao;
import io.yyy.entity.QuestionModel;
import io.yyy.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, QuestionModel> implements QuestionService {

    @Override
    public List<QuestionModel> listRandomQuestion(Integer number) {
        // 随机题目数量
        Page test = new Page(1, number);
        baseMapper.selectPage(test, null);
        // 总页数
        long pages = test.getPages();
        int l = new Random().nextInt((int) pages);
        Page page = new Page(l, number);
        baseMapper.selectPage(page, null);
        return page.getRecords();
    }
}
