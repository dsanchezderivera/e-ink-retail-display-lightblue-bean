#include <inttypes.h>
#include <avr/power.h>
#include <SPI.h>
#include <avr/wdt.h>

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

const uint8_t HEADER_BYTE_1 = 0x03;
const uint8_t HEADER_BYTE_2 = 0xA0;

const int Pin_EPD_RESET = 0;
const int Pin_EPD_CS = 1;
const int Pin_EPD_BUSY = 2;






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
  Serial.setTimeout(25);
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
      cmdBuffer = cmdBuffer.substring( lineEnd+1, cmdBuffer.length()+1 );

      // now we can do something with the command...

      // hello ... world
      if ( !strncmp( buffer, "hello", 5 ) )
      {
        Serial.println( "< world >" ); 
      }

      // request temperature data
      else if ( !strncmp( buffer, "temp", 4 ) )
      {
        int8_t newTemp = Bean.getTemperature();
        Serial.println( String("< Temperature: ") + String(newTemp) + "c >" );
      }

      // request accelerometer data
      else if ( !strncmp( buffer, "acc", 3 ) )
      {
        int8_t x = Bean.getAccelerationX();
        int8_t y = Bean.getAccelerationY();
        int8_t z = Bean.getAccelerationZ();
        Serial.println( String("< Accelerometer: ") + String(x) +","+ String(y) +","+ String(z) + " >" );
      }

      // setting the LED color
      else if ( !strncmp( buffer, "led", 3 ) )
      {
        char temp[64];

        char *tok = buffer;
        char *param = NULL;
        param = strtok_r( tok, " ", &tok ); // first is the "led" command
        uint8_t r = atoi( strtok_r( tok, " ", &tok ) ); // red
        uint8_t g = atoi( strtok_r( tok, " ", &tok ) ); // green
        uint8_t b = atoi( strtok_r( tok, " ", &tok ) ); // blue

        Bean.setLed( r, g, b );
        oldLed = Bean.getLed();
        Serial.println( String("< LED set to ") + String(r) +","+ String(g) +","+ String(b) + " >" );
      }

      // blink
      else if ( !strncmp( buffer, "blink", 5 ) )
      {
        doBlink = !doBlink;
        if ( doBlink && ( Bean.getLedRed() + Bean.getLedGreen() + Bean.getLedBlue() == 0 ) )
          Bean.setLed( 255, 255, 255 );
        else if ( !doBlink )
          Bean.setLed( 0, 0, 0 );
        oldLed = Bean.getLed();
        Serial.println( String("< Blink ") + (doBlink?"ON":"OFF") + " >" );
      }

      // White -> send white to display
      else if ( !strncmp( buffer, "white", 5 ) )
      {
        Serial.println("Start!");
        sendparameterstodisplay();
        transmitdatatodisplay(0x00);
      }

      // White -> send white to display
      else if ( !strncmp( buffer, "black", 5 ) )
      {
        Serial.println("Start!");
        sendparameterstodisplay();
        transmitdatatodisplay(0xFF);
      }

      // everything else, just echo it
      else
      {
        Serial.println( String("< ") + buffer + " >" ); 
      }
    }
  }

  if ( doBlink )
  {
    blinkCnt = ++blinkCnt % 10;
    if ( blinkCnt == 0 )
    {
      if ( blinkState )
        Bean.setLed( 0, 0, 0 );
      else
        Bean.setLed( oldLed.red, oldLed.green, oldLed.blue );
      blinkState = !blinkState;
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

void sendparameterstodisplay(){
  Serial.println("Sending parameters...");
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
  //Serial.println("Sent!");
}

void transmitdatatodisplay(uint8_t data){
  //Serial.println("Sending display...");
  for(int i=0; i<176; i++){
    for(int j=0; j<16 ; j++){
      //int mult = 2*j;
      SPI_put(data);
      SPI_put(data);
      Delay_us(50);
    }
    SPI_put(data);
    //Last 8 bits
    SPI_put(0x00);
    Delay_ms(1);
  }

  digitalWrite(Pin_EPD_CS, HIGH);
  Serial.println("Display Sent");
  SPI.end();
}