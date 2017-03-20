package utils;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Validates an integer to be bigger than 0
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */
public class IntegerBiggerThan0Validator implements IParameterValidator {
    /**
     * This method performs the integer validations
     *
     * @param   name   the argument that is being validated
     * @param   value  the value that needs to be validated
     */
    public void validate(String name, String value) {
        try {
            Integer s = Integer.parseInt(value.replace(",",""));
            if (s <= 0) {
                throw new ParameterException(name + ": Should have a value bigger than 0 !");
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(name + ": Should be a numeric!");
        }

    }
}
