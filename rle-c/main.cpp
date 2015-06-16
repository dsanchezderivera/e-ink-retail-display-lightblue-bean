#include <stdlib.h>
#include "rle_bmp_compress.cpp"
int main(int argc, char *argv[])
{
/*
   ***     C o m p r e s s e d   b i t m a p     ***
   -------------------------------------------------
        In general, each uncompressed bitmap is aligned
     into 4 byte boundary segment. Segment is always 4
     bytes long [DWORD]. If pixels in raster and their
     reprez. bytes does not form scanline aligned into
     DWORD on their own, then we must add redundant
     padding bytes, until nearest 4 bytes boundary is
     reached. Each scanline is assembled only of 4 bytes
     blocks DWORD.
     So if 8 bit bmp has 8 pixels in wide, then add 2 more
     bytes for padding, because 2 pixels = 2 bytes, next 2
     are needed to close 4 bytes boundary.
     More, if 1, then add three. This addition is only on
     the end of scanline, never anywhere in the middle.
     Simillary for 24 bit bitmaps, although not used here,
     they have RGBTRIPLEts to reprezent pixels. Because 
     their size is 3 bytes, 1 more byte is needed to close
     boundary. But, as always, only on the EOL.
     So if one pixel = 3 bytes R,G,B. We add one more
     to correct alignment, only in case, that
     width * 3 != multipl. of 4. 
     Eg.  [FF FF FF][FF FF FF] = 2 pixels, width in bytes =
     6. Nearest multip. of 4 is 8, add 2 pixels:
          [FF FF FF] [FF FF FF] [00][00]
           |--SEGMENT--| |--SEGMENT---|
     
     Instead, in compresesion, there is no alignment by any means.
     But it stands, that RLE compression can occur in two modes:
     ABSOLUTE and ENCODED.       
     This version of program uses most simple variant: 
        absolute. Always flows 1 byte COUNT, second byte VALUE (index).
        
   8 bit : BYTE count, BYTE index. End of line [00] [00] End of file [00] [01]
   
   4 bit : BYTE count, BYTE 2 indices. Count max = 127, because two pixels are 
   there together. End of line [00] [00] End of file [00] [01]
   
   All bitmaps begins their raster left bottom-most, ending top-most right.
   
   End of file is signed as [00] [01] instead of [00] [00]
*/  
    
    system("cls");
    printf("*** compressor of Win bitmaps, RLE. *** \n"
           "For 4 and 8 bit bitmaps, because it is defined so.\n"
           "Please, use uncompressed bitmaps only\n"
           "(bitmaps are not decompressed here)\n");
    if (argc < 3) {      
      printf("syntax: <bmp input> <bmp output>");
      system("PAUSE");
      return 0;
    }
    RLE_Win_Compress *rlebmp = new RLE_Win_Compress();
      if (!rlebmp->RLE_Compress(argv[1], argv[2])){
        printf("error during compression\n");
        system("PAUSE");
      }
    delete rlebmp;
    return EXIT_SUCCESS;
    
}
