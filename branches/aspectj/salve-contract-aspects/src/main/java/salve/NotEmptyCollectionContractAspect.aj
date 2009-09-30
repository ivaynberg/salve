/**
 * 
 */
package salve;

import java.util.Collection;

import org.aspectj.lang.JoinPoint;

/**
 * @author igor.vaynberg
 * 
 */
@SuppressWarnings("unchecked")
public aspect NotEmptyCollectionContractAspect
{
    before(Collection o): execution(* *(@salve.contract.NotEmpty  (Collection+),..)) && args(o,..) {
        checkNotEmpty(o, 1, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,o,..) {
        checkNotEmpty(o, 2, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,o,..) {
        checkNotEmpty(o, 3, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,o,..) {
        checkNotEmpty(o, 4, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,o,..) {
        checkNotEmpty(o, 5, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,o,..) {
        checkNotEmpty(o, 6, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 7, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 8, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 9, thisJoinPoint);
    }

    before(Collection o): execution(* *(*,*,*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 10, thisJoinPoint);
    }

    
    
    before(Collection o): execution(*.new(@salve.contract.NotEmpty  (Collection+),..)) && args(o,..) {
        checkNotEmpty(o, 1, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,o,..) {
        checkNotEmpty(o, 2, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,o,..) {
        checkNotEmpty(o, 3, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,o,..) {
        checkNotEmpty(o, 4, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,o,..) {
        checkNotEmpty(o, 5, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,o,..) {
        checkNotEmpty(o, 6, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 7, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 8, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 9, thisJoinPoint);
    }

    before(Collection o): execution(*.new(*,*,*,*,*,*,*,*,*,@salve.contract.NotEmpty  (Collection+),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 10, thisJoinPoint);
    }

    

    private static void checkNotEmpty(Collection<?> value, int argIndex, JoinPoint point)
    {
        if (value == null || value.isEmpty())
        {
            throw new IllegalArgumentException(String.format("Argument[%d] is empty at %s",
                    argIndex, point.getSignature().toString()));
        }

    }

}
