package com.af.formework.springmvc.servlet;

import com.af.formework.annotation.AfController;
import com.af.formework.annotation.AfRequestMapper;
import com.af.formework.context.AfApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2019/8/22.
 */
public class AfDispatcherServlet extends HttpServlet {


    private final String CONTEXT_LOCATION = "contextConfigLocation";

    private List<AfHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<AfHandlerMapping, AfHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<AfViewResolver> viewResolvers = new ArrayList<AfViewResolver>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化ApplciationContext
        AfApplicationContext context = new AfApplicationContext(config.getInitParameter(CONTEXT_LOCATION));
        //2、初始化SpringMVC九大组件

        //handlerMapping
        initHandlerMappings(context);
        //参数适配器
        initHandlerAdapters(context);
        //视图转换器
        initViewResolvers(context);

    }

    private void initViewResolvers(AfApplicationContext context) {
        //拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i < templates.length; i++) {
            //这里主要是为了兼容多模板，所有模仿Spring用List保存
            //在我写的代码中简化了，其实只有需要一个模板就可以搞定
            //只是为了仿真，所有还是搞了个List
            this.viewResolvers.add(new AfViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(AfApplicationContext context) {
        for (AfHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new AfHandlerAdapter());
        }
    }

    private void initHandlerMappings(AfApplicationContext context) {
        String[] beanNames = context.getBeanNames();
        for (String beanName : beanNames) {
            Object controller = context.getBean(beanName);
            Class<?> clazz = controller.getClass();
            if (!clazz.isAnnotationPresent(AfController.class)) {
                continue;
            }
            String baseUrl = "";
            //获取Controller的url配置
            if (clazz.isAnnotationPresent(AfRequestMapper.class)) {
                AfRequestMapper requestMapping = clazz.getAnnotation(AfRequestMapper.class);
                baseUrl = requestMapping.value();
            }
            //获取Method的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                //没有加RequestMapping注解的直接忽略
                if (!method.isAnnotationPresent(AfRequestMapper.class)) {
                    continue;
                }
                //映射URL
                AfRequestMapper requestMapping = method.getAnnotation(AfRequestMapper.class);
                //  /demo/query

                //  (//demo//query)
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                this.handlerMappings.add(new AfHandlerMapping(pattern, controller, method));
                // log.info("Mapped " + regex + "," + method);
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AfHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processResutl(req, resp, new AfModelAndView("404"));
            return;
        }

        //调用前准备参数
        AfHandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        AfModelAndView mv = handlerAdapter.handle(req, resp, handler);
        processResutl(req, resp, mv);
    }

    private AfHandlerAdapter getHandlerAdapter(AfHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        AfHandlerAdapter ha = this.handlerAdapters.get(handler);
        if (ha.supports(handler)) {
            return ha;
        }
        return null;
    }

    private void processResutl(HttpServletRequest req, HttpServletResponse resp, AfModelAndView mv) throws Exception {
        if(mv == null){
            return;
        }
        if(this.viewResolvers.isEmpty()){
            return;
        }
        for(AfViewResolver resolver : viewResolvers){
            AfView view = resolver.resolveViewName(mv.getViewName(), null);
            view.render(mv.getModel(),req,resp);
        }

    }

    private AfHandlerMapping getHandler(HttpServletRequest req) throws Exception {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (AfHandlerMapping handler : this.handlerMappings) {
            try {
                Matcher matcher = handler.getPattern().matcher(url);
                //如果没有匹配上继续下一个匹配
                if (!matcher.matches()) {
                    continue;
                }

                return handler;
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

}
