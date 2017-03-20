package utils;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Validates a string to contain a valid date and time in the format yyyy-mm-ttThh:mm
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */
public class DateTimeValidator implements IParameterValidator {
    /**
     * This method executes the date validation
     *
     * @param   name   the argument that is being validated
     * @param   value  the value that needs to be validated
     */
    public void validate(String name, String value) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            dateTimeFormat.setLenient(false);
            dateTimeFormat.parse(value);
        } catch (ParseException e) {
            throw new ParameterException(name + ": " + value + " could not be validated. " + "" +
                    " Check for a valid Date/Timestamp delivered in format yyyy-mm-dd'T'hh:mm:ss");
        }
    }
}
