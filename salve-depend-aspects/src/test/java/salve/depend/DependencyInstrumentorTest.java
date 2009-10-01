/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.depend;

import java.lang.reflect.Constructor;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import salve.depend.cache.NoopCacheProvider;
import salve.util.EasyMockTemplate;

public class DependencyInstrumentorTest extends Assert
{

    private static final String REMOVED_FIELD_PREFIX = "";

    private static Class< ? > beanClass;

    private static BlueDependency blue;
    private static RedDependency red;
    private static Locator locator;

    @BeforeClass
    public static void initClass() throws Exception
    {
        initDependencyLibrary();
        beanClass = Bean.class;
    }

    private static void initDependencyLibrary()
    {
        DependencyLibrary.clear();

        blue = EasyMock.createMock(BlueDependency.class);
        red = EasyMock.createMock(RedDependency.class);
        locator = EasyMock.createMock(Locator.class);
        DependencyLibrary.addLocator(locator);
        DependencyLibrary.setCacheProvider(new NoopCacheProvider());
    }

    @Before
    public void resetMocks()
    {
        EasyMock.reset(blue, red, locator);
    }


    @Test
    public void testAnonClassFieldRead() throws Exception
    {
        new EasyMockTemplate(locator, red)
        {

            @Override
            protected void setupExpectations()
            {
                EasyMock.expect(
                        locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red")))
                        .andReturn(red);
                red.method1();
            }

            @Override
            protected void testExpectations() throws Exception
            {

                Bean bean = (Bean)beanClass.newInstance();
                bean.methodAnonymous();
            }
        }.test();
    }

    @Test
    public void testFieldAccessInConstructor() throws Exception
    {

        new EasyMockTemplate(locator)
        {

            @Override
            protected void setupExpectations()
            {
                EasyMock.expect(
                        locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red")))
                        .andReturn(red);
                EasyMock.expect(locator.locate(new FieldKey(Bean.class, "blue"))).andReturn(blue);
            }

            @Override
            protected void testExpectations() throws Exception
            {
                Constructor< ? > c = beanClass.getConstructor(int.class);
                c.newInstance(5);
            }
        }.test();
    }

    @Test
    public void testFieldRead() throws Exception
    {

        new EasyMockTemplate(locator, red, blue)
        {

            @Override
            protected void setupExpectations()
            {
                /*
                 * when we call bean.method1() both red and blue will be looked up. when we call
                 * bean.method2() only red will be looked up. blue needs to be looked up only once
                 * because it is cached in the field. red is looked up twice because it is cached
                 * per method and we call two methods
                 */
                EasyMock.expect(
                        locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red")))
                        .andReturn(red).anyTimes();
                EasyMock.expect(locator.locate(new FieldKey(Bean.class, "blue"))).andReturn(blue)
                        .anyTimes();
                // inside bean.method1() and bean.method2() we call all four
                // methods
                blue.method1();
                blue.method1();
                blue.method2();
                blue.method2();
                red.method1();
                red.method1();
                EasyMock.expect(red.method2()).andReturn(null);
                EasyMock.expect(red.method2()).andReturn(null);

                // Bean.getStaticBlue().method1();
                EasyMock.expect(
                        locator
                                .locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX +
                                        "staticBlue"))).andReturn(blue);
                blue.method1();
            }

            @Override
            protected void testExpectations() throws Exception
            {
                Bean bean = (Bean)beanClass.newInstance();
                bean.method1();
                bean.method2();
                Bean.getStaticBlue().method1();
            }
        }.test();
    }

    @Test
    public void testFieldReadOnReturn() throws Exception
    {

        new EasyMockTemplate(locator, red, blue)
        {

            @Override
            protected void setupExpectations()
            {
                EasyMock.expect(
                        locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red")))
                        .andReturn(red);
                EasyMock.expect(locator.locate(new FieldKey(Bean.class, "blue"))).andReturn(blue);
            }

            @Override
            protected void testExpectations() throws Exception
            {
                Bean bean = (Bean)beanClass.newInstance();
                assertTrue(bean.getRed() == red);
                assertTrue(bean.getBlue() == blue);
            }
        }.test();
    }

    @Ignore
    @Test
    public void testInnerClassFieldRead() throws Exception
    {
        // aspectj cannot handle this because it does not instrument synthetic methods. may require
        // a migration tool for salve to detect these problems.


        new EasyMockTemplate(locator, red, blue)
        {

            @Override
            protected void setupExpectations()
            {
                EasyMock.expect(
                        locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red")))
                        .andReturn(red);
                EasyMock.expect(locator.locate(new FieldKey(Bean.class, "blue"))).andReturn(blue);

                blue.method1();
                red.method1();
            }

            @Override
            protected void testExpectations() throws Exception
            {
                Bean bean = (Bean)beanClass.newInstance();
                bean.methodInner();
            }
        }.test();
    }


    @Test
    public void testStaticFieldRead() throws Exception
    {

        new EasyMockTemplate(locator, red, blue)
        {

            @Override
            protected void setupExpectations()
            {
                EasyMock.expect(
                        locator
                                .locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX +
                                        "staticBlue"))).andReturn(blue);
                blue.method1();
            }

            @Override
            protected void testExpectations() throws Exception
            {
                Bean bean = (Bean)beanClass.newInstance();
                Bean.getStaticBlue().method1();
            }
        }.test();
    }


}
