package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "LoginCheckFilter")
public class LoginCheckFilter implements Filter {
    // 路径匹配器
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;

        String requestURI = servletRequest.getRequestURI();

        // log.info("拦截到的请求:" + servletRequest.getRequestURL());

        String[] requestURL = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };

        boolean checkURL = checkURL(requestURL, requestURI);
        // 如果不需要处理则放行

        if (checkURL) {
            chain.doFilter(request, response);
            return;
        }
        // 判断登录状态是否放行，如果已经登录，则放行
        if (servletRequest.getSession().getAttribute("employee") != null) {
            // 获取当前用户登录的id值
            Long employee = (Long) servletRequest.getSession().getAttribute("employee");
            // 将id值存入线程中
            BaseContext.setThreadLocalid(employee);

            chain.doFilter(request, response);

            return;
        }

    // 判断登录状态是否放行，如果已经登录，则放行
        if (servletRequest.getSession().getAttribute("user") != null) {
            // 获取当前用户登录的id值
            Long user = (Long) servletRequest.getSession().getAttribute("user");
            // 将id值存入线程中
            BaseContext.setThreadLocalid(user);

            chain.doFilter(request, response);

            return;
        }


        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /**
     * 判断请求是否相同,检查本次亲求是否放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean checkURL(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
