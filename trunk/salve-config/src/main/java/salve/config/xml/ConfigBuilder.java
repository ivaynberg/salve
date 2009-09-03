package salve.config.xml;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class ConfigBuilder extends DefaultHandler
{
    private Object result;
    private Stack<Handler> handlers;
    private final ClassLoader classloader;
    private final Map<String, String> aliases;

    public ConfigBuilder(ClassLoader classloader, Map<String, String> aliases)
    {
        this.classloader = classloader;
        this.aliases = aliases;
    }


    private void setResult(Object result)
    {
        this.result = result;
    }


    @Override
    public void startDocument() throws SAXException
    {
        handlers = new Stack<Handler>();
        handlers.push(new ValueHandler(this, "result"));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        Handler top = handlers.peek();
        Handler next = top.start(qName);
        handlers.push(next);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        Handler top = handlers.pop();
        top.end();
    }

    @Override
    public void endDocument() throws SAXException
    {
        Handler top = handlers.pop();
        top.end();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String value = new String(ch, start, length);
        value = value.trim();// replaceAll("\\s", "");
        if (value.length() > 0)
        {
            Handler top = handlers.peek();
            top.body(value);
        }
    }

    public Object getObject()
    {
        return result;
    }

    private abstract static class Handler
    {
        public abstract Handler start(String tag);

        public void end()
        {

        }

        public void body(String value)
        {

        }
    }


    private class PropertyHandler extends Handler
    {
        private final Object dest;

        public PropertyHandler(Object dest)
        {
            this.dest = dest;
        }


        @Override
        public Handler start(String tag)
        {
            return new ValueHandler(dest, tag);
        }

        @Override
        public void end()
        {
        }
    }


    private class ValueHandler extends Handler
    {
        private final Object dest;
        private final String property;

        public ValueHandler(Object dest, String property)
        {
            this.dest = dest;
            this.property = property;
        }

        @Override
        public Handler start(String tag)
        {
            try
            {
                String cn = aliases.get(tag);
                if (cn == null)
                {
                    cn = tag;
                }

                Class< ? > clazz = classloader.loadClass(cn);

                Object value = clazz.newInstance();
                set(value);
                return new PropertyHandler(value);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void body(String value)
        {
            set(value);
        }

        @Override
        public void end()
        {
        }

        private void set(Object value)
        {
            String setterName = "set" + property.substring(0, 1).toUpperCase() +
                    property.substring(1);
            String getterName = "get" + property.substring(0, 1).toUpperCase() +
                    property.substring(1);
            Method[] methods = dest.getClass().getDeclaredMethods();

            // find normal setter
            for (Method method : methods)
            {
                if (method.getName().equals(setterName))
                {
                    if (method.getParameterTypes().length == 1)
                    {
                        if (method.getParameterTypes()[0].isAssignableFrom(value.getClass()))
                        {
                            ConfigBuilder.set(dest, method, value);
                            return;
                        }
                    }
                }
            }
            // try to find a collection getter
            for (Method method : methods)
            {
                if (method.getName().equals(getterName))
                {
                    if (method.getParameterTypes().length == 0)
                    {
                        if (Collection.class.isAssignableFrom(method.getReturnType()))
                        {
                            Collection<Object> coll;
                            try
                            {
                                coll = (Collection<Object>)method.invoke(dest);
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                            coll.add(value);
                            return;

                        }
                    }
                }
            }
            throw new RuntimeException("Could not find accessor for property: " + property +
                    " in object of class: " + dest.getClass().getName());

        }
    }


    /**
     * @param method
     * @param value
     */
    private static void set(Object object, Method method, Object value)
    {
        try
        {
            method.invoke(object, new Object[] { value });
            // values.clear();
            return;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
// Class< ? > type = method.getReturnType();
// Collection<Object> collection = null;
// if (Set.class.isAssignableFrom(type))
// {
// collection = new HashSet<Object>();
// }
// else
// {
// collection = new ArrayList<Object>();
// }
// collection.addAll(values);
// set(object, method, collection);
