package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.MessageService;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.MessageDto;
import com.njwd.entity.platform.vo.MessageVo;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:52
 */
@RestController
@RequestMapping("message")
public class MessageController extends BaseController {

    @Resource
    private MessageService messageService;

    @RequestMapping("addMessage")
    public Result addMessage(@RequestBody MessageDto messageDto){
        messageService.addMessage(messageDto);
        return ok();
    }

    /**
     * 更新消息
     * @Author lj
     * @Date:15:02 2019/12/4
     * @param messageDto
     * @return com.njwd.support.Result<java.lang.Integer>
     **/
    @RequestMapping("updateMessageBatch")
    public Result<Integer> updateMessageBatch(@RequestBody MessageDto messageDto){
        FastUtils.checkListNullOrEmpty(messageDto.getIds());
        return ok(messageService.updateMessageBatch(messageDto));
    }

    /**
     * 查询消息列表分页
     * @Author lj
     * @Date:14:45 2019/12/4
     * @param messageDto
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.MessageVo>>
     **/
    @RequestMapping("findMessagePage")
    public Result<Page<MessageVo>> findMessagePage(@RequestBody MessageDto messageDto) {
        SysUserVo operator = UserUtils.getUserVo();
        messageDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(messageService.findMessagePage(messageDto));
    }
}
