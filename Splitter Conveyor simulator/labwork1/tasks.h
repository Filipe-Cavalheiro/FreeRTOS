#pragma once
#include <conio.h>
#include <my_interaction_functions.h>;
#include <list>;
#include <vector>;
#include <ctime>;

extern "C" {
	#include <freeRTOS.h>;
	#include <interface.h>;
	#include <semphr.h>;
	#include <interrupts.h>;
}

void initSemphr();
void initQueue();

void myDaemonTaskStartUpHook();