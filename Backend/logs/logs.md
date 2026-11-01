Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-10-31T09:31:52.851-03:00 ERROR 11716 --- [manager-zeta-pilot] [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'adminSmtpSettingsController' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\controller\AdminSmtpSettingsController.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'smtpSettingsService' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\service\SmtpSettingsService.class]: Unsatisfied dependency expressed through constructor parameter 1: Error creating bean with name 'settingsCryptoService' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\service\SettingsCryptoService.class]: Failed to instantiate [com.zetapilot.manager.service.SettingsCryptoService]: Constructor threw exception
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:971) ~[spring-context-6.1.12.jar:6.1.12]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:625) ~[spring-context-6.1.12.jar:6.1.12]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.3.3.jar:3.3.3]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.3.3.jar:3.3.3]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.3.3.jar:3.3.3]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:335) ~[spring-boot-3.3.3.jar:3.3.3]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1363) ~[spring-boot-3.3.3.jar:3.3.3]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1352) ~[spring-boot-3.3.3.jar:3.3.3]
	at com.zetapilot.manager.ManagerApplication.main(ManagerApplication.java:13) ~[classes/:na]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'smtpSettingsService' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\service\SmtpSettingsService.class]: Unsatisfied dependency expressed through constructor parameter 1: Error creating bean with name 'settingsCryptoService' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\service\SettingsCryptoService.class]: Failed to instantiate [com.zetapilot.manager.service.SettingsCryptoService]: Constructor threw exception
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:795) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:237) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.12.jar:6.1.12]
	... 19 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'settingsCryptoService' defined in file [C:\Desenvolvimento\ZetaPilotAI\Sistema\manager-zeta-pilot\Backend\target\classes\com\zetapilot\manager\service\SettingsCryptoService.class]: Failed to instantiate [com.zetapilot.manager.service.SettingsCryptoService]: Constructor threw exception
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:318) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:306) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1375) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1212) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:562) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:337) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:335) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:904) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:782) ~[spring-beans-6.1.12.jar:6.1.12]
	... 33 common frames omitted
Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.zetapilot.manager.service.SettingsCryptoService]: Constructor threw exception
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:221) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:117) ~[spring-beans-6.1.12.jar:6.1.12]
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:315) ~[spring-beans-6.1.12.jar:6.1.12]
	... 47 common frames omitted
Caused by: java.lang.IllegalStateException: SETTINGS_ENCRYPTION_KEY deve ter pelo menos 32 bytes aleatórios.
	at com.zetapilot.manager.service.SettingsCryptoService.<init>(SettingsCryptoService.java:25) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45) ~[na:na]
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500) ~[na:na]
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481) ~[na:na]
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:208) ~[spring-beans-6.1.12.jar:6.1.12]
	... 49 common frames omitted


Process finished with exit code 1
