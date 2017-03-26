package utils;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

/**
 * Converter class used by JCommander to process the -o --outputFormat arguments for allowed values
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */
public class OutputFormatConvertor implements IStringConverter<OutputFormatEnum> {

    /**
     * This method tries to converts the -o or --outputFormat parameter to OutputFormatEnum
     *
     * @param   outputFormat   Output format argument to validate
     * @return                 Returns the validated value in OutputFormatEnum
     */
    @Override
    public OutputFormatEnum convert(String outputFormat) {
        OutputFormatEnum convertedValue = OutputFormatEnum.fromString(outputFormat);
        if(convertedValue == null) {
            throw new ParameterException("Value " + outputFormat + " not allowed for -o or --outputFormat. " +
                        " Available values are CSV, XML or KAFKA");
        }
        return convertedValue;
    }
}
