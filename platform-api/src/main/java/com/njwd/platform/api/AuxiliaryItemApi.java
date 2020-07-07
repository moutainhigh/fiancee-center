package com.njwd.platform.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AuxiliaryItemDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:15:38 2019/6/19
 **/
@RequestMapping("platform/auxiliaryItem")
public interface AuxiliaryItemApi {

    /**
     * @Description 根据ID查询辅助核算
     * @Author liuxiang
     * @Date:17:08 2019/7/12
     * @Param [platformAuxiliaryItemDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemById")
    Result<AuxiliaryItemVo> findAuxiliaryItemById(AuxiliaryItemDto platformAuxiliaryItemDto);

    /**
     * @Description 查询辅助核算分页
     * @Author liuxiang
     * @Date:17:08 2019/7/12
     * @Param [platformAuxiliaryItemDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemPage")
    Result<Page<AuxiliaryItemVo>> findAuxiliaryItemPage(AuxiliaryItemDto platformAuxiliaryItemDto);

    /**
     * @Description 查询辅助核算列表
     * @Author liuxiang
     * @Date:15:07 2019/7/2
     * @Param [auxiliaryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemList")
    Result<List<AuxiliaryItemVo>> findAuxiliaryItemList(AuxiliaryItemDto auxiliaryDto);

    /**
     * @Description 根据名称字符串查询辅助核算
     * @Author liuxiang
     * @Date:17:09 2019/7/12
     * @Param [platformAuxiliaryItemDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemListByNames")
    Result<List<AuxiliaryItemVo>> findAuxiliaryItemListByNames(AuxiliaryItemDto platformAuxiliaryItemDto);

    /**
     * @Description 查询所有未删除的辅助核算类型
     * @Author wuweiming
     * @Param []
     * @return Result<List<AuxiliaryItemVo>>
     **/
    @PostMapping("findAllAuxiliaryItem")
    Result<List<AuxiliaryItemVo>> findAllAuxiliaryItem();

    /**
     * 查询科目表配置的辅助核算
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/12/3
     */
    @RequestMapping("findBySubjectId")
    Result<List<AuxiliaryItemVo>> findBySubjectId(@RequestBody SubjectDto subjectDto);
}
