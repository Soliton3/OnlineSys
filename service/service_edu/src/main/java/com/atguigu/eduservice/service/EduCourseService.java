package com.atguigu.eduservice.service;

import com.atguigu.eduservice.entity.EduCourse;
import com.atguigu.eduservice.entity.EduTeacher;
import com.atguigu.eduservice.entity.frontvo.CourseFrontVo;
import com.atguigu.eduservice.entity.frontvo.CourseWebVo;
import com.atguigu.eduservice.entity.vo.CourseInfo;
import com.atguigu.eduservice.entity.vo.CoursePublishVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程 服务类
 * </p>
 *
 * @author testjava
 * @since 2021-03-09
 */
public interface EduCourseService extends IService<EduCourse> {

    //添加课程基本信息的方法
    String saveCourseInfo(CourseInfo courseInfo);

    //根据课程id查询课程信息
    CourseInfo getCourseInfo(String courseId);

    //修改课程信息接口
    void updateCourseInfo(CourseInfo courseInfo);

    //根据课程id查询课程确认信息
    CoursePublishVo publishCourseInfo(String id);

    //条件查询课程带分页的方法
    List<EduCourse> getpageCourseCondition(long current, long limit, EduCourse course);

    //删除课程
    void removeCourse(String courseId);

    List<EduCourse> getCourselist();

    //1 条件查询带分页查询课程
    Map<String, Object> getCourseFrontList(Page<EduCourse> pageCourse, CourseFrontVo courseFrontVo);

    //2 课程详情的方法
    CourseWebVo getBaseCourseInfo(String courseId);
}
