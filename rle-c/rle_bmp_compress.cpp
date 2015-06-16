/*   RLE compressor for Windows Bitmaps "BM"     v.1.0   (c) 2010 Jan Mojzis   */
/*                                                                             */
/*   This is RLE bitmap compressor for Windows bitmaps, it performs compression*/
/*   and does not performs decompression                                       */
/*   RLE compression removes repeats, but patterns (2 byte) are not affected   */
/*   RLE Windows Bitmap compression is strictly defined:                       */
/*   1. There are only 4 and 8 bit bitmaps that can have RLE compression       */
/*   2. uncompressed bitmaps 4 and 8 bits ALWAYS use 4 bytes boundary, they    */
/*      have to be aligned on (their scanlines, vertical bit flow)             */
/*   3. compressed bitmaps, on the other hand, does not include anything like  */
/*      boundary (experimentally proved), but have to include 2 extra bytes at */
/*      the end of each scanline                                               */
/*     A) EOL - end of line: 0x00 0x00                                         */
/*     B) EOF - end of file: 0x00 0x01                                         */
/*     End of line is appended by 0x00 0x00                                    */
/*     End of file, additionally, appends with 0x00 0x01                       */
/*     compressed stream is a chain of blocks BYTE - number of runs, BYTE -    */
/*     PIXEL(s - 2= for 4 bit bitmaps) immediately after line, EOL is present, */
/*     optionally (when no more scanlines) followed by EOF sign immediately    */
/*   4. compressed bitmap's file size is never known before compression, so    */
/*      whole compression is in-memory, after compression size can be written  */
/*      into header                                                            */
/*   5. all windows bitmaps that are 4,8 bits can be compressed in ABSOLUTE or */
/*      ENCODED mode. I think, that this implementation uses Encoded Mode      */ 
/*                                                                             */  
/*   Notes:                                                                    */
/*      x many unreleased versions, this is the last, no bugs are knows        */
/*      x most common errors occurs on padding, which, when incorrectly deter- */
/*        mined, errors are growing geometrically                              */
/*      x can be adjusted with ease for other bmp formats as well (suppose),   */
/*        but since OS/2 is no longer supported by MS, OS/2 format is useless  */
/*        and moreover, old OS/2 V.1.0 does not support RLE                    */
/*      x even no error, and given that Gimp, PBrush, and Lister for TC        */
/*        4 bit RLE correctly, MS Photoeditor gives errorous pixels near the   */
/*        EOL                                                                  */
#include  <stdio.h>
#include "bmp.h"

class RLE_Win_Compress{
  public:
    bool RLE_Compress(char *input, char *output);
};

bool RLE_Win_Compress::RLE_Compress(char *input, char *output){
    FILE *f;
    f = fopen(input,"rb");
    if (!f)
       return false;
    FILE *fout = fopen(output, "wb");
    if (!fout){
       fclose(f);
      return false;
    }
    
    fseek(f, 0L, SEEK_END);    
    int delka = ftell(f);
    int filesize;
    int headersize;
    fseek(f, 0L, SEEK_SET);
    
    BITMAPFILEHEADER bitmapfileheader;
    fread(&bitmapfileheader, sizeof(BITMAPFILEHEADER), 1, f);

    BITMAPINFOHEADER bitmapinfoheader;
    fread(&bitmapinfoheader, sizeof(BITMAPINFOHEADER), 1, f);
    if ((bitmapinfoheader.biSize != 0x28) || (bitmapfileheader.bfType != 0x4d42) )
       return false;               // others than windows bitmaps are not supported
    if (bitmapinfoheader.biBitCount == 24)
       return false;               // 24 bit bitmaps cannot be compressed
    if (bitmapinfoheader.biCompression != 0)
       return false;               // only uncompressed bitmaps may be compressed
    
    // rle 8 bits
    if ((bitmapinfoheader.biBitCount == 8) && (bitmapinfoheader.biCompression == 0)){
       PALETTEENTRY *paletteentries;
       paletteentries = new PALETTEENTRY[ 256 ];
       fread(paletteentries, sizeof(PALETTEENTRY), 256, f);
       int LineWidth = bitmapinfoheader.biWidth;
       int InputWidth = LineWidth;
       int OutputWidth = 2*bitmapinfoheader.biWidth;
       int padding = (bitmapinfoheader.biWidth % 4);
       int padding2;
       while((LineWidth % 4) != 0)
          LineWidth++;
       BYTE *Input = new BYTE[LineWidth];       
       int *LineLengths = new int[bitmapinfoheader.biHeight];
       int *Paddings = new int[bitmapinfoheader.biHeight];
       BYTE **Output = (BYTE **) malloc( bitmapinfoheader.biHeight * sizeof(BYTE *));
       BYTE pixel;
       for (int i = 0; i < bitmapinfoheader.biHeight; i++){
           Output[i] = (BYTE *) malloc(((bitmapinfoheader.biWidth*2)+8) * sizeof(BYTE *));
           Output[i][bitmapinfoheader.biWidth+1] = 0; // for EOL
           Output[i][bitmapinfoheader.biWidth+2] = 0; // or EOF
           Output[i][bitmapinfoheader.biWidth+3] = 0; // I am not sure whether is this necessary
           Output[i][bitmapinfoheader.biWidth+4] = 0;
           Output[i][bitmapinfoheader.biWidth+5] = 0;
           Output[i][bitmapinfoheader.biWidth+6] = 0;
           Output[i][bitmapinfoheader.biWidth+7] = 0;
           LineLengths[i] = 0;
       }  
       int filesize = (sizeof(PALETTEENTRY)*256) + sizeof(bitmapfileheader) + sizeof(bitmapinfoheader);
       int headersize = filesize;
       register int Y, X;
       int j;
       int cnt;
       bool uncompressed;
       if (padding != 0) padding = 4-padding;
       for (Y = 0; Y < bitmapinfoheader.biHeight; Y++){
           fread(Input, bitmapinfoheader.biWidth, 1, f);
           for (X = 0; X < padding; X++)
              fgetc(f);  // padding bytes, can be 0 or anythyng, do not test for 0
           uncompressed = true;
           X = 0;
           j = 0;
           LineLengths[Y] =  0;
           while(uncompressed)
           {
             cnt = 0;
             pixel = Input[j]; // 1 pixel
             while ( (j < InputWidth) && (pixel == Input[j]) && (cnt < 255)){
                j++;
                cnt++;
             }
            if (j == InputWidth)
              uncompressed = false;
            Output[Y][X] = cnt;
            X++;
            Output[Y][X] = pixel;
            X++;
            LineLengths[Y] = LineLengths[Y] +2;
            filesize = filesize +2;                           
           }
           Output[Y][X] = 0x00;
           if (Y ==  bitmapinfoheader.biHeight-1)
              Output[Y][X+1] = 0x01;
           else  
              Output[Y][X+1] = 0x00;
          LineLengths[Y]+=2; // EOL 
          filesize+=2;         
        }
        bitmapinfoheader.biCompression =1; // set up header
        bitmapfileheader.bfSize = filesize;
        bitmapinfoheader.biSizeImage = filesize - headersize;
        // write headers
        fwrite(&bitmapfileheader, sizeof(bitmapfileheader), 1, fout);
        fwrite(&bitmapinfoheader, sizeof(bitmapinfoheader), 1, fout);
        
        fwrite(paletteentries, sizeof(PALETTEENTRY), 256, fout);
        for (Y = 0; Y < bitmapinfoheader.biHeight; Y++){          
             fwrite(Output[Y], LineLengths[Y], 1, fout); // write compressed stream
        }       
       for (int i = 0; i < bitmapinfoheader.biHeight; i++)
          free(Output[i]);
       free(Output);
       delete Input;
       delete Paddings;
       delete LineLengths;
       delete paletteentries;             
    }
    // rle 4 bits
    if ((bitmapinfoheader.biBitCount == 4) && (bitmapinfoheader.biCompression == 0)){
       PALETTEENTRY *paletteentries;
       paletteentries = new PALETTEENTRY[  16 ];
       fread(paletteentries, sizeof(PALETTEENTRY),16, f);
       int LineWidth = ((bitmapinfoheader.biWidth/2) + (bitmapinfoheader.biWidth%2));
       int InputWidth = LineWidth;
       int padding = LineWidth % 4;
       BYTE *Input = new BYTE[LineWidth];       
       int *LineLengths = new int[bitmapinfoheader.biHeight];
       BYTE **Output = (BYTE **) malloc( bitmapinfoheader.biHeight * sizeof(BYTE *));
       BYTE pixel;
       for (int i = 0; i < bitmapinfoheader.biHeight; i++){
          Output[i] = (BYTE *) malloc(((bitmapinfoheader.biWidth*2)+8) * sizeof(BYTE *));
          Output[i][bitmapinfoheader.biWidth+1] = 0;  // for EOL
          Output[i][bitmapinfoheader.biWidth+2] = 0;  // or EOF
          Output[i][bitmapinfoheader.biWidth+3] = 0;  // I am not sure whether is this necessary
          Output[i][bitmapinfoheader.biWidth+4] = 0;
          Output[i][bitmapinfoheader.biWidth+5] = 0;
          Output[i][bitmapinfoheader.biWidth+6] = 0;
          Output[i][bitmapinfoheader.biWidth+7] = 0;
          LineLengths[i] = 0;
       }   
       int filesize = (sizeof(PALETTEENTRY)*16) + sizeof(bitmapfileheader) + sizeof(bitmapinfoheader);
       int headersize = filesize;
       register int Y, X;
       int j;
       int padding2;
       int cnt;
       bool uncompressed;
       if (padding != 0) padding = 4-padding;
       for (Y = 0; Y < bitmapinfoheader.biHeight; Y++){
           fread(Input, /*bitmapinfoheader.biWidth*/LineWidth, 1, f);
           for (X = 0; X < padding; X++)
                 fgetc(f); // here too, padding can be anything from 0
           uncompressed = true;
           X = 0;
           j = 0;
           LineLengths[Y] =  0;
           while(uncompressed){
             cnt = 0;
             pixel = Input[j]; // 2 pixels
             while ( (j < InputWidth) && (pixel == Input[j]) && (cnt < 127)){
                j++;
                cnt+=2;
             }
            if (j == InputWidth)
              uncompressed = false;
            Output[Y][X] = cnt;
            X++;
            Output[Y][X] = pixel;
            X++;
            LineLengths[Y] = LineLengths[Y] +2;
            filesize = filesize +2;                           
           }
           Output[Y][X] = 0x00;   // EO...
           if (Y ==  bitmapinfoheader.biHeight-1)
              Output[Y][X+1] = 0x01; // ...F
           else  
              Output[Y][X+1] = 0x00; // ...L             
          LineLengths[Y]+=2; // EOL 
          filesize+=2;
        }
        bitmapinfoheader.biCompression =2;
        bitmapfileheader.bfSize = filesize;
        bitmapinfoheader.biSizeImage = filesize - headersize;
        fwrite(&bitmapfileheader, sizeof(bitmapfileheader), 1, fout);
        fwrite(&bitmapinfoheader, sizeof(bitmapinfoheader), 1, fout);        
        fwrite(paletteentries, sizeof(PALETTEENTRY), 16, fout);
        for (Y = 0; Y < bitmapinfoheader.biHeight; Y++){
              fwrite(Output[Y], LineLengths[Y], 1, fout);                    
        }   
       for (int i = 0; i < bitmapinfoheader.biHeight; i++)
          free(Output[i]);
       free(Output);
       delete Input;
       delete LineLengths;
       delete paletteentries;
    }        
    fclose(f);
    fclose(fout);
    return true;                    
}
