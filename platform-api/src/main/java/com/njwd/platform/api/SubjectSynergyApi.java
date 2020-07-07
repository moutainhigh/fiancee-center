package com.njwd.platform.api;

import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 科目协同配置
 *
 * @author xyyxhcj@qq.com
 * @date 2019/10/28 17:21
 **/
@RequestMapping("platform/subjectSynergy")
public interface SubjectSynergyApi {

    /**
     * 查详情
     *
     * @param subjectSynergy subjectSynergy 必传参数ID
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.SubjectSynergyVo>
     * @author xyyxhcj@qq.com
     * @date 2019/10/28 17:22
     **/
    @PostMapping("findDetail")
    Result<SubjectSynergyVo> findDetail(SubjectSynergy subjectSynergy);

}
