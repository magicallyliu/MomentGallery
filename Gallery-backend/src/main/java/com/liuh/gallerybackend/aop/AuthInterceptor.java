package com.liuh.gallerybackend.aop;

import com.liuh.gallerybackend.annotation.AuthCheck;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.model.entity.User;
import com.liuh.gallerybackend.model.enums.UserRoleEnum;
import com.liuh.gallerybackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author LiuH
 * @Date 2025/5/12 下午9:48
 * @PackageName com.liuh.gallerybackend.aop
 * @ClassName AuthInterceptor
 * @Version
 * @Description 权限效验AOP
 */

//- @Target(ElementType.METHOD)
//  - 作用：限定注解的应用范围
//    - ElementType.METHOD 表示该注解只能标注在方法上
//  - 其他常用作用域：
//    - TYPE（类/接口/枚举）
//    - FIELD（字段/枚举常量）
//    - PARAMETER（方法参数）
//    - CONSTRUCTOR（构造函数）
//  - 若不指定@Target，默认允许标注在任何程序元素上
//- @Retention(RetentionPolicy.RUNTIME)
//  - 作用：控制注解的保留阶段
//  - RetentionPolicy.RUNTIME 表示注解会保留到运行时，可通过反射获取
@SuppressWarnings("all")
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截, 环绕拦截 需要authCheck注解才会启动
     * 通过注解输入此时需要的权限
     *
     * @param pjp       切入点
     * @param authCheck 权限效验注解
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint pjp, AuthCheck authCheck) throws Throwable {
        //获取该角色
        String mustRole = authCheck.mustRole();

        //获取当前用户登录信息
        //RequestContextHolder
        //- 是 Spring 提供的工具类，通过 ThreadLocal 机制绑定当前线程的请求属性。
        //- 在 HTTP 请求到达时，Spring 会将请求信息存储到当前线程中，确保在多线程环境下每个请求的数据隔离。
        //currentRequestAttributes()
        //- 静态方法，返回当前线程关联的 RequestAttributes 对象。
        //- 若当前线程没有绑定请求（如非 Web 环境、异步线程或请求未到达时），会抛出 IllegalStateException。
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        //返回该权限
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        //表示不需要权限
        if (mustRoleEnum == null) {
            //放行
            return pjp.proceed();
        }

        //获取用户的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        //表示用户无权限
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //要求管理员的权限. 但是该用户不拥有管理员的权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return pjp.proceed();
    }
}
