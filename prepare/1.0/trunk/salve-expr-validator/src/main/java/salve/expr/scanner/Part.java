package salve.expr.scanner;


public enum Part {
    /** <code>this</code> */
    THIS,
    /** the root type of the expression, eg <code>Person.class</code> */
    TYPE,
    /** expression path, eg <code>"address.street"</code> */
    PATH,
    /** expression mode: <code>"r"</code>, or <code>"w"</code>, or <code>"rw"</code> */
    MODE;
}
