package com.zt.ntlm.controller;

import com.zt.ntlm.common.HttpKey;
import com.zt.ntlm.utils.HttpClientUtils;
import org.apache.http.Header;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zt
 * @Date: 2018/12/22 17:40
 */
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private NTLMEngine ntlmEngine;

    private String userName = "Administrator";

    private String PWD = "Cipher2018!";

    private String domain = "zhoutao";

    private String workStation = "192.168.1.175";

    @RequestMapping(value = "/ntlm")
    public void test(HttpServletResponse response, HttpServletRequest request){
        Map<String, String> headerMap = new HashMap<>();
        try {
            headerMap.put("Authorization", "Negotiate " + ntlmEngine.generateType1Msg(null, null));
            Map<String, Object> map = HttpClientUtils.doGet("http://192.168.1.175", headerMap);
            Header[] headers = (Header[]) map.get(HttpKey.HEADERS);
            String status = headerMap.get(HttpKey.STATUS_CODE);
            System.out.println("状态码:=>" + status);
            String messageType2 = null;
            for (Header header : headers) {
                System.out.println(header.getName() + "=>" + header.getValue());
                if ("WWW-Authenticate".equals(header.getName())) {
                    messageType2 = header.getValue();
                }
            }

//            NTLMEngineImpl.Type2Message type2Message = new NTLMEngineImpl.Type2Message(messageType2);
//            byte[] challenge = type2Message.getChallenge();
//            String challengeStr=new String(challenge);
            messageType2=messageType2.substring(9,messageType2.length());

            String type3Message = ntlmEngine.generateType3Msg(userName, PWD, null, null, messageType2);
            System.out.println("type3Message=>" + type3Message);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.addHeader("Authorization","Negotiate "+type3Message);
            response.addHeader("Host","192.168.1.175");
            PrintWriter out=response.getWriter();
            response.sendRedirect("http://192.168.1.175/");
//            out.write("<script>\n" +
//                    "\tlocation.href='http://192.168.1.175';\n" +
//                    "</script>");
        } catch (NTLMEngineException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
