package salve.expr.validator;

import salve.expr.validator.model.Person;

public class Usages
{
    public void use()
    {
        new PeModel(new String() + "foo", Person.class, "address.city.id", "r");
        new PeModel(this, "address.city.this", "r");
    }

    public class Foo
    {
        public void use()
        {
            new PeModel(Usages.this, "address.city.this", "r");
        }

        public class Bar
        {
            public void use()
            {
                new PeModel(Usages.this, "address.city.this", "r");
            }

            public class Baz
            {
                public void use()
                {
                    new PeModel(Usages.this, "address.city.this", "r");
                }

            }
        }
    }

}
