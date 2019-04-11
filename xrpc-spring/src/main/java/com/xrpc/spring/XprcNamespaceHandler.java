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
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class XprcNamespaceHandler extends NamespaceHandlerSupport {


    @Override
    public void init() {
        registerBeanDefinitionParser("node", new XprcBeanDefinitionParser(NodeConfig.class, true));
        registerBeanDefinitionParser("register", new XprcBeanDefinitionParser(RegisterConfig.class, false));
        registerBeanDefinitionParser("boot", new XprcBeanDefinitionParser(BootManager.class, false));
        registerBeanDefinitionParser("reference", new XprcBeanDefinitionParser(ReferBean.class, false));
        registerBeanDefinitionParser("service", new XprcBeanDefinitionParser(ServiceBean.class, false));
    }

}