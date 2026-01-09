package com.example.essaychecker.mapper;

import com.example.essaychecker.pojo.Essay;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface EssayMapper {

    @Insert("INSERT INTO essay (content, content_result) VALUES (#{content}, #{contentResult})")
    void insertEssay(Essay essay);

}
