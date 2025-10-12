package com.liuh.gallerybackend.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求包装过滤器
 *
 * 通常使用 ServletUtil.getBody(request); 获取请求体
 *      因为是以流的形式, 所有只能读取一遍
 *     通过包装请求，使 InputStream 可以重复读取
 *     同RequestWrapper一同使用
 *
 * @author pine
 */
@Order(1)
@Component
public class HttpRequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());
            if (ContentType.JSON.getValue().equals(contentType)) {
                // 可以再细粒度一些，只有需要进行空间权限校验的接口才需要包一层
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

}
