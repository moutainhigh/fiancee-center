package com.njwd.financeback.service;

import com.njwd.entity.platform.SysAuxData;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.Result;

import java.util.List;

/**
 * @description: 辅助资料
 * @author: zhuzs
 * @create: 2019/5/25
 */
public interface SysAuxDataService {

     /**
      * 根据类型查询辅助资料列表
      * @param dataType
      * @return
      */
     List<SysAuxDataVo> findAuxDataList(SysAuxDataDto dataType);

     /**
      * 根据类型和名称或者编码  分页  查询辅助资料列表
      * @param dataTypeAndCodeOrName
      * @return
      */
     Result findAuxDataListByCodeOrName(SysAuxDataDto dataTypeAndCodeOrName);


     /**
      * 根据类型和名称  查询辅助资料表
      * @param dataTypeName
      * @return
      */
     Result findAuxDataByName(SysAuxDataDto dataTypeName);

}
