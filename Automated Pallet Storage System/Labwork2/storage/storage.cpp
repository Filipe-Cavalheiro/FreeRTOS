#include <stdio.h>
#include <conio.h>
#include <stdio.h>

extern"C"{
#include <interface.h>
}

int main(){
	printf("Welcome to the Compact Storage Application\n");
	printf("press key");
	createDigitalInput(0);
	createDigitalInput(1);
	createDigitalOutput(2);
	printf("\nPress a key to start");
	_getch();
	writeDigitalU8(2, 0x04);
	printf("\nPress a key to finish");
	_getch();
	closeChannels();
	return 0;
}
