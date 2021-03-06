package com.eddie.serviceedu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.servicebase.exceptionhandler.GuliException;
import com.eddie.serviceedu.entity.EduCourse;
import com.eddie.serviceedu.entity.EduCourseDescription;
import com.eddie.serviceedu.entity.vo.CourseInfoVo;
import com.eddie.serviceedu.entity.vo.CoursePublishVo;
import com.eddie.serviceedu.entity.vo.CourseQuery;
import com.eddie.serviceedu.mapper.EduCourseMapper;
import com.eddie.serviceedu.service.EduChapterService;
import com.eddie.serviceedu.service.EduCourseDescriptionService;
import com.eddie.serviceedu.service.EduCourseService;
import com.eddie.serviceedu.service.EduVideoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 课程 服务实现类
 * </p>
 *
 * @author eddie
 * @since 2020-06-10
 */
@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements EduCourseService {

    /**
     * 课程描述的注入
     */
    @Autowired
    private EduCourseDescriptionService courseDescriptionService;

    /**
     * 小节service
     */
    @Autowired
    private EduVideoService eduVideoService;

    /**
     * 章节service
     */
    @Autowired
    private EduChapterService eduChapterService;

    @Autowired
    private EduCourseDescriptionService eduCourseDescriptionService;

    /**
     * 添加课程基本信息的方法
     *
     * @param courseInfoVo
     * @return
     */
    @Override
    public String saveCourseInfo(CourseInfoVo courseInfoVo) {
        // 1、向课程表添加课程基本信息
        EduCourse eduCourse = new EduCourse();
        // 将CourseInfo转换为EduCourse对象
        BeanUtils.copyProperties(courseInfoVo, eduCourse);
        int insert = baseMapper.insert(eduCourse);
        if (insert == 0) {
            throw new GuliException(20001, "添加课程信息失败");
        }

        // 获取添加之后课程id
        String cId= eduCourse.getId();

        // 2、向课程简介表添加课程简介
        // edu_course_description
        EduCourseDescription courseDescription = new EduCourseDescription();
        courseDescription.setDescription(courseInfoVo.getDescription());
        // 设置描述id就是课程id
        courseDescription.setId(cId);
        courseDescriptionService.save(courseDescription);
        return cId;
    }

    /**
     * 根据课程id查询基本信息
     * @param courseId
     * @return CourseInfoVo对象
     */
    @Override
    public CourseInfoVo getCourseInfo(String courseId) {
        // 1、查询课程表
        EduCourse eduCourse = baseMapper.selectById(courseId);
        CourseInfoVo courseInfoVo = new CourseInfoVo();
        BeanUtils.copyProperties(eduCourse,courseInfoVo);

        // 2、查询描述表
        EduCourseDescription descriptionServiceById = courseDescriptionService.getById(courseId);
        courseInfoVo.setDescription(descriptionServiceById.getDescription());

        return courseInfoVo;
    }

    /**
     * 修改课程信息
     * @param courseInfoVo
     */
    @Override
    public void updateCourseInfo(CourseInfoVo courseInfoVo) {
        // 1、修改课程表
        EduCourse eduCourse = new EduCourse();
        BeanUtils.copyProperties(courseInfoVo,eduCourse);
        int update = baseMapper.updateById(eduCourse);
        if (update == 0){
            throw  new GuliException(20001,"修改课程信息失败");
        }

        // 2、修改评论表
        EduCourseDescription description = new EduCourseDescription();
        description.setId(courseInfoVo.getId());
        description.setDescription(courseInfoVo.getDescription());
        courseDescriptionService.updateById(description);
    }

    /**
     * 根据课程id查询课程确认信息
     * @param id
     * @return CoursePublishVo
     */
    @Override
    public CoursePublishVo getPublishCourseInfo(String id) {
        CoursePublishVo coursePublishVo = baseMapper.getPublishCourseInfo(id);
        return coursePublishVo;
    }

    /**
     * 多条件课程分页查询
     * @param coursePage
     * @param courseQuery
     */
    @Override
    public void pageQuery(Page<EduCourse> coursePage, CourseQuery courseQuery) {
        // 构建条件
        QueryWrapper<EduCourse> courseWrapper = new QueryWrapper<>();
        courseWrapper.orderByDesc();
        System.out.println(courseQuery);
        // 如果courseQuery对象为空，则直接返回分页信息
        if (courseQuery == null){
            baseMapper.selectPage(coursePage,courseWrapper);
            return;
        }
        // 多条件组合查询
        String title = courseQuery.getTitle();
        String status = courseQuery.getStatus();
        if (!StringUtils.isEmpty(title)){
            courseWrapper.like("title",title);
        }
        if (!StringUtils.isEmpty(status)){
            courseWrapper.eq("status",status);
        }
        baseMapper.selectPage(coursePage,courseWrapper);
    }

    @Override
    public void removeCourse(String courseId) {
        // 1、根据课程id删除小节
        eduVideoService.removeVideoByCourseId(courseId);
        // 2、根据课程id删除章节
        eduChapterService.removeChapterByCourseId(courseId);
        // 3、根据课程id删除描述
        eduCourseDescriptionService.removeById(courseId);
        // 4、根据课程id删除课程本身
        int result = baseMapper.deleteById(courseId);
        if (result == 0){
            throw new GuliException(20001,"删除失败");
        }
    }


}
