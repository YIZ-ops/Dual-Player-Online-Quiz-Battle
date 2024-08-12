package io.yyy.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.yyy.entity.QuestionModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionDao extends BaseMapper<QuestionModel> {
}
