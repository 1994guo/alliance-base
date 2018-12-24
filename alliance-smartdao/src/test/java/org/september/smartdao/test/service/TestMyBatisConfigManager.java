package org.september.smartdao.test.service;

import org.september.smartdao.config.MyBatisConfigManager;
import org.springframework.stereotype.Component;

/**
 * @author yexinzhou
 * @Description:
 * @date 10:58 2018/1/2
 */
@Component
public class TestMyBatisConfigManager implements MyBatisConfigManager {

	@Override
	public String getTypeAliasesPackage() {
		return "org.september.smartdao.entity";
	}

	@Override
	public String getMapperLocation() {
		// 配置测试的mapper
		return this.getClass().getResource("/")+"test/mybatis/mapper/**/*.xml";
	}
}
