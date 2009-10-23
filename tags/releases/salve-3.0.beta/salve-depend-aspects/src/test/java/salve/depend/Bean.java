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


public class Bean extends AbstractBean
{

    private class InnerBean
    {
        public void method()
        {
            red.method1();
            blue.method1();
            staticBlue.method1();
        }
    }

    // force this class to have a clinit
    public static final long FORCE_CLINIT = System.currentTimeMillis();

    private static BlackDependency staticBlack;

    public static BlackDependency getStaticBlack()
    {
        return staticBlack;
    }

    public static void setStaticBlack(BlackDependency staticBlack)
    {
        Bean.staticBlack = staticBlack;
    }

//    public static void setStaticBlue(BlueDependency staticBlue)
//    {
//        Bean.staticBlue = staticBlue;
//    }

    @Dependency(strategy = InstrumentationStrategy.INJECT_FIELD)
    private BlueDependency blue;

    @Square
    @Dependency
    @Circle
    transient RedDependency red;

 
    @Circle
    private BlackDependency black;

    @Dependency
    private static BlueDependency staticBlue;


    public Bean()
    {

    }


    public Bean(int num)
    {
        super(num);
        @SuppressWarnings("unused")
        Object r = red;
        @SuppressWarnings("unused")
        Object b = blue;
    }

    public static BlueDependency getStaticBlue()
    {
        return staticBlue;
    }

    public BlackDependency getBlack()
    {
        return black;
    }

    public BlueDependency getBlue()
    {
        return blue;
    }

    public RedDependency getRed()
    {
        return red;
    }

    public void method1()
    {
        blue.method1();
        blue.method2();
        red.method1();
        red.method2();
    }

    public void method2()
    {
        blue.method1();
        blue.method2();
        Object tmp = null;
        if (tmp == null)
        {
            tmp = red.method2();
        }
        red.method1();

    }

    /**
     * Tests access to a dependency field from an anonymous class. Notice the field has to have
     * visibility of non-private for this test to work properly.
     */
    public void methodAnonymous()
    {
        Runnable runnable = new Runnable()
        {

            public void run()
            {
                red.method1();
            }
        };
        runnable.run();
    }

    public void methodInner()
    {
        InnerBean bean = new InnerBean();
        bean.method();

    }

    public void setBlack(BlackDependency black)
    {
        this.black = black;
    }

    public void setBlue(BlueDependency blue)
    {
        this.blue = blue;
    }

//    public void setRed(RedDependency red)
//    {
//        this.red = red;
//    }

}
