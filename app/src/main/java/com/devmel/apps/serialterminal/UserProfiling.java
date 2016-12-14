package test;

import java.text.DecimalFormat;

/**
 *
 * @author Kristijonas
 */
public class UserProfiling {

    public static void main(String[] args) {
 
        // USAGE:
        double firstPartC = firstCoeff(200, 250, 100, 10.522);
        int[] speedArray = new int[]{14, 50, 60, 17, 80};
        int secondPartC = secondCoeff(speedArray, 50);
        String[] pedalArr = new String[]{"Pressed", "Pressed", "ffff", "ffff", "Pressed", "Pressed", "Pressed", "Pressed", "Pressed"};
        int thirdPartC = thirdCoeff(pedalArr);
        int[] speedArray2 = new int[]{0, 5, 6, 0, 8, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0, 0, 0, 6, 9, 0, 0, 0, 0, 10};
        int fourthPartC = fourthCoeff(speedArray2, 0);
        double overallScore = overallScoreofFive(firstPartC, 5.7, secondPartC, 10, thirdPartC, 13, fourthPartC, 5);
        System.out.println(overallScore);
    }
    
    
    
    
// 1: km/pct-charge-used, i.e. range over used power.

    public static double firstCoeff(int startingOdo, int endOdo, double startChargePct, double endChargePct) {
        //check if no zeros passed as args
        if (startingOdo == 0 || endOdo == 0 || startChargePct == 0 || endChargePct == 0) {
            return 0;
        } else {
            double firstcoff = (startChargePct - endChargePct) / (endOdo - startingOdo);
            return rounding(firstcoff); //returns pct/km
        }
    }
    
    
    
    
// 2: Time in speeds outside of the speed limits e.g. 50 km/h

    public static int secondCoeff(int[] speedArray, int limit) {
        if (speedArray.length == 0) {
            return 0;
        } else {
            int timesOfftheLimit = 0;
            for (int i = 0; i < speedArray.length; i++) {
                if (speedArray[i] > limit) {
                    timesOfftheLimit++;
                }
            }
            return timesOfftheLimit; //returns times speed was over the specified limit
        }
    }
    

    // 3: Time that the brakes was used.
    public static int thirdCoeff(String[] pedalArray) {
        if (pedalArray.length == 0) {
            return 0;
        } else {
            int timesPressed = 0;
            for (String pedalArray1 : pedalArray) {
                if (pedalArray1.equals("Pressed")) {
                    timesPressed++;
                }
            }
            return timesPressed;
        }
    }
    

    // 4: Number of times the vehicle was at a full stop (following the logic 
    // that the inertial of the cars is highest => more power consumption)
    public static int fourthCoeff(int[] speedArray, int limit) {
        if (speedArray.length == 0) {
            return 0;
        } else {
            int timesFullStop = 0;
            int previousSpeedinArray = 0;
            for (int i = 0; i < speedArray.length; i++) {
                if (speedArray[i] > limit && previousSpeedinArray == 0) {
                    timesFullStop++;
                }
                previousSpeedinArray = speedArray[i];
            }
            return timesFullStop; //returns times at full stop
        }
    }

    public static double overallScoreofFive(double firstCoeff, double limitFirst, int secondCoeff, int secLimit,
            int thirdCoeff, int thirdLimit, int fourthCoeff, int fourthLimit) {
        double overallScoreofFive = 0;
        if (firstCoeff == 0 || thirdCoeff == 0 || fourthCoeff == 0
                || limitFirst == 0 || secLimit == 0 || thirdLimit == 0 || fourthLimit == 0) {
            return 0;
        } else {
            overallScoreofFive += (firstCoeff * 100 / limitFirst) * 3 / 100;
            overallScoreofFive += 0.5 - (secondCoeff * 100 / secLimit) * 0.5 / 100;
            overallScoreofFive += 0.75 - (thirdCoeff * 100 / thirdLimit) * 0.75 / 100;
            overallScoreofFive += 0.75 - (fourthCoeff * 100 / fourthLimit) * 0.75 / 100;
        }

        if (overallScoreofFive > 5) {
            overallScoreofFive = 5;
        } else if (overallScoreofFive < 0) {
            overallScoreofFive = 0;
        }

        return rounding1(overallScoreofFive);
    }

    private static double rounding(double val) {
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(val));
    }

    private static double rounding1(double val) {
        DecimalFormat df2 = new DecimalFormat("###.#");
        return Double.valueOf(df2.format(val));
    }

}
