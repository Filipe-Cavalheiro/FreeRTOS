#pragma once

extern "C" {
#include <freeRTOS.h>;
#include <interface.h>;
#include <semphr.h>;
}

int getBitValue(uInt8 value, uInt8 bit_n);
void setBitValue(uInt8* variable, int n_bit, int new_value_bit);
void writeCriticalDigitalU8(uInt32 port, uInt8 data);
uInt8 readCriticalDigitalU8(uInt32 port);

// CylinderStart related functions
void moveCylinderStartFront();
void moveCylinderStartBack();
void stopCylinderStart();
int  getCylinderStartPos();
void gotoCylinderStart(int pos);

// Cylinder1 related functions
void moveCylinder1Front();
void moveCylinder1Back();
void stopCylinder1();
int  getCylinder1Pos();
int getCylinder1Sensor();
void gotoCylinder1(int pos);

// Cylinder2 related functions
void moveCylinder2Front();
void moveCylinder2Back();
void stopCylinder2();
int  getCylinder2Pos();
int getCylinder2Sensor();
void gotoCylinder2(int pos);

// Put here the other function headers!!!
// Conveyor functions
void convOn();
void convOff();

//Mag Sensors Functions
int getMagSensor1();
int getMagSensor2();


void greenLedOn();
void greenLedOff();