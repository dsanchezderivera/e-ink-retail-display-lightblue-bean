/*
  Serial Interactivity Test

  Reads input from the serial port and looks for commands:
      hello
      temp
      acc
      led # # #
      blink

   Anything else sent is just echoed back. For the "led" command,
   give it zero to three numbers between 0 and 255 for the
   red, green, and blue values of the led.

   To use:
       1) upload the program to the Bean as usual
       2) right-click the Bean in the Loader and select "Use for Virtual Serial"
       3) in the Arduino IDE, select Tools -> Serial Port -> "/tmp/tty.LightBlue-Bean"
       4) in the Arduino IDE, select Tools -> Serial Monitor
       5) at the bottom of the serial window that opens, select "57600 baud"
       6) at the bottom of the serial window, select either "Both NL & CR" or "newline"
       7) type commands in the top of the serial window (try: "hello" first)

   by: Chris Innanen (aka Nonsanity)
 */

 // this holds all serial input until a full command is in it
String cmdBuffer;

// for the blink command
bool doBlink = false;
bool blinkState = true;
int blinkCnt = 0;
LedReading oldLed;


// the setup routine runs once
void setup()
{
  // initialize serial communication at 57600 bits per second:
  Serial.begin(57600);

  // on readBytes, return after 25ms or when the buffer is full
  Serial.setTimeout(25);
}


// the loop routine runs over and over again forever:
void loop()
{
  // this is the short-term buffer that gets added to the cmdBuffer
  char buffer[64];
  size_t length = 64; 

  // read as much as is available
  length = Serial.readBytes( buffer, length-1 );

  // null-terminate the data so it acts like a string
  buffer[length] = 0;

  // if we have data, so do something with it
  if ( length > 0 )
  {
    // stick it to the end of the main buffer
    cmdBuffer += buffer;

    // find the end of the command input (a new line character)
    size_t lineEnd = cmdBuffer.indexOf( "\n" );

    // if there IS a new line character, then...
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