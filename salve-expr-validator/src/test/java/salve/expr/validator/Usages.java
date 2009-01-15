package salve.expr.validator;

import salve.expr.PE;
import salve.expr.validator.model.City;
import salve.expr.validator.model.PeContainer;
import salve.expr.validator.model.Person;

public class Usages
{
    public void use()
    {
        new PE(Person.class, "address.city.id", "r");
//        new PE(Person.class, "address.city.idInteger", "r");
//        new PE(Person.class, "address.city.name", "r");
//        new PE(City.class, "name", "r");
//        new PE(Person.class, "address2.city.name", "r");
//        new PE(Person.class, "address.city2.name", "r");
//        new PE(Person.class, "address.city.name2", "r");
//        new PE(Person.class, "address.city.name.foo", "r");
//        new PeContainer(null, new PE(Person.class, "address2.city.name", "r"));
    }

}
