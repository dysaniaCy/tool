package com.yiyouliao.autoprod.liaoyuan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.yiyouliao.autoprod.common.constant.HttpCode;
import com.yiyouliao.autoprod.common.exception.BizException;
import com.yiyouliao.autoprod.liaoyuan.constants.RedisKeyConstant;
import com.yiyouliao.autoprod.liaoyuan.service.TokenService;
import com.yiyouliao.autoprod.liaoyuan.utils.UserContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

import static com.yiyouliao.autoprod.liaoyuan.constants.ContextConstants.TOKEN;

/**
 * Created with IntelliJ IDEA.
 *
 * @author ld
 * @date 2022/8/29
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Resource
    private TokenService tokenService;
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(TOKEN);
        if (StrUtil.isBlank(token)){
            throw new BizException(HttpCode.UN_LOGIN,"请在请求头中添加token");
        }
        //校验token
        Boolean success = tokenService.verifyToken(token);
        if (!success){
            throw new BizException(HttpCode.UN_LOGIN,"token不合法");
        }
        //验证token是否过期
        String redisToken = redisTemplate.opsForValue().get(RedisKeyConstant.TOKEN_PREFIX + token);
        if (StrUtil.isBlank(redisToken)){
            throw new BizException(HttpCode.LOGIN_EXPIRED,"token已经失效，请重新登录");
        }
        //token续期
        redisTemplate.expire(RedisKeyConstant.TOKEN_PREFIX + token,1, TimeUnit.DAYS);
        //用户信息保存到线程中
        tokenService.saveUser(token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
