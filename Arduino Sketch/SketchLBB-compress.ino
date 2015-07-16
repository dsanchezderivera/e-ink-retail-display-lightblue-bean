#include <inttypes.h>
#include <avr/power.h>
#include <SPI.h>
#include <avr/wdt.h>
#include <stdlib.h>

#define Delay_ms(ms) delay(ms)
#define Delay_us(us) delayMicroseconds(us)

#define LED_ON  HIGH
#define LED_OFF LOW

// this holds all serial input until a full command is in it
String cmdBuffer;
// for the blink command
bool doBlink = false;
bool blinkState = true;
int blinkCnt = 0;
LedReading oldLed;

long size = 0;

const uint8_t HEADER_BYTE_1 = 0x03;
const uint8_t HEADER_BYTE_2 = 0xA0;

const int Pin_EPD_RESET = 0;
const int Pin_EPD_CS = 1;
const int Pin_EPD_BUSY = 2;

char defaultArray[2300];
char lineArray[33];

int count=0;
int n;

int compressByteIndex = 0;
int decompressBitIndex= 0;
int actualBitLenght = 0;
char compressByte;
bool black = false;


static void SPI_put(byte c) {
  SPI.transfer(c);
}

void setup()
{

  pinMode(Pin_EPD_CS, OUTPUT);
  pinMode(Pin_EPD_RESET, OUTPUT);
  //pinMode(Pin_EPD_BUSY, INPUT);

  digitalWrite(Pin_EPD_RESET, LOW);
  digitalWrite(Pin_EPD_CS, LOW);

  // initialize serial communication at 57600 bits per second:
  Serial.begin(57600);

  // on readBytes, return after 25ms or when the buffer is full
  Serial.setTimeout(300);
}


void loop()
{
  char buffer[64];
  size_t length = 64; 
  length = Serial.readBytes( buffer, length-1 );
  buffer[length] = 0;
  if ( length > 0 )
  {
    cmdBuffer += buffer;
    size_t lineEnd = cmdBuffer.indexOf( "\n" );
    if ( lineEnd > 0 )
    {
      // copy off the command, reusing our buffer variable
      cmdBuffer.substring( 0, lineEnd ).toCharArray( buffer, 64 );

      // and remove it from the command buffer
      //cmdBuffer = cmdBuffer.substring( lineEnd+1, cmdBuffer.length()+1 );
      cmdBuffer = "";
      // now we can do something with the command...

      // hello ... world
      if ( !strncmp( buffer, "hello", 5 ) )
      {
        Serial.println( "< world >" ); 
      }
      // White -> send white to display
      else if ( !strncmp( buffer, "white", 5 ) )
      {
        Serial.println("Start!");
        sendparameterstodisplay(false);
        transmitdatatodisplay(0x00,176);
      }

      // White -> send white to display
      else if ( !strncmp( buffer, "black", 5 ) )
      {
        Serial.println("Start!");
        sendparameterstodisplay(false);
        transmitdatatodisplay(0xFF, 176);
      }
      // Image -> send Image to display
      else if ( !strncmp( buffer, "image", 5 ) )
      {
        Delay_ms(500);
        Serial.println("image command received... starting");
        Serial.flush();
        serialFlush();
        Serial.print(0);
        Serial.flush();
        receiveLine();
        sendparameterstodisplay(true);
        transmitdatatodisplay(0x0F, 5);
      }
      // Compress  Image -> send compress Image to display
      else if ( !strncmp( buffer, "cmage", 5 ) )
      {
        Delay_ms(500);
        Serial.println("cmage command received... waiting for image...");
        Serial.flush();
        serialFlush();
        Serial.print("data");
        Serial.flush();
        receiveData();
        receivePacket(size);

        sendparameterstodisplay(false);
        serialFlush();
        transmitcompressdatatodisplay();
      }
      // everything else, just echo it
      else
      {
        Serial.println( String("< ") + buffer + " >" ); 
      }
    }
  }
  
  Bean.sleep(50);
}


static void SPI_on() {
  SPI.end();
  SPI.begin();
  SPI.setBitOrder(MSBFIRST);
  SPI.setDataMode(SPI_MODE0);
  SPI.setClockDivider(SPI_CLOCK_DIV128);
  Delay_us(10);
}

void serialFlush(){
  while(Serial.available()) {
    Serial.read();
  }
}

/*int waitforbytes(){
  while(true){
    if(Serial.available() > 30)
      return 1;
  }
}*/
  
  void receiveLine(){
    Serial.readBytes((char*)defaultArray,  33);
  }

  void receiveData(){
    while (!Serial.available()) ;
    size = Serial.parseInt();
    Serial.println("size received");
  }

  void receivePacket(long sizelong){
    while (count < sizelong) {
      n = Serial.readBytes((char*)defaultArray+count, sizelong-count);
      if (n == 0) {
      while (!Serial.available()) ; // wait
    }
    count = count + n;
  }
  Serial.println("packet received");
}

void receiveUncompressPacket(){
  while (count < 1155) {
    n = Serial.readBytes((char*)defaultArray+count, 1155-count);
    if (n == 0) {
      while (!Serial.available()) ; // wait
    }
    count = count + n;
  }
  Serial.println("packet received");
}

void sendparameterstodisplay(bool isImage){
  //Resetea la TCon Board
  digitalWrite(Pin_EPD_CS, LOW);
  digitalWrite(Pin_EPD_RESET, LOW);
  Delay_ms(10);
  digitalWrite(Pin_EPD_CS, HIGH);
  digitalWrite(Pin_EPD_RESET, HIGH);
  Delay_ms(5);
  digitalWrite(Pin_EPD_RESET, LOW);
  Delay_ms(5);
  digitalWrite(Pin_EPD_RESET, HIGH);
  Delay_ms(19);

  digitalWrite(Pin_EPD_CS, LOW);
  SPI_on();
  Delay_ms(1);
  //Header Byte
  SPI_put(HEADER_BYTE_1);
  SPI_put(HEADER_BYTE_2);
  Delay_ms(125);
  if(isImage){
    for(int j=0; j<16 ; j++){
      SPI_put(defaultArray[2*j]);
      SPI_put(defaultArray[(2*j)+1]);
      Delay_us(50);
    }
    SPI_put(defaultArray[32]);
    //Last 8 bits
    SPI_put(0x00);
    Delay_ms(1);
  }
}

void transmitdatatodisplay(uint8_t data, int lines){
  for(int i=1; i<lines+1; i++){
    Serial.print(i);
    Serial.flush();
    receiveUncompressPacket();
    for(int i=0; i<35; i++){
      for(int j=0; j<16 ; j++){
        //int mult = 2*j;
        SPI_put(defaultArray[(2*j)+(33*i)]);
        SPI_put(defaultArray[(2*j)+(33*i)+1]);
        Delay_us(50);
      }
      SPI_put(defaultArray[(32)+(33*i)]);
      //Last 8 bits
      SPI_put(0x00);
      Delay_ms(1);
    }
   // serialFlush();
  }

  digitalWrite(Pin_EPD_CS, HIGH);
  Serial.println("Display Sent");
  SPI.end();
  Delay_ms(250);
  serialFlush();
}

void transmitcompressdatatodisplay(){
  int lines = 0;
  int bytesLine = 0;
  int byteComplete = 0;
  compressByteIndex = 0;
  char newByte1 = 0;
  char newByte2 = 0;
  char newByte3 = 0;
  for(int i=0; i<176; i++){ //lineas para pintar la pantalla completa
    for(int j=0; j<16 ; j++){ //bytes por linea
      //Construimos los bytes a enviar
      newByte1 = buildByte();
      newByte2 = buildByte();
      //Enviamos de dos en dos
      SPI_put(newByte1);
      SPI_put(newByte2);
      Delay_us(50);
    }
    //por ultimo el tercero
    newByte3 = buildByte();
    SPI_put(newByte3);
    //Last 8 bits
    SPI_put(0x00);
    Delay_ms(1);
  }
  //Acabamos....
  digitalWrite(Pin_EPD_CS, HIGH);
  Serial.println("Display Sent");
  SPI.end();
  Delay_ms(250);
}




bool nextBit(){
  //Analizamos cada byte comprimido para extraer longitud  y color
  if(decompressBitIndex > actualBitLenght){ //si hemos acabado con la tirada de bits actual
    decompressBitIndex = 0; //reiniciamos tirada actual
    compressByteIndex = compressByteIndex + 1; //incrementamos para coger nuevo byte
    compressByte = defaultArray[compressByteIndex]; //cogemos nuevo byte comprimido
    //Cogemos color y longitud de la nueva tirada
    if(compressByte > 127){
      black = true;
      actualBitLenght = compressByte - 127;
    }else{
      black = false;
      actualBitLenght = compressByte;
    }
  }
  decompressBitIndex = decompressBitIndex + 1;
  return black;
}

char buildByte(){
  //creamos un byte nuevo y modificamos los bits individuales
  char newByte = 0;
  for (int i = 0; i < 8; i++){
    if (nextBit()){ //false = blanco, true = negro
      newByte |= 1 << i;
    }
  }
  return newByte;
}
