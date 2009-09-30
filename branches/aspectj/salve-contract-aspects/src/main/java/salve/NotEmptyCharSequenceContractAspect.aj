/**
 * 
 */
package salve;

import org.aspectj.lang.JoinPoint;

/**
 * @author igor.vaynberg
 * 
 */
public aspect NotEmptyCharSequenceContractAspect
{
    before(String o): execution(* *(@salve.contract.NotEmpty (CharSequence+),..)) && args(o,..) {
        checkNotEmpty(o, 1, thisJoinPoint);
    }

    before(String o): execution(* *(*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,o,..) {
        checkNotEmpty(o, 2, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,o,..) {
        checkNotEmpty(o, 3, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,o,..) {
        checkNotEmpty(o, 4, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,o,..) {
        checkNotEmpty(o, 5, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,o,..) {
        checkNotEmpty(o, 6, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 7, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 8, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 9, thisJoinPoint);
    }

    before(String o): execution(* *(*,*,*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 10, thisJoinPoint);
    }


    before(String o): execution(*.new(@salve.contract.NotEmpty (CharSequence+),..)) && args(o,..) {
        checkNotEmpty(o, 1, thisJoinPoint);
    }

    before(String o): execution(*.new(*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,o,..) {
        checkNotEmpty(o, 2, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,o,..) {
        checkNotEmpty(o, 3, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,o,..) {
        checkNotEmpty(o, 4, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,o,..) {
        checkNotEmpty(o, 5, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,o,..) {
        checkNotEmpty(o, 6, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 7, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 8, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 9, thisJoinPoint);
    }

    before(String o): execution(*.new(*,*,*,*,*,*,*,*,*,@salve.contract.NotEmpty (CharSequence+),..)) && args(*,*,*,*,*,*,*,*,*,o,..) {
        checkNotEmpty(o, 10, thisJoinPoint);
    }


    
    
    private static void checkNotEmpty(String value, int argIndex, JoinPoint point)
    {
        if (value == null||value.trim().length()==0)
        {
            throw new IllegalArgumentException(String.format("Argument[%d] is empty at %s",
                    argIndex, point.getSignature().toString()));
        }

    }

}
