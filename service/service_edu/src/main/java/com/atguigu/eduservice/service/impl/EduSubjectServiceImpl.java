package com.atguigu.eduservice.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.eduservice.entity.EduSubject;
import com.atguigu.eduservice.entity.subject.OneSubject;
import com.atguigu.eduservice.entity.subject.TwoSubject;
import com.atguigu.eduservice.excel.SubjectData;
import com.atguigu.eduservice.listener.SubjectExcelListener;
import com.atguigu.eduservice.mapper.EduSubjectMapper;
import com.atguigu.eduservice.service.EduSubjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程科目 服务实现类
 * </p>
 *
 * @author testjava
 * @since 2021-03-08
 */
@Service
public class EduSubjectServiceImpl extends ServiceImpl<EduSubjectMapper, EduSubject> implements EduSubjectService {

    //添加课程分类
    @Override
    public void saveSubject(MultipartFile file, EduSubjectService subjectService) {
        try{
            //文件输入流
            InputStream in = file.getInputStream();
            //调用方法进行读取
            EasyExcel.read(in, SubjectData.class,new SubjectExcelListener(subjectService)).sheet().doRead();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //课程分类列表（树形）
    @Override
    public List<OneSubject> getAllOneTwoSubject() {
        //1 查询一级分类
        QueryWrapper<EduSubject> wrapperOne = new QueryWrapper();
        wrapperOne.eq("parent_id","0");
        List<EduSubject> oneSubjectList = baseMapper.selectList(wrapperOne);
        //或者使用
//        this.list(wrapperOne);
        //2 查询二级分类
        QueryWrapper<EduSubject> wrapperTwo = new QueryWrapper();
        wrapperTwo.ne("parent_id","0");
        List<EduSubject> twoSubjectList = baseMapper.selectList(wrapperTwo);

        //创建list集合，用于存储最终的封装数据
        List<OneSubject> finalSubject = new ArrayList<>();
        //3 封装一级分类
        //查询出所有一级分类list集合遍历，得到每一个一级分类对象，获取每一个一级分类对象值，封装到要求的finalSubject集合中
        for (int i = 0; i < oneSubjectList.size(); i++) {//遍历oneSubject
            //oneSubjectList的每个oneSubject
            EduSubject eduSubject = oneSubjectList.get(i);
            //把eduSubject中的值放入oneSubject中
            OneSubject oneSubject = new OneSubject();
            BeanUtils.copyProperties(eduSubject,oneSubject);
            //多个oneSubject放入finalSubject中
            finalSubject.add(oneSubject);

            //4 封装二级分类
            List<TwoSubject> twoFinalSubjectList = new ArrayList<>();
            for (int m = 0; m < twoSubjectList.size(); m++) {
                EduSubject twoeduSubject = twoSubjectList.get(m);
                //判断二级分类的parent_id和一级分类的id是否一样
                if (twoeduSubject.getParentId().equals(eduSubject.getId())){
                    //把twoeduSubject复制到twoFinalSubjectList中
                    TwoSubject twoSubject = new TwoSubject();
                    BeanUtils.copyProperties(twoeduSubject,twoSubject);
                    twoFinalSubjectList.add(twoSubject);
                }
            }
            //把二级分类放入一级分类中
            oneSubject.setChildren(twoFinalSubjectList);
        }




        return finalSubject;
    }
}
