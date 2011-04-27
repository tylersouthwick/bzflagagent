package cs470.movement;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Taylor
 * Date: 4/27/11
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class HermiteJ {
    private SimpleMatrix Q;
    private double [] z;
    private int max;

    public static void main(String[] args) throws Exception {
          double [] x = {1.3,1.6,1.9};
          double [] fx = {.6200860,.4554022,.2818186};
          double [] fxp = {-.5220232,-.5698959,-.5811571};

          HermiteJ test = new HermiteJ(x,fx,fxp);

        System.out.println(test.Evaluate(1.5));
    }

    public double EvaluateP(double x0){
        double h = 0.001;
        double fn2h = Evaluate(x0 - 2*h);
        double fn1h = Evaluate(x0-h);
        double f1h = Evaluate(x0 + h);
        double f2h = Evaluate(x0+2*h);

        double fp = 1/(12*h)*(fn2h - 8*fn1h + 8*f1h - f2h);

        return fp;
    }
    public double Evaluate(double x){
        double total = 0;

        for(int i = 0;i < max;i++){
           double subtotal = Q.get(i,i);

           for(int j = 0;j < i;j++){
               subtotal *= x - z[j];
           }
           total += subtotal;
        }

        return total;
    }

    public HermiteJ(double [] x, double [] fx, double [] fxp) throws Exception{
        if(x.length != fx.length && fx.length != fxp.length){
            throw new Exception("Invalid lists");
        }

        int n = x.length;
        max = 2*n;
        Q = new SimpleMatrix(max,max);
        z = new double[max];

        for(int i = 0;i < n;i++){
            z[2*i] = x[i];
            z[2*i+1] = x[i];
            Q.set(2*i,0,fx[i]);
            Q.set(2*i+1,0,fx[i]);
            Q.set(2*i+1,1,fxp[i]);

            if(i != 0){
                Q.set(2*i,1,(Q.get(2*i,0)-Q.get(2*i-1,0))/(z[2*i]-z[2*i-1]));
            }
        }

        for(int i = 2;i < max;i++){
            for(int j = 2;j < i + 1;j++){
                Q.set(i,j,(Q.get(i,j-1)-Q.get(i-1,j-1))/(z[i]-z[i-j]));
            }
        }

        System.out.println(Q);
    }
}
