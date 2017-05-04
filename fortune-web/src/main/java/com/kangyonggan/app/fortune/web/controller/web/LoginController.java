package com.kangyonggan.app.fortune.web.controller.web;

import com.kangyonggan.app.fortune.biz.service.UserService;
import com.kangyonggan.app.fortune.model.constants.AppConstants;
import com.kangyonggan.app.fortune.model.vo.User;
import com.kangyonggan.app.fortune.web.controller.BaseController;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 2017/1/8
 */
@Controller
@RequestMapping("/")
@Log4j2
public class LoginController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 登录界面
     *
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login() {
        return getPathIndex();
    }

    /**
     * 登录
     *
     * @param user
     * @param captcha
     * @param request
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> login(@RequestParam(value = "captcha") String captcha, User user, HttpServletRequest request) {
        Map<String, Object> resultMap = getResultMap();

        HttpSession session = request.getSession();
        String realCaptcha = (String) session.getAttribute(AppConstants.KEY_CAPTCHA);
        log.info("session中的验证码为：{}", realCaptcha);
        log.info("用户上送的验证码为：{}", captcha);

        if (!captcha.equalsIgnoreCase(realCaptcha)) {
            resultMap.put(ERR_CODE, FAILURE);
            resultMap.put(ERR_MSG, "验证码错误或已失效");
            return resultMap;
        }

        // 清除验证码
        session.removeAttribute(AppConstants.KEY_CAPTCHA);

        UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        final Subject subject = SecurityUtils.getSubject();

        try {
            session.setMaxInactiveInterval(30 * 24 * 60 * 60);// 30天
            subject.login(token);
        } catch (UnknownAccountException uae) {
            log.warn("未知用户名", uae);
            setResultMapFailure(resultMap, "未知用户名");
            return resultMap;
        } catch (IncorrectCredentialsException ice) {
            log.warn("用户名或密码错误", ice);
            setResultMapFailure(resultMap, "用户名或密码错误");
            return resultMap;
        } catch (DisabledAccountException dae) {
            log.warn("账号已禁用", dae);
            setResultMapFailure(resultMap, "账号已禁用, 请联系管理员");
            return resultMap;
        } catch (Exception e) {
            log.error("未知异常", e);
            setResultMapFailure(resultMap);
            return resultMap;
        }

        String redirectUrl = "/dashboard";

        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        // 获取之前访问的URL
        if (savedRequest != null && savedRequest.getRequestUrl() != null) {
            redirectUrl = savedRequest.getRequestUrl();
        }
        resultMap.put(ERR_MSG, redirectUrl);

        return resultMap;
    }

    /**
     * 注销
     *
     * @return
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logout() {
        final Subject subject = SecurityUtils.getSubject();
        log.info("logout {}", subject.getPrincipal());
        subject.logout();
        return "redirect:/";
    }

}
