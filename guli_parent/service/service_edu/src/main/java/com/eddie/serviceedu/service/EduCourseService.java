package com.eddie.serviceedu.service;

import com.eddie.serviceedu.entity.EduCourse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.serviceedu.entity.vo.CourseInfoVo;

/**
 * <p>
 * 课程 服务类
 * </p>
 *
 * @author eddie
 * @since 2020-06-10
 */
public interface EduCourseService extends IService<EduCourse> {

    /**
     * 添加课程基本信息的方法
     * @param courseInfoVo
     * @return
     */
    String saveCourseInfo(CourseInfoVo courseInfoVo);
}
