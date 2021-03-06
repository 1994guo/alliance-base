package org.september.simpleweb.controller;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import org.september.core.component.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigCenterRefreshController {

	@Autowired
	private AbstractEnvironment environment;
	
	@ResponseBody
	@RequestMapping(value = "/refresh")
	public String refresh() {
		PropertySource<?> target = null;
		String configLocation = environment.getProperty("spring.config.location");
		for(PropertySource<?> ps : environment.getPropertySources()) {
			if(ps.getName().startsWith("applicationConfig") && StringUtils.startsWithIgnoreCase(configLocation, "http")) {
				target  = ps;
				break;
			}
		}
		if(target ==null) {
			return "";
		}
		Properties pros = new Properties();
		try {
			PropertiesLoaderUtils.fillProperties(pros, new UrlResource(configLocation));
		} catch (IOException e) {
			e.printStackTrace();
			return "reload failed";
		}
		Map map = (Map) target.getSource();
		map.clear();
		map.putAll(pros);
		
		for(String beanName : ApplicationContextHolder.getContext().getBeanDefinitionNames()) {
			Object bean = ApplicationContextHolder.getContext().getBean(beanName);
			for(Field f : bean.getClass().getDeclaredFields()) {
				Value valueAnno = f.getAnnotation(Value.class);
				if(valueAnno==null) {
					continue;
				}
				String key = valueAnno.value();
				if(key==null) {
					continue;
				}
				key = key.replace("${", "").replace("}", "");
				key = key.split(":")[0];
				if(map.containsKey(key)) {
					f.setAccessible(true);
					try {
						f.set(bean, map.get(key));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return "success";
	}
}
