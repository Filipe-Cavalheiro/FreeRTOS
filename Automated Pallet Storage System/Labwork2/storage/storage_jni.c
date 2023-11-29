#include <jni.h>
#include <storage.h>

JNIEXPORT void JNICALL Java_Storage_initializeHardwarePorts(JNIEnv* jniEnv, jclass jClass){
	initializeHardwarePorts();
}

/*
* Class:     Storage 
* Method:    moveXRight
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_moveXRight(JNIEnv* jniEnv, jclass jClass){
	moveXRight();
}

/*
* Class:     Storage
* Method:    moveXLeft
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_moveXLeft(JNIEnv* jniEnv, jclass jClass) {
	moveXLeft();
}

/*
* Class:     Storage
* Method:    stopX
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_stopX(JNIEnv* jniEnv, jclass jClass) {
	stopX();
}

/*
* Class:     Storage
* Method:    getXPos
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getXPos(JNIEnv* jniEnv, jclass jClass) {
	getXPos();
}

	/*
	 * Class:     Storage
	 * Method:    moveZUp
	 * Signature: ()V
	 */
JNIEXPORT void JNICALL Java_Storage_moveZUp(JNIEnv* jniEnv, jclass jClass) {
	moveZUp();
}

/*
* Class:     Storage
* Method:    moveZDown
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_moveZDown(JNIEnv* jniEnv, jclass jClass) {
	moveZDown();
}

/*
* Class:     Storage
* Method:    stopZ
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_stopZ(JNIEnv* jniEnv, jclass jClass) {
	stopZ();
}

/*
* Class:     Storage
* Method:    getZPos
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getZPos(JNIEnv* jniEnv, jclass jClass){
	getZPos();
}

/*
* Class:     Storage
* Method:    moveYInside
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_moveYInside(JNIEnv* jniEnv, jclass jClass){
	moveYInside();
}

/*
* Class:     Storage
* Method:    moveYOutside
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_moveYOutside(JNIEnv* jniEnv, jclass jClass){
	moveYOutside();
}

/*
* Class:     Storage
* Method:    stopY
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_stopY(JNIEnv* jniEnv, jclass jClass) {
	stopY();
}

/*
* Class:     Storage
* Method:    getYPos
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getYPos(JNIEnv* jniEnv, jclass jClass){
	getYPos();
}

/*
* Class:     Storage
* Method:    getSwitch1
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getSwitch1(JNIEnv* jniEnv, jclass jClass){
	getSwitch1();
}

/*
* Class:     Storage
* Method:    getSwitch2
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getSwitch2(JNIEnv* jniEnv, jclass jClass){
	getSwitch2();
}

/*
* Class:     Storage
* Method:    getSwitch1_2
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_Storage_getSwitch1_12(JNIEnv* jniEnv, jclass jClass){
	getSwitch1_2();
}

/*
* Class:     Storage
* Method:    ledOn
* Signature: (I)V
*/
JNIEXPORT void JNICALL Java_Storage_ledOn(JNIEnv* jniEnv, jclass jClass, jint n){
	ledOn(n);
}

/*
* Class:     Storage
* Method:    ledsOff
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_Storage_ledsOff(JNIEnv* jniEnv, jclass jClass){
	ledsOff();
}