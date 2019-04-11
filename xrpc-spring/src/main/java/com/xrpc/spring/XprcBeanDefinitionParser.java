/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xrpc.spring;

import com.xprc.commons.model.NodeConfig;
import com.xrpc.core.BootManager;
import com.xrpc.core.client.ReferBean;
import com.xrpc.core.server.ServiceBean;
import com.xrpc.register.RegisterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class XprcBeanDefinitionParser implements org.springframework.beans.factory.xml.BeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(XprcBeanDefinitionParser.class);

    private final Class<?> beanClass;

    private final boolean required;


    public XprcBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }


    public BeanDefinition parse(Element element, ParserContext parserContext) {
        try {
            return parse(element, parserContext, beanClass, required);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) throws ClassNotFoundException {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);


        if (NodeConfig.class.equals(beanClass)) {
            if(!parserContext.getRegistry().containsBeanDefinition(NodeConfig.class.getName())){
                parserContext.getRegistry().registerBeanDefinition(NodeConfig.class.getName(), beanDefinition);

                String application = element.getAttribute("application");
                if (!StringUtils.isEmpty(application)) {
                    beanDefinition.getPropertyValues().addPropertyValue("application", application);
                }

                String port = element.getAttribute("port");
                if (!StringUtils.isEmpty(port)) {
                    beanDefinition.getPropertyValues().addPropertyValue("port", port);
                }
            }

        } else if (RegisterConfig.class.equals(beanClass)) {

            if(!parserContext.getRegistry().containsBeanDefinition(RegisterConfig.class.getName())){
                parserContext.getRegistry().registerBeanDefinition(RegisterConfig.class.getName(), beanDefinition);

                String address = element.getAttribute("address");
                if (!StringUtils.isEmpty(address)) {
                    beanDefinition.getPropertyValues().addPropertyValue("address", address);
                }

                String protocol = element.getAttribute("protocol");
                if (!StringUtils.isEmpty(protocol)) {
                    beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
                }
            }


        } else if (BootManager.class.equals(beanClass)) {
            if(!parserContext.getRegistry().containsBeanDefinition(BootManager.class.getName())){

                beanDefinition.setDependsOn(new String[]{NodeConfig.class.getName(),RegisterConfig.class.getName()});
                parserContext.getRegistry().registerBeanDefinition(BootManager.class.getName(), beanDefinition);

                if(parserContext.getRegistry().containsBeanDefinition(NodeConfig.class.getName())){
                    BeanDefinition nodeBean = parserContext.getRegistry().getBeanDefinition(NodeConfig.class.getName());
                    beanDefinition.getPropertyValues().addPropertyValue("nodeConfig", nodeBean);
                }

                if(parserContext.getRegistry().containsBeanDefinition(RegisterConfig.class.getName())){
                    BeanDefinition registerBean = parserContext.getRegistry().getBeanDefinition(RegisterConfig.class.getName());
                    beanDefinition.getPropertyValues().addPropertyValue("registerConfig", registerBean);
                }

            }

        } else if (ServiceBean.class.equals(beanClass)) {

            String interfaceName = element.getAttribute("interface");
            String version = element.getAttribute("version");
            String ref = element.getAttribute("ref");
            beanDefinition.getPropertyValues().addPropertyValue("serviceName", interfaceName);
            beanDefinition.getPropertyValues().addPropertyValue("version", version);

            //实例化服务
            parserContext.getRegistry().registerBeanDefinition(interfaceName, beanDefinition);

            if (parserContext.getRegistry().containsBeanDefinition(ref)) {
                BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(ref);
                if (! refBean.isSingleton()) {
                    throw new IllegalStateException("The exported service ref " + ref + " must be singleton! Please set the " + ref + " bean scope to singleton, eg: <bean id=\"" + ref+ "\" scope=\"singleton\" ...>");
                }
            }
            Object reference = new RuntimeBeanReference(ref);
            beanDefinition.getPropertyValues().addPropertyValue("ref", reference);

        } else if (ReferBean.class.equals(beanClass)) {

            String id = element.getAttribute("id");
            String interfaceName = element.getAttribute("interface");
            String version = element.getAttribute("version");
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            beanDefinition.getPropertyValues().addPropertyValue("serviceName", interfaceName);
            beanDefinition.getPropertyValues().addPropertyValue("version", version);

            //注册  直接把代理对象注册到容器中 autowired就可以直接拿到了
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        }

        return beanDefinition;
    }


}