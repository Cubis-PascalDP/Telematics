package telematics;

import telematics.rest.Token;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class GetTelemeticsData {
    public static void main(String[] args) {

        if (args.length <= 1) {
            System.out.println("No valid arguments given!");
        } else {
            Class<?> dynaClass;
            try {
                // Dynamically assign class using the first argument
                // Argument should represent a one of the rest
                dynaClass = Class.forName("telematics.rest." + args[0]);
                // Dynamically determine parsing method with second argument
                // Allowed values are XML or CSV
                Method parse = dynaClass.getMethod("parseTo" + args[1]);
                Method setBody = dynaClass.getMethod("setBody");
                Method getResponse = dynaClass.getMethod("getResponse");
                Method parseArguments = dynaClass.getMethod("parseArguments", String[].class);
                Method isContinuous = dynaClass.getMethod("isContinuous");

                Object dynaInst = dynaClass.newInstance();
                parseArguments.invoke(dynaInst, new Object[]{args});
                do {
                    setBody.invoke(dynaInst);
                    getResponse.invoke(dynaInst);
                    parse.invoke(dynaInst);
                } while((Boolean) isContinuous.invoke(dynaInst));

            } catch (ClassNotFoundException e) {
                System.err.println("First argument is not a valid Parsing Class!");
            } catch (NoSuchMethodException e) {
                System.err.println("Reconsider the given arguments. XML|CSV ");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
