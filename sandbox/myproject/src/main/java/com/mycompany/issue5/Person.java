package com.mycompany.issue5;

import salve.depend.Dependency;

public class Person {
    @Dependency
    public PersonService personService;

    public String getFoo() {
	return personService.getFoo();
    }
}
