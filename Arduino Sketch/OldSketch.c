#include <inttypes.h>
#include <avr/power.h>
#include <SPI.h>
#include <avr/wdt.h>

#define Delay_ms(ms) delay(ms)
#define Delay_us(us) delayMicroseconds(us)

#define LED_ON  HIGH
#define LED_OFF LOW

uint8_t val;
boolean ledon = false;
int state = 0;
uint8_t targetdisplay;
int count = 0;
byte linearray[176][33];
//byte* plinearray;

int chipselect;

uint8_t displaytoshow;

const int Pin_EPD_ON_1 = 5;
const int Pin_EPD_ON_2 = 6;
const int Pin_EPD_RESET = 9;
const int Pin_EPD_CS = 10;
const int Pin_EPD_BUSY = 11;
const int Pin_VCC = 12;

const int Pin_EPD_CS1 = 4;
const int Pin_EPD_CS2 = 3;

const uint8_t HEADER_BYTE_1 = 0x03;
const uint8_t HEADER_BYTE_2 = 0xA0;




int recibirEstado(){
  if(Serial.available() > 0 ){
    val = Serial.read();
    if(val == 1)
      return 1;
    else
      return 2;
  }
  return 0;
}



static void SPI_put(byte c) {
  SPI.transfer(c);
}

void setup(){
  pinMode(Pin_VCC, OUTPUT);
  pinMode(Pin_EPD_CS, OUTPUT);
  pinMode(Pin_EPD_RESET, OUTPUT);
  pinMode(Pin_EPD_ON_1, OUTPUT);
  pinMode(Pin_EPD_ON_2, OUTPUT);
  pinMode(Pin_EPD_BUSY, INPUT);
  digitalWrite(Pin_VCC, HIGH);
  digitalWrite(Pin_EPD_CS, LOW);
  digitalWrite(Pin_EPD_RESET, LOW);
  digitalWrite(Pin_EPD_ON_1, LOW);
  digitalWrite(Pin_EPD_ON_2, LOW);
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  Serial.begin(115200);
  
  pinMode(Pin_EPD_CS1, OUTPUT);
  pinMode(Pin_EPD_CS2, OUTPUT);
  digitalWrite(Pin_EPD_CS1, LOW);
  digitalWrite(Pin_EPD_CS2, LOW);
  
  //plinearray = linearray;

  clock_prescale_set(clock_div_2);
}


void loop(){
  delay(500);
  check_input();
  if(ledon){
    digitalWrite(13, LOW);
    ledon= false;
  }
  else{
    digitalWrite(13, HIGH);
    ledon=true;
  }
}

void check_input(){
  if(Serial.available() > 0 ){
    byte input = Serial.read();
    if(input == 51){
      Serial.write(0xDD);//handshake ok
      state=1;
      setandreceive();
      software_Reboot; 
    }
    else{
      Serial.write(Serial.available());
      digitalWrite(13, HIGH);
      Delay_ms(2000);
    }
  }
}

//void receivedisplay(){
//  for(int i=0; i<176; i++){
//    waitforbytes();
//    //Serial.readBytes((char *)*(plinearray+(33*i)),33);
//     Serial.readBytes((char *)linearray[33*i],33);
//    Serial.write(0xDD);
//  }
//}

void receivedisplay(){
  for(int i=0; i<176; i++){
    for(int j=0; j<33; j++){
      waitforbytes();
      linearray[i][j] = Serial.read();
    }
    Serial.write(0xDD);
  }
}

void setandreceive(){
  displaytoshow = receivebyte();
  Serial.write(0xDD);
  receivedisplay();
  //parpadeo();
  sendparameterstodisplay(displaytoshow);
  state = 2;
  transmitdatatodisplay();
  state = 3;
  wait_transmit();
  Delay_ms(1200);
  powerOFF();
  Serial.write(0xDD);
  state = 0;
  Serial.flush();
}



void transmitdatatodisplay(){
  for(int i=0; i<176; i++){
    for(int j=0; j<16 ; j++){
      //int mult = 2*j;
      SPI_put(linearray[i][2*j]);
      SPI_put(linearray[i][(2*j)+1]);
      Delay_us(50);
    }
    SPI_put(linearray[i][32]);
    //Last 8 bits
    SPI_put(0x00);
    Delay_ms(1);
  }

  Serial.write(0xDD);
}

int waitforbytes(){
  while(true){
    if(Serial.available() > 0)
      return 1;
  }
}

void parpadeo(){
  digitalWrite(13, HIGH);
  Delay_ms(250);
  digitalWrite(13, LOW);
  Delay_ms(250);
  digitalWrite(13, HIGH);
  Delay_ms(250);
  digitalWrite(13, LOW);
  Delay_ms(250);
  digitalWrite(13, HIGH);
  Delay_ms(250);
}

uint8_t receivebyte(){
  while(true){
    if(Serial.available() > 0)
      return Serial.read();
  }
}

void sendparameterstodisplay(uint8_t display){
  digitalWrite(Pin_EPD_CS1, LOW);
  digitalWrite(Pin_EPD_CS2, LOW);
  digitalWrite(Pin_EPD_RESET, LOW);
  digitalWrite(Pin_VCC, LOW);
  Delay_ms(15);
  SPI_on();
  
  digitalWrite(Pin_EPD_CS1, HIGH);
  digitalWrite(Pin_EPD_CS2, HIGH);
  
  
  //Resetea la TCon Board
  if(display == 2){
    chipselect = Pin_EPD_CS2;
    digitalWrite(Pin_EPD_ON_2, HIGH);
  }
  else{
    chipselect = Pin_EPD_CS1;
    digitalWrite(Pin_EPD_ON_1, HIGH);
  }
  digitalWrite(Pin_EPD_RESET, HIGH);
  Delay_ms(3);
  digitalWrite(Pin_EPD_RESET, LOW);
  Delay_ms(3);
  digitalWrite(Pin_EPD_RESET, HIGH);
  Delay_ms(10);

  digitalWrite(chipselect, LOW);
  Delay_ms(1);
  //Header Byte
  SPI_put(HEADER_BYTE_1);
  SPI_put(HEADER_BYTE_2);
  Delay_ms(65);
}

static void SPI_on() {
  SPI.end();
  SPI.begin();
  SPI.setBitOrder(MSBFIRST);
  SPI.setDataMode(SPI_MODE0);
  SPI.setClockDivider(SPI_CLOCK_DIV128);
  Delay_us(10);
}

void wait_transmit(){
  digitalWrite(chipselect, HIGH);
  uint8_t data=0xFF;
  do{
    SPI.transfer(0x00);
    data=SPI.transfer(0x00);
    Serial.println(data,DEC);
    Delay_ms(500);
  }
  while (data != 0x00);
}

void powerOFF(){
  digitalWrite(Pin_EPD_ON_1, LOW);
  digitalWrite(Pin_EPD_ON_2, LOW);
  digitalWrite(Pin_VCC, HIGH);


}

void software_Reboot()
{
  wdt_enable(WDTO_15MS);
  while(1)
  {
  }
}
