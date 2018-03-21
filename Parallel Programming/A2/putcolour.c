/* putcolour.c
* 
* WBG 14-Oct-13
* 
* Implementation of putcolour.h functions
* 
* Control techniques by Andrew Crowell
*/

#include <stdio.h>
#include <string.h>
#include "putcolour.h"

#ifdef _WIN32
#include <windows.h>
#include <conio.h>	
#endif


/* setcolour
* 
* Put stdout into specified colour mode, or if AUTO, reset it to original colours
*/
void setcolour( enum COL_MODE mode )
{
#ifdef _WIN32
    static int initialized = 0;
    static HANDLE WINAPI winout;
    static WORD attr,		// original console attributes
        fblack = 0,
        fwhite = FOREGROUND_RED | FOREGROUND_GREEN | FOREGROUND_BLUE,
        bwhite = BACKGROUND_RED | BACKGROUND_GREEN | BACKGROUND_BLUE,
        bblue = BACKGROUND_BLUE,
        bred = BACKGROUND_RED;

    if ( !initialized) {
        CONSOLE_SCREEN_BUFFER_INFO consoleInfo;
        
        winout = GetStdHandle(STD_OUTPUT_HANDLE);
        GetConsoleScreenBufferInfo( winout, &consoleInfo );
        attr = consoleInfo.wAttributes;		// save for restoration
        initialized = 1;
    }
        
    switch( mode ) {
        case PC_WHITE:
            SetConsoleTextAttribute( winout, fblack | bwhite );
            break;
        case PC_BLUE:
            SetConsoleTextAttribute( winout, fwhite | bblue );
            break;
        case PC_RED:
            SetConsoleTextAttribute( winout, fwhite | bred );
            break;
        case PC_AUTO:
            SetConsoleTextAttribute( winout, attr );
    }
    
#else

    /* Using ANSI escape sequences
    * 
    * Esc[#;...;#m => Set graphics mode, where m's are:
    *  1 bold
    *  30-37 foreground colours (black red ... white)
    *  40-47 background colours
    *  0 all attributes off
    */
    switch( mode ) {
        case PC_WHITE:
            fputs( "\x1B[30;47m", stdout );
            break;
        case PC_BLUE:
            fputs( "\x1B[1;37;44m", stdout );
            break;
        case PC_RED:
            fputs( "\x1B[1;37;41m", stdout );
            break;
        case PC_AUTO:
            fputs( "\x1B[0m", stdout );	// "normal" = all attributes off
    }
    
#endif
}


void putcolour( enum COL_MODE mode, const char *buff, int blen )
{
        int i;

    if ( blen < 0 ) blen = strlen( buff );
    if ( blen == 0 ) return;


    /* deal with AUTO mode first, switching colour for each character */
    if ( mode == PC_AUTO ) {
        enum COL_MODE lastChar = PC_AUTO,	// here means no colour established yet
            thisChar;
        
        for ( i=0; i<blen; i++ ) {
            thisChar = buff[i] == 'V' ? PC_BLUE : ( buff[i] == '>' ? PC_RED : PC_WHITE );
            if ( thisChar != lastChar ) setcolour( thisChar );
            putchar( buff[i] );
        }
    }
    
    /* switch colour once for whole buffer */
    else {
        setcolour( mode );
        for ( i=0; i<blen; i++ ) {
            putchar( buff[i] );
        }
    }
    
    /* restore previous colours */
    setcolour( PC_AUTO );
}
