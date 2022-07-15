package com.latlongbearing;

public class BearingUtil {

    public static int reloadonce=0;

    double new_latitude=0.0D, new_longitude=0.0D,cos=0.0D,your_metres=399000;
    //static double old_latitude=22.815683, old_longitude=86.0920392;

    // Kansas City: 39.099912, -94.581213
    // St Louis: 38.627089, -90.200203

    //static double old_latitude=39.099912, old_longitude=-94.581213;
    //double old_latitude=0.0D, old_longitude=0.0D;


    public  BearingUtil(double old_latitude, double old_longitude) {


        System.out.println("latitude longitude of an point using a distance parameter\n\n");

        double earth = 6378.137;  //radius of the earth in kilometer
        double pi = Math.PI;
        double m = (1 / ((2 * pi / 360) * earth)) / 1000;  //1 meter in degree

        System.out.println("old_latitude : "+ old_latitude);
        System.out.println("old_longitude : "+ old_longitude);

        new_latitude= old_latitude + (your_metres * m);

        System.out.println("\n\n new_latitude : "+ Round_off(new_latitude,8.0));


        // double cos = Math.cos();
        double m2 = (1 / ((2 * pi / 360) * earth)) / 1000;  //1 meter in degree


        new_longitude =  old_longitude + (your_metres * m2) / Math.cos(old_latitude * (pi / 180));

        System.out.println("new_longitude : "+ Round_off(new_longitude,8.0));

        float angle = CalculateBearingAngle(old_latitude, old_longitude, new_latitude, new_longitude);

        System.out.println("\n\nBearing angle between the coordinates : " + Round_off(angle,4));

    }

    public static double Round_off(double N, double n)
    {
        int h;
        double l, a, b, c, d, e, i, j, m, f, g;
        b = N;
        c = Math.floor(N);

        // Counting the no. of digits to the left of decimal point
        // in the given no.
        for (i = 0; b >= 1; ++i)
            b = b / 10;

        d = n - i;
        b = N;
        b = b * Math.pow(10, d);
        e = b + 0.5;
        if ((float)e == (float)Math.ceil(b)) {
            f = (Math.ceil(b));
            h = (int)(f - 2);
            if (h % 2 != 0) {
                e = e - 1;
            }
        }
        j = Math.floor(e);
        m = Math.pow(10, d);
        j = j / m;
        return j;
    }

    public static float CalculateBearingAngle(double startLatitude,double startLongitude, double endLatitude, double endLongitude){
        double Phi1 = Math.toRadians(startLatitude);
        double Phi2 = Math.toRadians(endLatitude);
        double DeltaLambda = Math.toRadians(endLongitude - startLongitude);

        double Theta = Math.atan2((Math.sin(DeltaLambda)*Math.cos(Phi2)) , (Math.cos(Phi1)*Math.sin(Phi2) - Math.sin(Phi1)*Math.cos(Phi2)*Math.cos(DeltaLambda)));
        return (float)Math.toDegrees(Theta);
    }
}