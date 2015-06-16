/* Header for BMP file format        (c) 2010 Jan Mojzis                      */
typedef unsigned short      WORD;
typedef unsigned long       DWORD;
typedef unsigned char       BYTE;
typedef long LONG;
BYTE *_8bppInput;
BYTE *_8bppOutput;
BYTE *_4bppInput;
BYTE *_4bppOutput;
BYTE **outbuffer;
#define packed
typedef struct PALETTEENTRY {
	BYTE peRed;
	BYTE peGreen;
	BYTE peBlue;
	BYTE peFlags;	
#ifdef packed	
}__attribute__((__packed__));
#else
};
#endif
PALETTEENTRY *paletteentries;

typedef struct tagRGBTRIPLE {
  BYTE rgbtBlue;
  BYTE rgbtGreen;
  BYTE rgbtRed;
#ifdef packed	
}__attribute__((__packed__)) RGBTRIPLE;
#else
} RGBTRIPLE;
#endif

typedef struct  tagBITMAPFILEHEADER {
  WORD  bfType;
        /*
            moznosti:
            1. Bm   - windows 3.1x, 95, nt bitmap
            2. Ma   - os/2 bitmap array
            3. Ci   - os/2 color icon
            4. Cp   - os/2 color pointer
            5. Ic   - os/2 icon
            6. Pt   - os/2 pointer
        */
  
  DWORD bfSize;  
  WORD  bfReserved1;
  WORD  bfReserved2;
  DWORD bfOffBits;
#ifdef packed	
} __attribute__((__packed__)) BITMAPFILEHEADER, *PBITMAPFILEHEADER ;
#else
} BITMAPFILEHEADER, *PBITMAPFILEHEADER ;
#endif

typedef struct  tagBITMAPINFOHEADER {
  DWORD biSize;
        /*
             moznosti
             1. 0x28 - windows 3.1, 95, nt
             2. 0x0c - os/2 1.x
             3. 0xf0 - os/2 2.x
             
        */          
  LONG  biWidth;  /* horizontal width */
  LONG  biHeight; /* vertical height */
  WORD  biPlanes;
  WORD  biBitCount; /* bits per pixel */
        /*
             choices
             1 - monochrome
             4 - 16 colors
             8 - 256 colors
             16 - 16 bit color bitmap (HIGH COLOR)
             24 - 24 bit color bitmap (TRUE COLOR)
             32 - 32 bit color bitmap (TRUE COLOR)             
        */  
  DWORD biCompression; /* information about compression used*/
        /*
             choices
             0 - none  (bi_rgb)
             1 - rle 8 bit on pixel (bi_rle8)
             2 - rle 4 bit on pixel (bi_rle4)
             3 - bitfields (bi_bitfields)
        */  
  DWORD biSizeImage;   /* must be rounded towards nearest */
                       /* multipl. of 4 */
  LONG  biXPelsPerMeter;  /* horizontal resolution per meter */         
  LONG  biYPelsPerMeter;  /* vertical resolution per meter */
  DWORD biClrUsed;    /* number of colors count in bitmap, for 8 bit */
                      /* it is 0x100 (256) */
  DWORD biClrImportant; /* the same as above */                        
                        /* when all colors are important */                                                                                
#ifdef packed	
}__attribute__((__packed__))  BITMAPINFOHEADER, *PBITMAPINFOHEADER;
#else
} BITMAPINFOHEADER, *PBITMAPINFOHEADER;
#endif



