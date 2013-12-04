/* * This file is part of LightSpeak. * *
     Copyright © 2013 Nokia Corporation and/or its subsidiary(-ies). All rights reserved. * 
     Contact: Pranav Mishra <pranav.mishra@nokia.com> * * 
     This software, including documentation, is protected by copyright controlled by Nokia Corporation.
     All rights are reserved. Copying, including reproducing, storing, adapting or translating,
     any or all of this material requires the prior written consent of Nokia Corporation.
     This material also contains confidential information which may not be disclosed to others 
     without the prior written consent of Nokia. */

// For more info about LightSpeak, refer to
// https://research.nokia.com/lightspeak
// 
// This code generates the coded pattern on outPin of the Arduino Board.
// The Duration of each bit is set that it covers four rows in the 


#include<avr/io.h>
#include<avr/interrupts.h>

//unsigned int Duration = 157 * 10/3;// Lumia 1020 capture mode
unsigned int Duration = 81;// Lumia 1020 preview mode
//unsigned int Duration = 157; // Lumia 920 capture mode
//unsigned int Duration = 157/2; // Lumia 920 preview mode
const int outPin1 = 12;
const int outPin2 = 11;
boolean enable = 0;
int i=1;
int countVal;//countValue = 16*Duration
int lightcode1[16] = {10,1,0,0,0,0,0,0,0,0,0,0,0,0,0};  // First bit defines the length of repeated code pattern. Subsequent bits define the pattern being generated.
// {3, 1, 0, 0,  ... } => 100100100100100100 is generated
// {4, 1, 0, 0, 0, ... } => 100010001000100010001000 is generated

void setup() {
// initialize the digital pin as an output.
  countVal = 16*Duration;
  sei();
  pinMode(outPin1, OUTPUT);
  pinMode(outPin2, OUTPUT); 
  digitalWrite(outPin1,LOW);
  digitalWrite(outPin2,LOW);
  delay(1000);
  //Timer1
  //enable
  PRR = 0;
  TIMSK1 = 0;
  TIMSK1 = (1 << OCIE1A);
  //set modes and prescales
  TCCR1A = 0;
  TCCR1B = 0;
  TCCR1B = TCCR1B | (1 << CS10) | (1 << WGM12);
  //set TOP/MAX for Output compare
  OCR1A = countVal;
}

void loop(){}

ISR(TIMER1_COMPA_vect){
      //for 1
      enable = lightcode1[i];
      if(enable == 1) {
        digitalWrite(outPin1, HIGH);
        digitalWrite(outPin2, HIGH);
      }
      else {
        digitalWrite(outPin1, LOW);
        digitalWrite(outPin2, LOW);
      }
      if(i == lightcode1[0])        i = 1;
      else        i=i+1;
}
