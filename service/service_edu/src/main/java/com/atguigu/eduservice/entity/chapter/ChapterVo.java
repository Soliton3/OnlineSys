package com.atguigu.eduservice.entity.chapter;

import lombok.Data;

import java.util.*;

@Data
public class ChapterVo {

    private String id;
    private String title;

    private List<VideoVo> children = new ArrayList<>();
}
