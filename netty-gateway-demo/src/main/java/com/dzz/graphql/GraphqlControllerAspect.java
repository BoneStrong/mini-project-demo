package com.dzz.graphql;

import graphql.kickstart.execution.GraphQLInvoker;
import graphql.kickstart.execution.GraphQLObjectMapper;
import graphql.kickstart.execution.GraphQLQueryResult;
import graphql.kickstart.execution.GraphQLRequest;
import graphql.kickstart.execution.input.GraphQLSingleInvocationInput;
import graphql.servlet.input.GraphQLInvocationInputFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zoufeng
 * @date 2020-3-20
 */
@Aspect
public class GraphqlControllerAspect {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlControllerAspect.class);

    @Autowired
    private GraphQLInvocationInputFactory invocationInputFactory;
    @Autowired
    private GraphQLInvoker queryInvoker;
    @Autowired
    private GraphQLObjectMapper graphQLObjectMapper;
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private Method getDataBinderFactoryMethod;

    private Method getModelFactoryMethod;

    private Method getMethodArgumentValuesMethod;

    @PostConstruct
    public void init() throws NoSuchMethodException {
        List<HandlerMethodArgumentResolver> resolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        argumentResolvers.addResolvers(resolvers);

        getDataBinderFactoryMethod = requestMappingHandlerAdapter.getClass().getDeclaredMethod("getDataBinderFactory", HandlerMethod.class);
        getDataBinderFactoryMethod.setAccessible(true);

        getModelFactoryMethod = requestMappingHandlerAdapter.getClass().getDeclaredMethod("getModelFactory", HandlerMethod.class, WebDataBinderFactory.class);
        getModelFactoryMethod.setAccessible(true);

        getMethodArgumentValuesMethod = InvocableHandlerMethod.class.getDeclaredMethod("getMethodArgumentValues", NativeWebRequest.class, ModelAndViewContainer.class, Object[].class);
        getMethodArgumentValuesMethod.setAccessible(true);

        logger.info("GraphqlControllerAspect init ok ");
    }

    @Pointcut(" @annotation(com.dzz.graphql.GraphqlMapping)")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String header = request.getHeader(GraphqlRestfulConstant.GRAPHQL_HEAD);

        if (header != null) {
            String graphql = null;
            try {
                graphql = URLDecoder.decode(header, "utf-8");
            } catch (UnsupportedEncodingException e) {
                logger.info("{} head decode exception ,to do restful request", GraphqlRestfulConstant.GRAPHQL_HEAD);
                return proceedingJoinPoint.proceed();
            }

            HandlerExecutionChain handlerChain = requestMappingHandlerMapping.getHandler(request);
            HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();

            //获取请求参数,没有则为null
            Map<String, Object> varibles = getMethodVaribles(handlerMethod);

            GraphQLRequest graphQLRequest = new GraphQLRequest(graphql, varibles, null);
            logger.debug("graphql is {} ", graphql);
            logger.debug("varibles is {} ", varibles);
            GraphQLSingleInvocationInput invocationInput = invocationInputFactory.create(graphQLRequest);
            GraphQLQueryResult query = queryInvoker.query(invocationInput);
            return graphQLObjectMapper.serializeResultAsJson(query.getResult());
        }
        return proceedingJoinPoint.proceed();
    }

    private Map<String, Object> getMethodVaribles(HandlerMethod handlerMethod) throws Exception {

        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        if (methodParameters.length == 0) {
            return null;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        Map<String, Object> varibles = new HashMap<>();

        WebDataBinderFactory dataBinderFactory = (WebDataBinderFactory) getDataBinderFactoryMethod.invoke(requestMappingHandlerAdapter, handlerMethod);

        ServletInvocableHandlerMethod invocableHandlerMethod = new ServletInvocableHandlerMethod(handlerMethod);

        invocableHandlerMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
        invocableHandlerMethod.setParameterNameDiscoverer(parameterNameDiscoverer);

        invocableHandlerMethod.setDataBinderFactory(dataBinderFactory);

        ModelFactory modelFactory = (ModelFactory) getModelFactoryMethod.invoke(requestMappingHandlerAdapter, handlerMethod, dataBinderFactory);

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
        modelFactory.initModel(webRequest, mavContainer, invocableHandlerMethod);

        Object[] args = (Object[]) getMethodArgumentValuesMethod.invoke(invocableHandlerMethod, webRequest, mavContainer, new Object[]{});

        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter methodParameter = methodParameters[i];
            GraphqlParam annotation = methodParameter.getParameterAnnotation(GraphqlParam.class);
            if (annotation != null) {
                varibles.put(annotation.name(), args[i]);
            }
        }
        return varibles;
    }

}
