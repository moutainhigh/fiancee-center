package com.njwd.basedata.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.njwd.entity.pushweb.dto.PushMessageDto;
import com.njwd.basedata.mapper.PushMapper;
import com.njwd.basedata.service.PushService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author bjw
 * @create 2019-08-08 下午 2:04
 */
@Service
public class PushServiceImpl implements PushService {

    @Resource
    private PushMapper pushMapper;

    @Value("${openfire.server}")
    protected String openfireServer;

    @Value("${openfire.domainName}")
    protected  String openfireDomain;

    @Value("${openfire.port}")
    protected  int openfirePort;

    @Value("${openfire.loginAccount}")
    protected  String loginAccount;

    @Value("${openfire.loginPassword}")
    protected  String loginPassword;

    //推送消息
    public boolean pushMsg(PushMessageDto pushMessageDto) {
        return sendSmackMessage(pushMessageDto.getUsername(), pushMessageDto.getContent(),
                pushMessageDto.getSubject(),false);
    }

    //注册新用户
    public boolean regist(PushMessageDto pushMessageDto){
        return registUser(pushMessageDto.getUsername());
    }


    public boolean sendSmackMessage(String username, String content, String subject,boolean deliveryReceiptRequest) {
        try {
            if (StringUtils.isEmpty(username)) {
                return false;
            }
            XMPPTCPConnection con = getXmpptcpConnection();
            con.connect();
            if (con.isConnected()) {
                SASLAuthentication.registerSASLMechanism(new SASLPlainMechanism());
                con.login(loginAccount,loginPassword);//匿名登录
               /* UserSearchManager userSearchManager=new UserSearchManager(con);
                Form searchForm=userSearchManager.getSearchForm("search."+con.getServiceName());
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer("Username", true);
                answerForm.setAnswer("search", username);
                ReportedData data=userSearchManager.getSearchResults(answerForm,"search."+con.getServiceName());
                List<ReportedData.Row> list = data.getRows();*/

                String[] users=username.split(",");
                for(int i=0;i<users.length;i++) {        //循环发送信息
                    Message m = new Message();
                    m.setBody(content);//设置消息。
                    m.setTo(users[i]+"@"+openfireDomain);//设置发送目标
                    if (deliveryReceiptRequest) {
                        m.setType(Message.Type.normal);
                    } else {
                        m.setType(Message.Type.headline);
                    }
                    m.setSubject(subject);
                    con.sendStanza(m);
                }
                PushMessageDto pushMessageDto=new PushMessageDto();
                pushMessageDto.setContent(content);
                pushMessageDto.setSubject(subject);
                pushMessageDto.setFrom_user(loginAccount);
                pushMessageDto.setTo_user(username);
                addPushMessage(pushMessageDto);
            }
            con.disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private XMPPTCPConnection getXmpptcpConnection() {
        XMPPTCPConnectionConfiguration.Builder config=XMPPTCPConnectionConfiguration.builder();
        config.setServiceName(openfireDomain);
        config.setHost(openfireServer);
        config.setPort(openfirePort);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setSendPresence(true);
        config.setCompressionEnabled(false);

        XMPPTCPConnection con = new XMPPTCPConnection(config.build());
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        SASLAuthentication.blacklistSASLMechanism("CRAM-MD5");
        return con;
    }

    /**
     * 注册用户
     *@author bjw
     */
    public boolean registUser(String userName){
        try {
        if (StringUtils.isEmpty(userName)) {
            return false;
        }
        XMPPTCPConnection con = getXmpptcpConnection();
            con.connect();
        if (con.isConnected()) {
            //创建账号成功返回成功
            System.out.println("userName:"+userName);
            AccountManager.getInstance(con).createAccount(userName, userName);
        }
            con.disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage().indexOf("conflict")>=0){
                return true;
            }
            return false;
        }
    }

    /**
     * 记录推送消息内容
     *@author bjw
     */
    /**
     * @return int
     * @Description 新增推送消息
     * @Author bjw
     * @Date 2019/6/23 11:45
     * @Param [pushMessageDto]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPushMessage(PushMessageDto pushMessageDto){
        return pushMapper.addSendMessageInfo(pushMessageDto);
    }

}
