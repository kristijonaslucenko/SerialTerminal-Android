/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Arrays;

/**
 *
 * @author Kristijonas
 */
public class Test {

    private static String[] packet;
    private static String[] PIDs;
    private static String VIN;
    private static double SOC;
    private static double EVP;
    private static int velocity;
    private static int odo;
    private static int chargeVoltage;
    private static String onBoardCharger;
    private static String quickCharge;
    private static String shiftStatus;
    private static int heatingLevel;
    private static String heatingStatus;
    private static String acStatus;
    private static String MaxHeating;
    private static String airCirculation;
    private static int ventilationLevel;
    private static String brakeOnOff;
    private static int brakePedalValue;
    private static String positionLightsStatus;
    private static String highBeamStatus;
    private static String tailLightsStatus;
    private static String lowBeamStatus;
    private static int steeringWheelPosition;
    private static String throttleStatus;

    public static void main(String[] args) {

        PIDsArray();
        VIN = "Na";
        SOC = 0;
        EVP = 0;
        velocity = 0;
        odo = 0;
        chargeVoltage = 0;
        onBoardCharger = "Na";
        quickCharge = "Na";
        shiftStatus = "Na";
        heatingLevel = 0;
        heatingStatus = "Na";
        acStatus = "Na";
        MaxHeating = "Na";
        airCirculation = "Na";
        ventilationLevel = 0;
        brakeOnOff = "Na";
        brakePedalValue = 0;
        positionLightsStatus = "Na";
        highBeamStatus = "Na";
        tailLightsStatus = "Na";
        lowBeamStatus = "Na";
        steeringWheelPosition = 0;
        throttleStatus = "Na";

        textReceived("424C3000C0042D603FF");

        //System.out.println(positionLightsStatus);

    }

    public static String textReceived(String text) {
        String pid;
        packet = new String[8];
        int pidNo = 0;
        //System.out.println(text.length());
        //make sure it's not an ACK
        if (text.length() >= 19) {
            pid = text.substring(0, 3);
            packet[0] = text.substring(3, 5);   //D0
            packet[1] = text.substring(5, 7);   //D1
            packet[2] = text.substring(7, 9);   //D2
            packet[3] = text.substring(9, 11);  //D3
            packet[4] = text.substring(11, 13); //D4
            packet[5] = text.substring(13, 15); //D5
            packet[6] = text.substring(15, 17); //D6
            packet[7] = text.substring(17, 19); //D7

            for (int i = 0; i < PIDs.length; i++) {
                if (PIDs[i].equals(pid)) {
                    pidNo = i;
                    break;
                }
            }

            switch (pidNo) {
                case 0:
                    // "374"; //State of Charge (SOC)
                    calculateStateOfCharge(packet);
                    break; // optional

                case 1:
                    // "346"; //EV Power
                    calculateEVpower(packet);
                    break;

                case 2:
                    // "412"; //Velocity and Odometer
                    calculateVelocityOdometer(packet);
                    break;
                case 3:
                    // "389"; //Onboard charge Volt Onboard charge status
                    calculateOnboardChargeStatus(packet);
                    break;
                case 4:
                    // "696"; //Quick charge status
                    calculateQuickChargeStatus(packet);
                    break;
                case 5:
                    // "418"; //Shift status
                    calculateShiftStatus(packet);
                    break;
                case 6:
                    // "3A4"; //A/C Status
                    calculateACstatus(packet);
                    break;
                case 7:
                    // "231"; //Brake lamp and Brake pedal switch sensor
                    calculateBrakeLampPedalSwitchSensor(packet);
                    break;
                case 8:
                    // "208"; //Brake pedal
                    calculateBrakePedal(packet);
                    break;
                case 9:
                    // "424"; //Light status Headlight + L/R
                    calculateLightStatus(packet);
                    break;
                case 10:
                    // "236"; //Steering wheel position
                    calculateSteeringWheelPosition(packet);
                    break;
                case 11:
                    // "210"; //Throttle
                    calculateThrottle(packet);
                    break;
                case 12:
                    // "6FA"; //VIN (Vehicle ID)
                    calculateVIN(packet);
                    break;
            }

            System.out.println();
        }

        return null;
    }

    public static void PIDsArray() {
        PIDs = new String[13];
        PIDs[0] = "374"; //State of Charge (SOC)
        PIDs[1] = "346"; //EV Power
        PIDs[2] = "412"; //Velocity and Odometer
        PIDs[3] = "389"; //Onboard charge Volt Onboard charge status
        PIDs[4] = "696"; //Quick charge status
        PIDs[5] = "418"; //Shift status
        PIDs[6] = "3A4"; //A/C Status
        PIDs[7] = "231"; //Brake lamp and Brake pedal switch sensor
        PIDs[8] = "208"; //Brake pedal
        PIDs[9] = "424"; //Light status Headlight + L/R
        PIDs[10] = "236"; //Steering wheel position
        PIDs[11] = "210"; //Throttle
        PIDs[12] = "6FA"; //VIN (Vehicle ID)
    }

    private static void calculateStateOfCharge(String[] packet) {
        SOC = Integer.parseInt(packet[1].trim(), 16) * 0.5 - 5;
    }

    private static void calculateEVpower(String[] packet) {
        String temp1 = packet[0] + packet[1];
        EVP = Integer.parseInt(temp1.trim(), 16) * 10 - 100000;
    }

    private static void calculateVelocityOdometer(String[] packet) {
        velocity = Integer.parseInt(packet[1].trim(), 16);
        String temp2 = packet[2] + packet[3] + packet[4];
        odo = Integer.parseInt(temp2.trim(), 16);
    }

    private static void calculateOnboardChargeStatus(String[] packet) {
        char[] temp = hexToBin(packet[5]).toCharArray();
        if ((temp[7] == '0') && (temp[6] == '1')) {
            chargeVoltage = 230;
        } else {
            chargeVoltage = 120;
        }
        if (temp[4] == '1') {
            onBoardCharger = "On";
        } else {
            onBoardCharger = "Off";
        }
    }

    private static void calculateQuickChargeStatus(String[] packet) {
        char[] temp = hexToBin(packet[5]).toCharArray();
        if (temp[3] == '1') {
            quickCharge = "On";
        } else {
            quickCharge = "Off";
        }
    }

    private static void calculateShiftStatus(String[] packet) {
        if (packet[0].equals("50")) {
            shiftStatus = "P";
        } else if (packet[0].equals("52")) {
            shiftStatus = "R";
        }
        if (packet[0].equals("4E")) {
            shiftStatus = "N";
        }
        if (packet[0].equals("44")) {
            shiftStatus = "D";
        }
        if (packet[0].equals("83")) {
            shiftStatus = "E";
        }
        if (packet[0].equals("32")) {
            shiftStatus = "B";
        }
        if (packet[0].equals("FF")) {
            shiftStatus = "NA";
        }
    }

    private static void calculateACstatus(String[] packet) {
        char[] temp0 = hexToBin(packet[0]).toCharArray();
        char[] temp01;
        temp01 = new char[4];
        System.arraycopy(temp0, 3, temp01, 0, 4);
        String temp01String = String.copyValueOf(temp01);
        heatingLevel = Integer.parseInt(temp01String, 2);
        if (heatingLevel == 7) {
            heatingStatus = "Off";
        } else if (heatingLevel < 7) {
            heatingStatus = "Cooling by " + heatingLevel;
        } else if (heatingLevel > 7) {
            heatingStatus = "Heating by " + heatingLevel;
        }

        if (temp0[0] == '1') {
            acStatus = "On";
        } else {
            acStatus = "Off";
        }

        if (temp0[2] == '1') {
            MaxHeating = "On";
        } else {
            MaxHeating = "Off";
        }

        if (temp0[3] == '1') {
            airCirculation = "On";
        } else {
            airCirculation = "Off";
        }

        char[] temp1 = hexToBin(packet[1]).toCharArray();
        char[] temp11;
        temp11 = new char[4];
        System.arraycopy(temp1, 3, temp11, 0, 4);
        String temp11String = String.copyValueOf(temp11);
        ventilationLevel = Integer.parseInt(temp11String, 2);

        //System.out.println(ventilationLevel);
    }

    private static void calculateBrakeLampPedalSwitchSensor(String[] packet) {
        if (packet[4].equals("00")) {
            brakeOnOff = "Free";
        } else if (packet[4].equals("02")) {
            brakeOnOff = "Pressed";
        }
    }

    private static void calculateBrakePedal(String[] packet) {
        String temp2 = packet[2] + packet[3];
        brakePedalValue = Integer.parseInt(temp2);
    }

    private static void calculateLightStatus(String[] packet) {
        String temp3 = Arrays.toString(packet);
        temp3 = temp3.replaceAll("[, ]", "");
        temp3 = temp3.replace("[", "");
        temp3 = temp3.replace("]", "");
        //System.out.println(temp3);
        if (temp3.equals("C3000C0042D603FF")) {
            positionLightsStatus = "Off";
            highBeamStatus = "On";
        } else if (temp3.equals("C7400C0042D703FF")) {
            positionLightsStatus = "On";
            tailLightsStatus = "On";
        } else if (temp3.equals("C7600C0042D903FF")) {
            positionLightsStatus = "On";
            lowBeamStatus = "On";
            tailLightsStatus = "On";
        }
    }

    private static void calculateSteeringWheelPosition(String[] packet) {
        //(((D0 * 256) + D1) - 4096) / 2 = steering wheel position in degrees). Negative angle - right, positive angle left.
        steeringWheelPosition = (((Integer.parseInt(packet[0].trim(), 16) * 256) + Integer.parseInt(packet[1].trim(), 16)) - 4096) / 2;
    }

    private static void calculateVIN(String[] packet) {
        //System.out.println( VIN.length());

        String vinpart = Arrays.toString(packet);
        vinpart = vinpart.replaceAll("[, ]", "");
        vinpart = vinpart.replace("[", "");
        vinpart = vinpart.replace("]", "");

        //System.out.println(VIN);
        switch (VIN.length()) {
            case 0:
                VIN = vinpart;
                break;
            case 16:
                VIN += vinpart;
                break;
            case 32:
                VIN += vinpart;
                break;
            default:
                break;
        }

    }

    private static String hexToBin(String hex) {
        String bin = "";
        String binFragment = "";
        int iHex;
        hex = hex.trim();
        hex = hex.replaceFirst("0x", "");

        for (int i = 0; i < hex.length(); i++) {
            iHex = Integer.parseInt("" + hex.charAt(i), 16);
            binFragment = Integer.toBinaryString(iHex);

            while (binFragment.length() < 4) {
                binFragment = "0" + binFragment;
            }
            bin += binFragment;
        }
        return bin;
    }

    private static void calculateThrottle(String[] packet) {
        throttleStatus = "NA";
    }

}
