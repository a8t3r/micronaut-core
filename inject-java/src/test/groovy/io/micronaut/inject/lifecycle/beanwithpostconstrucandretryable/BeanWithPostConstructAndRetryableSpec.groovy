/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.inject.lifecycle.beanwithpostconstrucandretryable

import io.micronaut.context.BeanContext
import io.micronaut.context.DefaultBeanContext
import io.micronaut.inject.AbstractTypeElementSpec
import io.micronaut.inject.BeanDefinition

class BeanWithPostConstructAndRetryableSpec extends AbstractTypeElementSpec {

    void "test @Retryable and @PostConstruct injection compile"() {
        given:"A bean that has life cycle and interceptor annotations"
        when:

        BeanDefinition beanDefinition = buildBeanDefinition('test.MyFooBean', '''
package test;
import javax.annotation.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import io.micronaut.retry.annotation.Retryable;

import io.micronaut.context.annotation.*;

@Executable
class MyFooBean {
    
    Foo foo;
    
    public Foo getFoo() { return this.foo; }
    
    @PostConstruct
    public void init(Foo foo) {
        this.foo = foo;
    }
    
    @PreDestroy
    public void setFoo(Foo foo) {
        this.foo = null;
    }
    
    @Retryable   
    public void retry() {}
}


@javax.inject.Singleton
class Foo {}

''')
        then:"the state is correct"
        beanDefinition.injectedMethods.size() == 2
        beanDefinition.preDestroyMethods.size() == 1
        beanDefinition.postConstructMethods.size() == 1
    }

    void "test that a bean with a post construct hook and retryable interceptor that the hook is invoked"() {
        given:
        BeanContext context = new DefaultBeanContext()
        context.start()

        when:
        B b = context.getBean(B)

        then:
        b.a != null
        b.injectedFirst // FAIL!
        b.setupComplete
    }
}
