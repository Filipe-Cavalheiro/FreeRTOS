// labwork1.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include<conio.h>
#include<stdlib.h>
#include<stdio.h>
#include <my_interaction_functions.h>
#include <tasks.h>
#include <windows.h> //for Sleep function

extern "C" {
	#include <FreeRTOS.h>
	#include <task.h>
	#include <timers.h>
	#include <semphr.h>
	#include <interface.h>	
	#include <interrupts.h>
}

#define mainREGION_1_SIZE   8201
#define mainREGION_2_SIZE   29905
#define mainREGION_3_SIZE   7607

void vAssertCalled(unsigned long ulLine, const char* const pcFileName)
{
	static BaseType_t xPrinted = pdFALSE;
	volatile uint32_t ulSetToNonZeroInDebuggerToContinue = 0;
	/* Called if an assertion passed to configASSERT() fails.  See
	http://www.freertos.org/a00110.html#configASSERT for more information. */
	/* Parameters are not used. */
	(void)ulLine;
	(void)pcFileName;
	printf("ASSERT! Line %ld, file %s, GetLastError() %ld\r\n", ulLine, pcFileName, GetLastError());

	taskENTER_CRITICAL();
	{
		/* Cause debugger break point if being debugged. */
		__debugbreak();
		/* You can step out of this function to debug the assertion by using
		   the debugger to set ulSetToNonZeroInDebuggerToContinue to a non-zero
		   value. */
		while (ulSetToNonZeroInDebuggerToContinue == 0)
		{
			__asm { NOP };
			__asm { NOP };
		}
	}
	taskEXIT_CRITICAL();
}

static void  initialiseHeap(void)
{
	static uint8_t ucHeap[configTOTAL_HEAP_SIZE];
	/* Just to prevent 'condition is always true' warnings in configASSERT(). */
	volatile uint32_t ulAdditionalOffset = 19;
	const HeapRegion_t xHeapRegions[] =
	{
		/* Start address with dummy offsetsSize */
		{ ucHeap + 1,mainREGION_1_SIZE },
		{ ucHeap + 15 + mainREGION_1_SIZE,mainREGION_2_SIZE },
		{ ucHeap + 19 + mainREGION_1_SIZE +
				mainREGION_2_SIZE,mainREGION_3_SIZE },
		{ NULL, 0 }
	};


	configASSERT((ulAdditionalOffset +
		mainREGION_1_SIZE +
		mainREGION_2_SIZE +
		mainREGION_3_SIZE) < configTOTAL_HEAP_SIZE);
	/* Prevent compiler warnings when configASSERT() is not defined. */
	(void)ulAdditionalOffset;
	vPortDefineHeapRegions(xHeapRegions);
}

void initPorts() {
	printf("\nWaiting for hardware simulator...\n");
	printf("Remaining: gotoXZ requires calibration first...\n");

	createDigitalInput(0);
	createDigitalInput(1);
	createDigitalOutput(2);

	printf("Got access to simulator...\n");
}

int main(int argc, char** argv) {
	
	initPorts();

	initialiseHeap();
	initSemphr();
	initQueue();

	vApplicationDaemonTaskStartupHook = &myDaemonTaskStartUpHook;

	vTaskStartScheduler();

	return 0;
}
