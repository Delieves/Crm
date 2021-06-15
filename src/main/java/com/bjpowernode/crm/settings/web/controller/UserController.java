package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/settings/qx/user/toLogin.do")
    public String toLogin(){

        return "settings/qx/user/login";
    }

    @RequestMapping("/settings/qx/user/login.do")
    public @ResponseBody Object login(String loginAct, String loginPwd, String isRemPwd, HttpServletRequest request, HttpSession session, HttpServletResponse response){
        //封装参数
        Map<String,Object> map = new HashMap<>();
        map.put("loginAct",loginAct);
        map.put("loginPwd",loginPwd);
        //调用service层方法，查询用户
        User user = userService.queryUserByLoginActAndPwd(map);
        //根据查询结果，生成响应信息
        Map<String,Object> retMap = new HashMap<>();
        if(user == null){
            //账号或者密码错误，登入失败
            retMap.put("code","0");
            retMap.put("message","账号或者密码错误");
        }else{
            //进一步判定账号是否过期，状态是否被锁定，ip是否被允许
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
            String nowStr = simpleDateFormat.format(new Date());
            if(nowStr.compareTo(user.getExpireTime())>0){
                //账号已过期，登入失败
                retMap.put("code","0");
                retMap.put("message","账号已过期");

            }else if("0".equals(user.getLockState())){
                //账号状态被锁定，登入失败
                retMap.put("code","0");
                retMap.put("message","账号状态被锁定");

            }else if(user.getAllowIps().contains(request.getRemoteAddr())){
                //ip受限，登录失败、
                retMap.put("code","0");
                retMap.put("message","ip受限");
            }else{
                //登录成功
                retMap.put("code","1");
                //为了在所有的业务页面显示用户的名字，把user保存到session中
                session.setAttribute("sessionUser",user);

                //如果需要记住密码
                if("true".equals(isRemPwd)){
                    Cookie c1 = new Cookie("loginAct",loginAct);
                    c1.setMaxAge(10*24*60*60);
                    response.addCookie(c1);

                    Cookie c2 = new Cookie("loginPwd",loginPwd);
                    c2.setMaxAge(10*24*60*60);
                    response.addCookie(c2);
                }else{//如果不需要记住密码，则清空cookie
                    Cookie c1 = new Cookie("loginAct",loginAct);
                    c1.setMaxAge(0);
                    response.addCookie(c1);

                    Cookie c2 = new Cookie("loginPwd",loginPwd);
                    c2.setMaxAge(0);
                    response.addCookie(c2);
                }
            }

        }
        return retMap;
    }
}
