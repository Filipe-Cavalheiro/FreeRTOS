#include "my_interaction_functions.h"

int getBitValue(uInt8 value, uInt8 bit_n)
// given a byte value, returns the value of its bit n
{
    return(value & (1 << bit_n));
}

void setBitValue(uInt8* variable, int n_bit, int new_value_bit)
// given a byte value, set the n bit to value
{
    uInt8 mask_on = (uInt8)(1 << n_bit);
    uInt8 mask_off = ~mask_on;
    if (new_value_bit) *variable |= mask_on;
    else *variable &= mask_off;
}

void writeCriticalDigitalU8(uInt32 port, uInt8 data) {
    taskENTER_CRITICAL();
    writeDigitalU8(port, data);
    taskEXIT_CRITICAL();
}

uInt8 readCriticalDigitalU8(uInt32 port) {
    uInt8 p;
    taskENTER_CRITICAL();
    p = readDigitalU8(port);
    taskEXIT_CRITICAL();
    return p;
}

// Cylinder start stuff
void moveCylinderStartFront() {
//    taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 0, 0); // set bit 0 to low level
    setBitValue(&p, 1, 1); // set bit 1 to high level
    writeCriticalDigitalU8(2, p); // update port 2
//    taskEXIT_CRITICAL();
}

void moveCylinderStartBack() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 1, 0); // set bit 1 to low level
    setBitValue(&p, 0, 1); // set bit 0 to high level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

void stopCylinderStart() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 0, 0); // set bit 0 to low level
    setBitValue(&p, 1, 0); // set bit 1 to low level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

int getCylinderStartPos() {
    uInt8 p0 = readCriticalDigitalU8(0);
    if (getBitValue(p0, 6))
        return 0;
    if (getBitValue(p0, 5))
        return 1;
    return(-1);
}

void gotoCylinderStart(int pos) {
    if (pos == getCylinderStartPos()) { return; }

    pos ? moveCylinderStartFront() : moveCylinderStartBack();

    while (getCylinderStartPos() != pos);

    stopCylinderStart();
}

// Cylinder 1 stuff
void moveCylinder1Front() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 3, 0); // set bit 0 to low level
    setBitValue(&p, 4, 1); // set bit 1 to high level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

void moveCylinder1Back() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 4, 0); // set bit 1 to low level
    setBitValue(&p, 3, 1); // set bit 0 to high level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

void stopCylinder1() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 3, 0); // set bit 0 to low level
    setBitValue(&p, 4, 0); // set bit 1 to low level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

int getCylinder1Pos() {
    uInt8 p0 = readCriticalDigitalU8(0);
    if (!getBitValue(p0, 4))
        return 0;
    if (!getBitValue(p0, 3))
        return 1;
    return(-1);
}

int getCylinder1Sensor() {
    uInt8 p0 = readCriticalDigitalU8(0);
    return getBitValue(p0, 0);
}

void gotoCylinder1(int pos) {
    if (pos == getCylinder1Pos()) { return; }

    pos ? moveCylinder1Front() : moveCylinder1Back();

    while (getCylinder1Pos() != pos);

    stopCylinder1();
}

// Cylinder 2 stuff
void moveCylinder2Front() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 5, 0); // set bit 0 to low level
    setBitValue(&p, 6, 1); // set bit 1 to high level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

void moveCylinder2Back() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 6, 0); // set bit 1 to low level
    setBitValue(&p, 5, 1); // set bit 0 to high level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

void stopCylinder2() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2); // read port 2
    setBitValue(&p, 6, 0); // set bit 0 to low level
    setBitValue(&p, 5, 0); // set bit 1 to low level
    writeCriticalDigitalU8(2, p); // update port 2
    //taskEXIT_CRITICAL();
}

int getCylinder2Pos() {
    uInt8 p0 = readCriticalDigitalU8(0);
    if (!getBitValue(p0, 2))
        return 0;
    if (!getBitValue(p0, 1))
        return 1;
    return(-1);
}

int getCylinder2Sensor() {
    uInt8 p0 = readCriticalDigitalU8(1);
    return getBitValue(p0, 7);
}

void gotoCylinder2(int pos) {
    if (pos == getCylinder2Pos()) { return; }

    pos ? moveCylinder2Front() : moveCylinder2Back();

    while (getCylinder2Pos() != pos);

    stopCylinder2();
}

//Conveyor
void convOn() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2);
    setBitValue(&p, 2, 1);
    writeCriticalDigitalU8(2, p);
    //taskEXIT_CRITICAL();
}

void convOff() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2);
    setBitValue(&p, 2, 0);
    writeCriticalDigitalU8(2, p);
    //taskEXIT_CRITICAL();
}

//Magnetic sensors
int getMagSensor1() {
    uInt8 p1 = readCriticalDigitalU8(1);
    return getBitValue(p1, 6);
}

int getMagSensor2() {
    uInt8 p1 = readCriticalDigitalU8(1);
    return getBitValue(p1, 5);
}

void greenLedOn() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2);
    setBitValue(&p, 7, 1);
    writeCriticalDigitalU8(2, p);
    //taskEXIT_CRITICAL();
}

void greenLedOff() {
    //taskENTER_CRITICAL();
    uInt8 p = readCriticalDigitalU8(2);
    setBitValue(&p, 7, 0);
    writeCriticalDigitalU8(2, p);
    //taskEXIT_CRITICAL();
}