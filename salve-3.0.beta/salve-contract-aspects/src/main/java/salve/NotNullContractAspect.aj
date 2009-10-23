/**
 * 
 */
package salve;

import org.aspectj.lang.JoinPoint;

/**
 * @author igor.vaynberg
 * 
 */
public aspect NotNullContractAspect
{
    before(Object o): execution(* *(@salve.contract.NotNull (*),..)) && args(o,..) {
        checkNotNull(o, 1, thisJoinPoint);
    }

    before(Object o): execution(* *(*,@salve.contract.NotNull (*),..)) && args(*,o,..) {
        checkNotNull(o, 2, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,@salve.contract.NotNull (*),..)) && args(*,*,o,..) {
        checkNotNull(o, 3, thisJoinPoint);
    }


    before(Object o): execution(* *(*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,o,..) {
        checkNotNull(o, 4, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,o,..) {
        checkNotNull(o, 5, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,o,..) {
        checkNotNull(o, 6, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotNull(o, 7, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 8, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 9, thisJoinPoint);
    }

    before(Object o): execution(* *(*,*,*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 10, thisJoinPoint);
    }

    before(Object o): execution(*.new(@salve.contract.NotNull (*),..)) && args(o,..) {
        checkNotNull(o, 1, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,@salve.contract.NotNull (*),..)) && args(*,o,..) {
        checkNotNull(o, 2, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,@salve.contract.NotNull (*),..)) && args(*,*,o,..) {
        checkNotNull(o, 3, thisJoinPoint);
    }


    before(Object o): execution(*.new(*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,o,..) {
        checkNotNull(o, 4, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,o,..) {
        checkNotNull(o, 5, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,o,..) {
        checkNotNull(o, 6, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotNull(o, 7, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 8, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 9, thisJoinPoint);
    }

    before(Object o): execution(*.new(*,*,*,*,*,*,*,*,*,@salve.contract.NotNull (*),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotNull(o, 10, thisJoinPoint);
    }
    
    private static void checkNotNull(Object value, int argIndex, JoinPoint point)
    {
        if (value == null)
        {
            throw new IllegalArgumentException(String.format("Argument[%d] is null at %s",
                    argIndex, point.getSignature().toString()));
        }

    }

}
