package com.atguigu.eduservice.service.impl;

import com.atguigu.eduservice.entity.EduCourse;
import com.atguigu.eduservice.entity.EduCourseDescription;
import com.atguigu.eduservice.entity.EduTeacher;
import com.atguigu.eduservice.entity.frontvo.CourseFrontVo;
import com.atguigu.eduservice.entity.frontvo.CourseWebVo;
import com.atguigu.eduservice.entity.vo.CourseInfo;
import com.atguigu.eduservice.entity.vo.CoursePublishVo;
import com.atguigu.eduservice.mapper.EduCourseMapper;
import com.atguigu.eduservice.service.EduChapterService;
import com.atguigu.eduservice.service.EduCourseDescriptionService;
import com.atguigu.eduservice.service.EduCourseService;
import com.atguigu.eduservice.service.EduVideoService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程 服务实现类
 * </p>
 *
 * @author testjava
 * @since 2021-03-09
 */
@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements EduCourseService {

    //课程描述的注入
    @Autowired
    private EduCourseDescriptionService courseDescriptionService;
    //注入小节和章节service
    @Autowired
    private EduVideoService eduVideoService;

    @Autowired
    private EduChapterService chapterService;

    //添加课程基本信息的方法
    @Override
    public String saveCourseInfo(CourseInfo courseInfo) {
        //1 向课程表添加课程基本信息
        //courseInfo对象转换成eduCourse对象
        EduCourse eduCourse = new EduCourse();
        BeanUtils.copyProperties(courseInfo,eduCourse);
        int insert = baseMapper.insert(eduCourse);
        if (insert <= 0){
            throw new GuliException(20001,"添加课程信息失败");
        }

        String cid = eduCourse.getId();
        //2 向课程简介表添加课程简介
        EduCourseDescription courseDescription = new EduCourseDescription();
        courseDescription.setDescription(courseInfo.getDescription());
        courseDescription.setId(cid);
        courseDescriptionService.save(courseDescription);

        return cid;
    }

    //根据课程id查询课程信息
    @Override
    public CourseInfo getCourseInfo(String courseId) {
        //1 查询课程表
        EduCourse eduCourse = baseMapper.selectById(courseId);
        CourseInfo courseInfo = new CourseInfo();
        BeanUtils.copyProperties(eduCourse,courseInfo);

        //2 查询课程描述表
        EduCourseDescription eduCourseDescription = courseDescriptionService.getById(courseId);
        courseInfo.setDescription(eduCourseDescription.getDescription());

        return courseInfo;
    }

    //修改课程信息接口
    @Override
    public void updateCourseInfo(CourseInfo courseInfo) {
        //1 修改课程表
        EduCourse eduCourse = new EduCourse();
        BeanUtils.copyProperties(courseInfo,eduCourse);
        int upadate = baseMapper.updateById(eduCourse);
        if (upadate == 0){
            throw new GuliException(20001,"修改课程信息失败");
        }

        //2 修改课程描述表
        EduCourseDescription eduCourseDescription = new EduCourseDescription();
        eduCourseDescription.setId(courseInfo.getId());
        eduCourseDescription.setDescription(courseInfo.getDescription());
        courseDescriptionService.updateById(eduCourseDescription);
    }

    //根据课程id查询课程确认信息
    @Override
    public CoursePublishVo publishCourseInfo(String id) {
        //调用mapper
        CoursePublishVo coursePublishVo = baseMapper.getPublishCourseInfo(id);
        return coursePublishVo;
    }

    @Override
    public List<EduCourse> getpageCourseCondition(long current, long limit, EduCourse course) {
        //构建page对象
        Page<EduCourse> pageCourse = new Page<>(current,limit);
        //构建条件
        QueryWrapper<EduCourse> wrapper = new QueryWrapper<>();
        //wrapper
        //判断条件值是否为空，如果不为空拼接条件
        String name = course.getTitle();
        String status = course.getStatus();
        if (!StringUtils.isEmpty(name)){
            //构建条件
            wrapper.like("title",name);
        }
        if (!StringUtils.isEmpty(status)){
            //构建条件
            wrapper.eq("status",status);
        }
        wrapper.orderByDesc("gmt_create");
        baseMapper.selectPage(pageCourse,wrapper);
        List<EduCourse> records = pageCourse.getRecords();//数据集合
        return records;
    }

    @Override
    public void removeCourse(String courseId) {
        //1 根据课程id删除小节
        eduVideoService.removeVideoByCourseId(courseId);

        //2 根据课程id删除章节
        chapterService.removeChapterByCourseId(courseId);

        //3 根据课程id删除描述
        courseDescriptionService.removeById(courseId);

        //4 根据课程id删除课程本身
        int result = baseMapper.deleteById(courseId);
        if(result == 0) { //失败返回
            throw new GuliException(20001,"删除失败");
        }
    }

    //查询所有teacher
    @Cacheable(value = "course",key = "'selectCourseList'")
    @Override
    public List<EduCourse> getCourselist() {
        //查询前8条热门课程
        QueryWrapper<EduCourse> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        wrapper.last("limit 8");
        List<EduCourse> eduList = baseMapper.selectList(wrapper);
        return eduList;
    }

    //1 条件查询带分页查询课程
    @Override
    public Map<String, Object> getCourseFrontList(Page<EduCourse> pageParam, CourseFrontVo courseFrontVo) {
        //2 根据讲师id查询所讲课程
        QueryWrapper<EduCourse> wrapper = new QueryWrapper<>();
        //判断条件值是否为空，不为空拼接
        if(!StringUtils.isEmpty(courseFrontVo.getSubjectParentId())) { //一级分类
            wrapper.eq("subject_parent_id",courseFrontVo.getSubjectParentId());
        }
        if(!StringUtils.isEmpty(courseFrontVo.getSubjectId())) { //二级分类
            wrapper.eq("subject_id",courseFrontVo.getSubjectId());
        }
        if(!StringUtils.isEmpty(courseFrontVo.getBuyCountSort())) { //关注度
            wrapper.orderByDesc("buy_count");
        }
        if (!StringUtils.isEmpty(courseFrontVo.getGmtCreateSort())) { //最新
            wrapper.orderByDesc("gmt_create");
        }

        if (!StringUtils.isEmpty(courseFrontVo.getPriceSort())) {//价格
            wrapper.orderByDesc("price");
        }

        baseMapper.selectPage(pageParam,wrapper);

        List<EduCourse> records = pageParam.getRecords();
        long current = pageParam.getCurrent();
        long pages = pageParam.getPages();
        long size = pageParam.getSize();
        long total = pageParam.getTotal();
        boolean hasNext = pageParam.hasNext();//下一页
        boolean hasPrevious = pageParam.hasPrevious();//上一页

        //把分页数据获取出来，放到map集合
        Map<String, Object> map = new HashMap<>();
        map.put("items", records);
        map.put("current", current);
        map.put("pages", pages);
        map.put("size", size);
        map.put("total", total);
        map.put("hasNext", hasNext);
        map.put("hasPrevious", hasPrevious);

        //map返回
        return map;
    }

    //根据课程id，编写sql语句查询课程信息
    @Override
    public CourseWebVo getBaseCourseInfo(String courseId) {
        return baseMapper.getBaseCourseInfo(courseId);
    }


}
