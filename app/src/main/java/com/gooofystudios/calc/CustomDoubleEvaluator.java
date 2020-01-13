package com.gooofystudios.calc;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Parameters;

import java.util.Iterator;

public class CustomDoubleEvaluator extends DoubleEvaluator {

    private static final Function SQRT = new Function("√", 1);
    private static final Function FACT = new Function("!", 1);
    private static final Parameters PARAMS;

    static {
        // Gets the default DoubleEvaluator's parameters
        PARAMS = DoubleEvaluator.getDefaultParameters();
        // add the new sqrt function to these parameters
        PARAMS.add(SQRT);

        // add the new factorial function
        PARAMS.add(FACT);

    }

    public CustomDoubleEvaluator() {
        super(PARAMS);
    }

    @Override
    protected Double evaluate(Function function, Iterator<Double> arguments, Object evaluationContext) {
        if (function == SQRT) {
            return Math.sqrt(arguments.next());
        }
        else if(function == FACT){
            return factorial(arguments.next());
        }
        else {
            // If it's another function, pass it to DoubleEvaluator
            return super.evaluate(function, arguments, evaluationContext);
        }
    }

    static double factorial(double n){
        if (n == 0)
            return 1;
        else
            return(n * factorial(n-1));
    }
}
