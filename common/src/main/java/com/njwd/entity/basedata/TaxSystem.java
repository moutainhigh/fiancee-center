package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;

/**
 * 税制
 *
 * @author zhuzs
 * @date 2019-07-12 10:48
 */
@Data
public class TaxSystem implements Serializable {
    private static final long serialVersionUID = 5638548836938709736L;

    /**
     * 税制ID
     */
    private Long taxSystemId;

    /**
     * 税制
     */
    private String taxSystemName;
}

