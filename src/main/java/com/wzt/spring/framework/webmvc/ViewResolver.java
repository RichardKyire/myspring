package com.wzt.spring.framework.webmvc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewResolver {
  private String viewName;
  private File template;

  public ViewResolver(String name, File template) {
    this.viewName = name;
    this.template = template;
  }

  public String getViewName() {
    return viewName;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public File getTemplate() {
    return template;
  }

  public void setTemplate(File template) {
    this.template = template;
  }

  public String viewResolver(ModelAndView mv) throws IOException {

    System.out.println(String.format("enter ViewResolver.viewResolver(%s)", mv));

    StringBuffer sb = new StringBuffer();

    RandomAccessFile ra = new RandomAccessFile(this.template, "r");
    String line = null;

    while (null != (line = ra.readLine())) {
      System.out.println("read line before replace :" + line);
      Matcher m = matcher(line);
      while (m.find()) {
        System.out.println(String.format("can match , groupCount[%d]",m.groupCount()));
        for (int i = 0; i < m.groupCount(); i++) {
          // 要把${ }中间的字符串给取出来
          String paramName = m.group(i).replaceAll("\\$\\{","").replaceAll("\\}","");
          Object paramValue = mv.getModel().get(paramName);
            System.out.println(String.format("paramValue[%s]",paramValue));
            if (null == paramValue) {
            continue;
          }
          line = line.replaceAll("\\$\\{" + paramName + "\\}", paramValue.toString());
          System.out.println("after replace:" + line);
        }
      }
      sb.append(line);
    }

    return sb.toString();
  }

  private Matcher matcher(String line) {
    Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(line);
    return matcher;
  }
}
