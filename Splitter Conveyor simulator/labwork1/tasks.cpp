#include "tasks.h";

typedef struct {
	int type;
	bool rejected;
	time_t date;
} Brick;

xSemaphoreHandle xSemaphore_magSensor;
xSemaphoreHandle xSemaphore_syncMagSorter;
xSemaphoreHandle xSemaphore_brickSorter;
xSemaphoreHandle xSemaphore_brickQueue;
xSemaphoreHandle xSemaphore_cylinderStart;
xSemaphoreHandle xSemaphore_cylinder1;
xSemaphoreHandle xSemaphore_cylinder2;
xSemaphoreHandle xSemaphore_convOff;
xSemaphoreHandle xSemaphore_brickWrong;
xSemaphoreHandle xSemaphore_syncCyl1Sorter;
xSemaphoreHandle xSemaphore_printBricks;
xSemaphoreHandle xSemaphore_printRejected;
xSemaphoreHandle xSemaphore_convOn;
xSemaphoreHandle xSemaphore_controller;
xSemaphoreHandle xSemaphore_calibration;
xSemaphoreHandle xSemaphore_getchStart;
xSemaphoreHandle xSemaphore_getchEnd;

xTaskHandle emergencyTask_handle;
xTaskHandle resumeTask_handle;
xTaskHandle initializerTask_handle;
xTaskHandle clearSystemTask_handle;

xQueueHandle xQueue_brickSorter;

std::list<int> brickQueue;
std::vector<Brick> bricks;
long long unsigned correct[3] = { 0, 0, 0 };
long long unsigned regected[3] = { 0, 0, 0 };
bool emergencyState = false;
bool cleared = false;
uInt8 prevState = 0;
char userInput = NULL;

void vTask_calibration(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_calibration, 10000) == pdFALSE) { continue; }

		printf("Starting manual calibration...\n");
		printf("Press enter when finished\n");

		int tecla = 0;

		while (tecla != 13) {
			xSemaphoreGive(xSemaphore_getchStart);
			while (1) {
				if (cleared) { break; }
				if (xSemaphoreTake(xSemaphore_getchEnd, 10000) == pdFALSE) { continue; }
				tecla = userInput;
				break;
			}
			switch (tecla) {
			case 'q':
				moveCylinderStartFront();
				vTaskDelay(100);
				stopCylinderStart();
				break;
			case 'w':
				moveCylinderStartBack();
				vTaskDelay(100);
				stopCylinderStart();
				break;
			case 'a':
				moveCylinder1Front();
				vTaskDelay(100);
				stopCylinder1();
				break;
			case 's':
				moveCylinder1Back();
				vTaskDelay(100);
				stopCylinder1();
				break;
			case 'z':
				moveCylinder2Front();
				vTaskDelay(100);
				stopCylinder2();
				break;
			case 'x':
				moveCylinder2Back();
				vTaskDelay(100);
				stopCylinder2();
				break;
			}
		}

		gotoCylinderStart(0);
		gotoCylinder1(0);
		gotoCylinder2(0);
		printf("Calibration finished...\n");
		xSemaphoreGive(xSemaphore_controller);
	}
}

void initSemphr() {
	xSemaphore_magSensor = xSemaphoreCreateCounting(1, 0);
	xSemaphore_syncMagSorter = xSemaphoreCreateCounting(1, 0);
	xSemaphore_brickSorter = xSemaphoreCreateCounting(10, 0);
	xSemaphore_brickQueue = xSemaphoreCreateCounting(1, 1);
	xSemaphore_cylinderStart = xSemaphoreCreateCounting(1, 0);
	xSemaphore_cylinder1 = xSemaphoreCreateCounting(1, 0);
	xSemaphore_syncCyl1Sorter = xSemaphoreCreateCounting(1, 0);
	xSemaphore_cylinder2 = xSemaphoreCreateCounting(10, 0);
	xSemaphore_convOff = xSemaphoreCreateCounting(1, 0);
	xSemaphore_convOn = xSemaphoreCreateCounting(1, 0);
	xSemaphore_printBricks = xSemaphoreCreateCounting(1, 0);
	xSemaphore_brickWrong = xSemaphoreCreateCounting(10, 0);
	xSemaphore_printRejected = xSemaphoreCreateCounting(1, 0);
	xSemaphore_calibration = xSemaphoreCreateCounting(1, 0);
	xSemaphore_controller = xSemaphoreCreateCounting(1, 0);
	xSemaphore_getchStart = xSemaphoreCreateCounting(1, 0);
	xSemaphore_getchEnd = xSemaphoreCreateCounting(1, 0);
}

void initQueue() {
	xQueue_brickSorter = xQueueCreate(2, sizeof(int));
}

void vTask_printBrincks(void* pvParameters) {
	char buffer[32];

	while (1) {
		if (xSemaphoreTake(xSemaphore_printBricks, 10000) == pdFALSE) { continue; }

		printf("\nHistory of the system\n\n");

		for (Brick &i : bricks) {
			ctime_s(buffer, sizeof buffer, &i.date);
			printf("birck type: %d\nbrick was %s at %s\n", i.type, (i.rejected) ? "regected" : "accepted", buffer);
		}
	}
}

void vTask_printRejected(void* pvParameters) {
	char buffer[32];

	while (1) {
		if (xSemaphoreTake(xSemaphore_printRejected, 10000) == pdFALSE) { continue; }

		printf("\nHistory of rejected\n\n");

		for (Brick& i : bricks) {
			if (!i.rejected) { continue; }
			ctime_s(buffer, sizeof buffer, &i.date);
			printf("birck type: %d\nbrick was %s at %s\n", i.type, (i.rejected) ? "regected" : "accepted", buffer);
		}
	}
}

void vTask_convOff(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_convOff, 10000) == pdFALSE) { continue; }

		convOff();
	}
}

void vTask_convOn(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_convOn, 10000) == pdFALSE) { continue; }

		convOn();
	}
}

void vTask_brickWrong(void* pvParameters) {
	const TickType_t xDelay = 3000;

	while (1) {
		if (xSemaphoreTake(xSemaphore_brickWrong, 10000) == pdFALSE) { continue; }
		greenLedOn();
		vTaskDelay(xDelay);
		greenLedOff();
	}
}

void vTask_moveCylinderStart(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_cylinderStart, 10000) == pdFALSE) { continue; }

		gotoCylinderStart(1);
		gotoCylinderStart(0);
	}
}

void vTask_moveCylinder1(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_cylinder1, 10000) == pdFALSE) { continue; }

		xSemaphoreGive(xSemaphore_convOff);
		gotoCylinder1(1);
		gotoCylinder1(0);
		xSemaphoreGive(xSemaphore_syncCyl1Sorter);
	}
}

void vTask_moveCylinder2(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_cylinder2, 10000) == pdFALSE) { continue; }

		while (!getCylinder2Sensor()) { if (cleared) { break; } }
		if (cleared) { break; }
		xSemaphoreGive(xSemaphore_convOff);

		gotoCylinder2(1);
		gotoCylinder2(0);
		xSemaphoreGive(xSemaphore_convOn);
	}
}

void vTask_controller(void* pvParameters) {
	int type = 0;
	int old_type = 0;

	while (1) {
		if (xSemaphoreTake(xSemaphore_controller, 10000) == pdFALSE) { continue;}

		while (1) {
			printf("Enter a brick type...\n");

			type = 0;
			while (type != 13) {
				old_type = type - 49;
				xSemaphoreGive(xSemaphore_getchStart);
				while (1) {
					if (cleared){break;};
					if (xSemaphoreTake(xSemaphore_getchEnd, 10000) == pdFALSE) { continue; }
					type = userInput;
					break;
				}
				printf("%c", type);
				if(cleared){break;}
			}

			if (cleared){break;}

			switch (old_type) {
			case 0:
			case 1:
			case 2:
				printf("Brick type: %d\n", old_type + 1);
				while (xSemaphoreTake(xSemaphore_brickQueue, 10000) == pdFALSE);
				brickQueue.push_back(old_type);
				xSemaphoreGive(xSemaphore_brickQueue);

				xSemaphoreGive(xSemaphore_brickSorter);
				break;

			case 59:
				xSemaphoreGive(xSemaphore_printBricks);
				break;

			case 53:
				printf("Enter type to see rejected...\n");
				type = 0;
				while (type != 13) {
					old_type = type - 49;
					xSemaphoreGive(xSemaphore_getchStart);
					while (1) {
						if (cleared) { break; }
						if (xSemaphoreTake(xSemaphore_getchEnd, 10000) == pdFALSE) { continue; }
						type = userInput;
						break;
					}
					printf("%c", type);
				}

				printf("Number of bricks of type %d rejected is %llu\n", old_type, regected[old_type]);
				printf("Number of bricks of type %d accepted is %llu\n", old_type, correct[old_type]);
				break;

			case 65:
				xSemaphoreGive(xSemaphore_printRejected);
				break;
			}
		}
	}
}

void vTask_brickSorter(void* pvParameter) {
	Brick temp;
	int old_type;
	int mag;

	while (1) {
		while (xSemaphoreTake(xSemaphore_brickSorter, 10000) == pdFALSE) { continue; }

		xSemaphoreGive(xSemaphore_convOn);
		xSemaphoreGive(xSemaphore_magSensor);
		xSemaphoreGive(xSemaphore_cylinderStart);

		int mag;
		while (xSemaphoreTake(xSemaphore_syncMagSorter, 10000) == pdFALSE);

		old_type = brickQueue.front();
		while (xSemaphoreTake(xSemaphore_brickQueue, 10000) == pdFALSE);
		brickQueue.pop_front();
		xSemaphoreGive(xSemaphore_brickQueue);

		if (cleared) {
			brickQueue.clear();
			cleared = false;
			xSemaphoreGive(xSemaphore_controller);
			continue;
		}
		
		xQueueReceive(xQueue_brickSorter, &mag, 0);
		printf("Type detected: %d\n", mag + 1);
		
		bool rej = mag != old_type;
		temp = Brick{ mag, rej, time(0) };

		bricks.push_back(temp);

		if(rej){
			++regected[mag];
			xSemaphoreGive(xSemaphore_brickWrong);

		}else{
			++correct[mag];

			switch (mag){
			case 0:
				xSemaphoreGive(xSemaphore_cylinder1);
				while (xSemaphoreTake(xSemaphore_syncCyl1Sorter, 10000) == pdFALSE) { if (cleared) break; };
				break;
			case 1:
				xSemaphoreGive(xSemaphore_cylinder2);
				break;
			}
		}

		if (brickQueue.empty()) { 
			vTaskDelay(4000);
			xSemaphoreGive(xSemaphore_convOff); 
		}
		while (getCylinder1Sensor());
	}
}
 
void vTask_magSensors(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_magSensor, 10000) == pdFALSE) { continue; }
		
		int mag1 = 0, mag2 = 0;
		while (!getCylinder1Sensor()) {
			mag1 = (mag1 == 0) ? getMagSensor1() : mag1;
			mag2 = (mag2 == 0) ? getMagSensor2() : mag2;
			if (cleared) { break; }
		}

		mag1 = (mag1 != 0) ? 1 : 0;
		mag2 = (mag2 != 0) ? 1 : 0;
		int mag3 = mag1 + mag2;

		xQueueSend(xQueue_brickSorter, &mag3, 0);
		xSemaphoreGive(xSemaphore_syncMagSorter);
	}
}

void vTask_emergencyState (void* pvParameters) {
	while (1) {
		vTaskSuspend(NULL);
		emergencyState = true;
		prevState = readCriticalDigitalU8(2);
		writeCriticalDigitalU8(2, 0);
		vTaskSuspend(initializerTask_handle);
	}
}

void vTask_ledSwitch (void* pvParameters) {
	while (1) {
		while (emergencyState) {
			greenLedOn();
			vTaskDelay(500);
			greenLedOff();
			vTaskDelay(500);
		}
	}
}

void vTask_resumeState(void* pvParameters) {
	while (1) {
		vTaskSuspend(NULL);
		if (!emergencyState) { continue; }
		emergencyState = false;
		writeCriticalDigitalU8(2, prevState);
		vTaskResume(initializerTask_handle);
	}
}

void vTask_clearSystem(void* pvParameters) {
	uInt8 timer = 0;

	while (1) {
		vTaskSuspend(NULL);
		if (!emergencyState) { continue; }
		
		cleared = true;

		initSemphr();
		bricks.clear();
		for (int i = 0; i < 3; ++i) {
			correct[i] = 0;
			regected[i] = 0;
		}

		while (timer < 13) {
			convOn();
			gotoCylinderStart(1);
			gotoCylinderStart(0);

			if (getCylinder1Sensor()) {
				timer = 0;
			}
			vTaskDelay(1000);
			++timer;
		}
		writeCriticalDigitalU8(2, 0);
		emergencyState = false;
		xSemaphoreGive(xSemaphore_calibration);
	}
}

void INT_dockEnd(ULONGLONG lastTime) {
	ULONGLONG time = GetTickCount64();
	printf("\nSwitch three RISING detected at time %llu...\n", time);
	BaseType_t xYieldRequired = xTaskResumeFromISR(clearSystemTask_handle);
}

void INT_dock1(ULONGLONG lastTime) {
	ULONGLONG time = GetTickCount64();
	printf("\nSwitch one RISING detected at time %llu...\n", time);
	BaseType_t xYieldRequired = xTaskResumeFromISR(emergencyTask_handle);
}

void INT_dock2(ULONGLONG lastTime) {
	ULONGLONG time = GetTickCount64();
	printf("\nSwitch two RISING detected at time %llu...\n", time);
	BaseType_t xYieldRequired = xTaskResumeFromISR(resumeTask_handle);
}

void vTask_getc(void* pvParameters) {
	while (1) {
		if (xSemaphoreTake(xSemaphore_getchStart, 10000) == pdFALSE) { continue; }
		userInput = _getch();
		xSemaphoreGive(xSemaphore_getchEnd);
	}
}

void myDaemonTaskStartUpHook() {
	xTaskCreate(vTask_calibration, "vTask_calibration", 100, NULL, 0, NULL);
	xTaskCreate(vTask_convOff, "vTask_convOff", 100, NULL, 0, NULL);
	xTaskCreate(vTask_convOn, "vTask_convOn", 100, NULL, 0, NULL);
	xTaskCreate(vTask_brickWrong, "vTask_brickWrong", 100, NULL, 0, NULL);
	xTaskCreate(vTask_moveCylinderStart, "vTask_moveCylinderStart", 100, NULL, 0, NULL);
	xTaskCreate(vTask_moveCylinder1, "vTask_moveCylinder1", 100, NULL, 0, NULL);
	xTaskCreate(vTask_moveCylinder2, "vTask_moveCylinder2", 100, NULL, 0, NULL);
	xTaskCreate(vTask_brickSorter, "vTask_brickSorter", 100, xQueue_brickSorter, 0, NULL);
	xTaskCreate(vTask_controller, "vTask_controller", 100, NULL, 0, &initializerTask_handle);
	xTaskCreate(vTask_magSensors, "vTask_magSensor", 100, xQueue_brickSorter, 0, NULL);
	xTaskCreate(vTask_printBrincks, "vTask_printBricks", 100, NULL, 0, NULL);
	xTaskCreate(vTask_printRejected, "vTask_printRejected", 100, NULL, 0, NULL);
	xTaskCreate(vTask_getc, "vTask_getc", 100, NULL, 0, NULL);

	xTaskCreate(vTask_ledSwitch, "vTask_ledSwitch", 100, NULL, 0, NULL);
	xTaskCreate(vTask_emergencyState, "vTask_emergencyState", 100, NULL, 0, &emergencyTask_handle);
	xTaskCreate(vTask_resumeState, "vTask_resumeState", 100, NULL, 0, &resumeTask_handle);
	xTaskCreate(vTask_clearSystem, "vTask_clearSystem", 100, NULL, 0, &clearSystemTask_handle);

	attachInterrupt(1, 2, INT_dockEnd, RISING);
	attachInterrupt(1, 3, INT_dock2, RISING);
	attachInterrupt(1, 4, INT_dock1, RISING);

	xSemaphoreGive(xSemaphore_calibration);
}