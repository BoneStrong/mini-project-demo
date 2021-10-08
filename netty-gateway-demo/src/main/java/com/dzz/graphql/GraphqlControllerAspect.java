package com.dzz.graphql;

import com.dzz.graphql.interf.RestControllerResponseProvider;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ClassStack;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.kickstart.execution.GraphQLInvoker;
import graphql.kickstart.execution.GraphQLObjectMapper;
import graphql.kickstart.execution.GraphQLQueryResult;
import graphql.kickstart.execution.GraphQLRequest;
import graphql.kickstart.execution.input.GraphQLSingleInvocationInput;
import graphql.servlet.input.GraphQLInvocationInputFactory;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
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
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zoufeng
 */
@Aspect
public class GraphqlControllerAspect implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlControllerAspect.class);
    private static final String GRAPHQL_CUSTOM_SEPARATOR = ";";

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

    private Method _fromAnyMethod;

    private RestControllerResponseProvider.DefaultRestControllerResponseProvider defaultRestControllerResponseProvider = new RestControllerResponseProvider.DefaultRestControllerResponseProvider();

    private RestControllerResponseProvider customerRestControllerResponseProvider;

    private static ConcurrentHashMap<HandlerMethod, RestControllerResponseProvider> methodResponseProviderMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<HandlerMethod, Type> methodActualTypeArgumentMap = new ConcurrentHashMap<>();

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

        _fromAnyMethod = TypeFactory.defaultInstance().getClass().getDeclaredMethod("_fromAny", ClassStack.class, Type.class, TypeBindings.class);
        _fromAnyMethod.setAccessible(true);

        logger.info("GraphqlControllerAspect init ok ");
    }

    @Pointcut(" @annotation(com.pingan.egis.optim.platform.graphql.ano.GraphqlMapping)")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String header = request.getHeader(GraphqlRestfulConstant.GRAPHQL_PROJECTION_HEADER);
        String header2 = request.getHeader(GraphqlRestfulConstant.GRAPHQL_PROJECTION_HEADER_URLENCODED);

        if (StringUtils.isAllBlank(header, header2)) {
            return proceedingJoinPoint.proceed();
        }

        String graphql = null;
        try {
            if (StringUtils.isNotBlank(header)) {
                graphql = header.replace(GRAPHQL_CUSTOM_SEPARATOR, System.lineSeparator());
            } else if (StringUtils.isNotBlank(header2)) {
                graphql = URLDecoder.decode(header, StandardCharsets.UTF_8.toString()).replace(GRAPHQL_CUSTOM_SEPARATOR, System.lineSeparator());
            }
        } catch (UnsupportedEncodingException e) {
            logger.info("{} head decode exception ,to do restful request", GraphqlRestfulConstant.GRAPHQL_PROJECTION_HEADER);
            return proceedingJoinPoint.proceed();
        }

        HandlerExecutionChain handlerChain = requestMappingHandlerMapping.getHandler(request);
        HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();

        //获取请求参数,没有则为null
        Map<String, Object> variables = getMethodVariables(handlerMethod);

        GraphQLRequest graphQLRequest = new GraphQLRequest(graphql, variables, null);
        logger.debug("graphql is {} ", graphql);
        logger.debug("variables is {} ", variables);
        GraphQLSingleInvocationInput invocationInput = invocationInputFactory.create(graphQLRequest);

        GraphQLQueryResult query = queryInvoker.query(invocationInput);
        return parseGraphQLQueryResult(handlerMethod, query, request, response);
    }

    private Object parseGraphQLQueryResult(HandlerMethod handlerMethod, GraphQLQueryResult query
            , HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ExecutionResult executionResult = query.getResult();

        RestControllerResponseProvider responseProvider = getResponseProvider(handlerMethod);

        if (executionResult.getErrors().isEmpty()) {
            Map<String, Object> result = graphQLObjectMapper.createResultFromExecutionResult(query.getResult());
            Map<String, Object> dataMap = checkAndParseMap(result);

            JavaType javaType = null;
            try {
                javaType = (JavaType) _fromAnyMethod.invoke(graphQLObjectMapper.getJacksonMapper().getTypeFactory()
                        ,  null
                        , methodActualTypeArgumentMap.get(handlerMethod)
                        , TypeBindings.emptyBindings());
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }

            Object actualEntity = graphQLObjectMapper.getJacksonMapper().convertValue(dataMap, javaType);

            return responseProvider.responseOkFunction().apply(actualEntity);

        }

        List<GraphQLError> errors = executionResult.getErrors();
        GraphQLError graphQLError = errors.get(0);
        //执行service 异常
        if (graphQLError instanceof ExceptionWhileDataFetching) {
            Throwable t = ((ExceptionWhileDataFetching) graphQLError).getException();
            if (responseProvider instanceof RestControllerResponseProvider.DefaultRestControllerResponseProvider)
                throw t;
            else
                return responseProvider.responseExceptionFunction()
                        .apply(graphQLError.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), t);
        }

        return responseProvider.responseExceptionFunction()
                .apply(graphQLError.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null);

    }

    private Map<String, Object> checkAndParseMap(Map<String, Object> result) {
        if (result == null)
            throw new RuntimeException("graph parse query sql result error");

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Collection<Object> values = data.values();
        Map<String, Object> dataMap = null;

        if (!values.isEmpty()) {
            if (values.size() > 1)
                throw new RuntimeException(String.format("graph parse query sql result error, graphQl data size >1 ,data is %s"
                        , values.toString()));
            Object o = values.iterator().next();
            if (o instanceof Map) {
                dataMap = (Map<String, Object>) o;
            }
        }
        return dataMap;
    }


    private RestControllerResponseProvider getResponseProvider(HandlerMethod handlerMethod) {
        if (methodResponseProviderMap.get(handlerMethod) == null) {
            ParameterizedTypeImpl type = (ParameterizedTypeImpl) ResolvableType.forMethodReturnType(handlerMethod.getMethod()).getType();
            Class<?> rawType = type.getRawType();
            Type[] actualTypeArguments = type.getActualTypeArguments();

            if (rawType.getTypeName().equals("com.dzz.graphql.interf.Response")) {
                methodResponseProviderMap.put(handlerMethod, defaultRestControllerResponseProvider);

            } else {
                if (customerRestControllerResponseProvider == null)
                    throw new RuntimeException(String.format("graphql module don`t support %s , please config RestControllerResponseProvider "
                            , rawType.getTypeName()));
                methodResponseProviderMap.put(handlerMethod, customerRestControllerResponseProvider);
            }

            Type actualTypeArgument = actualTypeArguments[0];

            methodActualTypeArgumentMap.put(handlerMethod, actualTypeArgument);
        }
        return methodResponseProviderMap.get(handlerMethod);
    }


    private Map<String, Object> getMethodVariables(HandlerMethod handlerMethod) throws Exception {

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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, RestControllerResponseProvider> beans = applicationContext.getBeansOfType(RestControllerResponseProvider.class);

        if (beans.isEmpty()) {
            logger.debug("No customized RestControllerResponseProvider found, using DefaultRestControllerResponseProvider ");
            return;
        }
        if (beans.size() > 1) {
            String[] names = applicationContext.getBeanNamesForType(RestControllerResponseProvider.class);
            logger.error("RestControllerResponseProvider only need 1 bean ,now spring have beans : {}", String.join(",", names));
            throw new IllegalStateException("RestControllerResponseProvider have more than 1 impl bean ,please delete someone");
        }
        customerRestControllerResponseProvider = applicationContext.getBean(RestControllerResponseProvider.class);
    }
}
