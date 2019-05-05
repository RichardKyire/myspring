package com.wzt.spring.framework.webmvc.servlet;

import com.wzt.spring.framework.annotation.Controller;
import com.wzt.spring.framework.annotation.RequestMapping;
import com.wzt.spring.framework.annotation.RequestParam;
import com.wzt.spring.framework.aop.AopProxyUtils;
import com.wzt.spring.framework.context.ApplicationContext;
import com.wzt.spring.framework.webmvc.HandlerAdapter;
import com.wzt.spring.framework.webmvc.HandlerMapping;
import com.wzt.spring.framework.webmvc.ModelAndView;
import com.wzt.spring.framework.webmvc.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Servlet只是作为MVC的入口
public class DispatchServlet extends HttpServlet {

  public static final String LOCATION = "contextConfigLocation";
  public static final String TEMPLATE_ROOT = "templateRoot";
  private Properties contextConfig = new Properties();

  private Map<String, Object> iocMap = new ConcurrentHashMap<>();

  private List<String> clazzNames = new ArrayList<>();

  //    private Map<String, HandlerMapping> handlerMapping = new HashMap<>();

  private List<HandlerMapping> handlerMappings = new ArrayList<>();

  private Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<>();

  private List<ViewResolver> viewResolvers = new ArrayList<>();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    this.doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    //        doDispatch(req,resp);
    String url = req.getRequestURI();
    String contextPath = req.getContextPath();
    url = url.replace(contextPath, "").replaceAll("/+", "/");
    //        HandlerMapping handler = handlerMapping.get(url);

    // 对象.方法名才能调用
    // 对象要从Ioc容器中获取
    //        try {
    //            ModelAndView mv = (ModelAndView)
    // handler.getMethod().invoke(handler.getController(),null);
    //        } catch (IllegalAccessException e) {
    //            e.printStackTrace();
    //        } catch (InvocationTargetException e) {
    //            e.printStackTrace();
    //        }

    try {
      doDispatch(req, resp);
    } catch (Exception e) {
      e.printStackTrace();
      resp.getWriter()
          .write(
              "500 Exception, Details :\r\n"
                  + Arrays.toString(e.getStackTrace())
                      .replaceAll("\\[|\\]", "")
                      .replaceAll("\\s", "\r\n"));
    }
  }

  private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

    //根据用户请求的URL来获得一个Handler
    HandlerMapping handler = getHandler(req);

    if(handler == null){
      resp.getWriter().write("404 Not Found\r\n @Kyrie.WuSpringMVC");
      return ;
    }

    HandlerAdapter ha = getHandlerAdapter(handler);

    //这一步只是调用方法，得到返回值
    ModelAndView mv = ha.handle(req, resp, handler);

    //这一步才是真的输出
    processDispatchRequest(resp, mv);
  }

  private void processDispatchRequest(HttpServletResponse resp, ModelAndView mv) throws IOException {
    System.out.println("enter DispatcherServlet.processDispatchRequest()");
    // TODO 调用ViewResovler 的resovleView方法
    if(null == mv){
      return ;
    }
    if(this.viewResolvers.isEmpty()){
      System.out.println("viewResolvers is empty");
      return ;
    }
    for(ViewResolver viewResolver : this.viewResolvers){

      System.out.println(String.format("mv.viewNmae[%s],viewResolver.viewName[%s]",mv.getViewName(),viewResolver.getViewName()));
      if(!mv.getViewName().equals(viewResolver.getViewName())){
        continue;
      }

      String out = viewResolver.viewResolver(mv);

      if(out!=null){
        resp.getWriter().write(out);
      }
    }



  }

  private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {

    if(this.handlerAdapters.isEmpty()){
      return null;
    }
    return this.handlerAdapters.get(handlerMapping);
  }

  private HandlerMapping getHandler(HttpServletRequest req) {

    if(this.handlerMappings.isEmpty()){
      return null;
    }

    String url = req.getRequestURI();
    String contextPath = req.getContextPath();
    url = url.replace(contextPath,"").replaceAll("/+","/");
    for(HandlerMapping handlerMapping : this.handlerMappings){
      Matcher matcher = handlerMapping.getPattern().matcher(url);
      if(!matcher.matches()){
        continue;
      }
      return handlerMapping;
    }

    return null;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {

    // 初始化IOC容器
    ApplicationContext applicationContext =
        new ApplicationContext(config.getInitParameter(LOCATION));

    try {
      initStrategies(applicationContext);
    } catch (Exception e) {
      e.printStackTrace();
    }

    super.init(config);
  }

  protected void initStrategies(ApplicationContext context) throws Exception {

    /** 有九种策略 针对每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出 每种资源可以自定义干预，但是最终的结果都是一致 */
    initMultipartResolver(context);
    initLocaleResolver(context);
    initThemeResolver(context);

    // HandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
    initHandlerMappings(context);

    // handlerAdapter 用来动态匹配Method参数,包括类转换，动态赋值
    initHandlerAdapters(context);

    initHandlerExceptionResolvers(context);
    initRequestToViewNameTranslator(context);

    // 通过ViewResolvers实现动态模板的解析
    // 自己解析一套语言
    initViewResolvers(context);
    initFlashMapManager(context);
  }

  private void initFlashMapManager(ApplicationContext context) {}

  private void initViewResolvers(ApplicationContext context) {

    // 在页面敲一个http://localhost/first.html
    // 解决页面名字和模板文件关联的问题
    String templateRoot = context.getConfig().getProperty(TEMPLATE_ROOT);
    String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
    File templateRootDir = new File(templateRootPath);
    for (File template : templateRootDir.listFiles()) {
      System.out.println(String.format("add viewResolvers(%s,%s)",template.getName(),template));
      this.viewResolvers.add(new ViewResolver(template.getName(), template));
    }
  }

  private void initRequestToViewNameTranslator(ApplicationContext context) {}

  private void initHandlerExceptionResolvers(ApplicationContext context) {}

  private void initHandlerAdapters(ApplicationContext context) {
    // 在初始化阶段，我们能做的就是，将这些参数的名字或者类型按一定的顺序保存下来
    // 因为后面用反射调用的时候，传的形参是一个数组
    // 可以通过记录这些参数的位置index,逐个从数组中填值，这样的话，就和参数的顺序无关了
    for (HandlerMapping handlerMapping : this.handlerMappings) {
      // 每一个方法有一个参数列表，那么这里保存的是形参列表
      Map<String, Integer> paramMapping = new HashMap<>();

      // 这里只处理了命名参数
      Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
      for (int i = 0; i < pa.length; i++) {
        for (Annotation a : pa[i]) {
          if (a instanceof RequestParam) {
            String paramName = ((RequestParam) a).value();
            if (!"".equals(paramName.trim())) {
              paramMapping.put(paramName, i);
            }
          }
        }
      }

      // 接下来，我们处理非命名参数
      // 只处理Request和Response
      Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        Class<?> type = parameterTypes[i];
        if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
          paramMapping.put(type.getName(), i);
        }
      }


      this.handlerAdapters.put(handlerMapping, new HandlerAdapter(paramMapping));
    }
  }

  // 将Controller中配置的RequestMapping 和Method 进行一一对应
  private void initHandlerMappings(ApplicationContext context) throws Exception {
    System.out.println("enter DispatchServlet.initHandlerMappings()");
    // 按照我们通常的理解应该是一个Map
    // Map<String,Method>

    // 首先从容器中取到所有的
    String[] beanNames = context.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      Object proxy = context.getBean(beanName);
      Object controller = AopProxyUtils.getTargetObject(proxy);
      Class<?> clazz = controller.getClass();
      if (!clazz.isAnnotationPresent(Controller.class)) {
        continue;
      }
      String baseUrl = "";
      if (clazz.isAnnotationPresent(RequestMapping.class)) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        baseUrl = requestMapping.value();
      }
      // 扫描所有的public方法
      Method[] methods = clazz.getMethods();
      for (Method method : methods) {
        if (!method.isAnnotationPresent(RequestMapping.class)) {
          continue;
        }
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        String regex = (("/" + baseUrl + methodRequestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/"));
        Pattern pattern = Pattern.compile(regex);
        this.handlerMappings.add(new HandlerMapping(pattern, controller, method));
        System.out.println("Mapping: " + regex + "," + method);
      }
    }
  }

  private void initThemeResolver(ApplicationContext context) {}

  private void initLocaleResolver(ApplicationContext context) {}

  private void initMultipartResolver(ApplicationContext context) {}
}
