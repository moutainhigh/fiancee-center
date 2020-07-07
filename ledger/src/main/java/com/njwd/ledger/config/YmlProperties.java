
package com.njwd.ledger.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 配置文件属性
 * @author CJ
 */
@Component
@ConfigurationProperties(prefix = "system.properties")
@Data
public class YmlProperties {
    
    /** 当前环境 dev：开发, test：测试, prod：生产  */
    private String profileActive;
    /**
     * njwd_core项目地址
     */
    private String njwdCoreUrl;
}
