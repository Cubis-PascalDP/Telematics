package utils;

/**
 * This Enum defines the allowed output format for API result parsing
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */

public enum OutputFormatEnum {
    /**
     * Output format in XML
     */
    XML,
    /**
     * Output format in CSV
     */
    CSV,
    /**
     * Output format in CSV and sent to KAFKA broker
     */
    KAFKA;

    /**
     * Converts string input to the corresponding enum value.
     *
     * @param code  String with the required output format.
     * @return      null or OutputFormatEnum
     */
    public static OutputFormatEnum fromString(String code) {

        for(OutputFormatEnum output : OutputFormatEnum.values()) {
            if(output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }

        return null;
    }
}
