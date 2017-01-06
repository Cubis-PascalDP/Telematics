package telematics;

import telematics.rest.TripProcessForVehicle;

/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class GetTripsInfo {
    public static void main(String[] args) {
        TripProcessForVehicle tpForVehicle = new TripProcessForVehicle();

        tpForVehicle.setBody(215, "2016-12-20", "2016-12-21");
        tpForVehicle.getResponse();
        tpForVehicle.parseToCSV();

 //       System.out.println(tpForVehicle);
    }
}
